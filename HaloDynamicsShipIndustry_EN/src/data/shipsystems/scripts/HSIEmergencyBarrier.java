package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSIEmergencyBarrier extends BaseShipSystemScript {
    private ShipAPI ship;
    // private CombatEngineAPI engine = Global.getCombatEngine();
    private HSITurbulanceShieldListenerV2 shield = null;
    public static final float BARRIER = 0.2f;
    public static final float REG_BUFF = 25f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (shield == null) {
            if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
                shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
            }
        } else {
            if (effectLevel >= 1) {
                shield.getShield().addExtraShield(shield.getShield().getShieldCap() * BARRIER);
                float time = (7f+shield.getRenderData().getSpreadLevel())*14f/24.5f;
                shield.getRenderData().setReverseSpreadForTime(true,time);
                /*if(shield.getShield().isShieldRegenBlocked()){
                    shield.getShield().getRegenCooldownTimer().setBrightness(1f);
                }*/
            }
        }
        //if(effectLevel>0&&shield!=null){
        //    shield.getRenderData().advance(-Global.getCombatEngine().getElapsedInLastFrame()*8f);
        //}
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (shield == null) shield = HSITurbulanceShieldListenerV2.getInstance(ship);
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (shield == null) shield = HSITurbulanceShieldListenerV2.getInstance(ship);
        return !(shield.getShield().getExtra() + shield.getShield().getShieldCap() * BARRIER > shield.getShield().getExtraShieldCap());
    }

    /*public StatusData getStatusData(int index, State state, float effectLevel) {
        // float percent = (1f - ENERGY_DAM_PENALTY_MULT) * effectLevel * 100;
        if (index == 0) {
            if(ship==null) return null;
            if (shield == null) {
                if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
                    shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
                }
            }
            if(shield!=null){
                return new StatusData((int) REG_BUFF * (1 - shield.getShield().getCurrent() / shield.getShield().getShieldCap())
                            + HSII18nUtil.getShipSystemString("HSIEmergencyBarrierStatus"), false);
            }
        }
        return null;
    }*/

}
