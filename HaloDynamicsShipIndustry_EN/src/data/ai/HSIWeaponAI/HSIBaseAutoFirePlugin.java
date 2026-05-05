package data.ai.HSIWeaponAI;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class HSIBaseAutoFirePlugin implements AutofireAIPlugin {

    protected WeaponAPI weapon;
    protected ShipAPI ship;
    public HSIBaseAutoFirePlugin(WeaponAPI weapon){
        this.weapon = weapon;
        this.ship = weapon.getShip();
    }

    protected boolean shouldFire = false;

    protected Vector2f target = new Vector2f();

    protected ShipAPI targetShip = null;
    protected MissileAPI targetMissile = null;

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean shouldFire() {
        return shouldFire&&
                Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(), VectorUtils.getAngle(weapon.getLocation(),target)))<=0.5f;
    }

    @Override
    public void forceOff() {
        shouldFire = false;
    }

    @Override
    public Vector2f getTarget() {
        return target;
    }

    @Override
    public ShipAPI getTargetShip() {
        return targetShip;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {
        return targetMissile;
    }

    protected boolean isTargetLegal(CombatEntityAPI t,Vector2f loc){
        return Math.abs(weapon.distanceFromArc(loc))<=0&& MathUtils.getDistance(weapon.getLocation(),loc)-t.getCollisionRadius()*0.8f<=weapon.getRange();
    }
}
