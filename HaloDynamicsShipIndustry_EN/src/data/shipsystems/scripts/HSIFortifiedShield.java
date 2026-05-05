package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;

public class HSIFortifiedShield extends BaseShipSystemScript {
    private ShipAPI ship;
    private CombatEngineAPI engine = Global.getCombatEngine();

    private boolean once = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(HSITurbulanceShieldListenerV2.hasShield(ship)){
            HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            shield.getStats().getShieldEffciency().modifyMult(id, (1f-0.9f*effectLevel));
            ship.setJitterUnder("HSI_FortifiedShield", new Color(200, 75, 75, 255), (effectLevel)*2f, 1, 0f, 15f);
            if(!once){
                once = true;
                shield.getShield().setShieldRegenBlocked(false);
            }
            if(shield.getShield().getShieldLevel()<=0){
                ship.getPhaseCloak().deactivate();
            }
        }
        if (ship.equals(Global.getCombatEngine().getPlayerShip()) ){
            addStatus();
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(HSITurbulanceShieldListenerV2.hasShield(ship)){
            HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            shield.getStats().getShieldEffciency().unmodify(id);
        }
        if(once){
            once = false;
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if(HSITurbulanceShieldListenerV2.hasShield(ship)){
            HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            return shield.getShield().getShieldLevel()>0;
        }else{
            return false;
        }
    }

    protected void addStatus() {
        String content = HSII18nUtil.getShipSystemString("HSIFortifiedShield0");
        Global.getCombatEngine().maintainStatusForPlayerShip("HSIFortifiedShield",
                "graphics/icons/hullsys/fortress_shield.png", HSII18nUtil.getShipSystemString("HSIFortifiedShieldName"), content,
                false);
    }
}
