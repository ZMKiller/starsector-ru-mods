package data.hullmods;

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
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_PDTargetingCore extends BaseHullMod {

	public static float DAMAGE_PENALTY = 0.1f;
	public static float RANGE_THRESHOLD_PD = 700f;
	public static float RANGE_MULT = 0.2f;
	
	public static float SMOD_MODIFIER = 5f;
	
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		stats.getEnergyWeaponDamageMult().modifyPercent(id, (sMod ? SMOD_MODIFIER : 0));
	}

	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round(SMOD_MODIFIER) + "%";
		return null;
	}


	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new dpl_pd_targetingRangeMod());
		
		boolean hasLargeNonPD = false;
		for (WeaponAPI weapons: ship.getAllWeapons()) {
			if (!weapons.isDecorative() && weapons.getSize().equals(WeaponSize.LARGE) && !(weapons.hasAIHint(AIHints.PD))) {
				hasLargeNonPD = true;
			}
		}
		if (hasLargeNonPD) {
			ship.addListener(new dpl_pd_targetingDamageMod(ship));
		}
	}
	
	public static class dpl_pd_targetingRangeMod implements WeaponBaseRangeModifier {
		public dpl_pd_targetingRangeMod() {
		}
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			float range = weapon.getSpec().getMaxRange();
			if (range < RANGE_THRESHOLD_PD) return 0;
			
			float past = range - RANGE_THRESHOLD_PD;
			float penalty = past * (1f - RANGE_MULT);
			return -penalty;
		}
	}
	
	public static class dpl_pd_targetingDamageMod implements DamageDealtModifier {
		protected ShipAPI ship;
		public dpl_pd_targetingDamageMod(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			damage.setDamage(damage.getDamage()*DAMAGE_PENALTY);
			return null;
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int)RANGE_PENALTY_PERCENT + "%";
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addPara("If this ship has any large non-PD weapon, its system will greatly suffer, resulting in a %s penalty of its damage.", opad, h,
				"" + (int)Math.round((1f - DAMAGE_PENALTY) * 100f) + "%"
				);
		
		tooltip.addPara("Reduces the portion of the range of all weapons that is above %s by %s. The base range is affected.", opad, h,
				"" + (int)RANGE_THRESHOLD_PD,
				"" + (int)Math.round((1f - RANGE_MULT) * 100f) + "%"
				);
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("The base range is reduced, thus percentage and multiplicative modifiers - such as from Integrated Targeting Unit, "
				+ "skills, or similar sources - apply to the reduced base value.", opad);
	}
}









