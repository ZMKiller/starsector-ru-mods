package org.amazigh.foundry.scripts.phantasmagoria;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_shroud_OnFireEffect implements OnFireEffectPlugin {

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
		engine.addNebulaSmokeParticle(MathUtils.getRandomPointInCircle(projectile.getLocation(), 3f),
				MathUtils.getPointOnCircumference(weapon.getShip().getVelocity(), 10f, projectile.getFacing()),
				10f, //size
				1.6f, //end mult
				0.4f, //ramp fraction
				0.5f, //full bright fraction
				0.35f, //duration
				new Color(85,140,255,110));
    }
  }