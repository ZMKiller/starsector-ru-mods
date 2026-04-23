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

import data.scripts.campaign.intel.missions.ProjectPrometheus.dpl_RepairCTap.Stage;

public class dpl_DeployDevice extends GABaseMission {

	public static enum Stage {
		GO_TO_FACTORY,
		ESCORT_HELA_DEVICE,
		RETURN_TO_ACADEMY,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI ross_higgs;
	protected StarSystemAPI system2;
	protected SectorEntityToken helheim_star;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$dpl_DeployDevice_ref")) {
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		
		ross_higgs = getImportantPerson("ross_higgs");
		if (ross_higgs == null) return false;
		
		system2 = Global.getSector().getStarSystem("helheim");
        if (system2 == null) return false;
        helheim_star = system2.getStar();
		
		setStartingStage(Stage.GO_TO_FACTORY);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		makeImportant(ross_higgs.getMarket(), null, Stage.GO_TO_FACTORY);
		makeImportant(ross_higgs, null, Stage.GO_TO_FACTORY);
		setStageOnGlobalFlag(Stage.ESCORT_HELA_DEVICE, "$dpl_DD_DeviceBuilt");
		makeImportant(helheim_star, null, Stage.ESCORT_HELA_DEVICE);
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, "$dpl_DD_DeviceDeployed");
		makeImportant(baird.getMarket(), null, Stage.RETURN_TO_ACADEMY);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_DD_completed");
		
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
		if (currentStage == Stage.GO_TO_FACTORY) {
			info.addPara("Go to the Lab Factory and meet with Ross.", opad);
		} else if (currentStage == Stage.ESCORT_HELA_DEVICE) {
			info.addPara("Set up a stable location in the Helheim System, and deploy the HELA device there.", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Go to Galactia Academy to report the project progress.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_FACTORY) {
			info.addPara("Go to the Lab Factory", tc, pad);
			return true;
		} else if (currentStage == Stage.ESCORT_HELA_DEVICE) {
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
		return "Deploy the Device";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	
}





