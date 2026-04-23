package data.scripts.campaign.econ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class dpl_MotivatedCadetsCondition extends BaseHazardCondition {
	public int modifier;
	public static Map<String, String> COMMODITY = new HashMap<String, String>();
	public static Map<String, Integer> MODIFIER = new HashMap<String, Integer>();
	public static Map<String, String> INDUSTRY = new HashMap<String, String>();
	public static Map<String, Integer> BASE_MODIFIER = new HashMap<String, Integer>();
	public static Set<String> BASE_ZERO  = new HashSet<String>();
	static {
		COMMODITY.put("dpl_unmotivated_cadets", "marines");
		
		MODIFIER.put("dpl_unmotivated_cadets", 1);
		
		INDUSTRY.put("marines", Industries.MILITARYBASE);
		
		BASE_MODIFIER.put("marines", 0);
		BASE_ZERO.add("marines");
	}
	
	public void apply(String id) {
		super.apply(id);
		return;
		
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
			
			Integer mod = modifier;
			if (mod != null) {
				CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(commodityId);
				
				String industryId = INDUSTRY.get(commodityId);
				IndustrySpecAPI ind = Global.getSettings().getIndustrySpec(industryId);
				String text = "";
				text = "No bonuses or penalties to " + spec.getName().toLowerCase() + " production (" + ind.getName() + ")";
				
				String str = "" + mod;
				float pad = 10f;
				tooltip.addPara(text, pad, Misc.getHighlightColor(), str);
			}
		}
	}
}




