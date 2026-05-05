package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_solidAI implements ShipSystemAIScript{    
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil timer= new IntervalUtil(0.5f,1.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        if(engine.isPaused()||system.isActive()){
            return;
        }
        
        timer.advance(amount);
        if(timer.intervalElapsed()){
            if(!system.isActive() && (
                    (ship.getFluxLevel()>0.66f && ship.getFluxTracker().getCurrFlux()/ship.getFluxTracker().getHardFlux()<0.25f) //lots of soft flux
                    ||
                    (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.IN_CRITICAL_DPS_DANGER))
                    ||
                    (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE))
                    )){
                if(AIUtils.canUseSystemThisFrame(ship))ship.useSystem();
            }
        }
    }
}