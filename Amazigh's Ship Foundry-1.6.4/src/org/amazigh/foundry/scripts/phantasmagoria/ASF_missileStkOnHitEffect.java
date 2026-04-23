package org.amazigh.foundry.scripts.phantasmagoria;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
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

public class ASF_missileStkOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(220,100,170,200);
    private static final Color COLOR_X = new Color(255,200,225,175);
    
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                160f,
                80f,
                projectile.getDamageAmount() * 0.25f,
                projectile.getDamageAmount() * 0.125f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                1f,
                7f,
                1.25f,
                40,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.ENERGY);
        blast.setShowGraphic(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,true);
		
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitter.location(point);
		emitter.life(1.1f, 1.3f);
		emitter.size(1f, 8f);
		emitter.velocity(9f, 114f); // 3 +((2-40) *3) 
		emitter.distance(2f, 38f);
		emitter.color(220,100,170,200);
		emitter.velDistLinkage(false);
		emitter.burst(40);
		
        Global.getCombatEngine().addSmoothParticle(point,
        		fxVel,
        		160f, //size
        		1.0f, //brightness
        		0.1f, //duration
        		COLOR_X);
		
        for (int i=0; i < 2; i++) {
    		engine.addNebulaSmokeParticle(MathUtils.getRandomPointInCircle(point, 5f),
    				MathUtils.getRandomPointInCircle(fxVel, 5f),
    				48f, //size
    				2f, //end mult
    				0.4f, //ramp fraction
    				0.65f, //full bright fraction
    				1.5f, //duration
    				new Color(220,100,170,110));
        	
            float distanceRandom1 = MathUtils.getRandomNumberInRange(28f, 50f);
    		float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
            
            float distanceRandom2 = MathUtils.getRandomNumberInRange(28f, 50f);
            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(140, 220);
            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
            
            engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 9f,
    				new Color(170,65,150,35),
    				new Color(255,200,225,40));
        }
        
	}
}
