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
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.ORA_txt.txt;
import org.lazywizard.lazylib.MathUtils;

public class ORA_valis implements SectorGeneratorPlugin {

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem(txt("vls_system")).getEntityById("vls_starA");
    }

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem(txt("vls_system"));        
        system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "ora_system_minor");        
        system.setBackgroundTextureFilename("graphics/ORA/backgrounds/ora_valis.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("vls_starA", // unique id for this star
                StarTypes.BLUE_GIANT, // id in planets.json
                1000f,
                550f);		// radius (in pixels at default zoom)
        system.setLightColor(new Color(210, 230, 255)); // light color in entire system, affects all entities

        system.getLocation().set(-10000, 9000);

        SectorEntityToken vls_nebula = Misc.addNebulaFromPNG("data/campaign/terrain/ora_valis_nebula.png",
                0, 0, // center of nebula
                system, // location to add to
                "terrain", "ora_nebula", // "nebula_blue", // texture to use, uses xxx_map for map
                8, 8, StarAge.OLD); // number of cells in texture

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
        PlanetAPI vls0 = system.addPlanet("vls_0", star, txt("vls_planetA"), "lava_minor", 90, 50, 2000, 110);

        PlanetAPI vls1 = system.addPlanet("vls_1", star, txt("vls_planetB"), "toxic", 180, 100, 3000, 160);

        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("vls_jumpPointA", txt("vls_jp1"));
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 240, 3000, 160);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(vls1);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);

        //4500
        PlanetAPI vls2 = system.addPlanet("ora_chindi", star, txt("vls_planetC"), "arid", 200, 220, 5000, 250);
        vls2.setCustomDescriptionId("ora_chindi");
        vls2.setInteractionImage("illustrations", "eochu_bres");

        PlanetAPI vls21 = system.addPlanet("ora_spin", vls2, txt("vls_planetC1"), "barren-desert", 90, 30, 450, 30);
        vls21.setCustomDescriptionId("ora_spin");
        vls21.setInteractionImage("illustrations", "desert_moons_ruins");

        PlanetAPI vls22 = system.addPlanet("ora_vortex", vls2, txt("vls_planetC2"), "desert", 310, 50, 600, 60);
//        vls22.setCustomDescriptionId("ora_vortex"); 

        //6000
        PlanetAPI vls3 = system.addPlanet("ora_omale", star, txt("vls_planetD"), "rocky_metallic", 275, 180, 6000, 400);
        vls3.setCustomDescriptionId("ora_omale");
        vls3.setInteractionImage("illustrations", "mine");

        SectorEntityToken vls_station1 = system.addCustomEntity("ora_relay",
                txt("vls_stationD"),
                "ora_recycled_type",
                "neutral");
        vls_station1.setCircularOrbitPointingDown(vls3, 20, 250, 40);
        vls_station1.setCustomDescriptionId("ora_relay");
        vls_station1.setInteractionImage("illustrations", "abandoned_station2");

        vls_station1.getMemoryWithoutUpdate().set("$abandonedStation", true);
        MarketAPI market1 = Global.getFactory().createMarket("ora_relay_market", txt("vls_stationD"), 0);
        market1.setPrimaryEntity(vls_station1);
        market1.setFactionId(vls_station1.getFaction().getId());
        market1.addCondition(Conditions.ABANDONED_STATION);
        market1.addCondition(Conditions.THIN_ATMOSPHERE);
        market1.addCondition(Conditions.IRRADIATED);
        market1.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market1.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        ((StoragePlugin) market1.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
        vls_station1.setMarket(market1);

        //RELAY
        SectorEntityToken relay = system.addCustomEntity("vls_relay", // unique id
                null, // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "ORA"); // faction
        relay.setCircularOrbit(star, 275 + 60, 6000, 400);

//        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.YOUNG,
//                        3, 6, // min/max entities to add
//                        10000, // radius to start adding at 
//                        4, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
//                        false); // whether to use custom or system-name based names
        system.addRingBand(star, "misc", "ora_ringsD", 1024f, 0, new Color(255, 210, 180, 255), 1024f, 9500, 800f);
        system.addAsteroidBelt(star, 120, 9500, 1000, 800, 850, Terrain.ASTEROID_BELT, txt("vls_ring1"));

        PlanetAPI vls39 = system.addPlanet("ora_janus", star, txt("vls_planetE"), "rocky_ice", 310, 150, 12000, 1000);
        vls39.setCustomDescriptionId("ora_janus");
        vls39.setInteractionImage("illustrations", "mine");

        SectorEntityToken locationB = system.addCustomEntity("vls_stableB", null, "stable_location", "neutral");
        locationB.setCircularOrbit(star, 10, 12000, 1000);

        PlanetAPI companion = system.addPlanet("ora_deepsix",
                star,
                txt("vls_starB"),
                StarTypes.RED_SUPERGIANT,
                140,
                800,
                22000f,
                12000f);
        system.addCorona(companion, 1300, 5f, 0.5f, 1f);

        system.setType(StarSystemGenerator.StarSystemType.BINARY_FAR);
        system.setSecondary(companion);

//        system.addRingBand(star, "misc", "ora_ringsC", 1024f, 0, new Color(170,200,255,100), 1024f, 9000, 1750f);
        system.addRingBand(companion, "misc", "ora_ringsD", 1024f, 0, new Color(255, 210, 180, 255), 1024f, 1900, 175f);
        system.addRingBand(companion, "misc", "ora_ringsD", 1024f, 0, new Color(255, 230, 160, 255), 1024f, 2100, 225f);

        system.addRingBand(companion, "misc", "ora_ringsR", 1024f, 0, new Color(255, 200, 170, 255), 1024f, 2000, 200f);

        system.addAsteroidBelt(companion, 90, 2000, 500, 150, 250, Terrain.ASTEROID_BELT, txt("vls_ring2"));

        PlanetAPI vls4 = system.addPlanet("ora_tschai", companion, txt("vls_planetF"), "tundra", 200, 200, 3500, 300);
        vls4.setCustomDescriptionId("ora_tschai");
        vls4.setInteractionImage("illustrations", "urban00");

        //JUMP POINT
        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("vls_jumpPointB", txt("vls_jp2"));
        jumpPoint2.setOrbit(Global.getFactory().createCircularOrbit(companion, 260, 3500, 300));
        jumpPoint2.setRelatedPlanet(vls4);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint2);

        SectorEntityToken locationA = system.addCustomEntity("vls_stableA", null, "stable_location", "neutral");
        locationA.setCircularOrbit(companion, 12, 4250, 350);

//        StarSystemGenerator.addOrbitingEntities(system, companion, StarAge.OLD,
//                        2, 4, // min/max entities to add
//                        5000, // radius to start adding at 
//                        1, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
//                        false); // whether to use custom or system-name based names
        //LAGRANGE ASTEROIDS
        SectorEntityToken asteroidField1 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                        1500f, // min radius
                        2000f, // max radius
                        10, // min asteroid count
                        30, // max asteroid count
                        8f, // min asteroid radius 
                        32f, txt("vls_fieldL5"))); // null for default name
        asteroidField1.setCircularOrbit(star, 140 + 60, 25000, 12000);

        SectorEntityToken asteroidField2 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                        1500f, // min radius
                        2000f, // max radius
                        10, // min asteroid count
                        30, // max asteroid count
                        8f, // min asteroid radius 
                        32f, txt("vls_fieldL4"))); // null for default name
        asteroidField2.setCircularOrbit(star, 140 - 60, 21000, 12000);

        SectorEntityToken vls_station2 = system.addCustomEntity("ora_lumenStation",
                txt("vls_station1"),
                "ora_lumen_type",
                "ORA");
        vls_station2.setCircularOrbitPointingDown(star, 140 + 60, 21000, 12000);

        SectorEntityToken vls_station3 = system.addCustomEntity("ora_lumenRing",
                txt("vls_station2"),
                "ora_lumen_ring",
                "neutral");
        vls_station3.setCircularOrbitWithSpin(star, 140 + 60, 21000, 12000, 45, 45);

        SectorEntityToken gate = system.addCustomEntity("vls_gate", txt("vls_gate"), // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                "neutral"); // faction
        gate.setCircularOrbitPointingDown(star, 140 - 60, 21000, 12000);

        MagicCampaign.createDerelict("colossus_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, true, 0, Math.random() < 0.3f, gate, MathUtils.getRandomNumberInRange(-180, 180), MathUtils.getRandomNumberInRange(50, 400), MathUtils.getRandomNumberInRange(180, 360));
        MagicCampaign.createDerelict("mule_d_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, true, 0, Math.random() < 0.3f, gate, MathUtils.getRandomNumberInRange(-180, 180), MathUtils.getRandomNumberInRange(50, 400), MathUtils.getRandomNumberInRange(180, 360));
        MagicCampaign.createDerelict("phaeton_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, true, 0, Math.random() < 0.5f, gate, MathUtils.getRandomNumberInRange(-180, 180), MathUtils.getRandomNumberInRange(50, 400), MathUtils.getRandomNumberInRange(180, 360));
        MagicCampaign.createDerelict("kite_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, true, 0, Math.random() < 0.5f, gate, MathUtils.getRandomNumberInRange(-180, 180), MathUtils.getRandomNumberInRange(50, 400), MathUtils.getRandomNumberInRange(180, 360));
        MagicCampaign.createDerelict("kite_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, true, 0, Math.random() < 0.3f, gate, MathUtils.getRandomNumberInRange(-180, 180), MathUtils.getRandomNumberInRange(50, 400), MathUtils.getRandomNumberInRange(180, 360));
        MagicCampaign.createDerelict("tarsus_d_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, true, 0, Math.random() < 0.3f, gate, MathUtils.getRandomNumberInRange(-180, 180), MathUtils.getRandomNumberInRange(50, 400), MathUtils.getRandomNumberInRange(180, 360));

        MagicCampaign.createDebrisField(
                "valis_debrisNextToGate", 500, 0.5f, 1000000, 0, 0,
                0, null, 0,
                0, false, 0,
                star, 140 - 60, 21000, 12000);

        system.autogenerateHyperspaceJumpPoints(true, true);
        MagicCampaign.hyperspaceCleanup(system);
    }
}
