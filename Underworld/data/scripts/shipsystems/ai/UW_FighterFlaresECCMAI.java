package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.UW_Util;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_FighterFlaresECCMAI implements ShipSystemAIScript {

    private static final float AIMING_RANGE = 800f;
    private static final float OPTIMAL_RANGE = 400f;

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        if (system.getCooldownRemaining() > 0) {
            return;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            float totalWeight = 0f;
            float bestWeight = 0f;

            List<MissileAPI> missiles = UW_Util.getMissilesWithinRange(ship.getLocation(), AIMING_RANGE);
            for (MissileAPI missile : missiles) {
                if (missile.getOwner() != ship.getOwner()) {
                    float distance = MathUtils.getDistance(missile, ship.getLocation());
                    float weight = (OPTIMAL_RANGE - Math.abs(distance - OPTIMAL_RANGE)) / OPTIMAL_RANGE;

                    if (weight > 0f) {
                        if (missile.isFlare()) {
                            weight *= 200f;
                            totalWeight += weight;

                            if (weight > bestWeight) {
                                bestWeight = weight;
                            }
                        } else {
                            weight *= ((missile.getDamageType() == DamageType.FRAGMENTATION) ? 0.25f : 1f)
                                    * missile.getDamageAmount() + 0.5f * missile.getEmpAmount();
                            totalWeight += weight;

                            if (weight > bestWeight) {
                                bestWeight = weight;
                            }
                        }
                    }
                }
            }

            if (bestWeight >= 100f || totalWeight >= 300f) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }
}
