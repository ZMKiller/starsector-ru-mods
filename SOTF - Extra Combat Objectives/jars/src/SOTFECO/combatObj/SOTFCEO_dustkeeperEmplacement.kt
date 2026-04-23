package SOTFECO.combatObj

import SOTFECO.ReflectionUtils
import SOTFECO.scripts.SOTFECO_flagScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.entities.BattleObjective
import data.scripts.combat.obj.SotfEmplacementEffect

class SOTFCEO_dustkeeperEmplacement: SotfEmplacementEffect() {

    var betrayed = false
    var activatedForPlayer = false

    companion object {
        fun announceBetrayal() {
            if (Global.getCombatEngine().customData["\$SOTFCEO_emplacementsBetrayed"] == true) return

            val defectString = "Alert: Dustkeeper emplacements activating and locking out controls"
            Global.getCombatEngine().combatUI.addMessage(
                1,
                Misc.getNegativeHighlightColor(), defectString
            )

            Global.getCombatEngine().customData["\$SOTFCEO_emplacementsBetrayed"] = true
        }

        fun announceActivation() {
            if (Global.getCombatEngine().customData["\$SOTFCEO_PROXemplacementsActivated"] == true) return

            val defectString = "Alert: Dustkeeper defensive measures activating"
            Global.getCombatEngine().combatUI.addMessage(
                1,
                Misc.getPositiveHighlightColor(), defectString
            )

            Global.getCombatEngine().customData["\$SOTFCEO_PROXemplacementsActivated"] = true
        }
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        val engine = Global.getCombatEngine()
        if (engine.isPaused) return

        var noEnemyShips = true
        for (ship in engine.ships) {
            if (ship.owner == 1 && !ship.hullSpec.hasTag("sotf_reinforcementship") && !ship.hullSpec
                    .hasTag("sotf_empl") && !ship.isHulk && !ship.isFighter
            ) {
                noEnemyShips = false
                break
            }
        }

        if (noEnemyShips) return

        if (!betrayed && SOTFECO_flagScript.dustKeepersHostile()) {
            if (objective.owner != 1) {
                announceBetrayal()

                val battleObjective: BattleObjective = objective as? BattleObjective ?: return

                ReflectionUtils.set("capProgress", battleObjective, 1001, BattleObjective::class.java)
                ReflectionUtils.set("capTime", battleObjective, 1000, BattleObjective::class.java)
                ReflectionUtils.set("capOwner", battleObjective, 1, BattleObjective::class.java)

                betrayed = true
            }
        }

        if (betrayed) {
            val allyManager = engine.getFleetManager(0).getTaskManager(true)
            for (assignment in allyManager.allAssignments.toMutableSet()) {
                if (assignment.target != objective) continue
                if (assignment.type == CombatAssignmentType.CAPTURE || assignment.type == CombatAssignmentType.ASSAULT) {
                    allyManager.removeAssignment(assignment)
                }
            }
        } else if (!activatedForPlayer && SOTFECO_flagScript.inProxyLocation()) {
            // these are technically owned by the player to begin with
            announceActivation()

            val battleObjective: BattleObjective = objective as? BattleObjective ?: return

            ReflectionUtils.set("capProgress", battleObjective, 1001, BattleObjective::class.java)
            ReflectionUtils.set("capOwner", battleObjective, 0, BattleObjective::class.java)

            activatedForPlayer = true
        }
    }
}