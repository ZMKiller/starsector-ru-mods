package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ElyonCoreBoss extends SWP_ElyonCore {

    private static final float BASE_POWER_LEVEL = 75f;
    private static final float SCALE_POWER_LEVEL = 75f;
    private static final float MINIMUM_SCALE_FOR_MEETING_THRESHOLD = 1.25f;
    private static final String STAT_ID = "SWP_ElyonCoreBoss";
    private static final Color FRINGE_COLOR = new Color(255, 0, 200);
    private static final Color FRINGE_COLOR2 = new Color(255, 0, 50);
    private static final Color CORE_COLOR = new Color(255, 0, 255);
    private static final Color CORE_COLOR2 = new Color(255, 0, 75);
    private static final Color GLOW_COLOR = new Color(128, 0, 255);
    private static final Color GLOW_COLOR2 = new Color(255, 0, 64);

    private final IntervalUtil interval = new IntervalUtil(1f, 1.5f);
    private final IntervalUtil interval2 = new IntervalUtil(1f, 2f);
    private final IntervalUtil interval3 = new IntervalUtil(0.1f, 0.2f);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        float maxCR = 1f;

        float objectiveAmount = amount / ship.getMutableStats().getTimeMult().getModifiedValue();
        if (ship.getCustomData().containsKey(STAT_ID + "_scale")) {
            float scale = (float) ship.getCustomData().get(STAT_ID + "_scale");
            float sqrtScale = (float) Math.sqrt(scale);
            float sqrt2Scale = (float) Math.sqrt(sqrtScale);
            interval2.advance(objectiveAmount * scale);
            if (interval2.intervalElapsed()) {
                float angle = MathUtils.getRandomNumberInRange(-180f, 180f);
                float distance = MathUtils.getRandomNumberInRange(75f * sqrt2Scale, 200f * sqrtScale);
                Vector2f point1 = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle);
                Vector2f point2 = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle + MathUtils.getRandomNumberInRange(10f, 50f));
                Color fringeColor = SWP_Util.interpolateColor255(FRINGE_COLOR, FRINGE_COLOR2, 1f - (ship.getCurrentCR() / Math.max(0.01f, maxCR)));
                Color coreColor = SWP_Util.interpolateColor255(CORE_COLOR, CORE_COLOR2, 1f - (ship.getCurrentCR() / Math.max(0.01f, maxCR)));
                EmpArcEntityAPI emp = engine.spawnEmpArcVisual(point1, null, point2, null, 10f * sqrtScale, fringeColor, coreColor);
                emp.setSingleFlickerMode();
            }
            interval3.advance(objectiveAmount);
            if (interval3.intervalElapsed()) {
                Color glowColor = SWP_Util.interpolateColor255(GLOW_COLOR, GLOW_COLOR2, 1f - (ship.getCurrentCR() / Math.max(0.01f, maxCR)));
                glowColor = SWP_Util.fadeColor255(glowColor, Math.min(1f, (float) Math.sqrt(scale / interval3.getIntervalDuration()) * 0.05f));
                engine.addSmoothParticle(ship.getLocation(), ship.getVelocity(), ship.getCollisionRadius() + Math.min(400f, (float) Math.sqrt(scale / interval3.getIntervalDuration()) * 400f),
                        0.1f, 0.1f + (0.4f * Math.min(1f, (float) Math.sqrt((scale / interval3.getIntervalDuration()) * 0.02f))), glowColor);
            }
        }

        interval.advance(objectiveAmount);
        if (!interval.intervalElapsed()) {
            return;
        }

        int otherSide = 0;
        if (ship.getOwner() == 0) {
            otherSide = 1;
        }
        float enemyPowerLevel = SWP_Util.calculatePowerLevel(engine.getShips(), engine, otherSide);

        if ((enemyPowerLevel > BASE_POWER_LEVEL) || (ship.getCurrentCR() < ship.getCRAtDeployment())) {
            float scale = Math.max(MINIMUM_SCALE_FOR_MEETING_THRESHOLD, (float) Math.pow(1f + Math.max(0.01f, enemyPowerLevel - BASE_POWER_LEVEL) / SCALE_POWER_LEVEL, 0.6));
            if (maxCR > 0.01f) {
                scale *= Math.max(1f, SWP_Util.lerp(1f, 3f, 1f - (ship.getCurrentCR() / maxCR)));
            }
            float sqrtScale = (float) Math.sqrt(scale);
            float sqrt2Scale = (float) Math.sqrt(sqrtScale);
            ship.getMutableStats().getMissileAmmoRegenMult().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getMissileRoFMult().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getMissileHealthBonus().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getMissileAccelerationBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getMissileMaxSpeedBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getMissileMaxTurnRateBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getMissileTurnAccelerationBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getEnergyRoFMult().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getEnergyProjectileSpeedMult().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getHitStrengthBonus().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getFluxCapacity().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getHighExplosiveDamageTakenMult().modifyMult(STAT_ID, 1f / sqrt2Scale);
            ship.getMutableStats().getKineticDamageTakenMult().modifyMult(STAT_ID, 1f / sqrt2Scale);
            ship.getMutableStats().getEnergyDamageTakenMult().modifyMult(STAT_ID, 1f / sqrt2Scale);
            ship.getMutableStats().getFragmentationDamageTakenMult().modifyMult(STAT_ID, 1f / sqrt2Scale);
            ship.getMutableStats().getEmpDamageTakenMult().modifyMult(STAT_ID, 1f / sqrt2Scale);
            ship.getMutableStats().getHullBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getArmorBonus().modifyMult(STAT_ID, sqrt2Scale);
            ship.getMutableStats().getWeaponHealthBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getEngineHealthBonus().modifyMult(STAT_ID, sqrtScale);
            ship.getMutableStats().getMaxSpeed().modifyMult(STAT_ID, 1f / sqrtScale);
            ship.getMutableStats().getAcceleration().modifyMult(STAT_ID, 1f / sqrtScale);
            ship.getMutableStats().getDeceleration().modifyMult(STAT_ID, 1f / sqrtScale);
            ship.getMutableStats().getTurnAcceleration().modifyMult(STAT_ID, 1f / sqrtScale);
            ship.getMutableStats().getMaxTurnRate().modifyMult(STAT_ID, 1f / sqrtScale);
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getId().contentEquals("amsrm")) {
                    weapon.getAmmoTracker().setReloadSize(Math.min(Math.round(sqrtScale), weapon.getMaxAmmo()));
                }
            }
            ship.setCustomData(STAT_ID + "_scale", scale);
        } else {
            ship.getMutableStats().getMissileAmmoRegenMult().unmodify(STAT_ID);
            ship.getMutableStats().getMissileRoFMult().unmodify(STAT_ID);
            ship.getMutableStats().getMissileHealthBonus().unmodify(STAT_ID);
            ship.getMutableStats().getMissileAccelerationBonus().unmodify(STAT_ID);
            ship.getMutableStats().getMissileMaxSpeedBonus().unmodify(STAT_ID);
            ship.getMutableStats().getMissileMaxTurnRateBonus().unmodify(STAT_ID);
            ship.getMutableStats().getMissileTurnAccelerationBonus().unmodify(STAT_ID);
            ship.getMutableStats().getEnergyRoFMult().unmodify(STAT_ID);
            ship.getMutableStats().getEnergyProjectileSpeedMult().unmodify(STAT_ID);
            ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(STAT_ID);
            ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(STAT_ID);
            ship.getMutableStats().getHitStrengthBonus().unmodify(STAT_ID);
            ship.getMutableStats().getFluxCapacity().unmodify(STAT_ID);
            ship.getMutableStats().getPhaseCloakUpkeepCostBonus().unmodify(STAT_ID);
            ship.getMutableStats().getHighExplosiveDamageTakenMult().unmodify(STAT_ID);
            ship.getMutableStats().getKineticDamageTakenMult().unmodify(STAT_ID);
            ship.getMutableStats().getEnergyDamageTakenMult().unmodify(STAT_ID);
            ship.getMutableStats().getFragmentationDamageTakenMult().unmodify(STAT_ID);
            ship.getMutableStats().getEmpDamageTakenMult().unmodify(STAT_ID);
            ship.getMutableStats().getHullBonus().unmodify(STAT_ID);
            ship.getMutableStats().getArmorBonus().unmodify(STAT_ID);
            ship.getMutableStats().getWeaponHealthBonus().unmodify(STAT_ID);
            ship.getMutableStats().getEngineHealthBonus().unmodify(STAT_ID);
            ship.getMutableStats().getMaxSpeed().unmodify(STAT_ID);
            ship.getMutableStats().getAcceleration().unmodify(STAT_ID);
            ship.getMutableStats().getDeceleration().unmodify(STAT_ID);
            ship.getMutableStats().getTurnAcceleration().unmodify(STAT_ID);
            ship.getMutableStats().getMaxTurnRate().unmodify(STAT_ID);
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getId().contentEquals("amsrm")) {
                    weapon.getAmmoTracker().setReloadSize(1);
                }
            }
            ship.removeCustomData(STAT_ID + "_scale");
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        stats.getCRPerDeploymentPercent().modifyMult(STAT_ID, 0.1f);
        stats.getSightRadiusMod().modifyMult(STAT_ID, 5f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        ship.addListener(new LessExplosionDamage());
    }

    public static class LessExplosionDamage implements DamageTakenModifier {

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (param instanceof DamagingProjectileAPI) {
                DamagingProjectileAPI proj = (DamagingProjectileAPI) param;
                if ((proj.getProjectileSpec() == null) && (proj.getDamageType() == DamageType.HIGH_EXPLOSIVE)
                        && proj.getSpawnType().equals(ProjectileSpawnType.OTHER)) {
                    damage.getModifier().modifyMult(STAT_ID + "_exp", 0.1f);
                    return STAT_ID + "_exp";
                }
            }

            return null;
        }
    }
}
