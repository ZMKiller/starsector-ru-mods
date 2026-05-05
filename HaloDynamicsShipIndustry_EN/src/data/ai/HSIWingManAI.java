package data.ai;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;

import data.kit.AjimusUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.targetSeeking;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CollisionGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HSIWingManAI implements ShipAIPlugin {
    private ShipAPI ship;
    private ShipAPI source;
    private IntervalUtil threatFinder = new IntervalUtil(0.2f, 0.3f);
    private CombatEntityAPI threat;
    private WeaponAPI beamThreatSource;
    private float r = (float) Math.random();
    private IntervalUtil updateListTracker = new IntervalUtil(0.05f, 0.1f);
    private List<CombatEntityAPI> hardAvoidList = new ArrayList<CombatEntityAPI>();
    public static float MAX_HARD_AVOID_RANGE = 200;
    public static float AVOID_RANGE = 50;
    public static float COHESION_RANGE = 100;
    private IntervalUtil threatHolderTracker = new IntervalUtil(2f, 3f);
    // private Map<ShipwideAIFlags,Object> flags = new HashMap<>();
    private ShipwideAIFlags flags = new ShipwideAIFlags();
    private ShipAPI potentialThreatHolder = null;
    private float elapsed = 0;

    private static final float EXPLOSION_DAMAGE = 500;

    public HSIWingManAI(ShipAPI ship, ShipAPI source) {
        this.ship = ship;
        this.source = source;
        doThreatAnalysis();
    }

    @Override
    public void advance(float amount) {
        if (source == null||!source.isAlive()) {
            source = AIUtils.getNearestAlly(ship);
        }
        if(source==null) return;
        flags.advance(amount);
        updateListTracker.advance(amount);
        if (updateListTracker.intervalElapsed()) {
            updateHardAvoidList();
        }
        threatFinder.advance(amount);
        if (threatFinder.intervalElapsed())
            doThreatAnalysis();
        threatHolderTracker.advance(amount);
        if (threatHolderTracker.intervalElapsed())
            threatHolderAnalysis();
        manuver();
        if (ship.getShield() != null && ship.getShield().isOff() && !ship.getFluxTracker().isOverloadedOrVenting()) {
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
        }
        if (ship.getFluxTracker().isOverloaded()) {
            explode();
            Global.getCombatEngine().applyDamage(ship, ship.getLocation(), 99999f, DamageType.OTHER, 0, true, false,
                    ship, false);

        }
        if (!Global.getCombatEngine().isPaused()) {
            elapsed += amount;
            if (elapsed >= 90) {
                explode();
                Global.getCombatEngine().applyDamage(ship, ship.getLocation(), 99999f, DamageType.OTHER, 0, true, false,
                        ship, false);
            }
        }
        float facing = VectorUtils.getAngle(ship.getLocation(), source.getMouseTarget());
        if(threat!=null){
            facing = VectorUtils.getAngle(ship.getLocation(), threat.getLocation());
        }
        else if(potentialThreatHolder!=null){
            facing = VectorUtils.getAngle(ship.getLocation(), potentialThreatHolder.getLocation());
        }
        facing+=180f;
        float rotation = MathUtils.getShortestRotation(ship.getFacing(), facing);
        if(Math.abs(rotation)>1.5f){
        if(rotation<0){
            ship.giveCommand(ShipCommand.TURN_LEFT, source.getMouseTarget(), 0);
        }else{
            ship.giveCommand(ShipCommand.TURN_RIGHT, source.getMouseTarget(), 0);
        }}else{
            ship.setAngularVelocity(ship.getAngularVelocity()*0.2f);
        }
    }

    public void updateHardAvoidList() {
        hardAvoidList.clear();

        CollisionGridAPI grid = Global.getCombatEngine().getAiGridShips();
        Iterator<Object> iter = grid.getCheckIterator(ship.getLocation(), MAX_HARD_AVOID_RANGE * 2f,
                MAX_HARD_AVOID_RANGE * 2f);
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof ShipAPI))
                continue;

            ShipAPI s = (ShipAPI) o;

            if (s.isFighter())
                continue;
            if (s.getOwner() == ship.getOwner())
                continue;
            hardAvoidList.add(ship);
        }
    }

    private void explode(){
        Global.getCombatEngine().spawnDamagingExplosion(AjimusUtils.createExplosionSpec(500,DamageType.ENERGY,
                new Color(255, 255, 255, 255),new Color(100, 100, 255, 175),125f,175f,0.1f), source, ship.getLocation(),false);
    }

    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public void forceCircumstanceEvaluation() {
        doThreatAnalysis();
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }

    @Override
    public ShipAIConfig getConfig() {
        return null;
    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public void setDoNotFireDelay(float amount) {

    }

    public void manuver() {
        if (source == null)
            return;

        // ShipAPI source = ship.getSource();
        CombatEngineAPI engine = Global.getCombatEngine();

        float avoidRange = AVOID_RANGE;

        float sin = (float) Math.sin(engine.getElapsedInContactWithEnemy() * 1f);
        float mult = 1f + sin * 0.25f;
        avoidRange *= mult;

        Vector2f total = new Vector2f();

        if (hasThreat()) {
            if (threat != null) {
                Vector2f tInter = AIUtils.getBestInterceptPoint(ship.getLocation(), ship.getMaxSpeedWithoutBoost(),
                        threat.getLocation(), threat.getVelocity());
                if (ship != null && tInter != null) {
                    float dist = Misc.getDistance(ship.getLocation(), tInter);
                    Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(ship.getLocation(), tInter));
                    float f = dist / 200f;
                    if (f > 1f)
                        f = 1f;
                    dir.scale(f * 3f);
                    Vector2f.add(total, dir, total);
                    avoidRange *= 3f;
                }
            }
            if (beamThreatSource != null) {
                Vector2f tInter = MathUtils.getNearestPointOnLine(ship.getLocation(), beamThreatSource.getFirePoint(0),
                        source.getLocation());
                if (ship != null && tInter != null) {
                    float dist = Misc.getDistance(ship.getLocation(), tInter);
                    Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(ship.getLocation(), tInter));
                    float f = dist / 200f;
                    if (f > 1f)
                        f = 1f;
                    dir.scale(f * 3f);
                    Vector2f.add(total, dir, total);
                    avoidRange *= 3f;
                }
            }
        }

        boolean hardAvoiding = false;
        for (CombatEntityAPI other : hardAvoidList) {
            float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
            float hardAvoidRange = other.getCollisionRadius() + avoidRange + 50f;
            if (dist < hardAvoidRange) {
                Vector2f dir = Misc
                        .getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(other.getLocation(), ship.getLocation()));
                float f = 1f - dist / (hardAvoidRange);
                dir.scale(f * 5f);
                Vector2f.add(total, dir, total);
                hardAvoiding = f > 0.5f;
            }
        }

        float sourceRejoin = source.getCollisionRadius() + source.getHullSize().ordinal() * 20f;

        float sourceRepel = source.getCollisionRadius() + source.getHullSize().ordinal() * 10f;
        float sourceCohesion = source.getCollisionRadius() + 200f;

        float dist = Misc.getDistance(ship.getLocation(), source.getLocation());
        if (dist > sourceRejoin) {
            Vector2f dir = Misc
                    .getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(ship.getLocation(), source.getLocation()));
            float f = dist / (sourceRejoin + 200f) - 1f;
            dir.scale(f * 0.5f);

            Vector2f.add(total, dir, total);
        }

        if (dist < sourceRepel) {
            Vector2f dir = Misc
                    .getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(source.getLocation(), ship.getLocation()));
            float f = 1f - dist / sourceRepel;
            dir.scale(f * 5f);
            Vector2f.add(total, dir, total);
        }

        if (dist < sourceCohesion && source.getVelocity().length() > 20f) {
            Vector2f dir = new Vector2f(source.getVelocity());
            Misc.normalise(dir);
            float f = 1f - dist / sourceCohesion;
            dir.scale(f * 1f);
            Vector2f.add(total, dir, total);
        }

        if (total.length() <= 0.05f) {
            float offset = r > 0.5f ? 90f : -90f;
            if (potentialThreatHolder != null) {
                float diff = MathUtils.getShortestRotation(
                        VectorUtils.getAngle(source.getLocation(), ship.getLocation()),
                        VectorUtils.getAngle(source.getLocation(), potentialThreatHolder.getLocation()));
                if (Math.abs(diff) > 60f) {
                    if (diff > 0) {
                        offset = -90f;
                        r = 0;
                    } else {
                        offset = 90f;
                        r = 1;
                    }
                }
            }
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                    Misc.getAngleInDegrees(ship.getLocation(), source.getLocation()) + offset);
            float f = 1f;
            dir.scale(f * 1f);
            Vector2f.add(total, dir, total);
        }

        if (total.length() > 0) {
            float dir = Misc.getAngleInDegrees(total);
            engine.headInDirectionWithoutTurning(ship, dir, 10000);
            ship.getEngineController().forceShowAccelerating();
        }
    }

    protected void doThreatAnalysis() {
        threat = null;
        beamThreatSource = null;
        float dmgMax = 0;
        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
            if (proj.getOwner() == 100 || proj.getOwner() == source.getOwner())
                continue;
            if (proj.getDamageAmount() < 200)
                continue;
            if (isAimingSource(proj, source) && (proj.getDamageAmount() > dmgMax||(proj.getDamageAmount()==dmgMax&&Math.random()>0.5))
                    && MathUtils.getDistanceSquared(source.getLocation(), proj.getLocation()) <= 4000000) {
                threat = proj;
            }
        }
        if (threat != null)
            return;
        for (BeamAPI beam : Global.getCombatEngine().getBeams()) {
            if (beam.getDamageTarget() instanceof ShipAPI && ((ShipAPI) beam.getDamageTarget()) == source) {
                if (beam.getDamage().getDamage() >= 500f && !beam.getWeapon().isBurstBeam()) {
                    beamThreatSource = beam.getWeapon();
                    return;
                }
            }
        }
        if (beamThreatSource != null)
            return;
        threat = MagicTargeting.pickTarget(source, targetSeeking.FULL_RANDOM, 1200, 360, 100, 0, 0, 0, 0,
                false);
        return;
    }

    protected void threatHolderAnalysis() {
        if (potentialThreatHolder != null) {
            if (potentialThreatHolder.isAlive()
                    && MathUtils.getDistance(potentialThreatHolder.getLocation(), source.getLocation()) <= 2500f) {
                return;
            } else {
                potentialThreatHolder = null;
            }
        }
        for (ShipAPI s : AIUtils.getNearbyEnemies(source, 2000f)) {
            if (s.getOwner() == 100 || s.getOwner() == source.getOwner()) {
                continue;
            }
            float sumTotal = 0;
            for (WeaponAPI w : s.getUsableWeapons()) {
                if (w.getDamage().getDamage() >= 500f && w.getCooldownRemaining() <= 2f) {
                    sumTotal += w.getDamage().getDamage();
                }
            }
            if (Math.random() < sumTotal / sumTotal + 4000f) {
                potentialThreatHolder = s;
            }
        }
    }

    protected boolean hasThreat() {
        return beamThreatSource != null || threat != null;
    }

    protected boolean isAimingSource(DamagingProjectileAPI proj, ShipAPI source) {
        if (proj.isFading() || proj.isExpired())
            return false;
        if (proj instanceof MissileAPI) {
            MissileAPI m = (MissileAPI) proj;
            if (m.isGuided()) {
                if (m.getAI() instanceof GuidedMissileAI) {
                    return ((GuidedMissileAI) m.getAI()).getTarget() == source;
                }
            }
        }
        Vector2f closest = MathUtils.getNearestPointOnLine(source.getLocation(), proj.getLocation(),
                Vector2f.add(proj.getLocation(), (Vector2f) (new Vector2f(proj.getVelocity()).scale(1500f)), null));
        return MathUtils.getDistance(closest, source.getLocation()) <= source.getShieldRadiusEvenIfNoShield();
    }
}
