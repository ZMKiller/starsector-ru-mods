package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import data.scripts.ai.ORA_invocationAI;
import data.scripts.ai.ORA_callingAI;
import data.scripts.ai.ORA_echoesAI;
import data.scripts.ai.ORA_prayerAI;
import data.scripts.world.ORA_gen;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class ORA_modPlugin extends BaseModPlugin {
    
    public static final String ECHOES_ID = "ora_echoesS";  
    public static final String INVOCATION_ID = "ora_invocationS";  
    public static final String CALLING_ID = "ora_callingS";  
    public static final String PRAYER_ID = "ora_prayerS"; 
    
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case ECHOES_ID:
                return new PluginPick<MissileAIPlugin>(new ORA_echoesAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case INVOCATION_ID:
                return new PluginPick<MissileAIPlugin>(new ORA_invocationAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case CALLING_ID:
                return new PluginPick<MissileAIPlugin>(new ORA_callingAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case PRAYER_ID:
                return new PluginPick<MissileAIPlugin>(new ORA_prayerAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:        
        }
        return null;
    }
    
    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        
        boolean hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasShaderLib){            
            ShaderLib.init();  
            LightData.readLightDataCSV("data/config/modFiles/ora_lights.csv"); 
            TextureData.readTextureDataCSV("data/config/modFiles/ora_maps.csv"); 
        }        
    }
	
    @Override
    public void onNewGame() {
	boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
	if (!haveNexerelin || SectorManager.getManager().isCorvusMode()){
            new ORA_gen().generate(Global.getSector());
        }
    }
    
    @Override
    public void onNewGameAfterEconomyLoad() {
	//special admins
        MarketAPI market =  Global.getSector().getEconomy().getMarket("joy_satiate");
        if (market != null && market.getAdmin()!=null) {
            PersonAPI person = market.getAdmin();

            if (Global.getSettings().getModManager().isModEnabled("IndEvo")) {
                person.getStats().setSkillLevel("indevo_fleet_logistics", 1);
                person.getStats().setSkillLevel("indevo_planetary_operations", 1);
            } else person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            //person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);
            //person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
        }
        
        market =  Global.getSector().getEconomy().getMarket("ora_poincare");
        if (market != null && market.getAdmin()!=null) {
            PersonAPI person = market.getAdmin();

            if (Global.getSettings().getModManager().isModEnabled("IndEvo")) {
                person.getStats().setSkillLevel("indevo_fleet_logistics", 1);
                person.getStats().setSkillLevel("indevo_planetary_operations", 1);
            } else person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            //person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);
            //person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
        }
        
        market =  Global.getSector().getEconomy().getMarket("ora_pendulum");
        if (market != null && market.getAdmin()!=null) {
            PersonAPI person = market.getAdmin();
            
            person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 0);
            if (Global.getSettings().getModManager().isModEnabled("IndEvo")) {
                person.getStats().setSkillLevel("indevo_planetary_operations", 1);
            } else person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            //person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 0);
            //person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
        }
    }
}