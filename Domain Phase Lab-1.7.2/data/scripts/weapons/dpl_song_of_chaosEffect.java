package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_song_of_chaosEffect implements BeamEffectPlugin {

	private boolean wasZero = true;
	private float AD_COEFFICIENT = 0.025f;
	private float FLUX_COEFFICIENT = 0.035f;
	private float Max_Interval_Self = 20f;
	private float Max_Interval_Enemy = 15f;
	private float Current_Interval = 0f;
	
	protected float self_interval = 1f;
	protected float enemy_interval = 0.8f;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		Current_Interval += amount;
		if (Current_Interval >= Max_Interval_Self) {
			
		}
		
		float dur_enemy = beam.getDamage().getDpsDuration();
		if (!wasZero) dur_enemy = 0;
		wasZero = beam.getDamage().getDpsDuration() <= 0;
		enemy_interval -= dur_enemy;
		
		float dur_self = amount;
		if (Current_Interval < Max_Interval_Self) dur_self = 0;
		self_interval -= dur_self;
		
		CombatEntityAPI target = beam.getDamageTarget();
		
		
		if (self_interval <= 0) {
			Color color = new Color(125,25,255,125);
			Color core = beam.getWeapon().getSpec().getGlowColor();
			
			EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(beam.getSource(), beam.getWeapon().getLocation(),
					beam.getSource(), beam.getSource(), DamageType.FRAGMENTATION, 
					0f, 0f, 100000f, 
					"shock_repeater_emp_impact", 50f, 
					color, core);

			Vector2f arcTo = arc.getTargetLocation();
			engine.spawnExplosion(arcTo, beam.getSource().getVelocity(), color, 100f, 3f);

			float dam = beam.getSource().getMaxHitpoints()*AD_COEFFICIENT;
			engine.applyDamage(beam.getSource(), arcTo, dam, DamageType.HIGH_EXPLOSIVE, 0f, true, false, beam.getSource());
			
			self_interval = Math.max(1f-0.08f*(Math.max(Current_Interval - Max_Interval_Self, 0f)), 0.1f);
		}
		
		if (beam.getDamageTarget() != null) {
			
			boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
			
			if (beam.getBrightness() >= 1f) {
				Color color = new Color(125,25,255,125);
				Color core = beam.getWeapon().getSpec().getGlowColor();
					
				if (!hitShield && enemy_interval <= 0) {
					EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(beam.getSource(), beam.getRayEndPrevFrame(),
							target, target, DamageType.FRAGMENTATION, 
							0f, 0f, 100000f, 
							"shock_repeater_emp_impact", 50f, 
							color, core);
	
					Vector2f arcTo = arc.getTargetLocation();
					engine.spawnExplosion(arcTo, target.getVelocity(), color, 100f, 3f);
					if (target instanceof ShipAPI) {
						ShipAPI targetShip = (ShipAPI) target;
						float dam = targetShip.getMaxHitpoints()*AD_COEFFICIENT;
						engine.applyDamage(target, arcTo, dam, DamageType.HIGH_EXPLOSIVE, 0f, true, false, target);
					}
					enemy_interval = Math.max(0.8f-0.08f*(Math.max(Current_Interval - Max_Interval_Enemy, 0f)), 0.25f);
				} else if (hitShield && target instanceof ShipAPI) {
					ShipAPI targetShip = (ShipAPI) target;
					targetShip.getFluxTracker().increaseFlux(targetShip.getMaxFlux()*FLUX_COEFFICIENT*beam.getDamage().getDpsDuration(), true);
				}
			}
		}		
		if (beam.getBrightness() <= 0.01f) {
			Current_Interval = 0f;
		}
	}
}
