package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.hullmods.ShieldMod.HSIBaseShieldModEffect;

public class HSIJuggernautCore extends HSIBaseShieldModEffect {

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //stats.getZeroFluxSpeedBoost().modifyMult(id,0f);
        //stats.getMaxSpeed().modifyMult(id,0.5f);
    }

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldRegenCooldown().modifyMult(getStandardId(shield),2f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if(!ship.isStationModule()&&ship.getParentStation()==null) {
            ship.addListener(new HSIJuggernautRetreat(ship));
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(ship.isAlive()&&ship.isStationModule()&&ship.getParentStation()!=null&&ship.getParentStation().getEngineController().isAccelerating()){
            ship.giveCommand(ShipCommand.ACCELERATE,null,0);
        }
        if(ship.getFullTimeDeployed()<=30f){
            ship.getMutableStats().getMaxSpeed().modifyFlat("HSI_JuggernautDeploy",50f);
        }else{
            ship.getMutableStats().getMaxSpeed().unmodify("HSI_JuggernautDeploy");
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }


    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship.getHullSpec().hasTag("HSI_Juggernaut");
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().hasTag("HSI_Juggernaut");
    }
}
