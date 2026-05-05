package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

public class HWIVTMissilePlugin extends BaseEveryFrameCombatPlugin{
    private MissileAPI missile = null;
    private DamagingExplosionSpec explosionSpec = null;
    private IntervalUtil scan = new IntervalUtil(0.05f, 0.05f);
    private CombatEngineAPI engine = Global.getCombatEngine();
    private float coreRange = 35f;
    private float Range = 5f;

    private boolean exploded = false;


    public HWIVTMissilePlugin(MissileAPI missile,DamagingExplosionSpec explosion){
        this.missile = missile;
        this.explosionSpec = explosion;
        coreRange = explosion.getCoreRadius();
        Range = explosion.getRadius();
        if(explosionSpec == null){
            explosionSpec = createExplosionSpec(missile.getDamageAmount());
        }
    }

    public void advance(float amount, List<InputEventAPI> events) {
        if(!engine.isEntityInPlay(missile)){
            if(!exploded){
                engine.spawnDamagingExplosion(explosionSpec, missile.getSource(), missile.getLocation(),false);
                exploded = true;
            }
            engine.removePlugin(this);
            return;
        }
        if(engine.isPaused()) return;
        scan.advance(amount);
        if(scan.intervalElapsed()){
            for(MissileAPI m : engine.getMissiles()){
                if(m.getOwner()!=missile.getOwner()&&Misc.getDistance(m.getLocation(), missile.getLocation())+m.getCollisionRadius()<coreRange){
                    engine.spawnDamagingExplosion(explosionSpec, missile.getSource(), missile.getLocation(),false);
                    engine.removeEntity(missile);
                    exploded = true;
                }
            }
            for(ShipAPI s : engine.getShips()){
                if(s.getOwner()!=missile.getOwner()&&Misc.getDistance(s.getLocation(), missile.getLocation())+s.getCollisionRadius()<coreRange){
                    engine.spawnDamagingExplosion(explosionSpec, missile.getSource(), missile.getLocation(),false);
                    engine.removeEntity(missile);
                    exploded = true;
                }
            }
        }
	}

    public DamagingExplosionSpec createExplosionSpec(float damage) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                45f, // radius
                35f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                150, // particleCount
                new Color(255, 255, 255, 255), // particleColor
                new Color(100, 100, 255, 175) // explosionColor
        );

        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_guardian");
        return spec;
    }
}
