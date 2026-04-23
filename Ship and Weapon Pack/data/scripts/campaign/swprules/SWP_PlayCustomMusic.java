package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

// SWP_PlayCustomMusic <musicId> <fadeOut> <fadeIn> <loop>
public class SWP_PlayCustomMusic extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        String musicId = params.get(0).getString(memoryMap);
        int fadeOut = params.get(1).getInt(memoryMap);
        int fadeIn = params.get(2).getInt(memoryMap);
        boolean loop = params.get(3).getBoolean(memoryMap);
        Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true);
        if ((Global.getSoundPlayer().getCurrentMusicId() != null) && Global.getSoundPlayer().getCurrentMusicId().replaceAll(".ogg", "").contentEquals(musicId)) {
            Global.getSoundPlayer().resumeCustomMusic();
        } else {
            Global.getSoundPlayer().playCustomMusic(fadeOut, fadeIn, musicId, loop);
        }

        return true;
    }
}
