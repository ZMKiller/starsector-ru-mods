package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.CombatEntityPluginWithParticles;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.scripts.weapons.dpl_song_of_creationEffect.ParticleData;

/**
 * IMPORTANT: will be multiple instances of this, as this doubles as the every frame effect and the on fire effect (same instance)
 * But also as the visual for each individual shot (created via onFire, using the non-default constructor)
 */
public class dpl_song_of_dimensionChargeGlow extends CombatEntityPluginWithParticles {

	public static enum EMPArcHitType {
		SOURCE,
		DEST,
		DEST_NO_TARGET,
	}
	
	public static float ARC_RATE_MULT = 1f;
	
	//public static int MAX_ARC_RANGE = 300;
	public static int MAX_ARC_RANGE = 500;
	public static int DETECTION_ARC_RANGE = 250;
	public static float REAL_PERCENT = 1f;
	public static float SOC_DAM_PERCENT = 0.05f;
	//public static int ARCS_ON_HIT = 15;
	
	public static float REPAIR_RATE_MULT = 0.667f;
	public static float REPAIR_RATE_DEBUFF_DUR = 10f;
	
	public static Color UNDERCOLOR = new Color(60, 0, 100, 100);
	public static Color RIFT_COLOR = new Color(150,60,255,255);
	
	
	public static Object STATUS_KEY = new Object();
	
	protected WeaponAPI weapon;
	protected DamagingProjectileAPI proj;
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
	protected IntervalUtil arcInterval = new IntervalUtil(0.08f, 0.12f);
	protected float delay = 1f;
	protected float effect_time = 0f;
	
	public dpl_song_of_dimensionChargeGlow(WeaponAPI weapon) {
		super();
		this.weapon = weapon;
		this.effect_time = 0f;
		arcInterval = new IntervalUtil(0.08f, 0.12f);
		delay = 0.5f;
	}
	
	public static class ParticleData {
		public DamagingProjectileAPI proj;
        public SpriteAPI sprite;
        public Vector2f offset = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float time = 0f;
        public float rot_rate = 1f;
        public float angle = 1f;
        public float maxDur = 1f;
        public float globalAlpha = 1f;
        public float size = 1f;
        public float scale = 1f;
        public float end_sizeMult = 1f;
        public float init_time = 0f;
        public boolean ifFirstFamily = false;
        public boolean longFadeIn = false;

        public FaderUtil fader;

        public ParticleData(DamagingProjectileAPI proj, SpriteAPI sprite, Vector2f vel, float size, float end_size, float init_time,
                            float angle, float rot_rate, float maxDur, float globalAlpha, boolean additiveBlending, boolean ifFirstFamily, boolean longFadeIn) {
        	this.proj = proj;
            this.sprite = sprite;
            this.rot_rate = rot_rate;
            this.maxDur = maxDur;
            this.globalAlpha = globalAlpha;
            this.angle = angle;
            this.size = size;
            this.end_sizeMult = end_size;
            this.init_time = init_time;
            this.ifFirstFamily = ifFirstFamily;
            this.longFadeIn = longFadeIn;
            this.vel = vel;

            fader = new FaderUtil(0f, 0.25f, 0.75f);
            fader.fadeIn();

            float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			sprite.setTexWidth(0.25f);
			sprite.setTexHeight(0.25f);
			sprite.setTexX(i * 0.25f);
			sprite.setTexY(j * 0.25f);
            
            if (additiveBlending) sprite.setAdditiveBlend();
            
            if (longFadeIn) sprite.setAlphaMult(0f);
        }
        
        public void advance(float amount) {
            angle += rot_rate * amount;
            
            Vector2f perp_vel = new Vector2f(0,0);
            Vector2f zero = new Vector2f(0,0);
            
            if (ifFirstFamily) {
            	perp_vel.x = -1f*vel.y;
            	perp_vel.y = vel.x;
            } else {
            	perp_vel.x = vel.y;
            	perp_vel.y = -1f*vel.x;
            }
            
            Vector2f perp_dir = Misc.getUnitVector(zero, perp_vel);
            
            float perp_rate = 0f;
            
            if (init_time == 0f) {
            	offset = Misc.getPointWithinRadius(zero, 50f);
            } else {
            	perp_rate = 75f * (float) (3f*Math.sin((init_time-time))*Math.cos(3f*time)*Math.atan(time) - Math.cos((init_time-time))*Math.sin(3f*time)*Math.atan(time) + (Math.sin((init_time-time))*Math.sin(3f*time))/(1f+time * time));
            }
            
            offset.x += (vel.x + perp_rate * perp_dir.x) * amount;
			offset.y += (vel.y + perp_rate * perp_dir.y) * amount;
            
            scale += ((end_sizeMult-1f) / maxDur) * amount;
            
            time  += amount; // accumulate time for animation
            fader.advance(amount);
            if (time >= maxDur) {
            	fader.fadeOut();
            }
        }
    }
	
	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	
	public void attachToProjectile(DamagingProjectileAPI proj) {
		this.proj = proj;
	}
	
	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		if (proj != null) {
			entity.getLocation().set(proj.getLocation());
		} else {
			entity.getLocation().set(weapon.getFirePoint(0));
			effect_time = 0f;
		}
		
		float MaxFlightTime = 20f;
		
		if (proj != null) {
			MaxFlightTime = proj.getWeapon().getRange()/proj.getMoveSpeed();
		}
		
		float globalAlpha = (float) (Math.atan(MaxFlightTime - effect_time)/Math.atan(MaxFlightTime));
		
		List<ParticleData> remove = new ArrayList<ParticleData>();
		
		for (ParticleData p : particles) {
            p.advance(amount);
            p.globalAlpha = globalAlpha;
            if (p.time >= p.maxDur) {
            	remove.add(p);
            }
        }
		
		particles.removeAll(remove);
		
		boolean keepSpawningParticles = isWeaponCharging(weapon) || 
					(proj != null && !isProjectileExpired(proj) && !proj.isFading());
		if (keepSpawningParticles) {
			interval.advance(amount);
			if (interval.intervalElapsed()) {
				if (effect_time >= 0.1f) {
					addChargingParticles(weapon, effect_time);
				} else {
					spawnArcWeapon();
				}
			}
		}
		
		if (proj != null && !isProjectileExpired(proj) && !proj.isFading()) {
			delay -= amount;
			
			if (delay <= 0) {
				arcInterval.advance(amount * ARC_RATE_MULT);
				effect_time += amount;
				if (arcInterval.intervalElapsed()) {
					spawnArc();
					spawnArcSoC();
				}
			}
		}
		if (proj != null) {
			Global.getSoundPlayer().playLoop("realitydisruptor_loop", proj, 1f, 1f * proj.getBrightness(),
											 proj.getLocation(), proj.getVelocity());
		}
	}
	
	@Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;
        
        Color color = RIFT_COLOR;

        for (ParticleData p : particles) {

            Vector2f loc = new Vector2f(x, y);
            
            float b = 1f;
    		b *= viewport.getAlphaMult();
            
            p.sprite.setAngle(p.angle + weapon.getCurrAngle() - 90f);
            p.sprite.setSize(p.size * p.scale, p.size * p.scale);
            p.sprite.setAlphaMult(b * ((float) Math.sin((Math.PI*p.time)/p.maxDur)) * p.globalAlpha * p.fader.getBrightness());
            p.sprite.setCenter(p.size * p.scale * 0.5f, p.size * p.scale * 0.5f);
            p.sprite.setColor(color);
            
            p.sprite.renderAtCenter(loc.x + p.offset.x, loc.y + p.offset.y);
        }
    }

	public boolean isExpired() {
		boolean keepSpawningParticles = isWeaponCharging(weapon) || 
					(proj != null && !isProjectileExpired(proj) && !proj.isFading());
		return super.isExpired() && (!keepSpawningParticles || (!weapon.getShip().isAlive() && proj == null));
	}

	
	public float getRenderRadius() {
		return 1200f;
	}
	
	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES);

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return layers;
    }

    @Override
    public void init(CombatEntityAPI entity) {
        super.init(entity);
    }
	
	@Override
	protected float getGlobalAlphaMult() {
		if (proj != null && proj.isFading()) {
			return proj.getBrightness();
		}
		return super.getGlobalAlphaMult();
	}
	
	public void addChargingParticles(WeaponAPI weapon, float effect_time) {
		
		float size = 50f;
		
		if (isWeaponCharging(weapon)) {
			size *= 0.25f + weapon.getChargeLevel() * 0.75f;
		}
		
		Vector2f negVel = new Vector2f(0f,0f);
		
		if (proj != null) {
			negVel.x = -1f*proj.getVelocity().x;
			negVel.y = -1f*proj.getVelocity().y;
		}
		
		SpriteAPI particle_sprite = Global.getSettings().getSprite("misc", "fx_particles2");
		
		float MaxFlightTime = proj.getWeapon().getRange()/proj.getWeapon().getProjectileSpeed();
		float globalAlpha = (float) (Math.atan(MaxFlightTime - effect_time)/Math.atan(MaxFlightTime));
		
		if (proj != null) {
			if (proj.getElapsed() > 0.2f) {
				particles.add(new ParticleData(proj, particle_sprite, negVel, size, 1f, effect_time, (float) Math.random() * 360f, 60f, 5f, globalAlpha, true, true, true));
				particles.add(new ParticleData(proj, particle_sprite, negVel, size, 1f, effect_time, (float) Math.random() * 360f, 60f, 5f, globalAlpha, true, false, true));
			}
			if (proj.getElapsed() > 0.4f) {
				particles.add(new ParticleData(proj, particle_sprite, negVel, size, 1.25f, effect_time, (float) Math.random() * 360f, 60f, 5f, globalAlpha, true, true, true));
				particles.add(new ParticleData(proj, particle_sprite, negVel, size, 1.25f, effect_time, (float) Math.random() * 360f, 60f, 5f, globalAlpha, true, false, true));
			}
			if (proj.getElapsed() > 0.6f) {
				particles.add(new ParticleData(proj, particle_sprite, negVel, size * .8f, 1.1f, effect_time, (float) Math.random() * 360f, 60f, 5f, globalAlpha, true, true, true));
				particles.add(new ParticleData(proj, particle_sprite, negVel, size * .8f, 1.1f, effect_time, (float) Math.random() * 360f, 60f, 5f, globalAlpha, true, false, true));
			}
		}
	}
	
	public void spawnArcWeapon() {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		float thickness = 20f;
		float coreWidthMult = 0.67f;
		Color color = weapon.getSpec().getGlowColor();
		
		Vector2f from = new Vector2f(weapon.getFirePoint(0));
		Vector2f to = pickNoTargetDestWpn(weapon, engine);
		EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, null, to, null, thickness, color, Color.white);
		arc.setCoreWidthOverride(thickness * coreWidthMult);
		arc.setSingleFlickerMode();
			
		Global.getSoundPlayer().playSound("realitydisruptor_emp_impact", 1f, 1f, to, new Vector2f());
			
		spawnEMPParticles(EMPArcHitType.SOURCE, from, null);
		spawnEMPParticles(EMPArcHitType.DEST_NO_TARGET, to, null);
	}
	
	public void spawnArc() {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		float emp = 0f;
		float dam = 1f;
	
		CombatEntityAPI target = findTarget(proj, weapon, engine);
		float thickness = 20f;
		float coreWidthMult = 0.67f;
		Color color = weapon.getSpec().getGlowColor();
		//color = new Color(255,100,100,255);
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArc(proj.getSource(), proj.getLocation(), null,
					   target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "realitydisruptor_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(255,255,255,255)
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			
			boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(arc.getTargetLocation());
			if (target instanceof ShipAPI && hitShield) {
				ShipAPI enemyShip = (ShipAPI) target;
				enemyShip.getFluxTracker().increaseFlux(proj.getDamageAmount(), true);
			}
			
			spawnEMPParticles(EMPArcHitType.SOURCE, proj.getLocation(), null);
			spawnEMPParticles(EMPArcHitType.DEST, arc.getTargetLocation(), target);
			
		} else {
			Vector2f from = new Vector2f(proj.getLocation());
			Vector2f to = pickNoTargetDest(proj, weapon, engine);
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, null, to, null, thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			
			Global.getSoundPlayer().playSound("realitydisruptor_emp_impact", 1f, 1f, to, new Vector2f());
			
			spawnEMPParticles(EMPArcHitType.SOURCE, from, null);
			spawnEMPParticles(EMPArcHitType.DEST_NO_TARGET, to, null);
		}
	}
	
	public void spawnArcSoC() {
		CombatEngineAPI engine = Global.getCombatEngine();
	
		DamagingProjectileAPI SoCProj = findSoCProj(proj, weapon, engine);
		float thickness = 30f;
		float coreWidthMult = 0.67f;
		Color color = new Color(50,100,255,255);
		
		if (SoCProj != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(proj.getLocation(), proj, SoCProj.getLocation(), SoCProj, thickness, color, color);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			
			CombatEntityAPI target = findTarget(SoCProj, weapon, engine);
			
			float emp = SoCProj.getEmpAmount();
			float dam = SoCProj.getDamageAmount()*SOC_DAM_PERCENT;
			
			if (target != null) {
				EmpArcEntityAPI RD_arc = engine.spawnEmpArcPierceShields(proj.getSource(), SoCProj.getLocation(), null,
						   target,
						   DamageType.ENERGY, 
						   dam,
						   emp, // emp 
						   100000f, // max range 
						   "realitydisruptor_emp_impact",
						   thickness, // thickness
						   color,
						   new Color(255,255,255,255)
						   );
				RD_arc.setCoreWidthOverride(thickness * coreWidthMult);
				RD_arc.setSingleFlickerMode();
				
				if (target instanceof ShipAPI) {
					ShipAPI s = (ShipAPI) target;
					float realDam = dam*REAL_PERCENT;
					s.setHitpoints(Math.max(s.getHitpoints() - realDam, 1));
					engine.addFloatingDamageText(RD_arc.getTargetLocation(), realDam, Misc.FLOATY_HULL_DAMAGE_COLOR, s, proj.getSource());
					if (s.getHitpoints() <= 10 && !s.getVariant().hasHullMod("vastbulk")) {
						//Just kill it if its hull point is too low.
						s.getMutableStats().getHullDamageTakenMult().unmodify();
						s.getMutableStats().getArmorDamageTakenMult().unmodify();
						s.setHitpoints(1f);
				        int[] cell = s.getArmorGrid().getCellAtLocation(RD_arc.getTargetLocation());
				        s.getArmorGrid().setArmorValue(cell[0], cell[1], 0f);
						engine.applyDamage(s, RD_arc.getTargetLocation(), 50000f, DamageType.OTHER, 0f, true, false, null);
					}
				}
			}
		}
	}

	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = 120f;
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, range * 0.25f);
		return dir;
	}
	
	public Vector2f pickNoTargetDestWpn(WeaponAPI weapon, CombatEngineAPI engine) {
		float range = 120f;
		Vector2f from = weapon.getFirePoint(0);
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, range * 0.25f);
		return dir;
	}
	
	public DamagingProjectileAPI findSoCProj(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = DETECTION_ARC_RANGE;
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		DamagingProjectileAPI best = null;
		
		float minScore = Float.MAX_VALUE;
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI)) continue;
			MissileAPI other = (MissileAPI) o;
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;
			
			if (other.getWeaponSpec() != null) {
				if (other.getWeaponSpec().getWeaponId() != null) {
					if (!(other.getWeaponSpec().getWeaponId().equals("dpl_song_of_creation"))) continue;
				} else continue;
			} else continue;
			
			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist > range) continue;
			
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
		}
		return best;
	}
	
	public CombatEntityAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = MAX_ARC_RANGE;
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isHulk()) continue;
				if (otherShip.isPhased()) continue;
				if (!otherShip.isTargetable()) continue;
			}
			if (other.getCollisionClass() == CollisionClass.NONE) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius - 50f;
			if (dist > range) continue;
			
			//float angleTo = Misc.getAngleInDegrees(from, other.getLocation());
			//float score = Misc.getAngleDiff(weapon.getCurrAngle(), angleTo);
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
		}
		return best;
	}
	
	public void spawnEMPParticles(EMPArcHitType type, Vector2f point, CombatEntityAPI target) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Color color = RiftLanceEffect.getColorForDarkening(RIFT_COLOR);
		
		float size = 30f;
		float baseDuration = 1.5f;
		Vector2f vel = new Vector2f();
		int numNegative = 5;
		switch (type) {
		case DEST:
			size = 50f;
			vel.set(target.getVelocity());
			if (vel.length() > 100f) {
				vel.scale(100f / vel.length());
			}
			break;
		case DEST_NO_TARGET:
			break;
		case SOURCE:
			size = 40f;
			numNegative = 10;
			break;
		}
		
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getArcFacing() + 180f);
		
		if (proj != null) {
			dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
		}
		
		for (int i = 0; i < numNegative; i++) {
			float dur = baseDuration + baseDuration * (float) Math.random();
			//float nSize = size * (1f + 0.0f * (float) Math.random());
			//float nSize = size * (0.75f + 0.5f * (float) Math.random());
			float nSize = size;
			if (type == EMPArcHitType.SOURCE) {
				nSize *= 1.5f;
			}
			Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
			Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
			v.scale(nSize + nSize * (float) Math.random() * 0.5f);
			v.scale(0.2f);
			
			float endSizeMult = 2f;
			if (type == EMPArcHitType.SOURCE) {
				pt = Misc.getPointWithinRadius(point, nSize * 0f);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 0.2f * i);
				Vector2f.add(pt, offset, pt);
				endSizeMult = 1.5f;
				v.scale(0.5f);
			}
			Vector2f.add(vel, v, v);
			
			float maxSpeed = nSize * 1.5f * 0.2f; 
			float minSpeed = nSize * 1f * 0.2f; 
			float overMin = v.length() - minSpeed;
			if (overMin > 0) {
				float durMult = 1f - overMin / (maxSpeed - minSpeed);
				if (durMult < 0.1f) durMult = 0.1f;
				dur *= 0.5f + 0.5f * durMult;
			}
			
//			if (type == EMPArcHitType.DEST || type == EMPArcHitType.DEST_NO_TARGET) {
//				v.set(0f, 0f);
//			}
			
			engine.addNegativeNebulaParticle(pt, v, nSize * 1f, endSizeMult,
			//engine.addNegativeSwirlyNebulaParticle(pt, v, nSize * 1f, endSizeMult,
											0.25f / dur, 0f, dur, color);
		}
		
		float dur = baseDuration; 
		float rampUp = 0.5f / dur;
		color = UNDERCOLOR;
		for (int i = 0; i < 7; i++) {
			Vector2f loc = new Vector2f(point);
			loc = Misc.getPointWithinRadius(loc, size * 1f);
			float s = size * 4f * (0.5f + (float) Math.random() * 0.5f);
			engine.addSwirlyNebulaParticle(loc, vel, s, 1.5f, rampUp, 0f, dur, color, false);
		}
	}
	

	public static boolean isProjectileExpired(DamagingProjectileAPI proj) {
		return proj.isExpired() || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj);
	}
	
	public static boolean isWeaponCharging(WeaponAPI weapon) {
		return weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
	}
}






