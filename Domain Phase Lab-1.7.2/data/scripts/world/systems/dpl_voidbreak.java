package data.scripts.world.systems;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager.WormholeItemData;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;

import data.scripts.world.dpl_phase_labAddEntities;

public class dpl_voidbreak {

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Voidbreak");
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight");
		system.getLocation().set(-w/2f - 3000f, -500);
		
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("dpl_voidbreak", // unique id for this star 
										    StarTypes.BLACK_HOLE,  // id in planets.json
										    400f, 		  // radius (in pixels at default zoom)
										    350f); // corona radius, from star edge
		SectorEntityToken event_horizon = system.addTerrain(Terrain.EVENT_HORIZON,
				new StarCoronaTerrainPlugin.CoronaParams(750, //Range
									375,	//Extra range?
									star,	//ORBITAL ENTITY
									-10f, 	//WindStrength;
									0f, 	//flareProbability?;
									5f)	//crLossMult;
				);
		
		SectorEntityToken dpl_voidbreak_SL = system.addCustomEntity(null, // unique id
				 null, // name - if null, defaultName from custom_entities.json will be used
				 Entities.STABLE_LOCATION, // type of object, defined in custom_entities.json
				 Factions.NEUTRAL); // faction
		
		float orbitDays = 1500f / (10f + (float) Math.random() * 5f);
		dpl_voidbreak_SL.setCircularOrbitPointingDown(star, 150, 1500f, orbitDays);
		
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "radiant_Assault", ShipCondition.WRECKED, 2550f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "radiant_Assault", ShipCondition.WRECKED, 2541f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "radiant_Assault", ShipCondition.WRECKED, 2552f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "radiant_Standard", ShipCondition.WRECKED, 2543f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "radiant_Standard", ShipCondition.WRECKED, 2554f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "radiant_Standard", ShipCondition.WRECKED, 2545f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "nova_Attack", ShipCondition.WRECKED, 2555f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "nova_Attack", ShipCondition.WRECKED, 2544f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "nova_Attack", ShipCondition.WRECKED, 2553f, false, false, null);
		dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "nova_Attack", ShipCondition.WRECKED, 2542f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2540f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2539f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2538f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2537f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2536f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2535f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2534f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 2533f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "apex_Overdriven", ShipCondition.WRECKED, 2558f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "apex_Overdriven", ShipCondition.WRECKED, 2532f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "apex_Overdriven", ShipCondition.WRECKED, 2559f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "apex_Overdriven", ShipCondition.WRECKED, 2560f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "apex_Overdriven", ShipCondition.WRECKED, 2561f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "apex_Overdriven", ShipCondition.WRECKED, 2546f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "brilliant_Standard", ShipCondition.WRECKED, 2547f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "brilliant_Standard", ShipCondition.WRECKED, 2548f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "brilliant_Standard", ShipCondition.WRECKED, 2549f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "brilliant_Standard", ShipCondition.WRECKED, 2551f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "brilliant_Standard", ShipCondition.WRECKED, 2556f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "brilliant_Standard", ShipCondition.WRECKED, 2530f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2557f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2529f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2565f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2566f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2528f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2527f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2567f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2542f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "scintilla_Support", ShipCondition.WRECKED, 2541f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "glimmer_Support", ShipCondition.WRECKED, 2535f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "glimmer_Support", ShipCondition.WRECKED, 2536f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "glimmer_Support", ShipCondition.WRECKED, 2537f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "glimmer_Support", ShipCondition.WRECKED, 2538f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "lumen_Standard", ShipCondition.WRECKED, 2545f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "lumen_Standard", ShipCondition.WRECKED, 2546f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "lumen_Standard", ShipCondition.WRECKED, 2547f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "lumen_Standard", ShipCondition.WRECKED, 2548f, false, false, null);
		
		system.setLightColor(new Color(200, 155, 255)); // light color in entire system, affects all entities		
		system.autogenerateHyperspaceJumpPoints(true, true);
	}
}










