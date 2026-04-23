//By VladimirVV. Spawns lore-friendly fleets.
package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
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
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.INVESTIGATORS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

public class dpl_exploratory_fleet_plugin implements EveryFrameScript, FleetEventListener {

    //The ID for the faction that gets their relations adjusted (sorry, couldn't remember your faction ID off the top of my head)
    private static final String MAIN_FACTION = "dpl_phase_lab";
    private static int MAX_FLEET = 3;
    protected Set<CampaignFleetAPI> expFleets = new HashSet<>();
    private int CURRENT_FLEET = 0;
    private boolean FLEET_SPAWNED = false;

    @Override
    public void advance( float amount ) {
        //Necessary Sector check
        SectorAPI sector = Global.getSector();
        if (sector == null) {
            return;
        }

        //This situation should never happen. But if it happens, we don't want this code to break the game.
        if (sector.getFaction(MAIN_FACTION) == null) {
            return;
        }

        CURRENT_FLEET = expFleets.size();
        FLEET_SPAWNED = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_exp_fleet_spawned");
        if (CURRENT_FLEET <= MAX_FLEET && !FLEET_SPAWNED) {
        	boolean success = spawnFleet();
        	if (success) {
            	Global.getSector().getMemoryWithoutUpdate().set("$dpl_exp_fleet_spawned", true, 7);
        	}
        }
        
    }
    
    protected StarSystemAPI pickTargetSystem() {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.THEME_CORE)) {
				continue;
			}
			
			// want: interesting
			float weight = 0f;

			if (system.hasTag(Tags.THEME_INTERESTING)) {
				weight = 5f;
			}
			
			picker.add(system, weight);
		}
		return picker.pick();
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
    
    protected Boolean spawnFleet() {
		StarSystemAPI target = pickTargetSystem();
		if (target == null) return false;
		
		MarketAPI source = FindLargestMarket();
		if (source == null) return false;
		SectorEntityToken planet = source.getPrimaryEntity();
		String name = source.getName();
		
		float combat = 75f;
		float tanker = 30f;
		float freighter = 30f;
		
		FleetParamsV3 params = new FleetParamsV3(
				source, 
				target.getLocation(),
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
		
		fleet.getFleetData().addFleetMember("dpl_pioneer_standard");
		
		fleet.setName("Phase Lab Exploratory Fleet");
		fleet.setNoFactionInName(true);
		
		fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NOT_CHASING_GHOST, true);
		
		planet.getContainingLocation().addEntity(fleet);
		fleet.setLocation(planet.getLocation().x, planet.getLocation().y);
			
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, planet, 2f + (float) Math.random() * 2f,
								"orbiting " + name);
		
		Vector2f dest = Misc.getPointAtRadius(target.getLocation(), 1500);
		LocationAPI hyper = Global.getSector().getHyperspace();
		SectorEntityToken token = hyper.createToken(dest.x, dest.y);
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, token, 1000,
				"traveling to the " + target.getBaseName() + " star system");

		fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target.getCenter(), 20,
					"doing investigations in the " + target.getBaseName() + " star system");
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, planet, 1000,
					"returning to " + name);
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, planet, 2f + 2f * (float) Math.random(),
					"transferring scientific data");
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, planet, 1000);
		
		fleet.addEventListener(this);
		
		expFleets.add(fleet);
		
		return true;
	}

    @Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
    	if (isDone()) return;
        if (expFleets.contains(fleet)) {
        	expFleets.remove(fleet);
        	CURRENT_FLEET = expFleets.size();
		}
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		// TODO Auto-generated method stub
		
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
