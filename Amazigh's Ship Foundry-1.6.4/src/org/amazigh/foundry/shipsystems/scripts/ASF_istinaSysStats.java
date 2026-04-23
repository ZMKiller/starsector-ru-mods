package org.amazigh.foundry.shipsystems.scripts;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class ASF_istinaSysStats extends BaseShipSystemScript {

	public static final float SPEED_MALUS = 0.5f;
	public static final float RANGE_BOOST = 80f;
	public static final float RoF_BOOST = 10f;

	private String TagWeapon = "A_S-F_targetinglaser_silaha"; // the id of the weapon to use for the targeting laser beam.
	//TODO - a real weapon
	private boolean init = false;
    protected ShipAPI beamDrone;
    
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = (ShipAPI)stats.getEntity();
		
		if (!ship.isAlive()) {
			if (beamDrone != null) {
				Global.getCombatEngine().removeEntity(beamDrone);
			}
			return;
		}
		
		stats.getMaxSpeed().modifyMult(id, 1f - (SPEED_MALUS * effectLevel));
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, (RANGE_BOOST * effectLevel)); // stat boosts
		stats.getEnergyRoFMult().modifyMult(id, 1f + (RoF_BOOST * effectLevel));

		float beamRange = 700;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getSize() == WeaponSize.LARGE) {
				beamRange = w.getRange();
			}
		}
		
		if (!init) {
			ShipHullSpecAPI spec = Global.getSettings().getHullSpec("dem_drone");
			ShipVariantAPI v = Global.getSettings().createEmptyVariant("dem_drone", spec);
			v.addWeapon("WS 000", TagWeapon);
			WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
			g.addSlot("WS 000");
			v.addWeaponGroup(g);
			
			beamDrone = Global.getCombatEngine().createFXDrone(v);
			beamDrone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
			beamDrone.setOwner(ship.getOriginalOwner());
			beamDrone.getMutableStats().getBeamWeaponRangeBonus().modifyFlat("dem", beamRange);
			beamDrone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
			beamDrone.setDrone(true);
			beamDrone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, ship);
			beamDrone.getMutableStats().getEnergyWeaponDamageMult().applyMods(ship.getMutableStats().getEnergyWeaponDamageMult());
			beamDrone.setCollisionClass(CollisionClass.NONE);
			beamDrone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
			Global.getCombatEngine().addEntity(beamDrone);
			
			init = true;
		}
		
		Vector2f tagpoint = new Vector2f();
		
		for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			  if (weapon.isSystemSlot() && weapon.getSlotSize() == WeaponSize.LARGE) {
				  tagpoint = weapon.computePosition(ship);
			  }
		}
		
		beamDrone.getLocation().set(tagpoint);
		beamDrone.setFacing(ship.getFacing());
		beamDrone.getVelocity().set(ship.getVelocity());
		
		WeaponAPI tLaser = beamDrone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
		tLaser.setFacing(ship.getFacing());
		tLaser.setKeepBeamTargetWhileChargingDown(true);
		tLaser.setScaleBeamGlowBasedOnDamageEffectiveness(false);
		tLaser.updateBeamFromPoints();
		
		//TODO - add "sweep"
		beamDrone.giveCommand(ShipCommand.FIRE, MathUtils.getPointOnCircumference(tagpoint, 100f, ship.getFacing()), 0);
		//TODO - add "sweep"
		
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getSize() == WeaponSize.LARGE) continue;
			
			w.setForceNoFireOneFrame(true);
			w.setGlowAmount(0, null);
			// if the weapon is not large size (so not the Expiation), block firing during system activation, and disable any system/etc weapon glow.
		}
		
		
		
		//TODO - glows on the reactors
		
		//TODO - visual engine size reduction
		
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		
		init = false;
		
		stats.getMaxSpeed().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);

		if (beamDrone != null) {
			Global.getCombatEngine().removeEntity(beamDrone);
		}
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("-" + (int)SPEED_MALUS + "% top speed", true);
		}
		return null;
	}
}