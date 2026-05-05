package data.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class rr_WrathOnHitEffect implements OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		for (int i=0; i < 16; i++) {
			
			Vector2f origin = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(40f, 180f));
			
			Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(75f, 150f));
			
			engine.spawnProjectile(projectile.getSource(),
					projectile.getWeapon(), "rr_wrath_burst",
					origin,
					projectile.getFacing(),
					randomVel);
			
			for (int j=0; j < 3; j++) {
    			// smoke particles
                Vector2f smokeVel = MathUtils.getRandomPointInCircle(null, 23f);
                Vector2f point1 = MathUtils.getRandomPointInCircle(origin, 43f);
                
                engine.addNebulaSmokeParticle(point1,
                		smokeVel,
                		53f, //size
                		MathUtils.getRandomNumberInRange(1.6f, 1.9f), //end mult
                		0.5f, //ramp fraction
                		0.6f, //full bright fraction
                		MathUtils.getRandomNumberInRange(1.9f, 2.5f), //duration
                		new Color(166,118,94,77));
            }
		}
	}
}
