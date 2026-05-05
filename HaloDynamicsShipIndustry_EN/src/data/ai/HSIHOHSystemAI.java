package data.ai;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class HSIHOHSystemAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;
    //private boolean systemuse = true;
    private ShipwideAIFlags flags;
    private IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    private ShipAPI fixedTarget = null;
    private WeaponAPI hoh;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.flags = flags;
        this.system = system;
        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.getSlot().isSystemSlot() && w.getId().equals("HWI_HammerOfHumanity")) {
                hoh = w;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (system.getAmmo() <= 0)
            return;
        if (ship.getCurrFlux() + system.getFluxPerUse() > ship.getMaxFlux() * 0.9f)
            return;
        if (system.isChargeup()) {
            tryToKeepShipAiming();
        } else if (system.getCooldownRemaining() <= 0 && fixedTarget == null) {
            tracker.advance(amount);
            if (tracker.intervalElapsed()) {
                float range = hoh.getRange();
                if (target != null && !target.isFighter() && MathUtils.getDistanceSquared(ship.getLocation(),
                        target.getLocation()) < range * range * 0.64) {
                    fixedTarget = target;
                    flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 4f, fixedTarget);
                } else {
                    float dmg = hoh.getDamage().getDamage();
                    float maxWeight = 0;
                    ShipAPI curr = null;
                    for (ShipAPI e : AIUtils.getNearbyEnemies(ship, range * 0.9f)) {
                        if(e.isFighter()) continue;
                        float angleDiff = MathUtils.getShortestRotation(ship.getFacing(),
                                VectorUtils.getAngleStrict(ship.getLocation(), e.getLocation()));
                        if (Math.abs(angleDiff) > 90f) {
                            continue;
                        } else {
                            float weight = e.getHullSize().ordinal()*e.getHullSize().ordinal();
                            if (e.getFluxTracker().isOverloadedOrVenting()
                                    && (e.getFluxTracker().getOverloadTimeRemaining() >= 5f
                                            || e.getFluxTracker().getTimeToVent() >= 5f)) {
                                weight*=60f;                
                            }
                            if(e.getShield()==null){
                                weight*=2f;
                            }else{
                                if(e.getShield().getFluxPerPointOfDamage()*dmg>(e.getMaxFlux()-e.getCurrFlux()-e.getMutableStats().getFluxDissipation().getModifiedValue()*2f)*1.25f){
                                    weight*=5f;
                                }
                            }
                            weight/=angleDiff;
                            if(weight>maxWeight){
                                maxWeight = weight;
                                curr = e;
                            }
                        }
                    }
                    if(curr!=null){
                        fixedTarget = curr;
                    }
                }
            }
        }else if(system.isActive()){
            fixedTarget = null;
        }
        if(fixedTarget!=null){
            useSystemAccordingly();
        }
    }

    public void tryToKeepShipAiming() {
        if (fixedTarget != null) {
            if (Math.abs(MathUtils.getShortestRotation(ship.getFacing(),
                    VectorUtils.getAngleStrict(ship.getLocation(), fixedTarget.getLocation()))) < 0.5f) {
                ship.setAngularVelocity(ship.getAngularVelocity() / 1000f);
            } else {
                float aimAngle = MathUtils.getShortestRotation(ship.getFacing(),
                        VectorUtils.getAngleStrict(ship.getLocation(), fixedTarget.getLocation()));
                if (aimAngle < 0) {
                    ship.giveCommand(ShipCommand.TURN_RIGHT, null, 1);
                } else {
                    ship.giveCommand(ShipCommand.TURN_LEFT, null, 1);
                }
            }
        }
    }

    public void useSystemAccordingly(){
        if(ship.getSystem()!=null&&ship.getSystem().equals(system)){
            ship.useSystem();
        }else if(ship.getPhaseCloak()!=null&&ship.getPhaseCloak().equals(system)){
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, fixedTarget, 0);
        }
    }
}
