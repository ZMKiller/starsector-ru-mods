package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicIncompatibleHullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;

public class ASF_PhantasmagoriaHullmod extends BaseHullMod {

	public static final float RESIST = 0.40f;
	public static final float SHIELD_BONUS = 20f;

	public static final float HEALTH_BONUS = 25f;
	public static final float REPAIR_BONUS = 0.3f;
	
	private final Map<Integer,String> B_SELECTOR = new HashMap<>();
    {
        B_SELECTOR.put(0, "A_S-F_phantasmagoria_slicer");
        B_SELECTOR.put(1, "A_S-F_phantasmagoria_cutter");
        B_SELECTOR.put(2, "A_S-F_phantasmagoria_piercer");
    }
    private final Map<String, Integer> SWITCH_TO_B = new HashMap<>();
    {
        SWITCH_TO_B.put("A_S-F_phantasmagoria_slicer",1);
        SWITCH_TO_B.put("A_S-F_phantasmagoria_cutter",2);
        SWITCH_TO_B.put("A_S-F_phantasmagoria_piercer",0);
    }
    
    private final Map<Integer,String> I_SELECTOR = new HashMap<>();
    {
        I_SELECTOR.put(0, "A_S-F_phantasmagoria_micro_missile");
        I_SELECTOR.put(1, "A_S-F_phantasmagoria_missile");
        I_SELECTOR.put(2, "A_S-F_phantasmagoria_missile_stk");
    }
    private final Map<String, Integer> SWITCH_TO_I = new HashMap<>();
    {
        SWITCH_TO_I.put("A_S-F_phantasmagoria_micro_missile",1);
        SWITCH_TO_I.put("A_S-F_phantasmagoria_missile",2);
        SWITCH_TO_I.put("A_S-F_phantasmagoria_missile_stk",0);
    }
    
    private final Map<Integer,String> C_SELECTOR = new HashMap<>();
    {
        C_SELECTOR.put(0, "A_S-F_PhantasmagoriaCore_on");
        C_SELECTOR.put(1, "A_S-F_PhantasmagoriaCore_off");
    }
    private final Map<Integer, String> SWITCH_TO_C = new HashMap<>();
    {
        SWITCH_TO_C.put(0, "A_S-F_PhantasmagoriaRegulator_off");
        SWITCH_TO_C.put(1, "A_S-F_PhantasmagoriaRegulator_on");
    }
    
    private final Map<Integer,String> SWITCH_B = new HashMap<>();
    {
        SWITCH_B.put(0,"A_S-F_PhantasmagoriaSlicer");
        SWITCH_B.put(1,"A_S-F_PhantasmagoriaCutter");
        SWITCH_B.put(2,"A_S-F_PhantasmagoriaPiercer");
    }
    
    private final Map<Integer,String> SWITCH_I = new HashMap<>();
    {
        SWITCH_I.put(0,"A_S-F_PhantasmagoriaShroud");
        SWITCH_I.put(1,"A_S-F_PhantasmagoriaCurtain");
        SWITCH_I.put(2,"A_S-F_PhantasmagoriaPartition");
    }
    
    private final Map<Integer,String> SWITCH_C = new HashMap<>();
    {
        SWITCH_C.put(0,"A_S-F_PhantasmagoriaRegulator_on");
        SWITCH_C.put(1,"A_S-F_PhantasmagoriaRegulator_off");
    }
    private final Map<Integer,String> LOAD_C = new HashMap<>();
    {
        LOAD_C.put(0,"A_S-F_PhantasmagoriaCore_off");
        LOAD_C.put(1,"A_S-F_PhantasmagoriaCore_on");
    }
    
    private final String barrageSlotID = "WS0002";
    private final String illusionSlotID = "WS0004";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
        //trigger a weapon switch if none of the selector hullmods are present (done separately for each set of selectors)
		boolean toSwitchB=true;
        for(int i=0; i<SWITCH_B.size(); i++){
            if(stats.getVariant().getHullMods().contains(SWITCH_B.get(i))){
                toSwitchB=false;
            }
        }
        boolean toSwitchI=true;
        for(int i=0; i<SWITCH_I.size(); i++){
            if(stats.getVariant().getHullMods().contains(SWITCH_I.get(i))){
                toSwitchI=false;
            }
        }
        boolean toSwitchC=true;
        for(int i=0; i<SWITCH_C.size(); i++){
            if(stats.getVariant().getHullMods().contains(SWITCH_C.get(i))){
                toSwitchC=false;
            }
        }
		
        //remove the weapons to change and swap the hullmod for the next mode
        if(toSwitchB){
            //select new weapon
            int selectedB;
            boolean random=false;
            if(stats.getVariant().getWeaponSpec(barrageSlotID)!=null){
                selectedB=SWITCH_TO_B.get(stats.getVariant().getWeaponSpec(barrageSlotID).getWeaponId());
            } else {
                selectedB=MathUtils.getRandomNumberInRange(0, SWITCH_TO_B.size()-1);
                random=true;
            }
            
            //add the proper hullmod
            stats.getVariant().addMod(SWITCH_B.get(selectedB));
            
            //clear the weapon to replace
            stats.getVariant().clearSlot(barrageSlotID);
            //select and place the proper weapon
            String toInstallBarrage=B_SELECTOR.get(selectedB);
            
            stats.getVariant().addWeapon(barrageSlotID, toInstallBarrage);
            
            if(random){
                stats.getVariant().autoGenerateWeaponGroups();
            }
        }
        
        if(toSwitchI){
            //select new weapon
            int selectedI;
            boolean random=false;
            if(stats.getVariant().getWeaponSpec(illusionSlotID)!=null){
                selectedI=SWITCH_TO_I.get(stats.getVariant().getWeaponSpec(illusionSlotID).getWeaponId());
            } else {
                selectedI=MathUtils.getRandomNumberInRange(0, SWITCH_TO_I.size()-1);
                random=true;
            }
            
            //add the proper hullmod
            stats.getVariant().addMod(SWITCH_I.get(selectedI));
            
            //clear the weapon to replace
            stats.getVariant().clearSlot(illusionSlotID);
            //select and place the proper weapon
            String toInstallIllusion=I_SELECTOR.get(selectedI);
            
            stats.getVariant().addWeapon(illusionSlotID, toInstallIllusion);
            
            if(random){
                stats.getVariant().autoGenerateWeaponGroups();
            }
        }
        
        if(toSwitchC){
            //select new hullmods
            int selectedC = 1;
            
            // set number to match the current indicator hullmod
            for(int i=0; i<C_SELECTOR.size(); i++){
                if(stats.getVariant().getHullMods().contains(C_SELECTOR.get(i))){
                	selectedC = i;
                }
            }
            
            // remove the current indicator
            stats.getVariant().removeMod(C_SELECTOR.get(selectedC));
            
            // add the next indicator
            stats.getVariant().addMod(LOAD_C.get(selectedC));
            
            // add the new (real) hullmod
            stats.getVariant().addMod(SWITCH_TO_C.get(selectedC));
            
        }
        
        
		stats.getBeamDamageTakenMult().modifyMult(id, (1f - RESIST));
		stats.getDamageToTargetShieldsMult().modifyPercent(id, SHIELD_BONUS);
		
		stats.getWeaponHealthBonus().modifyPercent(spec.getId(), HEALTH_BONUS);
		stats.getEngineHealthBonus().modifyPercent(spec.getId(), HEALTH_BONUS);
		stats.getCombatEngineRepairTimeMult().modifyMult(spec.getId(), 1f - REPAIR_BONUS);
		stats.getCombatWeaponRepairTimeMult().modifyMult(spec.getId(), 1f - REPAIR_BONUS);
	}

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
    	//only check for undo in refit to avoid issues
        if(ship.getOriginalOwner()<0){
            //undo fix for barrages / illusions put in cargo
            if(
                    Global.getSector()!=null && 
                    Global.getSector().getPlayerFleet()!=null && 
                    Global.getSector().getPlayerFleet().getCargo()!=null && 
                    Global.getSector().getPlayerFleet().getCargo().getStacksCopy()!=null &&
                    !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()
                    ){
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()){
                    if(
                            s.isWeaponStack() && (
                                B_SELECTOR.containsValue(s.getWeaponSpecIfWeapon().getWeaponId()) || 
                                I_SELECTOR.containsValue(s.getWeaponSpecIfWeapon().getWeaponId())
                                ) 
                            ){
                        Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                    }
                }
            }
        }
        
        MutableShipStatsAPI stats = ship.getMutableStats();
		if(stats.getVariant().getHullMods().contains("safetyoverrides")){
			//if someone tries to install SO, remove it
			MagicIncompatibleHullmods.removeHullmodWithWarning(
					stats.getVariant(),
					"safetyoverrides",
					"A_S-F_PhantasmagoriaHullmod"
					);	
		}
    }
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		if ( !ship.isAlive() || ship.isPiece() ) {
            return;
        }
		
		// Stat setup section
		float FLUX_USAGE = ship.getFluxLevel();
		// Stat setup section
		
		// Jitter section
		float ALPHA_1 = (FLUX_USAGE * 5) + 15f; // 20 + 10
		float ALPHA_2 = (FLUX_USAGE * 10) + 20f; // 25 + 15
		Color JITTER_COLOR = new Color(165,90,255,(int)ALPHA_1);
		Color JITTER_UNDER_COLOR = new Color(165,90,255,(int)ALPHA_2);
		
		float jitterRangeBonus_1 = FLUX_USAGE * 3f;
		float jitterRangeBonus_2 = FLUX_USAGE * 9f;
		
		float jitterLevel = ( (float) Math.sqrt(FLUX_USAGE) * 0.35f ) + 0.75f;
		
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 15f + jitterRangeBonus_1);
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 15, 0f, 30f + jitterRangeBonus_2);
		// Jitter section
		
	}
		// So what this hullmod does is as follows:
		// - Applies a large jitter effect that gets stronger as flux levels go up. (a helpful visualiser that flux is high, and looks kinda neat)
		// - Reduces beam damage taken by 40% (because no shields/phase means beams are scary!)
		// - Increases damage dealt to shields by 20%
		// - Increases weapon and engine health by 20%
		// - Reduces weapon and engine repair time by 30%
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;

		float RES_DESC = RESIST * 100f;
		float REPAIR_DESC = REPAIR_BONUS * 100f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("This is a unique experimental vessel, built around an advanced Temporal Core.", opad);
		
		label = tooltip.addPara("The temporal core even when not active, passively produces a dimensional distortion which reduces damage taken from %s by %s, And increases damage dealt to Shields by %s.", opad, h, "Beam Weapons", "" + (int) Math.round(RES_DESC) + "%", "" + (int)SHIELD_BONUS + "%");
		label.setHighlight("Beam Weapons", "" + (int) Math.round(RES_DESC) + "%", "" + (int)SHIELD_BONUS + "%");
		label.setHighlightColors(h, h, h);
		
		label = tooltip.addPara("An advanced set of reinforcements and repair systems have been installed to allow systems to remain combat-operable despite the lack of a shield emitter.", opad);
		label = tooltip.addPara("Weapon and Engine health increased by %s.", pad, h, "" + (int)HEALTH_BONUS + "%");
		label.setHighlight("" + (int)HEALTH_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Weapon and Engine repair times reduced by %s.", pad, h, "" + Math.round(REPAIR_DESC) + "%");
		label.setHighlight("" + Math.round(REPAIR_DESC) + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("%s cannot be installed in conjunction with the Temporal Core.", opad, bad, "Safety Overrides");
		label.setHighlight("Safety Overrides");
		label.setHighlightColors(bad);
		
	}
	
}