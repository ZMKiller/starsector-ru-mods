package data.ai;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HWIAdvancedPDAI implements AutofireAIPlugin {

    private WeaponAPI weapon;
    private ShipAPI ship;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private Vector2f targetLoc = null;
    private boolean shouldFire = true;

    public HWIAdvancedPDAI(WeaponAPI weapon) {
        this.weapon = weapon;
        ship = weapon.getShip();
    }

    public void advance(float amount) {
        if (engine!=null&&engine.isPaused())
            return;
        if(weapon.getAmmo()>0) shouldFire = true;
        targetLoc = MathUtils.getPoint(weapon.getLocation(), 1, weapon.getArcFacing()+ship.getFacing());
    }

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
        this.shouldFire = false;
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
        return null;
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
        return null;
    }

}
