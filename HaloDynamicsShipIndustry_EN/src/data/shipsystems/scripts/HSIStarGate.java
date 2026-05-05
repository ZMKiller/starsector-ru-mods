package data.shipsystems.scripts;

import org.lwjgl.util.vector.Vector2f;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class HSIStarGate extends BaseShipSystemScript {
    private ShipAPI ship;
    private Vector2f targeting = null;
    private WeaponAPI w = null;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private boolean once = true;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(once){
            targeting = ship.getMouseTarget();
            once = false;
        }
        if (ship.hasListenerOfClass(HSIStarKeyRangeControl.class)) {
            HSIStarKeyRangeControl keyControl = ship.getListeners(HSIStarKeyRangeControl.class).get(0);
            keyControl.setTargetLoc(targeting);
        }else{
            ship.addListener(new HSIStarKeyRangeControl());
        }
        if (w == null) {
            for (WeaponAPI wpn : ship.getAllWeapons()) {
                if (wpn.getSpec().getWeaponId().equals("HWI_StarKey"))
                    w = wpn;
                    w.setSuspendAutomaticTurning(true);
            }
        }
        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {
            float shouldFacing = Misc.getAngleInDegrees(w.getLocation(), targeting);
            if(shouldFacing<0) shouldFacing+=360f;
            engine.maintainStatusForPlayerShip("StarKey", null, "StarKey", w.getDisplayName(), false);
            w.setCurrAngle(shouldFacing);
            w.setForceFireOneFrame(true);
            if (state == State.ACTIVE) {
                addNebulas(w.getLocation(), true);
                addNebulas(targeting, false);
                for (FighterWingAPI wing : ship.getAllWings()) {
                    for (ShipAPI fighter : wing.getWingMembers()) {
                        if (ship.isPullBackFighters()) {
                            if (Misc.getDistance(fighter.getLocation(), targeting) < Misc.getDistance(
                                    fighter.getLocation(),
                                    ship.getLocation())) {
                                        moveFighterToGateTransport(fighter,targeting,w.getLocation());
                            }
                        }else if(wing.isReturning(fighter)){
                            if (Misc.getDistance(fighter.getLocation(), targeting) < Misc.getDistance(
                                    fighter.getLocation(),
                                    wing.getReturnData(fighter).bay.getLandingLocation(fighter))) {
                                        moveFighterToGateTransport(fighter,targeting,w.getLocation());
                                    }
                        }
                        else{
                            if(Misc.getDistance(fighter.getLocation(), w.getLocation()) < Misc.getDistance(
                                fighter.getLocation(),
                                targeting)){
                                    moveFighterToGateTransport(fighter,w.getLocation(),targeting);
                                }
                        }
                    }
                }
            }
        }
        stats.getMaxSpeed().modifyMult(id, 0);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (ship.hasListenerOfClass(HSIStarKeyRangeControl.class)) {
            ship.addListener(new HSIStarKeyRangeControl());
        }
        once = true;
    }

    private void moveFighterToGateTransport(ShipAPI fighter,Vector2f from,Vector2f to){
        float heading = Misc.getAngleInDegrees(fighter.getLocation(), from);
        if(heading<0) heading+=360f;
        engine.headInDirectionWithoutTurning(fighter, heading, fighter.getMaxSpeed());
        if(fighter.getFacing()-heading>5) fighter.giveCommand(ShipCommand.STRAFE_LEFT, to, 0);
        if(fighter.getFacing()-heading<-5) fighter.giveCommand(ShipCommand.STRAFE_RIGHT, to, 0);
        if(Misc.getDistance(fighter.getLocation(), from)<50f){
            fighter.getLocation().set(to);
        }
    }


    private void addNebulas(Vector2f center, boolean in) {
        for (int i = 0; i < 4; i++) {
            Vector2f start = Misc.getPointWithinRadius(center, 450f);
            Vector2f vel = start;
            if (in) {
                vel = Vector2f.sub(start, center, vel);
                engine.addNebulaParticle(start, vel, (float) (16f + 8f * Math.random()), 0.1f,
                        (float) (Math.random() * 1f + 1f),
                        (float) (Math.random() * 1f + 1f),
                        0.7f, new Color(101, 132, 162, 150), false);
            } else {
                vel = Vector2f.sub(center, start, vel);
                engine.addNebulaParticle(center, vel, 1f, (float) (8f + 16f * Math.random()),
                        (float) (Math.random() * 1f + 1f),
                        (float) (Math.random() * 1f + 1f),
                        0.7f, new Color(101, 132, 162, 150), false);
            }

        }
    }

    public class HSIStarKeyRangeControl implements WeaponRangeModifier {
        private Vector2f targetLoc = new Vector2f(0, 0);

        public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0;
        }

        public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }

        public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (weapon.getSpec().getWeaponId().equals("HWI_StarKey") && targetLoc != null&&ship.getSystem().isOn()) {
                float change = Misc.getDistance(weapon.getLocation(), targetLoc) * ship.getSystem().getEffectLevel();
                return change;
            }
            return 0f;
        }

        public void setTargetLoc(Vector2f loc) {
            targetLoc = loc;
        }
    }
}
