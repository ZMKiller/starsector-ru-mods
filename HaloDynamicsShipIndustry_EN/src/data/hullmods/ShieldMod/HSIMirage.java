package data.hullmods.ShieldMod;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.util.IntervalUtil;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class HSIMirage extends HSIBaseShieldModEffect {

    public static final int HIT_ACCUMULATE = 600;
    public static final int EXTRA_SHIELD = 300;

    public static final float TIME_IMMUNE = 1f;

    public static final int MIRAGE_PER_RATION = 25;

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getRenderData().setBlockRender(true);
    }

    public float processShieldEffectBeforeShieldProcess(float baseDamage, HSITurbulanceShieldListenerV2.ParamType type, DamageAPI damage, Vector2f point, ShipAPI ship) {
        if(type.equals(HSITurbulanceShieldListenerV2.ParamType.BEAM)||type.equals(HSITurbulanceShieldListenerV2.ParamType.ARC)||type.equals(HSITurbulanceShieldListenerV2.ParamType.SHIP)) {
            if (ship.hasListenerOfClass(HSIMirageListener.class)) {
                HSIMirageListener l = ship.getListeners(HSIMirageListener.class).get(0);
                l.accumulate(baseDamage);
            } else {
                HSIMirageListener l = new HSIMirageListener(ship);
                ship.addListener(l);
                l.accumulate(baseDamage);
            }
        }
        return baseDamage;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new HSIMirageListener(ship));
    }


    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return ""+HIT_ACCUMULATE;
        if(index == 1) return ""+(int)TIME_IMMUNE+HSII18nUtil.getHullModString("HSIUnitSec");
        if(index == 2) return ""+EXTRA_SHIELD;
        if(index == 3) return ""+MIRAGE_PER_RATION+"%";
        if(index == 4) return HSII18nUtil.getHullModString("HSIMatrixTargetingSelfBonus");
        if(index == 5) return "0";
        return super.getDescriptionParam(index, hullSize);
    }

    public static class HSIMirageListener implements AdvanceableListener, HullDamageAboutToBeTakenListener{

        private ShipAPI ship;
        private final float MAX_HITPOINTS;

        private float accumulate = 0;

        private float timeLeft = 0;

        public static final String KEY = "HSI_Mirage_Effect";

        protected HSITurbulanceShieldListenerV2 shield = null;

        private boolean TEXT_ONCE = false;

        public HSIMirageListener(ShipAPI ship){
            this.ship = ship;
            MAX_HITPOINTS = ship.getMaxHitpoints();
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
        }
        private static final Color TEXT_COLOR = new Color(155,155,155,255);

        private IntervalUtil timer = new IntervalUtil(0.1f,0.2f);
        @Override
        public void advance(float amount) {
            timeLeft = Math.max(0,timeLeft-amount);
            if(timeLeft>0){
                if(shield!=null) shield.getStats().getShieldEffciency().modifyMult(KEY,0);
                ship.getMutableStats().getHullDamageTakenMult().modifyMult(KEY,0);
                ship.setAlphaMult(0.6f);
                if(!TEXT_ONCE){
                    ship.getFluxTracker().showOverloadFloatyIfNeeded(
                            HSII18nUtil.getHullModString("HSIMirageTriggered"), TEXT_COLOR, 1.5f, true);
                    TEXT_ONCE = true;
                }
            }else{
                if(shield!=null) shield.getStats().getShieldEffciency().unmodify(KEY);
                ship.getMutableStats().getHullDamageTakenMult().unmodify(KEY);
                ship.setAlphaMult(1f);
                TEXT_ONCE = false;
            }
            timer.advance(amount);
            if(timer.intervalElapsed()) {
                for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
                    if (ship.equals(proj.getSource())) continue;
                    if (proj.getCustomData().containsKey("HSIMirageCounted")) continue;
                    if (proj.getOwner() == ship.getOwner() && (proj.getCollisionClass().equals(CollisionClass.PROJECTILE_NO_FF) || proj.getCollisionClass().equals(CollisionClass.MISSILE_NO_FF) || proj.getCollisionClass().equals(CollisionClass.PROJECTILE_FIGHTER)))
                        continue;
                    if (MathUtils.getDistanceSquared(proj.getLocation(), ship.getLocation()) <= 40000) {
                        accumulate(Math.max(proj.getDamage().getBaseDamage(), proj.getDamage().getDamage()));
                    }
                    proj.setCustomData("HSIMirageCounted", true);
                }
            }
        }

        private float hp_hit = 0;

        private  int p = 0;

        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float amount) {
            hp_hit+=amount;
            if(hp_hit>=(MIRAGE_PER_RATION/100f)*MAX_HITPOINTS&&p<5){
                hp_hit-=(MIRAGE_PER_RATION/100f)*MAX_HITPOINTS;
                timeLeft+=2f*TIME_IMMUNE;
                p++;
                if(shield!=null) shield.getShield().addExtraShield(2f*EXTRA_SHIELD);
                return false;
            }
            return false;
        }

        public void accumulate(float damage){
            if(timeLeft<=0&&shield!=null&&shield.getShield().getExtra()<=0){
                this.accumulate+=damage;
                if(accumulate>=HSIMirage.HIT_ACCUMULATE){
                        timeLeft += TIME_IMMUNE;
                        accumulate -= HIT_ACCUMULATE;
                        if (shield != null) shield.getShield().addExtraShield(EXTRA_SHIELD);
                }
            }
        }
    }
}