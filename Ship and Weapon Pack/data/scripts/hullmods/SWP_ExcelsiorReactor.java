package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.shipsystems.SWP_PhaseShuntDriveStats;
import data.scripts.util.SWP_Multi;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ExcelsiorReactor extends BaseHullMod {

    public static final float BEAM_DAMAGE_REDUCTION = 50f;
    public static final float EMP_DAMAGE_REDUCTION = 50f;
    public static final float BEAM_TO_FLUX_MULT = 1f;
    public static final float OVERLOAD_MULT = 50f;
    public static final float VENT_BONUS = 200f;

    private static final String DATA_KEY = "SWP_ExcelsiorReactor";
    private static final String DATA_KEY2 = "SWP_IndicatorRendererObject";
    private static final String DATA_KEY3 = "SWP_IndicatorRenderer";

    private static final Color VENT_COLOR_CORE = new Color(255, 100, 175);
    private static final Color VENT_COLOR_CORE_DARK = new Color(150, 0, 50);
    private static final Color VENT_COLOR_FRINGE = new Color(225, 0, 200);
    private static final Color VENT_COLOR_FRINGE_DARK = new Color(125, 0, 75);

    private static final Color GLOW_COLOR_400 = new Color(128, 0, 0);
    private static final Color GLOW_COLOR_6000 = new Color(128, 0, 255);
    private static final float PULSATE_FREQ_400 = 0.5f;
    private static final float PULSATE_FREQ_6000 = 3f;
    private static final float PULSATE_MAG_400 = 0.5f;
    private static final float PULSATE_MAG_6000 = 0.3f;
    private static final float FLICKER_RATE_400 = 3f;
    private static final float FLICKER_RATE_6000 = 30f;
    private static final float FLICKER_MAG_400 = 0.2f;
    private static final float FLICKER_MAG_6000 = 0.8f;

    private static final Vector2f ZERO = new Vector2f(0f, 0f);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine != null) {
            if (engine.getPlayerShip() == ship) {
                if (!engine.getCustomData().containsKey(DATA_KEY2)) {
                    engine.addLayeredRenderingPlugin(new IndicatorRenderer());
                    engine.getCustomData().put(DATA_KEY2, true);
                }
            }
        } else {
            return;
        }

        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, Float> tPulsateMap = localData.tPulsateMap;
        final Map<ShipAPI, IntervalUtil> intervals = localData.intervals;

        if (!tPulsateMap.containsKey(ship)) {
            tPulsateMap.put(ship, 0f);
        }
        if (!intervals.containsKey(ship)) {
            intervals.put(ship, new IntervalUtil(0.1f, 0.1f));
        }
        float tPulsate = tPulsateMap.get(ship);
        final IntervalUtil interval = intervals.get(ship);

        WeaponAPI glow = null;
        List<WeaponAPI> weapons = ship.getAllWeapons();
        for (WeaponAPI weapon : weapons) {
            if (weapon.getId().contentEquals("swp_excelsior_auxglow")) {
                glow = weapon;
                break;
            }
        }

        if (glow != null) {
            float effectLevel = (ship.getFluxTracker().getCurrFlux() - 400f) / (6000f - 400f);
            float fadeLevel = ship.getFluxTracker().getCurrFlux() / 400f;
            if (effectLevel > 0f) {
                glow.getAnimation().setFrame(1);
                SpriteAPI sprite = glow.getSprite();

                fadeLevel = 1f;

                float pulsateFreq = SWP_Util.lerp(PULSATE_FREQ_400, PULSATE_FREQ_6000, effectLevel);
                float pulsateMag = SWP_Util.lerp(PULSATE_MAG_400, PULSATE_MAG_6000, effectLevel);
                tPulsate += pulsateFreq * amount;
                fadeLevel *= 1f + ((pulsateMag * 0.5f) * (float) Math.sin(tPulsate * Math.PI * 2));

                float flickerRate = SWP_Util.lerp(FLICKER_RATE_400, FLICKER_RATE_6000, effectLevel);
                float flickerMag = SWP_Util.lerp(FLICKER_MAG_400, FLICKER_MAG_6000, effectLevel);
                float flickerChance = flickerRate * amount;
                if (Math.random() < flickerChance) {
                    fadeLevel *= 1f - flickerMag;
                }

                int red = SWP_Util.clamp255((int) ((GLOW_COLOR_400.getRed() + (effectLevel * (GLOW_COLOR_6000.getRed() - GLOW_COLOR_400.getRed()))) * fadeLevel));
                int green = SWP_Util.clamp255((int) ((GLOW_COLOR_400.getGreen() + (effectLevel * (GLOW_COLOR_6000.getGreen() - GLOW_COLOR_400.getGreen()))) * fadeLevel));
                int blue = SWP_Util.clamp255((int) ((GLOW_COLOR_400.getBlue() + (effectLevel * (GLOW_COLOR_6000.getBlue() - GLOW_COLOR_400.getBlue()))) * fadeLevel));

                sprite.setAdditiveBlend();
                sprite.setColor(new Color(red, green, blue, 255));
            } else if (fadeLevel > 0f) {
                glow.getAnimation().setFrame(1);
                SpriteAPI sprite = glow.getSprite();

                int red = SWP_Util.clamp255((int) (GLOW_COLOR_400.getRed() * fadeLevel));
                int green = SWP_Util.clamp255((int) (GLOW_COLOR_400.getGreen() * fadeLevel));
                int blue = SWP_Util.clamp255((int) (GLOW_COLOR_400.getBlue() * fadeLevel));

                sprite.setAdditiveBlend();
                sprite.setColor(new Color(red, green, blue, 255));
            } else {
                glow.getAnimation().setFrame(0);
            }
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        float fluxLevel = Math.max(0f, Math.min(1f, ship.getFluxTracker().getCurrFlux() / ship.getMutableStats().getFluxCapacity().getBaseValue()));
        Color glowColor = SWP_Util.interpolateColor255(GLOW_COLOR_400, GLOW_COLOR_6000, fluxLevel);
        Color highlightColor = SWP_Util.interpolateColor255(VENT_COLOR_CORE_DARK, VENT_COLOR_CORE, fluxLevel);

        if (ship.isHulk()) {
            Boolean deathExploded = (Boolean) ship.getCustomData().get("swp_excelsiordeath");
            if (deathExploded == null) {
                deathExploded = false;
            }
            if (!deathExploded) {
                if (ship.getFluxTracker().getCurrFlux() >= 6000f) {
                    Global.getSoundPlayer().playSound("swp_excelsior_boom4", 1f, 0.8f, ship.getLocation(), ZERO);
                }
                if (ship.getFluxTracker().getCurrFlux() >= 4000f) {
                    Global.getSoundPlayer().playSound("swp_excelsior_boom3", 1f, 0.5f, ship.getLocation(), ZERO);
                }
                if (ship.getFluxTracker().getCurrFlux() >= 2400f) {
                    Global.getSoundPlayer().playSound("swp_excelsior_boom2", 1f, 1f, ship.getLocation(), ZERO);
                }
                if (ship.getFluxTracker().getCurrFlux() >= 1200f) {
                    Global.getSoundPlayer().playSound("swp_excelsior_boom1", 1f, 1f, ship.getLocation(), ZERO);

                    engine.spawnExplosion(ship.getLocation(), ZERO, highlightColor, 750f * fluxLevel, fluxLevel);
                    engine.addHitParticle(ship.getLocation(), ZERO, 2000f * fluxLevel, 1f, (float) Math.sqrt(fluxLevel) * 2f, glowColor);
                    engine.addSmoothParticle(ship.getLocation(), ZERO, 2000f * fluxLevel, 1f, (float) Math.sqrt(fluxLevel) * 2f, glowColor);

                    StandardLight light = new StandardLight(ship.getLocation(), ZERO, ZERO, null);
                    light.setColor(glowColor);
                    light.setSize(1000f * fluxLevel);
                    light.setIntensity(fluxLevel);
                    light.fadeOut(0.75f * fluxLevel);
                    LightShader.addLight(light);

                    float range = (float) Math.sqrt(ship.getFluxTracker().getCurrFlux()) * 15f;
                    List<ShipAPI> targets = CombatUtils.getShipsWithinRange(ship.getLocation(), range + ship.getCollisionRadius());
                    targets.remove(ship);
                    int num = (int) Math.round(fluxLevel * 40f);
                    for (int i = 0; i < num; i++) {
                        if (!targets.isEmpty() && (Math.random() > 0.5)) {
                            ShipAPI target = targets.get(MathUtils.getRandom().nextInt(targets.size()));
                            if (SWP_Multi.isWithinEmpRange(ship.getLocation(), range * 1.25f, target)) {
                                Global.getCombatEngine().spawnEmpArcPierceShields(ship, MathUtils.getRandomPointOnCircumference(ship.getLocation(), 30f),
                                        ship, target, DamageType.ENERGY, 200f * fluxLevel, 200f * fluxLevel, range * 1.25f, null, 10f + (20f * fluxLevel),
                                        glowColor, highlightColor);
                            }
                        } else {
                            Global.getCombatEngine().spawnEmpArcVisual(MathUtils.getRandomPointOnCircumference(ship.getLocation(), 30f), ship,
                                    MathUtils.getRandomPointOnCircumference(ship.getLocation(), range), null, 10f + (20f * fluxLevel), glowColor, highlightColor);
                        }
                    }
                }
                ship.getFluxTracker().setHardFlux(0f);
                ship.getFluxTracker().setCurrFlux(0f);
                ship.getCustomData().put("swp_excelsiordeath", true);
            }
        }

        tPulsateMap.put(ship, tPulsate);

        interval.advance(amount);
        if (ship.getFluxTracker().isOverloaded()) {
            if (interval.intervalElapsed()) {
                if (Math.random() < 0.2) {
                    float shipRadius = SWP_Util.effectiveRadius(ship);

                    Global.getCombatEngine().spawnEmpArc(ship,
                            MathUtils.getRandomPointOnCircumference(ship.getLocation(), shipRadius * ((float) Math.random() + 1f)),
                            ship, ship, DamageType.ENERGY, 125f, 125f, 500f, null, 15f,
                            new Color(255, 150, 200),
                            new Color(255, 150, 255));
                }
            }
            ship.setWeaponGlow(0f, new Color(0, 0, 0, 0), EnumSet.of(WeaponType.ENERGY));
        } else if (ship.getFluxTracker().isVenting()) {
            if (interval.intervalElapsed()) {
                float r = (float) Math.random();
                ship.setVentCoreColor(SWP_Util.interpolateColor255(VENT_COLOR_CORE, VENT_COLOR_CORE_DARK, r));
                ship.setVentFringeColor(SWP_Util.interpolateColor255(VENT_COLOR_FRINGE, VENT_COLOR_FRINGE_DARK, r));
                float scale = ship.getMutableStats().getVentRateMult().getModifiedValue() * (2f / 3f)
                        * (ship.getMutableStats().getFluxDissipation().getModifiedValue() / 50f);
                if (Math.random() < (0.3 * Math.sqrt(scale))) {
                    float range = (float) Math.sqrt(ship.getFluxTracker().getCurrFlux()) * (float) Math.sqrt(scale) * 4f;
                    List<ShipAPI> targets = AIUtils.getNearbyEnemies(ship, range);
                    if (!targets.isEmpty()) {
                        ShipAPI target = targets.get(MathUtils.getRandom().nextInt(targets.size()));
                        if (SWP_Multi.isWithinEmpRange(ship.getLocation(), range * 1.25f, target)) {
                            Global.getCombatEngine().spawnEmpArc(ship, MathUtils.getRandomPointOnCircumference(ship.getLocation(), 30f),
                                    ship, target,
                                    DamageType.ENERGY,
                                    70f * scale, 70f * scale, range * 1.25f,
                                    "swp_excelsior_vent", 5f * scale,
                                    new Color(255, 50, 150), new Color(255, 100, 200));
                        }
                    } else {
                        AnchoredEntity entity = new AnchoredEntity(ship, MathUtils.getRandomPointOnCircumference(ship.getLocation(), 30f));
                        Global.getCombatEngine().spawnEmpArc(ship, MathUtils.getRandomPointOnCircumference(ship.getLocation(), range),
                                entity, entity,
                                DamageType.ENERGY, 0f, 0f, range * 2f, null, 5f * scale,
                                new Color(255, 50, 150),
                                new Color(255, 100, 200));
                    }
                }
            }
            ship.setWeaponGlow(0f, new Color(0, 0, 0, 0), EnumSet.of(WeaponType.ENERGY));
        } else {
            if (!ship.isAlive()) {
                ship.setWeaponGlow(0f, new Color(0, 0, 0, 0), EnumSet.of(WeaponType.ENERGY));
            } else {
                ship.setWeaponGlow(fluxLevel, glowColor, EnumSet.of(WeaponType.ENERGY));
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT / 100f);
        stats.getVentRateMult().modifyPercent(id, VENT_BONUS);
        stats.getEmpDamageTakenMult().modifyMult(id, EMP_DAMAGE_REDUCTION / 100f);
        stats.getBeamDamageTakenMult().modifyMult(id, BEAM_DAMAGE_REDUCTION / 100f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new ExcelsiorBeamSucc(ship));
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        switch (index) {
            case 0:
                return "" + (int) OVERLOAD_MULT + "%";
            case 1:
                return "" + (int) VENT_BONUS + "%";
            case 2:
                return "" + (int) EMP_DAMAGE_REDUCTION + "%";
            case 3:
                return "" + (int) BEAM_DAMAGE_REDUCTION + "%";
            default:
                break;
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

    private static final class LocalData {

        final Map<ShipAPI, IntervalUtil> intervals = new HashMap<>(50);
        final Map<ShipAPI, Float> tPulsateMap = new HashMap<>(50);
    }

    public static class ExcelsiorBeamSucc implements DamageTakenModifier {

        private final ShipAPI ship;

        public ExcelsiorBeamSucc(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (shieldHit) {
                return null;
            }

            float beamDamage = 0f;
            float empDamage = 0f;
            float amount = 1f;
            Color color = GLOW_COLOR_6000;
            if (param instanceof BeamAPI) {
                amount = damage.getDpsDuration();
                beamDamage = Math.min(5000f * amount, damage.computeDamageDealt(amount));
                empDamage = Math.min(5000f * amount, damage.computeFluxDealt(amount));
                color = ((BeamAPI) param).getFringeColor();
            }

            if ((beamDamage <= 0f) && (empDamage <= 0f)) {
                return null;
            }

            if (ship.getFluxTracker().getFluxLevel() >= 0.99f) {
                return null;
            }

            /* We're not actually modifying damage here; the stat modifiers are handled in the hullmod itself.
             * All we need to do is raise our ship's flux.  Note: these values should be the unmodified base damage of
             * the beam. */
            float fluxToRaise = 0.25f * BEAM_TO_FLUX_MULT * empDamage;
            if (damage.getType() == DamageType.FRAGMENTATION) {
                fluxToRaise += 0.25f * BEAM_TO_FLUX_MULT * beamDamage;
            } else {
                fluxToRaise += BEAM_TO_FLUX_MULT * beamDamage;
            }
            ship.getFluxTracker().increaseFlux(fluxToRaise, true);

            color = SWP_Util.fadeColor255(color, Math.min(1f, (float) Math.sqrt(fluxToRaise / amount) / 100f));
            Global.getCombatEngine().addSmoothParticle(ship.getLocation(), ship.getVelocity(), ship.getCollisionRadius() + Math.min(300f, (float) Math.sqrt(fluxToRaise / amount) * 20f),
                    0.1f, 0.1f + (0.4f * Math.min(1f, (float) Math.sqrt((fluxToRaise / amount) / 500f))), color);
            return null;
        }
    }

    private static class IndicatorRenderer implements CombatLayeredRenderingPlugin {

        private static final Color INDICATOR_COLOR_START = new Color(255, 255, 255, 255);
        private static final Color INDICATOR_COLOR = new Color(255, 0, 200, 255);

        private SpriteAPI indicatorSprite;

        @Override
        public void init(CombatEntityAPI entity) {
            indicatorSprite = Global.getSettings().getSprite("swp_indicators", "swp_excelsiorindicator");
            Global.getCombatEngine().getCustomData().put(DATA_KEY3, new LocalData());
        }

        @Override
        public void cleanup() {
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine() == null) {
                return;
            }

            final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY3);
            if (localData == null) {
                return;
            }

            float timeMult = Global.getCombatEngine().getTimeMult().getModifiedValue();

            final Map<CombatEntityAPI, IndicatorData> activeIndicators = localData.activeIndicators;
            for (Map.Entry<CombatEntityAPI, IndicatorData> activeIndicator : activeIndicators.entrySet()) {
                IndicatorData data = activeIndicator.getValue();
                data.timeElapsed += amount / timeMult;
            }

            final Map<CombatEntityAPI, LostData> lostIndicators = localData.lostIndicators;
            Iterator<Map.Entry<CombatEntityAPI, LostData>> iter = lostIndicators.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<CombatEntityAPI, LostData> lostIndicator = iter.next();
                CombatEntityAPI target = lostIndicator.getKey();
                LostData data = lostIndicator.getValue();
                data.timeLeft -= amount / timeMult;
                if (data.timeLeft <= 0f) {
                    iter.remove();
                    continue;
                }
                if (target instanceof DamagingProjectileAPI) {
                    DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                    if (proj.didDamage()) {
                        iter.remove();
                    }
                }
            }
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.FF_INDICATORS_LAYER);
        }

        @Override
        public float getRenderRadius() {
            return 99999999f;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (Global.getCombatEngine() == null) {
                return;
            }

            ShipAPI ship = Global.getCombatEngine().getPlayerShip();
            if (ship == null) {
                return;
            }
            if (!ship.getHullSpec().getBaseHullId().startsWith("swp_excelsior") && !ship.getHullSpec().getBaseHullId().startsWith("swp_boss_excelsior")) {
                return;
            }
            if (ship.getSystem() == null) {
                return;
            }

            boolean isActive = true;
            if (ship.getSystem().isOutOfAmmo()) {
                isActive = false;
            }
            if (ship.getSystem().getState() != SystemState.IDLE) {
                isActive = false;
            }
            if (ship.getFluxTracker().isOverloadedOrVenting()) {
                isActive = false;
            }
            if (!ship.isAlive()) {
                isActive = false;
            }

            final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY3);
            if (localData == null) {
                return;
            }

            final Map<CombatEntityAPI, IndicatorData> indicators = localData.indicators;
            final Map<CombatEntityAPI, IndicatorData> activeIndicators = localData.activeIndicators;
            final Map<CombatEntityAPI, LostData> lostIndicators = localData.lostIndicators;

            float fluxLevel = ship.getFluxTracker().getCurrFlux() / ship.getMutableStats().getFluxCapacity().getBaseValue();
            float range = SWP_PhaseShuntDriveStats.BASE_RANGE / (1f + fluxLevel);
            range = ship.getMutableStats().getSystemRangeBonus().computeEffective(range);

            if (isActive) {
                List<CombatEntityAPI> targets = new ArrayList<>(Global.getCombatEngine().getProjectiles().size() / 4);
                targets.addAll(SWP_Util.getProjectilesWithinRange(ship.getLocation(), range));
                targets.addAll(SWP_Util.getMissilesWithinRange(ship.getLocation(), range));
                targets.addAll(SWP_Util.getAsteroidsWithinRange(ship.getLocation(), range));
                for (CombatEntityAPI target : targets) {
                    if (target == null) {
                        continue;
                    }

                    float radius;
                    float powerFactor;
                    IndicatorData data = activeIndicators.get(target);
                    if (data == null) {
                        radius = target.getCollisionRadius();
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
                            if (proj.getProjectileSpec() != null) {
                                if ((proj.getProjectileSpec().getLength() > 0f) && (proj.getProjectileSpec().getWidth() > 0f)) {
                                    if (proj.getProjectileSpec().getLength() > proj.getProjectileSpec().getWidth()) {
                                        float ratio = proj.getProjectileSpec().getLength() / proj.getProjectileSpec().getWidth();
                                        radius = proj.getProjectileSpec().getWidth() * (float) Math.sqrt(ratio) / 2f;
                                    } else {
                                        float ratio = proj.getProjectileSpec().getWidth() / proj.getProjectileSpec().getLength();
                                        radius = proj.getProjectileSpec().getLength() * (float) Math.sqrt(ratio) / 2f;
                                    }
                                }
                            }
                            if (proj instanceof MissileAPI) {
                                MissileAPI missile = (MissileAPI) proj;
                                if (missile.getSpriteAPI() != null) {
                                    if ((missile.getSpriteAPI().getHeight() > 0f) && (missile.getSpriteAPI().getWidth() > 0f)) {
                                        if (missile.getSpriteAPI().getHeight() > missile.getSpriteAPI().getWidth()) {
                                            float ratio = missile.getSpriteAPI().getHeight() / missile.getSpriteAPI().getWidth();
                                            radius = missile.getSpriteAPI().getWidth() * (float) Math.sqrt(ratio) / 2f;
                                        } else {
                                            float ratio = missile.getSpriteAPI().getWidth() / missile.getSpriteAPI().getHeight();
                                            radius = missile.getSpriteAPI().getHeight() * (float) Math.sqrt(ratio) / 2f;
                                        }
                                    }
                                }
                            }

                            if (proj.getDamageType() == DamageType.FRAGMENTATION) {
                                powerFactor = ((proj.getDamageAmount() * 0.25f) + (proj.getEmpAmount() * 0.25f)) / 500f;
                            } else {
                                powerFactor = (proj.getDamageAmount() + (proj.getEmpAmount() * 0.25f)) / 500f;
                            }
                        } else {
                            powerFactor = target.getMass() / 500f;
                        }
                        powerFactor = Math.min(10f, powerFactor);

                        /* Make the radius represent the power */
                        if ((radius > 0f) && (powerFactor > 0f)) {
                            if (radius > ((float) Math.sqrt(powerFactor) * 50f)) {
                                float ratio = ((float) Math.sqrt(powerFactor) * 50f) / radius;
                                radius *= (float) Math.sqrt(ratio);
                            } else {
                                radius = (float) Math.sqrt(powerFactor) * 50f;
                            }
                        }
                        radius *= 3f;
                    } else {
                        radius = data.radius;
                        powerFactor = data.powerFactor;
                        if (target instanceof DamagingProjectileAPI) {
                            DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                            if (proj.didDamage()) {
                                continue;
                            }
                        }
                    }

                    float time = 0f;
                    if (data != null) {
                        time = data.timeElapsed;
                    } else {
                        data = new IndicatorData(radius, powerFactor);
                    }

                    float t = Math.min(1f, time / (0.3f * (1f + (0.3f * Math.min(2f, (float) Math.sqrt(powerFactor))))));
                    float fadeT = Math.min(1f, time / 0.1f);
                    Color indColor = SWP_Util.interpolateColor255(INDICATOR_COLOR_START, INDICATOR_COLOR, t);
                    indColor = SWP_Util.fadeColor255(indColor, fadeT * 0.5f * (1f + powerFactor));
                    indicatorSprite.setAdditiveBlend();
                    indicatorSprite.setColor(indColor);
                    indicatorSprite.setWidth(radius * (1.25f - (t * 0.25f)));
                    indicatorSprite.setHeight(radius * (1.25f - (t * 0.25f)));
                    indicatorSprite.renderAtCenter(target.getLocation().getX(), target.getLocation().getY());

                    indicators.put(target, data);
                }

                for (Map.Entry<CombatEntityAPI, IndicatorData> activeIndicator : activeIndicators.entrySet()) {
                    CombatEntityAPI target = activeIndicator.getKey();
                    if (!indicators.containsKey(target) && !lostIndicators.containsKey(target)) {
                        if (target instanceof DamagingProjectileAPI) {
                            DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                            if (proj.didDamage()) {
                                continue;
                            }
                        }
                        if (!Global.getCombatEngine().isEntityInPlay(target)) {
                            continue;
                        }

                        IndicatorData indData = activeIndicator.getValue();

                        float fadeT = Math.min(1f, indData.timeElapsed / 0.1f);
                        float endTime = 0.1f * (1f + (0.3333f * Math.min(2f, (float) Math.sqrt(indData.powerFactor))));
                        LostData lostData = new LostData(indData.timeElapsed, indData.radius, indData.powerFactor, endTime, fadeT * endTime);
                        lostIndicators.put(target, lostData);
                    }
                }
            }

            for (Map.Entry<CombatEntityAPI, LostData> lostIndicator : lostIndicators.entrySet()) {
                CombatEntityAPI target = lostIndicator.getKey();
                LostData data = lostIndicator.getValue();

                float t = Math.min(1f, data.activeTime / (0.3f * (1f + (0.3f * Math.min(2f, (float) Math.sqrt(data.powerFactor))))));
                float fadeT = Math.min(1f, data.activeTime / 0.1f) * data.timeLeft / data.origTimeLeft;
                Color indColor = SWP_Util.interpolateColor255(INDICATOR_COLOR_START, INDICATOR_COLOR, t);
                indColor = SWP_Util.fadeColor255(indColor, fadeT * 0.5f * (1f + data.powerFactor));
                indicatorSprite.setAdditiveBlend();
                indicatorSprite.setColor(indColor);
                indicatorSprite.setWidth(data.radius * (1.25f - (t * 0.25f)));
                indicatorSprite.setHeight(data.radius * (1.25f - (t * 0.25f)));
                indicatorSprite.renderAtCenter(target.getLocation().getX(), target.getLocation().getY());
            }

            activeIndicators.clear();
            activeIndicators.putAll(indicators);
            indicators.clear();
        }

        private static final class IndicatorData {

            float timeElapsed = 0f;
            final float radius;
            final float powerFactor;

            IndicatorData(float radius, float powerFactor) {
                this.radius = radius;
                this.powerFactor = powerFactor;
            }
        }

        private static final class LostData {

            final float activeTime;
            final float radius;
            final float powerFactor;
            float origTimeLeft;
            float timeLeft;

            LostData(float activeTime, float radius, float powerFactor, float origTimeLeft, float timeLeft) {
                this.activeTime = activeTime;
                this.radius = radius;
                this.powerFactor = powerFactor;
                this.origTimeLeft = origTimeLeft;
                this.timeLeft = timeLeft;
            }
        }

        private static final class LocalData {

            final Map<CombatEntityAPI, IndicatorData> indicators = new HashMap<>(100);
            final Map<CombatEntityAPI, IndicatorData> activeIndicators = new LinkedHashMap<>(100);
            final Map<CombatEntityAPI, LostData> lostIndicators = new LinkedHashMap<>(100);
        }
    }
}
