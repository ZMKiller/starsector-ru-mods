package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class ASF_PlasBursterOnExplEffect implements ProximityExplosionEffect {

	private static final Color COLOR_P = new Color(140,90,255,220);
	private static final Color COLOR_X = new Color(215,0,130,50);
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = explosion.getLocation();
 
		engine.addHitParticle(
                point,
                explosion.getVelocity(),
                64f,
                0.8f,
                0.1f,
                COLOR_P);
		
        for (int i = 0; i < 4; i++) {
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 13f), // 11
    				MathUtils.getRandomNumberInRange(23f, 41f), //size  23,61
    				1.85f, //endSizeMult  1.75
    				0f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1f, 1.6f), //dur
    				COLOR_X);
        }
        
        ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) explosion);
        emitter.location(point);
        emitter.life(0.9f, 1.3f);
        emitter.size(1f, 4f);
        emitter.velocity(0f, 4f);
        emitter.distance(0f, 35f);
        emitter.color(200,90,245,195);
        emitter.burst(24);
	}
}



