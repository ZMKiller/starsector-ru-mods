package shmoneelse.sicdustkeeper.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import data.scripts.campaign.ids.SotfIDs;
import data.scripts.utils.SotfMisc;

import java.util.Random;

public class sicdk_genXO implements ColonyInteractionListener {

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {
        if(market == null) return;
        if(!market.getId().equals("sotf_holdout_market")) return; // We at the right place?

        MemoryAPI sector_mem = Global.getSector().getMemoryWithoutUpdate();
        if(sector_mem.contains("$sicdk_madeXO")) // Backup in case this got loaded in again
        {
            removeMe();
            return;
        }

        Random rand = new Random();
        //if(rand.nextFloat() > .7f) return; // 70% chance to have XO in market each month


        // Make a random Dustkeeper
        PersonAPI person = market.getFaction().createRandomPerson(); // Random gender
        SotfMisc.dustkeeperifyAICore(person); // Proper Dustkeeper name for our XO
        person.setFaction(SotfIDs.DUSTKEEPERS);
        float pic = rand.nextFloat();
        if(pic < .25f)
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "red"));
        else if (pic < .5f)
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "yellow"));
        else if (pic < .75f)
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "white"));
        else
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "blue"));
        person.getMemoryWithoutUpdate().set("$sc_officer_aptitude","sc_dustkeeper");
        person.getMemoryWithoutUpdate().set("$sc_hireable", true);
        person.setPostId("executive_officer_sc_dustkeeper");

        market.getCommDirectory().addPerson(person);

        sector_mem.set("$sicdk_madeXO", true);
        removeMe();
    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {
    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
    }

    public void removeMe()
    {
        Global.getSector().getListenerManager().removeListenerOfClass(sicdk_genXO.class);
    }
}
