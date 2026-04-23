package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.dpl_EmergencyForge.dpl_EmergencyForgeScript;

public class dpl_Damage_Absorption extends BaseHullMod {

	public static float DAMAGE_FRACTION_ONE = 0.5f;
	public static float DAMAGE_FRACTION_TWO = 0.99f;
	public static String ABSORPTION_DATA_KEY = "dpl_absorption_data_key";
	public static String TOOK_DAMAGE_KEY = "dpl_DamageAbsorption_TookDam";
	public static String TOOK_DAMAGE_KEY_NOW = "dpl_DamageAbsorption_TookDamNow";
	
	public static final Object KEY_JITTER = new Object();
	public static final Color JITTER_UNDER_COLOR = new Color(125,0,255,125);
	public static final Color JITTER_COLOR = new Color(150,100,255,75);
	
	public float Progress = 0f;
	
	public static class dpl_DamageAbsorptionScript implements HullDamageAboutToBeTakenListener {
		public ShipAPI ship;
		public dpl_DamageAbsorptionScript(ShipAPI ship) {
			this.ship = ship;
		}

		@Override
		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			if (damageAmount > 0) {
				String key = TOOK_DAMAGE_KEY + ship.getId();
				Global.getCombatEngine().getCustomData().put(key, true);
				String key2 = TOOK_DAMAGE_KEY_NOW + ship.getId();
				Global.getCombatEngine().getCustomData().put(key2, true);
			}
			return false;
		}
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = ABSORPTION_DATA_KEY + "_" + ship.getId();
		
		float totalLiveModules = 0f;
		for (ShipAPI module : ship.getChildModulesCopy()) {
			if (!module.isFrigate() || module.getStationSlot() == null || !module.isAlive() || !Misc.isActiveModule(module)) continue;
			totalLiveModules += 1f;
			
			if (Global.getCombatEngine().getCustomData().get(TOOK_DAMAGE_KEY + ship.getId()) != null && Progress <= 1f) {
				float jitterLevel = (float) Math.max(10f*(Progress)*(1-Progress), 0);
				module.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, 5f);
				module.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + 5f);
			}
		}
		
		if (Global.getCombatEngine().getCustomData().get(TOOK_DAMAGE_KEY_NOW + ship.getId()) != null) {
			Progress = 0.5f;
			Global.getCombatEngine().getCustomData().remove(TOOK_DAMAGE_KEY_NOW + ship.getId());
		}
		
		Progress += amount * 0.5f;
		
		if (Progress >= 1f) {
			Global.getCombatEngine().getCustomData().remove(TOOK_DAMAGE_KEY + ship.getId());
		}
		
		if (totalLiveModules >= 1.9f) {
			ship.getMutableStats().getArmorDamageTakenMult().modifyMult(key, 1f-DAMAGE_FRACTION_TWO);
			ship.getMutableStats().getHullDamageTakenMult().modifyMult(key, 1f-DAMAGE_FRACTION_TWO);
			ship.getMutableStats().getEmpDamageTakenMult().modifyMult(key, 1f-DAMAGE_FRACTION_TWO);
		} else if (1.9f > totalLiveModules && totalLiveModules >= 0.9f) {
			ship.getMutableStats().getArmorDamageTakenMult().modifyMult(key, 1f-DAMAGE_FRACTION_ONE);
			ship.getMutableStats().getHullDamageTakenMult().modifyMult(key, 1f-DAMAGE_FRACTION_ONE);
			ship.getMutableStats().getEmpDamageTakenMult().modifyMult(key, 1f-DAMAGE_FRACTION_ONE);
		} else {
			ship.getMutableStats().getArmorDamageTakenMult().unmodifyMult(key);
			ship.getMutableStats().getHullDamageTakenMult().unmodifyMult(key);
			ship.getMutableStats().getEmpDamageTakenMult().unmodifyMult(key);
		}
	}
	
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new dpl_DamageAbsorptionScript(ship));
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round(DAMAGE_FRACTION_TWO * 100f) + "%";
		if (index == 1) return "" + (int) Math.round(DAMAGE_FRACTION_ONE * 100f) + "%";
		return null;
	}
}




