package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.everyframe.SWP_Trails;
import java.awt.Color;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_FluxShuntOnFireEffect implements OnFireEffectPlugin {

    private static final Color COLOR_1 = new Color(255, 150, 200);
    private static final Color COLOR_2 = new Color(255, 100, 150);
    private static final Color COLOR_3 = new Color(180, 60, 120);
    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if ((projectile == null) || (weapon == null)) {
            return;
        }

        SWP_Trails.createIfNeeded();

        ShipAPI ship = projectile.getSource();
        if (ship.getFluxTracker().getCurrFlux() >= 6000f) {
            engine.spawnProjectile(ship, projectile.getWeapon(), "swp_excelsiorcannon_60", projectile.getLocation(), weapon.getCurrAngle(),
                    ship.getVelocity());

            for (int i = 0; i < 25; i++) {
                float distance = (float) Math.random() * 15f;
                float size = (float) Math.random() * 8f + 8f;
                float angle = (float) Math.random() * 270f - 135f;
                Vector2f spawn_location = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, (angle + weapon.getCurrAngle()));
                float duration = (float) Math.random() * 0.25f + 0.25f;
                float speed = 20f * distance / duration;
                Vector2f particle_velocity = MathUtils.getPointOnCircumference(ship.getVelocity(), speed, angle + weapon.getCurrAngle());
                engine.addHitParticle(spawn_location, particle_velocity, size, 1f, duration, COLOR_1);
            }
            for (int i = 0; i < 2; i++) {
                float angle = (float) Math.random() * 360f;
                float distance = (float) Math.random() * 40f;
                Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, angle);
                Vector2f point2 = MathUtils.getPointOnCircumference(projectile.getLocation(), distance + (float) Math.random() * 60f, angle);
                engine.spawnEmpArc(ship, point1, new SimpleEntity(point1), new SimpleEntity(point2), DamageType.ENERGY, 0f, 0f, 1000f, null, 30f, COLOR_2, COLOR_1);
            }
            engine.spawnExplosion(projectile.getLocation(), ship.getVelocity(), COLOR_2, 200f, 0.175f);
            engine.addSmoothParticle(projectile.getLocation(), ship.getVelocity(), 400f, 0.5f, 0.125f, COLOR_3);

            RippleDistortion ripple = new RippleDistortion(projectile.getLocation(), ship.getVelocity());
            ripple.setSize(200f);
            ripple.setIntensity(20f);
            ripple.setFrameRate(180f);
            ripple.fadeInSize(0.4f);
            ripple.fadeOutIntensity(0.35f);
            DistortionShader.addDistortion(ripple);

            ship.getFluxTracker().decreaseFlux(120f);
            Global.getSoundPlayer().playSound("swp_fluxshuntcannon_60_fire", 1f, 1f, new Vector2f(projectile.getLocation()), ZERO);
        } else if (ship.getFluxTracker().getCurrFlux() >= 4000f) {
            engine.spawnProjectile(ship, projectile.getWeapon(), "swp_excelsiorcannon_40", projectile.getLocation(), weapon.getCurrAngle(),
                    ship.getVelocity());

            for (int i = 0; i < 20; i++) {
                float distance = (float) Math.random() * 12.5f;
                float size = (float) Math.random() * 7f + 7f;
                float angle = (float) Math.random() * 270f - 135f;
                Vector2f spawn_location = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, (angle + weapon.getCurrAngle()));
                float duration = (float) Math.random() * 0.2f + 0.2f;
                float speed = 20f * distance / duration;
                Vector2f particle_velocity = MathUtils.getPointOnCircumference(ship.getVelocity(), speed, angle + weapon.getCurrAngle());
                engine.addHitParticle(spawn_location, particle_velocity, size, 1f, duration, COLOR_1);
            }
            for (int i = 0; i < 1; i++) {
                float angle = (float) Math.random() * 360f;
                float distance = (float) Math.random() * 30f;
                Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, angle);
                Vector2f point2 = MathUtils.getPointOnCircumference(projectile.getLocation(), distance + (float) Math.random() * 45f, angle);
                engine.spawnEmpArc(ship, point1, new SimpleEntity(point1), new SimpleEntity(point2), DamageType.ENERGY, 0f, 0f, 1000f, null, 25f, COLOR_2,
                        COLOR_1);
            }
            engine.spawnExplosion(projectile.getLocation(), ship.getVelocity(), COLOR_2, 175f, 0.15f);
            engine.addSmoothParticle(projectile.getLocation(), ship.getVelocity(), 350f, 0.4f, 0.1f, COLOR_3);

            ship.getFluxTracker().decreaseFlux(100f);
            Global.getSoundPlayer().playSound("swp_fluxshuntcannon_40_fire", 1f, 1f, new Vector2f(projectile.getLocation()), ZERO);
        } else if (ship.getFluxTracker().getCurrFlux() >= 2400f) {
            engine.spawnProjectile(ship, projectile.getWeapon(), "swp_excelsiorcannon_24", projectile.getLocation(), weapon.getCurrAngle(),
                    ship.getVelocity());

            for (int i = 0; i < 15; i++) {
                float distance = (float) Math.random() * 10f;
                float size = (float) Math.random() * 6f + 6f;
                float angle = (float) Math.random() * 270f - 135f;
                Vector2f spawn_location = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, (angle + weapon.getCurrAngle()));
                float duration = (float) Math.random() * 0.15f + 0.15f;
                float speed = 20f * distance / duration;
                Vector2f particle_velocity = MathUtils.getPointOnCircumference(ship.getVelocity(), speed, angle + weapon.getCurrAngle());
                engine.addHitParticle(spawn_location, particle_velocity, size, 1f, duration, COLOR_1);
            }
            engine.spawnExplosion(projectile.getLocation(), ship.getVelocity(), COLOR_2, 150f, 0.125f);
            engine.addSmoothParticle(projectile.getLocation(), ship.getVelocity(), 300f, 0.3f, 0.075f, COLOR_3);

            ship.getFluxTracker().decreaseFlux(80f);
            Global.getSoundPlayer().playSound("swp_fluxshuntcannon_24_fire", 1f, 1f, new Vector2f(projectile.getLocation()), ZERO);
        } else if (ship.getFluxTracker().getCurrFlux() >= 1200f) {
            engine.spawnProjectile(ship, projectile.getWeapon(), "swp_excelsiorcannon_12", projectile.getLocation(), weapon.getCurrAngle(),
                    ship.getVelocity());

            for (int i = 0; i < 10; i++) {
                float distance = (float) Math.random() * 7.5f;
                float size = (float) Math.random() * 5f + 5f;
                float angle = (float) Math.random() * 270f - 135f;
                Vector2f spawn_location = MathUtils.getPointOnCircumference(projectile.getLocation(), distance, (angle + weapon.getCurrAngle()));
                float duration = (float) Math.random() * 0.1f + 0.1f;
                float speed = 20f * distance / duration;
                Vector2f particle_velocity = MathUtils.getPointOnCircumference(ship.getVelocity(), speed, angle + weapon.getCurrAngle());
                engine.addHitParticle(spawn_location, particle_velocity, size, 1f, duration, COLOR_1);
            }
            engine.spawnExplosion(projectile.getLocation(), ship.getVelocity(), COLOR_2, 125f, 0.1f);

            ship.getFluxTracker().decreaseFlux(60f);
            Global.getSoundPlayer().playSound("swp_fluxshuntcannon_12_fire", 1f, 1f, new Vector2f(projectile.getLocation()), ZERO);
        } else if (ship.getFluxTracker().getCurrFlux() >= 400f) {
            engine.spawnProjectile(ship, projectile.getWeapon(), "swp_excelsiorcannon_4", projectile.getLocation(), weapon.getCurrAngle(),
                    ship.getVelocity());

            engine.spawnExplosion(projectile.getLocation(), ship.getVelocity(), COLOR_2, 100f, 0.075f);

            ship.getFluxTracker().decreaseFlux(40f);
            Global.getSoundPlayer().playSound("swp_fluxshuntcannon_4_fire", 1f, 1f, new Vector2f(projectile.getLocation()), ZERO);
        } else {
            engine.spawnProjectile(ship, projectile.getWeapon(), "swp_excelsiorcannon_0", projectile.getLocation(), weapon.getCurrAngle(),
                    ship.getVelocity());

            ship.getFluxTracker().decreaseFlux(20f);
            Global.getSoundPlayer().playSound("swp_fluxshuntcannon_0_fire", 1f, 1f, new Vector2f(projectile.getLocation()), ZERO);
        }

        engine.removeEntity(projectile);
    }
}
