package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.RiftTrailEffect;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

import data.scripts.weapons.dpl_null_swarmEffect.dpl_null_swarmHitMod;

public class dpl_song_of_spiritEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin {

	public static float ARC = 30f;
	public static float DISRUPTION_DUR = 1.5f;
	public static Color OVERLOAD_COLOR = new Color(125,50,255,255);
	public static int MAXARC = 30;
	public static float CR_Mult = 0.05f;
	public static float MR_Mult = 0.05f;
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
	
		ShipAPI target = findTarget(projectile, weapon, engine);
		float thickness = 50f;
		float coreWidthMult = 0.67f;
		Color color = new Color(125,15,205,255);
		if (target != null) {
			for (int i=0; i <= MAXARC; i++) {
				EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(projectile.getSource(), projectile.getLocation(), weapon.getShip(),
						   target,
						   DamageType.ENERGY, 
						   dam/MAXARC,
						   emp/MAXARC, // emp 
						   100000f, // max range 
						   "shock_repeater_emp_impact",
						   thickness, // thickness
						   color,
						   new Color(150,5,215,255)
						   );
				arc.setCoreWidthOverride(thickness * coreWidthMult);
				arc.setTargetToShipCenter(projectile.getLocation(),target);
				arc.setSingleFlickerMode();
			}
			
			target.getFluxTracker().playOverloadSound();
			float crLoss = CR_Mult;
			target.setCurrentCR(target.getCurrentCR()-crLoss);
			target.setOverloadColor(OVERLOAD_COLOR);
			target.getFluxTracker().beginOverloadWithTotalBaseDuration(DISRUPTION_DUR);
			
			if (!target.hasListenerOfClass(dpl_SongOfSpiritHitMod.class)) {
				target.addListener(new dpl_SongOfSpiritHitMod(target));
			}
			List<dpl_SongOfSpiritHitMod> listeners = target.getListeners(dpl_SongOfSpiritHitMod.class);
			if (listeners.isEmpty()) return; // ???
				
			dpl_SongOfSpiritHitMod listener = listeners.get(0);
			listener.notifyHit(projectile);
		} else {
			Vector2f from = new Vector2f(projectile.getLocation());
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, weapon.getShip(), from, weapon.getShip(), thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			//Global.getSoundPlayer().playSound("shock_repeater_emp_impact", 1f, 1f, to, new Vector2f());
		}
	}
	
	public static class dpl_SongOfSpiritHitMod implements AdvanceableListener {
		//implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected TimeoutTracker<DamagingProjectileAPI> recentHits = new TimeoutTracker<DamagingProjectileAPI>();
		public dpl_SongOfSpiritHitMod(ShipAPI ship) {
		this.ship = ship;
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
				ship.getMutableStats().getWeaponMalfunctionChance().modifyFlat("dpl_song_of_spirit_hit", hits * MR_Mult);
				ship.getMutableStats().getCriticalMalfunctionChance().modifyFlat("dpl_song_of_spirit_hit", hits * MR_Mult);
			}
		}
	}
	
	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float spread = 50f;
		float range = weapon.getRange() - spread;
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, spread);
		return dir;
	}
	
	public ShipAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange();
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		ShipAPI best = null;
		float minScore = Float.MAX_VALUE;
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof ShipAPI)) continue;
			ShipAPI other = (ShipAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other.isFighter()) continue;
			
			if (other.isDrone()) continue;
			
			if (other.isShuttlePod()) continue;
			
			if (other.isHulk()) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist > range) continue;
			
			//float angleTo = Misc.getAngleInDegrees(from, other.getLocation());
			//float score = Misc.getAngleDiff(weapon.getCurrAngle(), angleTo);
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
		}
		return best;
	}
	
}




