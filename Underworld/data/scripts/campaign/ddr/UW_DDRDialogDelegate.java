package data.scripts.campaign.ddr;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import java.util.Map;

public class UW_DDRDialogDelegate implements CustomVisualDialogDelegate {

    protected DialogCallbacks callbacks;
    protected float endDelay = 2f;
    protected boolean finished = false;

    protected UW_DDRPanel ddrPanel;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected boolean practiceMode;

    public UW_DDRDialogDelegate(UW_DDRPanel ddrPanel, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, boolean practiceMode) {
        this.ddrPanel = ddrPanel;
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        this.practiceMode = practiceMode;
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return ddrPanel;
    }

    @Override
    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.callbacks = callbacks;
        callbacks.getPanelFader().setDurationOut(2f);
        ddrPanel.init(panel, callbacks, dialog);
    }

    @Override
    public float getNoiseAlpha() {
        return 0;
    }

    @Override
    public void advance(float amount) {
        /* TODO */
        if (!finished && false) {
            endDelay -= amount;
            if (endDelay <= 0f) {
                callbacks.getPanelFader().fadeOut();
                if (callbacks.getPanelFader().isFadedOut()) {
                    callbacks.dismissDialog();
                    finished = true;
                }
            }
        }
    }

    @Override
    public void reportDismissed(int option) {
        if (memoryMap != null) {
            if (!practiceMode) {
//                    memoryMap.get(MemKeys.LOCAL).set("$soe_playerWonDuel", true, 0);
//                FireBest.fire(null, dialog, memoryMap, "SOEDuelFinished");
            } else {
//                FireBest.fire(null, dialog, memoryMap, "SOETutorialFinished");
            }
        }
    }
}
