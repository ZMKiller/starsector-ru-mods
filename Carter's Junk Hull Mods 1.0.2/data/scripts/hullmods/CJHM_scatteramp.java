package data.scripts.hullmods;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
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

public class CJHM_scatteramp extends BaseHullMod {

	public static float RANGE_MULT = 20f;	
	public static float DAMAGE_BONUS_PERCENT = 10f;

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponRangeBonus().modifyMult(id, 1f - RANGE_MULT * 0.01f);
		stats.getBeamWeaponDamageMult().modifyMult(id, 1f - DAMAGE_BONUS_PERCENT * 0.01f);
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new ScatterAmpDamageDealtMod(ship));
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)RANGE_MULT + "%";
		if (index == 1) return "" + (int)DAMAGE_BONUS_PERCENT + "%";
		return null;
	}
	
	public static class ScatterAmpDamageDealtMod implements DamageDealtModifier {
		protected ShipAPI ship;
		public ScatterAmpDamageDealtMod(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			
			if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
				damage.setForceHardFlux(true);
			}
			return null;
		}
	}
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_brawlerenergy") || ship.getVariant().hasHullMod("high_scatter_amp"))
			return false;
		return super.isApplicableToShip(ship);
	}    
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_brawlerenergy") || ship.getVariant().hasHullMod("high_scatter_amp"))
			return "Incompatible with CJHM Brawler Energy or High Scatter Amplifier";
		return super.getUnapplicableReason(ship);
	}				
	
}








