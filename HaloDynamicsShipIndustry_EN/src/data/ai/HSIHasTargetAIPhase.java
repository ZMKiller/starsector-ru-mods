package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

public class HSIHasTargetAIPhase implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;


	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.engine = engine;
		this.system = ship.getPhaseCloak();
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if(system.getCooldownRemaining()>0) return;
		if(target==null) return;
		if(system.getAmmo()<=0) return;
		if(system.getFluxPerUse()>(1-ship.getFluxLevel())*2*ship.getMaxFlux()) return;
		ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, target.getLocation(), 0);
	}
}
