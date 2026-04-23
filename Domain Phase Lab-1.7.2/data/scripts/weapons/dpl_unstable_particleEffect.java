package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
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
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class dpl_unstable_particleEffect extends BaseEveryFrameCombatPlugin implements OnFireEffectPlugin {

	//This must match whatever in the weapons.csv file
	public float WEAPON_COOLDOWN = 1f;
	
	public dpl_unstable_particleEffect() {
    }
	
	protected WeaponAPI weapon;
	public dpl_unstable_particleEffect(WeaponAPI weapon, float timeReduction) {
        this.weapon = weapon;
        this.timeReduction = timeReduction;
    }
	
	private float timeReduction = 0f;
    @Override
    public void advance(float amount, List events) {
        weapon.setRemainingCooldownTo(WEAPON_COOLDOWN - timeReduction);
        Global.getCombatEngine().removePlugin(this);
    }
	
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		if (ship == null) {
			return;
		}
		if (!ship.hasListenerOfClass(dpl_unstable_particleMod.class)) {
			ship.addListener(new dpl_unstable_particleMod(ship));
		}
		List<dpl_unstable_particleMod> listeners = ship.getListeners(dpl_unstable_particleMod.class);
		if (listeners.isEmpty()) return; // ???
			
		dpl_unstable_particleMod listener = listeners.get(0);
		listener.notifyFire(weapon);
		
		float timeReduction = 0f;
		if (listener.recentFires.getRemaining(weapon) >= 4f && listener.recentFires.getRemaining(weapon) < 11f) {
			timeReduction = 0.2f;
		} else if (listener.recentFires.getRemaining(weapon) >= 11f && listener.recentFires.getRemaining(weapon) <= 12f) {
			timeReduction = 0.5f;
		}
		
		dpl_unstable_particleEffect effect = new dpl_unstable_particleEffect(weapon, timeReduction);
		Global.getCombatEngine().addPlugin(effect);
	}
	
	public static class dpl_unstable_particleMod implements AdvanceableListener {
		//implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected TimeoutTracker<WeaponAPI> recentFires = new TimeoutTracker<WeaponAPI>();
		protected TimeoutTracker<WeaponAPI> aliveWeapons = new TimeoutTracker<WeaponAPI>();
		public dpl_unstable_particleMod(ShipAPI ship) {
		this.ship = ship;
		}
		
		public void notifyFire(WeaponAPI weapon) {
			recentFires.add(weapon, 2f, 12f);
			aliveWeapons.add(weapon, 2f, 2f);
		}
		
		public void advance(float amount) {
			recentFires.advance(amount);
			aliveWeapons.advance(amount);
			List<WeaponAPI> toRemove = new ArrayList<WeaponAPI>();
			for (WeaponAPI weapon : recentFires.getItems()) {
				if (aliveWeapons.getRemaining(weapon) <= 0.5f) {
					toRemove.add(weapon);
				}
			}
			
			if (!toRemove.isEmpty()) {
				for (WeaponAPI weapon : toRemove) {
					aliveWeapons.remove(weapon);
					recentFires.remove(weapon);
				}
			}
		}
	}
}




