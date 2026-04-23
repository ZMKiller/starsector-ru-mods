package data.scripts.world.underworld;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDDelegate;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class UW_DickersonFleetManager extends BaseCampaignEventListener implements EveryFrameScript {

    private static final WeightedRandomPicker<String> DICKERSON_VARIANTS = new WeightedRandomPicker<>();

    static {
        DICKERSON_VARIANTS.add("uw_shadowclaw_pro", 10f);
        DICKERSON_VARIANTS.add("uw_shadowclaw_ber", 7.5f);
        DICKERSON_VARIANTS.add("uw_shadowclaw_sni", 5f);
    }

    private DickersonFleetData activeDickerson = null;
    private int battlesLost;
    private boolean dickersonHostile; // separate var in case we fight the level 0 fleet but fail to kill it, and it despawns later
    private final MarketAPI market;
    private float respawnTimer;
    private final IntervalUtil tracker;

    public UW_DickersonFleetManager(MarketAPI market) {
        super(true);
        this.market = market;

        tracker = new IntervalUtil(0.75f, 1.25f);
        battlesLost = 0;
        respawnTimer = 0f;
        dickersonHostile = false;
    }

    @Override
    public void advance(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);

        Integer level = (Integer) Global.getSector().getPersistentData().get("uw_dickerson_level");
        if (level == null) {
            Global.getSector().getPersistentData().put("uw_dickerson_level", battlesLost);
        } else {
            battlesLost = level;
        }

        tracker.advance(days);
        respawnTimer -= days;
        if (!tracker.intervalElapsed()) {
            return;
        }

        if (market.hasCondition(Conditions.DECIVILIZED)) {
            return;
        }

        if (activeDickerson != null) {
            if (activeDickerson.fleet.getContainingLocation() == null
                    || !activeDickerson.fleet.getContainingLocation().getFleets().contains(activeDickerson.fleet)
                    || !activeDickerson.fleet.isAlive()) {
                activeDickerson = null;
            }
        }

        if (market.getFactionId().contentEquals(Factions.PIRATES)) {
            if (activeDickerson == null && respawnTimer <= 0f) {
                int pts = 30 + battlesLost * 25 + Math.min(battlesLost, 6) * Math.min(battlesLost, 6) * 20;
                pts *= 5;

                FactionDoctrineAPI doct = market.getFaction().getDoctrine().clone();
                int officerBonus = (int) (Math.min(battlesLost, 6));
                int doctSize = 2;       // pirate default is 2
                int doctOfficer = 2 + (officerBonus / 2);    // pirate default is 1
                if (battlesLost >= 4) {
                    doctSize = 4;
                } else if (battlesLost >= 2) {
                    doctSize = 3;
                }
                doct.setShipSize(Math.max(doct.getShipSize(), doctSize));
                doct.setOfficerQuality(Math.max(doct.getOfficerQuality(), doctOfficer));

                FleetParamsV3 params = new FleetParamsV3(
                        market,
                        FleetTypes.PATROL_LARGE,
                        pts, // combatPts
                        0f, // freighterPts
                        0f, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        4f, // utilityPts
                        1f + Math.min(battlesLost, 6) // qualityBonus
                );

                params.ignoreMarketFleetSizeMult = true;
                params.officerLevelBonus = officerBonus / 2;
                params.officerNumberBonus = officerBonus;
                params.forceAllowPhaseShipsEtc = true;
                params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
                params.doNotPrune = true;
                params.doctrineOverride = doct;
                params.officerLevelLimit = 6 + (officerBonus / 2);
                params.commanderLevelLimit = 7 + (officerBonus / 2);
                params.averageSMods = (officerBonus * 2) / 3;
                params.noCommanderSkills = true;

                CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
                if ((fleet == null) || fleet.isEmpty()) {
                    return;
                }

                FleetMemberAPI oldFlagship = fleet.getFlagship();
                FleetMemberAPI shadowclaw = Global.getFactory().createFleetMember(FleetMemberType.SHIP, DICKERSON_VARIANTS.pick());
                fleet.getFleetData().addFleetMember(shadowclaw);

                oldFlagship.setFlagship(false);
                oldFlagship.setCaptain(shadowclaw.getCaptain());
                fleet.getFleetData().setFlagship(shadowclaw);

                fleet.getCommanderStats().setSkipRefresh(true);
                switch (battlesLost) {
                    case 0 -> {
                        /* 30 */
                        fleet.setName("Dickerson's Fleet");
                        fleet.getCommanderStats().setSkillLevel("coordinated_maneuvers", 1);
                    }
                    case 1 -> {
                        /* 75 */
                        fleet.setName("Dickerson's Renovated Flotilla");
                        fleet.getCommanderStats().setSkillLevel("coordinated_maneuvers", 1);
                        fleet.getCommanderStats().setSkillLevel("crew_training", 1);
                    }
                    case 2 -> {
                        /* 160 */
                        fleet.setName("Dickerson's Pirate Horde");
                        fleet.getCommanderStats().setSkillLevel("coordinated_maneuvers", 1);
                        fleet.getCommanderStats().setSkillLevel("crew_training", 1);
                        fleet.getCommanderStats().setSkillLevel("officer_training", 1);
                    }
                    case 3 -> {
                        /* 285 */
                        fleet.setName("Dickerson's Undying Legion");
                        fleet.getCommanderStats().setSkillLevel("coordinated_maneuvers", 1);
                        fleet.getCommanderStats().setSkillLevel("crew_training", 1);
                        fleet.getCommanderStats().setSkillLevel("officer_training", 1);
                        fleet.getCommanderStats().setSkillLevel("electronic_warfare", 1);
                    }
                    case 4 -> {
                        /* 450 */
                        fleet.setName("Dickerson's Divine Armada");
                        fleet.getCommanderStats().setSkillLevel("coordinated_maneuvers", 1);
                        fleet.getCommanderStats().setSkillLevel("crew_training", 1);
                        fleet.getCommanderStats().setSkillLevel("officer_training", 1);
                        fleet.getCommanderStats().setSkillLevel("electronic_warfare", 1);
                        fleet.getCommanderStats().setSkillLevel("fighter_uplink", 1);
                    }
                    case 5 -> {
                        /* 655 */
                        fleet.setName("The Sons of Dicker");
                        fleet.getCommanderStats().setSkillLevel("coordinated_maneuvers", 1);
                        fleet.getCommanderStats().setSkillLevel("crew_training", 1);
                        fleet.getCommanderStats().setSkillLevel("officer_training", 1);
                        fleet.getCommanderStats().setSkillLevel("electronic_warfare", 1);
                        fleet.getCommanderStats().setSkillLevel("fighter_uplink", 1);
                        fleet.getCommanderStats().setSkillLevel("flux_regulation", 1);
                    }
                    default -> {
                        /* 900 */
                        if (battlesLost == 6) {
                            fleet.setName("The Infinite Sons of Dicker");
                        } else {
                            fleet.setName("The Infinite Sons of Dicker " + Global.getSettings().getRoman(battlesLost - 5));
                        }
                        fleet.getCommanderStats().setSkillLevel("coordinated_maneuvers", 1);
                        fleet.getCommanderStats().setSkillLevel("crew_training", 1);
                        fleet.getCommanderStats().setSkillLevel("officer_training", 1);
                        fleet.getCommanderStats().setSkillLevel("electronic_warfare", 1);
                        fleet.getCommanderStats().setSkillLevel("fighter_uplink", 1);
                        fleet.getCommanderStats().setSkillLevel("flux_regulation", 1);
                        fleet.getCommanderStats().setSkillLevel("carrier_group", 1);
                    }
                }
                fleet.getCommanderStats().setSkipRefresh(false);
                fleet.getCommanderStats().refreshCharacterStatsEffects();

                fleet.getMemoryWithoutUpdate().set("$uwIsDickerson", true);
                fleet.getMemoryWithoutUpdate().set("$stillAlive", true);
                fleet.getMemoryWithoutUpdate().set("$uwDickersonLevel", battlesLost);
                fleet.setNoFactionInName(true);

                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, "uw_dickerson");

                if (battlesLost > 0) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);
                }
                if (!dickersonHostile) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
                }
                if (battlesLost <= 1) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE, true);
                }
                if (battlesLost > 2) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
                }
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_ALLOW_TOFF, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
                fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new DickersonInteractionConfigGen());
                fleet.getMemoryWithoutUpdate().set("$nex_noKeepSMods", true);
                Misc.makeLowRepImpact(fleet, "uw_dickerson");

                PersonAPI commander = fleet.getCommander();
                commander.setPostId(Ranks.POST_GANGSTER);
                commander.setRankId(Ranks.SPACE_CAPTAIN);
                commander.setPortraitSprite("graphics/uw/portraits/uw_dickerson_extended_family.png");
                if (battlesLost < 1) {
                    commander.getName().setLast("Dickerson");
                } else {
                    commander.getName().setLast("Dickerson " + Global.getSettings().getRoman(battlesLost + 1));
                }
                shadowclaw.setCaptain(commander);

                fleet.getFleetData().sort();

                SectorEntityToken entity = market.getPrimaryEntity();
                entity.getContainingLocation().addEntity(fleet);
                fleet.setLocation(entity.getLocation().x, entity.getLocation().y);

                fleet.updateCounts();
                fleet.forceSync();

                if (battlesLost == 0) {
                    fleet.getFlagship().setVariant(fleet.getFlagship().getVariant().clone(), false, false);
                    fleet.getFlagship().getVariant().setSource(VariantSource.REFIT);
                    fleet.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
                }
                fleet.getFlagship().getRepairTracker().setCR(shadowclaw.getRepairTracker().getMaxCR());

                DickersonFleetData data = new DickersonFleetData(fleet);
                data.startingFleetPoints = fleet.getFleetPoints();
                data.sourceMarket = market;
                data.source = market.getPrimaryEntity();
                data.dickerson = commander;
                activeDickerson = data;

                UW_DickersonAssignmentAI ai = new UW_DickersonAssignmentAI(fleet, data);
                fleet.addScript(ai);
            }
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {
        super.reportBattleOccurred(primaryWinner, battle);

        if (activeDickerson == null) {
            return;
        }

        if (!battle.isInvolved(activeDickerson.fleet)) {
            return;
        }

        // check if flagship was destroyed by third party
        if (!battle.isPlayerInvolved() || battle.onPlayerSide(activeDickerson.fleet)) {
            if (activeDickerson.fleet.getFlagship() == null
                    || activeDickerson.fleet.getFlagship().getCaptain() != activeDickerson.dickerson) {
                activeDickerson.fleet.getMemoryWithoutUpdate().set("$stillAlive", false);
                return;
            }
        }

        if (!dickersonHostile) {
            activeDickerson.fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE);
            dickersonHostile = true;
        }

        // didn't destroy the original flagship
        if (activeDickerson.fleet.getFlagship() != null
                && activeDickerson.fleet.getFlagship().getCaptain() == activeDickerson.dickerson) {
            return;
        }

        battlesLost++;
        Global.getSector().getPersistentData().put("uw_dickerson_level", battlesLost);
        activeDickerson.fleet.getMemoryWithoutUpdate().set("$stillAlive", false);
        respawnTimer = 20f;
    }

    @Override
    public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        super.reportFleetDespawned(fleet, reason, param);

        if (activeDickerson == null) {
            return;
        }

        if (activeDickerson.fleet == fleet) {
            activeDickerson = null;
            respawnTimer += 20f;
        }
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    public static class DickersonFleetData {

        public PersonAPI dickerson;
        public CampaignFleetAPI fleet;
        public SectorEntityToken source;
        public MarketAPI sourceMarket;
        public float startingFleetPoints = 0;

        public DickersonFleetData(CampaignFleetAPI fleet) {
            this.fleet = fleet;
        }
    }

    public static class DickersonInteractionConfigGen implements FIDConfigGen {

        @Override
        public FIDConfig createConfig() {
            FIDConfig config = new FIDConfig();
            config.impactsEnemyReputation = false;

            config.delegate = new FIDDelegate() {
                @Override
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.enemyDeployAll = true;
                }

                @Override
                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                @Override
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context,
                        CargoAPI salvage) {
                }
            };

            return config;
        }
    }
}
