package org.dark.shaders.light;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.dark.shaders.light.LightShader.LocalData;
import org.dark.shaders.util.GraphicsLibSettings;
import org.lwjgl.util.vector.Vector2f;

/* God have mercy on my soul */
public class LSProxDetector implements ProximityExplosionEffect {

    public static final Map<String, ProximityExplosionEffect> ORIGINAL_EFFECTS = new HashMap<>(50);

    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        final ProximityExplosionEffect originalEffect = ORIGINAL_EFFECTS.get(originalProjectile.getProjectileSpecId());
        if (originalEffect != null) {
            /* Vanilla apparently instantiates this every time */
            ProximityExplosionEffect newEffect = null;
            try {
                newEffect = originalEffect.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Global.getLogger(LightShader.class).log(Level.ERROR,
                        "Failed to instantiate ProximityExplosionEffect from " + originalProjectile.getProjectileSpecId() + ": " + ex.getMessage());
            }
            if (newEffect != null) {
                newEffect.onExplosion(explosion, originalProjectile);
            }

        }

        if (originalProjectile.getProjectileSpecId() != null) {
            final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(LightShader.DATA_KEY);
            final List<LightAPI> lights = localData.lights;
            final Map<DamagingProjectileAPI, Boolean> projectiles = localData.projectiles;

            LightEntry data = null;
            if (LightData.projectileLightData.containsKey(originalProjectile.getProjectileSpecId())) {
                data = LightData.projectileLightData.get(originalProjectile.getProjectileSpecId());
            }

            boolean hadAttachment = false;

            final Iterator<LightAPI> iter = lights.iterator();
            while (iter.hasNext()) {
                final LightAPI light = iter.next();

                if (light instanceof StandardLight standardLight) {
                    final StandardLight sLight = standardLight;

                    if (sLight.getAttachment() == originalProjectile) {
                        hadAttachment = true;

                        if ((data == null) || !data.hasHit) {
                            // Fadeout light
                            sLight.unattach();
                            sLight.setLocation(originalProjectile.getLocation());
                            sLight.fadeOut(sLight.getAutoFadeOutTime());
                        } else {
                            // Prepare for hit light
                            sLight.unattach();
                            iter.remove();
                        }
                    }
                }
            }

            if (data != null) {
                // Hit light
                if (data.hasHit) {
                    if (((float) Math.random() <= data.chance) || hadAttachment) {
                        final StandardLight light = new StandardLight(originalProjectile.getLocation(), ZERO, ZERO, null);
                        if ((originalProjectile.getSource() != null) && (originalProjectile.getSource().getHullSize() == HullSize.FIGHTER) && data.fighterDim) {
                            light.setIntensity(data.hitIntensity * GraphicsLibSettings.fighterBrightnessScale());
                            light.setSize(data.hitSize * GraphicsLibSettings.fighterBrightnessScale());
                        } else {
                            light.setIntensity(data.hitIntensity);
                            light.setSize(data.hitSize);
                        }
                        light.setColor(data.hitColor);
                        light.fadeOut(data.hitFadeout);
                        light.setHeight(GraphicsLibSettings.weaponLightHeight());
                        lights.add(light);
                    }
                }
            }

            projectiles.remove(originalProjectile);
        }
    }
}
