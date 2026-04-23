package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

//credits to DR for reference for hull/armour repair in the Excelsior phase system.

public class ASF_GaderogaHullmod extends BaseHullMod {

	public static final float SUPPLY_COST = 75f;
	
	public static final float OVERLOAD_MULT = 0.85f;
	
	public static final float REVENGE_SHOT = 30f;
	
	private static Map<HullSize, Float> REPAIR_MULT = new HashMap<HullSize, Float>();
	static {
		REPAIR_MULT.put(HullSize.FRIGATE, 80f); // 500 armour
		REPAIR_MULT.put(HullSize.DESTROYER, 150f); // 800 armour
		REPAIR_MULT.put(HullSize.CRUISER, 250f); // 1250 armour
		REPAIR_MULT.put(HullSize.CAPITAL_SHIP, 400f); // no capitals (yet?)
	}
	
	private static Map<HullSize, Float> BLAST_SIZE = new HashMap<HullSize, Float>();
	static {
		BLAST_SIZE.put(HullSize.FRIGATE, 210f); // 75 radius
		BLAST_SIZE.put(HullSize.DESTROYER, 310f); // 108 radius
		BLAST_SIZE.put(HullSize.CRUISER, 350f); // 120 radius
		BLAST_SIZE.put(HullSize.CAPITAL_SHIP, 510f); // no capitals (yet?)
	}
	
	//private static final float ARMOR_REPAIR_MULTIPLIER = 150.0f;
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
        
	public static final float TIMESCALE = 4f;
	public static final float TIME_SPEED = 0.65f;
	public static final float TIME_RoF = 0.55f;
	
	public Color ENGINE_COLOR = new Color(90,255,165,55);
    private static final Color COLOR_EX = new Color(90,255,165,155);
		
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_COST);
        stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("WARBURN_DATA_KEY" + ship.getId());
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
		
		
		// Revenge Shot
		stats.getBallisticWeaponFluxCostMod().modifyPercent(spec.getId(), -(REVENGE_SHOT * HULL_RATIO));
		stats.getEnergyWeaponFluxCostMod().modifyPercent(spec.getId(), -(REVENGE_SHOT * HULL_RATIO));
		stats.getMissileWeaponFluxCostMod().modifyPercent(spec.getId(), -(REVENGE_SHOT * HULL_RATIO));
		// Revenge Shot
		
		// Vent Repair Section
		if (ship.getFluxTracker().isVenting()) {
			interval.advance(engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue());
	        if (interval.intervalElapsed()) {
	            ArmorGridAPI armorGrid = ship.getArmorGrid();
	            int x = rand.nextInt(armorGrid.getGrid().length);
	            int y = rand.nextInt(armorGrid.getGrid()[0].length);
	            float newArmor = armorGrid.getArmorValue(x, y);
	            float cellSize = armorGrid.getCellSize();

	            if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) < 0) {
	                newArmor += ((Float) REPAIR_MULT.get(ship.getHullSize())) * interval.getIntervalDuration() * (2f + ship.getFluxLevel());
	                
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
	                		(7f * (float) Math.random()) + 5f,
	                		0.9f,
	                		0.35f,
	                        SPARK_COLOR);
	                if (REPAIR) {
	                	engine.spawnExplosion(cellLoc, ship.getVelocity(), SPARK_COLOR, 20f, 0.2f);
	                }
	            }
	            ship.syncWithArmorGridState(); // to remove gross damage when repairing, thanks ruddygreat
	            ship.syncWeaponDecalsWithArmorDamage();
	        }
		}
		// Vent Repair Section
		
		
		// Fancy Damage Buff Section
		if (!info.SET) {
			info.THRESHOLD = ship.getHullLevel();
			info.SET = true;
		}
		
		if (ship.getHullLevel() < info.THRESHOLD) {
			info.TIMER += ((info.THRESHOLD - ship.getHullLevel()) * 90f);
			info.THRESHOLD = ship.getHullLevel();
		}
		
		if (info.TIMER > 6f) {
			info.ARMED = true;
		}
		
		if (info.TIMER > 0f && info.ARMED) {
			if (!info.ACTIVE) {
				// INITIAL EFFECT
				info.ACTIVE = true;
				engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), COLOR_EX, ((Float) BLAST_SIZE.get(ship.getHullSize())), 0.25f);	
				Global.getSoundPlayer().playSound("system_temporalshell", 1f, 0.9f, ship.getLocation(), ship.getVelocity());
				// INITIAL EFFECT
			}
			
			interval_2.advance(engine.getElapsedInLastFrame());
			if (interval_2.intervalElapsed()) {
				info.TIMER -= interval_2.getIntervalDuration();
			}
			//TIMER -= engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();
			
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
			
			float hullBonus = Math.min(0.8f, 1 - ship.getHullLevel()) / 0.8f;
			float DAM_RES = 0.1f + (0.4f * hullBonus);
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
		
		engine.getCustomData().put("WARBURN_DATA_KEY" + ship.getId(), info);
		
		
		// AI trickery section
		if (Global.getCombatEngine().isPaused() || ship.getShipAI() == null) {
			return;
		}
		if (ship.getFluxLevel() > 0.9f) {
			info.VENTBRAINTIMER += (amount * 2f);	
        } else if (ship.getFluxLevel() < 0.8f) {
        	info.VENTBRAINTIMER = Math.max(0f, info.VENTBRAINTIMER - amount);
        }
		if (info.VENTBRAINTIMER > 4f && !ship.getSystem().isActive()) {
			ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
			info.VENTBRAINTIMER = 0f;
		}
		
		engine.getCustomData().put("WARBURN_DATA_KEY" + ship.getId(), info);
		
			// while flux is over 90%, gain "brain" value at 2/sec
			// if flux is below 80%, lose "brain" value at 1/sec
			// if "brain" value is at 4 or more, and the system is not currently active, force a vent.
		// AI trickery section
	}
		// So what this hullmod does is as follows:
		// - Increases monthly supply cost by 75%
		// - Reduces overload duration by 15%
		// - Reduces all weapons flux cost as the ship takes hull damage
		// - Repairs armour while venting, the amount repaired scales up based on current flux level
		// -- On taking Hull damage:
		// --- Increment a "timer" gaining 9 seconds of time for each 10% hull damage taken.
		// --- If timer is over 6, start decaying the timer and gain a timeflow + damage resistance buff until the timer runs out.
		// --- Speed and RoF are reduced while this buff is active, to stop you becoming a psycho demon, and to make it more of a "free vent" than a buff.
	
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
		
		LabelAPI label = tooltip.addPara("The internal systems of this ship have been constructed using unknown advanced Domain technology.", opad);
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
    }
}
