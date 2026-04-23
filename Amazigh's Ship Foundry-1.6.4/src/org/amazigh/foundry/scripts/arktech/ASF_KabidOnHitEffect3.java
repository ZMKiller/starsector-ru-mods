package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_KabidOnHitEffect3 implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (!projectile.isFading()) {
			
			WeaponAPI weapon = projectile.getWeapon();
			ShipAPI ship = projectile.getSource();
			
			float randomArc = MathUtils.getRandomNumberInRange(-4f, 4f);
        	
        	Vector2f vel = (Vector2f) (ship.getVelocity());
        	Vector2f RandomVel = MathUtils.getRandomPointOnCircumference(vel, MathUtils.getRandomNumberInRange(20f, 100f));
			
			Global.getCombatEngine().spawnProjectile(ship,
	                weapon,
	                "A_S-F_kabid_sub3",
	                weapon.getFirePoint(0),
	                weapon.getCurrAngle() + randomArc,
	                RandomVel);
			
			ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) ship);
            emitter.location(weapon.getFirePoint(0));
            emitter.angle(projectile.getFacing() -4.4f, 8.8f);
            emitter.life(0.3f, 0.45f);
            emitter.size(2f, 4f);
            emitter.velocity(8f, 147f);
            emitter.distance(2f, 31f);
            emitter.color(150,255,240,170);
            emitter.velDistLinkage(false);
            emitter.coreDispersion(4f);
            emitter.burst(32);
            			
	        engine.spawnEmpArcVisual(point, target, weapon.getFirePoint(0), ship, 10f,
					new Color(120,85,130,30),
					new Color(205,225,255,35));
			
			Global.getSoundPlayer().playSound("A_S-F_kabid_fire", 1.6f, 0.6f, ship.getLocation(), ship.getVelocity());
    		
    	}
		
	}
}