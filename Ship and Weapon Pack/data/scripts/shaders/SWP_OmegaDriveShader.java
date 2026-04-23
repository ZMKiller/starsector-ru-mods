package data.scripts.shaders;

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

public class SWP_OmegaDriveShader implements ShaderAPI {

    private static boolean active = false;
    private static float degree = 1f;

    public static void setActive(boolean nowActive, float newDegree) {
        active = nowActive;
        degree = newDegree;
    }

    private boolean enabled = false;
    private final int index[] = new int[3];
    private int program = 0;
    private boolean validated = false;

    public SWP_OmegaDriveShader() {
        if (!ShaderLib.areShadersAllowed()) {
            return;
        }

        String vertShader;
        String fragShader;

        try {
            vertShader = Global.getSettings().loadText("data/shaders/omega/omega.vert");
            fragShader = Global.getSettings().loadText("data/shaders/omega/omega.frag");
        } catch (IOException ex) {
            return;
        }

        program = ShaderLib.loadShader(vertShader, fragShader);

        if (program == 0) {
            return;
        }

        GL20.glUseProgram(program);
        index[0] = GL20.glGetUniformLocation(program, "tex");
        index[1] = GL20.glGetUniformLocation(program, "degree");
        index[2] = GL20.glGetUniformLocation(program, "time");
        GL20.glUniform1i(index[0], 0);
        GL20.glUseProgram(0);

        enabled = true;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
    }

    @Override
    public void destroy() {
        if (program != 0) {
            ByteBuffer countbb = ByteBuffer.allocateDirect(4);
            ByteBuffer shadersbb = ByteBuffer.allocateDirect(8);
            IntBuffer count = countbb.asIntBuffer();
            IntBuffer shaders = shadersbb.asIntBuffer();
            GL20.glGetAttachedShaders(program, count, shaders);
            for (int i = 0; i < 2; i++) {
                GL20.glDeleteShader(shaders.get());
            }
            GL20.glDeleteProgram(program);
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
        if (!enabled) {
            return;
        }

        if (!active) {
            return;
        }

        draw();
    }

    private void draw() {
        ShaderLib.beginDraw(program);

        GL20.glUniform1f(index[1], degree); // degree
        GL20.glUniform1f(index[2], Global.getCombatEngine().getTotalElapsedTime(true)); // time

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

        if (!validated) {
            validated = true;

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
