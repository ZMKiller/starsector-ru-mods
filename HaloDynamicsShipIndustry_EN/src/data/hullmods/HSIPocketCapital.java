package data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.lwjgl.util.vector.Vector2f;

public class HSIPocketCapital extends BaseHullMod {
    protected float DMG_BUFF = 20f;
    protected float RELOAD_BUFF = 20f;
    public void applyEffectsBeforeShipCreation(HullSize hullSize,
											   MutableShipStatsAPI stats, String id) {
        stats.getBallisticAmmoRegenMult().modifyPercent(id, RELOAD_BUFF);
        stats.getEnergyAmmoRegenMult().modifyPercent(id, RELOAD_BUFF);
        stats.getBallisticWeaponDamageMult().modifyPercent(id,DMG_BUFF);
        stats.getEnergyWeaponDamageMult().modifyPercent(id,DMG_BUFF);
	}
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) DMG_BUFF + "%";
        if (index == 1)
            return "" + (int) RELOAD_BUFF + "%";
        return null;
    }

}