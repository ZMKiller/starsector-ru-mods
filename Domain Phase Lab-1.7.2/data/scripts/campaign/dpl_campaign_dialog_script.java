package data.scripts.campaign;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_campaign_dialog_script implements EveryFrameScript {

	protected IntervalUtil interval = new IntervalUtil(0f, 1f);
	protected float elapsed;
	protected String trigger;
	protected boolean isDone = false;

	public dpl_campaign_dialog_script(String trigger) {
		this.trigger = trigger;
	}

	public void advance(float amount) {
		interval.advance(amount);
		if (interval.intervalElapsed()) {
			CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
			Misc.showRuleDialog(playerFleet, trigger);
			isDone = true;
		}
	}
	
	public boolean isDone() {
		return isDone;
	}

	public boolean runWhilePaused() {
		return false;
	}	
}



