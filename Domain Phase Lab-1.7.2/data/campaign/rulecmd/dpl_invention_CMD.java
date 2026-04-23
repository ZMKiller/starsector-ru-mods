package data.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 *	dpl_ptrs_CMD <action> <parameters>
 */
public class dpl_invention_CMD extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();	
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
				
		if ("isDwarf".equals(action)) {
			SectorEntityToken planet = dialog.getInteractionTarget();
			if (planet instanceof PlanetAPI) {
				PlanetAPI thePlanet = (PlanetAPI) planet;
				List<SectorEntityToken> AllEntities = thePlanet.getStarSystem().getAllEntities();
				for (SectorEntityToken Entity: AllEntities) {
					if (Entity.isStar()) {
						PlanetAPI theStar = (PlanetAPI) Entity;
						if (theStar.getTypeId().equals(StarTypes.NEUTRON_STAR)) {
							return true;
						}
					}
				}
			}
			return false;
		} else if ("isBlackhole".equals(action)) {
			SectorEntityToken planet = dialog.getInteractionTarget();
			if (planet instanceof PlanetAPI) {
				PlanetAPI thePlanet = (PlanetAPI) planet;
				List<SectorEntityToken> AllEntities = thePlanet.getStarSystem().getAllEntities();
				for (SectorEntityToken Entity: AllEntities) {
					if (Entity.isStar()) {
						PlanetAPI theStar = (PlanetAPI) Entity;
						if (theStar.getTypeId().equals(StarTypes.BLACK_HOLE)) {
							return true;
						}
					}
				}
			}
			return false;
		} else if ("isBlueGiant".equals(action)) {
			SectorEntityToken planet = dialog.getInteractionTarget();
			if (planet instanceof PlanetAPI) {
				PlanetAPI thePlanet = (PlanetAPI) planet;
				List<SectorEntityToken> AllEntities = thePlanet.getStarSystem().getAllEntities();
				for (SectorEntityToken Entity: AllEntities) {
					if (Entity.isStar()) {
						PlanetAPI theStar = (PlanetAPI) Entity;
						if (theStar.getTypeId().equals(StarTypes.BLUE_GIANT) || theStar.getTypeId().equals(StarTypes.BLUE_SUPERGIANT)) {
							return true;
						}
					}
				}
			}
			return false;
		} else if ("checkAllScan".equals(action)) {
			boolean scannedDwarf = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_invention_dwarf");
			boolean scannedBlkHole = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_invention_blackhole");
			boolean scannedBlueG = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_invention_blue_giant");
			
			if (scannedDwarf && scannedBlkHole && scannedBlueG) {
				Global.getSector().getMemoryWithoutUpdate().set("$dpl_invention_scanned", true);
				return true;
			}
			return false;
		} else if ("checkKitAvailable".equals(action)) {
			int daysSinceStart = Global.getSector().getMemoryWithoutUpdate().getInt("$daysSinceStart");
			if (memory.contains("$dpl_lastDayVisit")) {
				int lastVisitTime = memory.getInt("$dpl_lastDayVisit");
		        int numMonths = (int) ((daysSinceStart - lastVisitTime)/30f);
		        if (numMonths > 6) {
		        	numMonths = 6;
		        }
		        if (numMonths > 0) {
		        	memory.set("$dpl_numKits", numMonths, 0);
		        	return true;
		        }
			}
	        return false;
		} else if ("CollectKits".equals(action)) {
			if (memory.contains("$dpl_numKits")) {
				int numKits = memory.getInt("$dpl_numKits");
				CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
				pf.getCargo().addSpecial(new SpecialItemData("dpl_condenser", null), numKits);
				AddRemoveCommodity.addItemGainText(new SpecialItemData("dpl_condenser", null), numKits, text);
				int daysSinceStart = Global.getSector().getMemoryWithoutUpdate().getInt("$daysSinceStart");
		        memory.set("$dpl_lastDayVisit", daysSinceStart);
		        memory.unset("$dpl_numKits");
		        return true;
			}
			return false;
		} else if ("CollectKitsFT".equals(action)) {
			int numKits = 1;
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			pf.getCargo().addSpecial(new SpecialItemData("dpl_condenser", null), numKits);
			AddRemoveCommodity.addItemGainText(new SpecialItemData("dpl_condenser", null), numKits, text);
			int daysSinceStart = Global.getSector().getMemoryWithoutUpdate().getInt("$daysSinceStart");
		    memory.set("$dpl_lastDayVisit", daysSinceStart);
		    memory.unset("$dpl_numKits");
		    return true;
		}
		return false;
	}
	
}
