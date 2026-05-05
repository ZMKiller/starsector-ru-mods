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

import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;

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
import static data.scripts.util.JYD_txt.txt;

public class JYD_dogstar {	

	public void generate(SectorAPI sector) {
		
		
		StarSystemAPI system = sector.createStarSystem("Dogstar");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/JYD/backgrounds/Dogstar_background.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("Dogstar", // unique id for this star
										    "star_white",  // id in planets.json
										    575f, 		  // radius (in pixels at default zoom)
										    460, // corona
										    12f, // solar wind burn level
											0.65f, // flare probability
											2.5f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(245, 230, 235)); // light color in entire system, affects all entities
		system.getLocation().set(-5000, -7500);

        SectorEntityToken dogstar_nebula = Misc.addNebulaFromPNG("data/campaign/terrain/JYD_dogstar.png",
				  0, 0, 
				  system, 
				  "terrain", "nebula", 
				  4, 4, StarAge.AVERAGE); 

		PlanetAPI Dogstar1 = system.addPlanet("Dogstar1", star, "Croce", "rocky_metallic", 200, 50, 1100, 60);
		Misc.initConditionMarket(Dogstar1);
		Dogstar1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Dogstar1.getMarket().addCondition(Conditions.LOW_GRAVITY);
		Dogstar1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
		Dogstar1.getMarket().addCondition(Conditions.IRRADIATED);
		Dogstar1.getMarket().addCondition(Conditions.VERY_HOT);
		Dogstar1.getMarket().addCondition(Conditions.ORE_RICH);
		Dogstar1.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);	

		PlanetAPI Dogstar2 = system.addPlanet("Dogstar2", star, "Jigsaw", "rocky_unstable", 120, 65, 2000, 185);
		Misc.initConditionMarket(Dogstar2);
		Dogstar2.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Dogstar2.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
		Dogstar2.getMarket().addCondition(Conditions.IRRADIATED);
		Dogstar2.getMarket().addCondition(Conditions.VERY_HOT);
		Dogstar2.getMarket().addCondition(Conditions.ORE_RICH);		
		Dogstar2.getMarket().addCondition(Conditions.RARE_ORE_RICH);	

		PlanetAPI Dogstar3 = system.addPlanet("Dogstar3", star, "Junkyard", "jungle", 20, 185, 6200, 210);
		Dogstar3.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Dogstar3.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		Dogstar3.getSpec().setUseReverseLightForGlow(true);
		Dogstar3.applySpecChanges();
		Dogstar3.setCustomDescriptionId("planet_junkyard");

		PlanetAPI Dogstar3a = system.addPlanet("Dogstar3a", Dogstar3, "Slag", "lava_minor", 120, 45, 700, 32);
		Misc.initConditionMarket(Dogstar3a);
		Dogstar3a.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Dogstar3a.getMarket().addCondition(Conditions.HOT);
		Dogstar3a.getMarket().addCondition(Conditions.LOW_GRAVITY);
		Dogstar3a.getMarket().addCondition(Conditions.ORE_SPARSE);			

		system.addAsteroidBelt(star, 400, 6800, 512, 550, 600, Terrain.ASTEROID_BELT, "The Belt");
		system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 6800, 550f);

		PlanetAPI Dogstar4 = system.addPlanet("Dogstar4", star, "Scrapyard", "arid", 320, 55, 7900, 550);
		Dogstar4.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Dogstar4.getSpec().setGlowColor(new Color(100, 100, 255, 255));
		Dogstar4.getSpec().setUseReverseLightForGlow(true);
		Dogstar4.applySpecChanges();
		Dogstar4.setCustomDescriptionId("planet_scrapyard");

            // Belt debris fields.
            DebrisFieldTerrainPlugin.DebrisFieldParams params1 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                450f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                10000000f, // duration in days 
                0f); // days the field will keep generating glowing pieces
            params1.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
            params1.baseSalvageXP = 500; // base XP for scavenging in field
            SectorEntityToken debrisBelt1 = Misc.addDebrisField(system, params1, StarSystemGenerator.random);
            debrisBelt1.setSensorProfile(1000f);
            debrisBelt1.setDiscoverable(true);
            debrisBelt1.setCircularOrbit(star, 120f, 6880, 240f);
            debrisBelt1.setId("dogstar_debris1");
                
            DebrisFieldTerrainPlugin.DebrisFieldParams params2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                300f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                10000000f, // duration in days 
                0f); // days the field will keep generating glowing pieces
            params2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
            params2.baseSalvageXP = 500; // base XP for scavenging in field
            SectorEntityToken debrisBelt2 = Misc.addDebrisField(system, params2, StarSystemGenerator.random);
            debrisBelt2.setSensorProfile(1000f);
            debrisBelt2.setDiscoverable(true);
            debrisBelt2.setCircularOrbit(star, 360f, 6880, 240f);
            debrisBelt2.setId("dogstar_debris2");
			
		SectorEntityToken Dogstar5 = system.addCustomEntity("Dogstar5", "Processing", "station_lowtech2", "JYD");
		Dogstar5.setCircularOrbitPointingDown(star, 140, 7800, 550);
		Dogstar5.setCustomDescriptionId("Processing_desc");
		Dogstar5.setInteractionImage("illustrations", "orbital");


		PlanetAPI Dogstar6 = system.addPlanet("Dogstar6", star, "El Dorado", "toxic", 20, 125, 12550, 210);
		Dogstar6.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Dogstar6.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		Dogstar6.getSpec().setUseReverseLightForGlow(true);
		Dogstar6.applySpecChanges();
		Dogstar6.setCustomDescriptionId("planet_El Dorado");
		Dogstar6.setInteractionImage("illustrations", "pirate_station");
		
		
        // Outer belt.
        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 15200, 450f, Terrain.RING, "Outer Band");

        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.gray, 256f, 15580, 453f, null, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 15620, 461f, null, null);
        system.addAsteroidBelt(star, 150, 15600, 170, 200, 520, Terrain.ASTEROID_BELT, "Outer Belt");		

        //Salvage
        SectorEntityToken scrap1 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_SMALL_REMNANT, Factions.DERELICT);
        scrap1.setId("dogstar_scrap1");
		scrap1.setCircularOrbit(star, 165, 6420, 223);
		Misc.setDefenderOverride(scrap1, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap1.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap2 = DerelictThemeGenerator.addSalvageEntity(system, Entities.EQUIPMENT_CACHE_SMALL, Factions.DERELICT);
        scrap2.setId("dogstar_scrap2");
		scrap2.setCircularOrbit(star, 285, 6420, 223);
		Misc.setDefenderOverride(scrap2, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap2.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap3 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_REMNANT, Factions.DERELICT);
        scrap3.setId("dogstar_scrap3");
	scrap3.setCircularOrbit(star, 45, 6480, 223);
	Misc.setDefenderOverride(scrap3, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap3.setDiscoverable(Boolean.TRUE);


		// Relays
		SectorEntityToken Dogstar_relay = system.addCustomEntity("Dogstar_relay", // unique id
				"Dogstar Relay", // name - if null, defaultName from custom_entities.json will be used
				"comm_relay_makeshift", // type of object, defined in custom_entities.json
				"JYD"); // faction
		Dogstar_relay.setCircularOrbitPointingDown(star, 300, 4500, 700);

		SectorEntityToken Dogstar_buoy = system.addCustomEntity("Dogstar_buoy", // unique id
				"Dogstar Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
				"nav_buoy_makeshift", // type of object, defined in custom_entities.json
				"JYD"); // faction
		Dogstar_buoy.setCircularOrbitPointingDown(star, 120, 5500, 700);

		SectorEntityToken Dogstar_sensor = system.addCustomEntity("Dogstar_sensor", // unique id
				"Dogstar Sensor Array", // name - if null, defaultName from custom_entities.json will be used
				"sensor_array_makeshift", // type of object, defined in custom_entities.json
				"JYD"); // faction
		Dogstar_sensor.setCircularOrbitPointingDown(star, 60, 8000, 1000);

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("Dogstar_jump_point1", "Processing Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 140, 8200, 550);
		jumpPoint.setOrbit(orbit);
		orbit.setEntity(jumpPoint);
		jumpPoint.setRelatedPlanet(Dogstar6);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

		PlanetAPI Dogstar7 = system.addPlanet("Dogstar7", star, "32", "gas_giant", 360, 250, 8800, 580);
		Misc.initConditionMarket(Dogstar7);
		Dogstar7.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
		Dogstar7.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		Dogstar7.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		Dogstar7.getMarket().addCondition(Conditions.VERY_COLD);
		Dogstar7.getMarket().addCondition(Conditions.POOR_LIGHT);
		Dogstar7.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);

		PlanetAPI Dogstar8 = system.addPlanet("Dogstar8", star, "Razor", "ice_giant", 320, 500, 17000, 525);
		Misc.initConditionMarket(Dogstar8);
		Dogstar8.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
		Dogstar8.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		Dogstar8.getMarket().addCondition(Conditions.EXTREME_WEATHER);
		Dogstar8.getMarket().addCondition(Conditions.VERY_COLD);
		Dogstar8.getMarket().addCondition(Conditions.POOR_LIGHT);
		Dogstar8.getMarket().addCondition(Conditions.ORE_SPARSE);
		Dogstar8.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);			
		Dogstar8.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
		
		SectorEntityToken dogstar_basefield1 = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldTerrainPlugin.MagneticFieldParams(500f, // terrain effect band width
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
		dogstar_basefield1.setCircularOrbit(star, 0, 0, 100);
		
        // Dogstar9, debris, asteroids and derelicts
		addDerelict(system, Dogstar4, "JYD_lean_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 175f, (Math.random()<0.6));
		addDerelict(system, Dogstar4, "JYD_relaxed_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 100f, (Math.random()<0.6));
		addDerelict(system, Dogstar4, "JYD_short_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));
		addDerelict(system, Dogstar4, "JYD_irksome_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));
		addDerelict(system, Dogstar4, "JYD_lively_standard", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));
		addDerelict(system, Dogstar6, "falcon_p_Strike", ShipRecoverySpecial.ShipCondition.BATTERED, 270f,  (Math.random()<0.6));
		addDerelict(system, Dogstar6, "wolf_d_pirates_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 270f,  (Math.random()<0.6));
		addDerelict(system, Dogstar7, "hammerhead_Support", ShipRecoverySpecial.ShipCondition.BATTERED, 270f,  (Math.random()<0.6));
		addDerelict(system, Dogstar7, "lasher_PD", ShipRecoverySpecial.ShipCondition.BATTERED, 125f, (Math.random()<0.6));
		addDerelict(system, Dogstar8, "medusa_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 125f, (Math.random()<0.6));
		addDerelict(system, Dogstar8, "brawler_tritachyon_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, 85f, (Math.random()<0.6));
		addDerelict(system, Dogstar8, "dram_Light", ShipRecoverySpecial.ShipCondition.BATTERED, 100f, (Math.random()<0.6));

        PlanetAPI Dogstar9 = system.addPlanet("Dogstar9", star, "Kong", "barren-bombarded", 210, 40, 10850, 300);
        Dogstar9.setCustomDescriptionId("planet_dogstar9");      
            
            Misc.initConditionMarket(Dogstar9);
            Dogstar9.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
            Dogstar9.getMarket().addCondition(Conditions.LOW_GRAVITY);
            Dogstar9.getMarket().addCondition(Conditions.METEOR_IMPACTS);
			Dogstar9.getMarket().addCondition(Conditions.ORE_SPARSE);			
            Dogstar9.getMarket().addCondition(Conditions.VOLATILES_TRACE);
			
		addDerelict(system, Dogstar9, "hound_hegemony_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));
		addDerelict(system, Dogstar9, "kite_pirates_Raider", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, (Math.random()<0.6));
		
            SectorEntityToken scrap4 = DerelictThemeGenerator.addSalvageEntity(system, Entities.SUPPLY_CACHE, Factions.DERELICT);
            scrap4.setId("dogstar_scrap4");
            scrap4.setCircularOrbit(Dogstar9, 105, 240, 135);
            Misc.setDefenderOverride(scrap4, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
            scrap4.setDiscoverable(Boolean.TRUE);
			
		SectorEntityToken Dogfield1 = system.addTerrain(Terrain.ASTEROID_FIELD, 
				new AsteroidFieldTerrainPlugin.AsteroidFieldParams(500f, 700f,	30,	40, 4f,	24f, 
				txt("jyd_asteroid1"))); 
		
		SectorEntityToken Dogfield2 = system.addTerrain(Terrain.ASTEROID_FIELD, 
				new AsteroidFieldTerrainPlugin.AsteroidFieldParams(500f, 700f,	30,	40, 4f,	24f, 
				txt("jyd_asteroid2"))); 	
						
 		Dogfield1.setCircularOrbit(Dogstar9, 180+45+60, 18, 5);
		Dogfield2.setCircularOrbit(Dogstar2, 180+45-60, 18, 5);                   
			
            DebrisFieldTerrainPlugin.DebrisFieldParams params3 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                700f, // field radius - should not go above 1000 for performance reasons
                1.0f, // density, visual - affects number of debris pieces
		10000000f, // duration in days 
		0f); // days the field will keep generating glowing pieces
            params3.source = DebrisFieldTerrainPlugin.DebrisFieldSource.SALVAGE;
            params3.baseSalvageXP = 900; // base XP for scavenging in field
            SectorEntityToken debrisL4 = Misc.addDebrisField(system, params3, StarSystemGenerator.random);
            debrisL4.setSensorProfile(600f);
            debrisL4.setDiscoverable(true);
            debrisL4.setCircularOrbit(star, 210f, 10800, 300f);
            debrisL4.setId("dogstar_debris5");
 		

		system.addRingBand(Dogstar8, "misc", "rings_special0", 256f, 1, new Color(225,215,255,200), 128f, 750, 60f, Terrain.RING, "The Rings of Razor");

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