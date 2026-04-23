package org.xhan.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class XHAN_PlanetaryMegastructure extends BaseMarketConditionPlugin {

    public static final float STABILITY_BONUS = 2f;
    public static final float DEFENSE_MULT = 3f;

    @Override
    public void apply(String id) {
        market.getStability().modifyFlat(id, STABILITY_BONUS, this.getName());
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), DEFENSE_MULT, this.getName());
    }

    @Override
    public void unapply(String id) {
    }
}
