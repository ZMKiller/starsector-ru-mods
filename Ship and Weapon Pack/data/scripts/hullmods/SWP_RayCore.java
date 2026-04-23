package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.HighScatterAmp.HighScatterAmpDamageDealtMod;
import com.fs.starfarer.api.impl.hullmods.HighScatterAmp.HighScatterAmpRangeMod;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SWP_RayCore extends BaseHullMod {

    private static final String DATA_KEY = "SWP_RayCore";

    public static final float DAMAGE_MISSILES_PERCENT = 200f;
    public static final float DAMAGE_FIGHTERS_PERCENT = 100f;
    public static final float DAMAGE_SHIELDS_MULT = 2f / 3f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToMissiles().modifyPercent(id, DAMAGE_MISSILES_PERCENT);
        stats.getDamageToFighters().modifyPercent(id, DAMAGE_FIGHTERS_PERCENT);
        stats.getDamageToTargetShieldsMult().modifyMult(id, DAMAGE_SHIELDS_MULT);
        stats.getBeamWeaponTurnRateBonus().modifyMult(id, 2f);
        stats.getAutofireAimAccuracy().modifyFlat(id, 1f);
        stats.getEngineDamageTakenMult().modifyMult(id, 0f);
        stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
        stats.getDynamic().getMod(Stats.PD_BEST_TARGET_LEADING).modifyFlat(id, 1f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        List weapons = ship.getAllWeapons();
        Iterator iter = weapons.iterator();
        while (iter.hasNext()) {
            WeaponAPI weapon = (WeaponAPI) iter.next();
            if (weapon.getSize() == WeaponAPI.WeaponSize.SMALL) {
                weapon.setPD(true);
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) {
            return;
        }

        LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            Global.getCombatEngine().getCustomData().put(DATA_KEY, localData);
        }

        IntervalUtil timer = localData.checkTimers.get(ship);
        if (timer == null) {
            timer = new IntervalUtil(0.16f, 0.24f);
            localData.checkTimers.put(ship, timer);
        }

        timer.advance(amount);
        if (timer.intervalElapsed()) {
            ShipAPI mothership = ship.getDroneSource();
            if (mothership != null) {
                ship.getMutableStats().getEnergyWeaponRangeBonus().applyMods(mothership.getMutableStats().getEnergyWeaponRangeBonus());
                ship.getMutableStats().getBeamWeaponRangeBonus().applyMods(mothership.getMutableStats().getBeamWeaponRangeBonus());
                ship.getMutableStats().getWeaponRangeThreshold().applyMods(mothership.getMutableStats().getWeaponRangeThreshold());
                ship.getMutableStats().getWeaponRangeMultPastThreshold().applyMods(mothership.getMutableStats().getWeaponRangeMultPastThreshold());
                ship.getMutableStats().getBeamPDWeaponRangeBonus().applyMods(mothership.getMutableStats().getBeamPDWeaponRangeBonus());
                ship.getMutableStats().getNonBeamPDWeaponRangeBonus().applyMods(mothership.getMutableStats().getNonBeamPDWeaponRangeBonus());
                if (mothership.hasListenerOfClass(HighScatterAmpDamageDealtMod.class) && !ship.hasListenerOfClass(HighScatterAmpDamageDealtMod.class)) {
                    ship.addListener(new HighScatterAmpDamageDealtMod(ship));
                } else if (!mothership.hasListenerOfClass(HighScatterAmpDamageDealtMod.class) && ship.hasListenerOfClass(HighScatterAmpDamageDealtMod.class)) {
                    ship.removeListenerOfClass(HighScatterAmpDamageDealtMod.class);
                }
                if (mothership.hasListenerOfClass(HighScatterAmpRangeMod.class) && !ship.hasListenerOfClass(HighScatterAmpRangeMod.class)) {
                    ship.addListener(new HighScatterAmpRangeMod());
                } else if (!mothership.hasListenerOfClass(HighScatterAmpRangeMod.class) && ship.hasListenerOfClass(HighScatterAmpRangeMod.class)) {
                    ship.removeListenerOfClass(HighScatterAmpRangeMod.class);
                }
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) DAMAGE_MISSILES_PERCENT + "%";
        }
        if (index == 1) {
            return "" + (int) DAMAGE_FIGHTERS_PERCENT + "%";
        }
        if (index == 2) {
            return "" + (int) Math.round((1f - DAMAGE_SHIELDS_MULT) * 100f) + "%";
        }
        if (index == 3) {
            return "High Scatter Amplifier";
        }
        return null;
    }

    private static final class LocalData {

        final Map<ShipAPI, IntervalUtil> checkTimers = new LinkedHashMap<>(50);
    }
}
