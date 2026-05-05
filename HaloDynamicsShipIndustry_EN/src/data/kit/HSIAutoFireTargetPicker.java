package data.kit;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.util.Misc;

public class HSIAutoFireTargetPicker {
    public HSIAutoFireTargetPicker() {

    }

    public static CombatEntityAPI PickAutoFireTarget(WeaponAPI weapon) {
        ShipAPI source = weapon.getShip();
        if (source != null) {
            WeaponGroupAPI group = source.getWeaponGroupFor(weapon);
            if (group != null) {
                if (group.isAutofiring()) {
                    AutofireAIPlugin ai = group.getAutofirePlugin(weapon);
                    ShipAPI targetShip = ai.getTargetShip();
                    MissileAPI targetMissile = ai.getTargetMissile();
                    if (weapon.getSpec().getAIHints().contains(AIHints.PD_ALSO) && targetShip != null) {
                        return targetShip;
                    } else if (weapon.getSpec().getAIHints().contains(AIHints.PD_ONLY)) {
                        return targetMissile;
                    } else {
                        if (targetShip != null) {
                            return targetShip;
                        } else {
                            return targetMissile;
                        }
                    }
                } else {
                    Vector2f interestPoint = source.getMouseTarget();
                    return Misc.findClosestShipEnemyOf(source, interestPoint, HullSize.FIGHTER, 600f,
                            true);
                }
            } else {
                Vector2f interestPoint = source.getMouseTarget();
                return Misc.findClosestShipEnemyOf(source, interestPoint, HullSize.FIGHTER, 600f, true);
            }
        } else {
            return null;
        }
    }
}
