package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.hullmods.HSIElegySystem;
import data.hullmods.HSIElegySystemScript;
import data.kit.HSII18nUtil;

public class HSIElegyShipSystem extends BaseShipSystemScript {

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0) {
            if (index == 0) {
                float damMult = 1f + (HSIElegySystem.DAMAGE_BUFF_MULT - 1f) * effectLevel;
                return new StatusData(HSII18nUtil.getShipSystemString("HSIElegyStatusData0") + "+"
                        + (int) ((damMult - 1f) * 100f) + HSII18nUtil.getShipSystemString("HSIElegyStatusData1"),
                        false);
            }
        }
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo())
            return null;
        if (system.getState() != SystemState.IDLE)
            return null;

        HSIElegySystemScript script = HSIElegySystemScript.getInstance(ship);
        if (script.hasTarget()) {
            return HSII18nUtil.getShipSystemString("HSIUsable");
        }
        return HSII18nUtil.getShipSystemString("HSINoTarget");
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        // if (true) return true;
        HSIElegySystemScript script = HSIElegySystemScript.getInstance(ship);
        return script.hasTarget();
    }
}
