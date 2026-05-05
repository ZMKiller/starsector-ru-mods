package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;

import data.kit.AjimusUtils;
import data.kit.HSIVisualUtils;
import data.weapons.scripts.beam.HWIFMBeamEffect;

import java.awt.*;

public class HSITravelJump extends BaseShipSystemScript {
    private boolean once = true;// 入场
    private boolean landed = false;
    private boolean onceFx = false;
    public CombatEngineAPI engine = Global.getCombatEngine();
    private ShipAPI ship;
    //private int wait = 15;
    private Vector2f endLoc = null;

    private Vector2f orLoc = null;

    private Vector2f lastFrameLoc = null;
    private float elpased = 0;

    private IntervalUtil afterImage = new IntervalUtil(0.05f,0.05f);

    protected static final Color AFTER_IMAGE = new Color(175, 175, 255, 175);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (once&&!ship.getVariant().hasHullMod("HSI_No_TravelJump")) {
            if (state == ShipSystemStatsScript.State.OUT) {
                stats.getMaxSpeed().unmodify(id);
            } else {
                stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
                stats.getAcceleration().modifyFlat(id, 600f * effectLevel);
                stats.getDeceleration().modifyFlat(id,300f*effectLevel);
                // stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
            }
            if (state == State.OUT) {
                ship.setExtraAlphaMult2(1);
                if(!ship.getChildModulesCopy().isEmpty()){
                    for(ShipAPI module:ship.getChildModulesCopy()){
                        module.setExtraAlphaMult2(1f);
                    }
                }
                float level = 1f-effectLevel*effectLevel;
                //Vector2f currLoc = new Vector2f(orLoc.getX()*(1f-level)+endLoc.getX()*level,orLoc.getY()*(1f-level)+endLoc.getY()*level);
                //Vector2f diff = Vector2f.sub(lastFrameLoc,currLoc,null);
                Vector2f currLoc = ship.getLocation();
                Vector2f diff = new Vector2f(ship.getVelocity());
                //diff.scale(1f/engine.getElapsedInLastFrame());
                ship.giveCommand(ShipCommand.ACCELERATE,null,0);

                lastFrameLoc = new Vector2f(currLoc);
                ship.getLocation().set(currLoc);

                afterImage.advance(engine.getElapsedInLastFrame());
                if (afterImage.intervalElapsed()) {
                    Color c = new Color(AFTER_IMAGE.getRed(),AFTER_IMAGE.getGreen(),AFTER_IMAGE.getBlue(),(int)(AFTER_IMAGE.getAlpha()*effectLevel));
                    ship.addAfterimage(c, 0, 0, -diff.getX(), -diff.getY(),
                            0, 0.1f, 0f, 0.1f, false, true, true);
                    if(!ship.getChildModulesCopy().isEmpty()){
                        for(ShipAPI module:ship.getChildModulesCopy()){
                            module.addAfterimage(c, 0, 0, diff.getX(), diff.getY(),
                                    0, 0.1f, 0f, 0.1f, false, true, true);
                        }
                    }
                }
                if (!onceFx) {
                    onceFx = true;
                    HSIVisualUtils.easyRippleOut(ship.getLocation(), new Vector2f(ship.getVelocity()),
                            ship.getCollisionRadius() * 3f, 100f, 1f, 20f);
                    MagicLensFlare.createSharpFlare(engine, ship, ship.getLocation(), 9f,
                            ship.getCollisionRadius() * 1.5f, ship.getFacing() + 90f,
                            HWIFMBeamEffect.EXPLOSION_UNDERCOLOR, HWIFMBeamEffect.STANDARD_RIFT_COLOR);
                    Global.getSoundPlayer().playSound("mine_ping", 1f, 2f, ship.getLocation(), ship.getVelocity());
                }
            } else {
                ship.setExtraAlphaMult2(0f);
                if(!ship.getChildModulesCopy().isEmpty()){
                    for(ShipAPI module:ship.getChildModulesCopy()){
                        module.setExtraAlphaMult2(0f);
                    }
                }
            }
        } else {
            if (state == ShipSystemStatsScript.State.OUT) {
                stats.getMaxSpeed().unmodify(id);
            } else {
                stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
                stats.getAcceleration().modifyFlat(id, 600f * effectLevel);
                // stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        once = false;
        elpased = 0;
    }

    protected Vector2f evaluateLandLocationForStart(ShipAPI ship) {
        float dist = 0;
        float frac = 0.5f;
        if(Global.getCombatEngine().getTotalElapsedTime(false)<0.1f) frac = 0.25f;
        if(!Global.getCombatEngine().isSimulation()) dist = Math.min(Global.getCombatEngine().getMapHeight()*frac,Math.min(ship.getMaxSpeedWithoutBoost() + 400f, 600f)
                * (engine.getContext().getInitialDeploymentBurnDuration()));
        Vector2f base = ship.getLocation();
        Vector2f extend = (Vector2f) Misc.getUnitVectorAtDegreeAngle(ship.getFacing()).scale(dist);
        Vector2f predictedLandLocation = Vector2f.add(extend, base, null);
        predictedLandLocation = AjimusUtils.findClearLocation(predictedLandLocation, ship.getCollisionRadius());
        return predictedLandLocation;
    }
}
