package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HWIAntiProjectileAI implements AutofireAIPlugin {
    private Vector2f targetLoc = new Vector2f(0, 0);
    private WeaponAPI weapon;
    private ShipAPI ship;
    private DamagingProjectileAPI targetProj;
    private ShipAPI targetShip;
    private boolean shouldFire = false;
    private boolean isControlling = false;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private DamagingProjectileAPI lastTargeting = null;
    private TimeoutTracker<DamagingProjectileAPI> timeout = new TimeoutTracker<DamagingProjectileAPI>();

    public HWIAntiProjectileAI(WeaponAPI weapon) {
        this.weapon = weapon;
        ship = weapon.getShip();
    }

    public void advance(float amount) {
        targetLoc = ship.getMouseTarget();
        if (engine.isPaused())
            return;
        if (ship == null || engine.isEntityInPlay(ship) || !ship.isAlive())
            return;
        if (weapon.getCooldownRemaining() > 0)
            return;
        DamagingProjectileAPI Tproj = null;
        float maxEver = 0;
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            if (proj.getOwner() != ship.getOwner() && proj != lastTargeting) {
                if (isInRange(proj)) {
                    if (proj.getDamageAmount() > maxEver && proj.getDamageAmount() > 200f) {
                        maxEver = proj.getDamageAmount();
                        Tproj = proj;
                    }
                }
            }
        }
        if (Tproj != null) {
            targetLoc = engine.getAimPointWithLeadForAutofire(ship, ship.getAimAccuracy(), Tproj,
                    weapon.getProjectileSpeed());
        }
        targetProj = Tproj;
        shouldFire = !isControlling&&ship.getFluxLevel() < 0.9f && (targetShip != null || targetProj != null);
        isControlling = false;
    }

    /**
     * Only called when the group is on autofire.
     * 
     * @return whether the weapon should fire now.
     */
    public boolean shouldFire() {
        return shouldFire;
    }

    /**
     * Tells the weapon AI to reconsider whether it should be firing, before it
     * decides it should fire again.
     * 
     * Called when a group is toggled on/off.
     */
    public void forceOff() {
        isControlling = true;
    }

    /**
     * @return location to aim at, with target leading if applicable. Can be null if
     *         the weapon has no target/isn't trying to aim anywhere.
     */
    public Vector2f getTarget() {
        return targetLoc;
    }

    /**
     * @return current target, if it's a ship. null otherwise.
     */
    public ShipAPI getTargetShip() {
        return targetShip;
    }

    /**
     * @return the weapon that this AI is controlling. That means the plugin should
     *         hold on to it when it's passed in in
     *         ModPlugin.pickWeaponAutofireAI().
     */
    public WeaponAPI getWeapon() {
        return weapon;
    }

    public MissileAPI getTargetMissile() {
        if (targetProj instanceof MissileAPI) {
            return (MissileAPI) targetProj;
        } else {
            return null;
        }
    }

    public DamagingProjectileAPI getTargetProj(){
        return targetProj;
    }

    private boolean isInRange(CombatEntityAPI entity) {
        float dis = Misc.getDistance(weapon.getLocation(),
                entity.getLocation());
        return Math.abs(weapon.distanceFromArc(entity.getLocation()))<0&&dis-entity.getCollisionRadius()<=weapon.getRange();
    }

}
