package org.dark.graphics.light;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.RoilingSwarmParams;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import java.awt.Color;
import org.dark.shaders.util.MapObjectAPI;
import org.dark.shaders.util.TextureData;
import org.dark.shaders.util.TextureData.TextureDataType;
import org.lwjgl.util.vector.Vector2f;

public class ThreatMapObject implements MapObjectAPI {

    public static final String THREAT_MATERIAL_PATH = "graphics/shaders/material/ships/threat/threat_fragments_material.png";
    public static final String THREAT_NORMAL_PATH = "graphics/shaders/normal/ships/threat/threat_fragments_normal.png";
    public static final String THREAT_SURFACE_PATH = "graphics/shaders/surface/ships/threat/threat_fragments_surface.png";

    static protected TextureDataType prevBindType = TextureDataType.MATERIAL_MAP;

    final protected SwarmMember member;
    final protected RoilingSwarmEffect effect;
    final protected SpriteAPI materialSprite;
    final protected SpriteAPI normalSprite;
    final protected SpriteAPI surfaceSprite;

    public ThreatMapObject(SwarmMember member, RoilingSwarmEffect effect) {
        this.member = member;
        this.effect = effect;

        final float texWidth = this.member.sprite.getTexWidth();
        final float texHeight = this.member.sprite.getTexHeight();
        final float texX = this.member.sprite.getTexX();
        final float texY = this.member.sprite.getTexY();

        if (TextureData.isLoadMaterial()) {
            this.materialSprite = Global.getSettings().getSprite(THREAT_MATERIAL_PATH);
            this.materialSprite.setTexWidth(texWidth);
            this.materialSprite.setTexHeight(texHeight);
            this.materialSprite.setTexX(texX);
            this.materialSprite.setTexY(texY);
            this.materialSprite.setNormalBlend();
        } else {
            this.materialSprite = Global.getSettings().getSprite(this.effect.getParams().spriteCat, this.effect.getParams().spriteKey);
        }

        if (TextureData.isLoadNormal()) {
            this.normalSprite = Global.getSettings().getSprite(THREAT_NORMAL_PATH);
            this.normalSprite.setTexWidth(texWidth);
            this.normalSprite.setTexHeight(texHeight);
            this.normalSprite.setTexX(texX);
            this.normalSprite.setTexY(texY);
            this.normalSprite.setNormalBlend();
        } else {
            this.normalSprite = Global.getSettings().getSprite(this.effect.getParams().spriteCat, this.effect.getParams().spriteKey);
        }

        if (TextureData.isLoadSurface()) {
            this.surfaceSprite = Global.getSettings().getSprite(THREAT_SURFACE_PATH);
            this.surfaceSprite.setTexWidth(texWidth);
            this.surfaceSprite.setTexHeight(texHeight);
            this.surfaceSprite.setTexX(texX);
            this.surfaceSprite.setTexY(texY);
            this.surfaceSprite.setNormalBlend();
        } else {
            this.surfaceSprite = Global.getSettings().getSprite(this.effect.getParams().spriteCat, this.effect.getParams().spriteKey);
        }
    }

    @Override
    public Vector2f getLocation() {
        return new Vector2f(member.loc);
    }

    @Override
    public float getRenderRadius() {
        float size = effect.getParams().baseSpriteSize;
        size *= member.scale * member.fader.getBrightness();
        return size;
    }

    @Override
    public float getNormalAngle() {
        return member.angle;
    }

    @Override
    public boolean getNormalFlipHorizontal() {
        return false;
    }

    @Override
    public boolean getNormalFlipVertical() {
        return false;
    }

    @Override
    public float getNormalMagnitude() {
        return 1f;
    }

    @Override
    public boolean hasNormal() {
        return TextureData.isLoadNormal();
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport, TextureDataType type, boolean alwaysBind) {
        RoilingSwarmParams params = effect.getParams();
        float alphaMult = viewport.getAlphaMult();
        if (alphaMult <= 0f) {
            return;
        }

        alphaMult *= params.alphaMult;
        float size = params.baseSpriteSize;
        size *= member.scale * member.fader.getBrightness();

        float b = member.fader.getBrightness();

        final SpriteAPI sprite;
        switch (type) {
            case MATERIAL_MAP ->
                sprite = materialSprite;
            case NORMAL_MAP ->
                sprite = normalSprite;
            case SURFACE_MAP ->
                sprite = surfaceSprite;
            default -> {
                return;
            }
        }

        if (alwaysBind || (prevBindType != type)) {
            sprite.bindTexture();
            prevBindType = type;
        }

        sprite.setAngle(member.angle);
        sprite.setSize(size, size);
        sprite.setAlphaMult(alphaMult * b * params.alphaMultBase);

        switch (type) {
            case MATERIAL_MAP ->
                sprite.setColor(params.color);
            case NORMAL_MAP -> {
                if (!TextureData.isLoadNormal()) {
                    sprite.setColor(Color.WHITE);
                }
            }
            case SURFACE_MAP -> {
                if (!TextureData.isLoadSurface()) {
                    sprite.setColor(new Color(0, 0, 0, params.color.getAlpha()));
                }
            }
        }

        sprite.renderAtCenterNoBind(member.loc.x, member.loc.y);
    }
}
