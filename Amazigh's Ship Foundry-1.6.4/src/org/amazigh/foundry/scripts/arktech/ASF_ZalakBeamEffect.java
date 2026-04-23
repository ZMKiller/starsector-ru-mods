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

public class ASF_ZalakBeamEffect implements BeamEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.3f, 0.3f);
	private IntervalUtil arcInterval = new IntervalUtil(0.15f, 0.2f);
	private boolean wasZeroArc = true;
	private boolean wasZero = true;
	private boolean FIRED = false;
	private static final Color COLOR_X = new Color(100,250,170,155);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		
		if (target instanceof ShipAPI) {
			float arcDur = beam.getDamage().getDpsDuration();
			if (!wasZeroArc) arcDur = 0;
			wasZeroArc = beam.getDamage().getDpsDuration() <= 0;
			arcInterval.advance(arcDur);
			
			if (arcInterval.intervalElapsed()) {
				Vector2f point = beam.getRayEndPrevFrame();
				
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
	            float distanceRandom1 = MathUtils.getRandomNumberInRange(24f, 48f);
	            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
	            
	            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(40, 80);
	            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
	            
	            engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 10f,
						new Color(70,155,130,50),
						new Color(225,255,255,55));
	            
	            for (int i=0; i < 5; i++) {
	                  Vector2f randomVel = MathUtils.getRandomPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(25f, 60f));
	                  
	                  float randomSize = MathUtils.getRandomNumberInRange(10f, 25f);
	                  Global.getCombatEngine().addSmoothParticle(point,
	                      randomVel,
	                      randomSize, //size
	                      1.0f, //brightness
	                      0.45f, //duration
	                      new Color(70,155,130,175));
	              }
	            
			}
			
			if (beam.getBrightness() >= 1f) {
				float dur = beam.getDamage().getDpsDuration();
				// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
				if (!wasZero) dur = 0;
				wasZero = beam.getDamage().getDpsDuration() <= 0;
				fireInterval.advance(dur);
				
				if (fireInterval.intervalElapsed() && !FIRED) {
					FIRED = true;
					WeaponAPI weapon = beam.getWeapon();
					for (int i=0; i < 7; i++) {
						float angle = weapon.getCurrAngle() + MathUtils.getRandomNumberInRange(-13f, 13f);
		                Vector2f zalakRandomVel = MathUtils.getPointOnCircumference(weapon.getShip().getVelocity(), MathUtils.getRandomNumberInRange(-69f, 69f), angle);
		                CombatEntityAPI projZ = engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_zalak_bolt", beam.getFrom(), angle, zalakRandomVel);
		                engine.addPlugin(new ASF_ZalakProjScript((DamagingProjectileAPI) projZ, target));
		            }
					
					Global.getSoundPlayer().playSound("heavy_blaster_fire", 0.8f, 0.9f, beam.getFrom(), weapon.getShip().getVelocity());
		            engine.spawnExplosion(beam.getFrom(), weapon.getShip().getVelocity(), COLOR_X, 40f, 0.5f);   
				}
			}
		}
			
		
	}
}