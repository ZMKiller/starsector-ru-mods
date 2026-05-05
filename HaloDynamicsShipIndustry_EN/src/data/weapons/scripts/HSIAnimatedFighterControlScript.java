package data.weapons.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.HSIContrail.HSIContrailEntityPlugin;

import java.awt.*;

public class HSIAnimatedFighterControlScript implements EveryFrameWeaponEffectPlugin {

    protected boolean init = false;

    protected ShipAPI ship;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!init) init(weapon);
        ship.getSpriteAPI().setColor(new Color(255,255,255,0));
        if(ship!=null){
            for(WeaponAPI w:ship.getAllWeapons()) {
                if (w == weapon) continue;
                w.setCurrAngle(weapon.getCurrAngle());
            }
        }
    }

    private void init(WeaponAPI weapon){
        this.ship = weapon.getShip();
        init = true;
    }
}
