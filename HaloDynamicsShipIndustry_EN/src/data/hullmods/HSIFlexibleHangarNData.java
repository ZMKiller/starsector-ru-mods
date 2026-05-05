package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class HSIFlexibleHangarNData {
    protected float replaceRate = 0f;
    protected Map<FighterLaunchBayAPI, Float> fastreplacement = new HashMap<>();

    public HSIFlexibleHangarNData() {
    }

    public HSIFlexibleHangarNData(ShipAPI ship) {
        replaceRate = HSIFlexibleHangarN.calculateTotalEXRate(ship.getVariant());
    }

    public float getFastReplaceMent(FighterLaunchBayAPI bay) {
        if (fastreplacement.containsKey(bay)) {
            return fastreplacement.get(bay);
        } else {
            return 0;
        }
    }

    public float getReplaceRate() {
        return replaceRate;
    }

    public void setFastreplacement(FighterLaunchBayAPI bay, float replace) {
        if (!fastreplacement.containsKey(bay)) {
            fastreplacement.put(bay, 1f);
        } else {
            fastreplacement.put(bay, replace);
        }
    }

    public void setReplaceRate(float replaceRate) {
        this.replaceRate = replaceRate;
    }

    public static HSIFlexibleHangarNData getInstance(ShipAPI ship) {
        if (ship.getCustomData().containsKey("HSIFlexibleHangarNData")) {
            return (HSIFlexibleHangarNData) ship.getCustomData().get("HSIFlexibleHangarNData");
        } else {
            HSIFlexibleHangarNData data = new HSIFlexibleHangarNData();
            ship.setCustomData("HSIFlexibleHangarNData", data);
            return data;
        }
    }

    public static void clearInstance(ShipAPI ship) {
        if (ship.getCustomData().containsKey("HSIFlexibleHangarNData")) {
            HSIFlexibleHangarNData data = (HSIFlexibleHangarNData) ship.getCustomData().get("HSIFlexibleHangarNData");
            data.fastreplacement.clear();
        }
    }
}
