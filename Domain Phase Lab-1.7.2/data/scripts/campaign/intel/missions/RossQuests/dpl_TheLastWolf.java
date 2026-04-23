package data.scripts.campaign.intel.missions.RossQuests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.dpl_campaign_dialog_script;
import data.scripts.campaign.dpl_sat_bomb_script;
import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_TheLastWolf extends HubMissionWithBarEvent implements FleetEventListener {
    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        RETURN_TO_FACTORY,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected PersonAPI attackers_leader;
    protected MarketAPI ganir;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    
    protected static MarketAPI market = Global.getSector().getEconomy().getMarket("dpl_factory");

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

    	if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        PersonAPI person = getPerson();
        if (person == null) return false;
        
        ganir = Global.getSector().getEconomy().getMarket("corvus_IIIa");
        if (ganir == null) return false;
        
        system2 = ganir.getStarSystem();
        if (system2 == null) return false;
        
        system = market.getStarSystem();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_tlwf_ref")) {
            return false;
        }

        // set up the leader
        attackers_leader = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
        attackers_leader.setRankId(Ranks.SPECIAL_AGENT);
        attackers_leader.setPostId(Ranks.POST_SPECIAL_AGENT);
        attackers_leader.getMemoryWithoutUpdate().set("$dpl_tlwf_leader", true);
        
        attackers_leader.getStats().setLevel(8);
        attackers_leader.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        attackers_leader.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        attackers_leader.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        attackers_leader.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        attackers_leader.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        attackers_leader.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
        attackers_leader.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
        attackers_leader.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
        attackers_leader.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_attackers_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.RETURN_TO_FACTORY);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_fleets();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				motivate_cadets();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.FAILED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_fleets();
				transfer_factory();
			}
		});
        endTrigger();

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_tlwf_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
		setStageOnGlobalFlag(Stage.RETURN_TO_FACTORY, "$dpl_tlwf_killed");
        makeImportant(market, "$dpl_tlwf", Stage.RETURN_TO_FACTORY);
		makeImportant(person, "$dpl_tlwf", Stage.RETURN_TO_FACTORY);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_tlwf_completed");
        
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_FACTORY);
        setCreditReward(500000);

        return true;
    }

    // set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
    // and we need to know when this fleet dies or despawns. I also need to write it outside of create function, so that this fleet only gets
    // created after the mission is accepted, instead of when the mission is created.
    public void spawn_attackers_fleet() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.INDEPENDENT,
                null,
                PATROL_LARGE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                3f // qualityMod
        );

    	List<String> AddShips = new ArrayList<>();
    	for (int i=0; i<10; i++) {
    		AddShips.add("dpl_super_hyperion_standard");
    	}
    	AddShips.add("dpl_super_hyperion_flagship");
    	AddShips.add("dpl_super_hyperion_flagship");
    	AddShips.add("dpl_valkyrie_II_boss");
    	AddShips.add("dpl_valkyrie_II_boss");
    	AddShips.add("dpl_valkyrie_II_boss_standard");
    	AddShips.add("dpl_valkyrie_II_boss_standard");
    	params.addShips = AddShips;
    	
    	params.averageSMods = 3;
    	target = FleetFactoryV3.createFleet(params);
        target.setName("The Darktides");
        target.setNoFactionInName(true);

        target.setCommander(attackers_leader);
        target.getFlagship().setCaptain(attackers_leader);
		target.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		
        List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			if (!curr.isFlagship()) {
				PersonAPI person = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
				person.setPersonality(Personalities.RECKLESS);
				person.getStats().setLevel(6);
				person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
				person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
				person.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 2);
				person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
				person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
				person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
				person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
				person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
				person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
				person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		        
				curr.setVariant(curr.getVariant().clone(), false, false);
				curr.getVariant().setSource(VariantSource.REFIT);
				curr.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
				curr.setCaptain(person);
			}
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_tlwf");
        
        CargoAPI cargo1 = Global.getFactory().createCargo(true);
		cargo1.addSpecial(new SpecialItemData("dpl_data_archive", null), 6);
		cargo1.addWeapons("dpl_dualres_mrm", 4);
		cargo1.addWeapons("dpl_am_mrm", 12);
		cargo1.addWeapons("cryoflux", 4);
		cargo1.addWeapons("cryoblaster", 2);
		cargo1.addCommodity("marines", 300);
		BaseSalvageSpecial.addExtraSalvage(target, cargo1);

		target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        //Must be set to true, or some improper tithe check will ruin the story.
        target.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target.getMemoryWithoutUpdate().set("$dpl_tlwf_attackersfleet", true);
        target.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, ganir.getPrimaryEntity(), 200f, null);
        target.addEventListener(this);
        system2.addEntity(target);
    }
    
    protected void despawn_fleets() {
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }
    
    protected void motivate_cadets() {
    	if (market != null) {
    		market.removeCondition("dpl_unmotivated_cadets");
    		market.addCondition("dpl_motivated_cadets");
        }
    }
    
    public static void transfer_factory() {
    	MarketAPI theMarket = market;
		FactionAPI pirates = Global.getSector().getFaction("pirates");
		Set<SectorEntityToken> linkedEntities = theMarket.getConnectedEntities();
        for (SectorEntityToken entity : linkedEntities) {
        	entity.setFaction("pirates");
        }
        //Change people to the new faction
        final List<String> POSTS_TO_CHANGE_ON_CAPTURE = Arrays.asList(new String[]{
        		Ranks.POST_BASE_COMMANDER,
                Ranks.POST_OUTPOST_COMMANDER,
                Ranks.POST_STATION_COMMANDER,
                Ranks.POST_PORTMASTER,
                Ranks.POST_SUPPLY_OFFICER,
                Ranks.POST_ADMINISTRATOR
        });
        List<PersonAPI> people = theMarket.getPeopleCopy();
        for (PersonAPI person : people) {
        	if (POSTS_TO_CHANGE_ON_CAPTURE.contains(person.getPostId())) person.setFaction("pirates");
        }
        
        PersonAPI admin = theMarket.getAdmin();
        if (admin != null && admin.equals(Global.getSector().getPlayerPerson())) {
        	PersonAPI newAdmin = Global.getFactory().createPerson();
        	newAdmin.setFaction("pirates");
        	theMarket.setAdmin(newAdmin);
        }
        
        //set market to the new owner
        theMarket.setFactionId("pirates");
        theMarket.setPlayerOwned(false);
        
        theMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
        theMarket.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo();    // force cargo to generate if needed; fixes military submarket crash
        
        theMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
        theMarket.getSubmarket(Submarkets.SUBMARKET_BLACK).getCargo();    // force cargo to generate if needed; fixes military submarket crash
        
        //set submarkets to the new owner
        List<SubmarketAPI> submarkets = theMarket.getSubmarketsCopy();         
        for (SubmarketAPI submarket : submarkets)
        {
        	String submarketId = submarket.getSpecId();             
        	if (submarket.getPlugin().isFreeTransfer()) continue;
            if (!submarket.getPlugin().isParticipatesInEconomy()) continue;
            // reset smuggling suspicion
            if (submarketId.equals(Submarkets.SUBMARKET_BLACK)) {  
            	PlayerTradeDataForSubmarket tradeData = SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket);  
            	tradeData.setTotalPlayerTradeValue(0);
            	continue;
            }  
            submarket.setFaction(pirates);
         }
        // transfer defense station
        if (Misc.getStationFleet(theMarket) != null)
        {
            Misc.getStationFleet(theMarket).setFaction("pirates", true);
        }
        if (Misc.getStationBaseFleet(theMarket) != null)
        {
            Misc.getStationBaseFleet(theMarket).setFaction("pirates", true);
        }

        // don't lock player out of freshly captured market
        if (!pirates.isHostileTo(Factions.PLAYER)) {
        	theMarket.getMemoryWithoutUpdate().unset("$playerHostileTimeout");
        }
        
     // player: has to pay for storage unlock
     SubmarketAPI storage = theMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE);
     if (storage != null)
     {
         StoragePlugin plugin = (StoragePlugin)theMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin();
         if (plugin != null)
             plugin.setPlayerPaidToUnlock(false);
         }
     //Make sure conditions are good
     theMarket.reapplyConditions();
}
    
    protected void updateInteractionDataImpl() {
        set("$dpl_tlwf_barEvent", isBarEvent());
        set("$dpl_tlwf_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_tlwf_heOrShe", getPerson().getHeOrShe());
        set("$dpl_tlwf_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_tlwf_personName", getPerson().getNameString());
        set("$dpl_tlwf_patherName", attackers_leader.getNameString());
        set("$dpl_tlwf_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_tlwf_dist", getDistanceLY(system2));
    }

    // used to detect when the pather's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;
    }
    
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
		if (fleet.equals(target)) {
			Global.getSector().getMemoryWithoutUpdate().set("$dpl_tlwf_killed", true);
		}
	}

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Eliminate the 'Darktides' special force in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            info.addPara("Return to Lab Factory.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: THE ATTACKERS ARE LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Eliminate the 'Darktides' special force in the " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            info.addPara("Return to Lab Factory.", tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "The Last Wolf";
    }
}
