package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.Pair;


public class CFT_greyindustry extends BaseIndustry {

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();		
		
		demand(Commodities.HEAVY_MACHINERY, size +2);
		demand(Commodities.SUPPLIES, size +1);		
		demand(Commodities.ORGANICS, size+1);
		demand(Commodities.METALS, size +2);		
		
		supply(Commodities.DRUGS, size +2);	
		supply(Commodities.HAND_WEAPONS, size +2);		

		Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS, Commodities.METALS);		
		applyDeficitToProduction(1, deficit, Commodities.DRUGS);
		applyDeficitToProduction(1, deficit, Commodities.HAND_WEAPONS);	
		
		if (!isFunctional()) {
			supply.clear();
		}		

	}

	
	@Override
	public void unapply() {
		super.unapply();
	}

	
	public float getPatherInterest() {
		return 2f + super.getPatherInterest();
	}

	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
}
