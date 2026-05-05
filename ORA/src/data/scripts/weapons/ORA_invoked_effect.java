
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
//import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
//import java.util.HashMap;
//import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_invoked_effect implements OnHitEffectPlugin {    
    
//    private final static Map<ShipAPI.HullSize, Float> SIZE_RATIO = new HashMap<>();
//    static {
//        SIZE_RATIO.put(ShipAPI.HullSize.DEFAULT, 0.5f);
//        SIZE_RATIO.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.8f);
//        SIZE_RATIO.put(ShipAPI.HullSize.CRUISER, 0.6f);
//        SIZE_RATIO.put(ShipAPI.HullSize.DESTROYER, 0.4f);
//        SIZE_RATIO.put(ShipAPI.HullSize.FRIGATE, 0.2f);
//        SIZE_RATIO.put(ShipAPI.HullSize.FIGHTER, 0f);
//    }
    private final Float FIXED_SLOW = 0.25f;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        float mult=1;
        if(shieldHit)mult=0.5f;
        
//        if(target instanceof ShipAPI){
//            target.getVelocity().scale(mult*SIZE_RATIO.get(((ShipAPI)target).getHullSize()));
//        } else {
//            target.getVelocity().scale(0.5f);
//        }

        //effect get increased with skills and systems
        if(projectile.getSource()!=null && projectile.getSource().getMutableStats().getEnergyWeaponDamageMult()!=null){
            mult*=projectile.getSource().getMutableStats().getEnergyWeaponDamageMult().getModifiedValue();
        }
        
        target.getVelocity().scale(1-(FIXED_SLOW*mult));
        
        engine.addSmoothParticle(
                point,
                MathUtils.getPoint(
                        new Vector2f(),
                        150, 
                        VectorUtils.getAngle(target.getLocation(), point)
                ),
                100,
                0.75f,
                0.5f,
                new Color(255,25,200)
        );
    }
}
