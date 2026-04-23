package SOTFECO.combatObj

import SOTFECO.ReflectionUtils
import SOTFECO.combatObj.SOTFECO_derelictFleetObj.FleetMode.Companion.getFleetMode
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.starfarer.campaign.fleet.FleetMember
import com.fs.starfarer.combat.CombatEngine
import com.fs.starfarer.combat.CombatFleetManager
import com.fs.starfarer.combat.entities.BattleObjective
import com.fs.starfarer.combat.entities.Ship
import lunalib.lunaExtensions.getMarketsCopy
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.MathUtils.clamp
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getFactionMarkets
import org.magiclib.util.MagicRender
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class SOTFECO_derelictFleetObj: BaseBattleObjectiveEffect() {

    companion object {
        const val BATTLESIZE_TO_FP_FREIGHTER_MULT = 0.1f
        const val BATTLESIZE_TO_FP_FUEL_MULT = 0.025f
        const val CIV_FP_TO_COMBAT = 0.5f
        const val SPAWN_RADIUS = 2000f

        const val VALUE_TO_BP_LOW = 0.02f
        const val VALUE_TO_SPECIAL_TECH_LOW = 0.005f
        const val VALUE_TO_WEAPONS = 0.12f
        const val VALUE_TO_BULK = 400f

        fun genLoot(totalValue: Float): CargoAPI? {
            val dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()
            val dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()

            var d = DropData()
            d.chances = (totalValue * VALUE_TO_BP_LOW * MathUtils.getRandomNumberInRange(0.1f, 2f)).toInt()
            d.group = "blueprints_low"
            dropRandom.add(d)

            d = DropData()
            d.chances = (totalValue * VALUE_TO_SPECIAL_TECH_LOW * MathUtils.getRandomNumberInRange(0.1f, 2f)).toInt()
            d.group = "rare_tech_low"
            dropRandom.add(d)

            d = DropData()
            d.chances = (totalValue * VALUE_TO_WEAPONS * MathUtils.getRandomNumberInRange(0.1f, 2f)).toInt()
            d.group = "weapons2"
            dropRandom.add(d)

            d = DropData()
            d.value = (totalValue * VALUE_TO_BULK * MathUtils.getRandomNumberInRange(0.1f, 2f)).toInt()
            d.group = "freighter_cargo"
            dropValue.add(d)

            val result = SalvageEntity.generateSalvage(MathUtils.getRandom(), 1f, 1f, 1f, 1f, dropValue, dropRandom)
            return result
        }
    }

    enum class FleetMode(val introDialog: List<Pair<List<Any>, Float>>) {
        FRIENDLY_TO_PLAYER(listOf(
            Pair(
                listOf(
                    Misc.getTextColor(),
                    "\"Oh thank the stars, a friendly face! Listen - I know you're in the middle of something, but we really need a hand.",
                    Misc.getTextColor(),
                    "We just need a few supplies to get our engines back up and running, and once that's done, we can provide some fire support and even some of our loot!",
                    Misc.getTextColor(),
                    "Just please, get here before they do!\""
                ),
                10f
            ),
            Pair(
                listOf(
                    Misc.getTextColor(),
                    "\"A ship? A ship! We were certain we'd never see civilization again! Listen, give us some help and we'll give you some of our loot. We're explorers, just like you!\"",
                ),
                10f
            ),
            Pair(
                listOf(
                    Misc.getTextColor(),
                    "\"-epare scuttling charges-wait, friendly IFF? Is this thing on?! Hey! We've got plenty of salvage in our holds if you help us get out of this mess!",
                    Misc.getTextColor(),
                    "Just get near us and send us some engineers, dammit! We're on a time crunch!\"",
                ),
                9f
            )
        )) {
            override fun getValidFactions(battle: BattleAPI): MutableList<FactionAPI> {
                val baseFacs = Global.getSector().allFactions.filter { it.isShowInIntelTab }.toMutableList()
                val finalFacs = ArrayList<FactionAPI>()
                outer@ for (fac in baseFacs) {
                    for (fleet in battle.playerSide) {
                        if (fleet.faction.isHostileTo(fac)) {
                            continue@outer
                        }
                    }
                    for (fleet in battle.nonPlayerSide) {
                        if (!fleet.faction.isHostileTo(fac)) {
                            continue@outer
                        }
                    }
                    finalFacs += fac
                }
                if (Global.getSector().playerFaction.getMarketsCopy().isEmpty()) {
                    finalFacs -= Global.getSector().getFaction(Factions.PLAYER)
                }

                return finalFacs
            }

            override fun getWeight(): Float {
                return 20f
            }
        },
        FRIENDLY_TO_ENEMY(listOf(
            Pair(
                listOf(
                  Misc.getTextColor(),
                    "\"Oh hells oh hells oh hells! Not you! Who sent you?! Ludd damn it, we're sitting ducks! And just when we finally found a friendly face out here!\"",
                ),
                10f
            ),
            Pair(
                listOf(
                    Misc.getTextColor(),
                    "\"General quarters, general quarters! I don't care if life support's dead, or the doors are bolted, grab a gun and get into position!\"",
                ),
                10f
            ),
        )) {
            override fun getValidFactions(battle: BattleAPI): MutableList<FactionAPI> {
                val baseFacs = Global.getSector().allFactions.filter { it.isShowInIntelTab }.toMutableList()
                val finalFacs = ArrayList<FactionAPI>()
                outer@ for (fac in baseFacs) {
                    for (fleet in battle.playerSide) {
                        if (!fleet.faction.isHostileTo(fac)) {
                            continue@outer
                        }
                    }
                    for (fleet in battle.nonPlayerSide) {
                        if (fleet.faction.isHostileTo(fac)) {
                            continue@outer
                        }
                    }
                    finalFacs += fac
                }
                if (Global.getSector().playerFaction.getMarketsCopy().isEmpty()) {
                    finalFacs -= Global.getSector().getFaction(Factions.PLAYER)
                }

                return finalFacs
            }

            override fun getWeight(): Float {
                return 8f
            }
        },
        OH_SHIT_OH_FUCK(listOf(
            Pair(
                listOf(
                    Misc.getTextColor(),
                    "\"Yes! Finally! A fellow explorer! We just need - wait. Why are you charging weapons? Why are... all of you? Oh no...\"",
                ),
                10f
            ),
            Pair(
                listOf(
                    Misc.getTextColor(),
                    "\"Two enemy forces, on both our flanks. Our engines are shot, our weapons are burnt, and we're on our last legs. My crew? It was an honor serving with you.\"",
                ),
                10f
            ),
            Pair(
                listOf(
                    Misc.getTextColor(),
                    "\"...no no no no no no no NO! NO! NO!!\"",
                ),
                5f
            )
        )) {
            override fun getValidFactions(battle: BattleAPI): MutableList<FactionAPI> {
                val baseFacs = Global.getSector().allFactions.filter { it.isShowInIntelTab }.toMutableList()
                val finalFacs = ArrayList<FactionAPI>()
                outer@ for (fac in baseFacs) {
                    for (fleet in battle.playerSide) {
                        if (!fleet.faction.isHostileTo(fac)) {
                            continue@outer
                        }
                    }
                    for (fleet in battle.nonPlayerSide) {
                        if (!fleet.faction.isHostileTo(fac)) {
                            continue@outer
                        }
                    }
                    finalFacs += fac
                }
                if (Global.getSector().playerFaction.getMarketsCopy().isEmpty()) {
                    finalFacs -= Global.getSector().getFaction(Factions.PLAYER)
                }

                return finalFacs
            }

            override fun getWeight(): Float {
                return 3f
            }
        };

        open fun canBeUsed(battle: BattleAPI): Boolean {
            return getValidFactions(battle).isNotEmpty()
        }

        abstract fun getValidFactions(battle: BattleAPI): MutableList<FactionAPI>

        abstract fun getWeight(): Float

        companion object {
            fun getPicker(): WeightedRandomPicker<FleetMode> {
                val picker = WeightedRandomPicker<FleetMode>()
                entries.forEach { picker.add(it, it.getWeight()) }
                return picker
            }

            fun getFleetMode(battle: BattleAPI): FleetMode? {
                val picker = getPicker()
                var picked: FleetMode? = null
                while (!picker.isEmpty) {
                    val iter = picker.pickAndRemove()
                    if (!iter.canBeUsed(battle)) continue
                    picked = iter
                    break
                }

                return picked
            }
        }
    }

    val introInterval = IntervalUtil(10f, 11f)
    var doingIntro = true
    var mode: FleetMode? = null
    val ships: MutableList<ShipAPI> = ArrayList()
    var flagship: ShipAPI? = null
        get() {
            if (field?.isHulk == true) field = null

            return field
        }
    lateinit var fleet: CampaignFleetAPI

    override fun init(engine: CombatEngineAPI?, objective: BattleObjectiveAPI?) {
        super.init(engine, objective)

        mode = getFleetMode(getBattle())
        if (mode == null) {
            replace()
            return
        }

        spawnFleet()
        spawnShips()
    }

    fun getBattle(): BattleAPI = Global.getSector().playerFleet.battle

    private fun spawnFleet() {
        val faction = mode!!.getValidFactions(getBattle()).random()
        Global.getSector().playerFleet.containingLocation

        val frieghterFP = getFreighterFP()
        val fuelFP = getFuelFP()
        val utilFP = getUtilFP()
        val combinedLogFP = frieghterFP + fuelFP + utilFP
        val combatFP = getCombatFP(combinedLogFP)

        val params = FleetParamsV3(
            null,
            faction.id,
            null,
            FleetTypes.SCAVENGER_MEDIUM,
            combatFP,
            frieghterFP,
            fuelFP,
            0f,
            0f,
            utilFP,
            0f
        )
        val fleet = FleetFactoryV3.createFleet(params)
        fleet.name = "Scavenger"
        fleet.memoryWithoutUpdate["\$SOTFECO_derelictFleet"] = true
        for (member in fleet.fleetData.membersListCopy) {
            member.repairTracker.isMothballed = true
            member.repairTracker.cr = 0f
            member.status.applyDamage(500f, MathUtils.getRandomNumberInRange(0.3f, 0.9f))

            val newVariant = member.variant.clone()
            newVariant.source = VariantSource.REFIT
            newVariant.hullVariantId = member.hullId + "_" + Misc.genUID()
            member.setVariant(newVariant, false, false)
            newVariant.addMod("SOTFECO_noDP")
            //member.stats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult("\$SOTFECO_derelictShip", 0f)
        }

        this.fleet = fleet
    }

    private fun spawnShips() {
        getBattle()
        val engine = Global.getCombatEngine()
        val derelictManager = engine.getFleetManager(100)

        val wasSuppressing = derelictManager.isSuppressDeploymentMessages
        derelictManager.isSuppressDeploymentMessages = true

        for (member in fleet.fleetData.membersListCopy) {
            member.owner = 100
            val spawned = derelictManager.spawnFleetMember(
                member,
                Misc.ZERO,
                MathUtils.getRandomNumberInRange(0f, 360f),
                0f
            )
            spawned.mutableStats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult("\$SOTFECO_derelictShip", 0f)


            spawned.fleetMember = member
            for (weapon in spawned.allWeapons) {
                weapon.disable(true)
            }

            spawned.location.set(findClearLocation(spawned, MathUtils.getRandomPointInCircle(objective.location, SPAWN_RADIUS)))

            //spawned.setCustomData("SOTFECO_derelictShipAI", spawned.shipAI)
            spawned.shipAI = null
            spawned.fixedLocation = Vector2f(spawned.location)
            ships += spawned

            engine.addPlugin(DerelictShipPlugin(spawned))
        }

        derelictManager.isSuppressDeploymentMessages = wasSuppressing
    }

    class DerelictShipPlugin(val ship: ShipAPI): BaseEveryFrameCombatPlugin() {
        var timeLeft = 5f

        override fun advance(amount: Float, events: List<InputEventAPI?>?) {
            super.advance(amount, events)

            val engine = Global.getCombatEngine()
            if (engine.isPaused) return
            if (ship.owner != 100 || ship.isHulk || (!engine.isEntityInPlay(ship))) {
                engine.removePlugin(this)
                return
            }
            timeLeft -= amount
            if (timeLeft > 0f) return

            ship.velocity.set(0f, 0f)

            /*for (entry in ShipCommand.entries) {
                if (entry == ShipCommand.ACCELERATE || entry == ShipCommand.DECELERATE || entry == ShipCommand.STRAFE_LEFT || entry == ShipCommand.STRAFE_RIGHT) continue
                ship.blockCommandForOneFrame(entry)
            }*/
        }
    }

    private fun findClearLocation(ship: ShipAPI, dest: Vector2f): Vector2f? {
        if (isLocationClear(ship, dest)) return dest

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
                if (isLocationClear(ship, loc)) {
                    return loc
                }
                angle += 60f
            }
            distIndex *= 2f
        }

        if (tested.isEmpty) return dest // shouldn't happen


        return tested.pick()
    }

    private fun isLocationClear(ship: ShipAPI, loc: Vector2f): Boolean {
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
            var checkDist = ship.collisionRadius * 2f
            if (dist < r + checkDist) {
                return false
            }
        }
        for (other in Global.getCombatEngine().asteroids) {
            val dist = Misc.getDistance(loc, other.location)
            if (dist < other.collisionRadius + ship.collisionRadius * 2f) {
                return false
            }
        }

        return true
    }

    fun getMaxBattleSize(): Int {
        val engine = Global.getCombatEngine()

        val friendly = engine.getFleetManager(0).maxStrength
        val enemy = engine.getFleetManager(1).maxStrength

        return (friendly.coerceAtLeast(enemy))
    }

    private fun getFreighterFP(): Float {
        val battleSize = getMaxBattleSize()

        val FP = (battleSize * BATTLESIZE_TO_FP_FREIGHTER_MULT)

        return (clamp(FP, 15f, 60f))
    }

    private fun getFuelFP(): Float {
        val battleSize = getMaxBattleSize()

        val FP = (battleSize * BATTLESIZE_TO_FP_FUEL_MULT)

        return (clamp(FP, 3f, 6f))
    }

    private fun getUtilFP(): Float {
        if (MathUtils.getRandomNumberInRange(0f, 1f) > 0.1f) return 0f
        return 5f
    }

    private fun getCombatFP(civFP: Float): Float {
        var FP = (civFP * CIV_FP_TO_COMBAT)
        FP = clamp(FP, 10f, 100f)
        FP *= MathUtils.getRandomNumberInRange(0.8f, 1.2f)

        return FP
    }

    fun boardShips() {

        val owner = objective.owner
        val battle = getBattle()
        val toGain = if (owner == 0) battle.playerSide else battle.nonPlayerSide
        val toGainFleet = if (owner == 0) Global.getSector().playerFleet else toGain.random()

        for (ship in getViableShips()) {
            ship.owner = owner
            ship.originalOwner = owner
            ship.captain = null

            rejuvinate(ship)

            val member = ship.fleetMember
            member.captain = null
            fleet.fleetData.removeFleetMember(member)
            toGainFleet.fleetData.addFleetMember(member)

            member.variant.removeMod("SOTFECO_noDP")
        }
        fleet.despawn()

        val color = if (owner == 0) Misc.getPositiveHighlightColor() else Misc.getNegativeHighlightColor()
        Global.getCombatEngine().combatUI.addMessage(
            0,
            Misc.getTextColor(),
            "Derelict ships at ",
            color,
            objective.displayName,
            Misc.getTextColor(),
            " captured - now part of",
            color,
            toGainFleet.name,
            Misc.getTextColor(),
            " and fighting in-combat"
        )

        delete()
    }

    private fun rejuvinate(ship: ShipAPI) {
        var ship = ship
        val manager = Global.getCombatEngine().getFleetManager(ship.owner) as CombatFleetManager
        ship.fleetMember.owner = ship.owner
        ship.fleetMember.isAlly = ship.isAlly
        val test = SOTFECO_theOneJavaLine.getConstructor()
        val deployedObj = test.invoke(ship.fleetMember as FleetMember, ship as Ship)
        val method = manager.deployed::class.java.getMethod("add", Any::class.java) as Any?
        ReflectionUtils.invokeMethodHandle.invoke(method, manager.deployed, deployedObj)
        ship = ship as ShipAPI
        ship.shipAI = Global.getSettings().createDefaultShipAI(ship, ShipAIConfig())//ship.customData["SOTFECO_derelictShipAI"] as ShipAIPlugin
        ship.shipAI.forceCircumstanceEvaluation()
        ship.fleetMember.repairTracker.isMothballed = false
        ship.setControlsLocked(false)
        ship.fixedLocation = null

        ship.currentCR = 0.3f

        for (weapon in ship.allWeapons) {
            weapon.repair()
        }
        //SOTFECO_reflectionUtilsTwo.invoke("Ö00000", manager.getTaskManager(ship.isAlly))
    }

    fun getViableShips(): List<ShipAPI> = ships.filter { !it.isHulk && it.isAlive && !it.isExpired }
    fun getCombatShips(): List<ShipAPI> = getViableShips().filter { !it.hullSpec.isCivilianNonCarrier }
    fun getCivilianShips(): List<ShipAPI> = getViableShips().filter { it.hullSpec.isCivilianNonCarrier }

    fun finishRearmament() {
        val owner = objective.owner
        for (ship in getViableShips()) {
            ship.owner = owner
            if (owner == 0) ship.isAlly = true
            ship.originalOwner = owner

            rejuvinate(ship)
        }

        if (owner == 1) {
            Global.getCombatEngine().combatUI.addMessage(
                0,
                Misc.getTextColor(),
                "Derelict ships at ",
                Misc.getNegativeHighlightColor(),
                objective.displayName,
                Misc.getTextColor(),
                " crash-repaired - retreating",
            )
        } else {
            Misc.makeImportant(fleet, "\$SOTFECO_derelictsGivingLoot")
            fleet.memoryWithoutUpdate.set("\$genericHail", true)
            fleet.memoryWithoutUpdate.set("\$genericHail_openComms", "SOTFECO_derelictsGivingLootHail")

            doRevivedText()
        }

        doAssignments()
        Global.getSector().currentLocation.addEntity(fleet)
        val battleLoc = getBattle().computeCenterOfMass()
        fleet.setLocation(battleLoc.x, battleLoc.y)
        Global.getSector().addScript(SOTFECO_derelictFleetCampaignScript(fleet, this))
        delete()
    }

    private fun doRevivedText() {
        val picker = WeightedRandomPicker<String>()
        picker.add("Receiving your engineers now, captain. We'll be on our way shortly - cover our retreat and there's plenty of loot in it for you!", 10f)
        picker.add("Engines burning hot, plotting course for exfiltration. Burn bright, captain, we might just owe you our lives!", 10f)
        picker.add("Go go go go go! Turrets to PD, emergency burn! Let's get the hells out of here!", 10f)

        val speaker = getSpeakingShip() ?: return
        val picked = picker.pick()

        Global.getCombatEngine().combatUI.addMessage(
            0,
            Misc.getPositiveHighlightColor(),
            speaker,
            Misc.getPositiveHighlightColor(),
            speaker.name,
            Misc.getTextColor(),
            ": \"$picked\""
        )
    }

    fun getSpeakingShip(): ShipAPI? {
        return flagship ?: getViableShips().randomOrNull()
    }

    fun delete() {
        Global.getCombatEngine().removeEntity(objective)
    }

    fun doAssignments() {
        for (ship in getViableShips()) {
            val manager = Global.getCombatEngine().getFleetManager(ship.owner)
            val orderManager = manager.getTaskManager(ship.isAlly)
            val deployed = manager.getDeployedFleetMember(ship)
            orderManager.orderRetreat(
                deployed,
                false,
                false
            )
            ship.shipAI.cancelCurrentManeuver()
            ship.setRetreating(true, false)
            //ship.shipAI.cancelCurrentManeuver()
            //ship.shipAI.forceCircumstanceEvaluation()
            if (ship.owner == 0) {
                Global.getCombatEngine().addPlugin(PrizeShipPlugin(ship, this))
            }
        }

        for (ship in getCivilianShips()) {
            val manager = Global.getCombatEngine().getFleetManager(ship.owner)
            val orderManager = manager.getTaskManager(ship.isAlly)
            val deployed = manager.getDeployedFleetMember(ship)
            val order = orderManager.createAssignment(
                CombatAssignmentType.LIGHT_ESCORT,
                deployed,
                false
            )
        }

        /*for (ship in getCombatShips()) {
            val randDefenseTarget = getCivilianShips().randomOrNull() ?: continue
            val manager = Global.getCombatEngine().getFleetManager(ship.owner)
            val orderManager = manager.getTaskManager(ship.isAlly)
            val deployed = manager.getDeployedFleetMember(ship)
            val deployedCiv = manager.getDeployedFleetMember(randDefenseTarget)
        }*/
    }

    val destroyedShips = HashSet<FleetMemberAPI>()
    fun destroyed(dead: ShipAPI) {
        destroyedShips += dead.fleetMember
        return
    }

    class PrizeShipPlugin(val ship: ShipAPI, val source: SOTFECO_derelictFleetObj): BaseEveryFrameCombatPlugin() {
        val icon = Global.getSettings().getSprite("misc", "SOTFECO_prizeShipIcon")

        override fun advance(amount: Float, events: List<InputEventAPI?>?) {
            super.advance(amount, events)

            val engine = Global.getCombatEngine()
            if (engine.isPaused) return
            if (ship.isHulk) {
                source.destroyed(ship)
                engine.removePlugin(this)
                return
            }
            if (!engine.isEntityInPlay(ship)) {
                engine.removePlugin(this)
                return
            }

            val spriteSize = Vector2f(128f, 128f)
            val spriteLoc = Vector2f(ship.location)
            spriteLoc.y += ship.collisionRadius * 1.1f
            MagicRender.singleframe(
                icon,
                spriteLoc,
                spriteSize,
                0f,
                Color.WHITE,
                false,
                CombatEngineLayers.ABOVE_SHIPS_LAYER
            )

            return
        }
    }

    override fun advance(amount: Float) {
        val engine = Global.getCombatEngine()
        if (engine.isPaused) return

        if (Global.getCombatEngine().customData["\$SOTFECO_introObj"] == null) {
            Global.getCombatEngine().customData["\$SOTFECO_introObj"] = this
        }
        if (doingIntro && Global.getCombatEngine().customData["\$SOTFECO_introObj"] == this) {
            introInterval.advance(amount)
            if (introInterval.intervalElapsed()) {
                doingIntro = false
                val picker = WeightedRandomPicker<List<Any>>()
                mode!!.introDialog.forEach { picker.add(it.first, it.second) }
                val neutral = Global.getSettings().getColor("textNeutralColor")
                val speaker = getSpeakingShip() ?: return
                val base = listOf(
                    neutral,
                    speaker,
                    neutral,
                    "${speaker.name}",
                    Misc.getTextColor(),
                    ": "
                )
                val content = picker.pick()
                val final = (base + content).toTypedArray()

                engine.combatUI.addMessage(
                    0,
                    *final
                )
            }
        }

        if (!isCaptured()) return

        when (mode) {
            FleetMode.FRIENDLY_TO_PLAYER -> {
                if (objective.owner == 0) finishRearmament() else boardShips()
            }
            FleetMode.FRIENDLY_TO_ENEMY -> {
                if (objective.owner == 0) boardShips() else finishRearmament()
            }
            FleetMode.OH_SHIT_OH_FUCK -> boardShips()
            null -> return
        }
    }

    fun isCaptured(): Boolean {
        return objective.owner == 0 || objective.owner == 1
    }

    fun getPickerOf(list: List<Pair<String, Float>>): WeightedRandomPicker<String> {
        val picker = WeightedRandomPicker<String>()
        list.forEach { picker.add(it.first, it.second) }
        return picker
    }

    override fun getStatusItemsFor(ship: ShipAPI?): List<BattleObjectiveEffect.ShipStatusItem?>? {
        return null
    }

    override fun getLongDescription(): String? {

        var base =
            "- group of heavily damaged ships \n" +
            "- life signs positive \n"

        when (mode) {
            FleetMode.FRIENDLY_TO_PLAYER -> {
                base += "- friendly IFF \n" +
                        "- capture objective to send supplies and engineers to repair vessels \n" +
                        "- successful capture will reactivate vessels and begin a fighting retreat \n" +
                        "- successful retreat will reward you with valuable resources and treasures \n" +
                        "\n" +
                        "- enemy capture will result in ships being commandeered and used in-combat by your foes"
            }
            FleetMode.FRIENDLY_TO_ENEMY -> {
                base += "- hostile IFF \n" +
                        "- capture objective to repair & recover ships and add them to your fleet \n" +
                        "- vessels will have no DP contribution for this battle only \n" +
                        "\n" +
                        "- enemy capture will result in ships beginning a retreat and splitting into a new fleet"
            }
            FleetMode.OH_SHIT_OH_FUCK -> {
                base += "- hostile IFF \n" +
                        "- capture objective to repair & recover ships and add them to your fleet \n" +
                        "- vessels will have no DP contribution for this battle only \n" +
                        "\n" +
                        "- enemy capture will result in ships being commandeered and used in-combat by your foes"
            }
            null -> return null
        }

        return base
    }

    fun replace() {
        val engine = Global.getCombatEngine() as CombatEngine

        val newObj = BattleObjective(
            "nav_buoy",
            Vector2f(objective.location.x, objective.location.y),
            BattleObjectiveAPI.Importance.NORMAL
        )
        engine.addObject(newObj)
        delete()
    }

    class SOTFECO_derelictFleetCampaignScript(
        val fleet: CampaignFleetAPI,
        val obj: SOTFECO_derelictFleetObj
    ): EveryFrameScript {
        override fun isDone(): Boolean {
            return false
        }

        override fun runWhilePaused(): Boolean {
            return false
        }

        // runs only once
        override fun advance(amount: Float) {
            for (member in fleet.fleetData.membersListCopy) {
                member.variant.removeMod("SOTFECO_noDP")
                if (member in obj.destroyedShips) {
                    fleet.removeFleetMemberWithDestructionFlash(member)
                } else {
                    member.repairTracker.isMothballed = true
                }
            }
            if (fleet.isEmpty) {
                fleet.despawn()
                Global.getSector().removeScript(this)
                return
            }
            fleet.fleetData.membersListCopy.first().repairTracker.isMothballed = false

            if (!fleet.faction.isHostileTo(Global.getSector().playerFaction)) {
                fleet.addAssignmentAtStart(
                    FleetAssignment.INTERCEPT,
                    Global.getSector().playerFleet,
                    60f,
                    "approaching your fleet",
                    null
                )

                fleet.cargo.addAll(genLoot(fleet.cargo.maxCapacity * 0.5f))
            }
            val randPlanet = fleet.faction.getMarketsCopy().randomOrNull()?.primaryEntity ?: Global.getSector().getFaction(Factions.INDEPENDENT).getMarketsCopy().randomOrNull()?.primaryEntity ?: Global.getSector().playerFleet
            fleet.addAssignment(
                FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                randPlanet,
                Float.MAX_VALUE
            )

            Global.getSector().removeScript(this)
        }
    }
}