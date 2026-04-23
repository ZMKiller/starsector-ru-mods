package SOTFECO.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lwjgl.util.vector.Vector2f


class DiktatPropaganda: BaseHullMod() {

    companion object {
        const val INTERVAL_KEY = "SOTFECO_propogandaInterval"
        const val AFFECTING_KEY = "SOTFECO_propogandaAffecting"
        const val MOD_ID = "SOTFECO_propoganda"
        const val RANGE = 3000f

        val phrases = listOf(
            Pair("The lion provides! The lion protects! Dedicate yourself to the lion, and survive this trial!", 50f),
            Pair("The executor watches you with great interest. Protect our sovereignty!", 50f),
            Pair("The only path to progression is through battle. Fight!", 50f),
            Pair("Askonian steel is the strongest in the sector. Nary a harpoon nor reaper may penetrate!", 50f),
            Pair("May your aim be true, and your honor stronger!", 50f),
            Pair("Perform amicably, and you may be granted two hours of sick leave.", 20f),
            Pair("The highest kill-count in this battle receives a crate of volturnian brandy!", 10f),
        )

        fun getRandomPhrase(): String {
            return getPicker().pick()
        }

        fun getPicker(): WeightedRandomPicker<String> {
            val picker = WeightedRandomPicker<String>()

            phrases.forEach { picker.add(it.first, it.second) }

            return picker
        }
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        super.advanceInCombat(ship, amount)
        if (ship == null) return
        if (ship.fluxTracker.isOverloaded || ship.fluxTracker.isVenting || ship.isHulk) return

        if (ship.customData[INTERVAL_KEY] == null) ship.setCustomData(INTERVAL_KEY, IntervalUtil(10f, 15f))
        val interval: IntervalUtil = ship.customData[INTERVAL_KEY] as IntervalUtil
        if (ship.customData[AFFECTING_KEY] == null) ship.setCustomData(AFFECTING_KEY, HashSet<ShipAPI>())
        val affected = ship.customData[AFFECTING_KEY] as MutableSet<ShipAPI>

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            doPropoganda(ship)
        }

        val engine = Global.getCombatEngine()
        val checkIterator = engine.shipGrid.getCheckIterator(ship.location, RANGE, RANGE)

        for (ship in affected) {
            ship.mutableStats.recoilPerShotMult.unmodify(MOD_ID)
            ship.mutableStats.recoilDecayMult.unmodify(MOD_ID)
            ship.mutableStats.combatWeaponRepairTimeMult.unmodify(MOD_ID)
            ship.mutableStats.combatEngineRepairTimeMult.unmodify(MOD_ID)
        }
        affected.clear()
        while (checkIterator.hasNext()) {
            val iter = checkIterator.next() as ShipAPI
            if (iter.isHulk || iter.owner != ship.owner) continue

            if (iter.variant.hasHullMod(HullMods.ANDRADA_MODS)) {
                affected += iter
                ship.mutableStats.recoilDecayMult.modifyMult(MOD_ID, 0.8f)
                ship.mutableStats.recoilPerShotMult.modifyMult(MOD_ID, 1.2f)
                ship.mutableStats.combatWeaponRepairTimeMult.modifyMult(MOD_ID, 0.9f)
                ship.mutableStats.combatEngineRepairTimeMult.modifyMult(MOD_ID, 0.9f)

                if (iter == engine.playerShip) {
                    engine.maintainStatusForPlayerShip(
                        this,
                        "graphics/icons/tactical/neural_link.png",
                        "Ear-splitting Propaganda",
                        "Recoil increased, repair slowed",
                        true
                    )
                }
            }
        }
    }

    fun doPropoganda(ship: ShipAPI) {
        val pos = ship.location
        val textPos = Vector2f(pos.x, pos.y + ship.collisionRadius)
        val engine = Global.getCombatEngine()
        val message = getRandomPhrase()
        engine.addFloatingTextAlways(
            textPos,
            "\"$message\"",
            32f,
            Misc.getTextColor(),
            ship,
            0f,
            0f,
            10f,
            0.5f,
            0.5f,
            1f
        )
    }
}