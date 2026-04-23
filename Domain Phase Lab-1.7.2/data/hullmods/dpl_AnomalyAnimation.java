package data.hullmods;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.FaderUtil;

import data.hullmods.dpl_Precursor_Reboot.dpl_Precursor_RebootScript;
import data.scripts.weapons.dpl_anomalyEffect;

public class dpl_AnomalyAnimation extends BaseHullMod {
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldDamageTakenMult().modifyMult(id, 1f);	
	}
	
	public static class dpl_ModulizedShieldsScript implements CombatLayeredRenderingPlugin {
		public static class ParticleData {
			public SpriteAPI sprite;
			public float scale = 1f;
			public ShipAPI ship;
			public float rot_rate = 1f;
			public float angle = 1f;
			
			public FaderUtil fader;
			
			public ParticleData(ShipAPI ship, SpriteAPI sprite, float rot_rate, float scale, boolean AdditiveBlending) {
				this.ship = ship;
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
			
				fader = new FaderUtil(0f, 0.25f, 0.05f);
				fader.fadeIn();
			}
			
			public void advance(float amount) {
				
				angle += rot_rate * amount;
				
				fader.advance(amount);
			}
		}
		
		protected List<ParticleData> particles = new ArrayList<ParticleData>();
		
		protected ShipAPI ship;
		
		protected CombatEntityAPI entity;
		
		public dpl_ModulizedShieldsScript(ShipAPI ship) {
			this.ship = ship;
			
			SpriteAPI sprite1 = Global.getSettings().getSprite("effects", "dpl_nebula_1");
			SpriteAPI sprite2 = Global.getSettings().getSprite("effects", "dpl_nebula_2");
			SpriteAPI sprite3 = Global.getSettings().getSprite("effects", "dpl_nebula_3");
			SpriteAPI sprite4 = Global.getSettings().getSprite("effects", "dpl_nebula_4");
			SpriteAPI sprite5 = Global.getSettings().getSprite("effects", "dpl_nebula_5");
			
			particles.add(new ParticleData(ship,sprite5,60f,1f,true));
			particles.add(new ParticleData(ship,sprite4,-60f,1f,true));
			particles.add(new ParticleData(ship,sprite3,60f,1f,false));
			particles.add(new ParticleData(ship,sprite2,-60f,1f,false));
			particles.add(new ParticleData(ship,sprite1,60f,1f,false));
			
		}
		
		public float getRenderRadius() {
			return 700f;
		}
		
		
		protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER);
		@Override
		public EnumSet<CombatEngineLayers> getActiveLayers() {
			return layers;
		}

		public void init(CombatEntityAPI entity) {
			this.entity = entity;
		}
		
		public void advance(float amount) {
			if (Global.getCombatEngine().isPaused()) return;
			
			entity.getLocation().set(ship.getLocation());
			
			for (ParticleData p : particles) {
				p.advance(amount);
			}
		}


		public boolean isExpired() {
			return !ship.isAlive();
		}

		public void render(CombatEngineLayers layer, ViewportAPI viewport) {
			float x = entity.getLocation().x;
			float y = entity.getLocation().y;
			
			float b = 1f;
			b *= viewport.getAlphaMult();
			
			for (ParticleData p : particles) {
				//float size = proj.getProjectileSpec().getWidth() * 0.6f;
				float size = 100f;
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

		@Override
		public void cleanup() {
			
		}
	}
	
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		CombatEngineAPI engine = Global.getCombatEngine();
		dpl_ModulizedShieldsScript trail = new dpl_ModulizedShieldsScript(ship);
		CombatEntityAPI e = engine.addLayeredRenderingPlugin(trail);
		e.getLocation().set(ship.getLocation());
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null;
	}

}
