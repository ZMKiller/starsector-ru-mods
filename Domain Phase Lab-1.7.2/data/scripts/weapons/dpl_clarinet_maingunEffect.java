package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class dpl_clarinet_maingunEffect extends BaseCombatLayeredRenderingPlugin implements OnHitEffectPlugin, OnFireEffectPlugin {
	
	public dpl_clarinet_maingunEffect() {
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			//if (ship.getOwner() != projectile.getOwner()) {
			if (true) {
				if (!ship.hasListenerOfClass(dpl_clarinetMaingunHitMod.class)) {
					ship.addListener(new dpl_clarinetMaingunHitMod(ship));
				}
				List<dpl_clarinetMaingunHitMod> listeners = ship.getListeners(dpl_clarinetMaingunHitMod.class);
				if (listeners.isEmpty()) return; // ???
						
				dpl_clarinetMaingunHitMod listener = listeners.get(0);
				listener.notifyHit(projectile);
				
				float thickness = 25f;
				float coreWidthMult = 0.67f;
				
				EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(projectile.getSource(), point, ship,
						   ship,
						   DamageType.ENERGY, 
						   5f,
						   0f, // emp 
						   100000f, // max range 
						   "realitydisruptor_emp_impact",
						   thickness, // thickness
						   new Color(55,50,255,255),
						   new Color(255,255,255,255)
						   );
				arc.setCoreWidthOverride(thickness * coreWidthMult);
				arc.setSingleFlickerMode();
				
				Vector2f offset = Vector2f.sub(arc.getTargetLocation(), target.getLocation(), new Vector2f());
				offset = Misc.rotateAroundOrigin(offset, -target.getFacing());
				
				dpl_clarinet_maingunEffect effect = new dpl_clarinet_maingunEffect(projectile, ship, offset);
				CombatEntityAPI e = engine.addLayeredRenderingPlugin(effect);
				e.getLocation().set(arc.getTargetLocation());
			}
		}
		
		String impactSoundId = "mote_attractor_impact_damage";
		Global.getSoundPlayer().playSound(impactSoundId, 1f, 1f, point, new Vector2f());
	}
	
	public static class ParticleData {
		public SpriteAPI sprite;
		public Vector2f offset = new Vector2f();
		public Vector2f vel = new Vector2f();
		public float scale = 1f;
		public float scaleIncreaseRate = 1f;
		public float turnDir = 1f;
		public float angle = 1f;
		
		public float maxDur;
		public FaderUtil fader;
		public float elapsed = 0f;
		public float baseSize;
		
		public ParticleData(float baseSize, float maxDur, float endSizeMult) {
			sprite = Global.getSettings().getSprite("misc", "dpl_nebula_bright");
			float i = Misc.random.nextInt(4);
			float j = Misc.random.nextInt(4);
			sprite.setTexWidth(0.25f);
			sprite.setTexHeight(0.25f);
			sprite.setTexX(i * 0.25f);
			sprite.setTexY(j * 0.25f);
			sprite.setAdditiveBlend();
			
			angle = (float) Math.random() * 360f;
			
			this.maxDur = maxDur;
			scaleIncreaseRate = endSizeMult / maxDur;
			if (endSizeMult < 1f) {
				scaleIncreaseRate = -1f * endSizeMult;
			}
			scale = 1f;
			
			this.baseSize = baseSize;
			turnDir = Math.signum((float) Math.random() - 0.5f) * 20f * (float) Math.random();
			//turnDir = 0f;
			
			float driftDir = (float) Math.random() * 360f;
			vel = Misc.getUnitVectorAtDegreeAngle(driftDir);
			//vel.scale(proj.getProjectileSpec().getLength() / maxDur * (0f + (float) Math.random() * 3f));
			vel.scale(0.25f * baseSize / maxDur * (1f + (float) Math.random() * 1f));
			
			fader = new FaderUtil(0f, 0.5f, 0.5f);
			fader.forceOut();
			fader.fadeIn();
		}
		
		public void advance(float amount) {
			scale += scaleIncreaseRate * amount;
			
			offset.x += vel.x * amount;
			offset.y += vel.y * amount;
				
			angle += turnDir * amount;
			
			elapsed += amount;
			if (maxDur - elapsed <= fader.getDurationOut() + 0.1f) {
				fader.fadeOut();
			}
			fader.advance(amount);
		}
	}
	
	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	protected DamagingProjectileAPI proj;
	protected ShipAPI target;
	protected Vector2f offset;
	protected IntervalUtil interval;
	protected FaderUtil fader = new FaderUtil(1f, 0.5f, 0.5f);
	protected static float PlasmaPotentialTime = 12.5f;

	public dpl_clarinet_maingunEffect(DamagingProjectileAPI proj, ShipAPI target, Vector2f offset) {
		this.proj = proj;
		this.target = target;
		this.offset = offset;
		
		interval = new IntervalUtil(0.8f, 1f);
		interval.forceIntervalElapsed();
	}
	
	public float getRenderRadius() {
		return 500f;
	}
	
	
	protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.BELOW_INDICATORS_LAYER);
	@Override
	public EnumSet<CombatEngineLayers> getActiveLayers() {
		return layers;
	}

	public void init(CombatEntityAPI entity) {
		super.init(entity);
	}
	
	public void advance(float amount) {
		if (Global.getCombatEngine().isPaused()) return;
		
		Vector2f loc = new Vector2f(offset);
		loc = Misc.rotateAroundOrigin(loc, target.getFacing());
		Vector2f.add(target.getLocation(), loc, loc);
		entity.getLocation().set(loc);
		
		List<ParticleData> remove = new ArrayList<ParticleData>();
		for (ParticleData p : particles) {
			p.advance(amount);
			if (p.elapsed >= p.maxDur) {
				remove.add(p);
			}
		}
		particles.removeAll(remove);
		
		float volume = 1f;
		if (!target.isAlive() || !Global.getCombatEngine().isEntityInPlay(target)) {
			fader.fadeOut();
			fader.advance(amount);
			volume = fader.getBrightness();
		}
		Global.getSoundPlayer().playLoop("disintegrator_loop", target, 1f, volume, loc, target.getVelocity());
		
		
		interval.advance(amount);
		if (interval.intervalElapsed()) {
			dealDamage();
		}
	}


	protected void dealDamage() {
		boolean hasPlasmaPotential = false;
		List<dpl_clarinetMaingunHitMod> listeners = target.getListeners(dpl_clarinetMaingunHitMod.class);
		if (!listeners.isEmpty()) {
			dpl_clarinetMaingunHitMod listener = listeners.get(0);
			if (!listener.recentHits.getItems().isEmpty()) {
				hasPlasmaPotential = true;
			}
		}
		
		int num = 3;
		
		if (hasPlasmaPotential) {
			for (int i = 0; i < num; i++) {
				ParticleData p = new ParticleData(30f, 3f + (float) Math.random() * 2f, 2f);
				particles.add(p);
				p.offset = Misc.getPointWithinRadius(p.offset, 20f);
			}
		}
	}

	public boolean isExpired() {
		
		boolean hasPlasmaPotential = false;
		List<dpl_clarinetMaingunHitMod> listeners = target.getListeners(dpl_clarinetMaingunHitMod.class);
		if (!listeners.isEmpty()) {
			dpl_clarinetMaingunHitMod listener = listeners.get(0);
			if (!listener.recentHits.getItems().isEmpty()) {
				hasPlasmaPotential = true;
			}
		}
		
		return particles.isEmpty() && (!hasPlasmaPotential || !target.isAlive() || !Global.getCombatEngine().isEntityInPlay(target));
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		
		Color color = new Color(125,255,175,55);
		
		int PlasmaPotential = 1;
		List<dpl_clarinetMaingunHitMod> listeners = target.getListeners(dpl_clarinetMaingunHitMod.class);
		if (!listeners.isEmpty()) {
			dpl_clarinetMaingunHitMod listener = listeners.get(0);
			if (!listener.recentHits.getItems().isEmpty()) {
				PlasmaPotential = listener.recentHits.getItems().size();
			}
		}
		
		if (PlasmaPotential == 2) {
			color = new Color(155,255,255,55);
		} else if (PlasmaPotential == 3) {
			color = new Color(75,155,255,55);
		} else if (PlasmaPotential == 4) {
			color = new Color(175,125,255,55);
		}
		
		float b = viewport.getAlphaMult();
		
		for (ParticleData p : particles) {
			//float size = proj.getProjectileSpec().getWidth() * 0.6f;
			float size = p.baseSize * p.scale;
			
			Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
			
			float alphaMult = 1f;
			
			p.sprite.setAngle(p.angle);
			p.sprite.setSize(size, size);
			p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
			p.sprite.setColor(color);
			p.sprite.renderAtCenter(loc.x, loc.y);
		}
		
		GL14.glBlendEquation(GL14.GL_FUNC_ADD);
	}
	
	public static class dpl_clarinetMaingunHitMod implements AdvanceableListener {
		//implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected TimeoutTracker<DamagingProjectileAPI> recentHits = new TimeoutTracker<DamagingProjectileAPI>();
		public dpl_clarinetMaingunHitMod(ShipAPI ship) {
		this.ship = ship;
		}
		
		public void notifyHit(DamagingProjectileAPI proj) {
			if (recentHits.getItems().size() < 4f) {
				recentHits.add(proj, PlasmaPotentialTime, PlasmaPotentialTime);
				//Make the plasma fading more flat
				for (DamagingProjectileAPI item : recentHits.getItems()) {
					recentHits.add(item, 1f, PlasmaPotentialTime+1f);
				}
			}
		}
		
		public void advance(float amount) {
			recentHits.advance(amount);
		}
	}
}




