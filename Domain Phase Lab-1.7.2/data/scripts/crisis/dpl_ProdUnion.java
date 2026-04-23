package data.scripts.crisis;

import java.util.Map;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_ProdUnion extends BaseMarketConditionPlugin {
    public static float PROD_BONUS = 1f;

    public dpl_ProdUnion() {
    }

    @Override
    public void apply(String id) {
        String text = Misc.ucFirst(getName().toLowerCase());
        for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
				for (MutableCommodityQuantity demand: ind.getAllDemand()) {
					demand.getQuantity().modifyFlat("dpl_prod_union", PROD_BONUS);
				}
				for (MutableCommodityQuantity supply: ind.getAllSupply()) {
					supply.getQuantity().modifyFlat("dpl_prod_union", PROD_BONUS);
				}
			}
		}
    }

    @Override
    public void unapply(String id) {
    	for (Industry ind : market.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
				for (MutableCommodityQuantity demand: ind.getAllDemand()) {
					demand.getQuantity().unmodifyFlat("dpl_prod_union");
				}
				for (MutableCommodityQuantity supply: ind.getAllSupply()) {
					supply.getQuantity().unmodifyFlat("dpl_prod_union");
				}
			}
		}
    }
    
    @Override
	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
	}

	@Override
	public String[] getHighlights() {
		return super.getHighlights();
	}

    @Override
    public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        float opad = 10f;
        tooltip.addPara("%s unit of industrial production and demand", opad, Misc.getHighlightColor(), "+" + PROD_BONUS);
    }

    @Override
    public boolean hasCustomTooltip() {
        return true;
    }
}