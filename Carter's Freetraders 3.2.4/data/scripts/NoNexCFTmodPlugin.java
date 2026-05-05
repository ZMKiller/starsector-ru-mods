package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import data.scripts.world.CFT_gen;
//import exerelin.campaign.SectorManager;


public class CFTmodPlugin extends BaseModPlugin {

    @Override

    public void onNewGame() {
        new CFT_gen().generate(Global.getSector());
    }
}