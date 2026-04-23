package data.scripts.campaign.intel.missions.ProjectPrometheus;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.academy.GABaseMission;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_ActivateHELA extends GABaseMission {

	public static enum Stage {
		ACTIVATE_HELA,
		GO_TO_ACADEMY,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI eliza_lovelace;
	protected StarSystemAPI system2;
	protected SectorEntityToken dpl_HELA_device;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$dpl_ActivateHELA_ref")) {
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		system2 = Global.getSector().getStarSystem("helheim");
        if (system2 == null) return false;
        dpl_HELA_device = system2.getEntityById("dpl_HELA_Device");
        if (dpl_HELA_device == null) return false;
		
		setStartingStage(Stage.ACTIVATE_HELA);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		makeImportant(dpl_HELA_device, null, Stage.ACTIVATE_HELA);
		setStageOnGlobalFlag(Stage.GO_TO_ACADEMY, "$dpl_AHela_activated");
		makeImportant(baird.getMarket(), null, Stage.GO_TO_ACADEMY);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_AHela_completed");
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
	
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.ACTIVATE_HELA) {
			info.addPara("Go to the Helheim System to activate the HELA Device.", opad);
		} else if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to the Galactia Academy to report the project progress.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.ACTIVATE_HELA) {
			info.addPara("Go to the Helheim System", tc, pad);
			return true;
		} else if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to the Galatia Academy", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Activate the HELA Device";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	
}





