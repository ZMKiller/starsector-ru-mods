package data.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lwjgl.util.vector.Vector2f;

public class HSIWeaponBindEngine implements EveryFrameWeaponEffectPlugin {

    private boolean init = false;

    private ShipAPI ship;

    private WeaponAPI weapon;

    private ShipAPI engineDrone;
    private EngineSlotAPI droneEngineSlot;
    private ShipEngineControllerAPI controller;
    private float CONTRAIL_DUR = 0,CONTRAIL_WIDTH = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(!init) init(weapon,engine);
        if(ship == null) return;
        if(!ship.isAlive()){
            Global.getCombatEngine().removeEntity(engineDrone);
            return;
        }
        if(engineDrone!=null) {
            //engineDrone.getEngineController().setFlameLevel(droneEngineSlot, (float)(Math.sin(Math.PI*0.5f*ship.getSystem().getEffectLevel())));
            droneEngineSlot.setContrailDuration(CONTRAIL_DUR*(float)(Math.sin(Math.PI*0.5f*ship.getSystem().getEffectLevel())));
            droneEngineSlot.setContrailWidth(CONTRAIL_WIDTH*(float)(Math.sin(Math.PI*0.5f*ship.getSystem().getEffectLevel())));
            droneEngineSlot.setColor(ship.getEngineController().getFlameColorShifter().getCurr());
            engineDrone.getEngineController().getExtendGlowFraction().setBase(controller.getExtendGlowFraction().getCurr());
            engineDrone.getEngineController().getExtendLengthFraction().setBase(controller.getExtendLengthFraction().getCurr());
            engineDrone.getEngineController().getExtendWidthFraction().setBase(controller.getExtendWidthFraction().getCurr());
            engineDrone.giveCommand(ShipCommand.ACCELERATE, null, 0);

            //engineDrone.getLocation().set(weapon.getFirePoint(0));
            engineDrone.setFacing(weapon.getCurrAngle()+weapon.getSpec().getTurretAngleOffsets().get(0) - 180f);
            engineDrone.getLocation().set(weapon.getFirePoint(0));
        }
    }

    private void init(WeaponAPI weapon,CombatEngineAPI engine){
        this.ship = weapon.getShip();
        if(ship!=null) {
            controller = ship.getEngineController();
            ShipVariantAPI v = Global.getSettings().createEmptyVariant("HSI_FX_ENGINE_DRONE", Global.getSettings().getHullSpec("HSI_EngineFXDrone"));
            engineDrone = engine.createFXDrone(v);
            engineDrone.getMutableStats().getHullDamageTakenMult().modifyMult("HSI_FX_DRONE", 0f);
            engineDrone.setDrone(true);
            engineDrone.setShipAI(null);
            engine.addEntity(engineDrone);
            droneEngineSlot = engineDrone.getEngineController().getShipEngines().get(0).getEngineSlot();
            CONTRAIL_DUR = droneEngineSlot.getContrailDuration();
            CONTRAIL_WIDTH = droneEngineSlot.getContrailWidth();
            engineDrone.setForceHideFFOverlay(true);
            engineDrone.setDoNotFlareEnginesWhenStrafingOrDecelerating(true);
        }
        init = true;
    }
}
