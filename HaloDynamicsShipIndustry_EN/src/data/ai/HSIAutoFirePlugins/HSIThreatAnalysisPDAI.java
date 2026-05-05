package data.ai.HSIAutoFirePlugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ghosts.types.EchoGhost;
import com.fs.starfarer.api.util.Misc;
import data.kit.HSIAIUtils;
import data.weapons.scripts.HWIMGSystemWeapon;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSIThreatAnalysisPDAI implements AutofireAIPlugin {
    public static final String DATA_KEY = "HSIThreatSharedData";
    private Vector2f targetLoc = null;
    private WeaponAPI weapon;
    private ShipAPI ship;
    private MissileAPI targetMissile;
    private ShipAPI targetShip;
    private boolean shouldFire = false;
    private CombatEngineAPI engine = Global.getCombatEngine();

    private HWIMGSystemWeapon effect;
    private HSIAutoFireShipData shipData;

    public HSIThreatAnalysisPDAI(WeaponAPI weapon) {
        this.weapon = weapon;
        ship = weapon.getShip();
        if(weapon.getEffectPlugin() instanceof HWIMGSystemWeapon){
            effect = (HWIMGSystemWeapon) weapon.getEffectPlugin();
        }
        shipData = HSIAutoFireShipData.getInstance(ship);
    }

    public void advance(float amount) {
        if (engine.isPaused())
            return;
        // Global.getLogger(this.getClass()).info("Arc:"+weapon.getArcFacing());
        // think.advance(amount);
        // if(!think.intervalElapsed()) return;
        if(weapon.isDisabled()) return;
        if(ship.getFluxTracker().isOverloadedOrVenting()) return;
        if(weapon.getCooldownRemaining()<=0&&((targetShip==null||!targetShip.isAlive())&&(targetMissile==null||targetMissile.isExpired()||!engine.isEntityInPlay(targetMissile)))) {
            int num = 4;
            List<HSIAutoFireTargetingUnitData> datas = new ArrayList<>();
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.MISSILE, weapon.getRange()));
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.FIGHTER, weapon.getRange()));
            Collections.sort(datas, new HSIAutoFireTargetingUnitData.HSIAutoFireTargetingUnitDataScoreComparator());
            HSIAutoFireTargetingUnitData chosen = null;
            for (HSIAutoFireTargetingUnitData data : datas) {
                if (HSIAIUtils.isAcceptableAimLocation(data.getTarget().getLocation(), weapon)&&!HSIAIUtils.isOverKilled(data.getTarget())) {
                    chosen = data;
                    break;
                }
            }
            if (chosen == null) {
                num = 99;
                datas.clear();
            }
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.MISSILE, weapon.getRange()));
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.FIGHTER, weapon.getRange()));
            Collections.sort(datas, new HSIAutoFireTargetingUnitData.HSIAutoFireTargetingUnitDataScoreComparator());
            for (HSIAutoFireTargetingUnitData data : datas) {
                if (HSIAIUtils.isAcceptableAimLocation(data.getTarget().getLocation(), weapon)) {
                    chosen = data;
                    break;
                }
            }
            if (chosen == null) return;
            if(chosen.getTarget() instanceof MissileAPI){
                targetMissile = (MissileAPI) chosen.getTarget();
            }
            if(chosen.getTarget() instanceof ShipAPI){
                targetShip = (ShipAPI) chosen.getTarget();
            }
            CombatEntityAPI t = chosen.getTarget();
            targetLoc = HSIAIUtils.getAimPointSimple(weapon,t);
            shouldFire = true;
        }
    }

    public boolean shouldFire() {
        if ((targetShip == null && targetMissile == null)||targetLoc==null)
            return false;
        return shouldFire && (Math.abs((MathUtils.getShortestRotation(this.weapon.getCurrAngle(),
                VectorUtils.getAngleStrict(this.weapon.getFirePoint(0),
                        targetLoc)))) <= 2.0f
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

}
