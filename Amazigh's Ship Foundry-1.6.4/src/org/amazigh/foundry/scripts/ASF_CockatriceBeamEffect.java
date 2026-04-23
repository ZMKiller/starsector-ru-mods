package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_CockatriceBeamEffect implements BeamEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.15f, 0.25f); // ~22 arcs (average)
	private IntervalUtil arcInterval = new IntervalUtil(0.2f, 0.3f);
	private boolean wasZero = true;
	private float speedMult = 0.95f;
	private float spinMult = 0.95f;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		
		// spawn visual arcs around the missile when firing, because: it looks cool :)
		float dur = beam.getDamage().getDpsDuration();
		arcInterval.advance(dur);
    	if (arcInterval.intervalElapsed()) {
    		Vector2f vel = new Vector2f();
    		Vector2f loc = beam.getFrom();
        	
    		float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
            float distanceRandom1 = MathUtils.getRandomNumberInRange(16f, 33f);
            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(loc, distanceRandom1, angleRandom1);
            
            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(50, 130);
            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(loc, distanceRandom2, angleRandom2);
            
            engine.spawnEmpArcVisual(arcPoint1, beam.getSource(), arcPoint2, beam.getSource(), beam.getWidth() * 0.6f,
            		beam.getFringeColor(),
            		beam.getCoreColor());
            
    		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.9f, 0.4f, loc, vel);
    		
    		for (int i=0; i < 2; i++) {
    			engine.addNebulaParticle(loc,
        				MathUtils.getRandomPointInCircle(null, 12f),
        				MathUtils.getRandomNumberInRange(40f, 80f),
        				2f,
        				0.2f,
        				0.5f,
        				MathUtils.getRandomNumberInRange(0.6f, 0.8f),
        				new Color(12,21,25,90),
        				true);
    		}
    	}
		
    	// arcs!
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			fireInterval.advance(dur);
			
			if (fireInterval.intervalElapsed()) {
				ShipAPI ship = (ShipAPI) target;
				boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
				float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
				
				boolean piercedShield = hitShield && (float) Math.random() < pierceChance;
				//piercedShield = true;
				
				if (!hitShield || piercedShield) {
					Vector2f point = beam.getRayEndPrevFrame();
					float dam = beam.getDamage().getDamage() * 0.25f;
					float emp = beam.getDamage().getFluxComponent() * 0.4f;
					engine.spawnEmpArcPierceShields(
									   beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
									   DamageType.ENERGY,
									   dam, // damage
									   emp, // emp 
									   1000f, // max range 
									   "tachyon_lance_emp_impact",
									   beam.getWidth(),
									   beam.getFringeColor(),
									   beam.getCoreColor()
									   );
				}
				
				// we also *slow* the target when we do an arc, because it's Funny.
				Vector2f tagVel = target.getVelocity();
				
	    		tagVel.x = tagVel.x * speedMult;
	    		tagVel.y = tagVel.y * speedMult;
	    		target.getVelocity().set(tagVel);
	    		
	    		float angVel = target.getAngularVelocity();
	            angVel *= spinMult;
	    		target.setAngularVelocity(angVel);
				
			}
		}
	}
}
