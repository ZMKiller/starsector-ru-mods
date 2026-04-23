package data.campaign.rulecmd;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

import data.scripts.world.dpl_ContactIntel;

/**
 * AddPotentialContact <optional person id>
 */
public class dpl_AddPotentialContact extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (entity == null) return false;
		
		PersonAPI person = null;
		if (params.size() > 0) {
			String personId = params.get(0).getString(memoryMap);
			PersonDataAPI data = Global.getSector().getImportantPeople().getData(personId);
			if (data != null) {
				person = data.getPerson();
			}
		}
		
		if (person == null) {
			person = entity.getActivePerson();
		}
		if (person == null) return false;
		
		addPotentialContact(1f, person, entity.getMarket(), dialog.getTextPanel());
		return true;
	}
	
	public static void addPotentialContact(float probability, PersonAPI contact, MarketAPI market, TextPanelAPI text) {
		if (ContactIntel.playerHasIntelItemForContact(contact)) return;
		if (contact.getFaction().isPlayerFaction()) return;
		if (market == null) return;
		if (market != null && market.getMemoryWithoutUpdate().getBoolean(ContactIntel.NO_CONTACTS_ON_MARKET)) return;
		if (contact != null && contact.getFaction().getCustomBoolean(Factions.CUSTOM_NO_CONTACTS)) return;
		
		Random random = new Random(ContactIntel.getContactRandomSeed(contact));
		// if the player already has some existing relationship with the person, use it to 
		// modify the probability they'll be available as a potential contact
		probability += contact.getRelToPlayer().getRel();
		
		
		String key = "$potentialContactRollFails";
		MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
		float fails = mem.getInt(key);
		probability += ContactIntel.ADD_PER_FAIL * fails;
		
		if (random.nextFloat() >= probability && !DebugFlags.ALWAYS_ADD_POTENTIAL_CONTACT) {
			fails++;
			mem.set(key, fails);
			return;
		}
		
		mem.set(key, 0);
		
		dpl_ContactIntel intel = new dpl_ContactIntel(contact, market);
		Global.getSector().getIntelManager().addIntel(intel, false, text);
	}

}








