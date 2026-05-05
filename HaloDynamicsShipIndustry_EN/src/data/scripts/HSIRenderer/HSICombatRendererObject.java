package data.scripts.HSIRenderer;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;

public interface HSICombatRendererObject {
    public void advance(float amount);

    public void render();

    public boolean isRelative();
    public void setRelative(boolean relative);

    public CombatEntityAPI getAnchor();
    public void setAnchor(CombatEntityAPI anchor);

    public float getElapsed();
    public void setElapsed(float elapsed);

    public float getLife();
    public void setLife(float life);
    public boolean isExpired();

    public CombatEngineLayers getLayer();
    public void setLayer(CombatEngineLayers layer);

    public Vector3f getLocation3f();
    public Vector2f getLocation2f();
    public void setLocation3f(Vector3f location);
    public void setLocation2f(Vector2f location);

    public boolean shouldRender();
    public float getRadius();
}
