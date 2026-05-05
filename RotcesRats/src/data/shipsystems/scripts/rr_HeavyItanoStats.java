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
import org.lwjgl.util.vector.Vector2f;

public class rr_HeavyItanoStats extends BaseShipSystemScript {

private float RELOAD = 0.1f;
private float CHANCE = 0.1f;

public static final float DAMAGE_REDUCTION = 0.2f; // secret addition, because the ship was failing to live up to it's *18 dp* cost
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		ShipAPI ship = (ShipAPI)stats.getEntity();
		  
		CombatEngineAPI engine = Global.getCombatEngine();
		float timer = engine.getElapsedInLastFrame() * ship.getMutableStats().getTimeMult().getModifiedValue();
		
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		
		if (state == ShipSystemStatsScript.State.OUT) {
		} else {
			
			RELOAD -= timer;
			while (RELOAD <= 0f) {
				RELOAD += 0.1f;
			
				for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
					float TRUECHANCE = CHANCE * effectLevel;
					if (weapon.isSystemSlot() && (float) Math.random() < TRUECHANCE) {
						
						float WHATSHOT = (float) Math.random();
						
						float WHICHSHOT = WHATSHOT * effectLevel;
							// so it won't fire the spicier shit when spooling up/down
							// will also get a burst of kinetics at start/end, which helps with defeating shields
						
						float randomArc = MathUtils.getRandomNumberInRange(-12f, 12f);
						randomArc = randomArc * effectLevel;
						
						// 35% chance of a "Squall" rocket
						// 10% chance of 3x frag damage prox bombs
						// 20% chance of an Arpo
						// 20% chance of a "Pilum" (second stage)
						// 5% chance of a Breach
						// 10% chance of a Newt
						
						if (WHICHSHOT <= 0.35) {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_squall_itano",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("squall_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.9f;
						} else if (WHICHSHOT <= 0.45) {
					        for (int i = 0; i< 3; i++) {
					        	
					        	float randomArc2 = MathUtils.getRandomNumberInRange(-18f, 18f);
								randomArc2 = randomArc2 * effectLevel;
					        	
								Vector2f vel = (Vector2f) (ship.getVelocity());
				                Vector2f mineRandomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(11f, 51f));
				                mineRandomVel.x += vel.x;
				                mineRandomVel.y += vel.y;
								
					        	Global.getCombatEngine().spawnProjectile(ship,
					        			null,
					        			"rr_mine_itano",
					        			weapon.computePosition(ship),
					        			weapon.getAngle() + ship.getFacing() + randomArc2,
					        			mineRandomVel);
					        }
							Global.getSoundPlayer().playSound("bomb_bay_fire", 1f, 0.8f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.8f;
						} else if (WHICHSHOT <= 0.65) {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_arpo_f",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("harpoon_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.8f;
						} else if (WHICHSHOT <= 0.85) {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_pilum_itano",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("pilum_lrm_fire", 1f, 0.8f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.75f;
						} else if (WHICHSHOT <= 0.9) {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"breach",
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("breach_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.75f;
						} else {
							Global.getCombatEngine().spawnProjectile(ship,
									null,
									"rr_newt", // heatseeker
									weapon.computePosition(ship),
									weapon.getAngle() + ship.getFacing() + randomArc,
									ship.getVelocity());
							Global.getSoundPlayer().playSound("salamander_fire", 1f, 0.8f, ship.getLocation(), ship.getVelocity());
							CHANCE = CHANCE * 0.7f;
						}
						float randomSize = MathUtils.getRandomNumberInRange(15f, 33f);
						engine.addSwirlyNebulaParticle(weapon.computePosition(ship),
                ship.getVelocity(),
                randomSize, //size
                2.0f, //end mult
                0.5f, //ramp fraction
                0.6f, //full bright fraction
                0.9f, //duration
                new Color(125,115,110,190),
                true);
					}
				}
				CHANCE += 0.12f;
			}
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
		
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
	  
		if (index == 0) {
		  return new StatusData("It's the Circus", false);
		}
		return null;
	}
}
