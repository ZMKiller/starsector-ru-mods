package data.hullmods;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_Precursor_Reboot extends BaseHullMod {
	
	public static float REPAIR_MULT_FOR_EmergencyForge = 1f;
	public static float Max_Duration = 20f;
	public static float Initial_Cycle_Time = 0f;
	
	public static float RANGE = 500f;
	public static float DAM = 1500f;
	public static float EMP = 2000f;
	
	public static final Object KEY_JITTER = new Object();
	public static final Color JITTER_UNDER_COLOR = new Color(255,205,75,125);
	public static final Color JITTER_COLOR = new Color(255,165,155,75);
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEmpDamageTakenMult().modifyMult("dpl_Precursor_Reboot", 0.02f);
	}
	
	public static class dpl_Precursor_RebootScript implements AdvanceableListener, HullDamageAboutToBeTakenListener{
		public ShipAPI ship;
		public boolean canUse = false;
		public boolean resurrection = false;
		public float Progress = 0f;
		public float Health_old;
		public float Health_new;
		public float Duration = Max_Duration;
		public float Cyclic_Time = Initial_Cycle_Time;
		public dpl_Precursor_RebootScript(ShipAPI ship) {
			this.ship = ship;
			this.Health_old = ship.getMaxHitpoints();
		}
		
		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			
			if (damageAmount >= ship.getMaxHitpoints()*0.2f) {
				float r1 = ship.getCollisionRadius();
				Color c = new Color(255,200,255,255);
				c = Misc.setAlpha(c, 255);
				c = Misc.interpolateColor(c, Color.white, 0.5f);
				ship.setJitter(this, c, 0.1f, 20, r1*0.5f);
				if (ship.getFluxTracker().showFloaty()) {
					Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),
							"Precursor Dimensional Resonance!",
							NeuralLinkScript.getFloatySize(ship), c, ship, 16f , 3.2f, 1f, 0f, 0f,
							1f);
				}
				return true;
			}
			
			if (canUse && !resurrection) {
				String key = "dpl_PrecursorReboot_canRevive";
				boolean canForge = true;
				float depCost = 0.2f;
				float crLoss = depCost;
				canForge &= ship.getCurrentCR() >= crLoss;
				
				float hull = ship.getHitpoints();
				if (damageAmount >= hull && canForge) {
					ship.setHitpoints(1f);
					if (ship.getFleetMember() != null) { // fleet member is fake during simulation, so this is fine
						ship.setCurrentCR(ship.getCurrentCR()-crLoss);
					}
					resurrection = true;
					Global.getCombatEngine().getCustomData().put(key, true);
				}
			}
			
			if (canUse && resurrection) {
				return true;
			}
			return false;
		}
		
		private IntervalUtil fireInterval = new IntervalUtil(0.1f, 0.2f);

		public void advance(float amount) {
			String id = "dpl_precursor_reboot_modifier";
			if (ship.getOwner() == 0) {
				ship.setHitpoints(-1f);
			}
			
			Duration -= amount;
			fireInterval.advance(amount);
			
			Health_new = ship.getHitpoints();
			if (Health_old - Health_new >= ship.getMaxHitpoints() * 0.2f * amount) {
				ship.setHitpoints(Health_old - ship.getMaxHitpoints() * 0.2f * amount);
			}
			Health_old = ship.getHitpoints();
			if (ship.isAlive()) {
				if (Duration <= 0) {
					canUse = true;
					
					glowActive(ship, 2f - (float) Math.cos(Cyclic_Time));
					Cyclic_Time += amount;
					
					//Visual and real lightning stuffs
					if (fireInterval.intervalElapsed()) {
						addArcAllTargets(ship);
					}
					
					if (resurrection) {
						Color c = new Color(255,200,255,255);
						c = Misc.setAlpha(c, 255);
						c = Misc.interpolateColor(c, Color.white, 0.5f);
						if (Progress == 0f) {
							if (ship.getFluxTracker().showFloaty()) {
								Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),
										"Precursor Reboot!",
										NeuralLinkScript.getFloatySize(ship), c, ship, 16f , 3.2f, 1f, 0f, 0f,
										1f);
							}
						}
						
						ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
						Progress += amount * 0.5f;
						ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, 0f);
						float r1 = ship.getCollisionRadius();
						ship.setJitter(this, c, 0.1f, 20, r1*0.5f);
						
						if (Progress >= 1f) {
							Global.getSoundPlayer().playSound("phase_anchor_vanish", 1f, 1f, ship.getLocation(), ship.getVelocity());
							float r = ship.getCollisionRadius();
							ship.setJitter(this, c, 0.5f, 20, r*0.5f);
							ship.setHitpoints(ship.getMaxHitpoints()*1f);
							ship.getFluxTracker().setCurrFlux(0);
							ship.getFluxTracker().setHardFlux(0);
							List<WeaponAPI> AllWeapons =  ship.getAllWeapons();
							for (WeaponAPI weapon: AllWeapons) {
								if(weapon.isDisabled()) {
									weapon.repair();
								}
							}

					        ArmorGridAPI armorGrid = ship.getArmorGrid();
					        for (int i=0; i < armorGrid.getGrid().length; i++) {
					        	for (int j=0; j < armorGrid.getGrid()[0].length; j++) {
					        		int x = i;
							        int y = j;
							        float newArmor = armorGrid.getArmorValue(x, y);
							        float cellSize = armorGrid.getCellSize();

							        if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) < 0) {
							            armorGrid.setArmorValue(x, y, armorGrid.getMaxArmorInCell());

							            Vector2f cellLoc = getCellLocation(ship, x, y);
							                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
							                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
							        }
					        	}
					        }

					        ship.syncWithArmorGridState();
					        ship.syncWeaponDecalsWithArmorDamage();
					        ship.clearDamageDecals();
					        
					        addNoTargetExplosions(ship);
							addExplosionAllTargets(ship);
					        
							Duration = Max_Duration;
							Cyclic_Time = Initial_Cycle_Time;
							Health_old = ship.getMaxHitpoints();
							Progress = 0f;
							resurrection = false;
							canUse = false;
							ship.getMutableStats().getHullDamageTakenMult().unmodify(id);
						}
					}
				} else {
					if (fireInterval.intervalElapsed()) {
						addNoTargetArcEight(ship);
						addArcAllTargets(ship);
					}
					float jitterLevel = 1f;
					ship.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, 5f);
					ship.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 5f);
				}
			}
		}
	}
	
	public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new dpl_Precursor_RebootScript(ship));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	public static void glowActive(ShipAPI ship, float jitterLevel) {
		ship.setCircularJitter(true);
		ship.setJitter(KEY_JITTER, JITTER_COLOR, jitterLevel, 1, 0f, 10f);
		ship.setJitterUnder(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel*0.66f, 11, 5f);
	}
	
	public static void addNoTargetArcEight(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		float spread = 100f;
		float range = RANGE;
		Vector2f from = ship.getLocation();
		float thickness = 50f;
		float coreWidthMult = 0.67f;
		Color color = new Color(255,205,75,155);
		for (int i=0; i<=8; i++) {
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(i*45f);
			dir.scale(range);
			Vector2f.add(from, dir, dir);
			dir = Misc.getPointWithinRadius(dir, spread);
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, ship, dir, ship, thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
		}
	}
	
	public static void addArcAllTargets(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		float range = RANGE;
		Vector2f from = ship.getLocation();
		float thickness = 50f;
		float dam = DAM;
		float emp = EMP;
		float coreWidthMult = 0.67f;
		Color color = new Color(255,205,75,255);
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = ship.getOwner();
		
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
			
			EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship, from, ship, other, 
					DamageType.ENERGY, 
					dam, 
					emp, 
					range, 
					"shock_repeater_emp_impact", 
					thickness, 
					color, 
					Color.white);
			
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
		}
	}
	
	public static DamagingExplosionSpec createExplosionSpec() {
		float damage = 100f;
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.1f, // duration
				75f, // radius
				50f, // coreRadius
				damage, // maxDamage
				damage / 2f, // minDamage
				CollisionClass.PROJECTILE_FF, // collisionClass
				CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
				3f, // particleSizeMin
				3f, // particleSizeRange
				0.5f, // particleDuration
				150, // particleCount
				new Color(255,255,255,255), // particleColor
				new Color(255,205,75,175)  // explosionColor
		);

		spec.setDamageType(DamageType.FRAGMENTATION);
		spec.setUseDetailedExplosion(false);
		spec.setSoundSetId("explosion_guardian");
		return spec;		
	}
	
	public static void addNoTargetExplosions(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		float spread = 200f;
		float range = 2f*RANGE;
		Vector2f from = ship.getLocation();
		float thickness = 50f;
		float coreWidthMult = 0.67f;
		Color color = new Color(255,205,75,255);
		for (int i=0; i<=16; i++) {
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(i*22.5f);
			dir.scale(range);
			Vector2f.add(from, dir, dir);
			dir = Misc.getPointWithinRadius(dir, spread);
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, ship, dir, ship, thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			
			Color ExpColor = new Color(225,175,55,235);
			NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(ExpColor, 50f);
			p.fadeOut = 2f;
			p.hitGlowSizeMult = 1f;
			DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), ship, dir);
			// want a red rift, but still blue for subtracting from the red clouds
			// or not - actually looks better with the red being inverted and subtracted
			// despite this not matching the trail
			//p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
			RiftCascadeMineExplosion.spawnStandardRift(e, p);
			
			Vector2f vel = new Vector2f();
			vel.setX(0f);
			vel.setY(0f);
			Global.getSoundPlayer().playSound("rifttorpedo_explosion", 1f, 1f, dir, vel);
		}
	}
	
	public static void addExplosionAllTargets(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		float range = 2f*RANGE;
		Vector2f from = ship.getLocation();
		float thickness = 50f;
		float dam = DAM;
		float emp = EMP;
		float coreWidthMult = 0.67f;
		Color color = new Color(255,205,75,255);
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = ship.getOwner();
		
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
			
			EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship, from, ship, other, 
					DamageType.ENERGY, 
					dam, 
					emp, 
					range, 
					"shock_repeater_emp_impact", 
					thickness, 
					color, 
					Color.white);
			
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			
			Color ExpColor = new Color(225,175,55,235);
			NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(ExpColor, 50f);
			p.fadeOut = 2f;
			p.hitGlowSizeMult = 1f;
			DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), ship, arc.getTargetLocation());
			// want a red rift, but still blue for subtracting from the red clouds
			// or not - actually looks better with the red being inverted and subtracted
			// despite this not matching the trail
			//p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
			RiftCascadeMineExplosion.spawnStandardRift(e, p);
			
			Vector2f vel = new Vector2f();
			if (other != null) vel.set(other.getVelocity());
			Global.getSoundPlayer().playSound("rifttorpedo_explosion", 1f, 1f, arc.getTargetLocation(), vel);
		}
	}
}

