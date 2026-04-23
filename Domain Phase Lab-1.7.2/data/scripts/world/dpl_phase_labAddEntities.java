package data.scripts.world;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;

public class dpl_phase_labAddEntities {
    
    public static SectorEntityToken addDerelict(StarSystemAPI system, SectorEntityToken focus, String variantId,
            ShipCondition condition, float orbitRadius, boolean recoverable, boolean tag, String tagstring) {        
        
        DerelictShipData params = new DerelictShipData(new PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
        ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

        if (recoverable) {
            ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        if (tag) {
			ship.getMemoryWithoutUpdate().set(tagstring, true);
		}
        return ship;
    }
    
    public static SectorEntityToken spawnNamedWreck(Vector2f loc, StarSystemAPI system, String FactionId, String vId, String shipName, boolean recoverable) {
        String variantId = vId;
        Object params = new DerelictShipData(
            new PerShipData(
                Global.getSettings().getVariant(variantId), ShipRecoverySpecial.ShipCondition.WRECKED,
                shipName, FactionId, 0f
            ), false
        );

        SectorEntityToken shipWreck = BaseThemeGenerator.addSalvageEntity(
            system, Entities.WRECK, Factions.NEUTRAL, params
        );
        
        if (recoverable) {
        	ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
            PerShipData copy = new PerShipData(
                    Global.getSettings().getVariant(variantId), ShipRecoverySpecial.ShipCondition.WRECKED,
                    shipName, FactionId, 0f
                );
            data.addShip(copy);

            Misc.setSalvageSpecial(shipWreck, data);
        }

        shipWreck.setDiscoverable(true);
        shipWreck.setLocation(loc.x, loc.y);
        
        return shipWreck;
    }
    
    public static SectorEntityToken spawnUniqueWreck(Vector2f loc, StarSystemAPI system, String FactionId, String vId, String shipName, boolean recoverable) {
        String variantId = vId;
        Object params = new DerelictShipData(
            new PerShipData(
                Global.getSettings().getVariant(variantId), ShipRecoverySpecial.ShipCondition.WRECKED,
                shipName, FactionId, 0f
            ), false
        );

        SectorEntityToken shipWreck = BaseThemeGenerator.addSalvageEntity(
            system, Entities.WRECK, Factions.NEUTRAL, params
        );
        
        if (recoverable) {
        	ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
            PerShipData copy = new PerShipData(
                    Global.getSettings().getVariant(variantId), ShipRecoverySpecial.ShipCondition.WRECKED,
                    shipName, FactionId, 0f
                );
            copy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
            copy.variant.addTag(Tags.SHIP_UNIQUE_SIGNATURE);
            data.addShip(copy);

            Misc.setSalvageSpecial(shipWreck, data);
        }

        shipWreck.setDiscoverable(true);
        shipWreck.setLocation(loc.x, loc.y);
        
        return shipWreck;
    }
    
    public static SectorEntityToken spawnOlympias(LocationAPI loc, Vector2f pos) {
        SectorEntityToken Olympias = loc.addCustomEntity("dpl_Olympias",
				 "Olympias",
		"dpl_Olympias", // type of object, defined in custom_entities.json
		Factions.NEUTRAL); // faction
        
        StarSystemAPI System = Olympias.getStarSystem();
        PlanetAPI Star = System.getStar();
        float angle = Misc.getAngleInDegreesStrict(Star.getLocation(), pos);
        float radius = Misc.getDistance(Star.getLocation(), pos);
        Olympias.setCircularOrbit(Star, angle, radius, 180);
        
        return Olympias;
    }
    
    //This function spawns a fleet belonging to faction1 that is composed with ships from faction2.
    //This only spawns the fleet data! You need to add the fleet into your system separately. Giving you more freedom to
    //modify your fleets before you actually spawn them. If you don't want a second faction, imput null for faction2.
    public static CampaignFleetAPI spawn_fleet_2fctn(String name, String faction1, String faction2, String fleetType, float combatPts, float freighterPts, float tankerPts, float transportPts, float linerPts, float utilityPts, float qualityMod, int sMods) {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                faction1,
                null,
                fleetType,
                combatPts, // combatPts
                freighterPts, // freighterPts
                tankerPts, // tankerPts
                transportPts, // transportPts
                linerPts, // linerPts
                utilityPts, // utilityPts
                qualityMod // qualityMod
        );
    	params.averageSMods = sMods;
    	CampaignFleetAPI target;
    	
    	if (faction2 != null) {
    		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        	
        	target = Global.getFactory().createEmptyFleet(faction2, name, true);
        	List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
    		for (FleetMemberAPI curr : members) {
    			target.getFleetData().addFleetMember(curr);
    		}
    		target.getFleetData().sort();
    	} else {
    		target = FleetFactoryV3.createFleet(params);
    	}
    	
    	return target;
    }
    
    public static CampaignFleetAPI spawn_fleet_2fctn(String name, String faction1, String faction2, String fleetType, float combatPts, float freighterPts, float tankerPts, float transportPts, float linerPts, float utilityPts, float qualityMod) {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                faction1,
                null,
                fleetType,
                combatPts, // combatPts
                freighterPts, // freighterPts
                tankerPts, // tankerPts
                transportPts, // transportPts
                linerPts, // linerPts
                utilityPts, // utilityPts
                qualityMod // qualityMod
        );

    	CampaignFleetAPI target;
    	
    	if (faction2 != null) {
    		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        	
        	target = Global.getFactory().createEmptyFleet(faction2, name, true);
        	List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
    		for (FleetMemberAPI curr : members) {
    			target.getFleetData().addFleetMember(curr);
    		}
    		target.getFleetData().sort();
    	} else {
    		target = FleetFactoryV3.createFleet(params);
    	}
    	
    	return target;
    }

    public static CampaignFleetAPI spawnStation(float x, float y, StarSystemAPI system, String variant, String faction) {
    	String type = variant;
		Random random = new Random();
		final CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(faction, FleetTypes.BATTLESTATION, null);
				
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, type);
		fleet.getFleetData().addFleetMember(member);

		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
		fleet.addTag(Tags.NEUTRINO_HIGH);

		fleet.setStationMode(true);

		fleet.clearAbilities();
		fleet.addAbility(Abilities.TRANSPONDER);
		fleet.getAbility(Abilities.TRANSPONDER).activate();
		fleet.getDetectedRangeMod().modifyFlat("gen", 1000f);

		fleet.setAI(null);

		system.addEntity(fleet);
		fleet.setLocation(x, y);

		boolean damaged = type.toLowerCase().contains("damaged");
		String coreId = Commodities.ALPHA_CORE;
		if (damaged) {
			fleet.getMemoryWithoutUpdate().set("$damagedStation", true);
			fleet.setName(fleet.getName() + " (Damaged)");
		}

		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(coreId);
		PersonAPI commander = plugin.createPerson(coreId, fleet.getFaction().getId(), random);

		fleet.setCommander(commander);
		fleet.getFlagship().setCaptain(commander);

		if (!damaged) {
			RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.getFlagship());
			RemnantOfficerGeneratorPlugin.addCommanderSkills(commander, fleet, null, 3, random);
		}

		member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
		
		return fleet;
    }
    		
}