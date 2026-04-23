package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.TTE_systems.TTE_Generator;


@SuppressWarnings("unchecked")
public class TTEGen implements SectorGeneratorPlugin {
	
 @Override  
   public void generate(SectorAPI sector){
        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("$nex_randomSector")) {	

        new TTE_Generator().generate(sector);				
      }
	  
    }	
}
