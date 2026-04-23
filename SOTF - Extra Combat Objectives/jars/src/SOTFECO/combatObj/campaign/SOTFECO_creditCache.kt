package SOTFECO.combatObj.campaign

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BattleObjectiveEffect
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class SOTFECO_creditCache: BaseBattleObjectiveEffect() {

    companion object {
        fun getCreditCoeff(): Float {
            var coeff = 1f

            val level = Global.getSector()?.playerStats?.level?.toFloat() ?: 0f
            return coeff + (COEFF_INC_PER_LEVEL * level)
        }

        fun playerIsPoor(): Boolean {
            var max = BASE_MAX_CREDITS * getCreditCoeff()
            val currCreds = Global.getSector()?.playerFleet?.cargo?.credits?.get() ?: Float.MAX_VALUE

            return (currCreds < max)
        }

        const val COEFF_INC_PER_LEVEL = 0.3f
        const val MIN_CREDITS_BASE = 10000f
        const val MAX_CREDITS_BASE = 20000f
        const val BASE_MAX_CREDITS = 100000f
    }

    val credits = MathUtils.getRandomNumberInRange(MIN_CREDITS_BASE * getCreditCoeff(), MAX_CREDITS_BASE * getCreditCoeff())
    var fading = false
    val fadingInterval = IntervalUtil(1f, 1f)

    override fun advance(amount: Float) {
        val engine = Global.getCombatEngine()
        if (fading) {

            fadingInterval.advance(amount)
            if (fadingInterval.intervalElapsed()) {
                engine.removeEntity(objective)
                return
            }

            val progress = (fadingInterval.intervalDuration / fadingInterval.elapsed)
            objective.sprite.alphaMult = 1 - (progress)

            return
        }

        if (isCaptured()) {
            if (objective.owner == 0) {
                engine.combatUI.addMessage(
                    1,
                    Misc.getPositiveHighlightColor(),
                    objective.displayName,
                    Misc.getTextColor(),
                    " breach successful! ${Misc.getDGSCredits(credits)} added to account."
                )
                Global.getSoundPlayer().playUISound("ui_modspec_drop", 1f, 1f)
                Global.getSector()?.playerFleet?.cargo?.credits?.add(credits)
            }
            fading = true
        }
    }

    fun isCaptured(): Boolean {
        return objective.owner == 0 || objective.owner == 1
    }

    override fun getStatusItemsFor(ship: ShipAPI?): List<BattleObjectiveEffect.ShipStatusItem?>? {
        return null
    }

    override fun getLongDescription(): String {
        return String.format(
            """
            - stealth-coated credit cache, only detected by high-res combat sensors
            - protected by heavy-duty ICE, expect lengthy capture time
            - preliminary scans suggest ${Misc.getDGSCredits(credits)} inside
            
            """.trimIndent()
        )
    }
}