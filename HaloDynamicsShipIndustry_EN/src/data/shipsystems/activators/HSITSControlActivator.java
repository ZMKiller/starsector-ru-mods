package data.shipsystems.activators;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;
import data.scripts.HSIRenderer.HSICombatRendererV2;
import data.scripts.HSIRenderer.HSITSControlFxObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicShipSystemSubsystem;
import org.magiclib.subsystems.MagicSubsystem;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HSITSControlActivator extends MagicSubsystem {

    public HSITSControlActivator(ShipAPI ship) {
        super(ship);
    }

    private float maxRangeSelf = 0;
    private float minRangeSelf = 1000;

    @Override
    protected void init() {
        super.init();
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            float range = weapon.getRange();
            if (maxRangeSelf < range)
                maxRangeSelf = range;
            if (minRangeSelf > range)
                minRangeSelf = range;
        }
        fxObject = new HSITSControlFxObject(ship,this);
        HSICombatRendererV2.getInstance().addFxObject(fxObject);
    }

    @Override
    public float getBaseActiveDuration() {
        return 1;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 2;
    }

    @Override
    protected int getMaxCharges() {
        return 3;
    }

    @Override
    public float getBaseChargeRechargeDuration() {
        return 12f;
    }

    @Override
    public float getBaseInDuration() {
        return 0.5f;
    }

    @Override
    public float getBaseOutDuration() {
        return 0.5f;
    }
    private IntervalUtil threatChecker = new IntervalUtil(0.05f, 0.1f);

    private Vector2f weightedDefensiveDir = new Vector2f();

    private HSITSControlFxObject fxObject;
    @Override
    public boolean shouldActivateAI(float amount) {
        if(!canActivate()) return false;
        if(ship.getAI()==null) return false;
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
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS,1f, Vector2f.add(new Vector2f(ship.getLocation()),weightedDefensiveDir,null));
            }else{
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS,1f,ship.getShipTarget()==null?ship.getMouseTarget():ship.getShipTarget().getLocation());
            }
            //ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
        }
        return shouldDo;
    }

    private Vector2f targetLoc = null;
    public static final Color JITTER_COLOR = new Color(25, 171, 224, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(25, 171, 224, 55);
    public static final Color AFTERIMAGE_COLOR = new Color(25, 171, 224, 155);

    private IntervalUtil afterImageTest = new IntervalUtil(0.2f, 0.2f);

    private CombatEngineAPI engine = Global.getCombatEngine();

    private IntervalUtil projChecker = new IntervalUtil(0.1f,0.1f);

    private TimeoutTracker<BeamAPI> deflectBeam =  new TimeoutTracker<>();

    private Map<BeamAPI,Vector2f> beamEnd = new HashMap<>();

    private static String id = "HSITSControlActivator";
    @Override
    public void advance(float amount, boolean isPaused) {
        if(!isPaused&&isOn()){
            float effectLevel = getEffectLevel();
            /*if(targetLoc==null) {
                if (ship.getAI() == null) {
                    targetLoc = ship.getMouseTarget();
                } else {
                    targetLoc = (Vector2f) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS);
                }
                if (targetLoc != null) {
                    Vector2f base = VectorUtils.getDirectionalVector(new Vector2f(ship.getLocation()), targetLoc);
                    base.scale(ship.getMaxSpeedWithoutBoost());
                    ship.getVelocity().set(base);
                }
            }*/
            //}else{
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
            //}
            projChecker.advance(amount);
            deflectBeam.advance(amount);
            if(!deflectBeam.getItems().isEmpty()){
                java.util.List<BeamAPI> toRemove = new ArrayList<>();
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
                                new Vector2f(ship.getLocation())) < (ship.getCollisionRadius() + 400f)) {
                            if (proj.getCustomData().containsKey("HSITSControlDeflected")) continue;
                            float dis = (float) (ship.getCollisionRadius() + 200f * Math.random());
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
                            fxObject.addData(new HSITSControlFxObject.HSITSControlData(
                                    MathUtils.clamp(proj.getDamageAmount()/200f,0.7f,1.5f),
                                    new Vector2f(proj.getLocation()),
                                    proj.getFacing(),
                                    Vector2f.add(new Vector2f(ship.getLocation()), vec, null),
                                    angle,
                                    MathUtils.clamp(proj.getDamageAmount()/10f,10f,30f),
                                    null));
                            proj.getLocation().set(Vector2f.add(new Vector2f(ship.getLocation()), vec, null));
                            //engine.addNebulaParticle(proj.getLocation(), ship.getVelocity(),
                            //        proj.getCollisionRadius(),
                            //        0.2f, 0.7f, 0.4f, 1f, AFTERIMAGE_COLOR);
                            proj.getVelocity().set((Vector2f) Misc.getUnitVectorAtDegreeAngle(angle).scale(proj.getVelocity().length()));
                            proj.setCustomData("HSITSControlDeflected", true);
                        }
                    }
                }
            }

            if(projChecker.intervalElapsed()) {
                for (BeamAPI beam : engine.getBeams()) {
                    if (beam.getSource().getOwner() != ship.getOwner()&&!deflectBeam.contains(beam)) {
                        Vector2f nearest = MathUtils.getNearestPointOnLine(new Vector2f(ship.getLocation()), beam.getFrom(), beam.getTo());
                        float dist = MathUtils.getDistance(new Vector2f(ship.getLocation()), nearest);
                        if(dist<ship.getCollisionRadius()+100f&&beam.getRayEndPrevFrame()!=null&&Misc.getDistance(beam.getRayEndPrevFrame(),new Vector2f(ship.getLocation()))<ship.getCollisionRadius()+100f){
                            deflectBeam.add(beam,3f);
                        }
                        Vector2f end = Vector2f.add(new Vector2f(ship.getLocation()),(Vector2f)VectorUtils.getDirectionalVector(new Vector2f(ship.getLocation()),beam.getFrom()).scale(ship.getCollisionRadius()+75f) ,null);
                        /*fxObject.addData(new HSITSControlFxObject.HSITSControlData(
                                1f,
                                new Vector2f(end),
                                VectorUtils.getAngle(beam.getFrom(),end),
                                null,
                               0f,
                                beam.getWidth()+6f,
                                beam));*/
                        beamEnd.put(beam,end);
                    }
                }
            }
        }
    }

    @Override
    public void onFinished() {
        super.onFinished();
        ship.getMutableStats().getTimeMult().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);
        targetLoc = null;
    }

    protected boolean judgeThreats() {
        weightedDefensiveDir = new Vector2f();
        float potential = 0;
        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {
            if (proj.getOwner() == ship.getOwner())
                continue;
            if (MathUtils.getDistanceSquared(proj.getLocation(), new Vector2f(ship.getLocation())) > 360000)
                continue;
            potential += ((proj.getDamageType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                    * proj.getDamage().getDamage();
            Vector2f dir = VectorUtils.getDirectionalVector(new Vector2f(ship.getLocation()),proj.getLocation());
            VectorUtils.rotate(dir,70f+20f*(float)Math.random(),dir);
            dir.scale(proj.getDamageAmount()/10f);
            Vector2f.add(weightedDefensiveDir,dir,weightedDefensiveDir);
        }

        for(BeamAPI beam:Global.getCombatEngine().getBeams()){
            if(beam.getSource().getOwner()==ship.getOwner()) continue;
            Vector2f nearest = MathUtils.getNearestPointOnLine(new Vector2f(ship.getLocation()),beam.getFrom(),beam.getTo());
            if(MathUtils.getDistance(new Vector2f(ship.getLocation()),nearest)<ship.getCollisionRadius()*1.2f){
                potential+=beam.getDamage().getDamage()/2f;
            }
            Vector2f dir = VectorUtils.getDirectionalVector(nearest,new Vector2f(ship.getLocation()));
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
        if(getCharges()<=2) return false;
        return ship.getFluxLevel() < 0.7f && ship.getShipTarget().getFluxLevel() >= ship.getFluxLevel() - 0.1f &&ship.getShipTarget().getFluxLevel()>0.5f&&
                MathUtils.getDistance(new Vector2f(ship.getLocation()), ship.getShipTarget().getLocation()) <
                        (ship.getCollisionRadius() * 0.9f + ship.getShipTarget().getCollisionRadius() * 0.9f) + maxRangeSelf * 0.8f;
    }

    @Override
    public String getDisplayText() {
        return HSII18nUtil.getShipSystemString("HSITSControlAName");
    }
}
