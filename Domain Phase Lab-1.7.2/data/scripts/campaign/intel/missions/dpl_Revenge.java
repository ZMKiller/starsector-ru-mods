package data.scripts.campaign.intel.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Justice.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_Revenge extends HubMissionWithBarEvent {

    // mission stages
    public static enum Stage {
        FIND_SHIP,
        RETURN_TO_FACTORY,
        COMPLETED,
    }

    // important objects, systems and people
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected SectorEntityToken pioneer;
    protected boolean met_Ross;
    protected int daysSinceStart;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

    	MarketAPI Factory = Global.getSector().getEconomy().getMarket("dpl_factory");
    	if (Factory == null) return false;
    	PlanetAPI FactoryPlanet = Factory.getPlanetEntity();
    	if (FactoryPlanet == null) return false;
    	met_Ross = FactoryPlanet.getMemoryWithoutUpdate().getBoolean("$dpl_metRoss");
    	if (met_Ross) {
    		return false;
    	}
    	
    	// if bar event, let's create a person to actually give it to us
        if (barEvent) {
            setGiverRank(Ranks.PILOT);
            setGiverPost(Ranks.POST_SPACER);
            setGiverFaction(Factions.INDEPENDENT);
            setGiverVoice(Voices.SPACER);
            findOrCreateGiver(createdAt, false, false);
        }
    	
        PersonAPI person = getPerson();
        if (person == null) return false;
        
        MarketAPI giverMarket = person.getMarket();
        if (giverMarket == null) return false;
        if (!(giverMarket.getSize() >= 4)) return false;
        
        daysSinceStart = Global.getSector().getMemoryWithoutUpdate().getInt("$daysSinceStart");
        if (!(daysSinceStart >= 90)) return false;
		
        MarketAPI market = Global.getSector().getEconomy().getMarket("dpl_factory");
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;

        system = Global.getSector().getStarSystem("capella");
        if (system == null) return false;
        
        system2 = market.getStarSystem();
        if (system2 == null) return false;
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_Revenge_ref")) {
            return false;
        }
        
        beginStageTrigger(Stage.FIND_SHIP);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				if (!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$dpl_revengeSpawnedDerelicts")) {
					spawn_derelicts();
					Global.getSector().getPlayerMemoryWithoutUpdate().set("$dpl_revengeSpawnedDerelicts", true);
				}
			}
		});
        endTrigger();

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_Revenge_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.FIND_SHIP);
        setSuccessStage(Stage.COMPLETED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.RETURN_TO_FACTORY, "$dpl_Revenge_founddata");
        makeImportant(market, "$dpl_Revenge", Stage.RETURN_TO_FACTORY);
        setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_Revenge_completed");
        // set time limit and credit reward
        setStoryMission();
        setRepFactionChangesNone();
		setRepPersonChangesNone();

        return true;
    }
    
    protected void updateInteractionDataImpl() {
        set("$dpl_Revenge_barEvent", isBarEvent());
        set("$dpl_Revenge_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_Revenge_heOrShe", getPerson().getHeOrShe());
        set("$dpl_Revenge_reward", Misc.getWithDGS(getCreditsReward()));
        set("$dpl_Revenge_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_Revenge_dist", getDistanceLY(system2));
    }

    public void spawn_derelicts(){
    	// spawn a recoverable derelict ship, serving as clues. They have memory flags that are checked for in rules.csv
        // I need to add mission tag to the clarinet ship, and so I have to spawn this ship in the tedious way
    	pioneer = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, new DerelictShipData(new PerShipData("dpl_innovator_mission", ShipCondition.WRECKED), false));
    	pioneer.setDiscoverable(true);
    	pioneer.setCircularOrbit(system.getStar(), (float) Math.random() * 360f, 3000f, 3000f / (10f + (float) Math.random() * 5f));
        Misc.setSalvageSpecial(pioneer, new ShipRecoverySpecialCreator(null, 0, 0, false, null, null).createSpecial(pioneer, null));
        Misc.makeImportant(pioneer, "$dpl_Revenge");
        pioneer.getMemoryWithoutUpdate().set("$dpl_Revenge_pioneer", true);
        setEntityMissionRef(pioneer, "$dpl_Revenge_ref");
        
        //other ships can be spawned fine
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "dpl_innovator_mission", ShipCondition.WRECKED, 3050f, true, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "venture_Exploration", ShipCondition.WRECKED, 3050f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "phaeton_Standard", ShipCondition.WRECKED, 3030f, true, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "phaeton_Standard", ShipCondition.WRECKED, 3020f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "sunder_Assault", ShipCondition.WRECKED, 3035f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "sunder_Assault", ShipCondition.WRECKED, 3035f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "sunder_Assault", ShipCondition.WRECKED, 3035f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "tarsus_Standard", ShipCondition.WRECKED, 3045f, false, false, null);
    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.FIND_SHIP) {
            info.addPara("Check the situation in the system " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            info.addPara("Go to Lab Factory to return the dara storage, it's in the system " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: THE DERELICT IS LOCATED IN THE " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.FIND_SHIP) {
            info.addPara("Check the situation in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            info.addPara("Go to Lab Factory in " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.FIND_SHIP) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            return getMapLocationFor(system2.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Revenge";
    }
}
