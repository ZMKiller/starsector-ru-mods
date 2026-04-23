package SOTFECO.rulecmd

import SOTFECO.combatObj.SOTFECO_derelictFleetObj
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.AdjustRep
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class SOTFECO_derelictFleetCMD: BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: List<Misc.Token>?,
        memoryMap: Map<String?, MemoryAPI?>?
    ): Boolean {
        if (dialog == null || params == null) return false

        val command = params[0].getString(memoryMap)

        when (command) {
            "addLoot" -> {
                val fleet = dialog.interactionTarget as CampaignFleetAPI
                val newCargo = SOTFECO_derelictFleetObj.genLoot(fleet.cargo.maxCapacity / 2f)

                Global.getSector().playerFleet.cargo.addAll(newCargo)
            }
            "doRep" -> {
                val fleet = dialog.interactionTarget as CampaignFleetAPI
                val fac = fleet.faction.id

                AdjustRep().execute(ruleId, dialog, Misc.tokenize("$fac COOPERATIVE 5"), memoryMap)
            }
            "makeUnimportant" -> {
                val fleet = dialog.interactionTarget as CampaignFleetAPI
                Misc.makeUnimportant(fleet, "\$SOTFECO_derelictsGivingLoot")
            }
        }

        return false
    }
}