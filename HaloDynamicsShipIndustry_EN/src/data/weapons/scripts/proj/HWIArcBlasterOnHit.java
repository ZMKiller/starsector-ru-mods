package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class HWIArcBlasterOnHit implements OnHitEffectPlugin {

    public static final float FORCE = 40000f;
    public static final float MASS_DAMAGE = 0.25f;
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
            /*if(target instanceof ShipAPI){
                DamagingProjectileAPI proj = engine.spawnDamagingExplosion(createExplosionSpec(projectile.getDamageAmount()*0.25f,400f), projectile.getSource(), point,false);
                proj.getDamagedAlready().add(target);
                DamagingProjectileAPI proj2 = engine.spawnDamagingExplosion(createExplosionSpec(projectile.getDamageAmount()*0.25f,800f), projectile.getSource(), point,false);
                proj2.getDamagedAlready().add(target);
                DamagingProjectileAPI proj3 = engine.spawnDamagingExplosion(createExplosionSpec(projectile.getDamageAmount()*0.25f,1600f), projectile.getSource(), point,false);
                proj3.getDamagedAlready().add(target);
            }*/

        if(target instanceof ShipAPI){
            ShipAPI e = (ShipAPI) target;
            if(!e.isAlive()||e.isHulk()) return;
            Vector2f move = Misc.getUnitVector(e.getLocation(),point);
            float mass = 1;
            if(e.getMass()!=0) mass = e.getMass();
            float actualForce = FORCE/mass;
            move.scale(actualForce);
            e.getVelocity().set(Vector2f.add(new Vector2f(e.getVelocity()),move,null));
            if(actualForce<=2){
                for(ShipEngineControllerAPI.ShipEngineAPI se:e.getEngineController().getShipEngines()){
                    if(Math.random()>0.05f) se.disable();
                }
            }
            engine.applyDamage(projectile, e, point,
                    e.getMass()*MASS_DAMAGE, DamageType.KINETIC, 0f, false, false, projectile.getSource(), true);
            BoundsAPI bound = e.getExactBounds();
            if(bound != null){
                int skip = MathUtils.clamp(bound.getSegments().size()/10,0,2) ;
                int c = skip;
                Color color = new Color(255,255,255,255);

                for(int i = 0;i<10;i++){
                        EmpArcEntityAPI.EmpArcParams params = new EmpArcEntityAPI.EmpArcParams();
                        //Color color = weapon.getSpec().getGlowColor();
                        params.segmentLengthMult = 8f;
                        params.zigZagReductionFactor = 0.15f;
                        params.fadeOutDist = 500f;
                        params.minFadeOutMult = 2f;
                        params.flickerRateMult = 0.3f;
                        Vector2f loc = Vector2f.add(e.getLocation(),(Vector2f) Misc.getUnitVectorAtDegreeAngle(e.getFacing()+90f+i*20f).scale(e.getCollisionRadius()*0.8f),null);
                        EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(loc, e, point, e,
                                40f, // thickness
                                color,
                                new Color(255,255,255,255),
                                params
                        );
                        arc.setCoreWidthOverride(20f);

                        arc.setSingleFlickerMode(true);
                        arc.setRenderGlowAtStart(false);

                }
                Global.getSoundPlayer().playSound("energy_lash_fire_at_enemy", 1f, 1f, point, e.getVelocity());
            }
        }
    }

    public DamagingExplosionSpec createExplosionSpec(float damage,float radius) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                radius, // radius
                radius*0.66f, // coreRadius
                damage, // maxDamage
                damage / 4f, // minDamage
                CollisionClass.PROJECTILE_NO_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                0, // particleCount
                new Color(255, 255, 255, 255), // particleColor
                new Color(75, 75, 200, 35) // explosionColor
        );

        spec.setDamageType(DamageType.KINETIC);
        spec.setUseDetailedExplosion(true);
        spec.setSoundSetId("HSI_Blaster_Exposion");
        return spec;
    }
}
