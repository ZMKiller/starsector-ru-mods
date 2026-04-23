package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import data.scripts.util.UW_Multi;
import data.scripts.util.UW_Util;
import java.awt.Color;
import java.util.List;
import org.dark.graphics.plugins.ShipDestructionEffects;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class UW_TemporalCurse extends BaseHullMod {

    public static final float TIME_MULT = 4f;
    public static final Color EMP_CORE_COLOR = new Color(255, 50, 50, 255);
    public static final Color EMP_CORE_COLOR2 = new Color(50, 150, 255, 255);
    public static final Color EMP_FRINGE_COLOR = new Color(255, 25, 25, 255);
    public static final Color EMP_FRINGE_COLOR2 = new Color(25, 125, 255, 255);

    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBreakProb().modifyMult(id, 999f);
        stats.getBreakProb().modifyFlat(id, 999f);
        stats.getBreakProb().modifyPercent(id, 999f);
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        MutableShipStatsAPI stats = fighter.getMutableStats();

        stats.getTimeMult().modifyMult(id, TIME_MULT);
        stats.getMaxSpeed().modifyMult(id, 1f / TIME_MULT);
        stats.getAcceleration().modifyMult(id, 1f / TIME_MULT);
        stats.getDeceleration().modifyMult(id, 1f / TIME_MULT);
        stats.getMaxTurnRate().modifyMult(id, 1f / TIME_MULT);
        stats.getTurnAcceleration().modifyMult(id, 1f / TIME_MULT);
        stats.getFluxDissipation().modifyMult(id, 1f / TIME_MULT);
        //stats.getBeamWeaponDamageMult().modifyMult(id, 1f / TIME_MULT);
        //stats.getBeamWeaponFluxCostMult().modifyMult(id, 1f / TIME_MULT);
        stats.getShieldUpkeepMult().modifyMult(id, 1f / TIME_MULT);
        stats.getShieldTurnRateMult().modifyMult(id, 1f / TIME_MULT);
        stats.getShieldUnfoldRateMult().modifyMult(id, 1f / TIME_MULT);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f / TIME_MULT);
        //stats.getWeaponTurnRateBonus().modifyMult(id, 1f / TIME_MULT);
        stats.getCombatEngineRepairTimeMult().modifyMult(id, TIME_MULT);
        //stats.getCombatWeaponRepairTimeMult().modifyMult(id, TIME_MULT);
        stats.getHullCombatRepairRatePercentPerSecond().modifyMult(id, 1f / TIME_MULT);
        //stats.getRecoilDecayMult().modifyMult(id, 1f / TIME_MULT);
        stats.getOverloadTimeMod().modifyMult(id, TIME_MULT);
        stats.getZeroFluxSpeedBoost().modifyMult(id, 1f / TIME_MULT);
        stats.getPhaseCloakCooldownBonus().modifyMult(id, TIME_MULT);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) {
            return;
        }

        MutableShipStatsAPI stats = ship.getMutableStats();
        String id = "uwTemporalCurse";

        if (ship.isAlive()) {
            stats.getTimeMult().modifyMult(id, TIME_MULT);
            stats.getMaxSpeed().modifyMult(id, 1f / TIME_MULT);
            stats.getAcceleration().modifyMult(id, 1f / TIME_MULT);
            stats.getDeceleration().modifyMult(id, 1f / TIME_MULT);
            stats.getMaxTurnRate().modifyMult(id, 1f / TIME_MULT);
            stats.getTurnAcceleration().modifyMult(id, 1f / TIME_MULT);
            stats.getFluxDissipation().modifyMult(id, 1f / TIME_MULT);
            //stats.getWeaponMalfunctionChance().modifyMult(id, 1f / TIME_MULT);
            stats.getEngineMalfunctionChance().modifyMult(id, 1f / TIME_MULT);
            stats.getCriticalMalfunctionChance().modifyMult(id, 1f / TIME_MULT);
            stats.getShieldMalfunctionChance().modifyMult(id, 1f / TIME_MULT);
            stats.getPeakCRDuration().modifyMult(id, TIME_MULT);
            stats.getCRLossPerSecondPercent().modifyMult(id, 1f / TIME_MULT);
            //stats.getBeamWeaponDamageMult().modifyMult(id, 1f / TIME_MULT);
            //stats.getBeamWeaponFluxCostMult().modifyMult(id, 1f / TIME_MULT);
            stats.getShieldUpkeepMult().modifyMult(id, 1f / TIME_MULT);
            stats.getShieldTurnRateMult().modifyMult(id, 1f / TIME_MULT);
            stats.getShieldUnfoldRateMult().modifyMult(id, 1f / TIME_MULT);
            stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f / TIME_MULT);
            //stats.getWeaponTurnRateBonus().modifyMult(id, 1f / TIME_MULT);
            stats.getCombatEngineRepairTimeMult().modifyMult(id, TIME_MULT);
            //stats.getCombatWeaponRepairTimeMult().modifyMult(id, TIME_MULT);
            stats.getHullCombatRepairRatePercentPerSecond().modifyMult(id, 1f / TIME_MULT);
            //stats.getRecoilDecayMult().modifyMult(id, 1f / TIME_MULT);
            stats.getOverloadTimeMod().modifyMult(id, TIME_MULT);
            stats.getZeroFluxSpeedBoost().modifyMult(id, 1f / TIME_MULT);
            stats.getFighterRefitTimeMult().modifyMult(id, TIME_MULT);
            stats.getPhaseCloakCooldownBonus().modifyMult(id, TIME_MULT);

            if (ship.getFluxTracker().isVenting()) {
                if (Math.random() < (2f * amount)) {
                    float scale = ship.getMutableStats().getVentRateMult().getModifiedValue() * (2f / 3f)
                            * (ship.getMutableStats().getFluxDissipation().getModifiedValue() / 50f);
                    if (Math.random() < (0.3 * Math.sqrt(scale))) {
                        float range = (float) Math.sqrt(ship.getFluxTracker().getCurrFlux()) * (float) Math.sqrt(scale) * 4f;
                        List<ShipAPI> targets = AIUtils.getNearbyEnemies(ship, range);

                        float shipRadius = UW_Util.effectiveRadius(ship);
                        Vector2f empSource = null;
                        int max = 30;
                        while (empSource == null && max > 0) {
                            empSource = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                            if (!CollisionUtils.isPointWithinBounds(empSource, ship)) {
                                empSource = null;
                            }
                            max--;
                        }
                        if (empSource == null) {
                            empSource = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                        }

                        if (!targets.isEmpty()) {
                            ShipAPI target = targets.get(MathUtils.getRandom().nextInt(targets.size()));
                            if (UW_Multi.isWithinEmpRange(ship.getLocation(), range * 1.25f, target)) {
                                Global.getCombatEngine().spawnEmpArc(ship, empSource,
                                        ship, target,
                                        DamageType.ENERGY,
                                        50f * scale, 50f * scale, range * 1.25f,
                                        "uw_temporalcurse_emp", 4f * scale,
                                        EMP_FRINGE_COLOR, EMP_CORE_COLOR);
                            }
                        } else {
                            AnchoredEntity entity = new AnchoredEntity(ship, empSource);
                            Global.getCombatEngine().spawnEmpArc(ship, MathUtils.getRandomPointOnCircumference(ship.getLocation(), range),
                                    entity, entity,
                                    DamageType.ENERGY, 0f, 0f, range * 2f, null, 4f * scale,
                                    EMP_FRINGE_COLOR, EMP_CORE_COLOR);
                        }
                    }
                }
            }
            return;
        }

        stats.getTimeMult().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        //stats.getWeaponMalfunctionChance().unmodify(id);
        stats.getEngineMalfunctionChance().unmodify(id);
        stats.getCriticalMalfunctionChance().unmodify(id);
        stats.getShieldMalfunctionChance().unmodify(id);
        stats.getPeakCRDuration().unmodify(id);
        stats.getCRLossPerSecondPercent().unmodify(id);
        //stats.getBeamWeaponDamageMult().unmodify(id);
        //stats.getBeamWeaponFluxCostMult().unmodify(id);
        stats.getShieldUpkeepMult().unmodify(id);
        stats.getShieldTurnRateMult().unmodify(id);
        stats.getShieldUnfoldRateMult().unmodify(id);
        stats.getPhaseCloakUpkeepCostBonus().unmodify(id);
        //stats.getWeaponTurnRateBonus().unmodify(id);
        stats.getCombatEngineRepairTimeMult().unmodify(id);
        //stats.getCombatWeaponRepairTimeMult().unmodify(id);
        stats.getHullCombatRepairRatePercentPerSecond().unmodify(id);
        //stats.getRecoilDecayMult().unmodify(id);
        stats.getOverloadTimeMod().unmodify(id);
        stats.getZeroFluxSpeedBoost().unmodify(id);
        stats.getFighterRefitTimeMult().unmodify(id);
        stats.getPhaseCloakCooldownBonus().unmodify(id);

        String baseHullId = ship.getHullSpec().getBaseHullId();
        float shipRadius = UW_Util.effectiveRadius(ship);
        float damage = ship.getCollisionRadius();
        float thickness = (float) Math.sqrt(ship.getCollisionRadius());
        float volume = ship.getCollisionRadius() / 300f;

        if (ship.isPiece()) {
            ShipDestructionEffects.suppressEffects(ship, true, false);
        }

        boolean spawnedArc = false;
        List<ShipAPI> otherShips = CombatUtils.getShipsWithinRange(ship.getLocation(), shipRadius * 4f);
        for (ShipAPI otherShip : otherShips) {
            if ((ship == otherShip) || !otherShip.isHulk() || !otherShip.getHullSpec().getBaseHullId().contentEquals(baseHullId)) {
                continue;
            }

            float otherShipRadius = UW_Util.effectiveRadius(otherShip);

            float randScale = 1.25f - (MathUtils.getDistance(ship.getLocation(), otherShip.getLocation()) / (shipRadius * 4f));
            randScale = Math.max(randScale, 0.25f);
            randScale *= Math.min((float) Math.sqrt(otherShipRadius / shipRadius), 1f);
            if (Math.random() > (1.5f * amount * randScale)) {
                continue;
            }

            Vector2f empSource = null;
            int max = 30;
            while (empSource == null && max > 0) {
                empSource = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                if (!CollisionUtils.isPointWithinBounds(empSource, ship)) {
                    empSource = null;
                }
                max--;
            }
            if (empSource == null) {
                empSource = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
            }

            Vector2f empTarget = null;
            max = 30;
            while (empTarget == null && max > 0) {
                empTarget = MathUtils.getRandomPointInCircle(otherShip.getLocation(), otherShipRadius);
                if (!CollisionUtils.isPointWithinBounds(empTarget, otherShip)) {
                    empTarget = null;
                }
                max--;
            }
            if (empTarget == null) {
                empTarget = MathUtils.getRandomPointInCircle(otherShip.getLocation(), otherShipRadius);
            }

            Vector2f sourceForceDir = VectorUtils.getDirectionalVector(empSource, empTarget);
            float sourceForce = otherShip.getMass() / 100f;
            Vector2f targetForceDir = VectorUtils.getDirectionalVector(empTarget, empSource);
            float targetForce = ship.getMass() / 100f;

            AnchoredEntity entity = new AnchoredEntity(otherShip, empTarget);
            Global.getCombatEngine().spawnEmpArcPierceShields(ship, empSource, ship, entity, DamageType.ENERGY, 0, 0, 1000f + (shipRadius * 4f),
                    null, thickness, EMP_FRINGE_COLOR, EMP_CORE_COLOR);
            Global.getCombatEngine().applyDamage(otherShip, empTarget, damage, DamageType.ENERGY, damage, true, false, ship, false);
            UW_Util.applyForce(ship, sourceForceDir, sourceForce);
            UW_Util.applyForce(otherShip, targetForceDir, targetForce);
            spawnedArc = true;
        }
        for (ShipAPI otherShip : otherShips) {
            if ((ship == otherShip) || !otherShip.isAlive() || otherShip.getHullSpec().getBaseHullId().contentEquals(baseHullId)) {
                continue;
            }

            float otherShipRadius = UW_Util.effectiveRadius(otherShip);

            float randScale = 1.25f - (MathUtils.getDistance(ship.getLocation(), otherShip.getLocation()) / (shipRadius * 4f));
            randScale = Math.max(randScale, 0.25f);
            randScale *= Math.min((float) Math.sqrt(otherShipRadius / shipRadius), 1f);
            if (Math.random() > (1.5f * amount * randScale)) {
                continue;
            }

            Vector2f empSource = null;
            int max = 30;
            while (empSource == null && max > 0) {
                empSource = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                if (!CollisionUtils.isPointWithinBounds(empSource, ship)) {
                    empSource = null;
                }
                max--;
            }
            if (empSource == null) {
                empSource = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
            }

            Vector2f sourceForceDir = VectorUtils.getDirectionalVector(otherShip.getLocation(), empSource);
            float sourceForce = otherShip.getMass() / 200f;
            Vector2f targetForceDir = VectorUtils.getDirectionalVector(empSource, otherShip.getLocation());
            float targetForce = ship.getMass() / 200f;

            Global.getCombatEngine().spawnEmpArc(ship, empSource, ship, otherShip, DamageType.ENERGY, 0, damage, 1000f + (shipRadius * 4f),
                    null, thickness, EMP_FRINGE_COLOR2, EMP_CORE_COLOR2);
            UW_Util.applyForce(ship, sourceForceDir, sourceForce);
            UW_Util.applyForce(otherShip, targetForceDir, targetForce);
            spawnedArc = true;
        }

        if (spawnedArc && (Math.random() <= 0.33)) {
            Global.getSoundPlayer().playSound("uw_temporalcurse_emp", 1f, volume, ship.getLocation(), ZERO);
        }
    }
}
