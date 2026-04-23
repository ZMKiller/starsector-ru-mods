//By Nicke535, licensed under CC-BY-NC-SA 4.0 (https://creativecommons.org/licenses/by-nc-sa/4.0/)
// massively trimmed to be just a one-turn-dumb, with no sway
package org.amazigh.foundry.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class ASF_CoralianDumbProjScript extends BaseEveryFrameCombatPlugin {
	//---Settings: adjust to fill the needs of your implementation---
	
	//How fast the projectile is allowed to turn, in degrees/second
	private static final float TURN_RATE = 540f; // 2700f
	
	//Only used for ONE_TURN_DUMB: the actual target angle is randomly offset by this much, to simulate inaccuracy
	//2f means up to 2 degrees angle off from the actual target angle
	private static final float ONE_TURN_DUMB_INACCURACY = 0f;
	
	//Delays the activation of the script by a random amount of seconds between this MIN and MAX.
	//Note that ONE_TURN shots will still decide on target angle/point at spawn-time, not when this duration is up
	private static final float GUIDANCE_DELAY_MAX = 0.08f;
	private static final float GUIDANCE_DELAY_MIN = 0.08f;

	//---Internal script variables: don't touch!---
	private DamagingProjectileAPI proj; //The projectile itself
	private float targetAngle; // Only for ONE_TURN_DUMB, the target angle that we want to hit with the projectile
	private float delayCounter; // Counter for delaying targeting
	private Vector2f offsetVelocity; // Only used for ONE_TURN_DUMB: keeps velocity from the ship and velocity from the projectile separate (messes up calculations otherwise)
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
	public ASF_CoralianDumbProjScript(@NotNull DamagingProjectileAPI proj) {
		this.proj = proj;
		delayCounter = 0f;
		actualGuidanceDelay = MathUtils.getRandomNumberInRange(GUIDANCE_DELAY_MIN, GUIDANCE_DELAY_MAX);

		//For one-turns, we set our target point ONCE and never adjust it
		targetAngle = proj.getWeapon().getCurrAngle() + MathUtils.getRandomNumberInRange(-ONE_TURN_DUMB_INACCURACY, ONE_TURN_DUMB_INACCURACY);
		offsetVelocity = proj.getSource().getVelocity();
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
		
		//Delays targeting if we have that enabled
		if (delayCounter < actualGuidanceDelay) {
			delayCounter+=amount;
			return;
		}
		
		//Start our guidance stuff...
		//Dumb one-turns just turn toward an angle, though they also need to compensate for offset velocity to remain straight
		
		float facingSwayless = proj.getFacing();
		float angleDiffAbsolute = Math.abs(targetAngle - facingSwayless);
		while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
		facingSwayless += Misc.getClosestTurnDirection(facingSwayless, targetAngle) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
		Vector2f pureVelocity = new Vector2f(proj.getVelocity());
		pureVelocity.x -= offsetVelocity.x;
		pureVelocity.y -= offsetVelocity.y;
		proj.setFacing(facingSwayless);
		proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless).x + offsetVelocity.x;
		proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), pureVelocity.length(), facingSwayless).y + offsetVelocity.y;
	}

}