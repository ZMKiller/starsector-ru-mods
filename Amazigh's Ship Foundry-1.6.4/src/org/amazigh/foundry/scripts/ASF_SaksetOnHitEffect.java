package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_SaksetOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(225,195,175,180);
	private static final Color COLOR_X = new Color(95,165,55,160);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			if(target instanceof MissileAPI){
				
	        } else {
				fxVel.set(target.getVelocity());
	        }
		}
        
		
		// shrapnel spawning
		for (int i=0; i < 24; i++) {
			
			float rearRandom = projectile.getFacing() + MathUtils.getRandomNumberInRange(150f, 210f);
			float arcRandom = (i * 15f) + MathUtils.getRandomNumberInRange(0, 15f);
            Vector2f origin = MathUtils.getPointOnCircumference(point, 18f, rearRandom);
            Vector2f fragVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(-75f, 150f), arcRandom);
            engine.spawnProjectile(projectile.getSource(),
                        projectile.getWeapon(), "A_S-F_sakset_frag",
                         origin,
                         arcRandom,
                         fragVel);
		}
		
		
		//fx spawning
		float blastDamage = projectile.getDamageAmount() * 0.06f;
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                71f,
                35f,
                blastDamage,
                blastDamage,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                4f,
                0.75f,
                35,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(true);
        blast.setUseDetailedExplosion(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        	// ok so this does 30 frag damage, and we only spawn 24 projectiles, so the total damage is the same as listed, sort of.
        	// doing it like this to keep the explosion vfx, as it's part of the idea!
        
		for (int i=0; i < 10; i++) {
			
			float arcRandom = projectile.getFacing() + 150f + (i * 15f) + MathUtils.getRandomNumberInRange(0, 15);
			
			for (int j=0; j < 9; j++) {
				// some rearwards smoke particles
				
	            float angle1 = arcRandom + MathUtils.getRandomNumberInRange(-6f, 6f);
	            Vector2f smokeVel = MathUtils.getPointOnCircumference(fxVel, j * 7f, angle1);
	            Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), j + 2f, angle1);
	            engine.addNebulaSmokeParticle(point1,
	            		smokeVel,
	            		7f, //size
	            		MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
	            		0.6f, //ramp fraction
	            		0.5f, //full bright fraction
	            		MathUtils.getRandomNumberInRange(0.65f, 0.8f) * (0.5f + (0.05f * j)), //duration
	            		new Color(230,220,195,75));
			}
			
			// also some "general" smoke
			engine.addNebulaParticle(point,
					MathUtils.getRandomPointInCircle(null, 16f),
					MathUtils.getRandomNumberInRange(60f, 110f),
					2f,
					0.1f,
					0.3f,
					MathUtils.getRandomNumberInRange(0.9f, 1.3f),
					new Color(23,22,19,110),
					true);
			
		}
		
	}
}
