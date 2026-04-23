package SOTFECO

import SOTFECO.SOTFECO_settings.ENABLE_FAB_OBJECTIVE
import SOTFECO.scripts.SOTFECO_flagScript
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import lunalib.lunaSettings.LunaSettingsListener

class SOFTECO_modPlugin: BaseModPlugin() {

    companion object {
        var lunaLibEnabled = false
        var SA_enabled = false
        var AOTDVaultsEnabled = false
        const val modId = "niko_SOTFMoreCombatObjectives"
    }

    override fun onApplicationLoad() {
        lunaLibEnabled = Global.getSettings().modManager.isModEnabled("lunalib")
        SA_enabled = Global.getSettings().modManager.isModEnabled("niko_stationAugments")
        AOTDVaultsEnabled = Global.getSettings().modManager.isModEnabled("aotd_vok")

        if (lunaLibEnabled) {
            LunaSettings.addSettingsListener(settingsChangedListener())
        }

        SOTFECO_settings.loadSettings()
    }

    class settingsChangedListener(): LunaSettingsListener {
        override fun settingsChanged(modID: String) {
            SOTFECO_settings.loadSettings()
        }
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        val flagScript = SOTFECO_flagScript()
        Global.getSector().addTransientScript(flagScript)
        Global.getSector().addTransientListener(flagScript)
    }

    override fun onAboutToLinkCodexEntries() {
        super.onAboutToLinkCodexEntries()

        CodexData.linkCodexEntries()
    }
}