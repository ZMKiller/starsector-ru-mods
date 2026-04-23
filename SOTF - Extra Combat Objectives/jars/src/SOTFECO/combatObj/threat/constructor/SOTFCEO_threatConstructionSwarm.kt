package SOTFECO.combatObj.threat.constructor

import SOTFECO.combatObj.threat.SOTFCEO_threatSwarm
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Items
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial
import com.fs.starfarer.api.impl.combat.threat.*
import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript.SwarmConstructionData
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

abstract class SOTFCEO_threatConstructionSwarm: SOTFCEO_threatSwarm() {

    override var swarmSize: Int = 300
    var startedConstruction = false
    open var bonusOverseers = 1 // guaranteed overseer spawns
    val despawnInterval = IntervalUtil(1f, 1.1f) // seconds

    override fun captured(amount: Float) {
        if (startedConstruction) {
            despawnInterval.advance(amount)
            if (despawnInterval.intervalElapsed()) {
                delete()
            }
            return
        }

        startConstruction()
    }
    
    fun startConstruction() {
        startedConstruction = true

        createConstructionSwarms()
        if (objective.owner == 1 && MathUtils.getRandom().nextFloat() <= 0.3f) {
            var mainEnemyFleet: CampaignFleetAPI? = null
            for (enemyMember in engine.getFleetManager(1).deployedCopy) {
                if (enemyMember.fleetData != null) {
                    if (enemyMember.fleetData.fleet != null) {
                        // Fleet must actually exist in our current location - otherwise, might grab the shard's fleet (which we can't salvage from)
                        if (enemyMember.fleetData.fleet.containingLocation != null) {
                            mainEnemyFleet = enemyMember.fleetData.fleet
                        }
                    }
                }
            }
            if (mainEnemyFleet != null) {
                Global.getLogger(this.javaClass)
                    .info("Successfully found an enemy fleet to add threat loot to cargo of: " + mainEnemyFleet.fullName)
                val extraLoot = Global.getFactory().createCargo(true)
                if (MathUtils.getRandom().nextFloat() <= 0.5f) {
                    extraLoot.addSpecial(SpecialItemData(Items.FRAGMENT_FABRICATOR, null), 1f)
                } else {
                    extraLoot.addSpecial(SpecialItemData(Items.THREAT_PROCESSING_UNIT, null), 1f)
                }
                BaseSalvageSpecial.addExtraSalvage(mainEnemyFleet, extraLoot)
                //mainEnemyFleet.getCargo().addWeapons(picked.getId(), 1);
                Global.getLogger(this.javaClass).info("Successfully added threat loot to cargo of " + mainEnemyFleet.fullName)

            }
        }

        tryAddingCombatStrategy()
        //Global.getCombatEngine().addPlugin(SOTFCEO_forceThreatShipAllyScript())
    }

    private fun tryAddingCombatStrategy() {
        // todo
    }

    private fun createConstructionSwarms() {
        for (variant in getVariantsToConstruct()) {
            createConstructionSwarm(variant)
        }
    }

    private fun createConstructionSwarm(spec: Triple<String, Vector2f, Int>) {
        val engine = Global.getCombatEngine()
        val manager: CombatFleetManagerAPI = engine.getFleetManager(objective.owner)
        manager.isSuppressDeploymentMessages = true

        val loc = spec.second

        val fighter = manager.spawnShipOrWing(SwarmLauncherEffect.CONSTRUCTION_SWARM_WING, loc, MathUtils.getRandomNumberInRange(0f, 360f), 0f, null)
        if (objective.owner == 0) {
            fighter.isAlly = true
        }
        fighter.mutableStats.maxSpeed.modifyMult("construction_swarm", ConstructionSwarmSystemScript.CONSTRUCTION_SWARM_SPEED_MULT)

        fighter.isDoNotRender = true
        fighter.explosionScale = 0f
        fighter.hulkChanceOverride = 0f
        fighter.impactVolumeMult = SwarmLauncherEffect.IMPACT_VOLUME_MULT
        fighter.armorGrid.clearComponentMap() // no damage to weapons/engines

        val pluginSwarm = createSwarm(this)
        val swarm = FragmentSwarmHullmod.createSwarmFor(fighter)
        swarm.params.flashFringeColor = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR
        RoilingSwarmEffect.getFlockingMap().remove(swarm.params.flockingClass, swarm)
        swarm.params.flockingClass = FragmentSwarmHullmod.CONSTRUCTION_SWARM_FLOCKING_CLASS
        RoilingSwarmEffect.getFlockingMap().add(swarm.params.flockingClass, swarm)

        val variantId = spec.first

        val variant = Global.getSettings().getVariant(variantId)
        if (variant == null) return

        val dp = variant.hullSpec.suppliesToRecover

        val numFragments: Int = spec.third
        var radiusMult = 1f
        var collisionMult = 2f
        var hpMult = 1f
        var travelTime = 3f

        if (variant.hullSize == HullSize.DESTROYER) {
            radiusMult = 2f
            collisionMult = 4f
            hpMult = radiusMult
            travelTime = 4f
        } else if (variant.hullSize == HullSize.CRUISER) {
            radiusMult = 3.5f
            collisionMult = 6f
            hpMult = radiusMult
            travelTime = 5f
        } else if (variant.hullSize == HullSize.CAPITAL_SHIP) {
            radiusMult = 4f
            collisionMult = 8f
            hpMult = radiusMult
            travelTime = 6f
        }

        for (s in fighter.exactBounds.origSegments) {
            s.p1.scale(collisionMult)
            s.p2.scale(collisionMult)
            s.set(s.p1.x, s.p1.y, s.p2.x, s.p2.y)
        }
        fighter.collisionRadius = fighter.collisionRadius * collisionMult

        fighter.maxHitpoints = fighter.maxHitpoints * hpMult
        fighter.hitpoints = fighter.hitpoints * hpMult

        swarm.params.maxOffset *= radiusMult


        //		swarm.params.initialMembers *= numMult;
//		swarm.params.baseMembersToMaintain = swarm.params.initialMembers;
//		requiredFragments = swarm.params.initialMembers;
        swarm.params.initialMembers = numFragments
        swarm.params.baseMembersToMaintain = numFragments

        val overseer = variant.hullSpec.hasTag(Tags.THREAT_OVERSEER)

        val data = SwarmConstructionData()
        data.variantId = variantId
        data.constructionTime =
            ConstructionSwarmSystemScript.BASE_CONSTRUCTION_TIME + dp * ConstructionSwarmSystemScript.CONSTRUCTION_TIME_DP_MULT
        if (overseer) {
            data.constructionTime += ConstructionSwarmSystemScript.CONSTRUCTION_TIME_OVERSEER_EXTRA
        }
        data.preConstructionTravelTime = travelTime

        /*if (fastConstructionLeft > 0) {
            if (pick.size == HullSize.FRIGATE) {
                fastConstructionLeft--
                data.constructionTime = 2f
            } else {
                fastConstructionLeft = 0
            }
        }*/

        swarm.custom1 = data

        val transfer = min(numFragments.toDouble(), pluginSwarm.numActiveMembers.toDouble()).toInt()
        if (transfer > 0) {
            //loc = Vector2f(takeoffVel)
            //loc.scale(0.5f)
            //Vector2f.add(loc, fighter.location, loc)
            pluginSwarm.transferMembersTo(swarm, transfer, loc, 100f)
        }

        val add = numFragments - transfer
        if (add > 0) {
            swarm.addMembers(add)
        }
    }

    open fun getVariantsToConstruct(): MutableSet<Triple<String, Vector2f, Int>> {
        val constructingVariants = HashSet<Triple<String, Vector2f, Int>>()
        var totalUsedFragments = 0f
        var finalUsedFragments = 0
        var underflow: Float = 0f

        val variants = getRawVariants()

        for (variantSpec in variants) {
            totalUsedFragments += variantSpec.second
        }

        val swarm = createSwarm(this)
        if (totalUsedFragments > swarm.numActiveMembers) {
            swarm.addMembers((totalUsedFragments - swarm.numActiveMembers).toInt())
        }

        underflow = totalUsedFragments / swarm.numActiveMembers
        var fragmentMult = (1+1-(underflow)).coerceAtLeast(1f)

        var checkList = HashSet<Pair<Vector2f, Float>>()
        for (variantSpec in variants) {
            val variant = Global.getSettings().getVariant(variantSpec.first)
            if (variant == null) continue
            val loc = getRandomSpawnLoc(variant, checkList)
            checkList += Pair(loc, variant.hullSpec.collisionRadius)

            val fragmentsToUse = floor(variantSpec.second * fragmentMult).toInt()
            finalUsedFragments += fragmentsToUse
            constructingVariants += Triple(variantSpec.first, loc, fragmentsToUse)
        }

        return constructingVariants
    }

    private fun getRandomSpawnLoc(variant: ShipVariantAPI, checkList: HashSet<Pair<Vector2f, Float>>): Vector2f {
        val radius = variant.hullSpec.collisionRadius

        var failsafeIter = 0f
        val iterationsTilFailsafe = 50f
        while (true) {
            val randomLoc = MathUtils.getRandomPointInCircle(objective.location, swarmRadius)

            for (toCheck in checkList) {
                if (MathUtils.getDistance(randomLoc, toCheck.first) <= max(toCheck.second, radius)) continue
            }

            if (CombatUtils.getShipsWithinRange(randomLoc, radius).any { it.collisionClass != CollisionClass.FIGHTER }) continue
            return randomLoc
            break

            // failsafe code
            failsafeIter++
            if (failsafeIter >= iterationsTilFailsafe) {
                return MathUtils.getRandomPointInCircle(objective.location, swarmRadius)
            }
        }
    }

    open fun getRawVariants(): ArrayList<Pair<String, Int>> {
        val variants = ArrayList<Pair<String, Int>>()

        var fragmentsLeft = createSwarm(this).numActiveMembers

        ConstructionSwarmSystemScript.init()
        val picker = WeightedRandomPicker<ConstructionSwarmSystemScript.SwarmConstructableVariant>()
        val toPickFrom = getToPickFrom()
        for (entry in toPickFrom) {
            picker.add(entry, 1f)
        }

        val overseerVariantsToPickFrom = getOverseersToPickFrom()
        val overseerPicker = WeightedRandomPicker<ConstructionSwarmSystemScript.SwarmConstructableVariant>()
        for (entry in overseerVariantsToPickFrom) {
            overseerPicker.add(entry, 1f)
        }
        var overseersLeft = bonusOverseers
        while (overseersLeft-- > 0) {
            val picked = overseerPicker.pick()
            variants += Pair(picked.variantId, picked.fragments)
            fragmentsLeft -= picked.fragments
        }

        while (!picker.isEmpty) {
            val picked = picker.pick()
            if (picked.fragments > fragmentsLeft) {
                picker.remove(picked)
                continue
            }
            variants += Pair(picked.variantId, picked.fragments)
            fragmentsLeft -= picked.fragments
        }

        return variants
    }

    private fun getOverseersToPickFrom(): Collection<ConstructionSwarmSystemScript.SwarmConstructableVariant> {
        ConstructionSwarmSystemScript.init()
        return ConstructionSwarmSystemScript.CONSTRUCTABLE.filter { it.variantId.contains("overseer") }.shuffled()
    }

    abstract fun getToPickFrom(): Collection<ConstructionSwarmSystemScript.SwarmConstructableVariant>

    override fun getLongDescription(): String? {
        val min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide")
        val total = Global.getSettings().battleSize
        Math.round(total * (1f - min))

        return String.format(
            """
            - swarm of unidentifiable metallic objects
            - unknown interface, expect lengthy capture time
            
            """.trimIndent()
        )
    }
}