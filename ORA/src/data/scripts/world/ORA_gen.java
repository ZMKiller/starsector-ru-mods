package data.scripts.world;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import data.scripts.world.systems.ORA_godunov;
import data.scripts.world.systems.ORA_groom;
import data.scripts.world.systems.ORA_joy;
import data.scripts.world.systems.ORA_malachite;
import data.scripts.world.systems.ORA_valis;

@SuppressWarnings("unchecked")
public class ORA_gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
	
        new ORA_godunov().generate(sector);
        new ORA_malachite().generate(sector);
        new ORA_valis().generate(sector);
        new ORA_groom().generate(sector);
        new ORA_joy().generate(sector);

        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("ORA");   
//    }
//    
//    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI ora = sector.getFaction("ORA");
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

        ora.setRelationship(player.getId(), 0.2f);	
        ora.setRelationship(hegemony.getId(), -0.4f);
        ora.setRelationship(tritachyon.getId(), 0.5f);
        ora.setRelationship(pirates.getId(), -0.7f);
        ora.setRelationship(independent.getId(), 0.15f);
        ora.setRelationship(persean.getId(), -0.15f);	
        ora.setRelationship(church.getId(), 0.4f);
        ora.setRelationship(path.getId(), 0f);    
        ora.setRelationship(kol.getId(), 0.15f);    
        ora.setRelationship(diktat.getId(), -0.2f);
        ora.setRelationship(guard.getId(), -0.25f);     
                
        //modded factions
        ora.setRelationship("SCY", RepLevel.WELCOMING);
        ora.setRelationship("shadow_industry", RepLevel.FAVORABLE);
        ora.setRelationship("syndicate_asp", RepLevel.WELCOMING);
        
        ora.setRelationship("citadeldefenders", RepLevel.FAVORABLE);     
        ora.setRelationship("tiandong", RepLevel.FAVORABLE);        
        ora.setRelationship("metelson", RepLevel.FAVORABLE);  
        ora.setRelationship("Coalition", RepLevel.FAVORABLE);         
        
        ora.setRelationship("sun_ice", RepLevel.NEUTRAL);
        ora.setRelationship("pn_colony", RepLevel.NEUTRAL);       
        ora.setRelationship("neutrinocorp", RepLevel.NEUTRAL);    
        
        ora.setRelationship("dassault_mikoyan", RepLevel.SUSPICIOUS); 
        ora.setRelationship("interstellarimperium", RepLevel.SUSPICIOUS);
        ora.setRelationship("blackrock_driveyards", RepLevel.SUSPICIOUS);
        
        ora.setRelationship("pack", RepLevel.INHOSPITABLE);       
        ora.setRelationship("6eme_bureau", RepLevel.INHOSPITABLE);
        
        ora.setRelationship("diableavionics", RepLevel.HOSTILE);  
        ora.setRelationship("mayorate", RepLevel.HOSTILE);
        ora.setRelationship("pirateAnar", RepLevel.HOSTILE);
        ora.setRelationship("sun_ici", RepLevel.HOSTILE);
        ora.setRelationship("junk_pirates", RepLevel.HOSTILE);
        ora.setRelationship("exigency", RepLevel.HOSTILE);
        ora.setRelationship("exipirated", RepLevel.HOSTILE);        
        ora.setRelationship("cabal", RepLevel.HOSTILE);        
        ora.setRelationship("the_deserter", RepLevel.HOSTILE);
        ora.setRelationship("blade_breakers", RepLevel.HOSTILE);        
        ora.setRelationship("the_deserter", RepLevel.HOSTILE);
        
        ora.setRelationship("crystanite", RepLevel.VENGEFUL);          
        ora.setRelationship("new_galactic_order", RepLevel.VENGEFUL); 
        ora.setRelationship("explorer_society", RepLevel.VENGEFUL); 
        
        
        ora.setRelationship("noir", RepLevel.NEUTRAL);
        ora.setRelationship("Lte", RepLevel.NEUTRAL);
        ora.setRelationship("GKSec", RepLevel.NEUTRAL);
        ora.setRelationship("gmda", RepLevel.NEUTRAL);
        ora.setRelationship("oculus", RepLevel.NEUTRAL);
        ora.setRelationship("nomads", RepLevel.NEUTRAL);
        ora.setRelationship("thulelegacy", RepLevel.NEUTRAL);
        ora.setRelationship("infected", RepLevel.NEUTRAL);   
    }
}
