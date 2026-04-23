package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_KufikiriBeamEffect implements BeamEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.1f, 0.1f);
	private IntervalUtil particleInterval = new IntervalUtil(0.05f, 0.05f);
	private boolean wasZero = true;
	private boolean FIRED = false;
	private static final Color COLOR_HE = new Color(250,100,100,155);
	private static final Color COLOR_KE = new Color(255,210,210,155);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		
		if (target instanceof ShipAPI) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			
			particleInterval.advance(dur);
			if (particleInterval.intervalElapsed()) {
				Vector2f point = beam.getRayEndPrevFrame();
	            for (int i=0; i < 2; i++) {
	                  Vector2f randomVel = MathUtils.getRandomPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(25f, 50f));
	                  
	                  float randomSize = MathUtils.getRandomNumberInRange(9f, 18f);
	                  Global.getCombatEngine().addSmoothParticle(point,
	                      randomVel,
	                      randomSize, //size
	                      1.0f, //brightness
	                      0.45f, //duration
	                      new Color(195,50,50,175));
	              }
			}
			
			fireInterval.advance(dur);
			if (fireInterval.intervalElapsed() && !FIRED) {
				FIRED = true;
				WeaponAPI weapon = beam.getWeapon();
				ShipAPI ship = weapon.getShip();
				Vector2f vel = ship.getVelocity();
				
				boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
				
				if (hitShield) {
					for (int i=0; i < 3; i++) {
						float kufikiriKEAngle = (i * 14f) - 14f;
						CombatEntityAPI projK = engine.spawnProjectile(ship, weapon, "A_S-F_kufikiri_KE_sub", beam.getFrom(), weapon.getCurrAngle() + kufikiriKEAngle, vel);
						engine.addPlugin(new ASF_KufikiriKEProjScript((DamagingProjectileAPI) projK, target));
					}
					Global.getSoundPlayer().playSound("A_S-F_kufikiri_fire", 1.1f, 1.0f, beam.getFrom(), vel);
					engine.spawnExplosion(beam.getFrom(), vel, COLOR_KE, 40f, 0.5f);   
				} else {
					CombatEntityAPI projH = engine.spawnProjectile(ship, weapon, "A_S-F_kufikiri_HE_sub", beam.getFrom(), weapon.getCurrAngle() +  MathUtils.getRandomNumberInRange(3f, -3f), vel);
					engine.addPlugin(new ASF_KufikiriHEProjScript((DamagingProjectileAPI) projH, target));
					Global.getSoundPlayer().playSound("A_S-F_kufikiri_fire", 0.9f, 1.0f, beam.getFrom(), vel);
					engine.spawnExplosion(beam.getFrom(), vel, COLOR_HE, 40f, 0.5f);
					
				}
			}
		}
	}
		
}