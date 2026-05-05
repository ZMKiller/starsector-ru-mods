package progsmod.data.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;

import progsmod.util.SModUtils;

/** ProgSModUseReserveXP [fleetMember] -- spends reserve XP to increase [fleetMember]'s XP
 *  by the amount specified in the selector whose id is the same as the "option" variable */
@SuppressWarnings("unused")
public class PSM_UseReserveXP extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params.size() < 1) {
            return false;
        }

        FleetMemberAPI fleetMember = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(params.get(0).string);
        String option = memoryMap.get(MemKeys.LOCAL).getString("$option");
        if (!dialog.getOptionPanel().hasSelector(option)) {
            return false;
        }

        SModUtils.useReserveXP(fleetMember.getHullSpec().getBaseHullId(), fleetMember, dialog.getOptionPanel().getSelectorValue(option));
        SModUtils.displayXP(dialog, fleetMember);

        return true;
    }
    
}
