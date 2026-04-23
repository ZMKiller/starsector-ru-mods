package org.amazigh.foundry.scripts.phantasmagoria;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_curtain_OnFireEffect implements OnFireEffectPlugin {

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
		engine.addNebulaSmokeParticle(MathUtils.getRandomPointInCircle(projectile.getLocation(), 2f),
				MathUtils.getPointOnCircumference(weapon.getShip().getVelocity(), 15f, projectile.getFacing()),
				15f, //size
				1.6f, //end mult
				0.4f, //ramp fraction
				0.5f, //full bright fraction
				0.55f, //duration
				new Color(150,75,255,110));
    }
  }