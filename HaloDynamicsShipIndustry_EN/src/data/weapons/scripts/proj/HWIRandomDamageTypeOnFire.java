package data.weapons.scripts.proj;

import com.fs.starfarer.api.combat.*;

public class HWIRandomDamageTypeOnFire implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        int type = (int)(4f*Math.random());
        switch (type){
            case 0:
                projectile.getDamage().setType(DamageType.KINETIC);
                projectile.getDamage().getModifier().modifyMult("HSI_ChangedType",0.66f);
                break;
            case 1:
                projectile.getDamage().setType(DamageType.HIGH_EXPLOSIVE);
                projectile.getDamage().getModifier().modifyMult("HSI_ChangedType",0.66f);
                break;
        }
    }
}
