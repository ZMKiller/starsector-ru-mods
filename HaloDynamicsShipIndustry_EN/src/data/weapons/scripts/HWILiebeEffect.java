package data.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.FleetLog;

public class HWILiebeEffect implements EveryFrameWeaponEffectPlugin {
    private float maxLevel = 0;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        float chargeLevel = weapon.getChargeLevel();
        int level = (int)(Math.floor(maxLevel*5f));
        if(level<0) level = 0;
        if(chargeLevel>0&&weapon.getCooldownRemaining()<=0){
            if(maxLevel< chargeLevel){
                maxLevel = chargeLevel;
            }
        }else{
            if(maxLevel>0){
                fire(level,weapon);
                maxLevel = 0;
            }
        }

        if(weapon.getAnimation()!=null){
            weapon.getAnimation().setFrame(level);
        }
    }

    protected void fire(int level,WeaponAPI weapon){
        int acutalLevel = level;
        if(level < 5){
        float damageModifer = 0.6f+ level*0.08f;
        while (acutalLevel>=0) {
            if ((weapon.getShip().getFluxTracker().getMaxFlux() - weapon.getShip().getCurrFlux()) < weapon.getFluxCostToFire() * damageModifer) {
                acutalLevel--;
            }else{
                break;
            }
        }
        if(acutalLevel<0){
            //abort fire
            weapon.setRemainingCooldownTo(weapon.getCooldown());
            return;
        }
        DamagingProjectileAPI proj = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(weapon.getShip(),weapon,weapon.getId(),weapon.getFirePoint(0),weapon.getCurrAngle(),(weapon.getShip()!=null)?weapon.getShip().getVelocity():null);
        proj.getDamage().getModifier().modifyMult("HWI_LieBe_Charge_Factor",damageModifer);
        if(weapon.getShip()!=null){
            weapon.getShip().getFluxTracker().increaseFlux(weapon.getFluxCostToFire()*damageModifer,false);
        }
        weapon.setRemainingCooldownTo(weapon.getCooldown());
        Global.getSoundPlayer().playSound("gauss_cannon_fire",1f,1f,weapon.getLocation(),weapon.getShip().getVelocity());
        }
    }
}
