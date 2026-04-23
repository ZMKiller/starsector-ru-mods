package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class dpl_black_holeEffect implements OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		Color color = RiftCascadeEffect.STANDARD_RIFT_COLOR;
		Object o = projectile.getWeapon().getSpec().getProjectileSpec();
		if (o instanceof MissileSpecAPI) {
			MissileSpecAPI spec = (MissileSpecAPI) o;
			color = spec.getExplosionColor();
		}
		
		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color, 40f);
		p.fadeOut = 2f;
		p.hitGlowSizeMult = 1f;
		// want a red rift, but still blue for subtracting from the red clouds
		// or not - actually looks better with the red being inverted and subtracted
		// despite this not matching the trail
		//p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
		RiftCascadeMineExplosion.spawnStandardRift(projectile, p);
		
		Vector2f vel = new Vector2f();
		if (target != null) vel.set(target.getVelocity());
		Global.getSoundPlayer().playSound("rifttorpedo_explosion", 1f, 1f, point, vel);
		
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (!shieldHit) {
				float dam = projectile.getDamageAmount();
				ship.setHitpoints(Math.max(ship.getHitpoints() - dam, 1));
				engine.addFloatingDamageText(point, dam, Misc.FLOATY_HULL_DAMAGE_COLOR, ship, projectile.getSource());
				if (ship.getHitpoints() <= 10 && !ship.getVariant().hasHullMod("vastbulk")) {
					//Just kill it if its hull point is too low.
					ship.getMutableStats().getHullDamageTakenMult().unmodify();
					ship.getMutableStats().getArmorDamageTakenMult().unmodify();
					ship.setHitpoints(1f);
			        int[] cell = ship.getArmorGrid().getCellAtLocation(point);
			        ship.getArmorGrid().setArmorValue(cell[0], cell[1], 0f);
					engine.applyDamage(ship, point, 50000f, DamageType.OTHER, 0f, true, false, null);
				}
            }
		}
	}
}




