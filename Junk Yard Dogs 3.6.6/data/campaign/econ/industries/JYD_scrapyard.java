package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.Pair;


public class JYD_scrapyard extends BaseIndustry {

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();		
		
		demand(Commodities.HEAVY_MACHINERY, size); 
		demand(Commodities.SUPPLIES, size);		
		demand(Commodities.SHIPS, size);
		
		supply(Commodities.METALS, size + 1);	
		supply(Commodities.RARE_METALS, size);
		supply(Commodities.FUEL, size);			

		Pair<String, Integer> deficit = getMaxDeficit(Commodities.SHIPS);
		
		applyDeficitToProduction(1, deficit, Commodities.RARE_METALS);
		applyDeficitToProduction(1, deficit, Commodities.METALS);
		applyDeficitToProduction(1, deficit, Commodities.FUEL);			
		
		if (!isFunctional()) {
			supply.clear();
		}		

	}

	
	@Override
	public void unapply() {
		super.unapply();
	}

	
	public float getPatherInterest() {
		return 1f + super.getPatherInterest();
	}

	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
}
