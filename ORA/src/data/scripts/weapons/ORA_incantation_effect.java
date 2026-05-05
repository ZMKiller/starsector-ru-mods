
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.magiclib.util.MagicRender;
import java.awt.Color;
//import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_incantation_effect implements OnHitEffectPlugin {    
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult,  CombatEngineAPI engine) {
        
        DamagingExplosionSpec boom = new DamagingExplosionSpec(
                    0.05f,
                    projectile.getDamageAmount()/4,
                    0,
                    projectile.getDamageAmount(),
                    0,
                    CollisionClass.PROJECTILE_FF,
                    CollisionClass.PROJECTILE_NO_FF,
                    2,
                    5,
                    0.5f,
                    50,
                    new Color(225,100,0,64),
                    new Color(200,100,25,64)
            );
        boom.setDamageType(DamageType.FRAGMENTATION);
//        boom.setShowGraphic(MagicRender.screenCheck(1, point));
        boom.setShowGraphic(false);
        if(target instanceof ShipAPI){
            engine.spawnDamagingExplosion(boom, (ShipAPI) target, point, false);
            //DEBUG
//            for(int i =0; i<20; i++){
//                engine.addHitParticle(MathUtils.getRandomPointOnCircumference(point,projectile.getDamageAmount()/4), new Vector2f(), 5, 1, 0.5f, Color.RED);
//                engine.addHitParticle(MathUtils.getRandomPointOnCircumference(point,projectile.getDamageAmount()/8), new Vector2f(), 5, 1, 0.5f, Color.YELLOW);
//            }
        } else {
            engine.spawnDamagingExplosion(boom, projectile.getSource() , point);
            //DEBUG
//            for(int i =0; i<20; i++){
//                engine.addHitParticle(MathUtils.getRandomPointOnCircumference(point,projectile.getDamageAmount()/4), new Vector2f(), 5, 1, 0.5f, Color.RED);
//                engine.addHitParticle(MathUtils.getRandomPointOnCircumference(point,projectile.getDamageAmount()/8), new Vector2f(), 5, 1, 0.5f, Color.YELLOW);
//            }
        }
        
//        projectile.setDamageAmount(0.75f*projectile.getBaseDamageAmount());
        
        if(MagicRender.screenCheck(1, point)){
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    200,
                    1,
                    0.05f,
                    Color.WHITE
            );
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    200,
                    1,
                    0.1f,
                    Color.WHITE
            );
            engine.spawnExplosion(point, new Vector2f(), new Color(0,50,50,255), 300, 0.15f);
        }
    }
}
