package data.scripts.HSIGenerator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.characters.PersonAPI;

public class HSIMonthListener implements EconomyTickListener{

    @Override
    public void reportEconomyMonthEnd() {
        FactionAPI faction = Global.getSector().getFaction("HSI");
        if(faction==null) return;
        for(MarketAPI market:Global.getSector().getEconomy().getMarketsCopy()){
            if (market.getFaction() == faction) {
                boolean shouldAdd = true;
				for(PersonAPI person:market.getPeopleCopy()){
                    if(shouldAdd&&person.hasTag("HPSID")){
                        shouldAdd = false;
                        break;
                    }
                }
                if(shouldAdd){
                    HSIIPManager.createHPSIDOperator(market);
                }
			}
        }
    }

    @Override
    public void reportEconomyTick(int tick) {
        
    }
    
}
