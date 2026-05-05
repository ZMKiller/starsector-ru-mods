package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.hullmods.LogisticsMod.HSIBaseLogisticsMod;
import data.hullmods.ShieldMod.HSIBaseShieldModEffect;
import data.kit.HSII18nUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HSIAvangers extends HSIBaseShieldModEffect {
    protected static Map<ShipAPI.HullSize,Float> RANGE_THRESHOLD_EXTENSION = new HashMap<>();
    static {
        RANGE_THRESHOLD_EXTENSION.put(ShipAPI.HullSize.FIGHTER,0f);
        RANGE_THRESHOLD_EXTENSION.put(ShipAPI.HullSize.FRIGATE,80f);
        RANGE_THRESHOLD_EXTENSION.put(ShipAPI.HullSize.DESTROYER,150f);
        RANGE_THRESHOLD_EXTENSION.put(ShipAPI.HullSize.CRUISER,200f);
        RANGE_THRESHOLD_EXTENSION.put(ShipAPI.HullSize.CAPITAL_SHIP,300f);
    }

    private static Map<ShipAPI.HullSize,Float> speed = new HashMap();
    static {
        speed.put(ShipAPI.HullSize.FRIGATE, 50f);
        speed.put(ShipAPI.HullSize.DESTROYER, 30f);
        speed.put(ShipAPI.HullSize.CRUISER, 20f);
        speed.put(ShipAPI.HullSize.CAPITAL_SHIP, 10f);
    }


    //private static final float PEAK_MULT = 0.3333f;
    private static final float PEAK_MULT = 0.5f;
    //private static final float CR_DEG_MULT = 2f;
    //private static final float OVERLOAD_DUR = 50f;
    private static final float FLUX_DISSIPATION_MULT = 1.5f;
    //private static final float FLUX_CAPACITY_MULT = 1f;

    private static final float RANGE_THRESHOLD = 450f;
    private static final float RANGE_MULT = 0.25f;

    //private static final float RECOIL_MULT = 2f;
    //private static final float MALFUNCTION_PROB = 0.05f;

    //protected static final float ZERO_FLUX_BUFF = 20f;

    protected static final float SHIELD_CAP_DEBUFF = 0.9f;

    protected static final float SHIELD_REC_BUFF = 1.15f;

    protected static final Color Stalker = new Color(253, 44, 44, 225);
    private static final float WEAPON_MALFUNCTION_PROB = 0.03f;
    private static final float ENGINE_MALFUNCTION_PROB = 0.15f;

    public class HSIAvengerBuff implements BuffManagerAPI.Buff {
        public static final String ID = "HSI_Avanger_Random_Buff";
        private String causeH = "";
        private String causeM = "";
        private String causeS = "";

        private float H1 = 0;
        private float H2 = 0;

        private float M1 = 0;
        private float M2 = 0;

        private float S1 = 0;
        private float S2 = 0;

        public HSIAvengerBuff(){
            WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<>();
            picker.add(0,25f);
            picker.add(1,15f);
            picker.add(2,20f);
            picker.add(3,15f);
            picker.add(-1,25f);

            int H = picker.pick();
            int M = picker.pick();
            int S = picker.pick();
            Random r = new Random();
            if(H!=-1) {
                causeH = HSII18nUtil.getHullModString("HSIStalkerReasonH" + H);
                switch (H) {
                    case 0:
                        H1 = (-5f * r.nextFloat());
                        H2 = (8f * r.nextFloat());
                        break;
                    case 1:
                        H1 = (8f * r.nextFloat());
                        H2 = (8f * r.nextFloat());
                        break;
                    case 2:
                        H1 = (-5f * r.nextFloat());
                        H2 = (-5f * r.nextFloat());
                        break;
                    case 3:
                        H1 = (10f * (r.nextFloat() - 0.5f));
                        H2 = (10f * (r.nextFloat() - 0.5f));
                        break;
                }
            }
            if(M!=-1) {
                causeM = HSII18nUtil.getHullModString("HSIStalkerReasonM" + M);
                switch (M) {
                    case 0:
                        M1 = (4f * r.nextFloat());
                        M2 = (-4f * r.nextFloat());
                        break;
                    case 1:
                        M1 = (6f * r.nextFloat());
                        M2 = (6f * r.nextFloat());
                        break;
                    case 2:
                        M1 = (-4f * r.nextFloat());
                        M2 = (-4f * r.nextFloat());
                        break;
                    case 3:
                        M1 = (10f * (r.nextFloat() - 0.5f));
                        M2 = (10f * (r.nextFloat() - 0.5f));
                        break;
                }
            }
            if(S!=-1) {
                causeS = HSII18nUtil.getHullModString("HSIStalkerReasonS" + S);
                switch (S) {
                    case 0:
                        S1 = (4f * r.nextFloat());
                        S2 = (6f * r.nextFloat());
                        break;
                    case 1:
                        S1 = (-6f * r.nextFloat());
                        S2 = (6f * r.nextFloat());
                        break;
                    case 2:
                        S1 = (4f * r.nextFloat());
                        S2 = (-4f * r.nextFloat());
                        break;
                    case 3:
                        S1 = (10f * (r.nextFloat() - 0.5f));
                        S2 = (10f * (r.nextFloat() - 0.5f));
                        break;
                }
            }
        }


        public void apply(FleetMemberAPI member) {

        }

        public String getId() {
            return ID;
        }

        public void advance(float days) {

        }

        public boolean isExpired() {
            return false;
        }

        public float getH1() {
            return H1;
        }

        public float getH2() {
            return H2;
        }

        public float getM1() {
            return M1;
        }

        public float getM2() {
            return M2;
        }

        public float getS1() {
            return S1;
        }

        public float getS2() {
            return S2;
        }

        public String getCauseH() {
            return causeH;
        }

        public String getCauseM() {
            return causeM;
        }

        public String getCauseS() {
            return causeS;
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //stats.getMaxSpeed().modifyFlat(id, (Float) speed.get(hullSize));
        int DP_FLAT = 0;
        switch (hullSize){
            case DEFAULT:
                break;
            case FIGHTER:
                break;
            case FRIGATE:
                break;
            case DESTROYER:
                break;
            case CRUISER:
                DP_FLAT = 3;
                break;
            case CAPITAL_SHIP:
                DP_FLAT = 5;
                break;
        }
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id,DP_FLAT);
        stats.getAcceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
        stats.getDeceleration().modifyFlat(id, (Float) speed.get(hullSize) * 2f);
        stats.getTurnAcceleration().modifyFlat(id,(Float) speed.get(hullSize));
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on

        stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);

        stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
        stats.getVentRateMult().modifyMult(id, 0f);

        stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD+RANGE_THRESHOLD_EXTENSION.get(hullSize));
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT);
        if(stats.getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)){
            stats.getVariant().removeMod(HullMods.SAFETYOVERRIDES);
        }
        if(stats.getFleetMember()!=null&&stats.getFleetMember().getBuffManager()!=null) {
            HSIAvengerBuff buff;
            if (stats.getFleetMember().getBuffManager().getBuff(HSIAvengerBuff.ID) != null) {
                buff = (HSIAvengerBuff) stats.getFleetMember().getBuffManager().getBuff(HSIAvengerBuff.ID);
            } else {
                buff = new HSIAvengerBuff();
                stats.getFleetMember().getBuffManager().addBuff(buff);
            }
            String ID = HSIAvengerBuff.ID;
            float H1 = buff.getH1();
            float H2 = buff.getH2();
            float M1 = buff.getM1();
            float M2 = buff.getM2();
            float S1 = buff.getS1();
            float S2 = buff.getS2();
            if (H1 != 0) {
                if (H1 < 0) {
                    stats.getArmorBonus().modifyMult(ID, (1f + H1 / 100f));
                } else {
                   stats.getArmorBonus().modifyPercent(ID, H1);
                }
            }
            if (H2 != 0) {
                if (H2 < 0) {
                    stats.getHullBonus().modifyMult(ID, (1f + H2 / 100f));
                } else {
                    stats.getHullBonus().modifyPercent(ID, H2);
                }
            }
            if (M1 != 0) {
                if (M1 < 0) {
                    stats.getMaxSpeed().modifyMult(ID, (1f + M1 / 100f));
                } else {
                    stats.getMaxSpeed().modifyPercent(ID, M1);
                }
            }
            if (M2 != 0) {
                if (M2 < 0) {
                    stats.getAcceleration().modifyMult(ID, (1f + M2 / 100f));
                    stats.getDeceleration().modifyMult(ID, (1f + M2 / 100f));
                    stats.getMaxTurnRate().modifyMult(ID, (1f + M2 / 100f));
                    stats.getTurnAcceleration().modifyMult(ID, (1f + M2 / 100f));
                } else {
                    stats.getAcceleration().modifyPercent(ID, M2);
                    stats.getDeceleration().modifyPercent(ID, M2);
                    stats.getMaxTurnRate().modifyPercent(ID, M2);
                    stats.getTurnAcceleration().modifyPercent(ID, M2);
                }
            }
            if (S1 != 0) {
                if (S1 < 0) {
                    stats.getWeaponDamageTakenMult().modifyMult(ID, (1f + S1 / 100f));
                } else {
                    stats.getWeaponDamageTakenMult().modifyPercent(ID, S1);
                }
            }
            if (S2 != 0) {
                if (S2 < 0) {
                    stats.getEnergyWeaponDamageMult().modifyMult(ID, (1f + S2 / 100f));
                    stats.getBallisticWeaponDamageMult().modifyMult(ID, (1f + S2 / 100f));
                } else {
                    stats.getEnergyWeaponDamageMult().modifyPercent(ID, S2);
                    stats.getBallisticWeaponDamageMult().modifyPercent(ID, S2);
                }
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ship.getEngineController().fadeToOtherColor(this, Stalker, null, 1f, 0.4f);
        ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        if(member.getBuffManager()!=null&&member.getBuffManager().getBuff(HSIAvengerBuff.ID)==null){
            member.getBuffManager().addBuff(new HSIAvengerBuff());
        }
    }


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        // Color bad = Misc.getNegativeHighlightColor();
        Color bg = new Color(75, 75, 175, 200);
        Color c = new Color(175, 175, 225, 225);
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIStalkerSection"), Alignment.MID, opad);
        {
            //TooltipMakerAPI image = tooltip.beginImageWithText("graphics/illustrations/HSI_GuradFleet3_Intro.png",96f);
            LabelAPI desc = tooltip.addPara(HSII18nUtil.getHullModString("HSIStalkerIntro"), opad);
            desc.setColor(Stalker);
            //tooltip.addImageWithText(opad);
        }
        if(ship.getFleetMember()!=null&&ship.getFleetMember().getBuffManager()!=null){
            HSIAvengerBuff buff;
            if(ship.getFleetMember().getBuffManager().getBuff(HSIAvengerBuff.ID)!=null) {
                buff=(HSIAvengerBuff) ship.getFleetMember().getBuffManager().getBuff(HSIAvengerBuff.ID);
            }else{
                buff = new HSIAvengerBuff();
                ship.getFleetMember().getBuffManager().addBuff(buff);
            }
            tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIStalkerRandomSection"), Alignment.MID, opad);
            {
                //TooltipMakerAPI image = tooltip.beginImageWithText("graphics/illustrations/HSI_GuradFleet3_Intro.png",96f);
                LabelAPI desc = tooltip.addPara(HSII18nUtil.getHullModString("HSIStalkerRandomIntro"), opad);
                if(!buff.getCauseH().equals("")) tooltip.addPara(HSII18nUtil.getHullModString("HSIStalkerHullModification"), opad,h,buff.getCauseH(),
                        (buff.getH1()>0?"+":"")+String.format("%.1f",buff.getH1())+"%",
                        (buff.getH2()>0?"+":"")+String.format("%.1f",buff.getH2())+"%");
                if(!buff.getCauseM().equals("")) tooltip.addPara(HSII18nUtil.getHullModString("HSIStalkerManuModification"), opad,h,buff.getCauseM(),
                        (buff.getM1()>0?"+":"")+String.format("%.1f",buff.getM1())+"%",
                        (buff.getM2()>0?"+":"")+String.format("%.1f",buff.getM2())+"%");
                if(!buff.getCauseS().equals("")) tooltip.addPara(HSII18nUtil.getHullModString("HSIStalkerStatsModification"), opad,h,buff.getCauseS(),
                        (buff.getS1()>0?"+":"")+String.format("%.1f",buff.getS1())+"%",
                        (buff.getS2()>0?"+":"")+String.format("%.1f",buff.getS2())+"%");
                //tooltip.addImageWithText(opad);
            }
        }

    }

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldCap().modifyMult("HSI_Avenger",SHIELD_CAP_DEBUFF);
        shield.getStats().getShieldRecoveryRate().modifyMult("HSI_Avenger",SHIELD_REC_BUFF);
    }

    @Override
    public float getTooltipWidth() {
        return 425f;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index==5) return Global.getSettings().getHullModSpec(HullMods.SAFETYOVERRIDES).getDisplayName();
        if(index==4) return (int)(PEAK_MULT*100f)+"%";
        if(index==0) return (int)(((float)RANGE_THRESHOLD_EXTENSION.get(hullSize)+RANGE_THRESHOLD))+"";
        if(index==3) return (int)(100f-SHIELD_CAP_DEBUFF*100f)+"%";
        if(index==2) return String.format("%.1f",FLUX_DISSIPATION_MULT)+"";
        if(index==1) return (int)(SHIELD_REC_BUFF*100f-100f)+"%";
        //if(index==5) return ""+EXTRA_S_MOD;
        return null;
    }

    @Override
    public Color getNameColor() {
        return Stalker;
    }
}
