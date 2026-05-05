package data.ai.HSIWeaponAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class HSILiebeWeaponAI extends HSIBaseAutoFirePlugin {

    public HSILiebeWeaponAI(WeaponAPI weapon) {
        super(weapon);
    }

    private float maxLevel = 0;

    @Override
    public void advance(float amount) {
        if (weapon.getCooldownRemaining() > 0) return;
        float leftFlux = ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux();
        if (leftFlux < 0.66f * weapon.getFluxCostToFire())
            return;
        if (weapon.isDisabled()) return;
        target = Vector2f.add(weapon.getLocation(),Misc.getUnitVectorAtDegreeAngle(weapon.getSlot().computeMidArcAngle(ship)),null);
        float chargeLevel = weapon.getChargeLevel();
        if (chargeLevel <= 0) {//索敌
            boolean finishedSearch = false;
            if (ship.getShipTarget() != null&&ship.getShipTarget().isAlive()&&ship.getShipTarget().isTargetable()&&!ship.getShipTarget().isFighter()) {
                Vector2f preAim = Global.getCombatEngine().getAimPointWithLeadForAutofire(ship, ship.getMutableStats().getAutofireAimAccuracy().getModifiedValue(), ship.getShipTarget(), weapon.getProjectileSpeed());
                if (isTargetLegal(ship.getShipTarget(), preAim)) {
                    target = preAim;
                    targetShip = ship.getShipTarget();
                    finishedSearch = true;
                    shouldFire = true;
                }
            }
            for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, weapon.getRange())) {
                if (enemy.isFighter()) continue;
                if(!enemy.isTargetable()) continue;
                if (!finishedSearch) {
                    Vector2f preAim = Global.getCombatEngine().getAimPointWithLeadForAutofire(ship, ship.getMutableStats().getAutofireAimAccuracy().getModifiedValue(), enemy, weapon.getProjectileSpeed());
                    if (isTargetLegal(enemy, preAim)) {
                        targetShip = enemy;
                        target = preAim;
                        finishedSearch = true;
                        shouldFire = true;
                    }
                }else{
                    break;
                }
            }
        } else {
            int level = (int) (Math.floor(chargeLevel * 5f));

            int expectedLevel = 5;
            if(targetShip!=null){
                if(targetShip.getShield()!=null&&targetShip.getShield().isOn()){
                    expectedLevel = 0;
                }
            }

            if(leftFlux < 0.68f * weapon.getFluxCostToFire()){
                expectedLevel = 0;
            }
            if(leftFlux < 0.76f * weapon.getFluxCostToFire()){
                expectedLevel = Math.min(expectedLevel,1);
            }
            if(leftFlux < 0.84f * weapon.getFluxCostToFire()){
                expectedLevel = Math.min(expectedLevel,2);
            }
            if(leftFlux < 0.92f * weapon.getFluxCostToFire()){
                expectedLevel = Math.min(expectedLevel,3);
            }
            if(leftFlux < 0.92f * weapon.getFluxCostToFire()){
                expectedLevel = Math.min(expectedLevel,4);
            }
            //stopFire
            shouldFire = (level < expectedLevel||(expectedLevel == 0&&chargeLevel<0.06f))&&targetShip!=null;
        }
    }
}
