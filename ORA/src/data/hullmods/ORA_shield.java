package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;

public class ORA_shield extends BaseHullMod {
    
    private final Map<ShipAPI.HullSize, Float> DEVIATION = new HashMap<>();
    {
        DEVIATION.put(HullSize.DEFAULT, 20f);
        DEVIATION.put(HullSize.FRIGATE, 26f);
        DEVIATION.put(HullSize.DESTROYER, 32f);
        DEVIATION.put(HullSize.CRUISER, 42f);
        DEVIATION.put(HullSize.CAPITAL_SHIP, 72f);
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        
        //skip when not needed
        if(ship.getShield()==null || ship.getShield().isOff() || ship.getShield().getType()==ShieldType.FRONT){
            return;
        }
        
        //fake mutable stat to "store" the default shield arc value since hullmod scripts are not instanciated
        if(ship.getMutableStats().getShieldArcBonus().getFlatBonus("ORA_shield")==null){
            ship.getMutableStats().getShieldArcBonus().modifyFlat("ORA_shield",ship.getShield().getArc()/100);
        }
        
        //calculate the amount of deformation to add
        float offset = MathUtils.getShortestRotation(ship.getShield().getFacing(), ship.getFacing());        
        float side = Math.copySign(1,offset);
        offset = Math.abs(offset);
        offset = MathUtils.FPI * offset/90;                
        float radius = (1-(float)FastTrig.cos(offset))/4;        
        offset = (float)FastTrig.sin(offset/2);
        
        //apply deformation
        ship.getShield().setCenter(ship.getHullSpec().getShieldSpec().getCenterX(), offset*side*DEVIATION.get(ship.getHullSize()));
        ship.getShield().setRadius(ship.getCollisionRadius()+radius*DEVIATION.get(ship.getHullSize()));
        ship.getShield().setArc(ship.getMutableStats().getShieldArcBonus().getFlatBonus("ORA_shield").getValue()*100+radius*120);
    }
}
