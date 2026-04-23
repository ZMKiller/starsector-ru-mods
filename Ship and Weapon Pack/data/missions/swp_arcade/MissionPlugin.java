package data.missions.swp_arcade;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.SWPModPlugin;
import data.scripts.util.DS_Defs.Archetype;
import data.scripts.util.SWP_Multi;
import data.scripts.util.SWP_Util;
import data.scripts.variants.DS_VariantRandomizer;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.dark.shaders.post.PostProcessShader;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class MissionPlugin extends BaseEveryFrameCombatPlugin {

    private static final Map<HullSize, Integer> AURA_MOD = new HashMap<>(6);
    private static final String DATA_KEY = "SWP_Arcade";
    private static final String DATA_KEY_PLUGIN = "SWP_Arcade_Plugin";
    private static final WeightedRandomPicker<String> FACTIONS = new WeightedRandomPicker<>();
    private static final WeightedRandomPicker<String> ROLES = new WeightedRandomPicker<>();
    private static final WeightedRandomPicker<String> SPECIAL_SHIPS = new WeightedRandomPicker<>();

    private static final Vector2f ZERO = new Vector2f();

    private static boolean first = true;
    private static boolean campaignMode = false;

    static final Map<String, Float> BOSS_SHIPS = new HashMap<>(9);

    static {
        ROLES.add(ShipRoles.COMBAT_SMALL, 4f);
        ROLES.add(ShipRoles.COMBAT_MEDIUM, 5f);
        ROLES.add(ShipRoles.COMBAT_LARGE, 4f);
        ROLES.add(ShipRoles.COMBAT_CAPITAL, 3f);
        ROLES.add(ShipRoles.PHASE_SMALL, 0.2f);
        ROLES.add(ShipRoles.PHASE_MEDIUM, 0.25f);
        ROLES.add(ShipRoles.PHASE_LARGE, 0.2f);
        ROLES.add(ShipRoles.PHASE_CAPITAL, 0.15f);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_SMALL, 2f);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_MEDIUM, 2.5f);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_LARGE, 2f);
        ROLES.add(ShipRoles.CIV_RANDOM, 0.5f);
        ROLES.add(ShipRoles.CARRIER_SMALL, 1f);
        ROLES.add(ShipRoles.CARRIER_MEDIUM, 1.25f);
        ROLES.add(ShipRoles.CARRIER_LARGE, 1f);
        ROLES.add(ShipRoles.FREIGHTER_SMALL, 0.5f);
        ROLES.add(ShipRoles.FREIGHTER_MEDIUM, 0.625f);
        ROLES.add(ShipRoles.FREIGHTER_LARGE, 0.5f);
        ROLES.add(ShipRoles.TANKER_SMALL, 0.5f);
        ROLES.add(ShipRoles.TANKER_MEDIUM, 0.625f);
        ROLES.add(ShipRoles.TANKER_LARGE, 0.5f);
        ROLES.add(ShipRoles.PERSONNEL_SMALL, 0.5f);
        ROLES.add(ShipRoles.PERSONNEL_MEDIUM, 0.625f);
        ROLES.add(ShipRoles.PERSONNEL_LARGE, 0.5f);
        ROLES.add(ShipRoles.TUG, 0.5f);
        ROLES.add(ShipRoles.CRIG, 0.5f);
        ROLES.add(ShipRoles.UTILITY, 0.5f);
    }

    static {
        BOSS_SHIPS.put("swp_arcade_superhyperion", 4f);
        BOSS_SHIPS.put("swp_arcade_oberon", 2f);
        BOSS_SHIPS.put("swp_arcade_ultron", 3f);
        BOSS_SHIPS.put("swp_arcade_zeus", 2f);
        BOSS_SHIPS.put("swp_arcade_ezekiel", 4f);
        BOSS_SHIPS.put("swp_arcade_cristarium", 2f);
        BOSS_SHIPS.put("swp_arcade_zero", 4f);
        BOSS_SHIPS.put("swp_arcade_superzero", 5f);
        BOSS_SHIPS.put("swp_arcade_hyperzero", 6f);
    }

    static {
        SPECIAL_SHIPS.add("ii_boss_dominus_cus", 1f);
        SPECIAL_SHIPS.add("ii_boss_titanx_cus", 0.5f);
        SPECIAL_SHIPS.add("msp_boss_potniaBis_cus", 4f);
        SPECIAL_SHIPS.add("ms_boss_charybdis_cus", 3f);
        SPECIAL_SHIPS.add("ms_boss_mimir_cus", 2f);
        SPECIAL_SHIPS.add("tem_boss_paladin_cus", 1f);
        SPECIAL_SHIPS.add("tem_boss_archbishop_cus", 0.5f);
        SPECIAL_SHIPS.add("swp_boss_phaeton_cus", 4f);
        SPECIAL_SHIPS.add("swp_boss_hammerhead_cus", 5f);
        SPECIAL_SHIPS.add("swp_boss_sunder_cus", 5f);
        SPECIAL_SHIPS.add("swp_boss_tarsus_cus", 5f);
        SPECIAL_SHIPS.add("swp_boss_medusa_cus", 4f);
        SPECIAL_SHIPS.add("swp_boss_falcon_cus", 4f);
        SPECIAL_SHIPS.add("swp_boss_paragon_cus", 1f);
        SPECIAL_SHIPS.add("swp_boss_mule_cus", 3f);
        SPECIAL_SHIPS.add("swp_boss_aurora_cus", 2f);
        SPECIAL_SHIPS.add("swp_boss_odyssey_cus", 1f);
        SPECIAL_SHIPS.add("swp_boss_atlas_cus", 2f);
        SPECIAL_SHIPS.add("swp_boss_afflictor_cus", 4f);
        SPECIAL_SHIPS.add("swp_boss_brawler_cus", 5f);
        SPECIAL_SHIPS.add("swp_boss_cerberus_cus", 5f);
        SPECIAL_SHIPS.add("swp_boss_dominator_cus", 3f);
        SPECIAL_SHIPS.add("swp_boss_doom_cus", 2f);
        SPECIAL_SHIPS.add("swp_boss_euryale_cus", 3f);
        SPECIAL_SHIPS.add("swp_boss_lasher_b_cus", 5f);
        SPECIAL_SHIPS.add("swp_boss_lasher_r_cus", 5f);
        SPECIAL_SHIPS.add("swp_boss_onslaught_cus", 1f);
        SPECIAL_SHIPS.add("swp_boss_shade_cus", 4f);
        SPECIAL_SHIPS.add("swp_boss_eagle_cus", 3f);
        SPECIAL_SHIPS.add("swp_boss_beholder_cus", 3f);
        SPECIAL_SHIPS.add("swp_boss_dominator_luddic_path_cus", 4f);
        SPECIAL_SHIPS.add("swp_boss_onslaught_luddic_path_cus", 2f);
        SPECIAL_SHIPS.add("swp_boss_conquest_cus", 1f);
        SPECIAL_SHIPS.add("swp_boss_frankenstein_cus", 2f);
        SPECIAL_SHIPS.add("swp_boss_sporeship_cus", 0.5f);
        SPECIAL_SHIPS.add("swp_boss_excelsior_cus", 2f);
        SPECIAL_SHIPS.add("uw_boss_astral_cus", 1f);
        SPECIAL_SHIPS.add("uw_boss_cancer_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_corruption_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_cyst_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_disease_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_malignancy_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_metastasis_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_pustule_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_tumor_cur", 1f);
        SPECIAL_SHIPS.add("uw_boss_ulcer_cur", 1f);
        SPECIAL_SHIPS.add("swp_arcade_banana_cus", 1f);
        SPECIAL_SHIPS.add("tiandong_boss_wuzhang_cus", 2f);
        SPECIAL_SHIPS.add("pack_bulldog_bullseye_cus", 3f);
        SPECIAL_SHIPS.add("pack_pitbull_bullseye_cus", 4f);
        SPECIAL_SHIPS.add("pack_komondor_bullseye_cus", 4f);
        SPECIAL_SHIPS.add("pack_schnauzer_bullseye_cus", 5f);
        SPECIAL_SHIPS.add("diableavionics_IBBgulf_cus", 2f);
        SPECIAL_SHIPS.add("sun_ice_ihs_exp", 3f);
        SPECIAL_SHIPS.add("FOB_boss_rast_cus", 1f);
        SPECIAL_SHIPS.add("tahlan_vestige_boss", 4f);
        SPECIAL_SHIPS.add("loamtp_macnamara_boss", 4f);
    }

    static {
        AURA_MOD.put(HullSize.FIGHTER, 1);
        AURA_MOD.put(HullSize.FRIGATE, 2);
        AURA_MOD.put(HullSize.DESTROYER, 4);
        AURA_MOD.put(HullSize.DEFAULT, 4);
        AURA_MOD.put(HullSize.CRUISER, 6);
        AURA_MOD.put(HullSize.CAPITAL_SHIP, 8);
    }

    private HealthBar bar1 = null;
    private HealthBar bar2 = null;
    private HealthBar bar3 = null;
    private float bossAmmoRegenTimer = 0f;
    private float bossFlash = 0f;
    private int bossLevel = 0;
    private float buffLevel = 0f;
    private boolean combatOver = false;
    private CombatEngineAPI engine;
    private float mapX = 0f;
    private float mapY = 0f;
    private float maxPoints = 50f;
    private ProgressBar pbar = null;
    private int points = 0;
    private static int pointsStatic = 0;    // only updated at game start/end, used for campaign stuff to fetch
    private float scale = 1f;
    private float snapback = 0f;
    private int threshold = 0;
    private int ultralevel = 0;
    private float warningBeep = 0f;

    public static int getPoints() {
        return pointsStatic;
    }

    public int getBossLevel() {
        return bossLevel;
    }

    public void setCampaignMode(boolean mode) {
        campaignMode = mode;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        //SWP_MusicPlayer.clearPlaylistOnEngineChange();
        ShipAPI playerShip = engine.getPlayerShip();

        pbar.setProgress(Math.min((maxPoints - 50f) / 87.5f, 1f));
        pbar.render();

        engine.getFleetManager(FleetSide.PLAYER).setSuppressDeploymentMessages(true);
        engine.getFleetManager(FleetSide.ENEMY).setSuppressDeploymentMessages(true);
        if (!engine.isPaused()) {
            bossFlash += amount;
            bossAmmoRegenTimer += amount;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, Integer> shipTypes = localData.shipTypes;
        final Map<ShipAPI, Float> shipWorths = localData.shipWorths;

        float realBuff = (float) Math.pow(buffLevel * 0.33f, 0.4);
        float mookBuff = (float) Math.pow(buffLevel * 0.33f * scale, 0.7);
        List<ShipAPI> bufflist = engine.getShips();
        for (ShipAPI ship : bufflist) {
            if (ship != playerShip && ship.isAlive()) {
                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("arcade", 0.75f + mookBuff);
                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("arcade", 0.75f + mookBuff);
                ship.getMutableStats().getBallisticRoFMult().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getEnergyRoFMult().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("arcade", 0.75f + mookBuff);
                ship.getMutableStats().getFluxDissipation().modifyMult("arcade", 0.75f + mookBuff);
                ship.getMutableStats().getFluxCapacity().modifyMult("arcade", 0.75f + mookBuff * 0.5f);
                ship.getMutableStats().getAcceleration().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getDeceleration().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("arcade", 1f / (1f + mookBuff * 0.75f));
                ship.getMutableStats().getHullDamageTakenMult().modifyMult("arcade", 1f / (1f + mookBuff * 0.75f));
                ship.getMutableStats().getMaxSpeed().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("arcade", 1f + mookBuff * 0.35f);
                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("arcade", 1f + mookBuff * 0.35f);
                ship.getMutableStats().getWeaponHealthBonus().modifyMult("arcade", 1f + mookBuff * 0.5f);
                ship.getMutableStats().getMissileRoFMult().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getMissileGuidance().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getWeaponTurnRateBonus().modifyMult("arcade", 1f + mookBuff * 0.1f);
                ship.getMutableStats().getMissileMaxSpeedBonus().modifyMult("arcade", 1f + mookBuff * 0.25f);
                ship.getMutableStats().getMissileAccelerationBonus().modifyMult("arcade", 1f + mookBuff * 0.25f);
                ship.getMutableStats().getMissileMaxTurnRateBonus().modifyMult("arcade", 1f + mookBuff * 0.25f);
                ship.getMutableStats().getMissileTurnAccelerationBonus().modifyMult("arcade", 1f + mookBuff * 0.25f);
                ship.getMutableStats().getProjectileSpeedMult().modifyMult("arcade", 1f + mookBuff * 0.35f);
                ship.getMutableStats().getShieldUpkeepMult().modifyMult("arcade", 1f + mookBuff * 1.25f);
                ship.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("arcade", 1f + mookBuff);
                ship.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("arcade", 1f + mookBuff * 1.25f);
                if (ship.getOriginalOwner() != 1) {
                    ship.setOriginalOwner(1);
                    ship.setOwner(1);
                }
                if (ship.isFighter() && ship.getWing() != null) {
                    ship.getWing().setWingOwner(1);
                }

                float shipRadius = SWP_Util.effectiveRadius(ship);

                ShipAPI shipForStats = ship;
                if (shipForStats.getWing() != null) {
                    shipForStats = shipForStats.getWing().getSourceShip();
                }
                if (shipTypes.containsKey(shipForStats)) {
                    switch (shipTypes.get(shipForStats)) {
                        case 1 -> {
                            for (int i = 0; i < AURA_MOD.get(ship.getHullSize()); i++) {
                                if (Math.random() < (amount * 6f) && !engine.isPaused()) {
                                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                            shipRadius),
                                            ZERO,
                                            (float) Math.random() * 12.5f + 2.5f, (float) Math.random()
                                            * 1f, 1f, new Color(255, 255, 100));
                                }
                            }
                            if ((int) (engine.getTotalElapsedTime(true) * 60f) % 15 == 0) {
                                ship.getSpriteAPI().setColor(new Color(255, 255, 100));
                                for (WeaponAPI weapon : ship.getAllWeapons()) {
                                    if (weapon.getSlot().isHidden()) {
                                        continue;
                                    }
                                    if (weapon.getSprite() != null) {
                                        weapon.getSprite().setColor(new Color(255, 255, 100));
                                    }
                                    if (weapon.getBarrelSpriteAPI() != null) {
                                        weapon.getBarrelSpriteAPI().setColor(new Color(255, 255, 100));
                                    }
                                }
                                if (ship.getDeployedDrones() != null) {
                                    for (ShipAPI drone : ship.getDeployedDrones()) {
                                        drone.getSpriteAPI().setColor(new Color(255, 255, 100));
                                        drone.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.5f);
                                        drone.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.5f);
                                        drone.getMutableStats().getShieldDamageTakenMult().modifyMult("mook", 0.5f);
                                        drone.getMutableStats().getWeaponDamageTakenMult().modifyMult("mook", 0.25f);
                                        drone.getMutableStats().getEngineDamageTakenMult().modifyMult("mook", 0.25f);
                                        drone.getMutableStats().getAcceleration().modifyMult("mook", 0.5f);
                                        drone.getMutableStats().getDeceleration().modifyMult("mook", 0.5f);
                                        drone.getMutableStats().getMaxSpeed().modifyMult("mook", 0.5f);
                                        drone.getMutableStats().getShieldTurnRateMult().modifyMult("mook", 0.25f);
                                        drone.getMutableStats().getShieldUnfoldRateMult().modifyMult("mook", 0.5f);
                                        if (drone.getShield() != null) {
                                            drone.getShield().setInnerColor(new Color(255, 255, 100));
                                            drone.getShield().setRingColor(new Color(255, 255, 180));
                                        }
                                        if (drone.getEngineController() != null) {
                                            drone.getEngineController().fadeToOtherColor(this, new Color(255, 255, 180),
                                                    new Color(255, 255, 100), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.isShipWithModules()) {
                                    for (ShipAPI child : SWP_Multi.getChildren(ship)) {
                                        child.getSpriteAPI().setColor(new Color(255, 255, 100));
                                        child.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.5f);
                                        child.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.5f);
                                        child.getMutableStats().getShieldDamageTakenMult().modifyMult("mook", 0.5f);
                                        child.getMutableStats().getWeaponDamageTakenMult().modifyMult("mook", 0.25f);
                                        child.getMutableStats().getEngineDamageTakenMult().modifyMult("mook", 0.25f);
                                        child.getMutableStats().getAcceleration().modifyMult("mook", 0.5f);
                                        child.getMutableStats().getDeceleration().modifyMult("mook", 0.5f);
                                        child.getMutableStats().getMaxSpeed().modifyMult("mook", 0.5f);
                                        child.getMutableStats().getShieldTurnRateMult().modifyMult("mook", 0.25f);
                                        child.getMutableStats().getShieldUnfoldRateMult().modifyMult("mook", 0.5f);
                                        if (child.getShield() != null) {
                                            child.getShield().setInnerColor(new Color(255, 255, 100));
                                            child.getShield().setRingColor(new Color(255, 255, 180));
                                        }
                                        if (child.getEngineController() != null) {
                                            child.getEngineController().fadeToOtherColor(this, new Color(255, 255, 180),
                                                    new Color(255, 255, 100), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.getShield() != null) {
                                    ship.getShield().setInnerColor(new Color(255, 255, 100));
                                    ship.getShield().setRingColor(new Color(255, 255, 180));
                                }
                                if (ship.getEngineController() != null) {
                                    ship.getEngineController().fadeToOtherColor(this, new Color(255, 255, 180),
                                            new Color(255, 255, 100), 1f, 1f);
                                }
                            }
                            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getShieldDamageTakenMult().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("mook", 0.25f);
                            ship.getMutableStats().getEngineDamageTakenMult().modifyMult("mook", 0.25f);
                            ship.getMutableStats().getAcceleration().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getDeceleration().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getMaxSpeed().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getMaxTurnRate().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getTurnAcceleration().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getShieldTurnRateMult().modifyMult("mook", 0.25f);
                            ship.getMutableStats().getShieldUnfoldRateMult().modifyMult("mook", 0.5f);
                        }
                        case 2 -> {
                            for (int i = 0; i < AURA_MOD.get(ship.getHullSize()); i++) {
                                if (Math.random() < (amount * 9f) && !engine.isPaused()) {
                                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                            shipRadius),
                                            ZERO,
                                            (float) Math.random() * 10f + 5f,
                                            (float) Math.random() * 0.75f + 0.25f, 1f, new Color(255, 150,
                                            100));
                                }
                            }
                            if ((int) (engine.getTotalElapsedTime(true) * 60f) % 15 == 0) {
                                ship.getSpriteAPI().setColor(new Color(255, 150, 100));
                                for (WeaponAPI weapon : ship.getAllWeapons()) {
                                    if (weapon.getSlot().isHidden()) {
                                        continue;
                                    }
                                    if (weapon.getSprite() != null) {
                                        weapon.getSprite().setColor(new Color(255, 150, 100));
                                    }
                                    if (weapon.getBarrelSpriteAPI() != null) {
                                        weapon.getBarrelSpriteAPI().setColor(new Color(255, 150, 100));
                                    }
                                }
                                if (ship.getDeployedDrones() != null) {
                                    for (ShipAPI drone : ship.getDeployedDrones()) {
                                        drone.getSpriteAPI().setColor(new Color(255, 150, 100));
                                        drone.getMutableStats().getFluxDissipation().modifyMult("mook", 2f);
                                        drone.getMutableStats().getFluxCapacity().modifyMult("mook", 2f);
                                        drone.getMutableStats().getBallisticRoFMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getEnergyRoFMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getMissileRoFMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getBeamWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getBallisticWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getEnergyWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getMissileWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("mook", 2f);
                                        drone.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("mook", 2f);
                                        if (drone.getShield() != null) {
                                            drone.getShield().setInnerColor(new Color(255, 150, 100));
                                            drone.getShield().setRingColor(new Color(255, 205, 180));
                                        }
                                        if (drone.getEngineController() != null) {
                                            drone.getEngineController().fadeToOtherColor(this, new Color(255, 205, 180),
                                                    new Color(255, 150, 100), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.isShipWithModules()) {
                                    for (ShipAPI child : SWP_Multi.getChildren(ship)) {
                                        child.getSpriteAPI().setColor(new Color(255, 150, 100));
                                        child.getMutableStats().getFluxDissipation().modifyMult("mook", 2f);
                                        child.getMutableStats().getFluxCapacity().modifyMult("mook", 2f);
                                        child.getMutableStats().getBallisticRoFMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getEnergyRoFMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getMissileRoFMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getBeamWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getBallisticWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getEnergyWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getMissileWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("mook", 2f);
                                        child.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("mook", 2f);
                                        if (child.getShield() != null) {
                                            child.getShield().setInnerColor(new Color(255, 150, 100));
                                            child.getShield().setRingColor(new Color(255, 205, 180));
                                        }
                                        if (child.getEngineController() != null) {
                                            child.getEngineController().fadeToOtherColor(this, new Color(255, 205, 180),
                                                    new Color(255, 150, 100), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.getShield() != null) {
                                    ship.getShield().setInnerColor(new Color(255, 150, 100));
                                    ship.getShield().setRingColor(new Color(255, 205, 180));
                                }
                                if (ship.getEngineController() != null) {
                                    ship.getEngineController().fadeToOtherColor(this, new Color(255, 205, 180),
                                            new Color(255, 150, 100), 1f, 1f);
                                }
                            }
                            ship.getMutableStats().getFluxDissipation().modifyMult("mook", 2f);
                            ship.getMutableStats().getFluxCapacity().modifyMult("mook", 2f);
                            ship.getMutableStats().getBallisticRoFMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getEnergyRoFMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getMissileRoFMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("mook", 2f);
                            ship.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("mook", 2f);
                        }
                        case 3 -> {
                            for (int i = 0; i < AURA_MOD.get(ship.getHullSize()); i++) {
                                if (Math.random() < (amount * 12) && !engine.isPaused()) {
                                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                            shipRadius),
                                            ZERO,
                                            (float) Math.random() * 7.5f + 7.5f, (float) Math.random()
                                            * 0.5f + 0.5f, 1f, new Color(50, 150, 255));
                                }
                            }
                            if ((int) (engine.getTotalElapsedTime(true) * 60f) % 15 == 0) {
                                ship.getSpriteAPI().setColor(new Color(50, 150, 255));
                                for (WeaponAPI weapon : ship.getAllWeapons()) {
                                    if (weapon.getSlot().isHidden()) {
                                        continue;
                                    }
                                    if (weapon.getSprite() != null) {
                                        weapon.getSprite().setColor(new Color(50, 150, 255));
                                    }
                                    if (weapon.getBarrelSpriteAPI() != null) {
                                        weapon.getBarrelSpriteAPI().setColor(new Color(50, 150, 255));
                                    }
                                }
                                if (ship.getDeployedDrones() != null) {
                                    for (ShipAPI drone : ship.getDeployedDrones()) {
                                        drone.getSpriteAPI().setColor(new Color(50, 150, 255));
                                        drone.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.5f);
                                        drone.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.25f);
                                        drone.getMutableStats().getWeaponDamageTakenMult().modifyMult("mook", 0.25f);
                                        drone.getMutableStats().getEngineDamageTakenMult().modifyMult("mook", 0.25f);
                                        drone.getMutableStats().getAcceleration().modifyMult("mook", 2f);
                                        drone.getMutableStats().getDeceleration().modifyMult("mook", 2f);
                                        drone.getMutableStats().getMaxSpeed().modifyMult("mook", 2f);
                                        drone.getMutableStats().getFluxDissipation().modifyMult("mook", 3f);
                                        drone.getMutableStats().getFluxCapacity().modifyMult("mook", 3f);
                                        drone.getMutableStats().getBallisticRoFMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getEnergyRoFMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getMissileRoFMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getBeamWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getBallisticWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getEnergyWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getMissileWeaponDamageMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 2f);
                                        drone.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("mook", 2f);
                                        drone.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("mook", 2f);
                                        if (drone.getShield() != null) {
                                            drone.getShield().setInnerColor(new Color(50, 150, 255));
                                            drone.getShield().setRingColor(new Color(155, 205, 255));
                                        }
                                        if (drone.getEngineController() != null) {
                                            drone.getEngineController().fadeToOtherColor(this, new Color(155, 205, 255),
                                                    new Color(50, 150, 255), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.isShipWithModules()) {
                                    for (ShipAPI child : SWP_Multi.getChildren(ship)) {
                                        child.getSpriteAPI().setColor(new Color(50, 150, 255));
                                        child.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.5f);
                                        child.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.25f);
                                        child.getMutableStats().getWeaponDamageTakenMult().modifyMult("mook", 0.25f);
                                        child.getMutableStats().getEngineDamageTakenMult().modifyMult("mook", 0.25f);
                                        child.getMutableStats().getAcceleration().modifyMult("mook", 2f);
                                        child.getMutableStats().getDeceleration().modifyMult("mook", 2f);
                                        child.getMutableStats().getMaxSpeed().modifyMult("mook", 2f);
                                        child.getMutableStats().getFluxDissipation().modifyMult("mook", 3f);
                                        child.getMutableStats().getFluxCapacity().modifyMult("mook", 3f);
                                        child.getMutableStats().getBallisticRoFMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getEnergyRoFMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getMissileRoFMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getBeamWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getBallisticWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getEnergyWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getMissileWeaponDamageMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 2f);
                                        child.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("mook", 2f);
                                        child.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("mook", 2f);
                                        if (child.getShield() != null) {
                                            child.getShield().setInnerColor(new Color(50, 150, 255));
                                            child.getShield().setRingColor(new Color(155, 205, 255));
                                        }
                                        if (child.getEngineController() != null) {
                                            child.getEngineController().fadeToOtherColor(this, new Color(155, 205, 255),
                                                    new Color(50, 150, 255), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.getShield() != null) {
                                    ship.getShield().setInnerColor(new Color(50, 150, 255));
                                    ship.getShield().setRingColor(new Color(155, 205, 255));
                                }
                                if (ship.getEngineController() != null) {
                                    ship.getEngineController().fadeToOtherColor(this, new Color(155, 205, 255),
                                            new Color(50, 150, 255), 1f, 1f);
                                }
                            }
                            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.5f);
                            ship.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.25f);
                            ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("mook", 0.25f);
                            ship.getMutableStats().getEngineDamageTakenMult().modifyMult("mook", 0.25f);
                            ship.getMutableStats().getAcceleration().modifyMult("mook", 2f);
                            ship.getMutableStats().getDeceleration().modifyMult("mook", 2f);
                            ship.getMutableStats().getMaxSpeed().modifyMult("mook", 2f);
                            ship.getMutableStats().getTurnAcceleration().modifyMult("mook", 1.5f);
                            ship.getMutableStats().getMaxTurnRate().modifyMult("mook", 1.5f);
                            ship.getMutableStats().getFluxDissipation().modifyMult("mook", 3f);
                            ship.getMutableStats().getFluxCapacity().modifyMult("mook", 3f);
                            ship.getMutableStats().getBallisticRoFMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getEnergyRoFMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getMissileRoFMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 2f);
                            ship.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("mook", 2f);
                            ship.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("mook", 2f);
                        }
                        case 4 -> {
                            for (int i = 0; i < AURA_MOD.get(ship.getHullSize()); i++) {
                                if (Math.random() < (amount * 6f) && !engine.isPaused()) {
                                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                            shipRadius),
                                            ZERO,
                                            (float) Math.random() * 12.5f + 2.5f, (float) Math.random()
                                            * 1f, 1f, new Color(100, 255, 150));
                                }
                            }
                            if ((int) (engine.getTotalElapsedTime(true) * 60f) % 15 == 0) {
                                ship.getSpriteAPI().setColor(new Color(100, 255, 150));
                                for (WeaponAPI weapon : ship.getAllWeapons()) {
                                    if (weapon.getSlot().isHidden()) {
                                        continue;
                                    }
                                    if (weapon.getSprite() != null) {
                                        weapon.getSprite().setColor(new Color(100, 255, 150));
                                    }
                                    if (weapon.getBarrelSpriteAPI() != null) {
                                        weapon.getBarrelSpriteAPI().setColor(new Color(100, 255, 150));
                                    }
                                }
                                if (ship.getDeployedDrones() != null) {
                                    for (ShipAPI drone : ship.getDeployedDrones()) {
                                        drone.getSpriteAPI().setColor(new Color(100, 255, 150));
                                        if (drone.getShield() == null || drone.getShield().getType() != ShieldType.FRONT) {
                                            drone.setShield(ShieldType.FRONT, 0f, 0f, 180f);
                                        }
                                        drone.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 0f);
                                        drone.getMutableStats().getShieldDamageTakenMult().modifyMult("mook", 0f);
                                        drone.getMutableStats().getShieldUnfoldRateMult().modifyMult("mook", 5f);
                                        drone.getMutableStats().getTurnAcceleration().modifyMult("mook", 1.5f);
                                        drone.getMutableStats().getMaxTurnRate().modifyMult("mook", 1.5f);
                                        drone.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.8f);
                                        drone.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.8f);
                                        if (drone.getShield() != null) {
                                            drone.getShield().setInnerColor(new Color(100, 255, 150));
                                            drone.getShield().setRingColor(new Color(180, 255, 205));
                                        }
                                        if (drone.getEngineController() != null) {
                                            drone.getEngineController().fadeToOtherColor(this, new Color(180, 255, 205),
                                                    new Color(100, 255, 150), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.isShipWithModules()) {
                                    for (ShipAPI child : SWP_Multi.getChildren(ship)) {
                                        child.getSpriteAPI().setColor(new Color(100, 255, 150));
                                        child.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 0f);
                                        child.getMutableStats().getShieldDamageTakenMult().modifyMult("mook", 0f);
                                        child.getMutableStats().getShieldUnfoldRateMult().modifyMult("mook", 5f);
                                        child.getMutableStats().getTurnAcceleration().modifyMult("mook", 1.5f);
                                        child.getMutableStats().getMaxTurnRate().modifyMult("mook", 1.5f);
                                        child.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.8f);
                                        child.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.8f);
                                        if (child.getShield() != null) {
                                            child.getShield().setInnerColor(new Color(100, 255, 150));
                                            child.getShield().setRingColor(new Color(180, 255, 205));
                                        }
                                        if (child.getEngineController() != null) {
                                            child.getEngineController().fadeToOtherColor(this, new Color(180, 255, 205),
                                                    new Color(100, 255, 150), 1f, 1f);
                                        }
                                    }
                                }
                                if (ship.getShield() != null) {
                                    ship.getShield().setInnerColor(new Color(100, 255, 150));
                                    ship.getShield().setRingColor(new Color(180, 255, 205));
                                }
                                if (ship.getEngineController() != null) {
                                    ship.getEngineController().fadeToOtherColor(this, new Color(180, 255, 205),
                                            new Color(100, 255, 150), 1f, 1f);
                                }
                            }
                            float shieldsize;
                            shieldsize = switch (ship.getHullSize()) {
                                case FIGHTER ->
                                    180f;
                                case FRIGATE ->
                                    210f;
                                case DESTROYER ->
                                    240f;
                                case CRUISER ->
                                    270f;
                                case CAPITAL_SHIP ->
                                    300f;
                                default ->
                                    300f;
                            };
                            if (ship.getShield() == null || ship.getShield().getType() != ShieldType.FRONT
                                    || ship.getShield().getArc() != shieldsize) {
                                ship.setShield(ShieldType.FRONT, 0f, 0f, shieldsize);
                            }
                            ship.getMutableStats().getShieldUpkeepMult().modifyMult("mook", 0f);
                            ship.getMutableStats().getShieldDamageTakenMult().modifyMult("mook", 0f);
                            ship.getMutableStats().getShieldUnfoldRateMult().modifyMult("mook", 5f);
                            ship.getMutableStats().getTurnAcceleration().modifyMult("mook", 1.5f);
                            ship.getMutableStats().getMaxTurnRate().modifyMult("mook", 1.5f);
                            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("mook", 0.8f);
                            ship.getMutableStats().getHullDamageTakenMult().modifyMult("mook", 0.8f);
                        }
                        default -> {
                        }
                    }

                    if (BOSS_SHIPS.containsKey(shipForStats.getHullSpec().getHullId())) {
                        float snapbackdistance = 1750f;
                        float snapbackpower = 1f;
                        for (int i = 0; i < AURA_MOD.get(ship.getHullSize()); i++) {
                            if (Math.random() < (amount * 15f) && !engine.isPaused()) {
                                switch (shipForStats.getHullSpec().getHullId()) {
                                    case "swp_arcade_superhyperion" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 10f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f,
                                                new Color(255, 255, 255));
                                    case "swp_arcade_oberon" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 10f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f,
                                                new Color(255, 185, 0));
                                    case "swp_arcade_ultron" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 10f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f,
                                                new Color(200, 100, 255));
                                    case "swp_arcade_zeus" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 10f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f,
                                                new Color(255, 255, 255));
                                    case "swp_arcade_ezekiel" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 10f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f,
                                                new Color(255, 255, 255));
                                    case "swp_arcade_cristarium" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 10f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f,
                                                new Color(150, 225, 255));
                                    case "swp_arcade_zero" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 10f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f, new Color(
                                                255, 50,
                                                150));
                                    case "swp_arcade_superzero" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f, new Color(
                                                255, 0,
                                                0));
                                    case "swp_arcade_hyperzero" ->
                                        engine.addSmoothParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 20f + 5f,
                                                (float) Math.random() * 0.25f + 0.75f, 1f,
                                                new Color(50, 50,
                                                        50));
                                    default -> {
                                    }
                                }
                            }
                        }
                        ship.getMutableStats().getAutofireAimAccuracy().modifyMult("boss", 5f);
                        switch (shipForStats.getHullSpec().getHullId()) {
                            case "swp_arcade_superhyperion" -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getEngineDamageTakenMult().modifyMult("boss", 0.1f);
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                switch (bossLevel) {
                                    case 11 -> {
                                        ship.getMutableStats().getBallisticRoFMult().modifyMult("boss", 1.5f);
                                        ship.getMutableStats().getEnergyRoFMult().modifyMult("boss", 1.5f);
                                        ship.getMutableStats().getMissileRoFMult().modifyMult("boss", 1.5f);
                                        ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("boss", 1.5f);
                                        ship.getMutableStats().getOverloadTimeMod().modifyMult("boss", 1.5f);
                                    }
                                    case 12 -> {
                                        ship.getMutableStats().getBallisticRoFMult().modifyMult("boss", 3f);
                                        ship.getMutableStats().getEnergyRoFMult().modifyMult("boss", 3f);
                                        ship.getMutableStats().getMissileRoFMult().modifyMult("boss", 3f);
                                        ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("boss", 3f);
                                        ship.getMutableStats().getFluxDissipation().modifyMult("boss", 1.5f);
                                        ship.getMutableStats().getOverloadTimeMod().modifyMult("boss", 1f);
                                        ship.getMutableStats().getFluxDissipation().modifyMult("boss", 1.25f);
                                        ship.getMutableStats().getFluxCapacity().modifyMult("boss", 1.25f);
                                    }
                                    default -> {
                                        ship.getMutableStats().getBallisticRoFMult().modifyMult("boss", 4f);
                                        ship.getMutableStats().getEnergyRoFMult().modifyMult("boss", 4f);
                                        ship.getMutableStats().getMissileRoFMult().modifyMult("boss", 4f);
                                        ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("boss", 4f);
                                        ship.getMutableStats().getFluxDissipation().modifyMult("boss", 2f);
                                        ship.getMutableStats().getOverloadTimeMod().modifyMult("boss", 0.5f);
                                        ship.getMutableStats().getFluxDissipation().modifyMult("boss", 1.5f);
                                        ship.getMutableStats().getFluxCapacity().modifyMult("boss", 1.5f);
                                    }
                                }
                                ship.getMutableStats().getVentRateMult().modifyMult("boss", 0.5f);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getKineticShieldDamageTakenMult().modifyMult("boss", 0.67f);
                                ship.getMutableStats().getHighExplosiveShieldDamageTakenMult().modifyMult("boss", 1.5f);
                                if (maxPoints > 137.5f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getEnergyRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMissileRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    bar1.render();
                                    if (bar2 != null) {
                                        bar2.render();
                                    }
                                    if (bar3 != null) {
                                        bar3.render();
                                    }
                                    for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                                        if (!group.isAutofiring()) {
                                            group.toggleOn();
                                        }
                                    }
                                }
                            }
                            case "swp_arcade_oberon" -> {
                                ship.getMutableStats().getOverloadTimeMod().modifyMult("boss", 0.5f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                if (maxPoints > 75f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getEnergyRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMissileRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    bar1.render();
                                }
                            }
                            case "swp_arcade_ultron" -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getOverloadTimeMod().modifyMult("boss", 2f);
                                ship.getMutableStats().getVentRateMult().modifyMult("boss", 0.5f);
                                ship.getMutableStats().getShieldUnfoldRateMult().modifyMult("boss", 0.25f);
                                ship.getMutableStats().getShieldTurnRateMult().modifyMult("boss", 0.25f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                if (maxPoints > 87.5f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getEnergyRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMissileRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    bar1.render();
                                }
                            }
                            case "swp_arcade_zeus" -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getBallisticRoFMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getEnergyRoFMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getMissileRoFMult().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.5f);
                                ship.getMutableStats().getProjectileSpeedMult().modifyMult("boss", 0.5f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getEngineDamageTakenMult().modifyMult("boss", 0.5f);
                                ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                if (maxPoints > 100f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getEnergyRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMissileRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    bar1.render();
                                }
                            }
                            case "swp_arcade_ezekiel" -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 1.75f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 1.75f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 1.75f);
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                if (maxPoints > 112.5f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getEnergyRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMissileRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    bar1.render();
                                }
                            }
                            case "swp_arcade_cristarium" -> {
                                if (ship.getDeployedDrones() != null) {
                                    for (ShipAPI drone : ship.getDeployedDrones()) {
                                        drone.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 2.5f);
                                        drone.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 2.5f);
                                        drone.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 2.5f);
                                        drone.getMutableStats().getWeaponTurnRateBonus().modifyMult("boss", 20f);
                                        drone.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                        drone.getMutableStats().getEngineDamageTakenMult().modifyMult("boss", 0f);
                                        drone.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                        drone.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                        drone.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                    }
                                }
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 2.5f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 2.5f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 2.5f);
                                ship.getMutableStats().getBallisticRoFMult().modifyMult("boss", 4f);
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.4f);
                                ship.getMutableStats().getEnergyRoFMult().modifyMult("boss", 4f);
                                ship.getMutableStats().getMissileRoFMult().modifyMult("boss", 4f);
                                ship.getMutableStats().getWeaponTurnRateBonus().modifyMult("boss", 20f);
                                ship.getMutableStats().getEngineDamageTakenMult().modifyMult("boss", 0f);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                if (maxPoints > 125f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getEnergyRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMissileRoFMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    bar1.render();
                                }
                            }
                            case "swp_arcade_zero" -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 2f);
                                if (ship.getHullLevel() <= 0.33) {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.11f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.11f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 2.5f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 2.5f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getFluxDissipation().modifyMult("boss", 2f);
                                    ship.getMutableStats().getFluxCapacity().modifyMult("boss", 2f);
                                } else if (ship.getHullLevel() <= 0.67) {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.22f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.22f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 2f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getFluxDissipation().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getFluxCapacity().modifyMult("boss", 1.5f);
                                } else {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                }
                                ship.getMutableStats().getVentRateMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getBallisticRoFMult().modifyMult("arcade", 1f);
                                ship.getMutableStats().getEnergyRoFMult().modifyMult("arcade", 1f);
                                ship.getMutableStats().getMissileRoFMult().modifyMult("arcade", 1f);
                                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("arcade", 1f);
                                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("arcade", 1f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.01f);
                                ship.getMutableStats().getEmpDamageTakenMult().modifyMult("boss", 0.01f);
                                if (maxPoints > 150f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 4f);
                                    ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 4f);
                                    ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 4f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    bar1.render();
                                }
                            }
                            case "swp_arcade_superzero" -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 2.25f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 2.25f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 2.25f);
                                if (ship.getHullLevel() <= 0.33) {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.17f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.17f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 2.5f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 2.5f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getFluxDissipation().modifyMult("boss", 2f);
                                    ship.getMutableStats().getFluxCapacity().modifyMult("boss", 2f);
                                } else if (ship.getHullLevel() <= 0.67) {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.25f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.25f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 2f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getFluxDissipation().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getFluxCapacity().modifyMult("boss", 1.5f);
                                } else {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                }
                                ship.getMutableStats().getVentRateMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getBallisticRoFMult().modifyMult("arcade", 1.5f);
                                ship.getMutableStats().getEnergyRoFMult().modifyMult("arcade", 1.5f);
                                ship.getMutableStats().getMissileRoFMult().modifyMult("arcade", 1.5f);
                                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("arcade", 1.25f);
                                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("arcade", 1.25f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.01f);
                                ship.getMutableStats().getEmpDamageTakenMult().modifyMult("boss", 0.01f);
                                if (maxPoints > 155f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 4.5f);
                                    ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 4.5f);
                                    ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 4.5f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    if (Math.random() < (amount * 2.4f) && !engine.isPaused()) {
                                        Vector2f point = MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius);
                                        engine.spawnEmpArc(ship, point, ship, ship, DamageType.OTHER, 0f, 0f, 1000f,
                                                null,
                                                5f, new Color(255, 50, 50),
                                                new Color(255, 50, 50));
                                    }
                                    bar1.render();
                                }
                            }
                            case "swp_arcade_hyperzero" -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 2.5f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 2.5f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 2.5f);
                                if (ship.getHullLevel() <= 0.33) {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.19f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.19f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 2.5f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 2.5f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getFluxDissipation().modifyMult("boss", 2f);
                                    ship.getMutableStats().getFluxCapacity().modifyMult("boss", 2f);
                                } else if (ship.getHullLevel() <= 0.67) {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.26f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.26f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 2f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 2f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("boss", 1.25f);
                                    ship.getMutableStats().getFluxDissipation().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getFluxCapacity().modifyMult("boss", 1.5f);
                                } else {
                                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                    ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                    ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                }
                                ship.getMutableStats().getVentRateMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getBallisticRoFMult().modifyMult("arcade", 2f);
                                ship.getMutableStats().getEnergyRoFMult().modifyMult("arcade", 2);
                                ship.getMutableStats().getMissileRoFMult().modifyMult("arcade", 2f);
                                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("arcade", 1.5f);
                                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("arcade", 1.5f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.01f);
                                ship.getMutableStats().getEmpDamageTakenMult().modifyMult("boss", 0.01f);
                                if (maxPoints > 160f) {
                                    if (Math.random() < (amount * 30f) && !engine.isPaused()) {
                                        engine.addHitParticle(MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius),
                                                ZERO,
                                                (float) Math.random() * 15f + 10f, (float) Math.random()
                                                * 0.5f + 0.5f, 1.5f, new Color(255, 0, 0));
                                    }
                                    ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 5f);
                                    ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 5f);
                                    ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 5f);
                                    ship.getMutableStats().getMaxSpeed().modifyMult("rage", 2f);
                                    ship.getMutableStats().getMaxTurnRate().modifyMult("rage", 2f);
                                    ship.getMutableStats().getTurnAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getAcceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getDeceleration().modifyMult("rage", 3f);
                                    ship.getMutableStats().getProjectileSpeedMult().modifyMult("rage", 2f);
                                    snapbackdistance = 1000f;
                                    snapbackpower = 3f;
                                }
                                if (ship == shipForStats) {
                                    if (Math.random() < (amount * 4.8f) && !engine.isPaused()) {
                                        Vector2f point = MathUtils.getRandomPointInCircle(ship.getLocation(),
                                                shipRadius * 1.5f);
                                        engine.spawnEmpArc(ship, point, ship, ship, DamageType.OTHER, 0f, 0f, 1000f,
                                                null,
                                                5f, new Color(150, 150, 150), new Color(
                                                        255, 255, 255));
                                    }
                                    bar1.render();
                                }
                            }
                            default -> {
                                ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getMissileWeaponDamageMult().modifyMult("boss", 2f);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getWeaponDamageTakenMult().modifyMult("boss", 0.33f);
                                ship.getMutableStats().getAcceleration().modifyMult("boss", 1.5f);
                                ship.getMutableStats().getDeceleration().modifyMult("boss", 1.5f);
                                if (ship == shipForStats) {
                                    bar1.render();
                                }
                            }
                        }
                        List<WeaponAPI> weapons = ship.getAllWeapons();
                        for (WeaponAPI weapon : weapons) {
                            if (weapon.getMaxAmmo() > 0 && weapon.getMaxAmmo() < 100000
                                    && !(weapon.getId().contentEquals("swp_arcade_empbomb")
                                    || weapon.getId().contentEquals("swp_arcade_godmode"))) {
                                weapon.setAmmo(weapon.getMaxAmmo());
                            }
                            if (bossAmmoRegenTimer >= 120f && (weapon.getId().contentEquals("swp_arcade_empbomb")
                                    || weapon.getId().contentEquals("swp_arcade_godmode"))) {
                                weapon.setAmmo(1);
                            }
                        }
                        if (ship == shipForStats && bossFlash >= 3f && !engine.isPaused()) {
                            Vector2f loc = new Vector2f(ship.getLocation());
                            loc.setY(loc.y + shipRadius);
                            switch (ship.getHullSpec().getHullId()) {
                                case "swp_arcade_superhyperion" -> {
                                    switch (ship.getVariant().getHullVariantId()) {
                                        case "swp_arcade_superhyperion_hul" -> {
                                            engine.addFloatingText(loc, "Hulk Hogan", 75f, Color.ORANGE, ship, 2f, 2f);
                                            ship.getSpriteAPI().setColor(new Color(255, 0, 0));
                                            for (WeaponAPI weapon : ship.getAllWeapons()) {
                                                if (weapon.getSlot().isHidden()) {
                                                    continue;
                                                }
                                                if (weapon.getSprite() != null) {
                                                    weapon.getSprite().setColor(new Color(255, 0, 0));
                                                }
                                                if (weapon.getBarrelSpriteAPI() != null) {
                                                    weapon.getBarrelSpriteAPI().setColor(new Color(255, 0, 0));
                                                }
                                            }
                                            if (ship.getShield() != null) {
                                                ship.getShield().setInnerColor(new Color(255, 0, 0));
                                                ship.getShield().setRingColor(new Color(255, 130, 130));
                                            }
                                            if (ship.getEngineController() != null) {
                                                ship.getEngineController().fadeToOtherColor(this,
                                                        new Color(255, 130, 130),
                                                        new Color(255, 0, 0),
                                                        1f, 1f);
                                            }
                                        }
                                        case "swp_arcade_superhyperion_she" -> {
                                            engine.addFloatingText(loc, "Iron Sheik", 75f, Color.ORANGE, ship, 2f, 2f);
                                            ship.getSpriteAPI().setColor(new Color(255, 255, 0));
                                            for (WeaponAPI weapon : ship.getAllWeapons()) {
                                                if (weapon.getSlot().isHidden()) {
                                                    continue;
                                                }
                                                if (weapon.getSprite() != null) {
                                                    weapon.getSprite().setColor(new Color(255, 255, 0));
                                                }
                                                if (weapon.getBarrelSpriteAPI() != null) {
                                                    weapon.getBarrelSpriteAPI().setColor(new Color(255, 255, 0));
                                                }
                                            }
                                            if (ship.getShield() != null) {
                                                ship.getShield().setInnerColor(new Color(255, 255, 0));
                                                ship.getShield().setRingColor(new Color(255, 255, 130));
                                            }
                                            if (ship.getEngineController() != null) {
                                                ship.getEngineController().fadeToOtherColor(this,
                                                        new Color(255, 255, 130),
                                                        new Color(255, 255, 0),
                                                        1f, 1f);
                                            }
                                        }
                                        case "swp_arcade_superhyperion_war" -> {
                                            engine.addFloatingText(loc, "Ultimate Warrior", 75f, Color.ORANGE, ship, 2f,
                                                    2f);
                                            ship.getSpriteAPI().setColor(new Color(255, 0, 255));
                                            for (WeaponAPI weapon : ship.getAllWeapons()) {
                                                if (weapon.getSlot().isHidden()) {
                                                    continue;
                                                }
                                                if (weapon.getSprite() != null) {
                                                    weapon.getSprite().setColor(new Color(255, 0, 255));
                                                }
                                                if (weapon.getBarrelSpriteAPI() != null) {
                                                    weapon.getBarrelSpriteAPI().setColor(new Color(255, 0, 255));
                                                }
                                            }
                                            if (ship.getShield() != null) {
                                                ship.getShield().setInnerColor(new Color(255, 0, 255));
                                                ship.getShield().setRingColor(new Color(255, 130, 255));
                                            }
                                            if (ship.getEngineController() != null) {
                                                ship.getEngineController().fadeToOtherColor(this,
                                                        new Color(255, 130, 255),
                                                        new Color(255, 0, 255),
                                                        1f, 1f);
                                            }
                                        }
                                    }
                                }
                                case "swp_arcade_oberon" ->
                                    engine.addFloatingText(loc, "Oberon", 75f, Color.ORANGE, ship, 2f, 2f);
                                case "swp_arcade_ultron" ->
                                    engine.addFloatingText(loc, "Ultron", 75f, Color.ORANGE, ship, 2f, 2f);
                                case "swp_arcade_zeus" ->
                                    engine.addFloatingText(loc, "Zeus", 100f, Color.ORANGE, ship, 2f, 2f);
                                case "swp_arcade_ezekiel" ->
                                    engine.addFloatingText(loc, "Ezekiel", 75f, Color.ORANGE, ship, 2f, 2f);
                                case "swp_arcade_cristarium" ->
                                    engine.addFloatingText(loc, "Cristarium", 75f, Color.ORANGE, ship, 2f, 2f);
                                case "swp_arcade_zero" ->
                                    engine.addFloatingText(loc, "Zero", 75f, Color.ORANGE, ship, 2f, 2f);
                                case "swp_arcade_superzero" ->
                                    engine.addFloatingText(loc, "Super Zero", 75f, Color.RED, ship, 2f, 2f);
                                case "swp_arcade_hyperzero" ->
                                    engine.addFloatingText(loc, "Omega Zero", 75f, Color.WHITE, ship, 2f, 2f);
                                default -> {
                                }
                            }
                        }
                        if (!ship.getHullSpec().getHullId().contentEquals("swp_arcade_superhyperion")) {
                            boolean travelOn = snapback > 20f;

                            float distance = MathUtils.getDistance(ship, playerShip);
                            if (distance > 1750f) {
                                snapback += (1f + (distance - snapbackdistance) / 750f) * amount * snapbackpower;
                            } else {
                                if (snapback > 20f) {
                                    snapback = Math.max(snapback - (30f + (snapbackdistance - distance) / 10f) * amount,
                                            0f);
                                } else {
                                    snapback = Math.max(snapback - (3f + (snapbackdistance - distance) / 250f) * amount,
                                            0f);
                                }
                            }

                            if (snapback > 20f) {
                                if (!travelOn) {
                                    if (!engine.getViewport().isNearViewport(ship.getLocation(),
                                            ship.getCollisionRadius())) {
                                        ship.turnOnTravelDrive();
                                        ship.setFacing(
                                                VectorUtils.getAngle(ship.getLocation(), playerShip.getLocation()));
                                    } else {
                                        snapback = 19f;
                                    }
                                } else {
                                    ship.turnOnTravelDrive();
                                    ship.setFacing(VectorUtils.getAngle(ship.getLocation(), playerShip.getLocation()));
                                }
                            } else {
                                if (ship.getTravelDrive() != null && ship.getTravelDrive().isOn()) {
                                    ship.turnOffTravelDrive();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bossFlash >= 3f && !engine.isPaused()) {
            bossFlash -= 3f;
        }

        if (bossAmmoRegenTimer >= 120f && !engine.isPaused()) {
            bossAmmoRegenTimer -= 120f;
        }

        if (engine.isPaused()) {
            return;
        }

        playerShip.getMutableStats().getBallisticWeaponDamageMult().modifyMult("arcade", 2f + (float) Math.pow(realBuff,
                2));
        playerShip.getMutableStats().getEnergyWeaponDamageMult().modifyMult("arcade", 2f + (float) Math.pow(realBuff, 2));
        playerShip.getMutableStats().getMissileWeaponDamageMult().modifyMult("arcade", 1f
                + (float) Math.pow(realBuff, 2));
        playerShip.getMutableStats().getMaxCombatHullRepairFraction().modifyFlat("arcade", 1f);
        playerShip.getMutableStats().getBallisticRoFMult().modifyMult("arcade", 1f + realBuff * 0.25f);
        playerShip.getMutableStats().getEnergyRoFMult().modifyMult("arcade", 1f + realBuff * 0.25f);
        playerShip.getMutableStats().getBeamWeaponDamageMult().modifyMult("arcade", 1f + realBuff * 0.5f);
        playerShip.getMutableStats().getMissileRoFMult().modifyMult("arcade", 1f + realBuff);
        playerShip.getMutableStats().getMissileMaxSpeedBonus().modifyMult("arcade", 1f + realBuff * 0.5f);
        playerShip.getMutableStats().getMissileAccelerationBonus().modifyMult("arcade", 1f + realBuff * 1f);
        playerShip.getMutableStats().getMissileMaxTurnRateBonus().modifyMult("arcade", 1f + realBuff * 1f);
        playerShip.getMutableStats().getMissileTurnAccelerationBonus().modifyMult("arcade", 1f + realBuff * 1f);
        playerShip.getMutableStats().getShieldDamageTakenMult().modifyMult("arcade", 1f / (0.7f + realBuff * 0.7f));
        playerShip.getMutableStats().getProjectileSpeedMult().modifyMult("arcade", 1f + realBuff * 0.25f);
        playerShip.getMutableStats().getHardFluxDissipationFraction().modifyFlat("arcade", 1f - 1f / (1.25f + realBuff
                * 0.25f));
        playerShip.getMutableStats().getWeaponDamageTakenMult().modifyMult("arcade", 0.25f);
        playerShip.getMutableStats().getEngineDamageTakenMult().modifyMult("arcade", 0.25f);
        playerShip.getMutableStats().getBallisticWeaponFluxCostMod().modifyMult("arcade", 0.6f + realBuff * 0.6f);
        playerShip.getMutableStats().getEnergyWeaponFluxCostMod().modifyMult("arcade", 0.6f + realBuff * 0.6f);
        playerShip.getMutableStats().getMissileWeaponFluxCostMod().modifyMult("arcade", 0.6f + realBuff * 0.6f);
        playerShip.getMutableStats().getFluxDissipation().modifyMult("arcade", 1f + realBuff);
        playerShip.getMutableStats().getFluxCapacity().modifyMult("arcade", 1f + realBuff);
        playerShip.getMutableStats().getArmorDamageTakenMult().modifyMult("arcade", 1f / (1f + realBuff * 0.5f));
        playerShip.getMutableStats().getHullDamageTakenMult().modifyMult("arcade", 1f / (1f + realBuff));
        playerShip.getMutableStats().getAcceleration().modifyMult("arcade", 1f + realBuff * 0.25f);
        playerShip.getMutableStats().getDeceleration().modifyMult("arcade", 1f + realBuff * 0.25f);
        playerShip.getMutableStats().getMaxSpeed().modifyMult("arcade", 1f + realBuff * 0.25f);
        playerShip.getMutableStats().getShieldUnfoldRateMult().modifyMult("arcade", 2.5f);
        playerShip.getMutableStats().getOverloadTimeMod().modifyMult("arcade", 1f / (2f + realBuff * 0.25f));
        playerShip.getMutableStats().getVentRateMult().modifyMult("arcade", 1f + realBuff * 0.25f);
        playerShip.getMutableStats().getShieldUpkeepMult().modifyMult("arcade", 1f + realBuff);
        playerShip.getMutableStats().getPhaseCloakActivationCostBonus().modifyMult("arcade", 1f + realBuff);
        playerShip.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("arcade", 1f + realBuff);
        playerShip.getMutableStats().getKineticShieldDamageTakenMult().modifyMult("arcade", 0.67f);
        playerShip.getMutableStats().getHighExplosiveShieldDamageTakenMult().modifyMult("arcade", 1.5f);

        List<WeaponAPI> weapons = playerShip.getAllWeapons();
        if (bossLevel == 0 && maxPoints > 62.5f) {
            engine.addFloatingText(playerShip.getLocation(), "BOSS INCOMING", 150f, Color.ORANGE, playerShip, 2f, 10f);
            heal();
            makeBoss(1);
            bossLevel = 1;
            Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
        } else if (bossLevel == 2 && maxPoints > 75f) {
            engine.addFloatingText(playerShip.getLocation(), "BOSS INCOMING", 150f, Color.ORANGE, playerShip, 3f, 10f);
            heal();
            makeBoss(2);
            bossLevel = 3;
            Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
        } else if (bossLevel == 4 && maxPoints > 87.5f) {
            engine.addFloatingText(playerShip.getLocation(), "BOSS INCOMING", 150f, Color.ORANGE, playerShip, 4f, 10f);
            heal();
            makeBoss(3);
            bossLevel = 5;
            Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
        } else if (bossLevel == 6 && maxPoints > 100f) {
            engine.addFloatingText(playerShip.getLocation(), "BOSS INCOMING", 150f, Color.ORANGE, playerShip, 5f, 10f);
            heal();
            makeBoss(4);
            bossLevel = 7;
            Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
        } else if (bossLevel == 8 && maxPoints > 112.5f) {
            engine.addFloatingText(playerShip.getLocation(), "BOSS INCOMING", 150f, Color.ORANGE, playerShip, 6f, 10f);
            heal();
            makeBoss(5);
            bossLevel = 9;
            Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
        } else if (bossLevel == 10 && maxPoints > 125f) {
            engine.addFloatingText(playerShip.getLocation(), "BOSS INCOMING", 150f, Color.ORANGE, playerShip, 7f, 10f);
            heal();
            makeBoss(6);
            bossLevel = 11;
            Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
        } else if (bossLevel == 14 && maxPoints > 137.5f) {
            heal();
            int superammo1 = 0;
            int superammo2 = 0;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb")) {
                    superammo1 = weapon.getAmmo();
                }
                if (weapon.getId().contentEquals("swp_arcade_godmode")) {
                    superammo2 = weapon.getAmmo();
                }
            }
            if (superammo1 >= 14 && superammo2 >= 14) {
                Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
                Global.getSoundPlayer().playUISound("swp_arcade_newround", 1.5f, 1.5f);
                Global.getSoundPlayer().playUISound("swp_arcade_newround", 2f, 2f);
                engine.addFloatingText(playerShip.getLocation(), "PREPARE TO DIE", 150f, Color.RED, playerShip, 32f, 10f);
                ultralevel = 2;
            } else if (superammo1 >= 14 || superammo2 >= 14) {
                Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
                Global.getSoundPlayer().playUISound("swp_arcade_newround", 1.5f, 1.5f);
                engine.addFloatingText(playerShip.getLocation(), "TRUE FINAL BOSS INCOMING", 150f, Color.ORANGE,
                        playerShip, 16f, 10f);
                ultralevel = 1;
            } else {
                Global.getSoundPlayer().playUISound("swp_arcade_newround", 1f, 1f);
                engine.addFloatingText(playerShip.getLocation(), "FINAL BOSS INCOMING", 150f, Color.ORANGE, playerShip,
                        8f, 10f);
                ultralevel = 0;
            }
            makeBoss(7);
            bossLevel = 15;
        }

        if (threshold == 0 && maxPoints > 57.5f) {
            engine.addFloatingText(playerShip.getLocation(), "DIFFICULTY I", 150f, Color.GREEN, playerShip, 2f, 10f);
            heal();
            threshold = 1;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                    weapon.setAmmo(weapon.getAmmo() + 1);
                }
            }
            PostProcessShader.setSaturation(false, 1.03f);
            PostProcessShader.setContrast(false, 1.03f);
        } else if (threshold == 1 && maxPoints > 70f) {
            engine.addFloatingText(playerShip.getLocation(), "DIFFICULTY II", 150f, Color.GREEN, playerShip, 2f, 10f);
            heal();
            threshold = 2;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                    weapon.setAmmo(weapon.getAmmo() + 1);
                }
            }
            PostProcessShader.setSaturation(false, 1.06f);
            PostProcessShader.setContrast(false, 1.06f);
        } else if (threshold == 2 && maxPoints > 82.5f) {
            engine.addFloatingText(playerShip.getLocation(), "DIFFICULTY III", 150f, Color.YELLOW, playerShip, 4f, 10f);
            heal();
            threshold = 3;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                    weapon.setAmmo(weapon.getAmmo() + 1);
                }
            }
            PostProcessShader.setSaturation(false, 1.09f);
            PostProcessShader.setContrast(false, 1.09f);
        } else if (threshold == 3 && maxPoints > 95f) {
            engine.addFloatingText(playerShip.getLocation(), "DIFFICULTY IV", 150f, Color.YELLOW, playerShip, 4f, 10f);
            heal();
            threshold = 4;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                    weapon.setAmmo(weapon.getAmmo() + 1);
                }
            }
            PostProcessShader.setSaturation(false, 1.12f);
            PostProcessShader.setContrast(false, 1.12f);
        } else if (threshold == 4 && maxPoints > 107.5f) {
            engine.addFloatingText(playerShip.getLocation(), "DIFFICULTY V", 150f, Color.RED, playerShip, 6f, 10f);
            heal();
            threshold = 5;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                    weapon.setAmmo(weapon.getAmmo() + 1);
                }
            }
            PostProcessShader.setSaturation(false, 1.15f);
            PostProcessShader.setContrast(false, 1.15f);
        } else if (threshold == 5 && maxPoints > 120f) {
            engine.addFloatingText(playerShip.getLocation(), "DIFFICULTY VI", 150f, Color.RED, playerShip, 6f, 10f);
            heal();
            threshold = 6;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                    weapon.setAmmo(weapon.getAmmo() + 1);
                }
            }
            PostProcessShader.setSaturation(false, 1.18f);
            PostProcessShader.setContrast(false, 1.18f);
        } else if (threshold == 6 && maxPoints > 132.5f) {
            engine.addFloatingText(playerShip.getLocation(), "DIFFICULTY MAX", 150f, Color.WHITE, playerShip, 8f, 10f);
            heal();
            threshold = 7;
            for (WeaponAPI weapon : weapons) {
                if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                    weapon.setAmmo(weapon.getAmmo() + 1);
                }
            }
            PostProcessShader.setSaturation(false, 1.21f);
            PostProcessShader.setContrast(false, 1.21f);
        }

        if (combatOver) {
            combatOver = true;
            CombatFleetManagerAPI eManager = engine.getFleetManager(FleetSide.ENEMY);
            eManager.getTaskManager(false).orderFullRetreat();
            if (bossLevel < 16) {
                engine.addFloatingText(engine.getViewport().getCenter(), "GAME OVER! Score: " + points, 100f,
                        Color.YELLOW, new SimpleEntity(
                                engine.getViewport().getCenter()), 0f, 1f / amount);
                PostProcessShader.resetDefaults();
            } else {
                engine.addFloatingText(engine.getViewport().getCenter(), "YOU WIN! Score: " + points, 100f, Color.GREEN,
                        new SimpleEntity(
                                engine.getViewport().getCenter()), 0f, 1f / amount);
                PostProcessShader.resetDefaults();
                ShipAPI[] ships = engine.getShips().toArray(new ShipAPI[engine.getShips().size()]);
                for (ShipAPI ship : ships) {
                    if (ship == null) {
                        return;
                    }

                    if (ship.isAlive() && ship.getOwner() != playerShip.getOwner()) {
                        engine.applyDamage(ship, ship.getLocation(), 100000f, DamageType.OTHER, 0f, true, false,
                                playerShip, false);
                    } else if (ship.isPiece()) {
                        engine.removeEntity(ship);
                    }
                }
            }
            return;
        }

        if (playerShip.getHitpoints() < 0) {
            engine.removeEntity(playerShip);
        }

        if (!playerShip.isAlive()) {
            combatOver = true;
            //SWP_MusicPlayer.clearPlaylist();
            Global.getSoundPlayer().playUISound("swp_arcade_youaredead", 1f, 1f);
            if (bossLevel < 1) {
                Global.getSoundPlayer().playUISound("swp_arcade_fail", 1f, 1f);
            } else if (bossLevel >= 16) {
                Global.getSoundPlayer().playUISound("swp_arcade_win", 1f, 1f);
                for (WeaponAPI weapon : weapons) {
                    if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                        points += weapon.getAmmo() * 5000f;
                    }
                }
            } else if (UnrealAnnouncer.getComboMulti() >= 5) {
                Global.getSoundPlayer().playUISound("swp_arcade_denied", 1f, 1f);
            }
            pointsStatic = points;
            if (campaignMode) {
                engine.endCombat(3);
            }
        }

        for (WeaponAPI weapon : weapons) {
            if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                weapon.repair();
            }
        }

        if (playerShip.getFluxTracker().getFluxLevel() >= 0.75f && playerShip.getShield() != null
                && playerShip.getShield().isOn()) {
            float intensity = (playerShip.getFluxTracker().getFluxLevel() - 0.75f) + 0.05f;
            if ((float) Math.random() < intensity * 100f * amount) {
                float angle = (float) Math.random() * 360f;
                Vector2f point1 = MathUtils.getPointOnCircumference(playerShip.getShield().getLocation(),
                        playerShip.getShield().getRadius(), angle);
                Vector2f point2 = MathUtils.getPointOnCircumference(playerShip.getShield().getLocation(),
                        playerShip.getShield().getRadius(), angle + 30f);
                Global.getCombatEngine().spawnEmpArc(playerShip, point1, new AnchoredEntity(playerShip, point1),
                        new AnchoredEntity(playerShip, point2),
                        DamageType.ENERGY, 0f, 0f, 10000f, null, intensity * 0.4f,
                        new Color(100, 200, 255, SWP_Util.clamp255(5 * (int) intensity)),
                        new Color(255, 255, 255, SWP_Util.clamp255(5 * (int) intensity)));
            }
            warningBeep += amount * playerShip.getMutableStats().getTimeMult().getModifiedValue();
            if (warningBeep >= 0.517f / (1f + (intensity - 5f) / 100f)) {
                Global.getSoundPlayer().playUISound("swp_arcade_shieldwarning", 1f + (intensity - 5f) / 100f, 0.25f + intensity
                        / 100f);
                warningBeep -= 0.517f / (1f + (intensity - 5f) / 100f);
            }
        }

        engine.setDoNotEndCombat(true);

        ShipAPI[] ships = engine.getShips().toArray(new ShipAPI[engine.getShips().size()]);
        for (ShipAPI ship : ships) {
            if (ship == null) {
                return;
            }

            if (!ship.isFighter() && engine.getPlayerShip() != ship && ship.isAlive()) {
                if (!shipWorths.containsKey(ship)) {
                    DeployedFleetMemberAPI dfmember = engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(
                            ship);
                    if (dfmember != null) {
                        FleetMemberAPI fmember = dfmember.getMember();
                        if (fmember != null) {
                            shipWorths.put(ship, (float) fmember.getFleetPointCost());
                            shipTypes.put(ship, 0);
                        }
                    }
                }
            }

            if (ship.isAlive() && engine.getPlayerShip() != ship && !ship.isShuttlePod() && !ship.isWingLeader()
                    && !ship.isDrone()) {
                CombatFleetManagerAPI eManager = engine.getFleetManager(FleetSide.ENEMY);
                if (eManager.getTaskManager(false).getAssignmentFor(ship) != null
                        && eManager.getTaskManager(false).getAssignmentFor(ship).getType()
                        == CombatAssignmentType.RETREAT) {
                    eManager.getTaskManager(false).orderSearchAndDestroy(eManager.getDeployedFleetMember(ship), false);
                }
            }

            if (ship.getSpriteAPI() == null || (!ship.isAlive() && !ship.isHulk()) || !engine.isEntityInPlay(ship)) {
                engine.removeEntity(ship);
            }
        }

        Object[] entries = shipWorths.entrySet().toArray();
        for (Object obj : entries) {
            @SuppressWarnings("unchecked")
            Map.Entry<ShipAPI, Float> entry = (Map.Entry<ShipAPI, Float>) obj;
            ShipAPI ship = entry.getKey();
            float worth = entry.getValue();
            if ((ship.isHulk() || !engine.isEntityInPlay(ship)) && !ship.isFighter() && engine.getPlayerShip() != ship) {
                playerShip.getMutableStats().getHullDamageTakenMult().unmodify("startingbonus");
                playerShip.getMutableStats().getEmpDamageTakenMult().unmodify("startingbonus");
                playerShip.getMutableStats().getArmorDamageTakenMult().unmodify("startingbonus");

                UnrealAnnouncer.addKill(worth, shipTypes.get(ship));

                int bonusMultiplier = UnrealAnnouncer.getComboMulti();

                if (bonusMultiplier == 0) {
                    bonusMultiplier = 1;
                }

                if (points <= 1f) {
                    Global.getSoundPlayer().playUISound("swp_arcade_firstkill", 1f, 1f);

                    int supers = 1 + (threshold + Math.max(threshold - 1, 0)) / 2;

                    for (WeaponAPI weapon : weapons) {
                        if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                            weapon.setAmmo(supers);
                        }
                    }
                }

                playerShip.getFluxTracker().decreaseFlux(worth * 1000f * (float) Math.sqrt(threshold + 1)
                        / (float) Math.pow(bonusMultiplier, 0.33));

                if (BOSS_SHIPS.containsKey(ship.getHullSpec().getHullId())) {
                    float superscalar;
                    switch (bossLevel) {
                        case 1 ->
                            superscalar = 1f;
                        case 3 ->
                            superscalar = 1.5f;
                        case 5 ->
                            superscalar = 2f;
                        case 7 ->
                            superscalar = 2.5f;
                        case 9 ->
                            superscalar = 3f;
                        case 11, 12, 13 ->
                            superscalar = 2f;
                        case 15 -> {
                            switch (ultralevel) {
                                case 2:
                                    superscalar = 36f;
                                    break;
                                case 1:
                                    superscalar = 12f;
                                    break;
                                case 0:
                                default:
                                    superscalar = 4f;
                                    break;
                            }
                        }
                        default ->
                            superscalar = 1f;
                    }
                    points += worth * 10f * superscalar * scale;
                    engine.addFloatingText(new Vector2f(ship.getLocation()), "+" + (int) (worth * 10f * superscalar
                            * scale), 100f, Color.ORANGE,
                            new SimpleEntity(new Vector2f(ship.getLocation())), 2f, 3f);
                    if (bossLevel != 11 && bossLevel != 12 && bossLevel != 15) {
                        snapback = 0f;
                        for (WeaponAPI weapon : weapons) {
                            if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                                weapon.setAmmo(weapon.getAmmo() + 1);
                            }
                        }
                    }
                    bossLevel++;
                    heal();
                } else {
                    points += worth * bonusMultiplier * (threshold + 1) * scale;
                    switch (shipTypes.get(ship)) {
                        case 0 ->
                            engine.addFloatingText(new Vector2f(ship.getLocation()), "+" + (int) (worth * bonusMultiplier
                                    * (threshold + 1) * scale),
                                    35f,
                                    Color.WHITE, new SimpleEntity(new Vector2f(ship.getLocation())), 4f, 3f);
                        case 1 ->
                            engine.addFloatingText(new Vector2f(ship.getLocation()), "+" + (int) (worth * bonusMultiplier
                                    * (threshold + 1) * scale),
                                    50f,
                                    new Color(255, 255, 100), new SimpleEntity(new Vector2f(
                                            ship.getLocation())), 4f, 3f);
                        case 2 ->
                            engine.addFloatingText(new Vector2f(ship.getLocation()), "+" + (int) (worth * bonusMultiplier
                                    * (threshold + 1) * scale),
                                    65f,
                                    new Color(255, 150, 100), new SimpleEntity(new Vector2f(
                                            ship.getLocation())), 4f, 3f);
                        case 3 ->
                            engine.addFloatingText(new Vector2f(ship.getLocation()), "+" + (int) (worth * bonusMultiplier
                                    * (threshold + 1) * scale),
                                    80f,
                                    new Color(50, 150, 255), new SimpleEntity(
                                            new Vector2f(ship.getLocation())), 4f, 3f);
                        case 4 ->
                            engine.addFloatingText(new Vector2f(ship.getLocation()), "+" + (int) (worth * bonusMultiplier
                                    * (threshold + 1) * scale),
                                    50f,
                                    new Color(100, 255, 150), new SimpleEntity(new Vector2f(
                                            ship.getLocation())), 4f, 3f);
                    }
                    buffLevel += worth / 750f;
                    maxPoints += 1.33f / (1f + buffLevel * 0.33f) * worth * 0.04f;
                }

                if (ship.getHullSpec().getHullId().contentEquals("swp_arcade_zero")
                        || ship.getHullSpec().getHullId().contentEquals("swp_arcade_superzero")
                        || ship.getHullSpec().getHullId().contentEquals("swp_arcade_hyperzero")) {
                    if (bossLevel >= 16) {
                        Global.getSoundPlayer().playUISound("swp_arcade_win", 1f, 1f);
                        //SWP_MusicPlayer.clearPlaylist();
                        combatOver = true;
                        for (WeaponAPI weapon : weapons) {
                            if (weapon.getId().contentEquals("swp_arcade_empbomb") || weapon.getId().contentEquals("swp_arcade_godmode")) {
                                points += weapon.getAmmo() * 5000f * scale;
                                engine.addFloatingText(MathUtils.getRandomPointInCircle(playerShip.getLocation(), 100f),
                                        "+" + weapon.getAmmo() * 5000f * scale,
                                        50f, Color.ORANGE, new SimpleEntity(new Vector2f(
                                                playerShip.getLocation())), 8f, 3f);
                            }
                        }
                        pointsStatic = points;
                        if (campaignMode) {
                            engine.endCombat(3);
                        }
                    }
                }

                Vector2f playerShipLoc = playerShip.getLocation();
                Vector2f comboBonusTxt = new Vector2f(playerShipLoc);
                Vector2f totalPointsTxt = new Vector2f(playerShipLoc);

                comboBonusTxt.setY(comboBonusTxt.y + 70f);
                totalPointsTxt.setY(totalPointsTxt.y - 50f);

                if (UnrealAnnouncer.getComboMulti() > 1) {
                    engine.addFloatingText(comboBonusTxt, bonusMultiplier + "X COMBO BONUS!", 40f + bonusMultiplier * 5f,
                            Color.red, playerShip, bonusMultiplier,
                            3f);
                }
                engine.addFloatingText(totalPointsTxt, "Points: " + points, 50f, Color.green, playerShip, 2f, 3f);

                for (WeaponAPI weapon : weapons) {
                    if (!weapon.getId().contentEquals("swp_arcade_empbomb") && !weapon.getId().contentEquals("swp_arcade_godmode")) {
                        weapon.repair();
                        weapon.setRemainingCooldownTo(0f);
                        if (weapon.getAmmo() < 100000 && weapon.getAmmo() >= 0) {
                            if (weapon.getDerivedStats().getSustainedDps() < weapon.getDerivedStats().getDps()) {
                                weapon.setAmmo(Math.min(Math.max(weapon.getMaxAmmo() + (int) (weapon.getMaxAmmo()
                                        * worth * (float) Math.sqrt(
                                                bonusMultiplier)
                                        / 3f), weapon.getAmmo()),
                                        weapon.getMaxAmmo() * 10));
                            } else {
                                weapon.setAmmo(Math.min(weapon.getAmmo() + (int) (weapon.getMaxAmmo()
                                        * (float) Math.sqrt(worth
                                                * (float) Math.sqrt(
                                                        bonusMultiplier)
                                                * (weapon.getMaxAmmo() / (1f
                                                + weapon.getAmmo()))
                                                / 300f)),
                                        weapon.getMaxAmmo() * 10));
                            }
                        }
                    }
                }

                shipTypes.remove(ship);
                shipWorths.remove(ship);
                if (engine.isEntityInPlay(ship) && !BOSS_SHIPS.containsKey(ship.getHullSpec().getHullId())) {
                    engine.removeEntity(ship);
                }
            }

            if ((ship.isHulk() || !engine.isEntityInPlay(ship)) && !ship.isFighter() && !ship.isDrone()
                    && engine.getPlayerShip() != ship) {
                shipTypes.remove(ship);
                shipWorths.remove(ship);
                if (engine.isEntityInPlay(ship) && !BOSS_SHIPS.containsKey(ship.getHullSpec().getHullId())) {
                    engine.removeEntity(ship);
                }
            }
        }

        int fp = 0;
        int numShips = 0;
        for (ShipAPI s : ships) {
            if (engine.getPlayerShip() != s && s.isAlive() && !s.isFighter() && shipTypes.containsKey(s)
                    && !BOSS_SHIPS.containsKey(
                            s.getHullSpec().getHullId())) {
                DeployedFleetMemberAPI dfmember
                        = engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(s);
                if (dfmember != null) {
                    FleetMemberAPI fmember = dfmember.getMember();
                    if (fmember != null) {
                        int type = shipTypes.get(s);
                        switch (type) {
                            case 4 ->
                                fp += fmember.getFleetPointCost() * 2f;
                            case 3 ->
                                fp += fmember.getFleetPointCost() * 3f;
                            case 2 ->
                                fp += fmember.getFleetPointCost() * 2.5f;
                            case 1 ->
                                fp += fmember.getFleetPointCost() * 2f;
                            case 0 ->
                                fp += fmember.getFleetPointCost();
                            default -> {
                            }
                        }
                        numShips++;
                    }
                }
            }
        }

        int type;
        if (fp < maxPoints || numShips < 2) {
            List<ShipAPI> spawnList = new ArrayList<>(10);
            int bound = 100;
            while ((fp < maxPoints || numShips < 2) && bound > 0) {
                bound--;
                type = 0;
                double rand = Math.random();
                switch (threshold) {
                    case 0 -> {
                        if (rand >= 0.99) {
                            type = 3;
                        } else if (rand >= 0.97) {
                            type = 2;
                        } else if (rand >= 0.93) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    case 1 -> {
                        if (rand >= 0.98) {
                            type = 3;
                        } else if (rand >= 0.94) {
                            type = 2;
                        } else if (rand >= 0.86) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    case 2 -> {
                        if (rand >= 0.97) {
                            type = 3;
                        } else if (rand >= 0.91) {
                            type = 2;
                        } else if (rand >= 0.79) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    case 3 -> {
                        if (rand >= 0.96) {
                            type = 3;
                        } else if (rand >= 0.88) {
                            type = 2;
                        } else if (rand >= 0.72) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    case 4 -> {
                        if (rand >= 0.95) {
                            type = 3;
                        } else if (rand >= 0.85) {
                            type = 2;
                        } else if (rand >= 0.65) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    case 5 -> {
                        if (rand >= 0.94) {
                            type = 3;
                        } else if (rand >= 0.82) {
                            type = 2;
                        } else if (rand >= 0.58) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    case 6 -> {
                        if (rand >= 0.92) {
                            type = 3;
                        } else if (rand >= 0.76) {
                            type = 2;
                        } else if (rand >= 0.51) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    case 7 -> {
                        if (rand >= 0.897) {
                            type = 3;
                        } else if (rand >= 0.69) {
                            type = 2;
                        } else if (rand >= 0.44) {
                            if (Math.random() > 0.75) {
                                type = 4;
                            } else {
                                type = 1;
                            }
                        }
                    }
                    default -> {
                    }
                }
                ShipAPI spawned = makeShip();
                if (spawned == null) {
                    continue;
                }
                spawned.setOriginalOwner(1);
                spawned.setOwner(1);
                if (spawned.isFighter()) {
                    rand = Math.max(Math.random(), Math.max(rand, Math.random()));
                    switch (threshold) {
                        case 0 -> {
                            if (rand >= 0.99) {
                                type = 3;
                            } else if (rand >= 0.97) {
                                type = 2;
                            } else if (rand >= 0.93) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        case 1 -> {
                            if (rand >= 0.98) {
                                type = 3;
                            } else if (rand >= 0.94) {
                                type = 2;
                            } else if (rand >= 0.86) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        case 2 -> {
                            if (rand >= 0.97) {
                                type = 3;
                            } else if (rand >= 0.91) {
                                type = 2;
                            } else if (rand >= 0.79) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        case 3 -> {
                            if (rand >= 0.96) {
                                type = 3;
                            } else if (rand >= 0.88) {
                                type = 2;
                            } else if (rand >= 0.72) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        case 4 -> {
                            if (rand >= 0.95) {
                                type = 3;
                            } else if (rand >= 0.85) {
                                type = 2;
                            } else if (rand >= 0.65) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        case 5 -> {
                            if (rand >= 0.94) {
                                type = 3;
                            } else if (rand >= 0.82) {
                                type = 2;
                            } else if (rand >= 0.58) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        case 6 -> {
                            if (rand >= 0.92) {
                                type = 3;
                            } else if (rand >= 0.76) {
                                type = 2;
                            } else if (rand >= 0.51) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        case 7 -> {
                            if (rand >= 0.897) {
                                type = 3;
                            } else if (rand >= 0.69) {
                                type = 2;
                            } else if (rand >= 0.44) {
                                if (Math.random() > 0.75) {
                                    type = 4;
                                } else {
                                    type = 1;
                                }
                            }
                        }
                        default -> {
                        }
                    }
                }
                spawnList.add(spawned);
                List<ShipAPI> shipList = new ArrayList<>(spawnList.size());
                if (!spawned.isFighter()) {
                    shipList.add(spawned);
                    numShips++;
                } else {
                    shipList.addAll(spawned.getWing().getWingMembers());
                }
                for (ShipAPI shp : shipList) {
                    if ((shp.getShield() == null || (shp.getShield() != null && shp.getShield().getType()
                            == ShieldType.PHASE)) && type == 4) {
                        type = 1;
                    }
                    shipTypes.put(shp, type);
                    switch (type) {
                        case 4 -> {
                            // Shielded
                            shipWorths.put(shp, 1f * engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(
                                    shp).getMember().getFleetPointCost());
                            if (!shp.isFighter()) {
                                fp += engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(shp).getMember().getFleetPointCost()
                                        * 2f;
                            }
                        }
                        case 3 -> {
                            // Elite
                            shipWorths.put(shp, 2f * engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(
                                    shp).getMember().getFleetPointCost());
                            if (!shp.isFighter()) {
                                fp += engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(shp).getMember().getFleetPointCost()
                                        * 3f;
                            }
                        }
                        case 2 -> {
                            // Powered
                            shipWorths.put(shp, 1f * engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(
                                    shp).getMember().getFleetPointCost());
                            if (!shp.isFighter()) {
                                fp += engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(shp).getMember().getFleetPointCost()
                                        * 2.5f;
                            }
                        }
                        case 1 -> {
                            // Armored
                            shipWorths.put(shp, 1f * engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(
                                    shp).getMember().getFleetPointCost());
                            if (!shp.isFighter()) {
                                fp += engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(shp).getMember().getFleetPointCost()
                                        * 2f;
                            }
                        }
                        case 0 -> {
                            shipWorths.put(shp, 0.5f
                                    * engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(shp).getMember().getFleetPointCost());
                            if (!shp.isFighter()) {
                                fp += engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(shp).getMember().getFleetPointCost();
                            }
                        }
                    }
                }
            }

            for (ShipAPI newShip : spawnList) {
                if (!newShip.isFighter()) {
                    type = shipTypes.get(newShip);
                    switch (type) {
                        case 3 -> {
                            newShip.setCRAtDeployment(0.8f);
                            newShip.setCurrentCR(0.8f);
                        }
                        case 2, 4 -> {
                            newShip.setCRAtDeployment(0.7f);
                            newShip.setCurrentCR(0.7f);
                        }
                        case 1 -> {
                            newShip.setCRAtDeployment(0.5f);
                            newShip.setCurrentCR(0.5f);
                        }
                        case 0 -> {
                            newShip.setCRAtDeployment(0.6f);
                            newShip.setCurrentCR(0.6f);
                        }
                    }
                    engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(newShip).getMember().getCaptain().setPersonality(
                            "aggressive");
                    newShip.getShipAI().forceCircumstanceEvaluation();
                    newShip.getShipAI().cancelCurrentManeuver();
                }
            }
        }

        for (ShipAPI ship : ships) {
            if (!ship.isAlive()) {
                if (engine.isEntityInPlay(ship) && !BOSS_SHIPS.containsKey(ship.getHullSpec().getHullId())) {
                    engine.removeEntity(ship);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void init(CombatEngineAPI engine) {
        if (first) {
            first = false;
            init();
            engine.getCustomData().put(DATA_KEY_PLUGIN, this);
        }

        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
        mapX = engine.getMapWidth();
        mapY = engine.getMapHeight();
        combatOver = false;
        try {
            reloadSettings();
        } catch (Exception ex) {
            maxPoints = 50f;
            buffLevel = 0f;
            threshold = 0;
            bossLevel = 0;
            //SWP_MusicPlayer.setVolume(0.75f);
            Global.getLogger(MissionPlugin.class).log(Level.ERROR, ex);
        }
        points = 0;
        pointsStatic = 0;
        bossFlash = 0f;
        snapback = 0f;
        engine.getPlayerShip().getMutableStats().getHullDamageTakenMult().modifyMult("startingbonus", 0f);
        engine.getPlayerShip().getMutableStats().getEmpDamageTakenMult().modifyMult("startingbonus", 0f);
        engine.getPlayerShip().getMutableStats().getArmorDamageTakenMult().modifyMult("startingbonus", 0.00001f);
        List<Float> notches = new ArrayList<>(13);
        notches.add(0.075f * 1.1428571f);
        notches.add(0.125f * 1.1428571f);
        notches.add(0.2f * 1.1428571f);
        notches.add(0.25f * 1.1428571f);
        notches.add(0.325f * 1.1428571f);
        notches.add(0.375f * 1.1428571f);
        notches.add(0.45f * 1.1428571f);
        notches.add(0.5f * 1.1428571f);
        notches.add(0.575f * 1.1428571f);
        notches.add(0.625f * 1.1428571f);
        notches.add(0.7f * 1.1428571f);
        notches.add(0.75f * 1.1428571f);
        notches.add(0.825f * 1.1428571f);
        pbar = new ProgressBar(0f, notches, new Vector2f(Global.getSettings().getScreenWidth() - 10.5f,
                Global.getSettings().getScreenHeight() * 0.375f
                + 20f),
                Global.getSettings().getScreenHeight()
                * 0.75f - 20f, 12f);

        /*
         SWP_MusicPlayer.addToPlaylist("arcade_theme_1", 319f); SWP_MusicPlayer.addToPlaylist("arcade_theme_2", 173f);
         SWP_MusicPlayer.addToPlaylist("arcade_theme_3", 192f); if (!SWP_MusicPlayer.isPlaying()) { SWP_MusicPlayer.nextMusic(PlayMode.SHUFFLE); } else {
         SWP_MusicPlayer.setPlaylistMode(PlayMode.SHUFFLE); }
         */
    }

    public void reloadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(MissionDefinition.SETTINGS_FILE);

        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            threshold = LunaSettings.getInt("swp", "arcadeStartingDifficulty");
        } else {
            threshold = settings.getInt("startingDifficulty");
        }
        switch (threshold) {
            case 1 -> {
                maxPoints = 57.5f;
                buffLevel = 0.2f;
                bossLevel = 0;
            }
            case 2 -> {
                maxPoints = 70f;
                buffLevel = 0.55f;
                bossLevel = 2;
            }
            case 3 -> {
                maxPoints = 82.5f;
                buffLevel = 0.935f;
                bossLevel = 4;
            }
            case 4 -> {
                maxPoints = 95f;
                buffLevel = 1.365f;
                bossLevel = 6;
            }
            case 5 -> {
                maxPoints = 107.5f;
                buffLevel = 1.85f;
                bossLevel = 8;
            }
            case 6 -> {
                maxPoints = 120f;
                buffLevel = 2.375f;
                bossLevel = 10;
            }
            case 7 -> {
                maxPoints = 132.5f;
                buffLevel = 2.965f;
                bossLevel = 14;
            }
            default -> {
                maxPoints = 50f;
                buffLevel = 0f;
                bossLevel = 0;
            }
        }

        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            scale = (float) (double) LunaSettings.getDouble("swp", "arcadeDifficultyScalar");
        } else {
            scale = (float) settings.getDouble("difficultyScalar");
        }

        //SWP_MusicPlayer.setVolume((float) settings.getDouble("musicVolume"));
    }

    private void heal() {
        engine.getPlayerShip().getFluxTracker().setHardFlux(0f);
        engine.getPlayerShip().getFluxTracker().setCurrFlux(0f);
        engine.getPlayerShip().setHitpoints(engine.getPlayerShip().getMaxHitpoints());
        ArmorGridAPI armorGrid = engine.getPlayerShip().getArmorGrid();
        for (int x = 0; x < armorGrid.getGrid().length; x++) {
            for (int y = 0; y < armorGrid.getGrid()[x].length; y++) {
                armorGrid.setArmorValue(x, y, armorGrid.getMaxArmorInCell());
            }
        }
    }

    private void init() {
        FACTIONS.add(Factions.PIRATES, 1f);
        FACTIONS.add(Factions.HEGEMONY, 1f);
        FACTIONS.add(Factions.DIKTAT, 0.5f);
        FACTIONS.add(Factions.LIONS_GUARD, 0.25f);
        FACTIONS.add(Factions.INDEPENDENT, 1f);
        FACTIONS.add(Factions.SCAVENGERS, 0.25f);
        FACTIONS.add(Factions.TRITACHYON, 1f);
        FACTIONS.add(Factions.PERSEAN, 1f);
        FACTIONS.add(Factions.LUDDIC_PATH, 0.5f);
        FACTIONS.add(Factions.LUDDIC_CHURCH, 1f);
        FACTIONS.add(Factions.DERELICT, 0.25f);
        FACTIONS.add(Factions.REMNANTS, 0.5f);
        FACTIONS.add(Factions.OMEGA, 0.1f);
        //FACTIONS.add(Factions.THREAT, 0.25f);
        FACTIONS.add(Factions.DWELLER, 0.1f);
        FACTIONS.add("domain", 0.25f);
        FACTIONS.add("sector", 0.5f);
        FACTIONS.add("everything", 1f);
        if (SWPModPlugin.hasUnderworld) {
            FACTIONS.add("cabal", 0.25f);
        }
        if (SWPModPlugin.imperiumExists) {
            FACTIONS.add("interstellarimperium", 1f);
        }
        if (SWPModPlugin.blackrockExists) {
            FACTIONS.add("blackrock_driveyards", 0.75f);
            FACTIONS.add("br_consortium", 0.25f);
        }
        if (SWPModPlugin.exigencyExists) {
            FACTIONS.add("exipirated", 0.5f);
            FACTIONS.add("exigency", 0.75f);
        }
        if (SWPModPlugin.templarsExists) {
            FACTIONS.add("templars", 0.25f);
        }
        if (SWPModPlugin.shadowyardsExists) {
            FACTIONS.add("shadow_industry", 1f);
        }
        if (SWPModPlugin.junkPiratesExists) {
            FACTIONS.add("junk_pirates", 0.75f);
            FACTIONS.add("pack", 0.75f);
            FACTIONS.add("syndicate_asp", 0.5f);
        }
        if (SWPModPlugin.scyExists) {
            FACTIONS.add("SCY", 1f);
        }
        if (SWPModPlugin.tiandongExists) {
            FACTIONS.add("tiandong", 1f);
        }
        if (SWPModPlugin.diableExists) {
            FACTIONS.add("diableavionics", 1f);
        }
        if (SWPModPlugin.oraExists) {
            FACTIONS.add("ORA", 1f);
        }
        if (SWPModPlugin.tyradorExists) {
            FACTIONS.add("Coalition", 1f);
        }
        if (SWPModPlugin.iceExists) {
            FACTIONS.add("sun_ice", 0.75f);
        }
        if (SWPModPlugin.borkenExists) {
            FACTIONS.add("fob", 0.5f);
        }
        if (SWPModPlugin.scalarTechExists) {
            FACTIONS.add("scalartech", 0.5f);
            FACTIONS.add("scalartech_elite", 0.25f);
        }
        if (SWPModPlugin.dmeExists) {
            FACTIONS.add("dassault_mikoyan", 0.75f);
            FACTIONS.add("6eme_bureau", 0.25f);
            FACTIONS.add("blade_breakers", 0.25f);
        }
        if (SWPModPlugin.arkgneisisExists) {
            FACTIONS.add("al_ars", 1f);
        }
    }

    private void makeBoss(int level) {
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, Integer> shipTypes = localData.shipTypes;
        final Map<ShipAPI, Float> shipWorths = localData.shipWorths;

        String id;
        switch (level) {
            case 1 ->
                id = "swp_arcade_oberon_ult";
            case 2 ->
                id = "swp_arcade_ultron_ult";
            case 3 ->
                id = "swp_arcade_zeus_ult";
            case 4 ->
                id = "swp_arcade_ezekiel_ult";
            case 5 ->
                id = "swp_arcade_cristarium_ult";
            case 6 ->
                id = "swp_arcade_superhyperion_war";
            case 7 -> {
                switch (ultralevel) {
                    case 2:
                        id = "swp_arcade_hyperzero_ult";
                        break;
                    case 1:
                        id = "swp_arcade_superzero_ult";
                        break;
                    default:
                    case 0:
                        id = "swp_arcade_zero_ult";
                        break;
                }
            }
            default ->
                id = "swp_arcade_superhyperion_str";
        }
        mapX = engine.getMapWidth();
        mapY = engine.getMapHeight();
        ShipAPI boss = engine.getFleetManager(FleetSide.ENEMY).spawnShipOrWing(id,
                SWP_Util.getSafeSpawn(500f, FleetSide.ENEMY, mapX, mapY, false),
                270f, 3f);
        shipWorths.put(boss,
                engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(boss).getMember().getFleetPointCost()
                * 2f);
        shipTypes.put(boss, 99);
        boss.setCRAtDeployment(1f);
        boss.setCurrentCR(1f);
        engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(boss).getMember().getCaptain().setPersonality(
                "aggressive");
        boss.getShipAI().forceCircumstanceEvaluation();
        boss.getShipAI().cancelCurrentManeuver();
        bar1 = new HealthBar(boss, new Vector2f(Global.getSettings().getScreenWidth() / 2f, 20f), 35f, 400f);
        if (level == 6) {
            id = "swp_arcade_superhyperion_hul";
            boss
                    = engine.getFleetManager(FleetSide.ENEMY).spawnShipOrWing(id,
                            SWP_Util.getSafeSpawn(500f, FleetSide.ENEMY, mapX, mapY, false),
                            270f, 3f);
            shipWorths.put(boss,
                    engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(boss).getMember().getFleetPointCost()
                    * 2f);
            shipTypes.put(boss, 99);
            boss.setCRAtDeployment(1f);
            boss.setCurrentCR(1f);
            engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(boss).getMember().getCaptain().setPersonality(
                    "aggressive");
            boss.getShipAI().forceCircumstanceEvaluation();
            boss.getShipAI().cancelCurrentManeuver();
            bar2 = new HealthBar(boss, new Vector2f(Global.getSettings().getScreenWidth() / 2f, 60f), 35f, 400f);
            id = "swp_arcade_superhyperion_she";
            boss
                    = engine.getFleetManager(FleetSide.ENEMY).spawnShipOrWing(id,
                            SWP_Util.getSafeSpawn(500f, FleetSide.ENEMY, mapX, mapY, false),
                            270f, 3f);
            shipWorths.put(boss,
                    engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(boss).getMember().getFleetPointCost()
                    * 2f);
            shipTypes.put(boss, 99);
            boss.setCRAtDeployment(1f);
            boss.setCurrentCR(1f);
            engine.getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(boss).getMember().getCaptain().setPersonality(
                    "aggressive");
            boss.getShipAI().forceCircumstanceEvaluation();
            boss.getShipAI().cancelCurrentManeuver();
            bar3 = new HealthBar(boss, new Vector2f(Global.getSettings().getScreenWidth() / 2f, 100f), 35f, 400f);
        }
    }

    private ShipAPI makeShip() {
        int bound = 100;
        while (bound > 0) {
            bound--;

            String role = ROLES.pick();
            List<FleetMemberAPI> members = new ArrayList<>(1);
            if (Math.random() > 0.025) {
                String faction = FACTIONS.pick();
                try {
                    List<ShipRolePick> picks = Global.getSector().getFaction(faction).pickShip(role, ShipPickParams.all());
                    for (ShipRolePick pick : picks) {
                        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, pick.variantId);
                        members.add(member);
                    }
                } catch (NullPointerException ex) {
                    /* Whoops lol */
                    Global.getLogger(MissionPlugin.class).warn("NPE when spawning " + faction + " " + role);
                }
            } else {
                for (int i = 0; i < 10; i++) {
                    String hull = SPECIAL_SHIPS.pick();
                    FleetMemberAPI member;
                    try {
                        member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, hull);
                    } catch (Exception e) {
                        member = null;
                    }
                    if (member != null) {
                        members.add(member);
                        break;
                    }
                }
            }
            if (members.isEmpty()) {
                continue;
            }

            for (FleetMemberAPI member : members) {
                if (!member.isFighterWing()) {
                    String faction = FACTIONS.pick();

                    float bonus = threshold * 5f;
                    if (SPECIAL_SHIPS.getItems().contains(member.getHullId() + "_cus")
                            || SPECIAL_SHIPS.getItems().contains(member.getHullId())) {
                        bonus += 30f;
                    }

                    List<String> factionList = new ArrayList<>(1);
                    factionList.add(faction);

                    if (SWPModPlugin.hasDynaSector) {
                        DS_VariantRandomizer.setVariant(member, factionList, null, Archetype.ARCADE, (float) Math.random(),
                                bonus, false, new Random());

                        if (!member.getVariant().getDisplayName().contentEquals("Arcade")) {
                            continue;
                        }
                    }

                    member.getRepairTracker().setCrashMothballed(false);
                    member.getRepairTracker().setMothballed(false);
                    member.getCrewComposition().addCrew(member.getNeededCrew());
                    member.getRepairTracker().setCR(1f);
                    member.setOwner(1);
                    member.setAlly(false);
                    mapX = engine.getMapWidth();
                    mapY = engine.getMapHeight();
                    Global.getLogger(MissionPlugin.class).info("Spawning " + faction + " " + member.getHullId());
                    return engine.getFleetManager(FleetSide.ENEMY).spawnFleetMember(member, SWP_Util.getSafeSpawn(
                            SWP_Util.getMemberRadiusEstimate(member), FleetSide.ENEMY, mapX, mapY, false),
                            270f, 3f);
                }
            }
        }
        return null;
    }

    private static final class LocalData {

        final Map<ShipAPI, Integer> shipTypes = new LinkedHashMap<>(100);
        final Map<ShipAPI, Float> shipWorths = new LinkedHashMap<>(100);
    }
}
