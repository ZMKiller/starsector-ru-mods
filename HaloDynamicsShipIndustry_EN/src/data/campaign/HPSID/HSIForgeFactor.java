package data.campaign.HPSID;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import data.kit.HSII18nUtil;

public class HSIForgeFactor extends BaseOneTimeFactor {

    public HSIForgeFactor(int points) {
        super(points);
    }

    protected String getBulletPointText(BaseEventIntel intel) {
        return HSII18nUtil.getCampaignString("HPSIDForgeDesc");
    }

    @Override
	public String getDesc(BaseEventIntel intel) {
		return HSII18nUtil.getCampaignString("HPSIDForgeDesc")+"+"+points+"pt";
	}

	public TooltipCreator getMainRowTooltip(final BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara(getDesc(intel),
						0f);
			}
			
		};
	}
}
