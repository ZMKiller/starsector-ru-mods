package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.CallEvent;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.TokenType;
import data.scripts.campaign.intel.missions.SWP_BuriedTreasure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SWP_BTCall extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_ref")) {
            List<Token> callParams = new ArrayList<>(params);
            callParams.add(0, new Token("$global.swpBT_ref", TokenType.VARIABLE));
            return (new CallEvent()).execute(ruleId, dialog, callParams, memoryMap);
        } else {
            String action = params.get(0).getString(memoryMap);
            return SWP_BuriedTreasure.staticCall(action, ruleId, dialog, params, memoryMap);
        }
    }
}
