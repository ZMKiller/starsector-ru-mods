package data.scripts.campaign;

import java.awt.Color;
import java.util.LinkedHashSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel;
import com.fs.starfarer.api.impl.campaign.world.ZigLeashAssignmentAI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.JitterUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WarpingSpriteRendererUtil;

public class dpl_HELAEntityPlugin extends BaseCustomEntityPlugin {
	
	public static boolean isActive(SectorEntityToken gate) {
		return gate.getCustomPlugin() instanceof dpl_HELAEntityPlugin &&
				((dpl_HELAEntityPlugin)gate.getCustomPlugin()).isActive();
	}
	
	transient protected SpriteAPI baseSprite;
	transient protected SpriteAPI scannedGlow;
	
	transient protected SpriteAPI activeGlow;
	transient protected SpriteAPI whirl1;
	transient protected SpriteAPI whirl2;
	transient protected SpriteAPI starfield;
	transient protected SpriteAPI rays;
	transient protected SpriteAPI concentric;
	
	transient protected WarpingSpriteRendererUtil warp;
	
	protected FaderUtil beingUsedFader = new FaderUtil(0f, 1f, 1f, false, true);
	protected FaderUtil glowFader = new FaderUtil(0f, 1f, 1f, true, true);
	protected boolean madeActive = false;
	protected boolean addedIntel = false;
	protected float showBeingUsedDur = 0f;
	
	protected Color jitterColor = new Color(150, 100, 255, 255);
	protected JitterUtil jitter;
	protected FaderUtil jitterFader = null;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		scannedGlow = Global.getSettings().getSprite("gates", "glow_scanned");
		activeGlow = Global.getSettings().getSprite("gates", "glow_ring_active");
		concentric = Global.getSettings().getSprite("gates", "glow_concentric");
		rays = Global.getSettings().getSprite("gates", "glow_rays");
		whirl1 = Global.getSettings().getSprite("gates", "glow_whirl1");
		whirl2 = Global.getSettings().getSprite("gates", "glow_whirl2");
		starfield = Global.getSettings().getSprite("gates", "starfield");
		
		//warp = new WarpingSpriteRendererUtil(10, 10, 10f, 20f, 2f); 
		
		if (beingUsedFader == null) {
			beingUsedFader = new FaderUtil(0f, 1f, 1f, false, true);
		}
		if (glowFader == null) {
			glowFader = new FaderUtil(0f, 1f, 1f, true, true);
			glowFader.fadeIn();
		}
		inUseAngle = 0;
		return this;
	}
	
	public void jitter() {
		if (jitterFader == null) {
			jitterFader = new FaderUtil(0f, 2f, 2f);
			jitterFader.setBounceDown(true);
		}
		if (jitter == null) {
			jitter = new JitterUtil();
			jitter.updateSeed();
		}
		jitterFader.fadeIn();
	}
	
	public float getJitterLevel() {
		if (jitterFader != null) return jitterFader.getBrightness();
		return 0f;
	}

	public Color getJitterColor() {
		return jitterColor;
	}

	public void setJitterColor(Color jitterColor) {
		this.jitterColor = jitterColor;
	}

	public boolean isActive() {
		return madeActive;
	}
	
	public void showBeingUsed(float transitDistLY) {
		showBeingUsed(10f, transitDistLY);
	}
	
	public void showBeingUsed(float dur, float transitDistLY) {
		
		beingUsedFader.fadeIn();
		showBeingUsedDur = dur;
		
//		if (withSound && entity.isInCurrentLocation()) {
//			Global.getSoundPlayer().playSound("gate_being_used", 1, 1, entity.getLocation(), entity.getVelocity());
//		}
	}
	
	public float getProximitySoundFactor() {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float dist = Misc.getDistance(player.getLocation(), entity.getLocation());
		float radSum = entity.getRadius() + player.getRadius();
		
		//if (dist < radSum) return 1f;
		dist -= radSum;

		float f = 1f;
		if (dist > 300f) {
			f = 1f - (dist - 300f) / 100f;
		}

		//float f = 1f - dist / 300f;
		if (f < 0) f = 0;
		if (f > 1) f = 1;
		return f;
	}
	
	public void playProximityLoop() {
		if (!isActive() || !entity.isInCurrentLocation()) return;
		
		float prox = getProximitySoundFactor();
		float volumeMult = prox;
		float suppressionMult = prox;
		if (volumeMult <= 0) return;
		volumeMult = (float) Math.sqrt(volumeMult);
		//volumeMult = 1f;
		
		Global.getSector().getCampaignUI().suppressMusic(1f * suppressionMult);
		
		float dirToEntity = Misc.getAngleInDegrees(entity.getLocation(), this.entity.getLocation());
		Vector2f playbackLoc = Misc.getUnitVectorAtDegreeAngle(dirToEntity);
		playbackLoc.scale(500f);
		Vector2f.add(entity.getLocation(), playbackLoc, playbackLoc);
		
		Global.getSoundPlayer().playLoop("active_gate_loop", entity, 
				//volume * 0.25f + 0.75f, volume * volumeMult,
				1f, 1f * volumeMult,
				playbackLoc, Misc.ZERO);
	}

	protected float inUseAngle = 0f;
	public void advance(float amount) {
		if (showBeingUsedDur > 0 || !beingUsedFader.isIdle()) {
			showBeingUsedDur -= amount;
			if (showBeingUsedDur > 0) {
				beingUsedFader.fadeIn();
			} else {
				showBeingUsedDur = 0f;
			}
			inUseAngle += amount * 60f;
			if (warp != null) {
				warp.advance(amount);
			}
		}
		glowFader.advance(amount);
		
		if (jitterFader != null) {
			jitterFader.advance(amount);
			if (jitterFader.isFadedOut()) {
				jitterFader = null;
			}
		}
		
		beingUsedFader.advance(amount);

		if (!madeActive && entity.hasTag("dpl_HELA_activated")) {
			entity.setCustomDescriptionId("dpl_active_HELA");
			entity.setName("Active HELA Device");
			entity.setInteractionImage("illustrations", "active_gate");
			
			LocationAPI cl = entity.getContainingLocation();
			Vector2f loc = entity.getLocation();
			Vector2f vel = entity.getVelocity();
			for (int i=0; i < 50; i=i+1) {
				Vector2f finalVel = new Vector2f();
				finalVel.x = vel.x + (float) (2*Math.random()-1)*50;
				finalVel.y = vel.y + (float) (2*Math.random()-1)*50;
				
				Vector2f finalLoc = new Vector2f();
				finalLoc.x = loc.x + (float) (2*Math.random()-1)*50;
				finalLoc.y = loc.y + (float) (2*Math.random()-1)*50;
				
				float size = 3f + (float) Math.random() * 5f;
				size *= 3f;
						
				cl.addParticle(finalLoc, finalVel, size, 0.4f, 0f, 1f, new Color(255,100,255,175));
				cl.addParticle(finalLoc, finalVel, size*0.25f, 0.4f, 0f, 1f, new Color(255,100,255,175));
				cl.addParticle(finalLoc, finalVel, size*0.15f, 1f, 0f, 1f, new Color(255,100,255,175));
			}
			
			entity.removeTag(Tags.NEUTRINO);
			entity.addTag(Tags.NEUTRINO_HIGH);
			madeActive = true;
		}
		
		
		if (entity.isInCurrentLocation()) {
			if (amount > 0) {
				playProximityLoop();
				
				if (jitter != null) {
					jitter.updateSeed();
				}
			}
		}		
	}

	public float getRenderRange() {
		return entity.getRadius() + 500f;
	}

	@Override
	public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color color = entity.getFaction().getBaseUIColor();
		
		tooltip.addPara(entity.getName(), color, 0);
	}

	@Override
	public boolean hasCustomMapTooltip() {
		return true;
	}
	
	transient protected boolean scaledSprites = false;
	protected void scaleGlowSprites() {
		if (scaledSprites) return;
		CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
		if (spec != null) {
			baseSprite = Global.getSettings().getSprite(spec.getSpriteName());
			baseSprite.setSize(spec.getSpriteWidth(), spec.getSpriteHeight());
			
			scaledSprites = true;
			float scale = spec.getSpriteWidth() / Global.getSettings().getSprite(spec.getSpriteName()).getWidth();
			scannedGlow.setSize(scannedGlow.getWidth() * scale, scannedGlow.getHeight() * scale);
			activeGlow.setSize(activeGlow.getWidth() * scale, activeGlow.getHeight() * scale);
			
			rays.setSize(rays.getWidth() * scale, rays.getHeight() * scale);
			whirl1.setSize(whirl1.getWidth() * scale, whirl1.getHeight() * scale);
			whirl2.setSize(whirl2.getWidth() * scale, whirl2.getHeight() * scale);
			starfield.setSize(starfield.getWidth() * scale, starfield.getHeight() * scale);
			concentric.setSize(concentric.getWidth() * scale, concentric.getHeight() * scale);
		}
	}
	
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		if (layer == CampaignEngineLayers.BELOW_STATIONS) {
			boolean beingUsed = !beingUsedFader.isFadedOut();
			if (beingUsed) {
				float alphaMult = viewport.getAlphaMult();
				alphaMult *= entity.getSensorFaderBrightness();
				alphaMult *= entity.getSensorContactFaderBrightness();
				if (alphaMult <= 0f) return;
				
				if (warp == null) {
					int cells = 6;
					float cs = starfield.getWidth() / 10f;
					warp = new WarpingSpriteRendererUtil(cells, cells, cs * 0.2f, cs * 0.2f, 2f);
				}
				
				Vector2f loc = entity.getLocation();
				
				float glowAlpha = 1f;
				scaleGlowSprites();
				
				glowAlpha *= beingUsedFader.getBrightness();
				
				starfield.setAlphaMult(alphaMult * glowAlpha);
				starfield.setAdditiveBlend();
				//starfield.renderAtCenter(loc.x + 1.5f, loc.y);
				
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				warp.renderNoBlendOrRotate(starfield, loc.x + 1.5f - starfield.getWidth() / 2f,
											loc.y - starfield.getHeight() / 2f, false);
			}
		}
		
		if (layer == CampaignEngineLayers.STATIONS) {
			float alphaMult = viewport.getAlphaMult();
			alphaMult *= entity.getSensorFaderBrightness();
			alphaMult *= entity.getSensorContactFaderBrightness();
			if (alphaMult <= 0f) return;
			
			CustomEntitySpecAPI spec = entity.getCustomEntitySpec();
			if (spec == null) return;
			
			float w = spec.getSpriteWidth();
			float h = spec.getSpriteHeight();
			
			float scale = spec.getSpriteWidth() / Global.getSettings().getSprite(spec.getSpriteName()).getWidth(); 
			
			Vector2f loc = entity.getLocation();
			
			
			Color scannedGlowColor = new Color(255,200,0,255);
			Color activeGlowColor = new Color(200,50,255,255);
			
			scannedGlowColor = Color.white;
			activeGlowColor = Color.white;
			
			float glowAlpha = 1f;
			
			
			float glowMod1 = 0.5f + 0.5f * glowFader.getBrightness();
			float glowMod2 = 0.75f + 0.25f * glowFader.getBrightness();
			
			boolean beingUsed = !beingUsedFader.isFadedOut();
			boolean active = true;
			
			scaleGlowSprites();
			
			if (jitterFader != null && jitter != null) {
				Color c = jitterColor;
				if (c == null) c = new Color(255,255,255,255);
				baseSprite.setColor(c);
				baseSprite.setAlphaMult(alphaMult * jitterFader.getBrightness());
				baseSprite.setAdditiveBlend();
				jitter.render(baseSprite, loc.x, loc.y, 30f * jitterFader.getBrightness(), 10);
				baseSprite.renderAtCenter(loc.x, loc.y);
				if (madeActive) {
					activeGlow.setColor(activeGlowColor);
					//activeGlow.setSize(w * scale, h * scale);
					activeGlow.setAlphaMult(alphaMult * glowAlpha * glowMod2);
					activeGlow.setAdditiveBlend();
					activeGlow.renderAtCenter(loc.x, loc.y);
				}
			}
			
			float angle;
			
			if (madeActive) {
				rays.setAlphaMult(alphaMult * glowAlpha);
				rays.setAdditiveBlend();
				rays.renderAtCenter(loc.x + 1.5f, loc.y);
					
				concentric.setAlphaMult(alphaMult * glowAlpha * 1f);
				concentric.setAdditiveBlend();
				concentric.renderAtCenter(loc.x + 1.5f, loc.y);

				angle = -inUseAngle * 0.25f;
				angle = Misc.normalizeAngle(angle);
				whirl1.setAngle(angle);
				whirl1.setAlphaMult(alphaMult * glowAlpha);
				whirl1.setAdditiveBlend();
				whirl1.renderAtCenter(loc.x + 1.5f, loc.y);
					
				angle = -inUseAngle * 0.33f;
				angle = Misc.normalizeAngle(angle);
				whirl2.setAngle(angle);
				whirl2.setAlphaMult(alphaMult * glowAlpha * 0.5f);
				whirl2.setAdditiveBlend();
				whirl2.renderAtCenter(loc.x + 1.5f, loc.y);
			}
			
//			beingUsed = true;
//			showBeingUsedDur = 10f;
			if (beingUsed) {
				if (!active) {
					activeGlow.setColor(activeGlowColor);
					//activeGlow.setSize(w + 20, h + 20);
					activeGlow.setAlphaMult(alphaMult * glowAlpha * beingUsedFader.getBrightness() * glowMod2);
					activeGlow.setAdditiveBlend();
					activeGlow.renderAtCenter(loc.x, loc.y);
				}
				
				glowAlpha *= beingUsedFader.getBrightness();
				float angle1;
				
				rays.setAlphaMult(alphaMult * glowAlpha);
				rays.setAdditiveBlend();
				rays.renderAtCenter(loc.x + 1.5f, loc.y);
				
				concentric.setAlphaMult(alphaMult * glowAlpha * 1f);
				concentric.setAdditiveBlend();
				concentric.renderAtCenter(loc.x + 1.5f, loc.y);

				angle1 = -inUseAngle * 0.25f;
				angle1 = Misc.normalizeAngle(angle1);
				whirl1.setAngle(angle1);
				whirl1.setAlphaMult(alphaMult * glowAlpha);
				whirl1.setAdditiveBlend();
				whirl1.renderAtCenter(loc.x + 1.5f, loc.y);
				
				angle1 = -inUseAngle * 0.33f;
				angle1 = Misc.normalizeAngle(angle1);
				whirl2.setAngle(angle1);
				whirl2.setAlphaMult(alphaMult * glowAlpha * 0.5f);
				whirl2.setAdditiveBlend();
				whirl2.renderAtCenter(loc.x + 1.5f, loc.y);
			}
		}
	}
}