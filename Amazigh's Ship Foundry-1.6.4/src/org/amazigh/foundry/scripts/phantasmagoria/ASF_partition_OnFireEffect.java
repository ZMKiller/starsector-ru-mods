package org.amazigh.foundry.scripts.phantasmagoria;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_partition_OnFireEffect implements OnFireEffectPlugin {

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
		for (int i=0; i < 3; i++) {
			engine.addNebulaSmokeParticle(MathUtils.getRandomPointInCircle(projectile.getLocation(), 2f),
					MathUtils.getPointOnCircumference(weapon.getShip().getVelocity(), MathUtils.getRandomNumberInRange(18f, 24f), projectile.getFacing()),
					20f, //size
					1.6f, //end mult
					0.4f, //ramp fraction
					0.5f, //full bright fraction
					0.7f, //duration
					new Color(240,75,165,110));
		}
    	
    	Vector2f projOrient = MathUtils.getPointOnCircumference(null, 10f, projectile.getFacing());
    	Vector2f shipOrient = MathUtils.getPointOnCircumference(null, 10f, weapon.getShip().getFacing());
    	Vector2f midPoint = MathUtils.getMidpoint(projOrient, shipOrient);
    	
    	projectile.setFacing(VectorUtils.getFacing(midPoint));
    }
  }