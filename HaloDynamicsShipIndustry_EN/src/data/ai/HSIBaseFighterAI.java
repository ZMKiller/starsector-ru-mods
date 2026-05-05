package data.ai;

import java.util.EnumSet;

import javax.swing.WindowConstants;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;

public class HSIBaseFighterAI implements ShipAIPlugin {

    private ShipwideAIFlags flags = new ShipwideAIFlags();
    private ShipAPI ship;
    private ShipAPI sourceShip;
    private float nofire = 0;
    private float furthestAttackWeaponRange = 0;
    private float shortestAttackWeaponRange = 1000;
    private float rangeForDecision = 0;
    private ShipAPI target;
    private FighterWingAPI wing;

    public HSIBaseFighterAI(ShipAPI ship) {
        this.ship = ship;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (!weapon.isDecorative()) {
                if (weapon.getSpec().getAIHints().containsAll(EnumSet.of(AIHints.PD, AIHints.PD_ONLY))) {
                    ship.getWeaponGroupFor(weapon).toggleOn();
                } else {
                    if (weapon.getRange() > furthestAttackWeaponRange) {
                        furthestAttackWeaponRange = weapon.getRange();
                    } else if (weapon.getRange() < shortestAttackWeaponRange) {
                        shortestAttackWeaponRange = weapon.getRange();
                    }
                    ship.getWeaponGroupFor(weapon).toggleOn();
                }
            }
        }
        rangeForDecision = furthestAttackWeaponRange*0.6f+shortestAttackWeaponRange*0.4f;
        wing = ship.getWing();
    }

    @Override
    public void advance(float amount) {
        if(Global.getCombatEngine().isPaused()) return;
        if(wing==null) return;
        if(wing.getLeader().equals(ship)){
            analysis();
        }else{

        }
    }

    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public void forceCircumstanceEvaluation() {

    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }

    @Override
    public ShipAIConfig getConfig() {
        return null;
    }

    @Override
    public boolean needsRefit() {
        return sourceShip != null && sourceShip.isPullBackFighters() && ship.getHitpoints() < ship.getMaxHitpoints();
    }

    @Override
    public void setDoNotFireDelay(float amount) {
        nofire = amount;
    }

    public void analysis() {
        
    }

    public boolean isTargetable(ShipAPI t){
        return !(!t.isAlive()||!t.isAlly()||MathUtils.getDistance(ship.getLocation(), target.getLocation())>wing.getRange()+rangeForDecision*0.2f);
    }

}
