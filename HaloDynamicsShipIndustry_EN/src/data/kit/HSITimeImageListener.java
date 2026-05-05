package data.kit;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AmmoTrackerAPI;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;

public class HSITimeImageListener implements AdvanceableListener {
    private static final float GO_BACK = 3f;
    private List<HSITimeImage> timeflow = new ArrayList<HSITimeImage>();
    private IntervalUtil timer = new IntervalUtil(GO_BACK, GO_BACK);
    private boolean filled = false;
    private ShipAPI ship = null;
    private boolean backing = false;

    public HSITimeImageListener(ShipAPI ship) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused())
            return;
        if (ship == null)
            return;
        if (!ship.isAlive())
            ship.removeListener(this);
        if (backing) {
            goback();
            ship.setJitter(ship, new Color(50, 110, 200, 100), 0.5f, timeflow.size(), 15, 25);
            return;
        } else if (!filled) {
            timer.advance(amount);
            timeflow.add(new HSITimeImage(ship));
            if (timer.intervalElapsed()) {
                timeflow.remove(0);
                filled = true;
            }
        } else if (filled) {
            timeflow.add(new HSITimeImage(ship));
            timeflow.remove(0);
        }
    }

    public void goback() {
        backing = true;
        if (timeflow.size() > 1) {
            timeflow.remove(timeflow.size() - 1);
            setShipAtLastMoment();
            timeflow.remove(timeflow.size()-1);
        } else {
            backing = false;
            filled = false;
            ship.syncWeaponDecalsWithArmorDamage();
            ship.clearDamageDecals();
            ship.syncWithArmorGridState();
        }
    }

    private void setShipAtLastMoment() {
        int n = timeflow.size() - 1;
        int p = 1;
        if (n < 0)
            return;
        if (n != 0) {
            HSITimeImage i = timeflow.get(n);
            ship.getLocation().set(i.getLocation());
            ship.setFacing(i.getFacing());
            ship.setHitpoints(i.getHitpoints());
            ship.getFluxTracker().setCurrFlux(i.getsoftFlux());
            ship.getFluxTracker().setHardFlux(i.gethardFlux());
            int cx = 0;
            ArmorGridAPI aromr = ship.getArmorGrid();
            for (float[] y : i.getaromr()) {
                int cy = 0;
                for (float a : y) {
                    aromr.setArmorValue(cx, cy, a);
                }
            }
            Map<WeaponAPI,Integer> AmmoMap = i.getAmmoMap();
            Map<WeaponAPI,Float> CoolDownMap = i.getcooldownMap();
            for(WeaponAPI weapon:ship.getAllWeapons()){
                if(AmmoMap.containsKey(weapon)){
                    weapon.getAmmoTracker().setAmmo(AmmoMap.get(weapon));
                }
                weapon.setRemainingCooldownTo(CoolDownMap.get(weapon));
            }
        }else if(n == 0){
            HSITimeImage i = timeflow.get(n);
            ship.getLocation().set(i.getLocation());
            ship.setFacing(i.getFacing());
            ship.setHitpoints(i.getHitpoints());
            ship.getFluxTracker().setCurrFlux(i.getsoftFlux());
            ship.getFluxTracker().setHardFlux(i.gethardFlux());
            int cx = 0;
            ArmorGridAPI aromr = ship.getArmorGrid();
            for (float[] y : i.getaromr()) {
                int cy = 0;
                for (float a : y) {
                    aromr.setArmorValue(cx, cy, a);
                }
            }
            ship.getVelocity().set(i.getVelocity());
            Map<WeaponAPI,Integer> AmmoMap = i.getAmmoMap();
            Map<WeaponAPI,Float> CoolDownMap = i.getcooldownMap();
            for(WeaponAPI weapon:ship.getAllWeapons()){
                if(AmmoMap.containsKey(weapon)){
                    weapon.getAmmoTracker().setAmmo(AmmoMap.get(weapon));
                }
                weapon.setRemainingCooldownTo(CoolDownMap.get(weapon));
            }
        }
    }

    public HSITimeImage getExactStart(){
        if(timeflow.size()>0)
        return timeflow.get(0);
        return null;
    }

    public int getSize(){
        return timeflow.size();
    }
}
