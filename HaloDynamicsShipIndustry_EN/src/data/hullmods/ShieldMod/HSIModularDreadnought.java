package data.hullmods.ShieldMod;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;

public class HSIModularDreadnought extends HSIBaseShieldModEffect {
    protected static String UkiyoA = "HSI_Ukiyo_TypeA";

    protected static String UkiyoB = "HSI_Ukiyo_TypeB";

    protected static String UkiyoC = "HSI_Ukiyo_TypeC";


    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        FleetMemberAPI member = stats.getFleetMember();
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id,0f);
        ShipHullSpecAPI orig = Global.getSettings().getHullSpec(stats.getVariant().getHullSpec().getBaseHullId());
        boolean shouldAddToCargo = isInPlayerFleet(stats);
        if(stats.getVariant().hasHullMod(UkiyoA)){
            if(!stats.getVariant().getHullSpec().getBaseHullId().equals("HSI_UkiyoA")) {
                if(member!=null) {
                    ShipVariantAPI v2 = member.getVariant().clone();
                    v2.setSource(VariantSource.REFIT);
                    v2.setHullVariantId(Misc.genUID());
                    member.setVariant(v2, false, false);
                }
                ShipVariantAPI v = stats.getVariant();
                ShipHullSpecAPI hull = Global.getSettings().getHullSpec("HSI_UkiyoA");
                checkNecessaryWeaponsRemove(orig, hull, v, shouldAddToCargo);
                v.setHullSpecAPI(hull);
                //v.getStationModules().clear();
                /*ShipVariantAPI module = Global.getSettings().getVariant("HSI_ModuleReplacement_Standard");
                v.setModuleVariant("M 01",module);
                v.setModuleVariant("M 03",module);
                v.setModuleVariant("M 02",module);*/
                //Global.getLogger(this.getClass()).info("Before:"+v.getStationModules().toString());
                /*v.getStationModules().put("M 01","HSI_ModuleReplacement_Standard");
                v.getStationModules().put("M 02","HSI_ModuleReplacement_Standard");
                v.getStationModules().put("M 03","HSI_ModuleReplacement_Standard");*/
                //Global.getLogger(this.getClass()).info("After:"+v.getStationModules().toString());
                v.clearHullMods();
            }else{
                stats.getVariant().removeMod(UkiyoA);
            }
        }
        if(stats.getVariant().hasHullMod(UkiyoB)){
            if(!stats.getVariant().getHullSpec().getBaseHullId().equals("HSI_UkiyoB")) {
                if(member!=null) {
                    ShipVariantAPI v2 = member.getVariant().clone();
                    v2.setSource(VariantSource.REFIT);
                    v2.setHullVariantId(Misc.genUID());
                    member.setVariant(v2, false, false);
                }
                ShipVariantAPI v = stats.getVariant();
                ShipHullSpecAPI hull = Global.getSettings().getHullSpec("HSI_UkiyoB");
                checkNecessaryWeaponsRemove(orig, hull, v, shouldAddToCargo);
                v.setHullSpecAPI(hull);
                /*ShipVariantAPI module = Global.getSettings().getVariant("HSI_ModuleReplacement_Standard");
                v.setModuleVariant("M 01",module);
                v.setModuleVariant("M 03",module);
                v.setModuleVariant("M 02",module);*/
                //Global.getLogger(this.getClass()).info("Before:"+v.getStationModules().toString());
               /* v.getStationModules().clear();
                v.getStationModules().put("M 01","HSI_ModuleReplacement_Standard");
                v.getStationModules().put("M 02","HSI_ModuleReplacement_Standard");
                v.getStationModules().put("M 03","HSI_ModuleReplacement_Standard");*/
                //Global.getLogger(this.getClass()).info("After:"+v.getStationModules().toString());
                //v.getStationModules().clear();
                v.clearHullMods();
            }else{
                stats.getVariant().removeMod(UkiyoB);
            }
        }
        if(stats.getVariant().hasHullMod(UkiyoC)){
            if(!stats.getVariant().getHullSpec().getBaseHullId().equals("HSI_UkiyoC")) {
                if(member!=null) {
                    ShipVariantAPI v2 = member.getVariant().clone();
                    v2.setSource(VariantSource.REFIT);
                    v2.setHullVariantId(Misc.genUID());
                    member.setVariant(v2, false, false);
                }
                ShipVariantAPI v = stats.getVariant();
                ShipHullSpecAPI hull = Global.getSettings().getHullSpec("HSI_UkiyoC");
                checkNecessaryWeaponsRemove(orig, hull, v, shouldAddToCargo);
                v.setHullSpecAPI(hull);
                /*v.getStationModules().clear();
                v.getStationModules().put("M 01","HSI_UkiyoC_ArmorTop_Standard");
                v.getStationModules().put("M 02","HSI_UkiyoC_ArmorR_Standard");
                v.getStationModules().put("M 03","HSI_UkiyoC_ArmorL_Standard");*/
                /*ShipVariantAPI module = Global.getSettings().getVariant("HSI_UkiyoC_ArmorTop_Standard");
                v.setModuleVariant("M 01",module);
                module = Global.getSettings().getVariant("HSI_UkiyoC_ArmorL_Standard");
                v.setModuleVariant("M 03",module);
                module = Global.getSettings().getVariant("HSI_UkiyoC_ArmorR_Standard");
                v.setModuleVariant("M 02",module);*/
                v.clearHullMods();
            }else{
                stats.getVariant().removeMod(UkiyoC);
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        /*if(ship.getHullSpec().getBaseHullId().equals("HSI_UkiyoC")&&Global.getCombatEngine()!=null){
            ship.setShipWithModules(true);
            ShipVariantAPI v = Global.getSettings().getVariant("HSI_UkiyoC_ArmorTop_Standard");
            ShipAPI module = Global.getCombatEngine().createFXDrone(v);
            module.setOwner(ship.getOwner());
            module.setAlly(ship.isAlly());
            module.setParentStation(ship);
            module.setStationSlot(ship.getVariant().getSlot("M 01"));
            Global.getCombatEngine().addEntity(module);

            v = Global.getSettings().getVariant("HSI_UkiyoC_ArmorR_Standard");
            module = Global.getCombatEngine().createFXDrone(v);
            module.setOwner(ship.getOwner());
            module.setAlly(ship.isAlly());
            module.setParentStation(ship);
            module.setStationSlot(ship.getVariant().getSlot("M 02"));
            Global.getCombatEngine().addEntity(module);

            v = Global.getSettings().getVariant("HSI_UkiyoC_ArmorL_Standard");
            module = Global.getCombatEngine().createFXDrone(v);
            module.setOwner(ship.getOwner());
            module.setAlly(ship.isAlly());
            module.setParentStation(ship);
            module.setStationSlot(ship.getVariant().getSlot("M 03"));
            Global.getCombatEngine().addEntity(module);
        }*/
    }

    private void checkNecessaryWeaponsRemove(ShipHullSpecAPI orig, ShipHullSpecAPI current, ShipVariantAPI variant, boolean shouldAddToCargo){
        for(String wing:variant.getWings()){
            if(shouldAddToCargo){
                if(Global.getSector()!=null&&Global.getSector().getPlayerFleet()!=null&&Global.getSector().getPlayerFleet().getCargo()!=null){
                    Global.getSector().getPlayerFleet().getCargo().addFighters(wing,1);
                }
            }
        }
        variant.getWings().clear();
        List<String> toCheck = new ArrayList<>();
        for(WeaponSlotAPI slot:current.getAllWeaponSlotsCopy()){
            toCheck.add(slot.getId());
        }
        for(WeaponSlotAPI slot:orig.getAllWeaponSlotsCopy()){
            if(!toCheck.contains(slot.getId())){
                //
                if(Global.getSector()!=null&&Global.getSector().getPlayerFleet()!=null&&Global.getSector().getPlayerFleet().getCargo()!=null){
                    String id = variant.getWeaponId(slot.getId());
                    if(id!=null) Global.getSector().getPlayerFleet().getCargo().addWeapons(id,1);
                }
                variant.clearSlot(slot.getId());
            }
        }

    }
}
