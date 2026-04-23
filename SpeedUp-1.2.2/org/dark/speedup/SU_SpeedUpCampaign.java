package org.dark.speedup;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.dark.speedup.SU_Hotkey.Modifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles speed mult modifier in campaign.
 *
 * @author Histidine
 */
public class SU_SpeedUpCampaign implements CampaignInputListener {

    private static final float BASE_SPEEDUP = Global.getSettings().getFloat("campaignSpeedupMult");
    private static final String SOUND_ID = "ui_noise_static_message";
    private static final String TEXT_COLOR = "standardTextColor";

    private static ArrayList<SU_Hotkey> Hotkeys;
    private static boolean initialized = false;
    private static boolean firstFrame = true;
    private static float mult = BASE_SPEEDUP;

    public static void reloadSettings(JSONArray options) throws JSONException {
        if (options.length() == 0) {
            return;
        }

        Hotkeys = new ArrayList<>();
        for (int i = 0; i < options.length(); i++) {
            final JSONObject option = options.getJSONObject(i);
            final boolean isToggle = option.optInt("toggleKey", -1) != -1 || option.optInt("toggleMouse", -1) != -1;
            final EnumSet<Modifier> mods = EnumSet.noneOf(Modifier.class);
            for (Map.Entry<String, Modifier> modifier : (isToggle ? SU_Hotkey.toggleMap : SU_Hotkey.activateMap).entrySet()) {
                if (option.optBoolean(modifier.getKey(), false)) {
                    mods.add(modifier.getValue());
                }
            }

            SU_Hotkey hotkey = new SU_Hotkey() {
                {
                    isToggleHotkey = isToggle;
                    triggerKey = isToggle ? option.optInt("toggleKey", -1) : option.optInt("activateKey", -1);
                    triggerMouse = isToggle ? option.optInt("toggleMouse", -1) : option.optInt("activateMouse", -1);
                    modifiers = mods;
                    speedUpMult = (float) option.optDouble("speedUpMult", 1.0);
                    printMessage = option.optBoolean("printMessage", false);
                    onAtStart = option.optBoolean("onAtStart", false);
                }
            };

            Hotkeys.add(hotkey);
        }

        initialized = true;
    }

    @Override
    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {
        if (!initialized) {
            return;
        }

        firstFrame = SU_Hotkey.setStartFrameData(Hotkeys, firstFrame);
        SU_Hotkey.processInputEvents(Hotkeys, events);

        float currMult = BASE_SPEEDUP;
        for (SU_Hotkey hotkey : Hotkeys) {
            if (hotkey.active) {
                currMult *= hotkey.speedUpMult;
            }
        }

        if (mult != currMult) {
            mult = currMult;
            Global.getSettings().setFloat("campaignSpeedupMult", mult);
            Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
            // TODO externalize
            String str = String.format(Global.getSettings().getString("speedUp", "campaignMultMsg"), mult);
            Global.getSector().getCampaignUI().addMessage(str,
                    Global.getSettings().getColor(TEXT_COLOR),
                    mult + "",
                    "",
                    Misc.getHighlightColor(),
                    Color.BLACK);
        }
    }

    @Override
    public void processCampaignInputPreCore(List<InputEventAPI> events) {
    }

    @Override
    public void processCampaignInputPostCore(List<InputEventAPI> events) {
    }

    @Override
    public int getListenerInputPriority() {
        return 1;    // no idea what other listeners have
    }
}
