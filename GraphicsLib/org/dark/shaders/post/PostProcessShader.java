package org.dark.shaders.post;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.apache.log4j.Level;
import org.dark.shaders.util.GraphicsLibSettings;
import org.dark.shaders.util.ShaderAPI;
import org.dark.shaders.util.ShaderHook;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

/**
 * This implementation of ShaderAPI contains the general full-screen post-process shader. Do not modify.
 * <p>
 * @author DarkRevenant
 * <p>
 * @since Beta 1.07
 */
public class PostProcessShader implements ShaderAPI {

    /**
     * Resets the post-processing shader to the default settings, taking into account color blindness options.
     * <p>
     * @since Beta 1.07
     */
    public static void resetDefaults() {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            ((PostProcessShader) postShader).setDefaultSettings();
        }
    }

    /**
     * Sets the hue shift, saturation, and lightness for the <b>blue</b> color range.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param hue How much, in degrees, to shift the screen's hue by, for the given color range.
     * <p>
     * @param saturation The desired saturation, for the given color range.
     * <p>
     * @param lightness The desired lightness, for the given color range.
     * <p>
     * @since Beta 1.07
     */
    public static void setBlueHSL(boolean post, float hue, float saturation, float lightness) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPost[11], hue, saturation, lightness);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPre[11], hue, saturation, lightness);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the screen's contrast to the given amount.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param amount The desired contrast. 1 is the default; 0 is completely gray.
     * <p>
     * @since Beta 1.07
     */
    public static void setContrast(boolean post, float amount) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[13], amount);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[13], amount);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the hue shift, saturation, and lightness for the <b>green</b> color range.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param hue How much, in degrees, to shift the screen's hue by, for the given color range.
     * <p>
     * @param saturation The desired saturation, for the given color range.
     * <p>
     * @param lightness The desired lightness, for the given color range.
     * <p>
     * @since Beta 1.07
     */
    public static void setGreenHSL(boolean post, float hue, float saturation, float lightness) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPost[9], hue, saturation, lightness);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPre[9], hue, saturation, lightness);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Shifts the screen's hue by the given amount.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param degrees How much, in degrees, to shift the screen's hue by. For reference, 0 degrees is red, 120 degrees
     * is green, and 240 degrees is blue.
     * <p>
     * @since Beta 1.07
     */
    public static void setHueShift(boolean post, float degrees) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[2], degrees / 360f);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[2], degrees / 360f);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the screen's lightness to the given amount.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param amount The desired lightness. 1 is the default; 0 is completely black.
     * <p>
     * @since Beta 1.07
     */
    public static void setLightness(boolean post, float amount) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[5], amount);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[5], amount);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the hue shift, saturation, and lightness for the <b>magenta</b> color range.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param hue How much, in degrees, to shift the screen's hue by, for the given color range.
     * <p>
     * @param saturation The desired saturation, for the given color range.
     * <p>
     * @param lightness The desired lightness, for the given color range.
     * <p>
     * @since Beta 1.07
     */
    public static void setMagentaHSL(boolean post, float hue, float saturation, float lightness) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPost[12], hue, saturation, lightness);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPre[12], hue, saturation, lightness);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Gives the screen some degree of black noise.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param amount The desired screen noise intensity. 0 is the default (off); 1 is completely noisy.
     * <p>
     * @since Beta 1.07
     */
    public static void setNoise(boolean post, float amount) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[14], amount);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[14], amount);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the hue shift, saturation, and lightness for the <b>red</b> color range.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param hue How much, in degrees, to shift the screen's hue by, for the given color range.
     * <p>
     * @param saturation The desired saturation, for the given color range.
     * <p>
     * @param lightness The desired lightness, for the given color range.
     * <p>
     * @since Beta 1.07
     */
    public static void setRedHSL(boolean post, float hue, float saturation, float lightness) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPost[7], hue, saturation, lightness);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPre[7], hue, saturation, lightness);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the screen's color saturation to the given amount.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param amount The desired saturation. 1 is the default; 0 is completely monochrome.
     * <p>
     * @since Beta 1.07
     */
    public static void setSaturation(boolean post, float amount) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[3], amount);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[3], amount);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Gives the screen CRT-style scan lines.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param amount The desired scan line intensity. 0 is the default (off); 1 is completely black.
     * <p>
     * @param interval The interval, given in rows of pixels, by which scan lines are generated. For example, interval 3
     * means that there is one scan line per three rows of pixels.
     * <p>
     * @param width The width, in rows of pixels, of each scan line.
     * <p>
     * @since Beta 1.07
     */
    public static void setScanLines(boolean post, float amount, int interval, int width) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[15], amount);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[16], (float) interval
                        / ShaderLib.getInternalHeight());
                GL20.glUniform1f(((PostProcessShader) postShader).indexPost[17], (float) width
                        / ShaderLib.getInternalHeight());
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[15], amount);
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[16], (float) interval
                        / ShaderLib.getInternalHeight());
                GL20.glUniform1f(((PostProcessShader) postShader).indexPre[17], (float) width
                        / ShaderLib.getInternalHeight());
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the hue shift, saturation, and lightness for the <b>teal</b> color range.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param hue How much, in degrees, to shift the screen's hue by, for the given color range.
     * <p>
     * @param saturation The desired saturation, for the given color range.
     * <p>
     * @param lightness The desired lightness, for the given color range.
     * <p>
     * @since Beta 1.07
     */
    public static void setTealHSL(boolean post, float hue, float saturation, float lightness) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPost[10], hue, saturation, lightness);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPre[10], hue, saturation, lightness);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the hue shift, saturation, and lightness for the <b>yellow</b> color range.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param hue How much, in degrees, to shift the screen's hue by, for the given color range.
     * <p>
     * @param saturation The desired saturation, for the given color range.
     * <p>
     * @param lightness The desired lightness, for the given color range.
     * <p>
     * @since Beta 1.07
     */
    public static void setYellowHSL(boolean post, float hue, float saturation, float lightness) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPost[8], hue, saturation, lightness);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform3f(((PostProcessShader) postShader).indexPre[8], hue, saturation, lightness);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the lightness function to use an exponential curve when lightness is below 1. Note: If lightness is above 1,
     * an exponential curve is always used.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param value Whether to use exponential darkness. Default off.
     * <p>
     * @since Beta 1.07
     */
    public static void useExponentialDarkness(boolean post, boolean value) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1i(((PostProcessShader) postShader).indexPost[6], value ? 1 : 0);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1i(((PostProcessShader) postShader).indexPre[6], value ? 1 : 0);
            }
            GL20.glUseProgram(0);
        }
    }

    /**
     * Sets the saturation function to use an exponential curve when saturation is below 1. Note: If saturation is above
     * 1, an exponential curve is always used.
     * <p>
     * Note: This must be called at minimum one frame <b>after</b> combat is initiated, because the settings are
     * overridden by defaults (such as color blind mode) on the first frame. Note: color blind mode is a post-shader
     * effect.
     * <p>
     * @param post Selects whether the effect should apply to the pre-shader or post-shader. Set to true to affect the
     * entire screen including UI; set to false to exclude the main UI. Effects can be independently stacked/chained
     * between both steps, as each is a separate shader object.
     * <p>
     * @param value Whether to use exponential desaturation. Default off.
     * <p>
     * @since Beta 1.07
     */
    public static void useExponentialDesaturation(boolean post, boolean value) {
        final ShaderAPI postShader = ShaderLib.getShaderAPI(PostProcessShader.class);

        if (postShader instanceof PostProcessShader && postShader.isEnabled()) {
            if (post) {
                GL20.glUseProgram(((PostProcessShader) postShader).programPost);
                GL20.glUniform1i(((PostProcessShader) postShader).indexPost[4], value ? 1 : 0);
            } else {
                GL20.glUseProgram(((PostProcessShader) postShader).programPre);
                GL20.glUniform1i(((PostProcessShader) postShader).indexPre[4], value ? 1 : 0);
            }
            GL20.glUseProgram(0);
        }
    }

    private boolean enabled = false;
    private boolean validatedPost = false;
    private boolean validatedPre = false;
    protected final int[] indexPost = new int[18];
    protected final int[] indexPre = new int[18];
    protected int programPost = 0;
    protected int programPre = 0;

    @SuppressWarnings("UseSpecificCatch")
    public PostProcessShader() {
        if (!ShaderLib.areShadersAllowed()) {
            enabled = false;
            return;
        }

        Global.getLogger(PostProcessShader.class).setLevel(Level.INFO);

        enabled = GraphicsLibSettings.enablePostProcess();
        if (!enabled) {
            return;
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        }

        String vertShader;
        String fragShader;
        try {
            vertShader = Global.getSettings().loadText("data/shaders/post/pre.vert");
            fragShader = Global.getSettings().loadText("data/shaders/post/pre.frag");
        } catch (IOException ex) {
            Global.getLogger(PostProcessShader.class).log(Level.ERROR,
                    "Post Process pre-shader loading error!  Post Processing disabled!"
                    + ex.getMessage());
            enabled = false;
            if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
            }
            return;
        }

        programPre = ShaderLib.loadShader(vertShader, fragShader);

        if (programPre == 0) {
            enabled = false;
            Global.getLogger(PostProcessShader.class).log(Level.ERROR,
                    "Post Process pre-shader compile error!  Post Processing disabled!");
            if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
            }
            return;
        }

        if (!ShaderLib.isAACompatMode()) {
            try {
                vertShader = Global.getSettings().loadText("data/shaders/post/post.vert");
                fragShader = Global.getSettings().loadText("data/shaders/post/post.frag");
            } catch (IOException ex) {
                Global.getLogger(PostProcessShader.class).log(Level.ERROR,
                        "Post Process post-shader loading error!  Post Processing disabled!"
                        + ex.getMessage());
                enabled = false;
                if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                    GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
                }
                return;
            }

            programPost = ShaderLib.loadShader(vertShader, fragShader);

            if (programPost == 0) {
                enabled = false;
                Global.getLogger(PostProcessShader.class).log(Level.ERROR,
                        "Post Process post-shader compile error!  Post Processing disabled!");
                if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                    GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
                }
                return;
            }
        }

        GL20.glUseProgram(programPre);
        indexPre[0] = GL20.glGetUniformLocation(programPre, "screen");
        indexPre[1] = GL20.glGetUniformLocation(programPre, "time");
        indexPre[2] = GL20.glGetUniformLocation(programPre, "hueshift");
        indexPre[3] = GL20.glGetUniformLocation(programPre, "saturation");
        indexPre[4] = GL20.glGetUniformLocation(programPre, "expdesaturation");
        indexPre[5] = GL20.glGetUniformLocation(programPre, "lightness");
        indexPre[6] = GL20.glGetUniformLocation(programPre, "expdarkness");
        indexPre[7] = GL20.glGetUniformLocation(programPre, "redhsl");
        indexPre[8] = GL20.glGetUniformLocation(programPre, "yellowhsl");
        indexPre[9] = GL20.glGetUniformLocation(programPre, "greenhsl");
        indexPre[10] = GL20.glGetUniformLocation(programPre, "tealhsl");
        indexPre[11] = GL20.glGetUniformLocation(programPre, "bluehsl");
        indexPre[12] = GL20.glGetUniformLocation(programPre, "magentahsl");
        indexPre[13] = GL20.glGetUniformLocation(programPre, "contrast");
        indexPre[14] = GL20.glGetUniformLocation(programPre, "noise");
        indexPre[15] = GL20.glGetUniformLocation(programPre, "scanlines");
        indexPre[16] = GL20.glGetUniformLocation(programPre, "scanint");
        indexPre[17] = GL20.glGetUniformLocation(programPre, "scanwidth");
        GL20.glUniform1i(indexPre[0], 0);
        GL20.glUniform1f(indexPre[2], 0f);
        GL20.glUniform1f(indexPre[3], 1f);
        GL20.glUniform1i(indexPre[4], 0);
        GL20.glUniform1f(indexPre[5], 1f);
        GL20.glUniform1i(indexPre[6], 0);
        GL20.glUniform3f(indexPre[7], 0f, 1f, 1f);
        GL20.glUniform3f(indexPre[8], 0f, 1f, 1f);
        GL20.glUniform3f(indexPre[9], 0f, 1f, 1f);
        GL20.glUniform3f(indexPre[10], 0f, 1f, 1f);
        GL20.glUniform3f(indexPre[11], 0f, 1f, 1f);
        GL20.glUniform3f(indexPre[12], 0f, 1f, 1f);
        GL20.glUniform1f(indexPre[13], 1f);
        GL20.glUniform1f(indexPre[14], 0f);
        GL20.glUniform1f(indexPre[15], 0f);
        GL20.glUniform1f(indexPre[16], 3f / ShaderLib.getInternalHeight());
        GL20.glUniform1f(indexPre[17], 1f / ShaderLib.getInternalHeight());
        GL20.glUseProgram(0);

        if (!ShaderLib.isAACompatMode()) {
            GL20.glUseProgram(programPost);
            indexPost[0] = GL20.glGetUniformLocation(programPost, "screen");
            indexPost[1] = GL20.glGetUniformLocation(programPost, "time");
            indexPost[2] = GL20.glGetUniformLocation(programPost, "hueshift");
            indexPost[3] = GL20.glGetUniformLocation(programPost, "saturation");
            indexPost[4] = GL20.glGetUniformLocation(programPost, "expdesaturation");
            indexPost[5] = GL20.glGetUniformLocation(programPost, "lightness");
            indexPost[6] = GL20.glGetUniformLocation(programPost, "expdarkness");
            indexPost[7] = GL20.glGetUniformLocation(programPost, "redhsl");
            indexPost[8] = GL20.glGetUniformLocation(programPost, "yellowhsl");
            indexPost[9] = GL20.glGetUniformLocation(programPost, "greenhsl");
            indexPost[10] = GL20.glGetUniformLocation(programPost, "tealhsl");
            indexPost[11] = GL20.glGetUniformLocation(programPost, "bluehsl");
            indexPost[12] = GL20.glGetUniformLocation(programPost, "magentahsl");
            indexPost[13] = GL20.glGetUniformLocation(programPost, "contrast");
            indexPost[14] = GL20.glGetUniformLocation(programPost, "noise");
            indexPost[15] = GL20.glGetUniformLocation(programPost, "scanlines");
            indexPost[16] = GL20.glGetUniformLocation(programPost, "scanint");
            indexPost[17] = GL20.glGetUniformLocation(programPost, "scanwidth");
            GL20.glUniform1i(indexPost[0], 0);
            GL20.glUniform1f(indexPost[2], 0f);
            GL20.glUniform1f(indexPost[3], 1f);
            GL20.glUniform1i(indexPost[4], 0);
            GL20.glUniform1f(indexPost[5], 1f);
            GL20.glUniform1i(indexPost[6], 0);
            GL20.glUniform3f(indexPost[7], 0f, 1f, 1f);
            GL20.glUniform3f(indexPost[8], 0f, 1f, 1f);
            GL20.glUniform3f(indexPost[9], 0f, 1f, 1f);
            GL20.glUniform3f(indexPost[10], 0f, 1f, 1f);
            GL20.glUniform3f(indexPost[11], 0f, 1f, 1f);
            GL20.glUniform3f(indexPost[12], 0f, 1f, 1f);
            GL20.glUniform1f(indexPost[13], 1f);
            GL20.glUniform1f(indexPost[14], 0f);
            GL20.glUniform1f(indexPost[15], 0f);
            GL20.glUniform1f(indexPost[16], 3f / ShaderLib.getInternalHeight());
            GL20.glUniform1f(indexPost[17], 1f / ShaderLib.getInternalHeight());
            GL20.glUseProgram(0);
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        }

        enabled = true;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!enabled || !ShaderHook.enableShaders) {
            return;
        }

        if (!ShaderLib.isAACompatMode()) {
            draw(true);
        }
    }

    @Override
    public void destroy() {
        if (!enabled) {
            return;
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        }

        if (programPre != 0) {
            ByteBuffer countbb = ByteBuffer.allocateDirect(4);
            ByteBuffer shadersbb = ByteBuffer.allocateDirect(8);
            IntBuffer count = countbb.asIntBuffer();
            IntBuffer shaders = shadersbb.asIntBuffer();
            GL20.glGetAttachedShaders(programPre, count, shaders);
            for (int i = 0; i < 2; i++) {
                GL20.glDeleteShader(shaders.get());
            }
            GL20.glDeleteProgram(programPre);
            programPre = 0;
        }
        if (programPost != 0) {
            ByteBuffer countbb = ByteBuffer.allocateDirect(4);
            ByteBuffer shadersbb = ByteBuffer.allocateDirect(8);
            IntBuffer count = countbb.asIntBuffer();
            IntBuffer shaders = shadersbb.asIntBuffer();
            GL20.glGetAttachedShaders(programPost, count, shaders);
            for (int i = 0; i < 2; i++) {
                GL20.glDeleteShader(shaders.get());
            }
            GL20.glDeleteProgram(programPost);
            programPost = 0;
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        }
    }

    @Override
    public RenderOrder getRenderOrder() {
        return RenderOrder.SCREEN_SPACE;
    }

    @Override
    public void initCombat() {
        setDefaultSettings();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void renderInScreenCoords(ViewportAPI viewport) {
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (!enabled) {
            return;
        }

        draw(false);
    }

    private void draw(boolean post) {
        if (ShaderLib.isAACompatMode() && post) {
            return;
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        }
        if (post) {
            ShaderLib.beginDraw(programPost);
        } else {
            ShaderLib.beginDraw(programPre);
        }

        if (post) {
            GL20.glUniform1f(indexPost[1], Global.getCombatEngine().getTotalElapsedTime(true) + 1000f
                    * (float) Math.random()); // time
        } else {
            GL20.glUniform1f(indexPre[1], Global.getCombatEngine().getTotalElapsedTime(true) + 1000f
                    * (float) Math.random()); // time
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

        if (post) {
            if (!validatedPost) {
                if (!ShaderLib.VALIDATE_EVERY_FRAME) {
                    validatedPost = true;
                }

                // This stuff here is for AMD compatability, normally it would be way back in the shader loader
                GL20.glValidateProgram(programPost);
                if (GL20.glGetProgrami(programPost, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                    Global.getLogger(ShaderLib.class).log(Level.ERROR, ShaderLib.getProgramLogInfo(programPost));
                    ShaderLib.exitDraw();
                    enabled = false;
                    if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                        GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
                    }
                    return;
                }
            }
        } else {
            if (!validatedPre) {
                if (!ShaderLib.VALIDATE_EVERY_FRAME) {
                    validatedPre = true;
                }

                // This stuff here is for AMD compatability, normally it would be way back in the shader loader
                GL20.glValidateProgram(programPre);
                if (GL20.glGetProgrami(programPre, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                    Global.getLogger(ShaderLib.class).log(Level.ERROR, ShaderLib.getProgramLogInfo(programPre));
                    ShaderLib.exitDraw();
                    enabled = false;
                    if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                        GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
                    }
                    return;
                }
            }
        }
        GL11.glDisable(GL11.GL_BLEND);

        ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0);

        ShaderLib.exitDraw();
        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        }
    }

    private void setDefaultSettings() {
        if (!enabled) {
            return;
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        }

        GL20.glUseProgram(programPost);
        switch (GraphicsLibSettings.colorBlindnessMode()) {
            case 1: // Protanomaly (hard to see red)
                GL20.glUniform1f(indexPost[3], 1f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], -0.1f, 1.3f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0.05f, 1f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], 0.2f, 1.6f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0.05f, 1.0f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0f, 1f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1f); // contrast
                break;
            case 2: // Deuteranomaly (hard to see green)
                GL20.glUniform1f(indexPost[3], 1f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], -0.05f, 0.5f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0.1f, 0.75f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], 0.25f, 1.6f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0.1f, 1.0f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0f, 1f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1f); // contrast
                break;
            case 3: // Tritanomaly (hard to see blue)
                GL20.glUniform1f(indexPost[3], 1f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], -0.1f, 1f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0f, 1f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], -0.1f, 1f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0f, 1.3f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0f, 1.6f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1.3f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1f); // contrast
                break;
            case 4: // Protanopia (cannot see red)
                GL20.glUniform1f(indexPost[3], 1f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], -0.1f, 1.3f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0.2f, 1f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], 0.3f, 1.6f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0.1f, 0.75f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0f, 1f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1f); // contrast
                break;
            case 5: // Deuteranopia (cannot see green)
                GL20.glUniform1f(indexPost[3], 1f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], -0.05f, 1f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0.3f, 1f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], 0.25f, 1.6f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0.1f, 0.75f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0f, 1f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1f); // contrast
                break;
            case 6: // Tritanopia (cannot see blue)
                GL20.glUniform1f(indexPost[3], 1f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], -0.1f, 1f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0f, 1f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], -0.1f, 1f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0f, 1.3f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0.1f, 1.6f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1.3f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1f); // contrast
                break;
            case 7: // Achromatopsia (cannot see any color)
                GL20.glUniform1f(indexPost[3], 2f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], 0f, 1f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0f, 1f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], 0f, 1f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0f, 1f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0f, 1f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1.25f); // contrast
                break;
            case 0: // Trichromancy (not color blind)
            default:
                GL20.glUniform1f(indexPost[3], 1f); // saturation
                GL20.glUniform1f(indexPost[5], 1f); // lightness
                GL20.glUniform3f(indexPost[7], 0f, 1f, 1f); // redhsl
                GL20.glUniform3f(indexPost[8], 0f, 1f, 1f); // yellowhsl
                GL20.glUniform3f(indexPost[9], 0f, 1f, 1f); // greenhsl
                GL20.glUniform3f(indexPost[10], 0f, 1f, 1f); // tealhsl
                GL20.glUniform3f(indexPost[11], 0f, 1f, 1f); // bluehsl
                GL20.glUniform3f(indexPost[12], 0f, 1f, 1f); // magentahsl
                GL20.glUniform1f(indexPost[13], 1f); // contrast
                break;
        }
        GL20.glUniform1f(indexPost[2], 0f); // hueshift
        GL20.glUniform1i(indexPost[4], 0); // expdesaturation
        GL20.glUniform1i(indexPost[6], 1); // expdarkness
        GL20.glUniform1f(indexPost[14], 0f); // noise
        GL20.glUniform1f(indexPost[15], 0f); // scanlines
        GL20.glUniform1f(indexPost[16], 3f / ShaderLib.getInternalHeight()); // scanint
        GL20.glUniform1f(indexPost[17], 1f / ShaderLib.getInternalHeight()); // scanwidth
        GL20.glUseProgram(0);

        GL20.glUseProgram(programPre);
        GL20.glUniform1f(indexPre[2], 0f); // hueshift
        GL20.glUniform1f(indexPre[3], 1f); // saturation
        GL20.glUniform1i(indexPre[4], 0); // expdesaturation
        GL20.glUniform1f(indexPre[5], 1f); // lightness
        GL20.glUniform1i(indexPre[6], 1); // expdarkness
        GL20.glUniform3f(indexPre[7], 0f, 1f, 1f); // redhsl
        GL20.glUniform3f(indexPre[8], 0f, 1f, 1f); // yellowhsl
        GL20.glUniform3f(indexPre[9], 0f, 1f, 1f); // greenhsl
        GL20.glUniform3f(indexPre[10], 0f, 1f, 1f); // tealhsl
        GL20.glUniform3f(indexPre[11], 0f, 1f, 1f); // bluehsl
        GL20.glUniform3f(indexPre[12], 0f, 1f, 1f); // magentahsl
        GL20.glUniform1f(indexPre[13], 1f); // contrast
        GL20.glUniform1f(indexPre[14], 0f); // noise
        GL20.glUniform1f(indexPre[15], 0f); // scanlines
        GL20.glUniform1f(indexPre[16], 3f / ShaderLib.getInternalHeight()); // scanint
        GL20.glUniform1f(indexPre[17], 1f / ShaderLib.getInternalHeight()); // scanwidth
        GL20.glUseProgram(0);

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        }
    }

    @Override
    public CombatEngineLayers getCombatLayer() {
        return CombatEngineLayers.JUST_BELOW_WIDGETS;
    }

    @Override
    public boolean isCombat() {
        return false;
    }
}
