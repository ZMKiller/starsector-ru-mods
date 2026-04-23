package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_SilahaWeaponScript implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin  {
	
	private float steam = 0f;
	private IntervalUtil steamInterval = new IntervalUtil(0.05f, 0.05f);
	
	private String TagWeapon = "A_S-F_targetinglaser_silaha"; // the id of the weapon to use for the targeting laser beam.
	
	private boolean init = false;
	private boolean fired = true;
	
	private static final Color FLASH_COLOR = new Color(150,95,150,210);
    private static final Color SMK_SPRK_COLOR = new Color(90,170,95,150);
    
    protected ShipAPI demDrone;
    
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
		fired = true;
		
		steam = weapon.getCooldown() * 0.6f;
		// setting the "cooldown" steam to start
		
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = projectile.getLocation();
        Vector2f point = weapon.getFirePoint(0);
        
        engine.spawnExplosion(proj_location, ship_velocity, new Color(175,255,175,120), 25f, 0.18f);
        
        engine.addHitParticle(proj_location, ship_velocity, 75f, 1f, 0.1f, FLASH_COLOR.brighter());
        
        ASF_RadialEmitter emitterFront = new ASF_RadialEmitter((CombatEntityAPI) ship);
        emitterFront.location(proj_location);
		emitterFront.angle(projectile.getFacing() - 5f, 10f);
		emitterFront.life(0.5f, 3f);
		emitterFront.size(2f, 3f);
		emitterFront.velocity(0f, 10f);
		emitterFront.distance(5f, 120f);
		emitterFront.color(150,95,150,210);
		emitterFront.velDistLinkage(false);
		emitterFront.emissionOffset(-85f, 85f);
		emitterFront.coreDispersion(5f);
		emitterFront.burst(115);
        
		ASF_RadialEmitter emitterFront2 = new ASF_RadialEmitter((CombatEntityAPI) ship);
		emitterFront2.location(proj_location);
		emitterFront2.angle(projectile.getFacing() - 3f, 6f);
		emitterFront2.life(1.1f, 1.7f);
		emitterFront2.size(3f, 4f);
		emitterFront2.velocity(9f, 86f);
		emitterFront2.color(90,170,95,150);
		emitterFront2.coreDispersion(3f);
		emitterFront2.burst(54);
		
		
        // spawns particles along an 8px line, centered:  7.5 backwards, 12.5 out to each side
        	// lining up with the "vents" on the sprite!
        
    	Vector2f loc1f = MathUtils.getPointOnCircumference(point, 12.98f, projectile.getFacing() - 105.6f);
    	Vector2f loc1b = MathUtils.getPointOnCircumference(point, 16.98f, projectile.getFacing() - 132.6f);

    	Vector2f loc2f = MathUtils.getPointOnCircumference(point, 12.98f, projectile.getFacing() + 105.6f);
    	Vector2f loc2b = MathUtils.getPointOnCircumference(point, 16.98f, projectile.getFacing() + 132.6f);
    	
        for (int i=0; i < 9; i++) {
        	
        	Vector2f loc1t = MathUtils.getRandomPointOnLine(loc1f, loc1b);
        	
        	Vector2f loc2t = MathUtils.getRandomPointOnLine(loc2f, loc2b);
        	
        	float arcPoint1 = MathUtils.getRandomNumberInRange(projectile.getFacing() - 92f, projectile.getFacing() -88f);
        	float arcPoint2 = MathUtils.getRandomNumberInRange(projectile.getFacing() + 88f, projectile.getFacing() + 92f);
        	
        	Vector2f velocity1 = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(5f, 45f), arcPoint1);
        	Vector2f velocity2 = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(5f, 45f), arcPoint2);
        	
        	engine.addSmoothParticle(loc1t,
            		velocity1,
    				MathUtils.getRandomNumberInRange(4f, 6f), //size
    				1f, //brightness
    				MathUtils.getRandomNumberInRange(0.6f, 0.9f), //duration
    				SMK_SPRK_COLOR.darker());
        	engine.addSmoothParticle(loc2t,
            		velocity2,
    				MathUtils.getRandomNumberInRange(4f, 6f), //size
    				1f, //brightness
    				MathUtils.getRandomNumberInRange(0.6f, 0.9f), //duration
    				SMK_SPRK_COLOR.darker());
        	
        }
        
	}
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
		if (engine.isPaused()) {
			return;
		}
		
		if (!weapon.getShip().isAlive()) {
			if (demDrone != null) {
				engine.removeEntity(demDrone);
			}
			return;
		}
		
		if (!init) {
			ShipHullSpecAPI spec = Global.getSettings().getHullSpec("dem_drone");
			ShipVariantAPI v = Global.getSettings().createEmptyVariant("dem_drone", spec);
			v.addWeapon("WS 000", TagWeapon);
			WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
			g.addSlot("WS 000");
			v.addWeaponGroup(g);
			
			demDrone = Global.getCombatEngine().createFXDrone(v);
			demDrone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
			demDrone.setOwner(weapon.getShip().getOriginalOwner());
			demDrone.getMutableStats().getBeamWeaponRangeBonus().modifyFlat("dem", weapon.getRange());
			demDrone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
			demDrone.setDrone(true);
			demDrone.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, weapon.getShip());
			demDrone.getMutableStats().getEnergyWeaponDamageMult().applyMods(weapon.getShip().getMutableStats().getBallisticWeaponDamageMult());
			demDrone.setCollisionClass(CollisionClass.NONE);
			demDrone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);
			Global.getCombatEngine().addEntity(demDrone);
			
			init = true;
		}
		
		Vector2f tagpoint = MathUtils.getPointOnCircumference(weapon.getFirePoint(0), 13.83f, weapon.getCurrAngle() - 49.4f);
		
		demDrone.getLocation().set(tagpoint);
		demDrone.setFacing(weapon.getCurrAngle());
		demDrone.getVelocity().set(weapon.getShip().getVelocity());
		
		WeaponAPI tLaser = demDrone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
		tLaser.setFacing(weapon.getCurrAngle());
		tLaser.setKeepBeamTargetWhileChargingDown(true);
		tLaser.setScaleBeamGlowBasedOnDamageEffectiveness(false);
		tLaser.updateBeamFromPoints();
		
		if (weapon.getChargeLevel() > 0.05f && !fired) {
			demDrone.giveCommand(ShipCommand.FIRE, MathUtils.getPointOnCircumference(tagpoint, 100f, weapon.getCurrAngle()), 0);
		}
		
		if (fired && weapon.getChargeLevel() < 0.05f) {
			fired = false;
		}
		
		

		// "steam/etc" "vent" visual effect
		
		if (steam > 0f) {
			steamInterval.advance(amount);
			if (steamInterval.intervalElapsed()) {
				steam -= 0.05;
				
				// spawns smoke along an 8px line, centered:  7.5 backwards, 12.5 out to each side
				// lining up with the "vents" on the sprite!
				
				Vector2f point = weapon.getFirePoint(0);
				Vector2f ship_velocity = weapon.getShip().getVelocity();
				
				Vector2f loc1f = MathUtils.getPointOnCircumference(point, 12.98f, weapon.getCurrAngle() - 105.6f);
				Vector2f loc1b = MathUtils.getPointOnCircumference(point, 16.98f, weapon.getCurrAngle() - 132.6f);
				Vector2f loc1t = MathUtils.getRandomPointOnLine(loc1f, loc1b);
				
				Vector2f loc2f = MathUtils.getPointOnCircumference(point, 12.98f, weapon.getCurrAngle() + 105.6f);
				Vector2f loc2b = MathUtils.getPointOnCircumference(point, 16.98f, weapon.getCurrAngle() + 132.6f);
				Vector2f loc2t = MathUtils.getRandomPointOnLine(loc2f, loc2b);
				
				float arcPoint1 = MathUtils.getRandomNumberInRange(weapon.getCurrAngle() - 94f, weapon.getCurrAngle() -86f);
				float arcPoint2 = MathUtils.getRandomNumberInRange(weapon.getCurrAngle() + 86f, weapon.getCurrAngle() + 94f);
				
				Vector2f velocity1 = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(3f, 40f), arcPoint1);
				Vector2f velocity2 = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(3f, 40f), arcPoint2);
				
				float steamMath = Math.min(1.25f, steam);
				
				engine.addNebulaSmokeParticle(loc1t,
						velocity1,
						15f - (steamMath * 4f), //size
						MathUtils.getRandomNumberInRange(1.6f, 1.9f) - (steamMath * 0.32f), //end mult
						0.4f, //ramp fraction
						0.5f, //full bright fraction
						MathUtils.getRandomNumberInRange(0.7f, 1.0f), //duration
						new Color(170f/255f,
								185f/255f,
								180f/255f,
								(30f + (32f * steamMath))/255f));
				engine.addNebulaSmokeParticle(loc2t,
						velocity2,
						15f - (steamMath * 4f), //size
						MathUtils.getRandomNumberInRange(1.6f, 1.9f) - (steamMath * 0.32f), //end mult
						0.4f, //ramp fraction
						0.5f, //full bright fraction
						MathUtils.getRandomNumberInRange(0.7f, 1.0f), //duration
						new Color(170f/255f,
								185f/255f,
								180f/255f,
								(30f + (32f * steamMath))/255f));
				
				engine.addSmoothParticle(loc1t,
						velocity1,
						MathUtils.getRandomNumberInRange(3f, 4f), //size
						0.8f, //brightness
						MathUtils.getRandomNumberInRange(0.55f, 0.75f), //duration
						SMK_SPRK_COLOR);
				engine.addSmoothParticle(loc2t,
						velocity2,
						MathUtils.getRandomNumberInRange(3f, 4f), //size
						0.8f, //brightness
						MathUtils.getRandomNumberInRange(0.55f, 0.75f), //duration
						SMK_SPRK_COLOR);
				
			}
		}
		
	}
  }