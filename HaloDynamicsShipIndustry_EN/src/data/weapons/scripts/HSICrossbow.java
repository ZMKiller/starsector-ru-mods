package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.Random;

public class HSICrossbow implements EveryFrameWeaponEffectPlugin {
    private boolean isPlaying = false;
    private float elapsed = 0;

    private static float MAX_TIME = 1.2f;
    private static float FRAME_RATE = 20f;

    private static  int FRAME = 18;

    private boolean reloaded = false;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        weapon.getAnimation().pause();
        if(isPlaying){
            elapsed+=amount;
            int frame = (int)(elapsed*FRAME_RATE);
            if(frame>=FRAME){
                isPlaying = false;
                reloaded =false;
                elapsed = 0;
                return;
            }
            weapon.getAnimation().setFrame(frame);
        }
        if(weapon.getAmmo()==0&&reloaded){
            isPlaying = true;
        }
        if(weapon.getAmmo()>=1||weapon.getAmmoTracker().getReloadProgress()>0.99f){
            reloaded = true;
        }
    }
}
