package org.dark.shaders.util;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.dark.graphics.light.ThreatMapObject;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.post.PostProcessShader;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;

/**
 * This class handles settings for GraphicsLib and is intended solely for internal use.
 * <p>
 * @author DarkRevenant
 * <p>
 * @since 1.12.0
 */
public class GraphicsLibSettings {

    public static final String SETTINGS_FILE = "GRAPHICS_OPTIONS.ini";

    private static final String WAVE_PATH = "graphics/shaders/distortions/wave.png";
    private static final String LARGE_RIPPLE_PATH_START = "graphics/shaders/distortions/ripple/";
    private static final String SMALL_RIPPLE_PATH_START = "graphics/shaders/distortions/smallripple/";

    private static boolean useLunaLib = false;

    private static boolean enableShaders = false;
    private static boolean aaCompatMode = false;
    private static boolean use64BitBuffer = false;
    private static boolean extraScreenClear = false;
    private static int debugLevel = 0;
    private static int toggleKey = 197;
    private static int reloadKey = 68;

    private static boolean enableLights = false;
    private static boolean preloadAllMaps = false;
    private static boolean loadMaterial = false;
    private static int maximumLights = 100;
    private static int maximumLineLights = 50;
    private static float intensityScale = 0.5f;
    private static float sizeScale = 2f;
    private static float fighterBrightnessScale = 1f;
    private static float defaultMaterialBrightness = 0.8f;
    private static boolean enableBloom = false;
    private static int bloomQuality = 5;
    private static int bloomMips = 2;
    private static float bloomScale = 1f;
    private static float bloomIntensity = 0.5f;
    private static boolean enableNormal = false;
    private static boolean autoGenNormals = false;
    private static boolean optimizeNormals = false;
    private static boolean loadSurface = false;
    private static float specularIntensity = 2.5f;
    private static float specularHardness = 0.85f;
    private static float normalFlatness = 0f;
    private static float lightDepth = 0.2f;
    private static float weaponFlashHeight = 50f;
    private static float weaponLightHeight = 25f;

    private static boolean enableDistortion = false;
    private static boolean useLargeRipple = false;
    private static boolean useSmallRipple = false;
    private static int maximumDistortions = 100;

    private static boolean enablePostProcess = false;
    private static int colorBlindnessMode = 0;

    private static boolean drawOffscreenParticles = false;
    private static boolean enableShieldRipples = false;
    private static boolean enableMjolnirRipples = false;
    private static boolean enableExplosionEffects = false;
    private static boolean enableFullExplosionEffects = false;
    private static boolean enableExplosionShockwave = false;
    private static boolean enableExplosionTrails = false;
    private static float explosionTrailScale = 0f;
    private static boolean enableOverloadArcs = false;
    private static boolean useVentColorsForOverloadArcs = false;
    private static boolean enableWeaponSmoke = false;
    private static boolean enableInsignias = false;
    private static boolean enableSunLight = false;
    private static boolean enableHyperLight = false;

    static {
        Global.getLogger(GraphicsLibSettings.class).setLevel(Level.DEBUG);
    }

    public static void load() throws IOException, JSONException {
        final JSONObject settings = Global.getSettings().loadJSON(GraphicsLibSettings.SETTINGS_FILE);

        useLunaLib = Global.getSettings().getModManager().isModEnabled("lunalib");

        enableShaders = settings.getBoolean("enableShaders");
        toggleKey = settings.getInt("toggleKey");
        reloadKey = settings.getInt("reloadKey");
        if (enableShaders) {
            aaCompatMode = settings.getBoolean("aaCompatMode");
            use64BitBuffer = settings.getBoolean("use64BitBuffer");
            extraScreenClear = settings.getBoolean("extraScreenClear");
            debugLevel = settings.getInt("debugLevel");

            enableLights = settings.getBoolean("enableLights");
            if (enableLights) {
                preloadAllMaps = settings.getBoolean("preloadAllMaps");
                loadMaterial = settings.getBoolean("loadMaterial");
                maximumLights = settings.getInt("maximumLights");
                maximumLineLights = settings.getInt("maximumLineLights");
                intensityScale = (float) settings.getDouble("intensityScale");
                sizeScale = (float) settings.getDouble("sizeScale");
                fighterBrightnessScale = (float) settings.getDouble("fighterBrightnessScale");
                defaultMaterialBrightness = (float) settings.getDouble("defaultMaterialBrightness");
                enableBloom = settings.getBoolean("enableBloom");
                if (enableBloom) {
                    bloomQuality = Math.max(Math.min(settings.getInt("bloomQuality"), 5), 1);
                    bloomMips = Math.max(Math.min(settings.getInt("bloomMips"), 5), 1);
                    bloomScale = (float) settings.getDouble("bloomScale");
                    bloomIntensity = (float) settings.getDouble("bloomIntensity");
                }
                enableNormal = settings.getBoolean("enableNormal");
                specularIntensity = (float) settings.getDouble("specularIntensity");
                lightDepth = (float) settings.getDouble("lightDepth");
                weaponFlashHeight = (float) settings.getDouble("weaponFlashHeight");
                weaponLightHeight = (float) settings.getDouble("weaponLightHeight");
                if (enableNormal) {
                    autoGenNormals = settings.getBoolean("autoGenNormals");
                    optimizeNormals = settings.getBoolean("optimizeNormals");
                    loadSurface = settings.getBoolean("loadSurface");
                    specularHardness = (float) settings.getDouble("specularHardness");
                    normalFlatness = (float) settings.getDouble("normalFlatness");
                }
            }

            enableDistortion = settings.getBoolean("enableDistortion");
            if (enableDistortion) {
                useLargeRipple = settings.getBoolean("useLargeRipple");
                maximumDistortions = settings.getInt("maximumDistortions");
            } else {
                useLargeRipple = false;
            }

            enablePostProcess = settings.getBoolean("enablePostProcess");
            if (enablePostProcess) {
                colorBlindnessMode = settings.getInt("colorBlindnessMode");
            }
        }

        drawOffscreenParticles = settings.getBoolean("drawOffscreenParticles");
        enableShieldRipples = settings.getBoolean("enableShieldRipples");
        enableMjolnirRipples = settings.getBoolean("enableMjolnirRipples");
        enableExplosionEffects = settings.getBoolean("enableExplosionEffects");
        if (enableExplosionEffects) {
            enableFullExplosionEffects = settings.getBoolean("enableFullExplosionEffects");
        } else {
            enableFullExplosionEffects = false;
        }
        enableExplosionShockwave = settings.getBoolean("enableExplosionShockwave");
        enableExplosionTrails = settings.getBoolean("enableExplosionTrails");
        if (enableExplosionTrails) {
            explosionTrailScale = (float) settings.getDouble("explosionTrailScale");
            if (explosionTrailScale <= 0.01f) {
                enableExplosionTrails = false;
            }
        }
        enableOverloadArcs = settings.getBoolean("enableOverloadArcs");
        if (enableOverloadArcs) {
            useVentColorsForOverloadArcs = settings.getBoolean("useVentColorsForOverloadArcs");
        }
        enableWeaponSmoke = settings.getBoolean("enableWeaponSmoke");
        enableInsignias = settings.getBoolean("enableInsignias");
        enableSunLight = settings.getBoolean("enableSunLight");
        enableHyperLight = settings.getBoolean("enableHyperLight");

        if (useLunaLib) {
            if (!LunaSettings.hasSettingsListenerOfClass(GraphicsLibSettingsListener.class)) {
                LunaSettings.addSettingsListener(new GraphicsLibSettingsListener());
            }

            enableShaders = enableShaders() && ShaderLib.areShadersAllowed();
            use64BitBuffer = use64BitBuffer();
            debugLevel = debugLevel();
            enableLights = enableLights();
            preloadAllMaps = preloadAllMaps();
            loadMaterial = loadMaterial();
            enableBloom = enableBloom();
            bloomQuality = bloomQuality();
            bloomMips = bloomMips();
            bloomScale = bloomScale();
            bloomIntensity = bloomIntensity();
            enableNormal = enableNormal();
            autoGenNormals = autoGenNormals();
            loadSurface = loadSurface();
            specularIntensity = specularIntensity();
            specularHardness = specularHardness();
            enableDistortion = enableDistortion();
            useLargeRipple = useLargeRipple();
            useSmallRipple = useSmallRipple();
            enablePostProcess = enablePostProcess();
            colorBlindnessMode = colorBlindnessMode();
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public static void reload() {
        try {
            GraphicsLibSettings.load();
        } catch (Exception e) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Failed to load shader settings: " + e.getMessage());
        }

        applyChanges();

        boolean inCombat = false;
        if ((Global.getCurrentState() == GameState.COMBAT) || (Global.getCurrentState() == GameState.TITLE)) {
            inCombat = Global.getCombatEngine() != null;
        }
        if (!inCombat) {
            return;
        }

        final List<ShaderAPI> shaders = ShaderLib.getShaderAPIs();
        final List<ShaderAPI> newShaders = new ArrayList<>(shaders.size());

        for (ShaderAPI shader : shaders) {
            shader.destroy();
            try {
                @SuppressWarnings("deprecation")
                final ShaderAPI newShader = (ShaderAPI) Global.getSettings().getScriptClassLoader().loadClass(shader.getClass().getName()).newInstance();
                newShaders.add(newShader);
                newShader.initCombat();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Reload Error! " + e);
            }
        }

        shaders.clear();
        for (ShaderAPI shader : newShaders) {
            shaders.add(shader);
        }
    }

    public static void applyChanges() {
        boolean inCombat = false;
        if ((Global.getCurrentState() == GameState.COMBAT) || (Global.getCurrentState() == GameState.TITLE)) {
            inCombat = Global.getCombatEngine() != null;
        }

        final boolean enableShadersPrev = enableShaders;
        final boolean use64BitBufferPrev = use64BitBuffer;
        final int debugLevelPrev = debugLevel;
        final boolean enableLightsPrev = enableLights;
        final boolean preloadAllMapsPrev = preloadAllMaps;
        final boolean loadMaterialPrev = loadMaterial;
        final boolean enableBloomPrev = enableBloom;
        final int bloomQualityPrev = bloomQuality;
        final int bloomMipsPrev = bloomMips;
        final float bloomScalePrev = bloomScale;
        final float bloomIntensityPrev = bloomIntensity;
        final boolean enableNormalPrev = enableNormal;
        final boolean autoGenNormalsPrev = autoGenNormals;
        final boolean loadSurfacePrev = loadSurface;
        final float specularIntensityPrev = specularIntensity;
        final float specularHardnessPrev = specularHardness;
        final boolean enableDistortionPrev = enableDistortion;
        final boolean useLargeRipplePrev = useLargeRipple;
        final boolean useSmallRipplePrev = useSmallRipple;
        final boolean enablePostProcessPrev = enablePostProcess;
        final int colorBlindnessModePrev = colorBlindnessMode;

        enableShaders = enableShaders() && ShaderLib.areShadersAllowed();
        use64BitBuffer = use64BitBuffer();
        debugLevel = debugLevel();
        enableLights = enableLights();
        preloadAllMaps = preloadAllMaps();
        loadMaterial = loadMaterial();
        enableBloom = enableBloom();
        bloomQuality = bloomQuality();
        bloomMips = bloomMips();
        bloomScale = bloomScale();
        bloomIntensity = bloomIntensity();
        enableNormal = enableNormal();
        autoGenNormals = autoGenNormals();
        loadSurface = loadSurface();
        specularIntensity = specularIntensity();
        specularHardness = specularHardness();
        enableDistortion = enableDistortion();
        useLargeRipple = useLargeRipple();
        useSmallRipple = useSmallRipple();
        enablePostProcess = enablePostProcess();
        colorBlindnessMode = colorBlindnessMode();

        final List<ShaderAPI> shaders = ShaderLib.getShaderAPIs();
        final List<Class> newShaders = new ArrayList<>(shaders.size());
        final boolean reloadCoreData = enableShaders && (use64BitBufferPrev != use64BitBuffer);
        if ((enableShadersPrev != enableShaders) || (use64BitBufferPrev != use64BitBuffer)) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading all shaders");
            for (ShaderAPI shader : shaders) {
                shader.destroy();
                newShaders.add(shader.getClass());
            }
            shaders.clear();
        }
        if ((enableShadersPrev && !enableShaders) || reloadCoreData) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading core data");
            ShaderLib.unloadCoreData();
        }
        if ((!enableShadersPrev && enableShaders) || reloadCoreData) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading core data");
            ShaderLib.loadCoreData();
        }
        if ((enableShadersPrev != enableShaders) || (use64BitBufferPrev != use64BitBuffer)) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading all shaders");
            for (Class shader : newShaders) {
                try {
                    @SuppressWarnings("deprecation")
                    final ShaderAPI newShader = (ShaderAPI) Global.getSettings().getScriptClassLoader().loadClass(shader.getName()).newInstance();
                    shaders.add(newShader);
                    if (inCombat) {
                        newShader.initCombat();
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Error when changing settings! " + e);
                }
            }
        }

        if ((debugLevelPrev > 0) && (debugLevel == 0)) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Disabling debug log");
            ShaderLib.disableDebugLog();
        } else if ((debugLevelPrev == 0) && (debugLevel > 0)) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Enabling debug log");
            ShaderLib.enableDebugLog();
        }

        final boolean reloadLights = enableLights && ((enableNormalPrev != enableNormal) || (enableBloomPrev != enableBloom)
                || (bloomQualityPrev != bloomQuality) || (bloomMipsPrev != bloomMips) || (bloomScalePrev != bloomScale) || (bloomIntensityPrev != bloomIntensity)
                || (specularIntensityPrev != specularIntensity) || (specularHardnessPrev != specularHardness));
        final boolean refreshTextures = (enableLights != enableLightsPrev) || (preloadAllMaps != preloadAllMapsPrev) || (loadMaterial != loadMaterialPrev)
                || (enableNormalPrev != enableNormal) || (autoGenNormals != autoGenNormalsPrev) || (loadSurface != loadSurfacePrev);
        if ((enableLightsPrev && !enableLights) || reloadLights) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading lights shader and Threat map textures");
            final ShaderAPI shader = ShaderLib.getShaderAPI(LightShader.class);
            if (shader != null) {
                shader.destroy();
                ShaderLib.getShaderAPIs().remove(shader);
            }
            unloadThreat();
        }
        if ((!enableLightsPrev && enableLights) || reloadLights) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading lights shader and Threat map textures");
            final LightShader shader = new LightShader();
            ShaderLib.addShaderAPI(shader);
            if (inCombat) {
                shader.initCombat();
            }
            try {
                loadThreat();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (refreshTextures) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Refreshing map textures");
            if (enableLights) {
                TextureData.autoGenMissingNormalMaps();
            }
            TextureData.unloadAndPreloadTextures(null);
        }

        final boolean reloadDistortion = enableDistortion && ((useLargeRipplePrev && !useLargeRipple) || (useSmallRipplePrev && !useSmallRipple));
        if ((enableDistortionPrev && !enableDistortion) || reloadDistortion) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading distortion shader");
            final ShaderAPI shader = ShaderLib.getShaderAPI(DistortionShader.class);
            if (shader != null) {
                shader.destroy();
                ShaderLib.getShaderAPIs().remove(shader);
            }
            if (!reloadDistortion) {
                Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading wave texture");
                unloadWave();
            }
        }
        if (useLargeRipplePrev && !useLargeRipple) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading large ripple textures");
            unloadLargeRipple();
        } else if (!useLargeRipplePrev && useLargeRipple) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading large ripple textures");
            try {
                loadLargeRipple();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (useSmallRipplePrev && !useSmallRipple) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading small ripple textures");
            unloadSmallRipple();
        } else if (!useSmallRipplePrev && useSmallRipple) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading small ripple textures");
            try {
                loadSmallRipple();
            } catch (IOException e) {
                // Ignore
            }
        }
        if ((!enableDistortionPrev && enableDistortion) || reloadDistortion) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading distortion shader");
            final DistortionShader shader = new DistortionShader();
            ShaderLib.addShaderAPI(shader);
            if (inCombat) {
                shader.initCombat();
            }
            if (!reloadDistortion) {
                Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading wave texture");
                try {
                    loadWave();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        final boolean reloadPostProcess = enablePostProcess && (colorBlindnessModePrev != colorBlindnessMode);
        if ((enablePostProcessPrev && !enablePostProcess) || reloadPostProcess) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Unloading post-processing shader");
            final ShaderAPI shader = ShaderLib.getShaderAPI(PostProcessShader.class);
            if (shader != null) {
                shader.destroy();
                ShaderLib.getShaderAPIs().remove(shader);
            }
        }
        if ((!enablePostProcessPrev && enablePostProcess) || reloadPostProcess) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.INFO, "Loading post-processing shader");
            final PostProcessShader shader = new PostProcessShader();
            ShaderLib.addShaderAPI(shader);
            if (inCombat) {
                shader.initCombat();
            }
        }
    }

    public static boolean enableShaders() {
        if (useLunaLib) {
            return LunaSettings.getBoolean("shaderLib", "enableShaders");
        }
        return enableShaders;
    }

    public static boolean aaCompatMode() {
        if (useLunaLib) {
            return aaCompatMode = LunaSettings.getBoolean("shaderLib", "aaCompatMode");
        }
        return aaCompatMode;
    }

    public static boolean use64BitBuffer() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "use64BitBuffer") && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
        }
        return (use64BitBuffer && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
    }

    public static boolean extraScreenClear() {
        if (useLunaLib) {
            return extraScreenClear = (LunaSettings.getBoolean("shaderLib", "extraScreenClear") && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
        }
        return (extraScreenClear && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
    }

    public static int debugLevel() {
        if (!enableShaders()) {
            return 0;
        }
        if (useLunaLib) {
            final String radioStr = LunaSettings.getString("shaderLib", "debugLevel");
            if (radioStr != null) {
                switch (radioStr) {
                    default:
                    case "Disabled":
                        return 0;
                    case "Log w/o Stack Trace":
                        return 1;
                    case "Log Unique Stack Traces":
                        return 2;
                    case "Log All Stack Traces":
                        return 3;
                }
            }
            return 0;
        }
        return debugLevel;
    }

    public static int toggleKey() {
        if (useLunaLib) {
            return toggleKey = LunaSettings.getInt("shaderLib", "toggleKey");
        }
        return toggleKey;
    }

    public static int reloadKey() {
        if (useLunaLib) {
            return reloadKey = LunaSettings.getInt("shaderLib", "reloadKey");
        }
        return reloadKey;
    }

    public static boolean enableLights() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "enableLights") && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
        }
        return (enableLights && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
    }

    public static boolean preloadAllMaps() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "preloadAllMaps") && enableLights());
        }
        return (preloadAllMaps && enableLights());
    }

    public static boolean loadMaterial() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "loadMaterial") && enableLights());
        }
        return (loadMaterial && enableLights());
    }

    public static int maximumLights() {
        if (useLunaLib) {
            return maximumLights = LunaSettings.getInt("shaderLib", "maximumLights");
        }
        return maximumLights;
    }

    public static int maximumLineLights() {
        if (useLunaLib) {
            return maximumLineLights = LunaSettings.getInt("shaderLib", "maximumLineLights");
        }
        return maximumLineLights;
    }

    public static float intensityScale() {
        if (useLunaLib) {
            return intensityScale = (LunaSettings.getFloat("shaderLib", "intensityScale") / 100f);
        }
        return intensityScale;
    }

    public static float sizeScale() {
        if (useLunaLib) {
            return sizeScale = (LunaSettings.getFloat("shaderLib", "sizeScale") / 100f);
        }
        return sizeScale;
    }

    public static float fighterBrightnessScale() {
        if (useLunaLib) {
            return fighterBrightnessScale = (LunaSettings.getFloat("shaderLib", "fighterBrightnessScale") / 100f);
        }
        return fighterBrightnessScale;
    }

    public static float defaultMaterialBrightness() {
        if (useLunaLib) {
            return defaultMaterialBrightness = (LunaSettings.getFloat("shaderLib", "defaultMaterialBrightness") / 100f);
        }
        return defaultMaterialBrightness;
    }

    public static boolean enableBloom() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "enableBloom") && enableLights());
        }
        return (enableBloom && enableLights());
    }

    public static int bloomQuality() {
        if (useLunaLib) {
            return LunaSettings.getInt("shaderLib", "bloomQuality");
        }
        return bloomQuality;
    }

    public static int bloomMips() {
        if (useLunaLib) {
            return LunaSettings.getInt("shaderLib", "bloomMips");
        }
        return bloomMips;
    }

    public static float bloomScale() {
        if (useLunaLib) {
            return (LunaSettings.getFloat("shaderLib", "bloomScale") / 100f);
        }
        return bloomScale;
    }

    public static float bloomIntensity() {
        if (useLunaLib) {
            return (LunaSettings.getFloat("shaderLib", "bloomIntensity") / 100f);
        }
        return bloomIntensity;
    }

    public static boolean enableNormal() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "enableNormal") && enableLights());
        }
        return (enableNormal && enableLights());
    }

    public static boolean autoGenNormals() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "autoGenNormals") && enableNormal());
        }
        return (autoGenNormals && enableNormal());
    }

    public static boolean optimizeNormals() {
        if (useLunaLib) {
            return optimizeNormals = LunaSettings.getBoolean("shaderLib", "optimizeNormals");
        }
        return optimizeNormals;
    }

    public static boolean loadSurface() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "loadSurface") && enableNormal());
        }
        return (loadSurface && enableNormal());
    }

    public static float specularIntensity() {
        if (useLunaLib) {
            return (LunaSettings.getFloat("shaderLib", "specularIntensity") / 100f);
        }
        return specularIntensity;
    }

    public static float specularHardness() {
        if (useLunaLib) {
            return (LunaSettings.getFloat("shaderLib", "specularHardness") / 100f);
        }
        return specularHardness;
    }

    public static float normalFlatness() {
        if (useLunaLib) {
            return normalFlatness = LunaSettings.getFloat("shaderLib", "normalFlatness");
        }
        return normalFlatness;
    }

    public static float lightDepth() {
        if (useLunaLib) {
            return lightDepth = (LunaSettings.getFloat("shaderLib", "lightDepth") / 100f);
        }
        return lightDepth;
    }

    public static float weaponFlashHeight() {
        if (useLunaLib) {
            return weaponFlashHeight = LunaSettings.getFloat("shaderLib", "weaponFlashHeight");
        }
        return weaponFlashHeight;
    }

    public static float weaponLightHeight() {
        if (useLunaLib) {
            return weaponLightHeight = LunaSettings.getFloat("shaderLib", "weaponLightHeight");
        }
        return weaponLightHeight;
    }

    public static boolean enableDistortion() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "enableDistortion") && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
        }
        return (enableDistortion && enableShaders() && ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed());
    }

    public static boolean useLargeRipple() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "useLargeRipple") && enableDistortion());
        }
        return (useLargeRipple && enableDistortion());
    }

    public static boolean useSmallRipple() {
        return (enableDistortion() && !useLargeRipple());
    }

    public static int maximumDistortions() {
        if (useLunaLib) {
            return maximumDistortions = LunaSettings.getInt("shaderLib", "maximumDistortions");
        }
        return maximumDistortions;
    }

    public static boolean enablePostProcess() {
        if (useLunaLib) {
            return (LunaSettings.getBoolean("shaderLib", "enablePostProcess") && enableShaders() && ShaderLib.areShadersAllowed());
        }
        return (enablePostProcess && enableShaders() && ShaderLib.areShadersAllowed());
    }

    public static int colorBlindnessMode() {
        if (!enableShaders()) {
            return 0;
        }
        if (useLunaLib) {
            final String radioStr = LunaSettings.getString("shaderLib", "colorBlindnessMode");
            if (radioStr != null) {
                switch (radioStr) {
                    default:
                    case "None":
                        return 0;
                    case "Protanomaly":
                        return 1;
                    case "Deuteranomaly":
                        return 2;
                    case "Tritanomaly":
                        return 3;
                    case "Protanopia":
                        return 4;
                    case "Deuteranopia":
                        return 5;
                    case "Tritanopia":
                        return 6;
                    case "Achromatopsia":
                        return 7;
                }
            }
            return 0;
        }
        return colorBlindnessMode;
    }

    public static boolean drawOffscreenParticles() {
        if (useLunaLib) {
            return drawOffscreenParticles = LunaSettings.getBoolean("shaderLib", "drawOffscreenParticles");
        }
        return drawOffscreenParticles;
    }

    public static boolean enableShieldRipples() {
        if (useLunaLib) {
            return enableShieldRipples = LunaSettings.getBoolean("shaderLib", "enableShieldRipples");
        }
        return enableShieldRipples;
    }

    public static boolean enableMjolnirRipples() {
        if (useLunaLib) {
            return enableMjolnirRipples = LunaSettings.getBoolean("shaderLib", "enableMjolnirRipples");
        }
        return enableMjolnirRipples;
    }

    public static boolean enableExplosionEffects() {
        if (useLunaLib) {
            final String radioStr = LunaSettings.getString("shaderLib", "enableExplosionEffects");
            if (radioStr != null) {
                switch (radioStr) {
                    default:
                    case "Disabled":
                        return enableExplosionEffects = false;
                    case "Enabled":
                    case "Extreme":
                        return enableExplosionEffects = true;
                }
            }
            return enableExplosionEffects = false;
        }
        return enableExplosionEffects;
    }

    public static boolean enableFullExplosionEffects() {
        if (useLunaLib) {
            final String radioStr = LunaSettings.getString("shaderLib", "enableFullExplosionEffects");
            if (radioStr != null) {
                switch (radioStr) {
                    default:
                    case "Disabled":
                    case "Enabled":
                        return enableFullExplosionEffects = false;
                    case "Extreme":
                        return enableFullExplosionEffects = true;
                }
            }
            return enableFullExplosionEffects = false;
        }
        return (enableFullExplosionEffects && enableExplosionEffects());
    }

    public static boolean enableExplosionShockwave() {
        if (useLunaLib) {
            return enableExplosionShockwave = LunaSettings.getBoolean("shaderLib", "enableExplosionShockwave");
        }
        return enableExplosionShockwave;
    }

    public static boolean enableExplosionTrails() {
        if (useLunaLib) {
            return enableExplosionTrails = ((LunaSettings.getFloat("shaderLib", "explosionTrailScale") / 100f) >= 0.01f);
        }
        return (enableExplosionTrails && (explosionTrailScale >= 0.01f));
    }

    public static float explosionTrailScale() {
        if (useLunaLib) {
            return explosionTrailScale = (LunaSettings.getFloat("shaderLib", "explosionTrailScale") / 100f);
        }
        return explosionTrailScale;
    }

    public static boolean enableOverloadArcs() {
        if (useLunaLib) {
            return enableOverloadArcs = LunaSettings.getBoolean("shaderLib", "enableOverloadArcs");
        }
        return enableOverloadArcs;
    }

    public static boolean useVentColorsForOverloadArcs() {
        if (useLunaLib) {
            return useVentColorsForOverloadArcs = LunaSettings.getBoolean("shaderLib", "useVentColorsForOverloadArcs");
        }
        return useVentColorsForOverloadArcs;
    }

    public static boolean enableWeaponSmoke() {
        if (useLunaLib) {
            return enableWeaponSmoke = LunaSettings.getBoolean("shaderLib", "enableWeaponSmoke");
        }
        return enableWeaponSmoke;
    }

    public static boolean enableInsignias() {
        if (useLunaLib) {
            return enableInsignias = LunaSettings.getBoolean("shaderLib", "enableInsignias");
        }
        return enableInsignias;
    }

    public static boolean enableSunLight() {
        if (useLunaLib) {
            return enableSunLight = LunaSettings.getBoolean("shaderLib", "enableSunLight");
        }
        return enableSunLight;
    }

    public static boolean enableHyperLight() {
        if (useLunaLib) {
            return enableHyperLight = LunaSettings.getBoolean("shaderLib", "enableHyperLight");
        }
        return enableHyperLight;
    }

    public static void loadThreat() throws IOException {
        if (TextureData.isLoadMaterial()) {
            try {
                Global.getSettings().forceMipmapsFor(ThreatMapObject.THREAT_MATERIAL_PATH, true);
                Global.getSettings().loadTexture(ThreatMapObject.THREAT_MATERIAL_PATH);
            } catch (IOException e) {
                Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Texture loading failed at " + ThreatMapObject.THREAT_MATERIAL_PATH + "! " + e.getMessage());
                throw e;
            }
        }
        if (TextureData.isLoadNormal()) {
            try {
                Global.getSettings().forceMipmapsFor(ThreatMapObject.THREAT_NORMAL_PATH, true);
                Global.getSettings().loadTexture(ThreatMapObject.THREAT_NORMAL_PATH);
            } catch (IOException e) {
                Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Texture loading failed at " + ThreatMapObject.THREAT_NORMAL_PATH + "! " + e.getMessage());
                throw e;
            }
        }
        if (TextureData.isLoadSurface()) {
            try {
                Global.getSettings().forceMipmapsFor(ThreatMapObject.THREAT_SURFACE_PATH, true);
                Global.getSettings().loadTexture(ThreatMapObject.THREAT_SURFACE_PATH);
            } catch (IOException e) {
                Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Texture loading failed at " + ThreatMapObject.THREAT_SURFACE_PATH + "! " + e.getMessage());
                throw e;
            }
        }
    }

    public static void unloadThreat() {
        final SpriteAPI material = Global.getSettings().getSprite(ThreatMapObject.THREAT_MATERIAL_PATH);
        if (material != null) {
            final int texId = material.getTextureId();
            Global.getSettings().unloadTexture(ThreatMapObject.THREAT_MATERIAL_PATH);
            GL11.glDeleteTextures(texId);
        }
        final SpriteAPI normal = Global.getSettings().getSprite(ThreatMapObject.THREAT_NORMAL_PATH);
        if (normal != null) {
            final int texId = normal.getTextureId();
            Global.getSettings().unloadTexture(ThreatMapObject.THREAT_NORMAL_PATH);
            GL11.glDeleteTextures(texId);
        }
        final SpriteAPI surface = Global.getSettings().getSprite(ThreatMapObject.THREAT_SURFACE_PATH);
        if (surface != null) {
            final int texId = surface.getTextureId();
            Global.getSettings().unloadTexture(ThreatMapObject.THREAT_SURFACE_PATH);
            GL11.glDeleteTextures(texId);
        }
    }

    public static void loadWave() throws IOException {
        try {
            Global.getSettings().forceMipmapsFor(WAVE_PATH, true);
            Global.getSettings().loadTexture(WAVE_PATH);
        } catch (IOException e) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Texture loading failed at " + WAVE_PATH + "! " + e.getMessage());
            throw e;
        }
    }

    public static void unloadWave() {
        final SpriteAPI sprite = Global.getSettings().getSprite(WAVE_PATH);
        if (sprite != null) {
            final int texId = sprite.getTextureId();
            Global.getSettings().unloadTexture(WAVE_PATH);
            GL11.glDeleteTextures(texId);
        }
    }

    public static void loadLargeRipple() throws IOException {
        String path = "";
        try {
            for (int i = 1; i <= 60; i++) {
                if (i < 10) {
                    path = LARGE_RIPPLE_PATH_START + "000" + i + ".PNG";
                } else {
                    path = LARGE_RIPPLE_PATH_START + "00" + i + ".PNG";
                }
                Global.getSettings().forceMipmapsFor(path, true);
                Global.getSettings().loadTexture(path);
            }
        } catch (IOException e) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Texture loading failed at " + path + "! " + e.getMessage());
            throw e;
        }
    }

    public static void unloadLargeRipple() {
        for (int i = 1; i <= 60; i++) {
            final String path;
            if (i < 10) {
                path = LARGE_RIPPLE_PATH_START + "000" + i + ".PNG";
            } else {
                path = LARGE_RIPPLE_PATH_START + "00" + i + ".PNG";
            }
            final SpriteAPI sprite = Global.getSettings().getSprite(path);
            if (sprite != null) {
                final int texId = sprite.getTextureId();
                Global.getSettings().unloadTexture(path);
                GL11.glDeleteTextures(texId);
            }
        }
    }

    public static void loadSmallRipple() throws IOException {
        String path = "";
        try {
            for (int i = 1; i <= 60; i++) {
                if (i < 10) {
                    path = SMALL_RIPPLE_PATH_START + "000" + i + ".PNG";
                } else {
                    path = SMALL_RIPPLE_PATH_START + "00" + i + ".PNG";
                }
                Global.getSettings().forceMipmapsFor(path, true);
                Global.getSettings().loadTexture(path);
            }
        } catch (IOException e) {
            Global.getLogger(GraphicsLibSettings.class).log(Level.ERROR, "Texture loading failed at " + path + "! " + e.getMessage());
            throw e;
        }
    }

    public static void unloadSmallRipple() {
        for (int i = 1; i <= 60; i++) {
            final String path;
            if (i < 10) {
                path = SMALL_RIPPLE_PATH_START + "000" + i + ".PNG";
            } else {
                path = SMALL_RIPPLE_PATH_START + "00" + i + ".PNG";
            }
            final SpriteAPI sprite = Global.getSettings().getSprite(path);
            if (sprite != null) {
                final int texId = sprite.getTextureId();
                Global.getSettings().unloadTexture(path);
                GL11.glDeleteTextures(texId);
            }
        }
    }
}
