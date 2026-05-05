package data.ai;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class HSITorpedoFactoryAI implements ShipSystemAIScript {
	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipwideAIFlags flags;
	private ShipSystemAPI system;

	private IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
	}

	private float bestFractionEver = 0f;
	private float sinceLast = 0f;

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);

		sinceLast += amount;

		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0)
				return;
			if (system.isOutOfAmmo())
				return;
			if (system.isActive())
				return;

			if (target == null)
				return;

			//float maxCooldown = 0f;
			//float totalCooldownSaved = 0f;

			//float totalWeight = 0f;
			//float totalReloadWeight = 0f;
			List<WeaponAPI> weapons = ship.getAllWeapons();

			float currAmmo = 0;
			float maxAmmo = 0;
			float fluxLevel = ship.getFluxTracker().getFluxLevel();

			float remainingFluxLevel = 1f - fluxLevel;

			float fluxFractionPerUse = system.getFluxPerUse() / ship.getFluxTracker().getMaxFlux();
			if (fluxFractionPerUse > remainingFluxLevel)
				return;
			for (WeaponAPI weapon : weapons) {
				//float weight = 0;
				//WeaponSize size = weapon.getSize();
				//if (size == WeaponSize.SMALL)
					//weight = 1;
				//else if (size == WeaponSize.MEDIUM)
					//weight = 2;
				//else if (size == WeaponSize.LARGE)
					//weight = 4;

				//if (weapon.getType() != WeaponType.MISSILE)
					//weight = 0;

				//totalWeight += weight;
				if (weapon.getCooldown() < 2f)
					continue;
				if (weapon.isInBurst())
					return;
				if (weapon.usesAmmo() && !weapon.isBeam() && weapon.getSpec().getProjectileSpec() instanceof MissileSpecAPI) {
					MissileSpecAPI spec = (MissileSpecAPI) weapon.getSpec().getProjectileSpec();
					if (spec.getTypeString().equals("ROCKET")
							|| spec.getTypeString().equals("MISSILE_TWO_STAGE_SECOND_UNGUIDED")
							|| spec.getTypeString().equals("PHASE_CHARGE") || spec.getTypeString().equals("BOMB")) {
						currAmmo += weapon.getAmmo();
						maxAmmo += weapon.getMaxAmmo();

					}
				}

				//maxCooldown += weapon.getCooldown();
				//totalCooldownSaved += weapon.getCooldownRemaining();

				//totalReloadWeight += weight;
			}

			//if ((maxCooldown <= 0 || totalCooldownSaved <= 0) && totalWeight <= 0f)
				//return;

			//float reloadSignificance = totalReloadWeight / totalWeight;


			//boolean targetIsVulnerable = target.getFluxTracker().isOverloadedOrVenting() && (target.getFluxTracker().getOverloadTimeRemaining() > 5f || target.getFluxTracker().getTimeToVent() > 5f);

			//if (targetIsVulnerable)
				//reloadSignificance *= 2f;



			//float fluxLevelAfterUse = fluxLevel + fluxFractionPerUse;
			//if (fluxLevelAfterUse > reloadSignificance || (fluxLevelAfterUse > 0.9f && fluxFractionPerUse > 0.025f))
				//return;

			//if (!targetIsVulnerable && sinceLast < 10f)
				//return;

			//float fraction = totalCooldownSaved / maxCooldown;

			//if (reloadSignificance >= 0.5f) {
				//ship.useSystem();
				//sinceLast = 0f;
				//return;
				
			//}

			if (currAmmo / maxAmmo <= 0.7f) {
				ship.useSystem();
				sinceLast = 0f;
				//return;
			}

		}
	}
}
