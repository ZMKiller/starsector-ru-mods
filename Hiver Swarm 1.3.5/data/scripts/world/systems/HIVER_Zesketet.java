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
import static data.scripts.util.HIVER_txt.txt;

public class HIVER_Zesketet {	

	public void generate(SectorAPI sector) {
		
		
		StarSystemAPI system = sector.createStarSystem("Zesketet");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/HIVER/backgrounds/Zesketet_background.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("Zesketet", // unique id for this star
										    "star_white",  // id in planets.json
										    575f, 		  // radius (in pixels at default zoom)
										    460, // corona
										    12f, // solar wind burn level
											0.65f, // flare probability
											2.5f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(245, 230, 235)); // light color in entire system, affects all entities
		system.getLocation().set(-75000, 35000);

		PlanetAPI Zesketet1 = system.addPlanet("Zesketet1", star, "Hive", "rocky_metallic", 900, 250, 3100, 260);
		Misc.initConditionMarket(Zesketet1);
		Zesketet1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Zesketet1.getMarket().addCondition(Conditions.LOW_GRAVITY);
		Zesketet1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
		Zesketet1.getMarket().addCondition(Conditions.IRRADIATED);
		Zesketet1.getMarket().addCondition(Conditions.VERY_HOT);
		Zesketet1.getMarket().addCondition(Conditions.ORE_RICH);
		Zesketet1.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);	

		PlanetAPI Zesketet2 = system.addPlanet("Zesketet2", star, "Isketot", "rocky_unstable", 420, 165, 2000, 185);
		Misc.initConditionMarket(Zesketet2);
		Zesketet2.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Zesketet2.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
		Zesketet2.getMarket().addCondition(Conditions.IRRADIATED);
		Zesketet2.getMarket().addCondition(Conditions.VERY_HOT);
		Zesketet2.getMarket().addCondition(Conditions.ORE_RICH);		
		Zesketet2.getMarket().addCondition(Conditions.RARE_ORE_RICH);	

		PlanetAPI Zesketet3 = system.addPlanet("Zesketet3", star, "Chozanti", "jungle", 200, 385, 14800, 290);
		Zesketet3.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Zesketet3.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		Zesketet3.getSpec().setUseReverseLightForGlow(true);
		Zesketet3.applySpecChanges();
		Zesketet3.setCustomDescriptionId("planet_chozanti");
		Zesketet3.setInteractionImage("illustrations", "HIVER_diplomatic");

		PlanetAPI Zesketet3a = system.addPlanet("Zesketet3a", star, "Xhezek", "lava_minor", 1520, 345, 3780, 132);
		Misc.initConditionMarket(Zesketet3a);
		Zesketet3a.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		Zesketet3a.getMarket().addCondition(Conditions.HOT);
		Zesketet3a.getMarket().addCondition(Conditions.LOW_GRAVITY);
		Zesketet3a.getMarket().addCondition(Conditions.ORE_SPARSE);			

		system.addAsteroidBelt(star, 400, 6800, 512, 550, 600, Terrain.ASTEROID_BELT, "The Belt");
		system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 6800, 550f);

		PlanetAPI Zesketet4 = system.addPlanet("Zesketet4", star, "Comb", "arid", 320, 55, 7900, 550);
		Zesketet4.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Zesketet4.getSpec().setGlowColor(new Color(100, 100, 255, 255));
		Zesketet4.getSpec().setUseReverseLightForGlow(true);
		Zesketet4.applySpecChanges();
		Zesketet4.setCustomDescriptionId("planet_comb");
		Zesketet4.setInteractionImage("illustrations", "HIVER_hanger");		

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
            debrisBelt1.setId("Zesketet_debris1");
                
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
            debrisBelt2.setId("Zesketet_debris2");
			
		SectorEntityToken Zesketet5 = system.addCustomEntity("Zesketet5", "Zytokot", "station_lowtech2", "HIVER");
		Zesketet5.setCircularOrbitPointingDown(star, 140, 7800, 550);
		Zesketet5.setCustomDescriptionId("Zytokot_desc");
		Zesketet5.setInteractionImage("illustrations", "orbital");


		PlanetAPI Zesketet6 = system.addPlanet("Zesketet6", star, "Ipriskin", "toxic", 2000, 125, 12550, 210);
		Zesketet6.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Zesketet6.getSpec().setGlowColor(new Color(255, 255, 255, 255));
		Zesketet6.getSpec().setUseReverseLightForGlow(true);
		Zesketet6.applySpecChanges();
		Zesketet6.setCustomDescriptionId("planet_Ipriskin");
		Zesketet6.setInteractionImage("illustrations", "HIVER_hanger");
		
		
        // Outer belt.
        system.addRingBand(star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 15200, 450f, Terrain.RING, "Outer Band");

        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.gray, 256f, 15580, 453f, null, null);
        system.addRingBand(star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 15620, 461f, null, null);
        system.addAsteroidBelt(star, 150, 15600, 170, 200, 520, Terrain.ASTEROID_BELT, "Outer Belt");		

        //Salvage
        SectorEntityToken scrap1 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_SMALL_REMNANT, Factions.DERELICT);
        scrap1.setId("Zesketet_scrap1");
		scrap1.setCircularOrbit(star, 165, 6420, 223);
		Misc.setDefenderOverride(scrap1, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap1.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap2 = DerelictThemeGenerator.addSalvageEntity(system, Entities.EQUIPMENT_CACHE_SMALL, Factions.DERELICT);
        scrap2.setId("Zesketet_scrap2");
		scrap2.setCircularOrbit(star, 285, 6420, 223);
		Misc.setDefenderOverride(scrap2, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap2.setDiscoverable(Boolean.TRUE);
        
        SectorEntityToken scrap3 = DerelictThemeGenerator.addSalvageEntity(system, Entities.WEAPONS_CACHE_REMNANT, Factions.DERELICT);
        scrap3.setId("Zesketet_scrap3");
	scrap3.setCircularOrbit(star, 45, 6480, 223);
	Misc.setDefenderOverride(scrap3, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        scrap3.setDiscoverable(Boolean.TRUE);


		// Relays
		SectorEntityToken Zesketet_relay = system.addCustomEntity("Zesketet_relay", // unique id
				"Zesketet Relay", // name - if null, defaultName from custom_entities.json will be used
				"comm_relay_makeshift", // type of object, defined in custom_entities.json
				"HIVER"); // faction
		Zesketet_relay.setCircularOrbitPointingDown(star, 300, 4500, 700);

		SectorEntityToken Zesketet_buoy = system.addCustomEntity("Zesketet_buoy", // unique id
				"Zesketet Nav Buoy", // name - if null, defaultName from custom_entities.json will be used
				"nav_buoy_makeshift", // type of object, defined in custom_entities.json
				"HIVER"); // faction
		Zesketet_buoy.setCircularOrbitPointingDown(star, 120, 5500, 700);

		SectorEntityToken Zesketet_sensor = system.addCustomEntity("Zesketet_sensor", // unique id
				"Zesketet Sensor Array", // name - if null, defaultName from custom_entities.json will be used
				"sensor_array_makeshift", // type of object, defined in custom_entities.json
				"HIVER"); // faction
		Zesketet_sensor.setCircularOrbitPointingDown(star, 60, 8000, 1000);

		JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("Zesketet_jump_point1", "Processing Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 140, 8200, 550);
		jumpPoint.setOrbit(orbit);
		orbit.setEntity(jumpPoint);
		jumpPoint.setRelatedPlanet(Zesketet6);
		jumpPoint.setStandardWormholeToHyperspaceVisual();
		system.addEntity(jumpPoint);

		PlanetAPI Zesketet7 = system.addPlanet("Zesketet7", star, "Chepren", "gas_giant", 360, 450, 8800,6580);
		Misc.initConditionMarket(Zesketet7);
		Zesketet7.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Zesketet7.getSpec().setGlowColor(new Color(100, 100, 255, 255));
		Zesketet7.getSpec().setUseReverseLightForGlow(true);
		Zesketet7.applySpecChanges();
		Zesketet7.setCustomDescriptionId("planet_chepren");
		Zesketet7.setInteractionImage("illustrations", "HIVER_hanger");		


		PlanetAPI Zesketet8 = system.addPlanet("Zesketet8", star, "Kiztac", "ice_giant", 320, 700, 17000, 525);
		Misc.initConditionMarket(Zesketet8);
		Zesketet8.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		Zesketet8.getSpec().setGlowColor(new Color(100, 100, 255, 255));
		Zesketet8.getSpec().setUseReverseLightForGlow(true);
		Zesketet8.applySpecChanges();
		Zesketet8.setCustomDescriptionId("planet_Kiztac");
		Zesketet8.setInteractionImage("illustrations", "HIVER_hanger");
		
		SectorEntityToken Zesketet_basefield1 = system.addTerrain(Terrain.MAGNETIC_FIELD,
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
		Zesketet_basefield1.setCircularOrbit(star, 0, 0, 100);
		


        PlanetAPI Zesketet9 = system.addPlanet("Zesketet9", star, "Nest", "barren-bombarded", 210, 40, 10850, 300);
        Zesketet9.setCustomDescriptionId("planet_Zesketet9"); 
		Zesketet9.setInteractionImage("illustrations", "HIVER_hanger");		
            
            Misc.initConditionMarket(Zesketet9);
            Zesketet9.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
            Zesketet9.getMarket().addCondition(Conditions.LOW_GRAVITY);
            Zesketet9.getMarket().addCondition(Conditions.METEOR_IMPACTS);
			Zesketet9.getMarket().addCondition(Conditions.ORE_SPARSE);			
            Zesketet9.getMarket().addCondition(Conditions.VOLATILES_TRACE);
			

		
            SectorEntityToken scrap4 = DerelictThemeGenerator.addSalvageEntity(system, Entities.SUPPLY_CACHE, Factions.DERELICT);
            scrap4.setId("Zesketet_scrap4");
            scrap4.setCircularOrbit(Zesketet9, 105, 240, 135);
            Misc.setDefenderOverride(scrap4, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
            scrap4.setDiscoverable(Boolean.TRUE);
			
		SectorEntityToken Bugfield1 = system.addTerrain(Terrain.ASTEROID_FIELD, 
new AsteroidFieldTerrainPlugin.AsteroidFieldParams(500f, 700f,    30,    40, 4f,    24f, "Nest asteroid field")); 
		
		SectorEntityToken Bugfield2 = system.addTerrain(Terrain.ASTEROID_FIELD, 
                new AsteroidFieldTerrainPlugin.AsteroidFieldParams(500f, 700f,    30,    40, 4f,    24f, "Isketot asteroid field"));
						
 		Bugfield1.setCircularOrbit(Zesketet9, 180+45+60, 18, 5);
		Bugfield2.setCircularOrbit(Zesketet2, 180+45-60, 18, 5);                   
			
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
            debrisL4.setId("Zesketet_debris5");
 		

		system.addRingBand(Zesketet8, "misc", "rings_special0", 256f, 1, new Color(225,215,255,200), 128f, 750, 60f, Terrain.RING, "The Rings of Razor");

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