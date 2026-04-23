package org.xhan.campaign.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
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
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import org.xhan.campaign.world.XHAN_MyriaLeashAssignmentAI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class XhanMyrianousSystem implements SectorGeneratorPlugin {

    // orbit radii, collected here for ez movement

    private static final float MIDDLE_RING = 6200;

    private static final float ORBITAL_P_ORBIT = 4100;
    private static final float ORBITAL_P_ANGLE = 280;

    private static final float GOLBA_ORBIT = 4200;
    private static final float GOLBA_ANGLE = 280;

    private static final float OUTER_RING = 9200;

    @Override
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Uvas");
        //make sure no procgen, MagicBounty, etc. can choose here
        system.addTag(Tags.THEME_UNSAFE);
        system.addTag(Tags.THEME_HIDDEN);
        system.addTag(Tags.THEME_SPECIAL);
        system.getLocation().set(-68000, 41000);
        system.setBackgroundTextureFilename("graphics/backgrounds/background_galatia.jpg");
        system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");

        // primary star
        PlanetAPI star = system.initStar("Uvas",
                StarTypes.BLUE_GIANT,
                500f,
                250, // extent of corona outside star
                10f, // solar wind burn level
                1f, // flare probability
                1f); // CR loss multiplier, good values are in the range of 1-5
        star.setName("Uvas");
        //system.setLightColor(new Color(255, 213, 133));

        // Golba
        PlanetAPI X_Golba = system.addPlanet("X_Golba", star, "Golba", "toxic", GOLBA_ANGLE, 90, GOLBA_ORBIT, 900);
        X_Golba.setCustomDescriptionId("X_Golba");

        SectorEntityToken orbitalOneMagField = system.addTerrain(Terrain.MAGNETIC_FIELD,
                new MagneticFieldTerrainPlugin.MagneticFieldParams(X_Golba.getRadius() + 120f, // terrain effect band width
                        (X_Golba.getRadius() + 120f) / 2f, // terrain effect middle radius
                        X_Golba, // entity that it's around
                        X_Golba.getRadius() + 15f, // visual band start
                        X_Golba.getRadius() + 50f + 180f, // visual band end
                        new Color(187, 255, 0, 72), // base color
                        0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                        new Color(121, 235, 203),
                        new Color(99, 210, 136),
                        new Color(129, 190, 107),
                        new Color(180, 210, 150),
                        new Color(200, 192, 118),
                        new Color(230, 148, 93),
                        new Color(220, 84, 84)
                ));
        orbitalOneMagField.setCircularOrbit(X_Golba, 0, 0, 100);

        Misc.initConditionMarket(X_Golba);
        X_Golba.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
        X_Golba.getMarket().addCondition(Conditions.RUINS_SCATTERED);
        X_Golba.getMarket().addCondition(Conditions.RARE_ORE_ULTRARICH);
        X_Golba.getMarket().addCondition(Conditions.VERY_HOT);
        X_Golba.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);
        X_Golba.getMarket().addCondition(Conditions.ORE_MODERATE);


        // some derelict ships around the Planet
        SectorEntityToken niceKetsil = addDerelictShip(system, "XHAN_Ketsil_Lander", ShipRecoverySpecial.ShipCondition.AVERAGE, true);
        niceKetsil.setCircularOrbit(X_Golba, 69, 270, 69f);
        SectorEntityToken lessNiceMeiche = addDerelictShip(system, "Meiche_Fueler", ShipRecoverySpecial.ShipCondition.BATTERED, Math.random() > 0.33f);
        lessNiceMeiche.setCircularOrbit(X_Golba, 150, 360, 69f);

        // Destroyed Station
        SectorEntityToken MyriaStation = system.addCustomEntity("MyriaStation", "Abandoned Research Station", "xhan_destroyed_station", Factions.NEUTRAL);
        MyriaStation.setCircularOrbitWithSpin(star, GOLBA_ANGLE + 180, GOLBA_ORBIT, 365, 20, 40);
        addFleet(MyriaStation);

        // some debris around the station
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
        debris.setCircularOrbit(star, GOLBA_ANGLE + 180, GOLBA_ORBIT, 365);

        // some derelict ships around the station
        SectorEntityToken niceOieHou = addDerelictShip(system, "OieHou_EliteGuard", ShipRecoverySpecial.ShipCondition.AVERAGE, true);
        niceOieHou.setCircularOrbit(MyriaStation, 69, 240, 69f);
        SectorEntityToken lessNiceOlkzan = addDerelictShip(system, "Olkzan_Lineman", ShipRecoverySpecial.ShipCondition.BATTERED, Math.random() > 0.33f);
        lessNiceOlkzan.setCircularOrbit(MyriaStation, 150, 160, 69f);
        SectorEntityToken wreckedCheborog = addDerelictShip(system, "Cheborog_Brawler", ShipRecoverySpecial.ShipCondition.WRECKED, Math.random() > 0.66f);
        wreckedCheborog.setCircularOrbit(MyriaStation, 303, 303, 69f);

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
                        "null")); // null for default name
        m4L4.setCircularOrbit(star, ORBITAL_P_ANGLE - 60, ORBITAL_P_ORBIT, 778);

        SectorEntityToken m4L5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        500f, // min radius
                        700f, // max radius
                        20, // min asteroid count
                        30, // max asteroid count
                        4f, // min asteroid radius
                        16f, // max asteroid radius
                        "null")); // null for default name
        m4L5.setCircularOrbit(star, ORBITAL_P_ANGLE + 60, ORBITAL_P_ORBIT, 778);


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

    public static void addFleet(SectorEntityToken MyriaStation) {
        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.OMEGA, FleetTypes.PATROL_LARGE, null);
        fleet.setName("Unidentified Vessel");
        fleet.setNoFactionInName(true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
        //fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true); // so it keeps transponder on
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, false);
        fleet.getMemoryWithoutUpdate().set("$Myrianous", true);

        fleet.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);


        fleet.getFleetData().addFleetMember("XHAN_Myrianous_Ocupex");
        fleet.getFleetData().ensureHasFlagship();

        fleet.clearAbilities();
//		fleet.addAbility(Abilities.TRANSPONDER);
//		fleet.getAbility(Abilities.TRANSPONDER).activate();

        // so it never shows as "Unidentified Fleet" but isn't easier to spot due to using the actual transponder ability
        fleet.setTransponderOn(true);

        PersonAPI person = createZigguratCaptain();
        fleet.setCommander(person);

        FleetMemberAPI flagship = fleet.getFlagship();
        flagship.setCaptain(person);
        flagship.updateStats();
        flagship.getRepairTracker().setCR(flagship.getRepairTracker().getMaxCR());
        flagship.setShipName("Thousand Eyes");

        // to "perm" the variant so it gets saved and not recreated from the "ziggurat_Experimental" id
        flagship.setVariant(flagship.getVariant().clone(), false, false);
        flagship.getVariant().setSource(VariantSource.REFIT);


        Vector2f loc = new Vector2f(MyriaStation.getLocation().x + 300 * ((float) Math.random() - 0.5f),
                MyriaStation.getLocation().y + 300 * ((float) Math.random() - 0.5f));
        fleet.setLocation(loc.x, loc.y);
        MyriaStation.getContainingLocation().addEntity(fleet);

        fleet.addScript(new XHAN_MyriaLeashAssignmentAI(fleet, MyriaStation));

    }


    public static PersonAPI createZigguratCaptain() {
        PersonAPI person = Global.getFactory().createPerson();
        person.setName(new FullName("Vos", "", FullName.Gender.ANY));
        person.setFaction(Factions.NEUTRAL);
        person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "Xhan_Vos"));
        person.setPersonality(Personalities.STEADY);
        person.setRankId(Ranks.SPACE_CAPTAIN);
        person.setPostId(null);

        person.getStats().setSkipRefresh(true);

        person.getStats().setLevel(10);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        //person.getStats().setSkillLevel(Skills.RELIABILITY_ENGINEERING, 2);
        //person.getStats().setSkillLevel(Skills.RANGED_SPECIALIZATION, 2);
        person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
        person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        //person.getStats().setSkillLevel(Skills.PHASE_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);

        person.getStats().setSkillLevel(Skills.NAVIGATION, 1);

        person.getStats().setSkipRefresh(false);

        return person;
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
