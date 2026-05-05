package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class HSITSControl extends BaseShipSystemScript {
    private ShipAPI ship;

    private Vector2f targetLoc = null;
    public static final Color JITTER_COLOR = new Color(25, 171, 224, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(25, 171, 224, 55);
    public static final Color AFTERIMAGE_COLOR = new Color(25, 171, 224, 155);

    private IntervalUtil afterImageTest = new IntervalUtil(0.2f, 0.2f);

    private CombatEngineAPI engine = Global.getCombatEngine();

    private IntervalUtil projChecker = new IntervalUtil(0.1f,0.1f);

    private TimeoutTracker<BeamAPI> deflectBeam =  new TimeoutTracker<>();

    private Map<BeamAPI,Vector2f> beamEnd = new HashMap<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(ship==null){
            if(stats.getEntity() instanceof ShipAPI){
                ship = (ShipAPI) stats.getEntity();
            }else{
                return;
            }
        }
        if(effectLevel>0){
            if(targetLoc==null){
                if(ship.getAI()==null){
                    targetLoc = ship.getMouseTarget();
                }else{
                    targetLoc = (Vector2f) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS);
                }
                if(targetLoc!=null){
                    Vector2f base = VectorUtils.getDirectionalVector(ship.getLocation(),targetLoc);
                    base.scale(ship.getMaxSpeedWithoutBoost());
                    ship.getVelocity().set(base);
                }
            }else{
                ship.getMutableStats().getTimeMult().modifyMult(id,(1f+effectLevel*3f));
                if(ship == Global.getCombatEngine().getPlayerShip()){
                    Global.getCombatEngine().getTimeMult().modifyMult(id,1f/(1f+effectLevel*3f));
                }
                float jitterLevel = (float) (Math.pow(effectLevel, 2));
                ship.setJitter(this, JITTER_COLOR, jitterLevel, 1, 0, jitterLevel * 3f);
                ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 15, 0f, 4f + jitterLevel * 3f);
                afterImageTest.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if (afterImageTest.intervalElapsed()) {
                    ship.addAfterimage(AFTERIMAGE_COLOR, 0, 0, ship.getVelocity().getX() * (-1),
                            ship.getVelocity().getY() * (-1), 1f, 0.1f, 0.3f, 0.1f, false, true, false);
                }
            }
            projChecker.advance(engine.getElapsedInLastFrame());
            deflectBeam.advance(engine.getElapsedInLastFrame());
            if(!deflectBeam.getItems().isEmpty()){
                List<BeamAPI> toRemove = new ArrayList<>();
                for(BeamAPI beam: deflectBeam.getItems()){
                    if(beam.getBrightness()<=0){
                        toRemove.add(beam);
                    }
                }
                for(BeamAPI b:toRemove){
                    deflectBeam.remove(b);
                    beamEnd.remove(b);
                }
            }

            if(!beamEnd.isEmpty()){
                List<BeamAPI> toRemove = new ArrayList<>();
                for(BeamAPI key: beamEnd.keySet()){
                    if(deflectBeam.contains(key)) continue;
                    toRemove.add(key);
                }
                for(BeamAPI b:toRemove){
                    beamEnd.remove(b);
                }
                for(BeamAPI b: beamEnd.keySet()){
                    b.getTo().set(beamEnd.get(b));
                }
            }
            //偏转
            if(projChecker.intervalElapsed()) {
                for (DamagingProjectileAPI proj : engine.getProjectiles()) {
                    if (proj.getOwner() != ship.getOwner()) {
                        if (Misc.getDistance(proj.getLocation(),
                                ship.getLocation()) < (ship.getCollisionRadius() + 400f)) {
                            if (proj.getCustomData().containsKey("HSITSControlDeflected")) continue;
                            float dis = (float) (200f + 300f * Math.random());
                            float angle = 0;
                            float diff = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getFacing(proj.getVelocity()));
                            if (diff < -120f) {
                                angle = VectorUtils.getFacing(proj.getVelocity()) + 30f + (float) Math.random() * 30f;
                            } else if (diff > 120f) {
                                angle = VectorUtils.getFacing(proj.getVelocity()) - 30f - (float) Math.random() * 30f;
                            } else {
                                angle = VectorUtils.getFacing(proj.getVelocity());
                            }
                            Vector2f vec = Misc.getUnitVectorAtDegreeAngle(angle);
                            float distMult = (proj instanceof MissileAPI)?3f:1f;
                            vec.scale(dis*distMult);
                            proj.getLocation().set(Vector2f.add(ship.getLocation(), vec, null));
                            engine.addNebulaParticle(proj.getLocation(), ship.getVelocity(),
                                    proj.getCollisionRadius(),
                                    0.2f, 0.7f, 0.4f, 1f, AFTERIMAGE_COLOR);
                            proj.getVelocity().set((Vector2f) Misc.getUnitVectorAtDegreeAngle(angle).scale(proj.getVelocity().length()));
                            proj.setCustomData("HSITSControlDeflected", true);
                        }
                    }
                }
            }

            if(projChecker.intervalElapsed()) {
                for (BeamAPI beam : engine.getBeams()) {
                    if (beam.getSource().getOwner() != ship.getOwner()&&!deflectBeam.contains(beam)) {
                        Vector2f nearest = MathUtils.getNearestPointOnLine(ship.getLocation(), beam.getFrom(), beam.getTo());
                        float dist = MathUtils.getDistance(ship.getLocation(), nearest);
                        if(dist<ship.getCollisionRadius()+100f){
                            deflectBeam.add(beam,3f);
                        }
                        Vector2f end = Vector2f.add(ship.getLocation(),(Vector2f)VectorUtils.getDirectionalVector(ship.getLocation(),beam.getFrom()).scale(ship.getCollisionRadius()+100f) ,null);
                        beamEnd.put(beam,end);
                    }
                }
            }
        }
    }


    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if(ship==null){
            if(stats.getEntity() instanceof ShipAPI){
                ship = (ShipAPI) stats.getEntity();
            }else{
                return;
            }
        }
        Global.getCombatEngine().getTimeMult().unmodify(id);
        targetLoc = null;
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(HSII18nUtil.getShipSystemString("HSITSControl"), false);
        }
        return null;
    }
}
