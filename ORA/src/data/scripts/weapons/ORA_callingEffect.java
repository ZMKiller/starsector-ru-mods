//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicTargeting;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_callingEffect implements EveryFrameWeaponEffectPlugin{
    
    private boolean runOnce=false;
    private final IntervalUtil timer = new IntervalUtil(0.2f,0.2f);
    ShipAPI ship;
    MissileAPI secondaryTarget=null;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(engine.isPaused()){return;}
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
        }
        //Only check firing weapons
        if(!ship.isAlive() && weapon.getCooldownRemaining()!=0){
            return;
        }

        timer.advance(amount);
        if(timer.intervalElapsed()){

            if(
                    ship.isPhased()
                    ||
                    ship.isHoldFire()
                    ||
                    ship.getFluxTracker().isOverloaded()
                    ||
                    ship.getFluxTracker().isVenting()
                    ||
                    weapon.isDisabled()
                    ){ 
                return;
            }

            
            
            if(
                    secondaryTarget==null //no target
                    || 
                    !engine.isEntityInPlay(secondaryTarget) //target is dead
                    || 
                    secondaryTarget.isFading() //target is dead
                    || 
                    secondaryTarget.isFizzling() //target is dead
                    || 
                    Math.abs(
                            MathUtils.getShortestRotation(
                                    weapon.getCurrAngle(),
                                    VectorUtils.getAngle(
                                            weapon.getLocation(),
                                            secondaryTarget.getLocation()
                                    )
                            )
                    )>50 //target is out of arc
                    ||
                    !MathUtils.isWithinRange(secondaryTarget, weapon.getLocation(), 500)
                    ){

                secondaryTarget=null;
                // find a target
                secondaryTarget = (MissileAPI) MagicTargeting.randomMissile(
                        ship,
                        MagicTargeting.missilePriority.DAMAGE_PRIORITY,
                        weapon.getLocation(),
                        weapon.getCurrAngle(),
                        90,
                        500
                );
                return;
            }

            //if target found
            if(secondaryTarget!=null){
                Vector2f lead;
                lead = AIUtils.getBestInterceptPoint(weapon.getLocation(), 1000, secondaryTarget.getLocation(), secondaryTarget.getVelocity());

                if(lead==null){
                    lead=secondaryTarget.getLocation();
                }

                engine.spawnProjectile(
                        ship,
                        weapon,
                        "ora_choir",
                        weapon.getLocation(),
                        VectorUtils.getAngle(
                                weapon.getLocation(),
                                lead
                        ),
                        ship.getVelocity()
                );

                Global.getSoundPlayer().playSound(
                        "ora_choir",
                        1,
                        1,
                        weapon.getLocation(),
                        ship.getVelocity()
                );

                engine.addHitParticle(
                        weapon.getLocation(),
                        ship.getVelocity(),
                        10+12*(float)Math.random(),
                        1,
                        0.12f,
                        new Color(100,255,200,255)
                );
            }
        }
    }
}