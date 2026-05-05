package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;

public class HSISAFEContext extends FleetEncounterContext{
    @Override
    public List<FleetMemberAPI> getRecoverableShips(BattleAPI battle, CampaignFleetAPI winningFleet, CampaignFleetAPI otherFleet) {
		
		storyRecoverableShips.clear();
		
		List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
//		int max = Global.getSettings().getMaxShipsInFleet() - 
//				  Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().size();
//		if (Misc.isPlayerOrCombinedContainingPlayer(winningFleet) && max <= 0) {
//			return result;
//		}	
		DataForEncounterSide winnerData = getDataFor(winningFleet);
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
//		for (FleetMemberData curr : all) {
//			System.out.println(curr.getMember().getHullId());
//		}
		
		
		int maxRecoverablePerType = 10000;

		for (FleetMemberData data : all) {
//			if (data.getMember().getHullId().contains("legion")) {
//				System.out.println("wefwefwefe");
//			}
			//if (data.getMember().getHullSpec().getHints().contains(ShipTypeHints.UNBOARDABLE)) continue;
			//if (Misc.isUnboardable(data.getMember())) continue;
			if (data.getStatus() != Status.DISABLED && data.getStatus() != Status.DESTROYED) continue;

			boolean own = ownCasualties.contains(data);
			if (own && data.getMember().isAlly()) continue;
			
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
			boolean normalRecovery = true;
			boolean storyRecovery = !noRecovery && !normalRecovery;
		
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
				
				//Random dModRandom = new Random(1000000 * (data.getMember().getId().hashCode() + Global.getSector().getClock().getDay()));
				Random dModRandom = new Random(1000000 * data.getMember().getId().hashCode() + Global.getSector().getPlayerBattleSeed());
				dModRandom = Misc.getRandom(dModRandom.nextLong(), 5);
				if (lessDmods) {
					DModManager.reduceNextDmodsBy = 3;	
				}				
				float weaponProb = 1f;
				float wingProb =1f;
				if (own) {
					weaponProb = 1f;
					wingProb = 1f;
				}
				
				boolean retain = true;
				prepareShipForRecovery(data.getMember(), own, true, !own && !retain, weaponProb, wingProb, new Random());
				
				if (normalRecovery) {
					if (result.size() < maxRecoverablePerType) {
						result.add(data.getMember());
					}
				} else if (storyRecovery) {
					if (storyRecoverableShips.size() < maxRecoverablePerType) {
						storyRecoverableShips.add(data.getMember());
					}
				}
			}
		}	
		recoverableShips.clear();
		recoverableShips.addAll(result);
		return result;
	}

	@Override
	public BattleAPI getBattle() {
		return battle;
	}
	@Override
	public void setBattle(BattleAPI battle) {
		this.battle = battle;
	}
	@Override
	public DataForEncounterSide getDataFor(CampaignFleetAPI participantOrCombined) {
		//Global.getLogger(this.getClass()).info(participantOrCombined==null);
		CampaignFleetAPI combined = battle.getCombinedFor(participantOrCombined);
		if (combined == null) {
			return new DataForEncounterSide(participantOrCombined);
		}
		
		for (DataForEncounterSide curr : sideData) {
			if (curr.getFleet() == combined) return curr;
		}
		DataForEncounterSide dfes = new DataForEncounterSide(combined);
		sideData.add(dfes);
		
		return dfes;
	}

	protected float xpGained = 0;
	protected void gainXP(@NotNull DataForEncounterSide side, DataForEncounterSide otherSide) {
		float bonusXP = 0f;
		float points = 0f;
		for (FleetMemberData data : side.getOwnCasualties()) {
			if (data.getStatus() == Status.DISABLED || 
					data.getStatus() == Status.DESTROYED) {
				float [] bonus = Misc.getBonusXPForScuttling(data.getMember());
				points += bonus[0];
				bonusXP += bonus[1] * bonus[0];
			}
		}
		if (bonusXP > 0 && points > 0) {
			points = 1;
			Global.getSector().getPlayerStats().setOnlyAddBonusXPDoNotSpendStoryPoints(true);
			Global.getSector().getPlayerStats().setBonusXPGainReason("from losing s-modded ships");
			Global.getSector().getPlayerStats().spendStoryPoints((int)Math.round(points), true, textPanelForXPGain, false, bonusXP, null);
			Global.getSector().getPlayerStats().setOnlyAddBonusXPDoNotSpendStoryPoints(false);
			Global.getSector().getPlayerStats().setBonusXPGainReason(null);
		}
		
		//CampaignFleetAPI fleet = side.getFleet();
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		int fpTotal = 0;
		for (FleetMemberData data : otherSide.getOwnCasualties()) {
			float fp = data.getMember().getFleetPointCost();
			fp *= 1f + data.getMember().getCaptain().getStats().getLevel() / 5f;
			fpTotal += fp;
		}
		
		float xp = (float) fpTotal * 250;
		xp *= 0.25f;
		
		float difficultyMult = Math.max(1f, difficulty);
		xp *= difficultyMult;
		
		xp *= computePlayerContribFraction();
		
		xp *= Global.getSettings().getFloat("xpGainMult");
		
		
		if (xp > 0) {
			//fleet.getCargo().gainCrewXP(xp);
			
			//if (side.getFleet().isPlayerFleet()) {
			//}
			// only gain XP if it's the player fleet anyway, no need to check this here
			gainOfficerXP(side, xp);
			
			fleet.getCommander().getStats().addXP((long) xp, textPanelForXPGain);
			fleet.getCommander().getStats().levelUpIfNeeded(textPanelForXPGain);
			
			xpGained = xp;
		}
	}
	@Override
	public DataForEncounterSide getWinnerData() {
		for (DataForEncounterSide curr : sideData) {
			if (!curr.disengaged()) {
				return curr;
			}
		}
		return null;
	}
	
	public DataForEncounterSide getLoserData() {
		for (DataForEncounterSide curr : sideData) {
			if (curr.disengaged()) {
				return curr;
			}
		}
		return null;
	}

	private float XP_MULT = 0.0f;

	public void setXPMULT(float mult){XP_MULT = mult;}
}
