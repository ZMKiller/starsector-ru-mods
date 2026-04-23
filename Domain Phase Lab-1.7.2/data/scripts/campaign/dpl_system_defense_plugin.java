//By VladimirVV. Spawns lore-friendly fleets, and gives some lore-friendly buffs to the system defense.
package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.INVESTIGATORS;
import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

public class dpl_system_defense_plugin implements EveryFrameScript, FleetEventListener {

    //The ID for the faction that gets their relations adjusted (sorry, couldn't remember your faction ID off the top of my head)
    private static final String MAIN_FACTION = "dpl_phase_lab";
    private static int MAX_FLEET_ENEMY = 5;
    private boolean played_phase_resonance = false;
    private boolean phase_resonance_active = false;
    private boolean sent_marinesSecurity = false;
    private boolean sent_marinesFactory = false;
    private boolean sent_marinesRSV = false;
    private boolean sent_marinesMoon = false;
    private StarSystemAPI muspelheim = null;
    private PlanetAPI Security = null;
    private PlanetAPI Factory = null;
    private PlanetAPI RSV = null;
    private PlanetAPI Moon = null;
    private MarketAPI largestMkt = null;
    protected Set<CampaignFleetAPI> enemyFleets = new HashSet<>();

    @Override
    public void advance(float amount) {
        //Necessary Sector check
        SectorAPI sector = Global.getSector();
        if (sector == null) {
            return;
        }
        
        largestMkt = FindLargestMarket();
        if (largestMkt == null) {
        	return;
        }
        
        muspelheim = FindLargestMarket().getStarSystem();
        if (muspelheim == null) {
        	return;
        }
        
        //This situation should never happen. But if it happens, we don't want this code to break the game.
        if (sector.getFaction(MAIN_FACTION) == null) {
            return;
        }
        
        Security = (PlanetAPI) muspelheim.getEntityById("dpl_security");
        Factory = (PlanetAPI) muspelheim.getEntityById("dpl_factory");
        RSV = (PlanetAPI) muspelheim.getEntityById("dpl_research_site_v");
        Moon = (PlanetAPI) muspelheim.getEntityById("dpl_research_site_v_moon");
        
        List<CampaignFleetAPI> allFleets = muspelheim.getFleets();
		for (CampaignFleetAPI fleets : allFleets) {
			if (fleets.getFaction() != null) {
				if (fleets.getFaction().isHostileTo(MAIN_FACTION)) {
					if (!(fleets.hasTag("dpl_marked_as_enemy"))) {
						fleets.addEventListener(this);
						fleets.addTag("dpl_marked_as_enemy");
						enemyFleets.add(fleets);
					}
				}
			}
		}
		
		//Visual Effects, for performance concerns, only do this if player is in system.
		if (Global.getSector().getPlayerFleet().isInOrNearSystem(muspelheim)) {
			List<SectorEntityToken> ResonanceArrays = muspelheim.getEntitiesWithTag("dpl_resonance_array");
			if (enemyFleets.size() >= MAX_FLEET_ENEMY && !ResonanceArrays.isEmpty()) {
				played_phase_resonance = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_played_phase_resonance");
				phase_resonance_active = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_phase_resonance_active");
				if (!played_phase_resonance || phase_resonance_active) {
					for (CampaignFleetAPI fleet : enemyFleets) {
						boolean dpl_resonated = fleet.getMemoryWithoutUpdate().getBoolean("$dpl_resonated");
						if (!dpl_resonated) {
							fleet.addScript(new dpl_relay_explosion_plugin(fleet));
							fleet.getMemoryWithoutUpdate().set("$dpl_resonated", true, 15);
							for (SectorEntityToken RA : ResonanceArrays) {
								if (!(RA.hasScriptOfClass(dpl_resonance_array_plugin.class))) {
									RA.addScript(new dpl_resonance_array_plugin(RA));
								}
							}
						}
					}
					Global.getSector().getMemoryWithoutUpdate().set("$dpl_phase_resonance_active", true, 5);
					Global.getSector().getMemoryWithoutUpdate().set("$dpl_played_phase_resonance", true, 15);
				}
			}
		}
		
		if (Security != null) {
			if (!Security.getFaction().equals(Global.getSector().getFaction("dpl_phase_lab"))) {
				sent_marinesSecurity = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_sent_marinesSecurity");
				if (!sent_marinesSecurity) {
					boolean successSecurity = spawnMarinesSecurity();
					if (successSecurity) {
		            	Global.getSector().getMemoryWithoutUpdate().set("$dpl_sent_marinesSecurity", true, 30);
		        	}
				}
			}
		}
		
		if (Factory != null) {
			if (!Factory.getFaction().equals(Global.getSector().getFaction("dpl_phase_lab"))) {
				sent_marinesFactory = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_sent_marinesFactory");
				if (!sent_marinesFactory) {
					boolean successFactory = spawnMarinesFactory();
					if (successFactory) {
		            	Global.getSector().getMemoryWithoutUpdate().set("$dpl_sent_marinesFactory", true, 30);
		        	}
				}
			}
		}
		
		if (RSV != null) {
			if (!RSV.getFaction().equals(Global.getSector().getFaction("dpl_phase_lab"))) {
				sent_marinesRSV = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_sent_marinesRSV");
				if (!sent_marinesRSV) {
					boolean successRSV = spawnMarinesRSV();
					if (successRSV) {
		            	Global.getSector().getMemoryWithoutUpdate().set("$dpl_sent_marinesRSV", true, 30);
		        	}
				}
			}
		}
		
		if (Moon != null) {
			if (!Moon.getFaction().equals(Global.getSector().getFaction("dpl_phase_lab"))) {
				sent_marinesMoon = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_sent_marinesMoon");
				if (!sent_marinesMoon) {
					boolean successMoon = spawnMarinesMoon();
					if (successMoon) {
		            	Global.getSector().getMemoryWithoutUpdate().set("$dpl_sent_marinesMoon", true, 30);
		        	}
				}
			}
		}
        
    }
    
    protected MarketAPI FindLargestMarket() {
	    MarketAPI largestMarket = null;
		int size = 0;
		List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
		for (MarketAPI market : allMarkets) {
			if (market.getFaction().equals(Global.getSector().getFaction("dpl_phase_lab"))) {
				if (market.getSize() >= size) {
					largestMarket = market;
					size = market.getSize();
				}
			}
		}
		return largestMarket;
    }
    
    public void checkCaptureFactory() {
    	MarketAPI FactoryMarket = Factory.getMarket();
    	if (FactoryMarket != null) {
    		if (FactoryMarket.getSize()>0) {
        		transferColony(FactoryMarket);
        	} else {
        		createColony(FactoryMarket,Factory);
        	}
    	}
	}
    
    public void checkCaptureSecurity() {
    	MarketAPI SecurityMarket = Security.getMarket();
    	if (SecurityMarket != null) {
    		if (SecurityMarket.getSize()>0) {
        		transferColony(SecurityMarket);
        	} else {
        		createColony(SecurityMarket,Security);
        	}
    	}
	}
    
    public void checkCaptureRSV() {
    	MarketAPI RSVMarket = RSV.getMarket();
    	if (RSVMarket != null) {
    		if (RSVMarket.getSize()>0) {
        		transferColony(RSVMarket);
        	} else {
        		createColony(RSVMarket,RSV);
        	}
    	}
	}
    
    public void checkCaptureMoon() {
    	MarketAPI MoonMarket = Moon.getMarket();
    	if (MoonMarket != null) {
    		if (MoonMarket.getSize()>0) {
        		transferColony(MoonMarket);
        	} else {
        		createColony(MoonMarket,Moon);
        	}
    	}
	}
    
    public static void transferColony(MarketAPI theMarket) {
			FactionAPI dpl_phase_lab = Global.getSector().getFaction("dpl_phase_lab");
			Set<SectorEntityToken> linkedEntities = theMarket.getConnectedEntities();
	        for (SectorEntityToken entity : linkedEntities) {
	        	entity.setFaction("dpl_phase_lab");
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
	        	if (POSTS_TO_CHANGE_ON_CAPTURE.contains(person.getPostId())) person.setFaction("dpl_phase_lab");
	        }
	        
	        PersonAPI admin = theMarket.getAdmin();
	        if (admin != null && admin.equals(Global.getSector().getPlayerPerson())) {
	        	PersonAPI newAdmin = Global.getFactory().createPerson();
	        	newAdmin.setFaction("dpl_phase_lab");
	        	theMarket.setAdmin(newAdmin);
	        }
	        
	        //set market to the new owner
	        theMarket.setFactionId("dpl_phase_lab");
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
	            submarket.setFaction(dpl_phase_lab);
	         }
	        // transfer defense station
	        if (Misc.getStationFleet(theMarket) != null)
	        {
	            Misc.getStationFleet(theMarket).setFaction("dpl_phase_lab", true);
	        }
	        if (Misc.getStationBaseFleet(theMarket) != null)
	        {
	            Misc.getStationBaseFleet(theMarket).setFaction("dpl_phase_lab", true);
	        }

	        // don't lock player out of freshly captured market
	        if (!dpl_phase_lab.isHostileTo(Factions.PLAYER)) {
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
    
    public static void createColony(MarketAPI market, PlanetAPI planet) {
		String factionId = "dpl_phase_lab";
		
		market.setSize(3);
		market.addCondition("population_3");
		market.setFactionId(factionId);
		market.setPlanetConditionMarketOnly(false);
		
		if (market.hasCondition(Conditions.DECIVILIZED))
		{
			market.removeCondition(Conditions.DECIVILIZED);
			market.addCondition(Conditions.DECIVILIZED_SUBPOP);
		}
		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.SPACEPORT);
		
		market.setIncoming(new PopulationComposition());
		
		market.getTariff().modifyFlat("generator", Global.getSector().getFaction(factionId).getTariffFraction());
					
		// submarkets
		if (!market.hasSubmarket(Submarkets.SUBMARKET_OPEN))
        {
            market.addSubmarket(Submarkets.SUBMARKET_OPEN);
            market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo();    // force cargo to generate if needed; fixes military submarket crash
        }
		
		if (!market.hasSubmarket(Submarkets.SUBMARKET_BLACK))
        {
            market.addSubmarket(Submarkets.SUBMARKET_BLACK);
            market.getSubmarket(Submarkets.SUBMARKET_BLACK).getCargo();    // force cargo to generate if needed; fixes military submarket crash
        }
		
		if (!market.hasSubmarket(Submarkets.GENERIC_MILITARY))
        {
            market.addSubmarket(Submarkets.GENERIC_MILITARY);
            market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo();    // force cargo to generate if needed; fixes military submarket crash
        }

		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		
		market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		for (MarketConditionAPI cond : market.getConditions())
		{
			cond.setSurveyed(true);
		}
		
		Global.getSector().getEconomy().addMarket(market, true);
		market.getPrimaryEntity().setFaction(factionId);
	}
    
    protected Boolean spawnMarinesFactory() {
		float combat = 240f;
		float tanker = 30f;
		float freighter = 30f;
		
		FleetParamsV3 params = new FleetParamsV3(
				largestMkt,
				muspelheim.getLocation(),
				MAIN_FACTION, // quality will always be reduced by non-market-faction penalty, which is what we want 
				null,
				INVESTIGATORS,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				2f // qualityMod
				);
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null || fleet.isEmpty()) return false;
		
		fleet.setName("Phase Lab Marine Fleet");
		fleet.setNoFactionInName(true);
		
		fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NOT_CHASING_GHOST, true);
		
		largestMkt.getPrimaryEntity().getContainingLocation().addEntity(fleet);
		fleet.setLocation(largestMkt.getPrimaryEntity().getLocation().x, largestMkt.getPrimaryEntity().getLocation().y);
			
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, largestMkt.getPrimaryEntity(), 2f + (float) Math.random() * 2f,
								"orbiting " + largestMkt.getName());
		
		fleet.addAssignment(FleetAssignment.DELIVER_MARINES, Factory, 90f,
				"traveling to the " + Factory.getName() + " planet");
		
		fleet.addAssignment(FleetAssignment.HOLD, Factory, 0.25f, "Dropping Marines and Colony Equipments", 
        		new Script() {
					public void run() {
						checkCaptureFactory();
					}
				});
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, Factory, 1000f,
				"traveling to the " + Factory.getName() + " planet");
		
		fleet.addEventListener(this);
		
		return true;
	}
    
    protected Boolean spawnMarinesSecurity() {
		float combat = 240f;
		float tanker = 30f;
		float freighter = 30f;
		
		FleetParamsV3 params = new FleetParamsV3(
				largestMkt,
				muspelheim.getLocation(),
				MAIN_FACTION, // quality will always be reduced by non-market-faction penalty, which is what we want 
				null,
				INVESTIGATORS,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				2f // qualityMod
				);
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null || fleet.isEmpty()) return false;
		
		fleet.setName("Phase Lab Marine Fleet");
		fleet.setNoFactionInName(true);
		
		fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NOT_CHASING_GHOST, true);
		
		largestMkt.getPrimaryEntity().getContainingLocation().addEntity(fleet);
		fleet.setLocation(largestMkt.getPrimaryEntity().getLocation().x, largestMkt.getPrimaryEntity().getLocation().y);
			
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, largestMkt.getPrimaryEntity(), 2f + (float) Math.random() * 2f,
								"orbiting " + largestMkt.getName());
		
		fleet.addAssignment(FleetAssignment.DELIVER_MARINES, Security, 90f,
				"traveling to the " + Security.getName() + " planet");
		
		fleet.addAssignment(FleetAssignment.HOLD, Security, 0.25f, "Dropping Marines and Colony Equipments", 
        		new Script() {
					public void run() {
						checkCaptureSecurity();
					}
				});
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, Security, 1000f,
				"traveling to the " + Security.getName() + " planet");
		
		fleet.addEventListener(this);
		
		return true;
	}
    
    protected Boolean spawnMarinesRSV() {
		float combat = 240f;
		float tanker = 30f;
		float freighter = 30f;
		
		FleetParamsV3 params = new FleetParamsV3(
				largestMkt,
				muspelheim.getLocation(),
				MAIN_FACTION, // quality will always be reduced by non-market-faction penalty, which is what we want 
				null,
				INVESTIGATORS,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				2f // qualityMod
				);
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null || fleet.isEmpty()) return false;
		
		fleet.setName("Phase Lab Marine Fleet");
		fleet.setNoFactionInName(true);
		
		fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NOT_CHASING_GHOST, true);
		
		largestMkt.getPrimaryEntity().getContainingLocation().addEntity(fleet);
		fleet.setLocation(largestMkt.getPrimaryEntity().getLocation().x, largestMkt.getPrimaryEntity().getLocation().y);
			
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, largestMkt.getPrimaryEntity(), 2f + (float) Math.random() * 2f,
								"orbiting " + largestMkt.getName());
		
		fleet.addAssignment(FleetAssignment.DELIVER_MARINES, RSV, 90f,
				"traveling to the " + RSV.getName() + " planet");
		
		fleet.addAssignment(FleetAssignment.HOLD, RSV, 0.25f, "Dropping Marines and Colony Equipments", 
        		new Script() {
					public void run() {
						checkCaptureRSV();
					}
				});
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, RSV, 1000f,
				"traveling to the " + RSV.getName() + " planet");
		
		fleet.addEventListener(this);
		
		return true;
	}
    
    protected Boolean spawnMarinesMoon() {
		float combat = 240f;
		float tanker = 30f;
		float freighter = 30f;
		
		FleetParamsV3 params = new FleetParamsV3(
				largestMkt,
				muspelheim.getLocation(),
				MAIN_FACTION, // quality will always be reduced by non-market-faction penalty, which is what we want 
				null,
				INVESTIGATORS,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				2f // qualityMod
				);
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		if (fleet == null || fleet.isEmpty()) return false;
		
		fleet.setName("Phase Lab Marine Fleet");
		fleet.setNoFactionInName(true);
		
		fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NOT_CHASING_GHOST, true);
		
		largestMkt.getPrimaryEntity().getContainingLocation().addEntity(fleet);
		fleet.setLocation(largestMkt.getPrimaryEntity().getLocation().x, largestMkt.getPrimaryEntity().getLocation().y);
			
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, largestMkt.getPrimaryEntity(), 2f + (float) Math.random() * 2f,
								"orbiting " + largestMkt.getName());
		
		fleet.addAssignment(FleetAssignment.DELIVER_MARINES, Moon, 90f,
				"traveling to the " + Moon.getName() + " planet");
		
		fleet.addAssignment(FleetAssignment.HOLD, Moon, 0.25f, "Dropping Marines and Colony Equipments", 
        		new Script() {
					public void run() {
						checkCaptureMoon();
					}
				});
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, Moon, 1000f,
				"traveling to the " + Moon.getName() + " planet");
		
		fleet.addEventListener(this);
		
		return true;
	}

    @Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
    	if (isDone()) return;
        if (enemyFleets.contains(fleet)) {
			enemyFleets.remove(fleet);
		}
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		// TODO Auto-generated method stub
		int numEnemyFleets = enemyFleets.size();
		if (enemyFleets.contains(fleet)) {
			if (!(battle.isPlayerInvolved())) {
				if ((enemyFleets.size()>MAX_FLEET_ENEMY) || (fleet.getFleetPoints() > 330)) {
					List<FleetMemberAPI> allMembers = fleet.getFleetData().getMembersListCopy();
					for (FleetMemberAPI ship : allMembers) {
						fleet.removeFleetMemberWithDestructionFlash(ship);
					}
				}
			}
		}
	}

    //We are never DONE.
    @Override
    public boolean isDone() {
        return false;
    }

    //No need to run while paused
    @Override
    public boolean runWhilePaused() {
        return false;
    }

}
