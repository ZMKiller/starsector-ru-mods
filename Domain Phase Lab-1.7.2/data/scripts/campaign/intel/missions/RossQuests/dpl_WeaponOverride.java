package data.scripts.campaign.intel.missions.RossQuests;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import data.scripts.campaign.intel.missions.dpl_Artworks.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

public class dpl_WeaponOverride extends HubMissionWithBarEvent {
	
	//public static float PROB_PATHER_OR_PIRATE = 0.5f;
//	public static int RAID_DIFFICULTY = 100;
//	public static int MARINES_REQUIRED = RAID_DIFFICULTY / 2;
	
	
	public static enum Stage {
		GO_TO_OUTPOST,
		RETURN_TO_FACTORY,
		COMPLETED,
		FAILED,
	}
	
	protected PlanetAPI planet;
	protected MarketAPI factory;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$dpl_rcwo_ref")) {
			return false;
		}
		
		PersonAPI person = getPerson();
        if (person == null) return false;
        factory = person.getMarket();
        if (factory == null) return false;
        if (!factory.getFactionId().equals("dpl_phase_lab")) return false;
		
		resetSearch();
		preferSystemInteresting();
		preferSystemOnFringeOfSector();
		preferSystemUnexplored();
		requirePlanetNotStar();
		requirePlanetUnpopulated();
		requirePlanetNotGasGiant();
		preferPlanetNotFullySurveyed();
		preferPlanetInDirectionOfOtherMissions();

		planet = pickPlanet();
		
		if (planet == null) {
			return false;
		}
		
		beginStageTrigger(Stage.GO_TO_OUTPOST);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				if (!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$dpl_rcwo_spawnedStation")) {
					spawn_derelicts();
					Global.getSector().getPlayerMemoryWithoutUpdate().set("$dpl_rcwo_spawnedStation", true);
				}
			}
		});
        endTrigger();
		
		setStartingStage(Stage.GO_TO_OUTPOST);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		makeImportant(getPerson(), "$dpl_rcwo_returnHere", Stage.RETURN_TO_FACTORY);
		
		setStageOnGlobalFlag(Stage.RETURN_TO_FACTORY, "$dpl_rcwo_gotWeaponOverride");
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_rcwo_completed");
		setStoryMission();
        setCreditReward(300000);		
		
		return true;
	}

	protected void updateInteractionDataImpl() {
		set("$dpl_rcwo_planetId", planet.getId());
		set("$dpl_rcwo_planetName", planet.getName());
		set("$dpl_rcwo_systemName", planet.getStarSystem().getNameWithNoType());
		set("$dpl_rcwo_dist", getDistanceLY(planet));
		set("$dpl_rcwo_reward", Misc.getWithDGS(getCreditsReward()));
	}
	
	public void spawn_derelicts(){
		CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec("dpl_station_research");
        float orbitRadius = planet.getRadius() + spec.getDefaultRadius() + 100f;
        float orbitDays = orbitRadius / 20f;
        SectorEntityToken entity;
        entity = BaseThemeGenerator.addSalvageEntity(planet.getStarSystem(), "dpl_station_research", Factions.NEUTRAL);
        entity.setCircularOrbitPointingDown(planet, 270f, orbitRadius, orbitDays);
        CampaignFleetAPI defenders = CreateDefenders();
        entity.getMemory().set("$defenderFleet", defenders);
        entity.getMemory().set("$hasDefenders", true);
        
        Misc.makeImportant(entity, "$dpl_rcwo_targetStation");
        entity.getMemoryWithoutUpdate().set("$dpl_rcwo_targetStation", true);
        setEntityMissionRef(entity, "$dpl_rcwo_ref");
    }
	
	public CampaignFleetAPI CreateDefenders() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.LUDDIC_PATH,
                null,
                PATROL_LARGE,
                300f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                3f // qualityMod
        );
    	
    	CampaignFleetAPI target;
    	target = FleetFactoryV3.createFleet(params);
        target.setName("Pirate Defense");
        target.setNoFactionInName(true);

        Misc.makeHostile(target);
        return target;
    }
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextPre(planet) +
					", and recover data from a fallen research station located there.", opad);
		} else if (currentStage == Stage.RETURN_TO_FACTORY) {
			info.addPara("Return to Lab Factory and talk to " + 
						 getPerson().getNameString() + " to receive your reward.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextShort(planet), tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_FACTORY) {
			info.addPara("Return to the Lab Factory and talk to " + getPerson().getNameString(), tc, pad);
			return true;
		}
		return false;
	}
	
	@Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.GO_TO_OUTPOST) {
            return getMapLocationFor(planet.getStarSystem().getCenter());
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            return getMapLocationFor(factory.getStarSystem().getCenter());
        }
        return null;
    }

	@Override
	public String getBaseName() {
		return "Recover Technology";
	}

}





