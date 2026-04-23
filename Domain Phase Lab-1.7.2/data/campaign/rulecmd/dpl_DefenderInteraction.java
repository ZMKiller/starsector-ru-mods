package data.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SalvageDefenderModificationPlugin;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	SalvageDefenderInteraction
 */
public class dpl_DefenderInteraction extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		final SectorEntityToken entity = dialog.getInteractionTarget();
		final MemoryAPI memory = getEntityMemory(memoryMap);

		final CampaignFleetAPI defenders = memory.getFleet("$defenderFleet");
		if (defenders == null) return false;
		
		dialog.setInteractionTarget(defenders);
		
		final FIDConfig config = new FIDConfig();
		config.leaveAlwaysAvailable = true;
		config.showCommLinkOption = false;
		config.showEngageText = false;
		config.showFleetAttitude = false;
		config.showTransponderStatus = false;
		config.showWarningDialogWhenNotHostile = false;
		config.alwaysAttackVsAttack = true;
		config.impactsAllyReputation = true;
		config.impactsEnemyReputation = false;
		config.pullInAllies = false;
		config.pullInEnemies = false;
		config.pullInStations = false;
		config.lootCredits = false;
		
		config.firstTimeEngageOptionText = "Engage the defense forces";
		config.afterFirstTimeEngageOptionText = "Re-engage defense forces";
		config.noSalvageLeaveOptionText = "Continue";
		
		config.dismissOnLeave = false;
		config.printXPToDialog = true;
		
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		config.salvageRandom = Misc.getRandom(seed, 75);
		
		
		final FleetInteractionDialogPluginImpl plugin = new FleetInteractionDialogPluginImpl(config);
		
		final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
		config.delegate = new BaseFIDDelegate() {
			@Override
			public void notifyLeave(InteractionDialogAPI dialog) {
				// nothing in there we care about keeping; clearing to reduce savefile size
				defenders.getMemoryWithoutUpdate().clear();
				// there's a "standing down" assignment given after a battle is finished that we don't care about
				defenders.clearAssignments();
				defenders.deflate();
				
				dialog.setPlugin(originalPlugin);
				dialog.setInteractionTarget(entity);
				
				if (plugin.getContext() instanceof FleetEncounterContext) {
					FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
					if (context.didPlayerWinEncounterOutright()) {
						
						SDMParams p = new SDMParams();
						p.entity = entity;
						p.factionId = defenders.getFaction().getId();
						
						SalvageDefenderModificationPlugin plugin = Global.getSector().getGenericPlugins().pickPlugin(
												SalvageDefenderModificationPlugin.class, p);
						if (plugin != null) {
							plugin.reportDefeated(p, entity, defenders);
						}
						
						memory.unset("$hasDefenders");
						memory.unset("$defenderFleet");
						memory.set("$defenderFleetDefeated", true);
						entity.removeScriptsOfClass(FleetAdvanceScript.class);
						if (entity.getCustomEntityType().equals("dpl_inactive_HELA")) {
							FireBest.fire(null, dialog, memoryMap, "dpl_HELA_VictoryOption");
						} else {
							FireBest.fire(null, dialog, memoryMap, "BeatDefendersContinue");
						}
					} else {
						boolean persistDefenders = false;
						if (context.isEngagedInHostilities()) {
							persistDefenders |= !Misc.getSnapshotMembersLost(defenders).isEmpty();
							for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) {
								if (member.getStatus().needsRepairs()) {
									persistDefenders = true;
									break;
								}
							}
						}
						
						if (persistDefenders) {
							if (!entity.hasScriptOfClass(FleetAdvanceScript.class)) {
								defenders.setDoNotAdvanceAI(true);
								defenders.setContainingLocation(entity.getContainingLocation());
								// somewhere far off where it's not going to be in terrain or whatever
								defenders.setLocation(1000000, 1000000);
								entity.addScript(new FleetAdvanceScript(defenders));
							}
							memory.expire("$defenderFleet", 10); // defenders may have gotten damaged; persist them for a bit
						}
						dialog.dismiss();
					}
				} else {
					dialog.dismiss();
				}
			}
			@Override
			public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
				bcc.aiRetreatAllowed = false;
				bcc.objectivesAllowed = false;
				bcc.enemyDeployAll = true;
			}
			@Override
			public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
				DataForEncounterSide winner = context.getWinnerData();
				DataForEncounterSide loser = context.getLoserData();

				if (winner == null || loser == null) return;
				
				float playerContribMult = context.computePlayerContribFraction();
				
				List<DropData> dropRandom = new ArrayList<DropData>();
				List<DropData> dropValue = new ArrayList<DropData>();
				
				float valueMultFleet = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.BATTLE_SALVAGE_MULT_FLEET);
				float valueModShips = context.getSalvageValueModPlayerShips();
				
				for (FleetMemberData data : winner.getEnemyCasualties()) {
					// add at least one of each weapon that was present on the enemy ships, since these
					// are hard to get; don't want them to be too RNG
					if (data.getMember() != null && context.getBattle() != null) {
						CampaignFleetAPI fleet = context.getBattle().getSourceFleet(data.getMember());
					
						if (fleet != null && fleet.getFaction().getId().equals("dpl_persean_imperium")) {
							if (salvage.getNumWeapons("dpl_weak_spot") <= 6) {	
								salvage.addWeapons("dpl_weak_spot", 6);
							}
							if (salvage.getNumWeapons("dpl_unstable_particle") <= 6) {
								salvage.addWeapons("dpl_unstable_particle", 6);
							}
							if (salvage.getNumWeapons("dpl_shield_disruptor") <= 4) {
								salvage.addWeapons("dpl_shield_disruptor", 4);
							}
							if (salvage.getNumWeapons("dpl_lightning_pulser") <= 5) {
								salvage.addWeapons("dpl_lightning_pulser", 5);
							}
						} else if (fleet != null && fleet.hasTag("dpl_btcl_attackerFleet")) {
							if (salvage.getNumWeapons("cryoflux") < 4) {
								salvage.addWeapons("cryoflux", 4-salvage.getNumWeapons("cryoflux"));
							}
							if (salvage.getNumWeapons("cryoblaster") < 6) {
								salvage.addWeapons("cryoblaster", 6-salvage.getNumWeapons("cryoblaster"));
							}
							if (salvage.getNumWeapons("minipulser") < 4) {
								salvage.addWeapons("minipulser", 4-salvage.getNumWeapons("minipulser"));
							}
							if (salvage.getNumWeapons("rifttorpedo") < 2) {
								salvage.addWeapons("rifttorpedo", 2-salvage.getNumWeapons("rifttorpedo"));
							}
							if (salvage.getNumWeapons("riftcascade") < 2) {
								salvage.addWeapons("riftcascade", 2-salvage.getNumWeapons("riftcascade"));
							}
							if (salvage.getNumWeapons("realitydisruptor") < 1) {
								salvage.addWeapons("realitydisruptor", 1-salvage.getNumWeapons("realitydisruptor"));
							}
						}
						
						if (fleet != null && 
								fleet.getFaction().getCustomBoolean(Factions.CUSTOM_NO_AI_CORES_FROM_AUTOMATED_DEFENSES)) {
							continue;
						}
					}
				}
				
				float fuelMult = Global.getSector().getPlayerFleet().getStats().getDynamic().getValue(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET);
				//float fuel = salvage.getFuel();
				//salvage.addFuel((int) Math.round(fuel * fuelMult));
				
				CargoAPI extra = SalvageEntity.generateSalvage(config.salvageRandom, valueMultFleet + valueModShips, 1f, 1f, fuelMult, dropValue, dropRandom);
				for (CargoStackAPI stack : extra.getStacksCopy()) {
					if (stack.isFuelStack()) {
						stack.setSize((int)(stack.getSize() * fuelMult));
					}
					salvage.addFromStack(stack);
				}
				
			}
			
		};
		
		
		dialog.setPlugin(plugin);
		plugin.init(dialog);
	
		return true;
	}
	
	public static boolean isCivilian(FleetMemberAPI member) {
		if (member == null) return false;
		MutableShipStatsAPI stats = member.getStats();
		return stats != null && stats.getVariant() != null && 
				((stats.getVariant().hasHullMod(HullMods.CIVGRADE) && 
				!stats.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) ||
				(!stats.getVariant().hasHullMod(HullMods.CIVGRADE) &&
						stats.getVariant().getHullSpec().getHints().contains(ShipTypeHints.CIVILIAN)));// &&
				//!stats.getVariant().getHullSpec().getHints().contains(ShipTypeHints.CARRIER);
	}

}




