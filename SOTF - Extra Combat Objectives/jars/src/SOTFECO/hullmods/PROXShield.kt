package SOTFECO.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.combat.ShipAPI

class PROXShield: BaseHullMod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null || id == null) return

        ship.setShield(ShieldType.OMNI, 0.4f, 0.9f, 270f)
    }
}