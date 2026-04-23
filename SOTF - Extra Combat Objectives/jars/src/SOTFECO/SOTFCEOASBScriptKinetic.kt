package SOTFECO

import java.awt.Color

class SOTFCEOASBScriptKinetic: SOTFCEOASBScript() {
    override fun getWeaponId(): String {
        return "SOTFECO_asb_kinetic"
    }

    override fun getRingColor(): Color? {
        return Color(240, 250, 255, 255)
    }

    override fun getTypeString(): String {
        return "KINETIC"
    }
}