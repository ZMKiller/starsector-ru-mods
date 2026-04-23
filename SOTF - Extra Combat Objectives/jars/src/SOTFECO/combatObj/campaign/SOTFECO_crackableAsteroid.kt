package SOTFECO.combatObj.campaign

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.BattleObjectiveAPI
import com.fs.starfarer.api.combat.BattleObjectiveEffect
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicRender
import java.awt.Color
import java.util.EnumSet
import kotlin.div

class SOTFECO_crackableAsteroid: BaseBattleObjectiveEffect() {

    companion object {
        const val MAX_BEEPS_TIL_BOOM = 4
        const val DEBRIS_COUNT = 7
    }

    var armed = false
    var armedBy = 100

    val beepTimer = IntervalUtil(0.25f, 0.25f)
    var timesBeeped = 0

    val bombPlugin = BombRenderingPlugin(this)

    class BombRenderingPlugin(val obj: SOTFECO_crackableAsteroid) : CombatLayeredRenderingPlugin {
        var bombOffsetX: Float? = null
        var bombOffsetY: Float? = null
        val bombAngle = MathUtils.getRandomNumberInRange(0f, 360f)
        var glowTime = 0f
        val bombSprite = Global.getSettings().getSprite("graphics/missiles/heavy_mine.png")
        val coreGlow = Global.getSettings().getSprite("campaignEntities", "abyssal_light_glow")

        override fun init(entity: CombatEntityAPI?) {
            bombSprite.setSize(bombSprite.width / 2f, bombSprite.height / 2f)

            bombOffsetX = MathUtils.getRandomNumberInRange(-obj.objective.sprite.width / 2, obj.objective.sprite.width / 2)
            bombOffsetY = MathUtils.getRandomNumberInRange(-obj.objective.sprite.height / 2, obj.objective.sprite.height / 2)
        }

        override fun cleanup() {
            return
        }

        override fun isExpired(): Boolean {
            return !Global.getCombatEngine().isEntityInPlay(obj.objective)
        }

        override fun advance(amount: Float) {
            val engine = Global.getCombatEngine()
            if (engine.isPaused) return

            glowTime = (glowTime - amount).coerceAtLeast(0f)

            return
        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers?>? {
            return EnumSet.of(CombatEngineLayers.UNDER_SHIPS_LAYER)
        }

        override fun getRenderRadius(): Float {
            return 10000f
        }

        override fun render(
            layer: CombatEngineLayers?,
            viewport: ViewportAPI?
        ) {
            if (layer == CombatEngineLayers.UNDER_SHIPS_LAYER) {
                if (!obj.armed) return
                val objLoc = obj.objective.location

                val x = objLoc.x + bombOffsetX!!
                val y = objLoc.y + bombOffsetY!!

                bombSprite.render(x, y)
                bombSprite.angle = bombAngle

                if (glowTime > 0f) {
                    MagicRender.singleframe(
                        coreGlow,
                        Vector2f(x, y),
                        Vector2f(bombSprite.width, bombSprite.height),
                        0f,
                        Color.RED,
                        true,
                        CombatEngineLayers.CAPITAL_SHIPS_LAYER,
                    )
                }
            }
        }

    }

    override fun advance(amount: Float) {
        val engine = Global.getCombatEngine()
        if (engine.isPaused) return

        if (!isCaptured()) return

        if (!armed) {
            armed = true
            armedBy = objective.owner
            engine.addLayeredRenderingPlugin(bombPlugin)
        }

        beepTimer.advance(amount)
        if (beepTimer.intervalElapsed()) {
            timesBeeped++

            Global.getSoundPlayer().playSound(
                "targeting_laser_burst",
                1f,
                1f,
                objective.location,
                Misc.ZERO
            )

            bombPlugin.glowTime = 0.1f
        }

        if (timesBeeped >= MAX_BEEPS_TIL_BOOM) {
            val spec = DamagingExplosionSpec(
                0.1f,
                50f,
                10f,
                250f,
                25f,
                CollisionClass.HITS_SHIPS_ONLY_FF,
                CollisionClass.HITS_SHIPS_ONLY_FF,
                1f,
                2f,
                5f,
                7,
                Color.RED,
                Color.RED
            )
            spec.damageType = DamageType.HIGH_EXPLOSIVE

            engine.spawnDamagingExplosion(
                spec,
                engine.playerShip ?: engine.ships.first(),
                objective.location
            )
            Global.getSoundPlayer().playSound(
                "gate_explosion_fleet_impact",
                1f,
                0.4f,
                objective.location,
                Misc.ZERO
            )

            var asteroidsToSpawn = DEBRIS_COUNT
            while (asteroidsToSpawn-- > 0) {

                val randTarget = MathUtils.getPointOnCircumference(objective.location, 50f, MathUtils.getRandomNumberInRange(0f, 360f))
                val vec = VectorUtils.getDirectionalVector(objective.location, randTarget)

                val asteroid = engine.spawnAsteroid(
                    MathUtils.getRandomNumberInRange(1, 2),
                    objective.location.x,
                    objective.location.y,
                    vec.x,
                    vec.y
                )

                Global.getCombatEngine().addPlugin(SOTFECO_collectAsteroidPlugin(asteroid))
            }

                /*if (armedBy == 0) {
                engine.combatUI.addMessage(
                    1,
                    Misc.getPositiveHighlightColor(),
                    objective.displayName,
                    Misc.getTextColor(),
                    " destroyed! ${Misc.getDGSCredits(credits)} added to account."
                )
            }*/

            engine.removeEntity(objective)
        }


    }

    fun isCaptured(): Boolean {
        return objective.owner == 0 || objective.owner == 1
    }

    override fun getStatusItemsFor(ship: ShipAPI?): List<BattleObjectiveEffect.ShipStatusItem?>? {
        return null
    }

    override fun getLongDescription(): String {
        return String.format(
            """
            - unusually large asteroid with extremely rich readings
            - requires a small period for a nearby ship to plant mining explosives
            - on destruction will provide the destroying side with precious minerals
            """.trimIndent()
        )
    }

    class SOTFECO_collectAsteroidPlugin(val asteroid: CombatEntityAPI): BaseEveryFrameCombatPlugin() {
        val ore = MathUtils.getRandomNumberInRange(250, 900)
        val transplutonic = MathUtils.getRandomNumberInRange(0, 500)
        val volatiles = MathUtils.getRandomNumberInRange(0, 50)

        val coreGlow = Global.getSettings().getSprite("campaignEntities", "abyssal_light_glow")

        override fun advance(amount: Float, events: List<InputEventAPI?>?) {
            super.advance(amount, events)

            val engine = Global.getCombatEngine()
            if (engine.isPaused) return

            MagicRender.singleframe(
                coreGlow,
                asteroid.location,
                Vector2f(asteroid.collisionRadius * 3f, asteroid.collisionRadius * 3f),
                0f,
                Color.YELLOW,
                true,
                CombatEngineLayers.CAPITAL_SHIPS_LAYER,
            )

            if (asteroid.isExpired || !engine.isEntityInPlay(asteroid)) {
                engine.removePlugin(this)
                return
            }

            val iterator = engine.shipGrid.getCheckIterator(asteroid.location, 300f, 300f)
            while (iterator.hasNext()) {
                val iter = iterator.next() as ShipAPI
                if (iter.isFighter || iter.isHulk) return
                val dist = MathUtils.getDistance(asteroid, iter)
                if (dist <= 10f) {
                    collectedBy(iter)
                    engine.removePlugin(this)
                    return
                }
            }

        }

        fun collectedBy(ship: ShipAPI) {
            val targetOwner = ship.owner
            if (targetOwner == 0) {
                Global.getSoundPlayer().playUISound(
                    "ui_cargo_metals_drop",
                    1f,
                    0.5f
                )

                Global.getCombatEngine().combatUI.addMessage(
                    0,
                    Misc.getPositiveHighlightColor(),
                    "Asteroid collected!",
                    Misc.getTextColor(),
                    " Gained $ore ore, $transplutonic transplutonics, and $volatiles volatiles."
                )
            }

            Global.getCombatEngine().addHitParticle(
                asteroid.location,
                asteroid.velocity,
                asteroid.collisionRadius * 6f,
                0.75f,
                3f,
                Color.WHITE
            )

            Global.getSector().playerFleet?.cargo?.addCommodity(Commodities.ORE, ore.toFloat())
            Global.getSector().playerFleet?.cargo?.addCommodity(Commodities.RARE_ORE, transplutonic.toFloat())
            Global.getSector().playerFleet?.cargo?.addCommodity(Commodities.VOLATILES, volatiles.toFloat())

            Global.getCombatEngine().removeEntity(asteroid)
        }
    }

}