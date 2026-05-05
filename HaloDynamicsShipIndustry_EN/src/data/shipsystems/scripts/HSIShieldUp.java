package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class HSIShieldUp extends BaseShipSystemScript {
    private WeaponAPI shield;
    private ShipAPI ship;

    private static float DMG_REDUCTION = 0.3f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(stats.getEntity() instanceof ShipAPI){
            ship = (ShipAPI) stats.getEntity();
        }else{
            return;
        }
        if(ship!=null&&shield==null){
            for(WeaponAPI weapon:ship.getAllWeapons()){
                if(!weapon.isDecorative()) continue;
                if(weapon.getSpec().getWeaponId().equals("HSI_Arbaletrier_L")){
                    shield = weapon;
                    break;
                }
            }
        }
        if(ship == null||shield == null) return;
        float mult = 1f;
        //if(ship.getHullSpec().getHullId().equals("HSI_Arbaletrier_WingMan")) mult = 0.5f;
        shield.setCurrAngle(Misc.normalizeAngle(ship.getFacing()-45f*effectLevel));
        stats.getHullDamageTakenMult().modifyMult(id,(1f-DMG_REDUCTION*mult*effectLevel));
        stats.getArmorDamageTakenMult().modifyMult(id,(1f-DMG_REDUCTION*mult*effectLevel));
        stats.getEnergyWeaponRangeBonus().modifyMult(id,1f+(0.5f*effectLevel));
        stats.getProjectileSpeedMult().modifyMult(id,1f+(0.33f*effectLevel));
        stats.getMaxSpeed().modifyMult(id,(1f-0.9f*effectLevel));
    }


    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getProjectileSpeedMult().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        if(stats.getEntity() instanceof  ShipAPI){
            ship = (ShipAPI) stats.getEntity();
        }
        if(ship!=null&&shield==null){
            for(WeaponAPI weapon:ship.getAllWeapons()){
                if(!weapon.isDecorative()) continue;
                if(weapon.getSpec().getWeaponId().equals("HSI_Arbaletrier_L")){
                    shield = weapon;
                    break;
                }
            }
        }
    }
}
