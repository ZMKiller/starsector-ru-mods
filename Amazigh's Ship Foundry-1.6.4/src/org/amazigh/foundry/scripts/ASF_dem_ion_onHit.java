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

public class ASF_dem_ion_onHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target instanceof ShipAPI) {
			if (!shieldHit) {
				float dam = projectile.getDamageAmount();
				float emp = projectile.getEmpAmount();
				engine.spawnEmpArc(projectile.getSource(), point, target, target,
						DamageType.ENERGY,
						dam, // damage
						emp, // emp
						1000f, // max range
						"tachyon_lance_emp_impact",
						12f, // thickness
						new Color(25,130,135,100),
						new Color(225,255,255,110));
			}
		}
		
	}
}
