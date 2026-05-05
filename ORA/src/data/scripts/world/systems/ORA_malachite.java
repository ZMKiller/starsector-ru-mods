    package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.ORA_txt.txt;

public class ORA_malachite implements SectorGeneratorPlugin {

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem(txt("mlct_system")).getEntityById("mlct_starA");
    }

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem(txt("mlct_system"));        
        system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "ora_system_major");        
        system.setBackgroundTextureFilename("graphics/ORA/backgrounds/ora_malachite.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("mlct_starA", // unique id for this star
                                        StarTypes.WHITE_DWARF, // id in planets.json
                                        250f,
                                        250);		// radius (in pixels at default zoom)
	system.setLightColor(new Color(245, 250, 255)); // light color in entire system, affects all entities

	system.getLocation().set(-20000, 8500);                
        
        /*
         * addPlanet() parameters:
         * 1. What the planet orbits (orbit is always circular)
         * 2. Name
         * 3. Planet type id in planets.json
         * 4. Starting angle in orbit, i.e. 0 = to the right of the star
         * 5. Planet radius, pixels at default zoom
         * 6. Orbit radius, pixels at default zoom
         * 7. Days it takes to complete an orbit. 1 day = 10 seconds.
         */
        /*
         * addAsteroidBelt() parameters:
         * 1. What the belt orbits
         * 2. Number of asteroids
         * 3. Orbit radius
         * 4. Belt width
         * 6/7. Range of days to complete one orbit. Value picked randomly for each asteroid. 
         */
        /*
         * addRingBand() parameters:
         * 1. What it orbits
         * 2. Category under "graphics" in settings.json
         * 3. Key in category
         * 4. Width of band within the texture
         * 5. Index of band
         * 6. Color to apply to band
         * 7. Width of band (in the game)
         * 8. Orbit radius (of the middle of the band)
         * 9. Orbital period, in days
         */        
        
        //2000
        PlanetAPI mlcht0 = system.addPlanet("mlcht_0", star, txt("mlct_planetA"), "barren-bombarded", 60, 60, 2000, 110);

        //4500
        PlanetAPI mlcht1 = system.addPlanet("ora_marble", star, txt("mlct_planetB"), "tundra", 160, 220, 4500, 300);
        mlcht1.setCustomDescriptionId("ora_marble");
        
        PlanetAPI mlcht11 = system.addPlanet("mlcht_11", mlcht1, txt("mlct_planetB1"), "barren", 60, 50, 600, 30);  
        mlcht11.setCustomDescriptionId("ora_amethyst"); 
        
        SectorEntityToken mlcht_station1 = system.addCustomEntity("ora_spore",
                txt("mlct_stationB"),
                "ora_sporeship",
                "ORA");
        mlcht_station1.setCircularOrbitPointingDown(mlcht1, 240, 300, 30);
        
        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("gdnv_jumpPointA", txt("mlct_jp1"));
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 220, 4500, 300);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(mlcht1);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);
        
        //6000
        PlanetAPI mlcht2 = system.addPlanet("mlcht_2", star, txt("mlct_planetC"), "irradiated", 30, 115, 6000, 600);
        
        SectorEntityToken mlcht_station2 = system.addCustomEntity("ora_refinery",
                txt("mlct_stationC"),
                "station_pirate_type",
                "neutral");
        mlcht_station2.setCircularOrbitPointingDown(mlcht2, 120, 300, 40);
        mlcht_station2.setCustomDescriptionId("ora_refinery");
        mlcht_station2.setInteractionImage("illustrations", "abandoned_station3");
        
        DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
				250f, // field radius - should not go above 1000 for performance reasons
				1f, // density, visual - affects number of debris pieces
				10000000f, // duration in days 
				0f); // days the field will keep generating glowing pieces
		params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.SALVAGE;
		params.baseSalvageXP = 250; // base XP for scavenging in field
		SectorEntityToken dilapidatedDebris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
		dilapidatedDebris.setSensorProfile(0.2f);
		dilapidatedDebris.setDiscoverable(true);
		dilapidatedDebris.setCircularOrbit(mlcht2, 120, 300, 40);
		dilapidatedDebris.setId("malachite_stationDebris");
        
        //9000
        //ASTEROID BELT
        system.addAsteroidBelt(star, 300, 9000, 1024, 1900, 2100);  
        
        //RELAY
        SectorEntityToken relay = system.addCustomEntity("mlcht_relay", txt("mlct_relay"), // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "ORA"); // faction
        relay.setCircularOrbit(star, 255, 9000, 2000);
        
        SectorEntityToken locationA = system.addCustomEntity("mlcht_stableA", null, "stable_location", "neutral");
        locationA.setCircularOrbit(star, 255+120, 9000, 2000);
        
        SectorEntityToken locationB = system.addCustomEntity("mlcht_stableB", null, "stable_location", "neutral");
        locationB.setCircularOrbit(star, 255-120, 9000, 2000);
        
        //CACHES
        SectorEntityToken ast1 = system.addCustomEntity("mlcht_ast1", txt("mlct_POI1"), // name - if null, defaultName from custom_entities.json will be used
                "ora_asteroid_1", // type of object, defined in custom_entities.json
                "neutral"); // faction
        ast1.setCircularOrbit(star, 85, 9000, 2000);
        ast1.setCustomDescriptionId("ora_cache1");
	ast1.setInteractionImage("illustrations", "ora_cache"); 
        
        ast1.setDiscoverable(Boolean.TRUE);
        ast1.setSensorProfile(0.4f);
        ast1.setDiscoveryXP(1000f);
        
            ast1.getMemoryWithoutUpdate().set("$abandonedStation", true);
            MarketAPI market1 = Global.getFactory().createMarket("mlcht_ast1_market", txt("mlct_POI1m"), 0);
            market1.setPrimaryEntity(ast1);
            market1.setFactionId(ast1.getFaction().getId());
            market1.addCondition(Conditions.ABANDONED_STATION);
            market1.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            market1.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            ((StoragePlugin)market1.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            ast1.setMarket(market1);
            ast1.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "dominator_d_Assault", "End of the Line");
            ast1.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("supplies", 13); 
        
        SectorEntityToken ast2 = system.addCustomEntity("mlcht_ast2", txt("mlct_POI2"), // name - if null, defaultName from custom_entities.json will be used
                "ora_asteroid_2", // type of object, defined in custom_entities.json
                "neutral"); // faction
        ast2.setCircularOrbit(star, 295, 9000, 2000);
        ast2.setCustomDescriptionId("ora_cache2");
	ast2.setInteractionImage("illustrations", "ora_cache");   
         
        ast2.setDiscoverable(Boolean.TRUE);
        ast2.setSensorProfile(0.4f);
        ast2.setDiscoveryXP(750f);
        
        ast2.getMemoryWithoutUpdate().set("$abandonedStation", true);
        MarketAPI market2 = Global.getFactory().createMarket("mlcht_ast2_market", txt("mlct_POI2m"), 0);
        market2.setPrimaryEntity(ast2);
        market2.setFactionId(ast1.getFaction().getId());
        market2.addCondition(Conditions.ABANDONED_STATION);
        market2.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market2.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        ((StoragePlugin) market2.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
        ast2.setMarket(market2);
        ast2.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("supplies", 43);
        ast2.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("fuel", 24);
        ast2.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("hand_weapons", 62);
        ast2.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("drugs", 17);
        ast2.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("lobster", 8);
        ast2.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("organs", 11);
        
        SectorEntityToken ast3 = system.addCustomEntity("mlcht_ast3", txt("mlct_POI3"), // name - if null, defaultName from custom_entities.json will be used
                "ora_asteroid_3", // type of object, defined in custom_entities.json
                "neutral"); // faction
        ast3.setCircularOrbit(star, 230, 9000, 2000);
         
        ast3.setDiscoverable(Boolean.TRUE);
        ast3.setSensorProfile(0.2f);
        ast3.setDiscoveryXP(250f);
        
        SectorEntityToken ast4 = system.addCustomEntity("mlcht_ast4", txt("mlct_POI4"), // name - if null, defaultName from custom_entities.json will be used
                "ora_asteroid_3", // type of object, defined in custom_entities.json
                "neutral"); // faction
        ast4.setCircularOrbit(star, 185, 9000, 2000);
        ast4.setCustomDescriptionId("ora_cache3");
	ast4.setInteractionImage("illustrations", "ora_cache");   
         
        ast4.setDiscoverable(Boolean.TRUE);
        ast4.setSensorProfile(0.3f);
        ast4.setDiscoveryXP(500f);
        
            ast4.getMemoryWithoutUpdate().set("$abandonedStation", true);
            MarketAPI market3 = Global.getFactory().createMarket("mlcht_ast4_market", txt("mlct_POI4m"), 0);
            market3.setPrimaryEntity(ast4);
            market3.setFactionId(ast1.getFaction().getId());
            market3.addCondition(Conditions.ABANDONED_STATION);
            market3.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            market3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            ((StoragePlugin)market3.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            ast4.setMarket(market3);
            ast4.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "hermes_d_Standard", "ISS New Frontier");
            ast4.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "mercury_d_Standard", "ISS Reliable");
            ast4.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("supplies", 8); 
            ast4.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("fuel", 4);
            ast4.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("food", 3);
            ast4.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("domestic_goods", 17);    

        SectorEntityToken ast5 = system.addCustomEntity("mlcht_ast5", txt("mlct_POI5"), // name - if null, defaultName from custom_entities.json will be used
                "ora_asteroid_4", // type of object, defined in custom_entities.json
                "neutral"); // faction
        ast5.setCircularOrbit(star, 125, 9000, 2000);
         
        ast5.setDiscoverable(Boolean.TRUE);
        ast5.setSensorProfile(0.2f);
        ast5.setDiscoveryXP(250f);
        
        SectorEntityToken ast6 = system.addCustomEntity("mlcht_ast6", txt("mlct_POI6"), // name - if null, defaultName from custom_entities.json will be used
                "ora_asteroid_4", // type of object, defined in custom_entities.json
                "neutral"); // faction
        ast6.setCircularOrbit(star, 355, 9000, 2000);
         
        ast6.setDiscoverable(Boolean.TRUE);
        ast6.setSensorProfile(0.2f);
        ast6.setDiscoveryXP(250f);

        for(int i=0; i<6; i++){
            //battle debris
            DebrisFieldTerrainPlugin.DebrisFieldParams debrisParams = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                100f, // field radius - should not go above 1000 for performance reasons
                1f, // density, visual - affects number of debris pieces
                360f +(float)Math.random()*1500, // duration in days 
                0f); // days the field will keep generating glowing pieces
            debrisParams.source = DebrisFieldTerrainPlugin.DebrisFieldSource.SALVAGE;
            debrisParams.baseDensity = 1;
            debrisParams.density = 1;
            debrisParams.baseSalvageXP = 250;
            SectorEntityToken debrisWreckage = Misc.addDebrisField(system, debrisParams, StarSystemGenerator.random);
            debrisWreckage.setDiscoverable(true);
            debrisWreckage.setDiscoveryXP(250f);
            debrisWreckage.setCircularOrbit(
                    star,
                    360*(float)Math.random(),
                    9000,
                    2000
            );

//                            debrisWreckage.addDropRandom("freighter_cargo", 50, 200);
            debrisWreckage.addDropRandom("supply", 33, 1000);
            debrisWreckage.addDropRandom("weapons1", 25, 250);
            debrisWreckage.addDropRandom("weapons2", 10, 150);
            debrisWreckage.addDropRandom("any_hullmod_medium", 2, 1000);
//

            debrisWreckage.setId("ora_malachite_debris"+i); 
        }
        
        
        system.addRingBand(star, "misc", "ora_ringsC", 1024f, 0, new Color(170,200,255,100), 1024f, 9000, 1750f);

        system.addRingBand(star, "misc", "ora_ringsD", 1024f, 0, new Color(180,210,255,255), 1024f, 8900, 1500f);
        system.addRingBand(star, "misc", "ora_ringsD", 1024f, 0, new Color(180,230,255,255), 1024f, 9100, 2500f);

        system.addRingBand(star, "misc", "ora_ringsR", 1024f, 0, new Color(180,200,255,255), 1024f, 9000, 2250f, Terrain.RING, txt("mlct_ring1"));   

        SectorEntityToken gate = system.addCustomEntity("mlcht_gate", // unique id
                "Malachite Gate", // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                "neutral"); // faction
        gate.setCircularOrbit(star, 0, 9000, 2000);        
        
        PlanetAPI mlcht3 = system.addPlanet("mlcht_3", star, txt("mlct_planetD"), "rocky_ice", 290, 400, 13000, 4000);
        mlcht3.setCustomDescriptionId("ora_jade");
        system.addRingBand(mlcht3, "misc", "ora_ringsI", 256, 2, Color.WHITE, 256f, 500, 3f,Terrain.RING, txt("mlct_ring2"));
        system.addRingBand(mlcht3, "misc", "ora_ringsI", 256, 2, Color.WHITE, 256f, 425, 2f);
        SectorEntityToken mlcht_station3 = system.addCustomEntity("ora_collector",
                txt("mlct_stationD"),
                "station_pirate_type",
                "ORA");
        mlcht_station3.setCircularOrbitPointingDown(mlcht3, 62, 600, 4f);
        mlcht_station3.setCustomDescriptionId("ora_iceCollector");
	mlcht_station3.setInteractionImage("illustrations", "cargo_loading");    
        
//        PlanetAPI mlcht4 = system.addPlanet("mlcht_4", star, "Dirt", "ice_giant", 15, 600, 19000, 6000);
//        
//                //LAGRANGE ASTEROIDS
//        SectorEntityToken asteroidField1 = system.addTerrain(Terrain.ASTEROID_FIELD,
//						new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
//								700f, // min radius
//								1000f, // max radius
//								40, // min asteroid count
//								50, // max asteroid count
//								4f, // min asteroid radius 
//								24f, // max asteroid radius
//								"L5 Asteroid Field")); // null for default name
//	asteroidField1.setCircularOrbit(star, 75f, 19000, 6000);
//        
//        SectorEntityToken asteroidField2 = system.addTerrain(Terrain.ASTEROID_FIELD,
//						new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
//								700f, // min radius
//								1000f, // max radius
//								40, // min asteroid count
//								50, // max asteroid count
//								4f, // min asteroid radius 
//								24f, // max asteroid radius
//								"L4 Asteroid Field")); // null for default name
//	asteroidField2.setCircularOrbit(star, -45f, 19000, 6000);
        
        
        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
            2, 4, // min/max entities to add
            15000, // radius to start adding at 
            4, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
            true); // whether to use custom or system-name based names
        
        system.autogenerateHyperspaceJumpPoints(true, true);        
        MagicCampaign.hyperspaceCleanup(system);
    }
}