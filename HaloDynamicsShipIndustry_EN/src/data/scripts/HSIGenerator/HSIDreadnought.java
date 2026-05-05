package data.scripts.HSIGenerator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class HSIDreadnought {

    public static class HSIDreadnoughtFIDConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

//			config.alwaysAttackVsAttack = true;
//			config.leaveAlwaysAvailable = true;
//			config.showFleetAttitude = false;
            config.showTransponderStatus = false;
            config.showEngageText = false;
            config.dismissOnLeave = false;
            //config.lootCredits = false;
            config.withSalvage = false;
            //config.showVictoryText = false;
            config.printXPToDialog = true;

            config.noSalvageLeaveOptionText = HSII18nUtil.getCampaignString("HSI_Continue");


            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                    new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.
                            postPlayerSalvageGeneration(dialog, context, salvage);
                }
                public void notifyLeave(InteractionDialogAPI dialog) {

                    SectorEntityToken other = dialog.getInteractionTarget();
                    if (!(other instanceof CampaignFleetAPI)) {
                        dialog.dismiss();
                        return;
                    }
                    CampaignFleetAPI fleet = (CampaignFleetAPI) other;

                    if (!fleet.isEmpty()) {
                        dialog.dismiss();
                        return;
                    }


                    ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData(Global.getSettings().createEmptyVariant("HSI_Life_Hull",Global.getSettings().getHullSpec("HSI_Life")).getHullVariantId(), ShipRecoverySpecial.ShipCondition.WRECKED, 0f);
                    ship.shipName = "HPS Nova Of Eridanus";
                    DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);
                    CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
                            fleet.getContainingLocation(),
                            Entities.WRECK, Factions.NEUTRAL, params);
                    Misc.makeImportant(entity, "HSI_Dreadnought");

                    entity.getLocation().x = fleet.getLocation().x + (50f - (float) Math.random() * 100f);
                    entity.getLocation().y = fleet.getLocation().y + (50f - (float) Math.random() * 100f);

                    ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                    data.notNowOptionExits = true;
                    data.noDescriptionText = true;
                    DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
                    ShipRecoverySpecial.PerShipData copy = (ShipRecoverySpecial.PerShipData) dsep.getData().ship.clone();
                    copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
                    copy.variantId = null;
                    copy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
                    data.addShip(copy);

                    Misc.setSalvageSpecial(entity, data);

                    Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().set("$HSIFLLostToPlayer",true);
                    AjimusUtils.setTraitorTrigger();
                    Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().set("$HSIisTraitor",true);

                    dialog.setInteractionTarget(entity);
                    RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("AfterZigguratDefeat");
                    dialog.setPlugin(plugin);
                    plugin.init(dialog);
                }

                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = true;
                    bcc.fightToTheLast = true;
                    bcc.enemyDeployAll = true;
                }
            };
            return config;
        }
    }
    public static void addFleet(SectorEntityToken rock) {
        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet("HSI", FleetTypes.PATROL_LARGE, null);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);

        fleet.setName(HSII18nUtil.getCampaignString("HSIDreadnoughtFleetName"));

        fleet.getFleetData().addFleetMember("HSI_Life_Elite_B");
        fleet.getFleetData().ensureHasFlagship();

        fleet.clearAbilities();
        fleet.setTransponderOn(true);

        PersonAPI person = createDreadnoughtCaptain();
        fleet.setCommander(person);

        FleetMemberAPI flagship = fleet.getFlagship();
        flagship.setCaptain(person);
        flagship.updateStats();
        flagship.getRepairTracker().setCR(flagship.getRepairTracker().getMaxCR());
        flagship.setShipName("HPS Nova Of Eridanus");

        // to "perm" the variant so it gets saved and not recreated from the "ziggurat_Experimental" id
        flagship.setVariant(flagship.getVariant().clone(), false, false);
        flagship.getVariant().setSource(VariantSource.REFIT);
        //flagship.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

        float size = Global.getSettings().getBattleSize()*2f;
        if(size<=500f){
            size = 500f;
        }
        {
            String variant = "HSI_UkiyoC_Elite";
            {
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createHigherCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                vf.addPermaMod(HullMods.TURRETGYROS,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant = "HSI_UkiyoA_Elite";
            {
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createHigherCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                vf.addPermaMod(HullMods.TURRETGYROS,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant = "HSI_ShanYu_Assault";
            for(int i=0;i<Math.ceil(size*0.125/35f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createHigherCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                vf.addPermaMod(HullMods.TURRETGYROS,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_Oath_GF3_Defense";
            for(int i=0;i<Math.ceil(size*0.05/55f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createHigherCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                vf.addPermaMod(HullMods.TURRETGYROS,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_FengYue_GF3_Support";
            for(int i=0;i<Math.ceil(size*0.05/65f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createHigherCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                vf.addPermaMod(HullMods.EXPANDED_DECK_CREW,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_Eagle_GF3_Support";
            for(int i=0;i<Math.ceil(size*0.05/30f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createLowerCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_Xianfeng_GF3_Strike";
            for(int i=0;i<Math.ceil(size*0.025/26f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createLowerCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_Elegy_Support";
            for(int i=0;i<Math.ceil(size*0.02/22f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createLowerCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_Weaver_GF3_Strike";
            for(int i=0;i<Math.ceil(size*0.04/13f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createLowerCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_Sunder_Assault";
            for(int i=0;i<Math.ceil(size*0.075/15f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createLowerCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
            variant ="HSI_YunYan_GF3_Assault";
            for(int i=0;i<Math.ceil(size*0.075/7f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createJuniorCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }

            variant = "HSI_TianGuang_Dash";
            for(int i=0;i<Math.ceil(size*0.05/15f);i++){
                FleetMemberAPI f = fleet.getFleetData().addFleetMember(variant);
                f.setCaptain(createLowerCaptain());
                ShipVariantAPI vf = f.getVariant().clone();
                vf.setSource(VariantSource.REFIT);
                vf.addPermaMod(HullMods.AUTOREPAIR,true);
                f.setVariant(vf,true,true);
                f.updateStats();
            }
        }


        Vector2f loc = new Vector2f(rock.getLocation().x + 300 * ((float) Math.random() - 0.5f),
                rock.getLocation().y + 300 * ((float) Math.random() - 0.5f));
        fleet.setLocation(loc.x, loc.y);
        rock.getContainingLocation().addEntity(fleet);

        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new HSIDreadnoughtFIDConfig());

        fleet.addAssignment(FleetAssignment.DEFEND_LOCATION,rock,1000000f);

    }


    public static PersonAPI createDreadnoughtCaptain() {
        PersonAPI person = Global.getFactory().createPerson();
        person.setGender(FullName.Gender.ANY);
        person.getName().setFirst("??");
        person.getName().setLast("'MP'");
        person.setPersonality(Personalities.AGGRESSIVE);
        person.setId("HSI_??");
        person.setPortraitSprite("graphics/portraits/portraits_HSI_04.png");
        person.getStats().setSkipRefresh(true);

        person.getStats().setLevel(1);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        //person.getStats().setSkillLevel(Skills.RELIABILITY_ENGINEERING, 2);
        //person.getStats().setSkillLevel(Skills.RANGED_SPECIALIZATION, 2);
        person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
        person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        //person.getStats().setSkillLevel(Skills.PHASE_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);

        person.getStats().setSkillLevel(Skills.NAVIGATION, 1);
        person.getStats().setSkillLevel(Skills.CREW_TRAINING,1);
        person.getStats().setSkillLevel(Skills.FLUX_REGULATION,1);
        person.getStats().setSkillLevel(Skills.CYBERNETIC_AUGMENTATION,1);

        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE,2);
        person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY,2);
        person.getStats().setSkillLevel(Skills.POINT_DEFENSE,2);


        person.getStats().setSkipRefresh(false);

        return person;
    }

    public static PersonAPI createHigherCaptain() {
        PersonAPI person = Global.getSector().getFaction("HSI").createRandomPerson();
        //person.setId(Misc.genUID());
        person.setPersonality(Personalities.AGGRESSIVE);
        person.getStats().setSkipRefresh(true);

        person.getStats().setLevel(10);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE,2);
        person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY,2);
        person.getStats().setSkillLevel(Skills.POINT_DEFENSE,2);


        person.getStats().setSkipRefresh(false);

        return person;
    }

    public static PersonAPI createLowerCaptain() {
        PersonAPI person = Global.getSector().getFaction("HSI").createRandomPerson();
        person.setPersonality(Personalities.AGGRESSIVE);
        person.getStats().setSkipRefresh(true);
        //person.setId(Misc.genUID());

        person.getStats().setLevel(5);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
        person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE,1);


        person.getStats().setSkipRefresh(false);

        return person;
    }

    public static PersonAPI createJuniorCaptain() {
        PersonAPI person = Global.getSector().getFaction("HSI").createRandomPerson();
        person.setPersonality(Personalities.AGGRESSIVE);
        person.getStats().setSkipRefresh(true);
        //person.setId(Misc.genUID());

        person.getStats().setLevel(3);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
        person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE,1);


        person.getStats().setSkipRefresh(false);

        return person;
    }

    public static SectorEntityToken addDerelict(StarSystemAPI system, SectorEntityToken focus,
                                                String variantId, String name, String id,
                                                ShipRecoverySpecial.ShipCondition condition, float orbitRadius, boolean recoverable) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition, 0f), false);
        if (name != null) {
            params.ship.shipName = name;
            params.ship.nameAlwaysKnown = true;
            params.ship.fleetMemberId = id;
        }
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
        ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        return ship;
    }

    public static class HSIDreadnoughtOverNumArtilleryPlugin extends BaseEveryFrameCombatPlugin{

        private IntervalUtil interval = new IntervalUtil(7f,12f);
        private Random r = new Random();

        private ShipAPI dreadnought;

        private static final DamagingExplosionSpec spec = createExplosionSpec();

        public HSIDreadnoughtOverNumArtilleryPlugin(ShipAPI dreadnought){
            this.dreadnought = dreadnought;
        }
        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            final String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_cr_penalty");
            Global.getCombatEngine().maintainStatusForPlayerShip(this,icon,HSII18nUtil.getCampaignString("HSIDreadnoughtArtilleryWarning"),HSII18nUtil.getCampaignString("HSIDreadnoughtArtilleryWarningCountdown")+String.format("%.1f",interval.getIntervalDuration()-interval.getElapsed())+"s",true);
            if(Global.getCombatEngine().isPaused()) return;
            interval.advance(amount);
            if(interval.intervalElapsed()){
                for(ShipAPI ship: Global.getCombatEngine().getShips()){
                    if(ship.getOwner()!=0) continue;
                    int count = 0;
                    while (r.nextFloat()<=(0.33f-0.06f*count)){
                        Global.getCombatEngine().spawnDamagingExplosion(spec, dreadnought, Misc.getPointWithinRadiusUniform(ship.getLocation(),ship.getCollisionRadius(),ship.getCollisionRadius()*1.5f,r),false);
                        count++;
                    }
                }
            }
        }

        public static DamagingExplosionSpec createExplosionSpec() {
            float level = (float) Math.random();
            float damage = 400f+400f*level;
            DamagingExplosionSpec spec = new DamagingExplosionSpec(
                    0.12f, // duration
                    300f+300f*level, // radius
                    150f+150f*level, // coreRadius
                    damage, // maxDamage
                    damage / 2f, // minDamage
                    CollisionClass.PROJECTILE_NO_FF, // collisionClass
                    CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                    3f, // particleSizeMin
                    3f, // particleSizeRange
                    0.5f, // particleDuration
                    150, // particleCount
                    new Color(255, 255, 255, 255), // particleColor
                    new Color(100, 100, 255, 175) // explosionColor
            );

            spec.setDamageType(DamageType.ENERGY);
            spec.setUseDetailedExplosion(false);
            spec.setSoundSetId("explosion_guardian");
            return spec;
        }

    }
}
