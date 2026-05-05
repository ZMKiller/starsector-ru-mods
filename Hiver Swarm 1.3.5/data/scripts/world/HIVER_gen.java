package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.systems.HIVER_Zesketet;
import data.scripts.world.systems.HIVER_Kiztac;
import data.scripts.world.systems.HIVER_Rizdet;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("unchecked")
public class HIVER_gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
	
        new HIVER_Zesketet().generate(sector);
        new HIVER_Kiztac().generate(sector);
        new HIVER_Rizdet().generate(sector);			
		
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("HIVER");

        FactionAPI HIVER = sector.getFaction("HIVER");
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

        HIVER.setRelationship(player.getId(), -0.7f);	
        HIVER.setRelationship(hegemony.getId(), -0.7f);
        HIVER.setRelationship(tritachyon.getId(), -0.7f);
        HIVER.setRelationship(pirates.getId(), -0.77f);
        HIVER.setRelationship(independent.getId(), -0.75f);
        HIVER.setRelationship(persean.getId(), -0.75f);	
        HIVER.setRelationship(church.getId(), -0.7f);
        HIVER.setRelationship(path.getId(), -0.7f);    
        HIVER.setRelationship(kol.getId(), -0.75f);    
        HIVER.setRelationship(diktat.getId(), -0.7f);
        HIVER.setRelationship(guard.getId(), -0.75f);     
  
        HIVER.setRelationship("player", RepLevel.VENGEFUL);  
		
        //modded factions
        HIVER.setRelationship("SCY", RepLevel.VENGEFUL);        
        HIVER.setRelationship("pn_colony", RepLevel.VENGEFUL);       
        HIVER.setRelationship("neutrinocorp", RepLevel.VENGEFUL);        
        HIVER.setRelationship("dassault_mikoyan", RepLevel.VENGEFUL); 
        HIVER.setRelationship("JYD", RepLevel.VENGEFUL);        
        HIVER.setRelationship("diableavionics", RepLevel.INHOSPITABLE);		
        HIVER.setRelationship("cabal", RepLevel.VENGEFUL);        
        HIVER.setRelationship("the_deserter", RepLevel.VENGEFUL);
        HIVER.setRelationship("blade_breakers", RepLevel.VENGEFUL);        
        HIVER.setRelationship("the_deserter", RepLevel.VENGEFUL);
		HIVER.setRelationship("kingdom_of_terra", RepLevel.VENGEFUL);
		HIVER.setRelationship("unitedpamed", RepLevel.VENGEFUL);		
        HIVER.setRelationship("brighton", RepLevel.VENGEFUL);
		HIVER.setRelationship("hiigaran_descendants", RepLevel.VENGEFUL);
		HIVER.setRelationship("prv", RepLevel.VENGEFUL);		
        HIVER.setRelationship("scalartech", RepLevel.VENGEFUL);
        HIVER.setRelationship("star_federation", RepLevel.VENGEFUL);
        HIVER.setRelationship("kadur_remnant", RepLevel.VENGEFUL);		
        HIVER.setRelationship("keruvim", RepLevel.VENGEFUL);
        HIVER.setRelationship("mayasura", RepLevel.VENGEFUL);	      
        HIVER.setRelationship("noir", RepLevel.VENGEFUL);
        HIVER.setRelationship("Lte", RepLevel.VENGEFUL);
        HIVER.setRelationship("GKSec", RepLevel.VENGEFUL);
        HIVER.setRelationship("gmda", RepLevel.VENGEFUL);
        HIVER.setRelationship("oculus", RepLevel.VENGEFUL);
        HIVER.setRelationship("nomads", RepLevel.VENGEFUL);
        HIVER.setRelationship("thulelegacy", RepLevel.VENGEFUL);
        HIVER.setRelationship("infected", RepLevel.VENGEFUL);
        HIVER.setRelationship("ORA", RepLevel.VENGEFUL);
        HIVER.setRelationship("HMI", RepLevel.VENGEFUL);
        HIVER.setRelationship("draco", RepLevel.VENGEFUL);
        HIVER.setRelationship("roider", RepLevel.VENGEFUL);
        HIVER.setRelationship("ironshell", RepLevel.VENGEFUL);
        HIVER.setRelationship("magellan_protectorate", RepLevel.VENGEFUL);
        HIVER.setRelationship("exalted", RepLevel.VENGEFUL);
        HIVER.setRelationship("fang", RepLevel.VENGEFUL);
        HIVER.setRelationship("xhanempire", RepLevel.VENGEFUL);
        HIVER.setRelationship("xlu", RepLevel.VENGEFUL);
        HIVER.setRelationship("fpe", RepLevel.VENGEFUL);
        HIVER.setRelationship("al_ars", RepLevel.VENGEFUL); 
        HIVER.setRelationship("UAF", RepLevel.VENGEFUL);  
        HIVER.setRelationship("Imperium", RepLevel.VENGEFUL); 
        HIVER.setRelationship("sotf_dustkeepers", RepLevel.VENGEFUL);  
        HIVER.setRelationship("sotf_dustkeepers_proxies", RepLevel.VENGEFUL);  
        HIVER.setRelationship("sotf_sierra_faction", RepLevel.VENGEFUL);  
        HIVER.setRelationship("sotf_taken", RepLevel.VENGEFUL); 		

List<FactionAPI> factionList = sector.getAllFactions();
        FactionAPI hivers = sector.getFaction("HIVER");

        for (FactionAPI faction : factionList) {
            if (faction != hivers &&
                !faction.isNeutralFaction()) {
                hivers.setRelationship(faction.getId(), RepLevel.VENGEFUL);
            }
		}
    }
}
