package data.weapons.scripts.beam;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class HWISlicingBeam implements BeamEffectPlugin {
    public static final float ARC = 60f;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused())
            return;
        float turnRate = ARC/beam.getWeapon().getSpec().getBurstDuration();
        if(beam.getBrightness()<=0&&beam.getWeapon().getBurstFireTimeRemaining()>0){
            beam.getWeapon().setCurrAngle(beam.getWeapon().getCurrAngle()-ARC/2);
        }else{
            beam.getWeapon().setCurrAngle(beam.getWeapon().getCurrAngle()+amount*turnRate);
        }
    }
}
