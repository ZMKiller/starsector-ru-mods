package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_newtonAI implements ShipSystemAIScript{     
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil timer = new IntervalUtil(0.5f,2f);

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
            if(!system.isActive() && ship.getFluxTracker().getFluxLevel()<0.85 && AIUtils.canUseSystemThisFrame(ship)){
                if(!AIUtils.getNearbyEnemies(ship, 500).isEmpty()){
                    ship.useSystem();
                }
            }
        }
    }
}