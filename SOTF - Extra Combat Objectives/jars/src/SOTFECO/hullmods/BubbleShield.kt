package SOTFECO.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class BubbleShield: BaseHullMod() {

    companion object {
        const val SHIELD_STRENGTH = 50000f
        const val CONSTANT_DISSIPATION = 250f
        const val UNFOLD_RATE_MULT = 3f

        const val OVERLOAD_DURATION_MULT = 3f
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (ship == null) return

        Global.getCombatEngine().addPlugin(DelayedBubbleShield(ship, id))
        //module.collisionClass = CollisionClass.PROJECTILE_FIGHTER

    }

    class DelayedBubbleShield(val ship: ShipAPI, val id: String?) : BaseEveryFrameCombatPlugin() {
        override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
            super.advance(amount, events)

            if (!ship.isAlive) {
                Global.getCombatEngine().removePlugin(this)
                return
            }

            val engine = Global.getCombatEngine()
            val fleetManager = engine.getFleetManager(ship.owner)
            fleetManager.isSuppressDeploymentMessages = true
            val shieldDrone = fleetManager.spawnShipOrWing("SOTFECO_shieldDrone_shield", Vector2f(ship.location), 0f)
            shieldDrone.collisionClass = CollisionClass.FIGHTER
            shieldDrone.hullSize = ShipAPI.HullSize.FIGHTER
            //shieldDrone.spriteAPI.alphaMult = 0f
            //shieldDrone.extraAlphaMult2 = 0f // invisible
            shieldDrone.isAlly = ship.isAlly
            shieldDrone.isHoldFire = true
            fleetManager.isSuppressDeploymentMessages = false

            shieldDrone.addListener(BubbleShieldDamageNullifier())

            shieldDrone.setShield(ShieldAPI.ShieldType.OMNI, 0f, 1f, 360f)
            shieldDrone.mutableStats.shieldUnfoldRateMult.modifyFlat(id, 1f)
            shieldDrone.mutableStats.fluxCapacity.modifyFlat(id, SHIELD_STRENGTH)
            shieldDrone.mutableStats.fluxDissipation.modifyFlat(id, CONSTANT_DISSIPATION)
            shieldDrone.mutableStats.hullDamageTakenMult.modifyMult(id, 0f) // cant kill it
            shieldDrone.mutableStats.shieldUnfoldRateMult.modifyMult(id, UNFOLD_RATE_MULT)
            shieldDrone.mutableStats.overloadTimeMod.modifyMult(id, OVERLOAD_DURATION_MULT)
            shieldDrone.mutableStats.hardFluxDissipationFraction.modifyFlat(id, 1f)
            shieldDrone.mutableStats.engineDamageTakenMult.modifyMult(id, 0f)
            shieldDrone.mutableStats.dynamic.getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, 0f)
            shieldDrone.mutableStats.dynamic.getStat(Stats.SHIP_OBJECTIVE_CAP_RATE_MULT).modifyMult(id, 0f)
            shieldDrone.mutableStats.dynamic.getStat(Stats.SHIP_OBJECTIVE_CAP_RANGE_MOD).modifyMult(id, 0f)

            shieldDrone.activeLayers.remove(CombatEngineLayers.FF_INDICATORS_LAYER)
            shieldDrone.isRenderEngines = false

            ship.mutableStats.fluxCapacity.modifyFlat(id, SHIELD_STRENGTH)

            val shieldRadius = ship.collisionRadius * 25f

            shieldDrone.shield.radius = shieldRadius
            shieldDrone.collisionRadius = shieldRadius

            shieldDrone.aiFlags.setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON)

            Global.getCombatEngine().addPlugin(BubbleShieldLinker(shieldDrone, ship, shieldDrone.collisionRadius))
            Global.getCombatEngine().removePlugin(this)
        }
    }

    class BubbleShieldLinker(val fxDrone: ShipAPI, val station: ShipAPI, val origRadius: Float) : BaseEveryFrameCombatPlugin() {
        companion object {
            const val JITTER_STRENGTH_MULT = 1f
            const val MAX_JITTER_RANGE = 100f
        }

        override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
            super.advance(amount, events)
            if (Global.getCombatEngine().isPaused) return

            //station.fluxTracker.currFlux = fxDrone.fluxTracker.currFlux

            if (fxDrone.fluxTracker.isOverloaded) {
                if (!station.fluxTracker.isOverloaded) {
                    station.fluxTracker.forceOverload(fxDrone.fluxTracker.overloadTimeRemaining)
                }
                fxDrone.mutableStats.fluxDissipation.modifyMult("bubbleShieldOverloadDissipation", 500f)
                fxDrone.collisionRadius = 0f
            } else {
                fxDrone.mutableStats.fluxDissipation.unmodify("bubbleShieldOverloadDissipation")
                fxDrone.shield.toggleOn()
                //station.fluxTracker.stopOverload()
            }

            fxDrone.location.set(station.location.x, station.location.y) // idk why but its super hesitant to do this otherwise, even with the ai flag
            /*if (fxDrone.shield.isOff) {
                fxDrone.shield.toggleOn()
            }*/

            val fluxUsed = fxDrone.fluxTracker.fluxLevel
            /*val jitterIntensity = (fluxUsed * JITTER_STRENGTH_MULT)
            if (jitterIntensity > 0f) {
                val jitterRange = (MAX_JITTER_RANGE * fluxUsed)

                fxDrone.isJitterShields = true
                fxDrone.setJitter("bubbleShieldJitter", fxDrone.shield.innerColor, jitterIntensity, 1, jitterRange)
            }*/

            fxDrone.extraAlphaMult2 = (1f - (fxDrone.fluxTracker.fluxLevel * 0.9f))

            if (station.isHulk || !station.isAlive) {
                fxDrone.mutableStats.hullDamageTakenMult.unmodify()
                Global.getCombatEngine().removeEntity(fxDrone)
                Global.getCombatEngine().removePlugin(this)
            }
        }
    }

    class BubbleShieldDamageNullifier(): DamageTakenModifier {
        override fun modifyDamageTaken(
            param: Any?,
            target: CombatEntityAPI?,
            damage: DamageAPI?,
            point: Vector2f?,
            shieldHit: Boolean
        ): String? {
            if (param !is DamagingProjectileAPI) return null
            if (!shieldHit) return null

            if (param.projectileSpec?.isPassThroughFighters == true) {
                Global.getCombatEngine().removeEntity(param)
            }

            return null
        }
    }

}