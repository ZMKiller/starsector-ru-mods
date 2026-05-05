package data.scripts.HSIRenderer;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class HSIButterflyRenderObject extends HSIBaseCombatRendererObject{
    private CombatEntityAPI anchor;

    private float fr = 15f;
    private int frameNum = 8;

    private float elapsed = 0f;

    private int currFrame = 0;
    public HSIButterflyRenderObject(CombatEntityAPI anchor,int frame,float fps){
        this.anchor = anchor;
        this.frameNum = frame;
        this.fr = fps;
    }

    @Override
    public void advance(float amount) {
        elapsed+=amount;
        currFrame = (int)(elapsed/(1f/fr));
        if(currFrame>=frameNum){
            currFrame = 0;
            elapsed-=(frameNum/fr);
        }
    }

    @Override
    public void render() {
        String name = "ButterflyBullet";
        if(anchor instanceof ShipAPI&&((ShipAPI)(anchor)).isFighter()) name = "ButterflyS";
        if(anchor instanceof ShipAPI&&!((ShipAPI)(anchor)).isFighter()) name = "Butterfly";
        SpriteAPI sprite = Global.getSettings().getSprite("HSI_Decoration",name+currFrame);
        sprite.setAngle(anchor.getFacing()-90f);
        sprite.renderAtCenter(anchor.getLocation().getX(),anchor.getLocation().getY());
    }

    @Override
    public boolean isExpired() {
        return anchor.isExpired()||!Global.getCombatEngine().isEntityInPlay(anchor)||anchor.getHitpoints()<=0|| anchor.getOwner() == 100;
    }

    @Override
    public boolean shouldRender() {
        return true;
    }
}
