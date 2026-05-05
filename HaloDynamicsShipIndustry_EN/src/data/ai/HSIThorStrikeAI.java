package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIThorStrikeAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private float fraction;
	private ShipSystemAPI system;
	private ShipwideAIFlags flags;

	private IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
	}

	private float sinceLast = 0f;

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (engine.isPaused())
			return;
		if (system.getCooldownRemaining() > 0)
			return;
		if (target != null) {
			float angle = Misc.getAngleInDegrees(ship.getLocation(), target.getLocation());
			if (angle < 0)
				angle += 360f;
			if ((Misc.getDistance(ship.getLocation(), target.getLocation()) - target.getCollisionRadius() < 400f)
					&& Math.abs(ship.getFacing() - angle) < 10f) {
				ship.useSystem();
			}
		}
	}
}
