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
import com.fs.starfarer.api.util.FaderUtil;

import data.kit.HSIIds;
import data.kit.HSIShadersUtil;

public class HSISpecialVentRendererObject extends HSIBaseCombatRendererObject{
    private static final String ventVert = "data/shaders/HSIGalaxyVent.vert";
    private static final String ventFrag = "data/shaders/HSIGalaxyVent.frag";
    public static final String SPECIAL_VENT_SHADER = "HSISpecialVentShader";
    private ShipAPI ship;
    private final int shader;
    private final int level;
    private final int t;
    private float elapsed = 0;
    private FaderUtil levelFader = new FaderUtil(0, 0.5f,1.0f);

    public HSISpecialVentRendererObject(ShipAPI ship){
        this.ship = ship;
        shader = HSIInitPlugin.ShaderManager.getShader(SPECIAL_VENT_SHADER);
        GL20.glUseProgram(shader);
        level = GL20.glGetUniformLocation(shader, "level");
        t = GL20.glGetUniformLocation(shader, "t");
        int tex = GL20.glGetUniformLocation(shader, "text0");
        GL20.glUniform1i(tex, 0);
        tex = GL20.glGetUniformLocation(shader, "text1");
        GL20.glUniform1i(tex, 1);
        GL20.glUseProgram(0);
        setLayer(CombatEngineLayers.BELOW_SHIPS_LAYER);
    }

    @Override
    public void advance(float amount) {
        if(!ship.isAlive()||!Global.getCombatEngine().isEntityInPlay(ship)){
            return;
        }
        float elapseFactor = 0;

        if(Global.getCombatEngine().isPaused()){
            elapseFactor = 0;
        }else{
            levelFader.advance(amount);
            elapseFactor = 1f+6*(levelFader.getBrightness()-0.5f);
            if(ship.getFluxTracker().isVenting()){
                levelFader.fadeIn();
            }else{
                levelFader.fadeOut();
            }
        }
        elapsed+=(elapseFactor*amount);
    }

    @Override
    public void render() {
        GL20.glUseProgram(shader);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL20.glUniform1f(t, elapsed);
        GL20.glUniform1f(level, (0.25f*ship.getFluxLevel())+0.6f*levelFader.getBrightness());
        SpriteAPI grey1 = Global.getSettings().getSprite(HSIIds.SPRITE.GREY_NOISE_64PX);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, grey1.getTextureId());
        SpriteAPI grey2 = Global.getSettings().getSprite(HSIIds.SPRITE.GREY_NOISE_256PX);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, grey2.getTextureId());
        Vector2f size = new Vector2f(ship.getCollisionRadius()*1.5f,ship.getCollisionRadius()*1.5f);
        Vector2f uv = new Vector2f(2.0f, 2.0f);
        GL11.glPushMatrix();
        //Global.getLogger(this.getClass()).info("translatedf:"+translate.x+","+translate.y);
        GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0.0f);
        GL11.glRotatef(ship.getFacing() - 90f, 0.0f, 0.0f, 1.0f);
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

    @Override
    public boolean isExpired() {
        return !ship.isAlive()||!Global.getCombatEngine().isEntityInPlay(ship);
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
