package data.scripts.world.systems;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
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
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import data.scripts.world.dpl_phase_labAddEntities;

public class dpl_helheim implements SectorGeneratorPlugin{

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Helheim");
		Random random = new Random();
		SectorEntityToken entity = null;
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");

		PlanetAPI star = system.initStar("dpl_helheim", // unique id for this star
										 StarTypes.BLUE_SUPERGIANT, // id in planets.json
										 1600f,		// radius (in pixels at default zoom)
										 800f); // corona radius, from star edge
		SectorEntityToken corona = system.addTerrain(Terrain.CORONA,
				new StarCoronaTerrainPlugin.CoronaParams(750, //Range
									375,	//Extra range?
									star,	//ORBITAL ENTITY
									10f, 	//WindStrength;
									0f, 	//flareProbability?;
									5f)	//crLossMult;
				);

		system.autogenerateHyperspaceJumpPoints(true, true);
		
		if (star != null) {
	        CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec("dpl_damaged_tap");
	        float orbitRadius = star.getRadius() + spec.getDefaultRadius() + 100f;
	        float orbitDays = orbitRadius / 20f;
	        entity = BaseThemeGenerator.addSalvageEntity(system, "dpl_damaged_tap", Factions.NEUTRAL);
	        entity.setCircularOrbitPointingDown(star, random.nextFloat() * 360f, orbitRadius, orbitDays);
	        CampaignFleetAPI defenders = CreateDefenders();
	        entity.getMemory().set("$defenderFleet", defenders);
	        entity.setId("dpl_helheim_damaged_tap");
	    }
		
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		float minRadius = plugin.getTileSize() * 2f;
		float radius = system.getMaxRadiusInHyperspace();
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, radius + minRadius * 0.5f, 0f, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, radius + minRadius, 0f, 360f, 0.25f);
	}
	
	public CampaignFleetAPI CreateDefenders() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.OMEGA,
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
        target.setName("Automatic Defense");
        target.setNoFactionInName(true);
        
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.OMEGA, "f1", true);
		fleet.getFleetData().addFleetMember("dpl_tesseract_Broken");
		FleetMemberAPI member = fleet.getFlagship();
		
		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
		PersonAPI person = plugin.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, genRandom);
		member.setCaptain(person);
		
		target.setCommander(person);
		target.getFleetData().addFleetMember(member);

		CampaignFleetAPI fleet2 = Global.getFactory().createEmptyFleet(Factions.OMEGA, "f2", true);
		fleet2.getFleetData().addFleetMember("dpl_tesseract_Broken");
		FleetMemberAPI member2 = fleet2.getFlagship();
		
		AICoreOfficerPlugin plugin2 = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
		PersonAPI person2 = plugin2.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, genRandom);
		member2.setCaptain(person2);
		
		target.getFleetData().addFleetMember(member2);
		
		target.getFleetData().sort();
		List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		member.setVariant(member.getVariant().clone(), false, false);
		member.getVariant().setSource(VariantSource.REFIT);
		member.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
		member.getVariant().addPermaMod("dpl_burnt_electronics");
		member.getVariant().addSuppressedMod("shard_spawner");
		member.getVariant().addPermaMod("faulty_grid");
		member.getVariant().addPermaMod("degraded_shields");
		member.getVariant().addPermaMod("glitched_sensors");
		
		member2.setVariant(member2.getVariant().clone(), false, false);
		member2.getVariant().setSource(VariantSource.REFIT);
		member2.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		member2.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
		member2.getVariant().addPermaMod("dpl_burnt_electronics");
		member2.getVariant().addSuppressedMod("shard_spawner");
		member2.getVariant().addPermaMod("faulty_grid");
		member2.getVariant().addPermaMod("degraded_shields");
		member2.getVariant().addPermaMod("glitched_sensors");

        Misc.makeHostile(target);
        return target;
    }

}
