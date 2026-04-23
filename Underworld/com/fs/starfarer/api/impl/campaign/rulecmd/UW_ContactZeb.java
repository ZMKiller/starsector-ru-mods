package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.campaign.fleets.UW_PalaceFleet;
import java.util.List;
import java.util.Map;
import java.util.Random;

// UW_ContactZeb
public class UW_ContactZeb extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        PersonAPI zeb = ip.getPerson("uw_zeb");
        if (zeb == null) {
            return false;
        }
        MarketAPI market = Global.getSector().getEconomy().getMarket("tibicena");
        if ((market == null) || !market.hasCondition("cabal_influence")) {
            market = UW_PalaceFleet.getRandomCabal(new Random());
        }
        if (market == null) {
            return false;
        }
        CampaignFleetAPI fleet;
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI campaignFleetAPI) {
            fleet = campaignFleetAPI;
        } else {
            return false;
        }
        List<OfficerDataAPI> officers = fleet.getFleetData().getOfficersCopy();

        fleet.getFleetData().removeOfficer(zeb);
        PersonAPI newCommander;
        if ((officers == null) || officers.isEmpty() || (officers.get(0) == zeb)) {
            newCommander = OfficerManagerEvent.createOfficer(fleet.getFaction(), 1, SkillPickPreference.ANY, false, fleet, true, true, -1, new Random());
            if (newCommander.getPersonalityAPI().getId().equals(Personalities.TIMID)) {
                newCommander.setPersonality(Personalities.CAUTIOUS);
            }
            fleet.getFleetData().addOfficer(newCommander);
        } else {
            newCommander = officers.get(0).getPerson();
            FleetMemberAPI oldShip = fleet.getFleetData().getMemberWithCaptain(newCommander);
            if (oldShip != null) {
                oldShip.setCaptain(null);
            }
        }
        fleet.setCommander(newCommander);
        if (fleet.getFlagship() != null) {
            fleet.getFlagship().setCaptain(newCommander);
        }

        ContactIntel.addPotentialContact(1f, zeb, market, dialog.getTextPanel());
        return true;
    }
}
