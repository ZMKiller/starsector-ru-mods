package data.scripts.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.CabalPickContributionMethod;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.UnderworldModPlugin;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;

public class UW_CabalFleetManager extends DisposableFleetManager {

    private static final Logger LOG = Global.getLogger(UW_CabalFleetManager.class);
    private static final boolean DEBUG2 = false;
    private static final float MAX_LY_FROM_CABAL = 15f;
    private static final float MAX_LY_FROM_PALACE = 5f;

    private Integer prevDesiredNumFleets;
    private IntervalUtil desiredNumFleetsTracker;

    static {
        LOG.setLevel(Level.INFO);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UW_CabalFleetManager() {
        float interval = 30f;
        tracker = new IntervalUtil(interval * 0.75f, interval * 1.25f);
        readResolve();
    }

    @Override
    protected Object readResolve() {
        super.readResolve();
        if (prevDesiredNumFleets == null) {
            prevDesiredNumFleets = 2;
        }
        if (desiredNumFleetsTracker == null) {
            desiredNumFleetsTracker = new IntervalUtil(0.75f, 1.25f);
        }
        return this;
    }

    @Override
    protected String getSpawnId() {
        return "cabal_spawn";
    }

    @Override
    protected int getMaxFleets() {
        if (!UnderworldModPlugin.isStarlightCabalEnabled()) {
            return 0;
        }

        /* Don't allow going past this cap, to avoid build-up of superfleets */
        return prevDesiredNumFleets + 1;
    }

    @Override
    public void advance(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        desiredNumFleetsTracker.advance(days);
        if (desiredNumFleetsTracker.intervalElapsed()) {
            prevDesiredNumFleets = getDesiredNumFleetsForSpawnLocation();
        }

        super.advance(amount);
    }

    @Override
    protected int getDesiredNumFleetsForSpawnLocation() {
        if (!UnderworldModPlugin.isStarlightCabalEnabled()) {
            prevDesiredNumFleets = 0;
            desiredNumFleetsTracker.forceIntervalElapsed();
            desiredNumFleetsTracker.advance(0f);
            return 0;
        }

        MarketAPI largestCoreMarketNearby = getLargestCoreMarket();
        MarketAPI closestCabalMarket = getClosestCabal();

        float desiredNumFleets;
        if (closestCabalMarket == null) {
            if (DEBUG2) {
                LOG.info("Cabal is destroyed.  No fleet spawns possible.");
            }
            prevDesiredNumFleets = 0;
            return 0;
        } else {
            float distScale = Math.max(0f, 1f - Misc.getDistanceToPlayerLY(closestCabalMarket.getLocationInHyperspace()) / MAX_LY_FROM_CABAL);
            desiredNumFleets = 1f * distScale;
        }
        if (DEBUG2) {
            LOG.info("Fleets from proximity to Cabal: " + desiredNumFleets);
        }

        float fromNearbyMarket = 0f;
        if (largestCoreMarketNearby != null) {
            fromNearbyMarket = Math.max(0f, (largestCoreMarketNearby.getSize() - 4f) * 0.5f);
            desiredNumFleets += fromNearbyMarket;
        }
        if (DEBUG2) {
            LOG.info("Fleets from large nearby market: " + fromNearbyMarket);
        }

        int level = getCabalLevel();
        desiredNumFleets += level * 0.5f;
        if (DEBUG2) {
            LOG.info("Fleets from Cabal affinity nearby: " + level * 0.5f);
        }
        float haLevel = getHALevel();
        desiredNumFleets += haLevel;
        if (DEBUG2) {
            LOG.info("Fleets from Hostile Activity: " + haLevel);
        }

        float palaceProx = 0f;
        for (EveryFrameScript script : Global.getSector().getScripts()) {
            if (script instanceof UW_PalaceFleet uW_PalaceFleet) {
                CampaignFleetAPI fleet = uW_PalaceFleet.getFleet();
                if ((fleet != null) && fleet.isAlive()) {
                    float distScale = Math.max(0f, 1f - Misc.getDistanceToPlayerLY(fleet.getLocationInHyperspace()) / MAX_LY_FROM_PALACE);
                    palaceProx = 2f * distScale;
                }
            }
        }
        desiredNumFleets += palaceProx;
        if (DEBUG2) {
            LOG.info("Fleets from proximity to a Palace: " + palaceProx);
        }

        int result = (int) Math.round(desiredNumFleets * UnderworldModPlugin.getCabalFleetFactor());
        if (DEBUG2) {
            LOG.info("Total desired num fleets: " + result);
        }
        prevDesiredNumFleets = result;
        return result;
    }

    protected float getHALevel() {
        if (currSpawnLoc == null) {
            return 0;
        }
        HostileActivityEventIntel intel = HostileActivityEventIntel.get();
        if (intel == null) {
            return 0;
        }

        Long timestamp = intel.getPlayerVisibleTimestamp();
        if (timestamp != null) {
            float daysSince = Global.getSector().getClock().getElapsedDaysSince(timestamp);
            if (daysSince < 30) {
                return 0;
            }
        }

        float mag = intel.getTotalActivityMagnitude(currSpawnLoc);
        float mag2 = intel.getProgressFraction();
        mag = Misc.interpolate(mag, mag2, 0.5f);

        if (mag <= 0f) {
            return 0;
        }

        int shinies = 0;
        List<MarketAPI> markets = Misc.getMarketsInLocation(currSpawnLoc, Factions.PLAYER);
        for (MarketAPI market : markets) {
            if (market.getSize() >= 5) {
                shinies++;
            }
            if (market.getSize() >= 6) {
                shinies++;
            }
            shinies += Math.round(LuddicPathBaseManager.getLuddicPathMarketInterest(market) / 4f);
            shinies += Math.round(market.getNetIncome() / 50000f);
        }
        mag *= (float) Math.sqrt(shinies / 4f);

        FactionAPI cabal = Global.getSector().getFaction("cabal");
        switch (cabal.getRelToPlayer().getLevel()) {
            default:
            case VENGEFUL:
            case HOSTILE:
                mag *= 1f;
                break;
            case INHOSPITABLE:
                mag *= 0.8f;
                break;
            case SUSPICIOUS:
                mag *= 0.6f;
                break;
            case NEUTRAL:
                mag *= 0.45f;
                break;
            case FAVORABLE:
                mag *= 0.3f;
                break;
            case WELCOMING:
                mag *= 0.2f;
                break;
            case FRIENDLY:
                mag *= 0.1f;
                break;
            case COOPERATIVE:
                mag *= 0f;
                break;
        }

        float max = Global.getSettings().getFloat("maxHostileActivityFleetsPerSystem");
        if (mag >= (max / 2f)) {
            mag = max / 2f;
        }

        return mag;
    }

    protected int getCabalLevel() {
        if (currSpawnLoc == null) {
            return 0;
        }
        int total = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(currSpawnLoc)) {
            if (market.isHidden()) {
                continue;
            }
            if (market.hasCondition(Conditions.DECIVILIZED)) {
                continue;
            }
            if (market.hasCondition("cabal_influence")) {
                total++;
            }
            switch (market.getFactionId()) {
                case Factions.TRITACHYON, "cabal" ->
                    total++;
                default -> {
                }
            }
        }
        return total;
    }

    protected MarketAPI getClosestCabal() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null) {
            return null;
        }

        MarketAPI closest = null;
        float minDistance = 1000000f;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isHidden()) {
                continue;
            }
            if (market.hasCondition(Conditions.DECIVILIZED)) {
                continue;
            }
            if (!market.hasCondition("cabal_influence")) {
                switch (market.getFactionId()) {
                    case Factions.TRITACHYON, "cabal" -> {
                    }
                    default -> {
                        continue;
                    }
                }
            }
            float distance = Misc.getDistanceToPlayerLY(market.getLocationInHyperspace());
            if (distance < minDistance) {
                closest = market;
                minDistance = distance;
            }
        }

        return closest;
    }

    protected MarketAPI getLargestCoreMarket() {
        if (currSpawnLoc == null) {
            return null;
        }
        if (!currSpawnLoc.hasTag(Tags.THEME_CORE) && !currSpawnLoc.hasTag(Tags.THEME_CORE_UNPOPULATED) && !currSpawnLoc.hasTag(Tags.THEME_CORE_POPULATED)) {
            return null;
        }

        MarketAPI largest = null;
        int maxSize = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(currSpawnLoc)) {
            if (market.isHidden()) {
                continue;
            }
            if (market.hasCondition(Conditions.DECIVILIZED)) {
                continue;
            }
            if (!market.hasCondition("cabal_influence")) {
                switch (market.getFactionId()) {
                    case Factions.TRITACHYON, "cabal" -> {
                    }
                    default -> {
                        continue;
                    }
                }
            }

            if (market.getSize() > maxSize) {
                maxSize = market.getSize();
                largest = market;
            }
        }

        return largest;
    }

    @Override
    protected float getExpireDaysPerFleet() {
        /* Bigger fleets, slower wind-down */
        return 20f;
    }

    public static float cabalScaler(Random rand, boolean palace) {
        float scale = 1f;
        float timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180f) / (365f * 3f);
        if (timeFactor < 0f) {
            timeFactor = 0f;
        }
        if (timeFactor > 1f) {
            timeFactor = (float) Math.sqrt(timeFactor);
        }
        if (timeFactor > 2f) {
            timeFactor = 2f;
        }
        if (palace) {
            scale *= 1f + (float) Math.sqrt(rand.nextFloat()) * timeFactor;
        } else {
            scale *= 1f + (rand.nextFloat() * timeFactor);
        }
        float levelFactor = 0f;
        if (Global.getSector().getPlayerPerson() != null) {
            levelFactor = Math.min(15f, Global.getSector().getPlayerPerson().getStats().getLevel()) / 50f;
        }
        if (palace) {
            scale *= 1f + (float) Math.sqrt(rand.nextFloat()) * levelFactor;
        } else {
            scale *= 1f + (rand.nextFloat() * levelFactor);
        }
        return scale;
    }

    @Override
    protected CampaignFleetAPI spawnFleetImpl() {
        if (!UnderworldModPlugin.isStarlightCabalEnabled()) {
            return null;
        }

        StarSystemAPI system = currSpawnLoc;
        if (system == null) {
            return null;
        }

        float combat = MathUtils.getRandomNumberInRange(20f, MathUtils.getRandomNumberInRange(40f, MathUtils.getRandomNumberInRange(80f, 160f)));
        combat *= cabalScaler(new Random(), false);

        String fleetType;
        if (combat < 50) {
            fleetType = FleetTypes.PATROL_SMALL;
        } else if (combat < 100) {
            fleetType = FleetTypes.PATROL_MEDIUM;
        } else {
            fleetType = FleetTypes.PATROL_LARGE;
        }

        FleetParamsV3 params = new FleetParamsV3(
                null, // market
                null, // location
                "cabal", // fleet's faction, if different from above, which is also used for source market picking
                2f,
                fleetType,
                combat, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0 // qualityBonus
        );

        FactionDoctrineAPI doctrine = Global.getSector().getFaction("cabal").getDoctrine().clone();

        CabalFleetType cabalFleetType;
        if ((combat < 80) && (Math.random() < 0.3)) {
            cabalFleetType = CabalFleetType.FRIGATES;
            doctrine.setShipSize(1);
            doctrine.setWarships(5);
            doctrine.setCarriers(0);
            doctrine.setPhaseShips(2);
        } else if ((combat >= 40) && (combat < 120) && (Math.random() < 0.3)) {
            cabalFleetType = CabalFleetType.DESTROYERS;
            doctrine.setShipSize(2);
            doctrine.setWarships(4);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(2);
        } else if ((combat >= 80) && (Math.random() < 0.3)) {
            cabalFleetType = CabalFleetType.CRUISERS;
            doctrine.setShipSize(4);
            doctrine.setWarships(4);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(2);
        } else if ((combat >= 120) && (Math.random() < 0.3)) {
            cabalFleetType = CabalFleetType.CAPITALS;
            doctrine.setShipSize(5);
            doctrine.setWarships(4);
            doctrine.setCarriers(1);
            doctrine.setPhaseShips(2);
        } else if ((combat >= 60) && (Math.random() < 0.4)) {
            cabalFleetType = CabalFleetType.CARRIERS;
            doctrine.setShipSize(4);
            doctrine.setWarships(2);
            doctrine.setCarriers(5);
            doctrine.setPhaseShips(0);
        } else if ((combat >= 60) && (combat <= 140) && (Math.random() < 0.4)) {
            cabalFleetType = CabalFleetType.PHASE;
            doctrine.setShipSize(3);
            doctrine.setWarships(2);
            doctrine.setCarriers(0);
            doctrine.setPhaseShips(5);
        } else {
            cabalFleetType = CabalFleetType.BALANCED;
            doctrine.setShipSize(3);
            doctrine.setWarships(3);
            doctrine.setCarriers(2);
            doctrine.setPhaseShips(2);
        }

        params.doctrineOverride = doctrine;
        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
        params.averageSMods = 2;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if ((fleet == null) || fleet.isEmpty()) {
            return null;
        }

        if (combat < 50) {
            fleet.getCommander().setRankId(Ranks.SPACE_COMMANDER);
        } else if (combat < 100) {
            fleet.getCommander().setRankId(Ranks.SPACE_CAPTAIN);
        } else {
            fleet.getCommander().setRankId(Ranks.SPACE_ADMIRAL);
        }

        switch (cabalFleetType) {
            case FRIGATES:
                fleet.setName("Wolfpack");
                break;
            case DESTROYERS:
                fleet.setName("Posse");
                break;
            case CRUISERS:
                fleet.setName("Team");
                break;
            case CAPITALS:
                fleet.setName("Group");
                break;
            case PHASE:
                fleet.setName("Spooks");
                break;
            case CARRIERS:
                fleet.setName("Carrier Swarm");
                break;
            default:
            case BALANCED:
                if (combat < 50) {
                    fleet.setName("Prowlers");
                } else if (combat < 100) {
                    fleet.setName("Coterie");
                } else {
                    fleet.setName("Clan Fleet");
                }
                break;
        }

        fleet.getMemoryWithoutUpdate().set("$nex_noKeepSMods", true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, "cabalFleet");
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);

        fleet.addScript(new ScrewWithPlayer(fleet));

        /* Avoid hyperspace build-up */
        int nf = getDesiredNumFleetsForSpawnLocation();
        float hyperProb = (float) Math.sqrt(0.5f / nf);
        setLocationAndOrders(fleet, hyperProb, hyperProb);

        return fleet;
    }

    public static class ScrewWithPlayer implements EveryFrameScript {

        private final CampaignFleetAPI fleet;
        private long signalExtortionPaid = 0L;
        private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.3f);

        ScrewWithPlayer(CampaignFleetAPI fleet) {
            this.fleet = fleet;
        }

        @Override
        public void advance(float amount) {
            long signalExtortionPaidGlobal = 0L;
            long timestamp = 0L;
            float timerGlobal = 0f;
            if (Global.getSector().getMemoryWithoutUpdate().contains("$uw_cabal_extortion_signal")) {
                signalExtortionPaidGlobal = Global.getSector().getMemoryWithoutUpdate().getLong("$uw_cabal_extortion_signal");
            }
            if (Global.getSector().getMemoryWithoutUpdate().contains("$uw_cabal_extortion_timer")) {
                timerGlobal = Global.getSector().getMemoryWithoutUpdate().getFloat("$uw_cabal_extortion_timer");
            }
            if (Global.getSector().getMemoryWithoutUpdate().contains("$uw_cabal_extortion_timestamp")) {
                timestamp = Global.getSector().getMemoryWithoutUpdate().getLong("$uw_cabal_extortion_timestamp");
            }
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

            float days = Global.getSector().getClock().convertToDays(amount);
            float scaler = 1f;
            if ((playerFleet == null) || (fleet.getContainingLocation() == playerFleet.getContainingLocation())) {
                scaler *= 0.1f;
            }
            tracker.advance(days * scaler);

            if ((timerGlobal > 0f) && (timestamp != Global.getSector().getClock().getTimestamp())) {
                timerGlobal -= Math.max(0f, Math.min(365f, Global.getSector().getClock().getElapsedDaysSince(timestamp)));
                Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
            }
            timestamp = Global.getSector().getClock().getTimestamp();
            Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timestamp", timestamp);

            MemoryAPI mem = fleet.getMemoryWithoutUpdate();
            if (mem.getBoolean("$Cabal_extortionAskedFor")) {
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);
            }

            float chaseDuration;
            float tryAgainTimer;
            boolean allowGlobalPay;
            switch (Global.getSector().getFaction("cabal").getRelToPlayer().getLevel()) {
                default:
                case VENGEFUL:
                    tryAgainTimer = 20f;
                    chaseDuration = 8f;
                    allowGlobalPay = false;
                    break;
                case HOSTILE:
                    tryAgainTimer = 25f;
                    chaseDuration = 7f;
                    allowGlobalPay = false;
                    break;
                case INHOSPITABLE:
                    tryAgainTimer = 30f;
                    chaseDuration = 6f;
                    allowGlobalPay = true;
                    break;
                case SUSPICIOUS:
                    tryAgainTimer = 35f;
                    chaseDuration = 5f;
                    allowGlobalPay = true;
                    break;
                case NEUTRAL:
                    tryAgainTimer = 45f;
                    chaseDuration = 4f;
                    allowGlobalPay = true;
                    break;
                case FAVORABLE:
                    tryAgainTimer = 50f;
                    chaseDuration = 3f;
                    allowGlobalPay = true;
                    break;
                case WELCOMING:
                    tryAgainTimer = 55f;
                    chaseDuration = 2f;
                    allowGlobalPay = true;
                    break;
                case FRIENDLY:
                    tryAgainTimer = 60f;
                    chaseDuration = 1f;
                    allowGlobalPay = true;
                    break;
                case COOPERATIVE:
                    return;
            }

            if (signalExtortionPaid < signalExtortionPaidGlobal) {
                if (mem.getBoolean("$Cabal_extortionPaid")) {
                    signalExtortionPaidGlobal = Global.getSector().getClock().getTimestamp();
                    timerGlobal = Math.max(tryAgainTimer, 30f);
                    Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_signal", signalExtortionPaidGlobal);
                    Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
                } else {
                    if (allowGlobalPay) {
                        mem.set("$Cabal_extortionPaid", true, tryAgainTimer);
                    }
                    signalExtortionPaid = signalExtortionPaidGlobal;
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);
                }
            }

            if (!fleet.getFaction().getId().contentEquals("cabal")) {
                Misc.clearFlag(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
            }

            if (tracker.intervalElapsed() && (timerGlobal <= 0f)) {
                doFactionChange(fleet, false);

                if (playerFleet == null) {
                    return;
                }
                if (playerFleet.getContainingLocation() != fleet.getContainingLocation()) {
                    return;
                }
                if (!CabalPickContributionMethod.playerHasAbilityToPayContribution(fleet)) {
                    return;
                }
                if ((fleet.getCurrentAssignment() != null) && (fleet.getCurrentAssignment().getAssignment() == FleetAssignment.GO_TO_LOCATION)) {
                    return;
                }
                if (!fleet.getFaction().getId().contentEquals("cabal")) {
                    return;
                }

                VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
                if (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
                    float chance = CabalPickContributionMethod.playerNetWorth() / 1000000f;
                    if (Math.random() < chance) {
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", true, chaseDuration);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", true, chaseDuration);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", true, chaseDuration);
                        mem.set(MemFlags.FLEET_BUSY, false);
                        timerGlobal = tryAgainTimer;
                        Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
                    } else {
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                        Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);
                        timerGlobal = 7f;
                        Global.getSector().getMemoryWithoutUpdate().set("$uw_cabal_extortion_timer", timerGlobal);
                    }
                }
            }
        }

        @Override
        public boolean isDone() {
            return !fleet.isAlive();
        }

        @Override
        public boolean runWhilePaused() {
            return false;
        }

        public static void doFactionChange(CampaignFleetAPI fleet, boolean isPalace) {
            boolean canSeePlayer = false;
            MemoryAPI mem = fleet.getMemoryWithoutUpdate();

            if ((fleet.getBattle() != null) && !fleet.getBattle().isDone()) {
                return;
            }

            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet != null) {
                if (CabalPickContributionMethod.playerHasAbilityToPayContribution(fleet)) {
                    if (playerFleet.getContainingLocation() == fleet.getContainingLocation()) {
                        VisibilityLevel level = playerFleet.getVisibilityLevelTo(fleet);
                        if ((level == VisibilityLevel.COMPOSITION_DETAILS) || (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS)) {
                            canSeePlayer = true;
                        }
                    }
                }
            }

            float fleetStrength = fleet.getEffectiveStrength();

            if (fleetStrength < 1f) {
                return;
            }

            float cabalWeakerTotal = getWeakerTotalForFaction(Global.getSector().getFaction("cabal"), fleet);
            float cabalStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction("cabal"), fleet);
            float cabalDecisionLevel = 0f;
            if (cabalWeakerTotal >= 1f) {
                cabalDecisionLevel += Math.min(1f, fleetStrength / cabalWeakerTotal);
            }
            cabalDecisionLevel -= (float) Math.sqrt(cabalStrongerTotal / fleetStrength);
            float worthLevel = CabalPickContributionMethod.playerNetWorth() / 1000000f;
            if (canSeePlayer) {
                cabalDecisionLevel += 1f * Math.min(1f, worthLevel);
            }
            if (mem.getBoolean(MemFlags.MEMORY_KEY_PURSUE_PLAYER)) {
                cabalDecisionLevel += 2f * Math.min(1f, worthLevel);
            }
            if (isPalace) {
                cabalDecisionLevel += 1f;
            }

            // Always Cabal in non-core
            StarSystemAPI nearest = pickNearestPopulatedSystem();
            if ((nearest == null) || nearest.hasTag(Tags.THEME_CORE) || nearest.hasTag(Tags.THEME_CORE_UNPOPULATED) || nearest.hasTag(Tags.THEME_CORE_POPULATED)) {
                cabalDecisionLevel = 10f;
            }

            if (cabalDecisionLevel >= 0.25f) {
                if (!fleet.getFaction().getId().contentEquals("cabal")) {
                    if (!isPalace) {
                        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
                        fleet.setNoFactionInName(false);
                    }
                    fleet.setFaction("cabal", true);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_NO_REP_IMPACT, "cabal_disguise", false, 0);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_LOW_REP_IMPACT, "cabal_disguise", false, 0);
                    if (!isPalace) {
                        Misc.clearFlag(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF);
                    }
                }
            } else {
                if (!isPalace) {
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "cabalScrewWithPlayer", false, 0f);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "cabalScrewWithPlayer", false, 0f);
                    Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, "cabalScrewWithPlayer", false, 0f);
                }

                float tritachyonStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction(Factions.TRITACHYON), fleet);
                float tritachyonDecisionLevel = 1f - (tritachyonStrongerTotal / fleetStrength);

                float independentStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction(Factions.INDEPENDENT), fleet);
                float independentDecisionLevel = 0.5f - (independentStrongerTotal / fleetStrength);

                float pirateStrongerTotal = getStrongerTotalForFaction(Global.getSector().getFaction(Factions.PIRATES), fleet);
                float pirateDecisionLevel = 0f - (pirateStrongerTotal / fleetStrength);

                if (!isPalace && (pirateDecisionLevel > tritachyonDecisionLevel) && (pirateDecisionLevel > independentDecisionLevel)) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
                    fleet.setNoFactionInName(true);
                    fleet.setFaction(Factions.PIRATES, true);
                } else if (!isPalace && (independentDecisionLevel > tritachyonDecisionLevel)) {
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, false);
                    fleet.setNoFactionInName(false);
                    fleet.setFaction(Factions.INDEPENDENT, true);
                } else {
                    if (!isPalace) {
                        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, false);
                        fleet.setNoFactionInName(false);
                    }
                    fleet.setFaction(Factions.TRITACHYON, true);
                }
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_NO_REP_IMPACT, "cabal_disguise", true, 99999);
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_LOW_REP_IMPACT, "cabal_disguise", true, 99999);
            }
        }

        public static float getWeakerTotalForFaction(FactionAPI faction, CampaignFleetAPI fleet) {
            List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
            float weakerTotal = 0f;
            for (CampaignFleetAPI other : visible) {
                if ((fleet.getAI() != null) && faction.isHostileTo(other.getFaction())) {
                    EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
                    float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
                    VisibilityLevel level = other.getVisibilityLevelTo(fleet);
                    boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS
                            || level == VisibilityLevel.COMPOSITION_DETAILS;
                    if ((dist < 800f) && seesComp) {
                        if ((option == EncounterOption.ENGAGE) || (option == EncounterOption.HOLD)) {
                            weakerTotal += other.getEffectiveStrength();
                        }
                    }
                }
            }

            return weakerTotal;
        }

        public static float getStrongerTotalForFaction(FactionAPI faction, CampaignFleetAPI fleet) {
            List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
            float strongerTotal = 0f;
            for (CampaignFleetAPI other : visible) {
                if ((fleet.getAI() != null) && faction.isHostileTo(other.getFaction())) {
                    EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
                    float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
                    VisibilityLevel level = other.getVisibilityLevelTo(fleet);
                    boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS
                            || level == VisibilityLevel.COMPOSITION_DETAILS;
                    if ((dist < 800f) && seesComp) {
                        if ((option == EncounterOption.DISENGAGE) || (option == EncounterOption.HOLD_VS_STRONGER)) {
                            strongerTotal += other.getEffectiveStrength();
                        }
                    }
                }
            }

            return strongerTotal;
        }

        public static StarSystemAPI pickNearestPopulatedSystem() {
            if (Global.getSector().isInNewGameAdvance()) {
                return null;
            }
            CampaignFleetAPI player = Global.getSector().getPlayerFleet();
            if (player == null) {
                return null;
            }
            StarSystemAPI nearest = null;
            float minDist = Float.MAX_VALUE;
            for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                if (market.isHidden()) {
                    continue;
                }

                if (market.isPlayerOwned() && (market.getSize() <= 3)) {
                    continue;
                }
                if (!market.hasSpaceport()) {
                    continue;
                }

                float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), market.getLocationInHyperspace());

                if (distToPlayerLY > (MAX_RANGE_FROM_PLAYER_LY * 3f)) {
                    continue;
                }

                if ((distToPlayerLY < minDist) && (market.getStarSystem() != null)) {
                    if (market.getStarSystem().getStar() != null) {
                        if (market.getStarSystem().getStar().getSpec().isPulsar()) {
                            continue;
                        }
                    }

                    nearest = market.getStarSystem();
                    minDist = distToPlayerLY;
                }
            }

            return nearest;
        }
    }

    private static enum CabalFleetType {

        FRIGATES, DESTROYERS, CRUISERS, CAPITALS, CARRIERS, PHASE, BALANCED
    }
}
