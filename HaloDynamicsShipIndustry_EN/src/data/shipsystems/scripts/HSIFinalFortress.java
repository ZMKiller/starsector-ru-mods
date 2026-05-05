package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.combat.AIUtils;

import java.util.HashMap;
import java.util.Map;

public class HSIFinalFortress extends BaseShipSystemScript {
    private ShipAPI ship;

    private static float RANGE = 3000;

    protected static float DAMAGE_REDUCTION = 50f;

    protected static float ROF_REDUCTION = 50f;

    private float ARMOR_CAL = 0f;

    protected static Map<ShipAPI.HullSize,Float> armorLevel = new HashMap<>();
    static {
        armorLevel.put(ShipAPI.HullSize.FIGHTER,3f);
        armorLevel.put(ShipAPI.HullSize.FRIGATE,8f);
        armorLevel.put(ShipAPI.HullSize.DESTROYER,11f);
        armorLevel.put(ShipAPI.HullSize.CRUISER,15f);
        armorLevel.put(ShipAPI.HullSize.CAPITAL_SHIP,20f);

    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(stats.getEntity() instanceof ShipAPI){
            ship = (ShipAPI) stats.getEntity();
        }else{
            return;
        }
        ARMOR_CAL = 0;
        for(ShipAPI enemies: AIUtils.getNearbyEnemies(ship,RANGE)){
            enemies.setShipTarget(ship);
            ARMOR_CAL+=armorLevel.get(enemies.getHullSize());
        }
        stats.getMaxArmorDamageReduction().modifyFlat(id,0.1f*effectLevel);
        stats.getMinArmorFraction().modifyFlat(id,0.15f*effectLevel);
        stats.getHullDamageTakenMult().modifyMult(id,1f-(DAMAGE_REDUCTION*effectLevel/100f));
        stats.getArmorDamageTakenMult().modifyMult(id,1f-(DAMAGE_REDUCTION*effectLevel/100f));
        //stats.getShieldDamageTakenMult().modifyMult(id,1f-(DAMAGE_REDUCTION*effectLevel/100f));
        stats.getEnergyRoFMult().modifyMult(id,(1f-ROF_REDUCTION*effectLevel/100f));
        stats.getBallisticRoFMult().modifyMult(id,(1f-ROF_REDUCTION*effectLevel/100f));
        stats.getMissileRoFMult().modifyMult(id,(1f-ROF_REDUCTION*effectLevel/100f));
        for(MissileAPI missile:AIUtils.getNearbyEnemyMissiles(ship,1500f+ship.getCollisionRadius())){
            if(missile.isFizzling()||missile.isExpired()||!missile.isGuided()) continue;
            if(missile.getOwner()==ship.getOwner()) continue;
            if(missile.getMissileAI() instanceof GuidedMissileAI){
                ((GuidedMissileAI) missile.getMissileAI()).setTarget(ship);
            }

        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if(stats.getEntity() instanceof ShipAPI){
            ship = (ShipAPI) stats.getEntity();
        }else{
            return;
        }
        stats.getMaxArmorDamageReduction().unmodify(id);
        stats.getMinArmorFraction().unmodify(id);
        stats.getMinArmorFraction().modifyFlat(id+"_Passive",0.05f);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);

        stats.getEnergyRoFMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);

        //stats.getEffectiveArmorBonus().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if(index == 0){
            return new StatusData("" + HSII18nUtil.getShipSystemString("HSIFF0") + (int)(ROF_REDUCTION*effectLevel)+"%", false);
        }
        if(index == 1){
            return new StatusData("" + HSII18nUtil.getShipSystemString("HSIFF1") + (int)(DAMAGE_REDUCTION*effectLevel)+"%", false);
        }
        if(index == 2){
            return new StatusData("" + HSII18nUtil.getShipSystemString("HSIFF2") + (int)ARMOR_CAL+"%", false);
        }
        return null;
    }

}
