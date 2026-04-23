package data.scripts.campaign.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import org.apache.log4j.Logger;

public class UW_EventManager extends BaseCampaignEventListener implements EveryFrameScript {

    public static final float EXTORT_MURDEROUS_PENALTY_KNOWN_MULT = 4;
    public static final float EXTORT_MURDEROUS_PENALTY_TIME = 20f;

    public static Logger log = Global.getLogger(UW_EventManager.class);

    public UW_EventManager() {
        super(true);
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public boolean isDone() {
        return false;
    }

    // if we rob people and then kill them anyway, others will refuse to give in to our extortion for a while
    // put this here because CBA to make a new script
    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        if (!result.getBattle().isPlayerInvolvedAtStart()) {
            return;
        }
        if (result.getBattle().getNonPlayerSide().isEmpty()) {
            return;
        }

        CampaignFleetAPI otherMain = result.getBattle().getNonPlayerSide().get(0);
        if (otherMain.getMemoryWithoutUpdate().getBoolean("$uw_extorted")) {
            float time = EXTORT_MURDEROUS_PENALTY_TIME;
            if (otherMain.knowsWhoPlayerIs()) {
                time *= EXTORT_MURDEROUS_PENALTY_KNOWN_MULT;
            }
            Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("$uw_extortMurderous", true, time);
        }
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
