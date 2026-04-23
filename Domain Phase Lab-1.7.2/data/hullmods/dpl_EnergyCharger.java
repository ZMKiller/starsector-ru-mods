package data.hullmods;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_EnergyCharger extends BaseHullMod {
	public static float DAMAGE_BONUS_PERCENT = 5f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileRoFMult().modifyMult(id, 1f - DAMAGE_BONUS_PERCENT * 0.01f);
		stats.getBallisticRoFMult().modifyMult(id, 1f - DAMAGE_BONUS_PERCENT * 0.01f);
		stats.getEnergyRoFMult().modifyMult(id, 1f - DAMAGE_BONUS_PERCENT * 0.01f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		float dam = DAMAGE_BONUS_PERCENT;
		if (ship.getHullSpec().isPhase()) {
			dam = DAMAGE_BONUS_PERCENT * 2;
		}
		ship.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(id, dam);
		ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(id, dam);
		ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(id, dam);
	}

	@Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "" + (int) DAMAGE_BONUS_PERCENT + "%";
        return null;
    }
	
}









