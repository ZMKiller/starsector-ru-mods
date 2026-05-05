package data.scripts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.hullmods.ShieldMod.HSIShieldLinkFrame;
import data.kit.HSIIds;

public class HSIShieldLinkPlugin extends BaseCombatLayeredRenderingPlugin {
    private IntervalUtil linkChecker = new IntervalUtil(0.8f, 1.2f);
    private List<HSIShieldLinkPair> pairs = new ArrayList<HSIShieldLinkPair>();
    private int side = 0;
    public static final Color LINK = new Color(180, 180, 225, 255);

    public class HSIShieldLinkPair {
        private ShipAPI node0;
        private ShipAPI node1;
        private FaderUtil brightness = new FaderUtil(0, 0.6f);
        private boolean expire = false;
        private float transferredThisPairLastFrame = 0;
        private int direction = 0;// 0 = 0->1 1 = 1->0
        private float textureLoc = 0;
        private boolean node0State = false;
        private boolean node1State = false;

        public HSIShieldLinkPair(ShipAPI node0, ShipAPI node1) {
            this.node0 = node0;
            this.node1 = node1;
            node0State = HSITurbulanceShieldListenerV2.getInstance(node0).getShield().isShieldRegenBlocked();
            node1State = HSITurbulanceShieldListenerV2.getInstance(node1).getShield().isShieldRegenBlocked();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HSIShieldLinkPair) {
                HSIShieldLinkPair o = (HSIShieldLinkPair) obj;
                return ((o.node0 == node0 && o.node1 == node1) || (o.node0 == node1 && o.node1 == node0));
            } else {
                return false;
            }
        }

        public void advance(float amount) {
            if (expire) {
                brightness.fadeOut();
            } else {
                brightness.fadeIn();
            }
            brightness.advance(amount);
        }

        public boolean isExpire() {
            return expire;
        }

        public void setExpire(boolean expire) {
            this.expire = expire;
        }

        public FaderUtil getBrightness() {
            return brightness;
        }

        public float getTransferredThisPairLastFrame() {
            return transferredThisPairLastFrame;
        }

        public void setTransferredThisPairLastFrame(float transferredThisPairLastFrame) {
            this.transferredThisPairLastFrame = transferredThisPairLastFrame;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public ShipAPI getNode0() {
            return node0;
        }

        public ShipAPI getNode1() {
            return node1;
        }

        public float getTextureLoc() {
            return textureLoc;
        }

        public void setNode0(ShipAPI node0) {
            this.node0 = node0;
        }

        public void setNode1(ShipAPI node1) {
            this.node1 = node1;
        }

        public void setTextureLoc(float textureLoc) {
            this.textureLoc = textureLoc;
        }

        public boolean isNode0State() {
            return node0State;
        }

        public boolean isNode1State() {
            return node1State;
        }

        public void setNode0State(boolean node0State) {
            this.node0State = node0State;
        }

        public void setNode1State(boolean node1State) {
            this.node1State = node1State;
        }
    }

    public HSIShieldLinkPlugin(int side) {
        this.side = side;
    }

    public static HSIShieldLinkPlugin getInstance(int side) {
        if (Global.getCombatEngine().getCustomData() != null
                && Global.getCombatEngine().getCustomData().containsKey("HSIShieldLinkPlugin" + side)) {
            return (HSIShieldLinkPlugin) Global.getCombatEngine().getCustomData().get("HSIShieldLinkPlugin" + side);
        } else {
            HSIShieldLinkPlugin plugin = new HSIShieldLinkPlugin(side);
            Global.getCombatEngine().addLayeredRenderingPlugin(plugin);
            Global.getCombatEngine().getCustomData().put("HSIShieldLinkPlugin" + side, plugin);
            return plugin;
        }
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused())
            return;
        linkChecker.advance(amount);
        List<HSIShieldLinkPair> toRemove = new ArrayList<>();
        for (HSIShieldLinkPair p : pairs) {
            p.advance(amount);
            p.setTransferredThisPairLastFrame(0);
            if (linkChecker.intervalElapsed())
                p.setExpire(true);
            if (p.isExpire() && p.getBrightness().getBrightness() == 0) {
                toRemove.add(p);
            }
        }
        pairs.removeAll(toRemove);
        if (linkChecker.intervalElapsed()) {
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                if (ship.isFighter() || ship.getOwner() != side || !ship.getVariant().hasHullMod(HSIIds.HullMod.HSISLF)
                        || !HSITurbulanceShieldListenerV2.hasShield(ship))
                    continue;
                for (ShipAPI ally : Global.getCombatEngine().getShips()) {
                    if (ally.isFighter() || ally.getOwner() != side
                            || !ally.getVariant().hasHullMod(HSIIds.HullMod.HSISLF)
                            || !HSITurbulanceShieldListenerV2.hasShield(ally)
                            || MathUtils.getDistance(ship.getLocation(), ally.getLocation()) > HSIShieldLinkFrame.RANGE
                                    + 400f)
                        continue;
                    HSIShieldLinkPair pair = new HSIShieldLinkPair(ship, ally);
                    boolean shouldAdd = true;
                    for (HSIShieldLinkPair p : pairs) {
                        if (p.equals(pair)) {
                            p.setExpire(false);
                            shouldAdd = false;
                        }
                    }
                    if (shouldAdd)
                        pairs.add(pair);
                }
            }
        }
        for (HSIShieldLinkPair p : pairs) {
            linkShield(p, amount);
        }
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (layer == CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER) {
            SpriteAPI sprite = Global.getSettings().getSprite("HSI_fx", "ContrailWeave");
            for (HSIShieldLinkPair p : pairs) {
                ShipAPI from = (p.getDirection() == 0) ? p.node0 : p.node1;
                ShipAPI to = (p.getDirection() == 1) ? p.node0 : p.node1;
                float transferFactor = 0.4f + Math.min(0.4f, p.transferredThisPairLastFrame / 12f);
                // Global.getLogger(this.getClass()).info("Pair0:"+p.node0.getName()+"|Pair1:"+p.node1.getName()+":"+p.transferredThisPairLastFrame);
                float textureRollSpeed = transferFactor * (48f / 60f) * ((p.getDirection() == 0) ? 1 : -1);
                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                sprite.bindTexture();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glBegin(GL11.GL_QUAD_STRIP);
                float textLength = sprite.getWidth();
                float textProgress = p.textureLoc;
                textProgress += textureRollSpeed;
                Vector2f mp0 = from.getLocation();
                Vector2f mp1 = to.getLocation();
                float linkAngle = VectorUtils.getAngle(mp0, mp1);
                // float width = WIDTH*midpoints.get(i).getOpacity()*0.5f;
                float width = 10f;
                float length = MathUtils.getDistance(mp0, mp1) / 10f;
                for (int i = 0; i < 11; i++) {
                    Vector2f center = new Vector2f(0.1f * i * mp0.x + 0.1f * (10 - i) * mp1.x,
                            0.1f * i * mp0.y + 0.1f * (10 - i) * mp1.y);
                    Vector2f v0 = MathUtils.getPointOnCircumference(center, width, linkAngle + 90f);
                    Vector2f v1 = MathUtils.getPointOnCircumference(center, width, linkAngle - 90f);
                    GL11.glColor4ub((byte) LINK.getRed(), (byte) LINK.getGreen(), (byte) LINK.getBlue(),
                            (byte) (LINK.getAlpha()
                                    * Math.sin(0.314f * i)
                                    * transferFactor
                                    * p.getBrightness().getBrightness()));
                    GL11.glTexCoord2f(textProgress, 0f);
                    GL11.glVertex2f(v0.x, v0.y);
                    GL11.glTexCoord2f(textProgress, 1f);
                    GL11.glVertex2f(v1.x, v1.y);
                    textProgress += (length / (10f * textLength));
                }
                GL11.glEnd();
                GL11.glPopMatrix();
                GL11.glPopAttrib();
                p.setTextureLoc(p.getTextureLoc() + textureRollSpeed);
            }
        }
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    }

    @Override
    public float getRenderRadius() {
        return 100000f;
    }

    public void linkShield(HSIShieldLinkPair p, float amount) {
        HSITurbulanceShieldListenerV2 shield0 = HSITurbulanceShieldListenerV2.getInstance(p.node0);
        HSITurbulanceShieldListenerV2 shield1 = HSITurbulanceShieldListenerV2.getInstance(p.node1);
        float level0 = shield0.getShield().getShieldLevel();
        float level1 = shield1.getShield().getShieldLevel();
        if (level0 < 1 || level1 < 1) {
            if (level0 > level1) {
                transferShield(p, shield0, shield1, amount);
                p.setDirection(0);
            } else if (level1 > level0) {
                transferShield(p, shield1, shield0, amount);
                p.setDirection(1);
            }
        }
        if (!p.node0State && shield0.getShield().isShieldRegenBlocked()
                && !(p.node0.getCustomData().containsKey("HSIShieldLinkSpreaded")
                        && (Boolean) p.node0.getCustomData().get("HSIShieldLinkSpreaded"))) {
            shield1.getShield().setShieldRegenBlocked(true);
            p.node1.setCustomData("HSIShieldLinkSpreaded", true);
        }
        if (!p.node1State && shield1.getShield().isShieldRegenBlocked()
                && !(p.node1.getCustomData().containsKey("HSIShieldLinkSpreaded")
                        && (Boolean) p.node1.getCustomData().get("HSIShieldLinkSpreaded"))) {
            shield0.getShield().setShieldRegenBlocked(true);
            p.node0.setCustomData("HSIShieldLinkSpreaded", true);
        }
        if (p.node0.getCustomData().containsKey("HSIShieldLinkSpreaded")) {
            p.node0.setCustomData("HSIShieldLinkSpreaded", false);
        }
        if (p.node1.getCustomData().containsKey("HSIShieldLinkSpreaded")) {
            p.node1.setCustomData("HSIShieldLinkSpreaded", false);
        }
        p.node0State = shield0.getShield().isShieldRegenBlocked();
        p.node1State = shield1.getShield().isShieldRegenBlocked();
    }

    public void transferShield(HSIShieldLinkPair p, HSITurbulanceShieldListenerV2 from,
            HSITurbulanceShieldListenerV2 to, float amount) {
        float expectLevel = (from.getShield().getCurrent() + to.getShield().getCurrent())
                / (from.getShield().getShieldCap() + to.getShield().getShieldCap());
        float transferSpeedLimit = Math.min(
                from.getShield().getBaseShieldRegen() * from.getStats().getShieldRecoveryRate().getModifiedValue(),
                to.getShield().getBaseShieldRegen() * to.getStats().getShieldRecoveryRate().getModifiedValue()) * amount
                * HSIShieldLinkFrame.FACTOR;
        float transferFrom = Math.min(from.getShield().getCurrent() - from.getShield().getShieldCap() * expectLevel,
                transferSpeedLimit);
        from.getShield().takeDamage(transferFrom);
        to.getShield().regenShield(transferFrom);
        p.setTransferredThisPairLastFrame(transferFrom);
    }
}
