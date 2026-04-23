package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_cauldron_onHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		for (int i=0; i < 13; i++) {
			// some "general" smoke
			engine.addNebulaParticle(point,
					MathUtils.getRandomPointInCircle(null, 50f),
					MathUtils.getRandomNumberInRange(90f, 170f),
					2f,
					0.1f,
					0.5f,
					MathUtils.getRandomNumberInRange(2.2f, 3.3f),
					new Color(30,9,27,140),
					true);
		}
		
		// some lens flares for visual appeal
		for (int i=0; i < 6; i++) {
			
			float flareAngle = projectile.getFacing() + (i * 60f) + MathUtils.getRandomNumberInRange(5f, 55f);
			Vector2f flarePoint = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(80f, 150f), flareAngle);
			
			MagicLensFlare.createSharpFlare(
				    engine,
				    projectile.getSource(),
				    flarePoint,
				    5,
				    200,
				    flareAngle + MathUtils.getRandomNumberInRange(80f, 100f),
				    new Color(120,40,110),
					new Color(220,80,240));
			
			// some "lingering sparks"
			for (int j=0; j < 12; j++) {
				Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point, 120f),
						MathUtils.getRandomPointInCircle(null, 30f),
		    			MathUtils.getRandomNumberInRange(4f, 7f), //size
		    			1.0f, //brightness
		    			MathUtils.getRandomNumberInRange(2.0f, 3.1f), //duration
		    			new Color(150,60,195,255));
			}
		}
		
		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.9f, 1f, point, fxVel);
		
		if (target instanceof ShipAPI) {
			
			ShipAPI tagShip = ((ShipAPI) target);
			
			if (shieldHit) {
				
				 // setting the flux conversion value to a max of 1000, and clamping it to never be below 0
				float fluxVal = Math.max(0f, Math.min(1000f, tagShip.getFluxTracker().getCurrFlux() - tagShip.getFluxTracker().getHardFlux()));
				
				tagShip.getFluxTracker().setCurrFlux(tagShip.getFluxTracker().getCurrFlux() - fluxVal);
				tagShip.getFluxTracker().increaseFlux(fluxVal, true);
				 // do the conversion
				
			} else {
				
				// spawn arcs
				for (int i=0; i < 4; i++) {
					float dam = projectile.getDamageAmount() * 0.125f;
					float emp = projectile.getEmpAmount() * (0.25f + (tagShip.getFluxLevel() * 0.25f)); // emp scales up to double as target flux level raises (not mentioned on statcard)
					engine.spawnEmpArc(projectile.getSource(), point, target, target,
							DamageType.ENERGY,
							dam, // damage
							emp, // emp
							1000f, // max range
							"tachyon_lance_emp_impact",
							11f, // thickness
							new Color(120,40,110,100),
							new Color(255,225,255,110));
					
				}
				
			}
		}
	}
}
