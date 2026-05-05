package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import org.lwjgl.util.vector.Vector2f;

public class HSILeviathanHunter extends BaseHullMod {
    protected  static  final float RANGE_BOUNS = 1.1f;
    protected static final float DAMAGE_BONUS = 10f;

    protected static final float SPEED_BONUS = 30f;

    protected static final float MANUVER_BONUS = 30f;
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(ship.getSystem()!=null&&ship.getSystem().isActive()){
            ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult("HSI_LeviathanHunter",RANGE_BOUNS);
            ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult("HSI_LeviathanHunter",RANGE_BOUNS);
            ship.getMutableStats().getDamageToCapital().modifyPercent("HSI_LeviathanHunter",DAMAGE_BONUS);
            ship.getMutableStats().getDamageToCruisers().modifyPercent("HSI_LeviathanHunter",DAMAGE_BONUS);
        }else{
            ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify("HSI_LeviathanHunter");
            ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify("HSI_LeviathanHunter");
            ship.getMutableStats().getDamageToCapital().unmodify("HSI_LeviathanHunter");
            ship.getMutableStats().getDamageToCruisers().unmodify("HSI_LeviathanHunter");
        }
        if(!ship.getMutableStats().getWeaponRangeMultPastThreshold().isUnmodified()) {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                float effectLevel = 1f;
                if (!weapon.isDecorative())
                    continue;
                switch (weapon.getId()) {
                    case "HWI_Comet_Deco2":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(effectLevel * -12f, 0));
                        break;
                    case "HWI_Comet_Deco3":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(effectLevel * -12f, 0));
                        break;
                    case "HWI_Comet_Deco4":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, effectLevel * 3f));
                        break;
                    case "HWI_Comet_Deco5":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, effectLevel * -3f));
                        break;
                    case "HWI_Comet_Deco6":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(3f, (effectLevel - 0.75f) * 2f));
                        break;
                    case "HWI_Comet_Deco7":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(3f, (effectLevel - 0.75f) * -2f));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if(!ship.getMutableStats().getWeaponRangeMultPastThreshold().isUnmodified()){
            ship.getMutableStats().getMaxSpeed().modifyFlat(id,SPEED_BONUS);
            ship.getMutableStats().getAcceleration().modifyPercent(id,MANUVER_BONUS);
            ship.getMutableStats().getDeceleration().modifyPercent(id,MANUVER_BONUS);
            ship.getMutableStats().getMaxTurnRate().modifyPercent(id,MANUVER_BONUS);
            ship.getMutableStats().getTurnAcceleration().modifyPercent(id,MANUVER_BONUS);
        }

    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToCapital().modifyPercent(id,DAMAGE_BONUS);
        stats.getDamageToCruisers().modifyPercent(id,DAMAGE_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0)
            return (int) (RANGE_BOUNS*100f)+"%";
        if (index == 1)
            return (int) DAMAGE_BONUS+"%";
        if(index==2)
            return Global.getSettings().getHullModSpec(HullMods.SAFETYOVERRIDES).getDisplayName();
        if(index == 3)
            return (int)SPEED_BONUS+"";
        if(index == 4)
            return (int)MANUVER_BONUS+"%";
        return null;
    }
}
