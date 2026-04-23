package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import data.scripts.util.UW_Util;
import java.awt.Color;
import java.util.EnumSet;
import org.lwjgl.util.vector.Vector2f;

public class UW_IncubusDriveStats extends BaseShipSystemScript {

    private static final float MIN_FLUX_LEVEL = 0.25f;

    private static final Color COLOR_ENGINE = new Color(255, 100, 200, 255);
    private static final Color COLOR_JITTER_NONE = new Color(255, 100, 200, 0);
    private static final Color COLOR_JITTER_FULL = new Color(255, 100, 200, 255);
    private static final Color COLOR_NOTHING = new Color(0, 0, 0, 0);
    private static final Vector2f ZERO = new Vector2f();

    private SoundAPI sound = null;
    private boolean started = false;
    private float tempFluxLevel = 0f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float flatscale = 1.0f;
        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (ship != null) {
            float fluxLevel = ship.getFluxTracker().getFluxLevel();
            tempFluxLevel = fluxLevel;

            if (!started) {
                started = true;
                float pitch;
                switch (ship.getHullSize()) {
                    case FIGHTER:
                        pitch = 1.3f;
                        break;
                    case FRIGATE:
                        pitch = 1.25f;
                        break;
                    case DEFAULT:
                    case DESTROYER:
                    default:
                        pitch = 1.15f;
                        break;
                    case CRUISER:
                        pitch = 1f;
                        break;
                    case CAPITAL_SHIP:
                        pitch = 0.9f;
                        break;
                }
                sound = Global.getSoundPlayer().playSound("uw_incubusdrive_activate", pitch, 1f, ship.getLocation(), ZERO);

                if (fluxLevel >= MIN_FLUX_LEVEL) {
                    Global.getSoundPlayer().playSound("uw_incubusdrive_super_activate", pitch, fluxLevel, ship.getLocation(), ZERO);
                }
            }
            if (sound != null) {
                sound.setLocation(ship.getLocation().x, ship.getLocation().y);
            }
            ship.getEngineController().fadeToOtherColor(this, COLOR_ENGINE, COLOR_NOTHING, effectLevel, 0.67f);
            ship.getEngineController().extendFlame(this, (1f + (fluxLevel * 0.5f)) * effectLevel, (1f + fluxLevel) * 0.5f * effectLevel, (1f + fluxLevel) * 0.5f * effectLevel);

            if (fluxLevel > MIN_FLUX_LEVEL) {
                if (state != ShipSystemStatsScript.State.OUT) {
                    Global.getSoundPlayer().playLoop("uw_incubusdrive_super_loop", ship, 1f, fluxLevel * effectLevel, ship.getLocation(), ZERO);
                }
                Color jitterColor = UW_Util.colorBlend(COLOR_JITTER_NONE, COLOR_JITTER_FULL, fluxLevel * effectLevel);
                ship.setJitterUnder(this, jitterColor, fluxLevel * effectLevel, 6, 0f, 10f + 15f * fluxLevel * effectLevel);
                ship.setWeaponGlow(fluxLevel * effectLevel, jitterColor, EnumSet.of(WeaponType.ENERGY));

                stats.getFluxDissipation().modifyMult(id, 1f + (fluxLevel * effectLevel * 2f));
                stats.getEnergyRoFMult().modifyMult(id, 1f + (fluxLevel * effectLevel * 2f));
                stats.getBeamWeaponDamageMult().modifyMult(id, 1f + (fluxLevel * effectLevel));
            } else {
                stats.getFluxDissipation().unmodify(id);
                stats.getEnergyRoFMult().unmodify(id);
                stats.getBeamWeaponDamageMult().unmodify(id);
                ship.setJitterUnder(this, COLOR_NOTHING, 0f, 0, 0f, 0f);
                ship.setWeaponGlow(0f, COLOR_NOTHING, EnumSet.of(WeaponType.ENERGY));
            }
        }

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().modifyFlat(id, 0f);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 200f);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 100f * flatscale * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 150f * flatscale * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 50f * flatscale * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 25f * flatscale * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
        }

//        if (ship != null) {
//            String key = ship.getId() + "_" + id;
//            Object test = Global.getCombatEngine().getCustomData().get(key);
//            if (state == State.IN) {
//                if (test == null && effectLevel > 0.2f) {
//                    Global.getCombatEngine().getCustomData().put(key, new Object());
//                    ship.getEngineController().getExtendLengthFraction().advance(1f);
//                    for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
//                        if (engine.isSystemActivated()) {
//                            ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
//                        }
//                    }
//                }
//            } else {
//                Global.getCombatEngine().getCustomData().remove(key);
//            }
//        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        switch (index) {
            case 0 -> {
                return new StatusData("improved maneuverability", false);
            }
            case 1 -> {
                return new StatusData("increased top speed", false);
            }
            case 2 -> {
                if (tempFluxLevel >= MIN_FLUX_LEVEL) {
                    return new StatusData("flux overcharge boost: " + Math.round(tempFluxLevel * 100f) + "%", false);
                }
            }
            default -> {
            }
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        started = false;
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getBeamWeaponDamageMult().unmodify(id);
        sound = null;

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship != null) {
            ship.setJitterUnder(this, COLOR_NOTHING, 0f, 0, 0f, 0f);
            ship.setWeaponGlow(0f, COLOR_NOTHING, EnumSet.of(WeaponType.ENERGY));
        }
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
        if (ship.getHullSize() == HullSize.CAPITAL_SHIP) {
            return 0.075f;
        }
        return -1;
    }
}
