package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.List;

public class ASF_chillblainDetonator extends BaseEveryFrameCombatPlugin {

	private static final float TIMER = 6f; // the time the missile lasts before automatically bursting
	
	private MissileAPI missile;
	private float fxCounter;
	private float timeCounter;
	
	public ASF_chillblainDetonator(@NotNull MissileAPI missile) {
		this.missile = missile;
	}

	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused()) {
			amount = 0f;
		}

		//Checks if our script should be removed from the combat engine
		if (missile == null || missile.didDamage() || missile.isFading() || !engine.isEntityInPlay(missile)) {
			engine.removePlugin(this);
			return;
		}
		
		
		// spawn a "fancy" trail
		fxCounter += amount;
		if (fxCounter > 0.05f) {
			for (int i=0; i < 3; i++) {
                engine.addSmoothParticle(missile.getLocation(),
						MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(20f, 60f)),
						MathUtils.getRandomNumberInRange(4f, 12f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(1.0f, 1.7f), //duration
		                new Color(35,65,195,240));
			}
			
			engine.addNebulaParticle(missile.getLocation(),
					MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(10f, 20f)),
					MathUtils.getRandomNumberInRange(30f, 35f),
					MathUtils.getRandomNumberInRange(0.4f, 0.75f),
					0.5f,
					0.25f,
					MathUtils.getRandomNumberInRange(1.0f, 1.4f),
					new Color(50,120,225,160));
			
			fxCounter -= 0.05f;
		}
		
		//Ticks up our timer, and if over the detonation time, do the burst
		timeCounter+=amount;
		if (timeCounter > TIMER) {

			Vector2f fieldRandomVel = MathUtils.getRandomPointInCircle(null, 5f);
			CombatEntityAPI proj = engine.spawnProjectile(missile.getWeapon().getShip(), missile.getWeapon(), "A_S-F_chillblain_field", missile.getLocation(), missile.getFacing(), fieldRandomVel);
			engine.addPlugin(new ASF_chillblainProjScript((DamagingProjectileAPI) proj));
			
			for (int i=0; i < 5; i++) {
				Vector2f offsetVel0 = MathUtils.getRandomPointInCircle(proj.getVelocity(), 45f);
	            Vector2f point0 = MathUtils.getRandomPointInCircle(missile.getLocation(), 25f);
	            Vector2f offsetVel01 = MathUtils.getRandomPointInCircle(proj.getVelocity(), 45f);
	            Vector2f point01 = MathUtils.getRandomPointInCircle(missile.getLocation(), 25f);
				
				engine.addNebulaParticle(point0,
						offsetVel0,
						MathUtils.getRandomNumberInRange(60f, 75f),
						MathUtils.getRandomNumberInRange(0.4f, 0.6f),
						0.4f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.7f, 1.1f),
						new Color(40,140,250,225)); 
				engine.addSwirlyNebulaParticle(point01,
						offsetVel01,
						MathUtils.getRandomNumberInRange(60f, 75f),
						MathUtils.getRandomNumberInRange(0.4f, 0.6f),
						0.4f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.7f, 1.1f),
						new Color(40,140,250,225), false);
			}
			
            Global.getCombatEngine().addSmoothParticle(missile.getLocation(),
            		proj.getVelocity(),
            		169f, //size
            		1.0f, //brightness
            		0.15f, //duration
            		new Color(75,175,250,180));
            
            Global.getSoundPlayer().playSound("hit_heavy_energy", 0.8f, 1.25f, missile.getLocation(), missile.getVelocity());
            
            engine.removeEntity(missile);
		}
	}
}