package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_KabidOnFireEffect implements OnFireEffectPlugin {

	private static final Color COLOR_MUZZLE = new Color(240,210,255,100);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
    	Vector2f vel = ship.getVelocity();
    	
        engine.spawnExplosion(weapon.getFirePoint(0), vel, COLOR_MUZZLE, 35f, 0.5f);
        
        ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) ship);
        emitter.location(weapon.getFirePoint(0));
        emitter.angle(projectile.getFacing() -1f, 2f);
        emitter.life(0.35f, 0.5f);
        emitter.size(2f, 4f);
        emitter.velocity(10f, 190f);
        emitter.distance(2f, 38f);
        emitter.color(150,255,240,200);
        emitter.velDistLinkage(false);
        emitter.coreDispersion(4f);
        emitter.burst(54);
        
    }
  }