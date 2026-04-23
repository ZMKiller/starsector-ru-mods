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
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BreachOnHitEffect;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_vaporizerEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin {

    public dpl_vaporizerEffect() {
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        
        ShipAPI ship = (weapon != null) ? weapon.getShip() : null;
        if (ship == null) return;
        
        float Sign = -1f;
        if (Misc.getAngleDiff(projectile.getFacing(), weapon.getCurrAngle()) > 45f) {
        	Sign = 1f;
        }
        dpl_vaporizerEffect effect = new dpl_vaporizerEffect(projectile, Sign);
        CombatEntityAPI e = engine.addLayeredRenderingPlugin(effect);
        e.getLocation().set(projectile.getLocation());
        
    }
    
    public static class ParticleData {
        public SpriteAPI sprite;
        public DamagingProjectileAPI proj;
        
        public float time = 0f;
        public float rot_rate = 1f;
        public float angle = 1f;
        public float scale = 1f;
        public boolean isEarlyFrame = false;

        public float maxDur;
        public FaderUtil fader;

        public ParticleData(DamagingProjectileAPI proj, SpriteAPI sprite,
                            float rot_rate, float scale, boolean additiveBlending, boolean isEarlyFrame) {
            this.proj = proj;
            this.sprite = sprite;
            this.rot_rate = rot_rate;
            this.scale = scale;
            this.isEarlyFrame = isEarlyFrame;
            
            angle = 0f;

            maxDur = proj.getWeapon().getRange() / proj.getWeapon().getProjectileSpeed();
            if (proj instanceof MissileAPI) {
                MissileAPI missile = (MissileAPI) proj;
                maxDur = missile.getMaxFlightTime();
            }

            fader = new FaderUtil(0f, 0.25f, 0.05f);
            fader.fadeIn();
            
            if (additiveBlending) sprite.setAdditiveBlend();
        }
        
        public void advance(float amount) {
            angle += rot_rate * amount;
            time  += amount; // accumulate time for animation
            fader.advance(amount);
        }
    }
    
    protected List<ParticleData> particles = new ArrayList<ParticleData>();
    protected DamagingProjectileAPI proj;
    protected float effect_time = 0f;
    protected float sign = 1f;
    protected Vector2f velocity = new Vector2f(0f,0f);

    public dpl_vaporizerEffect(DamagingProjectileAPI proj, float sign) {
        this.proj = proj;
        this.effect_time = 0f;
        this.velocity = proj.getVelocity();
        this.sign = sign;

        SpriteAPI sheet = Global.getSettings().getSprite("effects", "dpl_song_of_creation_effect");
        
        particles.add(new ParticleData(proj, sheet, 60f, 1f, false, true));
        particles.add(new ParticleData(proj, sheet, 60f, 1f, false, false));
    }

    @Override
    public float getRenderRadius() {
        return 700f;
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

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;
        // Keep the entity anchored to the projectile
        
        Vector2f perpVel = new Vector2f(0f,0f);
        Vector2f zero = new Vector2f(0f,0f);
        
        effect_time += amount;
        
		if (proj != null) {
			perpVel.x = -1f*velocity.y;
			perpVel.y = velocity.x;
		}
		
		Vector2f perp_dir = Misc.getUnitVector(zero, perpVel);
		
		float perp_rate = (float) (sign * (240f*Math.sin(2f*Math.PI*effect_time)-4.5f));
		
		proj.getLocation().x += (perp_rate * perp_dir.x) * amount;
		proj.getLocation().y += (perp_rate * perp_dir.y) * amount;
        
        entity.getLocation().set(proj.getLocation());

        // Advance each particle
        for (ParticleData p : particles) {
            p.advance(amount);
        }
    }

    @Override
    public boolean isExpired() {
        return proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj) || proj.didDamage();
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;

        float b = proj.getBrightness() * viewport.getAlphaMult();

        for (ParticleData p : particles) {
            
            float size = 40f * p.scale;

            Vector2f loc = new Vector2f(x, y);

            float alphaBase = b * p.fader.getBrightness();

            float cycleLength = 1.6f;
            float remainder   = p.time % cycleLength;
            
            float frameFloat = remainder / 0.025f;
            int frameIndex   = (int) frameFloat;
            float subFrame   = frameFloat - frameIndex;
            int nextIndex    = (frameIndex + 1) % 64;

            int i0 = frameIndex / 8;
            int j0 = frameIndex % 8;

            int i1 = nextIndex / 8;
            int j1 = nextIndex % 8;

            float w = 0.125f;
            float h = 0.125f;

            p.sprite.setTexWidth(w);
            p.sprite.setTexHeight(h);
            
            //First frame
            if (p.isEarlyFrame) {
                p.sprite.setTexX(i0 * w);
                p.sprite.setTexY(j0 * h);
                
                float factor_EF = 0.75f;
                if (subFrame >= 0.5f) {
                	factor_EF = 0f;
                }
                p.sprite.setAlphaMult(alphaBase * factor_EF);
            //Second frame
            } else {
            	p.sprite.setTexX(i1 * w);
                p.sprite.setTexY(j1 * h);
                
                float factor_LF = 0f;
                if (subFrame >= 0.5f) {
                	factor_LF = 0.75f;
                }
                p.sprite.setAlphaMult(alphaBase * factor_LF);
            }
            
            p.sprite.setAngle(p.angle);
            p.sprite.setSize(size, size);
            p.sprite.setColor(new Color(215,175,255,255));
            p.sprite.setCenter(size * 0.5f, size * 0.5f);

            p.sprite.renderAtCenter(loc.x, loc.y);
        }
    }
}

