package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;


public class CJHM_weaponover extends BaseHullMod {

    public static final float ROF_PER = 25f;
    public static final float FLUX_INCREASE = 20f;


	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyPercent(id, ROF_PER);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, FLUX_INCREASE);

        stats.getEnergyRoFMult().modifyPercent(id, ROF_PER);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, FLUX_INCREASE);

    }

    @Override	
	public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) ROF_PER + "%";
		if (index == 1) return "" + (int) FLUX_INCREASE + "%";
		return null;

	}	
}
