package org.dark.shaders.invert;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.apache.log4j.Level;
import org.dark.shaders.util.ShaderAPI;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

/**
 * <p>
 * @author DarkRevenant
 */
public class InvertShader implements ShaderAPI {

    private boolean active = false;
    private boolean enabled = false;
    private final int[] index = new int[1];
    private int program = 0;
    private boolean validated = false;

    public InvertShader() {
        if (!ShaderLib.areShadersAllowed()) {
            return;
        }

        String vertShader;
        String fragShader;

        try {
            vertShader = Global.getSettings().loadText("data/shaders/invert.vert");
            fragShader = Global.getSettings().loadText("data/shaders/invert.frag");
        } catch (IOException ex) {
            return;
        }

        program = ShaderLib.loadShader(vertShader, fragShader);

        if (program == 0) {
            return;
        }

        GL20.glUseProgram(program);
        index[0] = GL20.glGetUniformLocation(program, "tex");
        GL20.glUniform1i(index[0], 0);
        GL20.glUseProgram(0);

        enabled = true;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!enabled) {
            return;
        }

        for (InputEventAPI event : events) {
            if (event.isConsumed()) {
                continue;
            }

            if (event.isKeyDownEvent() && event.getEventValue() == org.lwjgl.input.Keyboard.KEY_PERIOD) {
                active = !active;
                event.consume();
                break;
            }
        }
    }

    @Override
    public void destroy() {
        if (!enabled) {
            return;
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
    }

    @Override
    public RenderOrder getRenderOrder() {
        return RenderOrder.SCREEN_SPACE;
    }

    @Override
    public void initCombat() {
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
        if (!enabled || !active) {
            return;
        }

        invertScreen();
    }

    private void invertScreen() {
        ShaderLib.beginDraw(program);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

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
                return;
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0);

        ShaderLib.exitDraw();
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
