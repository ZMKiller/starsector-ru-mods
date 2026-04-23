//By Nicke535, licensed under CC-BY-NC-SA 4.0 (https://creativecommons.org/licenses/by-nc-sa/4.0/)
// massively trimmed to be just a DUMBCAHSER, with no sway
package org.amazigh.foundry.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class ASF_CoralianProjScript extends BaseEveryFrameCombatPlugin {
	//---Settings: adjust to fill the needs of your implementation---
	
	//Determines all valid target types for this projectile's target re-acquiring. Only used if the projectile actually uses re-acquiring of targets
	//Possible values are:
	//	- "ASTEROID"
	//	- "MISSILE"
	//	- "FIGHTER"
	//	- "FRIGATE"
	//	- "DESTROYER"
	//	- "CRUISER"
	//	- "CAPITAL"
	private static final List<String> VALID_TARGET_TYPES = new ArrayList<>();
	static {
		VALID_TARGET_TYPES.add("FIGHTER");
		VALID_TARGET_TYPES.add("FRIGATE");
		VALID_TARGET_TYPES.add("DESTROYER");
		VALID_TARGET_TYPES.add("CRUISER");
		VALID_TARGET_TYPES.add("CAPITAL");
	}

	//How fast the projectile is allowed to turn, in degrees/second
	private static final float TURN_RATE = 540f; // 2700f

	//Delays the activation of the script by a random amount of seconds between this MIN and MAX.
	//Note that ONE_TURN shots will still decide on target angle/point at spawn-time, not when this duration is up
	private static final float GUIDANCE_DELAY_MAX = 0.07f;
	private static final float GUIDANCE_DELAY_MIN = 0.07f;

	//Whether phased ships are ignored for targeting (and an already phased target counts as "lost" and procs secondary targeting)
	private static final boolean BROKEN_BY_PHASE = true;

	//Whether the projectile switches to a new target if the current one becomes an ally
	private static final boolean RETARGET_ON_SIDE_SWITCH = false;

	//---Internal script variables: don't touch!---
	private DamagingProjectileAPI proj; //The projectile itself
	private CombatEntityAPI target; // Current target of the projectile
	private Vector2f targetPoint; // For ONE_TURN_TARGET, actual target position. Otherwise, an offset from the target's "real" position. Not used for ONE_TURN_DUMB
	private float lifeCounter; // Keeps track of projectile lifetime
	private float estimateMaxLife; // How long we estimate this projectile should be alive
	private float delayCounter; // Counter for delaying targeting
	private float actualGuidanceDelay; // The actual guidance delay for this specific projectile


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
	public ASF_CoralianProjScript(@NotNull DamagingProjectileAPI proj, CombatEntityAPI target) {
		this.proj = proj;
		this.target = target;
		lifeCounter = 0f;
		estimateMaxLife = proj.getWeapon().getRange() / new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y).length();
		delayCounter = 0f;
		actualGuidanceDelay = MathUtils.getRandomNumberInRange(GUIDANCE_DELAY_MIN, GUIDANCE_DELAY_MAX);

		{
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

		//Delays targeting if we have that enabled
		if (delayCounter < actualGuidanceDelay) {
			delayCounter+=amount;
			return;
		}
		
		//Check if we need to find a new target
		if (target != null) {
			if (!Global.getCombatEngine().isEntityInPlay(target)) {
				target = null;
			}
			if (target instanceof ShipAPI) {
				if (((ShipAPI)target).isHulk() || (((ShipAPI) target).isPhased() && BROKEN_BY_PHASE) || (target.getOwner() == proj.getOwner() && RETARGET_ON_SIDE_SWITCH)) {
					target = null;
				}
			}
		}

		//If our target is null somehow, just head in a straight line: no script is run
		if (target == null) {
			Global.getCombatEngine().removePlugin(this);
			return;
		}
		
		//Otherwise, we start our guidance stuff...
		else {
			//Dumbchasers just try to point straight at their target at all times
			float facingSwayless = proj.getFacing();
			Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
			float angleToHit = VectorUtils.getAngle(proj.getLocation(), Vector2f.add(target.getLocation(), targetPointRotated, new Vector2f(Misc.ZERO)));
			float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, angleToHit) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
			proj.setFacing(facingSwayless);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless).x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), proj.getVelocity().length(), facingSwayless).y;
		}
	}
	
}