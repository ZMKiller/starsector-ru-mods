package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow;

public class HWIPhenomenonEffect implements EveryFrameWeaponEffectPlugin,OnFireEffectPlugin{
    public static final String KEY = "HWIPhenomenon";

    protected CombatEntityAPI chargeGlowEntity;
	protected HWIPhenomenonGlow chargeGlowPlugin;
	public HWIPhenomenonEffect() {
	}
	
	//protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		//interval.advance(amount);
		
		boolean charging = weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
		if (charging && chargeGlowEntity == null) {
			chargeGlowPlugin = new HWIPhenomenonGlow(weapon);
			chargeGlowEntity = Global.getCombatEngine().addLayeredRenderingPlugin(chargeGlowPlugin);	
		} else if (!charging && chargeGlowEntity != null) {
			chargeGlowEntity = null;
			chargeGlowPlugin = null;
		}
	}
	
	
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		if (chargeGlowPlugin != null) {
			chargeGlowPlugin.attachToProjectile(projectile);
			chargeGlowPlugin = null;
			chargeGlowEntity = null;
			
			MissileAPI missile = (MissileAPI) projectile;
			missile.setMine(true);
			missile.setNoMineFFConcerns(true);
			missile.setMineExplosionRange(RealityDisruptorChargeGlow.MAX_ARC_RANGE + 50f);
			missile.setMinePrimed(true);
			missile.setUntilMineExplosion(0f);
		}
        weapon.getShip().setCustomData(KEY, projectile);
	}
	


    
}
