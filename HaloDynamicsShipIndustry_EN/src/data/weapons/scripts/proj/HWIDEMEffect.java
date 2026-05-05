package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

public class HWIDEMEffect implements OnFireEffectPlugin {
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (!(projectile instanceof MissileAPI)) return;

        MissileAPI missile = (MissileAPI) projectile;

        ShipAPI ship = null;
        if (weapon != null) ship = weapon.getShip();
        if (ship == null) return;

        HWIDEMScript script = new HWIDEMScript(missile, ship, weapon);
        Global.getCombatEngine().addPlugin(script);
    }

}