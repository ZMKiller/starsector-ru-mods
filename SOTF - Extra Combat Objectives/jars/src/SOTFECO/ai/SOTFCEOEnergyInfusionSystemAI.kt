package SOTFECO.ai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.impl.combat.threat.EnergyLashActivatedSystem
import com.fs.starfarer.api.impl.combat.threat.EnergyLashSystemAI
import com.fs.starfarer.api.impl.combat.threat.EnergyLashSystemScript
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.impl.combat.threat.ThreatSwarmAI
import com.fs.starfarer.api.impl.combat.threat.VoltaicDischargeOnFireEffect
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import kotlin.math.max

class SOTFCEOEnergyInfusionSystemAI: EnergyLashSystemAI() {

    override fun getWeightedTargets(shipTarget: ShipAPI?): WeightedRandomPicker<ShipAPI?> {
        val picker = WeightedRandomPicker<ShipAPI?>()

        for (other in getPossibleTargets()) {
            var w = 0f
            if (ship.owner == other.owner) {
                if (ThreatSwarmAI.isAttackSwarm(other)) {
                    val swarm = RoilingSwarmEffect.getSwarmFor(other)
                    if (swarm != null && !VoltaicDischargeOnFireEffect.isSwarmPhaseMode(other)) {
                        w = 0.5f
                    } else {
                        continue
                    }
                } else {
                    if (other.system != null && other.system.script is EnergyLashActivatedSystem) {
                        if (other.system.cooldownRemaining > 0) continue
                        if (other.system.isActive) continue
                        if (other.fluxTracker.isOverloadedOrVenting) continue

                        val otherSystem = other.system.script as EnergyLashActivatedSystem
                        w = otherSystem.getCurrentUsefulnessLevel(ship, other)
                    } else {
                        w = (other.hullSpec.suppliesToRecover / 25f).coerceAtMost(0.25f)
                        if (other.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.NEEDS_HELP)) {
                            w *= 0.45f
                        } else if (other.aiFlags.hasFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE)) { // engaged
                            w += 0.2f
                        }
                    }

                }
            } else {
                val targetShield = other.shield
                val targetShieldsFacingUs = targetShield != null &&
                        targetShield.isOn &&
                        Misc.isInArc(
                            targetShield.facing, max(30.0, targetShield.activeArc.toDouble()).toFloat(),
                            other.location, ship.location
                        )
                if (targetShieldsFacingUs && EnergyLashSystemScript.DAMAGE <= 0) continue

                var dist = Misc.getDistance(ship.location, other.location)
                dist -= ship.collisionRadius + other.collisionRadius
                if (dist < 0) dist = 0f
                if (other == shipTarget) {
                    w += 0.25f
                }
                if (dist < 1000f) {
                    w += (1f - (dist / 1000f)) * 0.5f
                }
                if (other.isPhased) {
                    w += 0.5f
                }
                w += 0.01f
            }
            picker.add(other, w)
        }
        return picker
    }

}