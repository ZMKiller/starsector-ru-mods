package data.hullmods.LogisticsMod;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.kit.HSII18nUtil;

public class HSICombatAutomateRepairSystem extends HSIBaseLogisticsMod {
    protected static final float REPAIR_SPEED = 50f;
    protected static final float REPAIR = 200f;
    protected static final float REPAIR_UPLIM = 10000f;
    protected static final float REPAIR_DOWNLIM = 1500f;

    @Override
    public boolean isLimitedMod() {
        return true;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        stats.getCombatWeaponRepairTimeMult().modifyPercent(id,REPAIR_SPEED);
        stats.getCombatEngineRepairTimeMult().modifyPercent(id,REPAIR_SPEED);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused())
            return;
        HSICombatAutomateRepairSystemManager manager = HSICombatAutomateRepairSystemManager.getInstance(ship);
        manager.tryRepair(ship, amount);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if(index == 0){
            return (int)REPAIR+"%";
        }
        if(index == 1){
            return ""+REPAIR_DOWNLIM;
        }
        if(index == 2){
            return ""+REPAIR_UPLIM;
        }
        if(index == 3){
            return "1%";
        }
        if(index == 4){
            return (int)REPAIR_SPEED+"%";
        }
        return null;
    }

    public static class HSICombatAutomateRepairSystemManager {
        private float repair;
        private float left;

        public HSICombatAutomateRepairSystemManager(ShipAPI ship) {
            repair = Math.min(ship.getArmorGrid().getArmorRating() * (REPAIR / 100f),REPAIR_UPLIM);
            repair = Math.max(repair,REPAIR_DOWNLIM);
            left = repair;
        }

        public void tryRepair(ShipAPI ship, float amount) {
            int weaponRepairing = 0;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.isDisabled()) {
                    //weapon.setCurrHealth(Math.min(0.99f,weapon.getMaxHealth() * amount * 0.05f + weapon.getCurrHealth()));
                    weaponRepairing++;
                }
            }
            boolean repaired = false;
            float repairFrame = Math.min(amount * repair * 0.01f, left);
            if (repairFrame <= 0) {
                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    addStatus(weaponRepairing, repaired);
                }
                return;
            }
            ArmorGridAPI a = ship.getArmorGrid();
            if (a == null)
                return;
            float[][] cag = a.getGrid();
            if (cag.length == 0)
                return;
            for (int i = 0; i < cag.length; i++) {
                for (int j = 0; j < cag[i].length; j++) {
                    float toRepair = Math.min(repairFrame, a.getMaxArmorInCell() - cag[i][j]);
                    toRepair = Math.max(0,toRepair);
                    a.setArmorValue(i, j, cag[i][j] + toRepair);
                    left -= toRepair;
                    repairFrame -= toRepair;
                    if (toRepair > 0)
                        repaired = true;
                }
            }
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                addStatus(weaponRepairing, repaired);
            }
        }

        protected void addStatus(int weaponRepairing, boolean armorRepairing) {
            String content = HSII18nUtil.getHullModString("HSICombatAutomateRepirSystemWeapon")+weaponRepairing;
            Global.getCombatEngine().maintainStatusForPlayerShip("HSICombatAutomateRepirSystemManager_Key0",
                    "graphics/icons/hullsys/damper_field.png", HSII18nUtil.getHullModString("HSICombatAutomateRepirSystemManager_Title"), content,
                    weaponRepairing>0);
            content = HSII18nUtil.getHullModString("HSICombatAutomateRepirSystemArmor")+String.format("%.1f", left);
            Global.getCombatEngine().maintainStatusForPlayerShip("HSICombatAutomateRepirSystemManager_Key1",
                    "graphics/icons/hullsys/damper_field.png", HSII18nUtil.getHullModString("HSICombatAutomateRepirSystemManager_Title"), content,
                    left<=0);
        }

        public static HSICombatAutomateRepairSystemManager getInstance(ShipAPI ship) {
            if (ship.getCustomData() != null
                    && ship.getCustomData().containsKey("HSICombatAutomateRepirSystemManager_Key")) {
                return (HSICombatAutomateRepairSystemManager) ship.getCustomData()
                        .get("HSICombatAutomateRepirSystemManager_Key");
            } else {
                HSICombatAutomateRepairSystemManager manager = new HSICombatAutomateRepairSystemManager(ship);
                ship.setCustomData("HSICombatAutomateRepirSystemManager_Key", manager);
                return manager;
            }
        }
    }
}
