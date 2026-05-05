package data.weapons.scripts.proj;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.AjimusUtils;

public class HWICounterEffect implements ProximityExplosionEffect, OnFireEffectPlugin {

    public static final Color FRINGE_COLOR = new Color(0, 255, 0, 255);
    public static final Color CORE_COLOR = new Color(255, 255, 255, 255);
    // public static final float WIDTH = 12f;// 电弧宽度
    // private boolean pierceShield = true;// 电弧是否穿盾
    // private static final DamageType TYPE = DamageType.ENERGY;// 电弧伤害类型
    // private CombatEngineAPI engine = Global.getCombatEngine();

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        CombatEntityAPI t = AjimusUtils.PickAutoFireTarget(weapon);
        if(t == null) return;
        float hp = t.getHitpoints();
        if (t instanceof ShipAPI) {
            hp = ((ShipAPI) t).getArmorGrid().getArmorRating() * 4f + hp;
        }
        if (hp >= 600) {

        } else if (hp >= 300) {
            projectile.getDamage().getModifier().modifyMult("HWI_CounterEffect", 1f/2f);
            duplicate(projectile, weapon, engine, 2);
        }else if(hp>=150){
            projectile.getDamage().getModifier().modifyMult("HWI_CounterEffect", 1f/4f);
            for(int i = 0;i<3;i++){
                duplicate(projectile, weapon, engine, 4);
            }
        }else{
            projectile.getDamage().getModifier().modifyMult("HWI_CounterEffect", 1f/12f);
            for(int i = 0;i<11;i++){
                duplicate(projectile, weapon, engine, 12);
            }
        }
    }

    protected void duplicate(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine,float total) {
        DamagingProjectileAPI proj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(), weapon,
                weapon.getId(), new Vector2f(weapon.getLocation()),
                (float) (Math.random() - 0.5) * 10f + weapon.getCurrAngle(), null);
        proj.getVelocity().scale(0.7f + (float) (0.5f * Math.random()));
        proj.getDamage().getModifier().modifyMult("HWI_CounterEffect", 1f/total);
    }

    @Override
    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        Iterator<Object> c = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(
                originalProjectile.getLocation(), 200f,
                200f);
        while (c.hasNext()) {
            Object o = c.next();
            if (o instanceof CombatEntityAPI) {
                CombatEntityAPI entity = (CombatEntityAPI) o;
                if (entity.getOwner() != explosion.getOwner()
                        && (Misc.getDistance(entity.getLocation(), originalProjectile.getLocation())) < 120f * 1.1f
                                + entity.getCollisionRadius()) {
                    if (entity instanceof DamagingProjectileAPI && !(entity instanceof MissileAPI))
                        continue;
                    if (entity instanceof ShipAPI && !((ShipAPI) entity).isFighter())
                        continue;
                    float gravForce = 600f / Misc.getDistance(entity.getLocation(), originalProjectile.getLocation());
                    float mass = entity.getMass();
                    if (mass == 0)
                        mass = 1;
                    gravForce /= mass;
                    Vector2f diff = Vector2f.sub(explosion.getLocation(), entity.getLocation(), null);
                    float angle = VectorUtils.getFacing(diff);
                    float angleDiff = angle - entity.getFacing();
                    entity.setAngularVelocity(angleDiff / 5);
                    diff.scale(gravForce / diff.length());
                    Vector2f vel = entity.getVelocity();
                    entity.getVelocity().set(vel.x - diff.x, vel.y - diff.y);
                }
            }
        }
    }
}
