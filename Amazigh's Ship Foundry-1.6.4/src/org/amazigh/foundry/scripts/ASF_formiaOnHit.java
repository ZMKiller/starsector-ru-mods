package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_formiaOnHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target instanceof ShipAPI) {
			if (!shieldHit) {
				float dam = projectile.getDamageAmount() * 0.25f;
				float emp = projectile.getEmpAmount() * 0.25f;
				engine.spawnEmpArc(projectile.getSource(), point, target, target,
						DamageType.ENERGY,
						dam, // damage
						emp, // emp
						1000f, // max range
						"tachyon_lance_emp_impact",
						12f, // thickness
						new Color(70,100,155,90),
	    				new Color(220,225,255,111));
			}
		}
	}
}
