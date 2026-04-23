package data.scripts.world.systems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import data.scripts.world.dpl_phase_labAddEntities;

public class dpl_proving_ground implements SectorGeneratorPlugin{

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Muspelheim");
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

		PlanetAPI star = system.initStar("dpl_proving_ground", // unique id for this star
										 StarTypes.YELLOW, // id in planets.json
										 600f,		// radius (in pixels at default zoom)
										 250f); // corona radius, from star edge
		
		SectorEntityToken ResonanceArray1 = system.addCustomEntity("dpl_resonance_array1", // unique id
				 "Resonance Array Balder", // name - if null, defaultName from custom_entities.json will be used
				 "dpl_resonance_array", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		ResonanceArray1.setCircularOrbitPointingDown(star, 0, 1020, 20);
		ResonanceArray1.setCustomDescriptionId("dpl_resonance_array");
		
		SectorEntityToken ResonanceArray2 = system.addCustomEntity("dpl_resonance_array2", // unique id
				 "Resonance Array Heimdall", // name - if null, defaultName from custom_entities.json will be used
				 "dpl_resonance_array", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		ResonanceArray2.setCircularOrbitPointingDown(star, 180, 1020, 20);
		ResonanceArray2.setCustomDescriptionId("dpl_resonance_array");
		
		PlanetAPI dpl_security = system.addPlanet("dpl_security", star, "Lab Security Department", "lava", 12, 100, 2025, 56);
		
		dpl_security.setCustomDescriptionId("dpl_security");		
		
		PlanetAPI dpl_factory = system.addPlanet("dpl_factory", star, "Lab Factory", "cryovolcanic", 5, 225, 3350, 119);
		
		dpl_factory.setCustomDescriptionId("dpl_factory");
		
		PlanetAPI dpl_research_site_v = system.addPlanet("dpl_research_site_v", star, "Research Site V", "terran", 0, 200, 5420, 244);
		
		dpl_research_site_v.setCustomDescriptionId("dpl_research_site_v");
		dpl_research_site_v.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
		dpl_research_site_v.getSpec().setGlowColor(new Color(255,255,255,255));
		dpl_research_site_v.getSpec().setUseReverseLightForGlow(true);
		dpl_research_site_v.applySpecChanges();
		
		//Cryosleeper station
			SectorEntityToken cryosleeperStation = system.addCustomEntity("dpl_station_cryosleeper",
					"Cryosleeper Station", "dpl_station_cryosleeper", "dpl_phase_lab");
					// "Jangala Station", "station_jangala_type", "hegemony");
			
			cryosleeperStation.setCircularOrbitPointingDown(system.getEntityById("dpl_research_site_v"), 48 + 180, 360, 30);
			cryosleeperStation.setCustomDescriptionId("dpl_station_cryosleeper");
		
		SectorEntityToken dpl_mirror1 = system.addCustomEntity("dpl_mirror1", // unique id
				 "Stellar Mirror I", // name - if null, defaultName from custom_entities.json will be used
				 "stellar_mirror", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		dpl_mirror1.setCircularOrbitPointingDown(dpl_research_site_v, 24, 300, 30);
		
		SectorEntityToken dpl_mirror2 = system.addCustomEntity("dpl_mirror2", // unique id
				 "Stellar Mirror II", // name - if null, defaultName from custom_entities.json will be used
				 "stellar_mirror", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		dpl_mirror2.setCircularOrbitPointingDown(dpl_research_site_v, 48, 300, 30);
		
		SectorEntityToken dpl_mirror3 = system.addCustomEntity("dpl_mirror3", // unique id
				 "Stellar Mirror III", // name - if null, defaultName from custom_entities.json will be used
				 "stellar_mirror", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		dpl_mirror3.setCircularOrbitPointingDown(dpl_research_site_v, 72, 300, 30);
		
		PlanetAPI dpl_research_site_v_moon = system.addPlanet("dpl_research_site_v_moon", dpl_research_site_v, "R.S.V Moon", "rocky_metallic", 30, 50, 700, 30);
		
		dpl_research_site_v_moon.setCustomDescriptionId("dpl_research_site_v_moon");
		
		// Asteroid belts

		system.addAsteroidBelt(star, 100, 7960, 256, 150, 250, Terrain.ASTEROID_BELT, null);		
				
		// Proving Ground Relay - L5 (behind); well, okay, not quite the L5. But whatever.
		SectorEntityToken dpl_proving_ground_relay = system.addCustomEntity("dpl_proving_ground_relay", // unique id
				 "Proving Ground Relay", // name - if null, defaultName from custom_entities.json will be used
				 "comm_relay", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		dpl_proving_ground_relay.setCircularOrbitPointingDown(star, 30, 6000, 284);
		
		SectorEntityToken dpl_proving_ground_array = system.addCustomEntity("dpl_proving_ground_array", // unique id
				 "Proving Ground Array", // name - if null, defaultName from custom_entities.json will be used
				 "sensor_array_makeshift", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		dpl_proving_ground_array.setCircularOrbitPointingDown(star, 150, 6000, 284);
		
		SectorEntityToken dpl_proving_ground_buoy = system.addCustomEntity("dpl_proving_ground_buoy", // unique id
				 "Proving Ground Buoy", // name - if null, defaultName from custom_entities.json will be used
				 "nav_buoy_makeshift", // type of object, defined in custom_entities.json
				 "dpl_phase_lab"); // faction
		
		dpl_proving_ground_buoy.setCircularOrbitPointingDown(star, 270, 6000, 284);
		
		JumpPointAPI inner_jump_point = Global.getFactory().createJumpPoint("inner_jump_point", "Inner System Jump-point");
		OrbitAPI inner_orbit = Global.getFactory().createCircularOrbit(star, 15, 3350, 119);
		inner_jump_point.setOrbit(inner_orbit);
		inner_jump_point.setRelatedPlanet(dpl_factory);
		inner_jump_point.setStandardWormholeToHyperspaceVisual();
		system.addEntity(inner_jump_point);
		
		JumpPointAPI outer_jump_point = Global.getFactory().createJumpPoint("outer_jump_point", "Outer System Jump-point");
		OrbitAPI outer_orbit = Global.getFactory().createCircularOrbit(star, 12, 5420, 244);
		outer_jump_point.setOrbit(outer_orbit);
		outer_jump_point.setRelatedPlanet(dpl_research_site_v);
		outer_jump_point.setStandardWormholeToHyperspaceVisual();
		system.addEntity(outer_jump_point);
		
		system.autogenerateHyperspaceJumpPoints(true, true);
		
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 2f;
		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, radius + minRadius * 0.5f, 0f, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, radius + minRadius, 0f, 360f, 0.25f);
	}

}
