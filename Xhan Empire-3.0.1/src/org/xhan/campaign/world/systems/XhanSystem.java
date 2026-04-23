package org.xhan.campaign.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.xhan.XhanEmpireModPlugin.XHAN_FACTION_ID;
import static org.xhan.XhanEmpireModPlugin.addMarketplace;

public class XhanSystem implements SectorGeneratorPlugin {

    // orbit radii, collected here for ez movement
    private static final float ORBITAL_1_ORBIT = 1875;
    private static final float ORBITAL_1_ANGLE = 100;

    private static final float CLUSTER_ORBIT = 4302;
    private static final float CLUSTER_ANGLE = 160;

    private static final float MIDDLE_RING = 6305;

    private static final float ORBITAL_3_ORBIT = 8042;
    private static final float ORBITAL_3_ANGLE = 220;

    private static final float ORBITAL_4_ORBIT = 10512;
    private static final float ORBITAL_4_ANGLE = 280;

    private static final float OUTER_RING = 13120;

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Cluster Prime");
        system.getLocation().set(-36000, 16000);
        system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");

        // primary star
        PlanetAPI star = system.initStar("xhanprime",
                StarTypes.YELLOW,
                775f,
                500, // extent of corona outside star
                10f, // solar wind burn level
                1f, // flare probability
                3f); // CR loss multiplier, good values are in the range of 1-5
        star.setName("Xhan Prime");
        //system.setLightColor(new Color(255, 213, 133));

        // Orbital One, volcanic planet close to the sun
        PlanetAPI orbitalOne = system.addPlanet("orbitalOne", star, "Orbital One", "lava", ORBITAL_1_ANGLE, 100, ORBITAL_1_ORBIT, 88);
        orbitalOne.setCustomDescriptionId("xhan_orbital_one");

        SectorEntityToken orbitalOneMagField = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(orbitalOne.getRadius() + 200f, // terrain effect band width 
                        (orbitalOne.getRadius() + 200f) / 2f, // terrain effect middle radius
                        orbitalOne, // entity that it's around
                        orbitalOne.getRadius() + 50f, // visual band start
                        orbitalOne.getRadius() + 50f + 250f, // visual band end
                        new Color(50, 20, 100, 40), // base color
                        0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(140, 100, 235),
                        new Color(180, 110, 210),
                        new Color(150, 140, 190),
                        new Color(140, 190, 210),
                        new Color(90, 200, 170),
                        new Color(65, 230, 160),
                        new Color(20, 220, 70)
                ));
        orbitalOneMagField.setCircularOrbit(orbitalOne, 0, 0, 100);

        orbitalOne.setFaction(XHAN_FACTION_ID);
        MarketAPI orbitalOneMarket = addMarketplace(XHAN_FACTION_ID, orbitalOne, null,
                "Orbital One", // name of the market
                5, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_conditions ids
                                Conditions.OUTPOST,
                                Conditions.TECTONIC_ACTIVITY,
                                Conditions.VERY_HOT,
                                Conditions.ORE_MODERATE,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.VICE_DEMAND,
                                Industries.POPULATION,
                                Industries.SPACEPORT)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // can't figure out how to add items inside my addmarketplace, too complicated, just brute force it separately
        orbitalOneMarket.addIndustry(Industries.HEAVYBATTERIES, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        orbitalOneMarket.addIndustry(Industries.PATROLHQ, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        orbitalOneMarket.addIndustry(Industries.MINING, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));

        // The Cluster, aka Orbital Two
        PlanetAPI clusterOrbital = system.addPlanet("clusterOrbital", star, "The Cluster", "xhan_cluster", CLUSTER_ANGLE, 200, CLUSTER_ORBIT, 365);
        clusterOrbital.setCustomDescriptionId("xhan_cluster");
        clusterOrbital.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
        clusterOrbital.getSpec().setGlowColor(new Color(255, 215, 185, 255));
        clusterOrbital.getSpec().setUseReverseLightForGlow(true);
        clusterOrbital.setInteractionImage("illustrations", "vacuum_colony");
        clusterOrbital.applySpecChanges();

        clusterOrbital.setFaction(XHAN_FACTION_ID);
        MarketAPI clusterMarket = addMarketplace(XHAN_FACTION_ID, clusterOrbital, null,
                "The Cluster", // name of the market
                8, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of conditions.IDs and Industries.IDs
                                Conditions.REGIONAL_CAPITAL,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.POLLUTION,
                                Conditions.URBANIZED_POLITY)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // can't figure out how to add items inside my addmarketplace, too complicated, just brute force it separately
        clusterMarket.addIndustry(Industries.POPULATION, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        clusterMarket.addIndustry(Industries.MEGAPORT, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        clusterMarket.addIndustry(Industries.STARFORTRESS, new ArrayList<>(List.of(Commodities.ALPHA_CORE)));
        clusterMarket.addIndustry(Industries.HEAVYBATTERIES, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        clusterMarket.addIndustry(Industries.ORBITALWORKS, new ArrayList<>(Arrays.asList(Commodities.GAMMA_CORE, Items.PRISTINE_NANOFORGE)));
        clusterMarket.addIndustry(Industries.HIGHCOMMAND, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        clusterMarket.addIndustry(Industries.REFINING, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));

        // Cluster Checkpoint
        SectorEntityToken clusterCheckpoint = system.addCustomEntity("clusterCheckpoint", "Cluster Checkpoint Station", "Xhan_Cluster_Station", XHAN_FACTION_ID);
        clusterCheckpoint.setCircularOrbitPointingDown(clusterOrbital, 60, 450, 70);
        clusterCheckpoint.setMarket(clusterMarket);

        clusterMarket.getConnectedEntities().add(clusterCheckpoint); //thanks Histidine

        // L4 relay  (initial position in degrees, distance in pixels, orbit speed in days)
        SectorEntityToken relay = system.addCustomEntity("cluster_relay", // unique id
                "Cluster Relay", // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                XHAN_FACTION_ID); // faction
        relay.setCircularOrbitPointingDown(star, CLUSTER_ANGLE - 60, CLUSTER_ORBIT, 365);

        // L5 jump point  (initial position in degrees, distance in pixels, orbit speed in days)
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("mirage_ii_jump", "Cluster Portal");
        jumpPoint.setCircularOrbit(star, CLUSTER_ANGLE + 60, CLUSTER_ORBIT, 365);
        jumpPoint.setRelatedPlanet(clusterOrbital);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        // some decrepit anti-ship mines
        SectorEntityToken mineOne = system.addCustomEntity(null, null, "xhan_mine", Factions.NEUTRAL);
        mineOne.setCircularOrbit(jumpPoint, 120, 100, 40f);
        SectorEntityToken mineTwo = system.addCustomEntity(null, null, "xhan_mine", Factions.NEUTRAL);
        mineTwo.setCircularOrbit(jumpPoint, 60, 150, 60f);
        SectorEntityToken mineThree = system.addCustomEntity(null, null, "xhan_mine", Factions.NEUTRAL);
        mineThree.setCircularOrbit(jumpPoint, 180, 200, 80f);
        SectorEntityToken mineFour = system.addCustomEntity(null, null, "xhan_mine", Factions.NEUTRAL);
        mineFour.setCircularOrbit(jumpPoint, 0, 250, 100f);
        SectorEntityToken mineFive = system.addCustomEntity(null, null, "xhan_mine", Factions.NEUTRAL);
        mineFive.setCircularOrbit(jumpPoint, 240, 300, 120f);

        // L3 empty cryosleeper
        SectorEntityToken clusterSleeper = system.addCustomEntity("clusterSleeper", "Empty Cryosleeper", "xhan_empty_cryosleeper", Factions.NEUTRAL);
        clusterSleeper.setCircularOrbitWithSpin(star, CLUSTER_ANGLE + 180, CLUSTER_ORBIT, 365, 20, 40);

        // some debris around the cryosleeper
        DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                300f, // field radius - should not go above 1000 for performance reasons
                1f, // density, visual - affects number of debris pieces
                66642069f, // duration in days 
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params.baseSalvageXP = 500; // base XP for scavenging in field
        SectorEntityToken debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        SalvageSpecialAssigner.assignSpecialForDebrisField(debris);
        debris.setSensorProfile(null);
        debris.setDiscoverable(null);
        debris.setCircularOrbit(star, CLUSTER_ANGLE + 180, CLUSTER_ORBIT, 365);

        // some derelict ships around the cryosleeper
        SectorEntityToken niceBoulo = addDerelictShip(system, "Boulo_HeavyDemo", ShipRecoverySpecial.ShipCondition.AVERAGE, true);
        niceBoulo.setCircularOrbit(clusterSleeper, 69, 240, 69f);
        SectorEntityToken lessNiceMeiche = addDerelictShip(system, "Meiche_Fueler", ShipRecoverySpecial.ShipCondition.BATTERED, Math.random() > 0.33f);
        lessNiceMeiche.setCircularOrbit(clusterSleeper, 150, 160, 69f);
        SectorEntityToken wreckedKaroba = addDerelictShip(system, "Karoba_Hauler", ShipRecoverySpecial.ShipCondition.WRECKED, Math.random() > 0.66f);
        wreckedKaroba.setCircularOrbit(clusterSleeper, 303, 303, 69f);


        // Orbital Three
        PlanetAPI orbitalThree = system.addPlanet("orbitalThree", star, "Orbital Three", "arid", ORBITAL_3_ANGLE, 155, ORBITAL_3_ORBIT, 400);
        orbitalThree.setCustomDescriptionId("xhan_orbital_three");

        orbitalThree.setFaction(XHAN_FACTION_ID);
        MarketAPI orbitalThreeMarket = addMarketplace(XHAN_FACTION_ID, orbitalThree, null,
                "Orbital Three", // name of the market
                6, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of conditions.IDs and Industries.IDs
                                Conditions.FARMLAND_ADEQUATE,
                                Conditions.ORGANICS_TRACE,
                                Conditions.COLD,
                                Conditions.ARID,
                                Conditions.INDUSTRIAL_POLITY,
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.BATTLESTATION_MID,
                                Industries.HEAVYBATTERIES,
                                Industries.PATROLHQ)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // can't figure out how to add items inside my addmarketplace, too complicated, just brute force it separately
        orbitalThreeMarket.addIndustry(Industries.MINING, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        orbitalThreeMarket.addIndustry(Industries.FARMING, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));

        // Central asteroid belt
        system.addAsteroidBelt(star, 450, MIDDLE_RING - 100, 666, 100, 200, Terrain.ASTEROID_BELT, "Xhan Belt");
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.CYAN.brighter().brighter(), 256f, MIDDLE_RING - 50, 400f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 1024f, 3, Color.white, 1024f, MIDDLE_RING - 150, 666f, null, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, MIDDLE_RING + 150, 69f, null, null);
        system.addAsteroidBelt(star, 100, MIDDLE_RING, 256, 150, 250, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, MIDDLE_RING - 100, 188, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, MIDDLE_RING - 150, 256, 200, 300, Terrain.ASTEROID_BELT, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, MIDDLE_RING - 150, 120f);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.CYAN.brighter().brighter(), 256f, MIDDLE_RING + 150, 160f);
        SectorEntityToken middle_ring = system.addTerrain(Terrain.RING, new RingParams(666, MIDDLE_RING - 100, null, "Xhan Ring"));
        middle_ring.setCircularOrbit(star, 0, 0, 100);

        // Orbital Four, gas giant
        PlanetAPI orbitalFour = system.addPlanet("orbitalFour", star, "Orbital Four", "gas_giant", ORBITAL_4_ANGLE, 300, ORBITAL_4_ORBIT, 778);
        orbitalFour.setCustomDescriptionId("xhan_orbital_four");

        PlanetAPI OrbitalFourA = system.addPlanet("OrbitalFourA", orbitalFour, "Orbital Four Alpha", "barren", 60, 60, 600, 28);
        OrbitalFourA.setCustomDescriptionId("xhan_orbital_four_alpha");
        Misc.initConditionMarket(OrbitalFourA);
        OrbitalFourA.getMarket().addCondition(Conditions.POOR_LIGHT);
        OrbitalFourA.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        OrbitalFourA.getMarket().addCondition(Conditions.ORE_SPARSE);
        OrbitalFourA.getMarket().addCondition(Conditions.VERY_COLD);
        OrbitalFourA.getMarket().addCondition(Conditions.LOW_GRAVITY);
        OrbitalFourA.getMarket().addCondition(Conditions.METEOR_IMPACTS);
        OrbitalFourA.getMarket().addCondition(Conditions.RUINS_SCATTERED);

        orbitalFour.setFaction(XHAN_FACTION_ID);
        MarketAPI OrbitalFourMarket = addMarketplace(XHAN_FACTION_ID, orbitalFour, null,
                "Orbital Four", // name of the market
                4, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_conditions ids
                                Conditions.POOR_LIGHT,
                                Conditions.DENSE_ATMOSPHERE,
                                Conditions.HIGH_GRAVITY,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.EXTREME_WEATHER,
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.HEAVYBATTERIES,
                                Industries.PATROLHQ)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // can't figure out how to add items inside my addmarketplace, too complicated, just brute force it separately
        OrbitalFourMarket.addIndustry(Industries.MINING, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));
        OrbitalFourMarket.addIndustry(Industries.FUELPROD, new ArrayList<>(List.of(Commodities.GAMMA_CORE)));

        // Orbital Four L4 and L5 trojans
        SectorEntityToken m4L4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, // max asteroid radius
                        "Orbital Four L4 Trojans")); // null for default name	
        m4L4.setCircularOrbit(star, ORBITAL_4_ANGLE - 60, ORBITAL_4_ORBIT, 778);

        SectorEntityToken m4L5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius 
                        16f, // max asteroid radius
                        "Orbital Four L5 Trojans")); // null for default name
        m4L5.setCircularOrbit(star, ORBITAL_4_ANGLE + 60, ORBITAL_4_ORBIT, 778);

        system.autogenerateHyperspaceJumpPoints(true, true);

        // An external belt just looks helpful
        system.addAsteroidBelt(star, 450, OUTER_RING - 100, 666, 800, 900, Terrain.ASTEROID_BELT, null);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, OUTER_RING - 50, 900f, null, null);
        system.addAsteroidBelt(star, 100, OUTER_RING, 256, 800, 900, Terrain.ASTEROID_BELT, null);
        system.addAsteroidBelt(star, 100, OUTER_RING - 100, 188, 800, 900, Terrain.ASTEROID_BELT, null);
        SectorEntityToken outer_ring = system.addTerrain(Terrain.RING, new RingParams(666, OUTER_RING - 100, null, "Xhan Rim Belt"));
        outer_ring.setCircularOrbit(star, 0, 0, 750);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

    /**
     * @param system
     * @param variantId
     * @param condition
     * @param recoverable
     * @return
     */
    private SectorEntityToken addDerelictShip(StarSystemAPI system,
                                              String variantId,
                                              ShipRecoverySpecial.ShipCondition condition,
                                              boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        return ship;
    }
}
