package data.shipsystems.scripts;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;

import data.hullmods.HSIEGO.HSIEGOStats;
import data.kit.HSII18nUtil;

public class HSIReloadDive extends BaseShipSystemScript {
    public static Color JITTER_COLOR = new Color(245, 245, 245, 255);
    public static float JITTER_FADE_TIME = 0.5f;

    public static float SHIP_ALPHA_MULT = 0.25f;
    // public static float VULNERABLE_FRACTION = 0.875f;
    public static float VULNERABLE_FRACTION = 0f;
    public static float INCOMING_DAMAGE_MULT = 0.25f;

    public static float MAX_TIME_MULT = 3f;

    public static boolean FLUX_LEVEL_AFFECTS_SPEED = true;
    public static float MIN_SPEED_MULT = 0.66f;
    public static float BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f;

    protected Object STATUSKEY1 = new Object();
    protected Object STATUSKEY2 = new Object();
    protected Object STATUSKEY3 = new Object();
    protected Object STATUSKEY4 = new Object();
    private boolean unapplyed = true;
    protected IntervalUtil butterflyTimer = new IntervalUtil(0.5f, 0.5f);
    protected float level = 0;
    protected CombatEngineAPI engine = Global.getCombatEngine();
    protected List<WeaponAPI> Freischutz = new ArrayList<>();

    public static float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }

    protected boolean isDisruptable(ShipSystemAPI cloak) {
        return cloak.getSpecAPI().hasTag(Tags.DISRUPTABLE);
    }

    protected float getDisruptionLevel(ShipAPI ship) {
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            float threshold = ship.getMutableStats().getDynamic().getMod(
                    Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED);
            if (threshold <= 0)
                return 1f;
            float level = ship.getHardFluxLevel() / threshold;
            if (level > 1f)
                level = 1f;
            return level;
        }
        return 0f;
    }

    protected void maintainStatus(ShipAPI playerShip, State state, float effectLevel) {
        float level = effectLevel;
        float f = VULNERABLE_FRACTION;

        ShipSystemAPI cloak = playerShip.getPhaseCloak();
        if (cloak == null)
            cloak = playerShip.getSystem();
        if (cloak == null)
            return;

        if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (level > f) {
                if (getDisruptionLevel(playerShip) <= 0f) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
                            cloak.getSpecAPI().getIconSpriteName(), HSII18nUtil.getShipSystemString("HSIPhaseText0"),
                            HSII18nUtil.getShipSystemString("HSIPhaseText2") + "100%", false);
                } else {
                    String speedPercentStr = (int) Math.round(getSpeedMult(playerShip, effectLevel) * 100f) + "%";
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
                            cloak.getSpecAPI().getIconSpriteName(),
                            // "phase coils at " + disruptPercent,
                            HSII18nUtil.getShipSystemString("HSIPhaseText1"),
                            HSII18nUtil.getShipSystemString("HSIPhaseText2") + speedPercentStr, true);
                }
            }
        }
    }

    public float getSpeedMult(ShipAPI ship, float effectLevel) {
        if (getDisruptionLevel(ship) <= 0f)
            return 1f;
        return MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(ship) * effectLevel);
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		if (Global.getCurrentState() != GameState.COMBAT)
			return;
            if(Freischutz.isEmpty()){
                for(WeaponAPI w:ship.getAllWeapons()){
                    if(w.isDecorative()) continue;
                    if(w.getId().startsWith("HSI_LD_Freischutz")){
                        Freischutz.add(w);
                    }
                }
            }else{
                WeaponAPI coolingdown = null;
                for(WeaponAPI w:Freischutz){
                    if(w.getCooldownRemaining()>0){
                        coolingdown = w;
                        break;
                    }
                }
                if(coolingdown==null) coolingdown = Freischutz.get(0);
                List<WeaponAPI> toSync = new ArrayList<>(Freischutz);
                toSync.remove(coolingdown);
                for(WeaponAPI w:toSync){
                    w.setRemainingCooldownTo(coolingdown.getCooldownRemaining());
                    w.setAmmo(coolingdown.getAmmo());
                }
            }
		if (player) {
			maintainStatus(ship, state, effectLevel);
		}else{
            if(!Freischutz.isEmpty()){
                if(Freischutz.get(0).getAmmo()<=0&&ship.getHardFluxLevel()<=0.2f&&(!ship.getPhaseCloak().isOn()&&!ship.getPhaseCloak().isChargedown())){
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
                }else if(Freischutz.get(0).getAmmo()<=7&&ship.getHardFluxLevel()<0.4f&&ship.getPhaseCloak().isOn()){
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                }
            }
        }
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		if (ship.hasListenerOfClass(HSIEGOStats.class)) {
			level = ship.getListeners(HSIEGOStats.class).get(0).getLevel();
		}
        
		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null)
			cloak = ship.getSystem();
		if (cloak == null)
			return;
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			if (state == State.ACTIVE || state == State.OUT || state == State.IN) {
				float mult = getSpeedMult(ship, effectLevel);
				if (mult < 1f) {
					stats.getMaxSpeed().modifyMult(id + "_2", mult);
				} else {
					stats.getMaxSpeed().unmodifyMult(id + "_2");
				}
				((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(getDisruptionLevel(ship));
			}
		}

		if (state == State.COOLDOWN || state == State.IDLE) {
			if(!unapplyed)
			unapply(stats, id);
			return;
		}


		float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
		float accelPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f);
		stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);
		stats.getAcceleration().modifyPercent(id, accelPercentMod * effectLevel);
		stats.getDeceleration().modifyPercent(id, accelPercentMod * effectLevel);

		float speedMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult();
		float accelMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult();
		stats.getMaxSpeed().modifyMult(id, speedMultMod * effectLevel);
		stats.getAcceleration().modifyMult(id, accelMultMod * effectLevel);
		stats.getDeceleration().modifyMult(id, accelMultMod * effectLevel);

		float level = effectLevel;
		// float f = VULNERABLE_FRACTION;

		float levelForAlpha = level;

		// ShipSystemAPI cloak = ship.getPhaseCloak();
		// if (cloak == null) cloak = ship.getSystem();

		if (state == State.IN || state == State.ACTIVE) {
			ship.setPhased(true);
			unapplyed = false;
			levelForAlpha = level;
		} else if (state == State.OUT) {
			if (level > 0.5f) {
				ship.setPhased(true);
			} else {
				ship.setPhased(false);
			}
			levelForAlpha = level;
			unapplyed = false;
		}
        if(effectLevel>0){
            for(WeaponAPI w:Freischutz){
                w.getAmmoTracker().setAmmoPerSecond(effectLevel);
            }
        }

		ship.setExtraAlphaMult(Math.max(0,1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha) );
		ship.setApplyExtraAlphaToEngines(true);

		// float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * levelForAlpha;
		float extra = 0f;
		// if (isDisruptable(cloak)) {
		// extra = disruptionLevel;
		// }
		float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha * (1f - extra);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}

	}

    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship = null;
        // boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            // player = ship == Global.getCombatEngine().getPlayerShip();
            // id = id + "_" + ship.getId();
        } else {
            return;
        }

        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);

        stats.getMaxSpeed().unmodify(id);
        stats.getMaxSpeed().unmodifyMult(id + "_2");
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);

        ShipSystemAPI cloak = ship.getPhaseCloak();
        if (cloak == null)
            cloak = ship.getSystem();
        if (cloak != null) {
            ((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(0f);
        }
        unapplyed = true;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        // if (index == 0) {
        // return new StatusData("time flow altered", false);
        // }
        // float percent = (1f - INCOMING_DAMAGE_MULT) * effectLevel * 100;
        // if (index == 1) {
        // return new StatusData("damage mitigated by " + (int) percent + "%", false);
        // }
        return null;
    }
}
