package data.weapons.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HWILightningWeapon implements EveryFrameWeaponEffectPlugin {
    private IntervalUtil interval = new IntervalUtil(1f, 1f);

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused())
            return;
        if(weapon.getSlot().isHidden()) return;
        interval.advance(amount);
        if (interval.intervalElapsed()) {
            float cd = weapon.getCooldown();
            float currcd = weapon.getCooldownRemaining();
            float frac = 1 - currcd / cd;
            float r = 6f;
            if (weapon.getSize() == WeaponSize.LARGE)
                r = 30f;
            if (weapon.getSize() == WeaponSize.MEDIUM)
                r = 13f;
            if (Math.random() < frac) {
                int fromPoint = (int) (Math.random() / 0.25f);
                if (fromPoint > 3)
                    fromPoint = 3;
                Vector2f from = weapon.getFirePoint(fromPoint);
                int toPoint = (int) (Math.random() / 0.25f);
                if (toPoint == fromPoint)
                    toPoint += 2;
                if (toPoint > 3)
                    toPoint -= 4;
                Vector2f to = weapon.getFirePoint(toPoint);
                engine.spawnEmpArcVisual(from, weapon.getShip(), to, weapon.getShip(),
                        (float) (1 + r / 15 * Math.random()),
                        new Color(210, 219, 255, 155), new Color(255, 255, 255, 255));
            }
            if (Math.random() < frac) {
                int fromPoint = (int) (Math.random() / 0.25f);
                if (fromPoint > 3)
                    fromPoint = 3;
                Vector2f from = weapon.getFirePoint(fromPoint);

                Vector2f to = Misc.getPointWithinRadius(from, r);
                engine.spawnEmpArcVisual(from, weapon.getShip(), to, weapon.getShip(),
                        (float) (1 + r / 15 * Math.random()),
                        new Color(210, 219, 255, 105), new Color(255, 255, 255, 165));
            }
        }
    }
}
