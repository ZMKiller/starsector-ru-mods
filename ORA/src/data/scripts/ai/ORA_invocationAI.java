package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicTargeting;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_invocationAI implements MissileAIPlugin, GuidedMissileAI {
    
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;    
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED, DETONATION_RANGE=300;
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0.25f;

    public ORA_invocationAI(MissileAPI missile, ShipAPI launchingShip) {	
	
        this.engine = Global.getCombatEngine();    
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed()*2;
    }

    @Override
    public void advance(float amount) {        
        
        //Instant fade of disabled missiles
        if(missile.isFizzling() || missile.isFading()){
            engine.addSmoothParticle(missile.getLocation(), missile.getVelocity(), 100, 0.5f, 0.25f, Color.blue);
            engine.addHitParticle(missile.getLocation(), missile.getVelocity(), 100, 1f, 0.1f, Color.white);
            engine.removeEntity(missile);
            return;
        }
            
        //no re-engage
        if(launch){            
            setTarget((CombatEntityAPI) MagicTargeting.pickTarget(missile,MagicTargeting.targetSeeking.NO_RANDOM,(int)missile.getWeapon().getRange(),90,0,1,1,1,1,true));
            missile.setCollisionClass(CollisionClass.NONE);
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() 
                || target == null 
                || !engine.isEntityInPlay(target) ){
            return;
        }
        
        //always accelerate
        missile.giveCommand(ShipCommand.ACCELERATE);  
        
        timer+=amount;    
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            
            //detonation check
            
            if(MathUtils.isWithinRange(missile, target, DETONATION_RANGE)){
//                float aim = missile.getFacing();
                float aim = VectorUtils.getFacing(missile.getVelocity());
                
                for (int i=0; i<25; i++){
                    engine.spawnProjectile(missile.getSource(), missile.getWeapon(), "ora_invoked", missile.getLocation(), aim -23.5f  + 2*i + (float)Math.random(), new Vector2f());
                }
                      
                if(MagicRender.screenCheck(0.2f, missile.getLocation())){
                    //shockwave
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("graphics/fx/ora_shockwave.png"),
                            new Vector2f(missile.getLocation()),
                            MathUtils.getPoint(new Vector2f(), 400, aim),
                            new Vector2f(1,1),
                            new Vector2f(1100,1400),
                            aim-90,
                            0,
                            Color.white,
                            true,
                            0f,
                            0.3f,
                            0.3f
                    );

                    //flare1
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("graphics/ORA/weapons/ora_flare1.png"),
                            new Vector2f(missile.getLocation()),
                            new Vector2f(),
                            (Vector2f)(new Vector2f(500,40)).scale(0.75f+0.25f*(float)Math.random()),
                            new Vector2f(1000,80),
                            0,
                            0,
                            Color.white,
                            true,
                            0f,
                            0.1f,
                            0.2f
                    );

                    //flare2
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("graphics/ORA/weapons/ora_flare2.png"),
                            new Vector2f(missile.getLocation()),
                            new Vector2f(),
                            (Vector2f)(new Vector2f(600,60)).scale(1.5f*(float)Math.random()-0.5f),
                            new Vector2f(1200,120),
                            0,
                            0,
                            Color.white,
                            true,
                            0f,
                            0.1f,
                            0.2f
                    );

                    //flash
                    engine.addSmoothParticle(missile.getLocation(), new Vector2f(), 800, 0.5f, 0.3f, Color.blue);
                    engine.addHitParticle(missile.getLocation(), new Vector2f(), 600, 1f, 0.2f, Color.white);
                    engine.spawnExplosion(missile.getLocation(), new Vector2f(), new Color(100,25,200,255), 500, 0.25f);
                }
                
                //sound
                Global.getSoundPlayer().playSound("ora_invocated", 1, 1, missile.getLocation(), missile.getVelocity());
                
                engine.removeEntity(missile);
                return;
            }
            
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
                    missile.getLocation(),
                    MAX_SPEED,
                    target.getLocation(),
                    target.getVelocity()
            );                
            //null pointer protection
            if (lead == null) {
                lead = target.getLocation(); 
            }
//            //debug
//            engine.addHitParticle(target.getLocation(), new Vector2f(), 20, 10, 0.25f, Color.yellow);            
        }
        
        //aim angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( missile.getFacing(), correctAngle);
        
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

    @Override
    public CombatEntityAPI getTarget(){
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target){
        this.target = target;
    }
}