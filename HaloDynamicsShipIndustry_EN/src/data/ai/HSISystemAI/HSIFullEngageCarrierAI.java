package data.ai.HSISystemAI;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import data.shipsystems.scripts.HSIDeckLoadStrike;
import data.shipsystems.scripts.HSIUplikeFireControl;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class HSIFullEngageCarrierAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;

    private IntervalUtil threatChecker = new IntervalUtil(0.3f, 0.6f);



    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        // this.flags = flags;
        this.engine = engine;
        this.system = ship.getPhaseCloak();
    }

    // private float sinceLast = 0f;

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        boolean shouldDo = false;
        if(system.isOutOfAmmo()||system.isCoolingDown()||system.isOn()) return;
        threatChecker.advance(amount);
        if(threatChecker.intervalElapsed()&&ship.areAnyEnemiesInRange()){
            float totalDP = 0;
            float engagingDP = 0;
            int num = 0;
            List<ShipAPI> allies = AIUtils.getAlliesOnMap(ship);
            allies.add(ship);
            for(ShipAPI s:allies){
                if(s.isFighter()) continue;
                if(s.isStationModule()) continue;

                float DP = (s.getFleetMember()!=null)?s.getFleetMember().getDeploymentPointsCost():0;
                totalDP+=DP;

                if(s.hasListenerOfClass(HSIDeckLoadStrike.HSIDLSTimer.class)) continue;

                if(s.areAnyEnemiesInRange()){
                    engagingDP+=0.33f*DP;
                }
                if(s.areSignificantEnemiesInRange()){
                    engagingDP+=0.67f*DP;
                }
                num++;
            }
            if(totalDP == 0) totalDP = 1f;
            shouldDo = num==1||(engagingDP/totalDP)>0.6f;
            //Global.getLogger(this.getClass()).info("Engaging:"+engagingDP+"|Total:"+totalDP);
        }

        if (shouldDo) {
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
        }
    }
}
