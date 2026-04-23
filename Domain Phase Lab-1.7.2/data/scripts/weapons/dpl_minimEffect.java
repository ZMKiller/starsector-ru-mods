package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.loading.MissileSpecAPI;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class dpl_minimEffect implements OnHitEffectPlugin, OnFireEffectPlugin {
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		((MissileAPI) projectile).setMine(true);
		((MissileAPI) projectile).setEmpResistance(1000);
		((MissileAPI) projectile).setEccmChanceOverride(1f);
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (!ship.isFighter() && !ship.isDrone()) {
				float pierceChance = 1f;
				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
				boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
				
				if (!shieldHit || piercedShield) {
					float emp = projectile.getEmpAmount();
					float dam = projectile.getDamageAmount()*0.75f; // this should be 1 for regular and a bunch for high-frequency
					
					engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
									   projectile.getDamageType(), 
									   dam,
									   emp, // emp 
									   100000f, // max range 
									   "mote_attractor_impact_emp_arc",
									   20f, // thickness
									   new Color(175,70,255,255),
									   new Color(255,255,255,255)
									   );
				}				
			} else {
				float damage = 1000f;
				Global.getCombatEngine().applyDamage(projectile, ship, point, 
						damage, DamageType.ENERGY, 0f, false, false, projectile.getSource(), true);
			}
		} else if (target instanceof MissileAPI) {
			float damage = 1000f;
			Global.getCombatEngine().applyDamage(projectile, target, point, 
					damage, DamageType.ENERGY, 0f, false, false, projectile.getSource(), true);
			float MaxHP = target.getMaxHitpoints();
			if (MaxHP >= 5000) {
				Global.getCombatEngine().applyDamage(projectile, target, point, 
						MaxHP*10f, DamageType.HIGH_EXPLOSIVE, 0f, false, false, projectile.getSource(), true);
			}
		}
		
		String impactSoundId = "mote_attractor_impact_damage";
		Global.getSoundPlayer().playSound(impactSoundId, 1f, 1f, point, new Vector2f());
	}
}




