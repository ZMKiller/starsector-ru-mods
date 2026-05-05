package data.weapons.scripts.HWIMaid;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.ArrayList;
import java.util.List;

public class HWIMaidSharedData implements AdvanceableListener {

    private ShipAPI ship;
    private List<WeaponAPI> maidLaunchers = new ArrayList<>();
    private IntervalUtil scan = new IntervalUtil(0.05f,0.1f);

    public HWIMaidSharedData getInstance(ShipAPI ship){
        if(ship.hasListenerOfClass(HWIMaidSharedData.class)){
            return ship.getListeners(HWIMaidSharedData.class).get(0);
        }else{
            HWIMaidSharedData data = new HWIMaidSharedData(ship);
            ship.addListener(data);
            return data;
        }
    }

    public HWIMaidSharedData(ShipAPI ship){
        this.ship = ship;
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.getSpec().getWeaponId().equals("HWI_Maid")){
                maidLaunchers.add(weapon);
            }
        }
    }
    @Override
    public void advance(float amount) {
        scan.advance(amount);
        if(!scan.intervalElapsed()) return;

    }
}
