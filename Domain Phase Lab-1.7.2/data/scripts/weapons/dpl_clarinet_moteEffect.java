package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

import data.hullmods.dpl_ClarinetMote.dpl_ClarinetMoteData;
import data.scripts.weapons.dpl_clarinet_maingunEffect.dpl_clarinetMaingunHitMod;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class dpl_clarinet_moteEffect implements OnHitEffectPlugin, OnFireEffectPlugin {
	
	public static String dpl_clarinetMote_DATA_KEY = "dpl_clarinet_mote_data_key";
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		((MissileAPI) projectile).setMine(true);
		((MissileAPI) projectile).setEmpResistance(1000);
		((MissileAPI) projectile).setEccmChanceOverride(1f);
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (projectile.getWeapon() == null) return;
		
		ShipAPI SelfShip = projectile.getWeapon().getShip();
		if (SelfShip == null) return;
		
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (ship.getOwner() != projectile.getOwner()) {
				if (ship.hasListenerOfClass(dpl_clarinetMaingunHitMod.class)) {
					int PlasmaPotential = 1;
					List<dpl_clarinetMaingunHitMod> listeners = ship.getListeners(dpl_clarinetMaingunHitMod.class);
					if (!listeners.isEmpty()) {
						dpl_clarinetMaingunHitMod listener = listeners.get(0);
						if (!listener.recentHits.getItems().isEmpty()) {
							PlasmaPotential = listener.recentHits.getItems().size();
						}
					}
					
					String key = dpl_clarinetMote_DATA_KEY + "_" + SelfShip.getId();
					dpl_ClarinetMoteData data = (dpl_ClarinetMoteData) engine.getCustomData().get(key);
					if (data == null) return;
					
					if (PlasmaPotential == 1) {
						data.count += 1;
					} else if (PlasmaPotential == 2) {
						data.count += 1;
						if (shieldHit) {
							ship.getFluxTracker().increaseFlux(projectile.getDamageAmount(), true);
						}
					} else if (PlasmaPotential == 3) {
						data.count += 1;
						if (!shieldHit) {
							engine.applyDamage(ship, point, projectile.getDamageAmount(), DamageType.ENERGY, 0f, true, false, SelfShip);
						}
					} else if (PlasmaPotential == 4) {
						data.count += 2;
					}
				}
				
				if (!ship.isFighter() && !ship.isDrone()) {
					float pierceChance = 1f;
					pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
					boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
					
					if (!shieldHit || piercedShield) {
						float emp = projectile.getEmpAmount();
						float dam = projectile.getDamageAmount()*0.25f; // this should be 1 for regular and a bunch for high-frequency
						
						engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
										   projectile.getDamageType(), 
										   dam,
										   emp, // emp 
										   100000f, // max range 
										   "mote_attractor_impact_emp_arc",
										   20f, // thickness
										   new Color(165,70,255,255),
										   new Color(255,255,255,255)
										   );
					}				
				} else {
					float damage = 1000f;
					Global.getCombatEngine().applyDamage(projectile, ship, point, 
							damage, DamageType.ENERGY, 0f, false, false, projectile.getSource(), true);
				}
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




