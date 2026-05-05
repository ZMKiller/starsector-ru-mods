package data.ai;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class HSICrossWindAI implements AutofireAIPlugin{
    protected final WeaponAPI weapon;
    private ShipAPI ship;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private ShipAPI target = null;
    private Vector2f targetLoc = null;
    private boolean shouldFire = false;
    private boolean isHardpoint =false;
    private float range = 0;
    protected static final float LOCKED_BONUS = 1.5f;

    public HSICrossWindAI(WeaponAPI weapon) {
        this.weapon = weapon;
        ship = weapon.getShip();
        isHardpoint = weapon.getSlot().isHardpoint();
        range = weapon.getRange();
    }

    public void advance(float amount) {
        if (engine.isPaused())
            return;
        if(weapon.getCooldownRemaining()>0) return;
        range = weapon.getRange();
        shouldFire = false;
        if(target!=null&&target.isAlive()){
            if((MathUtils.getDistance(weapon.getFirePoint(0), target.getLocation())-target.getCollisionRadius()/1.5f)<=(LOCKED_BONUS*range)){
                shouldFire = true;
                targetLoc = target.getLocation();
            }else{
                target = null;
                targetLoc = Vector2f.add(weapon.getLocation(), Misc.getUnitVectorAtDegreeAngle(weapon.getSlot().computeMidArcAngle(ship)),null);
            }
        }else{
            for(ShipAPI e:AIUtils.getNearbyEnemies(ship, range+ship.getCollisionRadius())){
                if((MathUtils.getDistance(weapon.getFirePoint(0), e.getLocation())-e.getCollisionRadius()/1.5f)<=(range)){
                    target = e;
                    break;
                }
            }
        }
    }

    public boolean shouldFire() {
        return shouldFire&&((isHardpoint)?true:Math.abs(weapon.distanceFromArc(targetLoc))<=30f);
    }

    public void forceOff() {
        this.shouldFire = false;
    }


    public Vector2f getTarget() {
        return targetLoc;
    }

    /**
     * @return current target, if it's a ship. null otherwise.
     */
    public ShipAPI getTargetShip() {
        return target;
    }

    public WeaponAPI getWeapon() {
        return weapon;
    }

    public MissileAPI getTargetMissile() {
        return null;
    }
    
}
