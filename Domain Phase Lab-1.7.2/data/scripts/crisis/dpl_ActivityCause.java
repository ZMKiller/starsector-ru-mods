package data.scripts.crisis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.HegemonyHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

public class dpl_ActivityCause extends BaseHostileActivityCause2 {
	public static int IGNORE_COLONY_THRESHOLD = 3;

    public dpl_ActivityCause(HostileActivityEventIntel intel) {
        super(intel);
    }

    @Override
    public TooltipCreator getTooltip() {
        return new BaseFactorTooltip() {
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
            	tooltip.addPara("The Phase Lab is interested in the industrial potential of your colony "
						+ "though they would ignore colonies of size %s or smaller.", 0f,
						Misc.getHighlightColor(), "" + IGNORE_COLONY_THRESHOLD);
            	tooltip.addPara("You received an anonymous invitation for a 'peaceful negotiation' about "
            			+ "your colonies at Lab Security Department planet. This looks like a blackmail.", 0f,
						Misc.getHighlightColor(), "Lab Security Department planet");
            	tooltip.addPara("Making a complaint at Research Site V planet might also help in stopping "
            			+ "these attacks.", 0f,
						Misc.getHighlightColor(), "Research Site V planet");
            }
        };
    }
    
    public boolean dpl_DealtWithBadGuy() {
    	boolean DealtWithBadGuy = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_DealtWithBadGuy");
		return DealtWithBadGuy;
	}
    
    public boolean dpl_DefeatedDPLAttack() {
    	boolean DefeatedDPLAttack = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_defeatedDPLAttack");
		return DefeatedDPLAttack;
	}

    @Override
    public boolean shouldShow() {
        return getProgress() != 0;
    }

    @Override
    public String getProgressStr() {
        return super.getProgressStr();
    }

    @Override
    public Color getProgressColor(BaseEventIntel intel) {
        return super.getProgressColor(intel);
    }

    @Override
    public int getProgress() {

        if (dpl_DealtWithBadGuy()) return 0;
        
        if (dpl_DefeatedDPLAttack()) return 0;

        int score = 0;
        for (MarketAPI market : Misc.getPlayerMarkets(false)) {
        	boolean hasInd = false;
        	for (Industry ind : market.getIndustries()) {
        		if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY) && (ind.getSpecialItem() != null)) {
        			hasInd = true;
        		}
        	}
        	
        	if (hasInd) {
        		int size = market.getSize();
                switch (size) {
                    case 1:
                    	break;
                    case 2:
                    	break;
                    case 3:
                    	break;
                    case 4:
                        score += 4;
                        break;
                    case 5:
                        score += 8;
                        break;
                    case 6:
                        score += 16;
                        break;
                    default:
                        score += 24;
                        break;
                }
        	}
        }
        
        return score;
    }

    @Override
    public String getDesc() {
        return "Heavy Industry";
    }
    
    public static float getIndustryPoints(MarketAPI market) {
		float total = 0f;
		
		if (market.getSize() >= 4) {
			for (Industry ind : market.getIndustries()) {
				if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
	    			total += market.getSize();
	    		}
			}
		}
		
		return total;
	}

    @Override
    public float getMagnitudeContribution(StarSystemAPI system) {
        if (getProgress() <= 0) return 0f;
        if (dpl_DealtWithBadGuy()) return 0f;
        if (dpl_DefeatedDPLAttack()) return 0f;

        List<MarketAPI> markets = Misc.getMarketsInLocation(system, Factions.PLAYER);
        
        float total = 0f;
		for (MarketAPI market : markets) {
			float points = getIndustryPoints(market);
			total += points;
		}
		
		total = Math.round(total * 100f) / 100f;
		
		return total;
    }
}