//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ORA_recitalEffect implements EveryFrameWeaponEffectPlugin{
    
    private float refire=1, BASE_REFIRE, FLUX=40;
    private final float FLUX_MULT=2;
    private int firing=1;
    private boolean runOnce=false;
    private ShipAPI ship;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused()){return;}

        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            FLUX=weapon.getFluxCostToFire()*FLUX_MULT;
            BASE_REFIRE=weapon.getCooldown();
        }

        if(weapon.isFiring()){
            //debug
//            engine.addHitParticle(weapon.getLocation(), weapon.getShip().getVelocity(), 5, 5, 0.05f, Color.RED);
//            engine.addFloatingText(weapon.getLocation(), ""+weapon.getCooldownRemaining(), 20, Color.yellow, ship, 1, 1);

            firing=1; //the weapon is firing, this is to check if the weapon does not cease to fire for more than one frame
            
            float rof = (1/ship.getMutableStats().getEnergyRoFMult().computeMultMod());
            
            if( weapon.getChargeLevel()==1){
                refire=Math.max(0,refire - 0.1f);
                weapon.setRemainingCooldownTo(Math.max(0.1f, refire*BASE_REFIRE)*rof);
                ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux()+FLUX*refire);
            }

        } else if(firing>0){
            firing--;
        } else {
            refire=1; //reset the refire delay
        }
    }
}