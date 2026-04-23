package org.dark.shaders.util;

import lunalib.lunaSettings.LunaSettingsListener;

/**
 * Support class for LunaLib, intended solely for internal use.
 * <p>
 * @author DarkRevenant
 */
public class GraphicsLibSettingsListener implements LunaSettingsListener {

    @Override
    public void settingsChanged(String modId) {
        if (!modId.contentEquals("shaderLib")) {
            return;
        }

        GraphicsLibSettings.applyChanges();
    }
}
