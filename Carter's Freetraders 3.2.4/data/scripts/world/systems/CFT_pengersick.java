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
import static data.scripts.util.CFT_txt.txt;

public class CFT_pengersick {	

	public void generate(SectorAPI sector) {
		
		
		StarSystemAPI system = sector.createStarSystem("Pengersick");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/CFT/backgrounds/Pengersick_background.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("Pengersick", // unique id for this star
											 "star_orange", // id in planets.json
										 650f, 		// radius (in pixels at default zoom)
										 500); // corona radius, from star edge
		system.setLightColor(new Color(255, 235, 205)); // light color in entire system, affects all entities
		system.getLocation().set(5000, 7500);

        SectorEntityToken pengersick_nebula = Misc.addNebulaFromPNG("data/campaign/terrain/CFT_pengersick.png",
				  0, 0, 
				  system, 
				  "terrain", "nebula", 
				  4, 4, StarAge.AVERAGE); 				  
				  
		//Planets
		PlanetAPI Pengersick1 = system.addPlanet("Pengersick1", star, "Piskie", "toxic", 530, 600, 20010, 826);
		Misc.initConditionMarket(Pengersick1);
		Pengersick1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Pengersick1.getMarket().addCondition(Conditions.LOW_GRAVITY);
		Pengersick1.getMarket().addCondition(Conditions.IRRADIATED);
		Pengersick1.getMarket().addCondition(Conditions.VERY_COLD);
		Pengersick1.getMarket().addCondition(Conditions.ORE_MODERATE);
		Pengersick1.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
		Pengersick1.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);		

		PlanetAPI Pengersick2 = system.addPlanet("Pengersick2", star, "Pestreath", "rocky_unstable", 120, 75, 18000, 185);
		Misc.initConditionMarket(Pengersick2);
		Pengersick2.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Pengersick2.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
		Pengersick2.getMarket().addCondition(Conditions.HOT);
		Pengersick2.getMarket().addCondition(Conditions.ORE_RICH);		
		Pengersick2.getMarket().addCondition(Conditions.RARE_ORE_RICH);	

		PlanetAPI Pengersick3 = system.addPlanet("Pengersick3", star, "Kings Cove", "terran-eccentric", 720, 185, 4200, 510);
		Pengersick3.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Pengersick3.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		Pengersick3.getSpec().setUseReverseLightForGlow(true);
		Pengersick3.applySpecChanges();
		Pengersick3.setCustomDescriptionId("planet_king");

		PlanetAPI Pengersick3a = system.addPlanet("Pengersick3a", Pengersick3, " Cudden", "lava_minor", 180, 190, 3400, 400);
		Misc.initConditionMarket(Pengersick3a);
		Pengersick3a.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Pengersick3a.getMarket().addCondition(Conditions.VERY_HOT);
		Pengersick3a.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		Pengersick3a.getMarket().addCondition(Conditions.ORE_RICH);		
		Pengersick3a.getMarket().addCondition(Conditions.RARE_ORE_RICH);	

		PlanetAPI Pengersick4 = system.addPlanet("Pengersick4", star, "Prussia", "arid", 320, 155, 5700, 550);
		Pengersick4.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Pengersick4.getSpec().setGlowColor(new Color(100, 100, 255, 255));
		Pengersick4.getSpec().setUseReverseLightForGlow(true);
		Pengersick4.applySpecChanges();
		Pengersick4.setCustomDescriptionId("planet_prussia"); 		
			
		SectorEntityToken Pengersick5 = system.addCustomEntity("Pengersick5", "Prussia Station", "station_midline3", "CFT");
		Pengersick5.setCircularOrbitPointingDown(star, 340, 7800, 250);
		Pengersick5.setCustomDescriptionId("Prussia_desc");
		Pengersick5.setInteractionImage("illustrations", "orbital");

		PlanetAPI Pengersick6 = system.addPlanet("Pengersick6", star, "Porthleah", "frozen", 360f, 400, 14000, 265);
		Pengersick6.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Pengersick6.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		Pengersick6.getSpec().setUseReverseLightForGlow(true);
		Pengersick6.applySpecChanges();
		Pengersick6.setCustomDescriptionId("planet_porthleah");
		Pengersick6.setInteractionImage("illustrations", "pirate_station");
		
		PlanetAPI Pengersick7 = system.addPlanet("Pengersick7", star, "Cove", "gas_giant", 360, 250, 8800, 580);
		Misc.initConditionMarket(Pengersick7);
		Pengersick7.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
		Pengersick7.getMarket().addCondition(Conditions.LOW_GRAVITY);
		Pengersick7.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		Pengersick7.getMarket().addCondition(Conditions.VERY_COLD);
		Pengersick7.getMarket().addCondition(Conditions.POOR_LIGHT);
		Pengersick7.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);

		PlanetAPI Pengersick8 = system.addPlanet("Pengersick8", star, "Penzance", "barren3", 200, 220, 6150f, 250f);
		Misc.initConditionMarket(Pengersick8);
		Pengersick8.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		Pengersick8.getMarket().addCondition(Conditions.VERY_COLD);
		Pengersick8.getMarket().addCondition(Conditions.ORE_MODERATE);
		Pengersick8.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);			
		Pengersick8.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
		
        PlanetAPI Pengersick9 = system.addPlanet("Pengersick9", star, "Sherborne", "barren-bombarded", 210, 40, 9750, 300);
            
            Misc.initConditionMarket(Pengersick9);
            Pengersick9.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
            Pengersick9.getMarket().addCondition(Conditions.LOW_GRAVITY);
            Pengersick9.getMarket().addCondition(Conditions.METEOR_IMPACTS);
			Pengersick9.getMarket().addCondition(Conditions.ORE_SPARSE);			
            Pengersick9.getMarket().addCondition(Conditions.VOLATILES_TRACE);		
			
		// Inner Belt
		system.addAsteroidBelt(star, 400, 6800, 512, 550, 600, Terrain.ASTEROID_BELT, "The Belt");
		system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 6800, 550f);				
		
        // Outer belt.
        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 15200, 450f, Terrain.RING, "Outer Band");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 3000, 201f, null, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 1, Color.white, 256f, 3100, 225f, null, null);
        system.addAsteroidBelt(star, 100, 3000, 500, 100, 190, Terrain.ASTEROID_BELT, "Outer Belt");		 
		
		//Jump Points
		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("Pengersick_jump_point1", "Prussia Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 340, 8300, 250);
		jumpPoint.setOrbit(orbit);
		orbit.setEntity(jumpPoint);
		jumpPoint.setRelatedPlanet(Pengersick6);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);
		
		JumpPointAPI fringe = Global.getFactory().createJumpPoint("Pengersick_jump_point2", "Fringe Jump-point");
		orbit = Global.getFactory().createCircularOrbit(star, 160, 11200, 600);
		fringe.setOrbit(orbit);
		fringe.setStandardWormholeToHyperspaceVisual();
		system.addEntity(fringe);		
		
		SectorEntityToken pengersick_basefield1 = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(550f, // terrain effect band width
						7250f, // terrain effect middle radius
						star, // entity that it's around
						7000f, // visual band start
						7500f, // visual band end
						new Color(50, 20, 100, 50), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(50, 20, 110, 130),
						new Color(150, 30, 120, 150),
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160),
						new Color(127, 0, 255)
				));
		pengersick_basefield1.setCircularOrbit(star, 0, 0, 100);
		
           // Belt debris fields.
            DebrisFieldTerrainPlugin.DebrisFieldParams params1 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                450f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                10000000f, // duration in days 
                0f); // days the field will keep generating glowing pieces
            params1.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
            params1.baseSalvageXP = 700; // base XP for scavenging in field
            SectorEntityToken debrisBelt1 = Misc.addDebrisField(system, params1, StarSystemGenerator.random);
            debrisBelt1.setSensorProfile(1000f);
            debrisBelt1.setDiscoverable(true);
            debrisBelt1.setCircularOrbit(star, 120f, 6880, 240f);
            debrisBelt1.setId("pengersick_debris1");
                
            DebrisFieldTerrainPlugin.DebrisFieldParams params2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                300f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                10000000f, // duration in days 
                0f); // days the field will keep generating glowing pieces
            params2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
            params2.baseSalvageXP = 700; // base XP for scavenging in field
            SectorEntityToken debrisBelt2 = Misc.addDebrisField(system, params2, StarSystemGenerator.random);
            debrisBelt2.setSensorProfile(1000f);
            debrisBelt2.setDiscoverable(true);
            debrisBelt2.setCircularOrbit(star, 360f, 6880, 240f);
            debrisBelt2.setId("pengersick_debris2");		
		
		// Relays
		SectorEntityToken Pengersick_relay = system.addCustomEntity("Pengersick_relay", // unique id
				"Pengersick Relay", // name - if null, defaultName from custom_entities.json will be used
				"comm_relay_makeshift", // type of object, defined in custom_entities.json
				"CFT"); // faction
		Pengersick_relay.setCircularOrbitPointingDown(star, 820, 3550, 700);

		SectorEntityToken Pengersick_buoy = system.addCustomEntity("Pengersick_buoy", // unique id
				"Pengersick Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
				"nav_buoy_makeshift", // type of object, defined in custom_entities.json
				"CFT"); // faction
		Pengersick_buoy.setCircularOrbitPointingDown(star, -120, 11000, 420);

		SectorEntityToken Pengersick_sensor = system.addCustomEntity("Pengersick_sensor", // unique id
				"Pengersick Sensor Array", // name - if null, defaultName from custom_entities.json will be used
				"sensor_array_makeshift", // type of object, defined in custom_entities.json
				"CFT"); // faction
		Pengersick_sensor.setCircularOrbitPointingDown(star, 157, 7500, 1275);		
		
       //Salvage
        SectorEntityToken scrap1 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_SMALL_REMNANT, Factions.DERELICT);
        scrap1.setId("pengersick_scrap1");
		scrap1.setCircularOrbit(star, 165, 2420, 23);
		Misc.setDefenderOverride(scrap1, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap1.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap2 = DerelictThemeGenerator.addSalvageEntity(system, Entities.EQUIPMENT_CACHE_SMALL, Factions.DERELICT);
        scrap2.setId("pengersick_scrap2");
		scrap2.setCircularOrbit(star, 285, 1420, 123);
		Misc.setDefenderOverride(scrap2, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap2.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap3 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_REMNANT, Factions.DERELICT);
        scrap3.setId("pengersick_scrap3");
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
		
        //Debris, asteroid fields and derelicts
		addDerelict(system, Pengersick1, "medusa_CS", ShipRecoverySpecial.ShipCondition.BATTERED, 625f, (Math.random()<0.6));
		addDerelict(system, Pengersick1, "CFT_vergulde_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 225f, (Math.random()<0.6));		
		addDerelict(system, Pengersick5, "wolf_Assault", ShipRecoverySpecial.ShipCondition.BATTERED, 275f, (Math.random()<0.6));
		addDerelict(system, Pengersick5, "lasher_CS", ShipRecoverySpecial.ShipCondition.BATTERED, 150f, (Math.random()<0.6));
		addDerelict(system, Pengersick2, "mule_Fighter_Support", ShipRecoverySpecial.ShipCondition.BATTERED, 350f, (Math.random()<0.6));
		addDerelict(system, Pengersick2, "tarsus_d_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, 280f, (Math.random()<0.6));
		addDerelict(system, Pengersick3a, "CFT_clipper_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 280f, (Math.random()<0.6));		
		addDerelict(system, Pengersick2, "condor_Support", ShipRecoverySpecial.ShipCondition.BATTERED, 450f, (Math.random()<0.6));
		addDerelict(system, Pengersick6, "falcon_p_Strike", ShipRecoverySpecial.ShipCondition.BATTERED, 370f,  (Math.random()<0.6));
		addDerelict(system, Pengersick6, "wolf_d_pirates_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 170f,  (Math.random()<0.6));
		addDerelict(system, Pengersick6, "enforcer_d_pirates_Strike", ShipRecoverySpecial.ShipCondition.BATTERED, 220f,  (Math.random()<0.6));		
		addDerelict(system, Pengersick7, "hammerhead_Support", ShipRecoverySpecial.ShipCondition.BATTERED, 510f,  (Math.random()<0.6));
		addDerelict(system, Pengersick7, "lasher_PD", ShipRecoverySpecial.ShipCondition.BATTERED, 325f, (Math.random()<0.6));
		addDerelict(system, Pengersick8, "brawler_pather_Raider", ShipRecoverySpecial.ShipCondition.BATTERED, 285f, (Math.random()<0.6));
		addDerelict(system, Pengersick8, "dram_Light", ShipRecoverySpecial.ShipCondition.BATTERED, 320f, (Math.random()<0.6));
		addDerelict(system, Pengersick9, "hound_hegemony_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, 450f, (Math.random()<0.6));
		addDerelict(system, Pengersick9, "kite_pirates_Raider", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));		

            SectorEntityToken scrap4 = DerelictThemeGenerator.addSalvageEntity(system, Entities.SUPPLY_CACHE, Factions.DERELICT);
            scrap4.setId("pengersick_scrap4");
            scrap4.setCircularOrbit(Pengersick9, 105, 240, 135);
            Misc.setDefenderOverride(scrap4, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
            scrap4.setDiscoverable(Boolean.TRUE);
			
		SectorEntityToken Dogfield1 = system.addTerrain(Terrain.ASTEROID_FIELD, 
				new AsteroidFieldTerrainPlugin.AsteroidFieldParams(500f, 700f,	30,	40, 4f,	24f, ("A gravitational anomaly that surrounds the planet Sherborne has pulled these asteroids together. The field is estimated to form a moon within the next 400 cycles."))); 
		
		SectorEntityToken Dogfield2 = system.addTerrain(Terrain.ASTEROID_FIELD, 
				new AsteroidFieldTerrainPlugin.AsteroidFieldParams(500f, 700f,	30,	40, 4f,	24f, ("The remains of a small moon that formerly orbited Pestreath. It shattered when a rogue comet was heading towed the planet and the then pirate residents towed the moon in it's path in hopes of saving the planet."))); 	
						
 		Dogfield1.setCircularOrbit(Pengersick9, 180+45+60, 18, 5);
		Dogfield2.setCircularOrbit(Pengersick2, 180+45-60, 18, 5);                   
			
            DebrisFieldTerrainPlugin.DebrisFieldParams params3 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                700f, // field radius - should not go above 1000 for performance reasons
                1.0f, // density, visual - affects number of debris pieces
		10000000f, // duration in days 
		0f); // days the field will keep generating glowing pieces
            params3.source = DebrisFieldTerrainPlugin.DebrisFieldSource.SALVAGE;
            params3.baseSalvageXP = 1000; // base XP for scavenging in field
            SectorEntityToken debrisL4 = Misc.addDebrisField(system, params3, StarSystemGenerator.random);
            debrisL4.setSensorProfile(600f);
            debrisL4.setDiscoverable(true);
            debrisL4.setCircularOrbit(star, 210f, 10800, 300f);
            debrisL4.setId("pengersick_debris5");
 		

		system.addRingBand(Pengersick1, "misc", "rings_special0", 256f, 1, new Color(225,215,255,200), 128f, 750, 60f, Terrain.RING, "The Rings of Piskie");

		system.autogenerateHyperspaceJumpPoints(true, true);
	}
protected void addDerelict(StarSystemAPI system,
							   SectorEntityToken focus,
							   String variantId,
							   ShipRecoverySpecial.ShipCondition condition,
							   float orbitRadius,
							   boolean recoverable) {
		DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), true);
		SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
		ship.setDiscoverable(true);

		float orbitDays = 60f;
		ship.setCircularOrbit(focus, (float) MathUtils.getRandomNumberInRange(-2,2) + 90f, orbitRadius, orbitDays);

		if (recoverable) {
			SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
			Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
		}
	}	
	
}