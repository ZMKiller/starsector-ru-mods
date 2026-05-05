package data.campaign.ability.HSIDarkTide;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class HSIDarkTideBombingIntel extends BaseIntelPlugin{
    public static class HSIDarkTideStrikeData{
        private FleetMemberAPI bomber;
        private SectorEntityToken target;
        private FaderUtil engagementTimer;
        private float backDist;
        private float speed;

        public HSIDarkTideStrikeData(FleetMemberAPI bomber,SectorEntityToken target,float engageTime){
            this.bomber = bomber;
            this.target = target;
            this.engagementTimer = new FaderUtil(0f, engageTime);
            engagementTimer.fadeIn();
            this.speed = Misc.getLYPerDayAtSpeed(Global.getSector().getPlayerFleet(), bomber.getStats().getMaxBurnLevel().getModifiedValue());
        }

        public void advance(float amount){
            if(engagementTimer.isFadedIn()){//arrived
                float days = Global.getSector().getClock().convertToDays(amount);
                
            }else{

            }
        }
    }
    @Override
    protected void advanceImpl(float amount) {
        
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean isEnded() {
        return false;
    }
}
