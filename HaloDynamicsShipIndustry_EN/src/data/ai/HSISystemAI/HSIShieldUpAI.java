package data.ai.HSISystemAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;


public class HSIShieldUpAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;
    private boolean isLeader = false;

    private Object KEY = new Object();

    private static float range = 900f;//判断用

    private ShipAPI leader;

    private TimeoutTracker<Object> SiegeKeeper = new TimeoutTracker<>();

    private TimeoutTracker<Object> DecKeeper = new TimeoutTracker<>();
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = ship.getPhaseCloak();
        isLeader = ship.getHullSpec().getHullId().equals("HSI_Arbaletrier");//只有弩手本体需要考虑是否架盾
        if(!isLeader){
            if(ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP)&&ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP) instanceof ShipAPI){
                leader = (ShipAPI) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP);
            }
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){

        //Global.getLogger(this.getClass()).info("IsLeader = "+isLeader);
        SiegeKeeper.advance(amount);
        DecKeeper.advance(amount);
        //Global.getLogger(this.getClass()).info("KeeperSize = "+SiegeKeeper.getItems().size()+"|"+DecKeeper.getItems().size());
        if (system.isCoolingDown())
            return;
        boolean shouldSiege = false;
        if (target != null) {
        float d = Misc.getDistance(ship.getLocation(), target.getLocation())-target.getCollisionRadius();
            if ( d > 0.8f*range
                    && d < range * 1.3f) {
                shouldSiege = true;
                //Global.getLogger(this.getClass()).info("Dist = "+d);
            } else {
                shouldSiege = false;
            }
            if(DecKeeper.getItems().isEmpty()) {
                if (isLeader) {//进行一些身位控制
                    float m = 0.9f;
                    if (system.isOn()) m = 1.15f;
                    if (d < range * m) {
                        ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
                        ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                        DecKeeper.add(KEY, 0.8f);
                        //Global.getLogger(this.getClass()).info("Retreating");
                    }
                }
            }else{
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
                ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                //Global.getLogger(this.getClass()).info("Retreating");
            }
            if(ship.getPhaseCloak().isOn()){
                float diff = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(),target.getLocation()));
                if(Math.abs(diff)>1){
                    if(diff>0) ship.giveCommand(ShipCommand.TURN_LEFT,null,0);
                    else ship.giveCommand(ShipCommand.TURN_RIGHT,null,0);
                }else{
                    ship.setAngularVelocity(ship.getAngularVelocity()*0.01f);
                }
            }
        }

        if (!SiegeKeeper.getItems().isEmpty()) {
            shouldSiege = true;
        }

        doSiege(shouldSiege);
    }

    protected void doSiege(boolean shouldSiege) {
        if (!shouldSiege) {
            if (ship.getPhaseCloak().isOn()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK,null,0);
                //Global.getLogger(this.getClass()).info("Stop siegeing.");
            }
        } else {
            if (!ship.getPhaseCloak().isOn()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK,null,0);
                SiegeKeeper.add(KEY, 1f);
                //Global.getLogger(this.getClass()).info("Start siegeing.");
            }
        }
    }
}
