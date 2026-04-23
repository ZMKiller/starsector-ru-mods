package SOTFECO

import SOTFECO.SOFTECO_modPlugin.Companion.SA_enabled
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.codex.CodexDataV2
import niko_SA.codex.CodexData.getAugmentEntryId

object CodexData {
    fun linkCodexEntries() {
        if (SA_enabled) {
            createReciprocalLink(getAugmentEntryId("SOTFECO_asbUplinkAugment"), CodexDataV2.getIndustryEntryId(Industries.HEAVYBATTERIES))
            createReciprocalLink(getAugmentEntryId("SOTFECO_asbUplinkAugment"), CodexDataV2.getIndustryEntryId(Industries.GROUNDDEFENSES))
        }
    }

    private fun createReciprocalLink(entryIdOne: String, entryIdTwo: String) {
        val entryOne = CodexDataV2.getEntry(entryIdOne)
        val entryTwo = CodexDataV2.getEntry(entryIdTwo)

        entryOne.addRelatedEntry(entryTwo)
        entryTwo.addRelatedEntry(entryOne)
    }
}