package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.util.HashMap;
import java.util.Map;
import static data.scripts.util.ORA_txt.txt;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

@SuppressWarnings("unchecked")
public class ORA_design extends BaseHullMod {
    
    @Override
    public int getDisplaySortOrder() {
        return 0;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 0;
    }
    
    private final float SHIELD_ROTATION = 50;
    
    private final static Map<WeaponAPI.WeaponSize, Float> DEBUFF = new HashMap<>();
    static {
        DEBUFF.put(WeaponAPI.WeaponSize.LARGE,0.2f);
        DEBUFF.put(WeaponAPI.WeaponSize.MEDIUM,0.1f);
        DEBUFF.put(WeaponAPI.WeaponSize.SMALL,0.05f);
    }
    
    //BEAM OP COST SHENANIGANS
    private final Integer BEAM_COST=3, PD_REFUND =-2;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.LARGE_BEAM_MOD).modifyFlat(id, BEAM_COST);
        stats.getDynamic().getMod(Stats.MEDIUM_BEAM_MOD).modifyFlat(id, BEAM_COST);
        stats.getDynamic().getMod(Stats.SMALL_BEAM_MOD).modifyFlat(id, BEAM_COST);
        
        stats.getDynamic().getMod(Stats.LARGE_PD_MOD).modifyFlat(id, PD_REFUND);
        stats.getDynamic().getMod(Stats.MEDIUM_PD_MOD).modifyFlat(id, PD_REFUND);
        stats.getDynamic().getMod(Stats.SMALL_PD_MOD).modifyFlat(id, PD_REFUND);
                
        stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_ROTATION);
    }
    
    @Override
    public boolean affectsOPCosts() {
        return true;
    }
    
    private final String ID="Xeno Warfare";
    private final Integer MAX_ASYMETRY=50;
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){          
        float asymetry=computeAsymetry(ship);
        if (asymetry!=0){
            ship.getMutableStats().getMaxSpeed().modifyMult(ID, 1-asymetry);
            ship.getMutableStats().getAcceleration().modifyMult(ID, 1-asymetry);
            ship.getMutableStats().getTurnAcceleration().modifyMult(ID, 1-asymetry);
            ship.getMutableStats().getMaxTurnRate().modifyMult(ID, 1-asymetry);            
        }
    }
    
    private float computeAsymetry (ShipAPI ship){
        float asymetry=0;
        
        for(WeaponAPI w : ship.getAllWeapons()){
            if(w.getSlot().isTurret() || w.getSlot().isHardpoint()){
                if(Math.round(MathUtils.getShortestRotation(0 , w.getSlot().getAngle())) > 1 && Math.round(MathUtils.getShortestRotation(0 , w.getSlot().getAngle())) < 179){
                    asymetry+=DEBUFF.get(w.getSize());
                } else if (Math.round(MathUtils.getShortestRotation(0 , w.getSlot().getAngle())) < -1 && Math.round(MathUtils.getShortestRotation(0 , w.getSlot().getAngle())) > -179){                    
                    asymetry-=DEBUFF.get(w.getSize());
                }
            }
        }
        
        Math.min(MAX_ASYMETRY/100, 1-Math.abs(asymetry));
        return asymetry;
    }
    
    private final Map <WeaponSize, Integer> weight = new HashMap<>();
    {
        weight.put(WeaponSize.SMALL, 1);
        weight.put(WeaponSize.MEDIUM, 2);
        weight.put(WeaponSize.LARGE, 4);
    }
    
    private final String DECS0=txt("%");
    private final String DECS1=txt("op");
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {        
        if (index == 0) {
            return MAX_ASYMETRY+DECS0;
        }
        if (index == 1) {
            return BEAM_COST+" "+DECS1;
        }
        if (index == 2) {
            return PD_REFUND+" "+DECS1;
        }  
        return null;
    }

    private final Color HL=Global.getSettings().getColor("hColor");
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        
        Integer asymetry = Math.round(100*computeAsymetry(ship));
        
        if(asymetry==0){            
            //title
            tooltip.addSectionHeading(txt("hm_slow0"), Alignment.MID, 15);  
        } else {
            
            //title
            tooltip.addSectionHeading(txt("hm_slow1"), Alignment.MID, 15);  
            tooltip.addPara(
                    txt("hm_slow2")
                    + asymetry               
                    +txt("hm_slow3")
                    ,10
                    ,HL
                    ,asymetry+""
            );
        }
    }
    
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a ORA hull id
        return ( ship.getHullSpec().getHullId().startsWith("ora_"));	
    }
}
