package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;


public class CJHM_brawlerballistics extends BaseHullMod {
	
	public static final float DAMAGE = 15f;
	public static final float FLUX = -15f;
	public static final float RANGE = -40f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponDamageMult().modifyPercent(id,DAMAGE);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id,FLUX);		
		stats.getBallisticWeaponRangeBonus().modifyPercent(id,RANGE);
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) DAMAGE + "%";
        if (index == 1) return "" + (int) FLUX + "%";
        if (index == 2) return "" + (int) RANGE + "%";
        return null;
    }
}
