package data.shipsystems.scripts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class HSISubSpaceWalker extends BaseShipSystemScript {
    private ShipAPI ship;
    private boolean once = false;
    private static final float SUBSPACE_MAX_SPEED_MULT = 100f;
    private static final float SUBSPACE_COLLISION_MULT = 0.15f;
    private static final float ROF_MULT = 100f;
    // private static final float SUBSPACE_DAMAGE_REDUCTION = -0.33f;
    private static final float ALPHA_MULT = 0.5f;

    private static final float TIME_MULT = 1f;
    private float collisionRadius = 0f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        if (collisionRadius == 0)
            collisionRadius = ship.getCollisionRadius();
        ShipSystemAPI cloak = ship.getPhaseCloak();
        if (cloak == null)
            cloak = ship.getSystem();
        /*
         * if (cloak != null) {
         * ((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(effectLevel);
         * }
         */
        float judgeCollision = ((1 - effectLevel) * (1 - SUBSPACE_COLLISION_MULT) + SUBSPACE_COLLISION_MULT)
                * collisionRadius;
        if (state == State.IDLE || state == State.COOLDOWN) {
            if (ship.getCollisionClass() == CollisionClass.NONE && ship.isFighter())
                ship.setCollisionClass(CollisionClass.FIGHTER);
            if (ship.getCollisionClass() == CollisionClass.NONE && !ship.isFighter())
                ship.setCollisionClass(CollisionClass.SHIP);
            unapply(stats, id);
            return;
        }
        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {
            if (ship.getCollisionClass() != CollisionClass.NONE)
                ship.setCollisionClass(CollisionClass.NONE);
            
            /*
             * Iterator<Object> iter =
             * engine.getAllObjectGrid().getCheckIterator(ship.getLocation(), judgeCollision
             * * 2f,
             * judgeCollision * 2f);
             * while (iter.hasNext()) {
             * Object curr = iter.next();
             * if (!(curr instanceof CombatEntityAPI))
             * continue;
             * if (curr == ship)
             * continue;
             * 
             * CombatEntityAPI entity = (CombatEntityAPI) curr;
             * 
             * float dist = Misc.getDistance(entity.getLocation(), ship.getLocation());
             * 
             * if (entity instanceof DamagingProjectileAPI) {
             * DamagingProjectileAPI proj = (DamagingProjectileAPI) entity;
             * if (proj.getSource() == ship)
             * continue;
             * 
             * if (dist < judgeCollision) {
             * engine.applyDamage(ship, proj.getLocation(), proj.getDamageAmount(),
             * proj.getDamageType(),
             * proj.getEmpAmount(), true, false, proj.getSource());
             * engine.removeEntity(proj);
             * }
             * }
             * }
             * 
             * for (BeamAPI beam : engine.getBeams()) {
             * Vector2f closestP = Misc.closestPointOnLineToPoint(beam.getFrom(),
             * beam.getRayEndPrevFrame(),
             * ship.getLocation());
             * float dist = Misc.getDistance(ship.getLocation(), closestP);
             * if (dist < judgeCollision&&beam.getSource()!=ship) {
             * beam.getRayEndPrevFrame().set(closestP);
             * engine.applyDamage(ship, closestP,
             * beam.getDamage().getDamage() *
             * Global.getCombatEngine().getElapsedInLastFrame(),
             * beam.getDamage().getType(),
             * 0f, true, false, beam.getSource());
             * }
             * 
             * }
             */
            ship.setExtraAlphaMult(Math.max(0, 1 - ((ship.getOwner() == 0) ? ALPHA_MULT : 0.7f) * effectLevel));
            ship.setApplyExtraAlphaToEngines(true);
            // ship.setForceHideFFOverlay(true);
            // ship.setDrone(true);
            // if(engine.getPlayerShip()!=null&&engine.getPlayerShip().getShipTarget()==ship){
            // engine.getPlayerShip().setShipTarget(null);
            // }
            /*if (effectLevel >=0.5f) {
                stats.getHullDamageTakenMult().modifyMult(id, 0f);
                ship.setDrone(true);
            }*/
            /*stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f);
            stats.getAcceleration().modifyPercent(id,
                    effectLevel * SUBSPACE_MAX_SPEED_MULT * ship.getFluxTracker().getHardFlux() / ship.getMaxFlux());
            stats.getDeceleration().modifyPercent(id,
                    effectLevel * SUBSPACE_MAX_SPEED_MULT * ship.getFluxTracker().getHardFlux() / ship.getMaxFlux());
            stats.getMaxSpeed().modifyPercent(id,
                    effectLevel * SUBSPACE_MAX_SPEED_MULT * ship.getFluxTracker().getHardFlux() / ship.getMaxFlux());
            stats.getMaxTurnRate().modifyPercent(id,
                    effectLevel * SUBSPACE_MAX_SPEED_MULT * ship.getFluxTracker().getHardFlux() / ship.getMaxFlux());
            stats.getTurnAcceleration().modifyPercent(id,
                    effectLevel * SUBSPACE_MAX_SPEED_MULT * ship.getFluxTracker().getHardFlux() / ship.getMaxFlux());
            stats.getBallisticRoFMult().modifyPercent(id,
                    effectLevel * ROF_MULT);
            stats.getEnergyRoFMult().modifyPercent(id,
                    effectLevel * ROF_MULT);
            stats.getMissileRoFMult().modifyPercent(id,
                    effectLevel * ROF_MULT);
            stats.getFluxDissipation().modifyMult(id, 2f);*/
            /*if (state == State.ACTIVE) {
                for (int i = 0; i < ship.getFluxLevel() * 5 + 1; i++) {
                    spawnNebulaEffect(engine, judgeCollision, effectLevel);
                }
            }*/
            stats.getTimeMult().modifyMult(id, 1f+TIME_MULT*effectLevel);
            if(Global.getCombatEngine().getPlayerShip() == ship){
                Global.getCombatEngine().getTimeMult().modifyMult(id,1f/(1f+TIME_MULT*effectLevel));
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        ShipSystemAPI cloak = ship.getPhaseCloak();
        if (cloak == null)
            cloak = ship.getSystem();
        // if (cloak != null) {
        // ((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(0f);
        // }
        // ship.setAlphaMult(1f);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        //stats.getHullDamageTakenMult().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getZeroFluxMinimumFluxLevel().unmodify(id);
        stats.getTimeMult().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);
        // ship.setForceHideFFOverlay(false);
        //ship.setDrone(false);
    }

    private void spawnNebulaEffect(CombatEngineAPI engine, float judgeCollision, float effectLevel) {
        Vector2f loc = Misc.getPointAtRadius(ship.getLocation(),
                (float) (judgeCollision * (Math.random() * 0.2f + 0.8f)));
        Vector2f vel = Misc.getUnitVector(ship.getLocation(), loc);
        vel.scale(effectLevel * ship.getCollisionRadius() / 2f * (float) (0.5f + Math.random() * 0.5f));
        Vector2f.add(vel, ship.getVelocity(), vel);
        engine.addNebulaParticle(loc, vel, 2f, (float) (2f + 0.5f * Math.random()), (float) (Math.random() * 1f + 1f),
                (float) (Math.random() * 1f + 1f),
                0.7f, ship.getOverloadColor(), false);
        // engine.addNebulaParticle(loc, vel, 2f, (float) (3f + 1f * Math.random()), 1f
        // + effectLevel, 0.05f,
        // 0.7f, ship.getOverloadColor(), false);
    }

    private void spawnLargeNebulaEffect(CombatEngineAPI engine, float judgeCollision, float effectLevel) {
        Vector2f loc = Misc.getPointAtRadius(ship.getLocation(),
                (float) (judgeCollision * (Math.random() * 0.2f + 0.8f)));
        Vector2f vel = Misc.getUnitVector(ship.getLocation(), loc);
        vel.scale(effectLevel * ship.getCollisionRadius() * (float) (0.5f + Math.random() * 0.5f));
        Vector2f.add(vel, ship.getVelocity(), vel);
        engine.addNebulaParticle(loc, vel, 2f, (float) (15f), 1f, (float) (Math.random() * 1f + 1f),
                0.3f, ship.getOverloadColor(), false);
        // engine.addNebulaParticle(loc, vel, 2f, (float) (3f + 1f * Math.random()), 1f
        // + effectLevel, 0.05f,
        // 0.7f, ship.getOverloadColor(), false);
    }

}
