package data.hullmods.LogisticsMod;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.loading.HullModSpecAPI;

public class HSIBaseLogisticsMod extends BaseLogisticsHullMod implements HSILogisticsMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if(isLimitedMod()&&!isLimitedModShouldExist(stats, spec.getId())){
            stats.getVariant().removeMod(spec.getId());
            return;
        }
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        /*if (isLimitedMod() && !isLimitedModAvailable(ship.getMutableStats(), spec.getId())) {
            return HSII18nUtil.getHullModString("HSILogisticLimitedReached");
        }*/
        return super.getUnapplicableReason(ship);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (isLimitedMod()) {
            return isLimitedModAvailable(ship.getMutableStats(), spec.getId());
        }
        return super.isApplicableToShip(ship);
    }

    @Override
    public int getNumForLimitedMod(ShipHullSpecAPI spec, String mod) {
        return 0;
    }

    @Override
    public boolean isLimitedMod() {
        return false;
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return isLimitedModAvailable(ship.getMutableStats(), spec.getId());
    }

    protected boolean isLimitedModAvailable(MutableShipStatsAPI stats,String mod) {
        int exist = 0;
        int lim = 0;
        if (stats.getFleetMember() != null && stats.getFleetMember().getFleetData() != null) {
            FleetDataAPI fleetData = stats.getFleetMember().getFleetData();
            for (FleetMemberAPI member : fleetData.getMembersListCopy()) {
                for (String hullmod : member.getVariant().getHullMods()) {
                    HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullmod);
                    if (spec != null) {
                        if (spec.getEffect() instanceof HSILogisticsMod) {
                            lim += ((HSILogisticsMod) (spec.getEffect())).getNumForLimitedMod(member.getHullSpec(),
                                    mod);
                        }
                    }
                    if (hullmod.contentEquals(mod)) {
                        exist++;
                    }
                }
            }
        }
        return lim > exist;
    }

    protected boolean isLimitedModShouldExist(MutableShipStatsAPI stats, String mod) {
        int exist = 0;
        int lim = 0;
        if (stats.getFleetMember() != null && stats.getFleetMember().getFleetData() != null) {
            FleetDataAPI fleetData = stats.getFleetMember().getFleetData();
            for (FleetMemberAPI member : fleetData.getMembersListCopy()) {
                for (String hullmod : member.getVariant().getHullMods()) {
                    HullModSpecAPI spec = Global.getSettings().getHullModSpec(hullmod);
                    if (spec != null) {
                        if (spec.getEffect() instanceof HSILogisticsMod) {
                            lim += ((HSILogisticsMod) (spec.getEffect())).getNumForLimitedMod(member.getHullSpec(),
                                    mod);
                        }
                    }
                    if (hullmod.contentEquals(mod)) {
                        exist++;
                    }
                }
            }
        }
        return lim >= exist;
    }
}
