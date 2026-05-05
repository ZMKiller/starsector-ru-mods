package data.weapons.scripts.Mech;

import com.fs.starfarer.api.combat.*;

import java.util.ArrayList;
import java.util.List;

public class HSIApostleLanceEffect implements BeamEffectPlugin {

    private List<CombatEntityAPI> damaged = new ArrayList<>();

    private boolean reachedTop = false;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        beam.getDamage().getModifier().modifyMult("NO_DIRECT",0.01f);
        CombatEntityAPI target = beam.getDamageTarget();
        // boolean first = beam.getWeapon().getBeams().indexOf(beam) == 0;
        if (target != null&&!damaged.contains(target)) {
            damaged.add(target);
            if(beam.getWeapon().getSpec().getWeaponId().equals("HSI_Apostle_LAD")){
                engine.applyDamage(target, beam.getRayEndPrevFrame(), 150f * beam.getDamage().getModifier().getModifiedValue() * 100f, DamageType.ENERGY, 0, false, false,
                        beam.getSource(), false);
            }else {
                engine.applyDamage(target, beam.getRayEndPrevFrame(), beam.getWeapon().getDerivedStats().getBurstDamage() * beam.getDamage().getModifier().getModifiedValue() * 100f, DamageType.ENERGY, 0, false, false,
                        beam.getSource(), false);
            }
        }

        if(beam.getBrightness()<=0.1f&&reachedTop){
            damaged.clear();
            reachedTop = false;
        }
        if(beam.getBrightness()>=0.95f){
            reachedTop = true;
        }
    }
}
