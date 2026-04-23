package org.dark.speedup;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dark.speedup.SU_Hotkey.Modifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SU_SpeedUpEveryFrame extends BaseEveryFrameCombatPlugin {

    private static final String SOUND_ID = "ui_noise_static_message";
    private static final String STAT_ID_BASE = "SU_SpeedUpEveryFrame";
    private static final String STAT_ID_NBT = STAT_ID_BASE + "NBT";
    private static final String TEXT_COLOR = "standardTextColor";

    private static ArrayList<SU_Hotkey> Hotkeys;
    private static boolean initialized = false;
    private CombatEngineAPI engine;
    private boolean firstFrame;
//    public static Logger log = Global.getLogger(SU_SpeedUpEveryFrame.class);

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
                    bulletTimeMult = (float) option.optDouble("bulletTimeMult", option.optBoolean("noBulletTime", false) ? 1 : -1.0);
                    printMessage = option.optBoolean("printMessage", false);
                    onAtStart = option.optBoolean("onAtStart", false);
                    disableOthers = option.optBoolean("disableOthers", false);
                    capToFPS = (float) option.optDouble("capToFPS", 0.0);
                }
            };

            Hotkeys.add(hotkey);
        }

        initialized = true;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (!initialized) {
            return;
        }

        if (engine == null || engine.getCombatUI() == null) {
            return;
        }

        /* Exclude the starting screen */
        ShipAPI player = engine.getPlayerShip();
        if ((player == null || !engine.isEntityInPlay(player))
                && !engine.isInCampaign()
                && !engine.isInCampaignSim()
                && !engine.isUIShowingHUD()) {
            return;
        }

        firstFrame = SU_Hotkey.setStartFrameData(Hotkeys, firstFrame);
        boolean hotkeyWasPressed = SU_Hotkey.processInputEvents(Hotkeys, events);

        for (SU_Hotkey hotkey : Hotkeys) {
            if (hotkey.active) {
                /* Turned on */
                if (hotkey.speedUpMult != 1f) {
                    String statId = STAT_ID_BASE + Hotkeys.indexOf(hotkey);
                    float timeMult = hotkey.speedUpMult;
                    if ((timeMult > 1.0) && (hotkey.capToFPS > 0.0)) {
                        int roundedFrameTimeMsec = (int) Math.ceil(1000f * Global.getCombatEngine().getElapsedInLastFrame() + 1);
                        float scaledFPS = 1000f / roundedFrameTimeMsec;
                        float unscaledFPS = Global.getCombatEngine().getTimeMult().getModifiedValue() * scaledFPS;
                        float cappedTimeMult = Math.max(1f, unscaledFPS / hotkey.capToFPS);
                        timeMult = Math.min(cappedTimeMult, timeMult);
                    }
                    Global.getCombatEngine().getTimeMult().modifyMult(statId, timeMult, "SpeedUp");
                }
                if (hotkey.bulletTimeMult > 0.0) {
                    float totalPercentMod = 0f;
                    HashMap<String, StatMod> percentMods = Global.getCombatEngine().getTimeMult().getPercentMods();
                    for (Entry<String, StatMod> modEntry : percentMods.entrySet()) {
                        String id = modEntry.getKey();
                        if (id.startsWith(STAT_ID_BASE)) {
                            continue;
                        }
                        StatMod mod = modEntry.getValue();
                        totalPercentMod += mod.getValue();
                    }

                    float baseTimeMult = 1f + (totalPercentMod / 100f);
                    HashMap<String, StatMod> flatMods = Global.getCombatEngine().getTimeMult().getFlatMods();
                    for (Entry<String, StatMod> modEntry : flatMods.entrySet()) {
                        String id = modEntry.getKey();
                        if (id.startsWith(STAT_ID_BASE)) {
                            continue;
                        }
                        StatMod mod = modEntry.getValue();
                        baseTimeMult += mod.getValue();
                    }

                    HashMap<String, StatMod> multMods = Global.getCombatEngine().getTimeMult().getMultMods();
                    for (Entry<String, StatMod> modEntry : multMods.entrySet()) {
                        String id = modEntry.getKey();
                        if (id.startsWith(STAT_ID_BASE)) {
                            continue;
                        }
                        StatMod mod = modEntry.getValue();
                        baseTimeMult *= mod.getValue();
                    }

                    baseTimeMult = Math.min(Math.max(baseTimeMult, 1e-7f), 1f);
                    if (baseTimeMult < 1f) {
                        Global.getCombatEngine().getTimeMult().modifyMult(STAT_ID_NBT, hotkey.bulletTimeMult / baseTimeMult, "SpeedUp NBT");
                    } else {
                        Global.getCombatEngine().getTimeMult().unmodify(STAT_ID_NBT);
                    }
                }

                if (hotkey.printMessage && !hotkey.wasActive && hotkeyWasPressed) {
                    if (hotkey.speedUpMult > 1f) {
                        if (Math.abs(hotkey.speedUpMult - Math.round(hotkey.speedUpMult)) < 0.05f) {
                            if (hotkey.capToFPS > 0.0) {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged 1–%d%s speed-up.", Math.round(hotkey.speedUpMult), Strings.X));
                            } else {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged %d%s speed-up.", Math.round(hotkey.speedUpMult), Strings.X));
                            }
                        } else {
                            if (hotkey.capToFPS > 0.0) {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged 1–%.1f%s speed-up.", hotkey.speedUpMult, Strings.X));
                            } else {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged %.1f%s speed-up.", hotkey.speedUpMult, Strings.X));
                            }
                        }
                    } else if (hotkey.speedUpMult < 1f) {
                        if (Math.abs(hotkey.speedUpMult - Math.round(hotkey.speedUpMult)) < 0.05f) {
                            Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                    String.format("Engaged %d%s slow-mo.", Math.round(hotkey.speedUpMult), Strings.X));
                        } else {
                            Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                    String.format("Engaged %.1f%s slow-mo.", hotkey.speedUpMult, Strings.X));
                        }
                    }

                    if (hotkey.bulletTimeMult > 0.0) {
                        Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                String.format("Bullet-time intensity reduced by %.0f%%%s", hotkey.bulletTimeMult * 100, "."));
                    }
                    Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
                }
            } else if (hotkey.wasActive) {
                /* Turned off */
                if (hotkey.speedUpMult != 1f) {
                    String statId = STAT_ID_BASE + Hotkeys.indexOf(hotkey);
                    Global.getCombatEngine().getTimeMult().unmodify(statId);

                    if (hotkey.printMessage && hotkeyWasPressed) {
                        if (hotkey.speedUpMult > 1f) {
                            if (Math.abs(hotkey.speedUpMult - Math.round(hotkey.speedUpMult)) < 0.05f) {
                                if (hotkey.capToFPS > 0.0) {
                                    Global.getCombatEngine().getCombatUI().addMessage(0,
                                            Global.getSettings().getColor(TEXT_COLOR),
                                            String.format("Disengaged 1–%d%s speed-up.", Math.round(hotkey.speedUpMult), Strings.X));
                                } else {
                                    Global.getCombatEngine().getCombatUI().addMessage(0,
                                            Global.getSettings().getColor(TEXT_COLOR),
                                            String.format("Disengaged %d%s speed-up.", Math.round(hotkey.speedUpMult), Strings.X));
                                }
                            } else {
                                if (hotkey.capToFPS > 0.0) {
                                    Global.getCombatEngine().getCombatUI().addMessage(0,
                                            Global.getSettings().getColor(TEXT_COLOR),
                                            String.format("Disengaged 1–%.1f%s speed-up.", hotkey.speedUpMult, Strings.X));
                                } else {
                                    Global.getCombatEngine().getCombatUI().addMessage(0,
                                            Global.getSettings().getColor(TEXT_COLOR),
                                            String.format("Disengaged %.1f%s speed-up.", hotkey.speedUpMult, Strings.X));
                                }
                            }
                        } else if (hotkey.speedUpMult < 1f) {
                            if (Math.abs(hotkey.speedUpMult - Math.round(hotkey.speedUpMult)) < 0.05f) {
                                Global.getCombatEngine().getCombatUI().addMessage(0,
                                        Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Disengaged %d%s slow-mo.", Math.round(hotkey.speedUpMult), Strings.X));
                            } else {
                                Global.getCombatEngine().getCombatUI().addMessage(0,
                                        Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Disengaged %.1f%s slow-mo.", hotkey.speedUpMult, Strings.X));
                            }
                        }
                        Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
                    }
                }
                if (hotkey.bulletTimeMult > 0.0) {
                    Global.getCombatEngine().getTimeMult().unmodify(STAT_ID_NBT);
                    if (hotkey.printMessage) {
                        Global.getCombatEngine().getCombatUI().addMessage(0,
                                Global.getSettings().getColor(TEXT_COLOR),
                                "Bullet-time intensity set to normal.");
                        Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        this.firstFrame = true;
    }
}
