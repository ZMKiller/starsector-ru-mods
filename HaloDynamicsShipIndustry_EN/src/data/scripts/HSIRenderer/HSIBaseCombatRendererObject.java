package data.scripts.HSIRenderer;

import org.dark.shaders.util.ShaderLib;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;

public class HSIBaseCombatRendererObject implements HSICombatRendererObject{
    private boolean relative;
    private CombatEntityAPI anchor;//if relative
    private Vector3f location;
    private float life;
    private float elapsed;//for life use
    private CombatEngineLayers layer = CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER;

    public HSIBaseCombatRendererObject(){
        this.relative = false;
        this.anchor = null;
        this.location = new Vector3f();
        this.life = -1;
        this.elapsed = 0;
    }

    public HSIBaseCombatRendererObject(boolean relative,CombatEntityAPI anchor,Vector2f location,float life){
        this.relative = relative;
        this.anchor = anchor;
        setLocation2f(location);
        this.life = life;
        this.elapsed = 0;
    }

    public HSIBaseCombatRendererObject(boolean relative,CombatEntityAPI anchor,Vector3f location,float life){
        this.relative = relative;
        this.anchor = anchor;
        setLocation3f(location);
        this.life = life;
        this.elapsed = 0;
    }
    
    public void advance(float amount){
        if(life!=-1){
            elapsed+=life;
        }
    }

    public void render(){

    }

    public boolean isRelative() {
        return relative;
    }

    public void setRelative(boolean relative) {
        this.relative = relative;
    }

    public CombatEntityAPI getAnchor() {
        return anchor;
    }

    public void setAnchor(CombatEntityAPI anchor) {
        this.anchor = anchor;
    }

    public float getElapsed() {
        return elapsed;
    }

    public void setElapsed(float elapsed) {
        this.elapsed = elapsed;
    }

    public float getLife() {
        return life;
    }

    public void setLife(float life) {
        this.life = life;
    }

    @Override
    public boolean isExpired() {
        return !(life==-1||elapsed<life);
    }

    public Vector3f getLocation3f() {
        return location;
    }

    public Vector2f getLocation2f() {
        return new Vector2f(location.x,location.y);
    }

    public void setLocation3f(Vector3f location) {
        this.location = new Vector3f(location);
    }

    public void setLocation2f(Vector2f location){
        this.location = new Vector3f(location.x,location.y,0.0f);
    }

    @Override
    public CombatEngineLayers getLayer() {
        return layer;
    }

    @Override
    public void setLayer(CombatEngineLayers layer) {
        this.layer = layer;
    }

    @Override
    public boolean shouldRender() {
        return ShaderLib.isOnScreen(getAnchor().getLocation(), getRadius());
    }

    @Override
    public float getRadius() {
        return getAnchor().getCollisionRadius()*3f;
    }
}
