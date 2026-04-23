package SOTFECO.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI

class ForceUseSystem: BaseHullMod() {

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        super.advanceInCombat(ship, amount)

        if (ship == null) return

        if (ship.system.canBeActivated() && !ship.system.isActive) {
            ship.useSystem()
        }
    }
}