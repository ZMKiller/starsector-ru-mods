package data.scripts.HSIGenerator;

import java.util.List;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import data.scripts.HSIGenerator.HSI_system.HSI_Intrusion;

public class HSIGen implements SectorGeneratorPlugin {
    @Override
    public void generate(SectorAPI sector) {
        new HSI_Intrusion().generate(sector);
        FactionAPI HSI = sector.getFaction("HSI");

        // default relation
        if (HSI != null) {

            List<FactionAPI> allFactions = sector.getAllFactions();
            for (FactionAPI f : allFactions) {
                HSI.setRelationship(f.getId(), RepLevel.SUSPICIOUS);
            }
            FactionAPI player = sector.getFaction(Factions.PLAYER);
            FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
            FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
            FactionAPI pirates = sector.getFaction(Factions.PIRATES);
            FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
            FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
            FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
            FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
            FactionAPI persean = sector.getFaction(Factions.PERSEAN);
            FactionAPI guard = sector.getFaction(Factions.LIONS_GUARD);
            FactionAPI remnant = sector.getFaction(Factions.REMNANTS);
            FactionAPI omega = sector.getFaction(Factions.OMEGA);

            HSI.setRelationship(player.getId(), 0.1f);
            HSI.setRelationship(hegemony.getId(), 0.3f);
            HSI.setRelationship(tritachyon.getId(), -0.5f);
            HSI.setRelationship(pirates.getId(), -0.7f);
            HSI.setRelationship(independent.getId(), -0.1f);
            HSI.setRelationship(persean.getId(), -0.1f);
            HSI.setRelationship(church.getId(), 0f);
            HSI.setRelationship(path.getId(), -0.7f);
            HSI.setRelationship(diktat.getId(), -0.2f);
            HSI.setRelationship(remnant.getId(), 0.2f);
            HSI.setRelationship(omega.getId(), 0.2f);
        }

        FactionAPI HSI_GF3 = sector.getFaction("GF3");
        if(HSI_GF3!=null){
            HSI_GF3.setRelationship("HSI",1.0f);
        }

        FactionAPI HSI_SS = sector.getFaction("HSI_Stalker");
        if(HSI_SS!=null){
            HSI_SS.setRelationship("HSI",1.0f);
        }
    }
}
