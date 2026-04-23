package data.scripts.weapons;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.TimeoutTracker;

public class dpl_weak_spotEffect implements BeamEffectPlugin {

	public static float EFFECT_DUR = 1f;
	
	public static float DAMAGE_PERCENT_ONE = 1.025f;
	public static float DAMAGE_PERCENT_TWO = 1.05f;
	public static float DAMAGE_PERCENT_THREE = 1.075f;
	public static float DAMAGE_PERCENT_FOUR = 1.1f;
	
	protected boolean wasZero = true;
	
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f && beam.getWeapon() != null) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			
			// beam tick, apply damage modifier effect if needed
			if (dur > 0) {
				ShipAPI ship = (ShipAPI) target;
				if (!ship.hasListenerOfClass(ChantsDamageTakenMod.class)) {
					ship.addListener(new ChantsDamageTakenMod(ship));
				}
				List<ChantsDamageTakenMod> listeners = ship.getListeners(ChantsDamageTakenMod.class);
				if (listeners.isEmpty()) return; // ???
					
				ChantsDamageTakenMod listener = listeners.get(0);
				listener.notifyHit(beam.getWeapon());
			}
		}
	}
	

	public static String DAMAGE_MOD_ID = "dpl_weak_spot_damage_mod";

	public static class ChantsDamageTakenMod implements AdvanceableListener {
							//implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected TimeoutTracker<WeaponAPI> recentHits = new TimeoutTracker<WeaponAPI>();
		public ChantsDamageTakenMod(ShipAPI ship) {
			this.ship = ship;
			//ship.addListener(new GravitonBeamDamageTakenModRemover(ship));
		}
		
		public void notifyHit(WeaponAPI w) {
			recentHits.add(w, EFFECT_DUR, EFFECT_DUR);
			
		}
		
		public void advance(float amount) {
			recentHits.advance(amount);
			
			int beams = recentHits.getItems().size();

			float bonus = 0;
			if (beams == 1) {
				bonus = DAMAGE_PERCENT_ONE;
			} else if (beams == 2) {
				bonus = DAMAGE_PERCENT_TWO;
			} else if (beams >= 3) {
				bonus = DAMAGE_PERCENT_THREE;
			} else if (beams >= 4) {
				bonus = DAMAGE_PERCENT_FOUR;
			}
			
			if (bonus > 0) {
				ship.getMutableStats().getHullDamageTakenMult().modifyMult(DAMAGE_MOD_ID, bonus);
				ship.getMutableStats().getArmorDamageTakenMult().modifyMult(DAMAGE_MOD_ID, bonus);
				ship.getMutableStats().getShieldDamageTakenMult().modifyMult(DAMAGE_MOD_ID, bonus);
				ship.getMutableStats().getEmpDamageTakenMult().modifyMult(DAMAGE_MOD_ID, bonus);

			} else {
				ship.removeListener(this);
				ship.getMutableStats().getHullDamageTakenMult().unmodify(DAMAGE_MOD_ID);
				ship.getMutableStats().getArmorDamageTakenMult().unmodify(DAMAGE_MOD_ID);
				ship.getMutableStats().getShieldDamageTakenMult().unmodify(DAMAGE_MOD_ID);
				ship.getMutableStats().getEmpDamageTakenMult().unmodify(DAMAGE_MOD_ID);
			}
		}

	}

}
