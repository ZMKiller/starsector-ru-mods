package data.hullmods.ShieldMod;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.hullmods.HSITurbulanceShieldListenerV2.ParamType;

public interface HSIShieldModEffect extends HullModEffect{
    public float getApplicationOrder();

    public float processShieldEffectBeforeShieldProcess(float baseDamage,ParamType type,DamageAPI damage,Vector2f point,ShipAPI ship);
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield);
    public float processShieldEffect(float processedDamage,ParamType type,DamageAPI damage,Vector2f point,ShipAPI ship);
    public void processEveryFrameShieldEffects(ShipAPI ship,float amount);
    public HullModSpecAPI getSpec();
}
