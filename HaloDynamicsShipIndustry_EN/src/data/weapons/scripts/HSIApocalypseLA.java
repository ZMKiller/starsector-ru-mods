package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIApocalypseLA implements EveryFrameWeaponEffectPlugin {
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        float slotAngle = weapon.getSlot().getAngle()+weapon.getShip().getFacing();
        weapon.setCurrAngle(slotAngle + weapon.getChargeLevel() * 60f);
    }
}
