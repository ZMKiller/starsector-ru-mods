package data.ai;

import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

public class HSILongRangeAttackFighterAI implements ShipAIPlugin{
    protected ShipAPI ship;
    protected ShipAIConfig config;
    protected float noFireDelay = 0;
    protected boolean needsRefit = false;

    public HSILongRangeAttackFighterAI(ShipAPI ship,ShipAIConfig config){
        this.ship = ship;
        this.config = config;
    }

    public void setDoNotFireDelay(float amount){
        noFireDelay = amount;
    }
	
	/**
	 * When this is called, the AI should immediately evaluate nearby threats and such,
	 * if it only does it periodically otherwise.
	 * 
	 * Called when the autopilot is toggled on.
	 */
	public void forceCircumstanceEvaluation(){

    }
	
	
	/**
	 * The AI should do its main work here.
	 * @param amount
	 */
	public void advance(float amount){

    }
	
	
	/**
	 * Only called for fighters, not regular ships or drones.
	 * @return whether the fighter needs refit
	 */
	public boolean needsRefit(){
        return needsRefit;
    }
	
	public ShipwideAIFlags getAIFlags(){
        return ship.getAIFlags();
    }

	public void cancelCurrentManeuver(){

    }

	public ShipAIConfig getConfig(){
        return config;
    }
}
