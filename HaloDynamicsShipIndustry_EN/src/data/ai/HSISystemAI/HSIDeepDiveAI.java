package data.ai.HSISystemAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.hullmods.HSITurbulanceShieldListenerV2;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.drones.PIDController;

import java.util.ArrayList;
import java.util.List;

public class HSIDeepDiveAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;

    private IntervalUtil threatChecker = new IntervalUtil(0.05f, 0.15f);

    private IntervalUtil weaponChecker = new IntervalUtil(0.8f,1.0f);

    private Vector2f weightedDefensiveDir = new Vector2f();

    private boolean shouldDo = false;

    private List<WeaponAPI> weaponPotentialHit = new ArrayList<>();

    //private PIDController controller = new PIDController(2f, 2f, 6f, 0.5f);

    private int r = 0;

    private float keep = 0;


    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        // this.flags = flags;
        this.engine = engine;
        this.system = ship.getPhaseCloak();
        r = (Math.random()>0.5f)?-1:1;
        if(ship.getShipAI()!=null){
            ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        //Global.getLogger(this.getClass()).info(ship.getLocation()+"||"+missileDangerDir+"||"+collisionDangerDir);
        threatChecker.advance(amount);
        //Global.getCombatEngine().headInDirectionWithoutTurning(ship,VectorUtils.getFacing(weightedDefensiveDir),ship.getMaxSpeed());
        keep = Math.max(0,keep-amount);
        weaponChecker.advance(amount);
        if(weaponChecker.intervalElapsed()){
            weaponPotentialHit.clear();
            for(ShipAPI e: AIUtils.getNearbyEnemies(ship,ship.getCollisionRadius()+10000f)){
                for(WeaponAPI weapon:ship.getAllWeapons()){
                    if(weapon.isDecorative()) continue;
                    if(weapon.isDisabled()) continue;
                    if(weapon.getRange()*weapon.getRange()> 1.21f*MathUtils.getDistanceSquared(ship.getLocation(),e.getLocation())) continue;
                    if(Math.abs(weapon.distanceFromArc(ship.getLocation()))>0) continue;
                    weaponPotentialHit.add(weapon);
                }
            }
        }
        if(threatChecker.intervalElapsed()) {
            weightedDefensiveDir = new Vector2f();
            if (collisionDangerDir != null) {
                Vector2f.add(weightedDefensiveDir, collisionDangerDir, weightedDefensiveDir);
            }
            boolean isActive = system.isOn();
            float potential = 0;
            for (DamagingProjectileAPI proj : engine.getProjectiles()) {
                if (proj.getOwner() == ship.getOwner())
                    continue;
                float dist = MathUtils.getDistanceSquared(proj.getLocation(), ship.getLocation());
                if (dist / ((proj.getMoveSpeed() == 0) ? 1 : proj.getMoveSpeed()) > ship.getPhaseCloak().getCooldown())
                    continue;
                if (dist < 2.5f * ship.getCollisionRadius() * ship.getCollisionRadius()) {
                    potential += ((proj.getDamageType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                            * proj.getDamage().getDamage();
                }
                Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(), proj.getLocation());
                VectorUtils.rotate(dir, 70f + 20f * (float) Math.random() * r, dir);
                dir.scale(proj.getDamageAmount() / 10f);
                Vector2f.add(weightedDefensiveDir, dir, weightedDefensiveDir);
            }

            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getSource().getOwner() == ship.getOwner()) continue;
                Vector2f nearest = MathUtils.getNearestPointOnLine(ship.getLocation(), beam.getFrom(), beam.getTo());
                if (MathUtils.getDistance(ship.getLocation(), nearest) < ship.getCollisionRadius() * 1.2f) {
                    potential += beam.getDamage().getDamage() / 2f;
                }
                Vector2f dir = VectorUtils.getDirectionalVector(nearest, ship.getLocation());
                dir.scale(beam.getDamage().getDamage() / 10f);
                Vector2f.add(weightedDefensiveDir, dir, weightedDefensiveDir);
            }

            for(WeaponAPI weapon:weaponPotentialHit){
                float mult = 0.1f;
                if(weapon.isBeam()) mult = 0.5f;
                float turnTime = Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(),VectorUtils.getAngle(weapon.getLocation(),ship.getLocation())))/MathUtils.clamp(weapon.getTurnRate(),1f,100f) ;
                potential+=MathUtils.clamp(5f-turnTime,0f,5f)*weapon.getDamage().getDamage()*mult;

                Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(), weapon.getLocation());
                VectorUtils.rotate(dir, 70f + 20f * (float) Math.random() * r, dir);
                dir.scale(weapon.getDamage().getDamage() / 10f);
                Vector2f.add(weightedDefensiveDir, dir, weightedDefensiveDir);
            }

            float lim = Math.min(700f, ship.getMaxHitpoints() * ship.getHullLevel());

            if (HSITurbulanceShieldListenerV2.hasShield(ship)) {
                HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
                if (shield.getShield().getShieldLevel() > 0.8f) {
                    lim *= 1.7f;
                }
                if (!isActive) {
                    if (shield.getShield().getShieldLevel() < 0.5f && shield.getShield().isShieldRegenBlocked()) {
                        lim *= 0f;
                        keep = shield.getShield().getShieldRegenTime()+(shield.getShield().getShieldCap()-shield.getShield().getCurrent())/shield.getShield().getBaseShieldRegen()*0.5f;
                    }
                } else {
                    if (shield.getShield().getShieldLevel() < 0.95f) {
                        lim *= 0f;
                    }
                }
            }
            if (ship.getEngineController().isFlamedOut()) {
                lim *= 0.1f;
            }
            shouldDo = potential >= lim;
            int totalScale = 1;
            float cdScale = 1.0f;
            float rprScale = 1.0f;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.isDecorative()) continue;
                totalScale += (2 * weapon.getSize().ordinal());
                if (weapon.getCooldownRemaining() > 2f) {
                    cdScale += (1.5f * weapon.getSize().ordinal());
                }
                if (weapon.isDisabled()) {
                    rprScale += (2f * weapon.getSize().ordinal());
                }
            }
            shouldDo = shouldDo || (rprScale / totalScale > 0.7f) ;
            if(shouldDo) keep = 0.5f;
        }

        shouldDo = shouldDo|| keep > 0;

        shouldDo = ship.getHardFluxLevel()<0.9f&&shouldDo;

        if (shouldDo) {
            if(!system.isOn()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
                keep = 2.5f;
            }
        }else{
            if(system.isOn()){
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
            }
        }
        //Global.getLogger(this.getClass()).info(shouldDo+"|"+rprScale/totalScale+"|"+keep);
        //Global.getCombatEngine().headInDirectionWithoutTurning(ship,VectorUtils.getFacing(weightedDefensiveDir),ship.getMaxSpeed());
    }
}
