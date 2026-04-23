//By VladimirVV. Prevents Phase Lab from getting too close to TT before the mission is done.
package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dpl_campaign_relations_plugin implements EveryFrameScript {

    //The ID for the faction that gets their relations adjusted (sorry, couldn't remember your faction ID off the top of my head)
    private static final String MAIN_FACTION = "dpl_phase_lab";

    //Map for faction relation limits: relations will never be above the level shown here for that faction, no matter what
    private static final Map<String, Float> FACTION_RELATION_MAXIMUMS = new HashMap<>();
    static {
        FACTION_RELATION_MAXIMUMS.put(Factions.TRITACHYON, -0.50f);       //Not on the best of terms. Ever.
    }

    @Override
    public void advance( float amount ) {
        //Necessary Sector check
        SectorAPI sector = Global.getSector();
        if (sector == null) {
            return;
        }

        //This situation should never happen. But if it happens, we don't want this code to break the game.
        if (sector.getFaction(MAIN_FACTION) == null) {
            return;
        }

        //Runs the code for downward-adjusting relations semi-sneakily when too high
        FactionAPI dpl_phase_lab = sector.getFaction(MAIN_FACTION);
        for (String s : FACTION_RELATION_MAXIMUMS.keySet()) {
            FactionAPI faction = sector.getFaction(s);
            if (faction == null) { continue; }

            //Adjusts relation downward if it is too high
            if (dpl_phase_lab.getRelationship(faction.getId()) > FACTION_RELATION_MAXIMUMS.get(s)) {
            	if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_canMakePeaceTT")) {
            		dpl_phase_lab.setRelationship(faction.getId(), FACTION_RELATION_MAXIMUMS.get(s));
            	}
            }
        }
    }

    //This code MUST 
    @Override
    public boolean isDone() {
    	if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_canMakePeaceTT")) {
    		return true;
    	}
        return false;
    }

    //No need to run while paused
    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
