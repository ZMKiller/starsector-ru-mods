package data.scripts.weapons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
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
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.dem.DEMScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_anomalyEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin{
	
	public dpl_anomalyEffect() {
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
		if (!(projectile instanceof MissileAPI)) return;
		
		MissileAPI missile = (MissileAPI) projectile;
		
		ShipAPI ship = null;
		if (weapon != null) ship = weapon.getShip();
		if (ship == null) return;
		
		DEMScript script = new DEMScript(missile, ship, weapon);
		Global.getCombatEngine().addPlugin(script);
		
		dpl_anomalyEffect trail = new dpl_anomalyEffect(projectile);
		CombatEntityAPI e = engine.addLayeredRenderingPlugin(trail);
		e.getLocation().set(projectile.getLocation());
		
		ship.getMutableStats().getHullDamageTakenMult().unmodify();
		ship.getMutableStats().getArmorDamageTakenMult().unmodify();
		ship.setHitpoints(1f);
        int[] cell = ship.getArmorGrid().getCellAtLocation(weapon.getLocation());
        ship.getArmorGrid().setArmorValue(cell[0], cell[1], 0f);
		engine.applyDamage(ship, weapon.getLocation(), 50000f, DamageType.OTHER, 0f, true, false, null);
		engine.removeEntity(ship);
	}
	
	public static class ParticleData {
		public SpriteAPI sprite;
		public float scale = 1f;
		public DamagingProjectileAPI proj;
		public float scaleIncreaseRate = 1f;
		public float rot_rate = 1f;
		public float angle = 1f;
		
		public float maxDur;
		public FaderUtil fader;
		
		public ParticleData(DamagingProjectileAPI proj, SpriteAPI sprite, float rot_rate, float scale, boolean AdditiveBlending) {
			this.proj = proj;
			this.sprite = sprite;
			this.rot_rate = rot_rate;
			this.scale = scale;
			
			sprite.setTexWidth(1f);
			sprite.setTexHeight(1f);
			sprite.setTexX(0f);
			sprite.setTexY(0f);
			if (AdditiveBlending) {
				sprite.setAdditiveBlend();
			}
			
			angle = (float) Math.random() * 360f;
			
			maxDur = proj.getWeapon().getRange() / proj.getWeapon().getProjectileSpeed();
			if (proj instanceof MissileAPI) {
				MissileAPI missile = (MissileAPI) proj;
				maxDur = missile.getMaxFlightTime();
			}
			
			scaleIncreaseRate = 0.2f / maxDur;
		
			fader = new FaderUtil(0f, 0.25f, 0.05f);
			fader.fadeIn();
		}
		
		public void advance(float amount) {
			scale += scaleIncreaseRate * amount;
			
			angle += rot_rate * amount;
			
			fader.advance(amount);
		}
	}
	
	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	
	protected DamagingProjectileAPI proj;
	protected Vector2f projVel;
	protected Vector2f projLoc;
	
	public dpl_anomalyEffect(DamagingProjectileAPI proj) {
		this.proj = proj;
		
		projVel = new Vector2f(proj.getVelocity());
		projLoc = new Vector2f(proj.getLocation());
		
		SpriteAPI sprite1 = Global.getSettings().getSprite("effects", "dpl_nebula_1");
		SpriteAPI sprite2 = Global.getSettings().getSprite("effects", "dpl_nebula_2");
		SpriteAPI sprite3 = Global.getSettings().getSprite("effects", "dpl_nebula_3");
		SpriteAPI sprite4 = Global.getSettings().getSprite("effects", "dpl_nebula_4");
		SpriteAPI sprite5 = Global.getSettings().getSprite("effects", "dpl_nebula_5");
		
		particles.add(new ParticleData(proj,sprite5,60f,1f,true));
		particles.add(new ParticleData(proj,sprite4,-60f,1f,true));
		particles.add(new ParticleData(proj,sprite3,60f,1f,false));
		particles.add(new ParticleData(proj,sprite2,-60f,1f,false));
		particles.add(new ParticleData(proj,sprite1,60f,1f,false));
		
	}
	
	public float getRenderRadius() {
		return 700f;
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
		
		entity.getLocation().set(proj.getLocation());
		
		for (ParticleData p : particles) {
			p.advance(amount);
		}
	}


	public boolean isExpired() {
		return proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj);
	}

	public void render(CombatEngineLayers layer, ViewportAPI viewport) {
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		
		float b = proj.getBrightness();
		b *= viewport.getAlphaMult();
		
		for (ParticleData p : particles) {
			//float size = proj.getProjectileSpec().getWidth() * 0.6f;
			float size = 120f;
			size *= p.scale;
			
			Vector2f loc = new Vector2f(x, y);
			
			float alphaMult = 1f;
			//float dParticle = Misc.getDistance(farAhead, loc);
			
			float a = alphaMult;
			
			p.sprite.setAngle(p.angle);
			p.sprite.setSize(size, size);
			//This coefficient of 0.35f and setCenter placed here JUST WORKS. It's found by trial & error. Don't touch.
			p.sprite.setCenter(size*0.35f, size*0.35f);
			p.sprite.setAlphaMult(b * a * p.fader.getBrightness());
			p.sprite.renderAtCenter(loc.x, loc.y);
		}
	}

}



