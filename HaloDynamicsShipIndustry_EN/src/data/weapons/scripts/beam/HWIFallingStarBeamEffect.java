package data.weapons.scripts.beam;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class HWIFallingStarBeamEffect extends RiftCascadeEffect {

    //private float elpased = 0;

    private int SPAWNED_TIME = 0;

    private float MAX_RANGE = 1;

    private boolean INIT = false;

    //private IntervalUtil spawnTracker = new IntervalUtil(0.1f,0.1f);
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if(!INIT){
            MAX_RANGE = beam.getWeapon().getRange();
            INIT= true;
        }
        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            spawnNegativeParticles(engine, beam);
        }
        //spawnTracker.advance(amount);
        //elpased+=amount;
        //float beamReachLevel = MathUtils.clamp(elpased/(beam.getWeapon().getSpec().getBurstDuration()),0,1);
        //float distLevel = MathUtils.clamp (1f-beamReachLevel*beamReachLevel,0,1);
        //beam.getTo().set(Vector2f.add(beam.getFrom(),
                //(Vector2f) Misc.getUnitVector(beam.getFrom(),beam.getRayEndPrevFrame()).scale(distLevel*beam.getWeapon().getRange()),null));
        ShipAPI ship = beam.getSource();

//		if (!canSpawn || beam.getBrightness() >= 1f) return;
        if(Misc.getDistance(beam.getFrom(),beam.getRayEndPrevFrame())>=SPAWNED_TIME*100f) {
            SPAWNED_TIME++;
            spawnMine(ship, beam.getRayEndPrevFrame());
        }

    }
    @Override
    public void spawnMine(ShipAPI source, Vector2f mineLoc) {
        CombatEngineAPI engine = Global.getCombatEngine();

        MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null,
                "HWI_FallingStar_MineLayer",
                mineLoc,
                (float) Math.random() * 360f, null);

        // "spawned" does not include this mine
        float sizeMult = 0.5f+(float)(Math.sin(Math.PI*SPAWNED_TIME*100f/MAX_RANGE));
        mine.setCustomData(RiftCascadeMineExplosion.SIZE_MULT_KEY, sizeMult);

        if (source != null) {
            Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                    source, WeaponAPI.WeaponType.ENERGY, false, mine.getDamage());
        }

        mine.getDamage().getModifier().modifyMult("mine_sizeMult", sizeMult);


        float fadeInTime = 0.05f;
        mine.getVelocity().scale(0);
        mine.fadeOutThenIn(fadeInTime);

        //Global.getCombatEngine().addPlugin(createMissileJitterPlugin(mine, fadeInTime));

        //mine.setFlightTime((float) Math.random());
        float liveTime = 0f;
        //liveTime = 0.01f;
        mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
        mine.addDamagedAlready(source);
        mine.setNoMineFFConcerns(true);
    }

    protected static Color FALLING_STAR_SOURCE_COLOR = new Color(225,200,65,25);
    @Override
    public void spawnNegativeParticles(CombatEngineAPI engine, BeamAPI beam) {
        float length = beam.getLengthPrevFrame();
        if (length <= 10f) return;

        //NEGATIVE_SOURCE_COLOR = new Color(200,255,200,25);

        Vector2f from = beam.getFrom();
        Vector2f to = beam.getRayEndPrevFrame();

        ShipAPI ship = beam.getSource();

        float angle = Misc.getAngleInDegrees(from, to);
        Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
        Color color = FALLING_STAR_SOURCE_COLOR;

        float sizeMult = 1f;
        sizeMult = 0.67f;

        for (int i = 0; i < 3; i++) {
            float rampUp = 0.25f + 0.25f * (float) Math.random();
            float dur = 1f + 1f * (float) Math.random();
            //dur *= 2f;
            float size = 200f + 50f * (float) Math.random();
            size *= sizeMult;
            //size *= 0.5f;
            //Vector2f loc = Misc.getPointWithinRadius(from, size * 0.5f);
            //Vector2f loc = Misc.getPointAtRadius(from, size * 0.33f);
            Vector2f loc = Misc.getPointAtRadius(beam.getWeapon().getLocation(), size * 0.33f);
            engine.addNegativeParticle(loc, ship.getVelocity(), size, rampUp / dur, dur, color);
            //engine.addNegativeNebulaParticle(loc, ship.getVelocity(), size, 2f, rampUp, 0f, dur, color);
        }

        //if (true) return;

        // particles along the beam
        float spawnOtherParticleRange = 100;
        if (length > spawnOtherParticleRange * 2f && (float) Math.random() < 0.25f) {
            //color = new Color(150,255,150,255);
            color = new Color(150,255,150,75);
            int numToSpawn = (int) ((length - spawnOtherParticleRange) / 200f + 1);
            numToSpawn = 1;
            for (int i = 0; i < numToSpawn; i++) {
                float distAlongBeam = spawnOtherParticleRange + (length - spawnOtherParticleRange * 2f) * (float) Math.random();
                float groupSpeed = 100f + (float) Math.random() * 100f;
                for (int j = 0; j < 7; j++) {
                    float rampUp = 0.25f + 0.25f * (float) Math.random();
                    float dur = 1f + 1f * (float) Math.random();
                    float size = 50f + 50f * (float) Math.random();
                    Vector2f loc = new Vector2f(dir);
                    float sign = Math.signum((float) Math.random() - 0.5f);
                    loc.scale(distAlongBeam + sign * (float) Math.random() * size * 0.5f);
                    Vector2f.add(loc, from, loc);

//					Vector2f off = new Vector2f(perp1);
//					if ((float) Math.random() < 0.5f) off = new Vector2f(perp2);
//
//					off.scale(size * 0.1f);
                    //Vector2f.add(loc, off, loc);

                    loc = Misc.getPointWithinRadius(loc, size * 0.25f);

                    float dist = Misc.getDistance(loc, to);
                    Vector2f vel = new Vector2f(dir);
                    if ((float) Math.random() < 0.5f) {
                        vel.negate();
                        dist = Misc.getDistance(loc, from);
                    }

                    float speed = groupSpeed;
                    float maxSpeed = dist / dur;
                    if (speed > maxSpeed) speed = maxSpeed;
                    vel.scale(speed);
                    Vector2f.add(vel, ship.getVelocity(), vel);

                    engine.addNegativeParticle(loc, vel, size, rampUp, dur, color);
                }
            }
        }
    }
}
