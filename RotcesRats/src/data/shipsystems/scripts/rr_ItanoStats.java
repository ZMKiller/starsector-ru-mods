package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class rr_ItanoStats extends BaseShipSystemScript {

private float RELOAD = 0.1f;
private float CHANCE = 0.1f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		ShipAPI ship = (ShipAPI)stats.getEntity();
		  
		CombatEngineAPI engine = Global.getCombatEngine();
		float timer = engine.getElapsedInLastFrame() * ship.getMutableStats().getTimeMult().getModifiedValue();

		if (state == ShipSystemStatsScript.State.OUT) {
		} else {
			
			RELOAD -= timer;
			while (RELOAD <= 0f) {
				RELOAD += 0.1f;
			
				for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
					float TRUECHANCE = CHANCE * effectLevel;
					if (weapon.isSystemSlot() && (float) Math.random() < TRUECHANCE) {
						
						float WHICHSHOT = (float) Math.random();
						
						float randomArc = MathUtils.getRandomNumberInRange(-18f, 18f);
						randomArc = randomArc * effectLevel;
						
						// 35% chance of a Gyro
						// 30% chance of a "Bearing" rocket
						// 20% chance of a Swarmer
						// 15% chance of a Locust
						
						if (WHICHSHOT <= 0.35) {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_gyro_itano",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
						} else if (WHICHSHOT <= 0.65) {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_bearing_itano",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("annihilator_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.85f;
						} else if (WHICHSHOT <= 0.85) {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_swarmer_itano",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.8f;
						} else {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_locust_itano",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.75f;
						}
						float randomSize = MathUtils.getRandomNumberInRange(8f, 24f);
						engine.addSwirlyNebulaParticle(weapon.computePosition(ship),
								ship.getVelocity(),
								randomSize, //size
								2.0f, //end mult
								0.5f, //ramp fraction
								0.4f, //full bright fraction
								0.7f, //duration
								new Color(125,115,110,150),
								true);
					}
				}
				CHANCE += 0.1f;
			}
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
	  
		if (index == 0) {
		  return new StatusData("It's the Circus", false);
		}
		return null;
	}
}
