package data.scripts.HSIRenderer;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class HSIButterflyRenderObjectV2 extends HSIBaseCombatRendererObject{
    private ShipAPI ship;

    private WeaponSlotAPI slot;
    private float fr = 20f;
    private int frameNum = 8;

    private float elapsed = 0f;

    private int currFrame = 0;
    public HSIButterflyRenderObjectV2(ShipAPI ship, WeaponSlotAPI slot ,int frame, float fps){
        this.ship = ship;
        this.slot = slot;
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
        String name = "Butterfly";
        SpriteAPI sprite = Global.getSettings().getSprite("HSI_Decoration",name+currFrame);
        float alpha = 1.5f-ship.getAlphaMult()*ship.getExtraAlphaMult()*ship.getExtraAlphaMult2();
        if(alpha>1) alpha = 1;
        if(alpha<0.1f) alpha = 0.1f;
        sprite.setAlphaMult(alpha);
        sprite.setAngle(ship.getFacing()-90f);
        sprite.renderAtCenter(slot.computePosition(ship).getX(),slot.computePosition(ship).getY());
    }

    @Override
    public boolean isExpired() {
        return !ship.isAlive()||!Global.getCombatEngine().isEntityInPlay(ship);
    }

    @Override
    public boolean shouldRender() {
        return true;
    }
}
