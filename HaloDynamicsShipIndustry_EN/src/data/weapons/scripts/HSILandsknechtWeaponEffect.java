package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSILandsknechtWeaponEffect implements EveryFrameWeaponEffectPlugin {

    protected WeaponAPI Arm_LT;
    protected WeaponAPI Arm_LB;
    protected WeaponAPI Arm_RT;
    protected WeaponAPI Arm_RB;
    protected WeaponAPI Sword;
    protected boolean init = false;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!init) {
            init(weapon);
            init = true;
        }
    }

    private void init(WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        for (WeaponAPI w : ship.getAllWeapons()) {
            switch (weapon.getSpec().getWeaponId()) {
                case "HSI_Landsknecht_Arm_LT":
                    Arm_LT = w;
                    break;
                case "HSI_Landsknecht_Arm_LB":
                    Arm_LB = w;
                    break;
                case "HSI_Landsknecht_Arm_RT":
                    Arm_RT = w;
                    break;
                case "HSI_Landsknecht_Arm_RB":
                    Arm_RB = w;
                    break;
                case "HSI_Landsknecht_Sword":
                    Sword = w;
                    break;
            }
        }
    }

}
