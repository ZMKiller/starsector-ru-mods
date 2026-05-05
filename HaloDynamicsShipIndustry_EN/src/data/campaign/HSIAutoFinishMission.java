package data.campaign;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;

public class HSIAutoFinishMission extends BaseHubMission{
    public static enum Stage {
		COMPLETED,
	}
    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
       setSuccessStage(Stage.COMPLETED);
       setStageOnMemoryFlag(Stage.COMPLETED, createdAt, missionId+"_completed");
       currentStage = Stage.COMPLETED;
       return true;
    }
    
}
