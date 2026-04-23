package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public class SWP_SensorArrayAI implements ShipSystemAIScript {

    public static final float HARD_FLUX_FACTOR = 0.25f;
    public static final float REMAINING_FLUX_FACTOR_ACTIVATE = 0.25f;
    public static final float REMAINING_FLUX_FACTOR_DEACTIVATE = 0.5f;

    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (system.getCooldownRemaining() > 0) {
                return;
            }

            float weight = 0f;
            if (system.isActive()) {
                float fluxThreshold = ship.getFluxTracker().getMaxFlux() * REMAINING_FLUX_FACTOR_DEACTIVATE;
                if ((ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux()) >= fluxThreshold) {
                    weight = 9f;
                }
            } else {
                float fluxThreshold = ship.getFluxTracker().getMaxFlux() * REMAINING_FLUX_FACTOR_ACTIVATE;
                float hardFluxThreshold = ship.getFluxTracker().getMaxFlux() * HARD_FLUX_FACTOR;
                if (((ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux()) < fluxThreshold) ||
                        (ship.getFluxTracker().getHardFlux() >= hardFluxThreshold)) {
                    weight = -9f;
                }
            }

            if (flags.hasFlag(AIFlags.PURSUING)) {
                weight += 5f;
            }
            if (flags.hasFlag(AIFlags.BACKING_OFF) || flags.hasFlag(AIFlags.BACK_OFF)) {
                weight -= 5f;
            }
            if (flags.hasFlag(AIFlags.BACK_OFF_MIN_RANGE)) {
                weight -= 5f;
            }
            if (flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)) {
                weight -= 10f;
            }
            if (flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
                weight += 5f;
            }
            if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) || flags.hasFlag(AIFlags.NEEDS_HELP)) {
                weight -= 5f;
            }
            if (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
                weight -= 10f;
            }
            if (flags.hasFlag(AIFlags.KEEP_SHIELDS_ON)) {
                weight -= 5f;
            }
            if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
                weight -= 10f;
            }

            if (ship.getFluxTracker().getFluxLevel() >= 0.25f) {
                flags.setFlag(AIFlags.OK_TO_CANCEL_SYSTEM_USE_TO_VENT);
            } else {
                flags.unsetFlag(AIFlags.OK_TO_CANCEL_SYSTEM_USE_TO_VENT);
            }
            if (!system.isActive() && weight >= 0f) {
                ship.useSystem();
            } else if (system.isActive() && weight < 0f) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
    }
}
