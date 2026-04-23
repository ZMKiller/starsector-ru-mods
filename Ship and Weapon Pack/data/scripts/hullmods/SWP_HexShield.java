package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_HexShield extends BaseHullMod {

    public static final float SHIELD_POP_TIME = 10f;
    public static final float SHIELD_UNFOLD_MULT = 10f;

    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if ((ship.getShield() == null) || (Global.getCombatEngine() == null)) {
            return;
        }

        String id1 = "SWP_HexShield1_" + ship.getId();
        String id2 = "SWP_HexShield2_" + ship.getId();

        if (!ship.isAlive()) {
            Global.getCombatEngine().getCustomData().remove(id1);
            Global.getCombatEngine().getCustomData().remove(id2);
            return;
        }

        if (Global.getCombatEngine().getCustomData().containsKey(id1)) {
            float timer = (float) Global.getCombatEngine().getCustomData().get(id1);
            timer -= amount;
            if (timer <= 0f) {
                Global.getCombatEngine().getCustomData().remove(id1);
                ship.setDefenseDisabled(false);
                Global.getSoundPlayer().playSound("shield_raise", 1.2f, 0.25f, ship.getShield().getLocation(), ZERO);
            } else {
                Global.getCombatEngine().getCustomData().put(id1, timer);
            }
        } else {
            float threshold = ship.getFluxTracker().getMaxFlux();
            threshold -= ship.getMutableStats().getFluxDissipation().getModifiedValue() / 15f;
            if (ship.getFluxTracker().isOverloaded() || (ship.getFluxTracker().getHardFlux() >= threshold)) {
                ship.getFluxTracker().setOverloadDuration(0f);
                ship.setDefenseDisabled(true);
                Global.getCombatEngine().getCustomData().put(id1, SHIELD_POP_TIME);
                Global.getSoundPlayer().playSound("shield_burnout", 1.2f, 0.5f, ship.getShield().getLocation(), ZERO);

                for (int x = 0; x < 4; x++) {
                    float angle = (float) Math.random() * 360f;
                    float distance = (float) (Math.random() * 2f + 1f) * ship.getShieldRadiusEvenIfNoShield();
                    Vector2f point1 = MathUtils.getPointOnCircumference(ship.getShield().getLocation(), distance, angle);
                    Vector2f point2 = MathUtils.getPointOnCircumference(ship.getShield().getLocation(), ship.getShieldRadiusEvenIfNoShield(), angle);
                    Global.getCombatEngine().spawnEmpArc(ship, point1, new SimpleEntity(point1), new SimpleEntity(point2),
                            DamageType.ENERGY, 0f, 0f, 1000f, null, 15f, ship.getShield().getInnerColor(), ship.getShield().getRingColor());
                }
            } else {
                boolean enemyNearby = false;

                /* Expensive check; do it 1/sec on average */
                if ((float) Math.random() <= amount) {
                    enemyNearby = !AIUtils.getNearbyEnemies(ship, 2000f).isEmpty();
                    Global.getCombatEngine().getCustomData().put(id2, enemyNearby);
                } else {
                    if (Global.getCombatEngine().getCustomData().containsKey(id2)) {
                        enemyNearby = (boolean) Global.getCombatEngine().getCustomData().get(id2);
                    } else {
                        Global.getCombatEngine().getCustomData().put(id2, enemyNearby);
                    }
                }

                if (enemyNearby) {
                    ship.getAIFlags().setFlag(AIFlags.KEEP_SHIELDS_ON, 0.1f);
                    if (ship.getShield().isOff()) {
                        ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                    } else {
                        ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                    }
                }
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldUnfoldRateMult().modifyMult(id, SHIELD_UNFOLD_MULT);
        stats.getOverloadTimeMod().modifyMult(id, 0.1f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) Math.round(SHIELD_POP_TIME);
        }
        return null;
    }

    @Override
    public Color getBorderColor() {
        return new Color(124, 230, 184);
    }

    @Override
    public Color getNameColor() {
        return new Color(51, 193, 94);
    }

    @Override
    public int getDisplaySortOrder() {
        return 0;
    }
}
