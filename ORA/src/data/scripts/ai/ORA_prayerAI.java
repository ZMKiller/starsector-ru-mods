
//By Tartiflette, simple and fast rocket AI that will try to attack a target in a frontal cone, and not reengage any if it misses.
//V2 done
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.magiclib.util.MagicTargeting;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_prayerAI implements MissileAIPlugin {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private Vector2f lead = new Vector2f();
    private boolean launch=false;
    private final float DAMPING = 0.05f, MAX_SPEED, offset;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ORA_prayerAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        missile.setCollisionClass(CollisionClass.MISSILE_NO_FF);
        MAX_SPEED = missile.getMaxSpeed();
        offset = MathUtils.getRandomNumberInRange(2.0f, 4.0f);
            
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {                
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused()) {return;}
        
        if(missile.isFading() || missile.isFizzling()){
            engine.addSmoothParticle(missile.getLocation(), missile.getVelocity(), 100, 0.5f, 0.25f, Color.blue);
            engine.addHitParticle(missile.getLocation(), missile.getVelocity(), 100, 1f, 0.1f, Color.white);
            engine.removeEntity(missile);
            return;
        }
        
        //forced acceleration by default
        missile.giveCommand(ShipCommand.ACCELERATE);
       
        if(!launch){
            //find proper target
            ShipAPI target = (ShipAPI) MagicTargeting.pickMissileTarget(
                    missile, 
                    MagicTargeting.targetSeeking.NO_RANDOM,
                    2000,
                    45,
                    1,
                    2,
                    4,
                    8,
                    16
            );
            
            //find fixed lead point
            if(target==null){
                //no target, random point forward
                lead = MathUtils.getPoint(missile.getLocation(), 2000, missile.getFacing()+MathUtils.getRandomNumberInRange(-15, 15));
            } else {
                //valit target, random point inside it with lead
                lead = AIUtils.getBestInterceptPoint(
                        missile.getLocation(),
                        MAX_SPEED, 
                        target.getLocation(), 
                        target.getVelocity()
                );
                if(lead==null){
                    lead=target.getLocation();
                }
                lead = MathUtils.getRandomPointInCircle(lead, target.getCollisionRadius()*0.66f);
            }
            
            //null check
            launch=(lead!=null);
            return;
        }
        
        //best angle for interception        
        float aimAngle =
                (float)FastTrig.sin(offset*missile.getElapsed())*7+
                MathUtils.getShortestRotation(
                    missile.getFacing(),
                    VectorUtils.getAngle(
                            missile.getLocation(),
                            lead
                    )
        );
        
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
    }
}
