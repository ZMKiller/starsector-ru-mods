package data.ai.HSISystemAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class HSITSControlAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;

    private IntervalUtil threatChecker = new IntervalUtil(0.05f, 0.1f);
    private float maxRangeSelf = 0;
    private float minRangeSelf = 1000;

    private Vector2f weightedDefensiveDir = new Vector2f();


    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        // this.flags = flags;
        this.engine = engine;
        this.system = ship.getPhaseCloak();
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            float range = weapon.getRange();
            if (maxRangeSelf < range)
                maxRangeSelf = range;
            if (minRangeSelf > range)
                minRangeSelf = range;
        }
    }

    // private float sinceLast = 0f;

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (system.getCooldownRemaining() > 0)
            return;
        if (system.isOutOfAmmo())
            return;
        if (system.isActive())
            return;
        boolean shouldDo = false;
        boolean defensive = true;
        threatChecker.advance(amount);
        if (threatChecker.intervalElapsed()) {
            shouldDo = judgeThreats();
            if(!shouldDo){
                shouldDo = judgeOffensive();
                defensive = false;
            }
        }
        if (shouldDo) {
            if(defensive){
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS,1f,Vector2f.add(ship.getLocation(),weightedDefensiveDir,null));
            }else{
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS,1f,ship.getShipTarget()==null?ship.getMouseTarget():ship.getShipTarget().getLocation());
            }
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
        }
    }

    protected boolean judgeThreats() {
        //if (shield.getShield().getCurrent() <= 0)
        //    return false;
        weightedDefensiveDir = new Vector2f();
        float potential = 0;
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            if (proj.getOwner() == ship.getOwner())
                continue;
            if (MathUtils.getDistanceSquared(proj.getLocation(), ship.getLocation()) > 360000)
                continue;
            potential += ((proj.getDamageType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                        * proj.getDamage().getDamage();
            Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(),proj.getLocation());
            VectorUtils.rotate(dir,70f+20f*(float)Math.random(),dir);
            dir.scale(proj.getDamageAmount()/10f);
            Vector2f.add(weightedDefensiveDir,dir,weightedDefensiveDir);
        }

        for(BeamAPI beam:engine.getBeams()){
            if(beam.getSource().getOwner()==ship.getOwner()) continue;
            Vector2f nearest = MathUtils.getNearestPointOnLine(ship.getLocation(),beam.getFrom(),beam.getTo());
            if(MathUtils.getDistance(ship.getLocation(),nearest)<ship.getCollisionRadius()*1.2f){
                potential+=beam.getDamage().getDamage()/2f;
            }
            Vector2f dir = VectorUtils.getDirectionalVector(nearest,ship.getLocation());
            dir.scale(beam.getDamage().getDamage()/20f);
            Vector2f.add(weightedDefensiveDir,dir,weightedDefensiveDir);
        }
        float lim = 4000f;
        if(HSITurbulanceShieldListenerV2.hasShield(ship)){
            HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            if(shield.getShield().getShieldLevel()<=0.4f){
                lim  *=0.4f;
            }
            if(shield.getShield().getShieldLevel()<=0.2f){
                lim *=0.2f;
            }

        }
        return potential >= lim;
    }

    protected boolean judgeOffensive(){
        if(ship.getShipTarget()==null) return false;
        if(ship.getFluxTracker().isOverloadedOrVenting()) return false;
        if(ship.getHullLevel()<0.5f) return false;
        if(ship.getPhaseCloak().getAmmo()>2) return true;
        return ship.getFluxLevel() < 0.7f && ship.getShipTarget().getFluxLevel() >= ship.getFluxLevel() - 0.1f &&ship.getShipTarget().getFluxLevel()>0.5f&&
                MathUtils.getDistance(ship.getLocation(), ship.getShipTarget().getLocation()) <
                        (ship.getCollisionRadius() * 0.9f + ship.getShipTarget().getCollisionRadius() * 0.9f) + maxRangeSelf * 0.8f;
    }
}
