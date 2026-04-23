package org.dark.shaders.light;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * A specialization of LightAPI suited for every case wherein the default, automatic lighting behaviors are sufficient.
 * <p>
 * @author DarkRevenant
 */
public class StandardLight implements LightAPI {

    protected static final Vector2f ZERO = new Vector2f();

    protected static Vector2f computeOffset(Vector2f location, Vector2f offset, float angle) {
        final float angleRads = (float) Math.toRadians(angle);
        final float cos = (float) ShaderLib.fastCos(angleRads);
        final float sin = (float) ShaderLib.fastSin(angleRads);
        final Vector2f dest = new Vector2f((offset.x * cos) - (offset.y * sin), (offset.x * sin) + (offset.y * cos));
        Vector2f.add(dest, location, dest);
        return dest;
    }

    protected float arcEnd = 0f;
    protected float arcStart = 0f;
    protected CombatEntityAPI attach = null;
    protected float autoFadeOutTime = 1f;
    protected BeamAPI beamAttach = null;
    protected BeamAPI beamLink = null;
    protected final Vector3f color = new Vector3f(1f, 1f, 1f);
    protected final Deque<Boolean> damageFrames = new ArrayDeque<>(4);
    protected boolean damageInLastFewFrames = false;
    protected final Vector3f direction;
    protected float height = 200f;
    protected float intensity = 0f;
    protected float intensityFade = 0f;
    protected float intensityMax = 0f;
    protected boolean isFadingIn = false;
    protected boolean isFadingOut = false;
    protected float lifetime = -1f;
    protected boolean linkToEnd = false;
    protected final Vector2f location;
    protected final Vector2f location2;
    protected final Vector2f offset;
    protected float size = 0f;
    protected float specularIntensity = 0f;
    protected float specularMultiplier = 1f;
    protected float superLifetime = 60f;
    protected int type;
    protected final Vector2f velocity;
    protected final Vector2f velocity2;

    /**
     * Constructs a generic light. Note that the light begins with an initial size and intensity of 0.
     * <p>
     * @since Beta 1.02
     */
    public StandardLight() {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        location = new Vector2f();
        location2 = new Vector2f();
        offset = new Vector2f();
        velocity = new Vector2f();
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        type = 0;
    }

    /**
     * Constructs a generic light.
     * <p>
     * @param intensity The desired intensity of the light.
     * @param size The desired size of the light.
     * <p>
     * @since Beta 1.07
     */
    public StandardLight(float intensity, float size) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        location = new Vector2f();
        location2 = new Vector2f();
        offset = new Vector2f();
        velocity = new Vector2f();
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        this.intensity = intensity;
        this.size = size;
        type = 0;
    }

    /**
     * Constructs a point light. Note that the light begins with an initial size and intensity of 0.
     * <p>
     * @param location The desired location for the light.
     * @param velocity The desired velocity for the light.
     * @param offset The desired attachment offset for the light.
     * @param attach The desired light attachment. May be null.
     * <p>
     * @since Beta 1.02
     */
    public StandardLight(Vector2f location, Vector2f velocity, Vector2f offset, CombatEntityAPI attach) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        location2 = new Vector2f();
        this.offset = new Vector2f(offset);
        this.velocity = new Vector2f(velocity);
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        if (attach != null) {
            pAttachTo(attach);
        }
        type = 0;
    }

    /**
     * Constructs a point light.
     * <p>
     * @param location The desired location for the light.
     * @param velocity The desired velocity for the light.
     * @param offset The desired attachment offset for the light.
     * @param attach The desired light attachment. May be null.
     * @param intensity The desired intensity of the light.
     * @param size The desired size of the light.
     * <p>
     * @since Beta 1.07
     */
    public StandardLight(Vector2f location, Vector2f velocity, Vector2f offset, CombatEntityAPI attach, float intensity,
            float size) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        location2 = new Vector2f();
        this.offset = new Vector2f(offset);
        this.velocity = new Vector2f(velocity);
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        if (attach != null) {
            pAttachTo(attach);
        }
        this.intensity = intensity;
        this.size = size;
        type = 0;
    }

    /**
     * Constructs a point light, attached to a beam endpoint. Note that the light begins with an initial size and
     * intensity of 0.
     * <p>
     * @param location The desired location for the light.
     * @param velocity The desired velocity for the light.
     * @param beamLink The desires beam attachment for the light. May be null.
     * @param linkToEnd Whether to link the light to the end of the beam or not. Ignored if beamLink is null.
     * <p>
     * @since Beta 1.02
     */
    public StandardLight(Vector2f location, Vector2f velocity, BeamAPI beamLink, boolean linkToEnd) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        location2 = new Vector2f();
        offset = new Vector2f();
        this.velocity = new Vector2f(velocity);
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        if (beamLink != null) {
            pLinkToBeam(beamLink, linkToEnd);
        }
        type = 0;
    }

    /**
     * Constructs a point light, attached to a beam endpoint.
     * <p>
     * @param location The desired location for the light.
     * @param velocity The desired velocity for the light.
     * @param beamLink The desires beam attachment for the light. May be null.
     * @param linkToEnd Whether to link the light to the end of the beam or not. Ignored if beamLink is null.
     * @param intensity The desired intensity of the light.
     * @param size The desired size of the light.
     * <p>
     * @since Beta 1.07
     */
    public StandardLight(Vector2f location, Vector2f velocity, BeamAPI beamLink, boolean linkToEnd, float intensity,
            float size) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        location2 = new Vector2f();
        offset = new Vector2f();
        this.velocity = new Vector2f(velocity);
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        if (beamLink != null) {
            pLinkToBeam(beamLink, linkToEnd);
        }
        this.intensity = intensity;
        this.size = size;
        type = 0;
    }

    /**
     * Constructs a line light. Note that the light begins with an initial size and intensity of 0.
     * <p>
     * @param location The desired origin location for the light.
     * @param location2 The desired endpoint location for the light.
     * @param velocity The desired velocity for the light's origin.
     * @param velocity2 The desired velocity for the light's endpoint.
     * @param beamAttach The beam attachment for the light. May be null.
     * <p>
     * @since Beta 1.02
     */
    public StandardLight(Vector2f location, Vector2f location2, Vector2f velocity, Vector2f velocity2,
            BeamAPI beamAttach) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        this.location2 = new Vector2f(location2);
        offset = new Vector2f();
        this.velocity = new Vector2f(velocity);
        this.velocity2 = new Vector2f(velocity2);
        direction = new Vector3f(0f, 0f, -1f);
        if (beamAttach != null) {
            pAttachToBeam(beamAttach);
        }
        type = 1;
    }

    /**
     * Constructs a line light.
     * <p>
     * @param location The desired origin location for the light.
     * @param location2 The desired endpoint location for the light.
     * @param velocity The desired velocity for the light's origin.
     * @param velocity2 The desired velocity for the light's endpoint.
     * @param beamAttach The beam attachment for the light. May be null.
     * @param intensity The desired intensity of the light.
     * @param size The desired size of the light.
     * <p>
     * @since Beta 1.07
     */
    public StandardLight(Vector2f location, Vector2f location2, Vector2f velocity, Vector2f velocity2,
            BeamAPI beamAttach, float intensity, float size) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        this.location2 = new Vector2f(location2);
        offset = new Vector2f();
        this.velocity = new Vector2f(velocity);
        this.velocity2 = new Vector2f(velocity2);
        direction = new Vector3f(0f, 0f, -1f);
        if (beamAttach != null) {
            pAttachToBeam(beamAttach);
        }
        this.intensity = intensity;
        this.size = size;
        type = 1;
    }

    /**
     * Constructs a cone light. Note that the light begins with an initial size and intensity of 0.
     * <p>
     * @param location The desired location for the light.
     * @param velocity The desired velocity for the light.
     * @param arcEnd The desired end of the visible arc of the light, in degrees.
     * @param arcStart The desired start of the visible arc of the light, in degrees.
     * <p>
     * @since Beta 1.02
     */
    public StandardLight(Vector2f location, Vector2f velocity, float arcEnd, float arcStart) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        location2 = new Vector2f();
        offset = new Vector2f();
        this.velocity = new Vector2f(velocity);
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        pSetArc(arcStart, arcEnd);
        type = 2;
    }

    /**
     * Constructs a cone light.
     * <p>
     * @param location The desired location for the light.
     * @param velocity The desired velocity for the light.
     * @param arcEnd The desired end of the visible arc of the light, in degrees.
     * @param arcStart The desired start of the visible arc of the light, in degrees.
     * @param intensity The desired intensity of the light.
     * @param size The desired size of the light.
     * <p>
     * @since Beta 1.07
     */
    public StandardLight(Vector2f location, Vector2f velocity, float arcEnd, float arcStart, float intensity, float size) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        this.location = new Vector2f(location);
        location2 = new Vector2f();
        offset = new Vector2f();
        this.velocity = new Vector2f(velocity);
        velocity2 = new Vector2f();
        direction = new Vector3f(0f, 0f, -1f);
        pSetArc(arcStart, arcEnd);
        this.intensity = intensity;
        this.size = size;
        type = 2;
    }

    /**
     * Constructs a directional light. Note that the light begins with an initial intensity of 0.
     * <p>
     * @param direction The desired 3D directional vector for the light.
     * <p>
     * @since Beta 1.02
     */
    public StandardLight(Vector3f direction) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        location = new Vector2f();
        location2 = new Vector2f();
        offset = new Vector2f();
        velocity = new Vector2f();
        velocity2 = new Vector2f();
        this.direction = new Vector3f(direction);
        this.direction.normalise();
        type = 3;
    }

    /**
     * Constructs a directional light.
     * <p>
     * @param direction The desired 3D directional vector for the light.
     * @param intensity The desired intensity of the light.
     * <p>
     * @since Beta 1.07
     */
    public StandardLight(Vector3f direction, float intensity) {
        for (int i = 0; i < 4; i++) {
            damageFrames.add(false);
        }
        location = new Vector2f();
        location2 = new Vector2f();
        offset = new Vector2f();
        velocity = new Vector2f();
        velocity2 = new Vector2f();
        this.direction = new Vector3f(direction);
        this.direction.normalise();
        this.intensity = intensity;
        type = 3;
    }

    /**
     * Runs once per frame.
     * <p>
     * @param amount Seconds since last frame.
     * <p>
     * @return True if the light should be destroyed this frame, false if it should not be destroyed this frame.
     */
    @Override
    public boolean advance(float amount) {
        if (beamLink != null) {
            damageFrames.removeLast();
            damageFrames.addFirst(beamLink.didDamageThisFrame());

            if (damageFrames.contains(true)) {
                damageInLastFewFrames = true;
            }
        }

        if (attach == null && beamAttach == null && beamLink == null) {
            location.translate(velocity.x * amount, velocity.y * amount);
            location2.translate(velocity2.x * amount, velocity2.y * amount);
        }

        if (isFadingOut) {
            intensity -= intensityFade * amount;
        } else if (isFadingIn) {
            intensity += intensityFade * amount;

            if (intensity >= intensityMax) {
                intensity = intensityMax;
                isFadingIn = false;
            }
        } else {
            if (lifetime >= 0f) {
                lifetime -= amount;

                if (lifetime < 0f) {
                    fadeOut(autoFadeOutTime);
                }
            }
        }

        if (superLifetime >= 0f) {
            superLifetime -= amount;

            if (superLifetime < 0f) {
                return true;
            }
        }

        if (type == 3) {
            return intensity <= 0f && specularIntensity <= 0f;
        } else {
            return size <= 0f || intensity <= 0f;
        }
    }

    /**
     * Attaches the light to an entity. Will unlatch the light from any other attachments.
     * <p>
     * @param attach The entity to attach to.
     */
    public void attachTo(CombatEntityAPI attach) {
        this.attach = attach;
        beamAttach = null;
        beamLink = null;
    }

    /**
     * Attaches the light to a beam. Will unlatch the light from any other attachments.
     * <p>
     * @param beamAttach The beam to attach to.
     */
    public void attachToBeam(BeamAPI beamAttach) {
        attach = null;
        this.beamAttach = beamAttach;
        beamLink = beamAttach;
    }

    /**
     * Fades in the light over a period of time. This method will also temporarily zero-out the light's intensity.
     * <p>
     * @param seconds The period of time desired for the light to fade in over. If zero, will immediately complete an
     * existing fade-in.
     */
    public void fadeIn(float seconds) {
        if (seconds <= 0f) {
            /* Cancel fade-in */
            if (isFadingIn) {
                intensity = intensityMax;

                isFadingIn = false;
                isFadingOut = false;
            }
        } else {
            intensityFade = intensity / seconds;

            intensityMax = intensity;
            intensity = 0f;

            isFadingIn = true;
            isFadingOut = false;
        }
    }

    /**
     * Fades the light out over a period of time. Giving a negative or zero value will cause the light to instantly
     * disappear.
     * <p>
     * @param seconds The period of time desired for the light to fade out over. A negative or zero value will cause the
     * light to instantly disappear.
     */
    public void fadeOut(float seconds) {
        if (seconds <= 0f) {
            intensity = 0f;
        } else {
            intensityFade = intensity / seconds;
        }

        isFadingOut = true;
        isFadingIn = false;
    }

    /**
     * The end of the current visible arc of the light, in degrees. Only used for cone-type lights.
     * <p>
     * @return The current facing direction of the light.
     * <p>
     * @since Alpha 1.4
     */
    @Override
    public float getArcEnd() {
        return arcEnd;
    }

    /**
     * The start of the current visible arc of the light, in degrees. Only used for cone-type lights.
     * <p>
     * @return The start of the current visible arc of the light, in degrees.
     * <p>
     * @since Alpha 1.4
     */
    @Override
    public float getArcStart() {
        return arcStart;
    }

    /**
     * Gets the entity that the light is attached to.
     * <p>
     * @return The entity that the light is attached to. Returns null if there is no such attachment.
     */
    public CombatEntityAPI getAttachment() {
        return attach;
    }

    /**
     * Gets the time the light takes to fade out if it is automatically flagged to fade out by the lighting system.
     * <p>
     * @return The time the light takes to fade out if it is automatically flagged to fade out by the lighting system.
     */
    public float getAutoFadeOutTime() {
        return autoFadeOutTime;
    }

    /**
     * Sets the time the light takes to fade out if it is automatically flagged to fade out by the lighting system.
     * <p>
     * @param autoFadeOutTime The time desired for the light to take to fade out if it is automatically flagged to fade
     * out by the lighting system.
     */
    public void setAutoFadeOutTime(float autoFadeOutTime) {
        this.autoFadeOutTime = autoFadeOutTime;
    }

    /**
     * Gets the beam that the light is attached to.
     * <p>
     * @return The beam that the light is attached to. Returns null if there is no such attachment.
     */
    public BeamAPI getBeamAttachment() {
        return beamAttach;
    }

    /**
     * Gets the location that the light is attached to, if it is attached to a beam's origin or endpoint.
     * <p>
     * @return The origin or endpoint of a beam that the light is attached to. Returns null if there is no such
     * attachment.
     */
    public BeamAPI getBeamLink() {
        return beamLink;
    }

    /**
     * Gets the light's color.
     * <p>
     * @return An object whose xyz components correspond to rgb, in terms of values between 0.0 and 1.0.
     */
    @Override
    public Vector3f getColor() {
        return color;
    }

    /**
     * Sets the light to a particular color.
     * <p>
     * @param color The color to set the light to, given by a Color object.
     */
    public void setColor(Color color) {
        this.color.x = color.getRed() / 255f;
        this.color.y = color.getGreen() / 255f;
        this.color.z = color.getBlue() / 255f;
    }

    /**
     * Sets the light to a particular color.
     * <p>
     * @param color The color to set the light to, given by an object whose xyz components correspond to rgb. Uses
     * values between 0.0 and 1.0.
     */
    public void setColor(Vector3f color) {
        this.color.x = color.x;
        this.color.y = color.y;
        this.color.z = color.z;
    }

    /**
     * Gets the light's directional vector. Only used for directional-type lights.
     * <p>
     * @return The light's directional vector.
     * <p>
     * @since Alpha 1.4
     */
    @Override
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * Sets the light's directional vector. Only used for directional-type lights.
     * <p>
     * @param direction The directional vector for the light.
     * <p>
     * @since Alpha 1.4
     */
    public void setDirection(Vector3f direction) {
        this.direction.set(direction);
        this.direction.normalise();
    }

    /**
     * Gets the height of the light, in world-space units. Only used for the purposes of normal maps.
     * <p>
     * @return The world-space height of the light.
     * <p>
     * @since Alpha 1.4
     */
    @Override
    public float getHeight() {
        return height;
    }

    /**
     * Sets the height of the light, in world-space units. Only used for the purposes of normal maps.
     * <p>
     * @param height The world-space height of the light.
     * <p>
     * @since Alpha 1.4
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Gets the light's intensity.
     * <p>
     * @return The light's intensity, in terms of lumens at the center of the light. This value is modulated by the
     * brightness of the beam it is attached to, if any.
     */
    @Override
    public float getIntensity() {
        if (beamLink != null) {
            if (!damageInLastFewFrames && linkToEnd) {
                return 0f;
            } else {
                return intensity * beamLink.getBrightness();
            }
        } else {
            return intensity;
        }
    }

    /**
     * Sets the light's intensity. A value of 1 will increase the luminosity of the pixel at the center of the light by
     * 1 times the pixel's current luminosity (e.g. a luminosity of 0.5 will become 1.0). This formula may change if
     * materials are added in the future.
     * <p>
     * @param intensity The desired intensity of the light.
     */
    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    /**
     * Gives the light a lifetime timer. If the timer runs out, the light will fade or disappear, depending on
     * parameters. Lights last forever by default. Lifetime will only decrement when the light is not fading.
     * <p>
     * @param lifetime The desired lifetime for the light.
     */
    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    /**
     * Gets the current location of the light.
     * <p>
     * @return The current location of the light, with respect to attachments.
     */
    @Override
    public Vector2f getLocation() {
        if (attach != null) {
            if (Float.compare(offset.x, 0f) != 0 || Float.compare(offset.y, 0f) != 0) {
                return computeOffset(attach.getLocation(), offset, attach.getFacing());
            }
            return attach.getLocation();
        } else if (beamAttach != null) {
            return beamAttach.getFrom();
        } else if (beamLink != null) {
            if (linkToEnd) {
                return beamLink.getTo();
            } else {
                return beamLink.getFrom();
            }
        } else {
            return location;
        }
    }

    /**
     * Sets the location of the light. Has no effect if the light is attached to something.
     * <p>
     * @param location The location to set the light to.
     */
    public void setLocation(Vector2f location) {
        this.location.set(location);
    }

    /**
     * Gets the current location of the light's secondary endpoint. Only used for line-type lights.
     * <p>
     * @return The current location of the light's secondary endpoint, with respect to attachments.
     */
    @Override
    public Vector2f getLocation2() {
        if (attach != null) {
            if (Float.compare(offset.x, 0f) != 0 || Float.compare(offset.y, 0f) != 0) {
                return computeOffset(attach.getLocation(), offset, attach.getFacing());
            }
            return attach.getLocation();
        } else if (beamAttach != null) {
            return beamAttach.getTo();
        } else {
            return location2;
        }
    }

    /**
     * Sets the secondary endpoint of the light. Only used for line-type lights. Has no effect if the light is attached
     * to something.
     * <p>
     * @param location The secondary endpoint of the light.
     */
    public void setLocation2(Vector2f location) {
        location2.set(location);
    }

    /**
     * Gets the attachment offset of the light.
     * <p>
     * @return The attachment offset of the light, ignoring attachment rotation.
     */
    public Vector2f getOffset() {
        return offset;
    }

    /**
     * Sets the attachment offset of the light. Note that this will offset both location vectors. Has no effect if the
     * light is not attached to something. Does not work with beam attachments.
     * <p>
     * @param offset The attachment offset to give the light, before attachment rotation.
     */
    public void setOffset(Vector2f offset) {
        this.offset.set(offset);
    }

    /**
     * Gets the remaining time the light has before it fades or disappears, depending on parameters.
     * <p>
     * @return The remaining time the light has before it fades or disappears, depending on parameters.
     */
    public float getRemainingLifetime() {
        return lifetime;
    }

    /**
     * Gets the light's size.
     * <p>
     * @return The light's size, in units.
     */
    @Override
    public float getSize() {
        return size;
    }

    /**
     * Sets the light's size.
     * <p>
     * @param size The size to set the light to, in units.
     */
    public void setSize(float size) {
        this.size = size;
    }

    /**
     * Gets the specular intensity of the light. Only used for directional-type lights.
     * <p>
     * @return The specular intensity of the light.
     * <p>
     * @since Alpha 1.4
     */
    @Override
    public float getSpecularIntensity() {
        return specularIntensity;
    }

    /**
     * Sets the specular intensity of the light. Only used for directional-type lights.
     * <p>
     * @param intensity The specular intensity of the light.
     * <p>
     * @since Alpha 1.4
     */
    public void setSpecularIntensity(float intensity) {
        specularIntensity = intensity;
    }

    /**
     * Gets the specular multiplier of the light. Only used for point-type lights.
     * <p>
     * @return The specular multiplier of the light.
     * <p>
     * @since 1.4.0
     */
    @Override
    public float getSpecularMult() {
        return specularMultiplier;
    }

    /**
     * Sets the specular multiplier of the light. Only used for point-type lights.
     * <p>
     * @param multiplier The specular multiplier of the light.
     * <p>
     * @since 1.4.0
     */
    public void setSpecularMult(float multiplier) {
        specularMultiplier = multiplier;
    }

    /**
     * Gets the light's type.
     * <p>
     * @return The light's current type. Type 0: point light (default). Type 1: line light. Type 2: cone light. Type 3:
     * directional light.
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Sets the light's type.
     * <p>
     * @param type The type of light to set it to. Type 0: point light (default). Type 1: line light. Type 2: cone
     * light. Type 3: directional light.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Gets the current velocity of the light. Returns zero if the light is attached to anything.
     * <p>
     * @return The current velocity of the light.
     * <p>
     * @since Alpha 1.2
     */
    public Vector2f getVelocity() {
        if (attach != null || beamAttach != null || beamLink != null) {
            return ZERO;
        } else {
            return velocity;
        }
    }

    /**
     * Sets the velocity for the light. Has no effect if the light is attached to something.
     * <p>
     * @param velocity The velocity to apply to the light.
     * <p>
     * @since Alpha 1.2
     */
    public void setVelocity(Vector2f velocity) {
        if (attach == null && beamAttach == null && beamLink == null) {
            this.velocity.set(velocity);
        }
    }

    /**
     * Gets the current velocity of the secondary endpoint of the light. Only used for line-type lights. Returns zero if
     * the light is attached to anything.
     * <p>
     * @return The current velocity of the light's secondary endpoint.
     * <p>
     * @since Alpha 1.2
     */
    public Vector2f getVelocity2() {
        if (attach != null || beamAttach != null || beamLink != null) {
            return ZERO;
        } else {
            return velocity2;
        }
    }

    /**
     * Sets the velocity for the secondary endpoint of the light. Only used for line-type lights. Has no effect if the
     * light is attached to something.
     * <p>
     * @param velocity The velocity to apply to the secondary endpoint of the light.
     * <p>
     * @since Alpha 1.2
     */
    public void setVelocity2(Vector2f velocity) {
        if (attach == null && beamAttach == null && beamLink == null) {
            velocity2.set(velocity);
        }
    }

    /**
     * Returns whether the light is fading in or not.
     * <p>
     * @return Whether the light is fading in or not.
     */
    public boolean isFadingIn() {
        return isFadingIn;
    }

    /**
     * Returns whether the light is fading out or not.
     * <p>
     * @return Whether the light is fading out or not.
     */
    public boolean isFadingOut() {
        return isFadingOut;
    }

    /**
     * Attaches the light to the origin or the endpoint of a beam. Will unlatch the light from any other attachments.
     * <p>
     * @param beamLink The beam to attach to the origin or endpoint of.
     * @param linkToEnd Whether the light should be attached to the beam's origin or its endpoint.
     */
    public void linkToBeam(BeamAPI beamLink, boolean linkToEnd) {
        attach = null;
        beamAttach = null;
        this.beamLink = beamLink;
        this.linkToEnd = linkToEnd;
    }

    /**
     * Makes the light permanent. This sets the lifetime of the light to -1 and sets the internal failsafe lifetime to
     * -1 as well, making it truly permanent until destroyed or otherwise made mortal. By default, lights disappear
     * after one minute to avoid "light leaks".
     * <p>
     * @since Alpha 1.62
     */
    public void makePermanent() {
        lifetime = -1f;
        superLifetime = -1f;
    }

    /**
     * Sets the visible arc of the light, in degrees. Only used for cone-type lights.
     * <p>
     * @param start The start of the light's visible arc, in degrees.
     * @param end The end of the light's visible arc, in degrees.
     * <p>
     * @since Alpha 1.4
     */
    public void setArc(float start, float end) {
        arcStart = start;
        arcEnd = end;
        if ((arcStart < -360f) || (arcStart > 360f)) {
            arcStart %= 360f;
        }
        if (arcStart < 0f) {
            arcStart += 360f;
        }
        if ((arcEnd < -360f) || (arcEnd > 360f)) {
            arcEnd %= 360f;
        }
        if (arcEnd < 0f) {
            arcEnd += 360f;
        }
    }

    /**
     * Sets the light to a particular color.
     * <p>
     * @param red The red component to set the light to. Uses a value between 0.0 and 1.0.
     * @param green The green component to set the light to. Uses a value between 0.0 and 1.0.
     * @param blue The blue component to set the light to. Uses a value between 0.0 and 1.0.
     */
    public void setColor(float red, float green, float blue) {
        color.x = red;
        color.y = green;
        color.z = blue;
    }

    /**
     * Unlatches the light from any attachments.
     */
    public void unattach() {
        attach = null;
        beamAttach = null;
        beamLink = null;
    }

    /**
     * Gets the change in intensity over time.
     * <p>
     * @return The rate of change of the intensity of the light.
     * <p>
     * @since 1.8.0
     */
    public float getIntensityFade() {
        return intensityFade;
    }

    /**
     * Sets the change in intensity over time.
     * <p>
     * @param intensityFade The rate of change of intensity to apply to the light. Positive for fade-in, negative for
     * fade-out, zero for no fading.
     * <p>
     * @since 1.8.0
     */
    public void setIntensityFade(float intensityFade) {
        this.intensityFade = Math.abs(intensityFade);
        if (Float.compare(intensityFade, 0f) == 0) {
            isFadingIn = false;
            isFadingOut = false;
        } else if (intensityFade > 0f) {
            isFadingIn = true;
            isFadingOut = false;
        } else {
            isFadingIn = false;
            isFadingOut = true;
        }
    }

    /**
     * Gets the maximum intensity to reach.
     * <p>
     * @return The maximum intensity that the light can reach.
     * <p>
     * @since 1.8.0
     */
    public float getIntensityMax() {
        return intensityMax;
    }

    /**
     * Sets the maximum intensity to reach.
     * <p>
     * @param intensityMax The maximum intensity that the light should reach.
     * <p>
     * @since 1.8.0
     */
    public void setIntensityMax(float intensityMax) {
        this.intensityMax = intensityMax;
    }

    private void pAttachTo(CombatEntityAPI attach) {
        attachTo(attach);
    }

    private void pAttachToBeam(BeamAPI beamAttach) {
        attachToBeam(beamAttach);
    }

    private void pLinkToBeam(BeamAPI beamLink, boolean linkToEnd) {
        linkToBeam(beamLink, linkToEnd);
    }

    private void pSetArc(float start, float end) {
        arcStart = start;
        arcEnd = end;
        if ((arcStart < -360f) || (arcStart > 360f)) {
            arcStart %= 360f;
        }
        if (arcStart < 0f) {
            arcStart += 360f;
        }
        if ((arcEnd < -360f) || (arcEnd > 360f)) {
            arcEnd %= 360f;
        }
        if (arcEnd < 0f) {
            arcEnd += 360f;
        }
    }
}
