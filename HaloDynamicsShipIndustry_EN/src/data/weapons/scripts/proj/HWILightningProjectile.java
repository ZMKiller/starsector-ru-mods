package data.weapons.scripts.proj;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class HWILightningProjectile implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        ShipAPI target = null;
        int owner = ship.getOwner();
        List<CombatEntityAPI> entities = new ArrayList<CombatEntityAPI>();
        boolean Speard = false;
        boolean Focus = false;
        boolean Normal = false;
        if (weapon.getSpec().hasTag("spread_arc"))
            Speard = true;
        if (weapon.getSpec().hasTag("focus_arc"))
            Focus = true;
        if (weapon.getSpec().hasTag("normal_arc"))
            Normal = true;

        if (ship.getWeaponGroupFor(weapon)!=null&&ship.getWeaponGroupFor(weapon).isAutofiring()) {
            target = ship.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
        }
        if (target == null) {
            target = ship.getShipTarget();
        }
        if (target == null) {
            Vector2f point = ship.getMouseTarget();
            target = Misc.findClosestShipEnemyOf(ship, point, HullSize.FRIGATE, 400f, true);
        }
        if (!Focus) {
            for (ShipAPI s : engine.getShips()) {
                if (s.getOwner() != owner && s.isAlive()) {
                    if (isInRange(s, weapon)) {
                        if (s.isFighter()) {
                            entities.add(s);
                        } else if (target == null) {
                            target = s;
                        } else {
                            entities.add(s);
                        }
                    }
                }
                if (Normal && entities.size() > 4)
                    break;
            }
            if (Speard) {
                for (MissileAPI m : engine.getMissiles()) {
                    if (m.getOwner() != owner && !m.isExpired() && !m.isFizzling()&&engine.isEntityInPlay(m)) {
                        entities.add(m);
                    }
                }
            }
            float leftDamage = 2f;
            if(Speard) leftDamage = 3f;
            float n = leftDamage / entities.size();
            if (n > 0.75f)
                n = 0.75f;
            for (CombatEntityAPI e : entities) {
                engine.spawnEmpArc(ship, projectile.getLocation(), ship, e, DamageType.ENERGY,
                        projectile.getDamageAmount() * n, projectile.getEmpAmount() * n,
                        weapon.getRange() * 1.2f, null, projectile.getCollisionRadius() / 3,
                        new Color(210, 219, 255, 155),
                        new Color(255, 255, 255, 255));
                leftDamage -= n;
            }
            if (leftDamage > 0.5f)
                leftDamage = 0.5f;
        }
        if (target != null) {
            engine.spawnEmpArc(ship, projectile.getLocation(), ship, target, DamageType.ENERGY,
                    projectile.getDamageAmount() , projectile.getEmpAmount(),
                    weapon.getRange() * 1.2f, null, projectile.getCollisionRadius() / 1.5f,
                    new Color(210, 219, 255, 155),
                    new Color(255, 255, 255, 255));
        }
        entities.clear();
        engine.removeEntity(projectile);
    }

    private boolean isInRange(CombatEntityAPI entity, WeaponAPI weapon) {
        Vector2f wloc = weapon.getLocation();
        Vector2f eloc = entity.getLocation();
        float linkAngle = Misc.getAngleInDegrees(wloc, eloc);
        float currAngle = weapon.getCurrAngle();
        if (linkAngle < 0) {
            linkAngle += 360f;
        }
        float diff = linkAngle - currAngle;
        if (diff > 180f)
            diff = linkAngle - currAngle - 360f;
        float dis = Misc.getDistance(wloc, eloc);
        float JudgeAngle = 30f;
        if (weapon.getSpec().hasTag("spread_arc")) JudgeAngle = 60f;
        return (Math.abs(diff) < JudgeAngle) && (dis < (weapon.getRange() + entity.getCollisionRadius()));
    }
}
