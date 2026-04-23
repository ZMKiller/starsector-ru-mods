package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.TTSC_systems.TTSC_Generator;
import exerelin.campaign.SectorManager;


public class TTSCGen implements SectorGeneratorPlugin {
   
   public void generate(SectorAPI sector) {	
     if (!Global.getSettings().getModManager().isModEnabled("nexerelin") || SectorManager.getCorvusMode()) 
	 {
        new TTSC_Generator().generate(sector);		
      }
        FactionAPI TTSC = sector.getFaction("TTSC");
		

	        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("TTSC");	
			
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

        TTSC.setRelationship(player.getId(), 0.3f);	
        TTSC.setRelationship(hegemony.getId(), -0.2f);
        TTSC.setRelationship(tritachyon.getId(), 0.9f);
        TTSC.setRelationship(pirates.getId(), -0.3f);
        TTSC.setRelationship(independent.getId(), 0.3f);
        TTSC.setRelationship(persean.getId(), -0.15f);	
        TTSC.setRelationship(church.getId(), -0.5f);
        TTSC.setRelationship(path.getId(), -0.7f);    
        TTSC.setRelationship(kol.getId(), 0.15f);    
        TTSC.setRelationship(diktat.getId(), -0.1f);
        TTSC.setRelationship(guard.getId(), -0.25f);     
                
        //modded factions
        TTSC.setRelationship("SCY", RepLevel.WELCOMING);
        TTSC.setRelationship("CFT", RepLevel.WELCOMING);	
        TTSC.setRelationship("JYD", RepLevel.WELCOMING);			
        
        TTSC.setRelationship("pn_colony", RepLevel.NEUTRAL);       
        TTSC.setRelationship("neutrinocorp", RepLevel.NEUTRAL);
        TTSC.setRelationship("noir", RepLevel.NEUTRAL);
        TTSC.setRelationship("Lte", RepLevel.NEUTRAL);
        TTSC.setRelationship("GKSec", RepLevel.NEUTRAL);
        TTSC.setRelationship("gmda", RepLevel.NEUTRAL);
        TTSC.setRelationship("oculus", RepLevel.NEUTRAL);
        TTSC.setRelationship("nomads", RepLevel.NEUTRAL);
        TTSC.setRelationship("thulelegacy", RepLevel.NEUTRAL);		
        
        TTSC.setRelationship("dassault_mikoyan", RepLevel.SUSPICIOUS); 
        
        
        TTSC.setRelationship("diableavionics", RepLevel.INHOSPITABLE);  
        TTSC.setRelationship("infected", RepLevel.INHOSPITABLE);		
		
        TTSC.setRelationship("cabal", RepLevel.HOSTILE);        
        TTSC.setRelationship("the_deserter", RepLevel.HOSTILE);
        TTSC.setRelationship("blade_breakers", RepLevel.HOSTILE);        
        TTSC.setRelationship("the_deserter", RepLevel.HOSTILE);       
        TTSC.setRelationship("HIVER", RepLevel.HOSTILE); 
	
    }
}
