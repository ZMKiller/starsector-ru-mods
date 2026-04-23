package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class ASF_BrambleOnExplEffect implements ProximityExplosionEffect {

	private static final Color COLOR_P = new Color(255,155,125,220);
	private static final Color COLOR_X1 = new Color(250,215,123,50);
	private static final Color COLOR_X2 = new Color(175,145,130,50);
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = explosion.getLocation();
 
		engine.addHitParticle(
                point,
                explosion.getVelocity(),
                80f,
                0.8f,
                0.1f,
                COLOR_P);
		
		engine.addNebulaParticle(point,
				explosion.getVelocity(),
				MathUtils.getRandomNumberInRange(48f, 60f), //size
				1.5f, //endSizeMult
				0f, //rampUpFraction
				0.35f, //fullBrightnessFraction
				MathUtils.getRandomNumberInRange(0.3f, 0.4f), //dur
				COLOR_X1);
		
        for (int i = 0; i < 4; i++) {
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(explosion.getVelocity(), 19f),
    				MathUtils.getRandomNumberInRange(52f, 93f), //size
    				1.8f, //endSizeMult
    				0f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1f, 1.6f), //dur
    				COLOR_X2);
        	
        }
	}
}



