package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.missions.HandMeDownFreighter;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.thoughtworks.xstream.XStream;
import data.scripts.campaign.SWP_CampaignPlugin;
import data.scripts.campaign.SWP_ExcelsiorSpawner;
import data.scripts.campaign.SWP_ExcelsiorUnlocker;
import data.scripts.campaign.SWP_IBBFleetEncounterContext;
import data.scripts.campaign.SWP_IBBInteractionDialogPlugin;
import data.scripts.campaign.SWP_MarketRiggerScript;
import data.scripts.campaign.fleets.SWP_TriTachyonFleetPlugin;
import data.scripts.campaign.intel.SWP_IBBIntel;
import data.scripts.campaign.intel.SWP_IBBTracker;
import data.scripts.campaign.intel.bar.events.SWP_IBBBarEvent;
import data.scripts.campaign.intel.bar.events.SWP_IBBBarEventCreator;
import data.scripts.campaign.intel.missions.SWP_BuriedTreasure;
import data.scripts.everyframe.SWP_BlockedHullmodDisplayScript;
import data.scripts.shaders.SWP_OmegaDriveShader;
import data.scripts.util.SWP_Util;
import data.scripts.weapons.ai.SWP_EMPBombWeaponAI;
import data.scripts.weapons.ai.SWP_FlareBurstAI;
import data.scripts.weapons.ai.SWP_FlareBurstWeaponAI;
import data.scripts.weapons.ai.SWP_FlareGunWeaponAI;
import data.scripts.weapons.ai.SWP_GodModeWeaponAI;
import data.scripts.weapons.ai.SWP_HornetAI;
import data.scripts.weapons.ai.SWP_RedeemerAI;
import data.scripts.weapons.ai.SWP_RedeemerSubAI;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.json.JSONException;
import org.json.JSONObject;

public class SWPModPlugin extends BaseModPlugin {

    public static final String EMPBOMB_ID = "swp_arcade_empbomb";
    public static final String FLAREBURST_ID = "swp_flareburst";
    public static final String FLAREBURST_MRM_ID = "swp_flareburst_mrm";
    public static final String FLAREGUN_ID = "swp_flaregun";
    public static final String GODMODE_ID = "swp_arcade_godmode";
    public static final String HORNET_MRM_ID = "swp_hornet_mrm";
    public static final String REDEEMER_MISSILE_ID = "swp_redeemer_missile";
    public static final String REDEEMER_SUB_MISSILE_ID = "swp_redeemer_sub_missile";

    public static String EXCELSIOR_DESCRIPTION;

    public static String EXCELSIOR_ALT_DESCRIPTION = "This half-finished ship is barely space-worthy, requiring a variety of exotic replacement components to remain in continuous operation. The mysterious capabilities of this strange vessel outstrip anything you had thought possible. Answers to cloying questions - How was this ship made? Why was it created? - elude you, for her secrets are inscrutable and unknowable.";

    // Both of these are used only when LunaLib is not available.
    public static boolean Module_FamousBounties = true;
    public static boolean Setting_IBBAlwaysRecover = true;

    public static boolean blackrockExists = false;
    public static boolean borkenExists = false;
    public static boolean checkMemory = false;
    public static boolean diableExists = false;
    public static boolean exigencyExists = false;
    public static boolean hasDynaSector = false;
    public static boolean hasGraphicsLib = false;
    public static boolean hasMagicLib = false;
    public static boolean hasUnderworld = false;
    public static boolean iceExists = false;
    public static boolean imperiumExists = false;
    public static boolean isExerelin = false;
    public static boolean junkPiratesExists = false;
    public static boolean oraExists = false;
    public static boolean scalarTechExists = false;
    public static boolean scyExists = false;
    public static boolean shadowyardsExists = false;
    public static boolean templarsExists = false;
    public static boolean tiandongExists = false;
    public static boolean tyradorExists = false;
    public static boolean dmeExists = false;
    public static boolean arkgneisisExists = false;

    private static final String SETTINGS_FILE = "SWP_OPTIONS.ini";

    static {
        HandMeDownFreighter.HULLS.add("swp_circe_Hull", 0.5f);
    }

    public static void syncSWPScripts() {
        Global.getSector().registerPlugin(new SWP_CampaignPlugin());

        if (!Global.getSector().hasScript(SWP_IBBTracker.class)) {
            SWP_IBBTracker ibbTracker = new SWP_IBBTracker();
            Global.getSector().addScript(ibbTracker);
        }

        BarEventManager bar = BarEventManager.getInstance();
        if (bar != null) {
            if (!bar.hasEventCreator(SWP_IBBBarEventCreator.class)) {
                bar.addEventCreator(new SWP_IBBBarEventCreator());
            }
        }

        SWP_Util.initExtraFactions();

        if (!Global.getSector().hasScript(SWP_MarketRiggerScript.class)) {
            Global.getSector().addScript(new SWP_MarketRiggerScript());
        }

        if (!Global.getSector().getGenericPlugins().hasPlugin(SWP_TriTachyonFleetPlugin.class)) {
            Global.getSector().getGenericPlugins().addPlugin(new SWP_TriTachyonFleetPlugin(), true);
        }

        if (!SharedUnlockData.get().isPlayerAwareOfShip("swp_excelsior")) {
            if (!Global.getSector().getListenerManager().hasListenerOfClass(SWP_ExcelsiorUnlocker.class)) {
                Global.getSector().getListenerManager().addListener(new SWP_ExcelsiorUnlocker(), true);
            }
        }

        syncIBBCodex();
    }

    public static void syncSWPScriptsExerelin() {
        Global.getSector().registerPlugin(new SWP_CampaignPlugin());

        if (!Global.getSector().hasScript(SWP_IBBTracker.class)) {
            SWP_IBBTracker ibbTracker = new SWP_IBBTracker();
            Global.getSector().addScript(ibbTracker);
        }

        BarEventManager bar = BarEventManager.getInstance();
        if (bar != null) {
            if (!bar.hasEventCreator(SWP_IBBBarEventCreator.class)) {
                bar.addEventCreator(new SWP_IBBBarEventCreator());
            }
        }

        SWP_Util.initExtraFactions();

        if (!Global.getSector().hasScript(SWP_MarketRiggerScript.class)) {
            Global.getSector().addScript(new SWP_MarketRiggerScript());
        }

        if (!Global.getSector().getGenericPlugins().hasPlugin(SWP_TriTachyonFleetPlugin.class)) {
            Global.getSector().getGenericPlugins().addPlugin(new SWP_TriTachyonFleetPlugin(), true);
        }

        if (!SharedUnlockData.get().isPlayerAwareOfShip("swp_excelsior")) {
            if (!Global.getSector().getListenerManager().hasListenerOfClass(SWP_ExcelsiorUnlocker.class)) {
                Global.getSector().getListenerManager().addListener(new SWP_ExcelsiorUnlocker(), true);
            }
        }

        syncIBBCodex();
    }

    private static void initGraphicsLib() {
        ShaderLib.init();

        if (ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed()) {
            LightData.readLightDataCSV("data/lights/swp_light_data.csv");
            TextureData.readTextureDataCSV("data/lights/swp_texture_data.csv");
            ShaderLib.addShaderAPI(new SWP_OmegaDriveShader());
        }
    }

    private static void loadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);

        Module_FamousBounties = settings.getBoolean("famousBounties");
        Setting_IBBAlwaysRecover = settings.getBoolean("IBBAlwaysRecover");
    }

    public static boolean isIBBEnabled() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            return Module_FamousBounties = LunaSettings.getBoolean("swp", "famousBounties");
        }
        return Module_FamousBounties;
    }

    public static boolean isIBBAlwaysRecover() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            return Setting_IBBAlwaysRecover = LunaSettings.getBoolean("swp", "IBBAlwaysRecover");
        }
        return Setting_IBBAlwaysRecover;
    }

    @Override
    public void configureXStream(XStream x) {
        x.alias("SWP_IBBBE", SWP_IBBBarEvent.class);
        x.alias("SWP_IBBBEC", SWP_IBBBarEventCreator.class);
        x.alias("SWP_IBBI", SWP_IBBIntel.class);
        x.alias("SWP_IBBT", SWP_IBBTracker.class);
        x.alias("SWP_CP", SWP_CampaignPlugin.class);
        x.alias("SWP_IBBFEC", SWP_IBBFleetEncounterContext.class);
        x.alias("SWP_IBBIDP", SWP_IBBInteractionDialogPlugin.class);
        x.alias("SWP_TTFP", SWP_TriTachyonFleetPlugin.class);
        x.alias("SWP_EU", SWP_ExcelsiorUnlocker.class);
    }

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void onApplicationLoad() throws Exception {
        hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            initGraphicsLib();
        }

        hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");

        try {
            loadSettings();
        } catch (Exception e) {
            Global.getLogger(SWPModPlugin.class).log(Level.ERROR, "Settings loading failed! " + e.getMessage());
        }

        isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        hasUnderworld = Global.getSettings().getModManager().isModEnabled("underworld");
        hasDynaSector = Global.getSettings().getModManager().isModEnabled("dynasector");

        borkenExists = Global.getSettings().getModManager().isModEnabled("fob");
        iceExists = Global.getSettings().getModManager().isModEnabled("nbj_ice");
        imperiumExists = Global.getSettings().getModManager().isModEnabled("Imperium");
        templarsExists = Global.getSettings().getModManager().isModEnabled("Templars");
        blackrockExists = Global.getSettings().getModManager().isModEnabled("blackrock_driveyards");
        exigencyExists = Global.getSettings().getModManager().isModEnabled("exigency");
        shadowyardsExists = Global.getSettings().getModManager().isModEnabled("shadow_ships");
        junkPiratesExists = Global.getSettings().getModManager().isModEnabled("junk_pirates_release");
        scyExists = Global.getSettings().getModManager().isModEnabled("SCY");
        tiandongExists = Global.getSettings().getModManager().isModEnabled("THI");
        diableExists = Global.getSettings().getModManager().isModEnabled("diableavionics");
        oraExists = Global.getSettings().getModManager().isModEnabled("ORA");
        tyradorExists = Global.getSettings().getModManager().isModEnabled("TS_Coalition");
        dmeExists = Global.getSettings().getModManager().isModEnabled("istl_dam");
        scalarTechExists = Global.getSettings().getModManager().isModEnabled("tahlan_scalartech");
        arkgneisisExists = Global.getSettings().getModManager().isModEnabled("ArkLeg");

        EXCELSIOR_DESCRIPTION = Global.getSettings().getDescription("swp_excelsior", Description.Type.SHIP).getText1();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientScript(new SWP_BlockedHullmodDisplayScript());

        if (isExerelin) {
            syncSWPScriptsExerelin();
        } else {
            syncSWPScripts();
        }

        SWP_IBBTracker ibbTracker = SWP_IBBTracker.getTracker();
        ibbTracker.refresh();

        if (Global.getSector().getMemoryWithoutUpdate().contains(SWP_BuriedTreasure.EXCELSIOR_DESC_ALT_KEY)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(SWP_BuriedTreasure.EXCELSIOR_DESC_ALT_KEY)) {
            Global.getSettings().getDescription("swp_excelsior", Description.Type.SHIP).setText1(EXCELSIOR_ALT_DESCRIPTION);
        } else {
            Global.getSettings().getDescription("swp_excelsior", Description.Type.SHIP).setText1(EXCELSIOR_DESCRIPTION);
        }
    }

    @Override
    public void onNewGame() {
        if (isExerelin) {
            syncSWPScriptsExerelin();
        } else {
            syncSWPScripts();
        }

        SWP_IBBTracker ibbTracker = SWP_IBBTracker.getTracker();
        ibbTracker.refresh();
    }

    @Override
    public void onNewGameAfterTimePass() {
        SectorAPI sector = Global.getSector();
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction != player) {
                faction.setRelationship("famous_bounty", RepLevel.NEUTRAL);
            }
        }
        player.setRelationship("famous_bounty", -1f);

        SWP_ExcelsiorSpawner.spawnExcelsior(Global.getSector());
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        if (HORNET_MRM_ID.contentEquals(missile.getProjectileSpecId())) {
            return new PluginPick<>(new SWP_HornetAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        if (FLAREBURST_MRM_ID.contentEquals(missile.getProjectileSpecId())) {
            return new PluginPick<>(new SWP_FlareBurstAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        if (REDEEMER_MISSILE_ID.contentEquals(missile.getProjectileSpecId())) {
            return new PluginPick<>(new SWP_RedeemerAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        if (REDEEMER_SUB_MISSILE_ID.contentEquals(missile.getProjectileSpecId())) {
            return new PluginPick<>(new SWP_RedeemerSubAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }

    @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
        Set<String> derelicts = new HashSet<>(3);
        String hullId = ship.getHullSpec().getBaseHullId();
        derelicts.add("swp_wall");
        derelicts.add("swp_wall_left");
        derelicts.add("swp_wall_right");
        if (derelicts.contains(hullId) && ship.getVariant().hasHullMod(HullMods.AUTOMATED)) {
            ShipAIConfig config = new ShipAIConfig();
            config.alwaysStrafeOffensively = true;
            config.backingOffWhileNotVentingAllowed = false;
            config.turnToFaceWithUndamagedArmor = false;
            config.burnDriveIgnoreEnemies = true;
            config.personalityOverride = Personalities.RECKLESS;
            return new PluginPick<>(Global.getSettings().createDefaultShipAI(ship, config), PickPriority.MOD_SET);
        }

        /* Don't mess with tournaments */
        if (!Global.getSettings().getModManager().isModEnabled("aibattles")) {
            if (hullId.contentEquals("swp_nightwalker")) {
                ShipAIConfig config = new ShipAIConfig();
                config.personalityOverride = SWP_Util.getMoreAggressivePersonality(member, ship);
                return new PluginPick<>(Global.getSettings().createDefaultShipAI(ship, config), PickPriority.MOD_SET);
            }
        }
        return null;
    }

    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        if (FLAREGUN_ID.contentEquals(weapon.getId())) {
            return new PluginPick<>(new SWP_FlareGunWeaponAI(weapon), PickPriority.MOD_SET);
        }
        if (FLAREBURST_ID.contentEquals(weapon.getId())) {
            return new PluginPick<>(new SWP_FlareBurstWeaponAI(weapon), PickPriority.MOD_SET);
        }
        if (GODMODE_ID.contentEquals(weapon.getId())) {
            return new PluginPick<>(new SWP_GodModeWeaponAI(weapon), PickPriority.MOD_SET);
        }
        if (EMPBOMB_ID.contentEquals(weapon.getId())) {
            return new PluginPick<>(new SWP_EMPBombWeaponAI(weapon), PickPriority.MOD_SET);
        }
        return null;
    }

    @Override
    public void onAboutToStartGeneratingCodex() {
        Global.getSettings().getHullSpec("swp_excelsior_boss").setLogisticsNAReason("Not available");
        Global.getSettings().getDescription("swp_excelsior", Description.Type.SHIP).setText1(EXCELSIOR_DESCRIPTION);
    }

    @Override
    public void onCodexDataGenerated() {
        syncIBBCodex();

        CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("swp_ray_core"), CodexDataV2.getShipEntryId("swp_beholder"));
        CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("swp_ray_core"), CodexDataV2.getShipSystemEntryId("swp_drone_ray"));
        CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("swp_ray_core"), CodexDataV2.getShipEntryId("swp_boss_beholder"));
        CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("swp_ray_core"), CodexDataV2.getShipSystemEntryId("swp_boss_drone_death_ray"));
        CodexDataV2.makeUnrelated(CodexDataV2.getShipEntryId("swp_excelsior"), CodexDataV2.getShipEntryId("swp_excelsior_boss"));
    }

    /* These are heuristics that may not always be correct... */
    public static void syncIBBCodex() {
        for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
            if ((spec.getBaseHullId().contains("_boss") && (spec.getDesignation().contains("Mod-") || spec.getManufacturer().contentEquals("Cursed")))
                    || spec.getBaseHullId().contentEquals("ii_boss_titanx") || spec.getBaseHullId().contentEquals("swp_boss_sporeship")) {
                if (!isIBBEnabled()) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                } else if (spec.getTags() != null) {
                    spec.getTags().remove(Tags.INVISIBLE_IN_CODEX);
                }
            }
        }
        for (WeaponSpecAPI spec : Global.getSettings().getActuallyAllWeaponSpecs()) {
            if (spec.getWeaponId().contains("_boss") && (spec.getManufacturer() != null) && spec.getManufacturer().contains("Unique")) {
                if (!isIBBEnabled()) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                } else if (spec.getTags() != null) {
                    spec.getTags().remove(Tags.INVISIBLE_IN_CODEX);
                }
            }
        }
        for (FighterWingSpecAPI spec : Global.getSettings().getAllFighterWingSpecs()) {
            if (spec.getId().contains("_boss") && (spec.getVariant() != null) && (spec.getVariant().getHullSpec() != null) && (spec.getVariant().getHullSpec().getManufacturer() != null)
                    && spec.getVariant().getHullSpec().getManufacturer().contains("Unique")) {
                if (!isIBBEnabled()) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                } else if (spec.getTags() != null) {
                    spec.getTags().remove(Tags.INVISIBLE_IN_CODEX);
                }
            }
        }
        for (HullModSpecAPI spec : Global.getSettings().getAllHullModSpecs()) {
            if (spec.getId().contains("_boss") && (spec.getManufacturer() != null) && spec.getManufacturer().contains("Unique")) {
                if (!isIBBEnabled()) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                } else if (spec.getTags() != null) {
                    spec.getTags().remove(Tags.INVISIBLE_IN_CODEX);
                }
            }
        }
        for (ShipSystemSpecAPI spec : Global.getSettings().getAllShipSystemSpecs()) {
            if (spec.getId().contains("_boss_") || spec.getId().contentEquals("ii_empburst")) {
                if (!isIBBEnabled()) {
                    spec.addTag(Tags.INVISIBLE_IN_CODEX);
                } else if (spec.getTags() != null) {
                    spec.getTags().remove(Tags.INVISIBLE_IN_CODEX);
                }
            }
        }
        if (!imperiumExists) {
            Global.getSettings().getHullSpec("swp_boss_excelsior").addTag(Tags.INVISIBLE_IN_CODEX);
            Global.getSettings().getWeaponSpec("swp_boss_roycocannon").addTag(Tags.INVISIBLE_IN_CODEX);
            Global.getSettings().getHullModSpec("swp_no_package").addTag(Tags.INVISIBLE_IN_CODEX);
        }
    }
}
