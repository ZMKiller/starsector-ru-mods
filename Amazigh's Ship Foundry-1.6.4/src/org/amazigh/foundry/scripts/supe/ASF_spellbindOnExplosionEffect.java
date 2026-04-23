package org.amazigh.foundry.scripts.supe;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import org.magiclib.util.MagicLensFlare;

public class ASF_spellbindOnExplosionEffect implements ProximityExplosionEffect {
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f point = explosion.getLocation();
        Vector2f fxVel = MathUtils.getRandomPointOnCircumference(null, 4f);
        
        engine.addNebulaParticle(point,
				fxVel,
				MathUtils.getRandomNumberInRange(69f, 85f), //60-75
				MathUtils.getRandomNumberInRange(1.5f, 2.0f),
				0.8f,
				0.4f,
				MathUtils.getRandomNumberInRange(0.75f, 0.9f),
				new Color(220,230,140,150),
				false);
        
        MagicLensFlare.createSharpFlare(
			    engine,
			    explosion.getSource(),
			    point,
			    4,
			    169, //160
			    MathUtils.getRandomNumberInRange(0f, 180f),
			    new Color(90,105,50),
				new Color(190,200,160));
        
		//play sound
		Global.getSoundPlayer().playSound("explosion_from_damage", 1.4f, 0.45f, point, fxVel); // "A_S-F_explosion_p_flak", 1f, 1f
				// explosion_missile
        Global.getSoundPlayer().playSound("hit_heavy_energy", 1.3f, 0.45f, point, fxVel);
        
	}
}



