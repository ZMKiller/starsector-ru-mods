package data.campaign.HPSID;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import data.kit.HSII18nUtil;

public class HSIHPSIDMonthlyFactor extends BaseEventFactor{
    
    public int getProgress(BaseEventIntel intel) {
        int relEffect = 0;
		float rel = (Global.getSector().getFaction("HSI")!=null)?Global.getSector().getFaction("HSI").getRelToPlayer().getRel():0;
		if(rel<0){
			relEffect = (int)(-100*rel);
		}else{
			relEffect = (int)(8*rel);
		}
        return relEffect;
	}

    public String getDesc(BaseEventIntel intel) {
		return HSII18nUtil.getCampaignString("HSI_HPSIDRelFactor");
	}
}
