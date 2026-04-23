package data.scripts.campaign.intel.missions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import data.scripts.campaign.intel.missions.dpl_BeInDeepWater.Stage;

public class dpl_MarineProductionMission extends HubMissionWithBarEvent implements EconomyTickListener, TooltipCreator {

	public static float MISSION_DAYS = 365f * 2f;
	public static int MISSION_CYCLES = (int) Math.round(MISSION_DAYS / 365f);
	public static int CONTRACT_DAYS = (int) Math.round(365f * 5f);
	public static int CONTRACT_MONTHS = (int) Math.round(CONTRACT_DAYS * 12f / 365f);
	public static int CONTRACT_CYCLES = (int) Math.round(CONTRACT_DAYS * 1f / 365f);
	
	public static enum Stage {
		WAITING,
		PAYING,
		COMPLETED,
		FAILED,
	}
	
	public static class CheckPlayerProduction implements ConditionChecker {
		protected String commodityId;
		protected int quantity;
		public CheckPlayerProduction(String commodityId, int quantity) {
			this.commodityId = commodityId;
			this.quantity = quantity;
		}
		public boolean conditionsMet() {
			return isPlayerProducing(commodityId, quantity);
		}
	}
	
//	public static class ConditionsMet implements TriggerAction {
//		
//	}
	
	protected String commodityId;
	protected int needed;
	protected int monthlyPayment;
	protected int totalPayment;
	
	protected int monthsRemaining;
	protected String uid;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		if (!setPersonMissionRef(person, "$dpl_mpm_ref")) {
			return false;
		}
		
		needed = Global.getSector().getMemory().getInt("$dpl_marine_amount");
		
		MarketAPI market = getPerson().getMarket();
		if (market == null) return false;
		if (market.isPlayerOwned()) return false;
		
		commodityId = Commodities.MARINES;
		
		//float basePayment = getSpec().getExportValue();
		float basePayment = 1000 + getSpec().getBasePrice() * 10;
		
		monthlyPayment = getRoundNumber(basePayment * needed);
		totalPayment = (int) Math.round(monthlyPayment * CONTRACT_MONTHS);
		if (monthlyPayment <= 0) return false;
		
		setStartingStage(Stage.WAITING);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		connectWithCustomCondition(Stage.WAITING, Stage.PAYING, new CheckPlayerProduction(commodityId, needed));
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.PAYING);
		
		monthsRemaining = (int) CONTRACT_MONTHS;
		
		beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				Global.getSector().getMemoryWithoutUpdate().set("$isProducingMarines", false);
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.FAILED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				Global.getSector().getMemoryWithoutUpdate().set("$isProducingMarines", false);
			}
		});
        endTrigger();
		
		return true;
	}
	
	@Override
	public void setCurrentStage(Object next, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.setCurrentStage(next, dialog, memoryMap);
		
		if (next == Stage.PAYING) {
			addPotentialContacts(dialog);
		}
	}

	protected void updateInteractionDataImpl() {
		set("$dpl_mpm_barEvent", isBarEvent());
		set("$dpl_mpm_manOrWoman", getPerson().getManOrWoman());
		set("$dpl_mpm_monthlyPayment", Misc.getWithDGS(monthlyPayment));
		set("$dpl_mpm_underworld", getPerson().hasTag(Tags.CONTACT_UNDERWORLD));
		set("$dpl_mpm_totalPayment", Misc.getWithDGS(totalPayment));
		set("$dpl_mpm_missionCycles", MISSION_CYCLES);
		set("$dpl_mpm_contractCycles", CONTRACT_CYCLES);
		set("$dpl_mpm_commodityName", getSpec().getLowerCaseName());
		set("$dpl_mpm_needed", needed);
		set("$dpl_mpm_playerHasColony", !Misc.getPlayerMarkets(false).isEmpty());
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.WAITING) {
			info.addPara("Produce at least %s units of " + getSpec().getLowerCaseName() + " at " +
					"a colony under your control.", opad, h, "" + needed);
			info.addPara("Once these terms are met, you will receive %s per month for the next " +
					"%s cycles, for a total of %s, as long as production is maintained.", opad, h,
					Misc.getDGSCredits(monthlyPayment),
					"" + (int)CONTRACT_CYCLES,
					Misc.getDGSCredits(totalPayment));
			if (!playerHasAColony()) {
				info.addPara("You will need to survey a suitable planet and establish a colony to complete " +
							 "this mission.", opad);
			}
		} else if (currentStage == Stage.PAYING) {
			info.addPara("You've met the initial terms of the contract to produce %s units of " + 
						 getSpec().getLowerCaseName() + " at " +
						 "a colony under your control.", opad, h, "" + needed);
			info.addPara("As long these terms are met, you will receive %s per month over %s cycles for " +
					"a total payout of %s, assuming there is no interruption in production.", opad, h,
					Misc.getDGSCredits(monthlyPayment),
					"" + (int)CONTRACT_CYCLES,
					Misc.getDGSCredits(totalPayment));
			info.addPara("Months remaining: %s", opad, h, "" + monthsRemaining);
			if (isPlayerProducing(commodityId, needed)) {
				info.addPara("You are currently meeting the terms of the contract.", 
							 Misc.getPositiveHighlightColor(), opad);
			} else {
				info.addPara("You are not currently meeting the terms of the contract.", 
							 Misc.getNegativeHighlightColor(), opad);
			}
		} else if (currentStage == Stage.COMPLETED) {
			info.addPara("The contract is completed.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.WAITING) {
			info.addPara("Produce at least %s units of " + getSpec().getLowerCaseName() + " at " +
						 "a colony", pad, tc, h, "" + needed);
			return true;
		} else if (currentStage == Stage.PAYING) {
			info.addPara("Receiving %s per month", pad, tc, h, Misc.getDGSCredits(monthlyPayment));
			info.addPara("Months remaining: %s", 0f, tc, h, "" + monthsRemaining);
			if (isPlayerProducing(commodityId, needed)) {
				info.addPara("Terms of contract met", tc, 0f);
			} else {
				info.addPara("Terms of contract not met", 
							 Misc.getNegativeHighlightColor(), 0f);
			}
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return getSpec().getName() + " Production";
	}
	

	public static boolean playerHasAColony() {
		return !Misc.getPlayerMarkets(true).isEmpty();
	}
	public static boolean isPlayerProducing(String commodityId, int quantity) {
		for (MarketAPI market : Misc.getPlayerMarkets(true)) {
			CommodityOnMarketAPI com = market.getCommodityData(commodityId);
			if (com.getMaxSupply() >= quantity) return true;
		}
		return false;
	}

	protected transient CommoditySpecAPI spec;
	protected CommoditySpecAPI getSpec() {
		if (spec == null) {
			spec = Global.getSettings().getCommoditySpec(commodityId);
		}
		return spec;
	}
	
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		Global.getSector().getListenerManager().removeListener(this);
	}

	@Override
	public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		super.acceptImpl(dialog, memoryMap);
		
		Global.getSector().getListenerManager().addListener(this);
		uid = Misc.genUID();
		connectWithGlobalFlag(Stage.PAYING, Stage.COMPLETED, getCompletionFlag());
	}

	public String getCompletionFlag() {
		return "$" + getMissionId() + "_" + commodityId + "_" + uid + "_completed";
	}
	
	
	public void reportEconomyTick(int iterIndex) {
		if (currentStage != Stage.PAYING) return;
		
		int numIter = (int) Global.getSettings().getFloat("economyIterPerMonth");
		
		MonthlyReport report = SharedData.getData().getCurrentReport();
		FDNode colonyNode = report.getNode(MonthlyReport.OUTPOSTS);
		FDNode paymentNode = report.getNode(colonyNode, getMissionId() + "_" + commodityId + "_" + uid);
		paymentNode.income += monthlyPayment / numIter;
		paymentNode.name = getBaseName();
		//paymentNode.icon = Global.getSettings().getSpriteName("income_report", "generic_income");
		paymentNode.icon = getSpec().getIconName();
		paymentNode.tooltipCreator = this;
	}
	

	public void reportEconomyMonthEnd() {
		monthsRemaining--;
		//monthsRemaining = 0;
		if (monthsRemaining <= 0) {
			Global.getSector().getMemoryWithoutUpdate().set(getCompletionFlag(), true);
		}
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
		tooltip.addSpacer(-10f);
		addDescriptionForNonEndStage(tooltip, getTooltipWidth(tooltipParam), 1000f);
	}

	public float getTooltipWidth(Object tooltipParam) {
		return 450;
	}

	public boolean isTooltipExpandable(Object tooltipParam) {
		return false;
	}
	
	protected String getMissionTypeNoun() {
		return "contract";
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		if (currentStage == Stage.PAYING) {
			tags.add(Tags.INTEL_AGREEMENTS);
			tags.remove(Tags.INTEL_ACCEPTED);
		}
		return tags;
	}
	
	
}

