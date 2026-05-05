package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.systems.CFT_pengersick;


import java.io.IOException;
import java.util.List;

@SuppressWarnings("unchecked")
public class CFT_gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("$nex_randomSector")) {
            new CFT_pengersick().generate(sector);
        }
		
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("CFT");

        FactionAPI cft = sector.getFaction("CFT");
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT); 
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);   
        FactionAPI kol = sector.getFaction(Factions.KOL);	
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT); 
		FactionAPI persean = sector.getFaction(Factions.PERSEAN);
        FactionAPI guard = sector.getFaction(Factions.LIONS_GUARD);

        cft.setRelationship(player.getId(), 0.2f);	
        cft.setRelationship(hegemony.getId(), -0.2f);
        cft.setRelationship(tritachyon.getId(), -0.2f);
        cft.setRelationship(pirates.getId(), 0.1f);
        cft.setRelationship(independent.getId(), 0.4f);
        cft.setRelationship(persean.getId(), -0.15f);	
        cft.setRelationship(church.getId(), -0.3f);
        cft.setRelationship(path.getId(), 0f);    
        cft.setRelationship(kol.getId(), -0.15f);    
        cft.setRelationship(diktat.getId(), -0.1f);
        cft.setRelationship(guard.getId(), -0.2f);     
                 
         //modded factions
        cft.setRelationship("SCY", RepLevel.SUSPICIOUS);        
        cft.setRelationship("pn_colony", RepLevel.NEUTRAL);       
        cft.setRelationship("neutrinocorp", RepLevel.SUSPICIOUS);        
        cft.setRelationship("dassault_mikoyan", RepLevel.SUSPICIOUS); 
        cft.setRelationship("JYD", RepLevel.NEUTRAL);        
        cft.setRelationship("diableavionics", RepLevel.NEUTRAL);		
        cft.setRelationship("cabal", RepLevel.NEUTRAL);        
        cft.setRelationship("the_deserter", RepLevel.SUSPICIOUS);
        cft.setRelationship("blade_breakers", RepLevel.SUSPICIOUS);        
        cft.setRelationship("the_deserter", RepLevel.SUSPICIOUS);
		cft.setRelationship("kingdom_of_terra", RepLevel.SUSPICIOUS);
		cft.setRelationship("unitedpamed", RepLevel.SUSPICIOUS);		
        cft.setRelationship("brighton", RepLevel.NEUTRAL);
		cft.setRelationship("hiigaran_descendants", RepLevel.NEUTRAL);
		cft.setRelationship("prv", RepLevel.SUSPICIOUS);		
        cft.setRelationship("scalartech", RepLevel.SUSPICIOUS);
        cft.setRelationship("star_federation", RepLevel.SUSPICIOUS);
        cft.setRelationship("kadur_remnant", RepLevel.SUSPICIOUS);		
        cft.setRelationship("keruvim", RepLevel.NEUTRAL);
        cft.setRelationship("mayasura", RepLevel.NEUTRAL);	      
        cft.setRelationship("noir", RepLevel.NEUTRAL);
        cft.setRelationship("Lte", RepLevel.NEUTRAL);
        cft.setRelationship("GKSec", RepLevel.NEUTRAL);
        cft.setRelationship("gmda", RepLevel.SUSPICIOUS);
        cft.setRelationship("oculus", RepLevel.NEUTRAL);
        cft.setRelationship("nomads", RepLevel.SUSPICIOUS);
        cft.setRelationship("thulelegacy", RepLevel.SUSPICIOUS);
        cft.setRelationship("infected", RepLevel.HOSTILE);
        cft.setRelationship("ORA", RepLevel.SUSPICIOUS);
        cft.setRelationship("HMI", RepLevel.NEUTRAL);
        cft.setRelationship("draco", RepLevel.NEUTRAL);
        cft.setRelationship("roider", RepLevel.NEUTRAL);
        cft.setRelationship("ironshell", RepLevel.SUSPICIOUS);
        cft.setRelationship("magellan_protectorate", RepLevel.NEUTRAL);
        cft.setRelationship("exalted", RepLevel.HOSTILE);
        cft.setRelationship("fang", RepLevel.NEUTRAL);
        cft.setRelationship("xhanempire", RepLevel.NEUTRAL);
        cft.setRelationship("TTSC", RepLevel.NEUTRAL);		
        cft.setRelationship("xlu", RepLevel.NEUTRAL);
        cft.setRelationship("fpe", RepLevel.SUSPICIOUS);
        cft.setRelationship("al_ars", RepLevel.SUSPICIOUS);   

    }
}
