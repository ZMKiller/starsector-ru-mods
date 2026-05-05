package data.hullmods;

import java.awt.Color;
import java.util.Iterator;

import com.fs.starfarer.api.combat.*;
import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSIFlexibleHangarN extends BaseHullMod {
    protected static final float MAX_EX_RATE = 0.15f;
    protected static final float MAX_EX_REPLACEMENT = 3;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused())
            return;
        if (!ship.isAlive()) {
            HSIFlexibleHangarNData.clearInstance(ship);
            return;
        }
        HSIFlexibleHangarNData data = HSIFlexibleHangarNData.getInstance(ship);
        int num = ship.getNumFighterBays();
        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (ship.getFullTimeDeployed() < 10f) {
                data.setReplaceRate(Math.min(data.getReplaceRate() + calculateEXRate(bay) * 0.1f * amount,
                        calculateTotalEXRate(ship)));
            } else {
                if (bay.getCurrRate() >= 1f) {
                    // Global.getLogger(this.getClass())
                    // .info("Bay " + bay.getWeaponSlot().getId() + " curr rate is " +
                    // bay.getCurrRate());
                    data.setReplaceRate(Math.min(data.getReplaceRate() + calculateEXRate(bay) * 0.05f * amount,
                            calculateTotalEXRate(ship)));
                    if (bay.getWing() != null
                            && bay.getWing().getWingMembers().size() < bay.getWing().getSpec().getNumFighters()) {

                    } else if (bay.getWing() != null) {
                        data.setFastreplacement(bay, Math.min(calculateEXReplacement(bay), data.getFastReplaceMent(bay)
                                + (calculateEXReplacementRate(bay.getWing().getSpec()) * amount)));
                    }
                } else {
                    if (data.getFastReplaceMent(bay) >= 1) {
                        bay.setFastReplacements(1);
                        bay.makeCurrentIntervalFast();
                        data.setFastreplacement(bay, data.getFastReplaceMent(bay) - 1f);
                    }
                    float sub = 1f - bay.getCurrRate();
                    float ex = data.getReplaceRate();
                    if (ex > sub) {
                        data.setReplaceRate(ex - sub);
                        bay.setCurrRate(1f);
                    } else {
                        data.setReplaceRate(0f);
                        bay.setCurrRate(bay.getCurrRate() + ex);
                    }
                }
            }
            // Global.getLogger(this.getClass()).info(bay.getWeaponSlot()+"-"+bay.getNumLost());
        }
        if (ship == Global.getCombatEngine().getPlayerShip()) {
            Global.getCombatEngine().maintainStatusForPlayerShip(spec.getId(),
                    Global.getSettings().getSpriteName("ui", "icon_tactical_bdeck"),
                    HSII18nUtil.getHullModString("HSIFlexibleHangar3"),
                    String.format("%.2f", data.getReplaceRate()),
                    data.getReplaceRate() <= 0f);
            String fr = "";
            Iterator<FighterLaunchBayAPI> KEY = data.fastreplacement.keySet().iterator();
            while (KEY.hasNext()) {
                fr += ("" + (int) data.getFastReplaceMent(KEY.next()));
                if (KEY.hasNext())
                    fr += "/";
            }
            Global.getCombatEngine().maintainStatusForPlayerShip(spec.getId() + "1",
                    Global.getSettings().getSpriteName("ui", "icon_tactical_bdeck"),
                    HSII18nUtil.getHullModString("HSIFlexibleHangar4"),
                    fr,
                    data.getReplaceRate() <= 0f);
        }

        /*for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.isDecorative()) continue;
            Global.getLogger(this.getClass()).info(Global.getCombatEngine().getTotalElapsedTime(false)+","+amount+","+
                    weapon.getChargeLevel()+","+weapon.getCooldownRemaining());
        }*/
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        // Color bad = Misc.getNegativeHighlightColor();
        Color bg = new Color(75, 75, 175, 200);
        Color b = new Color(225, 225, 255, 225);
        Color c = new Color(175, 175, 225, 225);
        TooltipMakerAPI text;
        float col1W = (width - 12f) / 6f;
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIFlexibleHangar0"),
                Alignment.TMID, 4f);
        tooltip.addPara(HSII18nUtil.getHullModString("HSIFlexibleHangar1"), 10, Misc.getHighlightColor(),
                "" + (int) (MAX_EX_RATE * 100f) + "%", "" + 3);
        tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                20f, true, true,
                new Object[] { HSII18nUtil.getHullModString("HSIFlexibleHangar2"), 2 * col1W,
                        HSII18nUtil.getHullModString("HSIFlexibleHangar3"), col1W,
                        HSII18nUtil.getHullModString("HSIFlexibleHangar4"), col1W,
                        HSII18nUtil.getHullModString("HSIFlexibleHangar5"), col1W,
                        HSII18nUtil.getHullModString("HSIFlexibleHangar6"), col1W });
        /*
         * for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
         * if (bay.getWing() != null && bay.getWing().getSpec() != null) {
         * tooltip.addRow(bay.getWing().getSpec().getWingName(), "" + (int)
         * (calculateEXRate(bay) * 100) + "%",
         * (int) calculateEXReplacement(bay), (int) (calculateEXRate(bay) * 2f) + "%",
         * calculateEXRate(bay) * 1f + "%");
         * }
         * }
         */
        for (String w : ship.getVariant().getWings()) {
            FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(w);
            if (spec == null)
                continue;
            tooltip.addRow(spec.getWingName(), "" + (int) (calculateEXRate(spec) * 100) + "%",
                    (int) calculateEXReplacement(ship, spec) + "",
                    String.format("%.2f", (calculateEXRate(spec)) * 5f) + "%",
                    String.format("%.3f", (calculateEXReplacementRate(spec))) + "%");
        }
        tooltip.addTable("Nan", 0, opad);
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            {
                LabelAPI desc = tooltip.addPara(HSII18nUtil.getHullModString("HSIFlexibleHangar7"), opad);
                desc.setColor(b);
                desc = tooltip.addPara(HSII18nUtil.getHullModString("HSIFlexibleHangar8"), opad);
                desc.setColor(b);
            }
        } else {
            tooltip.addPara(HSII18nUtil.getHullModString("HSIClickToChangeDetail"), opad, h, "LAlt");
        }
    }

    public float calculateEXReplacement(FighterLaunchBayAPI bay) {
        if (bay.getWing() != null && bay.getWing().getSpec() != null) {
            FighterWingSpecAPI spec = bay.getWing().getSpec();
            float op = spec.getOpCost(bay.getShip().getMutableStats());
            if (op > 15f) {
                return 1;
            } else if (op > 7f) {
                return 2;
            } else {
                return 3;
            }
        }
        return 0;
    }

    public static float calculateEXReplacement(ShipAPI ship, FighterWingSpecAPI spec) {
        float op = spec.getOpCost(ship.getMutableStats());
        if (op > 15f) {
            return 1;
        } else if (op > 7f) {
            return 2;
        } else {
            return 3;
        }
    }

    public static float calculateEXReplacementRate(FighterWingSpecAPI spec) {
        float time = spec.getRefitTime();
        time = Math.min(60f, time * 3f);
        if (time <= 1)
            time = 1;
        return 1 / time;
    }

    public static float calculateEXRate(FighterLaunchBayAPI bay) {
        if (bay.getWing() != null && bay.getWing().getSpec() != null) {
            FighterWingSpecAPI spec = bay.getWing().getSpec();
            float time = spec.getRefitTime();
            return MAX_EX_RATE * Math.min(1.5f, (10f / time));
        }
        return 0;
    }

    public static float calculateEXRate(FighterWingSpecAPI spec) {
        float time = spec.getRefitTime();
        return MAX_EX_RATE * Math.min(1.5f, (10f / time));
    }

    public static float calculateTotalEXRate(ShipAPI ship) {
        float sum = 0;
        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            sum += calculateEXRate(bay);
        }
        return sum;
    }

    public static float calculateTotalEXRate(ShipVariantAPI variant) {
        float sum = 0;
        for (String f : variant.getWings()) {
            if (Global.getSettings().getFighterWingSpec(f) != null) {
                sum += calculateEXRate(Global.getSettings().getFighterWingSpec(f));
            }
        }
        return sum;
    }

    /*
     * private void sendLog(String info){
     * Global.getLogger(this.getClass()).info(info);
     * }
     */
}
