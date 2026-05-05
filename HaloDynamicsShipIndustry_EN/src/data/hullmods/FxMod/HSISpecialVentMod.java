package data.hullmods.FxMod;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

import data.kit.HSIIds;
import data.scripts.HSIRenderer.HSICombatRendererV2;
import data.scripts.HSIRenderer.HSISpecialVentRendererObject;

public class HSISpecialVentMod extends BaseHullMod{

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.setFluxVentTextureSheet(HSIIds.SPRITE.EMPTY_16PX);
        if(Global.getCombatEngine()==null) return;
        HSICombatRendererV2.getInstance().addFxObject(new HSISpecialVentRendererObject(ship));
    }
}
