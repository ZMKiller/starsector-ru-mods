package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HSIHyperJump extends BaseShipSystemScript {
    public CombatEngineAPI engine = Global.getCombatEngine();
    private ShipAPI ship;
    protected static final float MAX_TRAVEL_SPEED = 1500f;
    protected IntervalUtil afterImage = new IntervalUtil(0.05f, 0.05f);
    protected static final Color AFTER_IMAGE = new Color(175, 175, 255, 175);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        float speed = ship.getMaxSpeed() + MAX_TRAVEL_SPEED;
        boolean shouldPhase = true;
        if (state == ShipSystemStatsScript.State.OUT) {
            speed = ship.getMaxSpeed() + MAX_TRAVEL_SPEED * effectLevel;
            shouldPhase = false;
        }
        if (ship.getVelocity().length() == 0) {
            ship.getVelocity().set(Misc.getUnitVectorAtDegreeAngle(ship.getFacing()));
        }
        ship.setPhased(shouldPhase);
        ship.getVelocity().scale(speed / ship.getVelocity().length());
        afterImage.advance(engine.getElapsedInLastFrame());
        if (afterImage.intervalElapsed()) {
            ship.addAfterimage(AFTER_IMAGE, 0, 0, -ship.getVelocity().getX() * 0.5f, -ship.getVelocity().getY() * 0.5f,
                    0, 0.1f, 0f, 0.1f, false, true, true);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        ship.setPhased(false);
        afterImage.forceIntervalElapsed();
    }
}
