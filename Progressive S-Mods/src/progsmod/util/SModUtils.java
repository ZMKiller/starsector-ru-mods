package progsmod.util;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.CampaignUIAPI.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.*;
import com.fs.starfarer.api.fleet.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.loading.*;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.*;
import org.json.*;
import progsmod.data.campaign.rulecmd.util.HullModButtonData;
import progsmod.data.combat.ContributionTracker.*;

import java.io.*;
import java.util.*;

public class SModUtils {

    /** For syncing the XP labels when executing a console command */
    public interface ForceUpdater {
        void addXP(int xp);
        void addReserveXP(int xp);
        void resetXP();
    }

    public static ForceUpdater forceUpdater = null;

    public enum GrowthType {LINEAR, EXPONENTIAL}

    /** Lookup key into the sector-persistent data that stores ship data */
    public static final String SHIP_DATA_KEY = "progsmod_ShipData";
    public static ShipDataTable SHIP_DATA_TABLE = new ShipDataTable();

    /** Lookup key into the sector-persistent data that stores reserve XP from losses */
    public static final String RESERVE_XP_KEY = "progsmod_ReserveXP";
    public static ReserveXPTable RESERVE_XP_TABLE = new ReserveXPTable();

    public static class Constants {
        public static int MAX_RECENTLY_BUILT_IN_SIZE;
        /** How many story points it costs to unlock the first extra SMod slot. */
        public static float BASE_EXTRA_SMOD_SP_COST_FRIGATE;
        public static float BASE_EXTRA_SMOD_SP_COST_DESTROYER;
        public static float BASE_EXTRA_SMOD_SP_COST_CRUISER;
        public static float BASE_EXTRA_SMOD_SP_COST_CAPITAL;
        /** How much XP it costs to unlock the first extra SMod slot. */
        public static float BASE_EXTRA_SMOD_XP_COST_FRIGATE;
        public static float BASE_EXTRA_SMOD_XP_COST_DESTROYER;
        public static float BASE_EXTRA_SMOD_XP_COST_CRUISER;
        public static float BASE_EXTRA_SMOD_XP_COST_CAPITAL;
        /** Whether the story point increase for unlocking extra SMod slots is linear or exponential. */
        public static GrowthType EXTRA_SMOD_SP_COST_GROWTHTYPE;
        /** If exponential, SP cost is BASE * GROWTH_FACTOR^n, otherwise SP cost is BASE + n*GROWTH_FACTOR. */
        public static float EXTRA_SMOD_SP_COST_GROWTHFACTOR;
        /** Same as above two but for XP cost */
        public static GrowthType EXTRA_SMOD_XP_COST_GROWTHTYPE;
        public static float EXTRA_SMOD_XP_COST_GROWTHFACTOR;
        /** The base amount of XP it costs per OP to build in a hull mod is defined as
         *  p(x) where x is the OP cost of the mod and p is a polynomial with coefficients
         * in [XP_COST_COEFFS] listed in ascending order. */
        public static float[] XP_COST_COEFF_FRIGATE;
        public static float[] XP_COST_COEFF_DESTROYER;
        public static float[] XP_COST_COEFF_CRUISER;
        public static float[] XP_COST_COEFF_CAPITAL;
        /** The XP cost is proportial to the ship's DP cost; the above represents the cost
         * for a ship with the base DP cost. */
        public static float BASE_DP_FRIGATE;
        public static float BASE_DP_DESTROYER;
        public static float BASE_DP_CRUISER;
        public static float BASE_DP_CAPITAL;
        /** DP cost penalty for s-mods over the normal limit */
        public static float DEPLOYMENT_COST_PENALTY;

        /** How much XP a ship gets refunded when you remove a built-in mod.
         * Set to something less than 0 to disable removing built-in mods completely. */
        public static float XP_REFUND_FACTOR;
        /** Whether or not ships that get disabled in battle should still get XP */
        public static boolean GIVE_XP_TO_DISABLED_SHIPS;
        /** Whether or not enemy ships that aren't disabled should still award XP */
        public static boolean ONLY_GIVE_XP_FOR_KILLS;
        /** XP gain multiplier */
        public static float XP_GAIN_MULTIPLIER;
        /** When the player loses a ship with XP, this fraction of XP lost is added to
         a reserve pool that can be used by any ship with the same hull type.*/
        public static float RESERVE_XP_FRACTION;
        /** Enemy ships with d-mods give less XP than pristine ships;
         *  however, regardless of the number of D-mods, they will always
         *  give at least this fraction of a pristine ship's XP. */
        public static float TARGET_DMOD_LOWER_BOUND;
        /** XP gained by ships as a fraction of total XP gain */
        public static float POST_BATTLE_XP_FRACTION;
        /** Multiplier of the above for civilian ships */
        public static float POST_BATTLE_CIVILIAN_MULTIPLIER;
        /** Multiplier for auto-resolve pursuits */
        public static float POST_BATTLE_AUTO_PURSUIT_MULTIPLIER;
        /** Fraction of enemy ships' total XP worth that goes toward the ATTACK role */
        public static float XP_FRACTION_ATTACK;
        /** Fraction of enemy ships' total XP worth that goes toward the DEFENSE role */
        public static float XP_FRACTION_DEFENSE;
        /** Fraction of enemy ships' total XP worth that goes toward the SUPPORT role */
        public static float XP_FRACTION_SUPPORT;
        /** Ignore the 'no_build_in' tag */
        public static boolean IGNORE_NO_BUILD_IN;
        /** Allows increasing # of built-in hull mods with story points */
        public static boolean ALLOW_INCREASE_SMOD_LIMIT;
        /** How often the combat tracker should update ship contribution. */
        public static float COMBAT_UPDATE_INTERVAL;

        /** Set to true to disable this mod's features */
        public static boolean DISABLE_MOD;

        public static boolean CONDENSE_XP_GAIN_MESSAGES;

        /** Load constants from a json file */
        private static void load(String filePath) throws IOException, JSONException {
            JSONObject json = Global.getSettings().loadJSON(filePath);
            MAX_RECENTLY_BUILT_IN_SIZE = json.getInt("recentlyBuiltInListSize");
            JSONObject augmentSP = json.getJSONObject("baseExtraSModSPCost");
            BASE_EXTRA_SMOD_SP_COST_FRIGATE = (float) augmentSP.getDouble("frigate");
            BASE_EXTRA_SMOD_SP_COST_DESTROYER = (float) augmentSP.getDouble("destroyer");
            BASE_EXTRA_SMOD_SP_COST_CRUISER = (float) augmentSP.getDouble("cruiser");
            BASE_EXTRA_SMOD_SP_COST_CAPITAL = (float) augmentSP.getDouble("capital");
            JSONObject augmentXP = json.getJSONObject("baseExtraSModXPCost");
            BASE_EXTRA_SMOD_XP_COST_FRIGATE = (float) augmentXP.getDouble("frigate");
            BASE_EXTRA_SMOD_XP_COST_DESTROYER = (float) augmentXP.getDouble("destroyer");
            BASE_EXTRA_SMOD_XP_COST_CRUISER = (float) augmentXP.getDouble("cruiser");
            BASE_EXTRA_SMOD_XP_COST_CAPITAL = (float) augmentXP.getDouble("capital");
            EXTRA_SMOD_SP_COST_GROWTHTYPE =
                    json.getInt("extraSModSPCostGrowthType") == 0 ? GrowthType.LINEAR : GrowthType.EXPONENTIAL;
            EXTRA_SMOD_SP_COST_GROWTHFACTOR = (float) json.getDouble("extraSModSPCostGrowthFactor");
            EXTRA_SMOD_XP_COST_GROWTHTYPE =
                    json.getInt("extraSModXPCostGrowthType") == 0 ? GrowthType.LINEAR : GrowthType.EXPONENTIAL;
            EXTRA_SMOD_XP_COST_GROWTHFACTOR = (float) json.getDouble("extraSModXPCostGrowthFactor");
            JSONObject costCoeff = json.getJSONObject("xpCostCoeff");
            XP_COST_COEFF_FRIGATE = loadCoeffsFromJSON(costCoeff, "frigate", 1f);
            XP_COST_COEFF_DESTROYER = loadCoeffsFromJSON(costCoeff, "destroyer", 1f);
            XP_COST_COEFF_CRUISER = loadCoeffsFromJSON(costCoeff, "cruiser", 1f);
            XP_COST_COEFF_CAPITAL = loadCoeffsFromJSON(costCoeff, "capital", 1f);
            DEPLOYMENT_COST_PENALTY = (float) json.getDouble( "deploymentCostPenalty");
            BASE_DP_FRIGATE = (float) json.getDouble("baseDPFrigate");
            BASE_DP_DESTROYER = (float) json.getDouble("baseDPDestroyer");
            BASE_DP_CRUISER = (float) json.getDouble("baseDPCruiser");
            BASE_DP_CAPITAL = (float) json.getDouble("baseDPCapital");
            XP_REFUND_FACTOR = (float) json.getDouble("xpRefundFactor");
            RESERVE_XP_FRACTION = (float) json.getDouble("reserveXPFraction");
            IGNORE_NO_BUILD_IN = json.getBoolean("ignoreNoBuildIn");
            ALLOW_INCREASE_SMOD_LIMIT = json.getBoolean("allowIncreaseSModLimit");
            DISABLE_MOD = json.getBoolean("disableMod");

            JSONObject combat = json.getJSONObject("combat");
            GIVE_XP_TO_DISABLED_SHIPS = combat.getBoolean("giveXPToDisabledShips");
            ONLY_GIVE_XP_FOR_KILLS = combat.getBoolean("onlyGiveXPForKills");
            XP_GAIN_MULTIPLIER = (float) combat.getDouble("xpGainMultiplier");
            POST_BATTLE_XP_FRACTION = (float) combat.getDouble("postBattleXPFraction");
            POST_BATTLE_CIVILIAN_MULTIPLIER = (float) combat.getDouble("postBattleCivilianMultiplier");
            POST_BATTLE_AUTO_PURSUIT_MULTIPLIER = (float) combat.getDouble("postBattleAutoPursuitMultiplier");
            TARGET_DMOD_LOWER_BOUND = (float) combat.getDouble("targetDModLowerBound");
            COMBAT_UPDATE_INTERVAL = (float) combat.getDouble("combatUpdateInterval");
            XP_FRACTION_ATTACK = (float) combat.getDouble("xpFractionAttack");
            XP_FRACTION_DEFENSE = (float) combat.getDouble("xpFractionDefense");
            XP_FRACTION_SUPPORT = (float) combat.getDouble("xpFractionSupport");
            CONDENSE_XP_GAIN_MESSAGES = combat.getBoolean("condenseXPGainMessages");
        }

        static float[] loadCoeffsFromJSON(JSONObject json, String name, float multiplier) throws JSONException {
            JSONArray jsonArray = json.getJSONArray(name);
            int length = jsonArray.length();
            float[] coeffs = new float[length];
            for (int i = 0; i < jsonArray.length(); i++) {
                coeffs[i] = (float) jsonArray.getDouble(i) * multiplier;
            }
            return coeffs;
        }
    }

    /** Contains XP and # of max perma mods over the normal limit. */
    public static class ShipData {
        public float xp;
        public float xpSpentOnIncreasingLimit = 0f;
        public int permaModsOverLimit;
        public boolean initialSModsAccountedFor = false;

        public ShipData(float xp, int pmol) {
            this.xp = xp;
            permaModsOverLimit = pmol;
        }
    }

    /** Wrapper class that maps ships to their ship data. */
    public static class ShipDataTable extends HashMap<String, ShipData> {}

    /** Wrapper class that maps hull id to reserve XP */
    public static class ReserveXPTable extends HashMap<String, Float> {}

    public static void loadConstants(String filePath) {
        try {
            if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
                LunaLibSettingsListener.init(filePath);
            }
            else {
                Constants.load(filePath);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /** Retrieve the persistent data for this mod, if it exists. Else create it. */
    public static void loadData() {
        if (!Global.getSector().getPersistentData().containsKey(SHIP_DATA_KEY)) {
            SHIP_DATA_TABLE = new ShipDataTable();
            Global.getSector().getPersistentData().put(SHIP_DATA_KEY, SHIP_DATA_TABLE);
        }
        else {
            SHIP_DATA_TABLE = (ShipDataTable) Global.getSector().getPersistentData().get(SHIP_DATA_KEY);
        }

        if (!Global.getSector().getPersistentData().containsKey(RESERVE_XP_KEY)) {
            RESERVE_XP_TABLE = new ReserveXPTable();
            Global.getSector().getPersistentData().put(RESERVE_XP_KEY, RESERVE_XP_TABLE);
        }
        else {
            RESERVE_XP_TABLE = (ReserveXPTable) Global.getSector().getPersistentData().get(RESERVE_XP_KEY);
        }

        // Convert reserve XP for specialized hull types to the base hull type
        // (for backwards compatibility)
        List<String> toRemove = new ArrayList<>();
        List<Pair<String, Float>> toAdd = new ArrayList<>();
        for (Map.Entry<String, Float> entry : RESERVE_XP_TABLE.entrySet()) {
            String hullId = entry.getKey();
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            float amount = entry.getValue();
            if (spec != null) {
                String baseHullId = spec.getBaseHullId();
                if (!hullId.equals(baseHullId)) {
                    toAdd.add(new Pair<>(baseHullId, amount));
                    toRemove.add(hullId);
                }
            }
        }
        for (String id : toRemove) {
            RESERVE_XP_TABLE.remove(id);
        }
        for (Pair<String, Float> entry : toAdd) {
            addReserveXP(entry.one, entry.two);
        }
    }

    /** Add [amount] of reserve XP to [hullId] (shared by all ships with hull [hullId]) */
    public static void addReserveXP(String hullId, float amount) {
        Float cur = RESERVE_XP_TABLE.get(hullId);
        RESERVE_XP_TABLE.put(hullId, cur == null ? amount : cur + amount);
    }

    public static float getReserveXP(String hullId) {
        Float reserveXP = RESERVE_XP_TABLE.get(hullId);
        return reserveXP == null ? 0f : reserveXP;
    }

    public static float getReserveXP(FleetMemberAPI fm) {
        return getReserveXP(fm.getHullSpec().getBaseHullId());
    }

    /** Decreases RESERVE_XP_TABLE[hullId] by [amount]. Increases SHIP_DATA_TABLE[fm.getId()].xp by [amount].
     *  Returns whether the operation was successful. */
    public static boolean useReserveXP(String hullId, FleetMemberAPI fm, float amount) {
        Float available = RESERVE_XP_TABLE.get(hullId);
        if (available == null || available < amount) {
            return false;
        }
        RESERVE_XP_TABLE.put(hullId, available - amount);
        giveXP(fm, amount);
        return true;
    }

    /** Add [xp] XP to [fmId]'s entry in the ship data table,
     *  creating the entry if it doesn't exist yet.
     *  Returns whether a new entry was created. */
    public static boolean giveXP(String fmId, float xp) {
        if (Float.isNaN(xp)) {
            return false;
        }
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        if (data == null) {
            SHIP_DATA_TABLE.put(fmId, new ShipData(xp, 0));
            return true;
        }
        else {
            data.xp += xp;
            return false;
        }
    }

    /** Same as giveXP(fmId, xp) except also adds a tracker hull mod if [fm]
     *  doesn't have one. */
    public static boolean giveXP(FleetMemberAPI fm, float xp) {
        String fmId = fm.getId();
        boolean createdEntry = giveXP(fmId, xp);
        addTrackerHullMod(fm);
        return createdEntry;
    }

    /** Adds an XP tracking hull mod to the ship in question */
    public static void addTrackerHullMod(FleetMemberAPI fm) {
        if (!fm.getVariant().hasHullMod("progsmod_xptracker")) {
            ShipVariantAPI variant = fm.getVariant();
            if (variant.isStockVariant() || variant.isGoalVariant() || variant.isEmptyHullVariant()) {
                fm.setVariant(variant.clone(), false, false);
                fm.getVariant().setSource(VariantSource.REFIT);
            }
            fm.getVariant().addPermaMod("progsmod_xptracker", false);
        }
    }

    public static void addTrackerHullMod(ShipVariantAPI variant) {
        if (!variant.hasHullMod("progsmod_xptracker")) {
            variant.addPermaMod("progsmod_xptracker", false);
        }
    }

    /** Remove [xp] XP from [fmId]'s entry in the ship data table.
     *  Returns [true] if and only if the operation succeeded. */
    public static boolean spendXP(String fmId, float xp) {
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        if (data == null) {
            data = new ShipData(0f, 0);
            SHIP_DATA_TABLE.put(fmId, data);
        }
        if (enoughXP(fmId, xp)) {
            data.xp -= xp;
            return true;
        }
        return false;
    }

    /** Returns [true] if [fmId]'s entry in the ship data table is >= [xp]. */
    public static boolean enoughXP(String fmId, float xp) {
        if (Float.isNaN(xp)) {
            return false;
        }
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        // Edge case: no ship data (technically 0 XP)
        // should still be able to build in hull mods
        // that cost 0 XP
        if (xp <= 0f) {
            return true;
        }
        return data != null && data.xp >= xp;
    }

    /** Removes [fmId] from the ship data table. */
    public static void deleteXPData(String fmId) {
        SHIP_DATA_TABLE.remove(fmId);
    }

    /** Increases [fleetMember]'s limit of built in hull mods by 1.
     *  Spends the required XP. */
    public static boolean incrementSModLimit(FleetMemberAPI fleetMember) {
        String fmId = fleetMember.getId();
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        int cost = getAugmentXPCost(fleetMember);
        if (data == null && cost <= 0f) {
            SHIP_DATA_TABLE.put(fmId, new ShipData(0, 1));
            // S-mod limit increase is applied by the XPTracker hull mod, so add it if needed
            addTrackerHullMod(fleetMember);
            fleetMember.updateStats();
            return true;
        } else if (data != null && spendXP(fmId, cost)) {
            data.permaModsOverLimit++;
            data.xpSpentOnIncreasingLimit += cost;
            fleetMember.updateStats();  // Force update XPTracker hull mod to apply S-mod limit
            return true;
        }
        return false;
    }

    public static float getXP(String fmId) {
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        return data == null ? 0f : data.xp;
    }

    public static int getNumOverLimit(String fmId) {
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        return data == null ? 0 : data.permaModsOverLimit;
    }

    public static float getXPSpentOnIncreasingLimit(String fmId) {
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        return data == null ? 0f : data.xpSpentOnIncreasingLimit;
    }

    public static class AugmentSPCost {
        public int spCost;
        public float bonusXP;

        @Override
        public String toString() {
            return "" + spCost; // "1" and not "1.0"
        }
    }

    /** Gets the story point cost of increasing the number of built-in hullmods of [ship] by 1. */
    public static AugmentSPCost getAugmentSPCost(FleetMemberAPI ship) {
        float baseCost;
        switch (ship.getVariant().getHullSize()) {
            case FRIGATE: baseCost = Constants.BASE_EXTRA_SMOD_SP_COST_FRIGATE; break;
            case DESTROYER: baseCost = Constants.BASE_EXTRA_SMOD_SP_COST_DESTROYER; break;
            case CRUISER: baseCost = Constants.BASE_EXTRA_SMOD_SP_COST_CRUISER; break;
            case CAPITAL_SHIP: baseCost = Constants.BASE_EXTRA_SMOD_SP_COST_CAPITAL; break;
            default: baseCost = 0;
        }

        int modsOverLimit = getNumOverLimit(ship.getId());

        double spCost = Constants.EXTRA_SMOD_SP_COST_GROWTHTYPE == GrowthType.EXPONENTIAL ?
                baseCost * Math.pow(Constants.EXTRA_SMOD_SP_COST_GROWTHFACTOR, modsOverLimit) :
                baseCost + modsOverLimit * Constants.EXTRA_SMOD_SP_COST_GROWTHFACTOR;

        // If augment would cost 0.75 SP, round up to 1 SP but grant 25% bonus XP to effectively refund 0.25 SP
        // Vanilla does the same thing with S-mods; E.g. building a mod into a frigate gives 75% bonus XP
        AugmentSPCost augmentSpCost = new AugmentSPCost();
        augmentSpCost.spCost = (int) Math.ceil(spCost);
        augmentSpCost.bonusXP = (float) (augmentSpCost.spCost - spCost);
        return augmentSpCost;
    }

    /** Gets the XP cost of increasing the number of built-in hullmods of [ship] by 1. */
    public static int getAugmentXPCost(FleetMemberAPI fleetMember) {
        return getAugmentXPCost(fleetMember, getNumOverLimit(fleetMember.getId()));
    }

    /** Gets the XP cost of increasing the number of buit-in hullmods by 1,
     *  when this option has already been used [nOverLimit] times. */
    public static int getAugmentXPCost(FleetMemberAPI fleetMember, int nOverLimit) {
        float baseCost;
        float deploymentCost = fleetMember.getUnmodifiedDeploymentPointsCost();
        switch (fleetMember.getVariant().getHullSize()) {
            case FRIGATE: baseCost = Constants.BASE_EXTRA_SMOD_XP_COST_FRIGATE * deploymentCost / Constants.BASE_DP_FRIGATE; break;
            case DESTROYER: baseCost = Constants.BASE_EXTRA_SMOD_XP_COST_DESTROYER * deploymentCost / Constants.BASE_DP_DESTROYER; break;
            case CRUISER: baseCost = Constants.BASE_EXTRA_SMOD_XP_COST_CRUISER * deploymentCost / Constants.BASE_DP_CRUISER; break;
            case CAPITAL_SHIP: baseCost = Constants.BASE_EXTRA_SMOD_XP_COST_CAPITAL * deploymentCost / Constants.BASE_DP_CAPITAL; break;
            default: baseCost = 0;
        }

        return Constants.EXTRA_SMOD_XP_COST_GROWTHTYPE == GrowthType.EXPONENTIAL ?
                (int) (baseCost * Math.pow(Constants.EXTRA_SMOD_XP_COST_GROWTHFACTOR, nOverLimit)) :
                (int) (baseCost + nOverLimit * baseCost * Constants.EXTRA_SMOD_XP_COST_GROWTHFACTOR);
    }

    /** Gets the XP cost of building in a certain hullmod */
    public static int getBuildInCost(HullModSpecAPI hullMod, HullSize size, float deploymentCost) {
        float cost, mult;
        switch (size) {
            case FRIGATE:
                cost = computePolynomial(hullMod.getFrigateCost(), Constants.XP_COST_COEFF_FRIGATE);
                mult = deploymentCost / Constants.BASE_DP_FRIGATE;
                break;
            case DESTROYER:
                cost = computePolynomial(hullMod.getDestroyerCost(), Constants.XP_COST_COEFF_DESTROYER);
                mult = deploymentCost / Constants.BASE_DP_DESTROYER;
                break;
            case CRUISER:
                cost = computePolynomial(hullMod.getCruiserCost(), Constants.XP_COST_COEFF_CRUISER);
                mult = deploymentCost / Constants.BASE_DP_CRUISER;
                break;
            case CAPITAL_SHIP:
                cost = computePolynomial(hullMod.getCapitalCost(), Constants.XP_COST_COEFF_CAPITAL);
                mult = deploymentCost / Constants.BASE_DP_CAPITAL;
                break;
            default: return 0;
        }
        return (int) Math.max(0f, cost * mult);
    }

    /** Given a list of fleetMembers, return a list of their ids */
    public static List<String> getFleetMemberIds(List<FleetMemberAPI> fleetMembers) {
        List<String> ids = new ArrayList<>(fleetMembers.size());
        for (FleetMemberAPI fm : fleetMembers) {
            ids.add(fm.getId());
        }
        return ids;
    }

    /** For when a ship has an increased s-mod limit from another source.
     *  Only do this once to avoid cheesing repeated assigning / unassigning of BotB. */
    @Deprecated
    public static void initializeSModIncreaseLimit(FleetMemberAPI fm, int nSMods) {
        int normalMax = getBaseSMods(fm);
        int numOverLimit = Math.max(0, nSMods - normalMax);
        String fmId = fm.getId();
        ShipData data = SHIP_DATA_TABLE.get(fmId);
        if (data == null) {
            ShipData newData = new ShipData(0f, numOverLimit);
            newData.initialSModsAccountedFor = true;
            SHIP_DATA_TABLE.put(fmId, newData);
        }
        else if (!data.initialSModsAccountedFor) {
            data.permaModsOverLimit = Math.max(data.permaModsOverLimit, numOverLimit);
            data.initialSModsAccountedFor = true;
        }
    }

    /** Given a fleet member, return its S-Mod limit */
    public static int getMaxSMods(FleetMemberAPI fleetMember) {
        return getNumOverLimit(fleetMember.getId()) + getBaseSMods(fleetMember);
    }

    public static int getBaseSMods(FleetMemberAPI fleetMember) {
        return getBaseSMods(fleetMember.getStats());
    }

    public static int getBaseSMods(MutableShipStatsAPI stats) {
        return (int) stats.getDynamic()
                .getMod(Stats.MAX_PERMANENT_HULLMODS_MOD)
                .computeEffective(Misc.MAX_PERMA_MODS)
                // XPTracker hull mod increases MAX_PERMANENT_HULLMODS_MOD, so subtract it
                - getNumOverLimit(stats.getFleetMember().getId());
    }

    /** Polynomial coefficients are listed in [coeff] lowest order first. */
    public static float computePolynomial(int x, float[] coeff) {
        float result = 0;
        for (int i = coeff.length - 1; i >= 0; i--) {
            result = result*x + coeff[i];
        }
        return result;
    }

    /** Mostly copied from the API */
    public static boolean canModifyHullMod(HullModSpecAPI spec, SectorEntityToken interactionTarget) {
        if (spec == null) return true;

        boolean reqSpaceport = spec.hasTag(HullMods.TAG_REQ_SPACEPORT);
        if (!reqSpaceport) return true;

        return isAtStationOrSpacePort(interactionTarget);
    }

    public static boolean canModifyHullMod(HullModButtonData buttonData) {
        return SModUtils.canModifyHullMod(Global.getSettings().getHullModSpec(buttonData.id),
                Global.getSector().getCampaignUI().getCurrentInteractionDialog().getInteractionTarget());
    }

    public static boolean isAtStationOrSpacePort(SectorEntityToken interactionTarget) {
        MarketAPI market = interactionTarget.getMarket();
        if (market == null) return false;

        Object tradeMode = interactionTarget.getMemory().get("$tradeMode");
        if (tradeMode == null || tradeMode.equals(CoreUITradeMode.NONE) || tradeMode.equals("NONE")) {
            return false;
        }

        for (Industry ind : market.getIndustries()) {
            if (ind.getSpec().hasTag(Industries.TAG_STATION)) return true;
            if (ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) return true;
        }

        return false;
    }

    /** Shows [fleetMember]'s XP as a paragraph in [dialog] */
    public static void displayXP(InteractionDialogAPI dialog, FleetMemberAPI fleetMember) {
        int xp = (int) SModUtils.getXP(fleetMember.getId());
        String xpFmt = Misc.getFormat().format(xp);
        dialog.getTextPanel()
                .addPara(String.format("The %s has %s XP.", fleetMember.getShipName(), xpFmt))
                .setHighlight(fleetMember.getShipName(), xpFmt);
    }

    public static void addCondensedXPGainToDialog(InteractionDialogAPI dialog, float xp, int shipCount) {
        if (dialog == null || dialog.getTextPanel() == null) return;
        dialog.getTextPanel().setFontSmallInsignia();
        if (shipCount == 0) return;
        else if (shipCount == 1) {
            dialog.getTextPanel().addPara(
                    "Gained a total of %s ship XP.",
                    Misc.getTextColor(),
                    Misc.getHighlightColor(),
                    Misc.getFormat().format(xp));
        }
        else {
            dialog.getTextPanel().addPara(
                    "Gained a total of %s ship XP across %s ships.",
                    Misc.getTextColor(),
                    Misc.getHighlightColor(),
                    Misc.getFormat().format(xp),
                    "" + shipCount);
        }
        dialog.getTextPanel().setFontInsignia();
    }

    /** Creates the text "The [fleetMember] gained [xp] xp [additionalText]: ",
     *  followed by a breakdown of ATTACK, DEFENSE, and SUPPORT XP gain amounts. */
    public static void addTypedXPGainToDialog(InteractionDialogAPI dialog, FleetMemberAPI fleetMember, Map<ContributionType, Float> xp, String additionalText) {
        if (dialog == null || dialog.getTextPanel() == null) {
            return;
        }
        float totalXPFloat = 0f;
        for (float partXP : xp.values()) {
            totalXPFloat += partXP;
        }
        int totalXP = Math.round(totalXPFloat);
        if (totalXP <= 0) {
            return;
        }
        List<String> highlights = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("The ");
        String shipName = fleetMember.getShipName();
        sb.append(shipName).append(", ");
        ShipHullSpecAPI hullSpec = fleetMember.getVariant().getHullSpec();
        String totalXPFmt = Misc.getFormat().format(totalXP);
        sb.append(String.format("%s gained %s XP %s:", hullSpec.getHullNameWithDashClass(), totalXPFmt, additionalText));
        highlights.add(hullSpec.getHullName());
        highlights.add(totalXPFmt);
        for (Map.Entry<ContributionType, Float> partXP : xp.entrySet()) {
            sb.append("\n    - ");
            int part = Math.round(partXP.getValue());
            String partFmt = Misc.getFormat().format(part);
            switch (partXP.getKey()) {
                case ATTACK:
                    sb.append(partFmt).append(" XP gained from offensive actions");
                    break;
                case DEFENSE:
                    sb.append(partFmt).append(" XP gained from defensive actions");
                    break;
                case SUPPORT:
                    sb.append(partFmt).append(" XP gained from supportive actions");
                    break;
                default:
                    break;
            }
            highlights.add(partFmt);
        }
        dialog.getTextPanel().setFontSmallInsignia();
        LabelAPI para = dialog.getTextPanel().addPara(sb.toString());
        para.setHighlight(highlights.toArray(new String[0]));
        dialog.getTextPanel().setFontInsignia();
    }

    /** Adds "All ships in fleet gained [xp] additional XP [additionalText]:" followed by the list of ships in
     *  [fleetMembers]. */
    public static void addPostBattleXPGainToDialog(InteractionDialogAPI dialog, List<FleetMemberAPI> civilianShips, int xp, int civilianXP, boolean isAutoResolve) {
        if (dialog == null || dialog.getTextPanel() == null) {
            return;
        }
        if (Constants.CONDENSE_XP_GAIN_MESSAGES) {
            int civilianSize = civilianShips.size();
            int combatSize = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().size() - civilianSize;
            int total = xp * combatSize + civilianXP * civilianSize;
            dialog.getTextPanel().setFontSmallInsignia();
            String xpFmt = Misc.getFormat().format(total);
            dialog.getTextPanel().addPara(
                    isAutoResolve ? "All ships in fleet gained a combined %s XP." : "All ships in fleet gained an additional combined %s XP.",
                    Misc.getTextColor(),
                    Misc.getHighlightColor(),
                    xpFmt);
            dialog.getTextPanel().setFontInsignia();
            return;
        }
        List<String> highlights = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String xpFmt = Misc.getFormat().format(xp);
        String civilianXPFmt = Misc.getFormat().format(civilianXP);
        sb.append("All combat ships in fleet gained ").append(xpFmt).append(" additional XP.");
        highlights.add(xpFmt);
        if (!civilianShips.isEmpty()) {
            sb.append("\nThe following ships gained ").append(civilianXPFmt)
                    .append(" XP due to being civilian ships, or having no weapons or fighters equipped:");
            highlights.add(civilianXPFmt);
            for (FleetMemberAPI fleetMember : civilianShips) {
                sb.append("\n    - ");
                String shipName = fleetMember.getShipName();
                ShipHullSpecAPI hullSpec = fleetMember.getVariant().getHullSpec();
                sb.append(shipName).append(", ").append(hullSpec.getHullNameWithDashClass());
                highlights.add(hullSpec.getHullName());
            }
        }
        dialog.getTextPanel().setFontSmallInsignia();
        LabelAPI text = dialog.getTextPanel().addPara(sb.toString());
        text.setHighlight(highlights.toArray(new String[0]));
        dialog.getTextPanel().setFontInsignia();
    }

    /** Returns a list of the module variants of a base variant that have positive OP. */
    public static List<ShipVariantAPI> getModuleVariantsWithOP(ShipVariantAPI base) {
        List<ShipVariantAPI> withOP = new ArrayList<>();
        if (base.getModuleSlots() == null) {
            return withOP;
        }
        for (int i = 0; i < base.getModuleSlots().size(); i++) {
            ShipVariantAPI moduleVariant = base.getModuleVariant(base.getModuleSlots().get(i));
            if (moduleVariant.getHullSpec().getOrdnancePoints(null) <= 0) {
                continue;
            }
            withOP.add(moduleVariant);
        }
        return withOP;
    }

    public static String shortenText(String text, LabelAPI label) {
        if (text == null) {
            return null;
        }
        float ellipsesWidth = label.computeTextWidth("...");
        float maxWidth = label.getPosition().getWidth() * 0.95f - ellipsesWidth;
        if (label.computeTextWidth(text) <= maxWidth) {
            return text;
        }
        int left = 0, right = text.length();

        String newText = text;
        while (right > left) {
            int mid = (left + right) / 2;
            newText = text.substring(0, mid);
            if (label.computeTextWidth(newText) > maxWidth) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        return newText + "...";
    }
}
