package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HSIDashAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private boolean systemuse = true;
	private float shortest = 1000f;
	private ShipwideAIFlags flags;
	private IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.engine = engine;
		this.flags = flags;
		this.system = system;
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
		systemuse = true;
		if (target == null)
			return;

		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0)
				systemuse = false;
			if (system.isOutOfAmmo())
				systemuse = false;
			if (system.isActive())
				systemuse = false;
			if (systemuse) {
				if (Misc.getDistance(ship.getLocation(), target.getLocation()) > shortest ) {
					Vector2f to = Vector2f.add(ship.getLocation(),
							(Vector2f) (Misc.getUnitVectorAtDegreeAngle(ship.getFacing())
									.scale(ship.getCollisionRadius() * 2f + target.getCollisionRadius() * 2f)),
							null);
					flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 5f, to);
					ship.useSystem();
					return;
				}
			}
		}
	}
}
