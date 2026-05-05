package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

public class HSIRapidReinforcementAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private ShipwideAIFlags flags;
	private IntervalUtil think = new IntervalUtil(0.2f, 0.3f);

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (engine.isPaused())
			return;
		if (system.isOn() || system.getCooldownRemaining() > 0)
			return;
		if (system.getAmmo() <= 0)
			return;
		think.advance(amount);
		if (think.intervalElapsed()) {
			float replacementTimeTotal = 0;
			float replacementTimeBaysHasLost = 0;
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() != null) {
					if (bay.getNumLost() > 0) {
						replacementTimeTotal += (bay.getNumLost() *
								ship.getMutableStats().getFighterRefitTimeMult().getModifiedValue() *
								bay.getWing().getSpec().getRefitTime());

					}
					replacementTimeBaysHasLost += ship.getMutableStats().getFighterRefitTimeMult().getModifiedValue()
							* bay.getWing().getSpec().getRefitTime();
				}
			}
			if (replacementTimeBaysHasLost <= 0)
				return;
			float frac = replacementTimeTotal / replacementTimeBaysHasLost;
			if (frac > 0.7f) {
				ship.useSystem();
			}
		}
	}
}
