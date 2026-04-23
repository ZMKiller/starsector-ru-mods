package org.amazigh.foundry.scripts;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.util.Misc;

import org.amazigh.foundry.scripts.ai.ASF_AlbatreosMagicMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_FormiaMagicMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_LamiaMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_LernaMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_LernaSubMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_MagicSwarmMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_PersisMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_PersisSwarmMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_PhiliaMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_RocketArtyMagicMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_TermiteMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_WeaverDrunkRocketAI;
import org.amazigh.foundry.scripts.everyframe.ASF_arkTechSpawnPlugin;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;
import exerelin.utilities.NexFactionConfig.StartFleetSet;
import exerelin.utilities.NexFactionConfig.StartFleetType;
import particleengine.BaseIEmitter;
import particleengine.ParticleData;

public class ASF_ModPlugin extends BaseModPlugin {
	
	public static final String LAMIA_MISSILE_ID = "A_S-F_lamia_main";
	public static final String LERNA_MISSILE_ID = "A_S-F_lerna_main";
	public static final String LERNA_SUB_MISSILE_ID = "A_S-F_lerna_sub";
	public static final String DESTRUCTOR_ROCKET_ID = "A_S-F_destructor_rocket";
	public static final String CONTRADICT_MISSILE_ID = "A_S-F_contradict_rocket";
	public static final String OMER_MISSILE_ID = "A_S-F_omer_srm";
	public static final String ASPINA_MISSILE_ID = "A_S-F_aspina_srm";
	public static final String AKVAVIT_MISSILE_ID = "A_S-F_akvavit_srm";
	public static final String FORMIA_MISSILE_ID = "A_S-F_formia_orb";
	public static final String FORMIA_SYS_MISSILE_ID = "A_S-F_formia_orb_sys";
	public static final String ASF_COPPERHEAD_MISSILE_ID = "A_S-F_copperhead_mssl";
	public static final String ASF_ALBATREOS_MISSILE_ID = "A_S-F_albatreos_missile";
	public static final String ASF_PHILIA_MISSILE_ID = "A_S-F_philia_srm";
	public static final String ASF_PHANTASMAGORIA_MICRO_MISSILE_ID = "A_S-F_phantasmagoria_micro_missile";
	public static final String ASF_TERMITE_MISSILE_ID = "A_S-F_termite_srm";
	public static final String ASF_PERSIS_MISSILE_ID = "A_S-F_persis_missile";
	public static final String ASF_PERSIS_SUB_MISSILE_ID = "A_S-F_persis_frag";
	public static final String ASF_WEAVER_ROCKET_ID = "A_S-F_weaver_rocket";
	public static final String ASF_NEXTER_MISSILE_ID = "A_S-F_nexter_mssl";

	public boolean HAS_GRAPHICSLIB = false;
    public boolean isExerelin = false;
    public boolean ratInfestation = false;
    
	public static String PHANTASMAGORIA_ALT_DESCRIPTION = "\"I am only satisfied if my spectators, shivering and shuddering, raise their hands or cover their eyes out of fear of ghosts and devils dashing towards them.\" - Found etched on primary flight control interface.";
    public static String TRANSPARENCE_ALT_DESCRIPTION = "The Transparence is a unique prototype ship featuring a wide variety of bleeding-edge technologies. The Photon Accelerator Core around which this vessel has been constructed stretches the bounds of what could be considered possible by conventional domain science to allow for the ship to deliver cruiser-grade levels of firepower while being nearly as agile as some frigates.";
    public static String RANGDA_ALT_DESCRIPTION = "A prototype testbed for a novel vectored thrust system, the Rangda is one of the most slippery vessels in the sector, able to get in and out of combat with ease.";
    public static String LAFIEL_ALT_DESCRIPTION = "Origins unknown, the designers clearly thought that it'd be sane to expose living crew to a rapidly fluctuating temporal gradient. Aftereffects of combat deployment mean that even with stringent psychological profiling, frequent cycling of crews is highly recommended.";
    public static String PERSENACHIA_ALT_DESCRIPTION = "A heretic, surrounded with the husks of the dead. In order to hide its form, it spreads a dense and violent storm. The glowing fog conceals it while it hunts its prey and then entraps them in a dance of death, creating more victims.";
    
    //New game stuff
    @Override
    public void onNewGameAfterProcGen() {
        //Spawning arkTech
    	ASF_arkTechSpawnPlugin.spawnArkTech(Global.getSector());
    }
    
    public void onApplicationLoad() throws Exception {

        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            HAS_GRAPHICSLIB = true;
            ShaderLib.init();
            //TextureData.readTextureDataCSV((String)"data/config/asf_texture_data.csv");
            LightData.readLightDataCSV((String)"data/config/asf_lights_data.csv");
        }
    	
    	isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
    	if(isExerelin) {
    		if (Global.getSettings().getMissionScore("ASF_phantasmagoria_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetPhant = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> phantasmagoriaFleet = new ArrayList<>(1);
    			phantasmagoriaFleet.add("A_S-F_phantasmagoria_starter");
    			fleetSetPhant.addFleet(phantasmagoriaFleet);
    			Global.getSettings().getDescription("A_S-F_phantasmagoria", Description.Type.SHIP).setText2(PHANTASMAGORIA_ALT_DESCRIPTION);
    		}
    		if (Global.getSettings().getMissionScore("ASF_transparence_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetTrans = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> transparenceFleet = new ArrayList<>(1);
    			transparenceFleet.add("A_S-F_transparence_starter");
    			fleetSetTrans.addFleet(transparenceFleet);
    			Global.getSettings().getDescription("A_S-F_transparence", Description.Type.SHIP).setText2(TRANSPARENCE_ALT_DESCRIPTION);
    		}
    		if (Global.getSettings().getMissionScore("ASF_rangda_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetRangda = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> rangdaFleet = new ArrayList<>(1);
    			rangdaFleet.add("A_S-F_rangda_starter");
    			fleetSetRangda.addFleet(rangdaFleet);
    			Global.getSettings().getDescription("A_S-F_rangda", Description.Type.SHIP).setText2(RANGDA_ALT_DESCRIPTION);
    		}
    		if (Global.getSettings().getMissionScore("ASF_persenachia_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetPers = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> persenachiaFleet = new ArrayList<>(1);
    			persenachiaFleet.add("A_S-F_persenachia_starter");
    			fleetSetPers.addFleet(persenachiaFleet);
    			Global.getSettings().getDescription("A_S-F_persenachia", Description.Type.SHIP).setText2(PERSENACHIA_ALT_DESCRIPTION);
    		}
    		
    		// so you *can* unlock the lafiel as a custom start, "just" beat all special missions with over 95% score (only 75% score is needed for the test mission tho!)
    		if (Global.getSettings().getMissionScore("ASF_phantasmagoria_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_transparence_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_rangda_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_persenachia_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_arkDefenders") > 0.95f && Global.getSettings().getMissionScore("ASF_testbattle") > 0.75f) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetLafiel = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> lafielFleet = new ArrayList<>(1);
    			lafielFleet.add("A_S-F_lafiel_starter");
    			fleetSetLafiel.addFleet(lafielFleet);
    			Global.getSettings().getDescription("A_S-F_lafiel", Description.Type.SHIP).setText2(LAFIEL_ALT_DESCRIPTION);
    		}
    		
    	}
    	
    	
    	// fucking with variants if we have rotcesrats installed
    	ratInfestation = Global.getSettings().getModManager().isModEnabled("rotcesrats");
    	if(ratInfestation) {
    		Global.getSettings().resetCached();
            if (Global.getSettings().getVariant("A_S-F_rinka_p_raider") != null) {
                ShipVariantAPI RaiderRinkaVariant = Global.getSettings().getVariant("A_S-F_rinka_p_raider");
                if (Global.getSettings().getWeaponSpec("rr_miser_coil") != null) {
                        RaiderRinkaVariant.setNumFluxCapacitors(RaiderRinkaVariant.getNumFluxCapacitors()-1); //8 to 7
                        RaiderRinkaVariant.clearSlot("WS0002");
                        RaiderRinkaVariant.addWeapon("WS0002","rr_miser_coil"); //from thumper
                        RaiderRinkaVariant.clearHullMods(); // to strip big mags
                        RaiderRinkaVariant.addMod("armoredweapons");
                        RaiderRinkaVariant.addMod("fluxdistributor");
                        RaiderRinkaVariant.addMod("frontemitter");
                        
                }
            }
    	}
    	
	}

	
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case LAMIA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_LamiaMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case LERNA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_LernaMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case LERNA_SUB_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_LernaSubMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case DESTRUCTOR_ROCKET_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_RocketArtyMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case CONTRADICT_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_RocketArtyMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case OMER_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASPINA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case AKVAVIT_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case FORMIA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_FormiaMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case FORMIA_SYS_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_FormiaMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_COPPERHEAD_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_RocketArtyMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_ALBATREOS_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_AlbatreosMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PHILIA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_PhiliaMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PHANTASMAGORIA_MICRO_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_TERMITE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_TermiteMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PERSIS_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_PersisMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PERSIS_SUB_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_PersisSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_WEAVER_ROCKET_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_WeaverDrunkRocketAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_NEXTER_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_RocketArtyMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
                return null;
        }
    }
    
    @Override
	public void onCodexDataGenerated() {
		
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_auditor"), CodexDataV2.getShipEntryId("A_S-F_auditor_mod"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_rinka"), CodexDataV2.getShipEntryId("A_S-F_rinka_p"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_gaoler"), CodexDataV2.getShipEntryId("A_S-F_mancatcher"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_bathory"), CodexDataV2.getShipEntryId("A_S-F_bathory_mod"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_henki"), CodexDataV2.getShipEntryId("A_S-F_pneuma"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_niteo"), CodexDataV2.getShipEntryId("A_S-F_lafiel"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_morris"), CodexDataV2.getShipEntryId("A_S-F_morris_p"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_peryton"), CodexDataV2.getShipEntryId("A_S-F_perytonne"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_lanner"), CodexDataV2.getShipEntryId("A_S-F_lanner_p"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_apologee"), CodexDataV2.getShipEntryId("apogee"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_phobia"), CodexDataV2.getShipEntryId("A_S-F_jorogumo"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_initone"), CodexDataV2.getShipEntryId("A_S-F_initone_lg"));
		CodexDataV2.makeRelated(CodexDataV2.getShipEntryId("A_S-F_chompiron"), CodexDataV2.getShipEntryId("champion"));
		// linking some variants to their base hulls
		
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyBallistic"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyEnergy"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyBallistic"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyFlux"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyBallistic"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyShields"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyBallistic"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyTargeting"));
		
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyEnergy"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyFlux"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyEnergy"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyShields"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyEnergy"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyTargeting"));

		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyFlux"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyShields"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyFlux"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyTargeting"));
		
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_anarchyShields"), CodexDataV2.getHullmodEntryId("A_S-F_anarchyTargeting"));
		// linking the anarchy hullmods, because it seems like an idea
		
		
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getWeaponEntryId("A_S-F_painter"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getWeaponEntryId("A_S-F_narc"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getShipSystemEntryId("A_S-F_designate"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getHullmodEntryId("A_S-F_ArtyWepHighTech"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getHullmodEntryId("A_S-F_ArtyWepRemnant"));
		CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getHullmodEntryId("A_S-F_ArtyWepMidline"));
					// CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getHullmodEntryId("A_S-F_ArtyWepDerelict"));
					// CodexDataV2.makeRelated(CodexDataV2.getHullmodEntryId("A_S-F_ArtyMount"), CodexDataV2.getHullmodEntryId("A_S-F_ArtyWepLowTech"));
			// adding these but commented out, under the assumption that i will actually make them eventually!?
		// linking artillery stuff
		
	}
    

    // Custom Particle Engine emitter, radial emission within an arc, with random velocity+distance matching facing within a provided min/max value
    // note that dist+vel scale together by default, but it can be set to independent/random
    public static class ASF_RadialEmitter extends BaseIEmitter {
    	
        private final Vector2f location;
		private float angle, arc, minLife, maxLife, minSize, maxSize, minVelocity, addVelocity, minDistance, addDistance, emissionOffsetBase, emissionOffsetAdd, coreDispersion, sizeScale;
        private final float[] color = new float[] {1f, 1f, 1f, 1f};
        private boolean linkage, lifeLink, angleSplit;
        private CombatEntityAPI anchor;
        
        public ASF_RadialEmitter(CombatEntityAPI host) {
        	anchor = host;
            location = new Vector2f();
            angle = coreDispersion = sizeScale = emissionOffsetBase = emissionOffsetAdd = minDistance = addDistance = 0f;
            arc = 360f; //this defaults to giving omnidirectional emission, as that's my main use-case for this Emitter
            minLife = maxLife = 0.5f;
            minSize = 20f;
            maxSize = 30f;
            minVelocity = addVelocity = 1f;
            linkage = true;
            lifeLink = angleSplit = false;
        }

		@Override
        public SpriteAPI getSprite() { //graphics/portraits/characters/sebestyen.png
            return particleengine.Utils.getLoadedSprite("graphics/fx/particlealpha64sq.png");
        }

        public ASF_RadialEmitter anchor(CombatEntityAPI anchor) {
            this.anchor = anchor;
            return this;
        }

        public ASF_RadialEmitter location(Vector2f location) {
            this.location.set(location);
            return this;
        }

        /**
         * @param angle
         * @return Starting angle an The arc in which to emit particles (defaults have it emit in all directions)
         */
        public ASF_RadialEmitter angle(float angle, float arc) {
            this.angle = angle;
            this.arc = arc;
            return this;
        }
        
        public ASF_RadialEmitter life(float minLife, float maxLife) {
            this.minLife = minLife;
            this.maxLife = maxLife;
            return this;
        }
        
        public ASF_RadialEmitter size(float minSize, float maxSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            return this;
        }

        public ASF_RadialEmitter color(float r, float g, float b, float a) {
            color[0] = r;
            color[1] = g;
            color[2] = b;
            color[3] = a;
            return this;
        }
        
        public ASF_RadialEmitter distance(float minDistance, float addDistance) {
            this.minDistance = minDistance;
            this.addDistance = addDistance;
            return this;
        }
        
        public ASF_RadialEmitter velocity(float minVelocity, float addVelocity) {
            this.minVelocity = minVelocity;
            this.addVelocity = addVelocity;
            return this;
        }
        
        /**
         * @param emissionOffset
         * @return an additional angle added to the angle of velocity (for when you want the angle of velocity/distance to not be linked)
         */
        public ASF_RadialEmitter emissionOffset(float emissionOffsetBase, float emissionOffsetAdd) {
            this.emissionOffsetBase = emissionOffsetBase;
            this.emissionOffsetAdd = emissionOffsetAdd;
            return this;
        }
        
        /**
         * @param velDistLinkage
         * @return If set to false then velocity and distance random variance will scale independently of each other
         */
        public ASF_RadialEmitter velDistLinkage(boolean linkage) {
            this.linkage = linkage;
            return this;
        }
        
        /**
         * @param coreDispersion
         * @return random radial offset around point (for 2-dimensional offsetting) (Ignored if below 1)
         */
        public ASF_RadialEmitter coreDispersion(float coreDispersion) {
            this.coreDispersion = coreDispersion;
            return this;
        }
        
        /**
         * @param lifeLink
         * @return If lifetime should be linked to vel/dist scaling (defaults to false)
         */
        public ASF_RadialEmitter lifeLinkage(boolean lifeLinkage) {
            this.lifeLink = lifeLinkage;
            return this;
        }
        
        /**
         * @param angleSplit
         * @return If angle of dist/vel should be generated seperate from each other (defaults to false)
         */
        public ASF_RadialEmitter angleSplit(boolean angleSplit) {
            this.angleSplit = angleSplit;
            return this;
        }
        
        /**
         * @param sizeScale
         * @return Has particles increase in size by this amount over spawn distance (positive values for bigger at max range, negative for smaller at max range)
         */
        public ASF_RadialEmitter sizeScale(float sizeScale) {
            this.sizeScale = sizeScale;
            return this;
        }
        
        
        @Override
        public Vector2f getLocation() {
            return location;
        }

        @Override
        protected ParticleData initParticle(int i) {
            ParticleData data = new ParticleData();

            float rand = MathUtils.getRandomNumberInRange(0f, 1f);
            
            // Life uniformly random between minLife and maxLife
            float life = MathUtils.getRandomNumberInRange(minLife, maxLife);
            
            if (lifeLink) {
            	life = minLife + ((maxLife - minLife) * rand);
            }
            
            data.life(life).fadeTime(0f, life);
            
            // velocity is random within the defined range
            float theta = angle + MathUtils.getRandomNumberInRange(0, arc);
            Vector2f vel = Misc.getUnitVectorAtDegreeAngle(theta + (emissionOffsetBase + MathUtils.getRandomNumberInRange(0, emissionOffsetAdd)));
            vel.scale(minVelocity + (rand * addVelocity));
            
            if (angleSplit) {
            	theta = angle + MathUtils.getRandomNumberInRange(0, arc);
            }
            
            Vector2f pt = new Vector2f(0,0);
            
            float sizeMult = rand;
            if (linkage) {
                pt = MathUtils.getPointOnCircumference(null, minDistance + (rand * addDistance), theta);
            } else {
            	sizeMult = MathUtils.getRandomNumberInRange(0f, 1f);
                pt = MathUtils.getPointOnCircumference(null, minDistance + (sizeMult * addDistance), theta);
            }
            
            if (coreDispersion >= 1f) {
            	Vector2f.add(MathUtils.getRandomPointInCircle(null, coreDispersion), pt, pt);
            }
            
            // Add the anchor's velocity, if it exists
            if (anchor != null) {
                Vector2f.add(anchor.getVelocity(), vel, vel);
            }
            data.offset(pt).velocity(vel);
            
            // Size uniformly random between minSize and maxSize (but with distance scaling stuff as an optional add on)
            float size = MathUtils.getRandomNumberInRange(minSize, maxSize) + (sizeScale * sizeMult);
            data.size(size, size);
            
            // Color
            data.color(color);
            
            return data;
        }
        
    }
}