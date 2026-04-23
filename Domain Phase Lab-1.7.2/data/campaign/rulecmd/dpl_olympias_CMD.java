package data.campaign.rulecmd;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.TASK_FORCE;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * 
 *	dpl_jasp_CMD <action> <parameters>
 */
public class dpl_olympias_CMD extends BaseCommandPlugin {
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
				
		if ("spawnFleets".equals(action)) {
			StarSystemAPI system = pf.getStarSystem();
			SectorEntityToken Olympias = system.getEntityById("dpl_Olympias");
			SectorEntityToken JumpPoint = system.getJumpPoints().get(0);
			if (Olympias == null) return false;
			List<FactionAPI> AllFactions = Global.getSector().getAllFactions();
			WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<FactionAPI>(getGenRandom());
			for (FactionAPI faction : AllFactions) {
				if (faction.isShowInIntelTab()) {
					picker.add(faction);
				}
			}
			picker.add(Global.getSector().getFaction(Factions.REMNANTS));
			picker.add(Global.getSector().getFaction(Factions.DERELICT));
			FactionAPI theFaction = picker.pick();
			if (theFaction.getId().equals(Factions.REMNANTS)) {
				int j = (int) Math.round(Math.random()*2.5 + 2);
				for (int i=0; i<j; i++) {
					FleetParamsV3 params1 = new FleetParamsV3(
			                null,
			                null,
			                theFaction.getId(),
			                null,
			                TASK_FORCE,
			                300f, // combatPts
			                0f, // freighterPts
			                0f, // tankerPts
			                0f, // transportPts
			                0f, // linerPts
			                0f, // utilityPts
			                3f // qualityMod
			        );
			    	params1.averageSMods = 3;
			    	CampaignFleetAPI fleet1;
			    	fleet1 = FleetFactoryV3.createFleet(params1);
			    	
			    	float s = (float) Math.random();
			    	if (s>0.975 && theFaction.getId().equals(Factions.REMNANTS)) {
			    		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.OMEGA, "", true);
			    		WeightedRandomPicker<String> picker1 = new WeightedRandomPicker<String>(getGenRandom());
			    		picker1.add("tesseract_Attack");
			    		picker1.add("tesseract_Attack2");
			    		picker1.add("tesseract_Strike");
			    		picker1.add("tesseract_Disruptor");
			    		fleet.getFleetData().addFleetMember(picker1.pick());
			    		FleetMemberAPI member = fleet.getFlagship();
			    		
			    		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
			    		PersonAPI person = plugin.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, getGenRandom());
			    		member.setCaptain(person);
			    		
			    		int i1 = fleet1.getFleetData().getMembersListCopy().size() - 1;
			    		FleetMemberAPI last = fleet1.getFleetData().getMembersListCopy().get(i1);
			    		fleet1.getFleetData().removeFleetMember(last);
			    		
			    		fleet1.setCommander(person);
			    		fleet1.getFleetData().addFleetMember(member);
			    		fleet1.getFleetData().sort();
			    		List<FleetMemberAPI> members = fleet1.getFleetData().getMembersListCopy();
			    		for (FleetMemberAPI curr : members) {
			    			if (!curr.getVariant().hasHullMod("shard_spawner")) {
			    				AICoreOfficerPlugin plugin1 = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
			    				PersonAPI person1 = plugin1.createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, getGenRandom());
			    				curr.setCaptain(person1);
			    			}
			    			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
			    		}
			    		
			    		member.setVariant(member.getVariant().clone(), false, false);
			    		member.getVariant().setSource(VariantSource.REFIT);
			    		member.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
			    		member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
			    	}
			    	
			    	CargoAPI cargo1 = Global.getFactory().createCargo(true);
					cargo1.addSpecial(new SpecialItemData("dpl_data_archive", null), 3);
					BaseSalvageSpecial.addExtraSalvage(fleet1, cargo1);

					Misc.makeHostile(fleet1);
					Misc.makeNoRepImpact(fleet1, "$dpl_OlympiasFleet");

					Vector2f pos = pf.getLocation();
					
					fleet1.setName("Elite Expedition Fleet");
					fleet1.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_OlympiasFleet");
					fleet1.getMemoryWithoutUpdate().set(MemFlags.FLEET_MILITARY_RESPONSE, "$dpl_OlympiasFleet");
					fleet1.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, Olympias, 200f, null);
					fleet1.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, JumpPoint, 200f, null);
					system.addEntity(fleet1);
			        fleet1.setLocation(pos.x*1.5f+100*((float) Math.random()*2-1), pos.y*1.5f+100*((float) Math.random()*2-1));
				}
			} else {
				int j = (int) Math.round(Math.random()*3.5 + 3);
				for (int i=0; i<j; i++) {
					FleetParamsV3 params1 = new FleetParamsV3(
			                null,
			                null,
			                theFaction.getId(),
			                null,
			                TASK_FORCE,
			                350f, // combatPts
			                0f, // freighterPts
			                0f, // tankerPts
			                0f, // transportPts
			                0f, // linerPts
			                0f, // utilityPts
			                3f // qualityMod
			        );
			    	params1.averageSMods = 3;
			    	CampaignFleetAPI fleet1;
			    	fleet1 = FleetFactoryV3.createFleet(params1);
			    	
			    	CargoAPI cargo1 = Global.getFactory().createCargo(true);
					cargo1.addSpecial(new SpecialItemData("dpl_data_archive", null), 2);
					BaseSalvageSpecial.addExtraSalvage(fleet1, cargo1);

					Misc.makeHostile(fleet1);
					Misc.makeNoRepImpact(fleet1, "$dpl_OlympiasFleet");

					fleet1.setName("Elite Expedition Fleet");
					fleet1.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_OlympiasFleet");
					fleet1.getMemoryWithoutUpdate().set(MemFlags.FLEET_MILITARY_RESPONSE, "$dpl_OlympiasFleet");
					fleet1.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, Olympias, 200f, null);
					fleet1.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, JumpPoint, 200f, null);
					system.addEntity(fleet1);
			        Vector2f pos = pf.getLocation();
			        fleet1.setLocation(pos.x*1.5f+100*((float) Math.random()*2-1), pos.y*1.5f+100*((float) Math.random()*2-1));
				}
			}
		}
		return false;
	}
}
