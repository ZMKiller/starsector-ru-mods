package data.ai;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

import data.shipsystems.scripts.HSIPOZ;

public class HSIPOZAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;


	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.engine = engine;
		this.system = system;
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if(system.getCooldownRemaining()>0) return;
		if(target==null) return;
		if(system.getAmmo()<=0) return;
		if(system.getFluxPerUse()>(1-ship.getFluxLevel())*2*ship.getMaxFlux()) return;
		int closeEnemies = AIUtils.getNearbyEnemies(ship, HSIPOZ.getRange(ship)).size();
		int FarEnemies = AIUtils.getNearbyEnemies(ship, (1.5f)*HSIPOZ.getRange(ship)).size();
		if(closeEnemies>0&&FarEnemies<=closeEnemies+1){
			ship.useSystem();
		}else if(closeEnemies>=5){
			ship.useSystem();
		}
	}
}
