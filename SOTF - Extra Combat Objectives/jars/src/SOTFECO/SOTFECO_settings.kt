package SOTFECO

import SOTFECO.SOFTECO_modPlugin.Companion.modId
import com.fs.starfarer.api.Global
import data.scripts.SotfModPlugin
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.json.iterator

object SOTFECO_settings {

    //var ABYSSAL_LIGHT_OBJ_ENABLED = true
    var DO_PLAYER_ASB_WARNING = true
    var ENABLE_FAB_OBJECTIVE = false
    var IGNORE_PREVIOUS_ENCOUNTER_REQS = false

    fun loadSettings() {
        DO_PLAYER_ASB_WARNING = LunaSettings.getBoolean(modId, "SOTFECO_doPlayerASBWarning")!!
        ENABLE_FAB_OBJECTIVE = LunaSettings.getBoolean(modId, "SOTFECO_enableFabObjective")!!
        IGNORE_PREVIOUS_ENCOUNTER_REQS = LunaSettings.getBoolean(modId, "SOTFECO_ignorePreviousEncounterReqs")!!
        toggleFlag("\$SOTFECO_enableFabObjective", ENABLE_FAB_OBJECTIVE)
    }

    fun toggleFlag(flag: String, value: Boolean) {
        val memory = Global.getSector()?.memoryWithoutUpdate ?: return

        var flag = flag
        val first = flag.firstOrNull() ?: return
        if (first != '$') {
            flag = "$$flag"
        }

        if (value) {
            memory.set(flag, true)
        } else {
            memory.unset(flag)
        }
    }

}
