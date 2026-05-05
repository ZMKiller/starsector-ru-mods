package data.kit;

import com.fs.starfarer.api.combat.AmmoTrackerAPI;
import com.fs.starfarer.api.util.FaderUtil;

public class HSIExtraAmmoTracker implements AmmoTrackerAPI {
    private int ammo;
    private int max;
    private int reloadSize;
    private float regen;
    private boolean isRegen;
    private FaderUtil regenProgress;
    private FaderUtil cd;

    public HSIExtraAmmoTracker(int maxAmmo, float regen, int reloadSize, float cd) {
        this.max = maxAmmo;
        this.regen = regen;
        this.reloadSize = reloadSize;
        ammo = maxAmmo;
        isRegen = regen > 0;
        if (isRegen)
            regenProgress = new FaderUtil(0f, reloadSize * (1 / regen));
        this.cd = new FaderUtil(0f, cd);
    }

    @Override
    public void addOneAmmo() {
        ammo++;
    }

    public void advance(float amount) {
        regenProgress.advance(amount);
        cd.advance(amount);
        if (regenProgress.isFadedOut()) {
            ammo = Math.min(max, ammo + reloadSize);
            if (ammo < max) {
                regenProgress.setBrightness(1f);
                regenProgress.fadeOut();
            }
        }
    }

    public void Fire(){
        deductOneAmmo();
        cd.setBrightness(1);
        cd.fadeOut();
        if(regenProgress.isFadedOut()) regenProgress.setBrightness(1);
        regenProgress.fadeOut();
    }

    public boolean isAvailable() {
        return cd.isFadedOut();
    }

    public float getCooldownRemaining(){
        return cd.getDurationOut()*cd.getBrightness();
    }

    @Override
    public boolean deductOneAmmo() {
        if (ammo > 0) {
            ammo--;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getAmmo() {
        return ammo;
    }

    @Override
    public float getAmmoPerSecond() {
        return regen;
    }

    @Override
    public int getMaxAmmo() {
        return max;
    }

    @Override
    public float getReloadProgress() {
        return regenProgress.getBrightness();
    }

    @Override
    public float getReloadSize() {
        return reloadSize;
    }

    @Override
    public void resetAmmo() {
        ammo = max;
    }

    @Override
    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    @Override
    public void setAmmoPerSecond(float regen) {
        this.regen = regen;
    }

    @Override
    public void setMaxAmmo(int max) {
        this.max = max;
    }

    @Override
    public void setReloadProgress(float progress) {
        regenProgress.setBrightness(progress);
    }

    @Override
    public void setReloadSize(float size) {
        this.reloadSize = (int) size;
    }

    @Override
    public boolean usesAmmo() {
        return true;
    }
}
