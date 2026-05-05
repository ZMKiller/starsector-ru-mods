package progsmod.util;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static progsmod.util.SModUtils.Constants;
import static progsmod.util.SModUtils.Constants.loadCoeffsFromJSON;

public class LunaLibSettingsListener implements LunaSettingsListener {

    private static String jsonFilePath;
    public static final String id = "progressiveSMods";

    public static void init(String filePath) {
        jsonFilePath = filePath;
        LunaLibSettingsListener listener = new LunaLibSettingsListener();
        LunaSettings.addSettingsListener(listener);
        listener.settingsChanged(id);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void settingsChanged(@NotNull String modId) {
        if (!id.equals(modId)) return;

        try {
            Constants.MAX_RECENTLY_BUILT_IN_SIZE = LunaSettings.getInt(id, "psm_recentlyBuiltInListSize");
            Constants.BASE_EXTRA_SMOD_SP_COST_FRIGATE = LunaSettings.getFloat(id, "psm_baseExtraSModSPCostFrigate");
            Constants.BASE_EXTRA_SMOD_SP_COST_DESTROYER = LunaSettings.getFloat(id, "psm_baseExtraSModSPCostDestroyer");
            Constants.BASE_EXTRA_SMOD_SP_COST_CRUISER = LunaSettings.getFloat(id, "psm_baseExtraSModSPCostCruiser");
            Constants.BASE_EXTRA_SMOD_SP_COST_CAPITAL = LunaSettings.getFloat(id, "psm_baseExtraSModSPCostCapital");
            Constants.BASE_EXTRA_SMOD_XP_COST_FRIGATE = LunaSettings.getFloat(id, "psm_baseExtraSModXPCostFrigate");
            Constants.BASE_EXTRA_SMOD_XP_COST_DESTROYER = LunaSettings.getFloat(id, "psm_baseExtraSModXPCostDestroyer");
            Constants.BASE_EXTRA_SMOD_XP_COST_CRUISER = LunaSettings.getFloat(id, "psm_baseExtraSModXPCostCruiser");
            Constants.BASE_EXTRA_SMOD_XP_COST_CAPITAL = LunaSettings.getFloat(id, "psm_baseExtraSModXPCostCapital");
            Constants.EXTRA_SMOD_SP_COST_GROWTHTYPE =
                    LunaSettings.getBoolean(id, "psm_extraSModSPCostGrowthType") ? SModUtils.GrowthType.EXPONENTIAL :
                            SModUtils.GrowthType.LINEAR;
            Constants.EXTRA_SMOD_XP_COST_GROWTHTYPE =
                    LunaSettings.getBoolean(id, "psm_extraSModXPCostGrowthType") ? SModUtils.GrowthType.EXPONENTIAL :
                            SModUtils.GrowthType.LINEAR;
            Constants.EXTRA_SMOD_SP_COST_GROWTHFACTOR = LunaSettings.getFloat(id, "psm_extraSModSPCostGrowthFactor");
            Constants.EXTRA_SMOD_XP_COST_GROWTHFACTOR = LunaSettings.getFloat(id, "psm_extraSModXPCostGrowthFactor");
            Constants.DEPLOYMENT_COST_PENALTY = LunaSettings.getFloat(id, "psm_deploymentCostPenalty");
            Constants.XP_REFUND_FACTOR = LunaSettings.getFloat(id, "psm_xpRefundFactor");
            Constants.RESERVE_XP_FRACTION = LunaSettings.getFloat(id, "psm_reserveXPFraction");
            Constants.IGNORE_NO_BUILD_IN = LunaSettings.getBoolean(id, "psm_IgnoreNoBuildIn");
            Constants.ALLOW_INCREASE_SMOD_LIMIT = LunaSettings.getBoolean(id, "psm_allowIncreaseSModLimit");
            Constants.DISABLE_MOD = LunaSettings.getBoolean(id, "psm_disableMod");

            Constants.GIVE_XP_TO_DISABLED_SHIPS = LunaSettings.getBoolean(id, "psm_giveXPToDisabledShips");
            Constants.ONLY_GIVE_XP_FOR_KILLS = LunaSettings.getBoolean(id, "psm_onlyGiveXPForKills");
            Constants.CONDENSE_XP_GAIN_MESSAGES = LunaSettings.getBoolean(id, "psm_condenseXPGainMessages");
            Constants.XP_GAIN_MULTIPLIER = LunaSettings.getFloat(id, "psm_xpGainMultiplier");
            Constants.TARGET_DMOD_LOWER_BOUND = LunaSettings.getFloat(id, "psm_targetDModLowerBound");
            Constants.POST_BATTLE_XP_FRACTION = LunaSettings.getFloat(id, "psm_postBattleXPFraction");
            Constants.POST_BATTLE_CIVILIAN_MULTIPLIER = LunaSettings.getFloat(id, "psm_postBattleCivilianMultiplier");
            Constants.POST_BATTLE_AUTO_PURSUIT_MULTIPLIER =
                    LunaSettings.getFloat(id, "psm_postBattleAutoPursuitMultiplier");
            Constants.XP_FRACTION_ATTACK = LunaSettings.getFloat(id, "psm_xpFractionAttack");
            Constants.XP_FRACTION_DEFENSE = LunaSettings.getFloat(id, "psm_xpFractionDefense");
            Constants.XP_FRACTION_SUPPORT = LunaSettings.getFloat(id, "psm_xpFractionSupport");
            Constants.COMBAT_UPDATE_INTERVAL = LunaSettings.getFloat(id, "psm_combatUpdateInterval");

            // Still need to load some stuff from the json
            JSONObject json = Global.getSettings().loadJSON(jsonFilePath);
            JSONObject costCoeff = json.getJSONObject("xpCostCoeff");
            Constants.XP_COST_COEFF_FRIGATE = loadCoeffsFromJSON(costCoeff, "frigate", LunaSettings.getFloat(id, "psm_xpCostMultiplierFrigate"));
            Constants.XP_COST_COEFF_DESTROYER = loadCoeffsFromJSON(costCoeff, "destroyer", LunaSettings.getFloat(id, "psm_xpCostMultiplierDestroyer"));
            Constants.XP_COST_COEFF_CRUISER = loadCoeffsFromJSON(costCoeff, "cruiser", LunaSettings.getFloat(id, "psm_xpCostMultiplierCruiser"));
            Constants.XP_COST_COEFF_CAPITAL = loadCoeffsFromJSON(costCoeff, "capital", LunaSettings.getFloat(id, "psm_xpCostMultiplierCapital"));
            Constants.BASE_DP_FRIGATE = (float) json.getDouble("baseDPFrigate");
            Constants.BASE_DP_DESTROYER = (float) json.getDouble("baseDPDestroyer");
            Constants.BASE_DP_CRUISER = (float) json.getDouble("baseDPCruiser");
            Constants.BASE_DP_CAPITAL = (float) json.getDouble("baseDPCapital");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
