package data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.campaign.econ.jydr_Items;


public class JYDRPlugin extends BaseModPlugin {
    public static float SENSOR_PROFILE_INCREASE_PERCENT = 300f;

    @Override
    public void afterGameSave() {
        Global.getSector().getCharacterData().addAbility("platinum_forge");
        Global.getSector().getCharacterData().addAbility("pluto_forge");
		Global.getSector().getCharacterData().addAbility("bio_forge");	
		Global.getSector().getCharacterData().addAbility("compv_forge");	
		Global.getSector().getCharacterData().addAbility("food_forge");	
		Global.getSector().getCharacterData().addAbility("fuel_forge");			
    }

    @Override
    public void beforeGameSave() {
        Global.getSector().getCharacterData().removeAbility("platinum_forge");
        Global.getSector().getCharacterData().removeAbility("pluto_forge");
	    Global.getSector().getCharacterData().removeAbility("bio_forge");
	    Global.getSector().getCharacterData().removeAbility("compv_forge");	
		Global.getSector().getCharacterData().removeAbility("food_forge");	
		Global.getSector().getCharacterData().removeAbility("fuel_forge");			
    }

    @Override
    public void onGameLoad(boolean newGame) {
        try {
            if(!Global.getSector().getPlayerFleet().hasAbility("platinum_forge")) {
                Global.getSector().getCharacterData().addAbility("platinum_forge");
			}	
			if(!Global.getSector().getPlayerFleet().hasAbility("pluto_forge")) {
                Global.getSector().getCharacterData().addAbility("pluto_forge");				
            }
		if(!Global.getSector().getPlayerFleet().hasAbility("bio_forge")) {
                Global.getSector().getCharacterData().addAbility("bio_forge");				
            }	
		if(!Global.getSector().getPlayerFleet().hasAbility("compv_forge")) {
                Global.getSector().getCharacterData().addAbility("compv_forge");				
            }	
		if(!Global.getSector().getPlayerFleet().hasAbility("food_forge")) {
                Global.getSector().getCharacterData().addAbility("food_forge");				
            }				
		if(!Global.getSector().getPlayerFleet().hasAbility("fuel_forge")) {
                Global.getSector().getCharacterData().addAbility("fuel_forge");				
            }						
        SENSOR_PROFILE_INCREASE_PERCENT = (float) Global.getSettings().getFloat("MineralForgingSensorProfileIncreasePercent");
        } catch (Exception e) {
            String stackTrace = "";

            for(int i = 0; i < e.getStackTrace().length; i++) {
                StackTraceElement ste = e.getStackTrace()[i];
                stackTrace += "    " + ste.toString() + System.lineSeparator();
            }

            Global.getLogger(JYDRPlugin.class).error(e.getMessage() + System.lineSeparator() + stackTrace);
        }
    }
}
