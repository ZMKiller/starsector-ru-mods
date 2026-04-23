package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

public class dpl_WeaponOvercharge extends BaseHullMod {
	
	private static final float HULL_MULT = 0.8f;
	private static final float DAMAGE_BONUS_PERCENT = 10f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyMult(id, HULL_MULT);
		stats.getArmorBonus().modifyMult(id, HULL_MULT);
		stats.getShieldDamageTakenMult().modifyMult(id, 2f-HULL_MULT);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new dpl_WeaponOverrideDamageDealtMod(ship));
	}



	public static class dpl_WeaponOverrideDamageDealtMod implements DamageDealtModifier {
		protected ShipAPI ship;
		protected float dam;
		public dpl_WeaponOverrideDamageDealtMod(ShipAPI ship) {
			this.ship = ship; 
			dam = DAMAGE_BONUS_PERCENT;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			if (param != null) {
				damage.getModifier().modifyMult("dpl_WeaponOvercharge", 1f + dam * 0.01f);
			}
			return null;
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(DAMAGE_BONUS_PERCENT) + "%";
		if (index == 1) return "" + (int) Math.round((1f - HULL_MULT) * 100f) + "%";
		if (index == 2) return "" + (int) Math.round((1f - HULL_MULT) * 100f) + "%";
		if (index == 3) return "" + (int) Math.round((1f - HULL_MULT) * 100f) + "%";
//		if (index == 4) return "" + (int) ACCELERATION_BONUS;
//		//if (index == 5) return "four times";
//		if (index == 5) return "4" + Strings.X;
//		if (index == 6) return "" + BURN_LEVEL_BONUS;
		return null;
	}
	

}
