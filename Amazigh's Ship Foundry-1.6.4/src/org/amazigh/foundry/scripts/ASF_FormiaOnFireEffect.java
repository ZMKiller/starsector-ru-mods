package org.amazigh.foundry.scripts;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_FormiaOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            
            //engine.spawnEmpArcVisual(projectile.getLocation(), ship, projectile.getLocation(), projectile, 9f,
    		//		new Color(70,100,155,90),
    		//		new Color(220,225,255,111));
            
            //engine.spawnEmpArcVisual(projectile.getLocation(), ship, MathUtils.getRandomPointOnCircumference(projectile.getLocation(), 3f), projectile, 9f,
    		//		new Color(70,110,200,90),
    		//		new Color(200,225,255,111));
            	// i wanted to have an EMP arc that "linked" the projectile and the weapon, but it just wasn't working out, so i dropped it.
            
    		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.5f, 0.5f, projectile.getLocation(), ship.getVelocity());
            
    		// scripted muzzle vfx
//    		for (int i=0; i < 10; i++) {
//    			// core "muzzle flash"
//                float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-5f, 5f);
//                Vector2f flashVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 15f), angle1);
//                
//                Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(2f, 20f), angle1);
//                
//                Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point1, 3f),
//                		flashVel,
//                		MathUtils.getRandomNumberInRange(8f, 12f), //size
//                		1.0f, //brightness
//                		MathUtils.getRandomNumberInRange(0.15f, 0.25f), //duration
//                		new Color(75,150,240,225));
//                
//                for (int j=0; j < 2; j++) {
//                	// sparkly "particle flash"
//                    Vector2f sparkleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 12f), projectile.getFacing());
//                    Vector2f point2 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(10f, 35f), projectile.getFacing());
//                    
//                    Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point2, 14f),
//                    		sparkleVel,
//                    		MathUtils.getRandomNumberInRange(3f, 5f), //size
//                    		1.0f, //brightness
//                    		MathUtils.getRandomNumberInRange(0.6f, 1.1f), //duration
//                    		new Color(100,200,255,255));
//                }
//            }
    		
    		ASF_RadialEmitter emitter1 = new ASF_RadialEmitter((CombatEntityAPI) ship);
    		emitter1.location(projectile.getLocation());
    		emitter1.angle(projectile.getFacing() -5f, 10f);
    		emitter1.life(0.15f, 0.25f);
    		emitter1.size(8f, 12f);
    		emitter1.distance(2f, 18f);
    		emitter1.velocity(1f, 14f);
    		emitter1.color(75,150,240,225);
    		emitter1.coreDispersion(3f);
    		emitter1.lifeLinkage(false);
    		emitter1.burst(10);
    		
    		ASF_RadialEmitter emitter2 = new ASF_RadialEmitter((CombatEntityAPI) ship);
    		emitter2.location(projectile.getLocation());
    		emitter2.angle(projectile.getFacing(), 0f);
    		emitter2.life(0.6f, 1.1f);
    		emitter2.size(3f, 5f);
    		emitter2.distance(10f, 25f);
    		emitter2.velocity(10f, 25f);
    		emitter2.color(100,200,255,255);
    		emitter2.coreDispersion(14f);
    		emitter2.lifeLinkage(false);
    		emitter2.burst(20);
    		
    		if (weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.ENERGY) {
    			projectile.setDamageAmount(projectile.getBaseDamageAmount()*(1.25f));
    		}
    		
    }
  }