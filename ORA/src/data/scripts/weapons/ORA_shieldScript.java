/*
    By Tartiflette
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicAnim;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_shieldScript implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false,noShield=false;
    private ShipAPI SHIP;
    private ShieldAPI SHIELD;
    private Vector2f SHIELD_CENTER;
    private float SHIELD_RADIUS;
    private float MAX_OFFSET;
    private float SHIELD_ARC;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused() || noShield)return;
        
        if(!runOnce){
            runOnce=true;
            SHIP=weapon.getShip();
            if(SHIP.getShield()==null || SHIP.getShield().getType()==ShieldType.FRONT){
                noShield=true;
            } else {
                SHIELD = SHIP.getShield();
                SHIELD_CENTER = SHIP.getShieldCenterEvenIfNoShield();
                SHIELD_RADIUS = SHIELD.getRadius();
                MAX_OFFSET = SHIP.getCollisionRadius();
                SHIELD_ARC = SHIELD.getArc()/2;
            }
            return;
        }
        
        //calculate shield deformation
        float direction = MathUtils.getShortestRotation( SHIP.getFacing(), SHIELD.getFacing());
//        boolean side = (direction>0);
        
        float smoothDeform = MagicAnim.smoothReturnNormalizeRange(
                Math.abs(direction),
                0,
                180
        );
//        deform = (float)FastTrig.sin(Math.abs(deform)*0.0174533f);

        SHIELD.setRadius(SHIELD_RADIUS + smoothDeform*MAX_OFFSET);
        
        Vector2f center = MathUtils.getPointOnCircumference(new Vector2f(), smoothDeform*MAX_OFFSET*1.33f, 180+direction);
        SHIELD.setCenter(center.x,center.y);
        SHIP.getMutableStats().getShieldTurnRateMult().modifyMult("ORA_shield", 1+smoothDeform);
//        if(side){
////            SHIELD.setCenter(SHIELD_CENTER.x + deform*MAX_OFFSET, SHIELD_CENTER.y);
//            SHIELD.setCenter( 0, -smoothDeform*MAX_OFFSET*1.33f);
//        } else {
////            SHIELD.setCenter(SHIELD_CENTER.x - deform*MAX_OFFSET, SHIELD_CENTER.y);
//            SHIELD.setCenter( 0, smoothDeform*MAX_OFFSET*1.33f);
//        }
//        
    }
}