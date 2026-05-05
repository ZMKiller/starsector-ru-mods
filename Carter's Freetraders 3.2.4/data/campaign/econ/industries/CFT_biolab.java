package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.Pair;


public class CFT_biolab extends BaseIndustry {

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();		
		
		demand(Commodities.HEAVY_MACHINERY, size + 1); 
		demand(Commodities.SUPPLIES, size);		
		demand(Commodities.ORGANICS, size + 1);
		
		supply(Commodities.DRUGS, size + 1);	
	

		Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS);
		
		applyDeficitToProduction(1, deficit, Commodities.DRUGS);
		
		
		if (!isFunctional()) {
			supply.clear();
		}		

	}

	
	@Override
	public void unapply() {
		super.unapply();
	}

	
	public float getPatherInterest() {
		return 3f + super.getPatherInterest();
	}

	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
}
