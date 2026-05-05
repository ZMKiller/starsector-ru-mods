/*
    By Tartiflette
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_hullScripts implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false;
    private ShipAPI SHIP;
    private float velocityScale=3f, attitudeScale=0.66f;
    private final IntervalUtil time = new IntervalUtil(0.0333f,0.0333f);
    
    private WeaponAPI FRONT_RIGHT, FRONT_LEFT, RIGHT_FRONT, RIGHT_REAR, LEFT_FRONT, LEFT_REAR;
    private final List<WeaponAPI> THRUSTERS = new ArrayList();
    
    private ShipSystemAPI SYSTEM;
    
    private float previousTurnAcceleration, deltaTurnAcceleration;
    private Vector2f previousVelocity= new Vector2f(), deltaVelocity=new Vector2f();
    private boolean init=false;
    private final Vector2f size= new Vector2f(8,74);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            
            SHIP=weapon.getShip();
            SYSTEM=SHIP.getSystem();
            
            if(SHIP.isCruiser()){
                velocityScale=1.5f;
                attitudeScale=0.4f;
            }
            
            for(WeaponAPI w : SHIP.getAllWeapons()){
                switch(w.getSlot().getId()){
                    case "FRONT_RIGHT":
                        FRONT_RIGHT = w;
                        THRUSTERS.add(w);
                        break;
                    case "FRONT_LEFT":
                        FRONT_LEFT = w;
                        THRUSTERS.add(w);
                        break;
                    case "RIGHT_FRONT":
                        RIGHT_FRONT = w;
                        THRUSTERS.add(w);
                        break;
                    case "RIGHT_REAR":
                        RIGHT_REAR = w;
                        THRUSTERS.add(w);
                        break;
                    case "LEFT_FRONT":
                        LEFT_FRONT = w;
                        THRUSTERS.add(w);
                        break;
                    case "LEFT_REAR":
                        LEFT_REAR = w;
                        THRUSTERS.add(w);
                        break;
                }
            }
        }    

        if(engine.isPaused() || SHIP.getOriginalOwner()==-1){
            return;
        }

        //check for death
        if(!SHIP.isAlive()){
            for(WeaponAPI w : THRUSTERS){
                w.getAnimation().setFrame(0);
                w.getAnimation().setFrameRate(0);
            }
            return;
        }

        if(SYSTEM.isActive()){
            if (!init){
                init=true;
                previousTurnAcceleration=SHIP.getAngularVelocity();
                previousVelocity = new Vector2f(SHIP.getVelocity());
            }
            //30FPS
            time.advance(amount);
            if(time.intervalElapsed()){
                
                //the system's level affect the color of the thrusters
                float power=SYSTEM.getEffectLevel();

                //get the changes in angular velocity, slightly smoothed.
                deltaTurnAcceleration = deltaTurnAcceleration + ((SHIP.getAngularVelocity()-previousTurnAcceleration)-deltaTurnAcceleration)/10;
                //clamp when the acceleration is almost null
                if(Math.abs(deltaTurnAcceleration)<0.02f){
                    deltaTurnAcceleration=0;
                }                
                //store the new value
                previousTurnAcceleration=SHIP.getAngularVelocity();

                //get the changes in velocity, smoothed too
                float deltaX, deltaY;
                deltaX=deltaVelocity.x + ((SHIP.getVelocity().x-previousVelocity.x)-deltaVelocity.x)/10;
                deltaY=deltaVelocity.y + ((SHIP.getVelocity().y-previousVelocity.y)-deltaVelocity.y)/10;
                //clamp when the acceleration is almost null
                if(Math.abs(deltaX)<0.001f){
                    deltaX=0;
                }
                if(Math.abs(deltaY)<0.001f){
                    deltaY=0;
                }                
                deltaVelocity = new Vector2f(deltaX,deltaY);
                //orient the frame of reference
                Vector2f velocityAcceleration = new Vector2f();
                VectorUtils.rotate(deltaVelocity, -SHIP.getFacing()+90, velocityAcceleration);
                //store the new value
                previousVelocity = new Vector2f(SHIP.getVelocity());

                if(MagicRender.screenCheck(0.25f, SHIP.getLocation())){
                    thrust(engine, FRONT_RIGHT, power* Math.max(0,Math.min(1,-(velocityScale/2) * velocityAcceleration.y)), power);
                    thrust(engine, FRONT_LEFT, power* Math.max(0,Math.min(1,-(velocityScale/2) * velocityAcceleration.y)), power);

                    thrust(engine, RIGHT_FRONT, power* Math.max(0,Math.min(1,(-velocityScale * velocityAcceleration.x)+(attitudeScale*deltaTurnAcceleration))), power);
                    thrust(engine, RIGHT_REAR, power* Math.max(0,Math.min(1,(-velocityScale * velocityAcceleration.x)-(attitudeScale*deltaTurnAcceleration))), power);

                    thrust(engine, LEFT_FRONT, power* Math.max(0,Math.min(1,(velocityScale * velocityAcceleration.x)-(attitudeScale*deltaTurnAcceleration))), power);
                    thrust(engine, LEFT_REAR, power* Math.max(0,Math.min(1,(velocityScale * velocityAcceleration.x)+(attitudeScale*deltaTurnAcceleration))), power);
                }
            }
        } else if(init){
                init=false;
                for(WeaponAPI w : THRUSTERS){
                    w.getAnimation().setFrame(0);
            }
        }
    }
    
    private void thrust(CombatEngineAPI engine, WeaponAPI weapon, float thrust, float system){
        
        engine.addSmoothParticle(
                weapon.getLocation(),
                MathUtils.getPoint(new Vector2f(), 100+500*thrust, weapon.getCurrAngle()),
                25+75*thrust,
                thrust*0.5f,
                (thrust+(float)Math.random()*0.5f)/3,
                new Color(0.02f, 0.066f*system, 0.1f*system)
        );
        
        int frame = (int)(Math.random() * (weapon.getAnimation().getNumFrames() - 1)) + 1;
        if(frame==weapon.getAnimation().getNumFrames()){
            frame=1;
        }        
        weapon.getAnimation().setFrame(frame);
        
        SpriteAPI sprite = weapon.getSprite();
        float width=MagicAnim.smooth(thrust)*size.x/2+size.x/2;
        float height=MagicAnim.smooth(thrust)*size.y+(float)Math.random()*3+3;
        sprite.setSize(width, height);
        sprite.setCenter(width/2, height/2);
        sprite.setColor(new Color(1f, 0.25f+3*system/4, system));
    }    
}