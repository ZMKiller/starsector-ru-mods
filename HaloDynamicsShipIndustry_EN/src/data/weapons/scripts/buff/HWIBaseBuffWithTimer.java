package data.weapons.scripts.buff;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HWIBaseBuffWithTimer implements AdvanceableListener{
    protected TimeoutTracker<Object> providers = new TimeoutTracker<Object>();
    protected ShipAPI ship;
    private int skipFrames = 15;

    public HWIBaseBuffWithTimer(ShipAPI ship){
        this.ship = ship;
    }

    @Override
    public void advance(float amount) {
        providers.advance(amount);
        if(providers.getItems().isEmpty()&&skipFrames<=0){
            ship.removeListener(this);
            clear();
            return;
        }
        if(skipFrames>0){
            skipFrames--;
        }
    }

    public void add(Object provider,float time){
        providers.add(provider, time);
    }

    public static HWIBaseBuffWithTimer getInstance(ShipAPI target){
        return null;
    }

    public void clear(){
        
    }
    
}
