package data.shipsystems.scripts;

import java.awt.Color;

import data.kit.HSII18nUtil;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;

import data.hullmods.HSIHaloV2;
import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSIUnstableTimeFlow extends BaseShipSystemScript {
    private ShipAPI ship;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private HSITurbulanceShieldListenerV2 shield;
    public static final float TIME_MULT = 100f;
    private FaderUtil fader = new FaderUtil(0f, 7f, 7f, true, true);
    protected HSIUnstableTimeFlowPassive passive;

    public static final Color JITTER_COLOR = new Color(90, 165, 255, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(90, 165, 255, 155);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        fader.advance(engine.getElapsedInLastFrame());
        if (fader.isFadedIn())
            fader.fadeOut();
        if (fader.isFadedOut())
            fader.fadeIn();
        stats.getTimeMult().modifyPercent(id + "1", effectLevel * TIME_MULT);
        //stats.getHullDamageTakenMult().modifyMult(id, 1f - 0.5f*effectLevel);
        stats.getTimeMult().modifyPercent(id + "2",
                (float) (Math.sin(fader.getBrightness() * 2f * Math.PI)) * 7.5f + 7.5f);
        if (effectLevel > 0) {
            float jitterLevel = effectLevel;
            float jitterRangeBonus = 0;
            float maxRangeBonus = 10f;
            if (state == State.IN) {
                jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
                if (jitterLevel > 1) {
                    jitterLevel = 1f;
                }
                jitterRangeBonus = jitterLevel * maxRangeBonus;
            } else if (state == State.ACTIVE) {
                jitterLevel = 1f;
                jitterRangeBonus = maxRangeBonus;
            } else if (state == State.OUT) {
                jitterRangeBonus = jitterLevel * maxRangeBonus;
            }
            jitterLevel = (float) Math.sqrt(jitterLevel);
            effectLevel *= effectLevel;

            ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
            ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
        } else {
            unapply(stats, id);
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        stats.getTimeMult().unmodify(id + "1");
        stats.getHullDamageTakenMult().unmodify(id);
        if (!ship.hasListenerOfClass(HSIUnstableTimeFlowPassive.class)) {
            passive = new HSIUnstableTimeFlowPassive(ship, stats);
            ship.addListener(passive);
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            float passiveLevel = 0;
            if(passive != null&&passive.isActive()) passiveLevel = 33f;
            return new StatusData(HSII18nUtil.getShipSystemString("HSIUTimeFlow")
                    + (int) ((float) (Math.sin(fader.getBrightness() * 2f * Math.PI)) * 15f + 5f
                            + effectLevel * TIME_MULT+passiveLevel)
                    + " %", false);
        }
        return null;
    }

    public class HSIUnstableTimeFlowPassive implements AdvanceableListener, DamageTakenModifier {
        private ShipAPI ship;
        private MutableShipStatsAPI stats;
        private IntervalUtil cd = new IntervalUtil(17f, 17f);
        private boolean usable = true;
        private IntervalUtil time = new IntervalUtil(1.4f, 1.4f);
        private boolean active = false;
        private float lim = 0;

        public HSIUnstableTimeFlowPassive(ShipAPI ship, MutableShipStatsAPI stats) {
            this.ship = ship;
            this.stats = stats;
            lim = HSITurbulanceShieldListenerV2.getInstance(ship).getShield().getShieldRegenBlock();
        }

        public void advance(float amount) {
            if (ship == null || !ship.isAlive()) {
                return;
            }
            if (!usable) {
                if (active) {
                    time.advance(amount);
                    ship.setJitter(this, JITTER_COLOR, 1, 3, 0, 3);
                    ship.setJitterUnder(this, JITTER_UNDER_COLOR, 1, 25, 0f, 6);
                    if (time.intervalElapsed()) {
                        active = false;
                        stats.getHullDamageTakenMult().unmodify("HSIUnstableTimeFlowPassive");
                        stats.getTimeMult().unmodify("HSIUnstableTimeFlowPassive");
                    }
                }
                cd.advance(amount);
                if (cd.intervalElapsed())
                    usable = true;
            }
        }

        public String modifyDamageTaken(Object param, CombatEntityAPI target,
                DamageAPI damage, Vector2f point, boolean shieldHit) {
            if(!usable) return null;
            boolean shouldActivate = false;
            if(damage.isDps()){
                float cal = damage.getDamage()/2;
                if(cal>lim) shouldActivate = true; 
            }else{
                if(damage.getDamage()>lim) shouldActivate = true;
            }
            if ((ship.getSystem() != null && !ship.getSystem().isActive())&&shouldActivate) {
                //damage.getModifier().modifyMult("HSIUnstableTimeFlowPassive", 0f);
                usable = false;
                //stats.getHullDamageTakenMult().modifyMult("HSIUnstableTimeFlowPassive", 0.75f);
                stats.getTimeMult().modifyPercent("HSIUnstableTimeFlowPassive", 50f);
                time.setElapsed(0f);
                active = true;
            }
            return "HSIUnstableTimeFlowPassive";
        }

        public boolean isActive(){
            return active;
        }
    }
}
