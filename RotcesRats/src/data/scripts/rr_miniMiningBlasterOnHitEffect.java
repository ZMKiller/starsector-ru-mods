package data.scripts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.BreachOnHitEffect;

public class rr_miniMiningBlasterOnHitEffect implements OnHitEffectPlugin {

	public static float DAMAGE = 40;

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (!shieldHit && target instanceof ShipAPI) {
			
			DAMAGE = projectile.getDamageAmount() * 0.4f;
			
			BreachOnHitEffect.dealArmorDamage(projectile, (ShipAPI) target, point, DAMAGE);
		}
	}
}
