package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class UW_ShredderEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final float FRAME_RATE = 35f;
    private static final float MIN_FRAME_RATE = 15f;

    private float charge = 0f;
    private float cooldown = 0f;
    private float delay = 1f / MIN_FRAME_RATE;
    private int frame = 0;
    private int startFrame = 0;
    private float timer = 0f;

    private boolean firing = false;
    private boolean spinning = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship = weapon.getShip();

        if (weapon.getSlot().isHidden()) {
            return;
        }

        AnimationAPI theAnim = weapon.getAnimation();

        float spinUp = 0.02f;
        float mult = ship.getMutableStats().getBallisticRoFMult().getModifiedValue();

        float minDelay = 1f / (FRAME_RATE * mult);
        float maxDelay = 1f / MIN_FRAME_RATE;
        int maxFrame = theAnim.getNumFrames();

        if (weapon.getChargeLevel() > 0f && (weapon.getChargeLevel() > charge || weapon.getChargeLevel() >= 1f)) {
            if (!firing) {
                if (frame == 0 || frame > 7) {
                    startFrame = 0;
                } else {
                    startFrame = 7;
                }
            }
            cooldown = weapon.getCooldownRemaining();
            delay = minDelay;
            firing = true;
            spinning = true;
        } else {
            firing = false;
        }

        if (firing) {
            if (weapon.getCooldownRemaining() > cooldown) {
                if (frame == 0 || frame > 7) {
                    startFrame = 0;
                } else {
                    startFrame = 7;
                }
            }

            float x = 1f - (weapon.getCooldownRemaining() / weapon.getCooldown());
            frame = startFrame + (int) Math.floor(x * 7);
            if (frame == maxFrame) {
                frame = 0;
            }
        } else {
            timer += amount;
            while (timer >= delay) {
                timer -= delay;

                if (delay >= maxDelay) {
                    delay = maxDelay;
                    spinning = false;
                } else {
                    delay += 0.2f * spinUp;
                }

                if (spinning || (frame != 0 && frame != 7)) {
                    frame++;
                    if (frame == maxFrame) {
                        frame = 0;
                    }
                }
            }
        }

        theAnim.setFrameRate(0f);
        theAnim.setFrame(frame);

        charge = weapon.getChargeLevel();
        cooldown = weapon.getCooldownRemaining();
    }
}
