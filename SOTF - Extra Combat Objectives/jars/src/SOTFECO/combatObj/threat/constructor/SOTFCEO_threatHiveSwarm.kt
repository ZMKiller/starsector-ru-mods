package SOTFECO.combatObj.threat.constructor

import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript

class SOTFCEO_threatHiveSwarm: SOTFCEO_threatConstructionSwarm() {

    override fun getToPickFrom(): Collection<ConstructionSwarmSystemScript.SwarmConstructableVariant> {
        return ConstructionSwarmSystemScript.CONSTRUCTABLE.filter { it.variantId.contains("hive") }.shuffled()
    }

}