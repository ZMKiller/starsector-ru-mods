package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class HSITacCenter extends BaseHullMod {

    protected static final float ECM_PANELTY = 0f;
    protected static final float ECM_BUFF = 10f;

    protected static final float SIGHT_BONUS = 50f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).modifyMult(id, ECM_PANELTY);
        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id,ECM_BUFF);
        stats.getSightRadiusMod().modifyPercent(id,SIGHT_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return (int)((1f-ECM_PANELTY)*100f)+"%";
        if(index == 1) return (int)ECM_BUFF+"%";
        if(index == 2) return (int)SIGHT_BONUS+"%";
        return null;
    }
}
