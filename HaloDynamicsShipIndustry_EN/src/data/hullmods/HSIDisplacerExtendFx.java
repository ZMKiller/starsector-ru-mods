package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

import data.scripts.HSIRenderer.HSICombatRendererV2;
import data.scripts.HSIRenderer.HSIDisplacerExtendFxObject;

public class HSIDisplacerExtendFx extends BaseHullMod{

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(!ship.isAlive()) return;
        if(ship.getSystem()!=null){
            if(ship.getSystem().getEffectLevel()>0&&!ship.getCustomData().containsKey(HSIDisplacerExtendFxObject.HSIDisplacerKey)){
                HSICombatRendererV2.getInstance().addFxObject(new HSIDisplacerExtendFxObject(ship));
                ship.setCustomData(HSIDisplacerExtendFxObject.HSIDisplacerKey, true);
            }else if(ship.getSystem().getEffectLevel()<=0&&ship.getCustomData().containsKey(HSIDisplacerExtendFxObject.HSIDisplacerKey)){
                ship.getCustomData().remove(HSIDisplacerExtendFxObject.HSIDisplacerKey);
            }
        }
    }
}
