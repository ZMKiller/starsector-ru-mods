package data.scripts.campaign;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_sat_bomb_script implements EveryFrameScript {

	protected IntervalUtil interval = new IntervalUtil(5f, 10f);
	protected float elapsed;
	protected String trigger;

	public dpl_sat_bomb_script(String trigger) {
		this.trigger = trigger;
	}

	public void advance(float amount) {
		interval.advance(amount);
		if (interval.intervalElapsed()) {
			boolean EllyNotThere = (Global.getSector().getImportantPeople().getData("elly_lovelace") == null);
			boolean GKNotThere = (Global.getSector().getImportantPeople().getData("kapteyn_greater") == null);
			boolean hasElly = false;
			boolean hasGK = false;
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			
			if (!EllyNotThere) {
				PersonAPI elly_lovelace = Global.getSector().getImportantPeople().getData("elly_lovelace").getPerson();
				List<OfficerDataAPI> mercs = Misc.getMercs(playerFleet);
				if (elly_lovelace != null && !mercs.isEmpty()) {
					for (OfficerDataAPI od : mercs) {
						if (od.getPerson().equals(elly_lovelace)) {
							hasElly = true;
						}
					}
				}
			}
			
			if (!GKNotThere) {
				PersonAPI kapteyn_greater = Global.getSector().getImportantPeople().getData("kapteyn_greater").getPerson();
				List<OfficerDataAPI> officers = playerFleet.getFleetData().getOfficersCopy();
				if (kapteyn_greater != null && !officers.isEmpty()) {	
					for (OfficerDataAPI od : officers) {
						if (od.getPerson().equals(kapteyn_greater)) {
							hasGK = true;
						}
					}
				}
			}
			
			if (hasGK || hasElly) {
				Misc.showRuleDialog(playerFleet, trigger);
			}
		}
	}
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}	
}



