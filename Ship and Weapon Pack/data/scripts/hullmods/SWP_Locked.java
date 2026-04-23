package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class SWP_Locked extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxCombatReadiness().modifyFlat(id, -1f);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        if (!member.isMothballed()) {
            member.getRepairTracker().setMothballed(true);
            if (member.getFleetData() != null) {
                member.getFleetData().setSyncNeeded();
                member.getFleetData().syncIfNeeded();
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    @Override
    public Color getBorderColor() {
        return new Color(0, 0, 0, 0);
    }

    @Override
    public Color getNameColor() {
        return Misc.getNegativeHighlightColor();
    }

    @Override
    public int getDisplaySortOrder() {
        return 0;
    }
}
