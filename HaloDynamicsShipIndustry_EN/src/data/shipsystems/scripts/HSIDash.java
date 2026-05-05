package data.shipsystems.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSIDash extends BaseShipSystemScript {
    private ShipAPI ship = null;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private float elapsed = 0f;
    public static final Color JITTER_UNDER_COLOR = new Color(55, 75, 255, 155);
    private boolean start = true;
    private Vector2f from = null;
    private Vector2f to = null;
    public static final float RANGE = 2000f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (start) {
            from = new Vector2f(ship.getLocation());
            if (ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
                to = new Vector2f((Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS));
            } else {
                to = new Vector2f(ship.getMouseTarget());
            }
            start = false;
        }
        if (effectLevel > 0) {
            move(state, effectLevel);
        }
        elapsed += engine.getElapsedInLastFrame();
        if (state == State.IN) {
            float seed = (float) Math.random();
            if (effectLevel > 0.2f && !ship.isPhased()) {
                ship.setPhased(true);
            }
            float gap = 0.1f;
            if (elapsed > gap) {
                elapsed -= gap;
                MissileAPI proj = (MissileAPI) engine.spawnProjectile(ship, null, "flare_standard",
                        Misc.getPointAtRadius(ship.getLocation(),
                                effectLevel * ship.getCollisionRadius() * 0.8f),
                        seed * 360f,
                        null);
            }
            //ship.setExtraAlphaMult(1f - effectLevel);
        }
        if (state == State.OUT) {
            float seed = (float) Math.random();
            if (effectLevel > 0.1f && !ship.isPhased()) {
                ship.setPhased(true);
            }
            float gap = 0.1f;
            if (elapsed > gap) {
                elapsed -= gap;
                MissileAPI proj = (MissileAPI) engine.spawnProjectile(ship, null, "flare_standard",
                        Misc.getPointAtRadius(ship.getLocation(),
                                effectLevel * ship.getCollisionRadius() * 0.8f),
                        seed * 360f,
                        null);
            }
            if (effectLevel < 0.1f && ship.isPhased()) {
                ship.setPhased(false);
            }
            //ship.setExtraAlphaMult(1f - effectLevel);
        }
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, effectLevel, 6, 0f, 3f + 0.5f*effectLevel);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        start = true;
        elapsed = 0;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (!isUsable(system, ship))
            return HSII18nUtil.getShipSystemString("HSIOutOfRange");
        return HSII18nUtil.getShipSystemString("HSIUsable");
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        Vector2f checkfrom = ship.getLocation();
        Vector2f checkto = ship.getMouseTarget();
        if (ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
            checkto = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
        }
        return Misc.getDistance(checkfrom, checkto) - ship.getCollisionRadius() < getRange(ship);
    }

    public static float getRange(ShipAPI ship) {
		if (ship == null)
			return RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
	}

    private void move(State state, float effectLevel) {
        if (from != null && to != null) {
            if (state == State.IN) {
                double t = (1-effectLevel) * (-Math.PI / 2);
                float frac = (float) ((Math.sin(t) + 1) / 2);
                ship.getLocation().set(from.x + frac * (to.x - from.x), from.y + frac * (to.y - from.y));
            } else if (state == State.OUT) {
                double t = (1 - effectLevel) * (Math.PI / 2);
                float frac = (float) ((Math.sin(t) + 1) / 2);
                ship.getLocation().set(from.x + frac * (to.x - from.x), from.y + frac * (to.y - from.y));
            }
        }
        /*if(ship.getShipTarget()!=null){
            ship.setFacing(VectorUtils.getAngle(ship.getLocation(), ship.getShipTarget().getLocation()));
        }else{
            ship.setFacing(VectorUtils.getAngle(ship.getLocation(), ship.getMouseTarget()));
        }*/
    }

}