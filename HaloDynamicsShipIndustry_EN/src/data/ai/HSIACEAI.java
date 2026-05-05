package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIACEAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private ShipwideAIFlags flags;
	private WeaponAPI HOH;


	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		for(WeaponAPI weapon:ship.getAllWeapons()){
			if(weapon.getSpec().getWeaponId().equals("HWI_HammerOfHumanity")){
				HOH = weapon;
			}
		}
	}


	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if(HOH!=null&&HOH.isFiring()&&system.getState()==SystemState.IDLE){
			ship.useSystem();
		}
	}
}
