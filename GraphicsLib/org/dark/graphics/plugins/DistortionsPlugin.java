package org.dark.graphics.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.hullmods.TEM_LatticeShield;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Level;
import org.dark.shaders.ShaderModPlugin;
import org.dark.shaders.distortion.DistortionAPI;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.util.GraphicsLibSettings;
import org.dark.shaders.util.ShaderLib;
import org.json.JSONArray;
import org.json.JSONException;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

@SuppressWarnings("UseSpecificCatch")
public class DistortionsPlugin extends BaseEveryFrameCombatPlugin {

    private static final Set<String> EXCLUDED_PROJECTILES = new HashSet<>(20);

    private static final String DATA_KEY = "GLib_Distortions";

    private static final String SETTINGS_SPREADSHEET = "data/config/glib/no_shield_ripple.csv";

    private static final Vector2f ZERO = new Vector2f();

    static {
        try {
            loadSettings();
        } catch (Exception e) {
            Global.getLogger(DistortionsPlugin.class).log(Level.ERROR, "Failed to load performance settings: "
                    + e.getMessage());
        }
    }

    private static void loadSettings() throws IOException, JSONException {
        for (ModSpecAPI mod : Global.getSettings().getModManager().getEnabledModsCopy()) {
            JSONArray rows;
            try {
                rows = Global.getSettings().getMergedSpreadsheetDataForMod("id", SETTINGS_SPREADSHEET, mod.getId());
            } catch (RuntimeException e) {
                continue;
            }

            for (int i = 0; i < rows.length(); i++) {
                String id = rows.getJSONObject(i).getString("id");
                EXCLUDED_PROJECTILES.add(id);
            }
        }
    }

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        boolean enabled = GraphicsLibSettings.enableShieldRipples() || GraphicsLibSettings.enableMjolnirRipples();
        if ((engine == null) || !enabled) {
            return;
        }

        if (engine.isPaused() || !ShaderLib.areShadersAllowed() || !ShaderLib.areBuffersAllowed()) {
            return;
        }

        if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
            Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<DamagingProjectileAPI, ProjectileInfo> projectiles = localData.projectiles;

        List<DamagingProjectileAPI> activeProjectiles = engine.getProjectiles();
        int size = activeProjectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI projectile = activeProjectiles.get(i);
            if (projectile.didDamage() || projectile.getElapsed() > 0.2f) {
                continue;
            }

            if (!projectiles.containsKey(projectile)) {
                if (GraphicsLibSettings.enableMjolnirRipples() && (projectile.getProjectileSpecId() != null) && projectile.getProjectileSpecId().contentEquals("mjolnir_shot")) {
                    WaveDistortion wave = new WaveDistortion(projectile.getLocation(), ZERO);
                    wave.setIntensity(5f);
                    wave.setSize(50f);
                    wave.flip(true);
                    DistortionShader.addDistortion(wave);
                    projectiles.put(projectile, new ProjectileInfo(wave, projectile.getDamageAmount()));
                } else if (GraphicsLibSettings.enableShieldRipples() && ((projectile.getProjectileSpecId() == null) || !EXCLUDED_PROJECTILES.contains(projectile.getProjectileSpecId()))) {
                    projectiles.put(projectile, new ProjectileInfo(projectile.getDamageAmount()));
                }
            }
        }

        Iterator<Map.Entry<DamagingProjectileAPI, ProjectileInfo>> iter = projectiles.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<DamagingProjectileAPI, ProjectileInfo> entry = iter.next();
            DamagingProjectileAPI projectile = entry.getKey();
            ProjectileInfo info = entry.getValue();
            if (Math.random() > 0.8) {
                info.damage = Math.max(info.damage, projectile.getDamageAmount());
            }

            if (projectile.didDamage()) {
                CombatEntityAPI target = projectile.getDamageTarget();

                if (GraphicsLibSettings.enableShieldRipples() && (target instanceof ShipAPI ship)) {
                    float distanceFromShieldBorder = 0f;
                    if (ship.getShield() != null) {
                        distanceFromShieldBorder = Math.abs((MathUtils.getDistance(projectile.getLocation(),
                                ship.getShield().getLocation()) - ship.getShield().getRadius()));
                    }
                    if (ship.getShield() != null
                            && ((ship.getShield().isOn() && ship.getShield().isWithinArc(projectile.getLocation()))
                            || (ship.getFluxTracker().isOverloaded()
                            && ship.getFluxTracker().getFluxLevel() >= 0.98f
                            && distanceFromShieldBorder <= 10f))) {
                        Vector2f position = VectorUtils.getDirectionalVector(ship.getShield().getLocation(),
                                projectile.getLocation());
                        position.scale(ship.getShield().getRadius());
                        Vector2f.add(position, ship.getShield().getLocation(), position);
                        float fader = 1f;
                        if (!(projectile instanceof MissileAPI) && projectile.getWeapon() != null) {
                            float lifetime = projectile.getWeapon().getRange()
                                    / projectile.getWeapon().getProjectileSpeed();
                            float fadetime = 400f / projectile.getWeapon().getProjectileSpeed();
                            fader = Math.max(0.25f, 1f - Math.max(0f, projectile.getElapsed() / lifetime - 1f)
                                    / fadetime);
                            if (fader < 0.99f) {
                                fader *= 0.5f;
                            }
                        }
                        float factor = ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
                        if (projectile instanceof MissileAPI) {
                            factor *= ship.getMutableStats().getMissileShieldDamageTakenMult().getModifiedValue();
                        } else {
                            factor *= ship.getMutableStats().getProjectileShieldDamageTakenMult().getModifiedValue();
                        }
                        switch (projectile.getDamageType()) {
                            case ENERGY ->
                                factor *= ship.getMutableStats().getEnergyShieldDamageTakenMult().getModifiedValue();
                            case KINETIC ->
                                factor *= ship.getMutableStats().getKineticShieldDamageTakenMult().getModifiedValue();
                            case HIGH_EXPLOSIVE ->
                                factor *= ship.getMutableStats().getHighExplosiveShieldDamageTakenMult().getModifiedValue();
                            case FRAGMENTATION ->
                                factor *= ship.getMutableStats().getFragmentationShieldDamageTakenMult().getModifiedValue();
                            default -> {
                            }
                        }
                        boolean dweller = ship.getHullStyleId().contentEquals("DWELLER");
                        createHitRipple(position, ship.getVelocity(), info.damage * fader * factor, projectile.getDamageType(),
                                VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getShield().getLocation(), projectile.getLocation())),
                                ship.getShield().getRadius(), dweller);
                    } else if (ShaderModPlugin.templarsExists && TEM_LatticeShield.shieldLevel(ship) > 0f) {
                        float fader = 1f;
                        if (!(projectile instanceof MissileAPI) && projectile.getWeapon() != null) {
                            float lifetime = projectile.getWeapon().getRange()
                                    / projectile.getWeapon().getProjectileSpeed();
                            float fadetime = 400f / projectile.getWeapon().getProjectileSpeed();
                            fader = Math.max(0.25f, 1f - Math.max(0f, projectile.getElapsed() / lifetime - 1f)
                                    / fadetime);
                            if (fader < 0.99f) {
                                fader *= 0.5f;
                            }
                        }
                        float factor = ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
                        createHitRipple(projectile.getLocation(), ship.getVelocity(), info.damage * fader * factor, projectile.getDamageType(),
                                VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), projectile.getLocation())),
                                ship.getCollisionRadius(), false);
                    }
                }

                if (info.distortion != null) {
                    if (projectile.getProjectileSpecId().contentEquals("mjolnir_shot")) {
                        WaveDistortion wave = (WaveDistortion) info.distortion;
                        if (!wave.isFading()) {
                            wave.fadeOutIntensity(0.2f);
                        }
                    }
                }

                iter.remove();
            } else if (!engine.isEntityInPlay(projectile)) {
                if (info.distortion != null) {
                    if (projectile.getProjectileSpecId().contentEquals("mjolnir_shot")) {
                        WaveDistortion wave = (WaveDistortion) info.distortion;
                        wave.setLifetime(0f);
                    }
                }

                iter.remove();
            } else if (info.distortion != null) {
                if (projectile.getProjectileSpecId().contentEquals("mjolnir_shot")) {
                    WaveDistortion wave = (WaveDistortion) info.distortion;
                    wave.setLocation(projectile.getLocation());
                }
            } else if (projectile.isFading()) {
                if (info.distortion != null) {
                    if (projectile.getProjectileSpecId().contentEquals("mjolnir_shot")) {
                        WaveDistortion wave = (WaveDistortion) info.distortion;
                        if (!wave.isFading()) {
                            wave.fadeOutIntensity(0.2f);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    private void createHitRipple(Vector2f location, Vector2f velocity, float damage, DamageType type, float direction,
            float shieldRadius, boolean dweller) {
        float dmg = damage;
        if (type == DamageType.FRAGMENTATION) {
            dmg *= 0.25f;
        }
        if (type == DamageType.HIGH_EXPLOSIVE) {
            dmg *= 0.5f;
        }
        if (type == DamageType.KINETIC) {
            dmg *= 2f;
        }

        if (dmg < 25f) {
            return;
        }

        float fadeTime = (float) Math.pow(dmg, 0.25) * 0.1f;
        float size = (float) Math.pow(dmg, 0.3333333) * 15f;

        float ratio = Math.min(size / shieldRadius, 1f);
        float arc = 90f - ratio * 14.54136f; // Don't question the magic number

        float start1 = direction - arc;
        if (start1 < 0f) {
            start1 += 360f;
        }
        float end1 = direction + arc;
        if (end1 >= 360f) {
            end1 -= 360f;
        }

        float start2 = direction + arc;
        if (start2 < 0f) {
            start2 += 360f;
        }
        float end2 = direction - arc;
        if (end2 >= 360f) {
            end2 -= 360f;
        }

        if (dweller) {
            fadeTime *= 1.5f;
            size *= 1.5f;

            RippleDistortion ripple = new RippleDistortion(location, velocity);
            ripple.setSize(size);
            ripple.setIntensity(size * 0.1f);
            ripple.setFrameRate(60f / fadeTime);
            ripple.fadeInSize(fadeTime * 1.2f);
            ripple.fadeOutIntensity(fadeTime);
            ripple.setSize(size * 0.2f);
            ripple.setArc(start1, end1);
            DistortionShader.addDistortion(ripple);
        } else {
            RippleDistortion ripple = new RippleDistortion(location, velocity);
            ripple.setSize(size);
            ripple.setIntensity(size * 0.3f);
            ripple.setFrameRate(60f / fadeTime);
            ripple.fadeInSize(fadeTime * 1.2f);
            ripple.fadeOutIntensity(fadeTime);
            ripple.setSize(size * 0.2f);
            ripple.setArc(start1, end1);
            DistortionShader.addDistortion(ripple);

            ripple = new RippleDistortion(location, velocity);
            ripple.setSize(size);
            ripple.setIntensity(size * 0.075f);
            ripple.setFrameRate(60f / fadeTime);
            ripple.fadeInSize(fadeTime * 1.2f);
            ripple.fadeOutIntensity(fadeTime);
            ripple.setSize(size * 0.2f);
            ripple.setArc(start2, end2);
            DistortionShader.addDistortion(ripple);
        }
    }

//    private float getAdjustedDamage(DamagingProjectileAPI proj, float baseDamage, boolean shields) {
//        DamageAPI damage = proj.getDamage();
//        MutableShipStatsAPI stats = damage.getStats();
//        WeaponAPI weapon = proj.getWeapon();
//        float dmg = baseDamage;
//
//        if (weapon == null) {
//            return dmg;
//        }
//
//        if (proj instanceof MissileAPI || weapon.getType() == WeaponType.MISSILE) {
//            dmg *= stats.getMissileWeaponDamageMult().getModifiedValue();
//        }
//
//        if (weapon.getType() == WeaponType.BALLISTIC) {
//            dmg *= stats.getBallisticWeaponDamageMult().getModifiedValue();
//        }
//        if (weapon.getType() == WeaponType.ENERGY) {
//            dmg *= stats.getEnergyWeaponDamageMult().getModifiedValue();
//        }
//        if (shields) {
//            dmg *= stats.getDamageToTargetShieldsMult().getModifiedValue();
//        }
//
//        return dmg;
//    }
    private static final class LocalData {

        final Map<DamagingProjectileAPI, ProjectileInfo> projectiles = new LinkedHashMap<>(1000);
    }

    private static class ProjectileInfo {

        float damage;
        DistortionAPI distortion;

        ProjectileInfo(DistortionAPI distortion, float damage) {
            this.distortion = distortion;
            this.damage = damage;
        }

        ProjectileInfo(float damage) {
            this.distortion = null;
            this.damage = damage;
        }
    }
}
