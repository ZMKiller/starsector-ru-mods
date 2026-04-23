package data.scripts.items;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_Condenser extends BaseSpecialItemPlugin {
	
	@Override
	public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		return super.getPrice(market, submarket);
	}
	
	@Override
	public String getDesignType() {
		return null;
	}
	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		//super.createTooltip(tooltip, expanded, transferHandler, stackSource);
		
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color b = Misc.getButtonTextColor();
		b = Misc.getPositiveHighlightColor();

		tooltip.addTitle(getName());
		
		String design = getDesignType();
		if (design != null) {
			Misc.addDesignTypePara(tooltip, design, 10f);
		}
		
		if (!spec.getDesc().isEmpty()) {
			tooltip.addPara(spec.getDesc(), Misc.getTextColor(), opad);
		}
		
		addCostLabel(tooltip, opad, transferHandler, stackSource);
		
		tooltip.addPara("Right-click to consume 1 unit of the " + getName() + " for fast repairs", b, opad);
	}

	@Override
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}
	
	@Override
	public boolean isTooltipExpandable() {
		return false;
	}
	
	@Override
	public boolean hasRightClickAction() {
		return true;
	}

	@Override
	public boolean shouldRemoveOnRightClickAction() {
		return true;
	}

	@Override
	public void performRightClickAction() {

        if (Global.getSector().getPlayerFleet() == null) return;
        
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI member: playerFleet.getFleetData().getMembersListCopy()) {
        	if (!member.isMothballed()) {
        		member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
        	}
        }
        
		Global.getSoundPlayer().playUISound(getSpec().getSoundId(), 1f, 1f);
		Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
				getName() + " consumed. All your ships are repaired to their best states.");//,
	}
}



