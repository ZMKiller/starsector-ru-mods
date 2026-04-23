package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
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

public class ASF_Expiation_3_OnHitEffect implements OnHitEffectPlugin {
	
	private static final Color STAGE_3_COLOR = new Color(125,50,255,200);
	private static final Color STAGE_3B_COLOR = new Color(100,75,240,120);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

		int damArcCount = 0;
		int visArcCount = 0;
		
		if (target instanceof ShipAPI) {
			target.getVelocity().scale(0.85f); // 15% slow applied onHit
		}
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
			
			for (int i=0; i < (int) projectile.getEmpAmount(); i+=200) {
				if (shieldHit) {
					ShipAPI ship = (ShipAPI) target;
					float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
					pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
					
					if ((float) Math.random() < pierceChance) {
						damArcCount ++;
					} else {
						visArcCount ++;
					}
			    } else {
			    	damArcCount ++;
			    }
			}
		} else {
			for (int i=0; i < (int) projectile.getEmpAmount(); i+=200) {
				visArcCount++;
			}
		}
		
		visArcCount = Math.min(4, visArcCount); // too many vis arcs would look jank, so limit it to 4
		
		//do arcs!
		for (int i=0; i < damArcCount; i++) {
			engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
					DamageType.ENERGY,
					projectile.getDamageAmount() * 0.05f, // damage
					projectile.getDamageAmount() * 0.25f, // emp
					500f, // max range
					"tachyon_lance_emp_impact",
					15f, // thickness
					new Color(100,25,165,155),
					new Color(255,255,255,160));
		}
		// outer arcs
		for (int i=0; i < visArcCount; i++) {
			float distanceRandom1 = MathUtils.getRandomNumberInRange(20f, 42f);
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
			Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
			
			float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.8f, 1.2f);
			float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(80, 130);
			Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
			
			engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
					new Color(130,25,155,80),
					new Color(240,220,252,93));
			
		}
		
		engine.spawnExplosion(point, fxVel, STAGE_3B_COLOR, 125f, 0.9f);
		
		engine.addHitParticle(point, fxVel,
				180f,
				1f,
				0.1f,
				STAGE_3_COLOR.brighter());
		
		// "swirly neb"
		for (int i=0; i < 5; i++) {
			engine.addSwirlyNebulaParticle(point,
					fxVel,
					18f * i,
					1.8f,
					0.1f,
					0.36f,
					MathUtils.getRandomNumberInRange(1.3f, 1.5f),
					STAGE_3_COLOR.darker(),
					true);
		}
		
		// "core" particles
		for (int i=0; i < 18; i++) {
			Vector2f coreLoc = MathUtils.getRandomPointInCircle(point, 21f);
			
			engine.addSmoothParticle(coreLoc,
            		fxVel,
            		MathUtils.getRandomNumberInRange(34f, 43f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.75f, 1.1f), //duration
            		STAGE_3_COLOR);
		}
		
		// core glow sorta thing
		ASF_RadialEmitter emitterInner = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterInner.location(point);
		emitterInner.life(0.65f, 0.86f);
		emitterInner.size(6f, 10f);
		emitterInner.velocity(50f, -45f);
		emitterInner.distance(0f, 11f);
		emitterInner.color(STAGE_3B_COLOR.getRed(),STAGE_3B_COLOR.getGreen(),STAGE_3B_COLOR.getBlue(),STAGE_3B_COLOR.getAlpha());
		emitterInner.burst(30);
		
		// sparks
		ASF_RadialEmitter emitterOuter = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterOuter.location(point);
		emitterOuter.life(0.55f, 0.8f);
		emitterOuter.size(2f, 4f);
		emitterOuter.velocity(9f, 18f);
		emitterOuter.distance(30f, 23f);
		emitterOuter.color(STAGE_3_COLOR.getRed(),STAGE_3_COLOR.getGreen(),STAGE_3_COLOR.getBlue(),STAGE_3_COLOR.getAlpha());
		emitterOuter.emissionOffset(-3f, 3f);
		emitterOuter.velDistLinkage(false);
		emitterOuter.lifeLinkage(true);
		emitterOuter.burst(180);
		
	}
}