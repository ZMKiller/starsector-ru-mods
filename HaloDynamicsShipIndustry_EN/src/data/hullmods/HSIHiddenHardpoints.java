package data.hullmods;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MissileRenderDataAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

public class HSIHiddenHardpoints extends BaseHullMod{
    

    public void advanceInCombat(ShipAPI ship, float amount) {
        for(WeaponAPI w:ship.getAllWeapons()){
            if(w.isDecorative()) continue;
            if(w.getSlot().isStationModule()) continue;
            if(w.getSlot().isHardpoint()&&(w.getSlot().getWeaponType().equals(WeaponType.MISSILE))){
                //if(Math.abs(w.getSlot().getAngle())<1){
                    if(w.getSprite()!=null){
                        w.getSprite().setSize(0, 0);
                    }
                    if(w.getBarrelSpriteAPI()!=null){
                        w.getBarrelSpriteAPI().setSize(0, 0);
                    }
                    if(w.getMissileRenderData()!=null&&!w.getMissileRenderData().isEmpty()){
                        for(MissileRenderDataAPI data:w.getMissileRenderData()){
                            data.getSprite().setSize(0,0);
                        }
                    }
                    if(w.getGlowSpriteAPI()!=null){
                        w.getGlowSpriteAPI().setSize(0, 0);
                    }
                    if(w.getUnderSpriteAPI()!=null){
                        w.getUnderSpriteAPI().setSize(0, 0);
                    }
                    w.ensureClonedSpec();
                    for(Vector2f fp:w.getSpec().getHardpointFireOffsets()){
                        fp.set(0,0);
                    }
                //}
            }
        }
        ship.getLargeHardpointCover().setSize(0, 0);
        ship.getMediumHardpointCover().setSize(0, 0);
        ship.getSmallHardpointCover().setSize(0, 0);
    }

}
