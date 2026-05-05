package data.shipsystems.scripts;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;
import data.scripts.HSIRenderer.HSICombatRenderer;

public class HSITacTeleport extends BaseShipSystemScript {
    private ShipAPI ship;
    //protected static final float MANU_BUFF = 1000f;
    // private boolean once = true;
    public static float RANGE = 2500f;
    private Vector2f target = null;
    private float tr = 0;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        HSICombatRenderer renderer = HSICombatRenderer.getInstance(ship);
        renderer.setBlockShieldRender(true);
        if (target == null)
            target = new Vector2f(ship.getMouseTarget());
        if (ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)
                && ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS) instanceof Vector2f) {
            target = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
        }

        // ship.setAlphaMult(0f);
        if (state == State.IN) {
            // stats.getTurnAcceleration().modifyPercent(id, MANU_BUFF);
            //stats.getMaxTurnRate().modifyPercent(id, MANU_BUFF);
            stats.getHullDamageTakenMult().modifyMult(id, 1f - effectLevel);
            ship.getVelocity().set(ship.getVelocity().x * 0.95f, ship.getVelocity().y * 0.95f);
            ship.setAngularVelocity(ship.getAngularVelocity() / 1000f);
            if (Math.abs(MathUtils.getShortestRotation(ship.getFacing(),
                    VectorUtils.getAngleStrict(ship.getLocation(), target))) < 1f) {
                // ship.setAlphaMult(0f);
                ship.setAngularVelocity(ship.getAngularVelocity() / 1000f);
                ship.setFacing(VectorUtils.getAngleStrict(ship.getLocation(), target));
                float animLim = (ship.getSystem().getSpecAPI().getIn() - 0.4f) / ship.getSystem().getSpecAPI().getIn();
                if (effectLevel < animLim) {
                    int index = (int) (effectLevel * 100);
                    index = 1 + index % 5;
                    // ship.setSprite(Global.getSettings().getSprite("HSI_Decoration",
                    // "HSI_ZhiYuan_caper00"));
                    if (ship.getHullSpec().getBaseHullId().equals("HSI_ZhiYuan"))
                        renderer.requestFX("HSI_Decoration", "HSI_ZhiYuan_caper0" + index, 1f);
                } else {
                    int index = (int) (Math.ceil((effectLevel - animLim) / (1 - animLim) * 12f) + 1);
                    // ship.setAlphaMult(0f);
                    if (ship.getHullSpec().getBaseHullId().equals("HSI_ZhiYuan"))
                        renderer.requestFX("HSI_Decoration", "HSI_ZhiYuan_caper0" + index, 1f);
                        if (index > 8){
                            ship.setAlphaMult(0f);
                        }else{
                            ship.setAlphaMult(1f);
                        }
                }
            } else {
                if (tr == 0) {
                    float time = ship.getSystem().getChargeUpDur() * (1 - effectLevel) - 0.4f;
                    float dist = MathUtils.getShortestRotation(ship.getFacing(),
                            VectorUtils.getAngleStrict(ship.getLocation(), target));
                    tr = dist / time;
                }
                ship.setFacing(ship.getFacing() + tr * Global.getCombatEngine().getElapsedInLastFrame());
            }
        } else if (state == State.ACTIVE) {
            // ship.setAlphaMult(0f);
            ship.getLocation().set(target);
            // renderer.requestFX("HSI_Decoration", "HSI_ZhiYuan_caper00", 0.2f);
        } else if (state == State.OUT) {
            stats.getMaxSpeed().modifyMult(id, 0f);
            // ship.setAlphaMult(0f);
            // float animLim = (ship.getSystem().getSpecAPI().getOut() - 0.4f) /
            // ship.getSystem().getSpecAPI().getOut();
            /*
             * if (effectLevel > 1 - animLim) {
             * ship.setAlphaMult(0f);
             * stats.getHullDamageTakenMult().modifyMult(id, 0f);
             * renderer.requestFX("HSI_Decoration", "HSI_ZhiYuan_caper00", 0.2f);
             * }
             */
            int index = (int) (Math.ceil(((1 - effectLevel) * 13f)));
            index = 13-index;
            // Global.getLogger(this.getClass()).info("Out"+index);
            stats.getHullDamageTakenMult().modifyMult(id, (1 - effectLevel));
            if (ship.getHullSpec().getBaseHullId().equals("HSI_ZhiYuan")) {
                if (index > 7){
                    ship.setAlphaMult(1f);
                }else{
                    ship.setAlphaMult(0f);
                }
                //renderer.requestFX("HSI_Decoration", "HSI_ZhiYuan_Appeared0" + index, 1f);
                renderer.requestFX("HSI_Decoration", "HSI_ZhiYuan_caper0" + index, 1f);
            }
            // renderer.requestFX("HSI_Decoration", "HSI_ZhiYuan_caper00", 0.2f * (10f -
            // index) / 5f);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        // ship.setSprite(Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()));
        ship.setAlphaMult(1f);
        HSICombatRenderer.getInstance(ship).setBlockShieldRender(false);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        tr = 0;
        target = null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (!isUsable(system, ship))
            return HSII18nUtil.getShipSystemString("HSIOutOfRange");
        return HSII18nUtil.getShipSystemString("HSIUsable");
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isActive())
            return false;
        Vector2f checkfrom = ship.getLocation();
        Vector2f checkto = ship.getMouseTarget();
        if (ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
            checkto = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
        }
        return Misc.getDistance(checkfrom, checkto) - ship.getCollisionRadius() < getRange(ship);
    }

    public static float getRange(ShipAPI ship) {
        if (ship == null)
            return RANGE;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
    }
}
