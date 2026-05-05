package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
//import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class ORA_pdEMP_effect implements OnHitEffectPlugin {  
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        if (target instanceof MissileAPI){
            ShipAPI s = ((MissileAPI) target).getSource();
            if(s!=null && s.getVariant().getHullMods().contains("eccm")){                
                zap(engine,(MissileAPI) target, projectile, 0.1f, 0.3f, 0.6f);
            } else {
                zap(engine,(MissileAPI) target, projectile, 0.2f, 0.55f, 0.75f);
            }
            
        } 
//        else if ((float) Math.random() > 0.75f && !shieldHit && target instanceof ShipAPI) {
//            float emp = projectile.getEmpAmount();
//            float dam = projectile.getDamageAmount()/2;
//
//            engine.spawnEmpArc(
//                    projectile.getSource(), 
//                    point, 
//                    target, 
//                    target,
//                    DamageType.ENERGY, 
//                    dam,
//                    emp, // emp 
//                    100000f, // max range 
//                    "tachyon_lance_emp_impact",
//                    20f, // thickness
//                    new Color(25,100,155,255),
//                    new Color(255,255,255,255)
//                    );
//        }
    }
    
    private void zap(CombatEngineAPI engine, MissileAPI missile, DamagingProjectileAPI projectile, float majorEffect, float normalEffect, float minorEffect){
        float rand=(float)Math.random();
        
        if(missile.isFizzling() && !missile.isArmedWhileFizzling()){rand=1;}
        
        //Effects are cumulative        
        if(rand<minorEffect){
            missile.flameOut();
        } else {
            return;
        }
        if(rand<normalEffect){                
            missile.setArmedWhileFizzling(false);
            missile.setArmingTime(60);
        } else {
            return;
        }
        if(rand<majorEffect){
            engine.applyDamage(
                    missile,
                    missile.getLocation(),
                    150,
                    DamageType.FRAGMENTATION, 
                    0,
                    true,
                    true,
                    projectile.getSource()
            );
        }
    } 
}
