package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SWPModPlugin;
import data.scripts.util.SWP_Util;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SWP_IBBFleetEncounterContext extends FleetEncounterContext {

    @Override
    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI winningFleet, CampaignFleetAPI otherFleet) {
        List<FleetMemberAPI> result = super.getRecoverableShips(battle, winningFleet, otherFleet);

        if (!SWPModPlugin.isIBBAlwaysRecover()) {
            return result;
        }

        if (Misc.isPlayerOrCombinedContainingPlayer(otherFleet)) {
            return result;
        }

        Set<String> recoveredTypes = new HashSet<>();
        for (FleetMemberAPI member : result) {
            recoveredTypes.add(SWP_Util.getNonDHullId(member.getHullSpec()));
        }

        DataForEncounterSide winnerData = getDataFor(winningFleet);

        float playerContribMult = computePlayerContribFraction();
        List<FleetMemberData> enemyCasualties = winnerData.getEnemyCasualties();

        for (FleetMemberData data : enemyCasualties) {
            if (Misc.isUnboardable(data.getMember())) {
                continue;
            }
            if ((data.getStatus() != Status.DISABLED) && (data.getStatus() != Status.DESTROYED)) {
                continue;
            }

            /* IBBs only */
            if (!SWP_Util.SPECIAL_SHIPS.contains(SWP_Util.getNonDHullId(data.getMember().getHullSpec()))) {
                continue;
            }

            /* Don't double-add */
            if (result.contains(data.getMember())) {
                continue;
            }
            if (storyRecoverableShips.contains(data.getMember())) {
                continue;
            }

            /* Only one of each type */
            if (recoveredTypes.contains(SWP_Util.getNonDHullId(data.getMember().getHullSpec()))) {
                continue;
            }

            if (playerContribMult > 0f) {
                data.getMember().setCaptain(Global.getFactory().createPerson());

                ShipVariantAPI variant = data.getMember().getVariant();
                variant = variant.clone();
                variant.setSource(VariantSource.REFIT);
                variant.setOriginalVariant(null);
                data.getMember().setVariant(variant, false, true);

                Random dModRandom = new Random(1000000 * data.getMember().getId().hashCode() + Global.getSector().getPlayerBattleSeed());
                dModRandom = Misc.getRandom(dModRandom.nextLong(), 5);
                DModManager.addDMods(data, false, Global.getSector().getPlayerFleet(), dModRandom);
                if (DModManager.getNumDMods(variant) > 0) {
                    DModManager.setDHull(variant);
                }

                float weaponProb = Global.getSettings().getFloat("salvageWeaponProb");
                float wingProb = Global.getSettings().getFloat("salvageWingProb");

                /* Always recover the unique wasps */
                if (SWP_Util.getNonDHullId(data.getMember().getHullSpec()).contentEquals("uw_boss_astral")) {
                    wingProb = 1f;
                }

                prepareShipForRecovery(data.getMember(), false, true, true, weaponProb, wingProb, getSalvageRandom());

                storyRecoverableShips.add(data.getMember());
                recoveredTypes.add(SWP_Util.getNonDHullId(data.getMember().getHullSpec()));
            }
        }

        return result;
    }
}
