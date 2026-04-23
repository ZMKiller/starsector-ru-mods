package SOTFECO.combatObj.threat.attack

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.impl.combat.threat.FragmentSwarmHullmod
import com.fs.starfarer.api.impl.combat.threat.SwarmLauncherEffect
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class SOTFCEO_threatAttackSwarmImpl: SOTFCEO_threatAttackSwarm() {

    override fun triggerAttack() {

        val manager: CombatFleetManagerAPI = engine.getFleetManager(objective.owner)
        manager.isSuppressDeploymentMessages = true

        manager.isSuppressDeploymentMessages = false
        val ourSwarm = createSwarm(this)
        val fighter = manager.spawnShipOrWing(
            SwarmLauncherEffect.ATTACK_SWARM_WING,
            objective.location, MathUtils.getRandomNumberInRange(0f, 360f), 0f, null
        )

        //Global.getSoundPlayer().playSound("threat_swarm_launched", 1f, 1f, objective.location, Misc.ZERO);
        for (curr in fighter.wing.wingMembers) {
            curr.isDoNotRender = true
            curr.explosionScale = 0f
            curr.hulkChanceOverride = 0f
            curr.impactVolumeMult = SwarmLauncherEffect.IMPACT_VOLUME_MULT
            curr.armorGrid.clearComponentMap() // no damage to weapons/engines
            //Vector2f.add(curr.velocity, takeoffVel, curr.velocity)

            /*var count = 0
            for (fragment in ourSwarm.members.shuffled()) {
                count++
                if (count != 5) continue // one in x get the arc
            }*/
        }

        val swarm = FragmentSwarmHullmod.createSwarmFor(fighter)
        ourSwarm.transferMembersTo(swarm, 1f)

        swarm.params.minOffset = 0f
        swarm.params.maxOffset = swarmRadius * 0.5f
    }
}