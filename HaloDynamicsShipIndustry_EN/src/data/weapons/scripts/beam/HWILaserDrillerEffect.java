package data.weapons.scripts.beam;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HWILaserDrillerEffect implements BeamEffectPlugin {
    public static Color STANDARD_RIFT_COLOR = new Color(25, 25, 255, 255);
    public static Color EXPLOSION_UNDERCOLOR = new Color(100, 0, 175, 100);
    public static Color NEGATIVE_SOURCE_COLOR = new Color(200, 255, 200, 25);

    protected IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused())
            return;
        if (beam.getBrightness() < 1f)
            return;
        if (beam.getDamageTarget() != null && beam.getDamageTarget() instanceof ShipAPI
                && beam.getDamageTarget().getOwner() != beam.getSource().getOwner()) {
            ShipAPI enemy = (ShipAPI) beam.getDamageTarget();
            if (enemy.getShield() != null && enemy.getShield().isWithinArc(beam.getTo())) {

            } else {
                if (!enemy.hasListenerOfClass(HWILaserDrillerListener.class)) {
                    enemy.addListener(new HWILaserDrillerListener(enemy));
                } else {
                    enemy.getListeners(HWILaserDrillerListener.class).get(0).addBeam(beam);
                }
            }
        }

    }

    public class HWILaserDrillerListener implements AdvanceableListener {
        private TimeoutTracker<BeamAPI> tracker = new TimeoutTracker<BeamAPI>();
        private static final float LOSS_RATE = 6f;
        private static final float INCREASE_RATE = 2f;
        private static final float MAX_ARMOR_EFFECTIVENESS = 30f;
        private ShipAPI ship;
        private float AromrEffectivenessLoss = 0f;

        public HWILaserDrillerListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            if (!Global.getCombatEngine().isPaused() && !Global.getCombatEngine().isEntityInPlay(ship)
                    && ship.isAlive()) {
                tracker.advance(amount);
                if (tracker.getItems().isEmpty()) {
                    AromrEffectivenessLoss = Math.max(0, AromrEffectivenessLoss - amount * LOSS_RATE);
                } else {
                    AromrEffectivenessLoss = Math.min(MAX_ARMOR_EFFECTIVENESS,
                            AromrEffectivenessLoss + amount * INCREASE_RATE);
                }
                ship.getMutableStats().getEffectiveArmorBonus().modifyPercent("HWILASERDRILLER",
                        -1f * AromrEffectivenessLoss);
            }
        }

        public void addBeam(BeamAPI beam) {
            tracker.add(beam, 1.2f);
        }
    }
}
