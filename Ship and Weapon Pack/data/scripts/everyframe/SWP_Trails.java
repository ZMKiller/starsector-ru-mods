package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.SWPModPlugin;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

public class SWP_Trails extends BaseEveryFrameCombatPlugin {

    private static final String CONTENDER_PROJECTILE_ID = "swp_contender_shot";
    private static final String FLAMER_PROJECTILE_ID = "swp_plasmaflame_shot";
    private static final String ION_BLASTER_PROJECTILE_ID = "swp_ionblaster_shot";

    private static final String GUNGNIR_PROJECTILE_ID = "swp_gungnir_shot";
    private static final String GUNGNIR_SUBMUNITION_PROJECTILE_ID = "swp_gungnir_sub_shot";
    private static final String EXCELSIOR_4_PROJECTILE_ID = "swp_excelsiorcannon_shot_4";
    private static final String EXCELSIOR_12_PROJECTILE_ID = "swp_excelsiorcannon_shot_12";
    private static final String EXCELSIOR_24_PROJECTILE_ID = "swp_excelsiorcannon_shot_24";
    private static final String EXCELSIOR_40_PROJECTILE_ID = "swp_excelsiorcannon_shot_40";
    private static final String EXCELSIOR_60_PROJECTILE_ID = "swp_excelsiorcannon_shot_60";

    private static final String CANISTER_PROJECTILE_ID = "swp_boss_canistercannon_shot";
    private static final String CANISTER_SUBMUNITION_PROJECTILE_ID = "swp_boss_canister_sub_shot";

    private static final String MEGAPULSE_PROJECTILE_ID = "swp_arcade_megapulse_shot";

    private static final Color CONTENDER_TRAIL_COLOR = new Color(255, 150, 135);
    private static final Color ION_BLASTER_TRAIL_COLOR_START = new Color(200, 255, 255);
    private static final Color ION_BLASTER_TRAIL_COLOR_END = new Color(100, 255, 255);
    private static final Color ION_BLASTER_TRAIL2_COLOR_START = new Color(85, 160, 210);
    private static final Color ION_BLASTER_TRAIL2_COLOR_END = new Color(20, 130, 65);

    private static final Color EXCELSIOR_TRAIL_COLOR = new Color(255, 100, 150);
    private static final Color EXCELSIOR_TRAIL2_COLOR = new Color(150, 100, 255);
    private static final Color EXCELSIOR_TRAIL3_COLOR_START = new Color(255, 100, 50);
    private static final Color EXCELSIOR_TRAIL3_COLOR_END = new Color(50, 100, 255);
    private static final Color GUNGNIR_TRAIL_COLOR_START = new Color(255, 200, 100);
    private static final Color GUNGNIR_TRAIL_COLOR_END = new Color(255, 150, 50);
    private static final Color GUNGNIR_TRAIL2_COLOR = new Color(100, 90, 80);
    private static final Color GUNGNIR_SUBMUNITION_TRAIL_COLOR_START = new Color(150, 110, 105);
    private static final Color GUNGNIR_SUBMUNITION_TRAIL_COLOR_END = new Color(150, 100, 60);

    private static final Color CANISTER_TRAIL_COLOR_START = new Color(255, 150, 100);
    private static final Color CANISTER_TRAIL_COLOR_END = new Color(255, 125, 75);
    private static final Color CANISTER_TRAIL2_COLOR = new Color(100, 90, 80);
    private static final Color CANISTER_SUBMUNITION_TRAIL_COLOR_START = new Color(150, 110, 110);
    private static final Color CANISTER_SUBMUNITION_TRAIL_COLOR_END = new Color(150, 90, 70);

    private static final Color MEGAPULSE_TRAIL_COLOR_START = new Color(100, 225, 255);
    private static final Color MEGAPULSE_TRAIL_COLOR_END = new Color(50, 200, 255);

    private static final float SIXTY_FPS = 1f / 60f;

    private static final String DATA_KEY = "SWP_Trails";

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<DamagingProjectileAPI, TrailData> trailMap = localData.trailMap;

        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        double trailCount = 0f;
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI projectile = projectiles.get(i);
            if (projectile.getProjectileSpecId() == null) {
                continue;
            }

            switch (projectile.getProjectileSpecId()) {
                case CONTENDER_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.3f)) {
                        trailCount += 1f;
                    }
                }
                case FLAMER_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.35f)) {
                        trailCount += 1f / 7.5f;
                    }
                }
                case ION_BLASTER_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.2f)) {
                        trailCount += 5f;
                    }
                }
                case GUNGNIR_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 1.2f)) {
                        trailCount += 2f;
                    }
                }
                case GUNGNIR_SUBMUNITION_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.6f)) {
                        trailCount += 1f / 2f;
                    }
                }
                case EXCELSIOR_4_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.2f)) {
                        trailCount += 1f;
                    }
                }
                case EXCELSIOR_12_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.25f)) {
                        trailCount += 2f;
                    }
                }
                case EXCELSIOR_24_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.3f)) {
                        trailCount += 3f;
                    }
                }
                case EXCELSIOR_40_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.3f)) {
                        trailCount += 4f;
                    }
                }
                case EXCELSIOR_60_PROJECTILE_ID -> {
                    if (SWP_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.3f)) {
                        trailCount += 5f;
                    }
                }
                default -> {
                }
            }
        }

        float trailFPSRatio = Math.min(3f, (float) Math.max(1f, (trailCount / 30f)));

        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            String spec = proj.getProjectileSpecId();
            TrailData data;
            if (spec == null) {
                continue;
            }

            boolean enableAngleFade = true;
            switch (spec) {
                case MEGAPULSE_PROJECTILE_ID ->
                    enableAngleFade = false;

                default -> {
                }
            }

            switch (spec) {
                case CONTENDER_PROJECTILE_ID, FLAMER_PROJECTILE_ID, ION_BLASTER_PROJECTILE_ID, GUNGNIR_PROJECTILE_ID, GUNGNIR_SUBMUNITION_PROJECTILE_ID,
                        EXCELSIOR_4_PROJECTILE_ID, EXCELSIOR_12_PROJECTILE_ID, EXCELSIOR_24_PROJECTILE_ID, EXCELSIOR_40_PROJECTILE_ID, EXCELSIOR_60_PROJECTILE_ID,
                        CANISTER_PROJECTILE_ID, CANISTER_SUBMUNITION_PROJECTILE_ID, MEGAPULSE_PROJECTILE_ID -> {
                    data = trailMap.get(proj);
                    if (data == null) {
                        data = new TrailData();
                        data.id = MagicTrailPlugin.getUniqueID();

                        switch (spec) {
                            case GUNGNIR_PROJECTILE_ID:
                            case EXCELSIOR_12_PROJECTILE_ID:
                            case CANISTER_PROJECTILE_ID:
                                data.id2 = MagicTrailPlugin.getUniqueID();
                                break;

                            case EXCELSIOR_24_PROJECTILE_ID:
                                data.id2 = MagicTrailPlugin.getUniqueID();
                                data.id3 = MagicTrailPlugin.getUniqueID();
                                break;

                            case EXCELSIOR_40_PROJECTILE_ID:
                                data.id2 = MagicTrailPlugin.getUniqueID();
                                data.id3 = MagicTrailPlugin.getUniqueID();
                                data.id4 = MagicTrailPlugin.getUniqueID();
                                break;

                            case ION_BLASTER_PROJECTILE_ID:
                            case EXCELSIOR_60_PROJECTILE_ID:
                                data.id2 = MagicTrailPlugin.getUniqueID();
                                data.id3 = MagicTrailPlugin.getUniqueID();
                                data.id4 = MagicTrailPlugin.getUniqueID();
                                data.id5 = MagicTrailPlugin.getUniqueID();
                                break;

                            default:
                                break;
                        }
                    }

                    trailMap.put(proj, data);
                }
                default -> {
                    continue;
                }
            }

            if (!data.enabled) {
                continue;
            }

            float fade = 1f;
            if (proj.getBaseDamageAmount() > 0f) {
                fade = proj.getDamageAmount() / proj.getBaseDamageAmount();
            }

            if (enableAngleFade) {
                float velFacing = VectorUtils.getFacing(proj.getVelocity());
                float angleError = Math.abs(MathUtils.getShortestRotation(proj.getFacing(), velFacing));

                float angleFade = 1f - Math.min(Math.max(angleError - 45f, 0f) / 45f, 1f);
                fade *= angleFade;

                if (angleFade <= 0f) {
                    if (!data.cut) {
                        MagicTrailPlugin.cutTrailsOnEntity(proj);
                        data.cut = true;
                    }
                } else {
                    data.cut = false;
                }
            }

            if (fade <= 0f) {
                continue;
            }

            fade = Math.max(0f, Math.min(1f, fade));

            Vector2f projVel = new Vector2f(proj.getVelocity());
            Vector2f projBodyVel = VectorUtils.rotate(new Vector2f(projVel), -proj.getFacing());
            Vector2f projLateralBodyVel = new Vector2f(0f, projBodyVel.getY());
            Vector2f sidewaysVel = VectorUtils.rotate(new Vector2f(projLateralBodyVel), proj.getFacing());

            Vector2f spawnPosition = new Vector2f(proj.getLocation());
            if (proj.getSpawnType() != ProjectileSpawnType.BALLISTIC_AS_BEAM) {
                spawnPosition.x += sidewaysVel.x * amount * -1.05f;
                spawnPosition.y += sidewaysVel.y * amount * -1.05f;
            }

            switch (spec) {
                case CONTENDER_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_smoothtrail"), /* sprite */
                                proj.getLocation(), /* position */
                                -100f, /* startSpeed */
                                -100f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                Math.max(fade * 7f, 3f), /* startSize */
                                3f, /* endSize */
                                CONTENDER_TRAIL_COLOR, /* startColor */
                                CONTENDER_TRAIL_COLOR, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.3f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case FLAMER_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        if (Math.random() <= 0.66) {
                            data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS * 2f);
                        } else {
                            data.enabled = false;
                            continue;
                        }
                    }
                    data.interval.advance(amount / trailFPSRatio);
                    if (data.interval.intervalElapsed()) {
                        float visualFacing = VectorUtils.getAngle(proj.getTailEnd(), proj.getLocation());
                        projLateralBodyVel.y *= 0.5 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_fuzzytrail"), /* sprite */
                                proj.getLocation(), /* position */
                                100f, /* startSpeed */
                                25f, /* endSpeed */
                                (visualFacing - 180f) + MathUtils.getRandomNumberInRange(-15f, 15f), /* angle */
                                0f, /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-60f, 60f), /* endAngularVelocity */
                                50f * MathUtils.getRandomNumberInRange(0.9f, 1.1f), /* startSize */
                                80f * MathUtils.getRandomNumberInRange(1f, 1.25f) * MathUtils.getRandomNumberInRange(1f, 1.25f), /* endSize */
                                new Color(MathUtils.getRandomNumberInRange(200, 255), MathUtils.getRandomNumberInRange(0, 50), MathUtils.getRandomNumberInRange(0, 25)), /* startColor */
                                new Color(MathUtils.getRandomNumberInRange(100, 200), MathUtils.getRandomNumberInRange(0, 75), MathUtils.getRandomNumberInRange(0, 50)), /* endColor */
                                fade * 0.6f * MathUtils.getRandomNumberInRange(0.9f, 1.1f), /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.15f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case ION_BLASTER_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 10f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                35f, /* startSize */
                                10f, /* endSize */
                                ION_BLASTER_TRAIL_COLOR_START, /* startColor */
                                ION_BLASTER_TRAIL_COLOR_END, /* endColor */
                                fade, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.12f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                256f, /* textureLoopLength */
                                2500f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 400f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 800f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-200f, 200f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-500f, 500f), /* endAngularVelocity */
                                25f, /* startSize */
                                5f, /* endSize */
                                ION_BLASTER_TRAIL2_COLOR_START, /* startColor */
                                ION_BLASTER_TRAIL2_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id3, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 400f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 800f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-200f, 200f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-500f, 500f), /* endAngularVelocity */
                                25f, /* startSize */
                                5f, /* endSize */
                                ION_BLASTER_TRAIL2_COLOR_START, /* startColor */
                                ION_BLASTER_TRAIL2_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id4, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 400f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 800f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-200f, 200f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-500f, 500f), /* endAngularVelocity */
                                25f, /* startSize */
                                5f, /* endSize */
                                ION_BLASTER_TRAIL2_COLOR_START, /* startColor */
                                ION_BLASTER_TRAIL2_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id5, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 400f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 800f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-200f, 200f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-500f, 500f), /* endAngularVelocity */
                                25f, /* startSize */
                                5f, /* endSize */
                                ION_BLASTER_TRAIL2_COLOR_START, /* startColor */
                                ION_BLASTER_TRAIL2_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case GUNGNIR_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 20f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_fuzzytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                14f, /* startSize */
                                8f, /* endSize */
                                GUNGNIR_TRAIL_COLOR_START, /* startColor */
                                GUNGNIR_TRAIL_COLOR_END, /* endColor */
                                fade, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.5f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                300f, /* textureLoopLength */
                                500f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_dirtytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                35f, /* startSize */
                                50f, /* endSize */
                                GUNGNIR_TRAIL2_COLOR, /* startColor */
                                GUNGNIR_TRAIL2_COLOR, /* endColor */
                                fade * 0.5f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                1.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                300f, /* textureLoopLength */
                                800f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case GUNGNIR_SUBMUNITION_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS * 2f, SIXTY_FPS * 2f);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 5f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_dirtytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                10f, /* startSize */
                                20f, /* endSize */
                                GUNGNIR_SUBMUNITION_TRAIL_COLOR_START, /* startColor */
                                GUNGNIR_SUBMUNITION_TRAIL_COLOR_END, /* endColor */
                                fade * 0.3f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.6f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                400f, /* textureLoopLength */
                                400f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case EXCELSIOR_4_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 6f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                18f, /* startSize */
                                9f, /* endSize */
                                EXCELSIOR_TRAIL_COLOR, /* startColor */
                                EXCELSIOR_TRAIL_COLOR, /* endColor */
                                fade * 0.4f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case EXCELSIOR_12_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 8f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                24f, /* startSize */
                                12f, /* endSize */
                                EXCELSIOR_TRAIL_COLOR, /* startColor */
                                EXCELSIOR_TRAIL_COLOR, /* endColor */
                                fade * 0.55f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_smoothtrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                20f, /* startSize */
                                30f, /* endSize */
                                EXCELSIOR_TRAIL2_COLOR, /* startColor */
                                EXCELSIOR_TRAIL2_COLOR, /* endColor */
                                fade * 0.3f, /* opacity */
                                0.05f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case EXCELSIOR_24_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 10f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                30f, /* startSize */
                                15f, /* endSize */
                                EXCELSIOR_TRAIL_COLOR, /* startColor */
                                EXCELSIOR_TRAIL_COLOR, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_smoothtrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                25f, /* startSize */
                                37.5f, /* endSize */
                                EXCELSIOR_TRAIL2_COLOR, /* startColor */
                                EXCELSIOR_TRAIL2_COLOR, /* endColor */
                                fade * 0.45f, /* opacity */
                                0.05f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id3, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 600f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-300f, 300f), /* endAngularVelocity */
                                12.5f, /* startSize */
                                6.25f, /* endSize */
                                EXCELSIOR_TRAIL3_COLOR_START, /* startColor */
                                EXCELSIOR_TRAIL3_COLOR_END, /* endColor */
                                fade * 0.5f, /* opacity */
                                0.05f, /* inDuration */
                                0.05f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case EXCELSIOR_40_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 12f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                36f, /* startSize */
                                18f, /* endSize */
                                EXCELSIOR_TRAIL_COLOR, /* startColor */
                                EXCELSIOR_TRAIL_COLOR, /* endColor */
                                fade * 0.85f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_smoothtrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                30f, /* startSize */
                                45f, /* endSize */
                                EXCELSIOR_TRAIL2_COLOR, /* startColor */
                                EXCELSIOR_TRAIL2_COLOR, /* endColor */
                                fade * 0.6f, /* opacity */
                                0.05f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id3, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 600f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-300f, 300f), /* endAngularVelocity */
                                20f, /* startSize */
                                10f, /* endSize */
                                EXCELSIOR_TRAIL3_COLOR_START, /* startColor */
                                EXCELSIOR_TRAIL3_COLOR_END, /* endColor */
                                fade * 0.6f, /* opacity */
                                0.075f, /* inDuration */
                                0.025f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id4, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 800f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-400f, 400f), /* endAngularVelocity */
                                10f, /* startSize */
                                5f, /* endSize */
                                EXCELSIOR_TRAIL3_COLOR_START, /* startColor */
                                EXCELSIOR_TRAIL3_COLOR_END, /* endColor */
                                fade * 0.6f, /* opacity */
                                0.05f, /* inDuration */
                                0.05f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case EXCELSIOR_60_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 14f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                42f, /* startSize */
                                21f, /* endSize */
                                EXCELSIOR_TRAIL_COLOR, /* startColor */
                                EXCELSIOR_TRAIL_COLOR, /* endColor */
                                fade, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_smoothtrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                35f, /* startSize */
                                52.5f, /* endSize */
                                EXCELSIOR_TRAIL2_COLOR, /* startColor */
                                EXCELSIOR_TRAIL2_COLOR, /* endColor */
                                fade * 0.85f, /* opacity */
                                0.05f, /* inDuration */
                                0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id3, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 600f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-300f, 300f), /* endAngularVelocity */
                                26.25f, /* startSize */
                                13.125f, /* endSize */
                                EXCELSIOR_TRAIL3_COLOR_START, /* startColor */
                                EXCELSIOR_TRAIL3_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0.1f, /* inDuration */
                                0.0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id4, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 800f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-400f, 400f), /* endAngularVelocity */
                                17.5f, /* startSize */
                                8.75f, /* endSize */
                                EXCELSIOR_TRAIL3_COLOR_START, /* startColor */
                                EXCELSIOR_TRAIL3_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0.075f, /* inDuration */
                                0.025f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id5, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 1000f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-500f, 500f), /* endAngularVelocity */
                                8.75f, /* startSize */
                                4.375f, /* endSize */
                                EXCELSIOR_TRAIL3_COLOR_START, /* startColor */
                                EXCELSIOR_TRAIL3_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0.05f, /* inDuration */
                                0.05f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case CANISTER_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 30f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_fuzzytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                15f, /* startSize */
                                10f, /* endSize */
                                CANISTER_TRAIL_COLOR_START, /* startColor */
                                CANISTER_TRAIL_COLOR_END, /* endColor */
                                fade, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.8f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                500f, /* textureLoopLength */
                                400f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_dirtytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                30f, /* startSize */
                                60f, /* endSize */
                                CANISTER_TRAIL2_COLOR, /* startColor */
                                CANISTER_TRAIL2_COLOR, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                500f, /* textureLoopLength */
                                600f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case CANISTER_SUBMUNITION_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS * 2f, SIXTY_FPS * 2f);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 5f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_dirtytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                10f, /* startSize */
                                20f, /* endSize */
                                CANISTER_SUBMUNITION_TRAIL_COLOR_START, /* startColor */
                                CANISTER_SUBMUNITION_TRAIL_COLOR_END, /* endColor */
                                fade * 0.4f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.8f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                400f, /* textureLoopLength */
                                400f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                case MEGAPULSE_PROJECTILE_ID -> {
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("swp_trails", "swp_cleantrail"), /* sprite */
                                proj.getLocation(), /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                VectorUtils.getFacing(projVel) - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                60f, /* startSize */
                                50f, /* endSize */
                                MEGAPULSE_TRAIL_COLOR_START, /* startColor */
                                MEGAPULSE_TRAIL_COLOR_END, /* endColor */
                                fade, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                new Vector2f(0f, 0f), /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                }

                default -> {
                }
            }
        }

        /* Clean up */
        Iterator<DamagingProjectileAPI> iter = trailMap.keySet().iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (!engine.isEntityInPlay(proj)) {
                iter.remove();
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    public static void createIfNeeded() {
        if (!SWPModPlugin.hasMagicLib) {
            return;
        }

        if (Global.getCombatEngine() != null) {
            if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
                Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
                Global.getCombatEngine().addPlugin(new SWP_Trails());
            }
        }
    }

    private static final class LocalData {

        final Map<DamagingProjectileAPI, TrailData> trailMap = new LinkedHashMap<>(100);
    }

    private static final class TrailData {

        Float id = null;
        Float id2 = null;
        Float id3 = null;
        Float id4 = null;
        Float id5 = null;
        IntervalUtil interval = null;
        boolean cut = false;
        boolean enabled = true;
    }
}
