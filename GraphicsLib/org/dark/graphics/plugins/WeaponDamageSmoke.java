package org.dark.graphics.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dark.graphics.util.ShipColors;
import org.dark.shaders.util.GraphicsLibSettings;
import org.dark.shaders.util.ShaderLib;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

@SuppressWarnings("UseSpecificCatch")
public class WeaponDamageSmoke extends BaseEveryFrameCombatPlugin {

    private static final Map<WeaponSize, Float> MAGNITUDE = new HashMap<>(3);

    private static final float OFFSCREEN_GRACE_CONSTANT = 500f;
    private static final float OFFSCREEN_GRACE_FACTOR = 2f;

    private static final Color SMOKE1_MOD = new Color(0, 0, 0, 0);
    private static final Color SMOKE2_MOD = new Color(70, 70, 70, 0);

    static {
        MAGNITUDE.put(WeaponSize.SMALL, 1f);
        MAGNITUDE.put(WeaponSize.MEDIUM, 1.5f);
        MAGNITUDE.put(WeaponSize.LARGE, 2.5f);
    }

    private CombatEngineAPI engine;
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if ((engine == null) || !GraphicsLibSettings.enableWeaponSmoke()) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        interval.advance(amount);

        if (interval.intervalElapsed()) {
            float smokeSize = 0.8f + 0.4f * (float) Math.random();

            List<ShipAPI> ships = engine.getShips();
            int size = ships.size();
            for (int i = 0; i < size; i++) {
                ShipAPI ship = ships.get(i);
                if (!ship.isAlive()) {
                    continue;
                }

                if (GraphicsLibSettings.drawOffscreenParticles()
                        || ShaderLib.isOnScreen(ship.getLocation(), (ship.getCollisionRadius() + 20f) * OFFSCREEN_GRACE_FACTOR + OFFSCREEN_GRACE_CONSTANT)) {
                    String style = ship.getHullStyleId();
                    Color smokeColor = ShipColors.SMOKE_COLORS.get(style);
                    if (smokeColor == null) {
                        smokeColor = ShipColors.SMOKE_COLORS.get("MIDLINE");
                    }
                    List<WeaponAPI> weapons = ship.getAllWeapons();
                    int weaponsSize = weapons.size();
                    for (int j = 0; j < weaponsSize; j++) {
                        WeaponAPI weapon = weapons.get(j);
                        if (weapon.isDisabled() && !weapon.isPermanentlyDisabled()) {
                            float smokeSizeValue = MAGNITUDE.get(weapon.getSize());

                            Vector2f location = MathUtils.getRandomPointInCircle(null, 5f);

                            Color color = ShipColors.colorJitter(ShipColors.colorBlend(smokeColor, SMOKE1_MOD, 0.9f),
                                    10f);
                            Color color2 = ShipColors.colorJitter(ShipColors.colorBlend(smokeColor, SMOKE2_MOD, 0.9f),
                                    30f);

                            engine.addSmokeParticle(weapon.getLocation(), location, 40f * smokeSize * smokeSizeValue,
                                    0.1f, 4f, color);
                            engine.addSmokeParticle(weapon.getLocation(), location, 20f * smokeSize * smokeSizeValue,
                                    0.1f, 3f, color2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
