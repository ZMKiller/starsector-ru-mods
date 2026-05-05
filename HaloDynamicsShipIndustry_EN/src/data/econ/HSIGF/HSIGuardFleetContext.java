package data.econ.HSIGF;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.*;

public class HSIGuardFleetContext extends FleetEncounterContext {

    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI winningFleet, CampaignFleetAPI otherFleet) {

        storyRecoverableShips.clear();

        List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
//		int max = Global.getSettings().getMaxShipsInFleet() -
//				  Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().size();
//		if (Misc.isPlayerOrCombinedContainingPlayer(winningFleet) && max <= 0) {
//			return result;
//		}

        if (Misc.isPlayerOrCombinedContainingPlayer(otherFleet)) {
            return result;
        }

        DataForEncounterSide winnerData = getDataFor(winningFleet);
        DataForEncounterSide loserData = getDataFor(otherFleet);

        float playerContribMult = computePlayerContribFraction();
        List<FleetMemberData> enemyCasualties = winnerData.getEnemyCasualties();
        List<FleetMemberData> ownCasualties = winnerData.getOwnCasualties();
        List<FleetMemberData> all = new ArrayList<FleetMemberData>();
        all.addAll(ownCasualties);
        Collections.sort(all, new Comparator<FleetMemberData>() {
            public int compare(FleetMemberData o1, FleetMemberData o2) {
                int result = o2.getMember().getVariant().getSMods().size() - o1.getMember().getVariant().getSMods().size();
                if (result == 0) {
                    result = o2.getMember().getHullSpec().getHullSize().ordinal() - o1.getMember().getHullSpec().getHullSize().ordinal();
                }
                return result;
            }
        });


        //Random random = Misc.getRandom(battle.getSeed(), 11);
        Random random = Misc.getRandom(Global.getSector().getPlayerBattleSeed(), 11);
        //System.out.println("BATTLE SEED: " + Global.getSector().getPlayerBattleSeed());

        // since the number of recoverable ships is limited, prefer "better" ships
        WeightedRandomPicker<FleetMemberData> enemyPicker = new WeightedRandomPicker<FleetMemberData>(random);

        // doesn't matter how it's sorted, as long as it's consistent so that
        // the order it's insertied into the picker in is the same
        List<FleetMemberData> enemy = new ArrayList<FleetMemberData>(enemyCasualties);
        Collections.sort(enemy, new Comparator<FleetMemberData>() {
            public int compare(FleetMemberData o1, FleetMemberData o2) {
                int result = o2.getMember().getId().hashCode() - o1.getMember().getId().hashCode();
                return result;
            }
        });

        for (FleetMemberData curr : enemy) {
            float base = 10f;
            switch (curr.getMember().getHullSpec().getHullSize()) {
                case CAPITAL_SHIP: base = 40f; break;
                case CRUISER: base = 20f; break;
                case DESTROYER: base = 10f; break;
                case FRIGATE: base = 5f; break;
            }
            float w = curr.getMember().getUnmodifiedDeploymentPointsCost() / base;

            enemyPicker.add(curr, w);
        }
        List<FleetMemberData> sortedEnemy = new ArrayList<FleetMemberData>();
        while (!enemyPicker.isEmpty()) {
            sortedEnemy.add(enemyPicker.pickAndRemove());
        }


        all.addAll(sortedEnemy);

//		for (FleetMemberData curr : all) {
//			System.out.println(curr.getMember().getHullId());
//		}

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        int maxRecoverablePerType = 24;

        float probLessDModsOnNext = Global.getSettings().getFloat("baseProbLessDModsOnRecoverableEnemyShip");
        float lessDmodsOnNextMult = Global.getSettings().getFloat("lessDModsOnRecoverableEnemyShipMultNext");

        int count = 0;
        for (FleetMemberData data : all) {
//			if (data.getMember().getHullId().contains("legion")) {
//				System.out.println("wefwefwefe");
//			}
            //if (data.getMember().getHullSpec().getHints().contains(ShipTypeHints.UNBOARDABLE)) continue;

            if (Misc.isUnboardable(data.getMember())) continue;
            if (data.getStatus() != Status.DISABLED && data.getStatus() != Status.DESTROYED) continue;

            boolean own = ownCasualties.contains(data);
            if (own && data.getMember().isAlly()) continue;
            if(!own && data.getMember().getHullSpec().getTags().contains("HSI_Ukiyo")) continue;

//			if (data.getMember().getHullId().startsWith("vanguard_pirates")) {
//				System.out.println("wefwefwefe12341234");
//			}

            float mult = 1f;
            if (data.getStatus() == Status.DESTROYED) mult = 0.5f;
            if (!own) mult *= playerContribMult;


            boolean useOfficerRecovery = false;
            if (own) {
                useOfficerRecovery = winnerData.getMembersWithOfficerOrPlayerAsOrigCaptain().contains(data.getMember());
                if (useOfficerRecovery) {
                    mult = 1f;
                }
            }

            boolean noRecovery = false;
            if (battle != null &&
                    battle.getSourceFleet(data.getMember()) != null) {
                CampaignFleetAPI fleet = battle.getSourceFleet(data.getMember());
                if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY)) {
                    noRecovery = true;
                }
            }

//			if (data.getMember().getHullId().startsWith("cerberus")) {
//				System.out.println("wefwefew");
//			}
            boolean normalRecovery = !noRecovery &&
                    Misc.isShipRecoverable(data.getMember(), playerFleet, own, useOfficerRecovery, 1f * mult);
            boolean storyRecovery = !noRecovery && !normalRecovery;

            boolean alwaysRec = data.getMember().getVariant().hasTag(Tags.VARIANT_ALWAYS_RECOVERABLE);

            float shipRecProb = data.getMember().getStats().getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).computeEffective(0f);
            if (!own && !alwaysRec && (storyRecovery || normalRecovery) && shipRecProb < 1f) {
                float per = Global.getSettings().getFloat("probNonOwnNonRecoverablePerDMod");
                float perAlready = Global.getSettings().getFloat("probNonOwnNonRecoverablePerAlreadyRecoverable");
                float max = Global.getSettings().getFloat("probNonOwnNonRecoverableMax");
                int dmods = DModManager.getNumDMods(data.getMember().getVariant());

                float assumedAddedDmods = 3f;
                assumedAddedDmods -= Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.SHIP_DMOD_REDUCTION, 0) * 0.5f;
                assumedAddedDmods = Math.min(assumedAddedDmods, 5 - dmods);

                float recoveredSoFar = 0f;
                if (storyRecovery) recoveredSoFar = storyRecoverableShips.size();
                else recoveredSoFar = result.size();

                if (random.nextFloat() < Math.min(max, (dmods + assumedAddedDmods) * per) + recoveredSoFar * perAlready) {
                    noRecovery = true;
                }
            }


            //if (true || Misc.isShipRecoverable(data.getMember(), playerFleet, own, useOfficerRecovery, battle.getSeed(), 1f * mult)) {
            if (!noRecovery && (normalRecovery || storyRecovery)) {
                //if (Misc.isShipRecoverable(data.getMember(), playerFleet, battle.getSeed(), 1f * mult)) {

                if (!own || !Misc.isUnremovable(data.getMember().getCaptain())) {
                    String aiCoreId = null;
                    if (own && data.getMember().getCaptain() != null &&
                            data.getMember().getCaptain().isAICore()) {
                        aiCoreId = data.getMember().getCaptain().getAICoreId();
                    }

                    // if it's an AI core on a player ship, then:
                    // 1. It's integrated/unremovable, so, don't remove (we don't even end up here)
                    // 2. Ship will be recovered and will still have it, or
                    // 3. Ship will not be recovered, and it will get added to loot in lootWeapons()
                    boolean keepCaptain = false;
                    // don't do this - want to only show the AI core in recovery dialog when
                    // it's integrated and would be lost if not recovered
//					if (own && (data.getMember().getCaptain() == null ||
//							data.getMember().getCaptain().isAICore())) {
//						keepCaptain = true;
//					}
                    if (!keepCaptain) {
                        data.getMember().setCaptain(Global.getFactory().createPerson());
                        if (aiCoreId != null) {
                            data.getMember().getCaptain().getMemoryWithoutUpdate().set(
                                    "$aiCoreIdForRecovery", aiCoreId);
                        }
                    }
                }

                ShipVariantAPI variant = data.getMember().getVariant();
                variant = variant.clone();
                variant.setSource(VariantSource.REFIT);
                variant.setOriginalVariant(null);
                //DModManager.setDHull(variant);
                data.getMember().setVariant(variant, false, true);

                boolean lessDmods = false;
                if (!own && data.getStatus() != Status.DESTROYED && random.nextFloat() < probLessDModsOnNext) {
                    lessDmods = true;
                    probLessDModsOnNext *= lessDmodsOnNextMult;
                }

                //Random dModRandom = new Random(1000000 * (data.getMember().getId().hashCode() + Global.getSector().getClock().getDay()));
                Random dModRandom = new Random(1000000 * data.getMember().getId().hashCode() + Global.getSector().getPlayerBattleSeed());
                dModRandom = Misc.getRandom(dModRandom.nextLong(), 5);
                if (lessDmods) {
                    DModManager.reduceNextDmodsBy = 3;
                }

                float probAvoidDmods =
                        data.getMember().getStats().getDynamic().getMod(
                                Stats.DMOD_AVOID_PROB_MOD).computeEffective(0f);

                float probAcquireDmods =
                        data.getMember().getStats().getDynamic().getMod(
                                Stats.DMOD_ACQUIRE_PROB_MOD).computeEffective(1f);

                if (dModRandom.nextFloat() >= probAvoidDmods && dModRandom.nextFloat() < probAcquireDmods) {
                    DModManager.addDMods(data, own, Global.getSector().getPlayerFleet(), dModRandom);
                    if (DModManager.getNumDMods(variant) > 0) {
                        DModManager.setDHull(variant);
                    }
                }

                float weaponProb = Global.getSettings().getFloat("salvageWeaponProb");
                float wingProb = Global.getSettings().getFloat("salvageWingProb");
                if (own) {
                    weaponProb = Global.getSettings().getFloat("salvageOwnWeaponProb");
                    wingProb = Global.getSettings().getFloat("salvageOwnWingProb");
                    weaponProb = playerFleet.getStats().getDynamic().getValue(Stats.OWN_WEAPON_RECOVERY_MOD, weaponProb);
                    wingProb = playerFleet.getStats().getDynamic().getValue(Stats.OWN_WING_RECOVERY_MOD, wingProb);
                }

                boolean retain = data.getMember().getHullSpec().hasTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY) ||
                        data.getMember().getVariant().hasTag(Tags.TAG_RETAIN_SMODS_ON_RECOVERY);
                prepareShipForRecovery(data.getMember(), own, true, !own && !retain, weaponProb, wingProb, getSalvageRandom());

                if (normalRecovery) {
                    if (result.size() < maxRecoverablePerType) {
                        result.add(data.getMember());
                    }
                } else if (storyRecovery) {
                    if (storyRecoverableShips.size() < maxRecoverablePerType) {
                        storyRecoverableShips.add(data.getMember());
                    }
                }

//				count++;
//				if (count >= max) break;
            }


//			else {
//				data.getMember().getVariant().removeTag(Tags.SHIP_RECOVERABLE);
//			}
        }

        //System.out.println("Recoverable: " + result.size() + ", story: " + storyRecoverableShips.size());


        recoverableShips.clear();
        recoverableShips.addAll(result);
        return result;
    }
}
