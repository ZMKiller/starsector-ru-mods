package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import static data.scripts.util.ORA_txt.txt;

public class ORA_ascensionSystem implements EveryFrameWeaponEffectPlugin {
    
    private boolean lightOn = true;
    private float leeching=0, level=0;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private boolean bonus = false, runOnce=false;
    private final IntervalUtil timer = new IntervalUtil(0.05f,0.05f);
    private final String ID = "AscensionLeech";
    
    private final String UI0=txt("sstm_newton0");
    private final String UI1=txt("sstm_newton1");
    private final String UI2=txt("%");
    
    private List<ShipAPI> leeched = new ArrayList<>();
    private final int MAX_BUFF = 4, RANGE = 1000;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        if (engine.isPaused() || !lightOn) return;
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();
            if(ship.getOriginalOwner()==-1){
                weapon.getAnimation().setFrame(0);
                lightOn = false;
                return;
            }
        }        
        
        if (system.isActive()) {
            
            boolean visible = MagicRender.screenCheck(1f, ship.getLocation());
            
            if(!bonus){
                bonus = true;
                leeched.clear();
                leeching=0;
                
                if(visible){
                    MagicRender.objectspace(
                            Global.getSettings().getSprite("graphics/fx/wormhole_ring_bright2.png"),
                            ship,
                            new Vector2f(),
                            new Vector2f(),
                            new Vector2f(RANGE*2+200,RANGE*2+200),
                            new Vector2f(-100,-100),
                            0,
                            0,
                            false,
                            new Color(255,0,50,255),
                            true,
                            0.05f,
                            0.0f,
                            0.5f,
                            true
                    );  
                }
            }
            
            level=system.getEffectLevel();
            
            //maintain player status
            if(ship==engine.getPlayerShip()){
                engine.maintainStatusForPlayerShip("ora_newton",
                    "graphics/ORA/icons/hullsys/ora_newton.png",
                    UI0,                        
                    UI1+Math.round(level*leeching*100*(1-(1/(MAX_BUFF+1))))+UI2,
                    leeching<=0
                );
                
                //half timewarp
                float slowdown= 1+(level*leeching*MAX_BUFF);
                engine.getTimeMult().modifyMult(ID, 1/slowdown);
                
//                engine.maintainStatusForPlayerShip(
//                    "ora_newton2",
//                    "graphics/icons/hullsys/high_energy_focus.png",
//                    "Newtonian Slinging",                        
//                    "time dillation:"+Math.round(100*(1-slowdown))+"%",
//                    leeching<=0
//                );
            }            
            
            if(visible){
                //jitter
                ship.setJitterUnder(ship, Color.PINK, level*leeching/2, (int)(10*leeching), level*50*leeching);
            }
            
            timer.advance(amount);
            if(timer.intervalElapsed()){
                
                //apply the leeching bonus
                if(!leeched.isEmpty()){
                    float targets=0;
                    for(ShipAPI s : leeched){
                        if(s.isCapital()){
                            targets+=30;
                        }else if(s.isCruiser()){
                            targets+=15;
                        }else if(s.isDestroyer()){
                            targets+=7;
                        }else if(s.isFrigate()){
                            targets+=3;
                        }else{
                            targets+=1;
                        }
                    }
                    //magic formula to soft cap the bonus
                    leeching = 1- ( 1/ ( (0.05f*targets) +1 ));
                    applyBuff(ship, leeching, level);
                } else {
                    unapplyBuff(ship);
                    leeching = 0;
                }
                
                List<ShipAPI> nearby = AIUtils.getNearbyEnemies(ship, RANGE);
                List<ShipAPI> previous = new ArrayList<>(leeched);
                
                if(!nearby.isEmpty()){
                    for(ShipAPI s : nearby){
                        //new affected ship
                        if(!previous.contains(s)){
                            applyLeech(s, ship, level, visible);
                            leeched.add(s);
                        }
                        //affected ship already present
                        if(previous.contains(s)){
                            previous.remove(s);
                            //in case another Ascension tried to remove the Leech form that ship
                            applyLeech(s, ship, level, visible);
                        }
                    }
                    //remaining ships get unleeched
                    if(!previous.isEmpty()){
                        for(ShipAPI s : previous){
                            leeched.remove(s);
                            unapplyLeech(s);
                        }
                    }
                } else if(!leeched.isEmpty()){
                    //no ship in range, make sure to unleech all ships
                    for(ShipAPI s : leeched){
                        unapplyLeech(s);
                    }
                    leeched.clear();
                }
                
                //visual effect
                float level = system.getEffectLevel();                
                weapon.getAnimation().setFrame(1);
                weapon.getSprite().setColor(
                        new Color(
                                1,
                                1,
                                1,
                                (float)Math.min(
                                        1,
                                        Math.max(
                                                0,
                                                level*0.75f + 0.25*Math.random()
                                        )
                                )
                        )
                );
            }
        } else if (bonus){
            bonus = false;
            weapon.getSprite().setColor(Color.black);
            weapon.getAnimation().setFrame(0);
            engine.getTimeMult().unmodify(ID);
            unapplyBuff(ship);
            if(!leeched.isEmpty()){
                for(ShipAPI s : leeched){
                    unapplyLeech(s);
                }
                leeched.clear();
            }
        }
    } 
    
    private void applyLeech(ShipAPI ship, ShipAPI source, float level, boolean visible){
        
        ship.getMutableStats().getMaxSpeed().modifyMult(ID, 1-(level*0.5f));
        ship.getMutableStats().getAcceleration().modifyMult(ID, 1-(level*0.5f));
        ship.getMutableStats().getDeceleration().modifyMult(ID, 1-(level*0.5f));
        ship.getMutableStats().getMaxTurnRate().modifyMult(ID, 1-(level*0.5f));
        ship.getMutableStats().getTurnAcceleration().modifyMult(ID, 1-(level*0.5f));
        
        float size=ship.getCollisionRadius()*2;
        float angle=VectorUtils.getAngle(ship.getLocation(), source.getLocation());
        float size2=source.getCollisionRadius()*2;
        
        int particle;
        
        if(ship.isCapital()){
            particle=4;
        }else if(ship.isCruiser()){
            particle=3;
        }else if(ship.isDestroyer()){
            particle=2;
        }else if(ship.isFrigate()){
            particle=1;
        }else{
            particle=0;
        }
        
        if(visible){
            MagicRender.objectspace(
                        Global.getSettings().getSprite("graphics/fx/ora_leech.png"),
                        ship,
                        new Vector2f(),
                        new Vector2f(),
                        new Vector2f(size,size),
                        new Vector2f(),
                        angle+180,
                        0,
                        false,
                        new Color(255,255,255,(int)(200*level)),
                        true,
                        0.05f,
                        0.0f,
                        0.05f,
                        true
                );
            
            for(int i=0; i<particle; i++){            
                Global.getCombatEngine().addHitParticle(
                        MathUtils.getPoint(ship.getLocation(), ship.getCollisionRadius()-(float)Math.random()*50, angle-10+(20*(float)Math.random())),
                        MathUtils.getPoint(new Vector2f(), 100+(float)Math.random()*100, angle),
                        3+5*(float)Math.random(),
                        0.5f,
                        0.25f+0.5f*(float)Math.random(),
                        Color.pink
                );
            }
        }
        
        if(MagicRender.screenCheck(0.1f, source.getLocation())){
            MagicRender.objectspace(
                        Global.getSettings().getSprite("graphics/fx/ora_leech.png"),
                        source,
                        new Vector2f(),
                        new Vector2f(),
                        new Vector2f(size2,size2),
                        new Vector2f(),
                        angle,
                        0,
                        false,
                        new Color(255,255,255,(int)(200*level)),
                        true,
                        0.05f,
                        0.0f,
                        0.1f,
                        true
                );

            for(int i=0; i<particle; i++){
                Global.getCombatEngine().addHitParticle(
                        MathUtils.getPoint(source.getLocation(), source.getCollisionRadius()+50+(float)Math.random()*100, angle+170+(20*(float)Math.random())),
                        MathUtils.getPoint(new Vector2f(), 100+(float)Math.random()*100, angle),
                        3+5*(float)Math.random(),
                        0.5f,
                        0.25f+0.5f*(float)Math.random(),
                        Color.pink
                );
            }
        }
    }
    
    private void unapplyLeech(ShipAPI ship){        
        ship.getMutableStats().getMaxSpeed().unmodify(ID);
        ship.getMutableStats().getAcceleration().unmodify(ID);
        ship.getMutableStats().getDeceleration().unmodify(ID);
        ship.getMutableStats().getMaxTurnRate().unmodify(ID);
        ship.getMutableStats().getTurnAcceleration().unmodify(ID);
    }
    
    private void applyBuff(ShipAPI ship, float bonus, float level){        
//        ship.getMutableStats().getMaxSpeed().modifyPercent(ID+ship.getId(), 50*level*MAX_BUFF*bonus);
//        ship.getMutableStats().getAcceleration().modifyPercent(ID+ship.getId(), 200*level*MAX_BUFF*bonus);
//        ship.getMutableStats().getDeceleration().modifyPercent(ID+ship.getId(), 200*level*MAX_BUFF*bonus);
//        ship.getMutableStats().getMaxTurnRate().modifyPercent(ID+ship.getId(), 100*level*MAX_BUFF*bonus);
//        ship.getMutableStats().getTurnAcceleration().modifyPercent(ID+ship.getId(), 200*level*MAX_BUFF*bonus);
//        
//        ship.getMutableStats().getFluxDissipation().modifyPercent(ID+ship.getId(), 100*level*MAX_BUFF*bonus);
        
        ship.getMutableStats().getTimeMult().modifyMult(ID+ship.getId(), 1+level*bonus*MAX_BUFF);
    }
    
    private void unapplyBuff(ShipAPI ship){
//        ship.getMutableStats().getMaxSpeed().unmodify(ID+ship.getId());
//        ship.getMutableStats().getAcceleration().unmodify(ID+ship.getId());
//        ship.getMutableStats().getDeceleration().unmodify(ID+ship.getId());
//        ship.getMutableStats().getMaxTurnRate().unmodify(ID+ship.getId());
//        ship.getMutableStats().getTurnAcceleration().unmodify(ID+ship.getId()); 
//        
//        ship.getMutableStats().getFluxDissipation().unmodify(ID+ship.getId());

        ship.getMutableStats().getTimeMult().unmodify(ID+ship.getId());
    }
}
