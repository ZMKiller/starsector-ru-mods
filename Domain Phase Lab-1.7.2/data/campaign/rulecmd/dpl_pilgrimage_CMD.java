package data.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
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
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 *	dpl_jasp_CMD <action> <parameters>
 */
public class dpl_pilgrimage_CMD extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
				
		if ("findVibraphone".equals(action)) {
			for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
				if (member.getHullSpec().getBaseHullId().equals("dpl_vibraphone_wreck")) {
					memory.set("$foundShipId", member.getId());				
					memory.set("$foundShipClass", member.getHullSpec().getNameWithDesignationWithDashClass());				
					memory.set("$foundShipName", member.getShipName());				
					return true;
				}
			}
		} else if ("endEmergency".equals(action)) {
			MarketAPI market3 = Global.getSector().getEconomy().getMarket("dpl_research_site_v");
			PersonAPI eliza_lovelace = Global.getSector().getImportantPeople().getData("eliza_lovelace").getPerson();
			PersonAPI banach_salazar = Global.getSector().getImportantPeople().getData("banach_salazar").getPerson();
			if (market3 != null) {
				market3.getCommDirectory().removePerson(banach_salazar);
				market3.getCommDirectory().removePerson(eliza_lovelace);
				eliza_lovelace.setPostId("committeeMember");
				banach_salazar.setPostId(Ranks.POST_FACTION_LEADER);
				banach_salazar.setRankId(Ranks.FACTION_LEADER);
				market3.getCommDirectory().addPerson(banach_salazar, 0);
				market3.getCommDirectory().addPerson(eliza_lovelace, 1);
			}
			
		} else if ("playMusic".equals(action)) {
			String dpl_pilgrimageMusic = "dpl_music_pilgrimage";
			Global.getSoundPlayer().playCustomMusic(1, 1, dpl_pilgrimageMusic, true);
		} else if ("endMusic".equals(action)) {
			Global.getSoundPlayer().restartCurrentMusic();
		}
		
		
		return false;
	}
}
