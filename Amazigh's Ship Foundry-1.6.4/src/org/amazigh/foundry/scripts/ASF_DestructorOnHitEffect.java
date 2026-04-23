package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_DestructorOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(255,125,0,255);
    private static final Color COLOR_X = new Color(255,90,0,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                100f,
                60f,
                projectile.getDamageAmount()/5f,
                projectile.getDamageAmount()/10f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                3f,
                4f,
                0.6f,
                80,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,true);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
		ASF_RadialEmitter emitterPulse = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterPulse.location(point);
		emitterPulse.life(0.5f, 0.7f);
		emitterPulse.size(3f, 7f);
		emitterPulse.velocity(19.5f, 123.5f);
		emitterPulse.distance(3f, 22f);
		emitterPulse.color(255,125,0,255);
		emitterPulse.burst(80);
		
        Global.getCombatEngine().addSmoothParticle(point,
        		fxVel,
        		100f, //size
        		1.0f, //brightness
        		0.1f, //duration
        		COLOR_X);
        
	}
}
