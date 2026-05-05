package data.weapons.scripts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;


public class HSIMassProjector implements OnFireEffectPlugin {
    public HSIMassProjector() {

    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        projectile.setDamageAmount(0f);
        projectile.setCollisionClass(CollisionClass.SHIP);
        projectile.setHitpoints(200f);
        projectile.setCollisionRadius(2f);
        Vector2f move = projectile.getVelocity();
        CombatFleetManagerAPI manager = engine.getFleetManager(weapon.getShip().getOwner());
        manager.setSuppressDeploymentMessages(true);
        ShipAPI proj = manager.spawnShipOrWing("HSI_MassProjectile_variant", projectile.getLocation(), projectile.getFacing());
        proj.getVelocity().x = move.x;
        proj.getVelocity().y = move.y;
        engine.removeEntity(projectile);
        manager.setSuppressDeploymentMessages(false);
    }
}