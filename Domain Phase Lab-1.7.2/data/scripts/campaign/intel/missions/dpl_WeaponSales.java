package data.scripts.campaign.intel.missions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class dpl_WeaponSales extends HubMissionWithBarEvent {
	
	protected int price;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		
		PersonAPI person = getPerson();
		if (person == null) return false;
		MarketAPI market = person.getMarket();
		if (market == null) return false;
		
		if (market.isPlayerOwned()) return false;
		if (!Misc.isMilitary(market) && market.getSize() < 7) return false;
		
		if (!setPersonMissionRef(person, "$dpl_weapon_sales_ref")) {
			return false;
		}
		
		if (barEvent) {
			setGiverIsPotentialContactOnSuccess();
		}
		
		//genRandom = Misc.random;
		
		price = 300000;
		
		setRepFactionChangesTiny();
		setRepPersonChangesVeryLow();
		
		return true;
	}
	
	protected void updateInteractionDataImpl() {
		// this is weird - in the accept() method, the mission is aborted, which unsets
		// $dpl_weapon_sales_ref. So: we use $dpl_weapon_sales_ref2 in the ContactPostAccept rule
		// and $dpl_weapon_sales_ref2 has an expiration of 0, so it'll get unset on its own later.
		set("$dpl_weapon_sales_ref2", this);
		
		set("$dpl_weapon_sales_barEvent", isBarEvent());
		set("$dpl_weapon_sales_price", Misc.getWithDGS(price));
		set("$dpl_weapon_sales_manOrWoman", getPerson().getManOrWoman());
		set("$dpl_weapon_sales_rank", getPerson().getRank().toLowerCase());
		set("$dpl_weapon_sales_rankAOrAn", getPerson().getRankArticle());
		set("$dpl_weapon_sales_hisOrHer", getPerson().getHisOrHer());
	}
	
	@Override
	protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
							     Map<String, MemoryAPI> memoryMap) {
		if ("showPerson".equals(action)) {
			dialog.getVisualPanel().showPersonInfo(getPerson(), true);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Limited Weapon Release"; // not used I don't think
	}
	
	@Override
	public void accept(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		// it's just an transaction immediate transaction handled in rules.csv
		// no intel item etc
		
		currentStage = new Object(); // so that the abort() assumes the mission was successful
		abort();
	
	}
	
}

