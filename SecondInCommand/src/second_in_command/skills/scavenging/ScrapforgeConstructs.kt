package second_in_command.skills.scavenging

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCData
import second_in_command.skills.scavenging.abilities.ScrapforgeDetectorAbility
import second_in_command.skills.scavenging.abilities.ScrapforgeDisruptorAbility
import second_in_command.specs.SCBaseSkillPlugin

class ScrapforgeConstructs : SCBaseSkillPlugin() {

    override fun getAffectsString(): String {
        return "fleet"
    }

    override fun addTooltip(data: SCData, tooltip: TooltipMakerAPI) {

        tooltip.addPara("Unlocks and enables the following abilities", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        tooltip.addSpacer(10f)

        var detectorAbility = Global.getSettings().getAbilitySpec("sc_detector_relay")
        var detectorImage = tooltip.beginImageWithText(detectorAbility.iconName, 48f)
        var detectAbility = ScrapforgeDetectorAbility()
        detectAbility.addAbilityTooltip(detectorImage, detectorAbility)
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(20f)

        var disruptorAbility = Global.getSettings().getAbilitySpec("sc_disruptor_relay")
        var disruptorImage = tooltip.beginImageWithText(disruptorAbility.iconName, 48f)
        var disruptAbility = ScrapforgeDisruptorAbility()
        disruptAbility.addAbilityTooltip(disruptorImage, disruptorAbility)
        tooltip.addImageWithText(0f)
    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI, variant: ShipVariantAPI, hullSize: ShipAPI.HullSize, id: String) {

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI, variant: ShipVariantAPI, id: String) {

    }

    override fun advance(data: SCData, amount: Float) {

    }



    override fun onActivation(data: SCData) {
        if (!data.fleet.hasAbility("sc_detector_relay")) {
            data.fleet.addAbility("sc_detector_relay")
            if (data.isPlayer) {
                Global.getSector().characterData.addAbility("sc_detector_relay")
            }
        }

        if (!data.fleet.hasAbility("sc_disruptor_relay")) {
            data.fleet.addAbility("sc_disruptor_relay")
            if (data.isPlayer) {
                Global.getSector().characterData.addAbility("sc_disruptor_relay")
            }
        }
    }

    override fun onDeactivation(data: SCData) {

    }

}