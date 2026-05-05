package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIDawnAI implements ShipSystemAIScript {

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
		tracker.advance(amount);

		sinceLast += amount;

		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0)
				return;
			if (system.isOutOfAmmo())
				return;
			if (system.isActive())
				return;
			fraction = 0;

			int size = 0;

			int maxAmmo = 1;
			int ammo = 0;
			for (WeaponAPI weapon : ship.getAllWeapons()) {
				if (weapon.usesAmmo()) {
					maxAmmo += weapon.getMaxAmmo();
					ammo += weapon.getAmmo();
				}
			}
			fraction = (float) (0.7f - ammo / maxAmmo);
			if (target != null) {
				if (target.getHullSize() == HullSize.DESTROYER)
					size = 1;

				if (target.getHullSize() == HullSize.CRUISER)
					size = 2;

				if (target.getHullSize() == HullSize.CAPITAL_SHIP)
					size = 3;

				if (size != 0) {

					boolean targetIsVulnerable = target != null && target.getFluxTracker().isOverloadedOrVenting() &&
							(target.getFluxTracker().getOverloadTimeRemaining() > 5f ||
									target.getFluxTracker().getTimeToVent() > 5f);

					if (targetIsVulnerable) {
						fraction += (0.1f * size);
					}
				}
			}

			if (fraction >= 0.5f) {
				ship.useSystem();
				sinceLast = 0f;
				return;
			}
		}
	}
}
