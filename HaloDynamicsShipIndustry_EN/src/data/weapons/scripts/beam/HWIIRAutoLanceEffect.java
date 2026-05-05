package data.weapons.scripts.beam;

import java.awt.Color;

import data.hullmods.WeaponMod.HSIProtocalMeltDown;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class HWIIRAutoLanceEffect implements BeamEffectPlugin {
    private boolean done = false;

    private float mult = 1f;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (done)
            return;
        if(beam.getSource()!=null&& HSIProtocalMeltDown.hasProtocalMeltDown(beam.getSource())){
            mult = HSIProtocalMeltDown.getMult();
        }
        CombatEntityAPI target = beam.getDamageTarget();
        // boolean first = beam.getWeapon().getBeams().indexOf(beam) == 0;
        if (target != null && beam.getBrightness() >= 1f) {
            Vector2f point = beam.getTo();
            engine.spawnDamagingExplosion(createExplosionSpec(), beam.getSource(), point);
            // e.addDamagedAlready(target);
            done = true;
        }
    }

    public DamagingExplosionSpec createExplosionSpec() {
        float damage = 50f*mult;
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                50f, // radius
                35f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                0f, // particleSizeMin
                0f, // particleSizeRange
                0f, // particleDuration
                0, // particleCount
                new Color(255, 255, 255, 255), // particleColor
                new Color(255, 75, 100, 175) // explosionColor
        );

        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_guardian");
        return spec;
    }
}
