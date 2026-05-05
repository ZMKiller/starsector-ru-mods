package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.ShieldMod.HSIShieldModEffect;
import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

public class HSIHaloV2 extends BaseHullMod {
    public static float LOW_REG_STOP_LIMIT = 0.01f;
    protected static float DEFAULT_DAMAGE_TAKEN = 1f;
    protected static float FRAGMENTATION_DAMAGE_TAKEN = 0.25f;
    protected static float VENTING_REG_LOSS = 0.6f;
    protected static float REGEN_FLUX_RATE = 1f;
    protected static final Color ABSORB = new Color(200, 155, 255, 255);

    protected static float SHIELD_BLOCK_MAX  = 75f;

    protected static float SHIELD_ARMOR_MAX = 250f;

    protected static float ARMOR_FRAC = 0.005f;


    protected static float FLUX_DEBUFF = 0.5f;

    protected static Map<HullSize, Float> SHIELDCAP = new HashMap<>();
    static {
        SHIELDCAP.put(HullSize.CAPITAL_SHIP, 0.8f);
        SHIELDCAP.put(HullSize.CRUISER, 0.75f);
        SHIELDCAP.put(HullSize.DESTROYER, 0.7f);
        SHIELDCAP.put(HullSize.FRIGATE, 0.7f);
        SHIELDCAP.put(HullSize.FIGHTER, 1.0f);
    }

    protected static Map<HullSize, Float> REGCD = new HashMap<>();
    static {
        REGCD.put(HullSize.CAPITAL_SHIP, 14f);
        REGCD.put(HullSize.CRUISER, 12f);
        REGCD.put(HullSize.DESTROYER, 11f);
        REGCD.put(HullSize.FRIGATE, 11f);
        REGCD.put(HullSize.FIGHTER, 10f);
    }

    protected static Map<HullSize, Float> REGMAXSPEED = new HashMap<>();
    static {
        REGMAXSPEED.put(HullSize.CAPITAL_SHIP, 0.7f);
        REGMAXSPEED.put(HullSize.CRUISER, 0.7f);
        REGMAXSPEED.put(HullSize.DESTROYER, 0.7f);
        REGMAXSPEED.put(HullSize.FRIGATE, 0.7f);
        REGMAXSPEED.put(HullSize.FIGHTER, 1.0f);
    }

    protected static float BUFFER_CAP = 0.15f;
    public static final Map<String, String> WeaponIndAdaptionMap = new HashMap<>();
    public static final Map<String, String> BaseMap = new HashMap<>();

    private static void putPair(String base, String ind) {
        WeaponIndAdaptionMap.put(base, ind);
        BaseMap.put(ind, base);
    }

    static {
        putPair("mjolnir", "HWI_mjolnir");
        putPair("lightneedler", "HWI_lightneedler");
        putPair("heavyneedler", "HWI_heavyneedler");
        putPair("irautolance", "HWI_irautolance");
        putPair("guardian", "HWI_guardian");
        putPair("autopulse", "HWI_autopulse");
        putPair("plasma", "HWI_plasma");
        putPair("hil", "HWI_hil");
        putPair("tachyonlance", "HWI_tachyonlance");
        putPair("phasebeam", "HWI_phasebeam");
        putPair("heavyblaster", "HWI_heavyblaster");
        putPair("gravitonbeam", "HWI_gravitonbeam");
        putPair("ionbeam", "HWI_ionbeam");
        putPair("mininglaser", "HWI_mininglaser");
        putPair("heavyac", "HWI_heavyac");
        putPair("ionpulser", "HWI_ionpulser");
        putPair("gauss","HWI_gauss");
        putPair("amblaster","HWI_amblaster");
        putPair("hveldriver","HWI_hveldriver");
        putPair("lightdualac","HWI_lightdualac");
        putPair("heavymauler","HWI_heavymauler");
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive())
            return;
        ship.getEngineController().extendFlame(this, 0.5f, 0.2f, 0.2f);
        /*if(Math.random()>0.33f&&!ship.getFluxTracker().isOverloadedOrVenting()) {
            if(ship.getSystem()!=null&&ship.getSystem().isOn()) return;
            if(ship.getPhaseCloak()!=null&&ship.getPhaseCloak().isOn()) return;
            int total = 0;
            int cd = 0;
            int fire = 0;
            float totalCD = 0;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if(weapon.isDecorative()) continue;
                if(weapon.isInBurst()){
                    switch (weapon.getSize()){
                        case LARGE:
                            fire+=4;
                            continue;
                        case MEDIUM:
                            fire+=2;
                            continue;
                        case SMALL:
                            fire+=1;
                            continue;
                    }
                }
                if(weapon.getSpec().getAIHints().contains(WeaponAPI.AIHints.PD)||weapon.getSpec().getAIHints().contains(WeaponAPI.AIHints.PD_ONLY)){
                    continue;
                }
                if(weapon.getCooldownRemaining()>0){
                    switch (weapon.getSize()){
                        case LARGE:
                            cd+=4;
                            totalCD+=(weapon.getCooldownRemaining()*4);
                            continue;
                        case MEDIUM:
                            cd+=2;
                            totalCD+=(weapon.getCooldownRemaining()*2);
                            continue;
                        case SMALL:
                            cd+=1;
                            totalCD+=(weapon.getCooldownRemaining());
                            continue;
                    }
                }
            }
            if(ship.getFluxLevel()>0.5f&&fire<=4&&((float)cd)/total>=0.3f&&ship.getFluxTracker().getTimeToVent()<=totalCD/cd*1.1f){
                ship.giveCommand(ShipCommand.VENT_FLUX,null,0);
            }
        }*/
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        HSITurbulanceShieldListenerV2.getInstance(ship);
        // ship.addListener(shield);
        if(!AjimusUtils.isInRefit(ship)) return;
        /*if (Global.getSector() != null &&
                Global.getSector().getPlayerFleet() != null &&
                Global.getSector().getPlayerFleet().getCargo() != null &&
                Global.getSector().getPlayerFleet().getCargo().getStacksCopy() != null &&
                !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()) {
            Set<String> indKey = BaseMap.keySet();
            int total = 0;
            for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
                if (s.isWeaponStack() && (indKey.contains(s.getWeaponSpecIfWeapon().getWeaponId()))) {
                    total+= Math.round(s.getSize());
                }
            }
            for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
                if (s.isWeaponStack() && (indKey.contains(s.getWeaponSpecIfWeapon().getWeaponId()))) {
                    Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                    if(total<=1&&ship.getVariant().hasHullMod("HSI_WeaponIndAdaption")) {
                        String base = BaseMap.get(s.getWeaponSpecIfWeapon().getWeaponId());
                        if (base != null) {
                            Global.getSector().getPlayerFleet().getCargo().addWeapons(base, Math.round(s.getSize()));
                            //ship.getVariant().removeMod("HSI_WeaponIndAdaption");
                            //ship.getVariant().setSource(VariantSource.REFIT);
                            //Global.getLogger(this.getClass()).info("added 1"+base);
                        }
                    }
                }
            }
        }*/
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize,
            MutableShipStatsAPI stats, String id) {
        //stats.getFluxDissipation().modifyMult(id,FLUX_DEBUFF);
        //stats.getFluxCapacity().modifyMult(id,FLUX_DEBUFF);
        ShipVariantAPI v = stats.getVariant();
        if (HSIDroneGroundSupport.getDGSwings(stats) > 0) {
            v.addMod("HSI_DGS");
        }
        if(stats.getVariant().hasHullMod(HullMods.ADVANCED_TARGETING_CORE)){
            stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id,40f);
            stats.getBeamPDWeaponRangeBonus().modifyPercent(id, 40f);
        }
        //if(stats.getFleetMember()==null) return;
        //if(!isInPlayerFleet(stats)) return;
        //if (Global.getSector() != null &&
                //Global.getSector().getPlayerFleet() != null &&
                //Global.getSector().getPlayerFleet().getCargo() != null &&
                //Global.getSector().getPlayerFleet().getCargo().getStacksCopy() != null) {
            //Set<String> indKey = BaseMap.keySet();
            //for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {

            //}
        //}

        //if(stats.getVariant().hasHullMod("HSI_WeaponIndAdaption")) {
            Set<String> MapKey = WeaponIndAdaptionMap.keySet();
            for (String slotId : v.getNonBuiltInWeaponSlots()) {
                String wpnId = v.getWeaponId(slotId);
                if (wpnId != null) {
                    if (MapKey.contains(wpnId)) {
                        v.clearSlot(slotId);
                        v.addWeapon(slotId, WeaponIndAdaptionMap.get(wpnId));
                    }
                }
            }
        //}
        /*else{
            Set<String> MapKey = BaseMap.keySet();
            for (String slotId : v.getNonBuiltInWeaponSlots()) {
                String wpnId = v.getWeaponId(slotId);
                if (wpnId != null) {
                    if (MapKey.contains(wpnId)) {
                        v.clearSlot(slotId);
                        v.addWeapon(slotId, BaseMap.get(wpnId));
                    }
                }
            }
        }*/
    }



    public Color getNameColor() {
        return new Color(217, 102, 255, 255);
    }

    public int getDisplaySortOrder() {
        return 1;
    }

    public Color getBorderColor() {
        return getNameColor();
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if(index == 0) return (int)(FLUX_DEBUFF*100f)+"%";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        // float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        // Color bad = Misc.getNegativeHighlightColor();
        Color bg = new Color(75, 75, 175, 200);
        Color b = new Color(225, 225, 255, 225);
        Color c = new Color(175, 175, 225, 225);
        HSITurbulanceShieldListenerV2 LFD = HSITurbulanceShieldListenerV2.getInstance(ship);

        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITurbulanceShieldV2Name"), Alignment.MID, opad);
        {
            LabelAPI desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2Desc1"), opad,h,(int)((float)SHIELDCAP.get(hullSize)*100f)+"%",(int)((float)REGMAXSPEED.get(hullSize)*100f)+"%",String.format("%.1f",ARMOR_FRAC*100f)+"%");
            desc.setColor(b);
            //desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2Desc2"), opad,h,(int)((float)BUFFER_CAP*100f)+"%",(int)((float)BUFFER_CAP*100f)+"%");
            //desc.setColor(b);
            desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2Desc2"), opad,h,"25%");
            desc.setColor(b);
            desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2Desc3"), opad);
            desc.setColor(b);
            desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2Desc4"), opad);
            desc.setColor(b);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITurbulanceDetailedDescTitle"), Alignment.MID,
                    opad);
            {
                LabelAPI desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceDetailedDesc"), opad);
                desc.setColor(b);
            }
        }else{
            tooltip.addPara(HSII18nUtil.getHullModString("HSIClickToChangeDetail"), opad,h,"LAlt");
        }

        float col1W = 120;
        float colW = (int) ((width - col1W - 12f));
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITurbulanceShieldV2TableTitle"), Alignment.MID,
                opad + 7f);
        tooltip.beginTable(c, bg, b, 20f, true, true,
                new Object[] { HSII18nUtil.getHullModString("HSITurbulanceShieldV2TableRowHeadL"), col1W,
                        HSII18nUtil.getHullModString("HSITurbulanceShieldV2TableRowHeadR"), colW });
        tooltip.addRow(Alignment.MID, c, HSII18nUtil.getHullModString("HSITurbulanceShieldV2Row1"), Alignment.MID, c,
                String.format("%.1f", LFD.getShield().getShieldCap()));
        tooltip.addRow(Alignment.MID, c, HSII18nUtil.getHullModString("HSITurbulanceShieldV2Row2"), Alignment.MID, c,
                String.format("%.2f", LFD.getStats().getShieldEffciency().getModifiedValue())+" x "+String.format("%.2f", LFD.getShield().getShipStatsDamageTakenSync())+"");
        tooltip.addRow(Alignment.MID, c, HSII18nUtil.getHullModString("HSITurbulanceShieldV2Row3"), Alignment.MID, c,
                String.format("%.1f", LFD.getShield().getBaseShieldRegen()));
        tooltip.addRow(Alignment.MID, c, HSII18nUtil.getHullModString("HSITurbulanceShieldV2Row4"), Alignment.MID, c,
                String.format("%.1f", LFD.getShield().getShieldRegenTime()));
        tooltip.addRow(Alignment.MID, c, HSII18nUtil.getHullModString("HSITurbulanceShieldV2Row5"), Alignment.MID, c,
                String.format("%.1f", LFD.getStats().getShieldRecoveryRate().getModifiedValue() * 100f) + "%");
        tooltip.addRow(Alignment.MID, c, HSII18nUtil.getHullModString("HSITurbulanceShieldV2Row6"), Alignment.MID, c,
                LFD.getShield().getShieldRegenBlock() <= 10000
                        ? String.format("%.1f", /*LFD.getShield().getShieldRegenBlock()*/LFD.getStats().getShieldArmorValue().computeEffective(0f))
                        : "INF");
        tooltip.addRow(Alignment.MID, c, HSII18nUtil.getHullModString("HSITurbulanceShieldV2Row7"), Alignment.MID, c,
                String.format("%.1f", LFD.getStats().getShieldRecoveryCost().getModifiedValue() * 100f) + "%");
        tooltip.addTable("", 0, opad / 2f);
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            tooltip.addSpacer(opad);
            {
                LabelAPI desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2RowDesc1"), opad);
                desc.setColor(b);
                desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2RowDesc2"), opad);
                desc.setColor(b);
                desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2RowDesc3"), opad);
                desc.setColor(b);
                desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2RowDesc4"), opad);
                desc.setColor(b);
                desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2RowDesc5"), opad);
                desc.setColor(b);
                desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2RowDesc6"), opad);
                desc.setColor(b);
                desc = tooltip.addPara(HSII18nUtil.getHullModString("HSITurbulanceShieldV2RowDesc7"), opad);
                desc.setColor(b);
            }
        }
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIAdaption"), Alignment.MID,
                opad + 7f);
        LabelAPI desc2 = tooltip.addPara(HSII18nUtil.getHullModString("HSIAdaptionText"), opad, c,HSII18nUtil.getHullModString("HSIAdaption"));
        desc2.setColor(b);
        for (HSIShieldModEffect se : LFD.getshieldModEffects(ship)) {
            HullModSpecAPI spec = se.getSpec();
            tooltip.addSectionHeading(spec.getDisplayName(),
                    Alignment.TMID, 4f);
            TooltipMakerAPI text;
            text = tooltip.beginImageWithText(spec.getSpriteName(), 32);
            LabelAPI desc = text.addPara(spec.getDescription(hullSize), 0);
            List<String> hls = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                String hl = se.getDescriptionParam(i, hullSize);
                if (hl == null || hl == "null") {
                    break;
                }
                hls.add(hl);
            }
            String[] hlArray = hls.toArray(new String[hls.size()]);
            desc.setHighlight(hlArray);
            desc.setColor(b);
            tooltip.addImageWithText(opad);
        }
    }

    public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

}