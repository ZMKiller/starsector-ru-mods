package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIDecorationAlwaysAnimate implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(weapon.getShip()!=null&&weapon.getShip().isAlive()){
            if(weapon.getAnimation()!=null) weapon.getAnimation().play();
        }
    }
}
