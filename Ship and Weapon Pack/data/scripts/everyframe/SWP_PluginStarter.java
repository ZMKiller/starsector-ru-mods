package data.scripts.everyframe;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.Description;
import data.scripts.SWPModPlugin;
import data.scripts.campaign.intel.missions.SWP_BuriedTreasure;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;
import exerelin.utilities.NexFactionConfig.StartFleetSet;
import exerelin.utilities.NexFactionConfig.StartFleetType;
import java.util.ArrayList;
import java.util.List;

public class SWP_PluginStarter extends BaseEveryFrameCombatPlugin {

    private static boolean addedOnce = false;
    private static boolean checkedOncePerCombat = false;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (SWPModPlugin.isExerelin) {
            if (checkedOncePerCombat) {
                if (!addedOnce && (Global.getCurrentState() == GameState.TITLE) && (Global.getSettings().getMissionScore("swp_duelofthecentury") > 0)) {
                    NexFactionConfig faction = NexConfig.getFactionConfig("player");
                    StartFleetSet fleetSet = faction.getStartFleetSet(StartFleetType.SUPER.name());
                    List<String> excelsiorFleet = new ArrayList<>(1);
                    excelsiorFleet.add("swp_excelsior_eli");
                    fleetSet.addFleet(excelsiorFleet);
                    if (Global.getSector().getMemoryWithoutUpdate().getBoolean(SWP_BuriedTreasure.EXCELSIOR_DESC_ALT_KEY)) {
                        Global.getSector().getMemoryWithoutUpdate().set(SWP_BuriedTreasure.EXCELSIOR_DESC_ALT_KEY, false);
                        Global.getSettings().getDescription("swp_excelsior", Description.Type.SHIP).setText1(SWPModPlugin.EXCELSIOR_DESCRIPTION);
                    }
                    addedOnce = true;
                }
                checkedOncePerCombat = false;
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        checkedOncePerCombat = true;

        if (engine.isInCampaign() && (Global.getSector() != null)) {
            if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_spatialAnomaly")) {
                boolean anomaly = Global.getSector().getMemoryWithoutUpdate().getBoolean("$swpBT_spatialAnomaly");
                if (anomaly) {
                    engine.addPlugin(new SWP_BTSpatialAnomaly());
                }
            }
        }
    }
}
