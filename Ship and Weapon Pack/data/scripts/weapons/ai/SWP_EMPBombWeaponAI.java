package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_EMPBombWeaponAI implements AutofireAIPlugin {

    private boolean shouldFire = false;
    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.5f);
    private final WeaponAPI weapon;

    public SWP_EMPBombWeaponAI(WeaponAPI weapon) {
        this.weapon = weapon;
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().getPlayerShip() == weapon.getShip()) {
            shouldFire = false;
        } else {
            tracker.advance(amount);
            if (tracker.intervalElapsed()) {
                List<ShipAPI> enemies = AIUtils.getNearbyEnemies(weapon.getShip(), 1500f);
                Iterator<ShipAPI> iter = enemies.iterator();
                while (iter.hasNext()) {
                    ShipAPI enemy = iter.next();
                    if (!enemy.isAlive() || enemy.isFighter() || enemy.isDrone() || enemy.getFluxTracker().getFluxLevel() > 0.4f
                            || enemy.getFluxTracker().isOverloaded()) {
                        iter.remove();
                    }
                }
                shouldFire = enemies.size() > 0;
            }
        }
    }

    @Override
    public void forceOff() {
    }

    @Override
    public Vector2f getTarget() {
        return null;
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
    public boolean shouldFire() {
        return shouldFire;
    }

    @Override
    public MissileAPI getTargetMissile() {
        return null;
    }
}
