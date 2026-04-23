package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

public class SFCRamDriveAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (!engine.isPaused()) {
            if (!system.isActive()) {
                if (target == null) {
                    return;
                }

                if (ship.getShipTarget() == null) {
                    ship.setShipTarget(target);
                    return;
                }

                if (!target.isAlive()) {
                    return;
                }

                if (target.isFighter() || target.isDrone() || target.isStation() || target.getEngineController().isFlamedOut()) {
                    return;
                }

                if ((Misc.getDistance(ship.getLocation(), target.getLocation()) < 700
                        || Misc.getDistance(ship.getLocation(), target.getLocation()) > 2000)
                        && !ship.getSystem().isActive()
                        && !ship.getSystem().isChargeup()
                        && !ship.getSystem().isCoolingDown()) {
                    return;
                }

                if (flags.hasFlag(AIFlags.MANEUVER_TARGET) || flags.hasFlag(AIFlags.PURSUING) || flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
                    ship.useSystem();
                }

            }

        }
    }
}
