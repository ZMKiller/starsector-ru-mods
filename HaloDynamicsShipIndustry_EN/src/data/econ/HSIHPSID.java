package data.econ;

import com.fs.starfarer.api.Global;
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

import java.awt.*;

public class HSIHPSID extends BaseIndustry{

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

        if (!isFunctional()) {
            supply.clear();
            unapply();
        }
    }


	@Override
	protected int getBaseStabilityMod() {
		return 2;
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		if(isDisrupted()){
			if(Math.random()>0.75f&&getDisruptedDays()>1){
				setDisrupted(getDisruptedDays()- 2*Global.getSector().getClock().convertToDays(amount));
			}
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