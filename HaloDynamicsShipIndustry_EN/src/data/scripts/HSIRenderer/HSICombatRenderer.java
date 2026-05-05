package data.scripts.HSIRenderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.TimeoutTracker;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSICombatRenderer extends BaseCombatLayeredRenderingPlugin {
    public TimeoutTracker<HSIShieldHitData> shieldHitsTracker = new TimeoutTracker<data.scripts.HSIRenderer.HSIShieldHitData>();
    private ShipAPI source;
    public static final String RENDER_KEY = "HSI_CombatRender";
    private EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER,
            CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER, CombatEngineLayers.BELOW_SHIPS_LAYER);
    private float elapsed = 0;
    private FaderUtil shieldFader = new FaderUtil(0f, 7f);
    private List<HSICombatRenderFXRequest> fxRequests = new ArrayList<>();
    private List<HSICombatRenderFXRequest> fxRequestsBelow = new ArrayList<>();
    private List<HSICombatRendererObject> fxObjects = new ArrayList<>();
    private boolean blockShieldRender = false;
    //private float shield1a;
    //private float shield2a;
    //private float shield3a;
    //private float ringa;
    

    private boolean renderShieldLayerExtra = false;
    private float renderShieldLayerExtraAplha = 0;

    public HSICombatRenderer(ShipAPI entity) {
        this.source = entity;
        // layer = CombatEngineLayers.ABOVE_PARTICLES_LOWER;
        shieldFader.fadeIn();
        // Global.getLogger(this.getClass()).info("Renderer Created!");
        // shield1a = LunaSettings.getFloat("HalopowerdShipIndustryDEV",
        // "shieldAlpha1");
        // shield2a = LunaSettings.getFloat("HalopowerdShipIndustryDEV",
        // "shieldAlpha2");
        // shield3a = LunaSettings.getFloat("HalopowerdShipIndustryDEV",
        // "shieldAlpha3");
        // ringa = LunaSettings.getFloat("HalopowerdShipIndustryDEV", "ringAlpha");

    }

    public static HSICombatRenderer getInstance(ShipAPI source) {
        if (source.getCustomData().containsKey(RENDER_KEY)) {
            return (HSICombatRenderer) source.getCustomData().get(RENDER_KEY);
        } else {
            HSICombatRenderer renderer = new HSICombatRenderer(source);
            if(Global.getCombatEngine()!=null) 
            Global.getCombatEngine().addLayeredRenderingPlugin(renderer);
            // source.getCustomData().put(RENDER_KEY, renderer);
            source.setCustomData(RENDER_KEY, renderer);
            return renderer;
        }
    }

    public class HSICombatRenderFXRequest {
        protected String catagory;
        protected String id;
        protected float alpha;
        protected boolean hasColor;
        protected Color color;
        protected float angle;
        protected Vector2f location;
        protected boolean isAdditive;
        protected float width;
        protected float height;

        public HSICombatRenderFXRequest(String catagory, String id, float alpha, Color color, float angle,
                Vector2f location, boolean isAdditive) {
            this.catagory = catagory;
            this.id = id;
            this.alpha = alpha;
            this.color = color;
            hasColor = color != null;
            this.angle = angle;
            this.location = location;
            this.isAdditive = isAdditive;
            SpriteAPI sprite = Global.getSettings().getSprite(catagory, id);
            if (sprite != null) {
                setWidth(sprite.getWidth());
                setHeight(sprite.getHeight());
            } else {
                setWidth(0);
                setHeight(0);
            }
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public float getAlpha() {
            return alpha;
        }

        public float getAngle() {
            return angle;
        }

        public String getCatagory() {
            return catagory;
        }

        public Color getColor() {
            return color;
        }

        public float getHeight() {
            return height;
        }

        public String getId() {
            return id;
        }

        public Vector2f getLocation() {
            return location;
        }

        public float getWidth() {
            return width;
        }

        public void setAdditive(boolean isAdditive) {
            this.isAdditive = isAdditive;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        public void setAngle(float angle) {
            this.angle = angle;
        }

        public void setCatagory(String catagory) {
            this.catagory = catagory;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void setHasColor(boolean hasColor) {
            this.hasColor = hasColor;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setLocation(Vector2f location) {
            this.location = location;
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
        return !source.isAlive();
    }

    public void advance(float amount) {
        // Global.getLogger(this.getClass()).info("Processing
        // "+shieldHitsTracker.getItems().size());
        if (Global.getCombatEngine().isPaused())
            return;
        shieldHitsTracker.advance(amount);
        elapsed += amount;
        if (elapsed > 15)
            elapsed = 0;
        if (!blockShieldRender && HSITurbulanceShieldListenerV2.hasShield(source)) {
            shieldFader.advance(amount);
            if (shieldFader.isFadedIn()) {
                shieldFader.setBrightness(0);
                shieldFader.fadeIn();
            }
        }
        for(HSICombatRendererObject obj:fxObjects){
            obj.advance(amount);
        }
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        switch (layer) {
            case ABOVE_SHIPS_AND_MISSILES_LAYER:
                // if (!blockShieldRender &&
                // source.hasListenerOfClass(HSITurbulanceShieldListenerV2.class))
                // processShieldV5();
                // processShieldV6();
                if (!blockShieldRender && HSITurbulanceShieldListenerV2.hasShield(source))
                    processShieldHits();
                break;
            case ABOVE_PARTICLES_LOWER:
                if (!blockShieldRender) {
                    if (renderShieldLayerExtra) {
                        processExtraShieldLayer(renderShieldLayerExtraAplha);
                    }
                }
                processFx();
                if (!Global.getCombatEngine().isPaused()) {
                    fxRequests.clear();
                    setShieldLayerExtraRender(false, 0);
                }
                break;
            case BELOW_SHIPS_LAYER:
                processFxBelow();
                if (!Global.getCombatEngine().isPaused())
                    fxRequestsBelow.clear();
                break;
            default:
                break;
        }

        for(HSICombatRendererObject obj:fxObjects){
            if(layer == obj.getLayer()){
                obj.render();
            }
        }
    }

    public HSICombatRenderFXRequest requestFX(String catagory, String id, float alpha, Color color, float angle,
            Vector2f location,
            boolean additive) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, color, angle, location,
                additive);
        fxRequests.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFX(String catagory, String id, float alpha, float facing) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, null, facing,
                source.getLocation(), false);
        fxRequests.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFX(String catagory, String id, float alpha, Color color) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, color, source.getFacing() - 90f,
                source.getLocation(), false);
        fxRequests.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFX(String catagory, String id, float alpha) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, null, source.getFacing() - 90f,
                source.getLocation(), false);
        fxRequests.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFX(String catagory, String id, float alpha, boolean additive) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, null, source.getFacing() - 90f,
                source.getLocation(), additive);
        fxRequests.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFXBelow(String catagory, String id, float alpha, Color color, float angle,
            Vector2f location,
            boolean additive) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, color, angle, location,
                additive);
        fxRequestsBelow.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFXBelow(String catagory, String id, float alpha, float facing) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, null, facing,
                source.getLocation(), false);
        fxRequestsBelow.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFXBelow(String catagory, String id, float alpha, Color color) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, color, source.getFacing() - 90f,
                source.getLocation(), false);
        fxRequestsBelow.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFXBelow(String catagory, String id, float alpha) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, null, source.getFacing() - 90f,
                source.getLocation(), false);
        fxRequestsBelow.add(r);
        return r;
    }

    public HSICombatRenderFXRequest requestFXBelow(String catagory, String id, float alpha, boolean additive) {
        HSICombatRenderFXRequest r = new HSICombatRenderFXRequest(catagory, id, alpha, null, source.getFacing() - 90f,
                source.getLocation(), additive);
        fxRequestsBelow.add(r);
        return r;
    }

    public void processFx() {
        for (HSICombatRenderFXRequest request : fxRequests) {
            SpriteAPI sprite = Global.getSettings().getSprite(request.catagory, request.id);
            if (request.hasColor)
                sprite.setColor(request.color);
            if (request.isAdditive)
                sprite.setAdditiveBlend();
            sprite.setAngle(request.angle);
            sprite.setAlphaMult(request.alpha);
            sprite.renderAtCenter(request.location.x, request.location.y);
            sprite.setSize(request.getWidth(), request.getHeight());
        }
        // fxRequests.clear();
    }

    public void processFxBelow() {
        for (HSICombatRenderFXRequest request : fxRequestsBelow) {
            SpriteAPI sprite = Global.getSettings().getSprite(request.catagory, request.id);
            if (request.hasColor)
                sprite.setColor(request.color);
            sprite.setAngle(request.angle);
            sprite.setAlphaMult(request.alpha);
            sprite.renderAtCenter(request.location.x, request.location.y);
        }
        // fxRequestsBelow.clear();
    }

    /*
     * public void processShieldV5() {
     * SpriteAPI shield = Global.getSettings().getSprite("fx", "HSI_Shield");
     * SpriteAPI shield2 = Global.getSettings().getSprite("fx", "HSI_Shield2");
     * SpriteAPI shield3 = Global.getSettings().getSprite("fx", "HSI_Shield3");
     * SpriteAPI shieldRing = Global.getSettings().getSprite("HSI_fx",
     * "Shield128c");
     * if (!source.getListeners(HSITurbulanceShieldListenerV2.class).isEmpty()) {
     * HSITurbulanceShieldListenerV2 s = (HSITurbulanceShieldListenerV2) source
     * .getListeners(HSITurbulanceShieldListenerV2.class).get(0);
     * if (s.getShield().getCurrent() > 0) {
     * shield.setAngle(source.getFacing() - 90f);
     * float alphaExtra = 1 - shieldFader.getBrightness();
     * float alphaBase = source.getAlphaMult();
     * GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
     * GL11.glEnable(GL11.GL_STENCIL_TEST);
     * GL11.glDisable(GL11.GL_DEPTH_TEST);
     * GL11.glDisable(GL11.GL_TEXTURE_2D);
     * GL11.glColorMask(false, false, false, false);
     * GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
     * GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
     * GL11.glEnable(GL11.GL_ALPHA_TEST);
     * GL11.glAlphaFunc(GL11.GL_GREATER, 0.2f);
     * SpriteAPI ship = source.getSpriteAPI();
     * ship.renderAtCenter(source.getLocation().x, source.getLocation().getY());
     * GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
     * GL11.glColorMask(true, true, true, true);
     * // GL11.glDisable(GL11.GL_ALPHA_TEST);
     * GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);
     * // GL11.glEnable(GL11.GL_BLEND);
     * // shieldRing.setBlendFunc(GL11.GL_STENCIL_BUFFER_BIT,
     * // GL11.GL_STENCIL_BUFFER_BIT);
     * // shield.setAlphaMult(shield1a*s.getShield().getCurrent()/s.getShield().getShieldCap());
     * // shield.renderAtCenter(source.getLocation().getX(),
     * // source.getLocation().getY());
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
     * // GL11.glDisable(GL11.GL_ALPHA_TEST);
     * // float maxSize = Math.max(ship.getWidth(),ship.getHeight());
     * // GL11.glEnable(GL11.GL_BLEND);
     * // shieldRing.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
     * // shieldRing.setSize(shieldFader.getBrightness()*maxSize,
     * // shieldFader.getBrightness()*maxSize);
     * shieldRing.setAngle(source.getFacing() - 90);
     * shieldRing.setColor(shield.getAverageBrightColor());
     * shieldRing.setAlphaMult(
     * alphaBase * ringa * s.getShield().getCurrent() / s.getShield().getShieldCap()
     * (float) (Math.sqrt(1 - shieldFader.getBrightness())) * alphaExtra);
     * shieldRing.setSize(shieldFader.getBrightness() * ship.getWidth() * 2.5f,
     * shieldFader.getBrightness() * ship.getHeight() * 2.5f);
     * shieldRing.renderAtCenter(source.getLocation().getX(),
     * source.getLocation().getY());
     * GL11.glStencilFunc(GL11.GL_EQUAL, 2, 255);
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
     * GL11.glDisable(GL11.GL_ALPHA_TEST);
     * GL11.glEnable(GL11.GL_BLEND);
     * shield.setBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
     * shield.setAlphaMult(alphaBase * shield1a * s.getShield().getCurrent() /
     * s.getShield().getShieldCap() * alphaExtra
     * (float) (Math.sqrt(1 - shieldFader.getBrightness())));
     * shield.setAngle(source.getFacing() - 90);
     * shield.renderAtCenter(source.getLocation().getX(),
     * source.getLocation().getY());
     * shield2.setBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
     * shield2.setAlphaMult(alphaBase * shield2a * s.getShield().getCurrent() /
     * s.getShield().getShieldCap() * alphaExtra
     * (float) (Math.sqrt(1 - shieldFader.getBrightness())));
     * shield2.setAngle(source.getFacing() - 90);
     * shield2.renderAtCenter(source.getLocation().getX(),
     * source.getLocation().getY());
     * shield3.setBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
     * shield3.setAngle(source.getFacing() - 90);
     * shield3.setAlphaMult(alphaBase * shield3a * s.getShield().getCurrent() /
     * s.getShield().getShieldCap() * alphaExtra
     * (float) (Math.sqrt(1 - shieldFader.getBrightness())));
     * shield3.renderAtCenter(source.getLocation().getX(),
     * source.getLocation().getY());
     * // GL11.glDisable(GL11.GL_BLEND);
     * // GL11.glStencilFunc(GL11.GL_LEQUAL, 16, 255);
     * // shield.setAdditiveBlend();
     * GL11.glDisable(GL11.GL_BLEND);
     * GL11.glEnable(GL11.GL_DEPTH_TEST);
     * GL11.glEnable(GL11.GL_TEXTURE_2D);
     * GL11.glStencilMask(255);
     * GL11.glDisable(GL11.GL_STENCIL_TEST);
     * GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 255);
     * GL11.glPopAttrib();
     * }
     * }
     * }
     */

    public void processExtraShieldLayer(float alpha) {
        SpriteAPI shield = Global.getSettings().getSprite("fx", "HSI_Shield");
        shield.setAngle(source.getFacing() - 90f);
        float alphaBase = source.getAlphaMult();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.2f);
        SpriteAPI ship = source.getSpriteAPI();
        ship.renderAtCenter(source.getLocation().x, source.getLocation().getY());
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glColorMask(true, true, true, true);
        // GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        shield.setAlphaMult(alpha * alphaBase);
        shield.setAngle(source.getFacing() - 90);
        shield.renderAtCenter(source.getLocation().getX(), source.getLocation().getY());
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glStencilMask(255);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 255);
        GL11.glPopAttrib();
    }

    /*
     * public void processShieldV6() {
     * SpriteAPI shield = Global.getSettings().getSprite("fx", "HSI_Shield");
     * SpriteAPI shield2 = Global.getSettings().getSprite("fx", "HSI_Shield2");
     * SpriteAPI shield3 = Global.getSettings().getSprite("fx", "HSI_Shield3");
     * // SpriteAPI shieldRing = Global.getSettings().getSprite("HSI_fx",
     * // "Shield128c");
     * if (!source.getListeners(HSITurbulanceShieldListenerV2.class).isEmpty()) {
     * HSITurbulanceShieldListenerV2 s = (HSITurbulanceShieldListenerV2) source
     * .getListeners(HSITurbulanceShieldListenerV2.class).get(0);
     * if (s.getShield().getCurrent() > 0) {
     * shield.setAngle(source.getFacing() - 90f);
     * float alphaExtra = 1 - shieldFader.getBrightness();
     * float alphaBase = source.getAlphaMult();
     * GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
     * GL11.glEnable(GL11.GL_STENCIL_TEST);
     * GL11.glDisable(GL11.GL_DEPTH_TEST);
     * GL11.glDisable(GL11.GL_TEXTURE_2D);
     * GL11.glColorMask(false, false, false, false);
     * GL11.glTranslatef(source.getLocation().x, source.getLocation().y,0.0f);
     * GL11.glRotatef(source.getFacing()-90f, 0.0f, 0.0f, 1.0f);
     * GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
     * GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
     * GL11.glEnable(GL11.GL_ALPHA_TEST);
     * GL11.glAlphaFunc(GL11.GL_GREATER, 0.2f);
     * SpriteAPI ship =
     * Global.getSettings().getSprite(source.getHullSpec().getSpriteName());
     * ship.renderAtCenter(0,0);
     * GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
     * GL11.glColorMask(true, true, true, true);
     * GL11.glAlphaFunc(GL11.GL_GREATER, 0.0f);
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
     * GL11.glStencilFunc(GL11.GL_EQUAL, 2, 255);
     * GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
     * GL11.glDisable(GL11.GL_ALPHA_TEST);
     * GL11.glEnable(GL11.GL_BLEND);
     * String vert;
     * String frag;
     * try {
     * vert = Global.getSettings().loadText("data/shaders/HSI_Shield.vert");
     * frag = Global.getSettings().loadText("data/shaders/HSI_Shield.frag");
     * } catch (Exception e) {
     * GL11.glDisable(GL11.GL_BLEND);
     * GL11.glEnable(GL11.GL_DEPTH_TEST);
     * GL11.glEnable(GL11.GL_TEXTURE_2D);
     * GL11.glStencilMask(255);
     * GL11.glDisable(GL11.GL_STENCIL_TEST);
     * GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 255);
     * GL11.glPopAttrib();
     * return;
     * }
     * int pro = ShaderLib.loadShader(vert, frag);
     * if (pro == 0) {
     * GL11.glDisable(GL11.GL_BLEND);
     * GL11.glEnable(GL11.GL_DEPTH_TEST);
     * GL11.glEnable(GL11.GL_TEXTURE_2D);
     * GL11.glStencilMask(255);
     * GL11.glDisable(GL11.GL_STENCIL_TEST);
     * GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 255);
     * GL11.glPopAttrib();
     * return;
     * }
     * 
     * GL20.glUseProgram(pro);
     * int wh = GL20.glGetUniformLocation(pro, "wh");
     * int t = GL20.glGetUniformLocation(pro, "t");
     * int c = GL20.glGetUniformLocation(pro, "c");
     * GL20.glUniform2f(wh, ship.getWidth() * 2f, ship.getHeight() * 2f);
     * GL20.glUniform1f(t, (1 - shieldFader.getBrightness()) * 7.0f);
     * float a, b;
     * if (ship.getWidth() > ship.getHeight()) {
     * a = ship.getWidth();
     * b = ship.getHeight();
     * } else {
     * a = ship.getHeight();
     * b = ship.getWidth();
     * }
     * GL20.glUniform1f(c, (float)(Math.sqrt(Math.pow(a, 2) - Math.pow(b, 2))));
     * shield.setBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
     * shield.setAlphaMult(alphaBase * shield1a * s.getShield().getCurrent() /
     * s.getShield().getShieldCap() * alphaExtra
     * (float) (Math.sqrt(1 - shieldFader.getBrightness())));
     * //shield.setAngle(source.getFacing() - 90);
     * shield.renderAtCenter(0,0);
     * shield2.setBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
     * shield2.setAlphaMult(alphaBase * shield2a * s.getShield().getCurrent() /
     * s.getShield().getShieldCap() * alphaExtra
     * (float) (Math.sqrt(1 - shieldFader.getBrightness())));
     * //shield2.setAngle(source.getFacing() - 90);
     * shield2.renderAtCenter(0,0);
     * shield3.setBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
     * //shield3.setAngle(source.getFacing() - 90);
     * shield3.setAlphaMult(alphaBase * shield3a * s.getShield().getCurrent() /
     * s.getShield().getShieldCap() * alphaExtra
     * (float) (Math.sqrt(1 - shieldFader.getBrightness())));
     * shield3.renderAtCenter(0,0);
     * // GL11.glDisable(GL11.GL_BLEND);
     * // GL11.glStencilFunc(GL11.GL_LEQUAL, 16, 255);
     * // shield.setAdditiveBlend();
     * //GL20.glDeleteProgram(pro);
     * GL11.glDisable(GL11.GL_BLEND);
     * GL11.glEnable(GL11.GL_DEPTH_TEST);
     * GL11.glEnable(GL11.GL_TEXTURE_2D);
     * GL11.glStencilMask(255);
     * GL11.glDisable(GL11.GL_STENCIL_TEST);
     * GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 255);
     * GL11.glPopAttrib();
     * GL20.glUseProgram(0);
     * }
     * }
     * }
     */

    public void processShieldHits() {
        Iterator<HSIShieldHitData> preProcess = shieldHitsTracker.getItems().iterator();
        SpriteAPI shield = Global.getSettings().getSprite("fx", "HSI_Shield");

        float alphaBase = source.getAlphaMult();
        if (HSITurbulanceShieldListenerV2.hasShield(source)) {
            HSITurbulanceShieldListenerV2 s = HSITurbulanceShieldListenerV2.getInstance(source);
            // shield.setColor(s.getShieldColor());
            shield.setAlphaMult(alphaBase * s.getShield().getShieldLevel() * 0.5f + 0.5f);
            shield.setAngle(source.getFacing() + 90f);
        } else {
            return;
        }
        //int i = 0;
        while (preProcess.hasNext()) {
            //i++;
            HSIShieldHitData data = preProcess.next();
            Vector2f loc = new Vector2f(data.loc);
            loc = VectorUtils.rotate(data.loc, source.getFacing(), loc);
            loc = Vector2f.add(loc, source.getLocation(), loc);
            float left = shieldHitsTracker.getRemaining(data) / data.time;
            if (left >= 0.5f) {
                left = 1;
            } else {
                left *= 2;
            }
            float time = (float) (left * Math.PI * 0.5f);
            float alpha = (float) (Math.sin(time));
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColorMask(false, false, false, false);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.2f);
            SpriteAPI circleMask = Global.getSettings().getSprite("HSI_fx", "Fog_Circle");
            circleMask.setSize(data.radius * 2f, data.radius * 2f);
            circleMask.setAngle(source.getFacing() + 90f);
            circleMask.renderAtCenter(loc.x, loc.y);
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
            SpriteAPI ship = source.getSpriteAPI();
            ship.renderAtCenter(source.getLocation().x, source.getLocation().getY());
            GL11.glStencilFunc(GL11.GL_EQUAL, 2, 255);
            GL11.glColorMask(true, true, true, true);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
            shield.setAlphaMult(alphaBase * alpha * 0.7f);
            shield.renderAtCenter(source.getLocation().getX(), source.getLocation().getY());
            shield.setAlphaMult(1);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glStencilMask(255);
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 255);
            GL11.glPopAttrib();
        }
    }

    public void requestShieldRender(HSIShieldHitData data, float time) {
        shieldHitsTracker.add(data, time);
        // Global.getLogger(this.getClass()).info("Requesting for " + time);
    }

    public boolean isBlockShieldRender() {
        return blockShieldRender;
    }

    public void setBlockShieldRender(boolean state) {
        blockShieldRender = state;
        if (state) {
            shieldFader.setBrightness(0f);
            shieldFader.fadeIn();
        }
    }

    public void setShieldLayerExtraRender(boolean shouldRender, float alpha) {
        renderShieldLayerExtra = shouldRender;
        renderShieldLayerExtraAplha = alpha;
    }
}
