package data.ai;

import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

public class HSIMaidAI implements ShipAIPlugin {
    private ShipwideAIFlags flags = new ShipwideAIFlags();


    private IntervalUtil workCheckInterval = new IntervalUtil(0.1f,0.2f);

    public static final float ARMOR_MAX_IN_CELL = 0.4f;
    public static final float HP_MAX = 0.6f;
    public static final float WEAPON_MAX = 0.8f;

    private HSIMaidAITask task;
    @Override
    public void setDoNotFireDelay(float amount) {

    }

    @Override
    public void forceCircumstanceEvaluation() {

    }

    @Override
    public void advance(float amount) {
        workCheckInterval.advance(amount);
        if(workCheckInterval.intervalElapsed()&&(task == null||task.isFinished)){
            
        }
    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }

    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public ShipAIConfig getConfig() {
        return null;
    }



    public class HSIMaidAITask{
        public int mode = 0;
        public boolean isFinished = false;

        public Object param;

        public HSIMaidAITask(int mode,Object param){
            this.mode = mode;
            this.param = param;
        }
    }
}
