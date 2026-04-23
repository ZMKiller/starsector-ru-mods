package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_doom_cannonEffect implements OnHitEffectPlugin {

	private IntervalUtil fireInterval = new IntervalUtil(0.05f, 0.1f);
	private boolean wasZero = true;

	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (!shieldHit) {
				float dam = projectile.getDamageAmount();
				engine.applyDamage(target, point, dam, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), false);
            }
		}
		
	}
}
