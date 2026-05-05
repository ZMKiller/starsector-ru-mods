package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

import data.scripts.HSIRenderer.HSICombatRenderer;
import data.weapons.scripts.proj.HWIPhenomenonEffect;

public class HSIDrift extends BaseHullMod {
    public static float SPD = 30f;
    public static float MANUVER = 50f;
    public static float ENERGY_ROF = 20f;

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getCustomData().containsKey(HWIPhenomenonEffect.KEY)) {
            DamagingProjectileAPI p = (DamagingProjectileAPI) ship.getCustomData().get(HWIPhenomenonEffect.KEY);
            if (p != null && !p.isExpired() && !p.isFading() && Global.getCombatEngine().isEntityInPlay(p)) {
                float dist = Misc.getDistance(p.getLocation(), ship.getLocation());
                if (dist <= 600f) {
                    float frac = 1;
                    if (dist <= 300f) {

                    } else {
                        frac = 1 - (dist - 300) / 300f;
                    }
                    ship.getMutableStats().getMaxSpeed().modifyFlat(HWIPhenomenonEffect.KEY, frac * SPD);
                    ship.getMutableStats().getMaxTurnRate().modifyPercent(HWIPhenomenonEffect.KEY, frac * MANUVER);
                    ship.getMutableStats().getTurnAcceleration().modifyPercent(HWIPhenomenonEffect.KEY, frac * MANUVER);
                    ship.getMutableStats().getAcceleration().modifyPercent(HWIPhenomenonEffect.KEY, frac * MANUVER);
                    ship.getMutableStats().getDeceleration().modifyPercent(HWIPhenomenonEffect.KEY, frac * MANUVER);
                    ship.getMutableStats().getEnergyRoFMult().modifyPercent(HWIPhenomenonEffect.KEY, frac * ENERGY_ROF);
                    ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyMult(HWIPhenomenonEffect.KEY,
                            1 - frac * ENERGY_ROF / 100f);
                    return;
                }
            }
        }
        ship.getMutableStats().getMaxSpeed().unmodify(HWIPhenomenonEffect.KEY);
        ship.getMutableStats().getMaxTurnRate().unmodify(HWIPhenomenonEffect.KEY);
        ship.getMutableStats().getTurnAcceleration().unmodify(HWIPhenomenonEffect.KEY);
        ship.getMutableStats().getAcceleration().unmodify(HWIPhenomenonEffect.KEY);
        ship.getMutableStats().getDeceleration().unmodify(HWIPhenomenonEffect.KEY);
        ship.getMutableStats().getEnergyRoFMult().unmodify(HWIPhenomenonEffect.KEY);
        ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(HWIPhenomenonEffect.KEY);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) SPD;
        if (index == 1)
            return "" + (int) (MANUVER) + "%";
        if (index == 2)
            return "" + (int) (ENERGY_ROF) + "%";
        if (index == 3)
            return "" + (int) (ENERGY_ROF) + "%";
        return null;
    }
}
