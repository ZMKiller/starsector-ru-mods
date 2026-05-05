package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSILowShieldAI implements ShipSystemAIScript {

	private ShipAPI ship;
	//private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private HSITurbulanceShieldListenerV2 shield;
	private IntervalUtil think = new IntervalUtil(0.2f, 0.3f);


	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		//this.engine = engine;
		this.system = system;
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (shield == null&&ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
			shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
		}
		if (shield == null)
			return;
		think.advance(amount);
		if(think.intervalElapsed()){
		float currShield = shield.getShield().getCurrent();
		float maxShield = shield.getShield().getShieldCap();
		float frac = currShield / maxShield;
		if (ship.getPhaseCloak() != null && ship.getPhaseCloak().getSpecAPI().getId().equals("HSI_EmergencyBarrier")) {
			if(ship.getPhaseCloak().isOn()||ship.getPhaseCloak().isOutOfAmmo()||ship.getPhaseCloak().isCoolingDown()) return;
			if (frac<0.5f&&shield.getShield().getExtra()+shield.getShield().getShieldCap()*0.2f<=shield.getShield().getExtraShieldCap()) {
				ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
			}
		}else if(ship.getSystem()!=null&&ship.getSystem().getSpecAPI().getId().equals("HSI_EmergencyBarrier")){
			if(ship.getSystem().isOn()||ship.getSystem().isOutOfAmmo()||ship.getSystem().isCoolingDown()) return;
			if (frac<0.5f&&shield.getShield().getExtra()+shield.getShield().getShieldCap()*0.2f<=shield.getShield().getExtraShieldCap()) {
				ship.useSystem();
			}
		}
	}
}}
