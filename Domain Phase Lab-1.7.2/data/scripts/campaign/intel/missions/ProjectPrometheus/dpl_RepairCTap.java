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

public class dpl_RepairCTap extends GABaseMission {

	public static enum Stage {
		REPAIR_CTAP,
		GO_TO_ACADEMY,
		RETURN_TO_CTAP,
		RETURN_TO_ACADEMY,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI eliza_lovelace;
	protected StarSystemAPI system2;
	protected SectorEntityToken dpl_coronal_tap;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$dpl_RepairCTap_ref")) {
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		system2 = Global.getSector().getStarSystem("helheim");
        if (system2 == null) return false;
        dpl_coronal_tap = system2.getEntityById("dpl_helheim_damaged_tap");
        if (dpl_coronal_tap == null) return false;
		
		setStartingStage(Stage.REPAIR_CTAP);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		makeImportant(dpl_coronal_tap, null, Stage.REPAIR_CTAP);
		setStageOnGlobalFlag(Stage.GO_TO_ACADEMY, "$dpl_RepairCTap_Repaired");
		makeImportant(baird.getMarket(), null, Stage.GO_TO_ACADEMY);
		setStageOnGlobalFlag(Stage.RETURN_TO_CTAP, "$dpl_RepairCTap_GotSensor");
		makeImportant(dpl_coronal_tap, null, Stage.RETURN_TO_CTAP);
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, "$dpl_RepairCTap_SensorSet");
		makeImportant(baird.getMarket(), null, Stage.RETURN_TO_ACADEMY);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_RepairCTap_complete");
		
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
		if (currentStage == Stage.REPAIR_CTAP) {
			info.addPara("Go to the Helheim System and repair the damaged coronal tap.", opad);
		} else if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to Galactia Academy to report the project progress.", opad);
		} else if (currentStage == Stage.RETURN_TO_CTAP) {
			info.addPara("Go to the Helheim System and install Eliza's sensor on the damaged coronal tap.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Go to Galactia Academy to report the project progress.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.REPAIR_CTAP) {
			info.addPara("Go to the Helheim System", tc, pad);
			return true;
		} else if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to Galatia Academy", tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_CTAP) {
			info.addPara("Go to the Helheim System", tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Go to Galatia Academy", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Repair the Coronal Tap";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	
}





