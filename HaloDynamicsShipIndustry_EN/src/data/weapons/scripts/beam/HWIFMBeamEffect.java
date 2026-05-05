package data.weapons.scripts.beam;

import java.awt.Color;

import data.hullmods.WeaponMod.HSIProtocalMeltDown;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HWIFMBeamEffect implements BeamEffectPlugin {
	private boolean wasZero = false;
	public static Color STANDARD_RIFT_COLOR = new Color(100, 60, 255, 255);
	public static Color EXPLOSION_UNDERCOLOR = new Color(50, 50, 225, 100);
	public static Color NEBULA_SOURCE_COLOR = new Color(80, 105, 250, 75);
	private IntervalUtil nebula = new IntervalUtil(0.1f, 0.15f);
	private IntervalUtil metor = new IntervalUtil(0.02f, 0.04f);
	private IntervalUtil timer = new IntervalUtil(0.9f, 0.9f);
	private FaderUtil fader = new FaderUtil(1f, 0.5f);
	private RippleDistortion distortion = null;
	private RippleDistortion self = null;
	protected boolean once = true;
	protected int current = 0;
	protected float max = 0;


	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (engine.isPaused())
			return;
		nebula.advance(amount);
		max = Math.max(max, beam.getBrightness());
		if (self == null) {
			self = new RippleDistortion(beam.getWeapon().getFirePoint(0), new Vector2f());
			self.setIntensity(1.25f);
			self.setSize(80f);
			self.fadeOutSize(1.5f);
			DistortionShader.addDistortion(self);
		} else if (!(max >= 1 && beam.getBrightness() < 1)) {
			self.setLocation(beam.getWeapon().getFirePoint(0));
		}
		if (max < 1 && beam.getBrightness() < 1) {
			if (nebula.intervalElapsed()) {
				spawnNebularParticles(engine, beam);
			}
			metor.advance(amount);
			if (metor.intervalElapsed() && Math.random() < 0.75f) {
				spawnMetor(engine, beam.getWeapon());
			}
		}
		if (max >= 1 && beam.getBrightness() < 1) {
			fader.advance(amount);
			fader.fadeOut();
			Vector2f loc = Vector2f.sub(beam.getFrom(), beam.getRayEndPrevFrame(), null);
			loc.scale(fader.getBrightness());
			loc = Vector2f.add(loc, beam.getRayEndPrevFrame(), loc);
			beam.getFrom().set(loc);
		}
		if (beam.getBrightness() >= 0.25f) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset
			// every frame as it should be
			if (!wasZero)
				dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			WeaponAPI weapon = beam.getWeapon();
			if (beam.getBrightness() >= 1f) {
				if (distortion == null) {
					distortion = new RippleDistortion(beam.getRayEndPrevFrame(), new Vector2f());
					distortion.setSize(200f);
					distortion.fadeOutSize(0.9f);
					DistortionShader.addDistortion(distortion);
				} else {
					distortion.setLocation(beam.getRayEndPrevFrame());
				}
				timer.advance(amount);
				if (timer.intervalElapsed()) {
					DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(createExplosionSpec(weapon.getDerivedStats().getBurstDamage(), 400f),
							beam.getSource(), beam.getRayEndPrevFrame());
					RippleDistortion r = new RippleDistortion();
					r.setLocation(beam.getRayEndPrevFrame());
					r.setSize(350f * 1.5f);
					r.fadeInSize(1.75f);
					r.fadeOutIntensity(1.75f);
					DistortionShader.addDistortion(r);
					explosion.getDamage().setFluxComponent(explosion.getDamageAmount());
				}
			}
		}
	}

	public void spawnMetor(CombatEngineAPI engine, WeaponAPI weapon) {
		DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(), weapon,
				"vpdriver", Misc.getPointWithinRadius(weapon.getFirePoint(0), 8f),
				weapon.getCurrAngle() + 8f * (float) (Math.random() - 0.5f), null);
		proj.getVelocity().scale(2.5f);
		proj.getDamage().setDamage(40 + 160 * (float) Math.random());
		proj.getDamage().setType(DamageType.FRAGMENTATION);
		//proj.getProjectileSpec().getOnFireEffect().onFire(proj, weapon, engine);
	}

	public void spawnNebularParticles(CombatEngineAPI engine, BeamAPI beam) {
		float length = beam.getLengthPrevFrame();
		if (length <= 10f)
			return;

		// NEGATIVE_SOURCE_COLOR = new Color(200,255,200,25);

		Vector2f from = beam.getFrom();
		Vector2f to = beam.getRayEndPrevFrame();

		ShipAPI ship = beam.getSource();

		float angle = Misc.getAngleInDegrees(from, to);
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
		// Vector2f perp1 = new Vector2f(-dir.y, dir.x);
		// Vector2f perp2 = new Vector2f(dir.y, -dir.x);

		// Color color = new Color(150,255,150,25);
		Color color = NEBULA_SOURCE_COLOR;
		// color = Misc.setAlpha(color, 50);

		float sizeMult = 1f;
		sizeMult = 0.67f;

		for (int i = 0; i < 3; i++) {
			float rampUp = 0.25f + 0.25f * (float) Math.random();
			float dur = 1.25f + 0.5f * (float) Math.random();
			// dur *= 2f;
			float size = 150f + 50f * (float) Math.random();
			size *= sizeMult;
			// size *= 0.5f;
			// Vector2f loc = Misc.getPointWithinRadius(from, size * 0.5f);
			// Vector2f loc = Misc.getPointAtRadius(from, size * 0.33f);
			Vector2f loc = Misc.getPointAtRadius(beam.getWeapon().getLocation(), size * 0.33f);
			// engine.addNegativeParticle(loc, ship.getVelocity(), size, rampUp / dur, dur,
			// color);
			engine.addNebulaParticle(loc, ship.getVelocity(), size, 1.5f, rampUp, 0f, dur, color);
			// engine.addNegativeNebulaParticle(loc, ship.getVelocity(), size, 2f, rampUp,
			// 0f, dur, color);
		}

		// if (true) return;

		// particles along the beam
		float spawnOtherParticleRange = 70;
		if (length > spawnOtherParticleRange * 2f && (float) Math.random() < 0.25f) {
			// color = new Color(150,255,150,255);
			color = new Color(150, 105, 250, 75);
			int numToSpawn = (int) ((length - spawnOtherParticleRange) / 100f + 1);
			// numToSpawn = 1;
			if (current < numToSpawn) {
				float distAlongBeam = spawnOtherParticleRange
						+ (length - spawnOtherParticleRange * 2f) * (float) Math.random();
				float groupSpeed = 100f + (float) Math.random() * 100f;
				for (int j = 0; j < 7; j++) {
					float rampUp = 0.25f + 0.25f * (float) Math.random();
					float dur = 1f + 1f * (float) Math.random();
					float size = 100f + 75f * (float) Math.random();
					Vector2f loc = new Vector2f(dir);
					float sign = Math.signum((float) Math.random() - 0.5f);
					loc.scale(distAlongBeam + sign * (float) Math.random() * size * 0.5f);
					Vector2f.add(loc, from, loc);

					// Vector2f off = new Vector2f(perp1);
					// if ((float) Math.random() < 0.5f) off = new Vector2f(perp2);
					//
					// off.scale(size * 0.1f);
					// Vector2f.add(loc, off, loc);

					loc = Misc.getPointWithinRadius(loc, size * 0.25f);

					float dist = Misc.getDistance(loc, to);
					Vector2f vel = new Vector2f(dir);
					if ((float) Math.random() < 0.5f) {
						vel.negate();
						dist = Misc.getDistance(loc, from);
					}

					float speed = groupSpeed;
					float maxSpeed = dist / dur;
					if (speed > maxSpeed)
						speed = maxSpeed;
					vel.scale(speed);
					Vector2f.add(vel, ship.getVelocity(), vel);

					engine.addNebulaParticle(loc, ship.getVelocity(), size, 1.5f, rampUp, 0f, dur, color);
				}
				current++;
			}
		}
	}

	public DamagingExplosionSpec createExplosionSpec(float damage, float radius) {
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.15f, // duration
				radius, // radius
				radius, // coreRadius
				damage, // maxDamage
				damage / 2f, // minDamage
				CollisionClass.PROJECTILE_FF, // collisionClass
				CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
				3f, // particleSizeMin
				5f, // particleSizeRange
				1f, // particleDuration
				200, // particleCount
				STANDARD_RIFT_COLOR, // particleColor
				EXPLOSION_UNDERCOLOR // explosionColor
		);
		spec.setDamageType(DamageType.ENERGY);
		spec.setUseDetailedExplosion(true);
		spec.setSoundSetId("explosion_guardian");
		return spec;
	}
}
