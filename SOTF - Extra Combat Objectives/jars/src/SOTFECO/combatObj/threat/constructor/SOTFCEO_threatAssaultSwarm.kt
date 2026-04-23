package SOTFECO.combatObj.threat.constructor

import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript

class SOTFCEO_threatAssaultSwarm: SOTFCEO_threatConstructionSwarm() {

    override var swarmSize: Int = 220 // assault units are STRONG

    override fun getToPickFrom(): Collection<ConstructionSwarmSystemScript.SwarmConstructableVariant> {
        return ConstructionSwarmSystemScript.CONSTRUCTABLE.filter { it.variantId.contains("assault") }.shuffled()
    }

}