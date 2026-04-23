package SOTFECO.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.input.InputEventAPI
import data.scripts.campaign.ids.SotfIDs
import data.scripts.campaign.ids.SotfIDs.SKILL_CYBERWARFARE

class ForceWarmind: BaseHullMod() {

    companion object {
        fun createWarmind(ship: ShipAPI): PersonAPI {
            val warmind = Global.getSector().getFaction(SotfIDs.DUSTKEEPERS).createRandomPerson()
            if (ship.captain != null) {
                for (skill in ship.captain.stats.skillsCopy) {
                    warmind.stats.setSkillLevel(skill.skill.id, skill.level)
                }
            }
            warmind.stats.setSkillLevel(SKILL_CYBERWARFARE, 1f)
            warmind.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)

            warmind.stats.level = 5
            return warmind
        }
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null || id == null) return

        Global.getCombatEngine().addPlugin(DelayedAddWarmind(ship))

        ship.captain = createWarmind(ship)

        return
    }

    class DelayedAddWarmind(
        val ship: ShipAPI
    ): BaseEveryFrameCombatPlugin() {
        override fun advance(amount: Float, events: List<InputEventAPI?>?) {
            super.advance(amount, events)

            ship.captain = createWarmind(ship)
            Global.getCombatEngine().removePlugin(this)
        }
    }

}