package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_SilahaOnHitEffect implements OnHitEffectPlugin {
	
	private static Map<HullSize, Float> impulseMult = new HashMap<HullSize, Float>();
	static {
		impulseMult.put(HullSize.FIGHTER, 0.2f);
		impulseMult.put(HullSize.FRIGATE, 0.45f);
		impulseMult.put(HullSize.DESTROYER, 0.55f);
		impulseMult.put(HullSize.CRUISER, 0.75f);
		impulseMult.put(HullSize.CAPITAL_SHIP, 0.9f);
		impulseMult.put(HullSize.DEFAULT, 0.6f);
	}
	
    private static final Color SPARK_COLOR = new Color(255,175,255,210);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
		if (target instanceof ShipAPI) {
			target.getVelocity().scale(0.85f); // 15% slow applied onHit
			
			CombatUtils.applyForce(target, projectile.getFacing(), projectile.getEmpAmount() * impulseMult.get(((ShipAPI) target).getHullSize())); // knockback!
			
			for (int i=0; i < 20; i++) {
				// done like this so i can force it to bring them (close) to overload, and not just waste the entire soft flux spike if near max flux
				((ShipAPI) target).getFluxTracker().increaseFlux(projectile.getEmpAmount() * 0.1f, false);
					// 20 x 40 = 800	But also scales with stat mods to wep damage :) 
			}
		}
		
        engine.spawnExplosion(point, fxVel, SPARK_COLOR, 33f, 0.7f);
        
        // arcs
		for (int i=0; i < 5; i++) {
	        Vector2f arcPoint = MathUtils.getPointOnCircumference(point, 30f + (i * 35f), MathUtils.getRandomNumberInRange(projectile.getFacing() -24f, projectile.getFacing() +24f));
	        
	        engine.spawnEmpArcVisual(point, target, arcPoint, target, 13f,
					new Color(90,175,100,125),
					new Color(255,175,255,140));
		}
		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.8f, 1.1f, point, fxVel);
        
		// frontal particles
		ASF_RadialEmitter emitterFront = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterFront.location(point);
		emitterFront.angle(projectile.getFacing() -25f, 50f);
		emitterFront.life(0.6f, 1.2f);
		emitterFront.size(4f, 10f);
		emitterFront.velocity(0f, 0f);
		emitterFront.distance(0f, 210f);
		emitterFront.color(SPARK_COLOR.darker().getRed(),SPARK_COLOR.darker().getGreen(),SPARK_COLOR.darker().getBlue(),SPARK_COLOR.darker().getAlpha());
		emitterFront.coreDispersion(6f);
        emitterFront.burst(80);
        
        // side particles
        ASF_RadialEmitter emitterSide1 = new ASF_RadialEmitter((CombatEntityAPI) target);
        emitterSide1.location(point);
        emitterSide1.angle(projectile.getFacing() + 79f, 10f);
        emitterSide1.life(0.6f, 1f);
        emitterSide1.size(2f, 9f);
        emitterSide1.velocity(30f, 145f);
        emitterSide1.color(SPARK_COLOR.darker().getRed(),SPARK_COLOR.darker().getGreen(),SPARK_COLOR.darker().getBlue(),190);
        emitterSide1.burst(20);
        ASF_RadialEmitter emitterSide2 = new ASF_RadialEmitter((CombatEntityAPI) target);
        emitterSide2.location(point);
        emitterSide2.angle(projectile.getFacing() - 89f, 10f);
        emitterSide2.life(0.6f, 1f);
        emitterSide2.size(2f, 9f);
        emitterSide2.velocity(30f, 145f);
        emitterSide2.color(SPARK_COLOR.darker().getRed(),SPARK_COLOR.darker().getGreen(),SPARK_COLOR.darker().getBlue(),190);
        emitterSide2.burst(20);
        
        // rear "jet" particles
        ASF_RadialEmitter emitterRear1 = new ASF_RadialEmitter((CombatEntityAPI) target);
        emitterRear1.location(point);
        emitterRear1.angle(projectile.getFacing() + 125f, 110f);
        emitterRear1.life(0.55f, 0.7f);
        emitterRear1.size(3f, 8f);
        emitterRear1.velocity(10f, 110f);
        emitterRear1.color(SPARK_COLOR.darker().getRed(),SPARK_COLOR.darker().getGreen(),SPARK_COLOR.darker().getBlue(),170);
        emitterRear1.burst(80);
        ASF_RadialEmitter emitterRear2 = new ASF_RadialEmitter((CombatEntityAPI) target);
        emitterRear2.location(point);
        emitterRear2.angle(projectile.getFacing() + 177f, 6f);
        emitterRear2.life(0.6f, 0.8f);
        emitterRear2.size(2f, 9f);
        emitterRear2.velocity(80f, 200f);
        emitterRear2.color(SPARK_COLOR.getRed(),SPARK_COLOR.getGreen(),SPARK_COLOR.getBlue(),SPARK_COLOR.getAlpha());
        emitterRear2.burst(20);
        
	}
}
