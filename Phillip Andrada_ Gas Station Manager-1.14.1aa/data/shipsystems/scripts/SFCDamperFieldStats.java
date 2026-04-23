package data.shipsystems.scripts;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SFCDamperFieldStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 0.5f;
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 0.33f);
		mag.put(HullSize.FRIGATE, 0.33f);
		mag.put(HullSize.DESTROYER, 0.33f);
		mag.put(HullSize.CRUISER, 0.5f);
		mag.put(HullSize.CAPITAL_SHIP, 0.5f);
	}
	
	protected Object STATUSKEY1 = new Object();
	
	//public static final float INCOMING_DAMAGE_MULT = 0.25f;
	//public static final float INCOMING_DAMAGE_CAPITAL = 0.5f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		effectLevel = 1f;
		
		float mult = (Float) mag.get(HullSize.CRUISER);
		if (stats.getVariant() != null) {
			mult = (Float) mag.get(stats.getVariant().getHullSize());
		}
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		
		
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		if (player) {
			ShipSystemAPI system = getDamper(ship);
			if (system != null) {
				float percent = (1f - mult) * effectLevel * 100;
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
					system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
					(int) Math.round(percent) + "% less damage taken", false);
			}
		}
		float mult2 = ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult2);
	}
	
	public static ShipSystemAPI getDamper(ShipAPI ship) {
//		ShipSystemAPI system = ship.getSystem();
//		if (system != null && system.getId().equals("damper")) return system;
//		if (system != null && system.getId().equals("damper_omega")) return system;
//		if (system != null && system.getSpecAPI() != null && system.getSpecAPI().hasTag(Tags.SYSTEM_USES_DAMPER_FIELD_AI)) return system;
//		return ship.getPhaseCloak();
		ShipSystemAPI system = ship.getPhaseCloak();
		if (system != null && system.getId().equals("damper")) return system;
		if (system != null && system.getId().equals("damper_omega")) return system;
		if (system != null && system.getSpecAPI() != null && system.getSpecAPI().hasTag(Tags.SYSTEM_USES_DAMPER_FIELD_AI)) return system;
		return ship.getSystem();
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
	}
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult2 = ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult2 - 1f) * 100f);
		if (index == 0) {
			return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", true);
		}
		return null;
	}

	
	
//	public StatusData getStatusData(int index, State state, float effectLevel) {
//		float mult = (Float) mag.get(HullSize.CRUISER);
//		if (stats.getVariant() != null) {
//			mult = (Float) mag.get(stats.getVariant().getHullSize());
//		}
//		effectLevel = 1f;
//		float percent = (1f - INCOMING_DAMAGE_MULT) * effectLevel * 100;
//		if (index == 0) {
//			return new StatusData((int) percent + "% less damage taken", false);
//		}
//		return null;
//	}
}
