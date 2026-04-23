package data.scripts.campaign;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;

public class SWP_IBBInteractionDialogPlugin extends FleetInteractionDialogPluginImpl {

    public SWP_IBBInteractionDialogPlugin() {
        this(null);
    }

    public SWP_IBBInteractionDialogPlugin(FIDConfig params) {
        super(params);
        context = new SWP_IBBFleetEncounterContext();
    }
}
