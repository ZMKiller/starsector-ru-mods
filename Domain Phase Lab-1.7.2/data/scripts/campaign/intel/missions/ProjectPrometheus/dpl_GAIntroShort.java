package data.scripts.campaign.intel.missions.ProjectPrometheus;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.academy.GABaseMission;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_GAIntroShort extends GABaseMission {

	public static enum Stage {
		GO_TO_ACADEMY,
		COMPLETED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI eliza_lovelace;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$dpl_gaIntroShort_ref")) {
			return false;
		}
		
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		
		eliza_lovelace = getImportantPerson("eliza_lovelace");
		if (eliza_lovelace == null) return false;
		
		setStartingStage(Stage.GO_TO_ACADEMY);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		makeImportant(baird.getMarket(), null, Stage.GO_TO_ACADEMY);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_gaIntroShort_completed");
		
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
		if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to the Galatia Academy and meet with the new Provost, " + baird.getNameString() + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to the Galatia Academy", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Go to Academy with Eliza";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	
}





