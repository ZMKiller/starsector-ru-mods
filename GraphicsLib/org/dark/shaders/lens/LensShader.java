package org.dark.shaders.lens;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Level;
import org.dark.shaders.util.ShaderAPI;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBTextureRg;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;

/**
 * Example lensing shader. Will be expanded to be usable as a ship system, later.
 * <p>
 * @author DarkRevenant
 */
public class LensShader implements ShaderAPI {

    public static final Comparator<CombatEntityAPI> ENTITYSIZE = (CombatEntityAPI entity1, CombatEntityAPI entity2) -> {
        if (entity1.getCollisionRadius() > entity2.getCollisionRadius()) {
            return -1;
        } else if (entity1.getCollisionRadius() < entity2.getCollisionRadius()) {
            return 1;
        } else {
            return 0;
        }
    };

    private static final String DATA_KEY = "shaderlib_LensShader";

    private static final int MAX_LENSES = 21;

    private boolean active = false;
    private final FloatBuffer dataBuffer = BufferUtils.createFloatBuffer(64);
    private final FloatBuffer dataBufferPre = BufferUtils.createFloatBuffer(64);
    private boolean enabled = false;
    private final int[] index = new int[7];
    private int program = 0;
    private int rippleTex = 0;
    private boolean validated = false;

    public LensShader() {
        if (!ShaderLib.areShadersAllowed()) {
            return;
        }

        String vertShader;
        String fragShader;

        try {
            vertShader = Global.getSettings().loadText("data/shaders/lens.vert");
            fragShader = Global.getSettings().loadText("data/shaders/lens.frag");
        } catch (IOException ex) {
            return;
        }

        program = ShaderLib.loadShader(vertShader, fragShader);

        if (program == 0) {
            return;
        }

        rippleTex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, rippleTex);
        if (ShaderLib.useBufferCore()) {
            GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL30.GL_R32F, 64, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT,
                    (java.nio.ByteBuffer) null);
        } else {
            GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, ARBTextureRg.GL_R32F, 64, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT,
                    (java.nio.ByteBuffer) null);
        }

        GL20.glUseProgram(program);
        index[0] = GL20.glGetUniformLocation(program, "tex");
        index[1] = GL20.glGetUniformLocation(program, "data");
        index[2] = GL20.glGetUniformLocation(program, "trans");
        index[3] = GL20.glGetUniformLocation(program, "intensity");
        index[4] = GL20.glGetUniformLocation(program, "size");
        index[5] = GL20.glGetUniformLocation(program, "sizeNormalization");
        index[6] = GL20.glGetUniformLocation(program, "normalization");
        GL20.glUniform1i(index[0], 0);
        GL20.glUniform1i(index[1], 1);
        GL20.glUniform1f(index[2], ShaderLib.getSquareTransform());
        GL20.glUniform1f(index[3], 1f);
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

            if (event.isKeyDownEvent() && event.getEventValue() == org.lwjgl.input.Keyboard.KEY_COMMA) {
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
        if (rippleTex != 0) {
            GL11.glDeleteTextures(rippleTex);
            rippleTex = 0;
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
        if (!enabled || !active) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        final List<ShipAPI> ships = ((LocalData) engine.getCustomData().get(DATA_KEY)).ships;

        // Add ships to the ripple system
        final List<ShipAPI> allShips = engine.getShips();
        for (ShipAPI ship : allShips) {
            if (ship.isHulk()) {
                continue;
            }

            if (ships.contains(ship)) {
                continue;
            }

            ships.add(ship);
        }

        // Remove dead ships from the ripple system
        final Iterator<ShipAPI> iter = ships.iterator();
        while (iter.hasNext()) {
            final ShipAPI ship = iter.next();

            if (ship.isHulk() || !engine.isEntityInPlay(ship)) {
                iter.remove();
            }
        }

        if (!ships.isEmpty()) {
            Collections.sort(ships, ENTITYSIZE);
            drawRipples();
        }
    }

    private void drawRipples() {
        ShaderLib.beginDraw(program);

        CombatEngineAPI engine = Global.getCombatEngine();

        final List<ShipAPI> ships = ((LocalData) engine.getCustomData().get(DATA_KEY)).ships;

        // Load all the data into a 1-dimensional texture
        Vector2f maxCoords = null;
        Vector2f minCoords = null;
        float maxSize = 0;

        int lenses = 0;
        for (ShipAPI ship : ships) {
            final Vector2f shipLocation = new Vector2f(ship.getLocation());

            if (!ShaderLib.isOnScreen(shipLocation, ship.getCollisionRadius())) {
                continue;
            }

            final Vector2f coords = ShaderLib.transformScreenToUV(ShaderLib.transformWorldToScreen(shipLocation));
            final float size = ShaderLib.unitsToUV(ship.getCollisionRadius());

            if (maxCoords == null || minCoords == null) {
                maxCoords = new Vector2f(coords);
                minCoords = new Vector2f(coords);
            } else {
                if (coords.x > maxCoords.x) {
                    maxCoords.x = coords.x;
                } else if (coords.x < minCoords.x) {
                    minCoords.x = coords.x;
                }
                if (coords.y > maxCoords.y) {
                    maxCoords.y = coords.y;
                } else if (coords.y < minCoords.y) {
                    minCoords.y = coords.y;
                }
            }
            if (size > maxSize) {
                maxSize = size;
            }

            dataBufferPre.put(coords.x);
            dataBufferPre.put(coords.y);
            dataBufferPre.put(size);

            lenses++;
            if (lenses >= MAX_LENSES) {
                break;
            }
        }

        // Exit if there is nothing to do
        if (lenses <= 0 || minCoords == null || maxCoords == null) {
            ShaderLib.exitDraw();
            return;
        }

        // Normalize the data to fit in a [0,1] clamp
        final Vector2f normX = ShaderLib.getTextureDataNormalization(minCoords.x, maxCoords.x);
        final Vector2f normY = ShaderLib.getTextureDataNormalization(minCoords.y, maxCoords.y);
        final Vector2f normS = ShaderLib.getTextureDataNormalization(0f, maxSize);

        dataBufferPre.flip();
        for (int i = 0; i < lenses * 3; i++) {
            int pos = i % 3;
            switch (pos) {
                case 0 ->
                    dataBuffer.put((dataBufferPre.get() - normX.y) / normX.x);
                case 1 ->
                    dataBuffer.put((dataBufferPre.get() - normY.y) / normY.x);
                default ->
                    dataBuffer.put(dataBufferPre.get() / normS.x);
            }
        }

        // Pad the usused data with zeros
        for (int i = lenses * 3; i < 64; i++) {
            dataBuffer.put(0f);
        }
        dataBuffer.flip();

        // Bind the data to our texture
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, rippleTex);
        if (ShaderLib.useBufferCore()) {
            GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL30.GL_R32F, 64, 0, GL11.GL_RED, GL11.GL_FLOAT, dataBuffer);
        } else {
            GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, ARBTextureRg.GL_R32F, 64, 0, GL11.GL_RED, GL11.GL_FLOAT, dataBuffer);
        }
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

        GL20.glUniform1i(index[4], lenses); // size
        GL20.glUniform1f(index[5], normS.x); // sizeNormalization
        GL20.glUniform4f(index[6], normX.x, normX.y, normY.x, normY.y); // normalization

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, rippleTex);

        if (!validated) {
            if (!ShaderLib.VALIDATE_EVERY_FRAME) {
                validated = true;
            }

            // This stuff here is for AMD compatability, normally it would be way back in the shader loader
            GL20.glValidateProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
                Global.getLogger(ShaderLib.class).log(Level.ERROR, ShaderLib.getProgramLogInfo(program));
                ShaderLib.exitDraw();
                dataBuffer.clear();
                dataBufferPre.clear();
                enabled = false;
                return;
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0);

        ShaderLib.exitDraw();

        dataBuffer.clear();
        dataBufferPre.clear();
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

        final List<ShipAPI> ships = new ArrayList<>(100);
    }
}
