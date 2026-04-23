package data.scripts.world.systems;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreLifecyclePluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.WarningBeaconEntityPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager.RemnantFleetInteractionConfigGen;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

public class dpl_acheron {
	
	public static String dpl_NASCENT_WELL_KEY = "$dpl_acheron_well";
	
	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Acheron");
		//system.setType(StarSystemType.NEBULA);
		system.setName("Acheron"); // to get rid of "Star System" at the end of the name
		system.setType(StarSystemType.DEEP_SPACE);
		system.addTag(Tags.THEME_UNSAFE);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		//system.getLocation().set(2500, 3000);
		system.getLocation().set(-36000, 12000);
		
		HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyperTerrain);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, 200, 0, 360f);
//		editor.regenNoise();
//		editor.noisePrune(0.8f);
//		editor.regenNoise();

		SectorEntityToken center = system.initNonStarCenter();
		
		system.setLightColor(new Color(225,175,125,255)); // light color in entire system, affects all entities
		center.addTag(Tags.AMBIENT_LS);
		
		String type = "barren";
		type = "barren-bombarded";
		PlanetAPI rock = system.addPlanet("acheron", center, "Acheron", type, 0, 250, 1200, 40);
		//rock.setCustomDescriptionId("???");
		rock.getMemoryWithoutUpdate().set("$dpl_acheron", true);
		
		rock.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		rock.getMarket().addCondition(Conditions.COLD);
		rock.getMarket().addCondition(Conditions.DARK);
		
		rock.getMarket().getMemoryWithoutUpdate().set("$ruinsExplored", true);
		//If this is done, shows up in intel planet list; doesn't matter either way
		//Misc.setFullySurveyed(rock.getMarket(), null, false);
		
		rock.setOrbit(null);
		rock.setLocation(1200, 600);
		
		SectorEntityToken dpl_acheronStation = system.addCustomEntity("dpl_acheronStation",
				"Acheron Station", "dpl_AcheronStation", "dpl_phase_lab");
				// "Jangala Station", "station_jangala_type", "hegemony");
		
		dpl_acheronStation.setCircularOrbitPointingDown(rock, 60 + 180, 360, 30);
		dpl_acheronStation.setCustomDescriptionId("dpl_AcheronStation");
		
		CustomCampaignEntityAPI beacon = system.addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
		beacon.setCircularOrbitPointingDown(rock, 0, 2500, 60);
		
		beacon.getMemoryWithoutUpdate().set("$ttBlackSite", true);
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_ID_KEY, Pings.WARNING_BEACON3);
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_FREQ_KEY, 1.5f);
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.PING_COLOR_KEY, new Color(250,125,0,255));
		beacon.getMemoryWithoutUpdate().set(WarningBeaconEntityPlugin.GLOW_COLOR_KEY, new Color(250,55,0,255));
		
		system.generateAnchorIfNeeded();
		
		NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(beacon, 50f);
		well.addTag(Tags.NO_ENTITY_TOOLTIP);
		well.setColorOverride(new Color(125, 50, 255));
		hyper.addEntity(well);
		well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(beacon, 0);
		
		Global.getSector().getMemoryWithoutUpdate().set(dpl_NASCENT_WELL_KEY, well);
	}
	
	public static NascentGravityWellAPI getWell() {
		return (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(dpl_NASCENT_WELL_KEY);
	}
	
}













