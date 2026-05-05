//By Tartiflette
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_echoesAI implements MissileAIPlugin, GuidedMissileAI {          
        
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private IntervalUtil timer=new IntervalUtil(0.05f,0.15f);

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ORA_echoesAI(MissileAPI missile, ShipAPI launchingShip) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }        
        this.missile = missile;
        target=null;
        missile.setCollisionClass(CollisionClass.NONE);
        missile.getSpriteAPI().setAdditiveBlend();
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() || missile.isFading()) {return;}
        
        missile.giveCommand(ShipCommand.ACCELERATE);
        
        Global.getSoundPlayer().playLoop("ora_echoesS", missile, 1, 0.5f, missile.getLocation(), missile.getVelocity());
        
        missile.getSpriteAPI().setColor(
                new Color(
                    0.8f+0.2f*(float)Math.random(),
                    0.8f+0.2f*(float)Math.random(),
                    0.8f+0.2f*(float)Math.random()
                ));

        int flip = 1;
        if(Math.random()>0.5){
            flip=-1;
        }

        missile.getSpriteAPI().setSize(
                flip*(88*(0.8f+0.4f*(float)Math.random())),
                88*(0.8f+0.2f*(float)Math.random())
        );

        missile.getSpriteAPI().setCenter(
                flip*(39+10*(float)Math.random()),
                39+10*(float)Math.random()
        );
        
        missile.setFacing(VectorUtils.getFacing(missile.getVelocity()));
        
        timer.advance(amount);
        
        if(timer.intervalElapsed()){
             
            if(MagicRender.screenCheck(0.1f, missile.getLocation())){
                engine.addHitParticle(
                        missile.getLocation(),
                        missile.getVelocity(),
                        200*(0.75f+0.25f*(float)Math.random()),
                        1,
                        0.1f,
                        new Color(50,150,250,250)
                );

                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(missile.getLocation(), 44),
                        new Vector2f(),
                        10,
                        1,
                        0.5f,
                        new Color(50,150,250,250)
                );

                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(missile.getLocation(), 44),
                        new Vector2f(),
                        20,
                        1,
                        0.2f,
                        new Color(50,150,250,250)
                );
            }
            
            List<ShipAPI>ships=AIUtils.getNearbyEnemies(missile, 50);
            for(ShipAPI s : ships){
                if(s.getCollisionClass()!=CollisionClass.NONE){
                    EmpArcEntityAPI arc = engine.spawnEmpArc(missile.getSource(), 
                            missile.getLocation(),
                            s,
                            s,
                            DamageType.ENERGY,
                            20,
                            200,
                            1000,
                            null,
                            3,
                            new Color(150,50,250,64),
                            new Color(150,200,250,64)
                    );
                }
            }
            
            List<MissileAPI>missiles=CombatUtils.getMissilesWithinRange(missile.getLocation(), 150);
            for(MissileAPI m : missiles){
                if(m==missile) continue;
                if(m.getOwner() == missile.getOwner()) continue;
                if(m.getCollisionClass()!=CollisionClass.NONE) {
                    float r=(float)Math.random();
                    if(r>0.75){
                        m.setArmedWhileFizzling(false);
                    }
                    if(r>0.5){
                        m.flameOut();

                        engine.addHitParticle(
                                m.getLocation(),
                                m.getVelocity(),
                                50,
                                1,
                                0.25f,
                                new Color(50,150,250,250)
                        );
                    }
                }
            }
            
            if(missile.isFizzling()){
                if(MagicRender.screenCheck(0.1f, missile.getLocation())){
                    engine.spawnExplosion(
                            missile.getLocation(),
                            new Vector2f(), 
                            new Color(50,150,250,250),
                            175,
                            0.5f
                    );
                    engine.addHitParticle(
                            missile.getLocation(),
                            new Vector2f(),
                            150,
                            1,
                            0.25f,
                            new Color(50,150,250,250)
                    );
                    engine.addHitParticle(
                            missile.getLocation(),
                            new Vector2f(),
                            150,
                            1,
                            0.1f,
                            Color.white
                    );
                }
                engine.removeEntity(missile);
            }
        }
    }
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}