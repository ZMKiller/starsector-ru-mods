package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.HashMap;
import java.util.Map;

public class SFCRamDriveStats extends BaseShipSystemScript {
    private static Map mag = new HashMap();
    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0.70f);
        mag.put(ShipAPI.HullSize.FRIGATE, 0.70f);
        mag.put(ShipAPI.HullSize.DESTROYER, 0.70f);
        mag.put(ShipAPI.HullSize.CRUISER, 0.85f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.85f);
    }
    private Float mass = null;

    protected Object STATUSKEY1 = new Object();

    //public static final float INCOMING_DAMAGE_MULT = 0.25f;
    //public static final float INCOMING_DAMAGE_CAPITAL = 0.5f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        effectLevel = 1f;

        float mult = (Float) mag.get(ShipAPI.HullSize.CRUISER);
        if (stats.getVariant() != null) {
            mult = (Float) mag.get(stats.getVariant().getHullSize());
        }
        stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);


        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        }
        if (player) {
            ShipSystemAPI system = getDamper(ship);
            if (system != null) {
                float percent = (1f - mult) * effectLevel * 100;
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
                        system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
                        (int) Math.round(percent) + "% less damage taken", false);
            }
        }
        if (ship == null) {
            return;
        }
        if (mass == null) {
            mass = ship.getMass();
        }
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 1200f * effectLevel);
        }
    }

    public static ShipSystemAPI getDamper(ShipAPI ship) {
//		ShipSystemAPI system = ship.getSystem();
//		if (system != null && system.getId().equals("damper")) return system;
//		if (system != null && system.getId().equals("damper_omega")) return system;
//		if (system != null && system.getSpecAPI() != null && system.getSpecAPI().hasTag(Tags.SYSTEM_USES_DAMPER_FIELD_AI)) return system;
//		return ship.getPhaseCloak();
        ShipSystemAPI system = ship.getPhaseCloak();
        if (system != null && system.getId().equals("damper")) return system;
        if (system != null && system.getId().equals("damper_omega")) return system;
        if (system != null && system.getSpecAPI() != null && system.getSpecAPI().hasTag(Tags.SYSTEM_USES_DAMPER_FIELD_AI)) return system;
        return ship.getSystem();
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased engine power", false);
        }
        return null;
    }
}