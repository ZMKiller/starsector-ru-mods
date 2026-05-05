package data.kit;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.AmmoTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSITimeImage {
    private Vector2f velocity;
    private Vector2f location;
    private float facing;
    private float hitpoints;
    private float softFlux;
    private float hardFlux;
    private float[][] aromr;
    private Map<WeaponAPI,Integer> ammoMap = new HashMap<WeaponAPI,Integer>();
    private Map<WeaponAPI,Float> cooldownMap = new HashMap<WeaponAPI,Float>();

    public HSITimeImage(ShipAPI ship){
        velocity = new Vector2f(ship.getVelocity().getX(), ship.getVelocity().getY());
        location = new Vector2f(ship.getLocation().getX(), ship.getLocation().getY());
        facing = ship.getFacing();
        hitpoints = ship.getHitpoints();
        softFlux = ship.getFluxTracker().getCurrFlux();
        hardFlux = ship.getFluxTracker().getHardFlux();
        aromr = ship.getArmorGrid().getGrid();
        for(WeaponAPI wpn:ship.getAllWeapons()){
            if(wpn.usesAmmo()){
                ammoMap.put(wpn, wpn.getAmmo());
            }
            cooldownMap.put(wpn, wpn.getCooldownRemaining());
        }
    }
    public Map<WeaponAPI,Integer> getAmmoMap(){
        return ammoMap;
    }

    public Map<WeaponAPI,Float> getcooldownMap(){
        return cooldownMap;
    }


    public Vector2f getLocation(){
        return location;
    }

    public Vector2f getVelocity(){
        return velocity;
    }

    public float getHitpoints(){
        return hitpoints;
    }

    public float getsoftFlux(){
        return softFlux;
    }

    public float gethardFlux(){
        return hardFlux;
    }

    public float[][] getaromr(){
        return aromr;
    }

    public float getFacing(){
        return facing;
    }
}
