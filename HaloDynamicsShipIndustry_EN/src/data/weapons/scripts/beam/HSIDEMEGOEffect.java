package data.weapons.scripts.beam;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;

import data.hullmods.HSIEGO.HSIEGOStats;

public class HSIDEMEGOEffect implements BeamEffectPlugin {
    private float elapsed = 0f;
    private HSIEGOStats ego = null;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (ego == null) {
            ShipAPI source = null;
            if(beam.getSource().getAIFlags().hasFlag(AIFlags.DRONE_MOTHERSHIP)){
                source = (ShipAPI)beam.getSource().getAIFlags().getCustom(AIFlags.DRONE_MOTHERSHIP);
            }
            if (source!=null&&source.hasListenerOfClass(HSIEGOStats.class)) {
                ego = beam.getSource().getListeners(HSIEGOStats.class).get(0);
            }
        }
        CombatEntityAPI target = beam.getDamageTarget();
        if (ego != null && target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            if (beam.didDamageThisFrame()) {
                ShipAPI ship = (ShipAPI) target;
                boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                if (!hitShield) {
                    int[] loc = ship.getArmorGrid().getCellAtLocation(beam.getRayEndPrevFrame());
                    if (ship.getArmorGrid().getArmorValue(loc[0], loc[1]) < ship.getArmorGrid().getMaxArmorInCell()
                            * 0.02f) {
                        elapsed += amount;
                        ego.addDamageDealt(beam.getDamage().computeDamageDealt(0.1f));
                    }
                }
            }
        }
    }
}
