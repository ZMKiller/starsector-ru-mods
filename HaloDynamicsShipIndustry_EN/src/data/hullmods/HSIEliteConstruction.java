package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.hullmods.ShieldMod.HSIBaseShieldModEffect;

public class HSIEliteConstruction extends HSIBaseShieldModEffect {
    protected static float DEPLOYMENT_PUNISHMENT = 1.5f;

    protected static float ROF_BONUS = 1.2f;
    protected static float DAMAGE_BONUS = 1.2f;

    protected static float ARMOR_BOUNS = 1.2f;

    protected static float HITPOINT_BONUS = 1.2f;

    protected static float FLUX_CAP_BONUS = 1.5f;

    protected static float FLUX_DIS_BONUS = 1.5f;

    protected static float SPEED_BONUS = 10f;

    protected static float MANUVER_BONUS = 10f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id,DEPLOYMENT_PUNISHMENT);
        stats.getBallisticRoFMult().modifyMult(id,ROF_BONUS);
        stats.getEnergyRoFMult().modifyMult(id,ROF_BONUS);
        stats.getMissileRoFMult().modifyMult(id,ROF_BONUS);

        stats.getBallisticWeaponDamageMult().modifyMult(id,DAMAGE_BONUS);
        stats.getEnergyWeaponDamageMult().modifyMult(id,DAMAGE_BONUS);
        stats.getMissileWeaponDamageMult().modifyMult(id,DAMAGE_BONUS);

        stats.getArmorBonus().modifyMult(id,ARMOR_BOUNS);
        stats.getHullBonus().modifyMult(id,HITPOINT_BONUS);
        stats.getFluxCapacity().modifyMult(id,FLUX_CAP_BONUS);
        stats.getFluxDissipation().modifyMult(id,FLUX_DIS_BONUS);

        stats.getMaxSpeed().modifyFlat(id,SPEED_BONUS);

        stats.getAcceleration().modifyFlat(id,MANUVER_BONUS);
        stats.getDeceleration().modifyFlat(id,MANUVER_BONUS);
        stats.getMaxTurnRate().modifyPercent(id,MANUVER_BONUS);
        stats.getTurnAcceleration().modifyPercent(id,MANUVER_BONUS);
    }


}
