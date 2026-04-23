package data.scripts.weapons;

import java.awt.Color;
import java.util.List;

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
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

import data.scripts.weapons.dpl_chantsEffect.ChantsDamageTakenMod;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class dpl_null_swarmEffect implements OnHitEffectPlugin, OnFireEffectPlugin {
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		((MissileAPI) projectile).setMine(true);
		((MissileAPI) projectile).setEmpResistance(10000);
		((MissileAPI) projectile).setEccmChanceOverride(1f);
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (ship.getOwner() != projectile.getOwner()) {
				float dam = projectile.getDamageAmount();
				if (!shieldHit) {
					if (!ship.hasListenerOfClass(dpl_null_swarmHitMod.class)) {
						ship.addListener(new dpl_null_swarmHitMod(ship));
					}
					List<dpl_null_swarmHitMod> listeners = ship.getListeners(dpl_null_swarmHitMod.class);
					if (listeners.isEmpty()) return; // ???
						
					dpl_null_swarmHitMod listener = listeners.get(0);
					listener.notifyHit(projectile);
				} else {
					ship.getFluxTracker().increaseFlux(dam, true);
				}
			}
		}
		
		String impactSoundId = "mote_attractor_impact_damage";
		Global.getSoundPlayer().playSound(impactSoundId, 1f, 1f, point, new Vector2f());
	}
	
	public static class dpl_null_swarmHitMod implements AdvanceableListener {
		//implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected TimeoutTracker<DamagingProjectileAPI> recentHits = new TimeoutTracker<DamagingProjectileAPI>();
		public dpl_null_swarmHitMod(ShipAPI ship) {
		this.ship = ship;
		//ship.addListener(new GravitonBeamDamageTakenModRemover(ship));
		}
		
		public void notifyHit(DamagingProjectileAPI proj) {
			if (recentHits.getItems().size() < 20f) {
				recentHits.add(proj, 600f, 600f);
			}
		}
		
		public void advance(float amount) {
			recentHits.advance(amount);
		
			int hits = recentHits.getItems().size();
		
			if (hits > 0) {
				ship.getMutableStats().getWeaponMalfunctionChance().modifyFlat("dpl_null_swarm_hit", hits * 0.05f);
				ship.getMutableStats().getCriticalMalfunctionChance().modifyFlat("dpl_null_swarm_hit", hits * 0.05f);
			}
		}
	}
}




