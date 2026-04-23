package org.amazigh.foundry.scripts;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ASF_ExpiationOnExplEffect implements ProximityExplosionEffect {
	
	private static final Color STAGE_4_COLOR = new Color(255,0,150,200);
	private static final Color STAGE_4B_COLOR = new Color(225,0,175,120);
	
	
	public void onExplosion(final DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		final CombatEngineAPI engine = Global.getCombatEngine();
		
		final Vector2f point = explosion.getLocation();
 
		engine.addHitParticle(
                point,
                explosion.getVelocity(),
                180f,
                0.8f,
                0.1f,
                STAGE_4_COLOR);
		
        for (int i = 0; i < 7; i++) {
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 17f),
    				MathUtils.getRandomNumberInRange(37f, 51f), //size
    				1.8f, //endSizeMult
    				0f, //rampUpFraction
    				0.5f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1f, 1.6f), //dur
    				STAGE_4B_COLOR.darker());
        }
        
        ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) explosion);
        emitter.location(point);
        emitter.life(1.0f, 1.35f);
        emitter.size(2f, 4f);
        emitter.velocity(0f, 8f);
        emitter.distance(40f, 25f);
        emitter.color(STAGE_4_COLOR.getRed(),STAGE_4_COLOR.getGreen(),STAGE_4_COLOR.getBlue(),STAGE_4_COLOR.getAlpha());
        emitter.burst(100);
        
        //TODO - real vfx!
        
        //TODO - vortex!
        
        engine.addPlugin(new EveryFrameCombatPlugin() {
			
            final float baseDamage = explosion.getDamageAmount() * 0.333333f;
            
            final IntervalUtil FXTimer1 = new IntervalUtil(0.1f, 0.1f);
            final IntervalUtil DamageTimer = new IntervalUtil(0.8f, 0.8f);
            float timer = 0f;
            
            private Map<HullSize, Float> impulseMult = new HashMap<HullSize, Float>();
        	{
        		impulseMult.put(HullSize.FIGHTER, 10f);
        		impulseMult.put(HullSize.FRIGATE, 18f);
        		impulseMult.put(HullSize.DESTROYER, 28f);
        		impulseMult.put(HullSize.CRUISER, 38f);
        		impulseMult.put(HullSize.CAPITAL_SHIP, 45f);
        		impulseMult.put(HullSize.DEFAULT, 30f);
        	}
        	
            @Override
            public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
            	
            }
            
            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                if (engine.isPaused()) return;
                timer += amount;
                
                float rangeScale = 40f;
                
                float mult = 2 + (timer*30);
                if (timer > 1f) {
                	mult = 32 - (timer*2);
                }
                
                FXTimer1.advance(amount);
                if (FXTimer1.intervalElapsed()) {
                	
                	//TODO - vortexc stufffff!
                		//TODO - main glow
                		//TODO - shrinking glows (?)
                		//TODO - neb(?) core
                		//TODO - sound?
                	
                	for (ShipAPI target_ship : engine.getShips()) {
                		
                		float suckRange = rangeScale * mult * 1.5f;
        				float tag_radius = target_ship.getCollisionRadius();
        				float dist = MathUtils.getDistanceSquared(point, target_ship.getLocation());
                		if (dist <= ((suckRange * suckRange) + (tag_radius * tag_radius) + 200f)) {
                			CombatUtils.applyForce(target_ship, Misc.getAngleInDegrees(target_ship.getLocation(), point), impulseMult.get(((ShipAPI) target_ship).getHullSize())); // the suck!
                		}
                		if (dist <= 1000f) {
                			target_ship.getVelocity().scale(0.95f); // 5% slow, to make stuff "stick" to the middle of the vortex somewhat 
                		}
                		
                	}
                	
                	//TODO - look at this? add more layers? idkkk?
                	int burstCount = (int) (5 * mult * mult);
            		ASF_RadialEmitter emitterVortex = new ASF_RadialEmitter(null);
            		emitterVortex.location(point);
            		emitterVortex.life(0.2f, 0.55f);
            		emitterVortex.size(8f, 10f);
            		emitterVortex.velocity(1f, 110f);
            		emitterVortex.distance(20f, rangeScale * mult);
            		emitterVortex.color(STAGE_4_COLOR.getRed(),STAGE_4_COLOR.getGreen(),STAGE_4_COLOR.getBlue(),STAGE_4_COLOR.getAlpha());
            		emitterVortex.emissionOffset(95f, 15f);
            		emitterVortex.lifeLinkage(true);
            		emitterVortex.sizeScale(-6f);
            		emitterVortex.burst(burstCount);
        			
                }

               DamageTimer.advance(amount);
                if (DamageTimer.intervalElapsed()) {
                	
                	DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                			rangeScale * mult * 0.8f,
                			rangeScale * mult * 0.3f,
                            baseDamage * 0.1f,
                            baseDamage * 0.05f,
                            CollisionClass.PROJECTILE_FF,
                            CollisionClass.PROJECTILE_FIGHTER,
                            4f,
                            4f,
                            1f,
                            0,
                            STAGE_4_COLOR,
                            STAGE_4_COLOR);
                    blast.setDamageType(DamageType.ENERGY);
                    blast.setShowGraphic(false);
                    engine.spawnDamagingExplosion(blast,explosion.getSource(),point,false);
                	
                	//TODO - damage!!
                }
                
                
                if (timer > 8.1f) {
                	engine.removePlugin(this);
                }
                
            }

            @Override
            public void renderInWorldCoords(ViewportAPI viewport) {

            }

            @Override
            public void renderInUICoords(ViewportAPI viewport) {

            }

            @Override
            public void init(CombatEngineAPI engine) {

            }
        });
        
       
	}
}

