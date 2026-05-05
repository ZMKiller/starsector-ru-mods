package progsmod.data.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.refit.ModPickerDialogV3;
import progsmod.util.ReflectionUtils;

import java.util.LinkedList;
import java.util.List;

public class RefitTabListenerAndScript implements CoreUITabListener, EveryFrameScript {
    private static boolean insideRefitScreen = false;

    @Override
    public void reportAboutToOpenCoreTab(CoreUITabId id, Object param) {
        if (CoreUITabId.REFIT.equals(id) && !insideRefitScreen) {
            insideRefitScreen = true;
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float v) {
        if (!insideRefitScreen) return;
        if (Global.getSector() == null || Global.getSector().getCampaignUI() == null) return;

        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (!CoreUITabId.REFIT.equals(ui.getCurrentCoreTab())) {
            insideRefitScreen = false;
            return;
        }

        LinkedList<UIPanelAPI> modsPanels = getAllModsPanels(ReflectionUtils.getCoreUI());
        // Delete the build-in button from all mods panels, including inactive ones
        for (UIPanelAPI panel : modsPanels) {
            ButtonAPI perm = (ButtonAPI) ReflectionUtils.invokeMethod(panel, "getPerm");
            if (perm != null) {
                perm.setOpacity(0f);
            }
        }
    }

    /**
     * Multiple mods panels can appear in two ways:
     * - If adding hullmods, a separate mods panel is generated (different from getModDisplay.getMods)
     * - In rare instances, rapid clicking of the Add button can generate multiple separate mods panels
     */
    public LinkedList<UIPanelAPI> getAllModsPanels(UIPanelAPI coreUI) {
        LinkedList<UIPanelAPI> panelList = new LinkedList<>();
        try {
            Object currentTab = ReflectionUtils.invokeMethodNoCatch(coreUI, "getCurrentTab");
            Object refitPanel = ReflectionUtils.invokeMethodNoCatch(currentTab, "getRefitPanel");
            Object modDisplay = ReflectionUtils.invokeMethodNoCatch(refitPanel, "getModDisplay");
            panelList.add((UIPanelAPI) ReflectionUtils.invokeMethodNoCatch(modDisplay, "getMods"));

            // The screen for adding hull mods has a different mod display object for some reason
            List<?> coreChildren = (List<?>) ReflectionUtils.invokeMethod(coreUI, "getChildrenNonCopy");
            for (Object child : coreChildren) {
                if (child instanceof ModPickerDialogV3) {
                    List<?> subChildren = (List<?>) ReflectionUtils.invokeMethod(child, "getChildrenNonCopy");
                    for (Object subChild : subChildren) {
                        if (modDisplay != null && subChild.getClass().equals(modDisplay.getClass())) {
                            panelList.add((UIPanelAPI) ReflectionUtils.invokeMethodNoCatch(subChild, "getMods"));
                        }
                    }
                }
            }

            return panelList;
        } catch (Exception e) {
            return new LinkedList<>();
        }
    }
}
