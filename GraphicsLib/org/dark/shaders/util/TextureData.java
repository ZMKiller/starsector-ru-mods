package org.dark.shaders.util;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.WeaponSkinType;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Level;
import org.dark.shaders.ShaderModPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * A class for reading csv data to the program so that textures such as material maps and normal maps can be handled.
 * <p>
 * @author DarkRevenant
 * <p>
 * @since Alpha 1.5
 */
@SuppressWarnings("UseSpecificCatch")
public class TextureData {

    public static final String AUTOGEN_OVERRIDE_TAG = "graphicslib_autogen_override";
    public static final String NO_AUTOGEN_TAG = "graphicslib_no_autogen";
    public static final String ALWAYS_PRELOAD_TAG = "graphicslib_preload";

    private static final String CACHE_HASH_FILE = "shaderlib_cache_hash";
    private static final String CACHE_DIR = "cache";

    public static boolean CHECK_INEFFICIENT = false;

    private static boolean invalidateCache = true;
    private static boolean everTraversedSpecs = false;
    private static boolean everPerformedAutoGen = false;

    private static final Map<String, TextureEntry> materialKeyToEntry = new LinkedHashMap<>(1000);
    private static final Map<String, TextureEntry> materialSpriteNameToEntry = new HashMap<>(1000);
    private static final Map<String, TextureEntry> normalKeyToEntry = new LinkedHashMap<>(1000);
    private static final Map<String, TextureEntry> normalSpriteNameToEntry = new HashMap<>(1000);
    private static final Map<String, TextureEntry> surfaceKeyToEntry = new LinkedHashMap<>(1000);
    private static final Map<String, TextureEntry> surfaceSpriteNameToEntry = new HashMap<>(1000);
    private static final Set<String> mnsSpritePathSet = new HashSet<>(1000);
    private static final Map<String, String> baseHullIdToHullStyle = new HashMap<>(1000);
    private static final Map<String, Integer> weaponIdToAnimFrames = new HashMap<>(1000);
    private static final Set<String> allHullStyles = new LinkedHashSet<>();

    private static int wastedBytesTotal = 0;
    private static final Set<String> consideredSprites = new HashSet<>(1000);

    private static long vramUsageBytes = 0; // An estimate

    private static final double[] BLUR_KERNEL = new double[]{1.0, 1.0 / 4.0, 1.0 / 9.0, 1.0 / 16.0, 1.0 / 25.0, 1.0 / 36.0, 1.0 / 49.0, 1.0 / 64.0,
        1.0 / 81.0, 1.0 / 100.0, 1.0 / 121.0, 1.0 / 144.0, 1.0 / 169.0, 1.0 / 196.0, 1.0 / 225.0, 1.0 / 256.0};

    static {
        Global.getLogger(TextureData.class).setLevel(Level.DEBUG);

        double kernelSum = BLUR_KERNEL[0];
        for (int i = 1; i < BLUR_KERNEL.length; i++) {
            kernelSum += BLUR_KERNEL[i] * 2.0;
        }
        for (int i = 0; i < BLUR_KERNEL.length; i++) {
            BLUR_KERNEL[i] /= kernelSum;
        }
    }

    /**
     * Gets the requested texture map data. This is guaranteed to never return an auto-generated normal map; use
     * getTextureDataWithAutoGen if you want that. Will load a texture into the game if needed.
     * <p>
     * @param key The ID of the desired material/normal/surface map texture.
     * @param map Whether the desired texture is a material, normal, or surface map.
     * @param type What kind of object the texture is intended for.
     * @param frame The frame of the animation, for an animated weapon. Use 0 for non-animated weapons and ships.
     * <p>
     * @return The requested data. Null if no such TextureData entry exists.
     * <p>
     * @since Alpha 1.5
     */
    public static TextureEntry getTextureData(String key, TextureDataType map, ObjectType type, int frame) {
        return getTextureDataWithAutoGen(key, map, type, frame, null, false);
    }

    /**
     * Gets the requested texture map data, with the possibility of auto-generation of normal maps.
     * <p>
     * This function can be used to procedurally preload specific texture maps at the start of combat. In an
     * EveryFrameScript or similar type of plugin, wait for ShaderHook.TEXTURE_PRELOAD_KEY to be set and then call this
     * function for every map that should be preloaded.
     * <p>
     * @param key The ID of the desired material/normal/surface map texture.
     * @param map Whether the desired texture is a material, normal, or surface map.
     * @param type What kind of object the texture is intended for.
     * @param frame The frame of the animation, for an animated weapon. Use 0 for non-animated weapons and ships.
     * Ignored if type isn't TURRET or HARDPOINT.
     * @param object The associated object that the texture is for. Should be ShipAPI for a ship or cover, WeaponAPI for
     * a weapon, WeaponAPI or MissileAPI for a missile, CombatEntityAPI for an asteroid. If null, shall not
     * auto-generate and shall not retrieve an entry that was auto-generated. If you don't have the right type of object
     * for some reason, any non-null object will permit retrieval (but not generation).
     * @param unloadedOk Return unloaded TextureEntries, if necessary. Almost always should be false.
     * <p>
     * @return The requested data. Null if no such TextureData entry exists and nothing could be auto-generated.
     * <p>
     * @since 1.11.0
     */
    public static TextureEntry getTextureDataWithAutoGen(String key, TextureDataType map, ObjectType type, int frame, Object object, boolean unloadedOk) {
        if (key == null) {
            return null;
        }

        final String dataKey = getTextureDataKey(key, type, frame);
        if (dataKey == null) {
            return null;
        }

        TextureEntry entry;
        if ((map == TextureDataType.MATERIAL_MAP) && isLoadMaterial()) {
            entry = materialKeyToEntry.get(dataKey);
        } else if ((map == TextureDataType.NORMAL_MAP) && isLoadNormal()) {
            TextureEntry normalEntry = normalKeyToEntry.get(dataKey);
            if (normalEntry == null) {
                if ((object != null) && GraphicsLibSettings.autoGenNormals()) {
                    String spriteName = null;
                    boolean autoGen = true;
                    boolean autoGenOverride = false;
                    switch (type) {
                        case SHIP:
                            if (object instanceof ShipAPI ship) {
                                autoGenOverride = ship.hasTag(AUTOGEN_OVERRIDE_TAG);
                                autoGen = !ship.hasTag(NO_AUTOGEN_TAG);
                                final ShipHullSpecAPI hullSpec = ship.getHullSpec();
                                if (hullSpec != null) {
                                    spriteName = hullSpec.getSpriteName();
                                    autoGenOverride |= hullSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
                                    autoGen &= !hullSpec.hasTag(NO_AUTOGEN_TAG);
                                    if (ship.isFighter()) {
                                        final FighterWingAPI wing = ship.getWing();
                                        if (wing != null) {
                                            FighterWingSpecAPI wingSpec = wing.getSpec();
                                            if (wingSpec != null) {
                                                autoGenOverride |= wingSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
                                                autoGen &= !wingSpec.hasTag(NO_AUTOGEN_TAG);
                                            }
                                            final ShipAPI sourceShip = wing.getSourceShip();
                                            if (sourceShip == null) {
                                                String skinSpriteName = getFighterSkinSpriteName(ship, sourceShip);
                                                if ((skinSpriteName != null) && !skinSpriteName.isEmpty()) {
                                                    spriteName = skinSpriteName;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case TURRET:
                        case TURRET_UNDER:
                        case HARDPOINT:
                        case HARDPOINT_UNDER:
                            if (object instanceof WeaponAPI weapon) {
                                final WeaponSpecAPI weaponSpec = weapon.getSpec();
                                if (weaponSpec != null) {
                                    spriteName = switch (type) {
                                        case TURRET ->
                                            weaponSpec.getTurretSpriteName();
                                        case TURRET_UNDER ->
                                            weaponSpec.getTurretUnderSpriteName();
                                        case HARDPOINT ->
                                            weaponSpec.getHardpointSpriteName();
                                        case HARDPOINT_UNDER ->
                                            weaponSpec.getHardpointUnderSpriteName();
                                        default ->
                                            null;
                                    };
                                    autoGenOverride = weaponSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
                                    autoGen = !weaponSpec.hasTag(NO_AUTOGEN_TAG);
                                    final ShipAPI ship = weapon.getShip();
                                    if (ship != null) {
                                        final WeaponSkinType skinType = switch (type) {
                                            case TURRET ->
                                                WeaponSkinType.TURRET;
                                            case TURRET_UNDER ->
                                                WeaponSkinType.UNDER;
                                            case HARDPOINT ->
                                                WeaponSkinType.HARDPOINT;
                                            case HARDPOINT_UNDER ->
                                                WeaponSkinType.UNDER;
                                            default ->
                                                null;
                                        };
                                        if (skinType != null) {
                                            String skinSpriteName = getWeaponSkinSpriteName(ship, weapon.getId(), skinType);
                                            if ((skinSpriteName != null) && !skinSpriteName.isEmpty()) {
                                                spriteName = skinSpriteName;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case MISSILE:
                            if (object instanceof WeaponAPI weapon) {
                                final WeaponSpecAPI weaponSpec = weapon.getSpec();
                                if (weaponSpec != null) {
                                    autoGenOverride = weaponSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
                                    autoGen = !weaponSpec.hasTag(NO_AUTOGEN_TAG);
                                    if (weaponSpec.getProjectileSpec() instanceof MissileSpecAPI missileSpec) {
                                        if (!missileSpec.getTypeString().contentEquals("MOTE") && !missileSpec.getTypeString().startsWith("FLARE")) {
                                            ShipHullSpecAPI hullSpec = missileSpec.getHullSpec();
                                            if (hullSpec != null) {
                                                spriteName = hullSpec.getSpriteName();
                                            }
                                        }
                                    }
                                }
                            } else if (object instanceof MissileAPI missile) {
                                autoGenOverride = missile.hasTag(AUTOGEN_OVERRIDE_TAG);
                                autoGen = !missile.hasTag(NO_AUTOGEN_TAG);
                                final MissileSpecAPI missileSpec = missile.getSpec();
                                if (missileSpec != null) {
                                    if (!missileSpec.getTypeString().contentEquals("MOTE") && !missileSpec.getTypeString().startsWith("FLARE")) {
                                        ShipHullSpecAPI hullSpec = missileSpec.getHullSpec();
                                        if (hullSpec != null) {
                                            spriteName = hullSpec.getSpriteName();
                                        }
                                    }
                                }
                                final WeaponSpecAPI weaponSpec = missile.getWeaponSpec();
                                if (weaponSpec != null) {
                                    autoGenOverride |= weaponSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
                                    autoGen &= !weaponSpec.hasTag(NO_AUTOGEN_TAG);
                                }
                            }
                            break;
                        case TURRET_COVER_SMALL:
                        case TURRET_COVER_MEDIUM:
                        case TURRET_COVER_LARGE:
                        case HARDPOINT_COVER_SMALL:
                        case HARDPOINT_COVER_MEDIUM:
                        case HARDPOINT_COVER_LARGE:
                        case TURRET_BARREL:
                        case HARDPOINT_BARREL:
                        case ASTEROID:
                        default:
                            /* There's nothing we can do here that couldn't have been done at the start of the game,
                             * since it all depends on JSONs or manual definitions.
                             */
                            break;
                    }
                    mapSpriteToMNSWithAutoGen(key, spriteName, type, frame, autoGen, autoGenOverride);
                    normalEntry = normalKeyToEntry.get(dataKey);
                    if (normalEntry == null) {
                        /* If it's *still* null, give up and add a blank entry so that we don't repeat this expensive
                         * sprite-finding logic.
                         */
                        normalKeyToEntry.put(dataKey, new TextureEntry());
                    }
                }
            } else if (normalEntry.autoGen && (object == null)) {
                return null;
            }
            entry = normalEntry;
        } else if ((map == TextureDataType.SURFACE_MAP) && isLoadSurface()) {
            entry = surfaceKeyToEntry.get(dataKey);
        } else {
            entry = null;
        }

        if (entry == null) {
            return null;
        }

        /* Last-minute texture loader, if we didn't preload it before this point.  Terrible performance impact, so
         * let's try not to rely on this fallback too much...
         */
        if (!entry.loaded && entry.validToLoad && (entry.spriteName != null) && !entry.spriteName.isEmpty() && !unloadedOk) {
            /* Add mappings to the database if we haven't caught them yet */
            if ((type == ObjectType.TURRET_COVER_SMALL) || (type == ObjectType.TURRET_COVER_MEDIUM) || (type == ObjectType.TURRET_COVER_LARGE)
                    || (type == ObjectType.HARDPOINT_COVER_SMALL) || (type == ObjectType.HARDPOINT_COVER_MEDIUM) || (type == ObjectType.HARDPOINT_COVER_LARGE)) {
                if (object instanceof ShipAPI ship) {
                    final ShipHullSpecAPI hullSpec = ship.getHullSpec();
                    if (hullSpec != null) {
                        baseHullIdToHullStyle.put(hullSpec.getBaseHullId(), ship.getHullStyleId());
                    }
                }
            } else if ((type == ObjectType.TURRET) || (type == ObjectType.TURRET_UNDER) || (type == ObjectType.TURRET_BARREL)
                    || (type == ObjectType.HARDPOINT) || (type == ObjectType.HARDPOINT_UNDER) || (type == ObjectType.HARDPOINT_BARREL)) {
                if (object instanceof WeaponAPI weapon) {
                    Integer numFrames = weaponIdToAnimFrames.get(weapon.getId());
                    if (numFrames == null) {
                        numFrames = 0;
                    }
                    final AnimationAPI anim = weapon.getAnimation();
                    if (anim != null) {
                        numFrames = Math.max(numFrames, anim.getNumFrames());
                    }
                    weaponIdToAnimFrames.put(weapon.getId(), numFrames);
                }
            }

            try {
                Global.getSettings().forceMipmapsFor(entry.spriteName, true);
                Global.getSettings().loadTexture(entry.spriteName);
            } catch (IOException | RuntimeException e) {
                entry.validToLoad = false;
                return null;
            }

            entry.sprite = Global.getSettings().getSprite(entry.spriteName);
            if ((entry.sprite != null) && (entry.sprite.getHeight() >= 1)) {
                if (entry.vramSize == 0) {
                    entry.vramSize = Math.round((double) (16 * (Math.round(entry.sprite.getWidth() / entry.sprite.getTextureWidth())
                            * Math.round(entry.sprite.getHeight() / entry.sprite.getTextureHeight()))) / 3.0);
                }
                vramUsageBytes += entry.vramSize;
                entry.loaded = true;
            }
        }
        if ((entry.sprite == null) && !unloadedOk) {
            return null;
        }
        return entry;
    }

    private static String getFighterSkinSpriteName(ShipAPI fighter, ShipAPI carrier) {
        if (carrier.getHullStyleId().equals(fighter.getHullStyleId())) {
            return null;
        }

        String cat;
        String skin = null;
        if ((carrier.getOwner() == 0) || (carrier.getOriginalOwner() == 0)) {
            cat = "fighterSkinsPlayerOnly";
            skin = getFighterSkinSpriteName(cat, fighter, carrier);
        }
        if (skin != null) {
            return skin;
        }

        cat = "fighterSkinsPlayerAndNPC";
        skin = getFighterSkinSpriteName(cat, fighter, carrier);
        return skin;
    }

    private static String getFighterSkinSpriteName(String cat, ShipAPI fighter, ShipAPI carrier) {
        final String exclude = "fighterSkinsExcludeFromSharing";
        final String id = fighter.getHullSpec().getHullId();
        final String style = carrier.getHullStyleId();

        final List<String> skins = Global.getSettings().getSpriteKeys(cat);
        final Set<String> noSharing = new LinkedHashSet<>(Global.getSettings().getSpriteKeys(exclude));

        final List<String[]> matching = new ArrayList<>();
        for (String key : skins) {
            if (key.equals(id + "_" + style)) {
                return Global.getSettings().getSpriteName(cat, key);
            }
            if (key.startsWith(id) && !noSharing.contains(key)) {
                final String[] skin = {cat, key};
                matching.add(skin);
            }
        }

        if (!matching.isEmpty()) {
            String best = null;
            float minDist = Float.MAX_VALUE;

            for (String[] curr : matching) {
                final SpriteAPI sprite = Global.getSettings().getSprite(curr[0], curr[1]);
                final float dist = Misc.getColorDist(carrier.getSpriteAPI().getAverageBrightColor(), sprite.getAverageBrightColor());
                if (dist < minDist) {
                    best = Global.getSettings().getSpriteName(curr[0], curr[1]);
                    minDist = dist;
                }
            }
            return best;
        }

        return null;
    }

    private static String getWeaponSkinSpriteName(ShipAPI ship, String weaponId, WeaponSkinType type) {
        String cat;
        String skin = null;
        if ((ship.getOwner() == 0) || (ship.getOriginalOwner() == 0)) {
            cat = "weaponSkinsPlayerOnly";
            skin = getWeaponSkinSpriteName(cat, weaponId, ship, type);
        }
        if (skin != null) {
            return skin;
        }

        cat = "weaponSkinsPlayerAndNPC";
        skin = getWeaponSkinSpriteName(cat, weaponId, ship, type);
        return skin;
    }

    private static String getWeaponSkinSpriteName(String cat, String weaponId, ShipAPI ship, WeaponSkinType type) {
        final String exclude = "weaponSkinsExcludeFromSharing";
        final String style = ship.getHullStyleId();

        final List<String> skins = Global.getSettings().getSpriteKeys(cat);
        final Set<String> noSharing = new LinkedHashSet<>(Global.getSettings().getSpriteKeys(exclude));

        final List<String[]> matching = new ArrayList<>();
        final String keyForHull = weaponId + ":" + style + ":" + type.name();
        for (String key : skins) {
            if (key.equals(keyForHull)) {
                return Global.getSettings().getSpriteName(cat, key);
            }
            if (key.startsWith(weaponId) && !noSharing.contains(key)) {
                final String[] skin = {cat, key};
                matching.add(skin);
            }
        }

        if (!matching.isEmpty()) {
            String best = null;
            float minDist = Float.MAX_VALUE;

            for (String[] curr : matching) {
                final SpriteAPI sprite = Global.getSettings().getSprite(curr[0], curr[1]);
                float dist = Misc.getColorDist(ship.getSpriteAPI().getAverageBrightColor(), sprite.getAverageBrightColor());
                if (dist < minDist) {
                    best = Global.getSettings().getSpriteName(curr[0], curr[1]);
                    minDist = dist;
                }
            }
            return best;
        }

        return null;
    }

    /**
     * Loads a texture data CSV and makes that data available internally. Duplicate entries will replace previous data.
     * <p>
     * @param localPath The local path to the csv file (ex. "data/lights/core_texture_data.csv").
     * <p>
     * @since Alpha 1.5
     */
    public static void readTextureDataCSV(String localPath) {
        readTextureDataCSVInner(localPath, true);
    }

    /**
     * Loads a texture data CSV and makes that data available internally. Duplicate entries will NOT replace previous
     * data.
     * <p>
     * @param localPath The local path to the csv file (ex. "data/lights/core_texture_data.csv").
     * <p>
     * @since Alpha 1.5
     */
    public static void readTextureDataCSVNoOverwrite(String localPath) {
        readTextureDataCSVInner(localPath, false);
    }

    private static void readTextureDataCSVInner(String localPath, boolean overwrite) {
        ShaderLib.init();
        try {
            final JSONArray textureData = Global.getSettings().loadCSV(localPath);

            for (int i = 0; i < textureData.length(); i++) {
                if ((i % 10) == 0) {
                    ShaderModPlugin.refresh();
                }

                final JSONObject row = textureData.getJSONObject(i);
                final String id = row.optString("id");
                final String type = row.optString("type");
                final String map = row.optString("map");
                final String path = row.optString("path");
                if (!id.isEmpty() && !type.isEmpty() && !map.isEmpty() && !path.isEmpty()) {
                    boolean success = true;
                    boolean forceLoad = isAlwaysPreload(id, type);
                    String typeForKey = "";
                    switch (type) {
                        case "ship" ->
                            typeForKey = "$$$ship";
                        case "turret" ->
                            typeForKey = "$$$turret";
                        case "turretbarrel" ->
                            typeForKey = "$$$turretbarrel";
                        case "turretunder" ->
                            typeForKey = "$$$turretunder";
                        case "turretcoversmall" ->
                            typeForKey = "$$$turretcoversmall";
                        case "turretcovermedium" ->
                            typeForKey = "$$$turretcovermedium";
                        case "turretcoverlarge" ->
                            typeForKey = "$$$turretcoverlarge";
                        case "hardpoint" ->
                            typeForKey = "$$$hardpoint";
                        case "hardpointbarrel" ->
                            typeForKey = "$$$hardpointbarrel";
                        case "hardpointunder" ->
                            typeForKey = "$$$hardpointunder";
                        case "hardpointcoversmall" ->
                            typeForKey = "$$$hardpointcoversmall";
                        case "hardpointcovermedium" ->
                            typeForKey = "$$$hardpointcovermedium";
                        case "hardpointcoverlarge" ->
                            typeForKey = "$$$hardpointcoverlarge";
                        case "missile" ->
                            typeForKey = "$$$missile";
                        case "asteroid" -> {
                            typeForKey = "$$$asteroid";
                        }
                        default ->
                            success = false;
                    }
                    if (!success) {
                        continue;
                    }

                    final SpriteAPI sprite = Global.getSettings().getSprite(path);
                    final String frameStr;
                    if (type.contentEquals("turret") || type.contentEquals("hardpoint")) {
                        frameStr = "" + row.optInt("frame", 0);
                    } else {
                        frameStr = "";
                        if (row.optInt("frame", 0) > 0) {
                            Global.getLogger(TextureData.class).log(Level.WARN, "Defined animation frames for incompatible object type "
                                    + type + " for " + id + "! (discarded)");
                        }
                    }
                    final float magnitude = (float) row.optDouble("magnitude", 1.0);

                    boolean spriteExists = false;
                    boolean spriteWasLoaded = false;
                    TextureEntry linkedEntry = null;
                    switch (map) {
                        case "material" -> {
                            if ((sprite == null) || (sprite.getHeight() < 1)) {
                                if (isLoadMaterial() && (GraphicsLibSettings.preloadAllMaps() || forceLoad)) {
                                    try {
                                        Global.getSettings().forceMipmapsFor(path, true);
                                        Global.getSettings().loadTexture(path);
                                    } catch (IOException e) {
                                        Global.getLogger(TextureData.class).log(Level.ERROR, "Texture loading failed at " + path + "! " + e.getMessage());
                                        continue;
                                    }
                                    final SpriteAPI loadedSprite = Global.getSettings().getSprite(path);
                                    if (loadedSprite != null) {
                                        spriteExists = true;
                                        spriteWasLoaded = true;
                                        vramUsageBytes += Math.round((double) (16 * (Math.round(loadedSprite.getWidth() / loadedSprite.getTextureWidth())
                                                * Math.round(loadedSprite.getHeight() / loadedSprite.getTextureHeight()))) / 3.0);
                                    }
                                } else {
                                    spriteExists = false;
                                    spriteWasLoaded = true; // Set this true, hoping it'll load; might set to false later if it fails
                                }
                            } else {
                                spriteExists = true;
                            }
                            if (overwrite || !materialKeyToEntry.containsKey(id + typeForKey + frameStr)) {
                                final TextureEntry otherEntry = materialKeyToEntry.get(id + typeForKey + frameStr);
                                if ((otherEntry != null) && (otherEntry.spriteName != null) && otherEntry.validToLoad && otherEntry.loaded && !otherEntry.spriteName.contentEquals(path)) {
                                    final int texId = otherEntry.sprite.getTextureId();
                                    Global.getSettings().unloadTexture(otherEntry.spriteName);
                                    GL11.glDeleteTextures(texId);
                                    otherEntry.sprite = null;
                                    otherEntry.loaded = false;
                                    vramUsageBytes -= otherEntry.vramSize;
                                    Global.getLogger(TextureData.class).log(Level.INFO, "Unloaded potentially orphaned material texture: " + otherEntry.spriteName);
                                }
                                if (mnsSpritePathSet.contains(path)) {
                                    for (TextureEntry entry : materialKeyToEntry.values()) {
                                        if ((entry.spriteName != null) && entry.spriteName.contentEquals(path)) {
                                            linkedEntry = entry;
                                            break;
                                        }
                                    }
                                }
                                if (linkedEntry == null) {
                                    materialKeyToEntry.put(id + typeForKey + frameStr,
                                            new TextureEntry(Global.getSettings().getSprite(path), path, magnitude, false, spriteWasLoaded, spriteExists, forceLoad));
                                    mnsSpritePathSet.add(path);
                                } else {
                                    materialKeyToEntry.put(id + typeForKey + frameStr, linkedEntry);
                                }
                            }
                        }
                        case "normal" -> {
                            if ((sprite == null) || (sprite.getHeight() < 1)) {
                                if (isLoadNormal() && (GraphicsLibSettings.preloadAllMaps() || forceLoad)) {
                                    try {
                                        Global.getSettings().forceMipmapsFor(path, true);
                                        Global.getSettings().loadTexture(path);
                                    } catch (IOException e) {
                                        Global.getLogger(TextureData.class).log(Level.ERROR, "Texture loading failed at " + path + "! " + e.getMessage());
                                        continue;
                                    }
                                    final SpriteAPI loadedSprite = Global.getSettings().getSprite(path);
                                    if (loadedSprite != null) {
                                        spriteExists = true;
                                        spriteWasLoaded = true;
                                        vramUsageBytes += Math.round((double) (16 * (Math.round(loadedSprite.getWidth() / loadedSprite.getTextureWidth())
                                                * Math.round(loadedSprite.getHeight() / loadedSprite.getTextureHeight()))) / 3.0);
                                    }
                                } else {
                                    spriteExists = false;
                                    spriteWasLoaded = true; // Set this true, hoping it'll load; might set to false later if it fails
                                }
                            } else {
                                spriteExists = true;
                            }
                            if (overwrite || !normalKeyToEntry.containsKey(id + typeForKey + frameStr)) {
                                final TextureEntry otherEntry = normalKeyToEntry.get(id + typeForKey + frameStr);
                                if ((otherEntry != null) && (otherEntry.spriteName != null) && otherEntry.validToLoad && otherEntry.loaded && !otherEntry.spriteName.contentEquals(path)) {
                                    final int texId = otherEntry.sprite.getTextureId();
                                    Global.getSettings().unloadTexture(otherEntry.spriteName);
                                    GL11.glDeleteTextures(texId);
                                    otherEntry.sprite = null;
                                    otherEntry.loaded = false;
                                    vramUsageBytes -= otherEntry.vramSize;
                                    Global.getLogger(TextureData.class).log(Level.INFO, "Unloaded potentially orphaned normal texture: " + otherEntry.spriteName);
                                }
                                String linkedId = null;
                                if (mnsSpritePathSet.contains(path)) {
                                    for (Entry<String, TextureEntry> mapEntry : normalKeyToEntry.entrySet()) {
                                        final TextureEntry entry = mapEntry.getValue();
                                        if ((entry.spriteName != null) && entry.spriteName.contentEquals(path)) {
                                            linkedEntry = entry;
                                            linkedId = mapEntry.getKey();
                                            break;
                                        }
                                    }
                                }
                                if (linkedEntry == null) {
                                    normalKeyToEntry.put(id + typeForKey + frameStr,
                                            new TextureEntry(Global.getSettings().getSprite(path), path, magnitude, false, spriteWasLoaded, spriteExists, forceLoad));
                                    mnsSpritePathSet.add(path);
                                } else {
                                    normalKeyToEntry.put(id + typeForKey + frameStr, linkedEntry);
                                    if (Math.abs(linkedEntry.magnitude - magnitude) > 1E-6) {
                                        Global.getLogger(TextureData.class).log(Level.WARN,
                                                "Linking identical normal maps for " + id + typeForKey + frameStr + " and " + linkedId + " but magnitude is different!");
                                    }
                                }
                            }
                        }
                        case "surface" -> {
                            if ((sprite == null) || (sprite.getHeight() < 1)) {
                                if (isLoadSurface() && (GraphicsLibSettings.preloadAllMaps() || forceLoad)) {
                                    try {
                                        Global.getSettings().forceMipmapsFor(path, true);
                                        Global.getSettings().loadTexture(path);
                                    } catch (IOException e) {
                                        Global.getLogger(TextureData.class).log(Level.ERROR, "Texture loading failed at " + path + "! " + e.getMessage());
                                        continue;
                                    }
                                    final SpriteAPI loadedSprite = Global.getSettings().getSprite(path);
                                    if (loadedSprite != null) {
                                        spriteExists = true;
                                        spriteWasLoaded = true;
                                        vramUsageBytes += Math.round((double) (16 * (Math.round(loadedSprite.getWidth() / loadedSprite.getTextureWidth())
                                                * Math.round(loadedSprite.getHeight() / loadedSprite.getTextureHeight()))) / 3.0);
                                    }
                                } else {
                                    spriteExists = false;
                                    spriteWasLoaded = true; // Set this true, hoping it'll load; might set to false later if it fails
                                }
                            } else {
                                spriteExists = true;
                            }
                            if (overwrite || !surfaceKeyToEntry.containsKey(id + typeForKey + frameStr)) {
                                final TextureEntry otherEntry = surfaceKeyToEntry.get(id + typeForKey + frameStr);
                                if ((otherEntry != null) && (otherEntry.spriteName != null) && otherEntry.validToLoad && otherEntry.loaded && !otherEntry.spriteName.contentEquals(path)) {
                                    final int texId = otherEntry.sprite.getTextureId();
                                    Global.getSettings().unloadTexture(otherEntry.spriteName);
                                    GL11.glDeleteTextures(texId);
                                    otherEntry.sprite = null;
                                    otherEntry.loaded = false;
                                    vramUsageBytes -= otherEntry.vramSize;
                                    Global.getLogger(TextureData.class).log(Level.INFO, "Unloaded potentially orphaned surface texture: " + otherEntry.spriteName);
                                }
                                if (mnsSpritePathSet.contains(path)) {
                                    for (TextureEntry entry : surfaceKeyToEntry.values()) {
                                        if ((entry.spriteName != null) && entry.spriteName.contentEquals(path)) {
                                            linkedEntry = entry;
                                            break;
                                        }
                                    }
                                }
                                if (linkedEntry == null) {
                                    surfaceKeyToEntry.put(id + typeForKey + frameStr,
                                            new TextureEntry(Global.getSettings().getSprite(path), path, magnitude, false, spriteWasLoaded, spriteExists, forceLoad));
                                    mnsSpritePathSet.add(path);
                                } else {
                                    surfaceKeyToEntry.put(id + typeForKey + frameStr, linkedEntry);
                                }
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        } catch (Exception e) {
            Global.getLogger(TextureData.class).log(Level.ERROR, "Texture data loading failed for " + localPath + "! " + e.getMessage());
        }
    }

    private static boolean isAlwaysPreload(String id, String typeStr) {
        if (allHullStyles.isEmpty()) {
            JSONObject styleJson;
            try {
                styleJson = Global.getSettings().loadJSON("data/config/hull_styles.json", true);
            } catch (IOException | JSONException | RuntimeException ex) {
                styleJson = null;
            }
            if (styleJson != null) {
                final Iterator iter = styleJson.keys();
                while (iter.hasNext()) {
                    final String key = (String) iter.next();
                    if (key == null) {
                        continue;
                    }
                    allHullStyles.add(key);
                }
            } else {
                Global.getLogger(TextureData.class).log(Level.WARN, "Failed to load hull styles JSON!");
            }
        }

        try {
            final ObjectType type;
            switch (typeStr) {
                case "ship" ->
                    type = ObjectType.SHIP;
                case "turret" ->
                    type = ObjectType.TURRET;
                case "turretbarrel" ->
                    type = ObjectType.TURRET_BARREL;
                case "turretunder" ->
                    type = ObjectType.TURRET_UNDER;
                case "hardpoint" ->
                    type = ObjectType.HARDPOINT;
                case "hardpointbarrel" ->
                    type = ObjectType.HARDPOINT_BARREL;
                case "hardpointunder" ->
                    type = ObjectType.HARDPOINT_UNDER;
                case "missile" ->
                    type = ObjectType.MISSILE;
                case "turretcoversmall", "turretcovermedium", "turretcoverlarge", "hardpointcoversmall","hardpointcovermedium", "hardpointcoverlarge" -> {
                    return false;
                }
                case "asteroid" -> {
                    return true;
                }
                default -> {
                    return false;
                }
            }

            if (type == ObjectType.SHIP) {
                String actualId = id;
                final int idLen = id.length();
                int longestMatchingHullStyle = 0;
                for (String hullStyle : allHullStyles) {
                    final int hullStyleLen = hullStyle.length();
                    if (id.endsWith("_" + hullStyle) && (hullStyleLen > longestMatchingHullStyle)) {
                        actualId = id.substring(0, idLen - (hullStyleLen + 1));
                        longestMatchingHullStyle = hullStyleLen;
                    }
                }
                if (actualId.contentEquals("shuttlepod")) {
                    return true;
                }
                final ShipHullSpecAPI hullSpec = Global.getSettings().getHullSpec(actualId);
                if (hullSpec == null) {
                    return false;
                }
                if (hullSpec.getHullSize() == HullSize.FIGHTER) {
                    for (FighterWingSpecAPI wingSpec : Global.getSettings().getAllFighterWingSpecs()) {
                        final ShipVariantAPI wingVariant = wingSpec.getVariant();
                        if (wingVariant != null) {
                            final ShipHullSpecAPI wingHullSpec = wingVariant.getHullSpec();
                            if ((wingHullSpec != null) && wingHullSpec.getHullId().contentEquals(actualId)) {
                                if (wingSpec.hasTag(ALWAYS_PRELOAD_TAG)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return hullSpec.hasTag(ALWAYS_PRELOAD_TAG);
            } else if ((type == ObjectType.TURRET) || (type == ObjectType.TURRET_BARREL) || (type == ObjectType.TURRET_UNDER)
                    || (type == ObjectType.HARDPOINT) || (type == ObjectType.HARDPOINT_BARREL) || (type == ObjectType.HARDPOINT_UNDER)) {
                String actualId = id;
                final String[] idParts = id.split(":");
                if (idParts != null && idParts.length == 3) {
                    actualId = id;
                }
                final WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(actualId);
                if (weaponSpec == null) {
                    return false;
                }
                return weaponSpec.hasTag(ALWAYS_PRELOAD_TAG);
            } else if (type == ObjectType.MISSILE) {
                for (WeaponSpecAPI weaponSpec : Global.getSettings().getActuallyAllWeaponSpecs()) {
                    final Object projSpec = weaponSpec.getProjectileSpec();
                    if (projSpec instanceof MissileSpecAPI missileSpec) {
                        final ShipHullSpecAPI hullSpec = missileSpec.getHullSpec();
                        if ((hullSpec != null) && hullSpec.getHullId().contentEquals(id)) {
                            if (hullSpec.hasTag(ALWAYS_PRELOAD_TAG)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * Finds objects that are missing normal maps and automatically generates normal maps for them. Results are cached
     * in the GraphicsLib/cache folder so that they don't need to be regenerated on each game load. However, since mod
     * updates can occasionally update existing sprites, a hash of the mod list (and version numbers) is tracked between
     * game loads, so that the cache can be invalidated and regenerated if anything in the mod list changes.
     * <p>
     * This function is intended to be used internally by GraphicsLib.
     * <p>
     * @since 1.11.0
     */
    public static void autoGenMissingNormalMaps() {
        if (!isLoadMaterial() && !isLoadNormal() && !isLoadSurface()) {
            return;
        }

        String modVersionData = Global.getSettings().getGameVersion();
        final List<ModSpecAPI> mods = Global.getSettings().getModManager().getEnabledModsCopy();
        for (ModSpecAPI mod : mods) {
            modVersionData += mod.getId() + mod.getVersion();
        }
        final int modVersionHash = modVersionData.hashCode();

        invalidateCache = true;
        if (GraphicsLibSettings.autoGenNormals() && isLoadNormal() && Global.getSettings().fileExistsInCommon(CACHE_HASH_FILE)) {
            int prevModVersionHash = 0;
            try {
                prevModVersionHash = Integer.decode(Global.getSettings().readTextFileFromCommon(CACHE_HASH_FILE));
            } catch (IOException | NumberFormatException e) {
                Global.getLogger(TextureData.class).log(Level.ERROR, "Failed to read " + CACHE_HASH_FILE + ": " + e.getMessage());
            }
            if (modVersionHash == prevModVersionHash) {
                invalidateCache = false;
            } else {
                Global.getLogger(TextureData.class).log(Level.INFO, "Mod list changed; regenerating automatic normal map cache...");
            }
        }

        /* Run without autogen first so that we can find and link all of the sprites to their associated texture
         * entries.  This has two key benefits.  First, we can avoid generating unnecessary normal maps for things that
         * do already have normal maps defined.  Second, we can automatically link stuff that has identical sprites to
         * their corresponding material/normal/surface maps, even if there isn't a corresponding entry in the handmade
         * texture data CSV.
         */
        if (!everTraversedSpecs) {
            autoGenMissingNormalMapsInner(false);
            everTraversedSpecs = true;

            if (CHECK_INEFFICIENT) {
                Global.getLogger(TextureData.class).log(Level.INFO, "Wasted " + wastedBytesTotal + " bytes over " + consideredSprites.size() + " sprites!");
            }
        }
        if (GraphicsLibSettings.autoGenNormals() && isLoadNormal() && !everPerformedAutoGen) {
            autoGenMissingNormalMapsInner(true);
            try {
                Global.getSettings().writeTextFileToCommon(CACHE_HASH_FILE, "" + modVersionHash);
            } catch (IOException e) {
                Global.getLogger(TextureData.class).log(Level.ERROR, "Failed to write " + CACHE_HASH_FILE + ": " + e.getMessage());
            }
            invalidateCache = false;
            everPerformedAutoGen = true;
        }

        Global.getLogger(TextureData.class).log(Level.INFO, "Estimated VRAM usage for material/normal/surface maps after initial load: " + vramUsageBytes + " bytes");
    }

    /**
     * Unloads all unnecessary material/normal/surface maps to save VRAM. Preloads necessary material/normal/surface
     * maps based on context. Any textures that were missed will be reloaded on demand, during combat.
     * <p>
     * This function is intended to be used internally by GraphicsLib.
     * <p>
     * @param members The complete set of fleet members to use for preload context. If none are provided, all textures
     * will be unloaded.
     * <p>
     * @since 1.11.0
     */
    public static void unloadAndPreloadTextures(Collection<FleetMemberAPI> members) {
        Global.getLogger(TextureData.class).log(Level.INFO, "Estimated VRAM usage for material/normal/surface maps before unload/preload: " + vramUsageBytes + " bytes");

        final Object autoGen = new Object();
        Set<TextureEntry> keptMaterialEntries = new LinkedHashSet<>(materialKeyToEntry.size());
        Set<TextureEntry> keptNormalEntries = new LinkedHashSet<>(normalKeyToEntry.size());
        Set<TextureEntry> keptSurfaceEntries = new LinkedHashSet<>(surfaceKeyToEntry.size());
        if ((members != null) && !members.isEmpty() && ShaderLib.enabled && (isLoadMaterial() || isLoadNormal() || isLoadSurface()) && !GraphicsLibSettings.preloadAllMaps()) {
            final List<PreloadEntry> hullsToProcess = new ArrayList<>(members.size());
            for (FleetMemberAPI member : members) {
                final ShipHullSpecAPI hullSpec = member.getHullSpec();
                final ShipVariantAPI variant = member.getVariant();
                if ((hullSpec != null) && (variant != null)) {
                    hullsToProcess.add(new PreloadEntry(hullSpec, variant));

                    final Set<String> wings = new LinkedHashSet<>();
                    if (variant.getWings() != null) {
                        wings.addAll(variant.getWings());
                    }
                    if (variant.getFittedWings() != null) {
                        wings.addAll(variant.getFittedWings());
                    }
                    for (String wingId : wings) {
                        final FighterWingSpecAPI wingSpec = Global.getSettings().getFighterWingSpec(wingId);
                        if (wingSpec == null) {
                            continue;
                        }

                        final ShipVariantAPI wingVariant = wingSpec.getVariant();
                        if (wingVariant == null) {
                            continue;
                        }
                        final ShipHullSpecAPI wingHullSpec = wingVariant.getHullSpec();
                        if (wingHullSpec == null) {
                            continue;
                        }

                        hullsToProcess.add(new PreloadEntry(wingHullSpec, wingVariant, getFighterSkinKey(wingHullSpec, hullSpec)));
                    }

                    final Map<String, String> modules = variant.getStationModules();
                    if (modules == null) {
                        continue;
                    }

                    for (String slotId : modules.keySet()) {
                        final ShipVariantAPI childVariant = variant.getModuleVariant(slotId);
                        if (childVariant == null) {
                            continue;
                        }
                        final ShipHullSpecAPI childHullSpec = childVariant.getHullSpec();
                        if (childHullSpec == null) {
                            continue;
                        }

                        hullsToProcess.add(new PreloadEntry(childHullSpec, childVariant));
                    }
                }
            }

            for (TextureDataType type : TextureDataType.values()) {
                Set<TextureEntry> keptEntries = keptMaterialEntries;
                switch (type) {
                    case MATERIAL_MAP ->
                        keptEntries = keptMaterialEntries;
                    case NORMAL_MAP ->
                        keptEntries = keptNormalEntries;
                    case SURFACE_MAP ->
                        keptEntries = keptSurfaceEntries;
                }
                for (PreloadEntry entry : hullsToProcess) {
                    final ShipHullSpecAPI hullSpec = entry.hullSpec;
                    final ShipVariantAPI variant = entry.variant;
                    if ((hullSpec == null) || (variant == null)) {
                        continue;
                    }

                    TextureEntry shipTexEntry = null;
                    if (entry.fighterSpriteKey != null) {
                        shipTexEntry = getTextureDataWithAutoGen(entry.fighterSpriteKey, type, ObjectType.SHIP, 0, autoGen, true);
                    }
                    if (shipTexEntry == null) {
                        shipTexEntry = getTextureDataWithAutoGen(hullSpec.getHullId(), type, ObjectType.SHIP, 0, autoGen, true);
                    }
                    if (shipTexEntry == null) {
                        shipTexEntry = getTextureDataWithAutoGen(hullSpec.getDParentHullId(), type, ObjectType.SHIP, 0, autoGen, true);
                    }
                    if (shipTexEntry == null) {
                        shipTexEntry = getTextureDataWithAutoGen(hullSpec.getBaseHullId(), type, ObjectType.SHIP, 0, autoGen, true);
                    }
                    if (shipTexEntry != null) {
                        keptEntries.add(shipTexEntry);
                    }

                    final Set<WeaponSlotAPI> emptySlots = new LinkedHashSet<>(hullSpec.getAllWeaponSlotsCopy());
                    final HashMap<String, String> builtInWeapons = hullSpec.getBuiltInWeapons();
                    final Set<String> filledSlotIds = new LinkedHashSet<>();
                    if (variant.getFittedWeaponSlots() != null) {
                        filledSlotIds.addAll(variant.getFittedWeaponSlots());
                    }
                    if (variant.getNonBuiltInWeaponSlots() != null) {
                        filledSlotIds.addAll(variant.getNonBuiltInWeaponSlots());
                    }
                    if (builtInWeapons != null) {
                        filledSlotIds.addAll(builtInWeapons.keySet());
                    }
                    for (String slotId : filledSlotIds) {
                        final WeaponSlotAPI slotToRemove = hullSpec.getWeaponSlot(slotId);
                        if (slotToRemove != null) {
                            for (Iterator<WeaponSlotAPI> iter = emptySlots.iterator(); iter.hasNext();) {
                                final WeaponSlotAPI slot = iter.next();
                                if (slot.getId().contentEquals(slotToRemove.getId())) {
                                    iter.remove();
                                    break;
                                }
                            }
                        }
                    }
                    final String hullStyle = baseHullIdToHullStyle.get(hullSpec.getBaseHullId());
                    if (hullStyle != null) {
                        for (WeaponSlotAPI slot : emptySlots) {
                            if (slot.isDecorative() || slot.isHidden() || slot.isSystemSlot() || (slot.getWeaponType() == WeaponType.LAUNCH_BAY)
                                    || slot.isStationModule() || slot.isBuiltIn()) {
                                continue;
                            }
                            final TextureEntry coverTexEntry;
                            switch (slot.getSlotSize()) {
                                default:
                                case SMALL:
                                    if (slot.isHardpoint()) {
                                        coverTexEntry = TextureData.getTextureDataWithAutoGen(hullStyle, type, ObjectType.HARDPOINT_COVER_SMALL, 0, autoGen, true);
                                    } else {
                                        coverTexEntry = TextureData.getTextureDataWithAutoGen(hullStyle, type, ObjectType.TURRET_COVER_SMALL, 0, autoGen, true);
                                    }
                                    break;
                                case MEDIUM:
                                    if (slot.isHardpoint()) {
                                        coverTexEntry = TextureData.getTextureDataWithAutoGen(hullStyle, type, ObjectType.HARDPOINT_COVER_MEDIUM, 0, autoGen, true);
                                    } else {
                                        coverTexEntry = TextureData.getTextureDataWithAutoGen(hullStyle, type, ObjectType.TURRET_COVER_MEDIUM, 0, autoGen, true);
                                    }
                                    break;
                                case LARGE:
                                    if (slot.isHardpoint()) {
                                        coverTexEntry = TextureData.getTextureDataWithAutoGen(hullStyle, type, ObjectType.HARDPOINT_COVER_LARGE, 0, autoGen, true);
                                    } else {
                                        coverTexEntry = TextureData.getTextureDataWithAutoGen(hullStyle, type, ObjectType.TURRET_COVER_LARGE, 0, autoGen, true);
                                    }
                                    break;
                            }
                            if (coverTexEntry != null) {
                                keptEntries.add(coverTexEntry);
                            }
                        }
                    }

                    for (String slotId : filledSlotIds) {
                        final WeaponSlotAPI slot = variant.getSlot(slotId);
                        final WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
                        if ((slot == null) || (weaponSpec == null)) {
                            continue;
                        }

                        final String weaponId = weaponSpec.getWeaponId();
                        String hardpointId = getWeaponSkinKey(hullSpec, weaponId, WeaponSkinType.HARDPOINT);
                        if (hardpointId == null) {
                            hardpointId = weaponId;
                        }
                        String hardpointBarrelId = getWeaponSkinKey(hullSpec, weaponId, WeaponSkinType.HARDPOINT_BARRELS);
                        if (hardpointBarrelId == null) {
                            hardpointBarrelId = weaponId;
                        }
                        String turretId = getWeaponSkinKey(hullSpec, weaponId, WeaponSkinType.TURRET);
                        if (turretId == null) {
                            turretId = weaponId;
                        }
                        String turretBarrelId = getWeaponSkinKey(hullSpec, weaponId, WeaponSkinType.TURRET_BARRELS);
                        if (turretBarrelId == null) {
                            turretBarrelId = weaponId;
                        }
                        String underId = getWeaponSkinKey(hullSpec, weaponId, WeaponSkinType.UNDER);
                        if (underId == null) {
                            underId = weaponId;
                        }

                        Integer numFrames = weaponIdToAnimFrames.get(weaponId);
                        if (numFrames == null) {
                            numFrames = 0;
                        }
                        for (int frame = 0; frame <= numFrames; frame++) {
                            final TextureEntry weaponTexEntry;
                            if (slot.isHardpoint()) {
                                weaponTexEntry = TextureData.getTextureDataWithAutoGen(hardpointId, type, ObjectType.HARDPOINT, frame, autoGen, true);
                            } else {
                                weaponTexEntry = TextureData.getTextureDataWithAutoGen(turretId, type, ObjectType.TURRET, frame, autoGen, true);
                            }
                            if (weaponTexEntry != null) {
                                keptEntries.add(weaponTexEntry);
                            }

                            final TextureEntry weaponBarrelTexEntry;
                            if (slot.isHardpoint()) {
                                weaponBarrelTexEntry = TextureData.getTextureDataWithAutoGen(hardpointBarrelId, type, ObjectType.HARDPOINT_BARREL, frame, autoGen, true);
                            } else {
                                weaponBarrelTexEntry = TextureData.getTextureDataWithAutoGen(turretBarrelId, type, ObjectType.TURRET_BARREL, frame, autoGen, true);
                            }
                            if (weaponBarrelTexEntry != null) {
                                keptEntries.add(weaponBarrelTexEntry);
                            }

                            final TextureEntry weaponUnderTexEntry;
                            if (slot.isHardpoint()) {
                                weaponUnderTexEntry = TextureData.getTextureDataWithAutoGen(underId, type, ObjectType.HARDPOINT_UNDER, frame, autoGen, true);
                            } else {
                                weaponUnderTexEntry = TextureData.getTextureDataWithAutoGen(underId, type, ObjectType.TURRET_UNDER, frame, autoGen, true);
                            }
                            if (weaponUnderTexEntry != null) {
                                keptEntries.add(weaponUnderTexEntry);
                            }
                        }

                        if (weaponSpec.getProjectileSpec() instanceof MissileSpecAPI missileSpec) {
                            if (!missileSpec.getTypeString().contentEquals("MOTE") && !missileSpec.getTypeString().startsWith("FLARE")) {
                                final ShipHullSpecAPI missileHullSpec = missileSpec.getHullSpec();
                                if (missileHullSpec != null) {
                                    final TextureEntry missileTexEntry = TextureData.getTextureDataWithAutoGen(missileHullSpec.getHullId(), type, ObjectType.MISSILE, 0, autoGen, true);
                                    if (missileTexEntry != null) {
                                        keptEntries.add(missileTexEntry);
                                    }

                                    if (missileSpec.getTypeString().startsWith("MIRV")) {
                                        JSONObject behaviorSpec = missileSpec.getBehaviorJSON();
                                        if ((behaviorSpec != null) && behaviorSpec.has("projectileSpec")) {
                                            String mirvId = behaviorSpec.optString("projectileSpec");
                                            if ((mirvId != null) && !mirvId.isEmpty()) {
                                                final TextureEntry mirvTexEntry = TextureData.getTextureDataWithAutoGen(mirvId, type, ObjectType.MISSILE, 0, autoGen, true);
                                                if (mirvTexEntry != null) {
                                                    keptEntries.add(mirvTexEntry);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (ShaderLib.enabled && GraphicsLibSettings.preloadAllMaps()) {
            if (isLoadMaterial()) {
                keptMaterialEntries = new LinkedHashSet<>(materialKeyToEntry.values());
            }
            if (isLoadNormal()) {
                keptNormalEntries = new LinkedHashSet<>(normalKeyToEntry.values());
            }
            if (isLoadSurface()) {
                keptSurfaceEntries = new LinkedHashSet<>(surfaceKeyToEntry.values());
            }
        }

        Global.getLogger(TextureData.class).log(Level.INFO, "Unloading...");
        for (TextureDataType type : TextureDataType.values()) {
            Set<Entry<String, TextureEntry>> keyToEntry = materialKeyToEntry.entrySet();
            Set<TextureEntry> keptEntries = null;
            switch (type) {
                case MATERIAL_MAP -> {
                    keyToEntry = materialKeyToEntry.entrySet();
                    if (isLoadMaterial()) {
                        keptEntries = keptMaterialEntries;
                    }
                }
                case NORMAL_MAP -> {
                    keyToEntry = normalKeyToEntry.entrySet();
                    if (isLoadNormal()) {
                        keptEntries = keptNormalEntries;
                    }
                }
                case SURFACE_MAP -> {
                    keyToEntry = surfaceKeyToEntry.entrySet();
                    if (isLoadSurface()) {
                        keptEntries = keptSurfaceEntries;
                    }
                }
            }
            for (Entry<String, TextureEntry> entry : keyToEntry) {
                final TextureEntry texEntry = entry.getValue();
                if ((keptEntries != null) && (texEntry != null)) {
                    if (texEntry.alwaysPreload) {
                        keptEntries.add(texEntry);
                    }
                    if (texEntry.autoGen && !GraphicsLibSettings.autoGenNormals()) {
                        keptEntries.remove(texEntry);
                    }
                }
                if ((texEntry != null) && (texEntry.sprite != null) && (texEntry.spriteName != null) && texEntry.validToLoad && texEntry.loaded) {
                    if ((keptEntries != null) && keptEntries.contains(texEntry)) {
                        continue;
                    }
                    final int texId = texEntry.sprite.getTextureId();
                    Global.getSettings().unloadTexture(texEntry.spriteName);
                    GL11.glDeleteTextures(texId);
                    texEntry.sprite = null;
                    texEntry.loaded = false;
                    vramUsageBytes -= texEntry.vramSize;
                }
            }
        }

        int count = 0;
        if (ShaderLib.enabled && (isLoadMaterial() || isLoadNormal() || isLoadSurface())) {
            Global.getLogger(TextureData.class).log(Level.INFO, "Preloading...");
            for (TextureDataType type : TextureDataType.values()) {
                if ((type == TextureDataType.MATERIAL_MAP) && !isLoadMaterial()) {
                    continue;
                } else if ((type == TextureDataType.NORMAL_MAP) && !isLoadNormal()) {
                    continue;
                } else if ((type == TextureDataType.SURFACE_MAP) && !isLoadSurface()) {
                    continue;
                }
                Set<TextureEntry> keptEntries = keptMaterialEntries;
                switch (type) {
                    case MATERIAL_MAP ->
                        keptEntries = keptMaterialEntries;
                    case NORMAL_MAP ->
                        keptEntries = keptNormalEntries;
                    case SURFACE_MAP ->
                        keptEntries = keptSurfaceEntries;
                }
                for (TextureEntry texEntry : keptEntries) {
                    if (!texEntry.loaded && texEntry.validToLoad && (texEntry.spriteName != null) && !texEntry.spriteName.isEmpty()
                            && (!texEntry.autoGen || GraphicsLibSettings.autoGenNormals())) {
                        try {
                            Global.getSettings().forceMipmapsFor(texEntry.spriteName, true);
                            Global.getSettings().loadTexture(texEntry.spriteName);
                        } catch (IOException | RuntimeException e) {
                            texEntry.validToLoad = false;
                            continue;
                        }
                        texEntry.sprite = Global.getSettings().getSprite(texEntry.spriteName);
                        if ((texEntry.sprite != null) && (texEntry.sprite.getHeight() >= 1)) {
                            if (texEntry.vramSize == 0) {
                                texEntry.vramSize = Math.round((double) (16 * (Math.round(texEntry.sprite.getWidth() / texEntry.sprite.getTextureWidth())
                                        * Math.round(texEntry.sprite.getHeight() / texEntry.sprite.getTextureHeight()))) / 3.0);
                            }
                            vramUsageBytes += texEntry.vramSize;
                            texEntry.loaded = true;
                        }
                        count++;
                        if ((count % 10) == 0) {
                            ShaderModPlugin.refresh();
                        }
                    }
                }
            }
        }

        Global.getLogger(TextureData.class).log(Level.INFO, "VRAM after unload/preload: " + vramUsageBytes + " bytes");
    }

    private static String getFighterSkinKey(ShipHullSpecAPI fighterSpec, ShipHullSpecAPI carrierSpec) {
        final String fighterStyle = baseHullIdToHullStyle.get(fighterSpec.getBaseHullId());
        final String carrierStyle = baseHullIdToHullStyle.get(carrierSpec.getBaseHullId());
        if ((fighterStyle != null) && (carrierStyle != null) && carrierStyle.equals(fighterStyle)) {
            return null;
        }

        String cat = "fighterSkinsPlayerOnly";
        String skin = getFighterSkinKey(cat, fighterSpec, carrierSpec);
        if (skin != null) {
            return skin;
        }

        cat = "fighterSkinsPlayerAndNPC";
        skin = getFighterSkinKey(cat, fighterSpec, carrierSpec);
        return skin;
    }

    private static String getFighterSkinKey(String cat, ShipHullSpecAPI fighterSpec, ShipHullSpecAPI carrierSpec) {
        final String exclude = "fighterSkinsExcludeFromSharing";
        final String id = fighterSpec.getHullId();
        final String style = baseHullIdToHullStyle.get(carrierSpec.getBaseHullId());
        if (style == null) {
            return null;
        }

        final List<String> skins = Global.getSettings().getSpriteKeys(cat);
        final Set<String> noSharing = new LinkedHashSet<>(Global.getSettings().getSpriteKeys(exclude));

        final List<String[]> matching = new ArrayList<>();
        for (String key : skins) {
            if (key.equals(id + "_" + style)) {
                return key;
            }
            if (key.startsWith(id) && !noSharing.contains(key)) {
                final String[] skin = {cat, key};
                matching.add(skin);
            }
        }

        final SpriteAPI carrierSprite = Global.getSettings().getSprite(carrierSpec.getSpriteName());
        if (carrierSprite == null) {
            return null;
        }
        if (!matching.isEmpty()) {
            String best = null;
            float minDist = Float.MAX_VALUE;

            for (String[] curr : matching) {
                final SpriteAPI sprite = Global.getSettings().getSprite(curr[0], curr[1]);
                final float dist = Misc.getColorDist(carrierSprite.getAverageBrightColor(), sprite.getAverageBrightColor());
                if (dist < minDist) {
                    best = curr[1];
                    minDist = dist;
                }
            }
            return best;
        }

        return null;
    }

    private static String getWeaponSkinKey(ShipHullSpecAPI shipSpec, String weaponId, WeaponSkinType type) {
        String cat = "weaponSkinsPlayerOnly";
        String skin = getWeaponSkinKey(cat, weaponId, shipSpec, type);
        if (skin != null) {
            return skin;
        }

        cat = "weaponSkinsPlayerAndNPC";
        skin = getWeaponSkinKey(cat, weaponId, shipSpec, type);
        return skin;
    }

    private static String getWeaponSkinKey(String cat, String weaponId, ShipHullSpecAPI shipSpec, WeaponSkinType type) {
        final String exclude = "weaponSkinsExcludeFromSharing";
        final String style = baseHullIdToHullStyle.get(shipSpec.getBaseHullId());
        if (style == null) {
            return null;
        }

        final List<String> skins = Global.getSettings().getSpriteKeys(cat);
        final Set<String> noSharing = new LinkedHashSet<>(Global.getSettings().getSpriteKeys(exclude));

        final List<String[]> matching = new ArrayList<>();
        final String keyForHull = weaponId + ":" + style + ":" + type.name();
        for (String key : skins) {
            if (key.equals(keyForHull)) {
                return key;
            }
            if (key.startsWith(weaponId) && !noSharing.contains(key)) {
                final String[] skin = {cat, key};
                matching.add(skin);
            }
        }

        final SpriteAPI shipSprite = Global.getSettings().getSprite(shipSpec.getSpriteName());
        if (shipSprite == null) {
            return null;
        }
        if (!matching.isEmpty()) {
            String best = null;
            float minDist = Float.MAX_VALUE;

            for (String[] curr : matching) {
                final SpriteAPI sprite = Global.getSettings().getSprite(curr[0], curr[1]);
                float dist = Misc.getColorDist(shipSprite.getAverageBrightColor(), sprite.getAverageBrightColor());
                if (dist < minDist) {
                    best = curr[1];
                    minDist = dist;
                }
            }
            return best;
        }

        return null;
    }

    private static class PreloadEntry {

        final ShipHullSpecAPI hullSpec;
        final ShipVariantAPI variant;
        final String fighterSpriteKey;

        PreloadEntry(ShipHullSpecAPI hullSpec, ShipVariantAPI variant) {
            this.hullSpec = hullSpec;
            this.variant = variant;
            fighterSpriteKey = null;
        }

        PreloadEntry(ShipHullSpecAPI hullSpec, ShipVariantAPI variant, String fighterSpriteKey) {
            this.hullSpec = hullSpec;
            this.variant = variant;
            this.fighterSpriteKey = fighterSpriteKey;
        }
    }

    private static void autoGenMissingNormalMapsInner(boolean autoGen) {
        int count = 0;

        JSONObject styleJson;
        try {
            styleJson = Global.getSettings().loadJSON("data/config/hull_styles.json", true);
        } catch (IOException | JSONException | RuntimeException ex) {
            styleJson = null;
        }
        if (styleJson != null) {
            final Iterator iter = styleJson.keys();
            while (iter.hasNext()) {
                final String key = (String) iter.next();
                if (key == null) {
                    continue;
                }

                final JSONObject styleJsonObj;
                try {
                    styleJsonObj = styleJson.getJSONObject(key);
                } catch (JSONException e) {
                    Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull style " + key + ": " + e.getMessage());
                    continue;
                }

                allHullStyles.add(key);

                if (styleJsonObj.has("slotCoverSmallTurret")) {
                    final String spriteName;
                    try {
                        spriteName = styleJsonObj.getString("slotCoverSmallTurret");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull style " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, spriteName, ObjectType.TURRET_COVER_SMALL, 0, autoGen, false);
                }
                if (styleJsonObj.has("slotCoverSmallHardpoint")) {
                    final String spriteName;
                    try {
                        spriteName = styleJsonObj.getString("slotCoverSmallHardpoint");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull style " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, spriteName, ObjectType.HARDPOINT_COVER_SMALL, 0, autoGen, false);
                }
                if (styleJsonObj.has("slotCoverMediumTurret")) {
                    final String spriteName;
                    try {
                        spriteName = styleJsonObj.getString("slotCoverMediumTurret");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull style " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, spriteName, ObjectType.TURRET_COVER_MEDIUM, 0, autoGen, false);
                }
                if (styleJsonObj.has("slotCoverMediumHardpoint")) {
                    final String spriteName;
                    try {
                        spriteName = styleJsonObj.getString("slotCoverMediumHardpoint");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull style " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, spriteName, ObjectType.HARDPOINT_COVER_MEDIUM, 0, autoGen, false);
                }
                if (styleJsonObj.has("slotCoverLargeTurret")) {
                    final String spriteName;
                    try {
                        spriteName = styleJsonObj.getString("slotCoverLargeTurret");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull style " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, spriteName, ObjectType.TURRET_COVER_LARGE, 0, autoGen, false);
                }
                if (styleJsonObj.has("slotCoverLargeHardpoint")) {
                    final String spriteName;
                    try {
                        spriteName = styleJsonObj.getString("slotCoverLargeHardpoint");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull style " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, spriteName, ObjectType.HARDPOINT_COVER_LARGE, 0, autoGen, false);
                }

                count++;
                if ((count % 10) == 0) {
                    ShaderModPlugin.refresh();
                }
            }
        } else {
            Global.getLogger(TextureData.class).log(Level.WARN, "Failed to load hull styles JSON!");
        }

        final Set<String> fighterSkinsPlayerNPC = new HashSet<>(Global.getSettings().getSpriteKeys("fighterSkinsPlayerAndNPC"));
        final Set<String> fighterSkinsPlayer = new HashSet<>(Global.getSettings().getSpriteKeys("fighterSkinsPlayerOnly"));
        for (ShipHullSpecAPI hullSpec : Global.getSettings().getAllShipHullSpecs()) {
            if (hullSpec.isDefaultDHull()) {
                continue;
            }

            final String key = hullSpec.getHullId();

            boolean autoGenOverride = hullSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
            boolean autoGenThisHull = autoGen && !hullSpec.hasTag(NO_AUTOGEN_TAG);
            boolean isFighter = false;
            if (hullSpec.getHullSize() == HullSize.FIGHTER) {
                for (FighterWingSpecAPI wingSpec : Global.getSettings().getAllFighterWingSpecs()) {
                    final ShipVariantAPI wingVariant = wingSpec.getVariant();
                    if (wingVariant != null) {
                        ShipHullSpecAPI wingHullSpec = wingVariant.getHullSpec();
                        if ((wingHullSpec != null) && wingHullSpec.getHullId().contentEquals(key)) {
                            autoGenOverride |= wingSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
                            autoGenThisHull &= !wingSpec.hasTag(NO_AUTOGEN_TAG);
                            isFighter = true;
                            break;
                        }
                    }
                }
            }

            mapSpriteToMNSWithAutoGen(key, hullSpec.getSpriteName(), ObjectType.SHIP, 0, autoGenThisHull, autoGenOverride);

            if (isFighter) {
                for (String hullStyle : allHullStyles) {
                    final String skinKey = key + "_" + hullStyle;
                    String skinSpriteName = null;
                    if (fighterSkinsPlayerNPC.contains(skinKey)) {
                        skinSpriteName = Global.getSettings().getSpriteName("fighterSkinsPlayerAndNPC", skinKey);
                    }
                    if (((skinSpriteName == null) || skinSpriteName.isEmpty()) && fighterSkinsPlayer.contains(skinKey)) {
                        skinSpriteName = Global.getSettings().getSpriteName("fighterSkinsPlayerOnly", skinKey);
                    }
                    mapSpriteToMNSWithAutoGen(skinKey, skinSpriteName, ObjectType.SHIP, 0, autoGenThisHull, autoGenOverride);
                }
            }

            final String baseHull = hullSpec.getBaseHullId();
            if (!baseHullIdToHullStyle.containsKey(baseHull)) {
                JSONObject shipJson;
                try {
                    shipJson = Global.getSettings().loadJSON("data/hulls/" + baseHull + ".ship");
                } catch (IOException | JSONException | RuntimeException ex) {
                    shipJson = null;
                }
                if (shipJson != null) {
                    if (shipJson.has("style")) {
                        String style;
                        try {
                            style = shipJson.getString("style");
                        } catch (JSONException e) {
                            Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for hull " + baseHull + ": " + e.getMessage());
                            continue;
                        }
                        baseHullIdToHullStyle.put(baseHull, style);
                    }
                }
            }

            count++;
            if ((count % 10) == 0) {
                ShaderModPlugin.refresh();
            }
        }

        final Set<String> weaponSkinsPlayerNPC = new HashSet<>(Global.getSettings().getSpriteKeys("weaponSkinsPlayerAndNPC"));
        final Set<String> weaponSkinsPlayer = new HashSet<>(Global.getSettings().getSpriteKeys("weaponSkinsPlayerOnly"));
        for (WeaponSpecAPI weaponSpec : Global.getSettings().getActuallyAllWeaponSpecs()) {
            final String key = weaponSpec.getWeaponId();

            final boolean autoGenOverride = weaponSpec.hasTag(AUTOGEN_OVERRIDE_TAG);
            final boolean autoGenThisWeapon = autoGen && !weaponSpec.hasTag(NO_AUTOGEN_TAG);

            final String turretSpriteName = weaponSpec.getTurretSpriteName();
            mapSpriteToMNSWithAutoGen(key, turretSpriteName, ObjectType.TURRET, 0, autoGenThisWeapon, autoGenOverride);
            final String turretUnderSpriteName = weaponSpec.getTurretUnderSpriteName();
            mapSpriteToMNSWithAutoGen(key, turretUnderSpriteName, ObjectType.TURRET_UNDER, 0, autoGenThisWeapon, autoGenOverride);
            final String hardpointSpriteName = weaponSpec.getHardpointSpriteName();
            mapSpriteToMNSWithAutoGen(key, hardpointSpriteName, ObjectType.HARDPOINT, 0, autoGenThisWeapon, autoGenOverride);
            final String hardpointUnderSpriteName = weaponSpec.getHardpointUnderSpriteName();
            mapSpriteToMNSWithAutoGen(key, hardpointUnderSpriteName, ObjectType.HARDPOINT_UNDER, 0, autoGenThisWeapon, autoGenOverride);

            JSONObject weaponJson;
            try {
                weaponJson = Global.getSettings().loadJSON("data/weapons/" + key + ".wpn");
            } catch (IOException | JSONException | RuntimeException ex) {
                weaponJson = null;
            }
            if (weaponJson == null) {
                try {
                    weaponJson = Global.getSettings().loadJSON("data/shipsystems/wpn/" + key + ".wpn");
                } catch (IOException | JSONException | RuntimeException ex) {
                    weaponJson = null;
                }
            }
            if (weaponJson != null) {
                if (weaponJson.has("turretGunSprite")) {
                    String turretBarrelSpriteName;
                    try {
                        turretBarrelSpriteName = weaponJson.getString("turretGunSprite");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for weapon " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, turretBarrelSpriteName, ObjectType.TURRET_BARREL, 0, autoGenThisWeapon, autoGenOverride);
                }
                if (weaponJson.has("hardpointGunSprite")) {
                    String hardpointBarrelSpriteName;
                    try {
                        hardpointBarrelSpriteName = weaponJson.getString("hardpointGunSprite");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for weapon " + key + ": " + e.getMessage());
                        continue;
                    }
                    mapSpriteToMNSWithAutoGen(key, hardpointBarrelSpriteName, ObjectType.HARDPOINT_BARREL, 0, autoGenThisWeapon, autoGenOverride);
                }
                if (weaponJson.has("numFrames")) {
                    final int numFrames;
                    try {
                        numFrames = weaponJson.getInt("numFrames");
                    } catch (JSONException e) {
                        Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for weapon " + key + ": " + e.getMessage());
                        continue;
                    }
                    for (int frame = 1; frame < numFrames; frame++) {
                        mapSpriteToMNSWithAutoGen(key, turretSpriteName, ObjectType.TURRET, frame, autoGenThisWeapon, autoGenOverride);
                        mapSpriteToMNSWithAutoGen(key, hardpointSpriteName, ObjectType.HARDPOINT, frame, autoGenThisWeapon, autoGenOverride);
                    }
                    weaponIdToAnimFrames.put(key, numFrames);
                } else {
                    weaponIdToAnimFrames.put(key, 0);
                }
            } else {
                Global.getLogger(TextureData.class).log(Level.WARN, "Failed to find JSON for weapon " + key);
            }

            for (String hullStyle : allHullStyles) {
                final String turretSkinKey = key + ":" + hullStyle + ":" + WeaponSkinType.TURRET.name();
                String turretSkinSpriteName = null;
                if (weaponSkinsPlayerNPC.contains(turretSkinKey)) {
                    turretSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerAndNPC", turretSkinKey);
                }
                if (((turretSkinSpriteName == null) || turretSkinSpriteName.isEmpty()) && weaponSkinsPlayer.contains(turretSkinKey)) {
                    turretSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerOnly", turretSkinKey);
                }
                mapSpriteToMNSWithAutoGen(turretSkinKey, turretSkinSpriteName, ObjectType.TURRET, 0, autoGenThisWeapon, autoGenOverride);

                final String turretBarrelSkinKey = key + ":" + hullStyle + ":" + WeaponSkinType.TURRET_BARRELS.name();
                String turretBarrelSkinSpriteName = null;
                if (weaponSkinsPlayerNPC.contains(turretBarrelSkinKey)) {
                    turretBarrelSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerAndNPC", turretBarrelSkinKey);
                }
                if (((turretBarrelSkinSpriteName == null) || turretBarrelSkinSpriteName.isEmpty()) && weaponSkinsPlayer.contains(turretBarrelSkinKey)) {
                    turretBarrelSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerOnly", turretBarrelSkinKey);
                }
                mapSpriteToMNSWithAutoGen(turretBarrelSkinKey, turretBarrelSkinSpriteName, ObjectType.TURRET_BARREL, 0, autoGenThisWeapon, autoGenOverride);

                final String hardpointSkinKey = key + ":" + hullStyle + ":" + WeaponSkinType.HARDPOINT.name();
                String hardpointSkinSpriteName = null;
                if (weaponSkinsPlayerNPC.contains(hardpointSkinKey)) {
                    hardpointSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerAndNPC", hardpointSkinKey);
                }
                if (((hardpointSkinSpriteName == null) || hardpointSkinSpriteName.isEmpty()) && weaponSkinsPlayer.contains(hardpointSkinKey)) {
                    hardpointSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerOnly", hardpointSkinKey);
                }
                mapSpriteToMNSWithAutoGen(hardpointSkinKey, hardpointSkinSpriteName, ObjectType.HARDPOINT, 0, autoGenThisWeapon, autoGenOverride);

                final String hardpointBarrelSkinKey = key + ":" + hullStyle + ":" + WeaponSkinType.HARDPOINT_BARRELS.name();
                String hardpointBarrelSkinSpriteName = null;
                if (weaponSkinsPlayerNPC.contains(hardpointBarrelSkinKey)) {
                    hardpointBarrelSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerAndNPC", hardpointBarrelSkinKey);
                }
                if (((hardpointBarrelSkinSpriteName == null) || hardpointBarrelSkinSpriteName.isEmpty()) && weaponSkinsPlayer.contains(hardpointBarrelSkinKey)) {
                    hardpointBarrelSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerOnly", hardpointBarrelSkinKey);
                }
                mapSpriteToMNSWithAutoGen(hardpointBarrelSkinKey, hardpointBarrelSkinSpriteName, ObjectType.HARDPOINT_BARREL, 0, autoGenThisWeapon, autoGenOverride);

                final String underSkinKey = key + ":" + hullStyle + ":" + WeaponSkinType.UNDER.name();
                String underSkinSpriteName = null;
                if (weaponSkinsPlayerNPC.contains(underSkinKey)) {
                    underSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerAndNPC", underSkinKey);
                }
                if (((underSkinSpriteName == null) || underSkinSpriteName.isEmpty()) && weaponSkinsPlayer.contains(underSkinKey)) {
                    underSkinSpriteName = Global.getSettings().getSpriteName("weaponSkinsPlayerOnly", underSkinKey);
                }
                mapSpriteToMNSWithAutoGen(underSkinKey, underSkinSpriteName, ObjectType.TURRET_UNDER, 0, autoGenThisWeapon, autoGenOverride);
                mapSpriteToMNSWithAutoGen(underSkinKey, underSkinSpriteName, ObjectType.HARDPOINT_UNDER, 0, autoGenThisWeapon, autoGenOverride);
            }

            if (weaponSpec.getProjectileSpec() instanceof MissileSpecAPI missileSpec) {
                if (!missileSpec.getTypeString().contentEquals("MOTE") && !missileSpec.getTypeString().startsWith("FLARE")) {
                    final ShipHullSpecAPI hullSpec = missileSpec.getHullSpec();
                    if (hullSpec != null) {
                        final String missileKey = hullSpec.getHullId();
                        final String missileSpriteName = hullSpec.getSpriteName();
                        mapSpriteToMNSWithAutoGen(missileKey, missileSpriteName, ObjectType.MISSILE, 0, autoGenThisWeapon, autoGenOverride);

                        /* "Only" one level of recursion */
                        if (missileSpec.getTypeString().startsWith("MIRV")) {
                            JSONObject behaviorSpec = missileSpec.getBehaviorJSON();
                            if ((behaviorSpec != null) && behaviorSpec.has("projectileSpec")) {
                                String mirvKey;
                                try {
                                    mirvKey = behaviorSpec.getString("projectileSpec");
                                } catch (JSONException e) {
                                    Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for missile " + missileKey + ": " + e.getMessage());
                                    continue;
                                }
                                if (mirvKey != null) {
                                    JSONObject mirvJson;
                                    try {
                                        mirvJson = Global.getSettings().loadJSON("data/weapons/proj/" + mirvKey + ".proj");
                                    } catch (IOException | JSONException | RuntimeException ex) {
                                        mirvJson = null;
                                    }
                                    if (mirvJson == null) {
                                        try {
                                            mirvJson = Global.getSettings().loadJSON("data/shipsystems/proj/" + mirvKey + ".proj");
                                        } catch (IOException | JSONException | RuntimeException ex) {
                                            mirvJson = null;
                                        }
                                    }
                                    if (mirvJson != null) {
                                        if (mirvJson.has("sprite")) {
                                            String mirvSpriteName;
                                            try {
                                                mirvSpriteName = mirvJson.getString("sprite");
                                            } catch (JSONException e) {
                                                Global.getLogger(TextureData.class).log(Level.ERROR, "JSON error for MIRV " + mirvKey + ": " + e.getMessage());
                                                continue;
                                            }
                                            mapSpriteToMNSWithAutoGen(mirvKey, mirvSpriteName, ObjectType.MISSILE, 0, autoGenThisWeapon, autoGenOverride);
                                        }
                                    } else {
                                        Global.getLogger(TextureData.class).log(Level.WARN, "Failed to find JSON for MIRV " + mirvKey);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            count++;
            if ((count % 10) == 0) {
                ShaderModPlugin.refresh();
            }
        }
    }

    /**
     * Utility function to associate a sprite with the internal texture database. Can auto-generate a normal map.
     * <p>
     * @param key The ID associated with the object that the sprite is meant for. The hull ID, weapon ID, etc.
     * @param spriteName The path of the sprite.
     * @param type What kind of object it is.
     * @param frame The frame of the animation, for an animated weapon. Use 0 for non-animated weapons. Ignored if type
     * isn't TURRET or HARDPOINT.
     * @param autoGen Enable auto-generation of normal maps.
     * @param autoGenOverride Causes the texture to not be marked as auto-generated.
     * <p>
     * @since 1.11.0
     */
    public static void mapSpriteToMNSWithAutoGen(String key, String spriteName, ObjectType type, int frame, boolean autoGen, boolean autoGenOverride) {
        if ((spriteName != null) && !spriteName.isEmpty() && (key != null) && !key.isEmpty()) {
            if (frame > 0) {
                spriteName = getAnimNameForFrame(spriteName, frame);
            }
            if (CHECK_INEFFICIENT) {
                alertInefficientSprite(spriteName, type);
            }
            if (isLoadMaterial()) {
                TextureEntry entry = getTextureDataWithAutoGen(key, TextureDataType.MATERIAL_MAP, type, frame, null, true);
                if (entry == null) {
                    entry = materialSpriteNameToEntry.get(spriteName);
                    if (entry != null) {
                        materialKeyToEntry.put(getTextureDataKey(key, type, frame), entry);
                        if ((entry.spriteName != null) && !spriteName.isEmpty()) {
                            mnsSpritePathSet.add(entry.spriteName);
                        }
                    }
                } else {
                    materialSpriteNameToEntry.put(spriteName, entry);
                }
            }
            if (isLoadNormal()) {
                TextureEntry entry = getTextureDataWithAutoGen(key, TextureDataType.NORMAL_MAP, type, frame, null, true);
                if (entry == null) {
                    entry = normalSpriteNameToEntry.get(spriteName);
                    if (entry == null) {
                        if (autoGen) {
                            final SpriteAPI sprite = autoGenNormalMap(spriteName, key, type, frame);
                            if (sprite != null) {
                                /* Even if we're not preloading, we do need to load the sprite when establishing the
                                 * cache of auto-generated normal maps.  However, once we have that sprite and have
                                 * made its texture entry, it's safe to just unload it to save memory.
                                 */
                                final boolean forceLoad;
                                switch (type) {
                                    case SHIP ->
                                        forceLoad = isAlwaysPreload(key, "ship");
                                    case TURRET ->
                                        forceLoad = isAlwaysPreload(key, "turret");
                                    case TURRET_BARREL ->
                                        forceLoad = isAlwaysPreload(key, "turretbarrel");
                                    case TURRET_UNDER ->
                                        forceLoad = isAlwaysPreload(key, "turretunder");
                                    case HARDPOINT ->
                                        forceLoad = isAlwaysPreload(key, "hardpoint");
                                    case HARDPOINT_BARREL ->
                                        forceLoad = isAlwaysPreload(key, "hardpointbarrel");
                                    case HARDPOINT_UNDER ->
                                        forceLoad = isAlwaysPreload(key, "hardpointunder");
                                    case MISSILE ->
                                        forceLoad = isAlwaysPreload(key, "missile");
                                    default -> {
                                        forceLoad = false;
                                    }
                                }
                                entry = new TextureEntry(sprite, filePathForCache(key, type, frame), 1f, !autoGenOverride, true, true, forceLoad);
                                if (!GraphicsLibSettings.preloadAllMaps()) {
                                    final int texId = entry.sprite.getTextureId();
                                    Global.getSettings().unloadTexture(entry.spriteName);
                                    GL11.glDeleteTextures(texId);
                                    entry.sprite = null;
                                    entry.loaded = false;
                                    vramUsageBytes -= entry.vramSize;
                                }
                                normalKeyToEntry.put(getTextureDataKey(key, type, frame), entry);
                                if ((entry.spriteName != null) && !spriteName.isEmpty()) {
                                    mnsSpritePathSet.add(entry.spriteName);
                                }
                                normalSpriteNameToEntry.put(spriteName, entry);
                            }
                        }
                    } else {
                        normalKeyToEntry.put(getTextureDataKey(key, type, frame), entry);
                        if ((entry.spriteName != null) && !spriteName.isEmpty()) {
                            mnsSpritePathSet.add(entry.spriteName);
                        }
                    }
                } else {
                    normalSpriteNameToEntry.put(spriteName, entry);
                }
            }
            if (isLoadSurface()) {
                TextureEntry entry = getTextureDataWithAutoGen(key, TextureDataType.SURFACE_MAP, type, frame, null, true);
                if (entry == null) {
                    entry = surfaceSpriteNameToEntry.get(spriteName);
                    if (entry != null) {
                        surfaceKeyToEntry.put(getTextureDataKey(key, type, frame), entry);
                        if ((entry.spriteName != null) && !spriteName.isEmpty()) {
                            mnsSpritePathSet.add(entry.spriteName);
                        }
                    }
                } else {
                    surfaceSpriteNameToEntry.put(spriteName, entry);
                }
            }
        }
    }

    private static void alertInefficientSprite(String spritePath, ObjectType type) {
        if (consideredSprites.contains(spritePath)) {
            return;
        }

        final SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        if ((sprite == null) || (sprite.getHeight() < 1)) {
            return;
        }

        final int prevBoundTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        sprite.bindTexture();
        final int texWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        final int texHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        final int texSize = texWidth * texHeight * 4;
        final ByteBuffer texBuffer = ByteBuffer.allocateDirect(texSize);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texBuffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevBoundTex);

        final int trueWidth = Math.round(texWidth * sprite.getTextureWidth());
        final int trueHeight = Math.round(texHeight * sprite.getTextureHeight());
        final int yDiff = texHeight - trueHeight;
        final byte pixel[] = new byte[4];
        int leftMargin = trueWidth - 1;
        int rightMargin = 0;
        int bottomMargin = trueHeight - 1;
        int topMargin = 0;
        for (int y = (texHeight - 1); y >= 0; y--) {
            for (int x = 0; x < texWidth; x++) {
                pixel[0] = texBuffer.get();
                pixel[1] = texBuffer.get();
                pixel[2] = texBuffer.get();
                pixel[3] = texBuffer.get();
                if (x >= trueWidth) {
                    continue;
                }
                if (y < yDiff) {
                    continue;
                }

                final double alpha = ((pixel[3] < 0) ? (pixel[3] + 256) : pixel[3]) / 255.0;
                if (alpha > 0.0) {
                    leftMargin = Math.min(leftMargin, x);
                    rightMargin = Math.max(rightMargin, x);
                    bottomMargin = Math.min(bottomMargin, y - yDiff);
                    topMargin = Math.max(topMargin, y - yDiff);
                }
            }
        }

        leftMargin = Math.max(leftMargin - 1, 0);
        rightMargin = Math.min(rightMargin + 1, trueWidth - 1);
        bottomMargin = Math.max(bottomMargin - 1, 0);
        topMargin = Math.min(topMargin + 1, trueHeight - 1);
        if ((type == ObjectType.TURRET) || (type == ObjectType.TURRET_BARREL) || (type == ObjectType.TURRET_UNDER)) {
            int leftLoss = leftMargin;
            int rightLoss = (trueWidth - 1) - rightMargin;
            int bottomLoss = bottomMargin;
            int topLoss = (trueHeight - 1) - topMargin;
            leftLoss = rightLoss = Math.abs(Math.min(leftLoss, rightLoss));
            bottomLoss = topLoss = Math.abs(Math.min(bottomLoss, topLoss));
            leftMargin = leftLoss;
            rightMargin = (trueWidth - 1) - rightLoss;
            bottomMargin = bottomLoss;
            topMargin = (trueHeight - 1) - topLoss;
        }
        if ((type == ObjectType.HARDPOINT) || (type == ObjectType.HARDPOINT_BARREL) || (type == ObjectType.HARDPOINT_UNDER)) {
            int leftLoss = leftMargin;
            int rightLoss = (trueWidth - 1) - rightMargin;
            int bottomLoss = bottomMargin;
            int topLoss = (trueHeight - 1) - topMargin;
            leftLoss = rightLoss = Math.abs(Math.min(leftLoss, rightLoss));
            bottomLoss = Math.abs(Math.min(bottomLoss, topLoss / 3));
            topLoss = bottomLoss * 3;
            leftMargin = leftLoss;
            rightMargin = (trueWidth - 1) - rightLoss;
            bottomMargin = bottomLoss;
            topMargin = (trueHeight - 1) - topLoss;
        }

        int possibleTexWidth = Math.max(rightMargin - leftMargin, 1);
        int possibleTexHeight = Math.max(topMargin - bottomMargin, 1);
        int possibleTexWidthNPO2 = 1;
        int possibleTexHeightNPO2 = 1;
        while (possibleTexWidthNPO2 < possibleTexWidth) {
            possibleTexWidthNPO2 *= 2;
        }
        while (possibleTexHeightNPO2 < possibleTexHeight) {
            possibleTexHeightNPO2 *= 2;
        }
        int wastedBytes = Math.round(((texWidth * texHeight) - (possibleTexWidthNPO2 * possibleTexHeightNPO2)) * 16f / 3f);
        float wastePct = (100f * ((texWidth * texHeight) - (possibleTexWidthNPO2 * possibleTexHeightNPO2))) / (texWidth * texHeight);
        if (wastePct > 0.1f) {
            Global.getLogger(TextureData.class).log(Level.INFO, "\"" + spritePath + "\" is " + wastePct + "% wasted");
        }

        wastedBytesTotal += wastedBytes;
        consideredSprites.add(spritePath);
    }

    private static String filePathForCache(String key, ObjectType type, int frame) {
        final String frameStr;
        if ((type == ObjectType.TURRET) || (type == ObjectType.HARDPOINT)) {
            frameStr = "" + frame;
        } else {
            frameStr = "";
        }
        return CACHE_DIR + "/" + key.replaceAll("[\\\\/:*?\"<>|]", "__") + "___" + type.name() + frameStr + "_normal.png";
    }

    /**
     * Creates a normal map for the given sprite. The image corresponding to the returned Sprite is saved to the
     * GraphicsLib/cache folder. If a cached version of the image exists, it will be loaded from the disk so that it
     * doesn't have to be regenerated from scratch. However, if the mod list changed since the previous run of the game
     * (i.e., any mod ID or mod version is different), the cache is invalidated and the normal map will always be
     * regenerated.
     * <p>
     * @param spritePath The path of the sprite to generate a normal map for.
     * @param key The ID associated with the object that the sprite is meant for. The hull ID, weapon ID, etc.
     * @param type What kind of object the normal map is for.
     * @param frame The frame of the animation, for an animated weapon. Use 0 for non-animated weapons. Ignored if type
     * isn't TURRET or HARDPOINT.
     * <p>
     * @return The requested sprite. Null if an error occurred.
     * <p>
     * @since 1.11.0
     */
    public static SpriteAPI autoGenNormalMap(String spritePath, String key, ObjectType type, int frame) {
        if (!isLoadNormal() || !GraphicsLibSettings.autoGenNormals()) {
            return null;
        }

        final String frameStr;
        if ((type == ObjectType.TURRET) || (type == ObjectType.HARDPOINT)) {
            frameStr = "" + frame;
        } else {
            frameStr = "";
        }

        final String filePath = filePathForCache(key, type, frame);

        SpriteAPI normalSprite;
        if (!invalidateCache) {
            /* Load cache if available */
            normalSprite = Global.getSettings().getSprite(filePath);
            boolean loaded = false;
            if ((normalSprite == null) || (normalSprite.getHeight() < 1)) {
                try {
                    Global.getSettings().forceMipmapsFor(filePath, true);
                    Global.getSettings().loadTexture(filePath);
                } catch (IOException | RuntimeException e) {
                }
                normalSprite = Global.getSettings().getSprite(filePath);
                loaded = true;
            }

            if ((normalSprite != null) && (normalSprite.getHeight() >= 1)) {
                if (loaded) {
                    vramUsageBytes += Math.round((double) (16 * (Math.round(normalSprite.getWidth() / normalSprite.getTextureWidth())
                            * Math.round(normalSprite.getHeight() / normalSprite.getTextureHeight()))) / 3.0);
                }
                return normalSprite;
            }
        }

        final SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        if ((sprite == null) || (sprite.getHeight() < 1)) {
            Global.getLogger(TextureData.class).log(Level.ERROR, "Cannot find sprite for " + key + " (" + type.name() + frameStr + "): " + spritePath);
            return null;
        }

        final Class<?> fileClass;
        final Class<?> imageIoClass;
        final MethodHandle filePathConstructor;
        final MethodHandle imageioWrite;

        try {
            fileClass = Class.forName("java.io.File", false, Class.class.getClassLoader());
            imageIoClass = Class.forName("javax.imageio.ImageIO", true, Class.class.getClassLoader());
            filePathConstructor = MethodHandles.lookup().findConstructor(fileClass, MethodType.methodType(void.class, String.class, String.class));
            imageioWrite = MethodHandles.lookup().findStatic(imageIoClass, "write", MethodType.methodType(boolean.class, RenderedImage.class, String.class, fileClass));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            Global.getLogger(TextureData.class).log(Level.ERROR, "Failed to reflect when generating normal map for " + key + " (" + type.name() + frameStr + "): " + e.getMessage());
            return null;
        }

        final int prevBoundTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        sprite.bindTexture();
        final int texWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        final int texHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        final int texSize = texWidth * texHeight * 4;
        final ByteBuffer texBuffer = ByteBuffer.allocateDirect(texSize);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texBuffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevBoundTex);

        final int trueWidth = Math.round(texWidth * sprite.getTextureWidth());
        final int trueHeight = Math.round(texHeight * sprite.getTextureHeight());
        final int trueSize = trueWidth * trueHeight * 4;
        final int yDiff = texHeight - trueHeight;
        double avgLuminosity = 0.0;
        double pixelWeight = 0.0;
        final byte pixel[] = new byte[4];
        final byte normalByteData[] = new byte[trueSize];
        final double heightMap[][] = new double[trueWidth][trueHeight];
        for (int y = (texHeight - 1); y >= 0; y--) {
            for (int x = 0; x < texWidth; x++) {
                pixel[0] = texBuffer.get();
                pixel[1] = texBuffer.get();
                pixel[2] = texBuffer.get();
                pixel[3] = texBuffer.get();
                if (x >= trueWidth) {
                    continue;
                }
                if (y < yDiff) {
                    continue;
                }

                final int idx = (x + ((y - yDiff) * trueWidth)) * 4;
                normalByteData[idx] = pixel[3]; // Alpha

                final double alpha = ((pixel[3] < 0) ? (pixel[3] + 256) : pixel[3]) / 255.0;
                final double red = (((pixel[0] < 0) ? (pixel[0] + 256) : pixel[0]) / 255.0) * alpha;
                final double green = (((pixel[1] < 0) ? (pixel[1] + 256) : pixel[1]) / 255.0) * alpha;
                final double blue = (((pixel[2] < 0) ? (pixel[2] + 256) : pixel[2]) / 255.0) * alpha;

                /* Luminosity = Height; quick and dirty */
                heightMap[x][y - yDiff] = Math.max(0.0, Math.min(1.0, Math.sqrt((0.299 * red * red) + (0.587 * green * green) + (0.114 * blue * blue))));
                avgLuminosity += heightMap[x][y - yDiff];
                pixelWeight += alpha;
            }
        }

        /* Apply an exponentiation to bring the average luminosity close to 0.5, for better consistency of results while preserving details and highs/lows */
        if (pixelWeight > 1E-6) {
            avgLuminosity /= pixelWeight;
        }
        double heightExponent = 1.0;
        if (avgLuminosity > 1E-6) {
            heightExponent = Math.log(0.5) / Math.log(avgLuminosity);
        }
        avgLuminosity = 0.0;
        for (int y = (trueHeight - 1); y >= 0; y--) {
            for (int x = 0; x < trueWidth; x++) {
                heightMap[x][y] = Math.pow(heightMap[x][y], heightExponent);
                avgLuminosity += heightMap[x][y];
            }
        }

        /* Calculate average (root mean squared) contrast of the height map, which we can use to bring it to a
         * standarized 0.5 luminosity and ~0.29 contrast.  Then fade (strongly) laterally and fade (not so strongly)
         * longitudinally to approximate the geometry of most ships.
         */
        if (pixelWeight > 1E-6) {
            avgLuminosity /= pixelWeight;
        }
        double rmsContrast = 0.0;
        for (int y = (trueHeight - 1); y >= 0; y--) {
            for (int x = 0; x < trueWidth; x++) {
                final int idx = (x + (y * trueWidth)) * 4;
                final double alpha = (normalByteData[idx] < 0 ? normalByteData[idx] + 256 : normalByteData[idx]) / 255.0;
                final double deviation = heightMap[x][y] - avgLuminosity;
                rmsContrast += deviation * deviation * alpha;
            }
        }
        double heightMult = 1.0;
        if (pixelWeight > 1E-6) {
            rmsContrast = Math.sqrt(rmsContrast / pixelWeight);
            heightMult = (Math.sqrt(3.0) / 6.0) / rmsContrast;
        }
        final double heightBias = 0.5 - (avgLuminosity * heightMult);
        for (int y = (trueHeight - 1); y >= 0; y--) {
            for (int x = 0; x < trueWidth; x++) {
                final double xRatio = (double) ((trueWidth - 1) - x) / (double) (trueWidth - 1);
                final double yRatio = (double) ((trueHeight - 1) - y) / (double) (trueHeight - 1);
                final double crossFade = Math.sqrt(1.0 - (1.75 * Math.abs(xRatio - 0.5))) * Math.sqrt(1.0 - Math.abs(yRatio - 0.5));
                heightMap[x][y] = ((((heightMap[x][y] * heightMult) + heightBias) * crossFade) + (crossFade - 0.25)) / 1.75;
            }
        }

        /* Two-step blur kernel to soften the height map and suggest medium-sized geometries; reasonably fast for decent results */
        final double blurHeightMap[][] = new double[trueWidth][trueHeight];
        for (int x = 0; x < trueWidth; x++) {
            for (int y = (trueHeight - 1); y >= 0; y--) {
                blurHeightMap[x][y] = heightMap[x][y] * BLUR_KERNEL[0];
                for (int i = 1; i < BLUR_KERNEL.length; i++) {
                    final int x1 = Math.max(0, x - i);
                    if (x1 >= 0) {
                        blurHeightMap[x][y] += heightMap[x1][y] * BLUR_KERNEL[i];
                    }
                    final int x2 = Math.min(trueWidth - 1, x + i);
                    if (x2 < trueWidth) {
                        blurHeightMap[x][y] += heightMap[x2][y] * BLUR_KERNEL[i];
                    }
                }
            }
        }
        for (int x = 0; x < trueWidth; x++) {
            for (int y = (trueHeight - 1); y >= 0; y--) {
                heightMap[x][y] = blurHeightMap[x][y] * BLUR_KERNEL[0];
                for (int i = 1; i < BLUR_KERNEL.length; i++) {
                    final int y1 = Math.max(0, y - i);
                    if (y1 >= 0) {
                        heightMap[x][y] += blurHeightMap[x][y1] * BLUR_KERNEL[i];
                    }
                    final int y2 = Math.min(trueHeight - 1, y + i);
                    if (y2 < trueHeight) {
                        heightMap[x][y] += blurHeightMap[x][y2] * BLUR_KERNEL[i];
                    }
                }
            }
        }

        heightMapToNormalMap(heightMap, normalByteData);

        final BufferedImage normalImage = new BufferedImage(trueWidth, trueHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final DataBufferByte normalImageBuffer = (DataBufferByte) normalImage.getRaster().getDataBuffer();
        System.arraycopy(normalByteData, 0, normalImageBuffer.getData(), 0, trueSize);

        final String graphicsLibPath = Global.getSettings().getModManager().getModSpec("shaderLib").getPath();

        try {
            final Object cacheDir = filePathConstructor.invoke(graphicsLibPath, CACHE_DIR);
            final MethodHandle fileExists = MethodHandles.lookup().bind(cacheDir, "exists", MethodType.methodType(boolean.class));
            if (!((boolean) fileExists.invoke())) {
                final MethodHandle fileMkDir = MethodHandles.lookup().bind(cacheDir, "mkdir", MethodType.methodType(boolean.class));
                fileMkDir.invoke();
            }

            final Object normalFile = filePathConstructor.invoke(graphicsLibPath, filePath);
            imageioWrite.invoke((RenderedImage) normalImage, "png", normalFile);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            Global.getLogger(TextureData.class).log(Level.ERROR, "Failed to write file when generating normal map for " + key + " (" + type.name() + frameStr + "): " + e.getMessage());
            return null;
        } catch (Throwable e) {
            Global.getLogger(TextureData.class).log(Level.ERROR, "Failed to write file when generating normal map for " + key + " (" + type.name() + frameStr + "): " + e.getMessage());
            return null;
        }

        normalSprite = Global.getSettings().getSprite(filePath);
        if ((normalSprite != null) && (normalSprite.getHeight() >= 1)) {
            final int texId = normalSprite.getTextureId();
            vramUsageBytes -= Math.round((double) (16 * (Math.round(normalSprite.getWidth() / normalSprite.getTextureWidth())
                    * Math.round(normalSprite.getHeight() / normalSprite.getTextureHeight()))) / 3.0);
            Global.getSettings().unloadTexture(filePath);
            GL11.glDeleteTextures(texId);
        }

        try {
            Global.getSettings().forceMipmapsFor(filePath, true);
            Global.getSettings().loadTexture(filePath);
        } catch (IOException | RuntimeException e) {
            Global.getLogger(TextureData.class).log(Level.ERROR, "Failed to load texture when generating normal map for " + key + " (" + type.name() + frameStr + "): " + e.getMessage());
            return null;
        }

        normalSprite = Global.getSettings().getSprite(filePath);
        if (normalSprite != null) {
            vramUsageBytes += Math.round((double) (16 * (Math.round(normalSprite.getWidth() / normalSprite.getTextureWidth())
                    * Math.round(normalSprite.getHeight() / normalSprite.getTextureHeight()))) / 3.0);
        }

        Global.getLogger(TextureData.class).log(Level.INFO, "Auto-generated normal map for \"" + spritePath + "\" at \"" + filePath + "\"");

        return normalSprite;
    }

    /**
     * Queries if material maps are configured to be loaded or not.
     * <p>
     * @return True if material maps should be loaded.
     * <p>
     * @since 1.10.0
     */
    public static boolean isLoadMaterial() {
        return GraphicsLibSettings.loadMaterial();
    }

    /**
     * Queries if normal maps are configured to be loaded or not.
     * <p>
     * @return True if normal maps should be loaded.
     * <p>
     * @since 1.10.0
     */
    public static boolean isLoadNormal() {
        return GraphicsLibSettings.enableNormal();
    }

    /**
     * Queries if surface maps are configured to be loaded or not.
     * <p>
     * @return True if surface maps should be loaded.
     * <p>
     * @since 1.10.0
     */
    public static boolean isLoadSurface() {
        return GraphicsLibSettings.loadSurface();
    }

    private static String getAnimNameForFrame(String frame0, int frame) {
        String animNameForFrame = frame0;
        for (int i = frame0.length() - 1; i >= 0; i--) {
            if (frame0.charAt(i) == '.') {
                String frameNumStr = "" + ((frame >= 10) ? frame : ("0" + frame));
                animNameForFrame = frame0.substring(0, i - 2) + frameNumStr + frame0.substring(i, frame0.length());
            }
        }

        return animNameForFrame;
    }

    private static String getTextureDataKey(String key, ObjectType type, int frame) {
        final String typeStr;
        switch (type) {
            case SHIP ->
                typeStr = "$$$ship";
            case TURRET ->
                typeStr = "$$$turret";
            case TURRET_BARREL ->
                typeStr = "$$$turretbarrel";
            case TURRET_UNDER ->
                typeStr = "$$$turretunder";
            case TURRET_COVER_SMALL ->
                typeStr = "$$$turretcoversmall";
            case TURRET_COVER_MEDIUM ->
                typeStr = "$$$turretcovermedium";
            case TURRET_COVER_LARGE ->
                typeStr = "$$$turretcoverlarge";
            case HARDPOINT ->
                typeStr = "$$$hardpoint";
            case HARDPOINT_BARREL ->
                typeStr = "$$$hardpointbarrel";
            case HARDPOINT_UNDER ->
                typeStr = "$$$hardpointunder";
            case HARDPOINT_COVER_SMALL ->
                typeStr = "$$$hardpointcoversmall";
            case HARDPOINT_COVER_MEDIUM ->
                typeStr = "$$$hardpointcovermedium";
            case HARDPOINT_COVER_LARGE ->
                typeStr = "$$$hardpointcoverlarge";
            case MISSILE ->
                typeStr = "$$$missile";
            case ASTEROID ->
                typeStr = "$$$asteroid";
            default -> {
                return null;
            }
        }
        if ((type == ObjectType.TURRET) || (type == ObjectType.HARDPOINT)) {
            return key + typeStr + frame;
        } else {
            return key + typeStr;
        }
    }

    private static void heightMapToNormalMap(double heightMap[][], byte normalByteData[]) {
        final int width = heightMap.length;
        final int height = heightMap[0].length;
        final double dz = 0.133333;
        Vector3f normal = new Vector3f();
        for (int y = (height - 1); y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                final int idx = (x + (y * width)) * 4;

                final double tl = heightMap[Math.max(0, x - 1)][Math.max(0, y - 1)];
                final double l = heightMap[Math.max(0, x - 1)][y];
                final double bl = heightMap[Math.max(0, x - 1)][Math.min(height - 1, y + 1)];
                final double t = heightMap[x][Math.max(0, y - 1)];
                final double b = heightMap[x][Math.min(height - 1, y + 1)];
                final double tr = heightMap[Math.min(width - 1, x + 1)][Math.max(0, y - 1)];
                final double r = heightMap[Math.min(width - 1, x + 1)][y];
                final double br = heightMap[Math.min(width - 1, x + 1)][Math.min(height - 1, y + 1)];

                /* Sobel's kernel */
                final double dx = tl + (l * 2.0) + bl - tr - (r * 2.0) - br;
                final double dy = tl + (t * 2.0) + tr - bl - (b * 2.0) - br;
                normal.set((float) dx, (float) dy, (float) dz);
                normal.normalise();

                normalByteData[idx + 3] = (byte) Math.max(0, Math.min(255, Math.round((0.5f + (0.5f * normal.x)) * 255f))); // Red / X
                normalByteData[idx + 2] = (byte) Math.max(0, Math.min(255, Math.round((0.5f + (0.5f * normal.y)) * 255f))); // Green / Y
                normalByteData[idx + 1] = (byte) Math.max(0, Math.min(255, Math.round((0.5f + (0.5f * normal.z)) * 255f))); // Blue / Z
            }
        }
    }

    public static enum TextureDataType {

        MATERIAL_MAP, NORMAL_MAP, SURFACE_MAP
    }

    public static enum ObjectType {

        SHIP,
        TURRET, TURRET_BARREL, TURRET_UNDER,
        TURRET_COVER_SMALL, TURRET_COVER_MEDIUM, TURRET_COVER_LARGE,
        HARDPOINT, HARDPOINT_BARREL, HARDPOINT_UNDER,
        HARDPOINT_COVER_SMALL, HARDPOINT_COVER_MEDIUM, HARDPOINT_COVER_LARGE,
        MISSILE,
        ASTEROID
    }
}
