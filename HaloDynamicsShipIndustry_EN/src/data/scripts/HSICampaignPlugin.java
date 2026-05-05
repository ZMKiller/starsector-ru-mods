package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import data.campaign.HSIStellaArena.HSISAAdvBCP;
import data.campaign.HSITTMission.HSITTMissionCombatCreator;
import data.econ.HSIGF.HSIGuardFleetInteractionDialog;

public class HSICampaignPlugin extends BaseCampaignPlugin {


    @Override
    public String getId() {
        return "HSICampaignPlugin";
    }

    @Override
    public PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(SectorEntityToken opponent) {
        //Global.getLogger(this.getClass()).info("BCPCreator:"+(opponent instanceof CampaignFleetAPI)+"||" +opponent.getTags()+"||"+opponent.getName());
        if (opponent.getName().startsWith("HSISA-A-")) {
            return new PluginPick<BattleCreationPlugin>(new HSISAAdvBCP(), CampaignPlugin.PickPriority.MOD_SPECIFIC);
        }
        if (opponent.getMemoryWithoutUpdate().contains("$HSI_TTMISSION")) {
            return new PluginPick<BattleCreationPlugin>(new HSITTMissionCombatCreator(), CampaignPlugin.PickPriority.MOD_SPECIFIC);
        }
        return null;
    }

    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if (interactionTarget instanceof CampaignFleetAPI&&interactionTarget.getMemoryWithoutUpdate().contains("$HSI_GuardFleet")) {
            return new PluginPick<InteractionDialogPlugin>(new HSIGuardFleetInteractionDialog(), PickPriority.MOD_SPECIFIC);
        }
        return null;
    }
}
