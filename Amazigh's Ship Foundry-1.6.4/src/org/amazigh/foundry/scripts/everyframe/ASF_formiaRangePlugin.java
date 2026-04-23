package org.amazigh.foundry.scripts.everyframe;
 // Credit to Nia for the lostech range modifier script this is based on

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import java.util.HashMap;
import java.util.Map;

public class ASF_formiaRangePlugin implements EveryFrameWeaponEffectPlugin {

    private static final Map<String,Float> RANGE_MODIFIERS = new HashMap<>();
    static {
        RANGE_MODIFIERS.put("A_S-F_formia_sling",-400f);
        RANGE_MODIFIERS.put("A_S-F_formia_storm",-600f);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getShip() == null) {
            return;
        }

        if (!weapon.getShip().hasListenerOfClass(asf_VariableRangeListener.class)) {
            weapon.getShip().addListener(new asf_VariableRangeListener());
        }
    }
    
    private static class asf_VariableRangeListener implements WeaponBaseRangeModifier {

        @Override
        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0f;
        }

        @Override
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }

        @Override
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (!RANGE_MODIFIERS.containsKey(weapon.getSpec().getWeaponId())) {
                return 0f;
            }
            
            if (weapon.getSlot().getWeaponType() == WeaponAPI.WeaponType.ENERGY) {
            	return RANGE_MODIFIERS.get(weapon.getSpec().getWeaponId());
            } else {
                return 0f;
            }
        }
    }
}