package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIAnimatedWeapon implements EveryFrameWeaponEffectPlugin {
    private boolean init = true;

    private int _CHARGE=0,_FIRE=0,_END=0,_CD=0,_TOTAL=0;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        if(init){
            init = false;
            init(weapon);
        }
        if(weapon.getAnimation()!=null){

        }
    }

    public void init(WeaponAPI weapon){
        for(String tag:weapon.getSpec().getTags()){
            if(tag.startsWith("HAW_Charge_")){
                _CHARGE = Integer.parseInt(tag.substring(11));
            }
            if(tag.startsWith("HAW_Fire_")){
                _FIRE = Integer.parseInt(tag.substring(9));
            }
            if(tag.startsWith("HAW_End_")){
                _END = Integer.parseInt(tag.substring(8));
            }
            if(tag.startsWith("HAW_CD_")){
                _CD = Integer.parseInt(tag.substring(7));
            }
        }
        if(weapon.getAnimation()!=null){
            _TOTAL = weapon.getAnimation().getNumFrames();
        }
    }
}
