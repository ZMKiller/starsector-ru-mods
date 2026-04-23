package org.amazigh.foundry.hullmods;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ASF_pritzlFlux extends BaseHullMod {

	public static final float MAX_VENT_BONUS = 75f;
	
	private IntervalUtil visInterval1 = new IntervalUtil(0.12f,0.18f);
	private IntervalUtil visInterval2 = new IntervalUtil(0.12f,0.18f);
	
	public static final float ENERGY_MULT = 10f;
	
	// particle variables
	public static final float OVOID_STRETCH = 30f;
	public static final float DIST_RANGE = 69f;
	public static final float P_VEL_MIN = 30f;
	public static final float P_VEL_MAX = 80f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_MULT);
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_MULT);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("PRITZL_FLUX_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        CombatEngineAPI engine = Global.getCombatEngine();
        
    	if (!ship.getFluxTracker().isVenting()) {

            if (ship.getFluxLevel() > 0f) {
            	info.BONUS_SCALE = 1f - ship.getHardFluxLevel() / ship.getFluxLevel();
            } else {
            	info.BONUS_SCALE = 1f;
            }    
    	}
    	
        // boost vent rate by the determined value based on current hard/soft flux levels
    	ship.getMutableStats().getVentRateMult().modifyPercent(ship.getId(), info.BONUS_SCALE * MAX_VENT_BONUS);
        
    	if (ship.getFluxTracker().isVenting()) {
        	
			visInterval1.advance(amount);
            if (visInterval1.intervalElapsed()) {
            	
            	float sparkangle = 0f;
            	Vector2f sparkPoint= ship.getLocation();
            	Vector2f sparkVel = ship.getVelocity();
            	if (Math.random() > 0.5) {
            		sparkangle = ship.getFacing() + MathUtils.getRandomNumberInRange(0f, 180f);
            		Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() + 90f);
            		sparkPoint = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle);
            		sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle + MathUtils.getRandomNumberInRange(-10f, 10f));
        		} else {
        			sparkangle = ship.getFacing() - MathUtils.getRandomNumberInRange(0f, 180f);
        			Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() - 90f);
            		sparkPoint = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle);
            		sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle + MathUtils.getRandomNumberInRange(-10f, 10f));
        		}
				Global.getCombatEngine().addSmoothParticle(sparkPoint,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						0.6f * info.BONUS_SCALE, //brightness
						0.7f, //duration
						new Color(70,150,135,255));
				
				float sparkangle2 = 0f;
            	Vector2f sparkPoint2 = ship.getLocation();
            	Vector2f sparkVel2 = ship.getVelocity();
            	if (Math.random() > 0.5) {
            		sparkangle2 = ship.getFacing() + MathUtils.getRandomNumberInRange(0f, 180f);
            		Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() + 90f);
            		sparkPoint2 = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle);
            		sparkVel2 = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle + MathUtils.getRandomNumberInRange(-10f, 10f));
        		} else {
        			sparkangle2 = ship.getFacing() - MathUtils.getRandomNumberInRange(0f, 180f);
        			Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() - 90f);
            		sparkPoint2 = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle2);
            		sparkVel2 = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle2 + MathUtils.getRandomNumberInRange(-10f, 10f));
        		}
				Global.getCombatEngine().addSmoothParticle(sparkPoint2,
						sparkVel2,
    					MathUtils.getRandomNumberInRange(4f, 9f), //size
    					0.6f * info.BONUS_SCALE, //brightness
    					0.7f, //duration
    					new Color(70,165,135,255));
						// new Color(70,150,135,255));
				
				
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
				int green = MathUtils.getRandomNumberInRange(150, 175);
				int blue = MathUtils.getRandomNumberInRange(125, 150);
				int alpha = (int) (70f * ship.getFluxLevel());
		        
				engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), 111f * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), 111f * (1f- dist), angle),
						60f,
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.6f,
						1f,
						new Color(70,green,blue,alpha),
						false);
		        
            }
            
            visInterval2.advance(amount);
            if (visInterval2.intervalElapsed()) {
            	
            	float sparkangle = 0f;
            	Vector2f sparkPoint= ship.getLocation();
            	Vector2f sparkVel = ship.getVelocity();
            	if (Math.random() > 0.5) {
            		sparkangle = ship.getFacing() + MathUtils.getRandomNumberInRange(0f, 180f);
            		Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() + 90f);
            		sparkPoint = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle);
            		sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle + MathUtils.getRandomNumberInRange(-10f, 10f));
        		} else {
        			sparkangle = ship.getFacing() - MathUtils.getRandomNumberInRange(0f, 180f);
        			Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() - 90f);
            		sparkPoint = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle);
            		sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle + MathUtils.getRandomNumberInRange(-10f, 10f));
        		}
				Global.getCombatEngine().addSmoothParticle(sparkPoint,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						0.6f * info.BONUS_SCALE, //brightness
						0.7f, //duration
						new Color(70,150,135,255));
				
				float sparkangle2 = 0f;
            	Vector2f sparkPoint2 = ship.getLocation();
            	Vector2f sparkVel2 = ship.getVelocity();
            	if (Math.random() > 0.5) {
            		sparkangle2 = ship.getFacing() + MathUtils.getRandomNumberInRange(0f, 180f);
            		Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() + 90f);
            		sparkPoint2 = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle);
            		sparkVel2 = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle + MathUtils.getRandomNumberInRange(-10f, 10f));
        		} else {
        			sparkangle2 = ship.getFacing() - MathUtils.getRandomNumberInRange(0f, 180f);
        			Vector2f sparkPointTemp = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(1f, OVOID_STRETCH), ship.getFacing() - 90f);
            		sparkPoint2 = MathUtils.getPointOnCircumference(sparkPointTemp, MathUtils.getRandomNumberInRange(1f, DIST_RANGE), sparkangle2);
            		sparkVel2 = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(P_VEL_MIN, P_VEL_MAX), sparkangle2 + MathUtils.getRandomNumberInRange(-10f, 10f));
        		}
				Global.getCombatEngine().addSmoothParticle(sparkPoint2,
						sparkVel2,
    					MathUtils.getRandomNumberInRange(4f, 9f), //size
    					0.6f * info.BONUS_SCALE, //brightness
    					0.7f, //duration
    					new Color(70,165,135,255));
						// new Color(70,150,135,255));
		        
				
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
				int green = MathUtils.getRandomNumberInRange(150, 175);
				int blue = MathUtils.getRandomNumberInRange(125, 150);
				int alpha = (int) (70f * ship.getFluxLevel());
		        
				engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), 111f * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), 111f * (1f- dist), angle),
		        		60f,
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.6f,
						1f,
						new Color(70,green,blue,alpha),
						false);
            }
        }
        
        if (ship == Global.getCombatEngine().getPlayerShip()) {
        	String note = "%";
        	if (ship.getFluxTracker().isVenting()) {
        		note = "% - Bonus Locked - Venting";
        	}
        	Global.getCombatEngine().maintainStatusForPlayerShip("PRITZLVENT", "graphics/icons/tactical/venting_flux.png", "Pritzl Coherer Vent Bonus: ", Math.round(info.BONUS_SCALE * MAX_VENT_BONUS) + note, false);
		}
        
        engine.getCustomData().put("PRITZL_FLUX_DATA_KEY" + ship.getId(), info);
	}
	

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
		
		LabelAPI label = tooltip.addPara("Specialised flux grid addition that is suited for hit and run engagements, but suffers interference when pressure is inflicted on the ships shield emitter.", pad);
		
		label = tooltip.addPara("Energy weapon damage and flux cost increased by %s.", opad, h, "" + (int)ENERGY_MULT+ "%");
		label.setHighlight("" + (int)ENERGY_MULT + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Increases active flux vent rate by up to %s.", opad, h, "" + (int)MAX_VENT_BONUS + "%");
		label.setHighlight("" + (int)MAX_VENT_BONUS + "%");
		label.setHighlightColors(h);
		
		tooltip.addPara("Ship will recieve the full vent rate bonus when it has no hard flux generated.", 4f);
		tooltip.addPara("The bonus scales down based on what proportion of current flux is hard flux, with no bonus if all flux is hard flux.", pad);
	}

    private class ShipSpecificData {
        private float BONUS_SCALE = 0f;
    }

}
