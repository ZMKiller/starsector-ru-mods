package data.ai.HSIWeaponAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.ai.HSIThreatSharedData;
import data.kit.HSIExtraAmmoTracker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.EnumSet;

public class HSIMissilePDAI implements AutofireAIPlugin {
    public static final String DATA_KEY = "HSIThreatSharedData";
    private Vector2f targetLoc = null;
    private WeaponAPI weapon;
    private ShipAPI ship;
    private MissileAPI targetMissile;
    private ShipAPI targetShip;
    private boolean shouldFire = false;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private TimeoutTracker<MissileAPI> guidingMissile = new TimeoutTracker<MissileAPI>();

    private IntervalUtil timer = new IntervalUtil(0.05f,0.2f);
    private boolean isBallistic = false;
    private boolean hasExtraMissile = false;
    private final float MSL_RANGE;
    private final float CONE = 60f;
    private final int MAX_TRACE;
    private float MSL_ANGLE = 60f;


    public HSIMissilePDAI(WeaponAPI weapon) {
        this.weapon = weapon;
        ship = weapon.getShip();
        MSL_RANGE = weapon.getRange();
        MAX_TRACE = (weapon.getSize().ordinal()+1)*2;
    }

    public void advance(float amount) {
        shouldFire = false;
        if (engine.isPaused())
            return;
        // Global.getLogger(this.getClass()).info("Arc:"+weapon.getArcFacing());
        // think.advance(amount);
        // if(!think.intervalElapsed()) return;

        guidingMissile.advance(amount);
        if(weapon.isDisabled()) return;
        if(ship.getFluxTracker().isOverloadedOrVenting()) return;
        timer.advance(amount);
        if(!timer.intervalElapsed()) return;
        for(MissileAPI m:guidingMissile.getItems()){
            if(!engine.isEntityInPlay(m)||m.isFading()||m.isFizzling()){
                guidingMissile.remove(m);
            }
        }
        if (guidingMissile.getItems().size() < MAX_TRACE) {
            if (weapon.getCooldownRemaining() <= 0 && weapon.getAmmo() > 0) {
                targetMissile = null;
                targetShip = null;
                float missileWeight = 0;
                MissileAPI currMissile = null;
                for (MissileAPI missile : engine.getMissiles()) {
                    if (missile.getOwner() == ship.getOwner())
                        continue;
                    if (missile.isFlare())
                        continue;
                    if (missile.isFizzling() || missile.isFading())
                        continue;
                    if (MathUtils.getDistanceSquared(weapon.getLocation(), missile.getLocation()) > MSL_RANGE
                            * MSL_RANGE)
                        continue;
                    if (weapon.distanceFromArc(missile.getLocation()) > Math.max(0, (CONE - weapon.getArc()) / 2f))
                        continue;
                    HSIThreatSharedData data = HSIThreatSharedData.getInstance(missile);
                    float dmg = data.getDMGToThis();
                    if (dmg >= 1.1f * missile.getHitpoints()) {
                        continue;
                    }
                    if (!missile.isGuided()) {
                        Vector2f closest = MathUtils.getNearestPointOnLine(ship.getLocation(), missile.getLocation(),
                                Vector2f.add(missile.getLocation(),
                                        (Vector2f) (new Vector2f(missile.getVelocity()).scale(1000f)), null));
                        if (MathUtils.getDistanceSquared(closest, ship.getLocation()) >
                                ship.getShieldRadiusEvenIfNoShield()
                                        * ship.getShieldRadiusEvenIfNoShield())
                            continue;
                    }
                    float weight = missile.getDamageAmount()
                            / MathUtils.getDistance(weapon.getLocation(), missile.getLocation());
                    if (missile.isMirv())
                        weight *= 1.5f;
                    if (missile.getSpec().getBehaviorJSON() != null
                            && missile.getSpec().getBehaviorJSON().has("triggerDistance"))
                        weight *= 2f;

                    //weight*=(float) (0.5f+Math.random()*0.5);
                    if (weight > missileWeight) {
                        missileWeight = weight;
                        currMissile = missile;
                    }
                }
                float fighterWeight = 0;
                ShipAPI currFighter = null;
                for (ShipAPI fighter : engine.getShips()) {
                    // if (!fighter.isFighter())
                    // continue;
                    if (weapon.getSpec().getAIHints().contains(WeaponAPI.AIHints.PD_ONLY) && !fighter.isFighter())
                        continue;
                    if (fighter.getOwner() == ship.getOwner() || fighter.getOwner() == 100)
                        continue;
                    float distSqr = MathUtils.getDistanceSquared(weapon.getLocation(), fighter.getLocation());
                    if (distSqr > MSL_RANGE * MSL_RANGE)
                        continue;
                    if (weapon.distanceFromArc(fighter.getLocation()) > Math.max(0, (CONE - weapon.getArc()) / 2f))
                        continue;
                    if (fighter.getCustomData().containsKey(DATA_KEY)) {
                        HSIThreatSharedData data = (HSIThreatSharedData) fighter.getCustomData().get(DATA_KEY);
                        float dmg = data.getDMGToThis();
                        if (dmg >= 1.1f * (fighter.getHitpoints() + fighter.getArmorGrid().getArmorRating() * 2f))
                            continue;
                    }

                    float damagePotential = 0f;
                    if (fighter.getWing() != null) {
                        switch (fighter.getWing().getRole()) {
                            case BOMBER:
                                for (WeaponAPI wpn : fighter.getUsableWeapons()) {
                                    if (wpn.getSpec().getAIHints()
                                            .containsAll(EnumSet.of(WeaponAPI.AIHints.DANGEROUS, WeaponAPI.AIHints.STRIKE)))
                                        damagePotential += wpn.getDerivedStats().getBurstDamage();
                                }
                                damagePotential *= 1.25f;
                                break;
                            case SUPPORT:
                                for (WeaponAPI wpn : fighter.getUsableWeapons()) {
                                    if (wpn.getRange() * wpn.getRange() < distSqr)
                                        damagePotential += wpn.getDerivedStats().getDps();
                                }
                                damagePotential *= 0.75f;
                                break;
                            default:
                                for (WeaponAPI wpn : fighter.getUsableWeapons()) {
                                    if (wpn.getSpec().getAIHints()
                                            .containsAll(EnumSet.of(WeaponAPI.AIHints.DANGEROUS, WeaponAPI.AIHints.STRIKE))) {
                                        damagePotential += wpn.getDerivedStats().getBurstDamage();
                                    } else {
                                        damagePotential += wpn.getDerivedStats().getDps();
                                    }
                                }
                                break;
                        }
                    } else {
                        damagePotential = 2000f / ship.getArmorGrid().getArmorRating();
                    }
                    float weight = (float) (damagePotential / Math.sqrt(distSqr));
                    //weight*=(float) (0.5f+Math.random()*0.5);
                    if (weight > fighterWeight) {
                        currFighter = fighter;
                        fighterWeight = weight;
                    }
                }
                CombatEntityAPI result = null;
                if (fighterWeight > missileWeight) {
                    result = currFighter;
                    targetShip = currFighter;
                    targetMissile = null;
                } else {
                    result = currMissile;
                    targetShip = null;
                    targetMissile = currMissile;
                }
                if (result != null) {
                        shouldFire = true;
                        targetLoc = AIUtils.getBestInterceptPoint(weapon.getFirePoint(0), weapon.getProjectileSpeed(),
                                result.getLocation(), result.getVelocity());
                }
            }
        }
    }

    public boolean shouldFire() {
        if ((targetShip == null && targetMissile == null)||targetLoc==null)
            return false;
        return shouldFire && (Math.abs((MathUtils.getShortestRotation(this.weapon.getCurrAngle(),
                VectorUtils.getAngleStrict(this.weapon.getFirePoint(0),
                        targetLoc)))) <= ( MSL_ANGLE)
                || this.weapon.getSlot().isHardpoint());
    }

    /**
     * Tells the weapon AI to reconsider whether it should be firing, before it
     * decides it should fire again.
     *
     * Called when a group is toggled on/off.
     */
    public void forceOff() {
        this.shouldFire = false;
    }

    /**
     * @return location to aim at, with target leading if applicable. Can be null if
     *         the weapon has no target/isn't trying to aim anywhere.
     */
    public Vector2f getTarget() {
        if (targetShip != null || targetMissile != null)
            return targetLoc;
        return null;
    }

    /**
     * @return current target, if it's a ship. null otherwise.
     */
    public ShipAPI getTargetShip() {
        return targetShip;
    }

    /**
     * @return the weapon that this AI is controlling. That means the plugin should
     *         hold on to it when it's passed in in
     *         ModPlugin.pickWeaponAutofireAI().
     */
    public WeaponAPI getWeapon() {
        return weapon;
    }

    public MissileAPI getTargetMissile() {
        return targetMissile;
    }

    public void addGuidingMissile(MissileAPI proj){
        guidingMissile.add(proj,proj.getMaxFlightTime());
    }

}
