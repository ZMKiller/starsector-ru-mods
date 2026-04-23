package SOTFECO

import SOTFECO.SOTFECO_settings.DO_PLAYER_ASB_WARNING
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import data.scripts.combat.SotfASBLockOnScript
import org.apache.log4j.Priority
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f

abstract class SOTFCEOASBScript : BaseShipSystemScript() {

    companion object {
        const val RANGE = 10000f
        const val CD_KEY = "SOTFCEO_ASBLocking"
        const val MARK_KEY = "\$SOTFCEO_ASBMark"

        const val THREAT_INDICATOR_WEAPON_ID = "SOTFECO_ASBIndicator"
    }

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        super.apply(stats, id, state, effectLevel)

        if (stats == null || id == null) return

        val ship = stats.entity as? ShipAPI ?: return
        val target = findTarget(ship) ?: return
        val moduleCore = target.parentStation ?: target

        val params = SotfASBLockOnScript.ASBParams(
            ship,
            ship.owner,
            target,
            MARK_KEY
        )
        if (DO_PLAYER_ASB_WARNING && target.owner == 0 && !target.isAlly && !target.isFighter) {
        //if (target.isAlly) {
            if (moduleCore.name != null) {
                Global.getCombatEngine().combatUI.addMessage(
                    0,
                    moduleCore,
                    Misc.getPositiveHighlightColor(),
                    moduleCore.name,
                    Misc.getNegativeHighlightColor(),
                    " TARGETED BY ${getTypeString()} ASB!"
                )
            }

            Global.getSoundPlayer().playUISound("SOTFECOASBTargetWarning", 0.6f, 0.4f)
        }
        Global.getCombatEngine().addFloatingText(
            Vector2f(target.shieldCenterEvenIfNoShield.x,
            target.shieldCenterEvenIfNoShield.y + target.shieldRadiusEvenIfNoShield + 50f),
            "-ASB LOCK DETECTED: ${getTypeString()}-",
            60f,
            getRingColor(),
            target,
            1f,
            0.2f
        )
        params.lockedColor = getRingColor()
        params.weaponId = getWeaponId()
        params.chargeTime = 12f // pretty long
        val plugin = SotfASBLockOnScript(params)
        Global.getCombatEngine().addPlugin(
            plugin
        )

        Global.getCombatEngine().addPlugin(
            SOTFCEOASBWarnScript(target, plugin)
        )
        ship.system.deactivate() // i hope this works lol

    }
    override fun getInfoText(system: ShipSystemAPI, ship: ShipAPI): String? {
        if (system.isOutOfAmmo) return null
        if (system.state != SystemState.IDLE) return null

        val target: ShipAPI? = findTarget(ship)
        if (target != null && target != ship) {
            return "READY"
        }
        if ((target == null) && ship.shipTarget != null) {
            return "OUT OF RANGE"
        }
        return "NO TARGET"
    }

    protected fun findTarget(ship: ShipAPI): ShipAPI? {
        val range = getMaxRange(ship)
        val player = ship == Global.getCombatEngine().playerShip
        var target = ship.shipTarget

        if (ship.shipAI != null && ship.aiFlags.hasFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)) {
            val custom = ship.aiFlags.getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM)
            if (custom is ShipAPI) {
                target = ship.aiFlags.getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM) as? ShipAPI
            }
        }

        if (target != null) {
            val dist = Misc.getDistance(ship.location, target.location)
            val radSum = ship.collisionRadius + target.collisionRadius
            if (dist > range + radSum) target = null
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.mouseTarget, HullSize.FRIGATE, range, true)
            } else {
                val test = ship.aiFlags.getCustom(AIFlags.MANEUVER_TARGET)
                if (test is ShipAPI) {
                    target = test
                    val dist = Misc.getDistance(ship.location, target.location)
                    val radSum = ship.collisionRadius + target.collisionRadius
                    if (dist > range + radSum || target.isFighter) target = null
                }
            }
        }

        if (target != null && target.isFighter) target = null
        if (target == null) {
            target = Misc.findClosestShipEnemyOf(ship, ship.location, HullSize.FRIGATE, range, true)
        }

        if (target?.isAlive != true) return null

        return target
    }


    fun getMaxRange(ship: ShipAPI): Float {
        return ship.mutableStats.systemRangeBonus.computeEffective(RANGE)
    }


    /*override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float): StatusData? {
        if (effectLevel > 0) {
            if (index == 0) {
                val damMult = 1f + (EntropyAmplifierStats.DAM_MULT - 1f) * effectLevel
                return StatusData("" + ((damMult - 1f) * 100f).toInt() + "% more damage to target", false)
            }
        }
        return null
    }*/


    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI): Boolean {
        //if (true) return true;
        val target: ShipAPI? = findTarget(ship)
        return target != null && target != ship
    }

    abstract fun getWeaponId(): String
    abstract fun getRingColor(): java.awt.Color?
    abstract fun getTypeString(): String

    class SOTFCEOASBWarnScript(
        val target: ShipAPI,
        val ASBScript: SotfASBLockOnScript
    ): BaseEveryFrameCombatPlugin() {
        var threatIndicator: CombatEntityAPI? = null
        val engine = Global.getCombatEngine()

        val repositionInterval = IntervalUtil(0.1f, 0.1f)
        val expiredInterval = IntervalUtil(3f, 3f)

        override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
            super.advance(amount, events)

            if (!ASBScript.fired) return

            if (threatIndicator == null) {
                threatIndicator = createThreatIndicator()
            }
            repositionThreatIndicator(amount)
            expiredInterval.advance(amount)
            if (expiredInterval.intervalElapsed()) {
                deleteThreatIndicator()
                engine.removePlugin(this)
            }
        }

        private fun repositionThreatIndicator(amount: Float) {
            repositionInterval.advance(amount)
            if (repositionInterval.intervalElapsed()) {
                deleteThreatIndicator()
                threatIndicator = createThreatIndicator()
            }
        }

        private fun deleteThreatIndicator() {
            if (threatIndicator != null) {
                threatIndicator!!.hitpoints = 0.00001f
                engine.removeEntity(threatIndicator)
            }
        }

        fun createThreatIndicator(): CombatEntityAPI {
            val threatIndicator = engine.spawnProjectile(
                null,
                null,
                THREAT_INDICATOR_WEAPON_ID,
                getThreatIndicatorPlacement(),
                0f,
                null
            )
            if (threatIndicator is MissileAPI) {
                threatIndicator.untilMineExplosion = (ASBScript.p.chargeTime - ASBScript.progress).coerceAtLeast(repositionInterval.intervalDuration + 0.2f)
                //threatIndicator.damageAmount = HYPERSTORM_ENERGY_DAMAGE
                val wpnSpec = Global.getSettings().getWeaponSpec(ASBScript.p.weaponId)
                val projSpec = wpnSpec.projectileSpec as? ProjectileSpecAPI
                threatIndicator.damage.damage = projSpec?.damage?.damage ?: 4000f // cant seem to get it from spec for SOME reason
                threatIndicator.damage.type = wpnSpec?.damageType
                /*threatIndicator.collisionClass = CollisionClass.FIGHTER
                threatIndicator.hitpoints = 9999999f*/
            }
            threatIndicator.owner = 100
            return threatIndicator
        }

        private fun getThreatIndicatorPlacement(): Vector2f {
            val targetLocation = target.location
            val direction = VectorUtils.getDirectionalVector(targetLocation, ASBScript.firingLocation)
            val directionToReturn = Vector2f(targetLocation.x + (direction.x*50), targetLocation.y + (direction.y*50))

            return directionToReturn
        }
    }

}