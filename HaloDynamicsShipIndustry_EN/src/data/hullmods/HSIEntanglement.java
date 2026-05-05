package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class HSIEntanglement extends BaseHullMod {

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            if (slot.isStationModule()) {
                ShipVariantAPI v = ship.getVariant().getModuleVariant(slot.getId());
                if (v != null) {
                    ShipAPI d = Global.getCombatEngine().createFXDrone(v);
                    d.setCustomData("HSIEntanglementData", ship);
                    ship.setCustomData("HSIEntanglementData", d);
                    d.setOwner(ship.getOwner());
                    d.setCollisionClass(CollisionClass.FIGHTER);
                    d.setDrone(false);
                    Global.getCombatEngine().addEntity(d);
                    ShipAIConfig config = new ShipAIConfig();
                    config.alwaysStrafeOffensively = true;
                    config.backingOffWhileNotVentingAllowed = false;
                    config.turnToFaceWithUndamagedArmor = false;
                    config.burnDriveIgnoreEnemies = true;
                    boolean carrier = false;
                    if (d != null && d.getVariant() != null) {
                        carrier = d.getVariant().isCarrier() && !d.getVariant().isCombat();
                    }
                    if (carrier) {
                        config.personalityOverride = Personalities.AGGRESSIVE;
                        config.backingOffWhileNotVentingAllowed = true;
                    } else {
                        config.personalityOverride = Personalities.RECKLESS;
                    }
                    d.setShipAI(Global.getSettings().createDefaultShipAI(d, config));
                    d.getLocation().set(slot.computePosition(ship));
                }
            }
        }
    }

}