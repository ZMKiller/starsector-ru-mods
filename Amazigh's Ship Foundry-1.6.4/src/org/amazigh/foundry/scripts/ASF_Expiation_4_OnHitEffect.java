package org.amazigh.foundry.scripts;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_Expiation_4_OnHitEffect implements OnHitEffectPlugin {

	private static final Color STAGE_4_COLOR = new Color(255,0,150,200);
	private static final Color STAGE_4B_COLOR = new Color(200,0,200,125);
	
	private static Map<HullSize, Float> impulseMult = new HashMap<HullSize, Float>();
	static {
		impulseMult.put(HullSize.FIGHTER, 0.25f);
		impulseMult.put(HullSize.FRIGATE, 0.3f);
		impulseMult.put(HullSize.DESTROYER, 0.35f);
		impulseMult.put(HullSize.CRUISER, 0.45f);
		impulseMult.put(HullSize.CAPITAL_SHIP, 0.6f);
		impulseMult.put(HullSize.DEFAULT, 0.4f);
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		// knockback!
		if (target instanceof ShipAPI) {
			target.getVelocity().scale(0.85f); // 15% slow applied onHit
			
			CombatUtils.applyForce(target, projectile.getFacing(), projectile.getEmpAmount() * impulseMult.get(((ShipAPI) target).getHullSize())); // knockback!
			
			for (int i=0; i < 20; i++) {
				// done like this so i can force it to bring them (close) to overload, and not just waste the entire soft flux spike if near max flux
				((ShipAPI) target).getFluxTracker().increaseFlux(projectile.getEmpAmount() * 0.1f, false);
					// 20 x 40 = 800	But also scales with stat mods to wep damage :) 
			}
		}
		
		// do arcs!
		for (int i=0; i < 5; i++) {
			engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
					DamageType.ENERGY,
					projectile.getEmpAmount() * 0.15f, // damage
					projectile.getEmpAmount() * 0.7f, // emp
					500f, // max range
					"tachyon_lance_emp_impact",
					15f, // thickness
					new Color(165,20,100,155),
					new Color(255,255,255,160));
		}
		// outer arcs
		for (int i=0; i < 4; i++) {
			float distanceRandom1 = MathUtils.getRandomNumberInRange(20f, 48f);
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
			Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
			
			float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.8f, 1.2f);
			float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(80, 130);
			Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
			
			engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
					new Color(155,15,130,80),
					new Color(252,200,240,93));
			
		}
		
		
			// this does a pretty dang cool "swirly" effect (use it!!)
		ASF_RadialEmitter emitterSpc1 = new ASF_RadialEmitter(null);
      emitterSpc1.location(point);
      emitterSpc1.life(1.1f, 2.2f);
      emitterSpc1.size(2f, 3f);
      emitterSpc1.velocity(70f, -45f);
      emitterSpc1.distance(30f, 90f);
		emitterSpc1.color(STAGE_4B_COLOR.getRed(),STAGE_4B_COLOR.getGreen(),STAGE_4B_COLOR.getBlue(),STAGE_4B_COLOR.getAlpha());
		emitterSpc1.emissionOffset(40f, 15f);
		emitterSpc1.lifeLinkage(true);
		emitterSpc1.burst(69);
		
		ASF_RadialEmitter emitterSpc2 = new ASF_RadialEmitter(null);
      emitterSpc2.location(point);
      emitterSpc2.life(1.1f, 2.2f);
      emitterSpc2.size(2f, 3f);
      emitterSpc2.velocity(70f, -45f);
	    emitterSpc2.distance(30f, 90f);
		emitterSpc2.color(STAGE_4B_COLOR.getRed(),STAGE_4B_COLOR.getGreen(),STAGE_4B_COLOR.getBlue(),STAGE_4B_COLOR.getAlpha());
		emitterSpc2.emissionOffset(-55f, 15f);
		emitterSpc2.lifeLinkage(true);
		emitterSpc2.burst(69);
		
		
		engine.spawnExplosion(point, fxVel, STAGE_4B_COLOR, 135f, 0.9f);
		
		engine.addHitParticle(point, fxVel,
				200f,
				1f,
				0.1f,
				STAGE_4_COLOR.brighter());
		
		// "swirly neb"
		for (int i=0; i < 5; i++) {
			engine.addSwirlyNebulaParticle(point,
					fxVel,
					20f * i,
					1.8f,
					0.1f,
					0.36f,
					MathUtils.getRandomNumberInRange(1.3f, 1.5f),
					STAGE_4_COLOR.darker(),
					true);
		}
		
		// "core" particles
		for (int i=0; i < 18; i++) {
			Vector2f coreLoc = MathUtils.getRandomPointInCircle(point, 21f);
			
			engine.addSmoothParticle(coreLoc,
            		fxVel,
            		MathUtils.getRandomNumberInRange(37f, 48f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.75f, 1.1f), //duration
            		STAGE_4_COLOR);
		}
		
		// core glow sorta thing
		ASF_RadialEmitter emitterInner = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterInner.location(point);
		emitterInner.life(0.65f, 0.86f);
		emitterInner.size(6f, 10f);
		emitterInner.velocity(50f, -45f);
		emitterInner.distance(0f, 13f);
		emitterInner.color(STAGE_4B_COLOR.getRed(),STAGE_4B_COLOR.getGreen(),STAGE_4B_COLOR.getBlue(),STAGE_4B_COLOR.getAlpha());
		emitterInner.burst(40);
		
		// sparks
		ASF_RadialEmitter emitterOuter = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterOuter.location(point);
		emitterOuter.life(0.55f, 0.8f);
		emitterOuter.size(2f, 4f);
		emitterOuter.velocity(9f, 18f);
		emitterOuter.distance(30f, 25f);
		emitterOuter.color(STAGE_4_COLOR.getRed(),STAGE_4_COLOR.getGreen(),STAGE_4_COLOR.getBlue(),STAGE_4_COLOR.getAlpha());
		emitterOuter.emissionOffset(-3f, 3f);
		emitterOuter.velDistLinkage(false);
		emitterOuter.lifeLinkage(true);
		emitterOuter.burst(200);
		
	}
}