package data.scripts.everyframe;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;

// Code originally by Tartiflette for Tiandong Heavy Industries
public class SWP_BlockedHullmodDisplayScript extends BaseEveryFrameCombatPlugin implements EveryFrameScript {

    private static final String NOTIFICATION_HULLMOD = "swp_blockedhullmod";
    private static final String NOTIFICATION_SOUND = "cr_allied_critical";
    private static ShipAPI ship;

    public static void showBlocked(ShipAPI blocked) {
        stopDisplaying();

        ship = blocked;
        ship.getVariant().addMod(NOTIFICATION_HULLMOD);
        Global.getSoundPlayer().playUISound(NOTIFICATION_SOUND, 1f, 1f);
    }

    public static void stopDisplaying() {
        if (ship != null) {
            ship.getVariant().removeMod(NOTIFICATION_HULLMOD);
            ship = null;
        }
    }

    @Override
    public void advance(float amount) {
        stopDisplaying();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        stopDisplaying();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        if (Global.getSettings().getCurrentState() != GameState.TITLE) {
            stopDisplaying();
            Global.getCombatEngine().removePlugin(this);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }
}
