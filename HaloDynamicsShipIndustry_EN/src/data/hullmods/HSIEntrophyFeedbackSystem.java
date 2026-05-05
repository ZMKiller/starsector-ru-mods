package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;
import data.shipsystems.activators.HSITSControlActivator;
import org.magiclib.subsystems.*;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class HSIEntrophyFeedbackSystem extends BaseHullMod {
    protected static final int FLUX_PER_LEVEL = 500;

    protected static final float DAMAGE_BUFF = 1f;

    protected static final float ROF_BUFF = 1f;
    protected static final float FLUX_GEN_BUFF = 0.5f;

    protected static final float VENT_REGEN_MULT = 0.35f;

    protected static final float VENT_MULT = 1.5f;


    public static class HSIEntropyFeedbackListener implements AdvanceableListener{
        private ShipAPI ship;
        private int flux_gen_level = 0;
        private float lastSecFlux = 0;

        private int skip = 15;

        private float mult = 1f;

        protected static final String KEY = "HSI_EntropyFeedbackSystem_Script";

        public HSIEntropyFeedbackListener(ShipAPI ship){
            this.ship = ship;
            if(ship.getVariant().hasHullMod("HSI_DamagedEnergySystem")){
                mult = 0.5f;
            }
        }

        private IntervalUtil fluxLevelCheck = new IntervalUtil(0.2f,0.2f);
        public void advance(float amount){
            int flux_store_level = (int)(30*mult*(Math.min(1f,ship.getFluxLevel()/0.6f)));
            fluxLevelCheck.advance(amount);
            if(fluxLevelCheck.intervalElapsed()){
                if(ship.getCurrFlux()>lastSecFlux){
                    flux_gen_level = Math.min((int) (20*mult),(int)(flux_gen_level+(ship.getCurrFlux()-lastSecFlux)/(FLUX_PER_LEVEL)));
                    skip = 15;
                }else if(ship.getCurrFlux()<lastSecFlux){
                    if(skip>0){
                        skip--;
                    }else{
                        flux_gen_level= Math.max(0,flux_gen_level-1);
                    }
                    if(ship.getFluxTracker().isOverloadedOrVenting()){
                        flux_gen_level = 0;
                        float diff = lastSecFlux - ship.getCurrFlux();
                        if(ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)){
                            HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
                            shield.getShield().regenShield(diff*VENT_REGEN_MULT);
                        }
                    }
                }
                lastSecFlux = ship.getCurrFlux();
            }
            int total_level = flux_store_level+flux_gen_level;
            if(ship == Global.getCombatEngine().getPlayerShip()){
                Global.getCombatEngine().maintainStatusForPlayerShip("HSI_EntrophyFeedback",
                        "graphics/icons/hullsys/temporal_shell.png",
                        Global.getSettings().getHullModSpec("HSI_EntrophyFeedbackSystem").getDisplayName(), HSII18nUtil.getHullModString("HSIEntrophyFeedBackBuff")+" : "+total_level, false);
            }
            if(!ship.isAlive()) return;
            ship.getMutableStats().getBallisticRoFMult().modifyMult(KEY,1f+total_level*ROF_BUFF/100f);
            ship.getMutableStats().getEnergyRoFMult().modifyPercent(KEY,1f+total_level*ROF_BUFF/100f);
            ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult(KEY,1f+total_level*ROF_BUFF/100f);
            ship.getMutableStats().getEnergyWeaponDamageMult().modifyMult(KEY,1f+total_level*ROF_BUFF/100f);
            ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyMult(KEY,(100f-total_level*FLUX_GEN_BUFF)/100f);
            ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyMult(KEY,(100f-total_level*FLUX_GEN_BUFF)/100f);
            if(ship==Global.getCombatEngine().getPlayerShip()&&ship.getAI()==null) return;
            boolean ifHasShieldShouldVent = false;
            if(ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)){
                HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
                if(shield.getShield().getShieldLevel()<0.2f) ifHasShieldShouldVent = true;
            }
            if(!(ship.getPhaseCloak()!=null&&ship.getPhaseCloak().isOn())&&(ship.getFluxLevel()>0.8f||(ifHasShieldShouldVent&&ship.getFluxLevel()>0.66f))){
                ship.giveCommand(ShipCommand.VENT_FLUX,null,0);
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getVentRateMult().modifyMult(id,VENT_MULT);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id,0f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new HSIEntropyFeedbackListener(ship));
        if(Global.getCombatEngine()!=null) Global.getCombatEngine().addPlugin(new HSITemperolProtection(ship));
        MagicSubsystemsManager.addSubsystemToShip(ship,new HSITSControlActivator(ship));
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused())
            return;

        MutableStat time = ship.getMutableStats().getTimeMult();
        if (time.getModifiedValue() > time.getBaseValue() && ship.areSignificantEnemiesInRange()) {
            if (ship.getPeakTimeRemaining() > 0) {
                float curr = 0;
                if (ship.getMutableStats().getPeakCRDuration().getFlatBonus(HSITimeAnchor.KEY) != null) {
                    curr = ship.getMutableStats().getPeakCRDuration().getFlatBonus(HSITimeAnchor.KEY).getValue();
                }
            /*Global.getLogger(this.getClass()).info(
                    "TIMEMULT:M:"+time.getModifiedValue()
                    +"||B:"+time.getBaseValue()
                    +"||AMT:"+amount
                    +"||EAMT:"+Global.getCombatEngine().getElapsedInLastFrame()
                    +"||ETIMEMULT:"+Global.getCombatEngine().getTimeMult().getModifiedValue()
                    +"||TOTALELAPSED:"+Global.getCombatEngine().getTotalElapsedTime(false)
                    +"||PEAK:"+ship.getPeakTimeRemaining()
                    +"||TESTBUFF:"+(time.getModifiedValue()-time.getBaseValue())/time.getModifiedValue()*amount
            );*/
                //Global.getLogger(this.getClass()).info("PEAKTIM:M:"+time.getModifiedValue()+" B:"+time.getBaseValue()+" AMT:"+amount+" EAMT:"+Global.getCombatEngine().getElapsedInLastFrame());

                ship.getMutableStats().getPeakCRDuration().modifyFlat(HSITimeAnchor.KEY, curr + (time.getModifiedValue() - time.getBaseValue()) / time.getModifiedValue() * amount);
            } else {
                ship.getMutableStats().getCRLossPerSecondPercent().modifyMult(HSITimeAnchor.KEY, 1f - (time.getModifiedValue() - time.getBaseValue()) / time.getModifiedValue());
            }
        } else {
            ship.getMutableStats().getCRLossPerSecondPercent().unmodify(HSITimeAnchor.KEY);
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width,
                                          boolean isForModSpec) {
        // float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        // Color bad = Misc.getNegativeHighlightColor();
        Color bg = new Color(75, 75, 175, 200);
        Color b = new Color(225, 225, 255, 225);
        Color c = new Color(175, 175, 225, 225);

        tooltip.addPara(HSII18nUtil.getHullModString("HSIEntroplyFeedBackSystemIntro0"), opad,c,h,HSII18nUtil.getHullModString("HSIEntrophyFeedBackBuff"),"60%","30");
        tooltip.addPara(HSII18nUtil.getHullModString("HSIEntroplyFeedBackSystemIntro1"), opad,c,h,""+FLUX_PER_LEVEL,HSII18nUtil.getHullModString("HSIEntrophyFeedBackBuff"),"20","3","5");

        tooltip.addPara(HSII18nUtil.getHullModString("HSIEntrophyFeedBackBuffIntro"),opad,c,h,HSII18nUtil.getHullModString("HSIEntrophyFeedBackBuff"),(int)DAMAGE_BUFF+"%",(int)ROF_BUFF+"%",String.format("%.1f",FLUX_GEN_BUFF)   +"%");

        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIEntroplyFeedBackSystemRegen"), Alignment.MID, opad);
        {
            tooltip.addPara(HSII18nUtil.getHullModString("HSIEntroplyFeedBackSystemRegenIntro0"), opad,c,h,VENT_MULT+"",(int)(VENT_REGEN_MULT*100f)+"%");
        }
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITSControlName"), Alignment.MID, opad);
        {
            tooltip.addPara(HSII18nUtil.getHullModString("HSITSControlDesc"), opad,c,h);
        }
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    public static class HSITemperolProtection extends BaseEveryFrameCombatPlugin{
        private ShipAPI ship;

        public HSITemperolProtection(ShipAPI ship){
            this.ship = ship;
        }

        private List<String> nameMult = new ArrayList<>();
        private List<String> namePercent = new ArrayList<>();
        private List<String> nameFlat = new ArrayList<>();

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if(!ship.isAlive()){
                Global.getCombatEngine().removePlugin(this);
                return;
            }
            MutableStat time = ship.getMutableStats().getTimeMult();
            for (String modify : nameMult) {
                time.unmodify(modify+"Anti");
            }
            nameMult.clear();
            List<String> zeroException = new ArrayList<>();
            for (String modify : time.getMultMods().keySet()) {
                    if (time.getMultMods().get(modify).getValue() < 1) {
                        if(time.getMultMods().get(modify).getValue()<=0){
                            zeroException.add(modify);
                        }else {
                            nameMult.add(modify);
                        }
                    }
            }
            for(String i:zeroException){
                time.unmodify(i);
            }
            for(String modify:nameMult){
                time.modifyMult(modify+"Anti",1f/time.getMultMods().get(modify).getValue());
            }
            for (String modify : namePercent) {
                time.unmodify(modify+"Anti");
            }
            namePercent.clear();
            for (String modify : time.getPercentMods().keySet()) {
                if (time.getPercentMods().get(modify).getValue() < 0) {
                    namePercent.add(modify);
                    //time.modifyPercent(modify+"Anti",-time.getPercentMods().get(modify).getValue());
                }
            }
            for (String modify : namePercent) {
                time.modifyPercent(modify+"Anti",-time.getPercentMods().get(modify).getValue());
            }
            for (String modify : nameFlat) {
                time.unmodify(modify+"Anti");
            }
            nameFlat.clear();
            for (String modify : time.getFlatMods().keySet()) {
                if (time.getFlatMods().get(modify).getValue() < 0) {
                    nameFlat.add(modify);
                    //time.modifyFlat(modify+"Anti",-time.getMultMods().get(modify).getValue());
                }
            }
            for (String modify : nameFlat) {
                time.modifyFlat(modify+"Anti",-time.getMultMods().get(modify).getValue());
            }

        }
    }
}
