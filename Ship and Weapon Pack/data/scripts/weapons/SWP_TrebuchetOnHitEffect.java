package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_TrebuchetOnHitEffect implements OnHitEffectPlugin {

    private static final float AREA_EFFECT = 250f;
    private static final float AREA_EFFECT_INNER = 125f;
    private static final float FRIENDLY_FIRE_MULT = 0.25f;

    private static final Color COLOR1 = new Color(25, 125, 150);
    private static final Color COLOR2 = new Color(255, 255, 255);

    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult,
            CombatEngineAPI engine) {
        if (target == null || point == null) {
            return;
        }
        float emp = projectile.getEmpAmount();
        float dam = projectile.getDamageAmount();

        List<ShipAPI> targets = SWP_Util.getShipsWithinRange(point, AREA_EFFECT);

        SWP_Util.filterObscuredTargets(target, point, targets, true, true, false);

        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            targets.remove(ship);

            engine.spawnEmpArc(projectile.getSource(), point, null, target, DamageType.ENERGY, dam, emp, 100000f,
                    "tachyon_lance_emp_impact", 40f, COLOR1, COLOR2);
        }

        for (ShipAPI ship : targets) {
            float distance = MathUtils.getDistance(ship.getLocation(), point);
            float reduction = 1;
            if (distance > AREA_EFFECT_INNER + ship.getCollisionRadius()) {
                reduction = (AREA_EFFECT - distance / 2f) / (AREA_EFFECT - AREA_EFFECT_INNER);
            }
            if (ship.getOwner() == projectile.getOwner()) {
                reduction *= FRIENDLY_FIRE_MULT;
            }

            engine.spawnEmpArc(projectile.getSource(), point, null, ship, DamageType.ENERGY, dam * reduction, emp
                    * reduction, 500f, "tachyon_lance_emp_impact",
                    40f, COLOR1, COLOR2);
        }

        for (int i = 0; i < 3; i++) {
            Vector2f location = new Vector2f(projectile.getLocation().x + (float) Math.random() * 200.0f + 100.0f,
                    projectile.getLocation().y);
            location = VectorUtils.rotateAroundPivot(location, projectile.getLocation(), (float) Math.random() * 360f,
                    location);
            engine.spawnEmpArc(projectile.getSource(), point, null, new SimpleEntity(location), DamageType.ENERGY, 0.0f,
                    0.0f, 100000f,
                    "tachyon_lance_emp_impact", 20f, COLOR1, COLOR2);
        }

        EMPBlast empBlast = new EMPBlast(point, 1f, AREA_EFFECT * 1.15f, 0.5f, 60f, 0.15f, 1f);
        empBlast.init(Global.getCombatEngine().addLayeredRenderingPlugin(empBlast));

        Global.getSoundPlayer().playSound("disabled_large", 0.75f, 0.8f, point, ZERO);
    }

    public static final class EMPBlast extends BaseCombatLayeredRenderingPlugin {

        private final Vector2f point;
        private final float brightness;
        private final float radius;
        private final float duration;
        private final float rotationRate;
        private final float coreSizeFractionStart;
        private final float coreSizeFractionEnd;
        private final SpriteAPI core;
        private final SpriteAPI fringe;
        private final boolean flip;

        private float coreAngle;
        private float fringeAngle;
        private float elapsed = 0f;

        @SuppressWarnings("LeakingThisInConstructor")
        public EMPBlast(Vector2f point, float brightness, float radius, float duration, float rotationRate, float coreSizeFractionStart, float coreSizeFractionEnd) {
            this.point = new Vector2f(point);
            this.brightness = brightness;
            this.radius = radius;
            this.duration = duration;
            this.rotationRate = rotationRate;
            this.coreSizeFractionStart = coreSizeFractionStart;
            this.coreSizeFractionEnd = coreSizeFractionEnd;

            layer = CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER;
            core = Global.getSettings().getSprite("swp_blast", "swp_empblast_core");
            fringe = Global.getSettings().getSprite("swp_blast", "swp_empblast_fringe");
            core.setAdditiveBlend();
            fringe.setAdditiveBlend();
            fringe.setAdditiveBlend();
            flip = Math.random() > 0.5;

            coreAngle = MathUtils.getRandomNumberInRange(-180f, 180f);
            fringeAngle = MathUtils.getRandomNumberInRange(-180f, 180f);
        }

        @Override
        public void advance(float amount) {
            if (!Global.getCombatEngine().isPaused()) {
                elapsed += amount;
            }

            float sign = flip ? 1f : -1f;
            coreAngle += sign * amount * rotationRate;
            fringeAngle -= sign * amount * rotationRate;
            float level = Math.min(1f, Math.max(0f, elapsed / duration));
            float coreSize = SWP_Util.lerp(radius * coreSizeFractionStart, radius * coreSizeFractionEnd, level) * 2f;
            float fringeSize = SWP_Util.lerp(0f, radius, level) * 2f;
            float alpha = Math.min(1f, Math.max(0f, SWP_Util.lerp(brightness, 0f, (float) Math.pow(level, 1.5))));

            core.setAlphaMult(alpha);
            core.setAngle(coreAngle);
            core.setSize(coreSize, coreSize);
            fringe.setAlphaMult(alpha);
            fringe.setAngle(fringeAngle);
            fringe.setSize(fringeSize, fringeSize);
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            core.renderAtCenter(point.x, point.y);
            fringe.renderAtCenter(point.x, point.y);
        }

        @Override
        public float getRenderRadius() {
            return radius;
        }

        @Override
        public boolean isExpired() {
            return elapsed >= duration;
        }

        @Override
        public void init(CombatEntityAPI entity) {
            this.entity = entity;
            entity.getLocation().set(point.x, point.y);
            advance(0f);
        }
    }
}
