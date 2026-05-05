package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import data.hullmods.ShieldMod.HSIBaseShieldModEffect;
import data.hullmods.ShieldMod.HSIShieldModEffect;
import org.lwjgl.util.vector.Vector2f;

public class HSIStealthDesign extends HSIBaseShieldModEffect {

    private static float SENSOR_BUFF = 75f;
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorProfile().modifyMult(id,(100f-SENSOR_BUFF)/100f);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return (int)SENSOR_BUFF+"%";
        return null;
    }

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldCap().modifyMult("HSI_Stealth_Design_Debuff",0.66f);
    }
}
