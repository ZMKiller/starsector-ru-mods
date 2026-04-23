package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ShieldBreakerAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            List<ShipAPI> possibleTargets = SWP_Util.getShipsWithinRange(ship.getLocation(), 1500f);
            Iterator<ShipAPI> iter = possibleTargets.iterator();
            while (iter.hasNext()) {
                ShipAPI possibleTarget = iter.next();
                if (possibleTarget.isFighter() || possibleTarget.isDrone() || !possibleTarget.isAlive() || possibleTarget.getShield() == null || possibleTarget ==
                                                                                                                                                 ship ||
                    possibleTarget.getOwner() == ship.getOwner()) {
                    iter.remove();
                }
            }

            if (possibleTargets.size() > 0) {
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
