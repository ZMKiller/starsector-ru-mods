package data.hullmods.WeaponMod;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.kit.HSII18nUtil;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class HSIProtocalSuppression extends HSIBaseWeaponModEffect{
    protected static Map<ShipAPI.HullSize,Float> DAMAGE_BUFF = new HashMap<>();
    static {
        DAMAGE_BUFF.put(ShipAPI.HullSize.FIGHTER,1f);
        DAMAGE_BUFF.put(ShipAPI.HullSize.FRIGATE,1f);
        DAMAGE_BUFF.put(ShipAPI.HullSize.DESTROYER,2f);
        DAMAGE_BUFF.put(ShipAPI.HullSize.CRUISER,3f);
        DAMAGE_BUFF.put(ShipAPI.HullSize.CAPITAL_SHIP,4f);
    }

    protected static final float SMOD_DAMAGE_BUFF = 2f;

    protected static final float DISTANCE = 1000f;

    protected static final int MAX_LEVEL = 2;

    public static class HSIProtocalSuppressionBuff implements AdvanceableListener{
        private TimeoutTracker<ShipAPI> sources = new TimeoutTracker<>();

        private TimeoutTracker<ShipAPI> SModSource=  new TimeoutTracker<>();
        private ShipAPI ship;

        public HSIProtocalSuppressionBuff(ShipAPI ship){
            this.ship = ship;
        }

        public static HSIProtocalSuppressionBuff getInstance(ShipAPI ship){
            if(ship.hasListenerOfClass(HSIProtocalSuppressionBuff.class)){
                return ship.getListeners(HSIProtocalSuppressionBuff.class).get(0);
            }else{
                HSIProtocalSuppressionBuff buff = new HSIProtocalSuppressionBuff(ship);
                ship.addListener(buff);
                return buff;
            }
        }
        @Override
        public void advance(float amount) {
            sources.advance(amount);
            if(sources.getItems().isEmpty()){
                ship.getMutableStats().getShieldDamageTakenMult().unmodify("HSI_ProtocalSuppression_Buff");
                ship.getMutableStats().getHullDamageTakenMult().unmodify("HSI_ProtocalSuppression_Buff");
                ship.getMutableStats().getArmorDamageTakenMult().unmodify("HSI_ProtocalSuppression_Buff");
                ship.removeListener(this);
                return;
            }
            float b1 =0,b2 = 0;
            for(ShipAPI s:sources.getItems()){
                if(b1>=4&&b2>=4) break;
                boolean alreadyCounted = false;
                if(DAMAGE_BUFF.get(s.getHullSize())>b1){
                    b1 = DAMAGE_BUFF.get(s.getHullSize());
                    alreadyCounted = true;
                }
                if(!alreadyCounted){
                    if(DAMAGE_BUFF.get(s.getHullSize())>b2){
                        b2 = DAMAGE_BUFF.get(s.getHullSize());
                    }
                }
            }
            float bs = 0;
            for(ShipAPI s:SModSource.getItems()){
                bs+=DAMAGE_BUFF.get(s.getHullSize())*0.5f;
            }
            float buff = b1+b2+bs;
            ship.getMutableStats().getShieldDamageTakenMult().modifyMult("HSI_ProtocalSuppression_Buff",1f+buff/100f);
            ship.getMutableStats().getHullDamageTakenMult().modifyMult("HSI_ProtocalSuppression_Buff",1f+buff/100f);
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("HSI_ProtocalSuppression_Buff",1f+buff/100f);
            if(ship == Global.getCombatEngine().getPlayerShip()){
                addStatus(buff);
            }
        }

        protected void addStatus(float buff) {
            String content = HSII18nUtil.getHullModString("HSIProtocalSuppressionContent")+String.format("%.1f",buff)+"%";
            Global.getCombatEngine().maintainStatusForPlayerShip("HSIProtocalSuppression",
                    "graphics/icons/hullsys/HSI_Lock.png", HSII18nUtil.getHullModString("HSIProtocalSuppressionTitle"), content,
                    true);
        }

        public void addSource(ShipAPI ship,boolean isSmod){
            if(sources.contains(ship)){
                sources.set(ship,4f);
            }else{
                sources.add(ship,4f);
            }
            if(isSmod){
                if(SModSource.contains(ship)){
                    SModSource.set(ship,4f);
                }else{
                    SModSource.add(ship,4f);
                }
            }
        }
    }

    public static class HSIProtocalSuppressionManager implements DamageDealtModifier{
        private ShipAPI ship;

        private boolean isSMod = false;

        public HSIProtocalSuppressionManager(ShipAPI ship,boolean isSMod){
            this.ship = ship;
            this.isSMod = isSMod;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if(target instanceof ShipAPI) {
                ShipAPI t = (ShipAPI) target;
                if(t.getOwner() == ship.getOwner()) return null;
                if (Misc.getDistance(ship.getLocation(), t.getLocation()) >= DISTANCE) {
                        HSIProtocalSuppressionBuff buff = HSIProtocalSuppressionBuff.getInstance(t);
                     if (buff != null) {
                        buff.addSource(ship,isSMod);
                    }
                }
            }
            return null;
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new HSIProtocalSuppressionManager(ship,isSMod(ship)));
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index==0) return ""+(int)DISTANCE;
        if(index==1) return ((int)(float)DAMAGE_BUFF.get(ShipAPI.HullSize.FRIGATE)+"%"
                +"/"+(int)(float)DAMAGE_BUFF.get(ShipAPI.HullSize.DESTROYER)+"%"
                +"/"+(int)(float)DAMAGE_BUFF.get(ShipAPI.HullSize.CRUISER)+"%"
                +"/"+(int)(float)DAMAGE_BUFF.get(ShipAPI.HullSize.CAPITAL_SHIP)+"%");
        if(index==2) return  ""+MAX_LEVEL;
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if(index == 0) return "50%";
        return super.getSModDescriptionParam(index, hullSize, ship);
    }
}
