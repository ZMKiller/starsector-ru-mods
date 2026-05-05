package data.hullmods.ShieldMod;

import com.fs.starfarer.api.ModSpecAPI;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.hullmods.HSITurbulanceShieldListenerV2.ParamType;
import data.kit.HSII18nUtil;

import java.util.ArrayList;
import java.util.List;

public class HSIBaseShieldModEffect extends BaseHullMod implements HSIShieldModEffect {
    protected static List<String> Incompatible = new ArrayList<>();
    static {
        Incompatible.add("HSI_EnhancedShield");
        Incompatible.add("HSI_HardenShield");
    }
    
    public float getApplicationOrder() {
        return 0;
    }

    @Override
    public float processShieldEffectBeforeShieldProcess(float baseDamage, ParamType type, DamageAPI damage, Vector2f point,ShipAPI ship) {
        return baseDamage;
    }

    public float processShieldEffect(float processedDamage, ParamType type, DamageAPI damage, Vector2f point,ShipAPI ship) {
        return processedDamage;
    }

    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        
    }

    public void processEveryFrameShieldEffects(ShipAPI ship, float amount) {

    }

    @Override
    public HullModSpecAPI getSpec() {
        return spec;
    }

    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship.getVariant().hasHullMod("HSI_Halo");
	}

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().hasHullMod("HSI_Halo");
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return HSII18nUtil.getHullModString("HSINoTurbulanceWarning");
    }

    protected String getStandardId(HSITurbulanceShieldListenerV2 shield){
        return  shield.getShip().getId() + spec.getId();
    }

}
