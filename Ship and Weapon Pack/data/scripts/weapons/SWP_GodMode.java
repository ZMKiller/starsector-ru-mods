package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_GodMode implements EveryFrameWeaponEffectPlugin {

    private static final Color MAIN_COLOR = new Color(175, 225, 255);
    private static final Vector2f ZERO = new Vector2f();

    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float timer = 0f;
    private boolean used = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getCooldownRemaining() > 0f) {
            if (!used) {
                used = true;
                Global.getSoundPlayer().playSound("swp_arcade_godmode_activate", 1f, 2f, weapon.getShip().getLocation(),
                        weapon.getShip().getVelocity());
                MutableShipStatsAPI stats = weapon.getShip().getMutableStats();
                stats.getShieldDamageTakenMult().modifyMult(weapon.getId(), 0f);
                stats.getHullDamageTakenMult().modifyMult(weapon.getId(), 0f);
                stats.getEmpDamageTakenMult().modifyMult(weapon.getId(), 0f);
                stats.getArmorDamageTakenMult().modifyMult(weapon.getId(), 0.00001f);
                weapon.getShip().getFluxTracker().setHardFlux(0f);
                weapon.getShip().getFluxTracker().setCurrFlux(weapon.getShip().getFluxTracker().getCurrFlux() / 2f);

                StandardLight light = new StandardLight(ZERO, ZERO, ZERO, weapon.getShip());
                light.setIntensity(1f);
                light.setSize(200f);
                light.setColor(MAIN_COLOR);
                light.fadeIn(0.33f);
                light.setLifetime(9.34f);
                light.setAutoFadeOutTime(0.33f);
                LightShader.addLight(light);

                timer = 10f;
            }
        }

        if (timer > 0f) {
            timer -= amount;
            if (timer <= 0f) {
                MutableShipStatsAPI stats = weapon.getShip().getMutableStats();
                stats.getShieldDamageTakenMult().unmodify(weapon.getId());
                stats.getHullDamageTakenMult().unmodify(weapon.getId());
                stats.getEmpDamageTakenMult().unmodify(weapon.getId());
                stats.getArmorDamageTakenMult().unmodify(weapon.getId());
            }

            if (weapon.getShip() == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(weapon,
                        "graphics/icons/hullsys/fortress_shield.png",
                        "Mega Shield", "Invincible", false);
            }
            Global.getSoundPlayer().playLoop("swp_arcade_godmode_loop", weapon.getShip(), 1f, 1f, weapon.getShip().getLocation(),
                    weapon.getShip().getVelocity());

            interval.advance(amount);
            if (interval.intervalElapsed()) {
                float shipRadius = SWP_Util.effectiveRadius(weapon.getShip());

                engine.addSmoothParticle(MathUtils.getRandomPointInCircle(weapon.getShip().getLocation(),
                        shipRadius * 1.25f),
                        ZERO,
                        (float) Math.random() * 5f + 35f, (float) Math.random() * 0.25f + 1f, 1f,
                        MAIN_COLOR);
            }
        }

        if (weapon.getCooldownRemaining() <= 0f) {
            if (used) {
                used = false;
            }
        }
    }
}
