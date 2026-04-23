package SOTFECO

import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SOTFCEOASBScriptHE: SOTFCEOASBScript() {
    override fun getWeaponId(): String {
        return "sotf_asb"
    }

    override fun getRingColor(): Color? {
        return Misc.getNegativeHighlightColor()
    }

    override fun getTypeString(): String {
        return "HIGH EXPLOSIVE"
    }
}