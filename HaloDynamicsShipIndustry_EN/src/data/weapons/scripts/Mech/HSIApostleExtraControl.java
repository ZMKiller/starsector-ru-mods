package data.weapons.scripts.Mech;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;

import java.awt.event.MouseEvent;
import java.util.List;

public class HSIApostleExtraControl extends BaseEveryFrameCombatPlugin {
    public static String KEY_BLADE_DEFENSE = "HSI_Blade_Defense";
    private final ShipAPI Apostle;
    public HSIApostleExtraControl(ShipAPI Apostle){
        this.Apostle = Apostle;
    }


    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if(Global.getCombatEngine().isPaused()) return;
        if(Apostle.getFluxTracker().isOverloadedOrVenting()){
            Apostle.setCustomData(KEY_BLADE_DEFENSE,false);
            return;
        }
        for(InputEventAPI event:events){
            if(event.isConsumed()) continue;
            if(event.getEventType().equals(InputEventType.MOUSE_DOWN)&&event.getEventValue()==MouseEvent.BUTTON2){
                Apostle.setCustomData(KEY_BLADE_DEFENSE,true);
            }else if(event.getEventType().equals(InputEventType.MOUSE_UP)&&event.getEventValue()==MouseEvent.BUTTON2){
                Apostle.setCustomData(KEY_BLADE_DEFENSE,false);
            }
        }
    }
}
