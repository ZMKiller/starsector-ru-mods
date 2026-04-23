package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class SWP_TimeFieldStats extends BaseShipSystemScript {

    public static final Color JITTER_COLOR = new Color(70, 190, 255, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(70, 190, 255, 155);
    public static final float MAX_RANGE = 1500f;
    public static final float MAX_TIME_MULT = 3f;

    private static Set<ShipAPI> getFighters(ShipAPI carrier) {
        Set<ShipAPI> result = new HashSet<>(20);

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        boolean player;
        String statId;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            statId = id + "_" + ship.getId();
        } else {
            return;
        }

        if (state == State.IDLE) {
            unapply(stats, id);
            return;
        }

        float jitterLevel = effectLevel;
        float jitterRangeBonus = 0;
        float maxRangeBonus = 10f;
        if (null != state) {
            switch (state) {
                case IN:
                    jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
                    if (jitterLevel > 1) {
                        jitterLevel = 1f;
                    }
                    jitterRangeBonus = jitterLevel * maxRangeBonus;
                    break;
                case ACTIVE:
                    jitterLevel = 1f;
                    jitterRangeBonus = maxRangeBonus;
                    break;
                case OUT:
                    jitterRangeBonus = jitterLevel * maxRangeBonus;
                    break;
                default:
                    break;
            }
        }
        jitterLevel = (float) Math.sqrt(jitterLevel);
        float effectLevelSquared = effectLevel * effectLevel;

        ship.setJitter(this, JITTER_COLOR, jitterLevel * 0.75f, 3,
                0f, jitterRangeBonus * 1.5f);
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel * 0.5f, 25,
                0f + jitterRangeBonus, 10f + jitterRangeBonus * 1.5f);

        float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevelSquared;
        stats.getTimeMult().modifyMult(statId, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(statId, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(statId);
        }

        ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0, 0, 0, 0), effectLevelSquared, 0.5f);
        ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);

        Set<ShipAPI> carriedFighters = getFighters(ship);
        for (ShipAPI fighter : carriedFighters) {
//            float atten = 1f - (MathUtils.getDistance(fighter, ship) / MAX_RANGE);
//            if (atten <= 0f) {
//                fighter.getMutableStats().getTimeMult().unmodify(statId);
//                continue;
//            }
            float atten = 1f;

            if (!fighter.isAlive()) {
                fighter.getMutableStats().getTimeMult().unmodify(statId);
                continue;
            }

            float fighterJitterLevel = jitterLevel * atten;
            float fighterJitterRangeBonus = jitterRangeBonus * atten;
            float fighterEffectLevelSquared = effectLevelSquared * atten;

            fighter.setJitter(this, JITTER_COLOR, fighterJitterLevel, 2,
                    0f, 0f + fighterJitterRangeBonus);
            fighter.setJitterUnder(this, JITTER_UNDER_COLOR, fighterJitterLevel, 15,
                    0f, 5f + fighterJitterRangeBonus);

            float fighterTimeMult = 1f + (MAX_TIME_MULT - 1f) * fighterEffectLevelSquared;
            fighter.getMutableStats().getTimeMult().modifyMult(statId, fighterTimeMult);

            fighter.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0, 0, 0, 0),
                    fighterEffectLevelSquared, 0.5f);
            fighter.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
        }

        /* Unapply for fighters that are not from the carrier - just in case */
        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (!fighter.isFighter()) {
                continue;
            }
            if (carriedFighters.contains(fighter)) {
                continue;
            }
            fighter.getMutableStats().getTimeMult().unmodify(statId);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (state != State.IDLE) {
            if (index == 0) {
                return new StatusData("time flow altered", false);
            } else if (index == 1) {
                return new StatusData("fighter time flow altered", false);
            }
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        String statId;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            statId = id + "_" + ship.getId();
        } else {
            return;
        }

        Global.getCombatEngine().getTimeMult().unmodify(statId);
        stats.getTimeMult().unmodify(statId);

        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (!fighter.isFighter()) {
                continue;
            }
            fighter.getMutableStats().getTimeMult().unmodify(statId);
        }
    }
}
