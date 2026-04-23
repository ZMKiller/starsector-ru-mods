package SOTFECO.combatObj.faction

import com.fs.starfarer.api.Global
import data.scripts.combat.obj.SotfEmplacementEffect
import kotlin.math.roundToLong

class SOTFECO_diktatEmplacement: SotfEmplacementEffect() {

    override fun getLongDescription(): String {
        val min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide")
        val total = Global.getSettings().battleSize
        val maxPoints = (total * (1f - min)).roundToLong()

        return "transmits diktat propaganda in local proximity\n" +
                "interfaces with friendly LG ships, causing major combat debuffs\n" +
                "propaganda transmitter can be repurposed into command module, significantly increasing DP allocation\n" +
                "\n" +
                "+$bonusDeploymentPoints bonus deployment points\n" +
                "up to a maximum of $maxPoints points"
    }

}