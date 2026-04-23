package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_SaksetOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
    	float angle = projectile.getFacing();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = projectile.getLocation();
    	
    	// explosion effect
    	engine.spawnExplosion(proj_location, ship_velocity, new Color(210,200,175,100), 40f, 0.9f); // 185,225,175
    	
    	for (int h=0; h < 16; h++) {
    		// left/right smoke spray
    		float angleL1 = angle + MathUtils.getRandomNumberInRange(-60f, -50f);
    		Vector2f smokeVelL = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 22f), angleL1);
    		Vector2f pointL1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(-1f, 24f), angleL1);
    		engine.addNebulaSmokeParticle(pointL1,
    				smokeVelL,
    				10f, //size
    				MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
    				0.6f, //ramp fraction
    				0.5f, //full bright fraction
    				MathUtils.getRandomNumberInRange(0.6f, 1.1f), //duration
    				new Color(120,105,90,75));
    		float angleR1 = angle + MathUtils.getRandomNumberInRange(50f, 60f);
    		Vector2f smokeVelR = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 22f), angleR1);
    		Vector2f pointR1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(-1f, 24f), angleR1);
    		engine.addNebulaSmokeParticle(pointR1,
    				smokeVelR,
    				10f, //size
    				MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
    				0.6f, //ramp fraction
    				0.5f, //full bright fraction
    				MathUtils.getRandomNumberInRange(0.6f, 1.1f), //duration
    				new Color(120,105,90,75));
    		
        	for (int i=0; i < 3; i++) {
        		// core smoke spray
        		float angle1 = angle + MathUtils.getRandomNumberInRange(-9f, 9f);
        		Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 67f), angle1);
        		Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(-1f, 74f), angle1);
        		engine.addNebulaSmokeParticle(point1,
        				smokeVel,
        				10f, //size
        				MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
        				0.6f, //ramp fraction
        				0.5f, //full bright fraction
        				MathUtils.getRandomNumberInRange(0.7f, 1.25f), //duration
        				new Color(120,105,90,75));       		
        	}
    	}
    	
		// left/right particle jets
    	ASF_RadialEmitter emitterJetL = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	emitterJetL.location(proj_location);
    	emitterJetL.angle(projectile.getFacing() -61f, 12f);
    	emitterJetL.life(0.6f, 0.96f);
    	emitterJetL.size(2f, 5f);
    	emitterJetL.velocity(4f, 56f);
		emitterJetL.distance(2f, 18f);
		emitterJetL.color(250,160,80,150);
		emitterJetL.velDistLinkage(false);
		emitterJetL.coreDispersion(3f);
		emitterJetL.burst(32);
		ASF_RadialEmitter emitterJetR = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	emitterJetR.location(proj_location);
    	emitterJetR.angle(projectile.getFacing() + 49f, 12f);
    	emitterJetR.life(0.6f, 0.96f);
    	emitterJetR.size(2f, 5f);
    	emitterJetR.velocity(4f, 56f);
		emitterJetR.distance(2f, 18f);
		emitterJetR.color(250,160,80,150);
		emitterJetR.velDistLinkage(false);
		emitterJetR.coreDispersion(3f);
		emitterJetR.burst(32);
    	
    	// core particle jet
    	ASF_RadialEmitter emitterJet = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	emitterJet.location(proj_location);
    	emitterJet.angle(projectile.getFacing() -10f, 20f);
    	emitterJet.life(0.9f, 1.2f);
		emitterJet.size(2f, 6f);
		emitterJet.velocity(5f, 85f);
		emitterJet.distance(2f, 28f);
		emitterJet.color(250,160,80,160);
		emitterJet.velDistLinkage(false);
		emitterJet.coreDispersion(3f);
		emitterJet.burst(80);
		
    }
  }