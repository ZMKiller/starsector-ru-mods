package data.weapons.scripts.beam;

import java.util.Random;

import data.hullmods.WeaponMod.HSIProtocalMeltDown;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HWIFocusBeamEffect implements BeamEffectPlugin {
    private IntervalUtil fireInterval = new IntervalUtil(0.2f, 0.25f);
    // private boolean wasZero = true;
    private float ignore = 0.15f;
    private boolean once = true;


    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused())
            return;
        if(beam.getSource()!=null&& HSIProtocalMeltDown.hasProtocalMeltDown(beam.getSource())&&once){
            ignore*=HSIProtocalMeltDown.getMult();
            once = false;
        }
        if (beam.getDamageTarget() instanceof ShipAPI) {
            ShipAPI target = (ShipAPI) beam.getDamageTarget();
            boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
            if (hitShield) {
                float shieldEfficiency = Math.min(1,
                        target.getShield().getFluxPerPointOfDamage()
                                * target.getMutableStats().getShieldDamageTakenMult().getModifiedValue()
                                * target.getMutableStats().getShieldAbsorptionMult().getModifiedValue());
                float reduction = beam.getDamage().computeDamageDealt(amount) * (1 - shieldEfficiency);
                float pierce = reduction * ignore;
                target.getFluxTracker().increaseFlux(pierce,true);
            }
        }
        if (beam.getBrightness() >= 0.5) {
            fireInterval.advance(amount);
            if (fireInterval.intervalElapsed())
                createVisualArc(beam, beam.getWidth());
        }
    }

    public void createVisualArc(BeamAPI beam, float r) {
        float dist = Misc.getDistance(beam.getFrom(), beam.getRayEndPrevFrame());
        Vector2f direction = VectorUtils.getDirectionalVector(beam.getFrom(), beam.getRayEndPrevFrame());
        float gap = 100f;
        float gap_random = 50f;
        float next = gap + (1 - 0.5f) * (float) gap_random;
        float currentDist = 0;
        Vector2f last = new Vector2f(beam.getFrom());
        while ((dist - currentDist) > next) {
            currentDist += next;
            Vector2f n = new Vector2f(direction);
            n.scale(currentDist);
            n = Vector2f.add(beam.getFrom(), n, n);
            n = Misc.getPointWithinRadiusUniform(n, r, r * 1.5f, new Random());
            EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArcVisual(last, beam.getSource(), n,
                    beam.getSource(), beam.getWidth() / 3f + beam.getWidth() / 8f * (float) Math.random(),
                    beam.getFringeColor(), beam.getCoreColor());
            arc.setSingleFlickerMode();
            last = new Vector2f(n);
            next = gap + (1 - 0.5f) * (float) gap_random;
        }
        EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArcVisual(last, beam.getSource(),
                beam.getRayEndPrevFrame(), beam.getSource(),
                beam.getWidth() / 3f + beam.getWidth() / 8f * (float) Math.random(), beam.getFringeColor(),
                beam.getCoreColor());
        arc.setSingleFlickerMode();
    }
}
