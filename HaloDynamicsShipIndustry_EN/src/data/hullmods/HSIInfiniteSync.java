package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.HSIGenerator.HSIDreadnought;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.combat.AIUtils;

public class HSIInfiniteSync extends BaseHullMod {

    public static final String KEY = "HSI_Infinite_Sync_Modification";

    private String nameSelf = "";
    private String nameElse = "";

    private Logger logger;

    private boolean once = false;
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(ship.getOwner() == 0&&ship.getOriginalOwner()==1){
            ship.setOwner(1);
            if(Global.getCombatEngine().getPlayerShip()!=null){
                Global.getCombatEngine().getPlayerShip().getFluxTracker().forceOverload(30f);
            }
        }
        if(ship.getOwner()!=1) return;
        logger = Global.getLogger(this.getClass());
        float judge = amount*60f;
        nameSelf = ship.getName();
        int num = 0;
        if(ship.isRetreating()){
            ship.setRetreating(false,false);
        }
        if(Math.random()>(Math.pow(0.75f,judge))){
            MutableShipStatsAPI self = ship.getMutableStats();
            self.getTimeMult().unmodify(KEY);
            for(ShipAPI enemy:AIUtils.getEnemiesOnMap(ship)){
                once = num == 0;
                MutableShipStatsAPI e = enemy.getMutableStats();
                if(e.getTimeMult().getModifiedValue()>5f){
                        if (enemy.getPhaseCloak() == null || !enemy.getPhaseCloak().getSpecAPI().getId().equals("phasecloak")) {
                            syncPositive(self.getTimeMult(),e.getTimeMult());
                        }

                }else{
                    if(enemy.isFighter()) continue;
                    if(enemy.isStationModule()) continue;
                    if(enemy.getSystem()!=null&&enemy.getSystem().isOn()) continue;
                    if(enemy.getPhaseCloak()!=null&&enemy.getPhaseCloak().isOn()) continue;
                }
                num++;
                nameElse = enemy.getName();

                syncPositive(self.getMaxSpeed(),e.getMaxSpeed());
                syncPositive(self.getAcceleration(),e.getAcceleration());
                syncPositive(self.getDeceleration(),e.getDeceleration());
                syncPositive(self.getTurnAcceleration(),e.getTurnAcceleration());
                syncPositive(self.getMaxTurnRate(),e.getTurnAcceleration());
                syncPositive(self.getCombatEngineRepairTimeMult(),e.getCombatEngineRepairTimeMult());
                syncPositive(self.getCombatWeaponRepairTimeMult(),e.getCombatWeaponRepairTimeMult());

                syncPositive(self.getDamageToFighters(),e.getDamageToFighters());
                syncPositive(self.getDamageToFrigates(),e.getDamageToFrigates());
                syncPositive(self.getDamageToDestroyers(),e.getDamageToDestroyers());
                syncPositive(self.getDamageToCruisers(),e.getDamageToCruisers());
                syncPositive(self.getDamageToCapital(),e.getDamageToCapital());
                syncPositive(self.getDamageToMissiles(),e.getDamageToMissiles());

                syncPositive(self.getBallisticRoFMult(),e.getBallisticRoFMult());
                syncPositive(self.getBallisticWeaponDamageMult(),e.getBallisticWeaponDamageMult());

                syncPositive(self.getEnergyRoFMult(),e.getEnergyRoFMult());
                syncPositive(self.getEnergyWeaponDamageMult(),e.getEnergyWeaponDamageMult());

                syncPositive(self.getMissileRoFMult(),e.getMissileRoFMult());
                syncPositive(self.getMissileWeaponDamageMult(),e.getMissileWeaponDamageMult());

                syncNegative(self.getHullDamageTakenMult(),e.getHullDamageTakenMult());
                syncNegative(self.getArmorDamageTakenMult(),e.getArmorDamageTakenMult());
                syncNegative(self.getShieldDamageTakenMult(),e.getShieldDamageTakenMult());

                syncNegative(self.getBeamDamageTakenMult(),e.getBeamDamageTakenMult());
                syncNegative(self.getProjectileDamageTakenMult(),e.getProjectileDamageTakenMult());
                syncNegative(self.getEnergyDamageTakenMult(),e.getEnergyDamageTakenMult());
                syncNegative(self.getHighExplosiveDamageTakenMult(),e.getHighExplosiveDamageTakenMult());
                syncNegative(self.getKineticDamageTakenMult(),e.getKineticDamageTakenMult());
            }
        }
        if(num>=Global.getSettings().getMaxShipsInFleet()*1.33f&&!Global.getCombatEngine().getCustomData().containsKey("HSIDreadnoughtOverNumAdded")){
            Global.getCombatEngine().addPlugin(new HSIDreadnought.HSIDreadnoughtOverNumArtilleryPlugin(ship));
            Global.getCombatEngine().getCustomData().put("HSIDreadnoughtOverNumAdded",true);
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id,0.5f);
        stats.getCRPerDeploymentPercent().modifyMult(id,0f);
        stats.getCRLossPerSecondPercent().modifyMult(id,0f);
    }

    private void syncPositive(MutableStat s1, MutableStat s2){
        if(once) s1.unmodify(KEY);
        if(s1.getMult()<s2.getMult()){
            s1.modifyMult(KEY,s2.getMult()/s1.getMult());
            //logger.info(nameElse+"'s mult higher at "+s2.getMult());
        }
        if(s1.getPercentMod()<s2.getPercentMod()){
            s1.modifyPercent(KEY,s2.getPercentMod()-s1.getPercentMod());
            //logger.info(nameElse+"'s percent higher at "+s2.getPercentMod());
        }
        if(s1.getFlatMod()<s2.getFlatMod()){
            s1.modifyFlat(KEY,s2.getFlatMod()-s1.getFlatMod());
            //logger.info(nameElse+"'s flat higher at "+s2.getPercentMod());
        }
    }

    private void syncNegative(MutableStat s1,MutableStat s2){
        if(once) s1.unmodify(KEY);
        if(s1.getMult()>s2.getMult()){
            s1.modifyMult(KEY,s2.getMult()/s1.getMult());
            //logger.info(nameElse+"'s mult lower at "+s2.getMult()+" while self is "+s1.getMult());
        }
        if(s1.getPercentMod()>s2.getPercentMod()){
            s1.modifyPercent(KEY,s2.getPercentMod()-s1.getPercentMod());
            //logger.info(nameElse+"'s percent lower at "+s2.getPercentMod()+" while self is "+s1.getPercentMod());
        }
        if(s1.getFlatMod()>s2.getFlatMod()){
            s1.modifyFlat(KEY,s2.getFlatMod()-s1.getFlatMod());
            //logger.info(nameElse+"'s flat lower at "+s2.getFlatMod()+" while self is "+s1.getFlatMod());
        }
    }

}
