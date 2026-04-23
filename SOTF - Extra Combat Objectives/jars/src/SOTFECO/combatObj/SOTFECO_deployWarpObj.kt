package SOTFECO.combatObj

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
import org.dark.shaders.distortion.WaveDistortion
import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicRender
import java.awt.Color


class SOTFECO_deployWarpObj: BaseBattleObjectiveEffect() {

    companion object {
        const val PRIMARY_RADIUS = 1000f
        const val SECONDARY_RADIUS = 2000f
        const val FINAL_RADIUS = 3000f

        const val CONTEST_RANGE = 2500f

        const val MIN_SPAWN_DIST = 100f
        const val MIN_SPAWN_DIST_FRIAGTE = 60f

        const val WARP_TIME = 3f

        val JITTER_COLOR = Color(70, 70, 180, 150)
        val JITTER_EVIL = Color(180, 70, 70, 150)
    }

    override fun advance(amount: Float) {
        val engine = Global.getCombatEngine()
        if (engine.isPaused) return

        if (!isCaptured()) return
        val owner = objective.owner
        val manager = engine.getFleetManager(owner)
        val usable = canBeUsed()
        objective.setCustomData("SOFTECO_unusable", !usable)
        if (!usable) return
        for (iterObj in engine.objectives.filter { it.type == "softCEO_objective_warp" && it.owner == objective.owner }) {
            if (iterObj == this) continue
            if (iterObj.customData["SOFTECO_unusable"] == true) continue

            val theirY = iterObj.location.y
            if (owner == 0 && theirY > objective.location.y) {
                return
            } // we arent the furthest
            if (owner == 1 && theirY < objective.location.y) {
                return
            }
        }
        for (member in manager.deployedCopyDFM) {
            val ship = member.ship ?: continue
            val isTrackingTravel = ship.customData["\$SOTFECO_trackingTravelDrive"] as? Boolean ?: false
            if (isTrackingTravel) {
                if (!ship.travelDrive.isOn) {
                    ship.setCustomData("\$SOTFECO_trackingTravelDrive",  false)
                    ship.setCustomData("\$SOTFECO_deployedAlready",  true)
                }
            } else if (!(ship.customData["\$SOTFECO_deployedAlready"] as? Boolean ?: false)) {
                ship.setCustomData("\$SOTFECO_trackingTravelDrive", true)
            }
            if (canWarp(ship)) {
                beginWarp(ship)
            }
        }
    }

    fun canWarp(ship: ShipAPI): Boolean {
        if (ship.isFighter) return false
        if (!ship.isAlive || ship.isHulk) return false
        if (ship.customData["\$SOTFECO_deployedAlready"] as? Boolean ?: false) return false
        if (!ship.travelDrive.isOn && !ship.isRetreating) return false

        if (ship.owner != objective.owner) return false

        return true
    }

    fun canBeUsed(): Boolean {
        if (!isCaptured()) return false

        val owner = objective.owner
        val manager = engine.getFleetManager(owner)
        val orderManager = manager.getTaskManager(false)
        for (order in orderManager.allAssignments) {
            if (order.target != objective) continue
            if (order.type == CombatAssignmentType.RALLY_CIVILIAN) {
                return false
            }
        }
        val nearbyShips = engine.shipGrid.getCheckIterator(objective.location, CONTEST_RANGE, CONTEST_RANGE)
        while (nearbyShips.hasNext()) {
            val ship = nearbyShips.next() as ShipAPI
            if (ship.isFighter || ship.isHulk) continue
            if (ship.owner != owner) {
                return false
            }
        }

        return true
    }

    private fun beginWarp(ship: ShipAPI) {
        ship.turnOffTravelDrive()

        val initialTarget = Misc.getPointWithinRadius(objective.location, PRIMARY_RADIUS)
        val finalTarget = findClearLocation(initialTarget) ?:
        findClearLocation(Misc.getPointWithinRadius(objective.location, SECONDARY_RADIUS)) ?:
        Misc.getPointWithinRadius(objective.location, FINAL_RADIUS)

        Global.getCombatEngine().addPlugin(SOTFECO_warpScript(ship, finalTarget, Vector2f(ship.location), this))
    }

    private fun findClearLocation(dest: Vector2f): Vector2f? {
        if (isLocationClear(dest)) return dest

        val incr = 50f

        val tested = WeightedRandomPicker<Vector2f?>()
        var distIndex = 1f
        while (distIndex <= 32f) {
            val start = Math.random().toFloat() * 360f
            var angle = start
            while (angle < start + 360) {
                val loc = Misc.getUnitVectorAtDegreeAngle(angle)
                loc.scale(incr * distIndex)
                Vector2f.add(dest, loc, loc)
                tested.add(loc)
                if (isLocationClear(loc)) {
                    return loc
                }
                angle += 60f
            }
            distIndex *= 2f
        }

        if (tested.isEmpty) return dest // shouldn't happen


        return tested.pick()
    }

    private fun isLocationClear(loc: Vector2f): Boolean {
        for (other in Global.getCombatEngine().ships) {
            if (other.isShuttlePod) continue
            if (other.isFighter) continue

//			Vector2f otherLoc = other.getLocation();
//			float otherR = other.getCollisionRadius();

//			if (other.isPiece()) {
//				System.out.println("ewfewfewfwe");
//			}
            var otherLoc = other.shieldCenterEvenIfNoShield
            var otherR = other.shieldRadiusEvenIfNoShield
            if (other.isPiece) {
                otherLoc = other.location
                otherR = other.collisionRadius
            }


//			float dist = Misc.getDistance(loc, other.getLocation());
//			float r = other.getCollisionRadius();
            val dist = Misc.getDistance(loc, otherLoc)
            val r = otherR
            //r = Math.min(r, Misc.getTargetingRadius(loc, other, false) + r * 0.25f);
            var checkDist = MIN_SPAWN_DIST
            if (other.isFrigate) checkDist = MIN_SPAWN_DIST_FRIAGTE
            if (dist < r + checkDist) {
                return false
            }
        }
        for (other in Global.getCombatEngine().asteroids) {
            val dist = Misc.getDistance(loc, other.location)
            if (dist < other.collisionRadius + MIN_SPAWN_DIST) {
                return false
            }
        }

        return true
    }

    fun isCaptured(): Boolean {
        return objective.owner == 0 || objective.owner == 1
    }

    override fun getStatusItemsFor(ship: ShipAPI?): List<BattleObjectiveEffect.ShipStatusItem?>? {
        return null
    }

    override fun getLongDescription(): String {
        val min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide")
        val total = Global.getSettings().getBattleSize()
        val maxPoints = Math.round(total * (1f - min))

        return String.format(
            """
            - warp-point for deploying ships
            - deploying vessels will warp to the furthest warp beacon after a small delay
            - if an anchor has a 'rally civilian craft' order, it will not be used
            - hold fire to prematurely exit
            - non-functional if contested
            -
            - +${getBonusDeploymentPoints()} bonus deployment points up to a maximum of $maxPoints points
            """.trimIndent()
        )
    }

    class SOTFECO_warpScript(val ship: ShipAPI, val dest: Vector2f, val from: Vector2f, val obj: SOTFECO_deployWarpObj) : BaseEveryFrameCombatPlugin() {
        //val ai: Any = ship.ai
        val wasPhased = ship.isPhased
        lateinit var distortion: WaveDistortion

        val afterimageTimer = IntervalUtil(0.05f, 0.05f)
        val timer = IntervalUtil(WARP_TIME, WARP_TIME)

        init {
            ship.isPhased = true
            ship.extraAlphaMult2 = 0.3f

            distortion = WaveDistortion()
            distortion.intensity = 20f
            distortion.size = ship.collisionRadius * 2f
            distortion.setArc(0f, 360f)
            distortion.flip(false)
            distortion.setLifetime(WARP_TIME)
            DistortionShader.addDistortion(distortion)
        }

        override fun advance(amount: Float, events: List<InputEventAPI?>?) {
            super.advance(amount, events)
            val engine = Global.getCombatEngine()
            if (engine.isPaused) return

            val color = if (ship.owner == 0) JITTER_COLOR else JITTER_EVIL

            distortion.location = ship.location

            amount * ship.mutableStats.timeMult.modified
            afterimageTimer.advance(amount)
            val sprite = ship.spriteAPI
            val offsetX = sprite.width / 2 - sprite.centerX
            val offsetY = sprite.height / 2 - sprite.centerY

            val trueOffsetX = FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetX - FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetY
            val trueOffsetY = FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetX + FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetY
            if (afterimageTimer.intervalElapsed()) {
                MagicRender.battlespace(
                    Global.getSettings().getSprite(ship.hullSpec.spriteName),
                    Vector2f(ship.location.getX() + trueOffsetX, ship.location.getY() + trueOffsetY),
                    Vector2f(0f, 0f),
                    Vector2f(ship.spriteAPI.width, ship.spriteAPI.height),
                    Vector2f(0f, 0f),
                    ship.facing - 90f,
                    0f,
                    Misc.setAlpha(color, 75),
                    false,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f,
                    0.1f,
                    0.5f,
                    CombatEngineLayers.BELOW_SHIPS_LAYER
                )
            }

            for (module in ship.childModulesCopy + ship) {
                module.setJitter(
                    "SOTFECO_warpScriptOver",
                    color,
                    3f,
                    5,
                    3f
                )
                module.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
                module.blockCommandForOneFrame(ShipCommand.ACCELERATE)
                module.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
                module.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)
                module.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
                module.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
                module.blockCommandForOneFrame(ShipCommand.FIRE)
                module.velocity.set(0f, 0f)
            }

            timer.advance(amount) // no time mult is intentional
            val progress = (timer.elapsed / timer.intervalDuration)
            ship.location.set(Misc.interpolateVector(from, dest, progress))
            for (wing in ship.allWings) {
                for (member in wing.wingMembers) {
                    member.location.set(Misc.interpolateVector(from, dest, progress))
                }
            }
            if (ship.ai != null) {
                val nearestEnemy = engine.ships.filter { it.isAlive && !it.isFighter && it.owner != ship.owner }.minByOrNull { MathUtils.getDistance(it, dest) }
                if (nearestEnemy != null) {
                    ship.facing = VectorUtils.getAngle(ship.location, nearestEnemy.location)
                }
            }

            if (engine.playerShip == ship) {
                Global.getCombatEngine().maintainStatusForPlayerShip(
                    "SOTFECO_warpScript",
                    "graphics/icons/hullsys/displacer.png",
                    "Warp Beacon",
                    "Warping to ${obj.objective.displayName}",
                    false
                )
            }

            Global.getSoundPlayer().playLoop(
                "terrain_slipstream",
                ship,
                1f,
                1f,
                ship.location,
                ship.velocity
            )

            MagicRender.battlespace(
                Global.getSettings().getSprite(ship.hullSpec.spriteName),
                dest,
                Vector2f(0f, 0f),
                Vector2f(ship.spriteAPI.width, ship.spriteAPI.height),
                Vector2f(0f, 0f),
                ship.facing - 90f,
                0f,
                Misc.setAlpha(color, (255f * (progress)).toInt()),
                true,
                2f,
                0f,
                0f,
                0f,
                0f,
                0f,
                0.1f,
                0.1f,
                CombatEngineLayers.BELOW_SHIPS_LAYER
            )

            if (ship.ai == null && ship == engine.playerShip && ship.isHoldFire) {
                finish()
                return
            }

            if (timer.intervalElapsed()) {
                finish()
            }
        }

        private fun finish() {
            Global.getCombatEngine().removePlugin(this)

            ship.isPhased = wasPhased
            //ship.location.set(dest.x, dest.y)

            val newDistortion = RippleDistortion()
            newDistortion.location = Vector2f(ship.location)
            newDistortion.intensity = distortion.intensity
            newDistortion.size = distortion.size * 3f
            newDistortion.setLifetime(3f)
            newDistortion.fadeInSize(0.3f)
            newDistortion.fadeOutIntensity(1.4f)

            DistortionShader.removeDistortion(distortion)
            DistortionShader.addDistortion(newDistortion)

            Global.getSoundPlayer().playSound(
                "SOTFECO_warpFinish",
                1f,
                1f,
                ship.location,
                Misc.ZERO
            )

            ship.shipAI?.forceCircumstanceEvaluation()
            ship.extraAlphaMult2 = 1f
        }
    }
}