package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ForceImpederAI implements ShipSystemAIScript {

    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(2f, 3f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);
        Vector2f shipLoc = ship.getLocation();

        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            boolean shouldUseSystem = false;
            float repulseRadius = 1500f;
            List<ShipAPI> nearbyEnemies = AIUtils.getNearbyEnemies(ship, repulseRadius);
            List<MissileAPI> nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, repulseRadius);
            List<DamagingProjectileAPI> nearbyBullets = SWP_Util.getProjectilesWithinRange(shipLoc, repulseRadius);
            if (nearbyMissiles.size() > 0 || nearbyEnemies.size() > 0 || nearbyBullets.size() > 0) {
                shouldUseSystem = true;
            }

            if (ship.getSystem().isActive() ^ shouldUseSystem) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }
}
