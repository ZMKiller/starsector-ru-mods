package data.scripts.world.systems;

import java.awt.Color;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;

import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;

import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;

import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;

import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;

import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;

import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.world.AddMarketplace;
import org.lazywizard.lazylib.MathUtils;
import static data.scripts.util.HIVER_txt.txt;

public class HIVER_Rizdet {

	public void generate(SectorAPI sector) {
		
		
		StarSystemAPI system = sector.createStarSystem("Rizdet");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background1.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("rizdet", // unique id for this star
										    "star_orange",  // id in planets.json
										    545f, 		  // radius (in pixels at default zoom)
										    440, // corona
										    10f, // solar wind burn level
											0.55f, // flare probability
											2.2f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(245, 230, 235)); // light color in entire system, affects all entities
		system.getLocation().set(-77500, 38500);

		PlanetAPI rizdet1 = system.addPlanet("rizdet1", star, "Merak", "rocky_unstable", 300, 50, 1100, 60);
		Misc.initConditionMarket(rizdet1);
		rizdet1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		rizdet1.getMarket().addCondition(Conditions.LOW_GRAVITY);
		rizdet1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
		rizdet1.getMarket().addCondition(Conditions.IRRADIATED);
		rizdet1.getMarket().addCondition(Conditions.VERY_HOT);
		rizdet1.getMarket().addCondition(Conditions.ORE_SPARSE);

		PlanetAPI rizdet2 = system.addPlanet("rizdet2", star, "Zenla", "barren3", 420, 65, 2000, 185);
		Misc.initConditionMarket(rizdet2);
		rizdet2.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		rizdet2.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
		rizdet2.getMarket().addCondition(Conditions.IRRADIATED);
		rizdet2.getMarket().addCondition(Conditions.VERY_HOT);


		PlanetAPI rizdet3 = system.addPlanet("rizdet3", star, "Wazn", "desert1", 220, 185, 5100, 210);
		rizdet3.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		rizdet3.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		rizdet3.getSpec().setUseReverseLightForGlow(true);
		rizdet3.applySpecChanges();
		rizdet3.setCustomDescriptionId("planet_Wazn");
		rizdet3.setInteractionImage("illustrations", "HIVER_hanger");		

		PlanetAPI rizdet3a = system.addPlanet("rizdet3a", rizdet3, "Vazkor", "barren_castiron", 520, 45, 700, 32);
		Misc.initConditionMarket(rizdet3a);
		rizdet3a.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		rizdet3a.getMarket().addCondition(Conditions.HOT);
		rizdet3a.getMarket().addCondition(Conditions.LOW_GRAVITY);

		system.addAsteroidBelt(star, 400, 7800, 512, 550, 600, Terrain.ASTEROID_BELT, "The Belt");
		system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 5800, 550f);

		PlanetAPI rizdet4 = system.addPlanet("rizdet4", star, "Vilani", "jungle", 320, 55, 6100, 550);
		rizdet4.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		rizdet4.getSpec().setGlowColor(new Color(100, 100, 255, 255));
		rizdet4.getSpec().setUseReverseLightForGlow(true);
		rizdet4.applySpecChanges();
		rizdet4.setCustomDescriptionId("planet_Vilani");
		rizdet4.setInteractionImage("illustrations", "HIVER_diplomatic");		

		SectorEntityToken rizdet5_roids = system.addTerrain(Terrain.ASTEROID_FIELD,
				new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
						400f, // min radius
						600f, // max radius
						20, // min asteroid count
						40, // max asteroid count
						4f, // min asteroid radius
						8f, // max asteroid radius
						"Vilani Cloud")); // null for default name
		rizdet5_roids.setCircularOrbit(star, 320, 7800, 550);

		SectorEntityToken rizdet5 = system.addCustomEntity("rizdet5", "Tunguska Station", "station_side02", "HIVER");
		rizdet5.setCircularOrbitPointingDown(star, 140, 8800, 550);
		rizdet5.setCustomDescriptionId("HIVER_Tunguska_station_desc");
		rizdet5.setInteractionImage("illustrations", "HIVER_hanger");


		PlanetAPI rizdet6 = system.addPlanet("rizdet6", star, "Fornax", "terran", 620, 125, 10050, 210);
		rizdet6.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		rizdet6.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		rizdet6.getSpec().setUseReverseLightForGlow(true);
		rizdet6.applySpecChanges();
		rizdet6.setCustomDescriptionId("planet_Fornax");
		rizdet6.setInteractionImage("illustrations", "HIVER_hanger");
		
        // Outer belt.
        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 15200, 450f, Terrain.RING, "Outer Band");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 3000, 201f, null, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 1, Color.white, 256f, 3100, 225f, null, null);
        system.addAsteroidBelt(star, 100, 3000, 500, 100, 190, Terrain.ASTEROID_BELT, "Outer Belt");		 		
		

		// Relays
		SectorEntityToken rizdet_relay = system.addCustomEntity("rizdet_relay", // unique id
				"Rizdet Relay", // name - if null, defaultName from custom_entities.json will be used
				"comm_relay_makeshift", // type of object, defined in custom_entities.json
				"HIVER"); // faction
		rizdet_relay.setCircularOrbitPointingDown(star, 300, 4500, 700);

		SectorEntityToken rizdet_buoy = system.addCustomEntity("rizdet_buoy", // unique id
				"Rizdet Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
				"nav_buoy_makeshift", // type of object, defined in custom_entities.json
				"HIVER"); // faction
		rizdet_buoy.setCircularOrbitPointingDown(star, 120, 4500, 700);

		SectorEntityToken rizdet_sensor = system.addCustomEntity("rizdet_sensor", // unique id
				"Rizdet Sensor Array", // name - if null, defaultName from custom_entities.json will be used
				"sensor_array_makeshift", // type of object, defined in custom_entities.json
				"HIVER"); // faction
		rizdet_sensor.setCircularOrbitPointingDown(star, 60, 12000, 1000);
		
       //Salvage
        SectorEntityToken scrap1 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_SMALL_REMNANT, Factions.DERELICT);
        scrap1.setId("rizdet_scrap1");
		scrap1.setCircularOrbit(star, 165, 2420, 23);
		Misc.setDefenderOverride(scrap1, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap1.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap2 = DerelictThemeGenerator.addSalvageEntity(system, Entities.EQUIPMENT_CACHE_SMALL, Factions.DERELICT);
        scrap2.setId("rizdet_scrap2");
		scrap2.setCircularOrbit(star, 285, 1420, 123);
		Misc.setDefenderOverride(scrap2, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap2.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap3 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_REMNANT, Factions.DERELICT);
        scrap3.setId("rizdet_scrap3");
		scrap3.setCircularOrbit(star, 145, 4480, 253);
		Misc.setDefenderOverride(scrap3, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap3.setDiscoverable(Boolean.TRUE);		
		
		SectorEntityToken probe = DerelictThemeGenerator.addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT);
		probe.setId("unknown_probe");
		probe.setCircularOrbit(star, 200, 17500, 400f);
		Misc.setDefenderOverride(probe, new DefenderDataOverride(Factions.DERELICT, 1f, 6, 6, 1));
		CargoAPI extraProbeSalvage = Global.getFactory().createCargo(true);
		extraProbeSalvage.addCommodity(Commodities.GAMMA_CORE, 1);
		BaseSalvageSpecial.addExtraSalvage(extraProbeSalvage, probe.getMemoryWithoutUpdate(), -1);			

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("rizdet_jump_point1", "Tunguska Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 140, 9200, 550);
		jumpPoint.setOrbit(orbit);
		orbit.setEntity(jumpPoint);
		jumpPoint.setRelatedPlanet(rizdet6);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

		PlanetAPI rizdet7 = system.addPlanet("rizdet7", star, "Silvaril", "gas_giant", 110, 700, 18000, 650);
		Misc.initConditionMarket(rizdet7);
		rizdet7.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
		rizdet7.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		rizdet7.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		rizdet7.getMarket().addCondition(Conditions.VERY_COLD);
		rizdet7.getMarket().addCondition(Conditions.POOR_LIGHT);
		rizdet7.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);


		PlanetAPI rizdet8 = system.addPlanet("rizdet8", star, "Shandor", "ice_giant", 120, 550, 21000, 725);
		Misc.initConditionMarket(rizdet8);
		rizdet8.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
		rizdet8.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		rizdet8.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		rizdet8.getMarket().addCondition(Conditions.VERY_COLD);
		rizdet8.getMarket().addCondition(Conditions.POOR_LIGHT);
		rizdet8.getMarket().addCondition(Conditions.VOLATILES_TRACE);

		system.addRingBand(rizdet8, "misc", "rings_special0", 256f, 1, new Color(225,215,255,200), 128f, 750, 60f, Terrain.RING, "The Rings of Shandor");

		system.autogenerateHyperspaceJumpPoints(true, true);
	}
}
