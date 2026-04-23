package second_in_command.skills.piracy

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.skills.TacticalDrills
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin

class ImprovisedRaids : SCBaseSkillPlugin() {

    override fun getAffectsString(): String {
        return "ground operations"
    }

    override fun addTooltip(data: SCData, tooltip: TooltipMakerAPI) {

        tooltip.addPara("+40%% effectiveness of ground operations such as raids", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("-25%% marine casualties suffered during ground operations such as raids", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI?, variant: ShipVariantAPI, hullSize: ShipAPI.HullSize?, id: String?) {


    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI?, variant: ShipVariantAPI, id: String?) {



    }

    override fun advance(data: SCData, amount: Float) {
        data.fleet.stats.dynamic.getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyPercent("sc_improvised_raids", 40f, "Improvised Raids")
        data.fleet.stats.dynamic.getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).modifyMult("sc_improvised_raids", 0.75f, "Improvised Raids")
    }

    override fun onActivation(data: SCData) {

    }

    override fun onDeactivation(data: SCData) {

    }
}