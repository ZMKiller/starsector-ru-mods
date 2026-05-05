package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;

public class HSITurnIntoControllerble extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        boolean shouldTransfer = !ship.getVariant().hasHullMod("HSI_NoTransferToShip");
        if(ship.isFighter()&&shouldTransfer){
            ShipVariantAPI v = ship.getVariant();
            ShipAPI s = Global.getCombatEngine().createFXDrone(v);
            s.setHullSize(ShipAPI.HullSize.FRIGATE);
            s.setCollisionClass(CollisionClass.FIGHTER);
            ShipAIConfig config = new ShipAIConfig();
            config.personalityOverride = Personalities.CAUTIOUS;
            s.setShipAI(Global.getSettings().createDefaultShipAI(ship,config));
        }
    }

    public static class HSIWingToShipControl implements AdvanceableListener{

        private ShipAPI fighter;
        private ShipAPI drone;

        public HSIWingToShipControl(ShipAPI fighter,ShipAPI drone){
            this.fighter = fighter;
            this.drone = drone;
        }
        @Override
        public void advance(float amount) {

        }
    }
}
