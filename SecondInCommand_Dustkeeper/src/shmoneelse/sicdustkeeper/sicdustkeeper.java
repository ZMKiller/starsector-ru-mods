package shmoneelse.sicdustkeeper;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import shmoneelse.sicdustkeeper.scripts.sicdk_genXO;

public class sicdustkeeper extends BaseModPlugin {

    //@Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        if(!Global.getSector().getMemoryWithoutUpdate().contains("$sicdk_madeXO") && !Global.getSector().getListenerManager().hasListenerOfClass(sicdk_genXO.class))
            Global.getSector().getListenerManager().addListener(new sicdk_genXO(), false);
    }

}
