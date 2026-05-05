package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.Global;

public class HSISamsaraAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipwideAIFlags flags;
	private ShipSystemAPI system;
	
	private IntervalUtil tracker = new IntervalUtil(0.5f, 1f);
	
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
	}
	
	
	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
			
		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (system.isActive()) return;
			
			if (target == null) return;
			if(ship.isPullBackFighters()) return;

            if(target.getHullSize().equals(HullSize.CAPITAL_SHIP) ||target.getHullSize().equals(HullSize.CRUISER)){

            boolean targetIsVulnerable = target != null && (target.getFluxTracker().isOverloadedOrVenting() && 
            (target.getFluxTracker().getOverloadTimeRemaining() > 5f || 
            target.getFluxTracker().getTimeToVent() > 5f)||
            (target.getMaxFlux()-target.getCurrFlux())<2000f);
            
            if(targetIsVulnerable){
                ship.useSystem();;
            }

        }

		boolean shipisVulnerable = ship.isAlive()&&ship.areSignificantEnemiesInRange()&&((ship.getMaxFlux()-ship.getCurrFlux())<0.25f*ship.getMaxFlux());

		if(shipisVulnerable){
			ship.useSystem();
		}

			
		}
}

}