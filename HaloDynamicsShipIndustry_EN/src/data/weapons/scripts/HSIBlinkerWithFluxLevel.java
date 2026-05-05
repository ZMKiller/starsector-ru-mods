package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import data.kit.HSIIds;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class HSIBlinkerWithFluxLevel implements EveryFrameWeaponEffectPlugin {
    private boolean once = true;

    private FaderUtil blinker;

    private IntervalUtil timer;

    private boolean withL = false;

    private StandardLight light;

    private Color c;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(once) {
            c = weapon.getSpec().getGlowColor();
            float interval = 4f;
            if(weapon.getSpec().getTags().contains(HSIIds.SPECIAL_TAG.SHORT_INTERVAL_BLINKER)){
                interval = 2f;
            }else if(weapon.getSpec().getTags().contains(HSIIds.SPECIAL_TAG.LONG_INTERVAL_BLINKER)){
                interval = 8f;
            }
            timer = new IntervalUtil(interval,interval);
            float blinkTime = 2f;
            if(weapon.getSpec().getTags().contains(HSIIds.SPECIAL_TAG.RAPID_BLINKER)){
                blinkTime = 0.8f;
            }
            blinker = new FaderUtil(0f,blinkTime/2f,blinkTime/2f,false,true);
            blinker.fadeIn();
            once = false;
        }
        float adv = amount;
        if(weapon.getShip()!=null){
            if(weapon.getShip().getFluxTracker().isOverloadedOrVenting()){
                adv = 2*amount;
            }
        }
        if(blinker.isFadedOut()){
            timer.advance(adv*(1f+weapon.getShip().getFluxLevel()));
            if(timer.intervalElapsed()){
                blinker.fadeIn();
            }
        }else{
            blinker.advance(adv);
            float alpha = 0.3f*weapon.getShip().getFluxLevel();
            weapon.getSprite().setColor(new Color(weapon.getSprite().getColor().getRed(),weapon.getSprite().getColor().getGreen(),
                    weapon.getSprite().getColor().getBlue(),(int)((blinker.getBrightness()*0.7f+alpha)*255)));
        }
    }
}
