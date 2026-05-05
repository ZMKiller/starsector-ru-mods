package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.ORA_txt.txt;

public class ORA_groom implements SectorGeneratorPlugin {

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem(txt("grm_system")).getEntityById("grm_starA");
    }

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem(txt("grm_system"));        
        system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "ora_system_minor");        
        system.setBackgroundTextureFilename("graphics/ORA/backgrounds/ora_groom.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("grm_starA", // unique id for this star
                StarTypes.YELLOW, // id in planets.json
                400f,
                300f);		// radius (in pixels at default zoom)
        system.setLightColor(new Color(255, 220, 180)); // light color in entire system, affects all entities
//        star.setCustomDescriptionId("ora_godunovStar");        

        system.getLocation().set(-21000, 4000);

//        SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
//			new MagneticFieldTerrainPlugin.MagneticFieldParams(800f, // terrain effect band width 
//					600, // terrain effect middle radius
//					star, // entity that it's around
//					100f, // visual band start
//					1100f, // visual band end
//					new Color(200, 50, 20, 70), // base color
//					0.25f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
//					new Color(110, 120, 20, 130),
//					new Color(120, 150, 30, 150), 
//					new Color(200, 130, 50, 190),
//					new Color(250, 150, 70, 240),
//					new Color(200, 130, 80, 255),
//					new Color(75, 160, 0, 255), 
//					new Color(127, 255, 5, 255)
//					));
//        field.setCircularOrbit(star, 0, 0, 150);
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
        PlanetAPI grm1 = system.addPlanet("grm_1", star, txt("grm_planetA"), "ora_lava", 90, 150, 700, 10);
//        grm1.setCircularOrbitPointingDown(star, 0, 700, 10);
        grm1.setCustomDescriptionId("ora_cureus");

        PlanetAPI grm2 = system.addPlanet("grm_2", star, txt("grm_planetB"), "irradiated", 120, 100, 2000, 200);

        SectorEntityToken grm_station0 = system.addCustomEntity("ora_haunted",
                txt("grm_stationB"),
                "station_side00",
                "tritachyon");
        grm_station0.setCircularOrbitPointingDown(grm2, 33, 225, 35);
        grm_station0.setCustomDescriptionId("ora_haunted");
        grm_station0.setInteractionImage("illustrations", "urban00");

        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("grm_jumpPointA", txt("grm_jp1"));
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 180, 2000, 2000);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(grm2);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);

//        //2500
//        //ASTEROID BELT
//        system.addAsteroidBelt(star, 250, 2700, 256, 190, 210);  
        //3000
        PlanetAPI grm3 = system.addPlanet("ora_camillia", star, txt("grm_planetC"), "ora_water", 270, 175, 5000, 600);
        grm3.setCustomDescriptionId("ora_camillia");
        grm3.setInteractionImage("illustrations", "ora_freighters");

        PlanetAPI grm31 = system.addPlanet("grm_31", grm3, txt("grm_planetC1"), "barren", 180, 50, 500, 50);

        SectorEntityToken grm_station1 = system.addCustomEntity("ora_dock",
                txt("grm_stationC"),
                "ora_station_side01",
                "independent");
        grm_station1.setCircularOrbitPointingDown(grm3, -33, 250, 15);
        grm_station1.setCustomDescriptionId("ora_dock");
        grm_station1.setInteractionImage("illustrations", "space_bar");

        system.addRingBand(star, "misc", "ora_ringsD", 1024, 0, Color.WHITE, 1024, 7000, 725);
        system.addRingBand(star, "misc", "ora_ringsD", 1024, 0, Color.WHITE, 1024, 7250, 750);
        system.addRingBand(star, "misc", "ora_ringsD", 1024, 0, Color.WHITE, 1024, 7500, 775);

        system.addAsteroidBelt(star, 50, 7250, 1024, 725, 775);

        PlanetAPI grm4 = system.addPlanet("grm_4", star, txt("grm_planetD"), "rocky_ice", 35, 310, 8500, 1000);

        SectorEntityToken derelict = DerelictThemeGenerator.addSalvageEntity(system, Entities.ORBITAL_HABITAT, Factions.DERELICT);
        derelict.setId("groom_derelict");
        derelict.setCircularOrbitPointingDown(grm4, 200, 500, 40f);
        Misc.setDefenderOverride(derelict, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        CargoAPI extraProbeSalvage = Global.getFactory().createCargo(true);
        extraProbeSalvage.addCommodity(Commodities.DRUGS, 127);
        BaseSalvageSpecial.addExtraSalvage(extraProbeSalvage, derelict.getMemoryWithoutUpdate(), -1);

        //RELAY
        SectorEntityToken relay = system.addCustomEntity("grm_relay", // unique id
                null, // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "neutral"); // faction
        relay.setCircularOrbit(star, 95, 8500, 1000);

        //GIANT
        PlanetAPI grm5 = system.addPlanet("grm_5", star, txt("grm_planetE"), "gas_giant", 275, 450, 14000, 2500);

        //STATION
        SectorEntityToken grm_station2 = system.addCustomEntity("ora_portia",
                txt("grm_stationE"),
                "station_side03",
                "pirates");
        grm_station2.setCircularOrbitPointingDown(grm5, 180, 550, 15);
        grm_station2.setCustomDescriptionId("ora_portia");
        grm_station2.setInteractionImage("illustrations", "ora_orbital");

        SectorEntityToken locationA = system.addCustomEntity("grm_stableA", null, "stable_location", "neutral");
        locationA.setCircularOrbit(grm5, 64, 1580, 45);

        system.addRingBand(grm5, "misc", "ora_rings4", 512f, 0, new Color(1, 1, 1, 0.25f), 512f, 1050, 75f);
        system.addRingBand(grm5, "misc", "ora_rings4", 512f, 1, new Color(1, 1, 1, 0.25f), 512f, 950, 85f, Terrain.RING, txt("grm_ring"));

        //LAGRANGE ASTEROIDS
        SectorEntityToken asteroidField1 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, // max asteroid radius
                        txt("grm_fieldL5"))); // null for default name
        asteroidField1.setCircularOrbit(star, 275 + 60, 14000, 2500);

        SectorEntityToken asteroidField2 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, // max asteroid radius
                        txt("grm_fieldL4"))); // null for default name
        asteroidField2.setCircularOrbit(star, 275 - 60, 14000, 2500);

        SectorEntityToken gate = system.addCustomEntity("grm_gate", // unique id
                txt("grm_gate"), // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                null); // faction
        gate.setCircularOrbit(star, 275 + 60, 14000, 2500);

        SectorEntityToken grm_station3 = system.addCustomEntity("ora_caretaker",
                txt("grm_station1"),
                "station_pirate_type",
                "pirates");
        grm_station3.setCircularOrbitPointingDown(star, 275 - 60, 14000, 2500);
        grm_station3.setCustomDescriptionId("ora_caretaker");
        grm_station3.setInteractionImage("illustrations", "pirate_station");

        grm_station3.setDiscoverable(Boolean.TRUE);
        grm_station3.setSensorProfile(1000f);
        grm_station3.setDiscoveryXP(750f);

        grm_station3.getMemoryWithoutUpdate().set("$abandonedStation", true);
        MarketAPI market = Global.getFactory().createMarket("ora_caretakerMarket", txt("grm_station1m"), 1);
        market.setPrimaryEntity(grm_station3);
        market.setFactionId(grm_station3.getFaction().getId());
        market.addCondition(Conditions.DECIVILIZED);
        market.addCondition(Conditions.FRONTIER);
        market.addIndustry("population");
        market.addIndustry("spaceport");

        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        grm_station3.setMarket(market);

        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                1, 3, // min/max entities to add
                17000, // radius to start adding at 
                5, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

        system.autogenerateHyperspaceJumpPoints(true, true);
        MagicCampaign.hyperspaceCleanup(system);
    }
}
