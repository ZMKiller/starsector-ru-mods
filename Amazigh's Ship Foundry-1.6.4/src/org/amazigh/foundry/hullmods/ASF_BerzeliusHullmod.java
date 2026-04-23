package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_BerzeliusHullmod extends BaseHullMod {
	
	public static final float SUPPLY_COST = 80f;
	
	public static final float OVERLOAD_MULT = 0.9f;
		//this is a bit weaker than on normal warburn, because the ship is good enough already
	
	public static final float RoF_PENALTY = 0.15f; // because the ship is too strong, give it an inherent reduction to RoF to balans it out
	
	public static final float BROAD_RADAR = 50f; // [CUSTOM CARTRIDGE: BROAD RADAR]
	
	public static final float REPAIR_MULT = 240f; // 1800 armour - we repair *twice*, but at a bit under half the power it ""should"" have (which would be 550ish) 
	public static final float BLAST_SIZE = 666f; // radius 226
	
	public static final float SHUNT = 0.05f;
	
    private final IntervalUtil interval = new IntervalUtil(0.033f, 0.033f);
    private final IntervalUtil interval_2 = new IntervalUtil(0.033f, 0.033f);
    private final Random rand = new Random();
    private static final Color SPARK_COLOR = new Color(130, 40, 140);
    
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
        
	public static final float TIMESCALE = 3f; //4f
	public static final float TIME_SPEED = 0.5f; //0.66f
	public static final float TIME_RoF = 0.5f; //0.6f
	
	public Color ENGINE_COLOR = new Color(90,255,165,55);
    private static final Color COLOR_EX = new Color(90,255,165,155);
	
    private final IntervalUtil arcInterval1 = new IntervalUtil(0.5f, 0.7f);
    private final IntervalUtil arcInterval2 = new IntervalUtil(0.5f, 0.7f);
    private final IntervalUtil arcInterval3 = new IntervalUtil(0.5f, 0.7f);
    private final IntervalUtil arcInterval4 = new IntervalUtil(0.5f, 0.7f);
    private static Map<HullSize, Float> phase2Impulse = new HashMap<HullSize, Float>();
	static {
		phase2Impulse.put(HullSize.FIGHTER, 200f);
		phase2Impulse.put(HullSize.FRIGATE, 500f);
		phase2Impulse.put(HullSize.DESTROYER, 800f);
		phase2Impulse.put(HullSize.CRUISER, 1600f);
		phase2Impulse.put(HullSize.CAPITAL_SHIP, 2000f);
		phase2Impulse.put(HullSize.DEFAULT, 1100f);
	}
	public static final float PD_MALUS = 0.5f;
	public static final float P2_SHIELD_MALUS = 1.25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_COST);
        stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT);
        
		stats.getSightRadiusMod().modifyPercent(id, BROAD_RADAR);
        
		stats.getHardFluxDissipationFraction().modifyFlat(id, SHUNT); // 5% flux shunt ;)
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			engine.getTimeMult().unmodify(spec.getId());
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("WARBURN_B_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
		
		MutableShipStatsAPI stats = ship.getMutableStats();
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == engine.getPlayerShip();
		}
		
		// Stat setup section
		float DAMAGE = 1.01f - ship.getHullLevel();
		if (DAMAGE > 0.8f) {
			DAMAGE = 0.8f;
		}
		float HULL_RATIO = DAMAGE / 0.8f;
		// Stat setup section
		
		// RoF Penalty
		stats.getBallisticRoFMult().modifyMult(spec.getId(), 1f - RoF_PENALTY);
		// RoF Penalty
		
		
		// Phase 2 [ANIMA EMULATION: ENGAGED]
		if (info.PHASE2) {
			
			// up to +30hp/sec when below 50% hp
			if (ship.getHitpoints() < ship.getMaxHitpoints() * 0.5f) {
				ship.setHitpoints(ship.getHitpoints() + (HULL_RATIO * 30f * amount));
			}
			
			// -50% damage to missiles, +25% shield damage taken, it's swapping defense for offense! 
			stats.getDamageToMissiles().modifyMult(spec.getId(), PD_MALUS);
			stats.getShieldDamageTakenMult().modifyMult(spec.getId(), P2_SHIELD_MALUS);
			
			
			if (ship.getSystem().isActive()) {
				// arcs!!
				arcInterval1.advance(amount);
				if (arcInterval1.intervalElapsed()) {
					for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			            if (weapon.getId().matches("SYS01")) {
			            	
			            	Vector2f arcStart = weapon.computePosition(ship);
			            	float arcAngle = weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-34f, 34f);
			            	Vector2f arcEnd = MathUtils.getPointOnCircumference(arcStart, MathUtils.getRandomNumberInRange(200f, 280f), arcAngle);
			            	
			            	engine.spawnEmpArcVisual(arcStart, ship, arcEnd, ship, 10f,
			            			new Color(150,128,56,255),
			            			new Color(255,225,205,255));
			            	
			            	Vector2f randomVel1 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 17f));
			            	
			            	Global.getCombatEngine().spawnProjectile(ship,
			            			null,
			            			"A_S-F_AURA_missile_berz",
			            			arcEnd,
			            			arcAngle,
			            			randomVel1);
			            	for (int i=0; i < 6; i++) {
			            		Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 44f));
			            		
			            		Global.getCombatEngine().addSmoothParticle(arcEnd,
			            				randomVel2,
			            				MathUtils.getRandomNumberInRange(7f, 17f), //size
			            				1.0f, //brightness
			            				0.65f, //duration
			            				new Color(215,190,75,255));
			            	}
			            	Global.getSoundPlayer().playSound("amsrm_fire", 1.1f, 0.6f, arcEnd, ship.getVelocity());
			            }
					}
				}
				
				arcInterval2.advance(amount);
				if (arcInterval2.intervalElapsed()) {
					for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			            if (weapon.getId().matches("SYS02")) {
			            	
			            	Vector2f arcStart = weapon.computePosition(ship);
			            	float arcAngle = weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-34f, 34f);
			            	Vector2f arcEnd = MathUtils.getPointOnCircumference(arcStart, MathUtils.getRandomNumberInRange(200f, 280f), arcAngle);
			            	
			            	engine.spawnEmpArcVisual(arcStart, ship, arcEnd, ship, 10f,
			            			new Color(150,128,56,255),
			            			new Color(255,225,205,255));
			            	
			            	Vector2f randomVel1 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 17f));
			            	
			            	Global.getCombatEngine().spawnProjectile(ship,
			            			null,
			            			"A_S-F_AURA_missile_berz",
			            			arcEnd,
			            			arcAngle,
			            			randomVel1);
			            	for (int i=0; i < 6; i++) {
			            		Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 44f));
			            		
			            		Global.getCombatEngine().addSmoothParticle(arcEnd,
			            				randomVel2,
			            				MathUtils.getRandomNumberInRange(7f, 17f), //size
			            				1.0f, //brightness
			            				0.65f, //duration
			            				new Color(215,190,75,255));
			            	}
			            	Global.getSoundPlayer().playSound("amsrm_fire", 1.1f, 0.6f, arcEnd, ship.getVelocity());
			            }
					}
				}
				
				arcInterval3.advance(amount);
				if (arcInterval3.intervalElapsed()) {
					for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			            if (weapon.getId().matches("SYS03")) {
			            	
			            	Vector2f arcStart = weapon.computePosition(ship);
			            	float arcAngle = weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-34f, 34f);
			            	Vector2f arcEnd = MathUtils.getPointOnCircumference(arcStart, MathUtils.getRandomNumberInRange(200f, 280f), arcAngle);
			            	
			            	engine.spawnEmpArcVisual(arcStart, ship, arcEnd, ship, 10f,
			            			new Color(150,128,56,255),
			            			new Color(255,225,205,255));
			            	
			            	Vector2f randomVel1 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 17f));
			            	
			            	Global.getCombatEngine().spawnProjectile(ship,
			            			null,
			            			"A_S-F_AURA_missile_berz",
			            			arcEnd,
			            			arcAngle,
			            			randomVel1);
			            	for (int i=0; i < 6; i++) {
			            		Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 44f));
			            		
			            		Global.getCombatEngine().addSmoothParticle(arcEnd,
			            				randomVel2,
			            				MathUtils.getRandomNumberInRange(7f, 17f), //size
			            				1.0f, //brightness
			            				0.65f, //duration
			            				new Color(215,190,75,255));
			            	}
			            	Global.getSoundPlayer().playSound("amsrm_fire", 1.1f, 0.6f, arcEnd, ship.getVelocity());
			            }
					}
				}
				
				arcInterval4.advance(amount);
				if (arcInterval4.intervalElapsed()) {
					for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			            if (weapon.getId().matches("SYS04")) {
			            	
			            	Vector2f arcStart = weapon.computePosition(ship);
			            	float arcAngle = weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-34f, 34f);
			            	Vector2f arcEnd = MathUtils.getPointOnCircumference(arcStart, MathUtils.getRandomNumberInRange(200f, 280f), arcAngle);
			            	
			            	engine.spawnEmpArcVisual(arcStart, ship, arcEnd, ship, 10f,
			            			new Color(150,128,56,255),
			            			new Color(255,225,205,255));
			            	
			            	Vector2f randomVel1 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 17f));
			            	
			            	Global.getCombatEngine().spawnProjectile(ship,
			            			null,
			            			"A_S-F_AURA_missile_berz",
			            			arcEnd,
			            			arcAngle,
			            			randomVel1);
			            	for (int i=0; i < 6; i++) {
			            		Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 44f));
			            		
			            		Global.getCombatEngine().addSmoothParticle(arcEnd,
			            				randomVel2,
			            				MathUtils.getRandomNumberInRange(7f, 17f), //size
			            				1.0f, //brightness
			            				0.65f, //duration
			            				new Color(215,190,75,255));
			            	}
			            	Global.getSoundPlayer().playSound("amsrm_fire", 1.1f, 0.6f, arcEnd, ship.getVelocity());
			            }
					}
				}
				// arcs!!
				
			}
		} else {
			
			if (ship.getHullLevel() < 0.5f) {
				info.PHASE2 = true;

		        // TEXT and sound
				Global.getSoundPlayer().playSound("ui_refit_slot_filled_ballistic_large", 1.2f, 2.5f, ship.getLocation(), ship.getVelocity());
					// "ui_refit_slot_filled_energy_large"
				
				engine.addFloatingTextAlways(ship.getLocation(),
						"[Engaging ANIMA Emulation]",
						NeuralLinkScript.getFloatySize(ship) * 1.5f, new Color(150,215,69,255), ship,
						12f, // flashFrequency
						4f, // flashDuration
						0.5f, // durInPlace
						2f, // durFloatingUp
						1.5f, // durFadingOut
						1f); // baseAlpha
		        // TEXT and sound
				
				
				// hull restore to 50%, so unless you somehow one-tap the last 50% hp and skip phase 2, any "excess" damage is wasted. 
				ship.setHitpoints(ship.getMaxHitpoints() * 0.5f); // [ARMOR DRIVE: LAST STAND]
				
				// flux reset! (it's a phase transition, only "fair" it gets a reset!)
				ship.getFluxTracker().setHardFlux(0f);
				ship.getFluxTracker().setCurrFlux(0f);
				
				// full armour repair
				ArmorGridAPI armorGrid = ship.getArmorGrid();
		        final float[][] grid = armorGrid.getGrid();
		        
				for (int x = 0; x < grid.length; x++) {
		            for (int y = 0; y < grid[0].length; y++) {
		            	armorGrid.setArmorValue(x, y, armorGrid.getMaxArmorInCell()); // [ARMOR DRIVE: LAST STAND]
		            }
		        }
				
				ship.clearDamageDecals();
				ship.syncWithArmorGridState();
		        ship.syncWeaponDecalsWithArmorDamage();
				// full armour repair
				
		        // push shockwave
		        for (ShipAPI target_ship : engine.getShips()) {
		        	float dist = ship.getCollisionRadius() + 4000f;
					if (MathUtils.isWithinRange(ship, target_ship, dist)) {
						
						float impulseForce = Math.min(phase2Impulse.get(((ShipAPI) target_ship).getHullSize()) * 4f, (target_ship.getMass() * 0.8f) + phase2Impulse.get(((ShipAPI) target_ship).getHullSize()));
						// force is whatever is lower of:
							// 3x hullsize scalar
							// hullsize scalar + 80% of target mass
								// so we get a respectable push, but it's not SILLY on stuff like the invictus
						
						float range = MathUtils.getDistance(ship, target_ship);
						float pushScalar = 0.1f + (range / dist); //scaling down pushforce on stuff that is further away!
						
						CombatUtils.applyForce(target_ship, VectorUtils.getDirectionalVector(ship.getLocation(), target_ship.getLocation()), impulseForce * pushScalar); // knockback!
						
					}
				}
		        // push shockwave		        
				
		        // blast and blast vfx
		        float outerSize = (ship.getCollisionRadius() * 3f) + 200f;
		        
		        DamagingExplosionSpec blast = new DamagingExplosionSpec(0.8f,
		        		outerSize,
		                ship.getCollisionRadius(),
		                600f,
		                60f,
		                CollisionClass.PROJECTILE_FF,
		                CollisionClass.PROJECTILE_FIGHTER,
		                3f,
		                6f,
		                0.9f,
		                500,
		                new Color(215,190,75,255),
		                new Color(150,128,56,255));
		        blast.setDamageType(DamageType.FRAGMENTATION);
		        blast.setShowGraphic(true);
		        blast.setDetailedExplosionFlashColorCore(new Color(160,165,160,255));
		        blast.setDetailedExplosionFlashColorFringe(new Color(200,140,80,255));
		        blast.setUseDetailedExplosion(true);
		        blast.setDetailedExplosionRadius(outerSize * 1.1f);
		        blast.setDetailedExplosionFlashRadius(outerSize * 1.3f);
		        blast.setDetailedExplosionFlashDuration(0.9f);
		        
		        engine.spawnDamagingExplosion(blast,ship,ship.getLocation(),true);
		        
		        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), new Color(215,190,75,255), ship.getCollisionRadius() + 200f, 0.75f);
		        
		    	// background smoke
		        for (int i=0; i < 15; i++) {
		        	float angle = i * 24;
		        	engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), 169f, angle),
			        		MathUtils.getPointOnCircumference(ship.getVelocity(), 10f, angle),
			        		201f,
							MathUtils.getRandomNumberInRange(1.7f, 2.1f),
							0.9f,
							0.5f,
							MathUtils.getRandomNumberInRange(3.1f, 3.6f),
							new Color(140,120,40,51),
							false);
		        }
				
		        	// main smoke ring
		        for (int i=0; i < 80; i++) {
		        	float nebAngle = i * 4.5f;
		        	float dist = MathUtils.getRandomNumberInRange(0.1f, 0.3f);
		        	
		        	engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), outerSize * dist, nebAngle),
		        			MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), nebAngle),
		        			61f,
		        			MathUtils.getRandomNumberInRange(1.8f, 2.3f),
		        			0.75f,
		        			0.5f,
		        			MathUtils.getRandomNumberInRange(1.9f, 2.2f),
		        			new Color(190,150,65,75),
		        			false);
		        }
		        // blast and blast vfx
		        
		        // reinforcements
		        float spawnPointY = (engine.getMapHeight() * 0.5f) + 250f;
		        CombatFleetManagerAPI fleetManager = engine.getFleetManager(ship.getOriginalOwner());
		        
		        // CombatTaskManagerAPI taskManager = (fleetManager != null) ? fleetManager.getTaskManager(ship.isAlly()) : null;
		        // CombatFleetManagerAPI.AssignmentInfo assignmentInfo = taskManager.createAssignment(CombatAssignmentType.DEFEND, fleetManager.getDeployedFleetMemberEvenIfDisabled(ship), false);
		        	// order thing disabled because it causes a crash *if* berz is mind controlled when it triggers phase 2
		        
		        PersonAPI zelius = Global.getSettings().createPerson();
		        zelius.setPortraitSprite("graphics/portraits/portrait_berzel.png");
		        zelius.setFaction(Factions.SCAVENGERS);
		        zelius.setAICoreId(Commodities.ALPHA_CORE);
		        zelius.getStats().setLevel(5);
                zelius.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		        zelius.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		        zelius.getStats().setSkillLevel(Skills.POINT_DEFENSE, 1);
		        zelius.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
		        zelius.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
		        
				ShipAPI hoka = fleetManager.spawnShipOrWing("A_S-F_grandum_ark", new Vector2f(0f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, zelius);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(hoka), assignmentInfo, false);
                hoka.setName("Hoka");
				
		        ShipAPI kyokan = fleetManager.spawnShipOrWing("A_S-F_giganberg_ark", new Vector2f(300f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(kyokan), assignmentInfo, false);
                kyokan.setName("Kyokan");
                
                ShipAPI hohei = fleetManager.spawnShipOrWing("A_S-F_gaderoga_ark", new Vector2f(-300f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(hohei), assignmentInfo, false);
                hohei.setName("Hohei");
                ShipAPI heiki = fleetManager.spawnShipOrWing("A_S-F_gaderoga_ark", new Vector2f(600f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(heiki), assignmentInfo, false);
                heiki.setName("Heiki");
		        
                ShipAPI kame = fleetManager.spawnShipOrWing("A_S-F_genbura_ark", new Vector2f(-600f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(kame), assignmentInfo, false);
                kame.setName("Kame");
                ShipAPI kuro = fleetManager.spawnShipOrWing("A_S-F_genbura_ark", new Vector2f(900f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(kuro), assignmentInfo, false);
                kuro.setName("Kuro");
                
                ShipAPI shoju = fleetManager.spawnShipOrWing("A_S-F_gathima_ark", new Vector2f(-900f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(shoju), assignmentInfo, false);
                shoju.setName("Shoju");
                ShipAPI tanju = fleetManager.spawnShipOrWing("A_S-F_gathima_ark", new Vector2f(1200f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(tanju), assignmentInfo, false);
                tanju.setName("Tanju");
                ShipAPI kenju = fleetManager.spawnShipOrWing("A_S-F_gathima_ark", new Vector2f(-1200f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(kenju), assignmentInfo, false);
                kenju.setName("Kenju");
                
                ShipAPI hagane = fleetManager.spawnShipOrWing("A_S-F_galsteel_ark", new Vector2f(1500f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(hagane), assignmentInfo, false);
                hagane.setName("Hagane");
                ShipAPI tetsu = fleetManager.spawnShipOrWing("A_S-F_galsteel_ark", new Vector2f(-1500f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(tetsu), assignmentInfo, false);
                tetsu.setName("Tetsu");
                ShipAPI shin = fleetManager.spawnShipOrWing("A_S-F_galsteel_ark", new Vector2f(1800f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(shin), assignmentInfo, false);
                shin.setName("Shin");
                
                ShipAPI wani = fleetManager.spawnShipOrWing("A_S-F_gatorbacker_ark", new Vector2f(-1800f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(wani), assignmentInfo, false);
                wani.setName("Wani");
                ShipAPI kani = fleetManager.spawnShipOrWing("A_S-F_gatorbacker_ark", new Vector2f(2100f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(kani), assignmentInfo, false);
                kani.setName("Kani");
                ShipAPI nani = fleetManager.spawnShipOrWing("A_S-F_gatorbacker_ark", new Vector2f(-2100f, spawnPointY + MathUtils.getRandomNumberInRange(-100f, 100f)), -90f, 5f, null);
                //taskManager.giveAssignment(fleetManager.getDeployedFleetMemberEvenIfDisabled(nani), assignmentInfo, false);
                nani.setName("Nani");
				// reinforcements
		        
			}
		}
		// Phase 2
		
		
		
		// Vent Repair Section
		if (ship.getFluxTracker().isVenting()) {
			interval.advance(engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue());
	        if (interval.intervalElapsed()) {
	            ArmorGridAPI armorGrid = ship.getArmorGrid();
	            
	            for (int i = 0; i < 2; i++) {
	            	int x = rand.nextInt(armorGrid.getGrid().length);
		            int y = rand.nextInt(armorGrid.getGrid()[0].length);
		            float newArmor = armorGrid.getArmorValue(x, y);
		            float cellSize = armorGrid.getCellSize();

		            if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) < 0) {
		            	
		            	float phaseMult = REPAIR_MULT;
		        		if (info.PHASE2) {
		        			phaseMult *= 0.5f;
		            		// halved repair in phase2!
		        		}
		        		
		                newArmor += phaseMult * interval.getIntervalDuration() * (2f + ship.getFluxLevel());
		                
		                boolean REPAIR = false;
		                if (armorGrid.getArmorValue(x, y) < armorGrid.getMaxArmorInCell()) {
		                	REPAIR = true;
		                }
		                armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));
		                
		                Vector2f cellLoc = getCellLocation(ship, x, y);
		                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
		                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
		                engine.addHitParticle(cellLoc,
		                		ship.getVelocity(),
		                		(8f * (float) Math.random()) + 6f,
		                		0.9f,
		                		0.36f,
		                        SPARK_COLOR);
		                if (REPAIR) {
		                	engine.spawnExplosion(cellLoc, ship.getVelocity(), SPARK_COLOR, 22f, 0.21f);
		                }
		            }
	            }
	            
	        }
		}
		
		if (ship.getFluxTracker().isVenting() || info.ACTIVE) { // [CUSTOM CARTRIDGE: STUN REGAIN]
            
            if (DefenseUtils.hasArmorDamage(ship)) {

	            ArmorGridAPI armorGrid = ship.getArmorGrid();
	            
	        	final float[][] grid = armorGrid.getGrid();
		        final float max = armorGrid.getMaxArmorInCell();
		        
		        float repairAmount = 5f * amount;
		        	// so you get a flat 5/sec armour repair in all cells, during the "hull charge"
		        

            	if (info.PHASE2) {
            		repairAmount *= 0.5f;
            		// halved repair in phase2!
        		}
            	if (info.ACTIVE) {
		        	repairAmount *= 0.5f;
		        	// halving repair during "hull charge" (balans)
		        }
		        
				for (int x = 0; x < grid.length; x++) {
		            for (int y = 0; y < grid[0].length; y++) {
		                if (grid[x][y] < max) {
		                    float regen = grid[x][y] + repairAmount;
		                    armorGrid.setArmorValue(x, y, regen);
		                }
		            }
		        }
				
        	}
            
            ship.syncWithArmorGridState(); // to remove gross damage when repairing, thanks ruddygreat
            ship.syncWeaponDecalsWithArmorDamage();
		}
		
		// 
		
		if (ship.getFluxTracker().isVenting()) {
			float phaseMult = 100f;
    		if (info.PHASE2) {
    			phaseMult *= 0.5f;
        		// halved repair in phase2!
    		}
			ship.setHitpoints(Math.min(ship.getHitpoints() + (phaseMult * amount), ship.getMaxHitpoints())); // [CUSTOM CARTRIDGE: STUN REGAIN]
		}
		
		// Vent Repair Section
		
		
		// Fancy Damage Buff Section
		if (!info.SET) {
			info.THRESHOLD = ship.getHullLevel();
			info.SET = true;
		}
		
		if (ship.getHullLevel() < info.THRESHOLD) {
			info.TIMER += ((info.THRESHOLD - ship.getHullLevel()) * 100f);
			info.THRESHOLD = ship.getHullLevel();
		}
		
		if (info.TIMER > 8f) {
			info.ARMED = true;
		}
		
		if (info.ACTIVE) {
			ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX); // having Berz vent while in the ACTIVE state is... not great fun
		}
		
		if (info.TIMER > 0f && info.ARMED) {
			if (!info.ACTIVE) {
				// INITIAL EFFECT
				info.ACTIVE = true;
				engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), COLOR_EX, BLAST_SIZE, 0.25f);	
				Global.getSoundPlayer().playSound("system_temporalshell", 1f, 0.9f, ship.getLocation(), ship.getVelocity());
				// INITIAL EFFECT
			}
			
			interval_2.advance(engine.getElapsedInLastFrame());
			if (interval_2.intervalElapsed()) {
				info.TIMER -= interval_2.getIntervalDuration();
			}
			
			// BUFF
			float shipTimeMult = 1f + ((TIMESCALE - 1f) * (Math.min(info.TIMER/3f, 1f)));
			if (player) {
				stats.getTimeMult().modifyMult(spec.getId(), shipTimeMult);
				engine.getTimeMult().modifyMult(spec.getId(), 1f / shipTimeMult);
			} else {
				stats.getTimeMult().modifyMult(spec.getId(), shipTimeMult);
				engine.getTimeMult().unmodify(spec.getId());
			}
			stats.getMaxSpeed().modifyMult(spec.getId(), 1f - (TIME_SPEED * (Math.min(info.TIMER/3f, 1f))));
			stats.getBallisticRoFMult().modifyMult(spec.getId(), 1f - (TIME_RoF * (Math.min(info.TIMER/3f, 1f))));
			stats.getMissileRoFMult().modifyMult(spec.getId(), 1f - (TIME_RoF * (Math.min(info.TIMER/3f, 1f))));
			
			float DAM_RES = 0.1f + (0.4f * HULL_RATIO);
			stats.getHullDamageTakenMult().modifyMult(spec.getId(), 1f - DAM_RES);
			stats.getArmorDamageTakenMult().modifyMult(spec.getId(), 1f - DAM_RES);
			stats.getEmpDamageTakenMult().modifyMult(spec.getId(), 1f - DAM_RES);
			// BUFF
			
			// Jitter
			float ALPHA_1 = (Math.min(info.TIMER, 3f) * 1.75f) + 15f;
			float ALPHA_2 = (Math.min(info.TIMER, 3f) * 2.6f) + 20f;
			Color JITTER_COLOR = new Color(90,255,165,(int)ALPHA_1);
			Color JITTER_UNDER_COLOR = new Color(90,255,165,(int)ALPHA_2);
			
			float jitterRangeBonus_1 = (Math.min(info.TIMER, 3f)) * 1.5f;
			float jitterRangeBonus_2 = (Math.min(info.TIMER, 3f)) * 3f;
			
			float jitterLevel = ( (float) Math.sqrt((Math.min(info.TIMER/3f, 1f))) * 0.35f ) + 0.65f;
			
			ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 10f + jitterRangeBonus_1);
			ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 15, 0f, 15f + jitterRangeBonus_2);
			// Jitter

			// ENGINE FX
			ship.getEngineController().fadeToOtherColor(this, ENGINE_COLOR, new Color(15,0,30,40), Math.min(info.TIMER/3f, 1f), 0.6f);
			ship.getEngineController().extendFlame(this, 0.2f, 0.2f, 0.2f);
			// ENGINE FX
			
		} else if (info.ACTIVE) {
			info.ACTIVE = false;
			info.SET = false;
			info.ARMED = false;
			stats.getTimeMult().unmodify(spec.getId());
			engine.getTimeMult().unmodify(spec.getId());
			stats.getMaxSpeed().unmodify(spec.getId());
			stats.getBallisticRoFMult().unmodify(spec.getId());
			stats.getMissileRoFMult().unmodify(spec.getId());
		}
		
		if (ship.getHullLevel() > info.THRESHOLD) { // here we check if the ship has regenerated hull and adjust our threshold appropriately
			info.THRESHOLD = ship.getHullLevel();
		}
		// Fancy Damage Buff Section

		engine.getCustomData().put("WARBURN_B_DATA_KEY" + ship.getId(), info);
		
		
		// AI trickery section
		if (Global.getCombatEngine().isPaused() || ship.getShipAI() == null) {
			return;
		}
		if (ship.getFluxLevel() > 0.9f) {
			info.VENTBRAINTIMER += (amount * 2f);
        } else if (info.PHASE2) {
			if (ship.getFluxLevel() > 0.7f) {
				info.VENTBRAINTIMER += (amount); // in phase2, we are to be more aggressive with venting, as shields are disabled!
			} else {
	        	info.VENTBRAINTIMER = Math.max(0f, info.VENTBRAINTIMER - amount);
			}
		} else if (ship.getFluxLevel() < 0.8f) {
        	info.VENTBRAINTIMER = Math.max(0f, info.VENTBRAINTIMER - amount);
        }
		
		if (info.VENTBRAINTIMER > 4f && !ship.getSystem().isActive()) {
	        ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
	        info.VENTBRAINTIMER = 0f;
		}
		
		engine.getCustomData().put("WARBURN_B_DATA_KEY" + ship.getId(), info);
		
    		// while flux is over 90%, gain "brain" value at 2/sec {priority-1}
				// in phase 2 also gains "brain" value at 1/sec when over 70% flux {priority-2}
			// if flux is below 80%, lose "brain" value at 1/sec {priority-3}
			// if "brain" value is at 4 or more, and the system is not currently active, force a vent.
				// this leaves ~2s to deal the final ~2240 shield HP damage before it vents it away. 
		// AI trickery section
		
	}
		// So what this hullmod does is as follows:
		// - Increases monthly supply cost by 80%
		// - Reduces overload duration by 10%
		// - grants a 5% hardflux dissipation bonus
		// - Repairs armour while venting, the amount repaired scales up based on current flux level
		// - Also repairs hull while venting, albeit very slowly.
		// -- On taking Hull damage:
		// --- Increment a "timer" gaining 10 seconds of time for each 10% hull damage taken.
		// --- If timer is over 8, start decaying the timer and gain a timeflow + damage resistance buff until the timer runs out.
		// --- Speed and RoF are reduced while this buff is active, to stop you becoming a psycho demon, and to make it more of a "free vent" than a pure buff.
		// ---- also applies a slow flat armour repair to all cells during this "boost"
	
		//	- On dropping below 50% HP, enters "Phase 2"   [Engaging ANIMA Emulation]
		//	  on trigger:
		//	-- full armour repair
		//	-- flux reset
		//	-- hull repaired to 50%
		//	-- wide radius push (kinda weak tbh) (push str scales down with range to target)
		//	-- (3x collision radius +200) explosion dealing 600-60 frag dmg
		//	--- Phase 2 Effects:
		//		!! all the "normal" repairs have their power halved !!
		//		!! 50% reduced damage to missiles !!
		//		!! +25% shield damage taken !!
		//		 during sys use: the 4 "nodules" emit arcs, that spawn an AURA missile on their target point (each nodule has an independent 0.5s-0.7s interval)
		//  	 spawns a fleet of *standard* arktech ships as reinforcements!
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("The internal systems of this ship have been constructed using unknown advanced methods that only bear a passing resemblance to Domain technology.", opad);
		label = tooltip.addPara("What limited documentation could be found on internal systems classifies this special configuration as %s", pad, h, "\"Warburn Systems\"");
		label.setHighlight("\"Warburn Systems\"");
		label.setHighlightColors(h);
		label = tooltip.addPara("The one confirmed feature of these systems is that inner armour layers feature integrated repair nanites that will activate and repair a small amount of damaged armour while venting.", pad);
		
		label = tooltip.addPara("Other more esoteric behaviours are suggested in the limited recovered documentation, but limited initial testing has been unable to fully determine their exact nature.", opad);
		label = tooltip.addPara("Due to only possessing partial documentation and understanding of the internal systems, the maintenance cost for this ship is increased by %s.", pad, bad, "" + (int)SUPPLY_COST + "%");
		label.setHighlight("" + (int)SUPPLY_COST + "%");
		label.setHighlightColors(bad);		
	}

    private class ShipSpecificData {
        private boolean SET = false;
        private float THRESHOLD = 1.0f;
        private float TIMER = 0f;
        private boolean ARMED = false;
        private boolean ACTIVE = false;
        private float VENTBRAINTIMER = 0f;
        private boolean PHASE2 = false;
    }
}
