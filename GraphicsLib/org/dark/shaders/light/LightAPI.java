package org.dark.shaders.light;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * An interface for managing lights in the lighting engine.
 * <p>
 * @author DarkRevenant
 */
public interface LightAPI {

    /**
     * Gets the current location of the light.
     * <p>
     * @return The current location of the light.
     */
    Vector2f getLocation();

    /**
     * Gets the current location of the light's secondary endpoint. Only used for line-type lights.
     * <p>
     * @return The current location of the light's secondary endpoint.
     */
    Vector2f getLocation2();

    /**
     * The start of the current visible arc of the light, in degrees. Only used for cone-type lights.
     * <p>
     * @return The start of the current visible arc of the light, in degrees.
     * <p>
     * @since Alpha 1.4
     */
    float getArcStart();

    /**
     * The end of the current visible arc of the light, in degrees. Only used for cone-type lights.
     * <p>
     * @return The current facing direction of the light.
     * <p>
     * @since Alpha 1.4
     */
    float getArcEnd();

    /**
     * Gets the light's directional vector. Only used for directional-type lights.
     * <p>
     * @return The light's directional vector.
     * <p>
     * @since Alpha 1.4
     */
    Vector3f getDirection();

    /**
     * Gets the specular intensity of the light. Only used for directional-type lights.
     * <p>
     * @return The specular intensity of the light.
     * <p>
     * @since Alpha 1.4
     */
    float getSpecularIntensity();

    /**
     * Gets the specular multiplier of the light. Only used for point-type lights.
     * <p>
     * @return The specular multiplier of the light.
     * <p>
     * @since 1.4.0
     */
    float getSpecularMult();

    /**
     * Gets the height of the light, in world-space units. Only used for the purposes of normal maps.
     * <p>
     * @return The world-space height of the light.
     * <p>
     * @since Alpha 1.4
     */
    float getHeight();

    /**
     * Gets the light's color.
     * <p>
     * @return An object whose xyz components correspond to rgb, in terms of values between 0.0 and 1.0.
     */
    Vector3f getColor();

    /**
     * Gets the light's size.
     * <p>
     * @return The light's size, in units.
     */
    float getSize();

    /**
     * Gets the light's intensity.
     * <p>
     * @return The light's intensity, in terms of lumens at the center of the light.
     */
    float getIntensity();

    /**
     * Gets the light's type.
     * <p>
     * @return The light's current type. Type 0: point light (default). Type 1: line light. Type 2: cone light. Type 3:
     *         directional light.
     */
    int getType();

    /**
     * Runs once per frame.
     * <p>
     * @param amount Seconds since last frame.
     * <p>
     * @return True if the light should be destroyed this frame, false if it should not be destroyed this frame.
     * <p>
     * @since Alpha 1.2
     */
    boolean advance(float amount);
}
