package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

// Edited, various changes, those that i remember:
// reduced turn rate bonus
// altered speed variables (600 speed cap is still a thing so these changes are more academic than practical i think)
// made it so the boost is less "strafe focused" and gives more forwards/reverse boost distance
		// this can result in ramming at short ranges, but that's funny so i'll let it slide :)
// added extra sfx because i felt like it :)
// minor tweaks to the vfx
// added a "drift" mechanic so the ship will "drift" at up to +200 speed after boosting during State.OUT

public class ASF_QuickBoostStats extends BaseShipSystemScript {
    public static final float MAX_TURN_BONUS = 10f;
    public static final float TURN_ACCEL_BONUS = 20f;
    public static final float INSTANT_BOOST_FLAT = 300f;
    public static final float INSTANT_BOOST_MULT = 4f;
    public static final Map<String, Integer> CHARGECOUNT = new HashMap<>();
    private static final Color ENGINE_COLOR = new Color(175, 100, 10, 155); // "outer color"  10, 100, 175, 155
    private static final Color BOOST_COLOR = new Color(225, 125, 125, 160); // "inner color"  125, 125, 225, 160
    private static final Vector2f ZERO = new Vector2f();
    private static final String failstate = "ENGINES OFFLINE, CANNOT BOOST";
    private final Object ENGINEKEY = new Object();
    private final Map<Integer, Float> engState = new HashMap<>();
    private boolean started = false;
    private boolean ended = false;
    private float boostScale = 0.85f;
    private float boostVisualDir = 0f;
    private boolean boostForward = false;
    private static final float DRIFT_SPEED = 200f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {return;}
        float shipRadius = ship.getCollisionRadius();
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) {amount = 0f;}
        
        ship.getEngineController().extendFlame(ENGINEKEY, 0f, 1f * effectLevel, 3f * effectLevel);

        if (!ended) {
            /* Unweighted direction calculation for visual purposes - 0 degrees is forward */
            Vector2f direction = new Vector2f();
            if (ship.getEngineController().isAccelerating()) {
                direction.y += 1f;
            } else if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
                direction.y -= 1f;
            }
            if (ship.getEngineController().isStrafingLeft()) {
                direction.x -= 1f;
            } else if (ship.getEngineController().isStrafingRight()) {
                direction.x += 1f;
            }
            if (direction.length() <= 0f) {
                direction.y = 1f;
            }
            boostVisualDir = MathUtils.clampAngle(VectorUtils.getFacing(direction) - 90f);
        }

        if (state == State.IN) {
            List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
            for (int i = 0; i < engList.size(); i++) {
                ShipEngineAPI eng = engList.get(i);
                if (eng.isSystemActivated()) {
                    float targetLevel = getSystemEngineScale(eng, boostVisualDir) * 0.4f;
                    Float currLevel = engState.get(i);
                    if (currLevel == null) {
                        currLevel = 0f;
                    }
                    if (currLevel > targetLevel) {
                        currLevel = Math.max(targetLevel, currLevel - (amount / 0.1f));
                    } else {
                        currLevel = Math.min(targetLevel, currLevel + (amount / 0.1f));
                    }
                    engState.put(i, currLevel);
                    ship.getEngineController().setFlameLevel(eng.getEngineSlot(), currLevel);
                }
            }
        }

        if (state == State.OUT) {
            /* Black magic to counteract the effects of maneuvering penalties/bonuses on the effectiveness of this system */
            float decelMult = Math.max(0.5f, Math.min(2f, stats.getDeceleration().getModifiedValue() / stats.getDeceleration().getBaseValue()));
            float adjFalloffPerSec = 0.25f * (float) Math.pow(decelMult, 0.5);
            float maxDecelPenalty = 1f / decelMult;

            stats.getMaxTurnRate().unmodify(id);
            stats.getDeceleration().modifyMult(id, (1f - effectLevel) * 1f * maxDecelPenalty);
            stats.getMaxTurnRate().modifyFlat(id, MAX_TURN_BONUS * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, TURN_ACCEL_BONUS * effectLevel);
            
            if (boostForward) {
                ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
                ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
            } else {
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
            }

            // so this was fucking with my "Drift" add-on, i made it only run if speed is over the ships max speed
            // because this slows the ship down, and as such was compromising THE SOUL of my "Drift" mechanic 
            if (amount > 0f) {
            	if (ship.getVelocity().lengthSquared() > (ship.getMaxSpeed() * ship.getMaxSpeed())) {
                    ship.getVelocity().scale((float) Math.pow(adjFalloffPerSec, amount));
            	}
            }

            List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
            for (int i = 0; i < engList.size(); i++) {
                ShipEngineAPI eng = engList.get(i);
                if (eng.isSystemActivated()) {
                    float targetLevel = getSystemEngineScale(eng, boostVisualDir) * effectLevel;
                    if (targetLevel >= (1f - 0.15f/0.9f)) {
                        targetLevel = 1f;
                    } else {
                        targetLevel = targetLevel / (1f - 0.15f/0.9f);
                    }
                    engState.put(i, targetLevel);
                    ship.getEngineController().setFlameLevel(eng.getEngineSlot(), targetLevel);
                }
            }
            
            // my funny "drift" add-on
            stats.getMaxSpeed().modifyPercent(id, effectLevel * DRIFT_SPEED);
            // this should make the boost "sustain" somewhat during the State.OUT phase
            
        } else if (state == State.ACTIVE) {
            if (!started) {
                Global.getSoundPlayer().playSound("explosion_fleet_member", 1f, 1.0f, ship.getLocation(), ZERO);
                // originally in State.IN with "system_phase_skimmer"
                // more sound!
                started = true;
            }
            
            stats.getMaxTurnRate().modifyFlat(id, MAX_TURN_BONUS);
            stats.getTurnAcceleration().modifyFlat(id, TURN_ACCEL_BONUS);
            ship.getEngineController().getExtendLengthFraction().advance(amount * 2f);
            ship.getEngineController().getExtendWidthFraction().advance(amount * 2f);
            ship.getEngineController().getExtendGlowFraction().advance(amount * 2f);
            List<ShipEngineAPI> engList = ship.getEngineController().getShipEngines();
            for (int i = 0; i < engList.size(); i++) {
                ShipEngineAPI eng = engList.get(i);
                if (eng.isSystemActivated()) {
                    float targetLevel = getSystemEngineScale(eng, boostVisualDir);
                    Float currLevel = engState.get(i);
                    if (currLevel == null) {
                        currLevel = 0f;
                    }
                    if (currLevel > targetLevel) {
                        currLevel = Math.max(targetLevel, currLevel - (amount / 0.1f));
                    } else {
                        currLevel = Math.min(targetLevel, currLevel + (amount / 0.1f));
                    }
                    engState.put(i, currLevel);
                    ship.getEngineController().setFlameLevel(eng.getEngineSlot(), currLevel);
                }
            }
        }

        if (state == State.OUT) {
            if (!ended) {
                Vector2f direction = new Vector2f();
                boostForward = false;
                boostScale = 0.85f;
                if (ship.getEngineController().isAccelerating()) {
                    direction.y += 0.9f;
                    boostScale += 0.05f;
                    boostForward = true;
                } else if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
                    direction.y -= 0.7f; // 0.2 weaker when in reverse
                    boostScale -= 0.15f;
                }
                if (ship.getEngineController().isStrafingLeft()) {
                    direction.x -= 1f;
                    boostScale += 0.3f;
                    boostForward = false;
                } else if (ship.getEngineController().isStrafingRight()) {
                    direction.x += 1f;
                    boostScale += 0.3f;
                    boostForward = false;
                }
                if (direction.length() <= 0f) {
                    direction.y = 0.75f;
                    boostScale += 0.05f;
                }
                Misc.normalise(direction);
                VectorUtils.rotate(direction, ship.getFacing() - 90f, direction);
                direction.scale(((ship.getMaxSpeedWithoutBoost() * INSTANT_BOOST_MULT) + INSTANT_BOOST_FLAT) * boostScale);
                Vector2f.add(ship.getVelocity(), direction, ship.getVelocity());
                ended = true;

                float duration = (float) Math.sqrt(shipRadius) / 25f;
                ship.getEngineController().getExtendLengthFraction().advance(1f);
                ship.getEngineController().getExtendWidthFraction().advance(1f);
                ship.getEngineController().getExtendGlowFraction().advance(1f);
                for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
                    float level = 1f;
                    if (eng.isSystemActivated()) {
                        level = getSystemEngineScale(eng, boostVisualDir);
                    }
                    if ((eng.isActive() || eng.isSystemActivated()) && (level > 0f)) {
                        Color bigBoostColor = new Color(
                                Math.round(0.1f * ENGINE_COLOR.getRed()),
                                Math.round(0.1f * ENGINE_COLOR.getGreen()),
                                Math.round(0.1f * ENGINE_COLOR.getBlue()),
                                Math.round(0.3f * ENGINE_COLOR.getAlpha() * level));
                        Color boostColor = new Color(BOOST_COLOR.getRed(), BOOST_COLOR.getGreen(), BOOST_COLOR.getBlue(),
                                Math.round(BOOST_COLOR.getAlpha() * level));
                        Global.getCombatEngine().spawnExplosion(eng.getLocation(), ZERO, bigBoostColor,
                                12f * boostScale * eng.getEngineSlot().getWidth(), duration);
                        Global.getCombatEngine().spawnExplosion(eng.getLocation(), ZERO, boostColor,
                                6f * boostScale * eng.getEngineSlot().getWidth(), 0.15f);
                    }
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        started = false;
        ended = false;
        boostScale = 0.85f;
        boostVisualDir = 0f;
        boostForward = false;
        engState.clear();

        stats.getMaxTurnRate().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (ship != null) {
            if (ship.getEngineController().isFlamedOut()) {
                return failstate;
            }
        }
        return null;
    }
    
    private static float getSystemEngineScale(ShipEngineAPI engine, float direction) {
        float engAngle = engine.getEngineSlot().getAngle();
        if (Math.abs(MathUtils.getShortestRotation(engAngle, direction)) > 100f) {
            return 1f;
        } else {
            return 0f;
        }
    }
}
