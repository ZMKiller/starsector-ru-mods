package org.dark.speedup;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.apache.log4j.Logger;

public class SU_Hotkey {
//    public static Logger log = Global.getLogger(SU_Hotkey.class);

    public static Map<String, Modifier> activateMap = new HashMap<String, Modifier>() {
        {
            put("activateCtrl", Modifier.CTRL);
            put("activateAlt", Modifier.ALT);
            put("activateShift", Modifier.SHIFT);
        }
    };
    public static Map<String, Modifier> toggleMap = new HashMap<String, Modifier>() {
        {
            put("toggleCtrl", Modifier.CTRL);
            put("toggleAlt", Modifier.ALT);
            put("toggleShift", Modifier.SHIFT);
        }
    };
    public boolean isToggleHotkey;
    public int triggerKey;
    public int triggerMouse;
    public EnumSet<Modifier> modifiers;
    public float speedUpMult;
    public float bulletTimeMult;
    public boolean printMessage;
    public boolean onAtStart;
    public boolean disableOthers;
    public float capToFPS;
    public boolean active = false;
    public boolean toggled = false;
    public boolean wasActive = false;
    public boolean keepEnabled = false;

    SU_Hotkey() {
    }

    public static boolean setStartFrameData(ArrayList<SU_Hotkey> hotkeys, boolean firstFrame) {
        boolean tempFrame = firstFrame;
        for (SU_Hotkey hotkey : hotkeys) {
            hotkey.wasActive = hotkey.active;
            hotkey.keepEnabled = false;
            if (firstFrame) {
                if (hotkey.onAtStart) {
                    hotkey.active = true;
                    hotkey.toggled = true;
                } else {
                    hotkey.active = false;
                    hotkey.toggled = false;
                }
                hotkey.wasActive = false;
                tempFrame = false;
            }
        }

        return tempFrame;
    }

    public static void flipHotkeyState(SU_Hotkey hotkey) {
        if (hotkey.isToggleHotkey) {
            hotkey.active = !hotkey.active;
            hotkey.toggled = !hotkey.toggled;
        } else {
            hotkey.active = !hotkey.toggled;
        }
    }

    public static boolean processInputEvents(List<SU_Hotkey> hotkeys, List<InputEventAPI> events) {
        SU_Hotkey exclusiveHotkey = null;
        ArrayList<SU_Hotkey> nonExclusiveHotkeys = new ArrayList<>();
        boolean hotkeyWasPressed = false;
        for (InputEventAPI event : events) {
            if (event.isConsumed()) {
                continue;
            }

            boolean handledInput = false;
            for (SU_Hotkey hotkey : hotkeys) {
                if (!hotkey.isToggleHotkey) {
                    if ((event.getEventType() == InputEventType.KEY_UP && event.getEventValue() == hotkey.triggerKey)
                            || (event.getEventType() == InputEventType.MOUSE_UP && event.getEventValue() == hotkey.triggerMouse)) {
                        hotkey.active = hotkey.toggled;
                        handledInput = true;
                        hotkeyWasPressed = true;
                    }
                }
            }

            if (handledInput) {
                event.consume();
                continue;
            }

            int mostMods = 0;
            SU_Hotkey commandPrecedent = null;

            for (SU_Hotkey hotkey : hotkeys) {
                if (event.getEventType() == InputEventType.KEY_DOWN
                        && event.getEventValue() == hotkey.triggerKey) {
                    if (hotkey.modifiers.size() >= mostMods
                            && getUnpressedModifierKeys(hotkey.modifiers, event) == 0) {
                        mostMods = hotkey.modifiers.size();
                        commandPrecedent = hotkey;
                    }
                }
                if (event.getEventType() == InputEventType.MOUSE_DOWN
                        && event.getEventValue() == hotkey.triggerMouse) {
                    if (hotkey.modifiers.size() >= mostMods
                            && getUnpressedModifierKeys(hotkey.modifiers, event) == 0) {
                        mostMods = hotkey.modifiers.size();
                        commandPrecedent = hotkey;
                    }
                }
            }

            if (commandPrecedent == null) {
                continue;
            }

            if (commandPrecedent.disableOthers && exclusiveHotkey == null) {
                commandPrecedent.keepEnabled = true;
                exclusiveHotkey = commandPrecedent;
            } else {
                nonExclusiveHotkeys.add(commandPrecedent);
            }
            hotkeyWasPressed = true;
            event.consume();
        }

        if (exclusiveHotkey != null) {
            flipHotkeyState(exclusiveHotkey);
            if (exclusiveHotkey.active) {
                for (SU_Hotkey hotkey : hotkeys) {
                    if (!hotkey.keepEnabled) {
                        hotkey.wasActive = hotkey.active;
                        hotkey.active = false;
                        hotkey.toggled = false;
                    }
                }
            }
        }

        if (exclusiveHotkey == null || !exclusiveHotkey.active) {
            for (SU_Hotkey hotkey : nonExclusiveHotkeys) {
                flipHotkeyState(hotkey);
            }
        }

        return hotkeyWasPressed;
    }

    public static int getUnpressedModifierKeys(EnumSet<Modifier> modifierKeys, InputEventAPI event) {
        int condition = modifierKeys.size();
        for (Modifier modifier : modifierKeys) {
            switch (modifier) {
                case CTRL -> {
                    if (event.isCtrlDown()) {
                        condition--;
                    }
                }
                case ALT -> {
                    if (event.isAltDown()) {
                        condition--;
                    }
                }
                case SHIFT -> {
                    if (event.isShiftDown()) {
                        condition--;
                    }
                }
            }
        }

        return condition;
    }

    @Override
    public String toString() {
        return String.format(" isToggle %s - key %s - mouse %s - modifiers %s - speed %s - bullet %s - start %s - disable %s - cap %s - active %s - toggled %s - wasActive %s - keepEnabled %s",
                isToggleHotkey, triggerKey, triggerMouse, modifiers, speedUpMult, bulletTimeMult, onAtStart, disableOthers, capToFPS, active, toggled, wasActive, keepEnabled);
    }

    public enum Modifier {
        CTRL, ALT, SHIFT
    }
}
