package assortment_of_things.exotech.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lazywizard.lazylib.MathUtils

class HypersatialJavelinEffect : EveryFrameWeaponEffectPlugin {



    override fun advance(amount: Float, engine: CombatEngineAPI?, weapon: WeaponAPI) {
        weapon.ensureClonedSpec()
        var spec = weapon.spec
        var ship = weapon.ship
        if (spec is ProjectileWeaponSpecAPI) {

            if (weapon.spec.weaponId == "rat_hyper_dart") {
                spec.maxAmmo = 6
                weapon.maxAmmo = 6
                weapon.ammo = MathUtils.clamp(weapon.ammo, 0, 6)
            }
            else {
                spec.maxAmmo = 5
                weapon.maxAmmo = 5
                weapon.ammo = MathUtils.clamp(weapon.ammo, 0, 5)
            }

            if (ship.isPhased) {
                weapon.ammoTracker.reloadProgress += spec.ammoPerSecond / spec.reloadSize * amount
            }
            else {

            }
        }
    }

}