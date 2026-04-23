package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel.ContactState;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import java.awt.Color;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Special Contacts.
 */
public class dpl_ContactIntel extends ContactIntel {
	
	public dpl_ContactIntel(PersonAPI person, MarketAPI market) {
		super(person, market);
	}
	
	public String getName() {
		String name = "Special Contact" + ": " + person.getNameString();
		if (isEnding() || isEnded()) name += " - " + "lost";
		return name;
	}
	
	protected void dpl_addTypePara(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		String [] tags = {"Special"};
		if (tags.length <= 0) return;
		String str = "Type: ";
		for (String tag : tags) {
			str += tag + ", ";
		}
		if (tags.length > 0) {
			str = str.substring(0, str.length() - 2);
		}
		info.addPara(str, pad, tc, h, tags);
	}
	
	@Override
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = getBulletColorForMode(mode);
		
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;

		bullet(info);
		
		if (getListInfoParam() == UPDATE_RELOCATED_CONTACT) {
			info.addPara("Relocated to " + market.getName(), tc, initPad);
			initPad = 0f;
			info.addPara("New Importance is: %s", initPad, tc, h, person.getImportance().getDisplayName());
			initPad = 0f;
			unindent(info);
			return;
		}
		if (state == ContactState.LOST_CONTACT_DECIV) {
			if (mode != ListInfoMode.IN_DESC) {
				info.addPara(market.getName() + " decivilized", tc, initPad);
				initPad = 0f;
			}
			unindent(info);
			return;
		}
		
		if (state == ContactState.LOST_CONTACT) {
			unindent(info);
			return;
		}
		
		addFactionPara(info, tc, initPad);
		initPad = 0f;
		
		dpl_addTypePara(info, tc, initPad);
		initPad = 0f;
		
		if (mode != ListInfoMode.IN_DESC) {
			info.addPara("Importance: %s", initPad, tc, h, person.getImportance().getDisplayName());
			initPad = 0f;
			
			if (state == ContactState.PRIORITY || state == ContactState.NON_PRIORITY || state == ContactState.SUSPENDED) {
				long ts = BaseMissionHub.getLastOpenedTimestamp(person);
				if (ts <= Long.MIN_VALUE) {
					//info.addPara("Never visited.", opad);	
				} else {
					info.addPara("Last visited: %s.", initPad, tc, h, Misc.getDetailedAgoString(ts));
					initPad = 0f;
				}
			}
		}
	}
	
	// don't lose importance when relocating
	@Override
	public void relocateToMarket(MarketAPI other, boolean withIntelUpdate) {
		super.relocateToMarket(other, withIntelUpdate);
		person.setImportance(person.getImportance().next());
	}
	
}
