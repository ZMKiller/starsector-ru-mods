package data.scripts.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class CFT_regionalcapital extends BaseHazardCondition {
    
    @Override
    public void apply(String id) {
        if ("CFT".equals(market.getFactionId())) {
            market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, 1, null);
        } else {
            market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodifyFlat(id);
        }
    }
    
    @Override
    public void unapply(String id) {	
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodifyFlat(id);
    }
}