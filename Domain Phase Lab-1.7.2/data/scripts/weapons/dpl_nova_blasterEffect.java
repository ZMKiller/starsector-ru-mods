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
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class dpl_nova_blasterEffect extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin {

    public dpl_nova_blasterEffect() {
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        
        ShipAPI ship = (weapon != null) ? weapon.getShip() : null;
        if (ship == null) return;
        
        dpl_nova_blasterEffect effect = new dpl_nova_blasterEffect(projectile);
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

    public dpl_nova_blasterEffect(DamagingProjectileAPI proj) {
        this.proj = proj;

        SpriteAPI sheet = Global.getSettings().getSprite("effects", "dpl_nova_blaster_effect");
        
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
        entity.getLocation().set(proj.getLocation());

        // Advance each particle
        for (ParticleData p : particles) {
            p.advance(amount);
        }
    }

    @Override
    public boolean isExpired() {
        return proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj);
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;

        float b = proj.getBrightness() * viewport.getAlphaMult();

        for (ParticleData p : particles) {
            
            float size = 60f * p.scale;

            Vector2f loc = new Vector2f(x, y);

            float alphaBase = b * p.fader.getBrightness();

            float cycleLength = 6.4f;
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
            p.sprite.setCenter(size * 0.5f, size * 0.5f);

            p.sprite.renderAtCenter(loc.x, loc.y);
        }
    }
}




