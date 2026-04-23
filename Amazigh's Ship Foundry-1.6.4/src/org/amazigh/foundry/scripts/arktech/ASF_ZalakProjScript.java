//By Nicke535, licensed under CC-BY-NC-SA 4.0 (https://creativecommons.org/licenses/by-nc-sa/4.0/)
// Massively trimmed to be just INTERCEPT_SWARM type tracking
package org.amazigh.foundry.scripts.arktech;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ASF_ZalakProjScript extends BaseEveryFrameCombatPlugin {

	//The maximum range a target can be re-acquired at, in SU.
	//Note that this is counted from the *original* target by default, not the projectile itself (use _PROJ) for that behaviour
	private static final float TARGET_REACQUIRE_RANGE = 750f;

	//The maximum angle a target can be re-acquired at, in degrees.
	//90 means 90 degrees to either side, I.E. a hemisphere in front of the projectile. Values 180 and above turns off the limitation altogether
	private static final float TARGET_REACQUIRE_ANGLE = 69f;
	
	//How fast the projectile is allowed to turn, in degrees/second
	private static final float TURN_RATE_BASE = 13f;
	private static final float TURN_RATE_MULT = 42f;
	
	//If non-zero, the projectile will sway back-and-forth by this many degrees during its guidance (with a sway period determined by SWAY_PERIOD).
	//High values, as one might expect, give very poor tracking. Also, high values will decrease effective range (as the projectiles travel further) so be careful
	//Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
	private static final float SWAY_AMOUNT_PRIMARY = 3f;
	private static final float SWAY_AMOUNT_SECONDARY = 1.9f;
	
	//Used together with SWAY_AMOUNT, determines how fast the swaying happens
	//1f means an entire sway "loop" (max sway right -> min sway -> max sway left -> min sway again) per second, 2f means 2 loops etc.
	//Projectiles start at a random position in their sway loop on spawning
	//Secondary and primary sway both run in parallel, allowing double-sine swaying if desired
	private static final float SWAY_PERIOD_PRIMARY = 0.45f;
	private static final float SWAY_PERIOD_SECONDARY = 1.1f;

	//How fast, if at all, sway falls off with the projectile's lifetime.
	//At 1f, it's a linear falloff, at 2f it's quadratic. At 0f, there is no falloff
	private static final float SWAY_FALLOFF_FACTOR = 0.4f;

	//Only used for the INTERCEPT targeting types: number of iterations to run for calculations.
	//At 0 it's indistinguishable from a dumbchaser, at 15 it's frankly way too high. 4-7 recommended for slow weapons, 2-3 for weapons with more firerate/lower accuracy
	private static final int INTERCEPT_ITERATIONS = 4;
	
	//---Internal script variables: don't touch!---
	private DamagingProjectileAPI proj; //The projectile itself
	private CombatEntityAPI target; // Current target of the projectile
	private Vector2f targetPoint; // For ONE_TURN_TARGET, actual target position. Otherwise, an offset from the target's "real" position. Not used for ONE_TURN_DUMB
	private float swayCounter1; // Counter for handling primary sway
	private float swayCounter2; // Counter for handling secondary sway
	private float lifeCounter; // Keeps track of projectile lifetime
	private float estimateMaxLife; // How long we estimate this projectile should be alive
	
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
	public ASF_ZalakProjScript(@NotNull DamagingProjectileAPI proj, CombatEntityAPI target) {
		this.proj = proj;
		this.target = target;
		swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
		swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
		lifeCounter = 0f;
		estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
		
		if (target != null) {
			applySwarmOffset();
		} else {
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
			if (!Global.getCombatEngine().isEntityInPlay(target)) {
				target = null;
			}
			if (target instanceof ShipAPI) {
				if (((ShipAPI)target).isHulk() || (((ShipAPI) target).isPhased())) {
					target = null;
				}
			}
		}
		
		//If we need to retarget, do so
		if (target == null) {
			//Run retargeting
			reacquireTarget();
		}
		
		
		//If our retargeting failed, just head in a straight line: no script is run
		if (target == null) {
			return;
		}
		
		//Start our guidance stuff...
			//Interceptors use iterative calculations to find an intercept point to the target
			//We use fewer calculation steps for projectiles that are very close, as they aren't needed at close distances
			int iterations = INTERCEPT_ITERATIONS;
			
			float realTurn = TURN_RATE_BASE + (Math.min(1f, lifeCounter) * TURN_RATE_MULT);
			
			float facingSwayless = proj.getFacing() - swayThisFrame;
			Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
			float angleToHit = VectorUtils.getAngle(proj.getLocation(), Vector2f.add(getApproximateInterception(iterations), targetPointRotated, new Vector2f(Misc.ZERO)));
			float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, realTurn*amount);
			proj.setFacing(facingSwayless + swayThisFrame);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless+swayThisFrame).y;
	}


	//Re-acquires a target depending on re-acquiring strategy
	private void reacquireTarget() {
		CombatEntityAPI newTarget = null;
		Vector2f centerOfDetection = proj.getLocation();
		List<CombatEntityAPI> potentialTargets = new ArrayList<>();
		
		for (ShipAPI potTarget : CombatUtils.getShipsWithinRange(centerOfDetection, TARGET_REACQUIRE_RANGE)) {
			if (potTarget.getOwner() == proj.getOwner()
					|| Math.abs(VectorUtils.getAngle(proj.getLocation(), potTarget.getLocation()) - proj.getFacing()) > TARGET_REACQUIRE_ANGLE
					|| potTarget.isHulk()) {
				continue;
			}
			if (potTarget.isPhased()) {
				continue;
			}
			
			potentialTargets.add(potTarget);
			
		}
		//If we found any eligible target, continue selection, otherwise we'll have to stay with no target
		if (!potentialTargets.isEmpty()) {

			for (CombatEntityAPI potTarget : potentialTargets) {
				if (newTarget == null) {
					newTarget = potTarget;
				} else if (MathUtils.getDistance(newTarget, centerOfDetection) > MathUtils.getDistance(potTarget, centerOfDetection)) {
					newTarget = potTarget;
				}
			}

			//Once all that is done, set our target to the new target and select a new swarm point (if appropriate)
			target = newTarget;
			applySwarmOffset();
		}
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
	
	
	//Used for getting a swarm target point, IE a random point offset on the target. Should only be used when target != null
	private void applySwarmOffset() {
		int i = 20; //We don't want to take too much time, even if we get unlucky: only try 40 times
		boolean success = false;
		while (i > 0 && target != null) {
			i--;

			//Get a random position and check if its valid
			Vector2f potPoint = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
			if (CollisionUtils.isPointWithinBounds(potPoint, target)) {
				//If the point is valid, convert it to an offset and store it
				potPoint.x -= target.getLocation().x;
				potPoint.y -= target.getLocation().y;
				potPoint = VectorUtils.rotate(potPoint, -target.getFacing());
				targetPoint = new Vector2f(potPoint);
				success = true;
				break;
			}
		}

		//If we didn't find a point in 40 tries, just choose target center
		if (!success) {
			targetPoint = new Vector2f(Misc.ZERO);
		}
	}
}