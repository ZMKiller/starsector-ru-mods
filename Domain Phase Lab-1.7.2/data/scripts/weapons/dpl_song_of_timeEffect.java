package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
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
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.RiftTrailEffect;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_song_of_timeEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin, OnHitEffectPlugin {

	// each tick is on average .9 seconds
	// ticks can't be longer than a second or floating damage numbers separate
	public static int NUM_TICKS = 22;
	public static float ARMOR_PERCENT = 1f;
	public static float HULL_PERCENT = 1f;
	public static float PIERCING_PERCENT = 0.25f;
	
	public dpl_song_of_timeEffect() {
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (projectile.isFading()) return;
		if (!(target instanceof ShipAPI)) return;
		if (shieldHit) {
			float thickness = 100f;
			float coreWidthMult = 0.67f;
			float dam = projectile.getDamageAmount()*PIERCING_PERCENT;
			EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(projectile.getSource(), point, target,
					   target,
					   DamageType.ENERGY, 
					   dam, // dam
					   0f, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness, // thickness
					   new Color(100,100,205,200),
					   new Color(150,150,255,200)
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
		} else {
			Vector2f offset = Vector2f.sub(point, target.getLocation(), new Vector2f());
			offset = Misc.rotateAroundOrigin(offset, -target.getFacing());
			
			dpl_song_of_timeEffect effect = new dpl_song_of_timeEffect(projectile, (ShipAPI) target, offset);
			CombatEntityAPI e = engine.addLayeredRenderingPlugin(effect);
			e.getLocation().set(point);
		}
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		dpl_song_of_timeTrailEffect trail = new dpl_song_of_timeTrailEffect((MissileAPI) projectile, "rifttorpedo_loop");
		((MissileAPI) projectile).setMine(true);
		((MissileAPI) projectile).setEmpResistance(1000);
		((MissileAPI) projectile).setEccmChanceOverride(1f);
		Global.getCombatEngine().addPlugin(trail);
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
			sprite = Global.getSettings().getSprite("misc", "nebula_particles");
			//sprite = Global.getSettings().getSprite("misc", "dust_particles");
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
	protected int ticks = 0;
	protected IntervalUtil interval; 
	protected FaderUtil fader = new FaderUtil(1f, 0.5f, 0.5f); 

	public dpl_song_of_timeEffect(DamagingProjectileAPI proj, ShipAPI target, Vector2f offset) {
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
		if (ticks >= NUM_TICKS || !target.isAlive() || !Global.getCombatEngine().isEntityInPlay(target)) {
			fader.fadeOut();
			fader.advance(amount);
			volume = fader.getBrightness();
		}
		Global.getSoundPlayer().playLoop("disintegrator_loop", target, 1f, volume, loc, target.getVelocity());
		
		
		interval.advance(amount);
		if (interval.intervalElapsed() && ticks < NUM_TICKS) {
			dealDamage();
			ticks++;
		}
	}


	protected void dealDamage() {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		float TOTAL_DAMAGE = proj.getDamageAmount()*ARMOR_PERCENT;
		float TOTAL_HULL_DAMAGE = proj.getDamageAmount()*HULL_PERCENT;
		
		int num = 3;
		for (int i = 0; i < num; i++) {
			ParticleData p = new ParticleData(30f, 3f + (float) Math.random() * 2f, 2f);
			particles.add(p);
			p.offset = Misc.getPointWithinRadius(p.offset, 20f);
		}
		
		
		Vector2f point = new Vector2f(entity.getLocation());
		
		// maximum armor in a cell is 1/15th of the ship's stated armor rating

		ArmorGridAPI grid = target.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return;
		
		int gridWidth = grid.getGrid().length;
		int gridHeight = grid.getGrid()[0].length;
		
		float damageTypeMult = getDamageTypeMult(proj.getSource(), target);
		
		float damagePerTick = (float) TOTAL_DAMAGE / (float) NUM_TICKS;
		float hulldamagePerTick = (float) TOTAL_HULL_DAMAGE / (float) NUM_TICKS;
		float damageDealt = 0f;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners
				
				int cx = cell[0] + i;
				int cy = cell[1] + j;
				
				if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
				
				float damMult = 1/30f;
				if (i == 0 && j == 0) {
					damMult = 1/15f;
				} else if (i <= 1 && i >= -1 && j <= 1 && j >= -1) { // S hits
					damMult = 1/15f;
				} else { // T hits
					damMult = 1/30f;
				}
				
				float armorInCell = grid.getArmorValue(cx, cy);
				float damage = damagePerTick * damMult * damageTypeMult;
				damage = Math.min(damage, armorInCell);
				if (damage <= 0) continue;
				
				target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
				damageDealt += damage;
			}
		}
		
		target.setHitpoints(Math.max(target.getHitpoints() - hulldamagePerTick, 1));
		if (target.getHitpoints() < 10 && !target.getVariant().hasHullMod("vastbulk")) {
			//Just kill it if its hull point is too low.
			target.getMutableStats().getHullDamageTakenMult().unmodify();
			target.getMutableStats().getArmorDamageTakenMult().unmodify();
			target.setHitpoints(1f);
	        int[] cell1 = target.getArmorGrid().getCellAtLocation(point);
	        target.getArmorGrid().setArmorValue(cell1[0], cell1[1], 0f);
			engine.applyDamage(target, point, 50000f, DamageType.OTHER, 0f, true, false, null);
		}
		engine.addFloatingDamageText(point, hulldamagePerTick, Misc.FLOATY_HULL_DAMAGE_COLOR, target, proj.getSource());
		
		if (damageDealt > 0) {
			if (Misc.shouldShowDamageFloaty(proj.getSource(), target)) {
				engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, proj.getSource());
			}
			target.syncWithArmorGridState();
		}
		
	}

	public boolean isExpired() {
		return particles.isEmpty() && 
					(ticks >= NUM_TICKS || !target.isAlive() || !Global.getCombatEngine().isEntityInPlay(target));
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		
		Color color = new Color(100,150,255,35);
		float b = viewport.getAlphaMult();

		GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
		
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
	
	
	public static float getDamageTypeMult(ShipAPI source, ShipAPI target) {
		if (source == null || target == null) return 1f;
		
		float damageTypeMult = target.getMutableStats().getArmorDamageTakenMult().getModifiedValue();
		switch (target.getHullSize()) {
		case CAPITAL_SHIP:
			damageTypeMult *= source.getMutableStats().getDamageToCapital().getModifiedValue();
			break;
		case CRUISER:
			damageTypeMult *= source.getMutableStats().getDamageToCruisers().getModifiedValue();
			break;
		case DESTROYER:
			damageTypeMult *= source.getMutableStats().getDamageToDestroyers().getModifiedValue();
			break;
		case FRIGATE:
			damageTypeMult *= source.getMutableStats().getDamageToFrigates().getModifiedValue();
			break;
		case FIGHTER:
			damageTypeMult *= source.getMutableStats().getDamageToFighters().getModifiedValue();
			break;
		}
		return damageTypeMult;
	}
}




