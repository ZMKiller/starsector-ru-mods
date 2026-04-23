package org.amazigh.foundry.scripts;

import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_QuadMultiLanceBeamEffect implements BeamEffectPlugin {

    private final IntervalUtil flashInterval = new IntervalUtil(0.16f,0.3f);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

        flashInterval.advance(engine.getElapsedInLastFrame());
        if (flashInterval.intervalElapsed()) {
        	
            float size = beam.getWidth() * MathUtils.getRandomNumberInRange(1.9f, 2.2f);
            
            float dur = MathUtils.getRandomNumberInRange(0.15f,0.24f);
            
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), beam.getWidth(), 0.4f, dur, beam.getCoreColor());
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), size, 0.4f, dur, beam.getFringeColor().brighter());
            
            if (beam.didDamageThisFrame()) {
                engine.addHitParticle(beam.getTo(), beam.getSource().getVelocity(), size * 2.5f, 0.4f, dur, beam.getFringeColor());
            }
        }
        
    }
}