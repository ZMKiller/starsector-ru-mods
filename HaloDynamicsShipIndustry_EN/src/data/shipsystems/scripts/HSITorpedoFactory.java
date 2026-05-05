package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import data.kit.AjimusUtils;

import java.util.HashMap;
import java.util.Map;

public class HSITorpedoFactory extends BaseShipSystemScript {
    private ShipAPI ship;
    private Map<String, Float> leftDamage = new HashMap<String, Float>();
    private static Map<WeaponSize, Float> RESUPPLY_DAMAGE = new HashMap<>();
    static {
        RESUPPLY_DAMAGE.put(WeaponSize.SMALL, 1250f);
        RESUPPLY_DAMAGE.put(WeaponSize.MEDIUM, 2500f);
        RESUPPLY_DAMAGE.put(WeaponSize.LARGE, 5000f);
    }
    private boolean once = true;
    private WeaponAPI d1 = null;
    private WeaponAPI d2 = null;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        /*
        if (once) {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                switch (weapon.getSlot().getId()) {
                    case "DECO1":
                        d1 = weapon;
                        break;
                    case "DECO2":
                        d2 = weapon;
                        break;
                    default:
                        break;
                }
            }
        }*/
        if (effectLevel >= 1) {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (!weapon.isBeam() && weapon.getSpec().getProjectileSpec() instanceof MissileSpecAPI&&(weapon.getType().equals(WeaponType.MISSILE)||weapon.getType().equals(WeaponType.SYNERGY)||weapon.getType().equals(WeaponType.COMPOSITE)||weapon.getType().equals(WeaponType.UNIVERSAL))) {
                    MissileSpecAPI spec = (MissileSpecAPI) weapon.getSpec().getProjectileSpec();
                    if (!AjimusUtils.isGuidedMissile(spec.getTypeString())) {
                        if (weapon.usesAmmo()) {
                            float ResupplyDamage = RESUPPLY_DAMAGE.get(weapon.getSize());
                            String slotID = weapon.getSlot().getId();
                            if (leftDamage.containsKey(slotID)) {
                                ResupplyDamage += leftDamage.get(slotID);
                            }
                            int ResupplyAmount = (int) (ResupplyDamage / (spec.getDamage().getDamage()));
                            weapon.getAmmoTracker().setAmmo(weapon.getAmmo() + ResupplyAmount);
                            float left = ResupplyDamage - ResupplyAmount * spec.getDamage().getBaseDamage();
                            leftDamage.put(slotID, Math.max(0, left));
                        }

                    }
                    //weapon.setRemainingCooldownTo(2f);
                }
            }
        }
        // animationPart
        /*
        if (ship.getHullSpec().getBaseHullId().equals("HSI_Weaver")) {
            switch (state) {
                case COOLDOWN:
                    d1.getAnimation().play();
                    break;
                case IDLE:
                    if (!ship.getSystem().isOutOfAmmo()) {
                        d1.getAnimation().pause();
                    }
                    break;
                case IN:
                    d2.getAnimation().play();
                    break;
                case OUT:
                    d2.getAnimation().pause();
                    break;
                default:
                    break;
            }
        }*/
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        boolean usable = true;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getType().equals(WeaponType.MISSILE) && weapon.isInBurst()) {
                usable = false;
                break;
            }
        }
        return usable;
    }
}
