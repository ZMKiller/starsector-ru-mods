package org.dark.shaders.distortion;

import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;

/**
 * An interface for managing distortion textures in the distortion shader.
 * <p>
 * @author DarkRevenant
 * @since Alpha 1.1
 */
public interface DistortionAPI {

    /**
     * Returns the world-space quantity, in units, to distort by at the maximum distortion level (blue channel at 255).
     * The distortion engine will handle the transformation for you. Note that this function refers to the intensity
     * scale of the distortion texture, not its size. Modify the texture's sprite directly to change its size directly.
     * <p>
     * @return The scaling factor to transform the blue channel by, in world space units.
     * <p>
     * @since Alpha 1.1
     */
    float getIntensity();

    /**
     * Returns the current location of the distortion.
     * <p>
     * @return The current location of the distortion.
     * <p>
     * @since Alpha 1.1
     */
    Vector2f getLocation();

    /**
     * The current facing direction of the distortion, in degrees. Should generally match the sprite's rotation.
     * <p>
     * @return The current facing direction of the distortion.
     * <p>
     * @since Alpha 1.1
     */
    float getFacing();

    /**
     * The sprite used to draw the distortion texture. The red channel corresponds to horizontal distortion vector,
     * while the green channel corresponds to vertical distortion vector. The engine will normalize these values for
     * you. The blue channel corresponds to distortion magnitude. For example, a pure white square as a distortion
     * texture will copy a square of pixels somewhere to the top-right of the screen.
     * <p>
     * @return The sprite used to draw the distortion texture.
     * <p>
     * @since Alpha 1.1
     */
    SpriteAPI getSprite();

    /**
     * Whether the distortion should be flipped in magnitude. This simulates the effect of performing a
     * horizontal-vertical inversion of the red and green color channels. For example, if the distortion normally looks
     * like a sphere, it will instead look like a spherical hole.
     * <p>
     * @return Whether the magnitude should be flipped.
     * <p>
     * @since Alpha 1.1
     */
    boolean isFlipped();

    /**
     * The start of the current visible arc of the distortion, in degrees. Note: the entire shader is visible if the arc
     * spans 0 degrees.
     * <p>
     * @return The start of the current visible arc of the distortion, in degrees.
     * <p>
     * @since Alpha 1.11
     */
    float getArcStart();

    /**
     * The end of the current visible arc of the distortion, in degrees. Note: the entire shader is visible if the arc
     * spans 0 degrees.
     * <p>
     * @return The end of the current visible arc of the distortion, in degrees.
     * <p>
     * @since Alpha 1.11
     */
    float getArcEnd();

    /**
     * The width of the visible arc's edge attenuation, in degrees. Wider attenuation will make the transition smoother.
     * <p>
     * @return The width of the visible arc's edge attenuation, in degrees.
     * <p>
     * @since Beta 1.0
     */
    float getArcAttenuationWidth();

    /**
     * Runs once per frame.
     * <p>
     * @param amount Seconds since last frame.
     * <p>
     * @return True if the distortion object should be destroyed this frame, false if it should not be destroyed this
     *         frame.
     * <p>
     * @since Alpha 1.1
     */
    boolean advance(float amount);
}
