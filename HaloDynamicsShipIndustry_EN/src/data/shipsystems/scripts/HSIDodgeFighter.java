package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.TimeoutTracker;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HSIDodgeFighter extends BaseShipSystemScript {
    private ShipAPI ship;

    private Vector2f targetLoc = null;
    public static final Color AFTERIMAGE_COLOR = new Color(25, 171, 224, 155);

    private IntervalUtil afterImageTest = new IntervalUtil(0.05f, 0.05f);

    private IntervalUtil flareTest = new IntervalUtil(0.5f,0.7f);

    private static int MAX_FLARES = 6;

    private int flaresFired = 0;

    private boolean init = true;

    private boolean once = true;

    private List<WeaponSlotAPI> slots = new ArrayList<>();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (ship == null) {
            if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
            } else {
                return;
            }
        }
        if(init){
            for(WeaponSlotAPI slot:ship.getHullSpec().getAllWeaponSlotsCopy()){
                if(slot.isSystemSlot()){
                    slots.add(slot);
                }

            }
            init = false;
        }
        if (effectLevel>0&&once) {
            if (targetLoc == null) {
                if (ship.getAI() == null) {
                    targetLoc = ship.getMouseTarget();
                } else {
                    targetLoc = (Vector2f) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS);
                }
                if (targetLoc != null) {
                    Vector2f base = VectorUtils.getDirectionalVector(ship.getLocation(), targetLoc);
                    float toscale = (ship.isFighter())?ship.getMaxSpeedWithoutBoost():ship.getMaxSpeed()*3f;
                    if(toscale<100f) toscale = 300f;
                    base.scale(toscale);
                    ship.getVelocity().set(base);
                    once = false;
                }
            }
        }
        //stats.getMaxSpeed().modifyMult(id,1f+effectLevel);
        //stats.getAcceleration().modifyMult(id,1f+effectLevel);
        //stats.getDeceleration().modifyMult(id,1f+effectLevel);
        stats.getTimeMult().modifyMult(id,1f+0.5f*effectLevel);
        afterImageTest.advance(Global.getCombatEngine().getElapsedInLastFrame());
        flareTest.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if (afterImageTest.intervalElapsed()) {
            ship.addAfterimage(AFTERIMAGE_COLOR, 0, 0, ship.getVelocity().getX() * (-1),
                    ship.getVelocity().getY() * (-1), 1f, 0.1f, 0.3f, 0.1f, false, true, false);
        }

        if(flareTest.intervalElapsed()&&flaresFired<=MAX_FLARES){
            for(WeaponSlotAPI slot:slots){
                Global.getCombatEngine().spawnProjectile(ship,null,"flarelauncher2",slot.computePosition(ship),slot.computeMidArcAngle(ship),ship.getVelocity());
                if(ship!=null&&ship.isFighter()) flaresFired++;
            }

        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        targetLoc = null;
        once = true;
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTimeMult().unmodify(id);
    }
}


