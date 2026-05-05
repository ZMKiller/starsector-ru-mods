package data.hullmods.LogisticsMod;

import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;

public interface HSILogisticsMod extends HullModEffect{
    public int getNumForLimitedMod(ShipHullSpecAPI spec,String mod);
    public boolean isLimitedMod();
}
