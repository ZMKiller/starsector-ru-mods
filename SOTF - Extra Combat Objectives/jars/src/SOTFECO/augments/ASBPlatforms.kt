package SOTFECO.augments

import SOTFECO.ReflectionUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.BattleObjectiveAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.BattleAutoresolverPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.CombatEngine
import com.fs.starfarer.combat.entities.BattleObjective
import niko_SA.augments.core.stationAttachment
import org.lwjgl.util.vector.Vector2f
import kotlin.math.ceil

class ASBPlatforms: stationAttachment() {

    companion object {
        const val OBJ_ID = "sotfCEO_objective_ASB"

        const val EXTRA_Y_DISTANCE = 2500f

        const val AUTORESOLVE_ASB_NUM_ANCHOR = 2.3f
    }

    override fun applyInCombat(station: ShipAPI) {
        val toDeploy = getNumASBSToPlace()
        val asbs = deployASBs(toDeploy.first, station)
        Global.getCombatEngine().addPlugin(DelayedASBPlatformAdd(asbs, station))
    }

    private fun deployASBs(num: Int, station: ShipAPI): MutableSet<BattleObjective> {
        val owner = station.owner
        val playerside = owner == 0
        val engine = Global.getCombatEngine() as CombatEngine
        val objectives: MutableSet<BattleObjective> = HashSet()
        var yDist = EXTRA_Y_DISTANCE + station.collisionRadius
        if (playerside) yDist = yDist * -1
        if (num <= 0) return objectives
        if (num == 1) {
            val objective = BattleObjective(
                OBJ_ID,
                Vector2f(station.location.x, yDist),
                BattleObjectiveAPI.Importance.NORMAL
            )
            objectives += objective
            engine.addObject(objective)
        } else {
            val initialX = (engine.mapWidth * -0.12f)
            var currX = initialX
            val xOffset = (initialX * -2) / (num - 1)

            var objsLeft = num
            while (objsLeft-- > 0) {
                val objective = BattleObjective(
                    OBJ_ID,
                    Vector2f(currX, yDist),
                    BattleObjectiveAPI.Importance.NORMAL
                )
                objectives += objective
                engine.addObject(objective)
                currX += xOffset
            }
        }

        return objectives
    }

    override fun getUnavailableReason(): String? {
        val superValue = super.getUnavailableReason()
        if (superValue != null) return superValue

        val batteries = getBatteries()

        if (batteries == null) return "Market must have ground defenses or heavy batteries present"
        if (!batteries.isFunctional) return "Ground defenses non-functional"

        return null
    }

    override fun canBeRemoved(): Boolean {
        if (!super.canBeRemoved()) return false

        val batteries = getBatteries() ?: return true
        return batteries.isFunctional
    }

    private fun getBatteries(): Industry? {
        if (market == null) return null

        return market!!.getIndustry(Industries.HEAVYBATTERIES) ?: market!!.getIndustry(Industries.GROUNDDEFENSES)
    }

    fun getNumASBSToPlace(): Pair<Int, Int> {
        val ind = getBatteries() ?: return Pair(0, 0)
        var base = 0

        if (ind.spec.hasTag(Industries.TAG_GROUNDDEFENSES)) base += 2
        if (ind.id == Industries.HEAVYBATTERIES) base += 1
        if (ind.isImproved) base += 1
        var adjustedBase = base.toFloat()
        adjustedBase *= getPercentASBSReductionDueToDeficits()
        val finalBase = ceil(adjustedBase).toInt()

        return Pair(finalBase, (base - finalBase))
    }

    fun getPercentASBSReductionDueToDeficits(): Float {
        val batteries = getBatteries() ?: return 1f
        val maxDeficit = batteries.getMaxDeficit(
            Commodities.SUPPLIES,
            Commodities.HAND_WEAPONS
        )
        if (maxDeficit.one == null) return 1f
        val maxDemand = batteries.getDemand(maxDeficit.one)
        val beingSupplied = maxDemand.quantity.modified - maxDeficit.two
        val deficitRatio = (beingSupplied / maxDemand.quantity.modified)

        return deficitRatio
    }

    override fun getBlueprintValue(): Int {
        return 20000
    }

    override fun getBasicDescription(tooltip: TooltipMakerAPI, expanded: Boolean, panel: CustomPanelAPI?) {
        super.getBasicDescription(tooltip, expanded, panel)

        tooltip.addPara(
            "Deploys a number of %s around the station that coordinate fire from planetary ground defenses into orbit.",
            5f,
            Misc.getHighlightColor(),
            "ASB uplinks"
        )
        tooltip.addPara(
            "The uplinks are placed %s, are %s by the station's side. They %s by the opposing side, however.",
            5f,
            Misc.getHighlightColor(),
            "behind the station",
            "owned",
            "can be captured"
        )
        tooltip.addPara(
            "The number of uplinks is dependent on the effectiveness of the market's %s. %s provide more, as does an %s installation.",
            5f,
            Misc.getHighlightColor(),
            "ground defenses", "Heavy batteries", "improved"
        ).setHighlightColors(
            Misc.getHighlightColor(),
            Misc.getHighlightColor(),
            Misc.getStoryOptionColor()
        )
        if (market != null && applied) {
            tooltip.addSectionHeading("Effectiveness", Alignment.MID, 5f)
            tooltip.setBulletedListMode(BaseIntelPlugin.BULLET)
            val batteries = getBatteries()
            if (batteries == null) {
                tooltip.addPara("No ground defenses are present.", 5f).color = Misc.getNegativeHighlightColor()
            } else {
                tooltip.addPara("Currently coordinating %s.", 5f, Misc.getPositiveHighlightColor(), batteries.currentName)
                val numASBs = getNumASBSToPlace()
                val reduction = numASBs.second
                tooltip.addPara("Managing %s ASB uplinks", 5f, Misc.getPositiveHighlightColor(), numASBs.first.toString())
                if (reduction > 0) {
                    tooltip.addPara(
                        "Reduced by %s due to %s",
                        5f,
                        Misc.getNegativeHighlightColor(),
                        "$reduction",
                        "supply deficits"
                    )
                }
            }
            tooltip.setBulletedListMode(null)
        }

        tooltip.addSpacer(5f)
        tooltip.addPara("Cannot be removed if ground defenses are currently disrupted.", 5f).color = Misc.getGrayColor()
        tooltip.addPara(
            "Somewhat underpowered for it's AP cost if using only ground defenses, but becomes very powerful with %s.",
            5f,
            Misc.getHighlightColor(),
            "improved heavy batteries"
        ).color = Misc.getGrayColor()
    }

    override fun modifyAutoresolveForOurFleet(data: BattleAutoresolverPluginImpl.FleetAutoresolveData) {
        val ourMember = data.members.firstOrNull { it.member.isStation } ?: return
        var ourMult = apToMemberStrengthMult
        val numAsbs = getNumASBSToPlace().first
        val asbRatio = (numAsbs / AUTORESOLVE_ASB_NUM_ANCHOR)
        ourMult *= asbRatio

        val ap = getAugmentCost()
        val strength = (ap * ourMult)
        ourMember.strength += strength
    }

    class DelayedASBPlatformAdd(
        val asbs: MutableSet<BattleObjective>,
        val station: ShipAPI
    ): BaseEveryFrameCombatPlugin() {

        override fun advance(amount: Float, events: List<InputEventAPI?>?) {
            super.advance(amount, events)

            val engine = Global.getCombatEngine()
            var no_enemy_ships = true

            for (ship in engine.ships) {
                if (ship.owner == 1 && !ship.hullSpec.hasTag("sotf_reinforcementship") && !ship.hullSpec.hasTag("sotf_empl") && !ship.isHulk && !ship.isFighter
                ) {
                    no_enemy_ships = false
                }
            }

            if (!no_enemy_ships) {
                finalizeDeployment(asbs, station, station.owner, station.owner == 0)
                engine.removePlugin(this)
                return
            }
        }

        private fun finalizeDeployment(
            objectives: MutableSet<BattleObjective>,
            station: ShipAPI,
            owner: Int,
            playerside: Boolean
        ) {
            for (objective in objectives) {
                val existingTime = ReflectionUtils.get("capTime", objective, BattleObjective::class.java) as Float
                //val newTime = existingTime * 2f
                //ReflectionUtils.set("capTime", objective, newTime, BattleObjective::class.java)
                ReflectionUtils.set("capProgress", objective, existingTime + 1, BattleObjective::class.java)
                ReflectionUtils.set("capOwner", objective, owner, BattleObjective::class.java)
                ReflectionUtils.invoke("advance", objective, 1f)
            }
        }
    }
}