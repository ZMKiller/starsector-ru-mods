package data.ai.HSIWeaponAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.kit.AjimusUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.Iterator;
import java.util.List;

public class HSIApocalypseLanceAI implements AutofireAIPlugin {
    private WeaponAPI weapon;
    private ShipAPI ship;

    private static Vector2f IDIE_AIM = new Vector2f(50f,-70f);

    private static Vector2f DIRECT_AIM = new Vector2f(100f,0f);

    private static Vector2f sweep_MAX_AIM = new Vector2f(100f,40f);

    private Vector2f aimLoc;

    private boolean issweep = false;

    private boolean shouldFire = false;

    private TimeoutTracker<Object> sweepCoolDown = new TimeoutTracker<>();
    private IntervalUtil sweepCheckInterval = new IntervalUtil(0.1f,0.2f);

    private IntervalUtil sweepTimer = new IntervalUtil(1.6f,1.6f);

    private IntervalUtil PDCheckInterval = new IntervalUtil(0.05f,0.1f);

    private ShipAPI fighterTarget = null;


    public HSIApocalypseLanceAI(WeaponAPI weapon){
        this.weapon = weapon;
        this.ship = weapon.getShip();
    }

    @Override
    public void advance(float amount) {
        sweepCoolDown.advance(amount);
        sweepCheckInterval.advance(amount);
        PDCheckInterval.advance(amount);
        shouldFire = false;
        Vector2f toAdd = IDIE_AIM;
        //String info = "IDLE";

        boolean shouldsweep = !issweep&&sweepCoolDown.getItems().isEmpty();
        if(shouldsweep){
            shouldsweep = false;
            if(sweepCheckInterval.intervalElapsed()){
                Iterator<Object> potentialTargets = Global.getCombatEngine().getShipGrid().getCheckIterator(weapon.getFirePoint(0),30f,30f);
                while (potentialTargets.hasNext()){
                    Object o = potentialTargets.next();
                    if(o instanceof ShipAPI){
                        ShipAPI s = (ShipAPI) o;
                        if(s.getOwner()!=ship.getOwner()&&MathUtils.getDistance(weapon.getFirePoint(0),s.getLocation())<20f+s.getCollisionRadius()*0.6f){
                            shouldsweep = true;
                        }
                    }
                }
            }
        }
        if(shouldsweep){
            issweep = true;
            sweepCoolDown.add(new Object(),8.5f);
        }
        if(issweep){
            shouldFire = true;
            toAdd= sweep_MAX_AIM;
            sweepTimer.advance(amount);
            //Global.getLogger(this.getClass()).info("Time:"+Global.getCombatEngine().getTotalElapsedTime(true)+"-"+"Sweeped:"+Misc.getDistance(ship.getLocation(), e.getLocation()));
            if(sweepTimer.intervalElapsed()){
                issweep = false;
            }
            //info ="sweep";
        }
        if(ship!=null&&ship.getSystem()!=null&&ship.getSystem().isOn()){
            toAdd = DIRECT_AIM;
            issweep =  false;
            shouldFire = true;
            sweepTimer.forceIntervalElapsed();
            //info = "SYSTEM";
        }
        aimLoc = AjimusUtils.getEngineCoordFromRelativeCoord(weapon.getLocation(),toAdd,ship.getFacing());

        if(weapon.isFiring()){
            Iterator<Object> potentialTargets = Global.getCombatEngine().getShipGrid().getCheckIterator(weapon.getFirePoint(0),30f,30f);
            while (potentialTargets.hasNext()){
                Object o = potentialTargets.next();
                if(o instanceof ShipAPI){
                    ShipAPI s = (ShipAPI) o;
                    if(s.isFighter()&&s.getOwner()!=ship.getOwner()&&MathUtils.getDistance(s.getLocation(),weapon.getFirePoint(0))<=30f+s.getCollisionRadius()){
                        s.getLocation().set(weapon.getFirePoint(0));
                    }
                }
            }
        }

        //Global.getLogger(this.getClass()).info(info+"-ShouldF:"+shouldFire);
    }

    @Override
    public boolean shouldFire() {
        return shouldFire;
    }

    @Override
    public void forceOff() {

    }

    @Override
    public Vector2f getTarget() {
        return aimLoc;
    }

    @Override
    public ShipAPI getTargetShip() {
        return null;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {
        return null;
    }
}
