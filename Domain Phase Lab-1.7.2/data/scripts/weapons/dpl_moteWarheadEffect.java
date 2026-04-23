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
public class dpl_moteWarheadEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (!ship.isFighter() && !ship.isDrone()) {
				if (shieldHit) {
					float dam = 175f; // this should match the value in the weapons.csv highlights.
					ship.getFluxTracker().increaseFlux(dam, true);
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
		}
		
		String impactSoundId = "mote_attractor_impact_damage";
		Global.getSoundPlayer().playSound(impactSoundId, 1f, 1f, point, new Vector2f());
	}
}




