package data.scripts.campaign;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.SmugglingMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class cft_SmugglingMission extends SmugglingMission {
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		if (!("CFT".equals(createdAt.getFaction().getId()))) return false;
		
		if (barEvent) {
			setGiverRank(Ranks.CITIZEN);
			setGiverPost(pickOne(Ranks.POST_TRADER, Ranks.POST_COMMODITIES_AGENT, 
							 		 Ranks.POST_MERCHANT, Ranks.POST_INVESTOR, Ranks.POST_PORTMASTER));
			setGiverImportance(pickImportance());
			setGiverTags(pickOne(Tags.CONTACT_TRADE, Tags.CONTACT_MILITARY));
			findOrCreateGiver(createdAt, false, false);
		}
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		
		if (!setPersonMissionRef(person, "$cft_smug_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		requireMarketIsNot(createdAt);
		requireMarketFactionNotPlayer();
		requireMarketLocationNot(createdAt.getContainingLocation());
		requireMarketFactionCustom(ReqMode.NOT_ANY, Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE);
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		preferMarketInDirectionOfOtherMissions();
		
		requireCommodityIllegal();
		requireCommodityDemandAtLeast(1);
		
		com = pickCommodity();
		if (com == null) return false;
		
		market = com.getMarket();
		if (market == null) return false;
		
		
		float value = MIN_VALUE + getQuality() * (MAX_VALUE - MIN_VALUE);
		value *= 0.9f + genRandom.nextFloat() * 0.2f;
		
		quantity = getRoundNumber(value / com.getCommodity().getBasePrice());
		if (quantity < 10) quantity = 10;
		
		if (!setMarketMissionRef(market, "$cft_smug_ref")) {
			return false;
		}
		makeImportant(market, "$cft_smug_target", Stage.SMUGGLE);
		
		setStartingStage(Stage.SMUGGLE);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, market, "$cft_smug_completed");
		setNoAbandon();
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
		
		//setCreditReward((int)(value * 0.5f), (int)(value * 0.7f));
		setCreditRewardWithBonus(CreditReward.LOW, (int) (value * 0.5f));
		
		triggerCreateMediumPatrolAroundMarket(market, Stage.SMUGGLE, 1f);
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$cft_smug_barEvent", isBarEvent());
		set("$cft_smug_manOrWoman", getPerson().getManOrWoman());
		set("$cft_smug_reward", Misc.getWithDGS(getCreditsReward()));
		set("$cft_smug_commodityId", com.getId());
		set("$cft_smug_commodityName", com.getCommodity().getLowerCaseName());
		set("$cft_smug_quantity", Misc.getWithDGS(quantity));
		set("$cft_smug_playerHasEnough", playerHasEnough(com.getId(), quantity));
		
		set("$cft_smug_personName", getPerson().getNameString());
		set("$cft_smug_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$cft_smug_marketName", market.getName());
		set("$cft_smug_marketOnOrAt", market.getOnOrAt());
		set("$cft_smug_dist", getDistanceLY(market));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.SMUGGLE) {
			info.addPara("Smuggle %s units of " + com.getCommodity().getLowerCaseName() + " to " + market.getName() + 
					" in the " + market.getStarSystem().getNameWithLowercaseTypeShort() + ".", opad,
					h, Misc.getWithDGS(quantity));
			
			info.addPara("The authorities are aware the shipment is incoming and patrols are on high alert.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.SMUGGLE) {
			info.addPara("Smuggle %s units of " + com.getCommodity().getLowerCaseName() + " to " +
					market.getName() + " in the " + 
					market.getStarSystem().getNameWithLowercaseTypeShort(), pad, tc,
					h, Misc.getWithDGS(quantity));
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return "Smuggling " + com.getCommodity().getName();
	}
	
}