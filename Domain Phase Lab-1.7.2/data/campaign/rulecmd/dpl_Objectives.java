package data.campaign.rulecmd;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.CustomCampaignEntityPlugin;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CampaignObjective;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTNeutrinoBurstFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTPoints;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.CommSnifferIntel;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamVisibilityManager;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import data.scripts.campaign.dpl_HELAEntityPlugin;
import data.scripts.campaign.dpl_relay_explosion_plugin;

/**
 * NotifyEvent $eventHandle <params> 
 * 
 */
public class dpl_Objectives extends BaseCommandPlugin {
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected FactionAPI faction;
	
	public dpl_Objectives() {
		
	}
	
	public dpl_Objectives(SectorEntityToken entity) {
		init(entity);
	}
	
	protected void init(SectorEntityToken entity) {
		memory = entity.getMemoryWithoutUpdate();
		this.entity = entity;
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		faction = entity.getFaction();
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		entity = dialog.getInteractionTarget();
		init(entity);
		
		memory = getEntityMemory(memoryMap);
		
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();

		if (command.equals("build")) {
			String type = params.get(1).getString(memoryMap);
			build(type, Factions.PLAYER);
		} else if (command.equals("activate")) {
			activate();
		} else if (command.equals("makeHostile")) {
			makeHostile();
		} else if (command.equals("HELA_Explosion")) {
			HELA_Explosion();
		} else if (command.equals("ElizaWearDress")) {
			PersonAPI eliza_lovelace = Global.getSector().getImportantPeople().getData("eliza_lovelace").getPerson();
			eliza_lovelace.setPortraitSprite(Global.getSettings().getSpriteName("characters", "eliza_lovelace_dress"));
		} else if (command.equals("ElizaWearUniform")) {
			PersonAPI eliza_lovelace = Global.getSector().getImportantPeople().getData("eliza_lovelace").getPerson();
			eliza_lovelace.setPortraitSprite(Global.getSettings().getSpriteName("characters", "eliza_lovelace"));
		} else if (command.equals("ElizaWearLabCoat")) {
			PersonAPI eliza_lovelace = Global.getSector().getImportantPeople().getData("eliza_lovelace").getPerson();
			eliza_lovelace.setPortraitSprite(Global.getSettings().getSpriteName("characters", "eliza_lovelace_lab_coat"));
		} else if (command.equals("ElizaMovesToRSV")) {
			PersonAPI baird = Global.getSector().getImportantPeople().getData(People.BAIRD).getPerson();
			PersonAPI eliza_lovelace_PP = Global.getSector().getImportantPeople().getData("eliza_lovelace_PP").getPerson();
			baird.getMarket().removePerson(eliza_lovelace_PP);
			baird.getMarket().getCommDirectory().removePerson(eliza_lovelace_PP);
		} else if (command.equals("ElizaMovesToAcademy")) {
			PersonAPI eliza_lovelace_PP = Global.getSector().getImportantPeople().getData("eliza_lovelace_PP").getPerson();
			PersonAPI baird = Global.getSector().getImportantPeople().getData(People.BAIRD).getPerson();
			if (eliza_lovelace_PP != null && baird != null) {
				if (baird.getMarket() != null) {
					Misc.moveToMarket(eliza_lovelace_PP, baird.getMarket(), true);
				}
			}
		} else if (command.equals("dpl_go_to_GA")) {
			GoToGA();
		}
		return true;
	}
	
	public void GoToGA() {
		SectorEntityToken Gate = null;
		
		PersonAPI baird = Global.getSector().getImportantPeople().getData(People.BAIRD).getPerson();
		if (baird != null) {
			StarSystemAPI GA = baird.getMarket().getStarSystem();
			if (GA != null) {
				Gate = GA.getEntityById("galatia_gate");
			}
		}
		
		if (Gate != null) {
			dialog.dismiss();
			Global.getSector().setPaused(false);
			JumpDestination dest = new JumpDestination(Gate, null);
			Global.getSector().doHyperspaceTransition(playerFleet, entity, dest, 2f);
				
				float distLY = Misc.getDistanceLY(Gate, entity);
				
				if (entity.getCustomPlugin() instanceof dpl_HELAEntityPlugin) {
					dpl_HELAEntityPlugin plugin = (dpl_HELAEntityPlugin) entity.getCustomPlugin();
					plugin.showBeingUsed(distLY);
				}
				
				if (Gate.getCustomPlugin() instanceof GateEntityPlugin) {
					GateEntityPlugin plugin = (GateEntityPlugin) Gate.getCustomPlugin();
					plugin.showBeingUsed(distLY);
				}
				
				ListenerUtil.reportFleetTransitingGate(Global.getSector().getPlayerFleet(),
													   entity, Gate);

		}
	}
	
	public void HELA_Explosion() {
		entity.addScript(new dpl_relay_explosion_plugin(entity));
	}
	
	public void updateOrbitingEntities(LocationAPI loc, SectorEntityToken prev, SectorEntityToken built) {
		if (loc == null) return;
		for (SectorEntityToken other : loc.getAllEntities()) {
			if (other == prev) continue;
			if (other.getOrbit() == null) continue;
			if (other.getOrbitFocus() == prev) {
				other.setOrbitFocus(built);
			}
		}
	}
	
	public void activate() {
		entity.addTag("dpl_HELA_activated");
	}
	
	public void makeHostile() {
		if (!entity.getStarSystem().getEntitiesWithTag("dpl_HELA_device").isEmpty()) {
			SectorEntityToken hela_device = entity.getStarSystem().getEntitiesWithTag("dpl_HELA_device").get(0);
			if (hela_device != null) {
				CampaignFleetAPI defenders = CreateDefender();
				hela_device.getMemory().set("$defenderFleet", defenders);
				hela_device.getMemory().set("$hostile", true);
			}
		}
	}
	
	public void build(String type, String factionId) {
		if (entity.hasTag(Tags.NON_CLICKABLE)) return;
		if (entity.hasTag(Tags.FADING_OUT_AND_EXPIRING)) return;
		
		LocationAPI loc = entity.getContainingLocation();
		SectorEntityToken built = loc.addCustomEntity(null,
				 null,
				 type, // type of object, defined in custom_entities.json
				 Factions.NEUTRAL); // faction
		
		if (entity.getOrbit() != null) {
			built.setOrbit(entity.getOrbit().makeCopy());
		}
		
		built.setLocation(entity.getLocation().x, entity.getLocation().y);
		built.setId("dpl_HELA_Device");
		loc.removeEntity(entity);
		updateOrbitingEntities(loc, entity, built);
		
		//entity.setContainingLocation(null);
		built.getMemoryWithoutUpdate().set("$originalStableLocation", entity);
		
		if (text != null) {
			Global.getSoundPlayer().playUISound("ui_objective_constructed", 1f, 1f);
		}
	}

	public CampaignFleetAPI CreateDefender() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                PATROL_LARGE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                2f // qualityMod
        );
    	
    	Random genRandom = null;
    	genRandom = new Random();
    	
    	CampaignFleetAPI target;
    	target = FleetFactoryV3.createFleet(params);
        target.setName("HELA Device Defense");
        target.setNoFactionInName(true);
		
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_HELA_noshield")) {
        	target.getFleetData().addFleetMember("dpl_gate_station_mkII_standard");
        } else {
        	target.getFleetData().addFleetMember("dpl_gate_station_standard");
        }
        FleetMemberAPI member = target.getFlagship();
        member.getVariant().addTag(Tags.TAG_NO_AUTOFIT);
        
        target.getFleetData().sort();
		List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeHostile(target);
        return target;
    }

	public int [] getQuantities() {
		return new int[] {3000, 10000, 3000, 4000};
	}

	public String [] getResources() {
		return new String[] {Commodities.HEAVY_MACHINERY, Commodities.METALS, Commodities.RARE_METALS, Commodities.SUPPLIES};
	}
}















