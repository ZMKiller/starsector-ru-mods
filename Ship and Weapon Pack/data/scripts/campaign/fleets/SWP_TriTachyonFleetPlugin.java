package data.scripts.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.plugins.CreateFleetPlugin;
import java.util.Random;

/* Changes TT fleets to use a smattering of non-capital carriers if it's a capital-size fleet */
public class SWP_TriTachyonFleetPlugin implements CreateFleetPlugin {

    static boolean noInfiniteLoop = false;
    static boolean noInfiniteLoop2 = false;

    @Override
    public CampaignFleetAPI createFleet(FleetParamsV3 params) {
        CampaignFleetAPI fleet;

        FactionDoctrineAPI originalOverride = null;
        if (params.doctrineOverride != null) {
            originalOverride = params.doctrineOverride.clone();
        } else {
            params.doctrineOverride = Global.getSector().getFaction(Factions.TRITACHYON).getDoctrine().clone();
        }

        /* Allow carriers to spawn */
        if (params.doctrineOverride.getCarriers() == 0) {
            if (Math.random() > 0.75) {
                int warships = params.doctrineOverride.getWarships();
                int phaseShips = params.doctrineOverride.getPhaseShips();
                if (warships > 1) {
                    params.doctrineOverride.setWarships(warships - 1);
                }
                if (phaseShips > 1) {
                    params.doctrineOverride.setPhaseShips(phaseShips - 1);
                }
                params.doctrineOverride.setCarriers(2);
            } else {
                if (Math.random() > 0.5) {
                    int warships = params.doctrineOverride.getWarships();
                    if (warships > 1) {
                        params.doctrineOverride.setWarships(warships - 1);
                    }
                } else {
                    int phaseShips = params.doctrineOverride.getPhaseShips();
                    if (phaseShips > 1) {
                        params.doctrineOverride.setPhaseShips(phaseShips - 1);
                    }
                }
                params.doctrineOverride.setCarriers(1);
            }
        }

        noInfiniteLoop = true;
        try {
            fleet = FleetFactoryV3.createFleet(params);
        } finally {
            noInfiniteLoop = false;
        }

        params.doctrineOverride = originalOverride;

        return fleet;
    }

    /* Determine if we're about to spawn a TT fleet that likely contains a capital ship */
    @Override
    public int getHandlingPriority(Object params) {
        if (noInfiniteLoop || noInfiniteLoop2) {
            return -1;
        }

        if (!(params instanceof FleetParamsV3)) {
            return -1;
        }

        FleetParamsV3 fleetParams = (FleetParamsV3) params;
        String factionId = fleetParams.factionId;
        if ((factionId == null) && (fleetParams.source != null)) {
            factionId = fleetParams.source.getFactionId();
        }
        if (factionId == null) {
            return -1;
        }

        if (!factionId.contentEquals(Factions.TRITACHYON)) {
            return -1;
        }

        if (fleetParams.maxShipSize < 4) {
            return -1;
        }

        /* Preserve the random */
        Random random = fleetParams.random;
        fleetParams.random = null;

        CampaignFleetAPI fleet;
        noInfiniteLoop2 = true;
        try {
            fleet = FleetFactoryV3.createFleet(fleetParams);
        } finally {
            noInfiniteLoop2 = false;
        }

        boolean hasCapital = false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isCapital() && !member.isCivilian() && !member.getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN)) {
                hasCapital = true;
                break;
            }
        }

        fleet.getFleetData().clear();
        fleet.setAbortDespawn(true);

        /* Restore the random */
        fleetParams.random = random;

        if (!hasCapital) {
            return -1;
        }

        return GenericPluginManagerAPI.MOD_SUBSET;
    }
}
