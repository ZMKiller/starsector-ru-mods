package data.ai;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSILuminarFlowAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;
    // private ShipwideAIFlags flags;
    // private HSITurbulanceShieldListenerV2 shield = null;
    // private List<WeaponAPI> threats = new ArrayList<>();
    // private IntervalUtil threatUpdater = new IntervalUtil(0.2f, 0.3f);
    private IntervalUtil threatChecker = new IntervalUtil(0.15f, 0.25f);
    private HSITurbulanceShieldListenerV2 shield;
    private float maxRangeSelf = 0;
    private float minRangeSelf = 1000;
    private float lastShield = 0;

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
        if (shield == null) {
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            lastShield = shield.getShield().getCurrent();
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
        if (shield == null) {
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
        }
        if (shield == null)
            return;
        if (shield.getShield().getCurrent() <= 0)
            return;
        boolean shouldDo = false;
        threatChecker.advance(amount);
        if (threatChecker.intervalElapsed()) {
            shouldDo = judgeThreats();
            lastShield = shield.getShield().getCurrent();
        }
        
        if (shouldDo) {
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
        }
    }

    protected boolean judgeThreats() {
        if (shield == null) {
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
        }
        if (shield == null)
            return false;
        //if(lastShield>=shield.getShield().getCurrent()*1.1f){
         //   return true;
        //}
        //if(shield.getShield().isShieldRegenBlocked()&&(shield.getShield().getCurrent()/shield.getShield().getShieldCap())<=0.4f){
        //    return true;
        //}
        //if (shield.getShield().getCurrent() <= 0)
        //    return false;
        float potential = 0;
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            if (proj.getOwner() == ship.getOwner())
                continue;
            if (MathUtils.getDistanceSquared(proj.getLocation(), ship.getLocation()) > 1000000)
                continue;
            if (proj instanceof MissileAPI && ((MissileAPI) proj).isGuided()) {
                potential += ((proj.getDamageType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                        * proj.getDamage().getDamage();
            } else if (Math.abs(MathUtils.getShortestRotation(VectorUtils.getFacing(proj.getVelocity()),
                    VectorUtils.getAngle(proj.getLocation(), ship.getLocation()))) < 60f) {
                potential += ((proj.getDamageType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                        * proj.getDamage().getDamage();
            }
        }

        for(BeamAPI beam:engine.getBeams()){
            if(beam.getSource().getOwner()==ship.getOwner()) continue;
            Vector2f nearest = MathUtils.getNearestPointOnLine(ship.getLocation(),beam.getFrom(),beam.getTo());
            if(MathUtils.getDistance(ship.getLocation(),nearest)<ship.getCollisionRadius()*1.2f){
                potential+=beam.getDamage().getDamage()/2f;
            }
        }
        if(AIUtils.getNearbyEnemies(ship, 800f).size()>2){
            potential+=1000;
        }
        return potential >= Math.min(Math.min(500f, shield.getShield().getCurrent() * 0.1f),ship.getHitpoints()*2f);
    }
}
