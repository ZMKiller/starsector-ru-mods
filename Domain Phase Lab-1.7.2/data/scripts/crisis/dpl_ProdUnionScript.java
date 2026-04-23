package data.scripts.crisis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

public class dpl_ProdUnionScript implements EconomyUpdateListener {
    public static String KEY = "$dpl_prod_union_ref";

    public dpl_ProdUnionScript() {
        sendGainedMessage();

        // to avoid duplicates
        dpl_ProdUnionScript existing = get();
        if (existing != null) {
            return;
        }

        Global.getSector().getEconomy().addUpdateListener(this);
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);

        economyUpdated();
    }

    @Override
    public void commodityUpdated(String commodityId) {
    }

    @Override
    public void economyUpdated() {
        for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
            if (!curr.hasCondition("dpl_prod_union")) {
                curr.addCondition("dpl_prod_union");
            }
        }
        for (MarketAPI curr : Misc.getFactionMarkets("dpl_phase_lab")) {
            if (!curr.hasCondition("dpl_prod_union")) {
                curr.addCondition("dpl_prod_union");
            }
        }
    }

    @Override
    public boolean isEconomyListenerExpired() {
        if (dpl_HostileActivityFactor.dpl_DealtWithBadGuy()) return false;
        if (!dpl_AgreedUnionWithDPL()) return false;

        if (!isTrustworthyToDPL()) {
            cleanup();
            return true;
        }
        return false;
    }
    
    public static boolean dpl_AgreedUnionWithDPL() {
    	boolean AgreedUnionWithDPL = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_AgreedUnionWithDPL");
		return AgreedUnionWithDPL;
	}

    public static boolean isTrustworthyToDPL() {
        return (Global.getSector().getFaction("dpl_phase_lab").getRelToPlayer().isAtWorst(RepLevel.FRIENDLY));
    }

    public static dpl_ProdUnionScript get() {
        return (dpl_ProdUnionScript) Global.getSector().getMemoryWithoutUpdate().get(KEY);
    }

    private void sendGainedMessage() {
        MessageIntel msg = new MessageIntel();
        msg.addLine("Production Union gained", Misc.getBasePlayerColor());
        msg.addLine(BaseIntelPlugin.BULLET + "Your colonies and the Phase Lab's colonies receive a %s increase to heavy industry production and cost.", Misc.getTextColor(), new String[]{"bonus"}, Misc.getHighlightColor());

        msg.setIcon(Global.getSettings().getSpriteName("events", "stage_unknown_good"));
        msg.setSound(Sounds.REP_GAIN);
        Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
    }

    private void sendExpiredMessage() {
        MessageIntel msg = new MessageIntel();
        msg.addLine("Production Union removed", Misc.getBasePlayerColor());
        msg.addLine(BaseIntelPlugin.BULLET + "Due to deteriorating relations with the %s", Misc.getTextColor(), new String[]{"Phase Lab"}, Global.getSector().getFaction("dpl_phase_lab").getBaseUIColor());
        msg.setIcon(Global.getSettings().getSpriteName("events", "stage_unknown_bad"));
        msg.setSound(Sounds.REP_LOSS);
        Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.COLONY_INFO);
    }

    private void cleanup() {
        if (Global.getSector().getMemoryWithoutUpdate().contains(KEY)) {
            sendExpiredMessage();
        }
        Global.getSector().getMemoryWithoutUpdate().unset(KEY);
        for (MarketAPI curr : Misc.getPlayerMarkets(false)) {
            if (curr.hasCondition("dpl_prod_union")) {
                curr.removeCondition("dpl_prod_union");
            }
        }
        for (MarketAPI curr : Misc.getFactionMarkets("dpl_phase_lab")) {
            if (curr.hasCondition("dpl_prod_union")) {
                curr.removeCondition("dpl_prod_union");
            }
        }
    }
}