package data.scripts.HSIRenderer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.hullmods.HSITurbulanceShieldListenerV2.HSIShieldRenderData;
import data.kit.HSIShadersUtil;

public class HSICombatRendererV2 extends BaseCombatLayeredRenderingPlugin {
    // public TimeoutTracker<HSIShieldHitData> shieldHitsTracker = new
    // TimeoutTracker<data.scripts.HSIRenderer.HSIShieldHitData>();
    public static final String RENDER_KEY = "HSI_CombatRender";
    public CombatEngineAPI engine = Global.getCombatEngine();
    private EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER,
            CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER, CombatEngineLayers.BELOW_SHIPS_LAYER);
    private List<HSICombatRendererObject> fxObjects = new ArrayList<>();
    private final int[] uniform;
    private static final String ShieldVertKey = "data/shaders/HSIShield.vert";
    private static final String ShieldFragKey = "data/shaders/HSIShield.frag";
    private static final String ShieldShaderKey  = "HSI_Shield";
    // private static final String ShieldFragKey = "data/shaders/HexTest.frag";
    private int shader;
    private HSICombatRendererV2ShaderManager ShaderManager = HSIInitPlugin.ShaderManager;

    public static class HSICombatRendererV2ShaderManager{
        private Map<String,Integer> shaders = new HashMap<>();
        
        public int createShader(String vert,String frag,String key){
            if(shaders.containsKey(key)){
                return shaders.get(key);
            }
            int shaderN = createShaderProgram(vert, frag);
            shaders.put(key, shaderN);
            return shaderN;
        }

        public int getShader(String key){
            if(shaders.containsKey(key)){
                return shaders.get(key);
            }else{
                return 0;
            }
        }
    }

    public HSICombatRendererV2() {
        shader = ShaderManager.getShader(ShieldShaderKey);
        // shader = ShaderLib.loadShader(ShieldVertKey, ShieldFragKey);
        GL20.glUseProgram(shader);
        uniform = new int[] { GL20.glGetUniformLocation(shader, "shieldTex"),
                GL20.glGetUniformLocation(shader, "fxTex"), GL20.glGetUniformLocation(shader, "type"),
                GL20.glGetUniformLocation(shader, "state") };
        GL20.glUniform1i(uniform[0], 0);
        GL20.glUniform1i(uniform[1], 1);
        GL20.glUseProgram(0);
    }

    public static HSICombatRendererV2 getInstance() {
        if (Global.getCombatEngine()!=null&&Global.getCombatEngine().getCustomData().containsKey(RENDER_KEY)) {
            return (HSICombatRendererV2) Global.getCombatEngine().getCustomData().get(RENDER_KEY);
        } else {
            HSICombatRendererV2 renderer = new HSICombatRendererV2();
            if (Global.getCombatEngine() != null) {
                Global.getCombatEngine().addLayeredRenderingPlugin(renderer);
                // source.getCustomData().put(RENDER_KEY, renderer);
                Global.getCombatEngine().getCustomData().put(RENDER_KEY, renderer);
            }
            return renderer;

        }
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return layers;
    }

    public float getRenderRadius() {
        return 10000f;
    }

    public boolean isExpired() {
        // if(!source.isAlive()) Global.getLogger(this.getClass()).info("Renderer
        // Removed for ship dead.");
        return false;
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused())
            return;
        // shieldHitsTracker.advance(amount);
        List<HSICombatRendererObject> toClean = new ArrayList<>();
        for (HSICombatRendererObject obj : fxObjects) {
            if(obj.isExpired()) toClean.add(obj);
            else obj.advance(amount);
        }
        fxObjects.removeAll(toClean);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (layer == CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER) {
            GL20.glUseProgram(shader);
            for (ShipAPI ship : engine.getShips()) {
                if (HSITurbulanceShieldListenerV2.hasShield(ship)
                        && ShaderLib.isOnScreen(ship.getLocation(), 2f * ship.getCollisionRadius())&&!ship.isStationModule()) {
                    processShield(ship);
                }
            }
            GL20.glUseProgram(0);
        }
        for (HSICombatRendererObject obj : fxObjects) {
            if (layer == obj.getLayer()) {
                if(!obj.isExpired()&&obj.shouldRender()) obj.render();
            }
        }
    }

    public HSICombatRendererV2ShaderManager getShaderManager() {
        return ShaderManager;
    }

    public List<HSICombatRendererObject> getFxObjects() {
        return fxObjects;
    }

    public void addFxObject(HSICombatRendererObject object){
        this.fxObjects.add(object);
    }

    protected void processShield(ShipAPI ship) {
        HSIShieldRenderData data = HSITurbulanceShieldListenerV2.getInstance(ship).getRenderData();
        SpriteAPI shield = Global.getSettings().getSprite("fx", "HSI_Shield");
        SpriteAPI hitFx = Global.getSettings().getSprite("HSI_fx", "Fog_Circle");
        float max = (Math.max(ship.getSpriteAPI().getHeight(), ship.getSpriteAPI().getWidth()));
        max = Math.max(512f, max);
        max *= 0.6f;
        Vector2f size = new Vector2f(max, max);
        Vector2f uv = new Vector2f(1.0f, 1.0f);
        float combinedShieldAlphaBase = ship.getAlphaMult() * ship.getExtraAlphaMult() * ship.getExtraAlphaMult2();
        float combinedShieldAlpha = combinedShieldAlphaBase * HSITurbulanceShieldListenerV2.getInstance(ship).getShield().getShieldLevel();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        //GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.2f);

        GL20.glUniform1f(uniform[2], 3.0f);
        SpriteAPI ss = ship.getSpriteAPI();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ss.getTextureId());
        ss.renderAtCenter(ship.getLocation().x, ship.getLocation().y);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glColorMask(true, true, true, true);
        // GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        if(!data.getBlockRender()) {

            // Global.getLogger(this.getClass()).info(data.getSpreadLevel());
            GL11.glPushMatrix();
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shield.getTextureId());
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, hitFx.getTextureId());
            GL20.glUniform1f(uniform[2], 0.0f);
            GL20.glUniform4f(uniform[3], data.getSpreadLevel(), data.getSpreadLevel(),
                    Math.max(ship.getSpriteAPI().getHeight(), ship.getSpriteAPI().getWidth()), combinedShieldAlpha);
            GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0.0f);
            GL11.glRotatef(ship.getFacing(), 0.0f, 0.0f, 1.0f);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColorMask(true, true, true, true);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
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
        }
        if (!data.getHitData().getItems().isEmpty()) {
            Iterator<HSIShieldHitData> hitIter = data.getHitData().getItems().iterator();
            GL11.glPushMatrix();
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shield.getTextureId());
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, hitFx.getTextureId());
            GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0.0f);
            GL11.glRotatef(ship.getFacing(), 0.0f, 0.0f, 1.0f);
            while (hitIter.hasNext()) {
                HSIShieldHitData hit = hitIter.next();
                GL20.glUniform1f(uniform[2], 1.0f+combinedShieldAlphaBase);
                GL20.glUniform4f(uniform[3], (data.getHitData().getRemaining(hit) / hit.time), hit.radius,
                        hit.loc.x / 256.0f, hit.loc.y / 256.0f);
                size = new Vector2f(hit.radius, hit.radius);
                GL11.glBegin(GL11.GL_QUADS);

                GL11.glTexCoord2f(0.0f, 0.0f);
                GL11.glVertex2f(-size.x + hit.loc.x, -size.y + hit.loc.y);
                // GL11.glVertex2f(getVertex(-size.x + hit.loc.x, -size.y +
                // hit.loc.y).x,getVertex(-size.x + hit.loc.x, -size.y + hit.loc.y).y);
                GL11.glTexCoord2f(0.0f, uv.y);
                GL11.glVertex2f(-size.x + hit.loc.x, size.y + hit.loc.y);
                // GL11.glVertex2f(getVertex(-size.x + hit.loc.x, size.y +
                // hit.loc.y).x,getVertex(-size.x + hit.loc.x, size.y + hit.loc.y).y);
                GL11.glTexCoord2f(uv.x, uv.y);
                GL11.glVertex2f(size.x + hit.loc.x, size.y + hit.loc.y);
                // GL11.glVertex2f(getVertex(size.x + hit.loc.x, size.y +
                // hit.loc.y).x,getVertex(-size.x + hit.loc.x, size.y + hit.loc.y).y);
                GL11.glTexCoord2f(uv.x, 0.0f);
                GL11.glVertex2f(size.x + hit.loc.x, -size.y + hit.loc.y);
                // GL11.glVertex2f(getVertex(size.x + hit.loc.x, -size.y +
                // hit.loc.y).x,getVertex(-size.x + hit.loc.x, -size.y + hit.loc.y).y);
                GL11.glEnd();
            }
            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 255);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopAttrib();
    }

    protected Vector2f getVertex(float x, float y) {
        Vector2f vertex = new Vector2f(y, x);
        return vertex;
    }

    public static int createShader(String soruce, int shaderType) {
        // 你不可能在硬件不支持 OpenGL2.0 的情况下调用相关方法，原因是游戏本体的需求只做到了固定管线，所以返回；此处填0意味着输出了一个空的着色器
        if (!GLContext.getCapabilities().OpenGL20) {
            Global.getLogger(Global.class).log(Level.ERROR, "'Your hardware is not supported OpenGL2.0.");
            return 0;
        }
        int shaderID = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shaderID, soruce);
        GL20.glCompileShader(shaderID);
        // 该分支用于检测着色器是否通过编译而可用
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            // 输出相关错误信息至log供修改
            Global.getLogger(Global.class).log(Level.ERROR,
                    "Shader ID: '" + shaderID + "-" + soruce + "' compilation failed:\n"
                            + GL20.glGetShaderInfoLog(shaderID, GL20.glGetShaderi(shaderID, GL20.GL_INFO_LOG_LENGTH)));
            // 既然不可用，那么将其删除释放相关资源
            GL20.glDeleteShader(shaderID);
            // 在出错的情况下，可以检测这个返回值
            return 0;
        } else {
            // 编译成功，输出一行成功的信息
            Global.getLogger(Global.class).info("Shader compiled with ID: '" + shaderID + "'");
            return shaderID;
        }
    }

    public static int createShaderProgram(String vertSource, String fragSource) {
        // 同创建着色器
        if (!GLContext.getCapabilities().OpenGL20) {
            Global.getLogger(Global.class).log(Level.ERROR, "'Your hardware is not supported OpenGL2.0.");
            return 0;
        }
        int programID = GL20.glCreateProgram();
        int[] shaders = new int[] { createShader(vertSource, GL20.GL_VERTEX_SHADER),
                createShader(fragSource, GL20.GL_FRAGMENT_SHADER) };
        if (shaders[0] == 0 || shaders[1] == 0)
            return 0; // 只要有任意一个着色器出问题，必然不可能让这个无效程序就这么运行；此处返回0是因为OpenGL在启用ID为0的着色器程序时意味着关闭
        // 将有效的着色器附着于着色器程序，并链接程序
        GL20.glAttachShader(programID, shaders[0]);
        GL20.glAttachShader(programID, shaders[1]);
        GL20.glLinkProgram(programID);

        // 同着色器创建的操作
        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            Global.getLogger(Global.class).log(Level.ERROR, "'Shader program ID: '" + programID + "' linking failed:\n"
                    + GL20.glGetProgramInfoLog(programID, GL20.glGetProgrami(programID, GL20.GL_INFO_LOG_LENGTH)));
            GL20.glDeleteProgram(programID);
            GL20.glDetachShader(programID, shaders[0]);
            GL20.glDeleteShader(shaders[0]);
            GL20.glDetachShader(programID, shaders[1]);
            GL20.glDeleteShader(shaders[1]);
            return 0;
        } else {
            Global.getLogger(Global.class).info("Shader program created with ID: '" + programID + "'");
            return programID;
        }
    }
}
