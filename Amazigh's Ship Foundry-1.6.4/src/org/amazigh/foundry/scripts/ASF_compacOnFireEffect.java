package org.amazigh.foundry.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_compacOnFireEffect implements OnFireEffectPlugin {
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI source = weapon.getShip();
    	ShipAPI target = null;
    	
        if(source.getWeaponGroupFor(weapon)!=null ){
            //WEAPON IN AUTOFIRE
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //weapon group is autofiring
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon)){ //weapon group is not the selected group
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            }
            else {
                target = source.getShipTarget();
            }
        }
        
		CombatEntityAPI projC = engine.spawnProjectile(source, weapon, "A_S-F_compac_sub", projectile.getLocation(), projectile.getFacing(), source.getVelocity());
    	engine.addPlugin(new ASF_compacProjScript((DamagingProjectileAPI) projC, target));
    	
		engine.removeEntity(projectile);
	}
}