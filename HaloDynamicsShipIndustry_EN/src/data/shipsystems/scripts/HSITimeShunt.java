package data.shipsystems.scripts;

import java.util.ArrayList;
import java.util.List;

import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;

public class HSITimeShunt extends BaseShipSystemScript {
    public static enum BOOSTERMODE {
        HANGAR, WEAPON, FIGHTER;

        public BOOSTERMODE next() {
            switch (this) {
                case HANGAR:
                    return WEAPON;
                case WEAPON:
                    return FIGHTER;
                case FIGHTER:
                    return HANGAR;
                default:
                    return this;
            }
        }
    }

    private BOOSTERMODE mode = BOOSTERMODE.HANGAR;
    protected static final float REFIT_BUFF = 0.8f;
    protected static final float WEAPON_BUFF = 20f;
    protected static final float FIGHTER_BUFF = 20f;
    public static final Color CONTRAIL = new Color(125,175,255,255);
    protected HSITimeShuntRenderer renderer = new HSITimeShuntRenderer();
    public boolean once = true;

    private List<HSITimeBoosterFx> fxs = new ArrayList<>();

    public class HSITimeBoosterFx {
        private float elapsed;
        private Vector2f location;// relative
        private ShipAPI attached;
        private float size;
        private static final float Dur = 0.8f;
        private FaderUtil fader = new FaderUtil(0f, Dur);
        private WaveDistortion IO;
        protected final SpriteAPI sprite;

        // private WaveDistortion CON;

        public HSITimeBoosterFx(Vector2f location, ShipAPI attached, float size) {
            this.location = location;
            this.attached = attached;
            this.size = size;
            this.elapsed = 0;
            fader.fadeIn();
            IO = new WaveDistortion(
                    AjimusUtils.getEngineCoordFromRelativeCoord(attached.getLocation(), location, attached.getFacing()),
                    new Vector2f(0, 0));
            IO.setSize(size * 1.5f);
            IO.fadeOutIntensity(Dur);
            sprite = Global.getSettings().getSprite("HSI_fx", "ContrailTough");
        }

        public void advance(float amount) {
            if (!attached.isAlive()) {
                notifyEnding();
            }
            if (Global.getCombatEngine().isPaused())
                return;
            elapsed += amount;
            if (!fader.isIdle()) {
                fader.advance(amount);
                IO.setLocation(AjimusUtils.getEngineCoordFromRelativeCoord(attached.getLocation(), location,
                        attached.getFacing()));
            }
        }

        public void render() {
            float startAngle = MathUtils.clampAngle(elapsed * 180f);
            float alphaM = fader.getBrightness();
            int segNum = Math.min(30, (int) (elapsed * 30f));
            float segLength = 6f;
            float currAngle = startAngle;
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            sprite.bindTexture();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            float textLength = sprite.getWidth();
            float textProgress = 0;
            for (int i = 0; i < segNum - 1; i++) {
                Vector2f mp0;
                Vector2f mp1;
                mp0 = Vector2f.add(
                        AjimusUtils.getEngineCoordFromRelativeCoord(attached.getLocation(), location,
                                attached.getFacing()),
                        (Vector2f) (Misc.getUnitVectorAtDegreeAngle(currAngle).scale(size / 2)), null);
                currAngle = MathUtils.clampAngle(currAngle - segLength);
                mp1 = Vector2f.add(
                        AjimusUtils.getEngineCoordFromRelativeCoord(attached.getLocation(), location,
                                attached.getFacing()),
                        (Vector2f) (Misc.getUnitVectorAtDegreeAngle(currAngle).scale(size / 2)), null);
                float linkAngle = VectorUtils.getAngle(mp0, mp1);
                float width = size / 6f;
                float length = MathUtils.getDistance(mp0, mp1);
                Vector2f v0 = MathUtils.getPointOnCircumference(mp0, width, linkAngle + 90f);
                Vector2f v1 = MathUtils.getPointOnCircumference(mp0, width, linkAngle - 90f);
                GL11.glColor4ub((byte) CONTRAIL.getRed(), (byte) CONTRAIL.getGreen(), (byte) CONTRAIL.getBlue(),
                        (byte) (CONTRAIL.getAlpha() * alphaM * (1 - (float) i / (float) segNum)));
                GL11.glTexCoord2f(textProgress, 0f);
                GL11.glVertex2f(v0.x, v0.y);
                GL11.glTexCoord2f(textProgress, 1f);
                GL11.glVertex2f(v1.x, v1.y);
                textProgress += length / textLength;

            }
            GL11.glEnd();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }

        public void notifyEnding() {
            fader.fadeOut();
        }

        public boolean isEnded() {
            return fader.isFadedOut();
        }
    }

    public class HSITimeShuntRenderer extends BaseCombatLayeredRenderingPlugin {
        protected boolean shouldExpire = false;

        public HSITimeShuntRenderer(){
            this.layer = CombatEngineLayers.ABOVE_PARTICLES_LOWER;
            Global.getCombatEngine().addLayeredRenderingPlugin(this);
        }

        public void advance(float amount){
            if(Global.getCombatEngine().isPaused()) return;
            if (!fxs.isEmpty()) {
                List<HSITimeBoosterFx> toRemove = new ArrayList<>();
                for (HSITimeBoosterFx bfx : fxs) {
                    if (bfx.isEnded()) {
                        toRemove.add(bfx);
                        continue;
                    }
                    bfx.advance(amount);
                }
                fxs.removeAll(toRemove);
            }
        }

        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (this.layer == layer) {
                for (HSITimeBoosterFx c : fxs) {
                    c.render();
                }
            }
        }

        public float getRenderRadius() {
            return 10000f;
        }

        public boolean shouldExpire() {
            return shouldExpire;
        }

        public boolean isExpired() {
            return shouldExpire();
        }
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI s = (ShipAPI) stats.getEntity();
            s.setCustomData("HSITimeBoosterMode", mode);
        }
        applyModeEffectWithoutFX(stats, id);
        if (effectLevel >= 1) {
            clearLastModeEffect(stats, id);
            mode = mode.next();
            applyModeEffect(stats, id);
        }  
    }

    private void clearLastModeEffect(MutableShipStatsAPI stats, String id) {
        switch (mode) {
            case HANGAR:
                stats.getFighterRefitTimeMult().unmodify(id);
                break;
            case WEAPON:
                stats.getBallisticRoFMult().unmodify(id);
                stats.getEnergyRoFMult().unmodify(id);
                break;
            case FIGHTER:
                if (stats.getEntity() instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) stats.getEntity();
                    for (FighterWingAPI wing : s.getAllWings()) {
                        for (ShipAPI f : wing.getWingMembers()) {
                            f.getMutableStats().getTimeMult().unmodify(id);
                        }
                    }
                }
                break;
        }
        for (HSITimeBoosterFx fx : fxs) {
            fx.notifyEnding();
        }
    }

    private void applyModeEffect(MutableShipStatsAPI stats, String id) {
        switch (mode) {
            case HANGAR:
                stats.getFighterRefitTimeMult().modifyMult(id, REFIT_BUFF);
                if (stats.getEntity() instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) stats.getEntity();
                    for (FighterLaunchBayAPI bay : s.getLaunchBaysCopy()) {
                        fxs.add(new HSITimeBoosterFx(new Vector2f(bay.getWeaponSlot().getLocation()), s, 20f));
                    }
                }
                break;
            case WEAPON:
                stats.getBallisticRoFMult().modifyPercent(id, WEAPON_BUFF);
                stats.getEnergyRoFMult().modifyPercent(id, WEAPON_BUFF);
                stats.getBallisticWeaponFluxCostMod().modifyMult(id, (100-WEAPON_BUFF)/100f);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (100-WEAPON_BUFF)/100f);
                if (stats.getEntity() instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) stats.getEntity();
                    for (WeaponAPI w : s.getAllWeapons()) {
                        if (w.isDecorative() || w.getSlot().getWeaponType().equals(WeaponType.STATION_MODULE)
                                || w.getSlot().getWeaponType().equals(WeaponType.SYSTEM))
                            continue;
                        fxs.add(new HSITimeBoosterFx(new Vector2f(w.getSlot().getLocation()), s,
                                Math.max(w.getSprite().getHeight(),(w.getSize().ordinal() + 1) * 20f)));
                    }
                }
                break;
            case FIGHTER:
                if (stats.getEntity() instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) stats.getEntity();
                    for (FighterWingAPI wing : s.getAllWings()) {
                        for (ShipAPI f : wing.getWingMembers()) {
                            f.getMutableStats().getTimeMult().modifyPercent(id, FIGHTER_BUFF);
                            fxs.add(new HSITimeBoosterFx(new Vector2f(0, 0), f, f.getCollisionRadius() * 1.8f));
                        }
                    }
                }
                break;
        }
    }

    private void applyModeEffectWithoutFX(MutableShipStatsAPI stats, String id) {
        switch (mode) {
            case HANGAR:
                stats.getFighterRefitTimeMult().modifyMult(id, REFIT_BUFF);
                break;
            case WEAPON:
                stats.getBallisticRoFMult().modifyPercent(id, WEAPON_BUFF);
                stats.getEnergyRoFMult().modifyPercent(id, WEAPON_BUFF);
                stats.getBallisticWeaponFluxCostMod().modifyMult(id, (100-WEAPON_BUFF)/100f);
                stats.getEnergyWeaponFluxCostMod().modifyMult(id, (100-WEAPON_BUFF)/100f);
                break;
            case FIGHTER:
                if (stats.getEntity() instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) stats.getEntity();
                    for (FighterWingAPI wing : s.getAllWings()) {
                        for (ShipAPI f : wing.getWingMembers()) {
                            f.getMutableStats().getTimeMult().modifyPercent(id, FIGHTER_BUFF);
                            boolean shouldadd = true;
                            for(HSITimeBoosterFx fx:fxs){
                                shouldadd = (!(fx.attached==f))&&shouldadd;
                            }
                            if(shouldadd) fxs.add(new HSITimeBoosterFx(new Vector2f(0, 0), f, f.getCollisionRadius() * 1.8f));
                        }
                    }
                }
                break;
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            if (mode == BOOSTERMODE.HANGAR) {
                return new StatusData(
                        HSII18nUtil.getShipSystemString("HSITimeBoosterHangar") + ":" + (int) ((100 - REFIT_BUFF* 100f) )
                                + "%",
                        false);
            }
            if (mode == BOOSTERMODE.WEAPON) {
                return new StatusData(
                        HSII18nUtil.getShipSystemString("HSITimeBoosterWeapon") + ":" + (int) (WEAPON_BUFF) + "%",
                        false);
            }
            if (mode == BOOSTERMODE.FIGHTER) {
                return new StatusData(
                        HSII18nUtil.getShipSystemString("HSITimeBoosterFighter") + ":" + (int) (FIGHTER_BUFF) + "%",
                        false);
            }
        }
        if(index==1){
            if (mode == BOOSTERMODE.WEAPON) {
                return new StatusData(
                        HSII18nUtil.getShipSystemString("HSITimeBoosterWeapon2") + ":" + (int) (WEAPON_BUFF) + "%",
                        false);
            }
        }
        return null;
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (mode == BOOSTERMODE.HANGAR) {
            return HSII18nUtil.getShipSystemString("HSITimeBoosterModeHangar");
        }
        if (mode == BOOSTERMODE.WEAPON) {
            return HSII18nUtil.getShipSystemString("HSITimeBoosterModeWeapon");
        }
        if (mode == BOOSTERMODE.FIGHTER) {
            return HSII18nUtil.getShipSystemString("HSITimeBoosterModeFighter");
        }
        return null;
    }

}
