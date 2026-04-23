package SOTFECO.combatObj.threat.constructor

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.impl.combat.threat.ThreatCombatStrategyForBothSidesPlugin
import com.fs.starfarer.api.impl.combat.threat.ThreatShipConstructionScript.SHIP_UNDER_CONSTRUCTION
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil

class SOTFCEO_forceThreatShipAllyScript(): BaseEveryFrameCombatPlugin() {
    val interval = IntervalUtil(10f, 10.1f)

    override fun advance(amount: Float, events: List<InputEventAPI?>?) {
        super.advance(amount, events)

        if (Global.getCombatEngine().isPaused) return

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            if (!Global.getCombatEngine().hasPluginOfClass(ThreatCombatStrategyForBothSidesPlugin::class.java)) {
                Global.getCombatEngine().addPlugin(ThreatCombatStrategyForBothSidesPlugin())
            }
            Global.getCombatEngine().removePlugin(this)
            return
        }

        for (ship in Global.getCombatEngine().ships.filter { it.owner == 0 }) {
            if (ship.hasTag(SHIP_UNDER_CONSTRUCTION)) {
                ship.isAlly = true
            }
        }
    }

}