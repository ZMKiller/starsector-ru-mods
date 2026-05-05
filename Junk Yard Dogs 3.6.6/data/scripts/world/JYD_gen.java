package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.systems.JYD_dogstar;


import java.io.IOException;
import java.util.List;

@SuppressWarnings("unchecked")
public class JYD_gen implements SectorGeneratorPlugin {

@Override
    public void generate(SectorAPI sector) {
        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("$nex_randomSector")) {
            new JYD_dogstar().generate(sector);
        }
		
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("JYD");

        FactionAPI jyd = sector.getFaction("JYD");
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

        jyd.setRelationship(player.getId(), 0.3f);	
        jyd.setRelationship(hegemony.getId(), -0.1f);
        jyd.setRelationship(tritachyon.getId(), 0.1f);
        jyd.setRelationship(pirates.getId(), -0.7f);
        jyd.setRelationship(independent.getId(), 0.75f);
        jyd.setRelationship(persean.getId(), -0.15f);	
        jyd.setRelationship(church.getId(), 0.3f);
        jyd.setRelationship(path.getId(), -0.1f);    
        jyd.setRelationship(kol.getId(), 0.15f);    
        jyd.setRelationship(diktat.getId(), -0.1f);
        jyd.setRelationship(guard.getId(), -0.25f);     
                
        //modded factions
        jyd.setRelationship("SCY", RepLevel.WELCOMING);
        jyd.setRelationship("CFT", RepLevel.WELCOMING);		
        
        jyd.setRelationship("pn_colony", RepLevel.NEUTRAL);       
        jyd.setRelationship("neutrinocorp", RepLevel.NEUTRAL);
        jyd.setRelationship("noir", RepLevel.NEUTRAL);
        jyd.setRelationship("Lte", RepLevel.NEUTRAL);
        jyd.setRelationship("GKSec", RepLevel.NEUTRAL);
        jyd.setRelationship("gmda", RepLevel.NEUTRAL);
        jyd.setRelationship("oculus", RepLevel.NEUTRAL);
        jyd.setRelationship("nomads", RepLevel.NEUTRAL);
        jyd.setRelationship("thulelegacy", RepLevel.NEUTRAL);	
        jyd.setRelationship("TTSC", RepLevel.NEUTRAL);		
        
        jyd.setRelationship("dassault_mikoyan", RepLevel.SUSPICIOUS); 
        
        
        jyd.setRelationship("diableavionics", RepLevel.INHOSPITABLE);  
        jyd.setRelationship("infected", RepLevel.INHOSPITABLE);		
		
        jyd.setRelationship("cabal", RepLevel.HOSTILE);        
        jyd.setRelationship("the_deserter", RepLevel.HOSTILE);
        jyd.setRelationship("blade_breakers", RepLevel.HOSTILE);        
        jyd.setRelationship("the_deserter", RepLevel.HOSTILE);       
        jyd.setRelationship("HIVER", RepLevel.HOSTILE); 
	
    }
}
