package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HWIUniversalLimit implements EveryFrameWeaponEffectPlugin {
    private boolean once = false;
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!once){
            if(weapon.getSlot().getWeaponType()!= WeaponAPI.WeaponType.UNIVERSAL
                    &&weapon.getSlot().getWeaponType()!= WeaponAPI.WeaponType.COMPOSITE
                    &&weapon.getSlot().getWeaponType()!= WeaponAPI.WeaponType.SYNERGY
                    &&weapon.getSlot().getWeaponType()!= WeaponAPI.WeaponType.MISSILE){
                weapon.setMaxAmmo(weapon.getSpec().getMaxAmmo());
                weapon.setAmmo(weapon.getMaxAmmo());
                once = true;
            }
        }
    }
}
