package progsmod.data.campaign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

import progsmod.data.combat.ContributionTracker;
import progsmod.data.combat.ContributionTracker.ContributionType;
import progsmod.util.SModUtils;

public class EngagementListener extends BaseCampaignEventListener {

    /** Maps ship id to the corresponding fleetMember id. */
    private Map<String, String> shipToFleetMemberMap;
    /** Maps fleetMemberIds to their corresponding fleetMembers. */
    private Map<String, FleetMemberAPI> idToFleetMemberMap;
    /** Maps fleet member ids to their total XP gain, by contribution type */
    private Map<String, Map<ContributionType, Float>> xpGainMap;
    /** Set of player ships that are eligible to gain XP */
    private Set<String> playerFilter;
    /** Set of enemy ships that are eligible to give XP */
    private Set<String> enemyFilter;
    /** Keep track of the last dialog opened in order to add text to it. */
    private InteractionDialogAPI lastDialog;

    public EngagementListener(boolean permaRegister) {
        super(permaRegister);
    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
        lastDialog = dialog;
    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (!battle.isPlayerInvolved()) {
            return;
        }
        Map<String, Float> totalReserveXP = new HashMap<>();
        for (CampaignFleetAPI fleet : battle.getPlayerSideSnapshot()) {
            if (!fleet.isPlayerFleet()) {
                continue;
            }
             for (FleetMemberAPI fm : Misc.getSnapshotMembersLost(fleet)) {
                // For player losses, add any lost XP to a reserve XP pool for that
                // hull type.
                float reserveXP = SModUtils.getXP(fm.getId());
                // Add XP for S-mods, equivalent to the XP that would be obtained by refunding the mods
                if (SModUtils.Constants.XP_REFUND_FACTOR > 0f) {
                    for (String modId : fm.getVariant().getSMods()) {
                        HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(modId);
                        reserveXP += SModUtils.Constants.XP_REFUND_FACTOR *
                                    SModUtils.getBuildInCost(hullMod, fm.getHullSpec().getHullSize(), fm.getUnmodifiedDeploymentPointsCost());
                    }
                    // If the ship has modules, add XP for any S-mods built into modules
                    List<String> moduleSlotIds = fm.getVariant().getModuleSlots();
                    if (!moduleSlotIds.isEmpty()) {
                        for (String id : moduleSlotIds) {
                            ShipVariantAPI moduleVariant = fm.getVariant().getModuleVariant(id);
                            for (String modId : moduleVariant.getSMods()) {
                                HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(modId);
                                reserveXP += SModUtils.Constants.XP_REFUND_FACTOR *
                                    SModUtils.getBuildInCost(hullMod, moduleVariant.getHullSize(), fm.getUnmodifiedDeploymentPointsCost());
                            }
                        }
                    }
                }
                // Add XP for increasing S-mod limits
                reserveXP += SModUtils.getXPSpentOnIncreasingLimit(fm.getId());
                reserveXP *= SModUtils.Constants.RESERVE_XP_FRACTION;
                if (reserveXP >= 1f) {
                    String hullId = fm.getHullSpec().getBaseHullId();
                    Float existingReserveXP = totalReserveXP.get(hullId);
                    totalReserveXP.put(hullId, existingReserveXP == null ? reserveXP : reserveXP + existingReserveXP);
                }
                // Remove this fleet member from the XP table
                SModUtils.deleteXPData(fm.getId());
            }
        } 
        for (Map.Entry<String, Float> reserveXPEntry : totalReserveXP.entrySet()) {
            String hullId = reserveXPEntry.getKey();
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            String hullName = spec == null ? "<unknown>" : spec.getHullName();
            float amount = reserveXPEntry.getValue();
            String amtFmt = Misc.getFormat().format((int) amount);
            SModUtils.addReserveXP(hullId, amount);
            Global.getSector().getCampaignUI().addMessage(
                String.format("Added %s XP to reserve XP pool for %s due to combat losses", amtFmt, hullName),
                Misc.getBasePlayerColor(),
                amtFmt,
                hullName,
                Misc.getHighlightColor(),
                Misc.getHighlightColor()
            );
        } 
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        // Populate the required utility mappings
        shipToFleetMemberMap = ContributionTracker.getShipToFleetMemberMap();
        Map<ContributionType, Map<String, Map<String, Float>>> totalContributionMap =
                ContributionTracker.getContributionTable();
        idToFleetMemberMap = new HashMap<>();
        xpGainMap = new HashMap<>();
        playerFilter = new HashSet<>();
        enemyFilter = new HashSet<>();

        if (totalContributionMap == null) {
            return;
        }

        EngagementResultForFleetAPI playerResult = result.getLoserResult(), enemyResult = result.getWinnerResult();
        List<FleetMemberAPI> playerFleet = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
        if (result.didPlayerWin()) {
            playerResult = result.getWinnerResult();
            enemyResult = result.getLoserResult();
        }

        // List of ships that are eligible to gain XP
        playerFilter.addAll(SModUtils.getFleetMemberIds(playerFleet));
        if (!SModUtils.Constants.GIVE_XP_TO_DISABLED_SHIPS) {
            playerFilter.removeAll(new HashSet<>(SModUtils.getFleetMemberIds(playerResult.getDestroyed())));
            playerFilter.removeAll(new HashSet<>(SModUtils.getFleetMemberIds(playerResult.getDisabled())));
        }
        // List of ships that can give XP when damaged
        enemyFilter.addAll(SModUtils.getFleetMemberIds(enemyResult.getDestroyed()));
        enemyFilter.addAll(SModUtils.getFleetMemberIds(enemyResult.getDisabled()));
        if (!SModUtils.Constants.ONLY_GIVE_XP_FOR_KILLS) {
            enemyFilter.addAll(SModUtils.getFleetMemberIds(enemyResult.getRetreated()));
            enemyFilter.addAll(SModUtils.getFleetMemberIds(enemyResult.getDeployed()));
        }

        // If nobody was deployed (second-in-command handles pursuit) no individual damage data
        if (playerResult.getAllEverDeployedCopy() == null) {
            giveXPForPursuit(playerFleet, enemyResult.getFleet().getFleetData().getMembersListCopy(), enemyFilter);
            return;
        }

        for (DeployedFleetMemberAPI dfm : playerResult.getAllEverDeployedCopy()) {
            idToFleetMemberMap.put(dfm.getMember().getId(), dfm.getMember());
        }
        for (DeployedFleetMemberAPI dfm : enemyResult.getAllEverDeployedCopy()) {
            idToFleetMemberMap.put(dfm.getMember().getId(), dfm.getMember());
        }

        // Convert player ships' contributions into XP gains
        for (ContributionType type : totalContributionMap.keySet()) {
            processContributions(type, totalContributionMap.get(type));
        }

        // Give XP to the ships that earned XP.
        float totalXPGain = 0f;
        for (Map.Entry<String, Map<ContributionType, Float>> xpGainEntry : xpGainMap.entrySet()) {
            String id = xpGainEntry.getKey();
            for (Map.Entry<ContributionType, Float> xpByType : xpGainEntry.getValue().entrySet()) {
                float xpGain = xpByType.getValue();
                SModUtils.giveXP(id, xpGain);
                totalXPGain += xpGain;
            }
            // Show the XP gain in the dialog
            if (idToFleetMemberMap.containsKey(id) && !SModUtils.Constants.CONDENSE_XP_GAIN_MESSAGES) {
                SModUtils.addTypedXPGainToDialog(
                    lastDialog, 
                    idToFleetMemberMap.get(id), 
                    xpGainMap.get(id), 
                    "from combat");
            }
        }

        if (SModUtils.Constants.CONDENSE_XP_GAIN_MESSAGES) {
            SModUtils.addCondensedXPGainToDialog(lastDialog, totalXPGain, xpGainMap.size());
        }

        givePostBattleXP(playerFleet, totalXPGain, false);
    }

    /** Give only the post battle XP for pursuits. */
    private void giveXPForPursuit(List<FleetMemberAPI> playerFleet, List<FleetMemberAPI> enemyFleet, Collection<String> enemyFilter) {
        float fakeXPGain = 0f;
        for (FleetMemberAPI enemy : enemyFleet) {
            if (!enemyFilter.contains(enemy.getId())) {
                continue;
            }
            fakeXPGain += 
                SModUtils.Constants.XP_GAIN_MULTIPLIER 
                * enemy.getStatus().getHullDamageTaken() 
                * Math.max(
                    enemy.getDeploymentCostSupplies(), 
                    SModUtils.Constants.TARGET_DMOD_LOWER_BOUND * enemy.getDeploymentPointsCost()
            );
        }
        givePostBattleXP(playerFleet, fakeXPGain, true);
    }

    /** Give a fraction of the total XP gained in an engagement to all ships currently in the player's fleet. */
    private void givePostBattleXP(List<FleetMemberAPI> playerFleet, float totalXPGain, boolean isAutoResolve) {
        List<FleetMemberAPI> civilianShips = new ArrayList<>();
        boolean shouldShow = false;
        for (FleetMemberAPI member : playerFleet) {
            if (playerFilter.contains(member.getId())) {
                float xpFraction = SModUtils.Constants.POST_BATTLE_XP_FRACTION;
                if (member.isCivilian() || (member.getVariant().hasHullMod("civgrade") && !member.getVariant().hasHullMod("militarized_subsystems"))) {
                    xpFraction *= SModUtils.Constants.POST_BATTLE_CIVILIAN_MULTIPLIER;
                    civilianShips.add(member);
                }
                else if (isAutoResolve) {
                    xpFraction *= SModUtils.Constants.POST_BATTLE_AUTO_PURSUIT_MULTIPLIER;
                }
                SModUtils.giveXP(member, totalXPGain * xpFraction);
                if ((int) (totalXPGain * xpFraction) > 0) {
                    shouldShow = true;
                }
            }
        }
        if (shouldShow) {
            SModUtils.addPostBattleXPGainToDialog(
                lastDialog, 
                civilianShips, 
                (int) (totalXPGain * SModUtils.Constants.POST_BATTLE_XP_FRACTION * (isAutoResolve ? SModUtils.Constants.POST_BATTLE_AUTO_PURSUIT_MULTIPLIER : 1.0f)), 
                (int) (totalXPGain * SModUtils.Constants.POST_BATTLE_XP_FRACTION * SModUtils.Constants.POST_BATTLE_CIVILIAN_MULTIPLIER), isAutoResolve);
        }
    }

    /** Process contributions for a specific contribution type (ATTACK, DEFENSE, SUPPORT).
     *  Puts the result into [this.xpGainMap].
     *  Note: [contributionTable] uses shipIds, whereas [xpGainMap] uses fleetMemberIds. */
    private void processContributions(ContributionType type,  Map<String, Map<String, Float>> contributionTable) {
        for (Map.Entry<String, Map<String, Float>> contributionByEnemy : contributionTable.entrySet()) {
            String enemyFleetMemberId = shipToFleetMemberMap.get(contributionByEnemy.getKey());
            if (enemyFleetMemberId == null) {
                continue;
            }
            if (!enemyFilter.contains(enemyFleetMemberId)) {
                continue;
            }
            FleetMemberAPI enemyFleetMember = idToFleetMemberMap.get(enemyFleetMemberId);
            if (enemyFleetMember == null) {
                continue;
            }
            float totalContribution = 0f;
            for (float contribution : contributionByEnemy.getValue().values()) {
                totalContribution += contribution;
            }
            if (totalContribution <= 0f) {
                continue;
            }
            float totalXP = 
                SModUtils.Constants.XP_GAIN_MULTIPLIER 
                * getXPFractionForType(type) 
                * enemyFleetMember.getStatus().getHullDamageTaken() 
                * Math.max(
                     enemyFleetMember.getDeploymentCostSupplies(), 
                     SModUtils.Constants.TARGET_DMOD_LOWER_BOUND * enemyFleetMember.getDeploymentPointsCost()
                );
            for (Map.Entry<String, Float> contributionByPlayer : contributionByEnemy.getValue().entrySet()) {
                String playerFleetMemberId = shipToFleetMemberMap.get(contributionByPlayer.getKey());
                if (playerFleetMemberId == null) {
                    continue;
                }
                if (!playerFilter.contains(playerFleetMemberId)) {
                    continue;
                }
                addXPGain(playerFleetMemberId, totalXP * contributionByPlayer.getValue() / totalContribution, type);
            }
        }
    }

    /** Does xpGainMap[fleetMemberId][type] += amount, initializing
     *  maps as needed. */
    private void addXPGain(String fleetMemberId, float amount, ContributionType type) {
        Map<ContributionType, Float> xpByType = xpGainMap.get(fleetMemberId);
        if (xpByType == null) {
            xpByType = new EnumMap<>(ContributionType.class);
            xpGainMap.put(fleetMemberId, xpByType);
        }
        Float xp = xpByType.get(type);
        xpByType.put(type, xp == null ? amount : xp + amount);
    }

    private float getXPFractionForType(ContributionType type) {
        switch (type) {
            case ATTACK: return SModUtils.Constants.XP_FRACTION_ATTACK;
            case DEFENSE: return SModUtils.Constants.XP_FRACTION_DEFENSE;
            case SUPPORT: return SModUtils.Constants.XP_FRACTION_SUPPORT;
            default: return 0f;
        }
    }
}
