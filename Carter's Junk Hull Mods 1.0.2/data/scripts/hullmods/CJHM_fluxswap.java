package data.scripts.hullmods;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CJHM_fluxswap extends BaseHullMod {
	
    public static final float DIS = 1.10f;
    public static final float CAP = .90f;
    
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
           stats.getFluxDissipation().modifyMult(id, 1f * DIS);
           stats.getFluxCapacity().modifyMult(id, 1f * CAP);           
        }
      
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "10";			
        if (index == 1) return "10";	
         return null;
    }
}