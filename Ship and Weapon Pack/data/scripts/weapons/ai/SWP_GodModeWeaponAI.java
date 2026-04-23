package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_GodModeWeaponAI implements AutofireAIPlugin {

    private boolean shouldFire = false;
    private final WeaponAPI weapon;

    public SWP_GodModeWeaponAI(WeaponAPI weapon) {
        this.weapon = weapon;
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().getPlayerShip() == weapon.getShip()) {
            shouldFire = false;
        } else {
            List<ShipAPI> enemies = AIUtils.getNearbyEnemies(weapon.getShip(), 1500f);
            Iterator<ShipAPI> iter = enemies.iterator();
            while (iter.hasNext()) {
                ShipAPI enemy = iter.next();
                if (!enemy.isAlive() || enemy.isFighter() || enemy.isDrone() || enemy.getFluxTracker().getFluxLevel()
                        < weapon.getShip().getFluxTracker().getFluxLevel()) {
                    iter.remove();
                }
            }
            shouldFire = (weapon.getShip().getFluxTracker().getFluxLevel() >= 0.95f || weapon.getShip().getHullLevel() <= 0.5f) && enemies.size() > 0;
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
