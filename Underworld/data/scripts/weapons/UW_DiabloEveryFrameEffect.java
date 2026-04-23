package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_DiabloEveryFrameEffect implements EveryFrameWeaponEffectPlugin {

    private static final Vector2f OFFSET_1 = new Vector2f(-58.5f, 20f);
    private static final Vector2f OFFSET_2 = new Vector2f(-64.5f, 0f);
    private static final Vector2f OFFSET_3 = new Vector2f(-58.5f, -20f);
    private static final Color SMOKE_COLOR = new Color(40, 10, 5, 30);
    private static final float SMOKE_DURATION = 1.5f;
    private static final float SMOKE_SIZE = 75.0f;
    private static final Color FLAME_COLOR = new Color(255, 100, 100, 255);
    private static final float FLAME_DURATION = 0.4f;
    private static final float FLAME_SIZE = 15.0f;
    private static final Color FLAME2_COLOR = new Color(255, 150, 75, 255);
    private static final float FLAME2_DURATION = 0.5f;
    private static final float FLAME2_SIZE = 30.0f;

    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;
    private int lastAmmo = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        float chargeLevel = weapon.getChargeLevel();
        float cooldownRemaining = weapon.getCooldownRemaining();
        int ammo = weapon.getAmmo();

        if ((chargeLevel > lastChargeLevel) || (lastCooldownRemaining < cooldownRemaining) || (ammo < lastAmmo)) {
            Vector2f weaponLocation = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            float weaponFacing = weapon.getCurrAngle();
            Vector2f shipVelocity = ship.getVelocity();
            Vector2f muzzleLocation;
            switch (MathUtils.getRandomNumberInRange(1, 3)) {
                case 1 ->
                    muzzleLocation = new Vector2f(OFFSET_1);
                case 2 ->
                    muzzleLocation = new Vector2f(OFFSET_2);
                case 3 ->
                    muzzleLocation = new Vector2f(OFFSET_3);
                default -> {
                    return;
                }
            }
            VectorUtils.rotate(muzzleLocation, weaponFacing, muzzleLocation);
            Vector2f.add(muzzleLocation, weaponLocation, muzzleLocation);

            if ((lastCooldownRemaining < cooldownRemaining) || (ammo < lastAmmo)) {
                Vector2f smokeVelocity = MathUtils.getPointOnCircumference(shipVelocity, -50f * ((float) Math.random() * 0.5f + 0.75f), weaponFacing);
                Vector2f flameVelocity = MathUtils.getPointOnCircumference(shipVelocity, -50f * ((float) Math.random() * 1f + 0.5f), weaponFacing);
                engine.addSmokeParticle(muzzleLocation, smokeVelocity, SMOKE_SIZE * ((float) Math.random() * 0.5f + 0.75f), 0.3f, SMOKE_DURATION
                        * ((float) Math.random() * 0.5f + 1.25f), SMOKE_COLOR);
                engine.spawnExplosion(muzzleLocation, flameVelocity, FLAME_COLOR, FLAME_SIZE * ((float) Math.random() * 1f + 0.5f),
                        FLAME_DURATION * ((float) Math.random() * 0.5f + 0.75f));
                engine.spawnExplosion(muzzleLocation, flameVelocity, FLAME2_COLOR, FLAME2_SIZE * ((float) Math.random() * 0.5f + 0.75f),
                        FLAME2_DURATION * ((float) Math.random() * 0.3f + 0.85f));
            }
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;
        lastAmmo = ammo;
    }
}
