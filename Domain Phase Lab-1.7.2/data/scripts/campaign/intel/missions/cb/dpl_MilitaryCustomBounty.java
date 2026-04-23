package data.scripts.campaign.intel.missions.cb;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.missions.cb.BaseCustomBounty;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBPirate;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBDeserter;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBMerc;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBPather;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBRemnant;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBRemnantStation;
import com.fs.starfarer.api.impl.campaign.missions.cb.CustomBountyCreator;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBDerelict;



public class dpl_MilitaryCustomBounty extends BaseCustomBounty {

	public static List<CustomBountyCreator> CREATORS = new ArrayList<CustomBountyCreator>();
	static {
		CREATORS.add(new CBPirate());
		CREATORS.add(new CBDeserter());
		CREATORS.add(new CBDerelict());
		CREATORS.add(new CBMerc());
		CREATORS.add(new CBPather());
		CREATORS.add(new CBRemnant());
		CREATORS.add(new CBRemnantStation());
		CREATORS.add(new dpl_CBRemnantPlus());
		CREATORS.add(new dpl_CBHelheimSingularity());
	}
	
	@Override
	public List<CustomBountyCreator> getCreators() {
		return CREATORS;
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		if (Factions.PIRATES.equals(createdAt.getFaction().getId())) {
			return false;
		}
		return super.create(createdAt, barEvent);
	}
	
}











