package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class dpl_lcCMD extends BaseCommandPlugin {

	protected FleetMemberAPI member;

	public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
	      
	    String action = params.get(0).getString(memoryMap);
			
		final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false;
	    
		if ("dpl_PickShip".equals(action)) {
			ArrayList<FleetMemberAPI> members = (ArrayList<FleetMemberAPI>) Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
		    int cols = Math.max(Math.min(members.size(), 7), 4);
		    if (members.size() > 30) {
		       cols = 14;
		    }

		    int rows = (members.size() - 1) / cols + 1;
		         
		    dialog.showFleetMemberPickerDialog("Select Your Ship",
		 		"Confirm",
		 		"Cancel",
		 		rows, cols, 96,
		 		true, false, members,
		 		new FleetMemberPickerListener() {
		 			public void pickedFleetMembers(List<FleetMemberAPI> members) {
		 				if (members.isEmpty()) return;
		 				member = members.get(0);
		 				memory.set("$dpl_LC_pickedShip", member, 0);
		 				FireAll.fire(null, dialog, memoryMap, "dpl_ShipPicked");
		 			}
		 			public void cancelledFleetMemberPicking() {

		 			}
		 		});
		    return true;
		} else if ("dpl_CheckShip".equals(action)) {
			String hullmodId = "dpl_logistic_center";
			member = (FleetMemberAPI) memory.get("$dpl_LC_pickedShip");
			if (member == null) {
				return false;
			} else {
				boolean hasHM = (member.getVariant().getHullMods().contains(hullmodId));
				int n = member.getVariant().getUnusedOP(null);
				int k = 99999;
				if (member.isCruiser()) {
					k = 25;
				} else if (member.isCapital()) {
					k = 35;
				}
				if (hasHM || k>n) return false;
			}
			return true;
		} else if ("dpl_AddHullmod".equals(action)) {
			String hullmodId = "dpl_logistic_center";
			member = (FleetMemberAPI) memory.get("$dpl_LC_pickedShip");
			if (member == null) {
				return false;
			} else {
				member.getVariant().addMod(hullmodId);
			}
			return true;
		} else if ("dpl_CheckLC".equals(action)) {
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
				if (member.getVariant().getHullMods().contains("dpl_logistic_center")) {		
					return true;
				}
			}
		} else if ("dpl_moveCamilla".equals(action)) {
			PersonAPI camilla_wiener = Global.getSector().getImportantPeople().getData("camilla_wiener").getPerson();
			MarketAPI lab_factory = Global.getSector().getEconomy().getMarket("dpl_factory");
			if (lab_factory == null) {return false;}
			Misc.moveToMarket(camilla_wiener, lab_factory, true);
			camilla_wiener.setRankId("spaceCaptain");
			camilla_wiener.setPostId("technicalSpecialist");
		}
		return false;
	}
}