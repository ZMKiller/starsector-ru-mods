package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;

public class HSIDockAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private float fraction;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;

    private IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    private float sinceLast = 0f;

    //@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
		float lowest = 1f;
        ShipAPI toDock = null;
		sinceLast += amount;
        if(ship.getSystem().isCoolingDown()) return;
        if(ship.getSystem().isOn()) return;

		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (system.isActive()) return;
            for(ShipAPI check : engine.getShips()){
                if(check.getOwner()==ship.getOwner()){
                    fraction = ship.getHitpoints()/ship.getMaxHitpoints();
                    boolean targetIsVulnerable = check != null && check.getFluxTracker().isOverloadedOrVenting() && 
                    (check.getFluxTracker().getOverloadTimeRemaining() > 5f || 
                    check.getFluxTracker().getTimeToVent() > 5f);
                    if(targetIsVulnerable) fraction/=2;
                    if(fraction<lowest){
                        lowest = fraction;
                        toDock = check;
                    }
                }          
			}
            if(toDock!=null&&lowest<=0.33f){
                ship.setShipTarget(toDock);
                ship.useSystem();
            }
		}
	}
}
