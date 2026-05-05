package data.weapons.scripts;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import data.kit.HSIIds;
import org.dark.shaders.light.LightData;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;
import java.util.List;

public class HSIBlinkerEffect implements EveryFrameWeaponEffectPlugin {
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
            withL = weapon.getSpec().getTags().contains(HSIIds.SPECIAL_TAG.WITH_LIGHT);
            blinker = new FaderUtil(0f,blinkTime/2f,blinkTime/2f,false,true);
            blinker.fadeIn();
            if(withL){
                light = new StandardLight(weapon.getLocation(), new Vector2f(0,0),new Vector2f(0,0),weapon.getShip());
                light.makePermanent();
                LightShader.addLight(light);
            }
            once = false;
        }
        if(withL&&(!weapon.getShip().isAlive())){
            LightShader.removeLight(light);
            //light.setLifetime(0.01f);
            light = null;
            return;
        }
        float adv = amount;
        if(weapon.getShip()!=null){
            if(weapon.getShip().getFluxTracker().isOverloadedOrVenting()){
                adv = 2*amount;
            }
        }
        if(blinker.isFadedOut()){
            timer.advance(adv);
            if(timer.intervalElapsed()){
                blinker.fadeIn();
            }
        }else{
            blinker.advance(adv);
            weapon.getSprite().setColor(new Color(weapon.getSprite().getColor().getRed(),weapon.getSprite().getColor().getGreen(),weapon.getSprite().getColor().getBlue(),(int)(blinker.getBrightness()*255)));
            if(withL&&light!=null) {
                float intensity = 0.8f+0.8f * blinker.getBrightness();
                float size = 200f+200f * blinker.getBrightness();
                if (c != null) light.setColor(c);
                light.setIntensity(intensity);
                light.setSize(size);
                light.setLocation(weapon.getLocation());
            }
        }
    }
}
