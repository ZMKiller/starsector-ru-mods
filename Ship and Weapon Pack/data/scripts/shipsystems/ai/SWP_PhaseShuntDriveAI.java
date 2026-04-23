package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import data.scripts.shipsystems.SWP_PhaseShuntDriveStats;
import data.scripts.util.SWP_Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

/* They said it couldn't be done */
public class SWP_PhaseShuntDriveAI implements ShipSystemAIScript {

    private static final float SECONDS_TO_LOOK_AHEAD = 0.25f;
    private static final float RE_EVAL_TIME = 1f;
    private static final boolean DEBUG = false;
    private final Object STATUSKEY1 = new Object();
    private final Object STATUSKEY2 = new Object();

    private CombatEngineAPI engine = null;
    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final ShipAIConfig savedConfig = new ShipAIConfig();

    private float blockPhaseTimer = 0f;
    private float reEvalTimer = 0f;
    private boolean forceAggressiveAI = false;
    private boolean forceTimidAI = false;
    private boolean forceStrafeAI = false;

    private final CollectionUtils.CollectionFilter<DamagingProjectileAPI> filterMisses = new CollectionUtils.CollectionFilter<DamagingProjectileAPI>() {
        @Override
        public boolean accept(DamagingProjectileAPI proj) {
            if (proj.getOwner() == ship.getOwner()) {
                return false;
            }

            if (proj instanceof MissileAPI) {
                MissileAPI missile = (MissileAPI) proj;
                if (missile.isFlare()) {
                    return false;
                }
            }

            return (CollisionUtils.getCollides(proj.getLocation(), Vector2f.add(proj.getLocation(),
                    (Vector2f) new Vector2f(proj.getVelocity()).scale(SECONDS_TO_LOOK_AHEAD), null), ship.getLocation(), ship.getCollisionRadius() + proj.getCollisionRadius())
                    && Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation()))) <= 90f);
        }
    };

    private static float getArmorLevel(ShipAPI ship) {
        if (ship == null || !Global.getCombatEngine().isEntityInPlay(ship)) {
            return 0f;
        }
        float current = 0f;
        float total = 0f;
        ArmorGridAPI armorGrid = ship.getArmorGrid();
        for (int x = 0; x < armorGrid.getGrid().length; x++) {
            for (int y = 0; y < armorGrid.getGrid()[x].length; y++) {
                current += armorGrid.getArmorFraction(x, y);
                total += 1f;
            }
        }
        return current / total;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        boolean wantToBeUnphased = false;
        boolean wantToBePhased = false;
        boolean mustUnphaseNow = false;

        flags.setFlag(AIFlags.DO_NOT_VENT, 1f);
        flags.unsetFlag(AIFlags.DO_NOT_USE_FLUX);
        if (ship.getCurrFlux() <= 300f) {
            mustUnphaseNow = true;
            if (ship.isPhased()) {
                if (ship.getPhaseCloak().isActive()) {
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                }
            } else {
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            wantToBeUnphased = true;
        }
        if (ship.isPhased() && (ship.getCurrFlux() <= 75f)) {
            ship.getPhaseCloak().deactivate();
        }

        float fluxLevel = ship.getFluxTracker().getCurrFlux() / ship.getMutableStats().getFluxCapacity().getBaseValue();
        float range = SWP_PhaseShuntDriveStats.BASE_RANGE / (1f + fluxLevel);
        range = ship.getMutableStats().getSystemRangeBonus().computeEffective(range);
        float fluxMargin = (ship.getMaxFlux() - 100f) - ship.getCurrFlux();

        float armorLevel = getArmorLevel(ship);
        float armorLevelSq = armorLevel * armorLevel;

        boolean unphaseDecision = !ship.getFluxTracker().isOverloadedOrVenting();

        int baseSize = engine.getProjectiles().size() / 4;
        List<CombatEntityAPI> possibleTargets = new ArrayList<>(baseSize);
        possibleTargets.addAll(SWP_Util.getProjectilesWithinRange(ship.getLocation(), range));
        possibleTargets.addAll(SWP_Util.getMissilesWithinRange(ship.getLocation(), range));
        possibleTargets.addAll(SWP_Util.getAsteroidsWithinRange(ship.getLocation(), range));
        float fluxGain = 0f;
        for (CombatEntityAPI possibleTarget : possibleTargets) {
            if (possibleTarget == null) {
                continue;
            }

            float level;
            if (possibleTarget instanceof DamagingProjectileAPI) {
                DamagingProjectileAPI proj = (DamagingProjectileAPI) possibleTarget;
                if (proj.getBaseDamageAmount() <= 0) {
                    continue;
                }
                if (proj.didDamage()) {
                    continue;
                }
                if (proj.getSpawnType() == ProjectileSpawnType.OTHER) {
                    continue;
                }
                if ((proj.getProjectileSpecId() != null) && proj.getProjectileSpecId().startsWith("swp_excelsiorcannon_shot") && (proj.getOwner() == ship.getOwner())) {
                    continue;
                }

                if (proj.getDamageType() == DamageType.FRAGMENTATION) {
                    level = (proj.getDamageAmount() * 0.25f) + (proj.getEmpAmount() * 0.25f);
                } else {
                    level = proj.getDamageAmount() + (proj.getEmpAmount() * 0.25f);
                }
            } else {
                level = possibleTarget.getMass();
            }

            fluxGain += level;
        }

        /* Repair up! */
        if (!wantToBeUnphased && (ship.getHullLevel() <= 0.9f) && (fluxLevel >= ((1f - ship.getHullLevel()) * (1f - armorLevel))) && (ship.getCurrFlux() > 300f)) {
            if (!ship.isPhased()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                flags.setFlag(AIFlags.KEEP_SHIELDS_ON, 0.3f);
                flags.setFlag(AIFlags.STAY_PHASED, 0.3f);
            } else if (!mustUnphaseNow) {
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            wantToBePhased = true;
            unphaseDecision = false;
        }

        /* Flamed out! */
        if (!wantToBeUnphased && (ship.getEngineController().isFlamedOut() || ship.getEngineController().isFlamingOut()) && (ship.getCurrFlux() > 300f)) {
            if (!ship.isPhased()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                flags.setFlag(AIFlags.KEEP_SHIELDS_ON, 0.3f);
                flags.setFlag(AIFlags.STAY_PHASED, 0.3f);
            } else if (!mustUnphaseNow) {
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            wantToBePhased = true;
            unphaseDecision = false;
        }

        float projectileDanger = 0f;
        List<DamagingProjectileAPI> nearbyThreats = new ArrayList<>(baseSize);
        for (DamagingProjectileAPI tmp : engine.getProjectiles()) {
            if (MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), ship.getCollisionRadius() * 5f)) {
                nearbyThreats.add(tmp);
            }
        }
        nearbyThreats = CollectionUtils.filter(nearbyThreats, filterMisses);
        Set<DamagingProjectileAPI> threatSet = new HashSet<>(nearbyThreats);
        List<MissileAPI> nearbyMissiles = SWP_Util.getMissilesWithinRange(ship.getLocation(), ship.getCollisionRadius() * 2f);
        for (MissileAPI missile : nearbyMissiles) {
            if (missile.getOwner() == ship.getOwner()) {
                continue;
            }
            if (!missile.getEngineController().isTurningLeft() && !missile.getEngineController().isTurningRight()) {
                continue;
            }
            if (!threatSet.contains(missile)) {
                nearbyThreats.add(missile);
            }
        }
        for (DamagingProjectileAPI threat : nearbyThreats) {
            float damage = threat.getDamageAmount();
            switch (threat.getDamageType()) {
                case FRAGMENTATION:
                    damage *= SWP_Util.lerp(0.5f, 0.25f, armorLevelSq);
                    break;
                case KINETIC:
                    damage *= SWP_Util.lerp(0.75f, 0.5f, armorLevelSq);
                    break;
                case HIGH_EXPLOSIVE:
                    damage *= SWP_Util.lerp(1f, 1.5f, armorLevelSq);
                    break;
                default:
                    break;
            }
            projectileDanger += Math.pow(damage + (0.25f * threat.getEmpAmount()) / 100f, 1.3f) * 100f;
        }

        if (!wantToBeUnphased && ((fluxGain >= fluxMargin) || AIUtils.canUseSystemThisFrame(ship))) {
            float projectileDangerThreshold = 100f * ((ship.getHullLevel() * 0.75f) + 0.25f) * (2f - ship.getFluxLevel());
            if ((projectileDanger >= projectileDangerThreshold) && (ship.getCurrFlux() > 300f)) {
                if (!ship.isPhased()) {
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                    flags.setFlag(AIFlags.KEEP_SHIELDS_ON, 0.3f);
                    flags.setFlag(AIFlags.STAY_PHASED, 0.3f);
                } else if (!mustUnphaseNow) {
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                }
                wantToBePhased = true;
                unphaseDecision = false;
            }
        }

        float beamDanger = 0f;
        List<BeamAPI> nearbyBeams = engine.getBeams();
        for (BeamAPI beam : nearbyBeams) {
            /* This is potentially a lot of line-circle-collision tracing, but it's a relatively inexpensive check */
            if ((beam.getDamageTarget() == ship) || (ship.isPhased() && CollisionUtils.getCollides(beam.getFrom(), beam.getTo(), ship.getLocation(), ship.getCollisionRadius() * 1.5f))) {
                float damage;
                float emp = beam.getWeapon().getDerivedStats().getEmpPerSecond();
                if (beam.getWeapon().getDerivedStats().getSustainedDps() < beam.getWeapon().getDerivedStats().getDps()) {
                    damage = beam.getWeapon().getDerivedStats().getBurstDamage() / beam.getWeapon().getDerivedStats().getBurstFireDuration();
                } else {
                    damage = beam.getWeapon().getDerivedStats().getDps();
                }
                switch (beam.getWeapon().getDamageType()) {
                    case FRAGMENTATION:
                        damage *= SWP_Util.lerp(0.5f, 0.25f, armorLevelSq);
                        break;
                    case KINETIC:
                        damage *= SWP_Util.lerp(0.75f, 0.5f, armorLevelSq);
                        break;
                    case HIGH_EXPLOSIVE:
                        damage *= SWP_Util.lerp(1f, 1.5f, armorLevelSq);
                        break;
                    default:
                        break;
                }
                beamDanger += Math.pow((damage + (0.25f * emp)) / 100f, 1.3f) * 100f;
            }
        }

        float beamDangerThreshold = 100f * ((ship.getHullLevel() * 0.75f) + 0.25f) * (2f - ship.getFluxLevel());
        if (!wantToBeUnphased && (beamDanger >= beamDangerThreshold) && (ship.getCurrFlux() > 300f)) {
            if (!ship.isPhased()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                flags.setFlag(AIFlags.KEEP_SHIELDS_ON, 0.3f);
                flags.setFlag(AIFlags.STAY_PHASED, 0.3f);
            } else if (!mustUnphaseNow) {
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            wantToBePhased = true;
            unphaseDecision = false;
        }

        /* Don't phase prematurely */
        if (!ship.isPhased() && !wantToBePhased && !flags.hasFlag(AIFlags.PHASE_ATTACK_RUN)
                && !flags.hasFlag(AIFlags.PURSUING) && !flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
            float projectileDangerLowThreshold = 25f * ((ship.getHullLevel() * 0.75f) + 0.25f) * (2f - ship.getFluxLevel());
            float beamDangerLowThreshold = 25f * ((ship.getHullLevel() * 0.75f) + 0.25f) * (2f - ship.getFluxLevel());
            if ((projectileDanger < projectileDangerLowThreshold) && (beamDanger < beamDangerLowThreshold)) {
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
        }

        float decisionLevel = 0f;
        if (fluxGain >= fluxMargin) {
            /* We want the AI to try to save itself in extreme circumstances, even if it overloads */
            decisionLevel -= 200f + Math.min(200f, (fluxGain - fluxMargin) / 50f);
            if (ship.getEngineController().isFlamedOut() || ship.getEngineController().isFlamingOut()) {
                decisionLevel += (1.1f - ship.getFluxLevel()) * (projectileDanger / 250f);
            } else {
                decisionLevel += Math.max(0f, 2f * (0.5f - ship.getFluxLevel())) * (projectileDanger / 1000f);
            }
            decisionLevel -= beamDanger / 100f;
        } else {
            decisionLevel += fluxGain / 30f;
            decisionLevel += Math.max(0f, 2f * (0.5f - ship.getFluxLevel())) * (projectileDanger / 500f);
            decisionLevel += Math.max(0f, 2f * (0.5f - ship.getFluxLevel())) * (beamDanger / 100f);
        }

        if (flags.hasFlag(AIFlags.HARASS_MOVE_IN)) {
            if (fluxGain < fluxMargin) {
                decisionLevel += fluxGain / 300f;
            }
        }

        if (flags.hasFlag(AIFlags.PURSUING)) {
            if (fluxGain < fluxMargin) {
                decisionLevel += fluxGain / 300f;
            }
        }

        if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
            if (fluxGain < fluxMargin) {
                decisionLevel += fluxGain / 300f;
            }
            unphaseDecision = false;
        }

        if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN)) {
            if (fluxGain < fluxMargin) {
                decisionLevel += fluxGain / 150f;
            }
            if (!flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT)) {
                unphaseDecision = false;
            }
        }

        if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_FROM_BEHIND_DIST_CRITICAL) || flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT)) {
            if (fluxGain < fluxMargin) {
                decisionLevel += fluxGain / 150f;
            }
        }

        if (fluxGain >= fluxMargin) {
            decisionLevel *= 4f * (1f - (ship.getHullLevel() * 0.75f));
        }

        float decisionTarget = Math.max(50f, 200f - (50f * ship.getSystem().getAmmo()));
        if (fluxGain >= fluxMargin) {
            decisionTarget = 250f;
        }
        if (!ship.isPhased() && !wantToBePhased) {
            if (decisionLevel >= decisionTarget) {
                if (AIUtils.canUseSystemThisFrame(ship)) {
                    ship.useSystem();
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                    blockPhaseTimer = 0.1f;
                    unphaseDecision = false;
                }
            }
        }

        if (DEBUG) {
            if (engine.getPlayerShip() == ship) {
                engine.maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(),
                        "AI", "Desire: " + Math.round(100f * decisionLevel) + "/" + Math.round(100f * decisionTarget), decisionLevel < decisionTarget);
                engine.maintainStatusForPlayerShip(STATUSKEY2, system.getSpecAPI().getIconSpriteName(),
                        "AI", "Flux Gain: " + Math.round(fluxGain), fluxGain >= fluxMargin);
            }
        }

        if (blockPhaseTimer > 0f) {
            blockPhaseTimer -= amount;
            if (!mustUnphaseNow) {
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            unphaseDecision = false;
        }

        if (reEvalTimer > 0f) {
            reEvalTimer -= amount;
        }
        WeaponAPI excelsiorCannon = null;
        for (WeaponAPI shipWeapon : ship.getAllWeapons()) {
            if (shipWeapon.getId().startsWith("swp_excelsiorcannon")) {
                excelsiorCannon = shipWeapon;
                break;
            }
        }
        if (excelsiorCannon != null) {
            WeaponGroupAPI excelsiorCannonGroup = null;
            if (!excelsiorCannon.isDisabled() && !excelsiorCannon.isPermanentlyDisabled() && !excelsiorCannon.isFiring()) {
                excelsiorCannonGroup = ship.getWeaponGroupFor(excelsiorCannon);
            }

            if (excelsiorCannonGroup != null) {
                int groupNum = 0;
                boolean foundGroup = false;
                for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                    if (group == excelsiorCannonGroup) {
                        foundGroup = true;
                        break;
                    } else {
                        groupNum++;
                    }
                }
                if (foundGroup) {
                    if (ship.getFluxLevel() >= 0.25f) {
                        if (!excelsiorCannonGroup.isAutofiring()) {
                            ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, groupNum);
                        }
                    } else if (ship.getFluxLevel() < 0.05f) {
                        if (excelsiorCannonGroup.isAutofiring()) {
                            ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, groupNum);
                        }
                    }
                }
            }
        }

        float engageRange = 1000f;
        float minWeaponRange = engageRange;
        for (WeaponAPI weapon : ship.getUsableWeapons()) {
            if (weapon.getType() == WeaponType.MISSILE) {
                continue;
            }
            minWeaponRange = Math.min(minWeaponRange, weapon.getRange());
            if (weapon.getRange() > engageRange) {
                engageRange = weapon.getRange();
            }
        }
        float optimalRange = 0f;
        float eligibleOP = 0f;
        for (WeaponAPI weapon : ship.getUsableWeapons()) {
            if (weapon.getType() == WeaponType.MISSILE) {
                continue;
            }
            if (weapon.hasAIHint(AIHints.PD) && !weapon.hasAIHint(AIHints.PD_ALSO)) {
                continue;
            }
            float opCost = Math.max(weapon.getSpec().getOrdnancePointCost(null), 1f);
            eligibleOP += opCost;
            optimalRange += weapon.getRange() * opCost;
        }
        if (eligibleOP >= 1f) {
            optimalRange /= eligibleOP;
        } else {
            optimalRange = minWeaponRange;
        }
        ShipAPI immediateShipTarget;
        if (flags.getCustom(AIFlags.MANEUVER_TARGET) instanceof ShipAPI) {
            immediateShipTarget = (ShipAPI) flags.getCustom(AIFlags.MANEUVER_TARGET);
        } else {
            immediateShipTarget = ship.getShipTarget();
        }
        boolean immediateTargetNearDangerousDeath = false;
        if ((immediateShipTarget != null) && !immediateShipTarget.isFighter() && !immediateShipTarget.isFrigate() && (immediateShipTarget.getHullLevel() <= 0.2f)) {
            immediateTargetNearDangerousDeath = true;
        }
        float tooCloseRange;
        if (immediateTargetNearDangerousDeath) {
            tooCloseRange = (optimalRange / 2f) + ship.getCollisionRadius();
        } else {
            tooCloseRange = (optimalRange / 3f) + ship.getCollisionRadius();
        }
        boolean immediateTargetTooClose = false;
        if ((immediateShipTarget != null) && !immediateShipTarget.isFighter() && (MathUtils.getDistance(immediateShipTarget, ship) < (tooCloseRange - ship.getCollisionRadius()))) {
            immediateTargetTooClose = true;
        }
        boolean immediateTargetTooFar = false;
        if ((immediateShipTarget != null) && !immediateShipTarget.isFighter() && (MathUtils.getDistance(immediateShipTarget, ship) >= (engageRange - ship.getCollisionRadius()))) {
            immediateTargetTooFar = true;
        }
        float goodRange;
        if (immediateTargetNearDangerousDeath) {
            goodRange = (optimalRange / 1.25f) + ship.getCollisionRadius();
        } else {
            goodRange = (optimalRange / 1.5f) + ship.getCollisionRadius();
        }
        boolean immediateTargetAtGoodRange = false;
        if (!immediateTargetTooFar && (immediateShipTarget != null) && !immediateShipTarget.isFighter() && (MathUtils.getDistance(immediateShipTarget, ship) >= (goodRange - ship.getCollisionRadius()))) {
            immediateTargetAtGoodRange = true;
        }
        boolean smallCraftTarget = false;
        if ((immediateShipTarget != null) && immediateShipTarget.isFighter()) {
            smallCraftTarget = true;
        }

        List<ShipAPI> collisionTargets = CombatUtils.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius());
        boolean collisionDanger = false;
        for (ShipAPI collisionTarget : collisionTargets) {
            if ((ship != collisionTarget) && (collisionTarget.getCollisionClass() == CollisionClass.SHIP)) {
                collisionDanger = true;
            }
        }
        if (immediateTargetNearDangerousDeath && (immediateShipTarget != null) && (MathUtils.getDistance(immediateShipTarget, ship) < (optimalRange / 4f))) {
            collisionDanger = true;
        }

        boolean beingAggressive = false;
        if (!((ship.isPhased() && collisionDanger && (ship.getFluxLevel() < 0.5f)) || ship.getFluxTracker().isOverloadedOrVenting()) && (ship.getFluxLevel() >= 0.3f)) {
            if (forceTimidAI || forceStrafeAI) {
                forceTimidAI = false;
                forceStrafeAI = false;
                reEvalTimer = 0f;
                restoreAIConfig(ship);
            }

            if (collisionDanger || immediateTargetTooClose) {
                flags.setFlag(AIFlags.BACK_OFF, 0.2f);
                flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
            } else {
                beingAggressive = true;
                if (immediateTargetAtGoodRange || immediateTargetTooFar || smallCraftTarget) {
                    flags.setFlag(AIFlags.DO_NOT_BACK_OFF, 0.2f);
                    flags.unsetFlag(AIFlags.BACK_OFF);
                    if ((!immediateTargetTooFar || smallCraftTarget) && forceAggressiveAI) {
                        ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                        ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = false;
                    }
                }
            }
            flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
            flags.unsetFlag(AIFlags.RUN_QUICKLY);
            flags.unsetFlag(AIFlags.DO_NOT_AUTOFIRE_NON_ESSENTIAL_GROUPS);

            if ((reEvalTimer <= 0f) && !forceAggressiveAI) {
                forceAggressiveAI = true;
                reEvalTimer = RE_EVAL_TIME;
                saveAIConfig(ship);
                if ((immediateTargetAtGoodRange && !immediateTargetTooFar) || smallCraftTarget) {
                    ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                    ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = false;
                }
                ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor = false;
                ship.getShipAI().getConfig().personalityOverride = SWP_Util.getMoreAggressivePersonality(CombatUtils.getFleetMember(ship), ship);
                ship.getShipAI().forceCircumstanceEvaluation();
            }
        } else {
            if (immediateTargetTooClose) {
                flags.setFlag(AIFlags.BACK_OFF, 0.2f);
            }
            if ((reEvalTimer <= 0f) && forceAggressiveAI) {
                forceAggressiveAI = false;
                reEvalTimer = RE_EVAL_TIME;
                flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
                restoreAIConfig(ship);
                ship.getShipAI().forceCircumstanceEvaluation();
                ship.getShipAI().cancelCurrentManeuver();
            }
        }

        /* IMMEDIATELY react if in this situation; don't collide to death!!! */
        if ((ship.isPhased() && collisionDanger && (ship.getFluxLevel() < 0.5f)) || ship.getFluxTracker().isOverloadedOrVenting()) {
            if (forceTimidAI || forceStrafeAI) {
                forceAggressiveAI = false;
                forceStrafeAI = false;
                reEvalTimer = 0f;
                restoreAIConfig(ship);
            }

            flags.setFlag(AIFlags.BACK_OFF, 0.2f);
            flags.setFlag(AIFlags.DO_NOT_PURSUE, 0.2f);
            flags.setFlag(AIFlags.RUN_QUICKLY, 0.2f);
            flags.unsetFlag(AIFlags.DO_NOT_BACK_OFF);
            if (!ship.getFluxTracker().isOverloadedOrVenting()) {
                if (!mustUnphaseNow) {
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                }
                blockPhaseTimer = 0.1f;
                unphaseDecision = false;
            }

            if (!forceTimidAI) {
                forceTimidAI = true;
                reEvalTimer = RE_EVAL_TIME;
                saveAIConfig(ship);
                ship.getShipAI().getConfig().personalityOverride = SWP_Util.getLessAggressivePersonality(CombatUtils.getFleetMember(ship), ship);
                ship.getShipAI().forceCircumstanceEvaluation();
                ship.getShipAI().cancelCurrentManeuver();
            }
        } else {
            if ((reEvalTimer <= 0f) && forceTimidAI) {
                forceTimidAI = false;
                reEvalTimer = RE_EVAL_TIME;
                flags.unsetFlag(AIFlags.BACK_OFF);
                flags.unsetFlag(AIFlags.DO_NOT_PURSUE);
                flags.unsetFlag(AIFlags.RUN_QUICKLY);
                restoreAIConfig(ship);
                ship.getShipAI().forceCircumstanceEvaluation();
            }
        }

        if (!forceTimidAI && !forceAggressiveAI && ((immediateTargetAtGoodRange && !immediateTargetTooFar) || smallCraftTarget)
                && (!flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) || flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_IN_GOOD_SPOT) || flags.hasFlag(AIFlags.PHASE_ATTACK_RUN_FROM_BEHIND_DIST_CRITICAL))) {
            if ((reEvalTimer <= 0f) && !forceStrafeAI) {
                forceStrafeAI = true;
                reEvalTimer = RE_EVAL_TIME;
                saveAIConfig(ship);
                ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                ship.getShipAI().forceCircumstanceEvaluation();
            }
        } else {
            if ((reEvalTimer <= 0f) && forceStrafeAI) {
                forceStrafeAI = false;
                reEvalTimer = RE_EVAL_TIME;
                restoreAIConfig(ship);
                ship.getShipAI().forceCircumstanceEvaluation();
            }
        }

        if (flags.hasFlag(AIFlags.PHASE_ATTACK_RUN) && (fluxLevel <= 0.1f)) {
            flags.unsetFlag(AIFlags.PHASE_ATTACK_RUN);
        }

        /* Aggressively come out of phase, if the situation is appropriate */
        boolean noDanger = !flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) && !flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER);
        if (ship.isPhased() && unphaseDecision && ((decisionLevel >= decisionTarget) || beingAggressive || noDanger)) {
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
        this.engine = engine;
    }

    private void saveAIConfig(ShipAPI ship) {
        if (ship.getShipAI().getConfig() != null) {
            savedConfig.alwaysStrafeOffensively = ship.getShipAI().getConfig().alwaysStrafeOffensively;
            savedConfig.turnToFaceWithUndamagedArmor = ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor;
            savedConfig.backingOffWhileNotVentingAllowed = ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed;
            savedConfig.personalityOverride = ship.getShipAI().getConfig().personalityOverride;
        }
    }

    private void restoreAIConfig(ShipAPI ship) {
        if (ship.getShipAI().getConfig() != null) {
            ship.getShipAI().getConfig().alwaysStrafeOffensively = savedConfig.alwaysStrafeOffensively;
            ship.getShipAI().getConfig().turnToFaceWithUndamagedArmor = savedConfig.turnToFaceWithUndamagedArmor;
            ship.getShipAI().getConfig().backingOffWhileNotVentingAllowed = savedConfig.backingOffWhileNotVentingAllowed;
            ship.getShipAI().getConfig().personalityOverride = savedConfig.personalityOverride;
        }
    }
}
