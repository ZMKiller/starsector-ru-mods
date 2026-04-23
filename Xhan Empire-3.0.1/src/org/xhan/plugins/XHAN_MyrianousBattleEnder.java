package org.xhan.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.mission.FleetSide;

import java.util.List;

public class XHAN_MyrianousBattleEnder extends BaseEveryFrameCombatPlugin {
    private static final float TIMER_LENGTH = 30f;
    private CombatEngineAPI engine;
    private float timer;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    @Override
    public void advance(float amount, List events) {
        if (Global.getCombatEngine() == null || Global.getCombatEngine().isPaused()) {
            return;
        }

        if (!engine.isSimulation()) { //don't end in the sim
            //first check if a living Myrianous is on the battlefield
            ShipAPI myrianous = null;
            for (ShipAPI ship : engine.getShips()) {
                if (ship.getHullSpec().getHullId().contentEquals("XHAN_Myrianous") && ship.isAlive()) {
                    myrianous = ship;
                }
            }
            if (myrianous != null) {
                //next see if there are any valid enemy targets that aren't currently under control NOTE: stranded fighters count
                boolean allShipsControlled = true;
                for (ShipAPI ship : engine.getShips()) {
                    if (ship != myrianous && ship.isAlive() && ship.getOriginalOwner() != myrianous.getOriginalOwner() && ship.getCustomData().get("XHAN_MindControl") == null) {
                        allShipsControlled = false;
                    }
                }
                if (allShipsControlled) {
                    timer += amount;
                } else {
                    timer = 0f;
                }

                if (timer >= TIMER_LENGTH) {
                    if (myrianous.getOriginalOwner() == 0) {
                        engine.endCombat(0f, FleetSide.PLAYER);
                        Global.getLogger(this.getClass()).info("Myrianous full control detected, ending battle");
                    }
                    if (myrianous.getOriginalOwner() == 1) {
                        engine.endCombat(0f, FleetSide.ENEMY);
                        Global.getLogger(this.getClass()).info("Myrianous full control detected, ending battle");
                    }
                }
            }
        }
    }
}