package SOTFECO.combatObj.threat.constructor

import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript.SwarmConstructableType
import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript.SwarmConstructableVariant

class SOTFCEO_threatFabSwarm: SOTFCEO_threatConstructionSwarm() {

    companion object {
        var fabVariant: SwarmConstructableVariant = SwarmConstructableVariant(
            SwarmConstructableType.COMBAT_UNIT,
            "fabricator_unit_Type450"
        )
    }

    override var bonusOverseers: Int = 0
    override var swarmSize = fabVariant.fragments

    override fun getToPickFrom(): Collection<SwarmConstructableVariant> {
        return listOf(fabVariant)
    }

}