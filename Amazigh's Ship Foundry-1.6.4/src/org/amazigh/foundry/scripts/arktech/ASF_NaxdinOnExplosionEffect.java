package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class ASF_NaxdinOnExplosionEffect implements ProximityExplosionEffect {
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = explosion.getLocation();
		
		for (int i=0; i < 3; i++) {

			Vector2f origin = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(3f, 36f));
			
			for (int j=0; j < 2; j++) {
				float arcRandomBolt = MathUtils.getRandomNumberInRange(-180, 180);
				Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(5f, 10f));
				
				engine.spawnProjectile(originalProjectile.getSource(),
						explosion.getWeapon(), "A_S-F_naxdin_bolt",
	                     origin,
	                     arcRandomBolt,
	                     randomVel);
			}
			
			MagicLensFlare.createSharpFlare(
				    engine,
				    explosion.getSource(),
				    origin,
				    5,
				    220,
				    MathUtils.getRandomNumberInRange(-10, 10),
				    new Color(50,105,90),
					new Color(160,200,200));
			
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
            float distanceRandom1 = MathUtils.getRandomNumberInRange(36f, 90f);
            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
            
            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(40, 80);
            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
            
            engine.spawnEmpArcVisual(arcPoint1, explosion, arcPoint2, explosion, 11f,
					new Color(70,155,130,50),
					new Color(225,255,255,55));
		}
		
        Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.75f, 0.7f, point, explosion.getVelocity());
	}
}



