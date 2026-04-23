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

public class dpl_PhaseProjection extends BaseHullMod {
	
	public static float CR_LOSS_MULT_FOR_EmergencyForge = 0f;
	public static float REPAIR_MULT_FOR_EmergencyForge = 1f;
	
	public static class dpl_PhaseProjectionScript implements AdvanceableListener, HullDamageAboutToBeTakenListener {
		public ShipAPI ship;
		public boolean resurrection = false;
		public float Progress = 0f;
		public dpl_PhaseProjectionScript(ShipAPI ship) {
			this.ship = ship;
		}
		
		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			if (!resurrection) {
				String key = "dpl_PhaseProjection_canRevive";
				boolean canForge = true;
				float depCost = 0f;
				if (ship.getFleetMember() != null) {
					depCost = ship.getFleetMember().getDeployCost();
				}
				float crLoss = CR_LOSS_MULT_FOR_EmergencyForge * depCost;
				canForge &= ship.getCurrentCR() >= crLoss;
				
				float hull = ship.getHitpoints();
				if (damageAmount >= hull && canForge) {
					ship.setHitpoints(1f);
					if (ship.getFleetMember() != null) { // fleet member is fake during simulation, so this is fine
						ship.getFleetMember().getRepairTracker().applyCREvent(-crLoss, "Phase Projection");
					}
					resurrection = true;
					Global.getCombatEngine().getCustomData().put(key, true);
				}
			}
			
			if (resurrection) {
				return true;
			}
			return false;
		}

		public void advance(float amount) {
			String id = "dpl_phase_projection_modifier";
			if (ship.getOwner() == 0) {
				ship.setHitpoints(-1f);
			}
			if (resurrection) {
				
				Color c = new Color(255,175,255,255);
				c = Misc.setAlpha(c, 255);
				c = Misc.interpolateColor(c, Color.white, 0.5f);
				if (Progress == 0f) {
					if (ship.getFluxTracker().showFloaty()) {
						Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),
								"Phase Projection!",
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
					resurrection = false;
					Progress = 0f;
					ship.getMutableStats().getHullDamageTakenMult().unmodify(id);
				}
			}
		}
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new dpl_PhaseProjectionScript(ship));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)(REPAIR_MULT_FOR_EmergencyForge * 100f) + "%";
		if (index == 1) return "" + (int)(CR_LOSS_MULT_FOR_EmergencyForge * 100f) + "%";
		return null;
	}
}

