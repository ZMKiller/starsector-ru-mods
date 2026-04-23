package org.xhan.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;

/*
code by Tomatopaste
*/

public class XHAN_TemporalLock extends BaseShipSystemScript {
    private static final Color JITTER_UNDER_COLOR = new Color(245, 15, 255, 197);
    private static final Color JITTER_COLOR = new Color(230, 170, 255, 136);
    private static final float JITTER_MAX_RANGE_BONUS = 150f;
    private static final float SYSTEM_EFFECT_RANGE = 800f;

    private static final Color TIME_LOCK_TEXT_COLOUR = new Color(226, 119, 255, 255);
    private static final float TARGET_TIME_MULT = 0.5f; //time mult compared to real time
    private static final float LOCK_DURATION = 7f; //effect lasts for this amount in seconds
    private static final Color TARGET_JITTER_UNDER_COLOUR = new Color(237, 0, 255, 255);
    private static final float TARGET_JITTER_UNDER_RANGE = 35f;
    private static final Color TARGET_JITTER_COLOUR = new Color(235, 109, 255, 255);
    private static final float TARGET_JITTER_RANGE = 250f;
    private static final Color TARGET_WEAPON_DISABLED_GLOW_COLOUR = new Color(232, 40, 255, 255);
    private static final Color TARGET_EXPLOSION_EFFECT_COLOUR = new Color(213, 130, 255, 164);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        //other stat changes go here

        float jitterLevel = effectLevel;
        if (state == State.OUT) {
            jitterLevel *= jitterLevel;
        }
        float jitterRangeBonus = jitterLevel * JITTER_MAX_RANGE_BONUS;

        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 21, 0f, 3f + jitterRangeBonus);
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

        String targetKey = ship.getId() + "_XHAN_TemporalLock_target";
        Object foundTarget = Global.getCombatEngine().getCustomData().get(targetKey);
        if (state == State.IN) {
            if (foundTarget == null) {
                ShipAPI target = findTarget(ship);
                Global.getCombatEngine().getCustomData().put(targetKey, target);
            }
        } else if (effectLevel >= 1) {
            if (foundTarget instanceof ShipAPI target) {
                if (target.getFluxTracker().isOverloadedOrVenting()) target = ship;
                applyEffectToTarget(ship, target);
            }
        } else if (state == State.OUT && foundTarget != null) {
            Global.getCombatEngine().getCustomData().remove(targetKey);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        //undo other stat changes
    }

    private ShipAPI findTarget(ShipAPI ship) {
        float range = SYSTEM_EFFECT_RANGE;
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null;
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FIGHTER, range, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI) {
                    target = (ShipAPI) test;
                    float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                    float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                    if (dist > range + radSum) target = null;
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FIGHTER, range, true);
            }
        }
        if (target == null || target.getFluxTracker().isOverloadedOrVenting()) target = ship;

        return target;
    }

    private void applyEffectToTarget(final ShipAPI ship, final ShipAPI target) {
        if (target.getFluxTracker().isOverloadedOrVenting()) {
            return;
        }
        if (target == ship) return;

        if (target.getFluxTracker().showFloaty() || ship == Global.getCombatEngine().getPlayerShip() || target == Global.getCombatEngine().getPlayerShip()) {
            target.getFluxTracker().playOverloadSound();
            target.getFluxTracker().showOverloadFloatyIfNeeded("Time Lock!", TIME_LOCK_TEXT_COLOUR, 6f, true);
        }

        for (int i = 0; i < 360; i++) {
            Vector2f loc = MathUtils.getPointOnCircumference(target.getLocation(), target.getShieldRadiusEvenIfNoShield(), i);
            Vector2f vel = Vector2f.sub(loc, target.getLocation(), new Vector2f());
            vel.normalise();
            vel.scale(1200f);

            Global.getCombatEngine().addSmoothParticle(loc, vel, 70f, 1f, 0.3f, TARGET_EXPLOSION_EFFECT_COLOUR);
        }
        //PSE_CombatEffectsPlugin.spawnParticleRing(new PSE_CombatEffectsPlugin.PSE_ParticleRing(0.3f, target.getLocation(), target.getShieldRadiusEvenIfNoShield(), ""));

        //Color contrail = target.getEngineController().getShipEngines().get(0).getContrailColor();
        Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
            private final IntervalUtil tracker = new IntervalUtil(LOCK_DURATION - (LOCK_DURATION * 0.1f), LOCK_DURATION - (LOCK_DURATION * 0.1f));
            private final IntervalUtil exitTracker = new IntervalUtil(LOCK_DURATION * 0.1f, LOCK_DURATION * 0.1f);
            private boolean isExiting = false;

            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                if (isExiting) {
                    exitTracker.advance(amount);
                } else {
                    tracker.advance(amount);

                    if (target.getShield() != null && target.getShield().isOn()) {
                        target.getShield().toggleOff();
                    }

                    target.getVelocity().set(new Vector2f()); //sets velocity to zero
                    target.setAngularVelocity(0f);
                    target.setWeaponGlow(1f, TARGET_WEAPON_DISABLED_GLOW_COLOUR, EnumSet.of(WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY));
                }

                target.getMutableStats().getTimeMult().modifyMult("XHAN_TemporalLock", TARGET_TIME_MULT);

                target.setJitterShields(false);
                target.setJitterUnder(this, TARGET_JITTER_UNDER_COLOUR, 0.4f, 5, TARGET_JITTER_UNDER_RANGE);
                target.setJitter(this, TARGET_JITTER_COLOUR, 0.3f, 5, TARGET_JITTER_RANGE);

                target.setCollisionClass(CollisionClass.NONE);

                target.getMutableStats().getMissileWeaponRangeBonus().modifyMult("XHAN_TemporalLock", -1f);
                target.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("XHAN_TemporalLock", -1f);
                target.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("XHAN_TemporalLock", -1f);

                target.getMutableStats().getEffectiveArmorBonus().modifyMult("XHAN_TemporalLock", 3f);

                target.getEngineController().fadeToOtherColor(this, TARGET_WEAPON_DISABLED_GLOW_COLOUR, TARGET_WEAPON_DISABLED_GLOW_COLOUR, 10f, 100f);

                //Global.getCombatEngine().maintainStatusForPlayerShip("lol", "graphics/icons/hullsys/drone_pd_high.png", "debug", target.getMutableStats().getWeaponRangeThreshold().modified + "", true);

                if (tracker.intervalElapsed()) {
                    isExiting = true;
                    target.setCollisionClass(CollisionClass.SHIP);
                    target.getMutableStats().getTimeMult().unmodify("XHAN_TemporalLock");
                    target.getMutableStats().getMissileWeaponRangeBonus().unmodify("XHAN_TemporalLock");
                    target.getMutableStats().getEnergyWeaponRangeBonus().unmodify("XHAN_TemporalLock");
                    target.getMutableStats().getBallisticWeaponRangeBonus().unmodify("XHAN_TemporalLock");
                }
                if (exitTracker.intervalElapsed()) {
                    target.setWeaponGlow(0f, new Color(0, 0, 0, 0), EnumSet.of(WeaponAPI.WeaponType.MISSILE, WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY));
                    target.getMutableStats().getEffectiveArmorBonus().unmodify("XHAN_TemporalLock");
                    target.setJitterShields(true);
                    Global.getCombatEngine().removePlugin(this);
                }
            }
        });
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != ship) {
            return "READY";
        }
        if (ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != ship;
    }
}
