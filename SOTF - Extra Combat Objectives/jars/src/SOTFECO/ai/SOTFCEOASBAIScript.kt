package SOTFECO.ai

import SOTFECO.SOTFCEOASBScript
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipSystemAIScript
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow

class SOTFCEOASBAIScript: ShipSystemAIScript {

    companion object {
        const val SCORE_NEEDED_FOR_PICK = 70f

        const val SPEED_ANCHOR = 60f // anything below this gets a boost to being picked
    }

    lateinit var ship: ShipAPI
    lateinit var system: ShipSystemAPI
    lateinit var engine: CombatEngineAPI

    val checkTimer = IntervalUtil(1.8f, 2.3f) // seconds

    override fun init(
        ship: ShipAPI?,
        system: ShipSystemAPI?,
        flags: ShipwideAIFlags?,
        engine: CombatEngineAPI?
    ) {
        if (ship == null || system == null || engine == null) return
        this.ship = ship
        this.system = system
        this.engine = engine
    }

    override fun advance(
        amount: Float,
        missileDangerDir: Vector2f?,
        collisionDangerDir: Vector2f?,
        target: ShipAPI?
    ) {

        checkTimer.advance(amount)
        if (checkTimer.intervalElapsed()) {
            tryActivating()
        }
    }

    private fun tryActivating() {
        if (!system.canBeActivated() || system.isCoolingDown || system.isActive || ship.fluxTracker.isOverloaded) return

        val validTargets = getValidTargets()
        val target = pickTargetFrom(validTargets)

        if (target != null) {
            ship.aiFlags.setFlag(ShipwideAIFlags.AIFlags.TARGET_FOR_SHIP_SYSTEM, 0f, target)
            ship.useSystem()
        }
    }

    private fun pickTargetFrom(validTargets: MutableSet<ShipAPI>): ShipAPI? {
        val picker = WeightedRandomPicker<ShipAPI>()
        for (target in validTargets) {
            var favorPlayer = true
            val flags = target.aiFlags
            var score = 0f
            when (target.hullSize) {
                HullSize.FRIGATE, HullSize.FIGHTER -> score += 30f
                HullSize.DESTROYER -> score += 30f
                HullSize.CRUISER -> score += 40f
                HullSize.CAPITAL_SHIP -> score += 60f
                else -> continue
            }
            val allyOrderManager = engine.getFleetManager(ship.owner).getTaskManager(ship.isAlly)
            val playerOrderManager = engine.getFleetManager(0).getTaskManager(false)
            val allOrders = if (ship.owner == 0) (allyOrderManager.allAssignments + playerOrderManager.allAssignments) else allyOrderManager.allAssignments
            for (order in allOrders) {
                if (order.type == CombatAssignmentType.INTERCEPT && order.target == target) {
                    score += 60f
                }
            }

            // MULTS
            if (target.isStationModule) {
                score *= 0.5f // target the parent instead stinky
            }
            val maxMovementSpeed = max(target.acceleration * 0.7f, target.deceleration)
            if (maxMovementSpeed == 0f) {
                score += 999f // DIE
            } else {
                val movementMult = (SPEED_ANCHOR / (maxMovementSpeed.coerceAtMost(SPEED_ANCHOR))).coerceAtMost(3f)
                score *= movementMult
            }

            val engaged = flags.hasFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE)
            if (engaged) {
                score *= 2f
                if (flags.hasFlag(ShipwideAIFlags.AIFlags.NEEDS_HELP)) {
                    score *= 6f // FINISH THEM OFF
                }
            }
            if (target.shield == null) {
                if (target.phaseCloak == null || target.phaseCloak.id != "phase_cloak") {
                    score *= 1.5f // cant avoid easily
                } else {
                    score *= 0.5f // it can just phase...
                    favorPlayer = false
                }
            }

            if (favorPlayer && engine.playerShip == target) {
                score *= 1.1f // to be annoying
            }

            score *= MathUtils.getRandomNumberInRange(0.8f, 1.2f)
            if (score > SCORE_NEEDED_FOR_PICK) {
                picker.add(target, (score.pow(2.5f))) // if something is highly likely to get picked, it gets an extra boost
            }
        }

        if (!picker.isEmpty) {
            return picker.pick()
        }
        return validTargets.randomOrNull() // we're still gonna shoot *something*
    }

    private fun getValidTargets(): MutableSet<ShipAPI> {
        val list = HashSet<ShipAPI>()
        val iterator = engine.shipGrid.getCheckIterator(ship.location, SOTFCEOASBScript.RANGE, SOTFCEOASBScript.RANGE)
        while (iterator.hasNext()) {
            val possibleTarget = iterator.next() as? ShipAPI ?: continue
            if (possibleTarget.owner == ship.owner) continue
            if (possibleTarget.isFighter && !possibleTarget.hasTag("fake_fighter")) continue
            if (!possibleTarget.isHullDamageable()) continue

            list += possibleTarget
        }
        return list
    }

    fun ShipAPI.isHullDamageable(): Boolean {
        return !(this.mutableStats.hullDamageTakenMult.getModifiedValue() <= 0.0f)
    }
}