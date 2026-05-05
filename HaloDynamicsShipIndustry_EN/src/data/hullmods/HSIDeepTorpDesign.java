package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIDeepTorpDesign extends BaseHullMod {
    protected static final float MISSILE_RANGE_BONUS = 1.5f;

    protected static final float GUIDED_OP = 3f;
    protected static final float TIME = 60f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.addListener(new HSILightTorpDesign.HSILightTorpDesignOPModifier());
    }

    @Override
    public boolean affectsOPCosts() {
        return  true;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        float frac = stats.getEnergyWeaponRangeBonus().computeEffective(1200f) / 1200f;
        stats.getMissileWeaponRangeBonus().modifyMult(id,frac*MISSILE_RANGE_BONUS);
        stats.getMissileMaxSpeedBonus().modifyMult(id, frac);
        stats.getMissileAccelerationBonus().modifyMult(id, frac);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0){
            return (int)(MISSILE_RANGE_BONUS*100f)+"%";
        }
        if(index==1)
            return (int)(GUIDED_OP*100f)+"%";
        return  null;
    }
}
