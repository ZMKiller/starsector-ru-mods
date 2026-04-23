package org.dark.shaders.distortion;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Level;
import org.dark.shaders.util.GraphicsLibSettings;
import org.dark.shaders.util.ShaderAPI;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.util.vector.Vector2f;

/**
 * This implementation of ShaderAPI contains the Starsector General Distortion Engine. Do not modify.
 * <p>
 * @author DarkRevenant
 * @since Alpha 1.1
 */
public class DistortionShader implements ShaderAPI {

    private static final String DATA_KEY = "shaderlib_DistortionShader";

    private static final Comparator<DistortionAPI> DISTORTIONSIZE = (DistortionAPI distortion1, DistortionAPI distortion2) -> {
        final float distortion1factor = distortion1.getIntensity() * Math.max(distortion1.getSprite().getWidth(),
                distortion1.getSprite().getHeight());
        final float distortion2factor = distortion2.getIntensity() * Math.max(distortion2.getSprite().getWidth(),
                distortion2.getSprite().getHeight());
        if (distortion1factor > distortion2factor) {
            return -1;
        } else if (distortion1factor < distortion2factor) {
            return 1;
        } else {
            return 0;
        }
    };

    /**
     * Adds a distortion object to the rendering list. This function will do nothing if the distortion shader is not
     * enabled.
     * <p>
     * Does not check for duplicates.
     * <p>
     * @param distortion The distortion object to add.
     * <p>
     * @since Alpha 1.1
     */
    public static void addDistortion(DistortionAPI distortion) {
        final ShaderAPI distortionShader = ShaderLib.getShaderAPI(DistortionShader.class);

        if (distortionShader instanceof DistortionShader && distortionShader.isEnabled()) {
            if (distortion != null) {
                LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
                if (localData == null) {
                    return;
                }
                final List<DistortionAPI> distortions = localData.distortions;
                if (distortions != null) {
                    distortions.add(distortion);
                }
            }
        }
    }

    /**
     * Forcibly removes a distortion object from the rendering list. This function will do nothing if the distortion
     * shader is not enabled.
     * <p>
     * @param distortion The distortion object to remove.
     * <p>
     * @since Beta 1.03
     */
    public static void removeDistortion(DistortionAPI distortion) {
        final ShaderAPI distortionShader = ShaderLib.getShaderAPI(DistortionShader.class);

        if (distortionShader instanceof DistortionShader && distortionShader.isEnabled()) {
            if (distortion != null) {
                LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
                if (localData == null) {
                    return;
                }
                final List<DistortionAPI> distortions = localData.distortions;
                if (distortions != null) {
                    distortions.remove(distortion);
                }
            }
        }
    }

    private boolean enabled = false;
    private final int[] index = new int[4];
    private final int[] indexAux = new int[7];
    private int program = 0;
    private int programAux = 0;
    private boolean validated = false;
    private boolean validatedAux = false;

    @SuppressWarnings("UseSpecificCatch")
    public DistortionShader() {
        if (!ShaderLib.areShadersAllowed() || !ShaderLib.areBuffersAllowed()) {
            enabled = false;
            return;
        }

        // Built-in reloader
        // Cheating a bit, but nobody cares
        RippleDistortion.pathsSet = false;
        WaveDistortion.pathsSet = false;

        Global.getLogger(DistortionShader.class).setLevel(Level.INFO);

        enabled = GraphicsLibSettings.enableDistortion();
        if (!enabled) {
            return;
        }

        String vertShader;
        String fragShader;
        String vertShaderAux;
        String fragShaderAux;

        try {
            vertShader = Global.getSettings().loadText("data/shaders/distortion/distortion.vert");
            fragShader = Global.getSettings().loadText("data/shaders/distortion/distortion.frag");
            vertShaderAux = Global.getSettings().loadText("data/shaders/distortion/2dtangent.vert");
            fragShaderAux = Global.getSettings().loadText("data/shaders/distortion/2dtangent.frag");
        } catch (IOException ex) {
            enabled = false;
            return;
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        }

        program = ShaderLib.loadShader(vertShader, fragShader);
        programAux = ShaderLib.loadShader(vertShaderAux, fragShaderAux);

        if (program == 0 || programAux == 0) {
            enabled = false;

            if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
            }
            return;
        }

        GL20.glUseProgram(program);
        index[0] = GL20.glGetUniformLocation(program, "tex");
        index[1] = GL20.glGetUniformLocation(program, "distort");
        index[2] = GL20.glGetUniformLocation(program, "screen");
        index[3] = GL20.glGetUniformLocation(program, "norm");
        GL20.glUniform1i(index[0], 0);
        GL20.glUniform1i(index[1], 1);
        GL20.glUniform4f(index[2], ShaderLib.getInternalWidth(), ShaderLib.getInternalHeight(), ShaderLib.getVisibleU(),
                ShaderLib.getVisibleV());
        GL20.glUseProgram(0);

        GL20.glUseProgram(programAux);
        indexAux[0] = GL20.glGetUniformLocation(programAux, "tex");
        indexAux[1] = GL20.glGetUniformLocation(programAux, "facing");
        indexAux[2] = GL20.glGetUniformLocation(programAux, "scale");
        indexAux[3] = GL20.glGetUniformLocation(programAux, "norm");
        indexAux[4] = GL20.glGetUniformLocation(programAux, "flip");
        indexAux[5] = GL20.glGetUniformLocation(programAux, "arc");
        indexAux[6] = GL20.glGetUniformLocation(programAux, "attwidth");
        GL20.glUniform1i(indexAux[0], 0);
        GL20.glUseProgram(0);

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        }

        enabled = true;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!enabled) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        final List<DistortionAPI> distortions
                = ((LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY)).distortions;

        if (!engine.isPaused()) {
            final Iterator<DistortionAPI> iter = distortions.iterator();
            while (iter.hasNext()) {
                final DistortionAPI distortion = iter.next();

                if (distortion.advance(amount)) {
                    iter.remove();
                }
            }
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

        if (program != 0) {
            final ByteBuffer countbb = ByteBuffer.allocateDirect(4);
            final ByteBuffer shadersbb = ByteBuffer.allocateDirect(8);
            final IntBuffer count = countbb.asIntBuffer();
            final IntBuffer shaders = shadersbb.asIntBuffer();
            GL20.glGetAttachedShaders(program, count, shaders);
            for (int i = 0; i < 2; i++) {
                GL20.glDeleteShader(shaders.get());
            }
            GL20.glDeleteProgram(program);
            program = 0;
        }
        if (programAux != 0) {
            final ByteBuffer countbb = ByteBuffer.allocateDirect(4);
            final ByteBuffer shadersbb = ByteBuffer.allocateDirect(8);
            final IntBuffer count = countbb.asIntBuffer();
            final IntBuffer shaders = shadersbb.asIntBuffer();
            GL20.glGetAttachedShaders(programAux, count, shaders);
            for (int i = 0; i < 2; i++) {
                GL20.glDeleteShader(shaders.get());
            }
            GL20.glDeleteProgram(programAux);
            programAux = 0;
        }

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        }
    }

    @Override
    public RenderOrder getRenderOrder() {
        return RenderOrder.DISTORTED_SPACE;
    }

    @Override
    public void initCombat() {
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
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

        CombatEngineAPI engine = Global.getCombatEngine();

        final List<DistortionAPI> distortions = ((LocalData) engine.getCustomData().get(DATA_KEY)).distortions;

        if (!distortions.isEmpty()) {
            Collections.sort(distortions, DISTORTIONSIZE);
            drawDistortion(viewport);
        }
    }

    private void drawDistortion(ViewportAPI viewport) {
        CombatEngineAPI engine = Global.getCombatEngine();

        final List<DistortionAPI> distortions = ((LocalData) engine.getCustomData().get(DATA_KEY)).distortions;

        if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        }

        GL20.glUseProgram(programAux);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        if (ShaderLib.useBufferCore()) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, ShaderLib.getAuxiliaryBufferId());
        } else if (ShaderLib.useBufferARB()) {
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, ShaderLib.getAuxiliaryBufferId());
        } else {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                    ShaderLib.getAuxiliaryBufferId());
        }

        GL11.glViewport(0, 0, (int) (Global.getSettings().getScreenWidthPixels() * Display.getPixelScaleFactor()),
                (int) (Global.getSettings().getScreenHeightPixels()
                * Display.getPixelScaleFactor()));

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(viewport.getLLX(), viewport.getLLX() + viewport.getVisibleWidth(), viewport.getLLY(),
                viewport.getLLY() + viewport.getVisibleHeight(),
                -2000, 2000);

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glColorMask(true, true, true, true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        float maxScale = 0;
        float minScale = 0;
        int distortionCount = 0;
        ListIterator<DistortionAPI> iter = distortions.listIterator();
        final int maximumDistortions = GraphicsLibSettings.maximumDistortions();
        while (iter.hasNext()) {
            final DistortionAPI distortion = iter.next();
            float scale = ShaderLib.unitsToUV(distortion.getIntensity());

            if (scale > maxScale) {
                maxScale = scale;
            }
            if (scale < minScale) {
                minScale = scale;
            }

            distortionCount++;
            if (distortionCount >= maximumDistortions) {
                break;
            }
        }

        final Vector2f normS = ShaderLib.getTextureDataNormalization(minScale, maxScale);

        iter = distortions.listIterator(distortionCount);
        distortionCount = 0;
        while (iter.hasPrevious()) {
            final DistortionAPI distortion = iter.previous();
            final Vector2f location = distortion.getLocation();
            final SpriteAPI sprite = distortion.getSprite();

            if (location == null || sprite == null || !ShaderLib.isOnScreen(location, Math.max(sprite.getHeight(), sprite.getWidth()))) {
                continue;
            }

            final float scale = ShaderLib.unitsToUV(Math.max(distortion.getIntensity(), 0f));

            GL20.glUniform1f(indexAux[1], distortion.getFacing()); // facing
            GL20.glUniform1f(indexAux[2], scale); // scale
            GL20.glUniform2f(indexAux[3], normS.x, normS.y); // norm
            GL20.glUniform1f(indexAux[4], distortion.isFlipped() ? -1f : 1f); // flip
            GL20.glUniform2f(indexAux[5], (float) Math.toRadians(distortion.getArcStart()), (float) Math.toRadians(distortion.getArcEnd())); // arc
            GL20.glUniform1f(indexAux[6], (float) Math.toRadians(distortion.getArcAttenuationWidth())); // attwidth

            if (!validatedAux) {
                if (!ShaderLib.VALIDATE_EVERY_FRAME) {
                    validatedAux = true;
                }

                // This stuff here is for AMD compatability, normally it would be way back in the shader loader
                GL20.glValidateProgram(programAux);
                if (GL20.glGetProgrami(programAux, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                    Global.getLogger(ShaderLib.class).log(Level.ERROR, ShaderLib.getProgramLogInfo(programAux));
                    ShaderLib.exitDraw();
                    enabled = false;
                    if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                        GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
                    }
                    return;
                }
            }

            sprite.renderAtCenter(location.x, location.y);

            distortionCount++;
            if (distortionCount >= maximumDistortions) {
                break;
            }
        }

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        if (ShaderLib.useBufferCore()) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        } else if (ShaderLib.useBufferARB()) {
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, 0);
        } else {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }
        GL11.glPopAttrib();

        ShaderLib.beginDraw(program);

        GL20.glUniform2f(index[3], normS.x, normS.y); // norm

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getAuxiliaryBufferTexture());

        if (!validated) {
            if (!ShaderLib.VALIDATE_EVERY_FRAME) {
                validated = true;
            }

            // This stuff here is for AMD compatability, normally it would be way back in the shader loader
            GL20.glValidateProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                Global.getLogger(ShaderLib.class).log(Level.ERROR, ShaderLib.getProgramLogInfo(program));
                ShaderLib.exitDraw();
                enabled = false;
                if (ShaderLib.DEBUG_CALLBACK_NO_VANILLA) {
                    GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
                }
                return;
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0);

        ShaderLib.exitDraw();
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
        return true;
    }

    private static final class LocalData {

        final List<DistortionAPI> distortions = new LinkedList<>();
    }
}
