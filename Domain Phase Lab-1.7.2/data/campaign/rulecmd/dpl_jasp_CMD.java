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
public class dpl_jasp_CMD extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
				
		if ("giveAnthem".equals(action)) {
			giveAnthem(dialog, params, memoryMap);
		} else if ("giveExecutor".equals(action)) {
			giveExecutor(dialog, params, memoryMap);
		} else if ("findExecutor".equals(action)) {
			for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
				if (member.getHullSpec().getHullId().equals("executor") || member.getHullSpec().getHullId().equals("executor_default_D")) {
					memory.set("$foundShipId", member.getId());				
					memory.set("$foundShipClass", member.getHullSpec().getNameWithDesignationWithDashClass());				
					memory.set("$foundShipName", member.getShipName());				
					return true;
				}
			}
		} else if ("diktatLearnAnthem".equals(action)) {
			FactionAPI faction = Global.getSector().getFaction("sindrian_diktat");
			String id = "dpl_lion_anthem";
			faction.clearShipRoleCache();
			if (!faction.knowsShip(id)) {
				faction.addKnownShip(id, true);
				faction.addUseWhenImportingShip(id);
				faction.getHullFrequency().put(id, 0.5f);
			} ;
		}
		return false;
	}

	protected void giveExecutor(InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		boolean removeSpecial = params.get(1).getBoolean(memoryMap);
		
		ShipVariantAPI v = Global.getSettings().getVariant("executor_Hull").clone();
		if (removeSpecial) {
			v.addSuppressedMod(HullMods.ANDRADA_MODS);
		}
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveAnthem(InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		boolean removeSpecial = params.get(1).getBoolean(memoryMap);
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_lion_anthem_Hull").clone();
		if (removeSpecial) {
			v.addSuppressedMod(HullMods.ANDRADA_MODS);
		}
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
}
