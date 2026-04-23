package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

// SWP_StopCustomMusic <fadeOut>
public class SWP_StopCustomMusic extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        int fadeOut = params.get(0).getInt(memoryMap);
        Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
        Global.getSoundPlayer().playCustomMusic(fadeOut, 0, null, false);

        return true;
    }
}
