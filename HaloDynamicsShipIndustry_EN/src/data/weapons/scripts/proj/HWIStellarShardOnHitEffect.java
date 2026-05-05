package data.weapons.scripts.proj;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.CryoblasterEffect;
import com.fs.starfarer.api.util.Misc;

public class HWIStellarShardOnHitEffect extends CryoblasterEffect {
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
            Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        Color color = projectile.getProjectileSpec().getFringeColor();
        color = Misc.setAlpha(color, 100);

        Vector2f vel = new Vector2f();
        if (target instanceof ShipAPI) {
            vel.set(target.getVelocity());
        }

        float sizeMult = Misc.getHitGlowSize(100f, projectile.getDamage().getBaseDamage(), damageResult) / 100f;

        for (int i = 0; i < 7; i++) {
            // float size = projectile.getProjectileSpec().getWidth() * (0.75f + (float)
            // Math.random() * 0.5f);
            float size = 10f * (0.75f + (float) Math.random() * 0.5f);

            float dur = 1f;
            // dur = 0.25f;
            float rampUp = 0f;
            Color c = Misc.scaleAlpha(color, projectile.getBrightness());
            engine.addNebulaParticle(point, vel, size, 5f + 3f * sizeMult,
                    rampUp, 0f, dur, c, true);
        }

        if (!(target instanceof ShipAPI))
            return;
        ShipAPI ship = (ShipAPI) target;
        float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
        pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

        boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
        // piercedShield = true;

        if (!shieldHit || piercedShield) {
            float emp = projectile.getEmpAmount();
            float dam = 0;
            engine.spawnEmpArcPierceShields(
                    projectile.getSource(), point, target, target,
                    DamageType.ENERGY,
                    dam, // damage
                    emp, // emp
                    100000f, // max range
                    "tachyon_lance_emp_impact",
                    20f, // thickness
                    // new Color(25,100,155,255),
                    // new Color(255,255,255,255)
                    new Color(125, 125, 225, 255),
                    new Color(255, 255, 255, 255));
        }
    }
}
