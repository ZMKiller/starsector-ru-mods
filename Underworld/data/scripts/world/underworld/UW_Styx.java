package data.scripts.world.underworld;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_Styx {

    private static final Random random = new Random();

    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity,
            ArrayList<SectorEntityToken> connectedEntities, String name, int size,
            ArrayList<String> conditionList, ArrayList<ArrayList<String>> industryList, ArrayList<String> submarkets,
            float tarrif, boolean freePort) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID/* + "_market"*/;

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tarrif);
        newMarket.getLocationInHyperspace().set(primaryEntity.getLocationInHyperspace());

        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        for (String condition : conditionList) {
            newMarket.addCondition(condition);
        }

        for (ArrayList<String> industryWithParam : industryList) {
            String industry = industryWithParam.get(0);
            if (industryWithParam.size() == 1) {
                newMarket.addIndustry(industry);
            } else {
                newMarket.addIndustry(industry, industryWithParam.subList(1, industryWithParam.size()));
            }
        }

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        newMarket.setFreePort(freePort);
        globalEconomy.addMarket(newMarket, true);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        return newMarket;
    }

    public static void dickersonifyBaseStaff() {
        SectorEntityToken arigato = getArigato();
        if (arigato == null) {
            return;
        }
        for (PersonAPI person : arigato.getMarket().getPeopleCopy()) {
            if (!person.getFaction().getId().equals(Factions.PIRATES)) {
                continue;
            }
            if (person.getPost().equals(Ranks.POST_MERCENARY)) {
                continue;
            }
            person.setPortraitSprite("graphics/uw/portraits/uw_dickerson_extended_family.png");
            person.getName().setLast("Dickerson");
        }
    }

    public static void generatePt2(SectorAPI sector) {
        StarSystemAPI system = sector.getStarSystem("Styx");
        MarketAPI scrapyardMarket = sector.getEconomy().getMarket("uw_arigato");

        pickLocation(sector, system);

        scrapyardMarket.getLocationInHyperspace().set(system.getLocation());

        system.autogenerateHyperspaceJumpPoints(true, true);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);

        float minRadius = plugin.getTileSize() * 2f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

    public static SectorEntityToken getArigato() {
        if (Global.getSector().getStarSystem("Styx") == null) {
            return null;
        }
        return Global.getSector().getStarSystem("Styx").getEntityById("uw_arigato");
    }

    public static SectorEntityToken getRoboto() {
        if (Global.getSector().getStarSystem("Styx") == null) {
            return null;
        }
        return Global.getSector().getStarSystem("Styx").getEntityById("uw_roboto");
    }

    public static SectorEntityToken getStyx() {
        if (Global.getSector().getStarSystem("Styx") == null) {
            return null;
        }
        return Global.getSector().getStarSystem("Styx").getEntityById("uw_styx");
    }

    private static float getRandom(float min, float max) {
        float radius = min + (max - min) * random.nextFloat();
        return radius;
    }

    private static void pickLocation(SectorAPI sector, StarSystemAPI system) {
        float radius = system.getMaxRadiusInHyperspace() + 200f;
        try_again:
        for (int i = 0; i < 100; i++) {
            Vector2f loc = new Vector2f(getRandom(20000f, 30000f), 0f);
            VectorUtils.rotate(loc, getRandom(0f, 360f), loc);

            for (LocationAPI location : sector.getAllLocations()) {
                if (location instanceof StarSystemAPI) {
                    float otherRadius = ((StarSystemAPI) location).getMaxRadiusInHyperspace();
                    if (MathUtils.getDistance(location.getLocation(), loc) < radius + otherRadius) {
                        continue try_again;
                    }
                }
            }

            system.getLocation().set(loc.x, loc.y);
            break;
        }
    }

    public void generate(SectorAPI sector) {
        random.setSeed(sector.getSeedString().hashCode());

        StarSystemAPI system = sector.createStarSystem("Styx");
        system.addTag(Tags.THEME_CORE_POPULATED);
        system.addTag(Tags.THEME_CORE);

        system.setBackgroundTextureFilename("graphics/uw/backgrounds/uw_styx.png");

        PlanetAPI star = system.initStar("uw_styx", StarTypes.RED_DWARF, 500f, 300f); // 0.3 solar masses
        star.setCustomDescriptionId("uw_star_styx");

        /* Index 4 */
        PlanetAPI roboto = system.addPlanet("uw_roboto", star, "Roboto", "toxic", 120, 120, 5500, 770); // 0.9 earth masses, 1.1 AU
        roboto.getSpec().setTilt(10f);
        roboto.getSpec().setCloudColor(new Color(230, 210, 200, 170));
        roboto.applySpecChanges();
        roboto.setCustomDescriptionId("uw_planet_roboto");

        MarketAPI robotoMarket = Global.getFactory().createMarket("uw_roboto_conditions", roboto.getName(), 0);
        robotoMarket.setPrimaryEntity(roboto);
        robotoMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        robotoMarket.setFactionId(Factions.NEUTRAL);
        robotoMarket.addCondition(Conditions.RUINS_VAST);
        robotoMarket.addCondition(Conditions.TOXIC_ATMOSPHERE);
        robotoMarket.addCondition(Conditions.POLLUTION);
        robotoMarket.addCondition(Conditions.ORE_ABUNDANT);
        robotoMarket.addCondition(Conditions.RARE_ORE_SPARSE);
        robotoMarket.setPlanetConditionMarketOnly(true);
        for (MarketConditionAPI cond : robotoMarket.getConditions()) {
            cond.setSurveyed(true);
        }
        roboto.setMarket(robotoMarket);

        SectorEntityToken arigato = system.addCustomEntity("uw_arigato", "Arigato",
                "station_sporeship_derelict", Factions.PIRATES);
        arigato.setCircularOrbitPointingDown(roboto, 90, 300, 13); // 0.0015 AU
        arigato.setInteractionImage("illustrations", "orbital_construction");
        arigato.setCustomDescriptionId("uw_station_arigato");

        system.addAsteroidBelt(star, 150, 4000, 350, 450, 500, Terrain.ASTEROID_BELT, "Modron Belt");
        system.addAsteroidBelt(star, 200, 13000, 2500, 2650, 2950, Terrain.ASTEROID_BELT, "Domo Belt");

        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("uw_styxgate", "Styx Jump-point");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 35, 4000, 475);
        jumpPoint.setRelatedPlanet(roboto);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        SectorEntityToken relay = system.addCustomEntity("uw_styx_relay", "Radio Styx", "comm_relay",
                Factions.PIRATES);
        relay.setCircularOrbit(star, 0, 5500, 770);

        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                5, 10, // min/max entities to add
                6500, // radius to start adding at
                1, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                false, // whether to use custom or system-name based names
                false); // habitable allowed

        MarketAPI scrapyardMarket = addMarketplace(Factions.PIRATES, arigato,
                null,
                "Arigato", 4, // 2 industry limit
                new ArrayList<>(Arrays.asList(
                        Conditions.POPULATION_4,
                        Conditions.RUINS_VAST)),
                new ArrayList<>(Arrays.asList(
                        new ArrayList<>(Arrays.asList(Industries.POPULATION)),
                        new ArrayList<>(Arrays.asList(Industries.SPACEPORT, Items.FULLERENE_SPOOL)),
                        new ArrayList<>(Arrays.asList(Industries.TECHMINING)), // Industry 
                        new ArrayList<>(Arrays.asList(Industries.PATROLHQ)),
                        new ArrayList<>(Arrays.asList(Industries.WAYSTATION)),
                        new ArrayList<>(Arrays.asList(Industries.ORBITALWORKS)), // Industry
                        new ArrayList<>(Arrays.asList(Industries.ORBITALSTATION_MID)))),
                new ArrayList<>(Arrays.asList(
                        Submarkets.SUBMARKET_OPEN,
                        "uw_scrapyard_submarket",
                        Submarkets.SUBMARKET_STORAGE)),
                0.3f,
                true
        );

        //ExerelinConstants.MEMORY_KEY_UNINVADABLE
        scrapyardMarket.getMemoryWithoutUpdate().set("$nex_uninvadable", true);

        system.addScript(new UW_DickersonFleetManager(scrapyardMarket));
        system.addScript(new Demilitarize(scrapyardMarket));
        system.addScript(new StripRuins(robotoMarket));

        if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
            system.removeTag(Tags.THEME_CORE_UNPOPULATED);
            system.addTag(Tags.THEME_CORE_POPULATED);
        }
    }

    public static class Demilitarize implements EveryFrameScript {

        private final MarketAPI market;

        Demilitarize(MarketAPI market) {
            this.market = market;
        }

        @Override
        public void advance(float amount) {
            if (market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
                market.removeSubmarket(Submarkets.GENERIC_MILITARY);
            }
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }
    }

    public static class StripRuins implements EveryFrameScript {

        private final MarketAPI market;

        StripRuins(MarketAPI market) {
            this.market = market;
        }

        @Override
        public void advance(float amount) {
            market.getMemoryWithoutUpdate().set("$hasUnexploredRuins", false);
            market.getMemoryWithoutUpdate().set("$ruinsExplored", true);
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }
    }
}
