package data.ai.HSISystemAI;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.Iterator;

public class HSISpaceStormAI extends HSIFortressShieldAI{


    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;
    private boolean systemuse = true;
    private static float RANGE = 1000f;
    private ShipwideAIFlags flags;
    private IntervalUtil tracker = new IntervalUtil(0.1f, 0.1f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.flags = flags;
        this.system = system;
    }

    @SuppressWarnings("unchecked")
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);
        systemuse = true;
        if (target == null)
            return;
        if (tracker.intervalElapsed()) {
            if (system.getCooldownRemaining() > 0)
                systemuse = false;
            if (system.isOutOfAmmo())
                systemuse = false;
            if (system.isActive())
                systemuse = false;
            float weight = 0;
            Iterator<Object> sg = engine.getShipGrid().getCheckIterator(ship.getLocation(),1000f,1000f);

            while (sg.hasNext()){
                Object o = sg.next();
                if(o instanceof ShipAPI){
                    ShipAPI s = (ShipAPI) o;
                    if(s.getOwner()==ship.getOwner()||s.getOwner() == 100){
                        continue;
                    }
                    float factor = 2f;
                    if(s.isFighter()){
                        if(s.getWing()!=null){
                            factor = (float) (s.getWing().getSpec().getFleetPoints()) /(float)(s.getWing().getSpec().getNumFighters());
                        }
                    }
                    weight+=(4-s.getHullSize().ordinal())*50f*factor*s.getHullSpec().getFleetPoints();
                }
            }

            Iterator<Object> mg = engine.getMissileGrid().getCheckIterator(ship.getLocation(),2000f,2000f);
            while (mg.hasNext()){
                Object o = mg.next();
                if(o instanceof MissileAPI){
                    MissileAPI m = (MissileAPI)o;
                    if(Misc.getDistance(m.getLocation(),ship.getLocation())-ship.getCollisionRadius()-m.getMaxSpeed()*1.25f<1000f){
                        weight+=(m.getDamageAmount()/10f);
                    }
                }
            }

            if(systemuse&&weight>800f){
                ship.useSystem();
            }
        }
    }
}
