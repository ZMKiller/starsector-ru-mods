package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_EMPBomb implements EveryFrameWeaponEffectPlugin {

    private static final Color COLOR1 = new Color(255, 255, 255);
    private static final Color COLOR2 = new Color(200, 225, 255, 150);
    private static final Vector2f ZERO = new Vector2f();

    private static final List<MegaArc> arcs = new ArrayList<>(100);
    private static final Map<String, Float> bossShips = new HashMap<>(8);
    private static CombatEngineAPI currentEngine;
    private static final List<MegaArc> newarcs = new ArrayList<>(100);

    static {
        bossShips.put("swp_arcade_oberon", 2f);
        bossShips.put("swp_arcade_ultron", 3f);
        bossShips.put("swp_arcade_zeus", 2f);
        bossShips.put("swp_arcade_ezekiel", 4f);
        bossShips.put("swp_arcade_cristarium", 2f);
        bossShips.put("swp_arcade_zero", 4f);
        bossShips.put("swp_arcade_superzero", 5f);
        bossShips.put("swp_arcade_hyperzero", 6f);
    }

    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private boolean used = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine != currentEngine) {
            arcs.clear();
            newarcs.clear();
            currentEngine = engine;
            return;
        }

        if (weapon.getChargeLevel() > 0f && !used) {
            interval.advance(amount);
            if (interval.intervalElapsed()) {
                float shipRadius = SWP_Util.effectiveRadius(weapon.getShip());

                Vector2f point = MathUtils.getRandomPointOnCircumference(weapon.getShip().getLocation(),
                        shipRadius * 5f);
                Vector2f vel = VectorUtils.getDirectionalVector(point, weapon.getShip().getLocation());
                vel.scale((float) Math.random() * 125f + 150f);
                engine.addSmoothParticle(point, vel, (float) Math.random() * 10f + 10f, 0.5f, 1f, COLOR1);
            }

            Global.getSoundPlayer().playLoop("high_intensity_laser_loop", weapon.getShip(), 1f
                    + weapon.getChargeLevel() * 4f, 3f,
                    weapon.getShip().getLocation(), weapon.getShip().getVelocity());
        }

        if (weapon.getCooldownRemaining() > 0f) {
            if (!used) {
                used = true;

                StandardLight light = new StandardLight(weapon.getShip().getLocation(), ZERO, ZERO, null);
                light.setIntensity(3f);
                light.setSize(3500f);
                light.setColor(COLOR1);
                light.fadeOut(2f);
                LightShader.addLight(light);

                for (int i = 0; i < 25; i++) {
                    Vector2f direction = new Vector2f(1f, 0f);
                    VectorUtils.rotate(direction, (float) Math.random() * 360f, direction);
                    arcs.add(new MegaArc(weapon.getShip(), weapon.getShip().getLocation(), direction,
                            (float) Math.random() * 2000f + 2000f,
                            (float) Math.random() * 20f + 15f,
                            (float) Math.random() * 0.5f + 0.2f));
                }

                engine.addSmoothParticle(weapon.getShip().getLocation(), ZERO, 1250f, 2f, 2f, COLOR1);
                engine.addHitParticle(weapon.getShip().getLocation(), ZERO, 750f, 2f, 2f, COLOR1);
                Global.getSoundPlayer().playSound("swp_arcade_empbomb_activate", 1f, 1f, weapon.getShip().getLocation(), ZERO);

                List<ShipAPI> ships = SWP_Util.getShipsWithinRange(weapon.getShip().getLocation(), 3000f);
                for (ShipAPI ship : ships) {
                    if (ship != weapon.getShip() && !bossShips.containsKey(ship.getHullSpec().getBaseHullId())
                            && (!ship.getHullSpec().getBaseHullId().contentEquals("swp_arcade_superhyperion")
                            || weapon.getShip() != engine.getPlayerShip())) {
                        ship.getFluxTracker().forceOverload(10f
                                + (3000f - MathUtils.getDistance(weapon.getShip(), ship)) / 250f);
                    }
                }
            }
        }

        if (weapon.getCooldownRemaining() <= 0f) {
            if (used) {
                used = false;
            }
        }

        Iterator<MegaArc> iter = arcs.iterator();
        while (iter.hasNext()) {
            MegaArc arc = iter.next();
            if (Math.random() >= 0.67) {
                if (arc.advance(amount)) {
                    iter.remove();
                }
            }
        }

        iter = newarcs.iterator();
        while (iter.hasNext()) {
            MegaArc arc = iter.next();
            arcs.add(arc);
            iter.remove();
        }
    }

    private static final class MegaArc {

        private final Vector2f direction;
        private final IntervalUtil interval = new IntervalUtil(0.035f, 0.035f);
        private final Vector2f location;
        private float remaining;
        private final ShipAPI source;
        private final float speed;
        private final float thickness;

        private MegaArc(ShipAPI source, Vector2f location, Vector2f direction, float speed, float thickness,
                float lifetime) {
            this.source = source;
            this.location = new Vector2f(location);
            this.direction = new Vector2f(direction);
            this.speed = speed;
            this.thickness = thickness;
            this.remaining = lifetime;
        }

        private boolean advance(float amount) {
            Vector2f newLocation = new Vector2f(direction);
            newLocation.scale(speed * amount);
            Vector2f.add(newLocation, location, newLocation);

            interval.advance(amount);
            boolean intervalElapsed = interval.intervalElapsed();

            if (intervalElapsed) {
                Global.getCombatEngine().spawnEmpArc(source, location, new SimpleEntity(newLocation), new SimpleEntity(
                        newLocation), DamageType.ENERGY, 0f, 0f,
                        10000f, null, thickness, COLOR2, COLOR1);
            }

            location.set(newLocation);
            VectorUtils.rotate(direction, (float) Math.random() * 60f - 30f, direction);

            if (intervalElapsed && Math.random() > 0.94) {
                Global.getSoundPlayer().playSound("swp_arcade_vortex_absorb", 1f, 0.5f, location, ZERO);
                Vector2f newDirection = new Vector2f(1f, 0f);
                VectorUtils.rotate(newDirection, (float) Math.random() * 360f, newDirection);
                newarcs.add(new MegaArc(source, location, newDirection, speed + (float) Math.random() * 200f - 100f,
                        Math.max(
                                thickness - (float) Math.random()
                                * 5f, 2.5f),
                        Math.max(remaining + (float) Math.random() * 0.3f - 0.15f, 0.01f)));
            }

            remaining -= amount;
            return remaining <= 0f;
        }
    }
}
