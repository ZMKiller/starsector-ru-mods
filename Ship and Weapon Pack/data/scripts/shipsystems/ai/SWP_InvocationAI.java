package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_InvocationAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(0.75f, 1.5f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused()) {
            return;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            boolean pointedAtTarget = false;
            if (target == null) {
                target = ship.getShipTarget();
            }
            if (target == null && ship.getWing() != null && ship.getWing().getLeader() != null) {
                target = ship.getWing().getLeader().getShipTarget();
            }
            if (target != null) {
                float angleToTarget = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
                if (MathUtils.getShortestRotation(angleToTarget, ship.getFacing()) <= 5f
                        && MathUtils.getDistance(ship, target) <= 400f) {
                    pointedAtTarget = true;
                }
            }

            if (pointedAtTarget) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }
}
