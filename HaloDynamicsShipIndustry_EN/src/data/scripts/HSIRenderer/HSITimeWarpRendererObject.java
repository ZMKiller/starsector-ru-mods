package data.scripts.HSIRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import data.kit.HSIShadersUtil;

public class HSITimeWarpRendererObject extends HSIBaseCombatRendererObject {
    private ShipAPI ship;
    private final int shader;
    private final int uniform;
    private static final String HSITimeWarpVertKey = "data/shaders/HSITimeWarp.vert";
    private static final String HSITimeWarpFragKey = "data/shaders/HSITimeWarp.frag";
    private static final String HSITimeWarpKey = "HSI_TimeWarp";
    private float effectLevel = 0;

    public HSITimeWarpRendererObject(ShipAPI ship) {
        this.ship = ship;
        shader = HSIInitPlugin.ShaderManager.getShader(HSITimeWarpKey);
        GL20.glUseProgram(shader);
        uniform = GL20.glGetUniformLocation(shader, "effectlevel");
        int tex = GL20.glGetUniformLocation(shader, "shipTex");
        GL20.glUniform1i(tex, 0);
        GL20.glUseProgram(0);
        setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    }

    @Override
    public void advance(float amount) {
        // Global.getLogger(getClass()).info("TimeWarp is active:"+isExpired());
        float combinedShieldAlphaBase = ship.getAlphaMult() * ship.getExtraAlphaMult() * ship.getExtraAlphaMult2();
        effectLevel = combinedShieldAlphaBase * ship.getSystem().getEffectLevel();
    }

    @Override
    public void render() {
        GL20.glUseProgram(shader);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL20.glUniform1f(uniform, effectLevel);
        SpriteAPI ss = ship.getSpriteAPI();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ss.getTextureId());
        Vector2f size = new Vector2f(getClosestSize(ss.getWidth()) / 2f, getClosestSize(ss.getHeight()) / 2f);
        Vector2f uv = new Vector2f(1.0f, 1.0f);
        GL11.glPushMatrix();
        GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0.0f);
        GL11.glRotatef(ship.getFacing() - 90f, 0.0f, 0.0f, 1.0f);
        float offsetx = (size.x - ss.getWidth() * 0.5f);
        float offsety = (size.y-ss.getHeight()*0.5f);
        GL11.glTranslatef(offsetx, offsety, 0.0f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(-size.x, -size.y);
        GL11.glTexCoord2f(0.0f, uv.y);
        GL11.glVertex2f(-size.x, size.y);
        GL11.glTexCoord2f(uv.x, uv.y);
        GL11.glVertex2f(size.x, size.y);
        GL11.glTexCoord2f(uv.x, 0.0f);
        GL11.glVertex2f(size.x, -size.y);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopAttrib();
        GL20.glUseProgram(0);
    }

    private float getClosestSize(float base) {
        int i = 1;
        while (Math.pow(2, i) < base) {
            i++;
        }
        return (float) Math.pow(2, i);
    }

    @Override
    public boolean isExpired() {
        return ship.getSystem().getEffectLevel()<=0&&!ship.getSystem().isOn();
    }

    @Override
    public CombatEntityAPI getAnchor() {
        return ship;
    }

}
