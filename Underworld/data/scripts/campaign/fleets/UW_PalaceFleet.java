package data.scripts.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDDelegate;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.UnderworldModPlugin;
import data.scripts.util.UW_Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

public class UW_PalaceFleet implements EveryFrameScript, FleetEventListener {

    private static final Logger LOG = Global.getLogger(UW_PalaceFleet.class);
    private static final boolean DEBUG = false;

    static {
        LOG.setLevel(Level.INFO);
    }

    private static final float MIN_DAYS_BEFORE_RETURN = 90f;
    private static final float MAX_DAYS_BEFORE_RETURN = 120f;

    private static final float MIN_RESPAWN_DELAY_DAYS = 90f;
    private static final float MAX_RESPAWN_DELAY_DAYS = 150f;

    private static final float MIN_DEFEAT_RESPAWN_DELAY_DAYS = 270f;
    private static final float MAX_DEFEAT_RESPAWN_DELAY_DAYS = 450f;

    private static final float MIN_FAILED_RESPAWN_DELAY_DAYS = 10f;
    private static final float MAX_FAILED_RESPAWN_DELAY_DAYS = 20f;

    protected float currDelay = MAX_DAYS_BEFORE_RETURN;
    protected CampaignFleetAPI fleet;
    protected Random random = new Random();

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if ((amount <= 0f) || !UnderworldModPlugin.isStarlightCabalEnabled()) {
            return;
        }

        if ((fleet != null) && !fleet.isAlive()) {
            fleet = null;
        }

        if (fleet == null) {
            float days = Global.getSector().getClock().convertToDays(amount);
            currDelay -= days;
            if (currDelay <= 0f) {
                currDelay = 0f;

                if (canSpawnFleetNow()) {
                    fleet = spawnFleet();
                    if (fleet != null) {
                        fleet.addEventListener(this);
                    }
                }
                if (fleet == null) {
                    if (DEBUG) {
                        LOG.warn("Failed to spawn Palace!");
                    }
                    currDelay = MIN_FAILED_RESPAWN_DELAY_DAYS
                            + (MAX_FAILED_RESPAWN_DELAY_DAYS - MIN_FAILED_RESPAWN_DELAY_DAYS) * random.nextFloat();
                }
            }
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (fleet == this.fleet) {
            this.fleet = null;
            currDelay = MIN_RESPAWN_DELAY_DAYS
                    + (MAX_RESPAWN_DELAY_DAYS - MIN_RESPAWN_DELAY_DAYS) * random.nextFloat();
            if (DEBUG) {
                LOG.info("Palace despawned!");
            }
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (fleet == this.fleet) {
            boolean palaceAlive = false;
            if ((fleet.getFlagship() != null) && fleet.getFlagship().getHullId().contains("uw_palace")) {
                palaceAlive = true;
            }

            fleet.getMemoryWithoutUpdate().set("$stillAlive", palaceAlive);

            if (!battle.isPlayerInvolved()) {
                return;
            }

            CampaignFleetAPI player = Global.getSector().getPlayerFleet();
            if (battle.getNonPlayerSideSnapshot().contains(fleet) && (primaryWinner != null) && (battle.isOnPlayerSide(primaryWinner) || (primaryWinner == player))) {
                if (!palaceAlive) {
                    this.fleet = null;
                    Global.getSector().getMemoryWithoutUpdate().set("$uwKilledPalace", true);
                    currDelay = MIN_DEFEAT_RESPAWN_DELAY_DAYS
                            + (MAX_DEFEAT_RESPAWN_DELAY_DAYS - MIN_DEFEAT_RESPAWN_DELAY_DAYS) * random.nextFloat();
                }
            }
        }
    }

    public CampaignFleetAPI getFleet() {
        return fleet;
    }

    public CampaignFleetAPI spawnFleet() {
        MarketAPI market = getRandomCabal(random);
        if (market == null) {
            return null;
        }
        if (market.getStarSystem() == null) {
            return null;
        }

        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();

        Vector2f loc = market.getLocationInHyperspace();

        boolean killedPalaceBefore = Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwKilledPalace");

        // Find Palace variants
        WeightedRandomPicker<ShipVariantAPI> variantPicker = new WeightedRandomPicker<>(random);
        if (killedPalaceBefore) {
            for (String variantId : Global.getSettings().getAllVariantIds()) {
                ShipVariantAPI variant;
                try {
                    variant = Global.getSettings().getVariant(variantId);
                } catch (Exception E) {
                    LOG.error("Invalid variant ID: " + variantId);
                    continue;
                }

                if (!variant.isGoalVariant()) {
                    continue;
                }
                if (UW_Util.getNonDHullId(variant.getHullSpec()).contentEquals("uw_palace")) {
                    variantPicker.add(variant);
                }
            }
        } else {
            ShipVariantAPI variant = null;
            try {
                variant = Global.getSettings().getVariant("uw_palace_gra");
            } catch (Exception E) {
                LOG.error("Invalid variant ID: " + "uw_palace_gra");
            }
            if (variant != null) {
                variantPicker.add(variant);
            }
        }
        if (variantPicker.isEmpty()) {
            return null;
        }

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        PersonAPI zeb = ip.getPerson("uw_zeb");
        float scaler = UW_CabalFleetManager.cabalScaler(random, true) * 0.5f;
        if (killedPalaceBefore) {
            scaler *= 2f;
        }

        m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_3, "cabal", FleetTypes.PATROL_LARGE, loc);
        m.triggerSetFleetOfficers(OfficerNum.DEFAULT, OfficerQuality.HIGHER);
        m.triggerSetFleetFaction("cabal");
        m.triggerSetFleetSizeFraction(scaler);
        m.triggerFleetSetNoFactionInName();
        m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, market);
        if (killedPalaceBefore) {
            m.triggerFleetSetName("Starlight Gala Cavalcade");
        } else {
            m.triggerFleetSetName("Starlight Gala Parade");
        }
        if ((zeb != null) && !ContactIntel.playerHasContact(zeb, true)) {
            m.triggerFleetSetCommander(zeb);
        }
        m.triggerOrderFleetPatrol(market.getStarSystem());

        CampaignFleetAPI newFleet = m.createFleet();
        newFleet.getMemoryWithoutUpdate().set("$nex_noKeepSMods", true);
        newFleet.getMemoryWithoutUpdate().set("$uwIsPalace", true);
        newFleet.getMemoryWithoutUpdate().set("$stillAlive", true);
        newFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
        newFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        newFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);
        if (!killedPalaceBefore) {
            newFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE, true);
            newFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
        }
        newFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY, true);
        newFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new PalaceInteractionConfigGen());
        newFleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
        market.getContainingLocation().addEntity(newFleet);
        newFleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
        newFleet.setFacing((float) random.nextFloat() * 360f);

        FleetMemberAPI oldFlagship = newFleet.getFlagship();
        FleetMemberAPI palace = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantPicker.pick());
        newFleet.getFleetData().addFleetMember(palace);
        if (!killedPalaceBefore) {
            palace.setShipName("Transcendance");
        }

        palace.setCaptain(oldFlagship.getCaptain());
        oldFlagship.setFlagship(false);
        newFleet.getFleetData().setFlagship(palace);
        newFleet.getFleetData().removeFleetMember(oldFlagship);
        if (newFleet.getCommander() != zeb) {
            newFleet.getCommander().setRankId(Ranks.SPACE_ADMIRAL);
        }

        newFleet.getFleetData().sort();
        newFleet.updateCounts();
        newFleet.forceSync();

        newFleet.removeAbility(Abilities.TRANSPONDER);
        newFleet.removeAbility(Abilities.SUSTAINED_BURN);
        newFleet.removeAbility(Abilities.SENSOR_BURST);
        newFleet.removeAbility(Abilities.GO_DARK);
        if (!killedPalaceBefore) {
            newFleet.removeAbility(Abilities.EMERGENCY_BURN);
        }
        newFleet.setTransponderOn(true);

        if (DEBUG) {
            LOG.info("Spawned Palace at " + market.getName());
        }

        UW_PalaceAssignmentAI ai = new UW_PalaceAssignmentAI(newFleet, market, random);
        newFleet.addScript(ai);

        return newFleet;
    }

    public boolean canSpawnFleetNow() {
        if (!UnderworldModPlugin.isStarlightCabalEnabled()) {
            return false;
        }
        return getRandomCabal(random) != null;
    }

    public static MarketAPI getRandomCabal(Random random) {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>(random);

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isHidden()) {
                continue;
            }
            if (market.hasCondition(Conditions.DECIVILIZED)) {
                continue;
            }
            if (market.getStarSystem() == null) {
                continue;
            }
            if (!market.getStarSystem().hasTag(Tags.THEME_CORE) && !market.getStarSystem().hasTag(Tags.THEME_CORE_POPULATED)
                    && !market.getStarSystem().hasTag(Tags.THEME_CORE_UNPOPULATED)) {
                continue;
            }
            if (market.getPrimaryEntity() == null) {
                continue;
            }

            float weight;
            if (!market.hasCondition("cabal_influence")) {
                switch (market.getFactionId()) {
                    case Factions.TRITACHYON, "cabal" ->
                        weight = 1f;
                    default -> {
                        continue;
                    }
                }
            } else {
                weight = 2f;
            }

            picker.add(market, weight);
        }

        return picker.pick();
    }

    public static class PalaceInteractionConfigGen implements FIDConfigGen {

        @Override
        public FIDConfig createConfig() {
            FIDConfig config = new FIDConfig();

            config.delegate = new FIDDelegate() {
                @Override
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                }

                @Override
                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                @Override
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                    if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) {
                        return;
                    }

                    CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();

                    DataForEncounterSide data = context.getDataFor(fleet);
                    List<FleetMemberAPI> losses = new ArrayList<>();
                    for (FleetMemberData fmd : data.getOwnCasualties()) {
                        losses.add(fmd.getMember());
                    }

                    List<DropData> dropRandom = new ArrayList<>();

                    int[] counts = new int[6];
                    String[] groups = new String[]{Drops.REM_WEAPONS2, Drops.AI_CORES3, "uw_cabalgoods", "any_hullmod_high", "blueprints_low", "rare_tech_low"};

                    for (FleetMemberAPI member : losses) {
                        if (UW_Util.getNonDHullId(member.getHullSpec()).contains("uw_palace")) {
                            counts[5] = 4;
                            counts[4] = 20;
                            counts[3] = 10;
                            counts[2] = 150000;
                            counts[1] = 10;
                            counts[0] = 40;
                        }
                    }

                    for (int i = 0; i < counts.length; i++) {
                        int count = counts[i];
                        if (count <= 0) {
                            continue;
                        }

                        DropData d = new DropData();
                        d.group = groups[i];
                        if (count >= 25000) {
                            d.chances = (int) Math.ceil(count / 25000f);
                            d.value = 25000;
                        } else {
                            d.chances = (int) Math.ceil(count * 1f);
                        }
                        dropRandom.add(d);
                    }

                    if (!dropRandom.isEmpty()) {
                        Random salvageRandom = new Random(Misc.getSalvageSeed(fleet));
                        CargoAPI extra = SalvageEntity.generateSalvage(salvageRandom, 1f, 1f, 1f, 1f, null, dropRandom);
                        for (CargoStackAPI stack : extra.getStacksCopy()) {
                            salvage.addFromStack(stack);
                        }
                    }
                }
            };

            return config;
        }
    }

    public class UW_PalaceAssignmentAI implements EveryFrameScript {

        private float daysTotal = 0f;
        private final float daysBeforeReturn;
        private final CampaignFleetAPI fleet;
        private final MarketAPI initialMarket;
        private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.3f);
        private boolean orderedReturn = false;
        private final Object loop1 = new Object();
        private final Object loop2 = new Object();
        private final Random random;

        public UW_PalaceAssignmentAI(CampaignFleetAPI fleet, MarketAPI initialMarket, Random random) {
            this.fleet = fleet;
            this.initialMarket = initialMarket;
            this.random = new Random(random.nextLong());
            daysBeforeReturn = MIN_DAYS_BEFORE_RETURN
                    + (MAX_DAYS_BEFORE_RETURN - MIN_DAYS_BEFORE_RETURN) * this.random.nextFloat();
            giveInitialAssignment();
        }

        @Override
        public void advance(float amount) {
            float days = Global.getSector().getClock().convertToDays(amount);
            if (Global.getSector().isPaused()) {
                days = 0f;
            }
            daysTotal += days;

            if (tracker.intervalElapsed()) {
                UW_CabalFleetManager.ScrewWithPlayer.doFactionChange(fleet, true);
            }

            if (!fleet.getMemoryWithoutUpdate().getBoolean("$stillAlive")) {
                giveReturnToSourceOrders();
                return;
            } else {
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                if ((playerFleet != null) && (fleet.getContainingLocation()) == (playerFleet.getContainingLocation())) {
                    float dist = Misc.getDistance(fleet, playerFleet);
                    float falloff2 = playerFleet.getMaxSensorRangeToDetect(fleet) + fleet.getRadius() + playerFleet.getRadius();
                    falloff2 = Math.min(1500f, falloff2);
                    float falloff1 = 1500f + (1.5f * falloff2);
                    if (!fleet.getContainingLocation().isHyperspace()) {
                        falloff1 += 3000f;
                    }
                    float vol1 = 1f - (dist / falloff1);
                    float vol2 = 1f - (dist / falloff2);
                    Vector2f loc = Global.getSoundPlayer().getListenerPos();
                    if (loc == null) {
                        loc = playerFleet.getLocation();
                    }
                    if (Global.getSector().getCampaignUI().isShowingDialog() || Global.getSector().getCampaignUI().isShowingMenu()
                            || (Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null)) {
                        vol1 *= 0.01f;
                        vol2 *= 0.01f;
                    }
                    if (vol1 > 0f) {
                        vol2 = Math.max(vol2, 0.01f);
                        Global.getSoundPlayer().playLoop("uw_palace_wubwub", loop1, 1f, vol1, loc, Misc.ZERO);
                        Global.getSoundPlayer().playLoop("uw_palace_bitcruncher", loop2, 1f, vol2, loc, Misc.ZERO);
                    }
                }
            }

            if (daysTotal > daysBeforeReturn) {
                giveReturnToSourceOrders();
            }
        }

        @Override
        public boolean isDone() {
            return !fleet.isAlive();
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }

        private void giveInitialAssignment() {
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, initialMarket.getPrimaryEntity(), 3, "loading up at " + initialMarket.getName());

            int picks = 0;
            Set<MarketAPI> pickedMarkets = new HashSet<>();
            Set<StarSystemAPI> pickedSystems = new HashSet<>();
            pickedMarkets.add(initialMarket);
            pickedSystems.add(initialMarket.getStarSystem());
            while (picks < 10) {
                MarketAPI market = getRandomMarketOfInterest(pickedMarkets);
                if (market == null) {
                    break;
                }

                pickedMarkets.add(market);
                if (random.nextInt(3) == 0) {
                    fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, market.getPrimaryEntity(), 1000, "traveling to " + market.getName());
                    fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, market.getPrimaryEntity(), random.nextInt(10) + 5, "hanging out at " + market.getName());
                    if (DEBUG) {
                        LOG.info("Palace will stop at " + market.getName());
                    }
                    picks++;
                } else {
                    if ((market.getStarSystem() != null) && !market.getStarSystem().isHyperspace() && (market.getStarSystem().getLocation() != null)) {
                        if (!pickedSystems.contains(market.getStarSystem())) {
                            pickedSystems.add(market.getStarSystem());
                            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, market.getStarSystem().getHyperspaceAnchor(), 1000, "traveling toward " + market.getStarSystem().getName());
                            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, market.getStarSystem().getHyperspaceAnchor(), random.nextInt(10) + 5, "hanging out around " + market.getStarSystem().getName());
                            if (DEBUG) {
                                LOG.info("Palace will stop outside " + market.getStarSystem().getName());
                            }
                            picks++;
                        }
                    }
                }
            }
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, initialMarket.getPrimaryEntity(), 1000,
                    "returning to " + initialMarket.getName());
        }

        private void giveReturnToSourceOrders() {
            if (!orderedReturn) {
                orderedReturn = true;

                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, initialMarket.getPrimaryEntity(), 1000,
                        "returning to " + initialMarket.getName());
            }
        }

        public MarketAPI getRandomMarketOfInterest(Set<MarketAPI> excluding) {
            WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>(random);

            for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                if (market.isHidden()) {
                    continue;
                }
                if (market.hasCondition(Conditions.DECIVILIZED)) {
                    continue;
                }
                if (market.getStarSystem() == null) {
                    continue;
                }
                if (!market.getStarSystem().hasTag(Tags.THEME_CORE) && !market.getStarSystem().hasTag(Tags.THEME_CORE_POPULATED)
                        && !market.getStarSystem().hasTag(Tags.THEME_CORE_UNPOPULATED)) {
                    continue;
                }
                if (excluding.contains(market)) {
                    continue;
                }

                float weight;
                if (!market.hasCondition("cabal_influence")) {
                    weight = switch (market.getFactionId()) {
                        case Factions.TRITACHYON, "cabal" ->
                            2f;
                        default ->
                            1f;
                    };
                } else {
                    weight = 3f;
                }
                weight *= market.getSize();
                weight *= 15f - market.getStabilityValue();
                if (market.getFaction().isHostileTo("cabal") && market.getFaction().isHostileTo(Factions.TRITACHYON)) {
                    weight *= 0.01f;
                } else if (market.getFaction().isHostileTo("cabal") || market.getFaction().isHostileTo(Factions.TRITACHYON)) {
                    weight *= 0.1f;
                }

                picker.add(market, weight);
            }

            return picker.pick();
        }
    }
}
