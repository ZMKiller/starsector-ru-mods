package org.dark.shaders.util;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.dark.shaders.util.TextureData.TextureDataType;
import org.lwjgl.util.vector.Vector2f;

/**
 * An interface for managing material/normal/surface-mapped objects in the lighting engine.
 * <p>
 * @since 1.10.0
 * <p>
 * @author DarkRevenant
 */
public interface MapObjectAPI {

    /**
     * Gets the current location of the object.
     * <p>
     * @return The current location of the object.
     * <p>
     * @since 1.10.0
     */
    Vector2f getLocation();

    /**
     * Gets the render radius of the object. This is not exactly the same as the usual getRenderRadius(); make sure to
     * explicitly include the entity's visible radius.
     * <p>
     * @return The render radius of the object.
     * <p>
     * @since 1.10.0
     */
    public float getRenderRadius();

    /**
     * Gets the rotational angle for the normal map. This is used to transform the vector components when the map is
     * being rendered. This should almost always be the in-engine getFacing() angle of the object, minus 90 degrees.
     * <p>
     * @return The rotational angle for the normal map, in degrees.
     * <p>
     * @since 1.10.0
     */
    public float getNormalAngle();

    /**
     * Gets the horizontal flip flag for the normal map. This is almost always false.
     * <p>
     * @return The horizontal flip flag for the normal map.
     * <p>
     * @since 1.10.0
     */
    public boolean getNormalFlipHorizontal();

    /**
     * Gets the vertical flip flag for the normal map. This is almost always false.
     * <p>
     * @return The vertical flip flag for the normal map.
     * <p>
     * @since 1.10.0
     */
    public boolean getNormalFlipVertical();

    /**
     * Gets the magnitude scalar for the normal map.
     * <p>
     * @return The magnitude multiplier for the normal map.
     * <p>
     * @since 1.10.0
     */
    public float getNormalMagnitude();

    /**
     * Reports if a normal map exists for this object. If none exists, different shader settings are used prior to the
     * render() call, which should render the material map or diffuse sprite in place of a normal map.
     * <p>
     * @return Whether a normal map exists for this object.
     * <p>
     * @since 1.10.0
     */
    public boolean hasNormal();

    /**
     * Renders the object with the specified map type.
     * <p>
     * @param layer Will presently *always* be ABOVE_SHIPS_LAYER.
     * @param viewport Same as the usual render() viewport argument.
     * @param type Map type that should be rendered.
     * @param alwaysBind Flag to indicate if the sprite texture should always be bound on this call.
     * <p>
     * @since 1.10.0
     */
    void render(CombatEngineLayers layer, ViewportAPI viewport, TextureDataType type, boolean alwaysBind);
}
