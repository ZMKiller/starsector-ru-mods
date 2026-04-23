package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

import org.magiclib.util.MagicLensFlare;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.List;

public class ASF_spellbindProjScript extends BaseEveryFrameCombatPlugin {

	private float timeCounter = 0.15f; // to "jumpstart" the first explosion spawn
	private float fxCounter; // variables to check when to do the stuff.
	private float timeMemory; // variable to remember how long the proj has existed for in total.
	
	private DamagingProjectileAPI proj; // The projectile itself
	
	private static final Color COLOR_N = new Color(210,255,120,160);
	private static final Color COLOR_P = new Color(190,255,125,240);
	private static final Color COLOR_X = new Color(200,235,150,255);
	private static final Color COLOR_X_2 = new Color(220,230,140,200);
	private static final Color COLOR_D_C = new Color(155,155,155,100); // (155,155,155,140)
	private static final Color COLOR_D_F = new Color(170,200,80,70); // (170,200,80,100)
	
	public ASF_spellbindProjScript(@NotNull DamagingProjectileAPI proj) {
		this.proj = proj;
	}
	
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		if (Global.getCombatEngine().isPaused()) {
			amount = 0f;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		
		if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
			engine.removePlugin(this);
			return;
		}
		
		if (proj.getBaseDamageAmount() == 200f) {
			engine.removePlugin(this);
			return;
			// little (unneeded?) sanity check to prevent this from attaching to the ""flak mines"" 
		}
		
		Vector2f point = proj.getLocation();
		
		timeMemory += amount;
		timeMemory = Math.min(5f, timeMemory); // capping time scaling, to prevent it going weird if/when (let's be real, it's when) someone manages to *massively* boost range
		
		// ""trail""
		fxCounter += amount;
		if (fxCounter > 0.05f) {
			
			for (int i=0; i < 3; i++) {
                engine.addSmoothParticle(MathUtils.getRandomPointInCircle(point, 10f),
						MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(30f, 60f + (timeMemory * 20f))),
						MathUtils.getRandomNumberInRange(4f, 12f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(1.0f, 1.6f), //duration
						COLOR_P);
			}
			
			float count_neb = Math.max(0.1f, timeMemory * MathUtils.getRandomNumberInRange(0.34f, 0.69f));
			for (int i=0; i < count_neb; i++) {
				engine.addNebulaParticle(MathUtils.getRandomPointInCircle(point, 12f + (timeMemory * 35f)),
						MathUtils.getRandomPointInCircle(null, 20f + (timeMemory * 3f)),
						MathUtils.getRandomNumberInRange(45f + (timeMemory * 15f), 60f + (timeMemory * 30f)),
						MathUtils.getRandomNumberInRange(0.95f, 1.5f),
						0.6f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.9f, 1.3f),
						COLOR_N);
			}
			
			fxCounter -= 0.05f;
		}
		
		timeCounter += (amount * (1f + (timeMemory / 4f))); // spawn rate increases over proj lifetime
		if (timeCounter >= 0.35f) {
			
			Vector2f fxVel = MathUtils.getRandomPointInCircle(null, 5f);
	        
			float count_b = 0.1f + (timeMemory * MathUtils.getRandomNumberInRange(0.5f, 1f));
			// blast count scales up as time goes up, semi-random
				// 1 guaranteed spawn, and it spawns up to 1 more for each second it has been in flight, but will never be below half of the current max spawn count
			
			float count_f = (timeMemory * MathUtils.getRandomNumberInRange(0.5f, 1.3f)); 
			// flak count scales slightly faster (on average) than blast count
			
			//spawn explosion
			float blastDamage = proj.getDamageAmount() * 0.2f;
			
			DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
	                180f, //160
	                105f, //90
	                blastDamage,
	                blastDamage * 0.6f,
	                CollisionClass.PROJECTILE_FF,
	                CollisionClass.PROJECTILE_FIGHTER,
	                4f,
	                4f,
	                0.6f,
	                40,
	                COLOR_P,
	                COLOR_X);
	        blast.setDamageType(DamageType.ENERGY);
	        blast.setShowGraphic(true);
	        blast.setDetailedExplosionFlashColorCore(COLOR_D_C);
	        blast.setDetailedExplosionFlashColorFringe(COLOR_D_F);
	        blast.setUseDetailedExplosion(true);
	        blast.setDetailedExplosionRadius(140f);
	        blast.setDetailedExplosionFlashRadius(190f);
	        blast.setDetailedExplosionFlashDuration(0.4f);
			
	        
			for (int i=0; i < count_b; i++) {

				float blastDist = Math.min(210f, 50f + (timeMemory * 30f)); //50 + t *25
				
		        Vector2f blastSide = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(-(timeMemory * 55f), timeMemory * 55f), proj.getFacing() + 90f);
		        Vector2f blastPoint = MathUtils.getRandomPointInCircle(blastSide, blastDist);
		        engine.spawnDamagingExplosion(blast, proj.getSource(), blastPoint, false);
		        
		        engine.addNebulaParticle(blastPoint,
						fxVel,
						MathUtils.getRandomNumberInRange(100f, 120f),
						MathUtils.getRandomNumberInRange(1.5f, 2.0f),
						0.8f,
						0.4f,
						MathUtils.getRandomNumberInRange(0.8f, 1.1f),
						COLOR_X_2,
						false);
		        
				for (int j=0; j < 2; j++) {
					
			        MagicLensFlare.createSharpFlare(
						    engine,
						    proj.getSource(),
						    MathUtils.getRandomPointInCircle(blastPoint, 40f),
						    5,
						    290,
						    MathUtils.getRandomNumberInRange(0f, 180f),
						    new Color(90,105,50),
							new Color(190,200,160));
					
				}
			}
			
			for (int i=0; i < count_f; i++) {
				
				float flakDist = Math.min(210f, 50f + (timeMemory * 30f)); //50 + t *25
				
		        //spawn ""flak mine""
				Vector2f flakSide = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(-(timeMemory * 45f), timeMemory * 45f), proj.getFacing() + 90f);
		        Vector2f flakPoint = MathUtils.getRandomPointInCircle(flakSide, flakDist); 
		        
		        engine.spawnProjectile(proj.getSource(),
		        		proj.getWeapon(),
		        		"A_S-F_spellbind_flak",
		        		flakPoint,
		        		proj.getFacing(),
		        		fxVel);
		        
				for (int j=0; j < 7; j++) {
			        engine.addSmoothParticle(flakPoint,
							MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(20f, 20f + (timeMemory * 10f))),
							MathUtils.getRandomNumberInRange(7f, 13f), //size
							1.0f, //brightness
							MathUtils.getRandomNumberInRange(0.7f, 0.9f), //duration
							COLOR_P);
				}
				
				engine.addNebulaParticle(flakPoint,
						MathUtils.getPointOnCircumference(fxVel, 40f, proj.getFacing()),
						MathUtils.getRandomNumberInRange(45f, 60f),
						1.25f,
						0.69f,
						0.35f,
						MathUtils.getRandomNumberInRange(1.1f, 1.5f),
						COLOR_N);
			}
	        
			//play sound
			Global.getSoundPlayer().playSound("explosion_from_damage", 1.3f, 0.6f, point, fxVel); // "A_S-F_explosion_p_flak", 1f, 1f
	        Global.getSoundPlayer().playSound("hit_heavy_energy", 1.25f, 0.6f, point, fxVel);
			
			timeCounter -= 0.36f;
		}
	}

}