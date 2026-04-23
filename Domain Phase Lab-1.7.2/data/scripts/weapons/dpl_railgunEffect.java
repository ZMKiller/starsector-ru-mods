package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.scripts.weapons.dpl_song_of_creationEffect.ParticleData;

/**
 * IMPORTANT: will be multiple instances of this, as this doubles as the every frame effect and the on fire effect (same instance)
 * But also as the visual for each individual shot (created via onFire, using the non-default constructor)
 */
public class dpl_railgunEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin, OnHitEffectPlugin, EveryFrameWeaponEffectPlugin {

	protected CombatEntityAPI e;
	protected dpl_railgunEffect effect;
	
	protected CombatEntityAPI trail;
	protected dpl_railgun_trailEffect trailPlugin;
	
	protected float time = 0f;
	
	protected IntervalUtil ringInterval = new IntervalUtil(0.15f, 0.25f);
	protected IntervalUtil finalInterval = new IntervalUtil(0.85f, 0.95f);
	
	public static Color EXPLOSION_COLOR = new Color(100,155,255,255);
	
	public dpl_railgunEffect() {
	}
    
	// EveryFrameWeaponEffectPlugin
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (Global.getCombatEngine().isPaused()) return;
		
		ShipAPI ship = weapon.getShip();
		if (ship == null) return;
		
		boolean charging = weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
		
		//Effect
		if (ship.isAlive() && !ship.getFluxTracker().isOverloadedOrVenting() && charging && effect == null) {
			effect = new dpl_railgunEffect(weapon);
			CombatEntityAPI e = engine.addLayeredRenderingPlugin(effect);
		    e.getLocation().set(weapon.getFirePoint(0));
		    
		    Global.getSoundPlayer().playSound("dpl_railgun_windup", 1f, 1f, weapon.getFirePoint(0), ship.getVelocity());
		} else if (!charging && effect != null) {
			time = 0f;
			engine.spawnExplosion(weapon.getFirePoint(0), weapon.getShip().getVelocity(), EXPLOSION_COLOR, 160f, 0.5f);
			engine.removeEntity(e);
			effect = null;
			e = null;
		} else if (!ship.isAlive() || ship.getFluxTracker().isOverloadedOrVenting()) { //Handle Boundary cases of various interruptions
			if (effect != null) {
				time = 0f;
				engine.removeEntity(e);
				effect = null;
				e = null;
			}
		}
		
		//trailEffect
		if (charging && trail == null) {
			trailPlugin = new dpl_railgun_trailEffect(weapon);
		    trail = Global.getCombatEngine().addLayeredRenderingPlugin(trailPlugin);
		} else if (!charging && trail != null) {
			trailPlugin = null;
			trail = null;
		}
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
		ShipAPI ship = weapon.getShip();
		if (ship == null) return;
		
		if (ship.getFluxTracker().isOverloadedOrVenting()) {
			weapon.setForceNoFireOneFrame(true);
		} else if (!ship.isAlive()) { //The projectile always spawns if interrupted by ship death. Must remove it.
			weapon.setForceNoFireOneFrame(true);
			engine.removeEntity(projectile);
		} else {
			if (trailPlugin != null) {
				trailPlugin.attachToProjectile(projectile);
				trailPlugin = null;
				trail = null;
			}
			
			Global.getSoundPlayer().playSound("dpl_railgun_firing", 1f, 1f, weapon.getFirePoint(0), ship.getVelocity());
		}
	}
	
	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f vel = new Vector2f();
		if (target instanceof ShipAPI) {
			vel.set(target.getVelocity());
		}
		
		if (!shieldHit && target instanceof ShipAPI) {
			float emp = 0f;
			float dam = projectile.getDamageAmount() * 0.05f;
			
			engine.spawnEmpArc(projectile.getSource(), point, target, target,
							   DamageType.ENERGY, 
							   dam,
							   emp, // emp 
							   100000f, // max range 
							   "tachyon_lance_emp_impact",
							   20f, // thickness
							   new Color(25,100,155,255),
							   new Color(255,255,255,255)
							   );
		}
		
		Misc.playSound(damageResult, point, vel,
				"hit_shield_light_energy",
				"hit_shield_solid_energy",
				"hit_shield_heavy_energy",
				"dpl_railgun_hit_light",
				"dpl_railgun_hit_solid",
				"dpl_railgun_hit_heavy");
		
	}
	
	public static class ParticleData {
        public SpriteAPI sprite;
        
        public Vector2f offset = new Vector2f(0f,0f);
        public float time = 0f;
        public float rot_rate = 1f;
        public float angle = 1f;
        public float maxDur = 1f;
        public float size = 1f;
        public float scale = 1f;
        public float scaleSgn = 1f;
        public boolean canGrow = false;
        public boolean longFadeIn = false;

        public FaderUtil fader;

        public ParticleData(SpriteAPI sprite, Vector2f offset, float size, float scaleSgn,
                            float angle, float rot_rate, float maxDur, boolean additiveBlending, boolean canGrow, boolean longFadeIn) {
            this.sprite = sprite;
            this.rot_rate = rot_rate;
            this.maxDur = maxDur;
            this.angle = angle;
            this.size = size;
            this.scaleSgn = scaleSgn;
            this.canGrow = canGrow;
            this.longFadeIn = longFadeIn;
            this.offset = offset;

            fader = new FaderUtil(0f, 0.25f, 0.05f);
            fader.fadeIn();

            sprite.setTexWidth(1f);
            sprite.setTexHeight(1f);
            sprite.setTexX(0f);
            sprite.setTexY(0f);
            
            if (additiveBlending) sprite.setAdditiveBlend();
            
            if (longFadeIn) sprite.setAlphaMult(0f);
        }
        
        public void advance(float amount) {
            angle += rot_rate * amount;
            
            if (canGrow) {
            	scale += ((0.75f * scaleSgn - 0.25f) / maxDur) * amount;
            }
            
            if (longFadeIn) {
            	sprite.setAlphaMult(time/maxDur);
            }
            
            time  += amount; // accumulate time for animation
            if (time >= maxDur) {
            	fader.fadeOut();
            }
            fader.advance(amount);
        }
    }
    
    protected List<ParticleData> particles = new ArrayList<ParticleData>();
    protected WeaponAPI weapon;
    protected DamagingProjectileAPI proj;

    public dpl_railgunEffect(WeaponAPI weapon) {
        this.weapon = weapon;
        this.time = 0f;
        this.ringInterval = new IntervalUtil(0.15f, 0.25f);
        this.finalInterval = new IntervalUtil(0.85f, 0.95f);
        
        SpriteAPI ball = Global.getSettings().getSprite("effects", "dpl_railgun_ball");
	    SpriteAPI shrink = Global.getSettings().getSprite("effects", "dpl_railgun_shrink");
	    SpriteAPI aim = Global.getSettings().getSprite("effects", "dpl_railgun_aiming");
        
	    Vector2f zero = new Vector2f(0f,0f);
	    
        particles.add(new ParticleData(aim, zero, 800f, 1f, 40f, -40f, 1f, true, false, true));
        particles.add(new ParticleData(aim, zero, 800f, 1f, -40f, 40f, 1f, true, false, true));
        particles.add(new ParticleData(ball, zero, 80f, 1f, 0f, 0f, 1f, true, true, false));
        particles.add(new ParticleData(shrink, zero, 160f, -1f, (float) Math.random() * 360f, 240f, 0.2f, true, true, true));
    }
    
    @Override
    public float getRenderRadius() {
        return 1250f;
    }

    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.BELOW_INDICATORS_LAYER);

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return layers;
    }

    @Override
    public void init(CombatEntityAPI entity) {
        super.init(entity);
    }
    
    // BaseCombatLayeredRenderingPlugin
    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;
        
        // Keep the entity anchored to the weapon.
        entity.getLocation().set(weapon.getFirePoint(0));
        
        time += amount;
		ringInterval.advance(amount);
		finalInterval.advance(amount);
		
		SpriteAPI shrink = Global.getSettings().getSprite("effects", "dpl_railgun_shrink");
		
		Vector2f zero = new Vector2f(0f,0f);
		
		if (ringInterval.intervalElapsed() && time <= 0.8f) {
			particles.add(new ParticleData(shrink, zero, 160f, -1f, (float) Math.random() * 360f, 240f, 0.2f, true, true, true));
		}
		
		SpriteAPI ring1 = Global.getSettings().getSprite("effects", "dpl_railgun_ring1");
		SpriteAPI ring2 = Global.getSettings().getSprite("effects", "dpl_railgun_ring2");
		SpriteAPI ring3 = Global.getSettings().getSprite("effects", "dpl_railgun_ring3");
		SpriteAPI flare = Global.getSettings().getSprite("effects", "dpl_railgun_lensflare");
		
		if (finalInterval.intervalElapsed() && time <= 0.95f) {
			particles.add(new ParticleData(ring1, zero, 40f, 3f, 0f, 0f, 0.2f, true, true, false));
			particles.add(new ParticleData(ring2, zero, 40f, 4.333f, 0f, 0f, 0.3f, true, true, false));
			particles.add(new ParticleData(ring3, zero, 40f, 5.667f, 0f, 0f, 0.4f, true, true, false));
			particles.add(new ParticleData(flare, zero, 120f, 3f, 0f, 0f, 0.2f, true, true, false));
		}

        List<ParticleData> remove = new ArrayList<ParticleData>();
		
		for (ParticleData p : particles) {
            p.advance(amount);
            if (p.time >= p.maxDur + 0.05f) {
            	remove.add(p);
            }
            
            ShipAPI ship = weapon.getShip();
    		if (ship == null) return;
    		
    		if (!ship.isAlive() || ship.getFluxTracker().isOverloadedOrVenting()) {
    			remove.add(p);
    		}
            
        }
		
		particles.removeAll(remove);
    }

    @Override
    public boolean isExpired() {
        return super.isExpired();
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;

        for (ParticleData p : particles) {

            Vector2f loc = new Vector2f(x, y);
            
            p.sprite.setAngle(p.angle + weapon.getCurrAngle() - 90f);
            p.sprite.setSize(p.size * p.scale, p.size * p.scale);
            
            p.sprite.setCenter(p.size * p.scale * 0.5f, p.size * p.scale * 0.5f);

            p.sprite.renderAtCenter(loc.x + p.offset.x, loc.y + p.offset.y);
        }
    }
}




