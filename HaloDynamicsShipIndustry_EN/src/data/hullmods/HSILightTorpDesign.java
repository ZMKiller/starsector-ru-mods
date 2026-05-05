package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;
import org.magiclib.util.MagicIncompatibleHullmods;

public class HSILightTorpDesign extends BaseHullMod {
    protected static final float GUIDED_OP = 3f;
    protected static final float TIME = 60f;
    public static class HSILightTorpDesignOPModifier implements WeaponOPCostModifier{
        public HSILightTorpDesignOPModifier(){

        }
        @Override
        public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI spec, int op) {
            float fop = op;
                if(spec.getProjectileSpec() instanceof MissileSpecAPI) {
                    MissileSpecAPI m = (MissileSpecAPI) spec.getProjectileSpec();
                    if (AjimusUtils.isGuidedMissile(m.getTypeString())) {
                        fop = (1 + GUIDED_OP) * op;
                    }
                }
            //Global.getLogger(this.getClass()).info(spec.getWeaponId()+" op-"+fop);
            return (int)fop;
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.addListener(new HSILightTorpDesignOPModifier());

    }

    @Override
    public boolean affectsOPCosts() {
        return  true;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for(WeaponAPI weapon: ship.getAllWeapons()){
            if(weapon.getSlot().getWeaponType().equals(WeaponAPI.WeaponType.MISSILE)){
                if(weapon.usesAmmo()){
                    if(weapon.getAmmoTracker().getAmmoPerSecond()>0){
                        int increase = Math.round(weapon.getAmmoPerSecond()*TIME);
                        weapon.getAmmoTracker().setAmmoPerSecond(0f);
                        weapon.setAmmo(weapon.getAmmo()+increase);
                    }
                }
            }
        }
        if(ship.getVariant().hasHullMod(HullMods.MISSLERACKS)){
            MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), HullMods.MISSLERACKS, spec.getId());
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index==0)
            return (int)(GUIDED_OP*100f)+"%";
        if(index == 1) return (int)TIME+"";
        return  null;
    }
}
