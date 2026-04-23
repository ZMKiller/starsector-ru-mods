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

import data.scripts.weapons.dpl_railgunEffect.ParticleData;

/**
 * IMPORTANT: will be multiple instances of this, as this doubles as the every frame effect and the on fire effect (same instance)
 * But also as the visual for each individual shot (created via onFire, using the non-default constructor)
 */
public class dpl_railgun_trailEffect extends CombatEntityPluginWithParticles {
	
	protected WeaponAPI weapon;
	protected DamagingProjectileAPI proj;
	protected IntervalUtil interval = new IntervalUtil(0.04f, 0.06f);
	protected float effect_time = 0f;
	
	public dpl_railgun_trailEffect(WeaponAPI weapon) {
		super();
		this.weapon = weapon;
		this.effect_time = 0f;
		interval = new IntervalUtil(0.08f, 0.12f);
	}
	
	public void attachToProjectile(DamagingProjectileAPI proj) {
		this.proj = proj;
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
            
            sprite.setAlphaMult(1f);
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
    
    @Override
	protected float getGlobalAlphaMult() {
		if (proj != null && proj.isFading()) {
			return proj.getBrightness();
		}
		return super.getGlobalAlphaMult();
	}
    
    // BaseCombatLayeredRenderingPlugin
    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;
        
		if (proj != null) {
			entity.getLocation().set(proj.getSpawnLocation());
		} else {
			entity.getLocation().set(weapon.getFirePoint(0));
			effect_time = 0f;
		}
		
		List<ParticleData> remove = new ArrayList<ParticleData>();
		
		for (ParticleData p : particles) {
            p.advance(amount);
            if (p.time >= p.maxDur + 0.05f) {
            	remove.add(p);
            }
        }
		particles.removeAll(remove);
		
		boolean keepSpawningTrail = isWeaponCharging(weapon) || 
				(proj != null && !isProjectileExpired(proj) && !proj.isFading());
        
		if (keepSpawningTrail) {
			interval.advance(amount);
			effect_time += amount;
			
	        Vector2f offset = new Vector2f(0f,0f);
			
			if (proj != null) {
				offset.x = proj.getLocation().x - proj.getSpawnLocation().x;
				offset.y = proj.getLocation().y - proj.getSpawnLocation().y;
			}
			
			SpriteAPI ring1 = Global.getSettings().getSprite("effects", "dpl_railgun_ring1");
			SpriteAPI ring2 = Global.getSettings().getSprite("effects", "dpl_railgun_ring2");
			SpriteAPI ring3 = Global.getSettings().getSprite("effects", "dpl_railgun_ring3");
			
			if (interval.intervalElapsed() && effect_time >= 0.1f) {
				particles.add(new ParticleData(ring1, offset, 20f, 3f, 0f, 0f, 0.1f, true, true, false));
				particles.add(new ParticleData(ring2, offset, 20f, 4.333f, 0f, 0f, 0.15f, true, true, false));
				particles.add(new ParticleData(ring3, offset, 20f, 5.667f, 0f, 0f, 0.2f, true, true, false));
			}
		}
    }

    @Override
    public boolean isExpired() {
    	boolean keepSpawningTrail = isWeaponCharging(weapon) || 
				(proj != null && !isProjectileExpired(proj) && !proj.isFading());
    	return particles.isEmpty() && (!keepSpawningTrail || (!weapon.getShip().isAlive() && proj == null));
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
    
    @Override
    public float getRenderRadius() {
        return 1250f;
    }

    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return layers;
    }

    @Override
    public void init(CombatEntityAPI entity) {
        super.init(entity);
    }
	
	public static boolean isProjectileExpired(DamagingProjectileAPI proj) {
		return proj.isExpired() || proj.didDamage() || !Global.getCombatEngine().isEntityInPlay(proj);
	}
	
	public static boolean isWeaponCharging(WeaponAPI weapon) {
		return weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
	}
}






