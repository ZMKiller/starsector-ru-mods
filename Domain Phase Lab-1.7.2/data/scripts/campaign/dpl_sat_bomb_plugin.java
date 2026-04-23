package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.TempData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Set;

/**
 *	Sierra's contact intel, also tracks what she wants to talk about and the Project SIREN event
 */

public class dpl_sat_bomb_plugin extends BaseIntelPlugin implements ColonyPlayerHostileActListener {

	// monthly "maintenance" cost for Sierra in credits
	private static final float MAINTENANCE_COST = 50f;
	private float randThoughtCounter = 0f;
	private final float randThoughtTime = 30f;
	//private static PersonAPI sierra = SotfPeople.getPerson(SotfPeople.SIERRA);

	// so we can hotswap the Sierra officer core for the special item in the cargo screen
	public boolean runWhilePaused() {
		return true;
	}

	public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
		Global.getSector().getScripts().add(new dpl_sat_bomb_script("dpl_mktSatBombCons"));
	}

	@Override
	public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market,
			TempData actionData, CargoAPI cargo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData,
			Industry industry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, TempData actionData) {
		// TODO Auto-generated method stub
		
	}
}







