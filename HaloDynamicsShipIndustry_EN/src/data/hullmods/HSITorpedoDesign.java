package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;
import data.scripts.HSIInteractableItems.HSIInteractableHullmodSave;

public class HSITorpedoDesign extends BaseHullMod {
    protected static Map<WeaponSize, Integer> OP_COST = new HashMap<>();
    static {
        OP_COST.put(WeaponSize.SMALL, 1);
        OP_COST.put(WeaponSize.MEDIUM, 3);
        OP_COST.put(WeaponSize.LARGE, 5);
    }
    protected static final float EXTRA_TORP_TIME = 25f;
    protected static final float TORP_LOSS = 50f;
    protected static final float LONG_DMG_DEBUFF = 0.6f;
    protected static final float LONG_RANGE_BONUS = 2f;

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int)TORP_LOSS + "%";
        if (index == 1)
            return "" + OP_COST.get(WeaponSize.SMALL) + "/" + OP_COST.get(WeaponSize.MEDIUM) + "/"
                    + OP_COST.get(WeaponSize.LARGE);
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize,
            MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, -OP_COST.get(WeaponSize.SMALL));
        stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, -OP_COST.get(WeaponSize.MEDIUM));
        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -OP_COST.get(WeaponSize.LARGE));
        stats.getMissileAmmoBonus().modifyMult(id, (100-TORP_LOSS)/100f);

        HSIInteractableHullmodSave save = HSIInteractableHullmodSave.getInstance(stats.getFleetMember());
        if (save != null) {
            if (!save.getMem().isEmpty() && save.getMem().get("HSITorpDesign").equals("HSITorpDesignLong")) {
                stats.getMissileWeaponRangeBonus().modifyMult(id, LONG_RANGE_BONUS);
                stats.getMissileWeaponDamageMult().modifyMult(id, LONG_DMG_DEBUFF);
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        MutableShipStatsAPI stats= ship.getMutableStats();
        float frac = stats.getEnergyWeaponRangeBonus().computeEffective(1200f) / 1200f-1f;
        if(frac<=0) frac = 0;
        stats.getMissileMaxSpeedBonus().modifyMult(id, frac*0.5f+1f);
        stats.getMissileAccelerationBonus().modifyMult(id, frac*0.5f+1f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        HSIInteractableHullmodSave save = HSIInteractableHullmodSave.getInstance(ship.getFleetMember());
        if (save != null) {
            if (!save.getMem().isEmpty() && save.getMem().get("HSITorpDesign").equals("HSITorpDesignLong")) {
                return;
            }
        }
        if (engine.isPaused())
            return;
        if (!ship.isAlive())
            return;
        boolean LastFrameisSteady = false;
        boolean shouldRunTorpAttack = false;
        float left = 0;
        if (ship.getCustomData().containsKey("HSITorp1")) {
            left = (Float) ship.getCustomData().get("HSITorp1");
            left = Math.max(0, left - amount);
            ship.setCustomData("HSITorp1", left);
        } else {
            ship.setCustomData("HSITorp1", left);
        }
        if (ship.getCustomData().containsKey("HSITorp0")) {
            LastFrameisSteady = (Boolean) ship.getCustomData().get("HSITorp0");
            if (LastFrameisSteady != ship.areSignificantEnemiesInRange()) {
                shouldRunTorpAttack = true;
            }
            ship.setCustomData("HSITorp0", ship.areSignificantEnemiesInRange());
        } else {
            ship.setCustomData("HSITorp0", ship.areSignificantEnemiesInRange());
        }
        if (shouldRunTorpAttack && left <= 0) {
            doTorpAttack(ship);
            ship.setCustomData("HSITorp1", 15f);
        }
    }

    private void doTorpAttack(ShipAPI ship) {
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            WeaponSlotAPI slot = weapon.getSlot();
            if (slot.isHardpoint() && Math.abs(slot.getAngle()) < 1 && !slot.isDecorative()
                    && !slot.isStationModule()
                    && (weapon.getType().equals(WeaponType.MISSILE) || weapon.getType().equals(WeaponType.COMPOSITE)
                            || weapon.getType().equals(WeaponType.SYNERGY)
                            || weapon.getType().equals(WeaponType.UNIVERSAL))) {
                int burstsize = weapon.getSpec().getBurstSize();
                int shot = 0;
                while (burstsize > 0) {
                    CombatEntityAPI e = Global.getCombatEngine().spawnProjectile(ship, weapon,
                            weapon.getSpec().getWeaponId(),
                            Misc.getPointWithinRadius(weapon.getFirePoint(0), OP_COST.get(weapon.getSize())),
                            (weapon.getCurrAngle() + (float) (Math.random() - 0.5) * 16f) % 360, ship.getVelocity());
                    if (e instanceof MissileAPI) {
                        MissileAPI m = (MissileAPI) e;
                        m.setMaxFlightTime(m.getMaxFlightTime() + EXTRA_TORP_TIME);
                        m.setCollisionClass(CollisionClass.MISSILE_NO_FF);
                        if (m.getSpec().getOnFireEffect() != null)
                            m.getSpec().getOnFireEffect().onFire(m, weapon, Global.getCombatEngine());
                    }
                    shot++;
                    burstsize -= (Math.max((int) (shot / 2), 1));
                }
                weapon.setRemainingCooldownTo(OPEN_FIRE_COOLDOWN);
            }
        }
    }

    protected static final float OPEN_FIRE_COOLDOWN = 10f;

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        TooltipMakerAPI text;
        //HSIInteractableHullmodSave save = HSIInteractableHullmodSave.getInstance(ship.getFleetMember());
        //if (save != null) {
            //processInput(save);
            //if (save.getMem().isEmpty()) {
                tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITorpDesign1"),
                        Alignment.TMID, 4f);
                tooltip.addPara(HSII18nUtil.getHullModString("HSITorpDesign2"), 10f, Misc.getHighlightColor(),
                        ""+(int)OPEN_FIRE_COOLDOWN,"" + (int)EXTRA_TORP_TIME);
            /*} else {
                if (save.getMem().get("HSITorpDesign").equals("HSITorpDesignClose")) {
                    tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITorpDesign1"),
                            Alignment.TMID, 4f);
                    tooltip.addPara(HSII18nUtil.getHullModString("HSITorpDesign2"), 10f, Misc.getHighlightColor(),
                            "" + (int)EXTRA_TORP_TIME);
                } else {
                    tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITorpDesign3"),
                            Alignment.TMID, 4f);
                    tooltip.addPara(HSII18nUtil.getHullModString("HSITorpDesign4"), 10f, Misc.getHighlightColor(),
                            "" + (int) ((100 - (int)(LONG_DMG_DEBUFF* 100))) + "%", "" + (int)((LONG_RANGE_BONUS - 1) * 100f )+ "%");
                }
            }
        } else {
            tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSITorpDesign1"),
                    Alignment.TMID, 4f);
            tooltip.addPara(HSII18nUtil.getHullModString("HSITorpDesign2"), 10f, Misc.getHighlightColor(),
                    "" + (int)EXTRA_TORP_TIME);
            //tooltip.addPara("NULL", 0);
        }
        tooltip.addPara(HSII18nUtil.getHullModString("HSIClickToChange"), 10f, Misc.getHighlightColor(),
                    "LAlt" );*/
    }

    private void processInput(HSIInteractableHullmodSave save) {
        if (save.getMem().isEmpty()) {
            save.getMem().put("HSITorpDesign", "HSITorpDesignClose");
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            if (save.getMem().get("HSITorpDesign").equals("HSITorpDesignClose")) {
                save.getMem().put("HSITorpDesign", "HSITorpDesignLong");
            } else {
                save.getMem().put("HSITorpDesign", "HSITorpDesignClose");
            }

        }
    }
}
