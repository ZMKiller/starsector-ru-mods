package data.scripts.hullmods;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.SWP_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;
import org.lwjgl.util.vector.Vector2f;

public class SWP_PDConversion extends BaseHullMod {

    public static int OP_INCREASE_SMALL = 1;
    public static int OP_INCREASE_MEDIUM = 2;
    public static int OP_INCREASE_LARGE = 4;
    public static float DAMAGE_BONUS_PERCENT = 25f;
    public static float HIT_STRENGTH_BONUS_PERCENT = 50f;
    public static float RANGE_BONUS_PERCENT = 25f;
    public static float SMOD_ALL_DAMAGE_BONUS_PERCENT = 5f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("pointdefenseai");
        BLOCKED_HULLMODS.add("ii_targeting_package");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.SMALL_PD_MOD).modifyFlat(id, OP_INCREASE_SMALL);
        stats.getDynamic().getMod(Stats.MEDIUM_PD_MOD).modifyFlat(id, OP_INCREASE_MEDIUM);
        stats.getDynamic().getMod(Stats.LARGE_PD_MOD).modifyFlat(id, OP_INCREASE_LARGE);

        if (isSMod(stats)) {
            stats.getEnergyWeaponDamageMult().modifyPercent(id, SMOD_ALL_DAMAGE_BONUS_PERCENT);
            stats.getBallisticWeaponDamageMult().modifyPercent(id, SMOD_ALL_DAMAGE_BONUS_PERCENT);
            stats.getMissileWeaponDamageMult().modifyPercent(id, SMOD_ALL_DAMAGE_BONUS_PERCENT);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getNonBuiltInHullmods().contains(tmp) && !ship.getVariant().getSMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                SWP_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }

        ship.addListener(new PDCDamageDealtMod());
        ship.addListener(new PDCWeaponRangeMod());
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            weapon.setPD(false);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "" + OP_INCREASE_SMALL;
        }
        if (index == 1) {
            return "" + OP_INCREASE_MEDIUM;
        }
        if (index == 2) {
            return "" + OP_INCREASE_LARGE;
        }
        if (index == 3) {
            return "" + (int) Math.round(RANGE_BONUS_PERCENT) + "%";
        }
        if (index == 4) {
            return "" + (int) Math.round(DAMAGE_BONUS_PERCENT) + "%";
        }
        if (index == 5) {
            return "" + (int) Math.round(HIT_STRENGTH_BONUS_PERCENT) + "%";
        }
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) Math.round(SMOD_ALL_DAMAGE_BONUS_PERCENT) + "%";
        }
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if ((ship == null) || (ship.getVariant() == null) || isForModSpec) {
            return;
        }

        float opad = 10f;
        int extraOPToInstall = 0;
        for (String slotId : ship.getVariant().getFittedWeaponSlots()) {
            WeaponSpecAPI weaponSpec = ship.getVariant().getWeaponSpec(slotId);
            if (weaponSpec.getAIHints().contains(AIHints.PD)) {
                switch (weaponSpec.getSize()) {
                    case SMALL ->
                        extraOPToInstall += OP_INCREASE_SMALL;
                    case MEDIUM ->
                        extraOPToInstall += OP_INCREASE_MEDIUM;
                    case LARGE ->
                        extraOPToInstall += OP_INCREASE_LARGE;
                    default -> {
                    }
                }
            }
        }

        if (extraOPToInstall > 0) {
            tooltip.addSectionHeading("Ordnance points", Alignment.MID, opad);

            if (ship.getVariant().hasHullMod(spec.getId())) {
                tooltip.addPara("An additional %s ordnance points are consumed by the ship's (would-be) point defense "
                        + "weapons as a result of this hullmod's effects.", opad,
                        Misc.getHighlightColor(),
                        "" + extraOPToInstall);
            } else {
                int opRequiredToInstall = spec.getCostFor(ship.getHullSize()) + extraOPToInstall;
                tooltip.addPara("If this hullmod were to be installed, an additional %s ordnance points would be "
                        + "consumed by the ship's point defense weapons, for a total cost of %s ordnance points.", opad,
                        enoughOPToInstall(ship) ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(),
                        "" + extraOPToInstall,
                        "" + opRequiredToInstall);
            }
        }
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    private boolean enoughOPToInstall(ShipAPI ship) {
        if ((ship == null) || (ship.getVariant() == null)) {
            return true;
        }
        if (ship.getVariant().hasHullMod(spec.getId())) {
            return true;
        }

        FleetMemberAPI member = ship.getFleetMember();
        MutableCharacterStatsAPI stats = null;
        if ((member != null) && (member.getFleetCommanderForStats() != null)) {
            stats = member.getFleetCommanderForStats().getFleetCommanderStats();
        }

        int unusedOP = ship.getVariant().getUnusedOP(stats);
        int opRequiredToInstall = spec.getCostFor(ship.getHullSize());
        for (String slotId : ship.getVariant().getFittedWeaponSlots()) {
            WeaponSpecAPI weaponSpec = ship.getVariant().getWeaponSpec(slotId);
            if (weaponSpec.getAIHints().contains(AIHints.PD)) {
                switch (weaponSpec.getSize()) {
                    case SMALL ->
                        opRequiredToInstall += OP_INCREASE_SMALL;
                    case MEDIUM ->
                        opRequiredToInstall += OP_INCREASE_MEDIUM;
                    case LARGE ->
                        opRequiredToInstall += OP_INCREASE_LARGE;
                    default -> {
                    }
                }
            }
        }

        return unusedOP >= opRequiredToInstall;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod("pointdefenseai")) {
            return "Incompatible with Integrated Point Defense AI";
        }
        if ((ship != null) && ship.getVariant().hasHullMod("ii_targeting_package")) {
            return "Incompatible with Imperial Targeting Package";
        }
        if (!enoughOPToInstall(ship)) {
            return "Insufficient ordnance points due to increased PD cost";
        }
        return super.getUnapplicableReason(ship);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod("pointdefenseai")) {
            return false;
        }
        if ((ship != null) && ship.getVariant().hasHullMod("ii_targeting_package")) {
            return false;
        }
        if (!enoughOPToInstall(ship)) {
            return false;
        }
        return super.isApplicableToShip(ship);
    }

    private static class PDCDamageDealtMod implements DamageDealtModifier {

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            WeaponAPI weapon = null;
            if (param instanceof DamagingProjectileAPI damagingProjectileAPI) {
                weapon = damagingProjectileAPI.getWeapon();
            } else if (param instanceof BeamAPI beamAPI) {
                weapon = beamAPI.getWeapon();
            } else if (param instanceof MissileAPI missileAPI) {
                weapon = missileAPI.getWeapon();
            }

            String id = "swp_pdc_dam_mod";
            if (weapon == null) {
                if (damage.getStats() != null) {
                    damage.getStats().getHitStrengthBonus().unmodify(id);
                }
                return null;
            }
            if (!weapon.getSpec().getAIHints().contains(AIHints.PD)) {
                if (damage.getStats() != null) {
                    damage.getStats().getHitStrengthBonus().unmodify(id);
                }
                return null;
            }

            damage.getModifier().modifyPercent(id, DAMAGE_BONUS_PERCENT);
            if (damage.getStats() != null) {
                damage.getStats().getHitStrengthBonus().modifyPercent(id, HIT_STRENGTH_BONUS_PERCENT);
            }

            return id;
        }
    }

    private static class PDCWeaponRangeMod implements WeaponRangeModifier {

        @Override
        public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            if (!weapon.getOriginalSpec().getAIHints().contains(AIHints.PD) || (weapon.getType() == WeaponType.MISSILE)) {
                return 0f;
            }
            return RANGE_BONUS_PERCENT;
        }

        @Override
        public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }

        @Override
        public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            return 0f;
        }
    }
}
