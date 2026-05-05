package data.weapons.scripts.proj;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.Misc;

public class HWIGunFireShockWaveEffect {
    private final static Vector2f stasis = new Vector2f(0, 0);

    //Everything MUST be non-null
    public static void createShockWave(float extendLength, float waveRadius, Vector2f baseLoc, float baseFacing,
            float waveThickness, int waveParticles,float waveTime,float brightness, Color waveColor) {
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f extendVector = (Vector2f) Misc.getUnitVectorAtDegreeAngle(baseFacing).scale(extendLength);
        extendVector = Vector2f.add(extendVector, baseLoc, extendVector);
        Vector2f normalVector = Misc.getUnitVectorAtDegreeAngle(baseFacing + 90f);
        if (waveParticles % 2 == 0) {
            for (int i = 0; i < waveParticles; i++) {
                Vector2f thisNormal = new Vector2f(normalVector);
                Vector2f thisVec = Vector2f.add(extendVector,
                        (Vector2f) thisNormal
                                .scale((i % 2 == 0) ? (float) ((-1 * i / 2 - 0.5f) * waveRadius / waveParticles)
                                        : (float) (((i + 1) / 2 - 0.5f) * waveRadius / waveParticles)),
                        null);
                engine.addHitParticle(thisVec, stasis, waveThickness, brightness, waveTime/4f, waveTime, waveColor);
            }
        }else{
            for (int i = 0; i < waveParticles; i++) {
                Vector2f thisNormal = new Vector2f(normalVector);
                Vector2f thisVec = Vector2f.add(extendVector,
                        (Vector2f) thisNormal
                                .scale((i % 2 == 0) ? (float) ((-1 * i / 2) * waveRadius / waveParticles)
                                        : (float) (((i + 1) / 2) * waveRadius / waveParticles)),
                        null);
                engine.addHitParticle(thisVec, stasis, waveThickness, brightness, waveTime/4f, waveTime, waveColor);
            }
        }
    }

}
