package data.hullmods;

import com.fs.starfarer.api.combat.*;

public class HSIArbaletrier extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEmpDamageTakenMult().modifyMult(id,0.33f);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id,0.33f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(ship.getHullSpec().getHullId().equals("HSI_Arbaletrier_WingMan")){
            ShipAPI leader = null;
            if(ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP)&&ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP) instanceof ShipAPI){
                leader = (ShipAPI) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP);
            }
            if(leader == null) return;
            if(leader.getPhaseCloak().isOn()&&!ship.getPhaseCloak().isOn()){
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK,null,0);
                //Global.getLogger(this.getClass()).info("Leader active.Activating.");
            }else if(ship.getPhaseCloak().isActive()&&(leader.getPhaseCloak().isChargedown()||!leader.getPhaseCloak().isOn())){
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK,null,0);
                //Global.getLogger(this.getClass()).info("Leader deactive.Deactivating.");
            }
            return;
        }
    }
}
