package data.econ;

import com.fs.starfarer.api.impl.campaign.econ.ConditionData;
import com.fs.starfarer.api.impl.campaign.econ.WorldFarming;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class HSIHeatingWorld extends WorldFarming{
    public HSIHeatingWorld() {
		super(ConditionData.WORLD_WATER_FARMING_MULT, ConditionData.WORLD_BARREN_MARGINAL_MACHINERY_MULT);
	}

    public void apply(String id) {
        market.getDemand(Commodities.FUEL).getDemand().modifyFlat(id, 2);
	}

	public void unapply(String id) {
        market.getDemand(Commodities.FUEL).getDemand().unmodify(id);
	}
}
