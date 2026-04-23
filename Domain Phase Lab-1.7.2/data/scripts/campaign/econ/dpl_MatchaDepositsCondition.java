package data.scripts.campaign.econ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class dpl_MatchaDepositsCondition extends BaseHazardCondition {
	
	public static Map<String, String> COMMODITY = new HashMap<String, String>();
	public static Map<String, Integer> MODIFIER = new HashMap<String, Integer>();
	public static Map<String, String> INDUSTRY = new HashMap<String, String>();
	public static Map<String, Integer> BASE_MODIFIER = new HashMap<String, Integer>();
	public static Set<String> BASE_ZERO  = new HashSet<String>();
	static {
		COMMODITY.put("dpl_matcha_makers", "dpl_matcha");
		
		MODIFIER.put("dpl_matcha_makers", 1);
		
		INDUSTRY.put("dpl_matcha", Industries.FARMING);
		
		BASE_MODIFIER.put("dpl_matcha", 0);
		BASE_ZERO.add("dpl_matcha");
	}
	
	public void apply(String id) {
		super.apply(id);
		
		String commodityId = COMMODITY.get(condition.getId());
		if (commodityId == null) return;
		
		Integer mod = MODIFIER.get(condition.getId());
		if (mod == null) return;
		
		Integer baseMod = BASE_MODIFIER.get(commodityId);
		if (baseMod == null) return;
		
		String industryId = INDUSTRY.get(commodityId);
		if (industryId == null) return;
		
		Industry industry = market.getIndustry(industryId);
		if (industry == null) {
			if (Industries.FARMING.equals(industryId)) {
				List<Industry> AllIndustries = market.getIndustries();
				for (Industry the_industry : AllIndustries) {
					if (the_industry.getSpec().hasTag(Industries.FARMING)) {
						industry = the_industry;
					}
				}
			}
			if (industry == null) return;
		}

		int size = market.getSize();
		if (BASE_ZERO.contains(commodityId)) {
			size = 0;
		}
		
		int base = size + baseMod;

		if (industry.isFunctional()) {
			industry.supply(id + "_0", commodityId, base, BaseIndustry.BASE_VALUE_TEXT);
			industry.supply(id + "_1", commodityId, mod, Misc.ucFirst(condition.getName().toLowerCase()));
		} else {
			industry.getSupply(commodityId).getQuantity().unmodifyFlat(id + "_0");
			industry.getSupply(commodityId).getQuantity().unmodifyFlat(id + "_1");
		}
		
// uncomment to make farming provide organics
// also need to adjust Farming to apply machinery deficit penalty
//		if ((Industries.FARMING.equals(industryId) ||
//				Industries.AQUACULTURE.equals(industryId) && Commodities.FOOD.equals(commodityId))) {
//			industry.getSupply(Commodities.ORGANICS).getQuantity().modifyFlat(id + "_0", size - 2, BaseIndustry.BASE_VALUE_TEXT);
//			industry.getSupply(Commodities.ORGANICS).getQuantity().modifyFlat(id + "_1", mod, Misc.ucFirst(condition.getName().toLowerCase()));
//		}
	}
	
	public void unapply(String id) {
		super.unapply(id);
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
	}

	@Override
	public String[] getHighlights() {
		return super.getHighlights();
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		
		String commodityId = COMMODITY.get(condition.getId());
		if (commodityId != null) {
			
			Integer mod = MODIFIER.get(condition.getId());
			if (mod != null) {
				CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
				
				String industryId = INDUSTRY.get(commodityId);
				IndustrySpecAPI ind = Global.getSettings().getIndustrySpec(industryId);
				
				
				String str = "" + mod;
				if (mod > 0) str = "+" + mod;
				String text = "";
				if (mod == 0) {
					text = "No bonuses or penalties to " + spec.getName().toLowerCase() + " production (" + ind.getName() + ")";
				} else {
					//text = "" + str + " to " + spec.getName().toLowerCase() + " production.";
					text = "" + str + " " + spec.getName().toLowerCase() + " production (" + ind.getName() + ")";
				}
				float pad = 10f;
				tooltip.addPara(text, pad, Misc.getHighlightColor(), str);
			}
		}
	}
}




