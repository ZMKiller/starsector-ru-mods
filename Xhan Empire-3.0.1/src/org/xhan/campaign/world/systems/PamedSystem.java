package org.xhan.campaign.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fs.starfarer.api.impl.campaign.ids.Factions.TRITACHYON;
import static org.xhan.XhanEmpireModPlugin.PAMED_FACTION_ID;
import static org.xhan.XhanEmpireModPlugin.addMarketplace;

public class PamedSystem implements SectorGeneratorPlugin {

    // orbit radii, collected here for ez movement
    private static final float OKA_ORBIT = 1300;
    private static final float OKA_ANGLE = 100;

    private static final float PRELAY_ORBIT = 4500;
    private static final float PRELAY_ANGLE = 160;

    private static final float MIDDLE_RING = 3200;

    private static final float PAMED_ORBIT = 2000;
    private static final float PAMED_ANGLE = 220;

    private static final float ORBITAL_P_ORBIT = 4100;
    private static final float ORBITAL_P_ANGLE = 280;

    private static final float DOBUS_ORBIT = 8200;
    private static final float DOBUS_ANGLE = 280;

    private static final float OUTER_RING = 4000;

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Paltan");
        system.getLocation().set(-24300, -7100);
        system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");

        // primary star
        PlanetAPI star = system.initStar("Paltan",
                StarTypes.ORANGE,
                400f,
                200, // extent of corona outside star
                10f, // solar wind burn level
                1f, // flare probability
                1f); // CR loss multiplier, good values are in the range of 1-5
        star.setName("Paltan");
        //system.setLightColor(new Color(255, 213, 133));


        // L4 relay  (initial position in degrees, distance in pixels, orbit speed in days)
        SectorEntityToken relay = system.addCustomEntity("pamed_relay", // unique id
                "Pamed Relay", // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                PAMED_FACTION_ID); // faction
        relay.setCircularOrbitPointingDown(star, PRELAY_ANGLE - 60, PRELAY_ORBIT, 365);

        // Dobus
        PlanetAPI P_Dobus = system.addPlanet("P_Dobus", star, "Dobus", "barren", DOBUS_ANGLE, 60, DOBUS_ORBIT, 900);
        P_Dobus.setCustomDescriptionId("P_Dobus");

        P_Dobus.setFaction(TRITACHYON);
        MarketAPI DobusMarket = addMarketplace(TRITACHYON, P_Dobus, null,
                "Dobus", // name of the market
                3, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of conditions.IDs and Industries.IDs
                                Conditions.NO_ATMOSPHERE,
                                Conditions.LOW_GRAVITY,
                                Conditions.COLD,
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.PATROLHQ)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // Oka
        PlanetAPI p_oka = system.addPlanet("p_oka", star, "Oka", "Pamed_Oka", OKA_ANGLE, 60, OKA_ORBIT, 180);
        p_oka.setCustomDescriptionId("pamed_oka");

        SectorEntityToken orbitalOneMagField = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldParams(p_oka.getRadius() + 120f, // terrain effect band width
                        (p_oka.getRadius() + 120f) / 2f, // terrain effect middle radius
                        p_oka, // entity that it's around
                        p_oka.getRadius() + 15f, // visual band start
                        p_oka.getRadius() + 50f + 180f, // visual band end
                        new Color(255, 182, 0, 125), // base color
                        0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(235, 203, 121),
                        new Color(210, 144, 99),
                        new Color(190, 113, 107),
                        new Color(166, 210, 150),
                        new Color(118, 200, 191),
                        new Color(93, 204, 230),
                        new Color(84, 204, 220)
                ));
        orbitalOneMagField.setCircularOrbit(p_oka, 0, 0, 100);

        p_oka.setFaction(PAMED_FACTION_ID);
        MarketAPI OkaMarket = addMarketplace(PAMED_FACTION_ID, p_oka, null,
                "Oka", // name of the market
                4, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of market_conditions ids
                                Conditions.ORE_ULTRARICH,
                                Conditions.RARE_ORE_ULTRARICH,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.HOT,
                                Conditions.NO_ATMOSPHERE,
                                Industries.POPULATION,
                                Industries.MINING,
                                Industries.HEAVYBATTERIES,
                                Industries.MILITARYBASE,
                                Industries.STARFORTRESS_HIGH,
                                Industries.SPACEPORT)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // Pamed
        PlanetAPI Pamed = system.addPlanet("Pamed", star, "Pamed", "jungle", PAMED_ANGLE, 155, PAMED_ORBIT, 400);
        Pamed.setCustomDescriptionId("Pamed");

        Pamed.setFaction(PAMED_FACTION_ID);
        MarketAPI PamedMarket = addMarketplace(PAMED_FACTION_ID, Pamed, null,
                "Pamed", // name of the market
                6, // size of the market
                new ArrayList<>(
                        Arrays.asList( // list of conditions.IDs and Industries.IDs
                                Conditions.FARMLAND_BOUNTIFUL,
                                Conditions.MILD_CLIMATE,
                                Conditions.HABITABLE,
                                Conditions.JUNGLE,
                                Conditions.REGIONAL_CAPITAL,
                                Industries.POPULATION,
                                Industries.MEGAPORT,
                                Industries.FARMING,
                                Industries.REFINING,
                                Industries.STARFORTRESS_HIGH,
                                Industries.HEAVYBATTERIES,
                                Industries.PATROLHQ)),
                new ArrayList<>(
                        Arrays.asList( // which submarkets to generate
                                Submarkets.SUBMARKET_BLACK,
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_STORAGE)),
                true, // with junk and chatter?
                false, // pirate mode? (i.e. hidden)
                false); // freeport

        // can't figure out how to add items inside my addmarketplace, too complicated, just brute force it separately
        PamedMarket.addIndustry(Industries.ORBITALWORKS, new ArrayList<>(List.of(Items.CORRUPTED_NANOFORGE)));


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

        // Peltan R4
        SectorEntityToken m4L4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius
                        16f, // max asteroid radius
                        "Peltan R4")); // null for default name
        m4L4.setCircularOrbit(star, ORBITAL_P_ANGLE - 60, ORBITAL_P_ORBIT, 778);

        SectorEntityToken m4L5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius
                        16f, // max asteroid radius
                        "Peltan R5")); // null for default name
        m4L5.setCircularOrbit(star, ORBITAL_P_ANGLE + 60, ORBITAL_P_ORBIT, 778);

        // L5 jump point  (initial position in degrees, distance in pixels, orbit speed in days)
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("mirage_ii_jump", "Pamed Portal");
        jumpPoint.setCircularOrbit(star, PAMED_ANGLE + 60, PAMED_ORBIT, 400);
        jumpPoint.setRelatedPlanet(Pamed);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

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
