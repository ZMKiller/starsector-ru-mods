package data.weapons.scripts;

import java.util.List;

import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;

import data.kit.AjimusUtils;

public class HWIFireComputer implements EveryFrameWeaponEffectPlugin {
    private boolean init = false;
    private HWILidarDrone LidarDrone;
    private boolean LidarHit = false;

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
            v.addWeapon("WS 000", "HWI_FireLidar");
            WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
            g.addSlot("WS 000");
            v.addWeaponGroup(g);
            drone = Global.getCombatEngine().createFXDrone(v);
            Lidar = drone.getAllWeapons().get(0);
            drone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
            drone.setOwner(weapon.getShip().getOwner());
            drone.getMutableStats().getBeamWeaponRangeBonus().modifyFlat("dem",
                    weapon.getRange() - Lidar.getRange() + 50f);
            drone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
            drone.setDrone(true);
            drone.setCollisionClass(CollisionClass.NONE);
            drone.setForceHideFFOverlay(true);
            Global.getCombatEngine().addEntity(drone);
        }

        public void updateDrone() {
            float facing = weapon.getCurrAngle();
            drone.getLocation().set(AjimusUtils.getEngineCoordFromRelativeCoord(weapon.getLocation(), DroneFixPoint, facing));
            drone.setFacing(facing);
            Lidar.setCurrAngle(facing);
        }

        public void forceLidarFire() {
            Lidar.setForceFireOneFrame(true);
        }

        public void cleanUp() {
            Global.getCombatEngine().removeEntity(drone);
        }

        public List<BeamAPI> getLidarBeams() {
            return Lidar.getBeams();
        }

    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!init) {
            init(weapon);
            init = true;
        }
        if (engine.isPaused())
            return;
        if ((weapon.getShip() == null || !weapon.getShip().isAlive()) && LidarDrone != null) {
            LidarDrone.cleanUp();
            LidarDrone = null;
        }
        if (LidarDrone == null)
            return;
        LidarDrone.updateDrone();
        if (((weapon.getChargeLevel() > 0 && weapon.getChargeLevel() < 0.25f)
                || (weapon.getChargeLevel() > 0.5 && weapon.getChargeLevel() < 0.75f))
                && weapon.getCooldownRemaining() == 0 && !LidarHit) {
            LidarDrone.forceLidarFire();
            if (!LidarDrone.getLidarBeams().isEmpty()) {
                for (BeamAPI beam : LidarDrone.getLidarBeams()) {
                    if (beam.getDamageTarget() != null) {
                        LidarHit = true;
                    }
                }
            }
        }
        if (weapon.getCooldownRemaining() > 0 && LidarHit) {
            weapon.setRemainingCooldownTo(weapon.getCooldown() * 0.8f);
            LidarHit = false;
        }
    }

    private void init(WeaponAPI weapon) {
        Vector2f fixPoint = null;
        if (weapon.getSpec().getWeaponId().equals("HWI_Arbiter")) {
            if(!weapon.getSlot().isHardpoint()){
                fixPoint = new Vector2f(-2, -15);
            }else{
                fixPoint = new Vector2f(5, -15);
            }
        }
        LidarDrone = new HWILidarDrone(weapon, fixPoint);
    }
}
