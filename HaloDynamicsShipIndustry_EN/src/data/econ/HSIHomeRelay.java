package data.econ;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSIHomeRelay extends BaseIndustry{

    @Override
    public boolean isHidden() {
        return !market.getFactionId().equals("HSI");
    }

    @Override
    public boolean isFunctional() {
        return super.isFunctional() && market.getFactionId().equals("HSI");
    }

    public void apply() {
        super.apply(true);

        int size = market.getSize();

        modifyStabilityWithBaseMod();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);

        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(getModId(1), 0.5f,
                HSII18nUtil.getEconString("HSIHomeRelayDesc0"));

        if (!isFunctional()) {
            supply.clear();
            unapply();
        }
    }

    @Override
    public void unapply() {
        super.unapply();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(getModId(1));

        unmodifyStabilityWithBaseMod();
    }

    @Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		//if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {			
			boolean works = Industries.ORBITALWORKS.equals(getId());
			if (works) {
				float total = 0.5f;
				String totalStr = "+" + (int)Math.round(total * 100f) + "%";
				Color h = Misc.getHighlightColor();
				if (total < 0) {
					h = Misc.getNegativeHighlightColor();
					totalStr = "" + (int)Math.round(total * 100f) + "%";
				}
				float opad = 10f;
				if (total >= 0) {
					tooltip.addPara(HSII18nUtil.getEconString("HSIShipQuality0"), opad, h, totalStr);
					tooltip.addPara(HSII18nUtil.getEconString("HSIShipQuality1"), 
							Misc.getGrayColor(), opad);
				}
			}
		}
	}
	
	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
	
	@Override
	public boolean wantsToUseSpecialItem(SpecialItemData data) {
		return false;
	}

	@Override
	public boolean isAvailableToBuild() {
		return false;
	}
	
	public boolean showWhenUnavailable() {
		return false;
	}

	@Override
	public boolean canImprove() {
		return false;
	}
}