package data.weapons.scripts.proj;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.weapons.scripts.proj.HWIDisruptorOnhit;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.loading.MissileSpecAPI;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 *
 * (Well, no data members, so not *that* important.)
 */
public class HWIDisruptorV2 implements OnFireEffectPlugin, OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        Color color = RiftCascadeEffect.STANDARD_RIFT_COLOR;
        Object o = projectile.getWeapon().getSpec().getProjectileSpec();
        if (o instanceof MissileSpecAPI) {
            MissileSpecAPI spec = (MissileSpecAPI) o;
            color = spec.getExplosionColor();
        }


        // want a red rift, but still blue for subtracting from the red clouds
        // or not - actually looks better with the red being inverted and subtracted
        // despite this not matching the trail
        //p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
        if(target instanceof ShipAPI){
            ShipAPI hit = (ShipAPI) target;
            hit.fadeToColor(hit,new Color(200, 200, 245, 255), 1.5f, 1.5f, 2f);
            WeightedRandomPicker<WeaponAPI> w = new WeightedRandomPicker<>();
            for(WeaponAPI weapon:hit.getAllWeapons()){
                if(weapon.isDecorative()) continue;
                w.add(weapon,weapon.getSize().ordinal()*2);
            }
            if(w.isEmpty()){
                NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color, 15f);
                p.fadeOut = 4f;
                p.hitGlowSizeMult = 1f;
                spawnStandardRift(point, p);
            }else{
                for(int i = hit.getHullSize().ordinal();i>0;i--){
                    if(w.isEmpty()) break;
                    NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color, 15f);
                    p.fadeOut = 4f;
                    p.hitGlowSizeMult = 1f;
                    spawnStandardRift(w.pickAndRemove().getLocation(), p);
                }
            }
            engine.addPlugin(new HWIDisruptorOnhit.HWIDisruptorEffectPlugin(hit,5f));
        }else{
            NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color, 15f);
            p.fadeOut = 4f;
            p.hitGlowSizeMult = 1f;
            spawnStandardRift(point, p);
        }


        Vector2f vel = new Vector2f();
        if (target != null) vel.set(target.getVelocity());
        Global.getSoundPlayer().playSound("rifttorpedo_explosion", 1f, 1f, point, vel);

    }

    public static void spawnStandardRift(Vector2f loc, NEParams params) {
        CombatEngineAPI engine = Global.getCombatEngine();

        CombatEntityAPI prev = null;
        for (int i = 0; i < 2; i++) {
            NEParams p = params;
            p.radius *= 0.75f + 0.5f * (float) Math.random();

            p.withHitGlow = prev == null;

            //Vector2f loc = location;
            //loc = Misc.getPointWithinRadius(loc, p.radius * 1f);

            CombatEntityAPI e = engine.addLayeredRenderingPlugin(new NegativeExplosionVisual(p));
            e.getLocation().set(loc);

            if (prev != null) {
                float dist = Misc.getDistance(prev.getLocation(), loc);
                Vector2f vel = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(loc, prev.getLocation()));
                vel.scale(dist / (p.fadeIn + p.fadeOut) * 0.7f);
                e.getVelocity().set(vel);
            }

            prev = e;
        }
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        HSIDisruptorV2TrailEffect trail = new HSIDisruptorV2TrailEffect((MissileAPI) projectile, "rifttorpedo_loop");
        ((MissileAPI) projectile).setEmpResistance(1000);
        ((MissileAPI) projectile).setEccmChanceOverride(1f);
        Global.getCombatEngine().addPlugin(trail);
    }


    public static class HSIDisruptorV2TrailEffect extends BaseEveryFrameCombatPlugin {

        protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);

        protected MissileAPI missile;
        protected String loopId;

        public static Color STANDARD_RIFT_COLOR = new Color(255,60,100,255);
        public static Color EXPLOSION_UNDERCOLOR = new Color(25, 0, 100, 100);


        public HSIDisruptorV2TrailEffect(MissileAPI missile, String loopId) {
            this.missile = missile;
            this.loopId = loopId;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (Global.getCombatEngine().isPaused()) return;

            if (loopId != null) {
                Global.getSoundPlayer().playLoop(loopId, missile, 1f, missile.getBrightness(),
                        missile.getLocation(), missile.getVelocity());
            }

            interval.advance(amount);
            if (interval.intervalElapsed()) {
                addParticles();
            }

            if (missile.isExpired() || missile.didDamage() || !Global.getCombatEngine().isEntityInPlay(missile)) {
                Global.getCombatEngine().removePlugin(this);
            }
        }


        public void addParticles() {
            CombatEngineAPI engine = Global.getCombatEngine();
            Color c = RiftLanceEffect.getColorForDarkening(STANDARD_RIFT_COLOR);
            // subtracting the standard color looks better, makes the red a bit purplish
            // inverting red to substract doesn't look as good for the trails
//		MissileSpecAPI spec = missile.getSpec();
//		c = spec.getExplosionColor();

            Color undercolor = EXPLOSION_UNDERCOLOR;

            float b = missile.getCurrentBaseAlpha();
            c = Misc.scaleAlpha(c, b);
            undercolor = Misc.scaleAlpha(undercolor, b);

            float baseDuration = 4f;
            float size = 30f;
            size = missile.getSpec().getGlowRadius() * 0.5f;

            Vector2f point = new Vector2f(missile.getLocation());
            Vector2f pointOffset = new Vector2f(missile.getVelocity());
            pointOffset.scale(0.1f);
            Vector2f.add(point, pointOffset, point);

            Vector2f vel = new Vector2f();

            for (int i = 0; i < 1; i++) {
                float dur = baseDuration + baseDuration * (float) Math.random();
                //float nSize = size * (1f + 0.0f * (float) Math.random());
                //float nSize = size * (0.75f + 0.5f * (float) Math.random());
                float nSize = size;
                Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
                Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
                v.scale(nSize + nSize * (float) Math.random() * 0.5f);
                v.scale(0.2f);
                Vector2f.add(vel, v, v);

                float maxSpeed = nSize * 1.5f * 0.2f;
                float minSpeed = nSize * 1f * 0.2f;
                float overMin = v.length() - minSpeed;
                if (overMin > 0) {
                    float durMult = 1f - overMin / (maxSpeed - minSpeed);
                    if (durMult < 0.1f) durMult = 0.1f;
                    dur *= 0.5f + 0.5f * durMult;
                }
                engine.addNegativeNebulaParticle(pt, v, nSize * 1f, 2f,
                        0.5f, 0f, dur, c);
            }

            float dur = baseDuration;
            float rampUp = 0f;
            rampUp = 0.5f;
            c = undercolor;
            for (int i = 0; i < 2; i++) {
                Vector2f loc = new Vector2f(point);
                loc = Misc.getPointWithinRadius(loc, size * 1f);
                float s = size * 3f * (0.5f + (float) Math.random() * 0.5f);
                engine.addNebulaParticle(loc, vel, s, 1.5f, rampUp, 0f, dur, c);
            }
        }

    }
}
