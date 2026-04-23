package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.shaders.SWP_OmegaDriveShader;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_OmegaDriveStats extends BaseShipSystemScript {

    private static final Color COLOR1 = new Color(255, 255, 255, 255);
    private static final Color COLOR2 = new Color(255, 255, 255, 50);

    private static final String DATA_KEY = "SWP_OmegaDrive";

    private static final Vector2f ZERO = new Vector2f();

    private static Vector2f getClamped(Vector2f origin, Vector2f target, float clamp) {
        Vector2f d = new Vector2f();

        Vector2f.sub(target, origin, d);
        float l = d.length();
        if (l > 0f) {
            d.scale(clamp / l);
        }

        Vector2f.add(origin, d, d);

        return d;
    }

    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private ShipAPI ship;
    private boolean started = false;
    private float teleportCooldown = 0f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ship = (ShipAPI) stats.getEntity();

        if (ship.getHullLevel() > 0.67f) {
            return;
        }

        ship.setPhased(true);

        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<DamagingProjectileAPI> orbs = localData.orbs;

        ShipAPI playerShip = engine.getPlayerShip();
        stats.getFluxCapacity().modifyMult(id, effectLevel * 100f);

        if (state == State.IN) {
            if (!started) {
                started = true;
                Global.getSoundPlayer().playUISound("swp_arcade_omegadrive_activate", 1f, 0.75f);
            }
        }

        if (state == State.ACTIVE) {
            if (ship != playerShip && playerShip != null) {
                teleportCooldown -= Global.getCombatEngine().getElapsedInLastFrame() * Global.getCombatEngine().getTimeMult().getModifiedValue();
                if (AIUtils.canUseSystemThisFrame(ship) && teleportCooldown <= 0f) {
                    float maxDistance;
                    if (ship.getHullLevel() > 0.67f) {
                        maxDistance = 300f;
                    } else if (ship.getHullLevel() > 0.33f) {
                        maxDistance = 450f;
                    } else {
                        maxDistance = 600f;
                    }
                    switch (ship.getHullSpec().getBaseHullId()) {
                        case "swp_arcade_superzero":
                            maxDistance += 150f;
                            break;
                        case "swp_arcade_hyperzero":
                            maxDistance += 300f;
                            break;
                        default:
                    }
                    if (MathUtils.getDistance(ship, playerShip) > 1000f) {
                        ship.getMouseTarget().set(playerShip.getLocation());
                        ship.giveCommand(ShipCommand.USE_SYSTEM,
                                getClamped(ship.getLocation(), playerShip.getLocation(), maxDistance * 2f), 0);
                    } else {
                        Vector2f point = MathUtils.getPointOnCircumference(ship.getLocation(), 300f, ship.getFacing()
                                + (Math.random() > 0.5 ? 90f : -90f)
                                * ((float) Math.random() * 0.2f
                                + 0.9f));
                        ship.getMouseTarget().set(point);
                        ship.giveCommand(ShipCommand.USE_SYSTEM, getClamped(ship.getLocation(), point, maxDistance), 0);
                    }

                    for (int i = 0; i < 2; i++) {
                        float angle = VectorUtils.getAngle(ship.getLocation(), playerShip.getLocation())
                                + (float) Math.random() * 120f - 60f;
                        Vector2f point = MathUtils.getPointOnCircumference(ship.getLocation(), 150f, angle);
                        if (!ship.getAllWeapons().isEmpty()) {
                            DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(ship, ship.getAllWeapons().get(0),
                                    "swp_arcade_omegaorb",
                                    point, angle, null);
                            orbs.add(proj);
                        }
                    }

                    engine.addHitParticle(ship.getLocation(), ZERO, 300f, 5f, 0.35f, COLOR1);
                    engine.spawnExplosion(ship.getLocation(), ZERO, COLOR1, 600f, 0.25f);
                    Global.getSoundPlayer().playSound("swp_arcade_omegaorb_fire", 1f, 1f, ship.getLocation(), ZERO);

                    teleportCooldown = 0.2f;
                }
            }
        }

        ship.getMutableStats().getTimeMult().modifyMult(id, 1f + effectLevel * 4f);
        Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / (1f + effectLevel * 9f));

        if (ship != playerShip && playerShip != null) {
            playerShip.getMutableStats().getTimeMult().modifyMult(id, 1f + effectLevel * 4f);
            playerShip.setCollisionClass(CollisionClass.NONE);
            playerShip.getMutableStats().getBallisticRoFMult().modifyMult(id, 1f - effectLevel);
            playerShip.getMutableStats().getMissileRoFMult().modifyMult(id, 1f - effectLevel);
            playerShip.getMutableStats().getEnergyRoFMult().modifyMult(id, 1f - effectLevel);
        }

        interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        boolean drawGraphics = interval.intervalElapsed();

        Iterator<DamagingProjectileAPI> iter = orbs.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();

            if (!engine.isEntityInPlay(proj)) {
                iter.remove();
                continue;
            }

            if (drawGraphics && Math.random() < 0.1) {
                Vector2f point1 = MathUtils.getRandomPointInCircle(proj.getLocation(), (float) Math.random() * 250f);
                engine.spawnEmpArc(ship, proj.getLocation(), new SimpleEntity(proj.getLocation()), new SimpleEntity(
                        point1), DamageType.ENERGY, 0f, 0f, 1000f,
                        null, 10f, COLOR1, COLOR1);
            }

            if (playerShip != null && (!playerShip.getHullSpec().getBaseHullId().contentEquals("swp_arcade_superhyperion")
                    || playerShip.getSystem() == null
                    || !playerShip.getSystem().isActive())) {
                if (MathUtils.getDistance(proj.getLocation(), playerShip.getLocation()) <= 75f) {
                    engine.addHitParticle(proj.getLocation(), ZERO, 250f, 1f, 0.1f, COLOR1);
                    engine.spawnExplosion(proj.getLocation(), ZERO, COLOR2, 400f, 0.1f);
                    for (int i = 0; i < 5; i++) {
                        float angle = (float) Math.random() * 360f;
                        float distance = (float) Math.random() * 200f + 100f;
                        Vector2f point1 = MathUtils.getPointOnCircumference(proj.getLocation(), distance, angle);
                        Vector2f point2 = MathUtils.getPointOnCircumference(proj.getLocation(), distance, angle + 30f
                                * (float) Math.random() + 15f);
                        engine.spawnEmpArc(ship, point1, new SimpleEntity(point1), new SimpleEntity(point2),
                                DamageType.ENERGY, 0f, 0f, 1000f, null, 40f,
                                COLOR1, COLOR1);
                    }
                    switch (ship.getHullSpec().getBaseHullId()) {
                        case "swp_arcade_hyperzero":
                            engine.applyDamage(playerShip, playerShip.getLocation(), 30000f, DamageType.ENERGY, 0, false,
                                    false, ship, false);
                            break;
                        case "swp_arcade_superzero":
                            engine.applyDamage(playerShip, playerShip.getLocation(), 22500f, DamageType.ENERGY, 0, false,
                                    false, ship, false);
                            break;
                        default:
                            engine.applyDamage(playerShip, playerShip.getLocation(), 15000f, DamageType.ENERGY, 0, false,
                                    false, ship, false);
                    }
                    Global.getSoundPlayer().playSound("swp_arcade_omegaorb_impact", 1f, 1f, proj.getLocation(), ZERO);
                    engine.removeEntity(proj);
                    iter.remove();
                }
            }
        }

        SWP_OmegaDriveShader.setActive(true, effectLevel);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        CombatEngineAPI engine = Global.getCombatEngine();

        started = false;
        SWP_OmegaDriveShader.setActive(false, 0f);
        stats.getFluxCapacity().unmodify(id);
        ShipAPI playerShip = engine.getPlayerShip();

        ship = (ShipAPI) stats.getEntity();

        ship.setPhased(false);
        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData != null) {
            final List<DamagingProjectileAPI> orbs = localData.orbs;

            for (DamagingProjectileAPI proj : orbs) {
                engine.addHitParticle(proj.getLocation(), ZERO, 100f, 1f, 0.5f, COLOR1);
                engine.spawnExplosion(proj.getLocation(), ZERO, COLOR1, 100f, 0.15f);
                engine.removeEntity(proj);
            }
            orbs.clear();
        }

        ship.getMutableStats().getTimeMult().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);
        if (ship != playerShip && playerShip != null) {
            playerShip.getMutableStats().getTimeMult().unmodify(id);
            playerShip.setCollisionClass(CollisionClass.SHIP);
            playerShip.getMutableStats().getBallisticRoFMult().unmodify();
            playerShip.getMutableStats().getMissileRoFMult().unmodify();
            playerShip.getMutableStats().getEnergyRoFMult().unmodify();
        }
    }

    private static final class LocalData {

        final List<DamagingProjectileAPI> orbs = new LinkedList<>();
    }
}
