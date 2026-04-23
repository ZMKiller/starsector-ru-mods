package data.scripts.util;

import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class UW_Defs {

    public static final WeightedRandomPicker<String> SCRAPYARD_FACTIONS = new WeightedRandomPicker<>();

    static {
        SCRAPYARD_FACTIONS.add(Factions.HEGEMONY, 4f);
        SCRAPYARD_FACTIONS.add(Factions.DIKTAT, 2f);
        SCRAPYARD_FACTIONS.add(Factions.INDEPENDENT, 5f);
        SCRAPYARD_FACTIONS.add(Factions.LIONS_GUARD, 1f);
        SCRAPYARD_FACTIONS.add(Factions.LUDDIC_CHURCH, 2f);
        SCRAPYARD_FACTIONS.add(Factions.LUDDIC_PATH, 2f);
        SCRAPYARD_FACTIONS.add(Factions.PIRATES, 10f);
        SCRAPYARD_FACTIONS.add(Factions.TRITACHYON, 3f);
        SCRAPYARD_FACTIONS.add(Factions.PERSEAN, 2f);
        SCRAPYARD_FACTIONS.add("cabal", 1f);
        SCRAPYARD_FACTIONS.add("interstellarimperium", 3f);
        SCRAPYARD_FACTIONS.add("exipirated", 3f);
        SCRAPYARD_FACTIONS.add("shadow_industry", 2f);
        SCRAPYARD_FACTIONS.add("junk_pirates", 2f);
        SCRAPYARD_FACTIONS.add("pack", 1f);
        SCRAPYARD_FACTIONS.add("syndicate_asp", 1f);
        SCRAPYARD_FACTIONS.add("SCY", 1f);
        SCRAPYARD_FACTIONS.add("tiandong", 3f);
        SCRAPYARD_FACTIONS.add("diableavionics", 1f);
        SCRAPYARD_FACTIONS.add("ORA", 1f);
    }
}
