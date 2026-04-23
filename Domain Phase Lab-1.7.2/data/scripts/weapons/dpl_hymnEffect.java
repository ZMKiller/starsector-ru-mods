package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

/**
 * Summarized all effects for hymn
 * @author Vladimir
 */

public class dpl_hymnEffect implements BeamEffectPlugin {
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI) {
			boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
			ShipAPI ship = (ShipAPI) target;
			if (hitShield) {
				float dam = beam.getDamage().computeDamageDealt(amount);
            	ship.getFluxTracker().increaseFlux(dam, true);
            }
		}
	}
}
