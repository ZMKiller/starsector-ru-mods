package SOTFECO

import SOTFECO.SOTFECO_MathUtils.roundNumTo
import SOTFECO.SOTFECO_MathUtils.trimHangingZero
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.threat.EnergyLashActivatedSystem
import com.fs.starfarer.api.impl.combat.threat.EnergyLashSystemScript
import com.fs.starfarer.api.impl.combat.threat.ThreatSwarmAI
import com.fs.starfarer.api.impl.combat.threat.VoltaicDischargeOnFireEffect
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.Misc

class SOTFCEOEnergyInfusionSystemScript: EnergyLashSystemScript() {

    companion object {
        const val SPEED_BONUS_PERCENT: Float = 50f
        const val FLUX_DISSIPATION_MULT: Float = 1.5f
        const val DAMAGE_TAKEN_PERCENT: Float = -10f

         fun setStandardJitterExternal(ship: ShipAPI, state: ShipSystemStatsScript.State?, effectLevel: Float) {
            if (ship.isHulk) return

            var jitterLevel = effectLevel
            jitterLevel = 0.5f + 0.5f * jitterLevel
            if (state == ShipSystemStatsScript.State.OUT) {
                jitterLevel *= jitterLevel
            }
            val base = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR
            val overColor = Misc.setAlpha(base, 255)

            ship.setJitter(this, overColor, jitterLevel, 1, 0f, 4f)
            ship.isJitterShields = false
            ship.setCircularJitter(true)
        }
    }

    override fun applyEffectToTarget(ship: ShipAPI?, target: ShipAPI?) {
        val isSwarm = ThreatSwarmAI.isAttackSwarm(target)
        if (!isSwarm) {
            if (target == null || target.system == null || target.isHulk) return
        }
        if (ship == null || ship.system == null || ship.isHulk) return

        if (ship.owner == target!!.owner) {

            var cooldown = target.hullSpec.suppliesToRecover

            //float cooldown = target.getMutableStats().getSuppliesToRecover().getBaseValue();
            //cooldown = (int)Math.round(target.getMutableStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).computeEffective(cooldown));
            cooldown = MIN_COOLDOWN + cooldown * COOLDOWN_DP_MULT
            if (cooldown > MAX_COOLDOWN) cooldown = MAX_COOLDOWN
            if (target.isFighter) cooldown = MIN_COOLDOWN
            //			ship.getSystem().setCooldown(cooldown);
//			ship.getSystem().setCooldownRemaining(cooldown);

            if (target.system != null && target.system.script is EnergyLashActivatedSystem) {
                val script = target.system.script as EnergyLashActivatedSystem
                script.hitWithEnergyLash(ship, target)
            } else if (isSwarm) {
                VoltaicDischargeOnFireEffect.setSwarmPhaseMode(target)
                sinceSwarmTargeted = 0f
            } else {
                Global.getCombatEngine().addPlugin(SOTFCEOEnergyInfusionScript(
                    target,
                    ship,
                    6f // seconds
                ))
                Global.getSoundPlayer().playSound(
                    "system_incursion_mode",
                    1.4f,
                    0.6f,
                    target.location,
                    Misc.ZERO
                )
            }

            cooldownToSet = cooldown
        } else {
            var hitPhase = false
            if (target.isPhased) {
                target.overloadColor = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR_BRIGHT
                target.fluxTracker.beginOverloadWithTotalBaseDuration(PHASE_OVERLOAD_DUR)
                if (target.fluxTracker.showFloaty() || ship == Global.getCombatEngine().playerShip || target == Global.getCombatEngine().playerShip) {
                    target.fluxTracker.playOverloadSound()
                    target.fluxTracker.showOverloadFloatyIfNeeded(
                        "Phase Field Disruption!",
                        VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR, 4f, true
                    )
                }

                Global.getCombatEngine().addPlugin(object : BaseEveryFrameCombatPlugin() {
                    override fun advance(amount: Float, events: MutableList<InputEventAPI?>?) {
                        if (!target.fluxTracker.isOverloadedOrVenting) {
                            target.resetOverloadColor()
                            Global.getCombatEngine().removePlugin(this)
                        }
                    }
                })

                hitPhase = true
            }

            var cooldown = MIN_HIT_ENEMY_COOLDOWN +
                    (MAX_HIT_ENEMY_COOLDOWN - MIN_HIT_ENEMY_COOLDOWN) * Math.random().toFloat()
            if (hitPhase) {
                cooldown *= HIT_PHASE_ENEMY_COOLDOWN_MULT
            }
            if (cooldown > MAX_COOLDOWN) cooldown = MAX_COOLDOWN


            //			ship.getSystem().setCooldown(cooldown);
//			ship.getSystem().setCooldownRemaining(cooldown);
            cooldownToSet = cooldown
        }
    }

    class SOTFCEOEnergyInfusionScript(
        val target: ShipAPI,
        val source: ShipAPI,
        val duration: Float
    ): BaseEveryFrameCombatPlugin() {
        var timeActive: Float = 0f
        var rampUpTime = (duration * 0.1f).coerceAtMost(0.2f)
        val rampDownTime = (duration * 0.1f).coerceAtMost(0.5f)
        var rampDownLeft = rampDownTime
        var rampingDown = false

        companion object {
            const val id = "SOTFCEO_energyInfusion"

            val INFOKEY_1 = "SOTFCEO_energyInfusionInfoOne"
            val INFOKEY_2 = "SOTFCEO_energyInfusionInfoTwo"
            val INFOKEY_3 = "SOTFCEO_energyInfusionInfoThree"
        }

        override fun advance(amount: Float, events: List<InputEventAPI?>?) {
            super.advance(amount, events)

            val engine = Global.getCombatEngine()
            if (engine.isPaused) return
            if (target.isHulk || !target.isAlive) {
                unapply()
                return
            }

            timeActive += amount
            if (timeActive >= duration) {
                rampingDown = true
            }
            var level = (timeActive / rampUpTime).coerceAtMost(1f)
            if (rampingDown) {
                rampDownLeft -= amount
                if (rampDownLeft <= 0f) {
                    unapply()
                    return
                }
                level = (rampDownTime / rampDownLeft).coerceAtMost(1f)
            }

            apply(level)
        }

        fun apply(level: Float) {
            val stats = target.mutableStats

            stats.maxSpeed.modifyPercent(id, SPEED_BONUS_PERCENT * level)
            stats.acceleration.modifyPercent(id, SPEED_BONUS_PERCENT * level)
            stats.deceleration.modifyPercent(id, SPEED_BONUS_PERCENT * level)

            //stats.getMissileAmmoRegenMult().modifyMult(id, AMMO_REGEN_MULT * effectLevel);
            stats.fluxDissipation.modifyMult(id,FLUX_DISSIPATION_MULT * level)

            stats.armorDamageTakenMult.modifyPercent(id, DAMAGE_TAKEN_PERCENT * level)
            stats.hullDamageTakenMult.modifyPercent(id, DAMAGE_TAKEN_PERCENT * level)
            stats.shieldDamageTakenMult.modifyPercent(id, DAMAGE_TAKEN_PERCENT * level)

            target.engineController.extendFlame(target.system, 1f * level, 0f * level, 0.5f * level)

            val state = if (rampingDown) ShipSystemStatsScript.State.OUT else if (level != 1f) ShipSystemStatsScript.State.IN else ShipSystemStatsScript.State.ACTIVE
            setStandardJitterExternal(
                target,
                state,
                level
            )

            handleNotif(level)
        }

        private fun handleNotif(level: Float) {
            if (target != Global.getCombatEngine().playerShip) return

            val engine = Global.getCombatEngine()
            engine.maintainStatusForPlayerShip(
                INFOKEY_1,
                "graphics/icons/hullsys/incursion_mode.png",
                "Energy Infusion",
                "Engine performance enhanced",
                false
            )
            engine.maintainStatusForPlayerShip(
                INFOKEY_2,
                "graphics/icons/hullsys/incursion_mode.png",
                "Energy Infusion",
                "${(FLUX_DISSIPATION_MULT * level).roundNumTo(2).trimHangingZero()}x flux dissipation",
                false
            )
            engine.maintainStatusForPlayerShip(
                INFOKEY_3,
                "graphics/icons/hullsys/incursion_mode.png",
                "Energy Infusion",
                "${(-DAMAGE_TAKEN_PERCENT * level).trimHangingZero()}% less damage taken",
                false
            )
        }

        fun unapply() {
            val stats = target.mutableStats
            stats.maxSpeed.unmodify(id)
            stats.acceleration.unmodify(id)
            stats.deceleration.unmodify(id)
            stats.fluxDissipation.unmodify(id)
            stats.armorDamageTakenMult.unmodify(id)
            stats.hullDamageTakenMult.unmodify(id)
            stats.shieldDamageTakenMult.unmodify(id)

            Global.getCombatEngine().removePlugin(this)
        }
    }

    override fun isValidLashTarget(ship: ShipAPI?, other: ShipAPI?): Boolean {
        if (super.isValidLashTarget(ship, other)) return true

        return (ship?.owner == other?.owner)
    }
}