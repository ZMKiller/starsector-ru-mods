package progsmod.data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import progsmod.util.SModUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XPTracker extends BaseHullMod {
    public static Map<String, Integer> numPenalizedMap = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > 1000;
        }
    };

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        return switch (index) {
            case 0 -> ship.getName();
            case 1 -> Misc.getFormat().format((int) SModUtils.getXP(ship.getFleetMemberId()));
            default -> null;
        };
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipVariantAPI variant = stats.getVariant();
        if (SModUtils.Constants.DISABLE_MOD || variant == null) return;

        // Increase S-mod limit ship stat if it was raised with SModUtils.incrementSModLimit()
        // Allows other mods like RAT to interact with S-mods
        if (stats.getFleetMember() != null) {
            int numOverLimit = SModUtils.getNumOverLimit(stats.getFleetMember().getId());
            if (numOverLimit > 0) {
                stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(id, numOverLimit);
            }

            int numPenalized = numPenalizedMap.getOrDefault(stats.getFleetMember().getId(), 0);
            if (numPenalized > 0) {
                int dp = computeDPModifier(stats, numPenalized);
                if (dp > 0) {
                    stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, dp);
                } else {
                    stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodify(id);
                }
            }
        }

    }

    // For better compatibility with Ship Mastery System, since that mod's applyEffectsBeforeShipCreation always happens last
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (SModUtils.Constants.DEPLOYMENT_COST_PENALTY <= 0f) return;
        if (ship.getMutableStats() == null) return;

        int sModsOverLimit = getNumPenalizedMods(ship.getMutableStats().getFleetMember());
        if (ship.getMutableStats().getFleetMember() != null && ship.getFleetMemberId() != null && sModsOverLimit > 0) {
            numPenalizedMap.put(ship.getFleetMemberId(), sModsOverLimit);
        } else {
            numPenalizedMap.remove(ship.getFleetMemberId());
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, final ShipAPI ship, float width, boolean isForModSpec) {
        if (ship == null || ship.getMutableStats() == null || ship.getVariant() == null) return;

        int sModsOverLimit = getNumPenalizedMods(ship.getFleetMember());
        int dpMod = computeDPModifier(ship.getMutableStats(), sModsOverLimit);
        if (dpMod > 0) {
            tooltip.addPara("Deployment point cost increased by %s due to the presence of additional S-mods over the standard limit.",
                    10f,
                    Misc.getNegativeHighlightColor(),
                    Misc.getHighlightColor(),
                    Integer.toString(dpMod));
        }
    }

    private int getNumPenalizedMods(FleetMemberAPI fm) {
        if (fm == null) return 0;

        ShipVariantAPI variant = fm.getVariant();
        if (variant == null) return 0;

        int sModsOverLimit = Math.max(0, variant.getSMods().size() - SModUtils.getBaseSMods(fm));

        List<ShipVariantAPI> modules = SModUtils.getModuleVariantsWithOP(variant);
        for (ShipVariantAPI module : modules) {
            sModsOverLimit = Math.max(sModsOverLimit, Math.max(0, module.getSMods().size() - SModUtils.getBaseSMods(fm)));
        }

        return Math.min(SModUtils.getNumOverLimit(fm.getId()), sModsOverLimit);
    }

    private int computeDPModifier(MutableShipStatsAPI stats, int sModsOverLimit) {
        if (stats == null || stats.getFleetMember() == null) return 0;

        float baseDPCost = stats.getFleetMember().getUnmodifiedDeploymentPointsCost();
        return (int) Math.ceil(baseDPCost * SModUtils.Constants.DEPLOYMENT_COST_PENALTY * sModsOverLimit);
    }
}
