package data.campaign.rulecmd;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomProductionPickerDelegateImpl;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ItemInProductionAPI;
import com.fs.starfarer.api.campaign.FactionProductionAPI.ProductionItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemPlugin.RightClickActionHelper;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveAnyItem;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.util.Misc.Token;

public class dpl_RossTradeCMD extends BaseCommandPlugin {
	protected Random genRandom = null;
	
	public Random getGenRandom() {
		return genRandom;
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
				
		if ("rollSmall".equals(action)) {
			int Seed = -1;
			boolean HasSeed = memory.getBoolean("$dpl_RossHasSeed");
			if (HasSeed) {
				Seed = memory.getInt("$dpl_RossSeed");
			} else {
				Seed = Math.round(1000000*(float) Math.random());
				memory.set("$dpl_RossSeed", Seed);
				memory.set("$dpl_RossHasSeed", true);
			}
			List<String> AllWeapons = new ArrayList<>();
			AllWeapons.add("dpl_black_hole");
			AllWeapons.add("dpl_weak_spot");
			AllWeapons.add("dpl_unstable_particle");
			AllWeapons.add("dpl_jingle");
			AllWeapons.add("dpl_nova_blaster");
			AllWeapons.add("dpl_dre");
			Random random1 = new Random(Seed);
			int i = random1.nextInt(AllWeapons.size());
			String theWeapon = AllWeapons.get(i);
			cargo.addWeapons(theWeapon, 1);
			
			Seed = Math.round(1000000*(float) random1.nextFloat());
			memory.set("$dpl_RossSeed", Seed);
			memory.set("$dpl_RossHasSeed", true);
			
		} else if ("rollMedium".equals(action)) {
			int Seed = -1;
			boolean HasSeed = memory.getBoolean("$dpl_RossHasSeed");
			if (HasSeed) {
				Seed = memory.getInt("$dpl_RossSeed");
			} else {
				Seed = Math.round(1000000*(float) Math.random());
				memory.set("$dpl_RossSeed", Seed);
				memory.set("$dpl_RossHasSeed", true);
			}
			List<String> AllWeapons = new ArrayList<>();
			AllWeapons.add("dpl_shield_disruptor");
			AllWeapons.add("dpl_chants");
			AllWeapons.add("dpl_vaporizer");
			AllWeapons.add("dpl_crotchets");
			AllWeapons.add("dpl_lightning_pulser");
			AllWeapons.add("dpl_arc_blaster");
			Random random1 = new Random(Seed);
			int i = random1.nextInt(AllWeapons.size());
			String theWeapon = AllWeapons.get(i);
			cargo.addWeapons(theWeapon, 1);
			
			Seed = Math.round(1000000*(float) random1.nextFloat());
			memory.set("$dpl_RossSeed", Seed);
			memory.set("$dpl_RossHasSeed", true);
			
		} else if ("showWeaponPicker".equals(action)) {
			showWeaponPicker(dialog, memoryMap);
			return true;
		}
		return false;
	}
	
	public static int getDataStorageCost(WeaponSpecAPI spec) {
		String id = spec.getWeaponId();
		if (id.equalsIgnoreCase("dpl_black_hole")) {
			return 2;
		} else if (id.equalsIgnoreCase("dpl_weak_spot")) {
			return 2;
		} else if (id.equalsIgnoreCase("dpl_unstable_particle")) {
			return 2;
		} else if (id.equalsIgnoreCase("dpl_jingle")) {
			return 2;
		} else if (id.equalsIgnoreCase("dpl_nova_blaster")) {
			return 2;
		} else if (id.equalsIgnoreCase("dpl_dre")) {
			return 2;
		} else if (id.equalsIgnoreCase("dpl_shield_disruptor")) {
			return 4;
		} else if (id.equalsIgnoreCase("dpl_chants")) {
			return 4;
		} else if (id.equalsIgnoreCase("dpl_vaporizer")) {
			return 4;
		} else if (id.equalsIgnoreCase("dpl_crotchets")) {
			return 4;
		} else if (id.equalsIgnoreCase("dpl_lightning_pulser")) {
			return 4;
		} else if (id.equalsIgnoreCase("dpl_arc_blaster")) {
			return 4;
		}
		return 0;
	}
	
	protected void showWeaponPicker(final InteractionDialogAPI dialog, final Map<String, MemoryAPI> memoryMap) {
		
		final int num = (int) Global.getSector().getPlayerFleet().getCargo().getQuantity(CargoItemType.SPECIAL, new SpecialItemData("dpl_data_archive", null));
		
		final Set<String> weapons = new LinkedHashSet<>();
		
		for (WeaponSpecAPI spec : Global.getSettings().getAllWeaponSpecs()) {
			int cost = getDataStorageCost(spec);
			if (cost > 0 && cost <= num) {
				weapons.add(spec.getWeaponId());
			}
		}
		
		dialog.showCustomProductionPicker(new BaseCustomProductionPickerDelegateImpl() {
			@Override
			public Set<String> getAvailableFighters() {
				return new LinkedHashSet<>();
			}
			@Override
			public Set<String> getAvailableShipHulls() {
				return new LinkedHashSet<>();
			}
			@Override
			public Set<String> getAvailableWeapons() {
				return weapons;
			}
			@Override
			public float getCostMult() {
				return 1f;
			}
			@Override
			public float getMaximumValue() {
				return num;
			}
			
			@Override
			public String getWeaponColumnNameOverride() {
				return "Weapon";
			}

			@Override
			public String getNoMatchingBlueprintsLabelOverride() {
				return "No matching weapons";
			}

			@Override
			public String getMaximumOrderValueLabelOverride() {
				return "Data Archives available";
			}

			@Override
			public String getCurrentOrderValueLabelOverride() {
				return "Data Archives required";
			}
			@Override
			public String getItemGoesOverMaxValueStringOverride() {
				return "Not enough Data Archives";
			}
			@Override
			public String getCustomOrderLabelOverride() {
				return "Order Specific Weapons";
			}
			@Override
			public String getNoProductionOrdersLabelOverride() {
				return "No available orders";
			}
			@Override
			public boolean withQuantityLimits() {
				return false;
			}
			@Override
			public boolean isUseCreditSign() {
				return false;
			}

			@Override
			public int getCostOverride(Object item) {
				if (item instanceof WeaponSpecAPI) {
					return getDataStorageCost((WeaponSpecAPI) item);
				}
				return -1;
			}
			
			@Override
			public void notifyProductionSelected(FactionProductionAPI production) {
				
				int cost = production.getTotalCurrentCost();
				AddRemoveCommodity.addItemLossText(new SpecialItemData("dpl_data_archive", null), cost, dialog.getTextPanel());
				Global.getSector().getPlayerFleet().getCargo().removeItems(CargoItemType.SPECIAL, new SpecialItemData("dpl_data_archive", null), cost);
				
				SectorEntityToken entity = dialog.getInteractionTarget();
				if (entity.getActivePerson() != null) {
					try {
						RepActions action = RepActions.valueOf(Integer.toString(cost));
						RepActionEnvelope envelope = new RepActionEnvelope(action, null, dialog.getTextPanel());
						ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(envelope, entity.getActivePerson());
					} catch (Throwable t) {
						CustomRepImpact impact = new CustomRepImpact();
						impact.delta = cost * 0.01f;
						ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.CUSTOM, impact,
													  null, dialog.getTextPanel(), true), entity.getActivePerson());
					}
				}
				
				for (ItemInProductionAPI item : production.getCurrent()) {
					if (item.getType() == ProductionItemType.WEAPON) {
						AddRemoveCommodity.addWeaponGainText(item.getSpecId(), item.getQuantity(), dialog.getTextPanel());
						Global.getSector().getPlayerFleet().getCargo().addWeapons(item.getSpecId(), item.getQuantity());
					}
				}
				
				FireBest.fire(null, dialog, memoryMap, "dpl_RossSpecificWeaponPicked");
				
				Global.getSoundPlayer().playUISound("ui_cargo_machinery_drop", 1f, 1f);
			}
		});
	}
}
