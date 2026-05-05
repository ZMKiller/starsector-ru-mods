package data.scripts;

import java.awt.Color;

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

public class rr_miserOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_X = new Color(200,150,120,125);
	private static final Color COLOR_P = new Color(255,210,190,255);
	private static final Color COLOR_P_F = new Color(250,210,190,180);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
			  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		float blastDamage = projectile.getDamageAmount() * 0.3333f;
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                31f,
                23f,
                blastDamage,
                blastDamage * 0.6f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                1f,
                4f,
                0.55f,
                29,
                COLOR_P_F,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(true);
        blast.setDetailedExplosionFlashColorCore(COLOR_P);
        blast.setDetailedExplosionFlashColorFringe(COLOR_X);
        blast.setUseDetailedExplosion(true);
        blast.setDetailedExplosionRadius(31f);
        blast.setDetailedExplosionFlashRadius(39f);
        blast.setDetailedExplosionFlashDuration(0.15f);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		for (int i=0; i < 21; i++) {
			Vector2f sparkVel1 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(43f, 127f), projectile.getFacing() + MathUtils.getRandomNumberInRange(150f, 210f));
			
			Global.getCombatEngine().addSmoothParticle(point,
					sparkVel1,
					MathUtils.getRandomNumberInRange(2f, 6f), //size
					0.8f, //brightness
					MathUtils.getRandomNumberInRange(0.5f, 0.65f), //duration
					COLOR_P);
		}
		
	}
}
