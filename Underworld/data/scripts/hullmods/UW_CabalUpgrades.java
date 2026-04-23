package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.util.UW_Util;
import java.awt.Color;
import org.lazywizard.lazylib.CollisionUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_CabalUpgrades extends BaseHullMod {

    private static final float ARMOR_PERCENT = 20f;
    private static final float ARMOR_PERCENT_PHASE = 10f;
    private static final float SHIELD_MULT = 0.9f;
    private static final float FLUX_MULT = 0.95f;
    private static final float CASUALTIES_PERCENT = 10f;
    private static final float REPAIR_PERCENT = 25f;
    private static final int EXTRA_S_MODS = 1;

    private static final float MAX_SPARKLE_CHANCE_PER_SECOND_PER_CELL = 0.75f;
    private static final Color SPARK_COLOR = new Color(190, 60, 255, 175);
    private static final float SPARK_DURATION = 0.2f;
    private static final float SPARK_RADIUS = 4f;

    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();

        float fluxLevel = ship.getFluxTracker().getFluxLevel();

        ArmorGridAPI armorGrid = ship.getArmorGrid();
        Color color = new Color(SPARK_COLOR.getRed(), SPARK_COLOR.getGreen(), SPARK_COLOR.getBlue(), UW_Util.clamp255((int) (SPARK_COLOR.getAlpha() * (1f - fluxLevel))));
        for (int x = 0; x < armorGrid.getGrid().length; x++) {
            for (int y = 0; y < armorGrid.getGrid()[0].length; y++) {
                float armorLevel = armorGrid.getArmorValue(x, y);
                if (armorLevel <= 0f) {
                    continue;
                }

                float chance = amount * (1f - fluxLevel) * MAX_SPARKLE_CHANCE_PER_SECOND_PER_CELL * armorLevel / armorGrid.getMaxArmorInCell();
                if (Math.random() >= chance) {
                    continue;
                }

                float cellSize = armorGrid.getCellSize();
                Vector2f cellLoc = getCellLocation(ship, x, y);
                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
                if (UW_Util.isOnscreen(cellLoc, SPARK_RADIUS) && CollisionUtils.isPointWithinBounds(cellLoc, ship)) {
                    engine.addHitParticle(cellLoc, ship.getVelocity(), 0.5f * SPARK_RADIUS * (float) Math.random() + SPARK_RADIUS, 1f, SPARK_DURATION,
                            UW_Util.colorJitter(color, 50f));
                }
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1 / SHIELD_MULT);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1 / SHIELD_MULT);

        if ((stats.getVariant() != null) && (stats.getVariant().getHullSpec().getDefenseType() == ShieldType.PHASE)) {
            stats.getArmorBonus().modifyPercent(id, ARMOR_PERCENT_PHASE);
        } else {
            stats.getArmorBonus().modifyPercent(id, ARMOR_PERCENT);
        }

        stats.getFluxCapacity().modifyMult(id, FLUX_MULT);
        stats.getFluxDissipation().modifyMult(id, FLUX_MULT);

        stats.getCrewLossMult().modifyPercent(id, CASUALTIES_PERCENT);

        stats.getCombatEngineRepairTimeMult().modifyPercent(id, REPAIR_PERCENT);
        stats.getCombatWeaponRepairTimeMult().modifyPercent(id, REPAIR_PERCENT);

        stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(id, EXTRA_S_MODS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + Math.round(ARMOR_PERCENT) + "%";
        }
        if (index == 1) {
            return "" + Math.round(ARMOR_PERCENT_PHASE) + "%";
        }
        if (index == 2) {
            return "" + Math.round((1f - SHIELD_MULT) * 100f) + "%";
        }
        if (index == 3) {
            return "" + Math.round((1f - FLUX_MULT) * 100f) + "%";
        }
        if (index == 4) {
            return "" + Math.round(CASUALTIES_PERCENT) + "%";
        }
        if (index == 5) {
            return "" + Math.round(REPAIR_PERCENT) + "%";
        }
        if (index == 6) {
            return "upgraded";
        }
        if (index == 7) {
            return "one additional built-in hullmod";
        }
        return null;
    }
}
