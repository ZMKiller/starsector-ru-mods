package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_termiteOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target instanceof ShipAPI) {
			
			int arcCount = 0;
			
			// 50% chance of no arc, 35% chance of 1 arc, 15% chance of 2 arcs
			if (Math.random() > 0.5f) {
				arcCount++;
				if (Math.random() > 0.7f) {
					arcCount++;
				}
			}
			
			for (int i=0; i < arcCount; i++) {
				Vector2f arcLoc = point;
				
				 // so we spawn arcs even on a shield hit, but offset them back a bit so they are more visible
				if (shieldHit) {
					float shieldOrient = VectorUtils.getAngle(target.getLocation(), point);
					arcLoc = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(24f, 36f), shieldOrient + MathUtils.getRandomNumberInRange(-30f, 30f));
				}
				
				engine.spawnEmpArc(projectile.getSource(), arcLoc, target, target,
						DamageType.ENERGY,
						projectile.getDamageAmount() * 0.1f, // damage
						projectile.getEmpAmount() * 0.5f, // emp
						2000f, // max range
						"A_S-F_quiet_emp_impact",
						10f, // thickness
						new Color(25,145,110,160),
						new Color(245,255,250,180));
			}	
		}
		
	}
}
