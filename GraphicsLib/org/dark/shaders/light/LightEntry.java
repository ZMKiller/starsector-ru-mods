package org.dark.shaders.light;

import java.awt.Color;

/**
 * Entry format for lighting data.
 * <p>
 * @author DarkRevenant
 */
public class LightEntry {

    public float chance = 0f;
    public boolean fighterDim = true;
    public Color flashColor = new Color(0, 0, 0);
    public float flashFadeout = 0f;
    public float flashIntensity = 0f;
    public float flashOffset = 0f;
    public float flashSize = 0f;
    public boolean hasFlash = false;
    public boolean hasHit = false;
    public boolean hasStandard = false;
    public Color hitColor = new Color(0, 0, 0);
    public float hitFadeout = 0f;
    public float hitIntensity = 0f;
    public float hitSize = 0f;
    public Color standardColor = new Color(0, 0, 0);
    public float standardFadeout = 0f;
    public float standardIntensity = 0f;
    public float standardOffset = 0f;
    public float standardSize = 0f;

    LightEntry() {
    }

    LightEntry(LightEntry entry) {
        hasStandard = entry.hasStandard;
        standardSize = entry.standardSize;
        standardIntensity = entry.standardIntensity;
        standardFadeout = entry.standardFadeout;
        standardColor = new Color(entry.standardColor.getRed(), entry.standardColor.getGreen(),
                entry.standardColor.getBlue());
        standardOffset = entry.standardOffset;

        hasHit = entry.hasHit;
        hitSize = entry.hitSize;
        hitIntensity = entry.hitIntensity;
        hitFadeout = entry.hitFadeout;
        hitColor = new Color(entry.hitColor.getRed(), entry.hitColor.getGreen(), entry.hitColor.getBlue());

        hasFlash = entry.hasFlash;
        flashSize = entry.flashSize;
        flashIntensity = entry.flashIntensity;
        flashFadeout = entry.flashFadeout;
        flashColor = new Color(entry.flashColor.getRed(), entry.flashColor.getGreen(), entry.flashColor.getBlue());
        flashOffset = entry.flashOffset;

        chance = entry.chance;
        fighterDim = entry.fighterDim;
    }
}
