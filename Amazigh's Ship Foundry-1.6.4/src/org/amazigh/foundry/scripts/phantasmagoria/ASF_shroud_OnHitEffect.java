package org.amazigh.foundry.scripts.phantasmagoria;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_shroud_OnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target instanceof ShipAPI) {
			if (!shieldHit) {
				float dam = projectile.getDamageAmount() * 0.4f;
				float emp = projectile.getEmpAmount() * 0.4f;
				engine.spawnEmpArc(projectile.getSource(), point, target, target,
						DamageType.FRAGMENTATION,
						dam, // damage
						emp, // emp
						1000f, // max range
						"A_S-F_quiet_emp_impact",
						9f, // thickness
						new Color(110,90,125,90),
	    				new Color(230,225,245,111));
			}
		}
	}
}
