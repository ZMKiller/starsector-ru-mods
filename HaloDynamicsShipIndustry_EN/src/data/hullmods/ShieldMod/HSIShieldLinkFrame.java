package data.hullmods.ShieldMod;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.scripts.HSIShieldLinkPlugin;

public class HSIShieldLinkFrame extends HSIBaseShieldModEffect{
    public static final float RANGE = 1500f;
    public static final float FACTOR = 0.2f;
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        HSIShieldLinkPlugin.getInstance(ship.getOwner());
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) RANGE;
        if (index == 1)
            return "" + (int) (FACTOR*100f)+"%";
        return null;
    }
}
