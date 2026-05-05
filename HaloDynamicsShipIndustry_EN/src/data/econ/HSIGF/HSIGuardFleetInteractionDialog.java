package data.econ.HSIGF;

import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;

public class HSIGuardFleetInteractionDialog extends FleetInteractionDialogPluginImpl {

    public HSIGuardFleetInteractionDialog() {
        super(null);
        context = new HSIGuardFleetContext();
    }

}
