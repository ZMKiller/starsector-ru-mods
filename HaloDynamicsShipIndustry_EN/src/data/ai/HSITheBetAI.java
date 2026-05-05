package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSITheBetAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private ShipwideAIFlags flags;
	private HSITurbulanceShieldListenerV2 shield = null;


	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		if(shield == null&&ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)){
			shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
		}
	}


	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if(system.isOutOfAmmo()) return;
		if(system.isCoolingDown()) return;
		if(system.isActive()) return;
		int maxFighters = 0;
		int currFighters = 0;
		for(FighterWingAPI wing:ship.getAllWings()){
			maxFighters+=wing.getSpec().getNumFighters();
			currFighters+=wing.getWingMembers().size();
		}
		float frac = 1;
		if(maxFighters!=0){
			frac = currFighters/maxFighters;
		}
		if(shield == null&&ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)){
			shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
		}
		if((frac<0.2f&&ship.getSharedFighterReplacementRate()<=0.4f)||
		(shield!=null&&(shield.getShield().getCurrent()<shield.getShield().getShieldCap()*0.2f)&&ship.getHullLevel()<0.4f&&ship.areSignificantEnemiesInRange())){
			ship.useSystem();
		}
	}
}
