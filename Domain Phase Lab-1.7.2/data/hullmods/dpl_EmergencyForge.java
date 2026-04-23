package data.hullmods;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_EmergencyForge extends BaseHullMod {
	
	public static float CR_LOSS_MULT_FOR_EmergencyForge = 1f;
	public static float REPAIR_MULT_FOR_EmergencyForge = 0.4f;
	public static float REPAIR_THRESHOLD = 0.2f;
	
	public static class dpl_EmergencyForgeScript implements AdvanceableListener, HullDamageAboutToBeTakenListener {
		public ShipAPI ship;
		public boolean emergencyForgeBegin = false;
		public boolean emergencyForge = false;
		public boolean emergencyForged = false;
		public float Progress = 0f;
		public dpl_EmergencyForgeScript(ShipAPI ship) {
			this.ship = ship;
		}
		
		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			if (!emergencyForgeBegin && !emergencyForged) {
				String key = "dpl_EmergencyForge_canForge";
				boolean canForge = !Global.getCombatEngine().getCustomData().containsKey(key);
				float depCost = 0f;
				if (ship.getFleetMember() != null) {
					depCost = ship.getFleetMember().getDeployCost();
				}
				float crLoss = CR_LOSS_MULT_FOR_EmergencyForge * depCost;
				canForge &= ship.getCurrentCR() >= crLoss;
				
				float hull = ship.getHitpoints();
				float max_hull = ship.getMaxHitpoints();
				if (max_hull * REPAIR_THRESHOLD > hull - damageAmount && canForge) {
					ship.setHitpoints(max_hull * REPAIR_THRESHOLD);
					if (ship.getFleetMember() != null) { // fleet member is fake during simulation, so this is fine
						ship.setCurrentCR(ship.getCurrentCR() - crLoss);
						ship.getFleetMember().getRepairTracker().applyCREvent(-crLoss, "Emergency Forge");
					}
					emergencyForge = true;
					emergencyForgeBegin = true;
					Global.getCombatEngine().getCustomData().put(key, true);
				}
			}
			
			if (emergencyForgeBegin) {
				return true;
			}
			
			return false;
		}

		public void advance(float amount) {
			if (emergencyForge && !emergencyForged) {
				
				Color c = new Color(255,175,255,255);
				c = Misc.setAlpha(c, 255);
				c = Misc.interpolateColor(c, Color.white, 0.5f);
				
				float initFlux = ship.getMaxFlux() * REPAIR_MULT_FOR_EmergencyForge * 0.75f;
				float initHardFlux = ship.getMaxFlux() * REPAIR_MULT_FOR_EmergencyForge * 0.75f;
				float maxHealth = ship.getMaxHitpoints()*REPAIR_MULT_FOR_EmergencyForge;
				
				if (Progress == 0f) {
					initFlux = ship.getFluxTracker().getCurrFlux() * REPAIR_MULT_FOR_EmergencyForge * 0.75f;
					initHardFlux = ship.getFluxTracker().getHardFlux() * REPAIR_MULT_FOR_EmergencyForge * 0.75f;
					
					ship.setOverloadColor(c);
					ship.getFluxTracker().beginOverloadWithTotalBaseDuration(2f);
					
					if (ship.getFluxTracker().showFloaty()) {
						Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),
								"Emergency Forge!",
								NeuralLinkScript.getFloatySize(ship), c, ship, 16f , 3.2f, 1f, 0f, 0f,
								1f);
						ship.getFluxTracker().playOverloadSound();
					}
				}
				
				ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
				Progress += amount * 0.5f;
				
				if (ship.isAlive()) {
					ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux() - initFlux * 0.5f * amount);
					ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getHardFlux() - initHardFlux * 0.5f * amount);
					ship.setHitpoints(ship.getHitpoints() + maxHealth * 0.5f * amount);
				}
				
				if (Progress >= 0.05f) {
					emergencyForgeBegin = false;
				}
				
				if (Progress >= 1f) {
					Global.getSoundPlayer().playSound("phase_anchor_vanish", 1f, 1f, ship.getLocation(), ship.getVelocity());
					float r = ship.getCollisionRadius();
					ship.setJitter(this, c, 0.5f, 20, r*0.5f);
					if (ship.getFluxTracker().isOverloadedOrVenting()) {
						ship.getFluxTracker().setOverloadDuration(0f);
					}
					emergencyForged = true;
					emergencyForge = false;
				}
			}
		}
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new dpl_EmergencyForgeScript(ship));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)(REPAIR_MULT_FOR_EmergencyForge * 100f) + "%";
		if (index == 1) return "" + (int)(CR_LOSS_MULT_FOR_EmergencyForge * 100f) + "%";
		return null;
	}
}

