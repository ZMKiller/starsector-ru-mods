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
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.ORA_txt.txt;

public class ORA_joy implements SectorGeneratorPlugin {

    public static SectorEntityToken getSectorAccess() {
        return Global.getSector().getStarSystem(txt("joy_system")).getEntityById("joy_starA");
    }

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem(txt("joy_system"));
        system.setBackgroundTextureFilename("graphics/ORA/backgrounds/ora_joy.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("joy_starA", // unique id for this star
                StarTypes.BLUE_GIANT, // id in planets.json
                900f,
                500f);		// radius (in pixels at default zoom)
        system.setLightColor(new Color(200, 230, 255)); // light color in entire system, affects all entities
//        star.setCustomDescriptionId("star_yellow");

        system.getLocation().set(-14000, 11000);

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
        SectorEntityToken barycenter = system.addCustomEntity("ora_barycenter",
                txt("joy_center"),
                "ora_barycenter",
                null);
        barycenter.setFixedLocation(0, 0);

        star.setCircularOrbit(barycenter, 0, 350, 10);

        PlanetAPI joy0 = system.addPlanet("joy_0", barycenter, txt("joy_starB"), "star_white", 180, 175, 800, 10);

        system.setType(StarSystemGenerator.StarSystemType.BINARY_CLOSE);
        system.setSecondary(joy0);

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
        //ASTEROID BELT
        system.addAsteroidBelt(barycenter, 500, 4000, 512, 190, 210);
        system.addRingBand(barycenter, "misc", "ora_ringsC", 1024f, 0, new Color(0.5f, 0.5f, 1, 0.25f), 1024f, 3800, 170f);
        system.addRingBand(barycenter, "misc", "ora_ringsC", 1024f, 0, new Color(0.5f, 0.5f, 1, 0.25f), 1024f, 4200, 230f);

        system.addRingBand(barycenter, "misc", "ora_ringsD", 1024f, 0, new Color(0.5f, 0.5f, 1, 0.5f), 1024f, 4000, 200f, Terrain.RING, txt("joy_ring1"));

        system.addRingBand(barycenter, "misc", "ora_ringsR", 1024f, 0, new Color(0.5f, 0.5f, 1, 1), 1024f, 3900, 180f);
        system.addRingBand(barycenter, "misc", "ora_ringsR", 1024f, 0, new Color(0.5f, 0.5f, 1, 1), 1024f, 4100, 220f);

        SectorEntityToken joy_station1 = system.addCustomEntity("ora_fortress",
                txt("joy_station1"),
                "station_side05",
                "pirates");
        joy_station1.setCircularOrbitPointingDown(barycenter, 270, 4000, 200);
        joy_station1.setCustomDescriptionId("ora_fortress");
        joy_station1.setInteractionImage("illustrations", "pirate_station");

        addDerelict(system, barycenter, "enforcer_d_pirates_Strike", ShipRecoverySpecial.ShipCondition.BATTERED, 4100, (Math.random() < 0.3f));
        addDerelict(system, barycenter, "wolf_d_pirates_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 4050, (Math.random() < 0.3f));
        addDerelict(system, barycenter, "buffalo_pirates_Standard", ShipRecoverySpecial.ShipCondition.AVERAGE, 3900, (Math.random() < 0.5f));
        addDerelict(system, barycenter, "cerberus_d_pirates_Standard", ShipRecoverySpecial.ShipCondition.AVERAGE, 4000, (Math.random() < 0.5f));
        addDerelict(system, barycenter, "afflictor_d_pirates_Strike", ShipRecoverySpecial.ShipCondition.BATTERED, 4250, (Math.random() < 0.3f));
        addDerelict(system, barycenter, "dominator_d_Assault", ShipRecoverySpecial.ShipCondition.BATTERED, 3800, (Math.random() < 0.3f));

        SectorEntityToken joy_station2 = system.addCustomEntity("ora_listeningpost",
                txt("joy_station2"),
                "station_side03",
                Factions.PERSEAN);
        joy_station2.setCircularOrbitPointingDown(barycenter, 60, 4750, 230);
        joy_station2.setCustomDescriptionId("ora_listeningpost");
        joy_station2.setInteractionImage("illustrations", "ora_orbital");

        //6000
        PlanetAPI joy1 = system.addPlanet("joy_satiate", barycenter, txt("joy_planetA"), "terran-eccentric", 66, 200, 6000, 400);
        joy1.setCustomDescriptionId("ora_satiate");

        SectorEntityToken joy_station3 = system.addCustomEntity("ora_famine",
                txt("joy_stationA"),
                "station_side02",
                "independent");
        joy_station3.setCircularOrbitPointingDown(joy1, -33, 300, 25);
        joy_station3.setCustomDescriptionId("ora_famine");
        joy_station3.setInteractionImage("illustrations", "space_bar");

        //JUMP POINT
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("joy_jumpPointA", txt("joy_jp1"));
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(barycenter, 126, 6000, 400);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(joy1);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);

        //3000
        PlanetAPI joy2 = system.addPlanet("joy_2", barycenter, txt("joy_planetB"), "gas_giant", 240, 300, 8500, 800);
//        joy2.setCustomDescriptionId("ora_camillia");
//	joy2.setInteractionImage("illustrations", "ora_freighters");

        PlanetAPI joy21 = system.addPlanet("joy_21", joy2, txt("joy_planetB1"), "barren-bombarded", 60, 50, 450, 30);
        PlanetAPI joy22 = system.addPlanet("ora_suffering", joy2, txt("joy_planetB2"), "toxic", 200, 60, 600, 50);
        joy22.setCustomDescriptionId("ora_suffering");
        joy22.setInteractionImage("illustrations", "vacuum_colony");

//        joy22.getMemoryWithoutUpdate().set("$abandonedStation", true);
//            MarketAPI market = Global.getFactory().createMarket("ora_sufferingMarket", "Dilapided Market", 3);
//            market.setPrimaryEntity(joy22);
//            market.setFactionId(Factions.PIRATES);
//            market.addCondition(Conditions.FRONTIER);
//            market.addCondition(Conditions.DECIVILIZED_SUBPOP);
//            market.addCondition(Conditions.VOLATILES_DIFFUSE);
//            market.addCondition(Conditions.TOXIC_ATMOSPHERE);
//            
//            market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
//            market.addSubmarket(Submarkets.SUBMARKET_BLACK);
//            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
//            joy22.setMarket(market);     
        PlanetAPI joy23 = system.addPlanet("joy_21", joy2, txt("joy_planetC"), "toxic_cold", 90, 30, 775, 120);

        //RELAY
        SectorEntityToken relay = system.addCustomEntity("joy_relay", // unique id
                null, // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "independent"); // faction
        relay.setCircularOrbit(barycenter, 240 + 60, 8500, 800);

        //LAGRANGE ASTEROIDS
        SectorEntityToken asteroidField1 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        1000f, // min radius
                        1500f, // max radius
                        40, // min asteroid count
                        60, // max asteroid count
                        4f, // min asteroid radius 
                        16f, txt("joy_fieldL5a"))); // null for default name
        asteroidField1.setCircularOrbit(barycenter, 240 + 60, 8500, 800);

        SectorEntityToken asteroidField2 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        1000f, // min radius
                        1500f, // max radius
                        40, // min asteroid count
                        60, // max asteroid count
                        4f, // min asteroid radius 
                        16f, txt("joy_fieldL4a"))); // null for default name
        asteroidField2.setCircularOrbit(barycenter, 240 - 60, 8500, 800);

        //GIANT
        PlanetAPI joy3 = system.addPlanet("joy_3", barycenter, txt("joy_planetD"), "ice_giant", 120, 400, 17000, 2500);

        PlanetAPI joy31 = system.addPlanet("joy_31", joy3, txt("joy_planetD1"), "frozen", 33, 50, 900, 900);
        joy31.setCustomDescriptionId("ora_ailment");
        joy31.setInteractionImage("illustrations", "abandoned_station");

        joy31.getMemoryWithoutUpdate().set("$abandonedStation", true);
        MarketAPI market1 = Global.getFactory().createMarket("ora_ailment_market", txt("joy_planetD1m"), 0);
        market1.setPrimaryEntity(joy31);
        market1.setFactionId(joy31.getFaction().getId());
        market1.addCondition(Conditions.ABANDONED_STATION);
        market1.addCondition(Conditions.ICE);
        market1.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market1.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        ((StoragePlugin) market1.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
        joy31.setMarket(market1);
        joy31.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "buffalo_d_Standard", "ISS Rabbit Foot IV");
        joy31.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("volatiles", 225);
        joy31.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("supplies", 21);
        joy31.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity("fuel", 27);
//            joy31.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSpecial(new SpecialItemData("ora_standard_package",null), 1);
//            joy31.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSpecial(new SpecialItemData("ora_tech_package",null), 1);

        SectorEntityToken locationA = system.addCustomEntity("joy_stableA", null, "stable_location", "neutral");
        locationA.setCircularOrbit(joy3, 167, 1750, 2200);

        system.addRingBand(joy3, "misc", "ora_ringsS", 256f, 3, new Color(0.7f, 0.8f, 1, 0.75f), 128f, 700, 70f, Terrain.RING, txt("joy_ring2"));
        system.addRingBand(joy3, "misc", "ora_ringsS", 256f, 0, new Color(0.7f, 0.8f, 1, 0.5f), 256f, 1200, 120f, Terrain.RING, txt("joy_ring3"));

        //LAGRANGE ASTEROIDS
        SectorEntityToken asteroidField3 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        750f, // max radius
                        10, // min asteroid count
                        20, // max asteroid count
                        4f, // min asteroid radius 
                        16f, txt("joy_fieldL5b"))); // null for default name
        asteroidField3.setCircularOrbit(barycenter, 120 + 60, 17000, 2500);

        SectorEntityToken asteroidField4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        750f, // max radius
                        10, // min asteroid count
                        20, // max asteroid count
                        4f, // min asteroid radius 
                        16f, txt("joy_fieldL4b"))); // null for default name
        asteroidField4.setCircularOrbit(barycenter, 120 - 60, 17000, 2500);

        SectorEntityToken gate = system.addCustomEntity("joy_gate", txt("joy_gate"), // name - if null, defaultName from custom_entities.json will be used
                "inactive_gate", // type of object, defined in custom_entities.json
                null); // faction
        gate.setCircularOrbit(star, 120 - 60, 17000, 2500);

        system.autogenerateHyperspaceJumpPoints(true, true);
        MagicCampaign.hyperspaceCleanup(system);
    }

    protected void addDerelict(StarSystemAPI system, SectorEntityToken focus, String variantId,
        ShipRecoverySpecial.ShipCondition condition, float orbitRadius, boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
        ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
    }
}
