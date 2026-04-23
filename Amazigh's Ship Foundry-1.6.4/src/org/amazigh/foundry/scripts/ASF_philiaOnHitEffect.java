package org.amazigh.foundry.scripts;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ASF_philiaOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		engine.addNebulaSmokeParticle(point,
        		fxVel,
        		27f, //size
        		1.9f, //end mult
        		0.6f, //ramp fraction
        		0.5f, //full bright fraction
        		0.8f, //duration
        		new Color(235,150,225,100));
		
		if (target instanceof ShipAPI) {
			target.getVelocity().scale(MathUtils.getRandomNumberInRange(0.8f, 0.9f)); // 10-20% slow applied onHit
			
	    	ShipAPI ship = (ShipAPI) target;
			
			if (shieldHit) {
				boolean piercedShield = false;
				float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
				
				piercedShield = (float) Math.random() < pierceChance;
				if (!piercedShield) {
					return;
				}
		    }
			
			engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
					DamageType.ENERGY,
					projectile.getDamageAmount() * 0.5f, // damage
					projectile.getEmpAmount() * 0.5f, // emp
					2000f, // max range
					"tachyon_lance_emp_impact",
					12f, // thickness
					new Color(235,150,225,155), // fringe
					new Color(255,245,255,165)); // core
			
		}
		
	}
}
