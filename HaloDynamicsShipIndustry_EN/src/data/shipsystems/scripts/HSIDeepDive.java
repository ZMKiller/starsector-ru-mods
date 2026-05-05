package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.kit.HSII18nUtil;

public class HSIDeepDive extends BaseShipSystemScript {
    private ShipAPI ship;
    // private static final float SUBSPACE_DAMAGE_REDUCTION = -0.33f;

    public static final String DEEP_DIVE = "HSI_DeepDive_Key";
    private static final float ALPHA_MULT = 0.5f;

    private static final float SPEED_BUFF = 100f;

    private static final float MANUVER_BUFF = 200f;
    private boolean once = false;
    private HullSize shipSize;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(!once){
            once = true;
            shipSize = ship.getHullSize();
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipSystemAPI cloak = ship.getPhaseCloak();
        if (cloak == null)
            cloak = ship.getSystem();
        if (state == State.IDLE || state == State.COOLDOWN) {

            unapply(stats, id);
            if (ship.getCollisionClass() == CollisionClass.NONE && ship.isFighter())
                ship.setCollisionClass(CollisionClass.FIGHTER);
            if (ship.getCollisionClass() == CollisionClass.NONE && !ship.isFighter())
                ship.setCollisionClass(CollisionClass.SHIP);
            return;
        }
        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {
            if (ship.getCollisionClass() != CollisionClass.NONE)
                ship.setCollisionClass(CollisionClass.NONE);
            ship.setExtraAlphaMult(Math.max(0,1 - ((ship.getOwner() == 0) ? ALPHA_MULT : 0.9f) * effectLevel) );
            ship.setApplyExtraAlphaToEngines(true);
            ship.setForceHideFFOverlay(true);
            ship.setDrone(true);
            ship.setCustomData(DEEP_DIVE,true);
            if (engine.getPlayerShip() != null && engine.getPlayerShip().getShipTarget() == ship) {
                engine.getPlayerShip().setShipTarget(null);
            }
            if (effectLevel>0) {
                stats.getHullDamageTakenMult().modifyMult(id, 0f);
            }
            ship.setHullSize(HullSize.FIGHTER);
        }
        ship.getMutableStats().getMaxSpeed().modifyPercent(id,SPEED_BUFF*effectLevel);
        ship.getMutableStats().getAcceleration().modifyPercent(id,MANUVER_BUFF*effectLevel);
        ship.getMutableStats().getDeceleration().modifyPercent(id,MANUVER_BUFF*effectLevel);
        ship.getMutableStats().getMaxTurnRate().modifyPercent(id,MANUVER_BUFF*effectLevel);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(id,MANUVER_BUFF*effectLevel);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        HSIDeepDiveEnginePlugin.getInstance(Global.getCombatEngine());
        ShipSystemAPI cloak = ship.getPhaseCloak();
        if (cloak == null)
            cloak = ship.getSystem();
        ship.setAlphaMult(1f);
        stats.getHullDamageTakenMult().unmodify(id);
        ship.setForceHideFFOverlay(false);
        ship.setDrone(false);
        ship.setHullSize(shipSize);
        ship.setCustomData(DEEP_DIVE,false);
        ship.getMutableStats().getMaxSpeed().unmodify(id);
        ship.getMutableStats().getAcceleration().unmodify(id);
        ship.getMutableStats().getDeceleration().unmodify(id);
        ship.getMutableStats().getMaxTurnRate().unmodify(id);
        ship.getMutableStats().getTurnAcceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(HSII18nUtil.getShipSystemString("HSIDeepDive"), false);
        }
        return null;
    }

}
