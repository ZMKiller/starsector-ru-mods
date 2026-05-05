package data.weapons.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import data.kit.AjimusUtils;

public class HWIHOHWeapon implements EveryFrameWeaponEffectPlugin {
    private float maxLevel = 0;
    protected static final Color ARC_CORE = new Color(85, 25, 215, 255);
    protected static final Color ARC_FRINGE = new Color(255, 255, 255, 255);
    protected static final Color ARC_CORE_LOW = new Color(85, 25, 215, 155);
    protected static final Color ARC_FRINGE_LOW = new Color(255, 255, 255, 155);
    protected static List<Vector2f> lower = new ArrayList<>();
    protected Random random = new Random();
    static {
        lower.add(new Vector2f(-44, 48));
        lower.add(new Vector2f(-54, 42));
        lower.add(new Vector2f(-74, 32));
        lower.add(new Vector2f(-44, -48));
        lower.add(new Vector2f(-54, -42));
        lower.add(new Vector2f(-74, -32));
    };

    protected static List<Vector2f> lowerCenter = new ArrayList<>();
    static {
        lowerCenter.add(new Vector2f(-39, -16));
        lowerCenter.add(new Vector2f(-39, 16));
        lowerCenter.add(new Vector2f(-44, -12));
        lowerCenter.add(new Vector2f(-44, 12));
        lowerCenter.add(new Vector2f(-53, 0));
    }

    protected static List<Vector2f> higher = new ArrayList<>();
    static {
        higher.add(new Vector2f(28, -30));
        higher.add(new Vector2f(28, 30));
        higher.add(new Vector2f(34, -44));
        higher.add(new Vector2f(34, 44));
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused())
            return;
        ShipAPI ship = weapon.getShip();
        if (weapon.getChargeLevel() > 0) {
            float level = weapon.getChargeLevel();
            // Global.getLogger(this.getClass()).info("At
            // "+engine.getTotalElapsedTime(true)+"level is "+level);
            // if(weapon.isFiring()) Global.getLogger(this.getClass()).info("At
            // "+engine.getTotalElapsedTime(true)+"Fired ");
            if (level > 1)
                level = 1;
            int time = (int) level * 6;
            if (level >= maxLevel) {
            } else {
                time = (int) level * 4;
            }
            do {
                time--;
                WeightedRandomPicker<Vector2f> lowPicker = new WeightedRandomPicker<>();
                for (Vector2f vec : lower) {
                    lowPicker.add(vec);
                }
                WeightedRandomPicker<Vector2f> lowCPicker = new WeightedRandomPicker<>();
                for (Vector2f vec : lowerCenter) {
                    lowCPicker.add(vec);
                }
                WeightedRandomPicker<Vector2f> highPicker = new WeightedRandomPicker<>();
                for (Vector2f vec : higher) {
                    highPicker.add(vec);
                }
                Vector2f l = AjimusUtils.getEngineCoordFromRelativeCoord(ship.getLocation(), lowPicker.pick(),
                        ship.getFacing());
                Vector2f lc = AjimusUtils.getEngineCoordFromRelativeCoord(ship.getLocation(), lowCPicker.pick(),
                        ship.getFacing());
                Vector2f h = AjimusUtils.getEngineCoordFromRelativeCoord(ship.getLocation(), highPicker.pick(),
                        ship.getFacing());
                if (random.nextDouble() < 0.03 * level) {
                    Global.getCombatEngine().spawnEmpArcVisual(l, ship, lc, ship, 3 + 9 * level,
                            ARC_CORE_LOW,
                            ARC_FRINGE_LOW);
                    Global.getCombatEngine().spawnEmpArcVisual(lc, ship, weapon.getFirePoint(0), ship, 3 + 9 * level,
                            ARC_CORE,
                            ARC_FRINGE);
                }
                if (random.nextDouble() < 0.03 * level) {
                    Global.getCombatEngine().spawnEmpArcVisual(lc, ship, weapon.getFirePoint(0), ship, 3 + 9 * level,
                            ARC_CORE,
                            ARC_FRINGE);
                }
                if (random.nextDouble() < 0.04 * level) {
                    Global.getCombatEngine().spawnEmpArcVisual(h, ship, weapon.getFirePoint(0), ship,
                            4 + 12 * level,
                            ARC_CORE, ARC_FRINGE);
                }
            } while (time >= 0);
            // float slice = 1f/30f;
        } else {
            maxLevel = 0;
            if (weapon.getCooldownRemaining() >0) {
                WeightedRandomPicker<Vector2f> highPicker = new WeightedRandomPicker<>();
                for (Vector2f vec : higher) {
                    highPicker.add(vec);
                }
                for (Vector2f vec : lowerCenter) {
                    highPicker.add(vec);
                }
                Vector2f h = AjimusUtils.getEngineCoordFromRelativeCoord(ship.getLocation(), highPicker.pick(),
                        ship.getFacing());
                if (Math.random() < 0.01) {
                    Global.getCombatEngine().spawnEmpArcVisual(h, ship, weapon.getFirePoint(0), ship,
                            6,
                            ARC_CORE_LOW, ARC_FRINGE_LOW);
                }
            }
        }
    }
}
