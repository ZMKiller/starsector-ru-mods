package data.hullmods;

import java.awt.Color;
import java.awt.Label;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;
import data.weapons.scripts.HWITurboChargerWeapon;

public class HSIWeaponIndUpgrades extends BaseHullMod {
    protected static List<String> availableWeapons = new ArrayList<>();
    static {
        availableWeapons.add("HWI_Stella");
        availableWeapons.add("HWI_Astesia");
        availableWeapons.add("HWI_TurbocannonL");
        availableWeapons.add("HWI_TurbocannonM");
        availableWeapons.add("HWI_TurbocannonS");
        availableWeapons.add("HWI_mjolnir");
        availableWeapons.add("HWI_lightneedler");
        availableWeapons.add("HWI_heavyneedler");
        availableWeapons.add("HWI_autopulse");
        availableWeapons.add("HWI_guardian");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        // ship.addListener(new HSIWeaponUpgradeListner());
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getSpec().getWeaponId().equals("HWI_Stella")) {
                weapon.getAmmoTracker().setAmmoPerSecond(0.05f);
            }
            if (weapon.getSpec().getWeaponId().equals("HWI_Astesia")) {
                weapon.getAmmoTracker().setAmmoPerSecond(0.15f);
                weapon.getAmmoTracker().setReloadSize(3);
            }
            if (weapon.getSpec().getWeaponId().equals("HWI_TurbocannonL")
                    || weapon.getSpec().getWeaponId().equals("HWI_TurbocannonM")
                    || weapon.getSpec().getWeaponId().equals("HWI_TurbocannonS")) {
                if (weapon.getEffectPlugin() instanceof HWITurboChargerWeapon) {
                    HWITurboChargerWeapon effect = (HWITurboChargerWeapon) weapon.getEffectPlugin();
                    effect.setMaxShot(14);
                    effect.setLowest(5);
                }
            }
            /*
             * if (weapon.getSpec().getWeaponId().equals("HWI_mjolnir")) {
             * //weapon.ensureClonedSpec();
             * //weapon.getSpec().setCustomPrimary(HSII18nUtil.getWeaponString("HWI_mjolnir"
             * ));
             * //weapon.getSpec().setCustomPrimaryHL(HSII18nUtil.getWeaponString(
             * "HWI_mjolnir_HL"));
             * }
             * if (weapon.getSpec().getWeaponId().equals("HWI_lightneedler")
             * || weapon.getSpec().getWeaponId().equals("HWI_heavyneedler")) {
             * weapon.ensureClonedSpec();
             * weapon.getAmmoTracker().setMaxAmmo(weapon.getSpec().getMaxAmmo() * 2);
             * weapon.getAmmoTracker().setAmmo(weapon.getMaxAmmo());
             * }
             * if (weapon.getSpec().getWeaponId().equals("HWI_autopulse")) {
             * weapon.ensureClonedSpec();
             * weapon.getAmmoTracker().setMaxAmmo((int) (weapon.getMaxAmmo() - 0.5f *
             * weapon.getSpec().getMaxAmmo()));
             * weapon.getAmmoTracker().setAmmo(weapon.getMaxAmmo());
             * weapon.getDamage().getModifier().modifyMult(id, 1.4f);
             * }
             */
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return HSII18nUtil.getHullModString("HSIUpgradesEffect");
        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getVariant().hasHullMod("HSI_Halo");
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null) {
            if (!ship.getVariant().hasHullMod("HSI_Halo"))
                return HSII18nUtil.getHullModString("HSINoTurbulanceWarning");
        }
        return null;
    }

    /*
     * public class HSIWeaponUpgradeListner implements WeaponBaseRangeModifier {
     * public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
     * return 0;
     * }
     * 
     * public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
     * return 1;
     * }
     * 
     * public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
     * if(weapon.getSpec().getWeaponId().startsWith("HWI_")||weapon.getSpec().
     * getWeaponId().startsWith("HSI_")){
     * if(weapon.getSpec().getMaxRange()<=900){
     * return Math.min(900-weapon.getSpec().getMaxRange(), 50f);
     * }
     * }
     * return 0;
     * }
     * }
     */

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        if (ship == null)
            return;
        float col1W = (width - 12f);
        // float col2W = (width - 12f) * 2f / 3f;
        // ,HSII18nUtil.getHullModString("HSIUpgradesEffect"), col2W });
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            List<String> hl = new ArrayList<>();
            String de = "";
            List<WeaponSpecAPI> specs = new ArrayList<>();
            if (ship != null) {

                tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                        20f, true, true,
                        new Object[] { HSII18nUtil.getHullModString("HSIUpgrades"), col1W });
                for (WeaponAPI weapon : ship.getAllWeapons()) {
                    if (availableWeapons.contains(weapon.getId()) && !specs.contains(weapon.getSpec())) {
                        specs.add(weapon.getSpec());
                    }
                }
                for (WeaponSpecAPI spec : specs) {
                    tooltip.addRow(spec.getWeaponName());
                }
                tooltip.addTable(HSII18nUtil.getHullModString("HSIUpgradesEmpty"), 0, opad);
                tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIUpgradesListTitle"), Alignment.MID,
                        opad + 7f);
            }
            for (WeaponSpecAPI s : Global.getSettings().getAllWeaponSpecs()) {
                if (s.hasTag("HWI_Upd")) {
                    de += " / ";
                    de += s.getWeaponName();
                    if (!specs.isEmpty() && specs.contains(s)) {
                        hl.add(s.getWeaponName());
                    }
                }
            }
            for (WeaponSpecAPI s : Global.getSettings().getSystemWeaponSpecs()) {
                if (s.hasTag("HWI_Upd")) {
                    de += " / ";
                    de += s.getWeaponName();
                    if (!specs.isEmpty() && specs.contains(s)) {
                        hl.add(s.getWeaponName());
                    }
                }
            }
            de = de.substring(3);
            tooltip.addPara(de, opad, h, hl.toArray(new String[hl.size()]));
        } else {
            tooltip.addPara(HSII18nUtil.getHullModString("HSIClickToChangeDetail"), opad, h, "LAlt");
        }

    }

}
