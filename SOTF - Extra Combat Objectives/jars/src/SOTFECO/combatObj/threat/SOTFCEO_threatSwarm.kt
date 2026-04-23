package SOTFECO.combatObj.threat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BattleObjectiveAPI
import com.fs.starfarer.api.combat.BattleObjectiveEffect
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.RoilingSwarmParams
import java.awt.Color

abstract class SOTFCEO_threatSwarm: BaseBattleObjectiveEffect() {

    companion object {
        fun createSwarm(plugin: SOTFCEO_threatSwarm): RoilingSwarmEffect {
            val existing = RoilingSwarmEffect.getSwarmFor(plugin.objective)
            if (existing != null) return existing


//		if (true) {
//			return SwarmLauncherEffect.createTestDwellerSwarmFor(ship);
//		}
            val params = RoilingSwarmParams()

            params.flashRateMult = 0.25f
            params.flashCoreRadiusMult = 0f
            params.flashRadius = 120f
            params.flashFringeColor = Color(255, 0, 0, 40)
            params.flashCoreColor = Color(255, 255, 255, 127)


            // if this is set to true and the swarm is glowing, missile-fragments pop over the glow and it looks bad
            //params.renderFlashOnSameLayer = true;
            params.minOffset = 0f
            params.maxOffset = plugin.swarmRadius
            params.generateOffsetAroundAttachedEntityOval = true
            params.despawnSound = null // TODO: need this?
            params.spawnOffsetMult = 0.33f
            params.spawnOffsetMultForInitialSpawn = 1f

            params.baseMembersToMaintain = plugin.swarmSize
            params.memberRespawnRate = 500f // TODO: test?
            params.maxNumMembersToAlwaysRemoveAbove = params.baseMembersToMaintain * 2


            params.initialMembers = params.baseMembersToMaintain
            params.removeMembersAboveMaintainLevel = false

            return RoilingSwarmEffect(plugin.objective, params)
        }
    }

    open var swarmSize = 100
    var done = false
    var swarmRadius = 1000f

    override fun init(engine: CombatEngineAPI?, objective: BattleObjectiveAPI?) {
        super.init(engine, objective)

        createSwarm(this)
    }

    fun delete() {
        killSwarm()
        Global.getCombatEngine().removeEntity(this.objective)
        done = true
    }

    override fun advance(amount: Float) {
        if (done) return
        val engine = Global.getCombatEngine()

        if (engine.isPaused) return

        if (!isCaptured()) return
        captured(amount)
    }

    abstract fun captured(amount: Float)

    fun isCaptured(): Boolean {
        return objective.owner == 0 || objective.owner == 1
    }

    override fun getStatusItemsFor(ship: ShipAPI?): List<BattleObjectiveEffect.ShipStatusItem?>? {
        return null
    }

    fun killSwarm() {
        val swarm = createSwarm(this)

        swarm.isForceDespawn = true
    }
}