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

public class dpl_doom_rayEffect implements BeamEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.05f, 0.1f);
	private boolean wasZero = true;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			boolean hitShield = ship.getShield() != null && target.getShield().isWithinArc(beam.getTo());
			if (hitShield){
            float dam = beam.getDamage().computeDamageDealt(amount);
            ship.getFluxTracker().increaseFlux(dam, false);
			}
		}
	}
}
