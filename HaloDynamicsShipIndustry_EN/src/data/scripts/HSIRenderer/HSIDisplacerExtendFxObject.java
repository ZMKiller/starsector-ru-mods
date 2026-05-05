package data.scripts.HSIRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import data.kit.HSIShadersUtil;

public class HSIDisplacerExtendFxObject extends HSIBaseCombatRendererObject{
    private ShipAPI ship;
    private final int shader;
    private final int uniform;
    private static final String HSIDisplacerVertKey = "data/shaders/HSIEdgeEffect.vert";
    private static final String HSIDisplacerFragKey = "data/shaders/HSIEdgeEffect.frag";
    public static final String HSIDisplacerKey = "HSI_Displacer";

    private float effectLevel = 0;
    private Vector2f translate = null;
    //private float facing = 0;

    public HSIDisplacerExtendFxObject(ShipAPI ship) {
        this.ship = ship;
        shader = HSIInitPlugin.ShaderManager.getShader(HSIDisplacerKey);
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
        //float combinedShieldAlphaBase = ship.getAlphaMult() * ship.getExtraAlphaMult() * ship.getExtraAlphaMult2();
        ShipSystemAPI system = ship.getSystem();
        float lim = 0.2f;
        if(system == null) return;
        effectLevel = Math.min(system.getEffectLevel()/lim,1f);
        translate = new Vector2f(ship.getLocation());
        if(system.isChargeup()){
            effectLevel = Math.min(0.5f,effectLevel);
            ship.setAngularVelocity(0);
        }else{
            if(system.getEffectLevel()<=lim){
                ship.setExtraAlphaMult(Math.max(0,((lim-system.getEffectLevel())/lim*(lim-system.getEffectLevel())/lim)) );
            }else{
                ship.setExtraAlphaMult(0);
                ship.getVelocity().set(0,0);
            }
        }
    }

    @Override
    public void render() {
        if(translate==null) return;
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
        //Global.getLogger(this.getClass()).info("translatedf:"+translate.x+","+translate.y);
        GL11.glTranslatef(translate.x, translate.y, 0.0f);
        GL11.glRotatef(ship.getFacing() - 90f, 0.0f, 0.0f, 1.0f);
        float offsetx = (size.x - ss.getWidth() * 0.5f);//and some unknown offset
        float offsety = (size.y-ss.getHeight()*0.5f);//and some unknown offset
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

    @Override
    public float getRadius() {
        return 10000f;
    }
}
