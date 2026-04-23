package data.scripts.campaign.swprules;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * SWP_BTTTContact
 */
public class SWP_BTTTContact extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
        PersonAPI person = dialog.getInteractionTarget().getActivePerson();
        if (person == null) {
            return false;
        }

        ContactIntel contactIntel = ContactIntel.getContactIntel(person);
        if (contactIntel == null) {
            return false;
        }

        return person.getFaction().getId().contentEquals(Factions.TRITACHYON);
    }
}
