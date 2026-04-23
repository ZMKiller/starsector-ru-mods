package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;

public class SWP_BlockedBlankHullmod extends BaseHullMod {

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "WARNING";
        }
        return null;
    }

    @Override
    public Color getBorderColor() {
        return new Color(0, 0, 0, 0);
    }
}
