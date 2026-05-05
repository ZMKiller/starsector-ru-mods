package data.scripts.HSIRenderer;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;

import data.kit.AjimusUtils;

public class HSISquareObject extends HSIBaseCombatRendererObject {
    private Color color;
    private float width;
    private float height;

    public HSISquareObject(boolean relative, CombatEntityAPI anchor, Vector2f location, float life, Color color,
            float width, float height) {
        super(relative, anchor, location, life);
        setColor(color);
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
    }

    @Override
    public void render() {
        Vector2f currentLocation = getLocation2f();
        if (isRelative())
            currentLocation = AjimusUtils.getEngineCoordFromRelativeCoord(getAnchor().getLocation(), getLocation2f(),
                    getAnchor().getFacing());
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPushMatrix();
        GL11.glTranslatef(currentLocation.getX(), currentLocation.getY(), 0.0f);
        GL11.glRotatef(getAnchor().getFacing(), 0f, 0f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4b((byte) (color.getRed() / 255f), (byte) (color.getGreen() / 255f),
                (byte) (color.getBlue() / 255f),
                (byte) (color.getAlpha() / 255f * Math.sin(Math.PI * getElapsed() / getLife())));
        GL11.glVertex2f(width / 2f, height / 2);
        GL11.glVertex2f(-width / 2f, height / 2);
        GL11.glVertex2f(-width / 2f, -height / 2);
        GL11.glVertex2f(width / 2f, -height / 2);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
