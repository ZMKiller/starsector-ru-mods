//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_sempiternalEffect implements EveryFrameWeaponEffectPlugin{
    
    private boolean runOnce=false, hidden=false, IN=true, OUT=false;
    private float flux=0;
    private final IntervalUtil time = new IntervalUtil(0.0333f,0.1f);
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!hidden && engine.isPaused()){return;}
        
        if(!runOnce){
            runOnce=true;
            //check if the mount is hidden
            if (weapon.getSlot().isHidden()){
                hidden=true;
                return;
            }
        }
        //Only check firing weapons
        if(weapon.getChargeLevel()>0){
            
            if(IN){
                //chargeup sound
                IN=false;
                Global.getSoundPlayer().playSound("ora_sempiternal_in", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());  
                //chargeup flash
                engine.addHitParticle(
                        weapon.getLocation(),
                        weapon.getShip().getVelocity(),
                        50,
                        1,
                        0.25f,
                        new Color(255,175,255,255)
                );
            } else if(weapon.getChargeLevel()==1){
                //fire loop
                OUT=true;
                Global.getSoundPlayer().playLoop("ora_sempiternal_loop", weapon, 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
            } else if(OUT){
                //chargedown sound
                OUT=false;
                Global.getSoundPlayer().playSound("ora_sempiternal_out", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
            }
            
            //random delay
            time.advance(amount);
            if(time.intervalElapsed()){
                //charge unstable particles
                if(MagicRender.screenCheck(0.1f, weapon.getLocation())){
                    engine.addHitParticle(
                            MathUtils.getRandomPointInCircle(weapon.getLocation(), weapon.getChargeLevel()*15),
                            weapon.getShip().getVelocity(),
                            5+(float)Math.random()*weapon.getChargeLevel()*15,
                            1,
                            0.1f+0.25f*(float)Math.random()*weapon.getChargeLevel(),
                            new Color(200,100,255,255)
                    );
                }
                
                //weapon is actually firing
                if(weapon.getChargeLevel()==1){
                    //EXTRA FLUX
                    flux=Math.min(flux+time.getElapsed()/2, 2f);
                    weapon.getShip().getFluxTracker().setCurrFlux(weapon.getShip().getFluxTracker().getCurrFlux()+time.getElapsed()*weapon.getDerivedStats().getFluxPerSecond()*flux);
                    
                    if(MagicRender.screenCheck(0.1f, weapon.getLocation())){
                        //CENTRAL GLOW                                  
                        engine.addHitParticle(
                                weapon.getLocation(),
                                weapon.getShip().getVelocity(),
                                20+(float)Math.random()*20,
                                1,
                                0.2f+0.2f*(float)Math.random(),
                                new Color(200,100,255,255)
                        );

                        //FLARE
                        Vector2f loc = new Vector2f(weapon.getLocation());
                        Vector2f.sub(loc, weapon.getShip().getLocation(), loc);
                        VectorUtils.rotate(loc, -weapon.getShip().getFacing(), loc);
                            /*
                                objectspaceRender(
                                    SpriteAPI sprite, 
                                    CombatEntityAPI anchor, 
                                    Vector2f offset, 
                                    Vector2f vel, 
                                    Vector2f size, 
                                    Vector2f growth, 
                                    float angle, 
                                    float spin, 
                                    boolean parent, 
                                    Color color, 
                                    boolean additive, 
                                    float fadein, 
                                    float full, 
                                    float fadeout, 
                                    boolean fadeOnDeath
                                    )
                            */                    
                        MagicRender.objectspace(                            
                                Global.getSettings().getSprite("misc","ora_flare1"),
                                weapon.getShip(),
                                loc,
                                new Vector2f(),
                                (Vector2f)(new Vector2f(200,10)).scale(0.75f+0.25f*(float)Math.random()),
                                new Vector2f(),
                                90-weapon.getShip().getFacing(),
                                -weapon.getShip().getAngularVelocity(),
                                true,
                                new Color(255,200,200,128),
                                true,
                                0.05f,
                                time.getMaxInterval()/2,
                                0.05f,
                                false
                        );                    

                        MagicRender.objectspace(                            
                                Global.getSettings().getSprite("misc","ora_flare2"),
                                weapon.getShip(),
                                loc,
                                new Vector2f(),
                                (Vector2f)(new Vector2f(300,30)).scale(1.5f*(float)Math.random()-0.5f),
                                new Vector2f(),
                                90-weapon.getShip().getFacing(),
                                -weapon.getShip().getAngularVelocity(),
                                true,
                                new Color(255,100,200,255),
                                true,
                                0.05f,
                                time.getMaxInterval(),
                                0.05f,
                                false
                        );
                    }
                } else {
                    //reset the extra flux
                    flux=0;
                }
            }
        } else {
            //weapon isn't firing
            IN=true;
            OUT=false;
        }
    }
}