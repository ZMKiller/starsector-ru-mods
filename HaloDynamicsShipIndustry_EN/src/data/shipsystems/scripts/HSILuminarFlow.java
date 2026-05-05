package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;

public class HSILuminarFlow extends BaseShipSystemScript {
    private ShipAPI ship;
    private HSITurbulanceShieldListenerV2 shield;
    public static final Color DEFAULT_JITTER_COLOR = new Color(100,165,255,75);
    private float ARMOR_EFFECT_BONUS = 2f;
    private float MIN_ARMOR = 0.3f;

    private float EFFECIENCY = 0.5f;
    private boolean once = true;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        /*if (HSITurbulanceShieldListenerV2.hasShield(ship)) {
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            if (shield != null) {
                shield.getShield().setEngageArmorProcess(true);
            }
            if (once) {
                if (shield.getShield().isShieldRegenBlocked()) {
                    shield.getShield().getRegenCooldownTimer()
                            .setBrightness(Math.min(shield.getShield().getRegenCooldownTimer().getBrightness(), 0.8f));
                }
            }
        }*/

        if (HSITurbulanceShieldListenerV2.hasShield(ship)) {
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            if (shield != null) {
                if(shield.getShield().getShieldLevel()>0){
                    shield.getStats().getShieldEffciency().modifyMult(id,1f-effectLevel*EFFECIENCY);
                }else{
                    stats.getArmorDamageTakenMult().modifyMult(id,1f-effectLevel*EFFECIENCY);
                    stats.getHullDamageTakenMult().modifyMult(id,1f-effectLevel*EFFECIENCY);
                }
            }
        }
        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip(ship.getPhaseCloak().getId() + ship.getId(),
                    "graphics/icons/hullsys/fortress_shield.png",
                    ship.getPhaseCloak().getDisplayName(),
                    HSII18nUtil.getShipSystemString("HSILuminarFlowHints"),
                    false);
        }
        ship.setJitterUnder(ship.getPhaseCloak().getId() + ship.getId(), new Color(100,165,255,255), effectLevel, 15, 0f, 15f);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (HSITurbulanceShieldListenerV2.hasShield(ship)) {
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            if (shield != null) {
               // shield.getShield().setEngageArmorProcess(false);
                shield.getStats().getShieldEffciency().unmodify(id);
            }
        }
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        once = true;
        // stats.getEffectiveArmorBonus().unmodify(id);
        // stats.getMinArmorFraction().unmodify(id);
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        // return shield != null && shield.getShield().getCurrent() > 0;
        return true;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(HSII18nUtil.getShipSystemString("HSILuminarFlowHints"), false);
        }
        return null;
    }

}
