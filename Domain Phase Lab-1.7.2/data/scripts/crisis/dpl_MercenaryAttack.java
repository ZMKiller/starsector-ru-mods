package data.scripts.crisis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_MercenaryAttack extends GenericRaidFGI {
    public static final String DPL_MERC_FLEET = "$dpl_Mercenary_fleet";
    public static String KEY = "$dpl_Mercenary_ref";
    protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);

    public dpl_MercenaryAttack(GenericRaidParams params) {
        super(params);

        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();

        Global.getSector().getMemoryWithoutUpdate().unset(KEY);
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
    }

    @Override
    public String getNoun() {
        return "mercenary attack";
    }

    @Override
    public String getForcesNoun() {
        return super.getForcesNoun();
    }

    @Override
    public String getBaseName() {
        return "Phase Lab Mercenary Attack";
    }

    @Override
    protected void preConfigureFleet(int size, FleetCreatorMission m) {
        m.setFleetTypeLarge(FleetTypes.TASK_FORCE); // default would be "Patrol", don't want that
    }

    @Override
    protected void configureFleet(int size, FleetCreatorMission m) {
    	m.triggerSetFleetQuality(FleetQuality.SMOD_1);
    	m.triggerSetFleetFaction(Factions.INDEPENDENT);
        m.triggerSetFleetFlag(DPL_MERC_FLEET);
        if (size >= 8) m.triggerSetFleetDoctrineOther(5, 0); // more capitals in large fleets
    }

    @Override
    public void abort() {
        if (!isAborted()) for (CampaignFleetAPI curr : getFleets())
            curr.getMemoryWithoutUpdate().unset(DPL_MERC_FLEET);
        super.abort();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        interval.advance(Misc.getDays(amount));

        if (interval.intervalElapsed() && isCurrent(PAYLOAD_ACTION)) {
            String reason = "dpl_merc_raid_fleet";
            for (CampaignFleetAPI curr : getFleets())
                Misc.setFlagWithReason(curr.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE, reason, true, 1f);
        }
    }

    public static dpl_MercenaryAttack get() {
        return (dpl_MercenaryAttack) Global.getSector().getMemoryWithoutUpdate().get(KEY);
    }
}