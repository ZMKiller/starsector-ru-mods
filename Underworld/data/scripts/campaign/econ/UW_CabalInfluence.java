package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class UW_CabalInfluence extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

    public static final float ACCESS_BONUS = 10f;
    public static final float STABILITY_PENALTY = 1f;

    @Override
    public void apply(String id) {
        market.addTransientImmigrationModifier(this);

        market.getStability().modifyFlat(id, -STABILITY_PENALTY, "Cabal influence");
        market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS / 100f, "Cabal influence");
    }

    @Override
    public void unapply(String id) {
        market.removeTransientImmigrationModifier(this);

        market.getStability().unmodify(id);
        market.getAccessibilityMod().unmodifyFlat(id);
    }

    @Override
    public boolean showIcon() {
        return !market.hasCondition(Conditions.DECIVILIZED);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("%s accessibility",
                10f, Misc.getHighlightColor(),
                "+" + (int) ACCESS_BONUS + "%");
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add("cabal", 10f);
    }
}
