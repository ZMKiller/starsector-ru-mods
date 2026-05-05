package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import data.kit.HSII18nUtil;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;
import java.util.Iterator;

public class HSISpaceStorm extends BaseShipSystemScript {
    private RippleDistortion in = null;
    private RippleDistortion out = null;

    private static final float STRENGTH = 1000f;

    public static Object KEY_SHIP = new Object();

    private ShipAPI ship = null;

    private boolean once = true;

    //private boolean once = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getHullDamageTakenMult().modifyMult(id,0.05f);
        stats.getArmorDamageTakenMult().modifyMult(id,0.05f);
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        //ship.fadeToColor(KEY_SHIP, new Color(75,75,75,255), 0.1f, 0.1f, effectLevel);
        //ship.setWeaponGlow(effectLevel, new Color(100,165,255,255), EnumSet.of(WeaponAPI.WeaponType.BALLISTIC, WeaponAPI.WeaponType.ENERGY, WeaponAPI.WeaponType.MISSILE));
        //ship.getEngineController().fadeToOtherColor(KEY_SHIP, new Color(0,0,0,0), new Color(0,0,0,0), effectLevel, 0.75f * effectLevel);
        ship.setJitterUnder(KEY_SHIP, new Color(100,165,255,255), effectLevel, 15, 0f, 15f);
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(ship == null) return;
        float strength = STRENGTH*ship.getHullSize().ordinal();
        if(state.equals(State.IN)){
            if (in == null) {
                in = new RippleDistortion(ship.getLocation(),new Vector2f());
                in.setIntensity(4f);
                in.setSize(700f);
                in.fadeOutSize(1.5f);
                DistortionShader.addDistortion(in);
            } else{
                in.setLocation(ship.getLocation());
            }
            Iterator<Object> sg = Global.getCombatEngine().getShipGrid().getCheckIterator(ship.getLocation(),2000f,2000f);
            while (sg.hasNext()){
                Object o = sg.next();
                if(o instanceof ShipAPI){
                    if(((ShipAPI) o).getOwner()==ship.getOwner()){
                        continue;
                    }
                    if(MathUtils.getDistanceSquared(((ShipAPI) o).getLocation(),ship.getLocation())>1000000){
                        continue;
                    }

                    applyForce(strength*ship.getHullSize().ordinal(),true,(ShipAPI)o,ship);
                }
            }
            Iterator<Object> ag = Global.getCombatEngine().getAsteroidGrid().getCheckIterator(ship.getLocation(),2000f,2000f);
            while (ag.hasNext()){
                Object o = ag.next();
                if(o instanceof CombatEntityAPI){
                    if(((CombatEntityAPI) o).getOwner()==ship.getOwner()){
                        continue;
                    }
                    if(MathUtils.getDistanceSquared(((CombatEntityAPI) o).getLocation(),ship.getLocation())>1000000){
                        continue;
                    }

                    applyForce(strength*ship.getHullSize().ordinal()/3f,true,(CombatEntityAPI)o,ship);
                }
            }
            Iterator<Object> mg = Global.getCombatEngine().getMissileGrid().getCheckIterator(ship.getLocation(),2000f,2000f);
            while (mg.hasNext()){
                Object o = mg.next();
                if(o instanceof CombatEntityAPI){
                    if(((CombatEntityAPI) o).getOwner()==ship.getOwner()){
                        continue;
                    }
                    if(MathUtils.getDistanceSquared(((CombatEntityAPI) o).getLocation(),ship.getLocation())>1000000){
                        continue;
                    }

                    applyForce(strength*ship.getHullSize().ordinal()/5f,true,(CombatEntityAPI)o,ship);
                }
            }
        }else if(state.equals(State.ACTIVE)){
            if(in!=null)
                Global.getCombatEngine().spawnDamagingExplosion(createExplosionSpec(300f,stats.getSystemRangeBonus().computeEffective(200f)*ship.getHullSize().ordinal(),ship),ship,ship.getLocation());
            in = null;
        }else if(state.equals(State.OUT)){
            if (out == null) {
                out = new RippleDistortion(ship.getLocation(), new Vector2f());
                out.setIntensity(6f);
                out.setSize(stats.getSystemRangeBonus().computeEffective(1000f));
                out.fadeOutSize(2f);
                out.fadeOutIntensity(2f);
                DistortionShader.addDistortion(out);
            } else{
                out.setLocation(ship.getLocation());
            }
            Iterator<Object> sg = Global.getCombatEngine().getShipGrid().getCheckIterator(ship.getLocation(),2000f,2000f);
            while (sg.hasNext()){
                Object o = sg.next();
                if(o instanceof ShipAPI){
                    if(((ShipAPI) o).getOwner()==ship.getOwner()){
                        continue;
                    }
                    if(MathUtils.getDistanceSquared(((ShipAPI) o).getLocation(),ship.getLocation())>1000000){
                        continue;
                    }
                    if(once){
                        ((ShipAPI) o).getVelocity().set(0,0);
                    }
                    applyForce(strength*ship.getHullSize().ordinal()*3f,false,(ShipAPI)o,ship);
                }
            }
            Iterator<Object> ag = Global.getCombatEngine().getAsteroidGrid().getCheckIterator(ship.getLocation(),2000f,2000f);
            while (ag.hasNext()){
                Object o = ag.next();
                if(o instanceof CombatEntityAPI){
                    if(((CombatEntityAPI) o).getOwner()==ship.getOwner()){
                        continue;
                    }
                    if(MathUtils.getDistanceSquared(((CombatEntityAPI) o).getLocation(),ship.getLocation())>1000000){
                        continue;
                    }
                    if(once){
                        ((CombatEntityAPI)o).getVelocity().set(0,0);
                    }
                    applyForce(strength*ship.getHullSize().ordinal()*3f,false,(CombatEntityAPI)o,ship);
                }
            }
            Iterator<Object> mg = Global.getCombatEngine().getMissileGrid().getCheckIterator(ship.getLocation(),2000f,2000f);
            while (mg.hasNext()){
                Object o = mg.next();
                if(o instanceof CombatEntityAPI){
                    if(((CombatEntityAPI) o).getOwner()==ship.getOwner()){
                        continue;
                    }
                    if(MathUtils.getDistanceSquared(((CombatEntityAPI) o).getLocation(),ship.getLocation())>1000000){
                        continue;
                    }
                    if(once){
                        ((CombatEntityAPI) o).getVelocity().set(0,0);
                    }
                    applyForce(strength*ship.getHullSize().ordinal()*3f,false,(CombatEntityAPI)o,ship);
                }
            }
            if(once) once = false;

        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        once = true;
        in = null;
        out = null;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(HSII18nUtil.getShipSystemString("HSISpaceWind"), false);
        }
        return null;
    }

    public static  DamagingExplosionSpec createExplosionSpec(float damage, float radius,ShipAPI ship) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.15f, // duration
                radius*1.1f, // radius
                radius*0.75f, // coreRadius
                damage, // maxDamage
                damage*0.8f, // minDamage
                CollisionClass.PROJECTILE_NO_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                0f, // particleSizeMin
                0f, // particleSizeRange
                0f, // particleDuration
                0, // particleCount
                new Color(0,0,0,0), // particleColor
                new Color(0,0,0,0) // explosionColor
        );
        spec.setDamageType(DamageType.ENERGY);
        //spec.setUseDetailedExplosion(true);
        //spec.setSoundSetId("explosion_guardian");
        return spec;
    }

    public static void applyForce(float strength, boolean pull, CombatEntityAPI target,ShipAPI self){
        float force = strength/(target.getMass()<=0?10f:target.getMass());
        Vector2f velChange;
        if(pull){
            velChange = (Vector2f) Misc.getUnitVector(target.getLocation(),self.getLocation()).scale(force*Global.getCombatEngine().getElapsedInLastFrame());
        }else{
            velChange = (Vector2f) Misc.getUnitVector(self.getLocation(),target.getLocation()).scale(force*Global.getCombatEngine().getElapsedInLastFrame());
        }
        target.getVelocity().set(Vector2f.add(target.getVelocity(),velChange,null));
    }
}
