package data.hullmods;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.dpl_PlasmaCoupling.dpl_PlasmaCouplingDamageDealtMod;

public class dpl_EntangledSystem extends BaseHullMod {
	
	public static final float DAMAGE_FACTOR = 10f;
	public static final float PENALTY_FACTOR = 100f-DAMAGE_FACTOR;
	
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if(ship.getVariant().hasDMods()) {
			ship.addListener(new dpl_DamageMod(ship));
		}
	}
	
	public static class dpl_DamageMod implements DamageDealtModifier {
		protected ShipAPI ship;
		public dpl_DamageMod(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			damage.setDamage(damage.getDamage()*DAMAGE_FACTOR*0.01f);
			return null;
		}
	}
	
	public void advanceInCombat(ShipAPI ship, float amount) {
		if(ship.getVariant().hasDMods()) {
			ship.setShipSystemDisabled(true);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) PENALTY_FACTOR + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null; 
	}

}
