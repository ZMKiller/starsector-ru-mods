package data.weapons.scripts;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;

public class HWIDualityEffect_Old implements EveryFrameWeaponEffectPlugin {
    private boolean init = false;
    private HWILidarDrone LidarDroneL;
    private HWILidarDrone LidarDroneR;

    public class HWILidarDrone {
        private Vector2f DroneFixPoint;
        private ShipAPI drone;
        private WeaponAPI weapon;
        private WeaponAPI Lidar;

        public HWILidarDrone(WeaponAPI weapon, Vector2f DroneFixPoint) {
            this.DroneFixPoint = (DroneFixPoint == null) ? (new Vector2f(0, 0)) : DroneFixPoint;
            this.weapon = weapon;
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec("dem_drone");
            ShipVariantAPI v = Global.getSettings().createEmptyVariant("dem_drone", spec);
            v.addWeapon("WS 000", "HWI_MGSystem_M");
            v.addWeapon("WS 001", "HWI_FireLidar");
            WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
            g.addSlot("WS 000");
            WeaponGroupSpec g2 = new WeaponGroupSpec(WeaponGroupType.LINKED);
            g.addSlot("WS 001");
            v.addWeaponGroup(g);
            v.addWeaponGroup(g2);
            drone = Global.getCombatEngine().createFXDrone(v);
            Lidar = drone.getAllWeapons().get(0);
            drone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
            drone.setOwner(weapon.getShip().getOwner());
            drone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
            drone.setDrone(true);
            drone.setCollisionClass(CollisionClass.NONE);
            Global.getCombatEngine().addEntity(drone);
            drone.giveCommand(ShipCommand.SELECT_GROUP, g, 0);
            drone.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, g, 0);
            drone.giveCommand(ShipCommand.SELECT_GROUP, g2, 1);
        }

        public void updateDrone() {
            float facing = weapon.getCurrAngle();
            Vector2f currLoc = new Vector2f(0, 0);
            VectorUtils.rotateAroundPivot(DroneFixPoint, new Vector2f(0f, 0f), facing, currLoc);
            drone.getLocation().set(Vector2f.add(weapon.getLocation(), currLoc, null));
            drone.setFacing(facing);
            Lidar.setCurrAngle(facing);
        }

        public void cleanUp() {
            Global.getCombatEngine().removeEntity(drone);
        }

    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!init) {
            init(weapon);
            init = true;
        }
        if (engine.isPaused())
            return;
        if ((weapon.getShip() == null || !weapon.getShip().isAlive()) && (LidarDroneL != null || LidarDroneR != null)) {
            if (LidarDroneL != null) {
                LidarDroneL.cleanUp();
                LidarDroneL = null;
            }
            if (LidarDroneR != null) {
                LidarDroneR.cleanUp();
                LidarDroneR = null;
            }
        }
        if (LidarDroneL == null || LidarDroneR == null)
            return;
        LidarDroneL.updateDrone();
        LidarDroneR.updateDrone();
    }

    private void init(WeaponAPI weapon) {
        if (weapon.getSlot().isHardpoint()) {
            LidarDroneL = new HWILidarDrone(weapon, new Vector2f(14, -10));
            LidarDroneR = new HWILidarDrone(weapon, new Vector2f(14, 10));
        } else {
            LidarDroneL = new HWILidarDrone(weapon, new Vector2f(3.5f, -10));
            LidarDroneR = new HWILidarDrone(weapon, new Vector2f(3.5f, 10));
        }
    }
}
