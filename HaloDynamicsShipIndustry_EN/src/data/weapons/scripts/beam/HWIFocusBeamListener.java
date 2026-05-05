package data.weapons.scripts.beam;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

public class HWIFocusBeamListener implements DamageDealtModifier {
    private String id = "HWI_FocusLance";

    public String modifyDamageDealt(Object param, CombatEntityAPI Target, DamageAPI damage, Vector2f point,
            boolean shieldHit) {
        if (shieldHit && param instanceof BeamAPI) {
            BeamAPI b = (BeamAPI) param;
            if (b.getWeapon().getId().equals(id)) {
                ShipAPI target = (ShipAPI) b.getDamageTarget();
                float shieldEfficiency = Math.min(1,
                        target.getShield().getFluxPerPointOfDamage()
                                * target.getMutableStats().getShieldDamageTakenMult().getModifiedValue()
                                * target.getMutableStats().getShieldAbsorptionMult().getModifiedValue());
                // Global.getCombatEngine().maintainStatusForPlayerShip(beam.getSource(),
                // null,"TargetEfficiency",
                // shieldEfficiency+"|"+target.getShield().getFluxPerPointOfDamage(), false);
                //float reduction = b.getDamage().computeDamageDealt(sinceLast) * (1 - shieldEfficiency);
                //float pierce = reduction * (ignore * keepTarget / max) / shieldEfficiency;
            }
        }
        return null;
    }
}
