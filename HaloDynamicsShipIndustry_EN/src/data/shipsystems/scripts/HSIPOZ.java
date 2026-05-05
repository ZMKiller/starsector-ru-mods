package data.shipsystems.scripts;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MoteAIScript;
import com.fs.starfarer.api.impl.combat.MoteControlScript.MoteData;
import com.fs.starfarer.api.impl.combat.MoteControlScript.SharedMoteAIData;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HSIPOZ extends BaseShipSystemScript {

	protected static float MAX_ATTRACTOR_RANGE = 2000f;
	public static float MAX_DIST_FROM_SOURCE_TO_ENGAGE_AS_PD = 2000f;
	public static float MAX_DIST_FROM_ATTRACTOR_TO_ENGAGE_AS_PD = 1000f;

	public static int MAX_MOTES = 15;
	//public static int MAX_MOTES_HF = 50;

	public static float ANTI_FIGHTER_DAMAGE = 200;
	//public static float ANTI_FIGHTER_DAMAGE_HF = 1000;

	public static float ANTI_SHIP_DAMAGE = 200;
	public static float ANTI_SHIP_EMP = 500;

	//public static float ATTRACTOR_DURATION_LOCK = 20f;
	//public static float ATTRACTOR_DURATION = 10f;

	public static Map<String, MoteData> MOTE_DATA = new HashMap<String, MoteData>();

	public static String HSI_LITTLE_STAR = "HWI_Z";
	// public static String MOTELAUNCHER_HF = "motelauncher_hf";

	static {
		MoteData normal = new MoteData();
		normal.jitterColor = new Color(100, 165, 255, 175);
		normal.empColor = new Color(100, 165, 255, 255);
		normal.maxMotes = MAX_MOTES;
		normal.antiFighterDamage = ANTI_FIGHTER_DAMAGE;
		normal.impactSound = "mote_attractor_impact_normal";
		normal.loopSound = "mote_attractor_loop";

		MOTE_DATA.put(HSI_LITTLE_STAR, normal);
	}

	public static String getWeapon() {
		return HSI_LITTLE_STAR;
	}

	public static float getAntiFighterDamage(ShipAPI ship) {
		return MOTE_DATA.get(getWeapon()).antiFighterDamage;
	}

	public static String getImpactSoundId(ShipAPI ship) {
		return MOTE_DATA.get(getWeapon()).impactSound;
	}

	public static Color getJitterColor(ShipAPI ship) {
		return MOTE_DATA.get(getWeapon()).jitterColor;
	}

	public static Color getEMPColor(ShipAPI ship) {
		return MOTE_DATA.get(getWeapon()).empColor;
	}

	public static int getMaxMotes(ShipAPI ship) {
		return MOTE_DATA.get(getWeapon()).maxMotes;
	}

	public static String getLoopSound(ShipAPI ship) {
		return MOTE_DATA.get(getWeapon()).loopSound;
	}

	public static SharedMoteAIData getSharedData(ShipAPI source) {
		String key = source + "_mote_AI_shared";
		SharedMoteAIData data = (SharedMoteAIData) Global.getCombatEngine().getCustomData().get(key);
		if (data == null) {
			data = new SharedMoteAIData();
			Global.getCombatEngine().getCustomData().put(key, data);
		}
		return data;
	}

	protected IntervalUtil launchInterval = new IntervalUtil(0.75f, 1.25f);
	protected IntervalUtil attractorParticleInterval = new IntervalUtil(0.05f, 0.1f);
	protected WeightedRandomPicker<WeaponSlotAPI> launchSlots = new WeightedRandomPicker<WeaponSlotAPI>();
	// protected WeaponSlotAPI attractor = null;

	// protected int empCount = 0;
	protected boolean findNewTargetOnUse = true;

	protected void findSlots(ShipAPI ship) {
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isSystemSlot()) {
					launchSlots.add(slot);
			}
		}
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		float amount = Global.getCombatEngine().getElapsedInLastFrame();

		// Global.getCombatEngine().setPaused(true);

		SharedMoteAIData data = getSharedData(ship);
		data.elapsed += amount;

		CombatEngineAPI engine = Global.getCombatEngine();

		launchInterval.advance(amount );
		if (launchInterval.intervalElapsed()) {
			Iterator<MissileAPI> iter = data.motes.iterator();
			while (iter.hasNext()) {
				if (!engine.isMissileAlive(iter.next())) {
					iter.remove();
				}
			}

			if (ship.isHulk()) {
				for (MissileAPI mote : data.motes) {
					mote.flameOut();
				}
				data.motes.clear();
				return;
			}

			int maxMotes = getMaxMotes(ship);
			if (data.motes.size() < maxMotes && // false &&
					!ship.getFluxTracker().isOverloadedOrVenting()) {
				findSlots(ship);

				WeaponSlotAPI slot = launchSlots.pick();

				Vector2f loc = slot.computePosition(ship);
				float dir = slot.computeMidArcAngle(ship);
				float arc = slot.getArc();
				dir += arc * (float) Math.random() - arc / 2f;

				String weaponId = getWeapon();
				MissileAPI mote = (MissileAPI) engine.spawnProjectile(ship, null,
						weaponId,
						loc, dir, null);
				mote.setWeaponSpec(weaponId);
				mote.setMissileAI(new MoteAIScript(mote));
				mote.getActiveLayers().remove(CombatEngineLayers.FF_INDICATORS_LAYER);
				mote.setEmpResistance(10000);
				data.motes.add(mote);

				engine.spawnMuzzleFlashOrSmoke(ship, slot, mote.getWeaponSpec(), 0, dir);

				Global.getSoundPlayer().playSound("mote_attractor_launch_mote", 1f, 0.25f, loc, new Vector2f());
			}
		}

		if (effectLevel >= 1) {
			Iterator<MissileAPI> motes = data.motes.iterator();
			for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, getRange(ship))) {
				for (int i = 0; i < enemy.getHullSize().ordinal(); i++) {
					if (motes.hasNext()) {
						MissileAPI mote = motes.next();
						engine.spawnEmpArc(ship, mote.getLocation(), mote, enemy, DamageType.ENERGY,
								ANTI_SHIP_DAMAGE, ANTI_SHIP_EMP,
								getRange(ship), "tachyon_lance_emp_impact", 18f, new Color(25, 100, 155, 255),
								new Color(255, 255, 255, 255));
						mote.flameOut();
					}
				}
				if(!motes.hasNext()) break;
			}
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}

	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}

	/*public Vector2f getTargetedLocation(ShipAPI from) {
		Vector2f loc = from.getSystem().getTargetLoc();
		if (loc == null) {
			loc = new Vector2f(from.getMouseTarget());
		}
		return loc;
	}*/

	public static float getRange(ShipAPI ship) {
		if (ship == null)
			return MAX_ATTRACTOR_RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(MAX_ATTRACTOR_RANGE);
	}

	public int getMaxMotes() {
	return MAX_MOTES;
	}

}
