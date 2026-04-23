package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import data.scripts.util.SWP_AnamorphicFlare;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_PhaseShuntDriveStats extends BaseShipSystemScript {

    public static final float BASE_RANGE = 700f;
    public static final Color COLOR1 = new Color(255, 75, 200, 150);
    public static final Color COLOR2 = new Color(255, 0, 200);
    public static final Color COLOR3 = new Color(255, 0, 255);

    private static final Vector2f ZERO = new Vector2f();

    public boolean activated = true;
    private float total = 0f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (state == State.IN) {
            if (!activated) {
                StandardLight light = new StandardLight(ship.getLocation(), ZERO, ZERO, null);
                light.setIntensity(3f);
                light.setSize(600f);
                light.setColor(COLOR1);
                light.fadeOut(0.25f);
                LightShader.addLight(light);

                float fluxLevel = ship.getFluxTracker().getCurrFlux() / stats.getFluxCapacity().getBaseValue();
                float range = BASE_RANGE / (1f + fluxLevel);
                range = ship.getMutableStats().getSystemRangeBonus().computeEffective(range);
                Global.getCombatEngine().spawnExplosion(ship.getLocation(), ZERO, COLOR1, range, 0.25f);

                List<CombatEntityAPI> targets = new ArrayList<>(Global.getCombatEngine().getProjectiles().size() / 4);
                targets.addAll(SWP_Util.getProjectilesWithinRange(ship.getLocation(), range));
                targets.addAll(SWP_Util.getMissilesWithinRange(ship.getLocation(), range));
                targets.addAll(SWP_Util.getAsteroidsWithinRange(ship.getLocation(), range));
                total = 0f;
                for (CombatEntityAPI target : targets) {
                    if (target == null) {
                        continue;
                    }

                    float level;
                    if (target instanceof DamagingProjectileAPI) {
                        DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                        if (proj.getBaseDamageAmount() <= 0) {
                            continue;
                        }
                        if (proj.didDamage()) {
                            continue;
                        }
                        if (proj.getSpawnType() == ProjectileSpawnType.OTHER) {
                            continue;
                        }
                        if (proj.getCollisionClass() == CollisionClass.GAS_CLOUD) {
                            continue;
                        }
                        if ((proj.getProjectileSpecId() != null) && proj.getProjectileSpecId().startsWith("swp_excelsiorcannon_shot") && (proj.getOwner() == ship.getOwner())) {
                            continue;
                        }
                        if ((proj.getWeapon() != null) && (proj.getWeapon().getSpec() != null) && (proj.getWeapon().getSpec().hasTag("dummy_proj"))) {
                            continue;
                        }

                        if (proj.getDamageType() == DamageType.FRAGMENTATION) {
                            level = (proj.getDamageAmount() * 0.25f) + (proj.getEmpAmount() * 0.25f);
                        } else {
                            level = proj.getDamageAmount() + (proj.getEmpAmount() * 0.25f);
                        }
                    } else {
                        level = target.getMass();
                    }
                    level = Math.min(5000f, level);
                    float sqrtLevel = (float) Math.sqrt(level);
                    float sqrtLevel2 = (float) Math.sqrt(sqrtLevel);

                    SWP_AnamorphicFlare.createStripFlare(ship, new Vector2f(target.getLocation()), Global.getCombatEngine(), sqrtLevel / 20f, 1,
                            sqrtLevel / 20f, sqrtLevel / 5f, (float) Math.random() * 360f, 0f, 1f, COLOR2, COLOR3, true);

                    RippleDistortion ripple = new RippleDistortion(target.getLocation(), ZERO);
                    ripple.setSize(sqrtLevel * 15f);
                    ripple.setIntensity(sqrtLevel * 1f);
                    ripple.setFrameRate(360f / sqrtLevel2);
                    ripple.fadeInSize(sqrtLevel2 / 6f);
                    ripple.fadeOutIntensity(sqrtLevel2 / 6f);
                    DistortionShader.addDistortion(ripple);

                    EmpArcEntityAPI arc = Global.getCombatEngine().spawnEmpArc(ship, new Vector2f(target.getLocation()), new SimpleEntity(target.getLocation()), ship,
                            DamageType.ENERGY, 0f, 0f, 1500f, null, sqrtLevel / 10f, COLOR2, COLOR3);
                    arc.setSingleFlickerMode();
                    ship.getFluxTracker().increaseFlux(level, true);

                    if (target instanceof DamagingProjectileAPI) {
                        DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                        proj.addDamagedAlready(ship);
                    }
                    if (target instanceof MissileAPI) {
                        MissileAPI missile = (MissileAPI) target;
                        missile.interruptContrail();
                    }
                    Global.getCombatEngine().removeEntity(target);
                    if (target instanceof MissileAPI) {
                        MissileAPI missile = (MissileAPI) target;
//                        if (!missile.didDamage()) {
//                            missile.explode();
//                        }
                        if (missile.getHitpoints() > 0f) {
                            missile.setHitpoints(0f);
                        }
                    }

                    total += level;
                }

                Global.getCombatEngine().addFloatingDamageText(ship.getLocation(), total, COLOR2, ship, ship);
            }
            activated = true;
        }

        if (state == State.OUT) {
            if (activated) {
                activated = false;
                Global.getSoundPlayer().playSound("swp_phaseshuntdrive_impact", 1f, 0.25f + (float) Math.sqrt(total) / 30f, ship.getLocation(), ZERO);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        activated = false;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if ((system == null) || (ship == null)) {
            return null;
        }
        if (ship.getSystem().isOutOfAmmo()) {
            return null;
        }
        if (ship.getFluxTracker().isOverloadedOrVenting()) {
            return null;
        }
        if (!ship.isAlive()) {
            return null;
        }

        if (system.getState() == SystemState.IDLE) {
            float fluxLevel = ship.getFluxTracker().getCurrFlux() / ship.getMutableStats().getFluxCapacity().getBaseValue();
            float range = BASE_RANGE / (1f + fluxLevel);
            range = ship.getMutableStats().getSystemRangeBonus().computeEffective(range);

            List<CombatEntityAPI> targets = new ArrayList<>(Global.getCombatEngine().getProjectiles().size() / 4);
            targets.addAll(SWP_Util.getProjectilesWithinRange(ship.getLocation(), range));
            targets.addAll(SWP_Util.getMissilesWithinRange(ship.getLocation(), range));
            targets.addAll(SWP_Util.getAsteroidsWithinRange(ship.getLocation(), range));
            float currTotal = 0f;
            for (CombatEntityAPI target : targets) {
                if (target == null) {
                    continue;
                }

                float level;
                if (target instanceof DamagingProjectileAPI) {
                    DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                    if (proj.getBaseDamageAmount() <= 0) {
                        continue;
                    }
                    if (proj.didDamage()) {
                        continue;
                    }
                    if (proj.getSpawnType() == ProjectileSpawnType.OTHER) {
                        continue;
                    }
                    if ((proj.getProjectileSpecId() != null) && proj.getProjectileSpecId().startsWith("swp_excelsiorcannon_shot") && (proj.getOwner() == ship.getOwner())) {
                        continue;
                    }

                    if (proj.getDamageType() == DamageType.FRAGMENTATION) {
                        level = proj.getDamageAmount() * 0.25f + proj.getEmpAmount() * 0.25f;
                    } else {
                        level = proj.getDamageAmount() + proj.getEmpAmount() * 0.25f;
                    }
                } else {
                    level = target.getMass();
                }

                currTotal += level;
            }

            if (currTotal >= 6000f) {
                return "READY - EXTREME";
            } else if (currTotal >= 4000f) {
                return "READY - V.HIGH";
            } else if (currTotal >= 2400f) {
                return "READY - HIGH";
            } else if (currTotal >= 1200f) {
                return "READY - MED";
            } else if (currTotal >= 400f) {
                return "READY - LOW";
            } else if (currTotal > 0f) {
                return "READY - V.LOW";
            } else {
                return "READY - NONE";
            }
        }

        return null;
    }
}
