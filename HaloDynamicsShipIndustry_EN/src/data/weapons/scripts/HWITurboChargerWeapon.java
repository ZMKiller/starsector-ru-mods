package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class HWITurboChargerWeapon implements EveryFrameWeaponEffectPlugin {
    private int shots = 0;
    //private boolean thisShot = false;
    private IntervalUtil holding  =  new IntervalUtil(3f, 3f);
    private int maxShot = 10;
    private int increaseShot = 1;
    private float MAX_REDUCTION = 100f;
    private static final float REDUCTION_PER_SHOT = 10;
    private boolean shot = false;
    private int skip = 2;
    private int lowest = 0;
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(shot){
            if(skip>0){
                skip--;
                return;
            }else{
                skip = 2;
                weapon.setRemainingCooldownTo(weapon.getCooldownRemaining()*(1-(MAX_REDUCTION*((float)shots/(float)maxShot)/(100f+MAX_REDUCTION))));
                //Global.getLogger(this.getClass()).info("CD: "+weapon.getCooldownRemaining());
                holding.setElapsed(0f);
                shots+=increaseShot;
                if(shots>maxShot){
                    shots = maxShot;
                }
                shot = false;
            }
        }else{
            holding.advance(amount);
            if(holding.intervalElapsed()){
                shots=Math.max(shots-1, lowest);
            }
        }
        
    }

    public int getShots(){
        return shots;
    }

    public void setMaxShot(int max){
        this.maxShot = max;
        MAX_REDUCTION=REDUCTION_PER_SHOT*maxShot;
    }

    public void setIncreaseShot(int inc){
        this.increaseShot = inc;
    }

    public void reportShot(){
        shot = true;
    }

    public void setLowest(int lowest) {
        this.lowest = lowest;
    }
}
