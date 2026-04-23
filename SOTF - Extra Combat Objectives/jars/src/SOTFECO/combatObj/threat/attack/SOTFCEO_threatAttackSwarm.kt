package SOTFECO.combatObj.threat.attack

import SOTFECO.combatObj.threat.SOTFCEO_threatSwarm
import com.fs.starfarer.api.Global

abstract class SOTFCEO_threatAttackSwarm: SOTFCEO_threatSwarm() {

    override var swarmSize: Int = 600

    override fun captured(amount: Float) {
        triggerAttack()

        delete()
    }

    abstract fun triggerAttack()

    override fun getLongDescription(): String? {
        val min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide")
        val total = Global.getSettings().battleSize
        Math.round(total * (1f - min))

        return String.format(
            """
            - swarm of unidentifiable metallic objects
            - unknown interface, expect lengthy capture time
            
            """.trimIndent()
        )
    }
}