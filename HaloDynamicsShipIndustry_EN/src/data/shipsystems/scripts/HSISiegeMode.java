package data.shipsystems.scripts;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class HSISiegeMode extends BaseShipSystemScript {
    // private boolean once = true;
    private static final float RECOIL = 0.5f;
    private static final float RANGE_FIX = 100f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getMaxSpeed().modifyMult(id, (1 - effectLevel));
        stats.getMaxRecoilMult().modifyMult(id, RECOIL * effectLevel);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_FIX * effectLevel);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_FIX * effectLevel);
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (!weapon.isDecorative())
                    continue;
                switch (weapon.getId()) {
                    case "HWI_Comet_Deco2":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(effectLevel * -12f, 0));
                        break;
                    case "HWI_Comet_Deco3":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(effectLevel * -12f, 0));
                        break;
                    case "HWI_Comet_Deco4":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, effectLevel * 3f));
                        break;
                    case "HWI_Comet_Deco5":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, effectLevel * -3f));
                        break;
                    case "HWI_Comet_Deco6":
                        if (effectLevel <= 0.75f) {
                            weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(effectLevel * 4f, 0f));
                        } else {
                            weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(3f, (effectLevel-0.75f) * 2f));
                        }
                        break;
                    case "HWI_Comet_Deco7":
                        if (effectLevel <= 0.75f) {
                            weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(effectLevel * 4f, 0f));
                        } else {
                            weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(3f, (effectLevel-0.75f) * -2f));
                        }
                        break;
                    default:
                        break;
                }
            }
            ship.getEngineController().forceShowAccelerating();
            ship.getEngineController().fadeToOtherColor(id, new Color(200,75,225,255), new Color(0,0,0,0), effectLevel, 0.67f);
            //ship.getEngineController().getShipEngines().get(0).getEngineSlot().setAngle(180f-effectLevel*45f);
            //ship.getEngineController().getShipEngines().get(1).getEngineSlot().setAngle(-180f+effectLevel*45f);
            //ship.getEngineController().setFlameLevel(ship.getEngineController().getShipEngines().get(0).getEngineSlot(), 0.4f+(0.5f*effectLevel));
            //ship.getEngineController().setFlameLevel(ship.getEngineController().getShipEngines().get(1).getEngineSlot(), 0.4f+(0.5f*effectLevel));
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxRecoilMult().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (!weapon.isDecorative())
                    continue;
                switch (weapon.getId()) {
                    case "HWI_Comet_Deco2":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0, 0f));
                        break;
                    case "HWI_Comet_Deco3":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0, 0f));
                        break;
                    case "HWI_Comet_Deco4":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, 0));
                        break;
                    case "HWI_Comet_Deco5":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, 0));
                        break;
                    case "HWI_Comet_Deco6":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, 0));
                        break;
                    case "HWI_Comet_Deco7":
                        weapon.setRenderOffsetForDecorativeBeamWeaponsOnly(new Vector2f(0f, 0));
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
