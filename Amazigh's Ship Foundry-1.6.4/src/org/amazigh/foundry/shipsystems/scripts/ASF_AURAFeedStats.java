package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_AURAFeedStats extends BaseShipSystemScript {

	public static final float TIME_MULT = 1.5f;
	public static final float SHIELD_BONUS = 20f;
	public static final float FLUX_BONUS = 35f;
	public static final float RATE_MULT = 0.4f;
    
    public static final float TIME_MULT_F = 3f;
    public static final float TIME_SPEED_F = 0.4f;
    public static final float DAM_RES_F = 0.4f;
    public Color ENGINE_COLOR_F = new Color(90,255,165,55);
    
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		
		float shipTimeMult = 1f + ((TIME_MULT - 1f) * effectLevel);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		float shieldMult = 1f - ((SHIELD_BONUS / 100f ) * effectLevel);
		stats.getShieldDamageTakenMult().modifyMult(id, shieldMult);
		
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_BONUS);
		stats.getMissileWeaponFluxCostMod().modifyPercent(id, -FLUX_BONUS);
		
		float rateMult = 1f + (RATE_MULT * effectLevel);
		stats.getBallisticRoFMult().modifyMult(id, rateMult);
		stats.getMissileRoFMult().modifyMult(id, rateMult);
		stats.getBallisticAmmoRegenMult().modifyMult(id, 1f + effectLevel);
		stats.getMissileAmmoRegenMult().modifyMult(id, 1f + effectLevel);
		
		float fighterTimeMult = 1f + ((TIME_MULT_F - 1f) * effectLevel);
		
		for (ShipAPI fighter : getFighters(ship)) {
			
            if (fighter.isHulk()) {
                continue;
            }
            
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            
            // Stats
			fStats.getMaxSpeed().modifyMult(id, 1f - (TIME_SPEED_F * effectLevel));
			
			fStats.getHullDamageTakenMult().modifyMult(id, 1f - (DAM_RES_F*effectLevel));
			fStats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAM_RES_F*effectLevel));
			fStats.getEmpDamageTakenMult().modifyMult(id, 1f - (DAM_RES_F*effectLevel));
			
            fStats.getTimeMult().modifyMult(id, fighterTimeMult);
			
			
			// FX
			float ALPHA_1 = (effectLevel * 40f) + 10f;
			float ALPHA_2 = (effectLevel * 70f) + 20f;
			
			Color JITTER_COLOR = new Color(90,255,165,(int)ALPHA_1);
			Color JITTER_UNDER_COLOR = new Color(90,255,165,(int)ALPHA_2);
			
			float jitterRangeBonus_1 = (effectLevel * 10f) + 4f;
			float jitterRangeBonus_2 = (effectLevel * 20f) + 8f;
			
			float jitterLevel = (effectLevel * 0.7f) + 0.2f;
			
			fighter.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 8f + jitterRangeBonus_1);
			fighter.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 10, 0f, 12f + jitterRangeBonus_2);
			
			fighter.getEngineController().fadeToOtherColor(this, ENGINE_COLOR_F, new Color(15,0,30,40), effectLevel, 0.6f);
			fighter.getEngineController().extendFlame(this, 0.2f, 0.2f, 0.2f);
        }
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getTimeMult().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
		Global.getCombatEngine().getTimeMult().unmodify(id);
		
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getMissileRoFMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);
		
		for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (fighter.isHulk()) {
                continue;
            }
            if (!fighter.isFighter()) {
                continue;
            }
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            
            fStats.getMaxSpeed().unmodify(id);
            fStats.getHullDamageTakenMult().unmodify(id);
            fStats.getArmorDamageTakenMult().unmodify(id);
            fStats.getEmpDamageTakenMult().unmodify(id);
            
            fStats.getTimeMult().unmodify(id);
        }
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = (SHIELD_BONUS * effectLevel);
		
		if (index == 0) {
			return new StatusData("time flow altered", false);
		}
		if (index == 1) {
			return new StatusData("shield damage taken reduced by " + (int) bonusPercent + "%", false);
		}
		if (index == 2) {
			return new StatusData("fighter performance enhanced", false);
		}
		return null;
	}
	


    public static List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<ShipAPI>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }
	
}
