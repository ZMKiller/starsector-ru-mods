package data.weapons.scripts.beam;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;

public class HWIHeatBeamEffect implements BeamEffectPlugin {
    private IntervalUtil fireInterval = new IntervalUtil(0.2f, 0.3f);
    private boolean wasZero = true;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused())
            return;
        fireInterval.advance(amount);
        if (fireInterval.intervalElapsed()) {
            Vector2f loc = beam.getRayEndPrevFrame();
            engine.addPlugin(new HWIHeatBeamEffectPlugin(loc, createExplosionSpec(beam.getDamage().getDamage() * 0.4f),
                    1.2f, 0.4f, beam.getSource()));
        }
    }

    public class HWIHeatBeamEffectPlugin extends BaseEveryFrameCombatPlugin {
        private DamagingExplosionSpec explosionSpec = null;
        private IntervalUtil fire = new IntervalUtil(0.1f, 0.1f);
        private IntervalUtil last = new IntervalUtil(1f, 1f);
        private CombatEngineAPI engine = Global.getCombatEngine();
        private Vector2f loc;
        private ShipAPI source = null;

        public HWIHeatBeamEffectPlugin(Vector2f loc, DamagingExplosionSpec explosion, float time, float interval,
                ShipAPI source) {
            this.explosionSpec = explosion;
            this.loc = loc;
            fire = new IntervalUtil(interval, interval);
            last = new IntervalUtil(time + interval / 2, time + interval / 2);
            this.source = source;
        }

        public void advance(float amount, List<InputEventAPI> events) {
            if (engine.isPaused())
                return;
            fire.advance(amount);
            if (fire.intervalElapsed()) {
                engine.spawnDamagingExplosion(explosionSpec, source, loc);
            }
            last.advance(amount);
            if (last.intervalElapsed()) {
                engine.removePlugin(this);
            }
        }

    }

    public DamagingExplosionSpec createExplosionSpec(float damage) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.2f, // duration
                25f, // radius
                25f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.2f, // particleDuration
                50, // particleCount
                new Color(255, 255, 255, 255), // particleColor
                new Color(255, 155, 0, 175) // explosionColor
        );
        spec.setDamageType(DamageType.HIGH_EXPLOSIVE);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_flak");
        return spec;
    }

}
