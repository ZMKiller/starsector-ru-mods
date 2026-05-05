package data.weapons.scripts.proj;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

import data.kit.HSIAutoFireTargetPicker;

public class HWISmartAmmoOnFire implements OnFireEffectPlugin{

    protected static final float FLUX_DISCOUNT = 0.5f;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        CombatEntityAPI target = HSIAutoFireTargetPicker.PickAutoFireTarget(weapon);
        HWISmartAmmoScript.getInstance().addProj(target, projectile);
    }

}
