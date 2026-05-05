package data.hullmods.WeaponMod;

import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;

import java.util.ArrayList;
import java.util.List;

public class HSIBaseWeaponModEffect extends BaseHullMod {
    protected static List<String> Incompatible = new ArrayList<>();
    static {
        Incompatible.add("HSI_ProtocalMeltDown");
        Incompatible.add("HSI_ProtocalAssault");
        Incompatible.add("HSI_ProtocalSuppression");
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship.getHullSpec().getBaseHullId().startsWith("HSI_");
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship)&&ship.getHullSpec().getBaseHullId().startsWith("HSI_")&&!AjimusUtils.hasIncompatible(Incompatible,spec,ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if(!super.isApplicableToShip(ship)) {
            return super.getUnapplicableReason(ship);
        }else{
            return HSII18nUtil.getHullModString("HSIWeaponModIncompatible");
        }
    }

    @Override
    public boolean hasSModEffectSection(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return true;
    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        upgrade(stats);
    }

    public void upgrade(MutableShipStatsAPI stats){

    }

    protected String getStandardId(ShipAPI ship){
        return  ship.getId() + spec.getId();
    }
}
