package data.weapons.scripts;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

import data.ai.HSIThreatSharedData;

public class HWIThreatAnalysisPDEffect implements OnFireEffectPlugin{

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
        if(weapon.getShip()!=null&&weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon)!=null){
            AutofireAIPlugin ai = weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon);
            CombatEntityAPI target = ai.getTargetMissile()!=null?ai.getTargetMissile():ai.getTargetShip();
            HSIThreatSharedData.addProjAuto(proj, target);
        }
    }
    
}
