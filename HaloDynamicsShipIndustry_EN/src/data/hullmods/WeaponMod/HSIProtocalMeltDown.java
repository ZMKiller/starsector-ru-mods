package data.hullmods.WeaponMod;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import data.kit.HSII18nUtil;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicIncompatibleHullmods;

public class HSIProtocalMeltDown extends HSIBaseWeaponModEffect {
    protected static final float SHIELD_REDUCTION = 0.15f;
    public static final float RANGE_THRESHOLD = 500f;
    public static final float RANGE_MULT = 0.5f;
    public static final float RANGE_REDUCTION_MAX = 400f;

    public static final float BEAM_WEAPON_DAMAGE = 10f;

    public static final float BEAM_WEAPON_EFFECT_BONUS = 100f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //if(stats.getVariant().hasHullMod(HullMods.HIGH_SCATTER_AMP))
        //        MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), HullMods.HIGH_SCATTER_AMP, HSII18nUtil.getHullModString("HSI_ProtocalMeltdown_NoAMP"));
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        /*ship.addListener(new HSIProtocalMeltDownRangeMod());
        ship.addListener(new HSIProtocalMeltDownDamageDealtMod());
        if(isSMod(ship)){*/
            float Hop = 0;
            float op = 0;
            for(WeaponAPI weapon:ship.getAllWeapons()){
                float wop = weapon.getSpec().getOrdnancePointCost((ship.getCaptain()!=null)?ship.getCaptain().getStats():null,ship.getMutableStats());
                op+=wop;
                if(weapon.getSpec().getWeaponId().startsWith("HSI_")||weapon.getSpec().getWeaponId().startsWith("HWI_")){
                    if(weapon.isBeam()) Hop+=wop;
                }
            }
            float frac = 0;
            if(op ==0){
                frac = 0;
            }
            else {
                frac = Hop/op;
            }
            ship.getMutableStats().getBeamWeaponDamageMult().modifyPercent(ship.getId()+"_"+spec.getId()+"_S",frac*BEAM_WEAPON_DAMAGE);
        //}
    }


    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize,ShipAPI ship) {
        if(index==0) return (int)BEAM_WEAPON_DAMAGE+"%";
        return  null;
    }

    @Override
    public boolean hasSModEffect() {
        return false;
    }

    @Override
    public boolean hasSModEffectSection(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void upgrade(MutableShipStatsAPI stats) {
        super.upgrade(stats);
    }


    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if(index==0) return (int)BEAM_WEAPON_DAMAGE+"%";
        if(index==1) return (int)BEAM_WEAPON_EFFECT_BONUS+"%";
        if(index == 2){
            if(ship==null) return "--";
            float op = 0;
            float Hop = 0;
            for(WeaponAPI weapon:ship.getAllWeapons()){
                float wop = weapon.getSpec().getOrdnancePointCost((ship.getCaptain()!=null)?ship.getCaptain().getStats():null,ship.getMutableStats());
                op+=wop;
                if(weapon.getSpec().getWeaponId().startsWith("HSI_")||weapon.getSpec().getWeaponId().startsWith("HWI_")){
                    if(weapon.isBeam()) Hop+=wop;
                }
            }
            float frac = 0;
            if(op ==0){
                frac = 0;
            }
            else {
                frac = Hop/op;
            }
            return (int)(frac*100)+"%";
        }
        return null;
    }

    public static class HSIProtocalMeltDownDamageDealtMod implements DamageDealtModifier {
        protected ShipAPI ship;

        public String modifyDamageDealt(Object param,
                                        CombatEntityAPI target, DamageAPI damage,
                                        Vector2f point, boolean shieldHit) {
            if (param instanceof BeamAPI) {
                damage.setForceHardFlux(true);
                if(shieldHit) {
                    damage.getModifier().modifyMult("HSI_ProtocalMeltdown", 1 - SHIELD_REDUCTION);
                }else{
                    damage.getModifier().unmodify("HSI_ProtocalMeltdown");
                }
            }
            return null;
        }
    }

    public static boolean hasProtocalMeltDown(ShipAPI ship){
        return ship.getVariant().hasHullMod("HSI_ProtocalMeltDown");
    }

    public static float getMult(){
        return ((100f+BEAM_WEAPON_EFFECT_BONUS)/100f);
    }

    public static class HSIProtocalMeltDownRangeMod implements WeaponBaseRangeModifier {

        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0;
        }
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (weapon.isBeam()) {
                float range = weapon.getSpec().getMaxRange();
                if (range < RANGE_THRESHOLD) return 0;

                float past = range - RANGE_THRESHOLD;
                float penalty = Math.min(past * (1f - RANGE_MULT),RANGE_REDUCTION_MAX);
                return -penalty;
            }
            return 0f;
        }
    }
}
