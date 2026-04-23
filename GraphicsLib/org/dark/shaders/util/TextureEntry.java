package org.dark.shaders.util;

import com.fs.starfarer.api.graphics.SpriteAPI;

/**
 * Entry format for texture data.
 * <p>
 * @author DarkRevenant
 * <p>
 * @since Alpha 1.5
 */
public class TextureEntry {

    public SpriteAPI sprite;
    public final String spriteName;
    public final float magnitude;
    public final boolean autoGen;
    public boolean validToLoad;
    public boolean loaded;
    public final boolean alwaysPreload;
    public long vramSize;

    public TextureEntry(SpriteAPI sprite, String spriteName, float magnitude, boolean autoGen, boolean validToLoad, boolean loaded, boolean alwaysPreload) {
        this.sprite = sprite;
        this.spriteName = spriteName;
        this.magnitude = magnitude;
        this.autoGen = autoGen;
        this.validToLoad = validToLoad;
        this.loaded = loaded;
        this.alwaysPreload = alwaysPreload;
        if ((sprite != null) && (sprite.getHeight() >= 1)) {
            vramSize = Math.round((double) (16 * (Math.round(this.sprite.getWidth() / this.sprite.getTextureWidth())
                    * Math.round(this.sprite.getHeight() / this.sprite.getTextureHeight()))) / 3.0);
        } else {
            vramSize = 0;
        }
    }

    public TextureEntry(SpriteAPI sprite, float magnitude) {
        this.sprite = sprite;
        spriteName = null;
        this.magnitude = magnitude;
        autoGen = false;
        validToLoad = false;
        loaded = true;
        alwaysPreload = false;
        if ((sprite != null) && (sprite.getHeight() >= 1)) {
            vramSize = Math.round((double) (16 * (Math.round(this.sprite.getWidth() / this.sprite.getTextureWidth())
                    * Math.round(this.sprite.getHeight() / this.sprite.getTextureHeight()))) / 3.0);
        } else {
            vramSize = 0;
        }
    }

    public TextureEntry(TextureEntry entry) {
        sprite = entry.sprite;
        spriteName = entry.spriteName;
        magnitude = entry.magnitude;
        autoGen = entry.autoGen;
        validToLoad = entry.validToLoad;
        loaded = entry.loaded;
        alwaysPreload = entry.alwaysPreload;
        vramSize = entry.vramSize;
    }

    public TextureEntry() {
        sprite = null;
        spriteName = null;
        magnitude = 0f;
        autoGen = false;
        validToLoad = false;
        loaded = false;
        alwaysPreload = false;
        vramSize = 0;
    }
}
