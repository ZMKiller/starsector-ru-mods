package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_TereshkovOnHitEffect implements OnHitEffectPlugin {
	private static final Color COLOR_U = new Color(170,50,30,200);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
        engine.spawnExplosion(point, fxVel, COLOR_U, 90f, 1.45f);
		
//        for (int i=0; i < 15; i++) {
//            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(25f, 60f));
//            
//            float randomSize = MathUtils.getRandomNumberInRange(10f, 25f);
//            float randomTime = MathUtils.getRandomNumberInRange(0.85f, 1.25f);
//            Global.getCombatEngine().addSmoothParticle(point,
//                randomVel,
//                randomSize, //size
//                1.0f, //brightness
//                randomTime, //duration
//                new Color(255,190,100,255));
//        }
        
        ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) target);
        emitter.location(point);
		emitter.life(0.85f, 1.25f);
		emitter.size(10f, 25f);
		emitter.velocity(24f, 35f);
		emitter.color(255,190,100,255);
		emitter.lifeLinkage(false);
		emitter.burst(15);
        
        for (int i=0; i < 5; i++) {
			
            float arcRandom = (i * 18f) - 36f + MathUtils.getRandomNumberInRange(-4f, 4f);
            
            Vector2f origin = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(40f, 55f), projectile.getFacing() + 180f + arcRandom);
            
            engine.spawnProjectile(projectile.getSource(),
                        projectile.getWeapon(), "A_S-F_tereshkov_onhit",
                         origin,
                         projectile.getFacing() + arcRandom,
                         null);
          }
        
		Global.getSoundPlayer().playSound("system_canister_flak_explosion", 0.70f, 1.1f, point, fxVel);
	}
}
