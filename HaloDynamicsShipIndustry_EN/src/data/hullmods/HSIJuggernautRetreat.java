package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class HSIJuggernautRetreat implements HullDamageAboutToBeTakenListener, AdvanceableListener {

    private boolean isRetreating = false;

    private FaderUtil retreatFader = new FaderUtil(1f,3f);

    private ShipAPI ship;

    public HSIJuggernautRetreat(ShipAPI ship){this.ship = ship;}
    @Override
    public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
        if (damageAmount >= ship.getHitpoints()) {
            if (!isRetreating) {
                isRetreating = true;
                ship.setHitpoints(100);
                retreatFader.fadeOut();
                if (!ship.isPhased()) {
                    Global.getSoundPlayer().playSound("system_phase_cloak_activate", 1f, 1f, ship.getLocation(), ship.getVelocity());
                    ship.setPhased(true);
                    for(ShipAPI module:ship.getChildModulesCopy()){
                        module.setPhased(true);
                    }
                }
                //reviveFader.setBounceUp(true);
            }
            if(!retreatFader.isIdle()) {
                ship.getMutableStats().getHullDamageTakenMult().modifyMult("HSI_JuggernautRetreat", 0f);
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    private static final Color JITTER = new Color(225,175,255,255);
    @Override
    public void advance(float amount) {
        if(isRetreating){
            retreatFader.advance(amount);
            Color c = JITTER ;
            c = Misc.setAlpha(c, 255);
            c = Misc.interpolateColor(c, Color.white, 0.5f);
            float b = retreatFader.getBrightness();
            b = MathUtils.clamp(b,0,1);
            ship.setExtraAlphaMult2(b);
            float r = ship.getCollisionRadius() * 5f;
            ship.setJitter(this, c, b, 20, r * (1f - b));
            for(ShipAPI s:ship.getChildModulesCopy()) {
                s.setExtraAlphaMult2(b);
                s.getMutableStats().getHullDamageTakenMult().modifyMult("HSI_JuggernautRetreat",0f);
                s.setJitter(this, c, b, 20, r*0.5f * (1f - b));

            }
            //ship.setRetreating(true, false);

            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
            ship.blockCommandForOneFrame(ShipCommand.FIRE);
            ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
            ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);


            if (retreatFader.isIdle()) {
                Global.getSoundPlayer().playSound("phase_anchor_vanish", 1f, 1f, ship.getLocation(), ship.getVelocity());
                ship.getLocation().set(0, -1000000f);
                ship.getMutableStats().getHullDamageTakenMult().unmodify("HSI_JuggernautRetreat");
                //ship.getFleetMember().getStatus().disable();
                //ship.getFleetMember().updateStats();
                ship.setOwner(100);
                Global.getCombatEngine().applyDamage(ship,ship.getLocation(),99999999f, DamageType.OTHER,0f,true,false,ship);
            }
        }
    }


}
