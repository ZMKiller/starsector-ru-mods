//By Nicke535, licensed under CC-BY-NC-SA 4.0 (https://creativecommons.org/licenses/by-nc-sa/4.0/)
// Massively trimmed to be just an INTERCEPT type AI, that continues to home in on the targets last position if it dies. 
package org.amazigh.foundry.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class ASF_diadolProjScript extends BaseEveryFrameCombatPlugin {

	//How fast the projectile is allowed to turn, in degrees/second
	private static final float TURN_RATE = 12f;

	//If non-zero, the projectile will sway back-and-forth by this many degrees during its guidance (with a sway period determined by SWAY_PERIOD).
	//High values, as one might expect, give very poor tracking. Also, high values will decrease effective range (as the projectiles travel further) so be careful
	//Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
	private static final float SWAY_AMOUNT_PRIMARY = 3.1f;
	private static final float SWAY_AMOUNT_SECONDARY = 1.3f;

	//Used together with SWAY_AMOUNT, determines how fast the swaying happens
	//1f means an entire sway "loop" (max sway right -> min sway -> max sway left -> min sway again) per second, 2f means 2 loops etc.
	//Projectiles start at a random position in their sway loop on spawning
	//Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
	private static final float SWAY_PERIOD_PRIMARY = 0.6f;
	private static final float SWAY_PERIOD_SECONDARY = 1.4f;

	//How fast, if at all, sway falls off with the projectile's lifetime.
	//At 1f, it's a linear falloff, at 2f it's quadratic. At 0f, there is no falloff
	private static final float SWAY_FALLOFF_FACTOR = 0.5f;

	//Only used for the INTERCEPT targeting types: number of iterations to run for calculations.
	//At 0 it's indistinguishable from a dumbchaser, at 15 it's frankly way too high. 4-7 recommended for slow weapons, 2-3 for weapons with more firerate/lower accuracy
	private static final int INTERCEPT_ITERATIONS = 5;
	
	//---Internal script variables: don't touch!---
	private DamagingProjectileAPI proj; //The projectile itself
	private CombatEntityAPI target; // Current target of the projectile
	private Vector2f targetPoint; // For ONE_TURN_TARGET, actual target position. Otherwise, an offset from the target's "real" position. Not used for ONE_TURN_DUMB
	private float swayCounter1; // Counter for handling primary sway
	private float swayCounter2; // Counter for handling secondary sway
	private float lifeCounter; // Keeps track of projectile lifetime
	private float estimateMaxLife; // How long we estimate this projectile should be alive
	private Vector2f lastTargetPos; // The last position our target was located at, for "dumb" mode when the target is dead.
	private boolean targetDead = false; // if the target is Dead, and whether we should swap to "dumb" mode.
	

	/**
	 * Initializer for the guided projectile script
	 *
	 * @param proj
	 * The projectile to affect. proj.getWeapon() must be non-null.
	 *
	 * @param target
	 * The target missile/asteroid/ship for the script's guidance.
	 * Can be null, if the script does not follow a target ("ONE_TURN_DUMB") or to instantly activate secondary guidance mode.
	 */
	public ASF_diadolProjScript(@NotNull DamagingProjectileAPI proj, CombatEntityAPI target) {
		this.proj = proj;
		this.target = target;
		lastTargetPos = target != null ? target.getLocation() : new Vector2f(proj.getLocation());
		swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
		swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
		lifeCounter = 0f;
		estimateMaxLife = 5000 / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
		
		if (target != null) {
			targetPoint = new Vector2f(Misc.ZERO);
		}
	}


	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		if (Global.getCombatEngine().isPaused()) {
			amount = 0f;
		}

		//Checks if our script should be removed from the combat engine
		if (proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj)) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}

		//Ticks up our life counter: if we miscalculated, also top it off
		lifeCounter+=amount;
		if (lifeCounter > estimateMaxLife) { lifeCounter = estimateMaxLife; }
		
		//Tick the sway counter up here regardless of if we need it or not: helps reduce boilerplate code
		swayCounter1 += amount*SWAY_PERIOD_PRIMARY;
		swayCounter2 += amount*SWAY_PERIOD_SECONDARY;
		float swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
				((float)(FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float)(FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));
		
		//Check if we need to find a new target
		if (target != null) {
			if (!Global.getCombatEngine().isEntityInPlay(target) || ((ShipAPI)target).isHulk()) {
				target = null;
			}
		}
		
		//If target is dead, force set target position.
		if (target == null) {
			targetDead = true;
		}
		
		//Otherwise, we store the location of our target in case we need to retarget next frame
		else {lastTargetPos = new Vector2f(target.getLocation());}
		
		
		//If our retargeting failed, just head in a straight line: no script is run
		if (target == null) {
			return;
		}
		
		//Start our guidance stuff...
			//Interceptors use iterative calculations to find an intercept point to the target
			//We use fewer calculation steps for projectiles that are very close, as they aren't needed at close distances
			
			float facingSwayless = proj.getFacing() - swayThisFrame;
			
			float angleToHit = 1f;
			if (targetDead) {
				angleToHit = VectorUtils.getAngle(proj.getLocation(), lastTargetPos);
			} else {
				Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
				angleToHit = VectorUtils.getAngle(proj.getLocation(), Vector2f.add(getApproximateInterception(INTERCEPT_ITERATIONS), targetPointRotated, new Vector2f(Misc.ZERO)));
			}
			
			float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
			proj.setFacing(facingSwayless + swayThisFrame);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
	}

	//Iterative intercept point calculation: has option for taking more or less calculation steps to trade calculation speed for accuracy
	private Vector2f getApproximateInterception(int calculationSteps) {
		Vector2f returnPoint = new Vector2f(target.getLocation());
		
		//Iterate a set amount of times, improving accuracy each time
		for (int i = 0; i < calculationSteps; i++) {
			//Get the distance from the current iteration point and the projectile, and calculate the approximate arrival time
			float arrivalTime = MathUtils.getDistance(proj.getLocation(), returnPoint)/proj.getVelocity().length();
			
			//Calculate the targeted point with this arrival time
			returnPoint.x = target.getLocation().x + (target.getVelocity().x * arrivalTime);
			returnPoint.y = target.getLocation().y + (target.getVelocity().y * arrivalTime);
		}
		
		return returnPoint;
	}

}