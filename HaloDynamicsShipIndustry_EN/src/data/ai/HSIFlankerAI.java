package data.ai;

import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

public class HSIFlankerAI implements ShipAIPlugin {
    private ShipwideAIFlags aiFlags = new ShipwideAIFlags();
    private ShipAIConfig config = new ShipAIConfig();
    private ShipAPI ship;

    private IntervalUtil interval = new IntervalUtil(0.1f,0.15f);

    public HSIFlankerAI(ShipAPI ship){
        this.ship = ship;
    }

    private float noFire = 0;
    @Override
    public void setDoNotFireDelay(float v) {
        this.noFire = v;
    }
    private boolean forceRefresh = false;
    @Override
    public void forceCircumstanceEvaluation() {
        forceRefresh = true;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return null;
    }

    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public ShipAIConfig getConfig() {
        return null;
    }
}
