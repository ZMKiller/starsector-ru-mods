package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.thoughtworks.xstream.XStream;
import data.scripts.campaign.UW_MarketRiggerScript;
import data.scripts.campaign.econ.UW_CabalInfluence;
import data.scripts.campaign.events.UW_EventManager;
import data.scripts.campaign.fleets.UW_CabalFleetManager;
import data.scripts.campaign.fleets.UW_PalaceFleet;
import data.scripts.campaign.intel.UW_StarlightGala;
import data.scripts.campaign.intel.bar.CabalBarEventCreator;
import data.scripts.campaign.submarkets.UW_CabalMarketPlugin;
import data.scripts.campaign.submarkets.UW_ScrapyardMarketPlugin;
import data.scripts.util.UW_Util;
import data.scripts.world.underworld.UW_DickersonAssignmentAI;
import data.scripts.world.underworld.UW_DickersonFleetManager;
import data.scripts.world.underworld.UW_Styx;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;

public class UnderworldModPlugin extends BaseModPlugin {

    // Both of these are used only when LunaLib is not available.
    public static boolean Module_StarlightCabal = true;
    private static float cabalFleetFactor = 1f;

    public static boolean hasGraphicsLib = false;
    public static boolean hasMagicLib = false;
    public static boolean hasSWP = false;
    public static boolean hasIndEvo = false;
    public static boolean isExerelin = false;

    private static final String SETTINGS_FILE = "UNDERWORLD_OPTIONS.ini";

    private static final Set<String> CURSED = new HashSet<>();

    static {
        CURSED.add("uw_boss_cancer");
        CURSED.add("uw_boss_corruption");
        CURSED.add("uw_boss_cyst");
        CURSED.add("uw_boss_disease");
        CURSED.add("uw_boss_malignancy");
        CURSED.add("uw_boss_metastasis");
        CURSED.add("uw_boss_pustule");
        CURSED.add("uw_boss_tumor");
        CURSED.add("uw_boss_ulcer");
    }

    public static void syncUnderworldScripts() {
        if (!Global.getSector().hasScript(UW_CabalFleetManager.class)) {
            Global.getSector().addScript(new UW_CabalFleetManager());
        }
        syncCabalMarkets();

        if (!Global.getSector().hasScript(UW_MarketRiggerScript.class)) {
            Global.getSector().addScript(new UW_MarketRiggerScript());
        }
        if (!Global.getSector().hasScript(UW_PalaceFleet.class)) {
            Global.getSector().addScript(new UW_PalaceFleet());
        }
        syncCodex();
    }

    public static void syncUnderworldScriptsExerelin() {
        if (!Global.getSector().hasScript(UW_CabalFleetManager.class)) {
            Global.getSector().addScript(new UW_CabalFleetManager());
        }
        syncCabalMarkets();

        if (!Global.getSector().hasScript(UW_MarketRiggerScript.class)) {
            Global.getSector().addScript(new UW_MarketRiggerScript());
        }
        if (!Global.getSector().hasScript(UW_PalaceFleet.class)) {
            Global.getSector().addScript(new UW_PalaceFleet());
        }
        syncCodex();
    }

    private static void initUnderworldRelationships(SectorAPI sector) {
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI kol = sector.getFaction(Factions.KOL);
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI cabal = sector.getFaction("cabal");

        List<FactionAPI> allFactions = sector.getAllFactions();
        for (FactionAPI curFaction : allFactions) {
            if (curFaction == cabal || curFaction.isNeutralFaction()) {
                continue;
            }
            cabal.setRelationship(curFaction.getId(), RepLevel.HOSTILE);
        }

        cabal.setRelationship(tritachyon.getId(), RepLevel.FAVORABLE);
        cabal.setRelationship("blackrock_driveyards", RepLevel.NEUTRAL);
        cabal.setRelationship("exigency", RepLevel.SUSPICIOUS);
        cabal.setRelationship("exipirated", RepLevel.SUSPICIOUS);
        cabal.setRelationship("templars", RepLevel.SUSPICIOUS);
        cabal.setRelationship(pirates.getId(), RepLevel.SUSPICIOUS);
        cabal.setRelationship(kol.getId(), RepLevel.VENGEFUL);
        cabal.setRelationship(church.getId(), RepLevel.VENGEFUL);
        cabal.setRelationship(path.getId(), RepLevel.VENGEFUL);
        player.setRelationship(cabal.getId(), -0.65f);
    }

    private static void loadSettingsFile() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);

        Module_StarlightCabal = settings.getBoolean("starlightCabal");
        cabalFleetFactor = (float) settings.getDouble("cabalFleetFactor");
    }

    private static void syncCabalMarkets() {
        String cabalMarkets[] = new String[]{"port_tse", "tibicena"};
        if (isStarlightCabalEnabled()) {
            Global.getSector().getFaction("cabal").setShowInIntelTab(true);
            for (String marketStr : cabalMarkets) {
                MarketAPI market = Global.getSector().getEconomy().getMarket(marketStr);
                if (market == null) {
                    // Handles non-Corvus Mode Nexerelin
                    continue;
                }
                if (!market.hasCondition("cabal_influence")) {
                    market.addCondition("cabal_influence");
                    market.addSubmarket("uw_cabalmarket");
                    UW_Util.setMarketInfluence(market, "cabal");
                }
            }
        } else {
            Global.getSector().getFaction("cabal").setShowInIntelTab(false);
            for (String marketStr : cabalMarkets) {
                MarketAPI market = Global.getSector().getEconomy().getMarket(marketStr);
                if (market == null) {
                    continue;
                }
                if (market.hasCondition("cabal_influence")) {
                    market.removeCondition("cabal_influence");
                    market.removeSubmarket("uw_cabalmarket");
                    UW_Util.removeMarketInfluence(market, "cabal");
                }
            }
        }
    }

    public static boolean isStarlightCabalEnabled() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            return Module_StarlightCabal = LunaSettings.getBoolean("underworld", "isStarlightCabalEnabled");
        }
        return Module_StarlightCabal;
    }

    public static float getCabalFleetFactor() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            return LunaSettings.getFloat("underworld", "cabalFleetFactor");
        }
        return cabalFleetFactor;
    }

    @Override
    public void configureXStream(XStream x) {
        x.alias("UW_CabalFleetManager", UW_CabalFleetManager.class);
        x.alias("UW_CabalInfluence", UW_CabalInfluence.class);
        x.alias("UW_CabalMarketPlugin", UW_CabalMarketPlugin.class);
        x.alias("UW_Styx", UW_Styx.class);
        x.alias("UW_DickersonFleetManager", UW_DickersonFleetManager.class);
        x.alias("UW_DickersonAssignmentAI", UW_DickersonAssignmentAI.class);
        x.alias("UW_ScrapyardMarketPlugin", UW_ScrapyardMarketPlugin.class);
        x.alias("UW_EventManager", UW_EventManager.class);
        x.alias("UW_PalaceFleet", UW_PalaceFleet.class);
        x.alias("UW_StarlightGala", UW_StarlightGala.class);
    }

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void onApplicationLoad() throws Exception {
        hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            UW_ModPluginAlt.initGraphicsLib();
        }

        hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        hasSWP = Global.getSettings().getModManager().isModEnabled("swp");
        hasIndEvo = Global.getSettings().getModManager().isModEnabled("IndEvo");

        try {
            loadSettingsFile();
        } catch (Exception e) {
            Global.getLogger(UnderworldModPlugin.class).log(Level.ERROR, "Failed to load settings file! " + e.getMessage());
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (isExerelin) {
            syncUnderworldScriptsExerelin();
        } else {
            syncUnderworldScripts();
        }
        BarEventManager bar = BarEventManager.getInstance();
        if (!bar.hasEventCreator(CabalBarEventCreator.class)) {
            bar.addEventCreator(new CabalBarEventCreator());
        }
    }

    @Override
    public void onNewGame() {
        initUnderworldRelationships(Global.getSector());

        ProcgenUsedNames.notifyUsed("Styx");
        ProcgenUsedNames.notifyUsed("Arigato");
        ProcgenUsedNames.notifyUsed("Roboto");

        if (isExerelin) {
            syncUnderworldScriptsExerelin();
        } else {
            syncUnderworldScripts();
        }
        Global.getSector().addScript(new UW_EventManager());

        UW_ModPluginAlt.initUW();
    }

    @Override
    public void onNewGameAfterProcGen() {
        UW_Styx.generatePt2(Global.getSector());
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        UW_Styx.dickersonifyBaseStaff();

        MarketAPI market = Global.getSector().getEconomy().getMarket("uw_arigato");
        if (market != null) {
            PersonAPI admin = market.getFaction().createRandomPerson(StarSystemGenerator.random);
            admin.setPostId(Ranks.POST_ADMINISTRATOR);
            admin.setRankId(Ranks.SPACE_ADMIRAL);
            admin.getName().setLast("Dickerson");
            admin.setPortraitSprite("graphics/uw/portraits/uw_dickerson_extended_family.png");
            admin.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);

            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);

            for (PersonAPI p : market.getPeopleCopy()) {
                if (Ranks.POST_ADMINISTRATOR.equals(p.getPostId())) {
                    market.removePerson(p);
                    ip.removePerson(p);
                    market.getCommDirectory().removePerson(p);
                    break;
                }
            }

            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);
        }

        PersonAPI person = Global.getSector().getFaction("cabal").createRandomPerson(StarSystemGenerator.random);
        person.setId("uw_zeb");
        person.setName(new FullName("Zeb", "Sparks", Gender.MALE)); // Original name is Zebroid Williams
        person.setRankId(Ranks.FACTION_LEADER);
        person.setPostId(Ranks.FACTION_LEADER);
        person.addTag(Tags.CONTACT_MILITARY);
        person.addTag(Tags.CONTACT_UNDERWORLD);
        person.setImportance(PersonImportance.VERY_HIGH);
        person.setVoice(Voices.VILLAIN); // Maybe create a unique voice?
        person.setGender(Gender.MALE);
        person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "uw_zeb"));
        person.setPersonality("aggressive");
        person.getStats().setLevel(7);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        person.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1); // commander skill
        person.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1); // commander skill
        person.getStats().setSkillLevel(Skills.CYBERNETIC_AUGMENTATION, 1); // commander skill
        ip.addPerson(person);

        person = Global.getSector().getFaction("cabal").createRandomPerson(StarSystemGenerator.random);
        person.setId("uw_peggy");
        person.setName(new FullName("Pigmenta", "Pennington", Gender.FEMALE)); // "Peggy"
        person.setRankId("uw_bursar");
        person.setPostId(Ranks.CITIZEN);
        person.setGender(Gender.FEMALE);
        person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "uw_peggy"));
        ip.addPerson(person);

        person = Global.getSector().getFaction("cabal").createRandomPerson(StarSystemGenerator.random);
        person.setId("uw_shark1");
        person.setName(new FullName("Cozen", "Shylock", Gender.MALE));
        person.setRankId(Ranks.CITIZEN);
        person.setPostId(Ranks.CITIZEN);
        person.setGender(Gender.MALE);
        person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "uw_shark1"));
        ip.addPerson(person);

        person = Global.getSector().getFaction("cabal").createRandomPerson(StarSystemGenerator.random);
        person.setId("uw_shark2");
        person.setName(new FullName("Cuda", "Bilkins", Gender.FEMALE));
        person.setRankId(Ranks.CITIZEN);
        person.setPostId(Ranks.CITIZEN);
        person.setGender(Gender.FEMALE);
        person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "uw_shark2"));
        ip.addPerson(person);
    }

    @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
        if (ship.isFighter()) {
            return null;
        }

        String hullId = ship.getHullSpec().getBaseHullId();
        if (!CURSED.contains(hullId)) {
            return null;
        }

        ShipAIConfig config = new ShipAIConfig();
        config.backingOffWhileNotVentingAllowed = false;
        config.burnDriveIgnoreEnemies = true;
        config.personalityOverride = Personalities.RECKLESS;

        return new PluginPick<>(Global.getSettings().createDefaultShipAI(ship, config), PickPriority.MOD_SPECIFIC);
    }

    @Override
    public void onCodexDataGenerated() {
        syncCodex();
    }

    public static void syncCodex() {
        for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
            if ((spec.getManufacturer() != null) && spec.getManufacturer().contentEquals("Starlight Cabal")) {
                if (!isStarlightCabalEnabled()) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                } else if (spec.getTags() != null) {
                    spec.getTags().remove(Tags.INVISIBLE_IN_CODEX);
                }
            }
        }
        for (HullModSpecAPI spec : Global.getSettings().getAllHullModSpecs()) {
            if ((spec.getManufacturer() != null) && spec.getManufacturer().contentEquals("Starlight Cabal")) {
                if (!isStarlightCabalEnabled()) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                } else if (spec.getTags() != null) {
                    spec.getTags().remove(Tags.INVISIBLE_IN_CODEX);
                }
            }
        }
        if (!isStarlightCabalEnabled()) {
            Global.getSettings().getShipSystemSpec("uw_incubusdrive").addTag(Tags.INVISIBLE_IN_CODEX);
            Global.getSettings().getMarketConditionSpec("cabal_influence").addTag(Tags.INVISIBLE_IN_CODEX);
        } else {
            Global.getSettings().getShipSystemSpec("uw_incubusdrive").getTags().remove(Tags.INVISIBLE_IN_CODEX);
            Global.getSettings().getMarketConditionSpec("cabal_influence").getTags().remove(Tags.INVISIBLE_IN_CODEX);
        }
        if (!hasSWP) {
            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                if (spec.getBaseHullId().startsWith("uw_boss")) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                }
            }
            Global.getSettings().getFighterWingSpec("uw_boss_wasp_wing").addTag(Tags.INVISIBLE_IN_CODEX);
            Global.getSettings().getShipSystemSpec("uw_boss_drone_disco_ball").addTag(Tags.INVISIBLE_IN_CODEX);
        }
    }
}
