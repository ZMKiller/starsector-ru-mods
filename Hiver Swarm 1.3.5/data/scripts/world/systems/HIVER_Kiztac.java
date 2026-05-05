package data.scripts.world.systems;

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

import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.world.AddMarketplace;

import java.awt.*;

public class HIVER_Kiztac {

    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Kiztac");

        system.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");

        PlanetAPI Kiztac_star = system.initStar("Kiztac", // unique id for this star
                "star_yellow", // Star type. Vanilla star types can be found in starsector-core/data/campaign/procgen/star_gen_data.csv and starsector-core/data/config/planets.json
                700f, 		  //gfbdfgb radius (in pixels at default zoom)
                350); // corona radius, from star edge

        system.setLightColor(new Color(245, 187, 186)); // light color in entire system, affects all entities


        PlanetAPI HIVER_kyrika = system.addPlanet("HIVER_kyrika", Kiztac_star, "Kyrika", "arid", 270, 200, 3900, 60);
        HIVER_kyrika.setCustomDescriptionId("planet_kyrika");
		HIVER_kyrika.setInteractionImage("illustrations", "HIVER_hanger");					
		
        PlanetAPI HIVER_Tototchic = system.addPlanet("HIVER_Tototchic", Kiztac_star, "Tototchic", "jungle", 2270, 200, 2800, 560);
        HIVER_Tototchic.setCustomDescriptionId("planet_Tototchic");
		HIVER_Tototchic.setInteractionImage("illustrations", "HIVER_hanger");		

        SectorEntityToken Kiztac_asteroids = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        300f, // min radius
                        500f, // max radius
                        16, // min asteroid count
                        24, // max asteroid count
                        4f, // min asteroid radius
                        16f, // max asteroid radius
                        "Kiztac Asteroids")); // null for default name

        SectorEntityToken Kiztac_asteroids2 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(
                        300f, // min radius
                        500f, // max radius
                        16, // min asteroid count
                        24, // max asteroid count
                        4f, // min asteroid radius
                        16f, // max asteroid radius
                        "Kiztac Asteroid field")); // null for default name

        Kiztac_asteroids.setCircularOrbit(Kiztac_star, 270 +60, 2800, 80);
        Kiztac_asteroids2.setCircularOrbit(Kiztac_star, 270 -60, 2800, 80);


        PlanetAPI HIVER_Arokor = system.addPlanet("HIVER_Arokor", Kiztac_star, "Arokor", "terran", 220, 160, 8300, 180);
        HIVER_Arokor.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        HIVER_Arokor.getSpec().setGlowColor(new Color(170,255,240,255));
        HIVER_Arokor.getSpec().setUseReverseLightForGlow(true);
        HIVER_Arokor.applySpecChanges();
        HIVER_Arokor.setCustomDescriptionId("planet_arokor");
		HIVER_Arokor.setInteractionImage("illustrations", "HIVER_hanger");		

        PlanetAPI HIVER_Zozoris = system.addPlanet("HIVER_Zozoris", Kiztac_star, "Zozoris", "desert", 1220, 460, 7300, 180);
        HIVER_Zozoris.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        HIVER_Zozoris.getSpec().setGlowColor(new Color(170,255,240,255));
        HIVER_Zozoris.getSpec().setUseReverseLightForGlow(true);
        HIVER_Zozoris.applySpecChanges();
        HIVER_Zozoris.setCustomDescriptionId("planet_zozoris");
		HIVER_Zozoris.setInteractionImage("illustrations", "HIVER_hanger");		
		
        // counter-orbit sensor array
        SectorEntityToken Kiztac_loc1 = system.addCustomEntity(null, null, "sensor_array_makeshift", "HIVER");
        Kiztac_loc1.setCircularOrbitPointingDown(Kiztac_star, 180-120, 6600, 400);

        SectorEntityToken Kiztac_loc2 = system.addCustomEntity(null, null, "comm_relay_makeshift", "HIVER");
        Kiztac_loc2.setCircularOrbitPointingDown(Kiztac_star, 180+120, 7300, 400);

        // fun debris
        DebrisFieldParams params = new DebrisFieldParams(
                200f, // field radius - should not go above 1000 for performance reasons
                1f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces

        params.source = DebrisFieldSource.SALVAGE;
        params.baseSalvageXP = 250; // base XP for scavenging in field
        SectorEntityToken debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        SalvageSpecialAssigner.assignSpecialForDebrisField(debris);

        // makes the debris field always visible on map/sensors and not give any xp or notification on being discovered
        debris.setSensorProfile(null);
        debris.setDiscoverable(null);
        debris.setCircularOrbit(Kiztac_star, 220 + 16, 4300, 180);

        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("Kiztac_jump", "Kiztac Jump Point");
        jumpPoint2.setCircularOrbit( system.getEntityById("Kiztac"), 220 + 60, 4300, 180);
        jumpPoint2.setRelatedPlanet(HIVER_Arokor);
        system.addEntity(jumpPoint2);


        system.addRingBand(Kiztac_star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 5900, 220f, null, null);
        system.addAsteroidBelt(Kiztac_star, 150, 5900, 128, 200, 240, Terrain.ASTEROID_BELT, "The Hate");



        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, Kiztac_star, StarAge.AVERAGE,
                1, 4, // min/max entities to add
                9900, // radius to start adding at
                6, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                false); // whether to use custom or system-name based names

        system.autogenerateHyperspaceJumpPoints(true, true);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

    }
}