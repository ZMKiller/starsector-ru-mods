package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.ORA_txt.txt;

public class ORA_godunov implements SectorGeneratorPlugin {

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem(txt("gdnv_system")).getEntityById("gdnv_starA");
    }

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem(txt("gdnv_system"));
        system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "ora_system_major");
        system.setBackgroundTextureFilename("graphics/ORA/backgrounds/ora_godunov.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("gdnv_starA", // unique id for this star
                "ora_godunovStar", // id in planets.json
                350f,
                150);		// radius (in pixels at default zoom)
        system.setLightColor(new Color(255, 220, 200)); // light color in entire system, affects all entities
        star.setCustomDescriptionId("ora_godunovStar");

        system.getLocation().set(-15500, 6000);

        SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldTerrainPlugin.MagneticFieldParams(800f, // terrain effect band width 
                        600, // terrain effect middle radius
                        star, // entity that it's around
                        100f, // visual band start
                        1100f, // visual band end
                        new Color(200, 50, 20, 70), // base color
                        0.25f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(110, 120, 20, 130),
                        new Color(120, 150, 30, 150),
                        new Color(200, 130, 50, 190),
                        new Color(250, 150, 70, 240),
                        new Color(200, 130, 80, 255),
                        new Color(75, 160, 0, 255),
                        new Color(127, 255, 5, 255)
                ));
        field.setCircularOrbit(star, 0, 0, 150);

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
        PlanetAPI gdnv1 = system.addPlanet("gdnv_1", star, txt("gdnv_planetA"), "lava_minor", 75, 120, 1250, 100);

        //2500
        //ASTEROID BELT
        system.addAsteroidBelt(star, 250, 2000, 256, 190, 210);

        //3000
        PlanetAPI gdnv2 = system.addPlanet("ora_poincare", star, txt("gdnv_planetB"), "terran-eccentric", 270, 175, 3000, 250);
        gdnv2.setCustomDescriptionId("ora_homeworld");
        gdnv2.setInteractionImage("illustrations", "ora_homeworld");

        SectorEntityToken gdnv_station1 = system.addCustomEntity("ora_devaney",
                txt("gdnv_stationB"),
                "ora_devaney_type",
                "ORA");
        gdnv_station1.setCircularOrbitPointingDown(gdnv2, 62, 300, 20);
//        gdnv_station1.setCustomDescriptionId("ora_hub");
//	gdnv_station1.setInteractionImage("illustrations", "urban02");

        SectorEntityToken gdnv2_shade1 = system.addCustomEntity("gdnv_shade1", txt("gdnv_shadeB1"), "stellar_shade", "neutral");
        gdnv2_shade1.setCircularOrbitPointingDown(gdnv2, 90, 500, 250);
        gdnv2_shade1.setCustomDescriptionId("stellar_shade");

        SectorEntityToken gdnv2_shade2 = system.addCustomEntity("gdnv_shade2", txt("gdnv_shadeB2"), "stellar_shade", "neutral");
        gdnv2_shade2.setCircularOrbitPointingDown(gdnv2, 83, 500, 250);
        gdnv2_shade2.setCustomDescriptionId("stellar_shade");

        SectorEntityToken gdnv2_shade3 = system.addCustomEntity("gdnv_shade3", txt("gdnv_shadeB3"), "stellar_shade", "neutral");
        gdnv2_shade3.setCircularOrbitPointingDown(gdnv2, 97, 500, 250);
        gdnv2_shade3.setCustomDescriptionId("stellar_shade");

        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("gdnv_jumpPointA", txt("gdnv_jp1"));
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 270 + 60, 1750, 150);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(gdnv2);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);

        //RELAY
        SectorEntityToken relay = system.addCustomEntity("gdnv_relay", txt("gdnv_relay"), // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "ORA"); // faction
        relay.setCircularOrbit(star, 270 - 60, 3000, 250);

        SectorEntityToken buoy = system.addCustomEntity("gdnv_buoy", txt("gdnv_buoy"), // name - if null, defaultName from custom_entities.json will be used
                "nav_buoy", // type of object, defined in custom_entities.json
                "ORA"); // faction
        buoy.setCircularOrbit(star, 270 - 180, 3100, 250);

        SectorEntityToken array = system.addCustomEntity("gdnv_sensor", txt("gdnv_sensor"), // name - if null, defaultName from custom_entities.json will be used
                "sensor_array", // type of object, defined in custom_entities.json
                "ORA"); // faction
        array.setCircularOrbit(star, 270 + 60, 3200, 250);

        //7000
        //JUMP POINT
        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("gdnv_jumpPointB", txt("gdnv_jp2"));
        OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(star, 35, 5000, 700);
        jumpPoint2.setOrbit(orbit2);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint2);

        //TWIN GIANTS
        PlanetAPI gdnv3 = system.addPlanet("gdnv_3", jumpPoint2, txt("gdnv_planetC"), "ice_giant", 180, 200, 600, 30);
        gdnv3.setSkipForJumpPointAutoGen(true);
        PlanetAPI gdnv4 = system.addPlanet("gdnv_4", jumpPoint2, txt("gdnv_planetD"), "gas_giant", 0, 300, 400, 30);
        gdnv4.setSkipForJumpPointAutoGen(true);

        jumpPoint2.setRelatedPlanet(gdnv3);

        //LAGRANGE STATION
        SectorEntityToken gdnv_station2 = system.addCustomEntity("ora_pendulum",
                txt("gdnv_stationC"),
                "ora_pendulum_type",
                "ORA");
        gdnv_station2.setCircularOrbitPointingDown(jumpPoint2, 180, 275, 30);

        //LAGRANGE ASTEROIDS
        SectorEntityToken asteroidField1 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, txt("gdnv_fieldL4"))); // null for default name
        asteroidField1.setCircularOrbit(star, 35 + 60, 5000, 700);

        SectorEntityToken wreck = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_MINING, Factions.DERELICT);
        wreck.setId("godunov_remains");
        wreck.setCircularOrbit(star, 35 + 60, 5000, 700);
        Misc.setDefenderOverride(wreck, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
        wreck.setDiscoverable(Boolean.TRUE);

        SectorEntityToken asteroidField2 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, txt("gdnv_fieldL4"))); // null for default name
        asteroidField2.setCircularOrbit(star, 35 - 60, 5000, 700);

        SectorEntityToken gate = system.addCustomEntity("gdnv_gate", txt("gdnv_gate"), // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                null); // faction
        gate.setCircularOrbit(star, 35 - 60, 5000, 700);

//        //15000
//        PlanetAPI gdnv5 = system.addPlanet("gdnv_5", star, "Smoosh", "frozen", 80, 75, 14000, 1700);    
//        //ASTEROIDS
//        system.addAsteroidBelt(gdnv5, 50, 600, 128, 39, 45);
        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                2, 4, // min/max entities to add
                7000, // radius to start adding at 
                4, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

        PlanetAPI gdnv6 = system.addPlanet("ora_3bodies", star, txt("gdnv_planetE"), "cryovolcanic", 220, 150, 22000, 3200);
        gdnv6.setCustomDescriptionId("ora_3bodies");
        gdnv6.setInteractionImage("illustrations", "ora_iceOutpost");
        system.addRingBand(gdnv6, "misc", "ora_ringsI", 256, 1, Color.WHITE, 256, 550, 30);
        system.addRingBand(gdnv6, "misc", "ora_ringsI", 256, 1, Color.WHITE, 256, 650, 40);

        system.autogenerateHyperspaceJumpPoints(true, true);
        MagicCampaign.hyperspaceCleanup(system);
    }
}
