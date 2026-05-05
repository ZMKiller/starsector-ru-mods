package data.campaign;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSISpySatMission extends HubMissionWithSearch{
    
	public static float PROB_PATROL_AROUND_TARGET = 0.5f;
	
	public static float MISSION_DAYS = 120f;
	
	public static enum Stage {
		DEPLOY,
		COMPLETED,
		FAILED,
	}
	
	protected MarketAPI market;
	protected SectorEntityToken target;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		//genRandom = Misc.random;
		
		//if (Factions.PIRATES.equals(createdAt.getFaction().getId())) return false;
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		
		
		if (!setPersonMissionRef(person, "$HSI_SpySat_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		requireMarketIsNot(createdAt);
		requireMarketLocationNot(createdAt.getContainingLocation());
		requireMarketFactionNotPlayer();
		requireMarketFactionNot(person.getFaction().getId());
		requireMarketFactionCustom(ReqMode.NOT_ANY, Factions.TRITACHYON);
		requireMarketMilitary();
		requireMarketNotHidden();
		requireMarketNotInHyperspace();
		preferMarketInDirectionOfOtherMissions();
		market = pickMarket();
		
		if (market == null) return false;
		
		target = spawnMissionNode(
					new LocData(EntityLocationType.ORBITING_PARAM, market.getPrimaryEntity(), market.getStarSystem()));
		if (!setEntityMissionRef(target, "$HSI_SpySat_ref")) return false;
		
		makeImportant(target, "$HSI_SpySat_target", Stage.DEPLOY);
		setMapMarkerNameColor(market.getTextColorForFactionOrPlanet());
		
		setStartingStage(Stage.DEPLOY);
		setSuccessStage(Stage.COMPLETED);
		setFailureStage(Stage.FAILED);
		
		setStageOnMemoryFlag(Stage.COMPLETED, target, "$HSI_SpySat_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
		
//		int sizeModifier = market.getSize() * 10000;
//		setCreditReward(10000 + sizeModifier, 30000 + sizeModifier);
		setCreditReward(CreditReward.AVERAGE, market.getSize());
		
		if (rollProbability(PROB_PATROL_AROUND_TARGET)) {
			triggerCreateMediumPatrolAroundMarket(market, Stage.DEPLOY, 1f);
		}
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		set("$HSI_SpySat_barEvent", isBarEvent());
		set("$HSI_SpySat_underworld", getPerson().hasTag(Tags.CONTACT_UNDERWORLD));
		set("$HSI_SpySat_manOrWoman", getPerson().getManOrWoman());
		set("$HSI_SpySat_reward", Misc.getWithDGS(getCreditsReward()));
		
		set("$HSI_SpySat_personName", getPerson().getNameString());
		set("$HSI_SpySat_systemName", market.getStarSystem().getNameWithLowercaseTypeShort());
		set("$HSI_SpySat_marketName", market.getName());
		set("$HSI_SpySat_marketOnOrAt", market.getOnOrAt());
		set("$HSI_SpySat_dist", getDistanceLY(market));
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DEPLOY) {
			info.addPara(HSII18nUtil.getEconString("HSI_SpySat_0") +
					market.getName() + HSII18nUtil.getEconString("HSI_SpySat_1") + 
					market.getStarSystem().getNameWithLowercaseTypeShort()+ HSII18nUtil.getEconString("HSI_SpySat_2"), opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.DEPLOY) {
			info.addPara(HSII18nUtil.getEconString("HSI_SpySat_0") +
					market.getName() + HSII18nUtil.getEconString("HSI_SpySat_1") + 
					market.getStarSystem().getNameWithLowercaseTypeShort()+ HSII18nUtil.getEconString("HSI_SpySat_2"), tc, pad);
			return true;
		}
		return false;
	}	
	
	@Override
	public String getBaseName() {
		return HSII18nUtil.getEconString("HSI_SpySat_NAME");
	}
	
}
