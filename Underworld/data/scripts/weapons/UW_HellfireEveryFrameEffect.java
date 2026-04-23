package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_HellfireEveryFrameEffect implements EveryFrameWeaponEffectPlugin {

    private static final Vector2f OFFSET_1 = new Vector2f(-58.5f, 20f);
    private static final Vector2f OFFSET_2 = new Vector2f(-64.5f, 0f);
    private static final Vector2f OFFSET_3 = new Vector2f(-58.5f, -20f);
    private static final Color SMOKE_COLOR_1 = new Color(100, 75, 70, 200);
    private static final Color SMOKE_COLOR_2 = new Color(40, 40, 40, 100);
    private static final float SMOKE_DURATION = 2.5f;
    private static final float SMOKE_SIZE = 50.0f;

    private float lastChargeLevel = 0.0f;
    private float lastCooldownRemaining = 0.0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        float chargeLevel = weapon.getChargeLevel();
        float cooldownRemaining = weapon.getCooldownRemaining();

        if (chargeLevel > lastChargeLevel || lastCooldownRemaining < cooldownRemaining) {
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

            Vector2f smokeVelocity = MathUtils.getPointOnCircumference(shipVelocity, -20f * ((float) Math.random() * 0.5f + 0.75f), weaponFacing);
            if (lastCooldownRemaining < cooldownRemaining) {
                engine.addSmokeParticle(muzzleLocation, smokeVelocity, SMOKE_SIZE * ((float) Math.random() * 0.5f + 1.25f), 0.3f, SMOKE_DURATION
                        * ((float) Math.random()
                        * 0.5f + 1.25f),
                        SMOKE_COLOR_2);
                engine.addSmokeParticle(muzzleLocation, smokeVelocity, SMOKE_SIZE * ((float) Math.random() * 1f + 0.5f), 0.5f, SMOKE_DURATION
                        * ((float) Math.random()
                        * 0.5f + 0.75f),
                        SMOKE_COLOR_1);
            }
        }

        lastChargeLevel = chargeLevel;
        lastCooldownRemaining = cooldownRemaining;
    }
}
