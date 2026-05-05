//By Tartiflette, simple and fast rocket AI that will try to attack a target in a frontal cone, and not reengage any if it misses.
//V2 done
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_callingAI implements MissileAIPlugin {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private Vector2f lead = new Vector2f();
    private float deviation=0;
    private final IntervalUtil timer = new IntervalUtil(0.1f,0.5f);
    private final float DAMPING = 0.05f, RANDOMNESS;
    private final int side;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ORA_callingAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
//        MAX_SPEED = missile.getMaxSpeed();
        lead=null;
        //force a lead point since the auto fire AI don't lead with missiles
        WeaponGroupAPI group = missile.getSource().getWeaponGroupFor(missile.getWeapon());
        if(group!=null && group.getAutofirePlugin(missile.getWeapon())!=null){            
            AutofireAIPlugin auto = group.getAutofirePlugin(missile.getWeapon());
            if(group.isAutofiring() && auto.getTargetShip()!=null){            
                lead = AIUtils.getBestInterceptPoint(missile.getLocation(), missile.getMaxSpeed(), auto.getTargetShip().getLocation(), auto.getTargetShip().getVelocity());
                //debug
    //            Global.getCombatEngine().addHitParticle(lead, new Vector2f(), 20, 1, 1, Color.red);            
                if(lead!=null){
                    lead=MathUtils.getPoint(missile.getLocation(), missile.getWeapon().getRange(), VectorUtils.getAngle(missile.getLocation(), lead));
                } else {
                    lead=MathUtils.getPoint(missile.getLocation(), missile.getWeapon().getRange(), missile.getFacing());
                }            
            } else {
                lead=MathUtils.getPoint(missile.getLocation(), missile.getWeapon().getRange(), missile.getFacing());
                //debug
    //            Global.getCombatEngine().addHitParticle(lead, new Vector2f(), 20, 1, 1, Color.blue);
            }
        }
        
        if(lead==null){
            lead=MathUtils.getPoint(missile.getLocation(), missile.getWeapon().getRange(), missile.getFacing());
        }
        
            
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        timer.forceIntervalElapsed();
        RANDOMNESS=45;
        //swap the damage bonuses from missile to energy
        missile.setDamageAmount(
                missile.getBaseDamageAmount()
                        *missile.getSource().getMutableStats().getEnergyWeaponDamageMult().computeMultMod()
                        /missile.getSource().getMutableStats().getMissileWeaponDamageMult().computeMultMod()
        );
        side=missile.getOwner();
        missile.setCollisionClass(CollisionClass.MISSILE_NO_FF);
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {                
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused()) {return;}
        
        if(missile.isFading() || missile.isFizzling() || missile.getOwner()!=side){
            engine.addSmoothParticle(missile.getLocation(), missile.getVelocity(), 100, 0.5f, 0.25f, Color.blue);
            engine.addHitParticle(missile.getLocation(), missile.getVelocity(), 100, 1f, 0.1f, Color.white);
            engine.removeEntity(missile);
            return;
        }
        
        //forced acceleration by default
        missile.giveCommand(ShipCommand.ACCELERATE);
       
        timer.advance(amount);
        if(timer.intervalElapsed()){
            deviation = (-(RANDOMNESS/2)+(float)Math.random()*RANDOMNESS)*(4-missile.getFlightTime())/3;
//            float d=missile.getBaseDamageAmount()*0.95f;
//            missile.setDamageAmount(d);
            
            //debug
//            engine.addFloatingText(missile.getLocation(), ""+d, 20, Color.yellow, missile, 1, 1);
//            engine.addFloatingText(missile.getLocation(), ""+missile.getDamageAmount(), 20, Color.yellow, missile, 1, 1);
        }
        
        //best angle for interception        
        float aimAngle = deviation + 
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
