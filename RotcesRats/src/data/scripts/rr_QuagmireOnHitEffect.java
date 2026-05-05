package data.scripts;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class rr_QuagmireOnHitEffect implements ProximityExplosionEffect {
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f point = explosion.getLocation();
        
        for (int i = 0; i < explosion.getDamageAmount(); i+=10) {
        	
        	float angleRandom = MathUtils.getRandomNumberInRange(-180, 180);
            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(5f, 75f));
            
            engine.spawnProjectile(explosion.getSource(),
					explosion.getWeapon(), "rr_quagmire_splinter",
                     point,
                     angleRandom,
                     randomVel);   
        }
	}
}



