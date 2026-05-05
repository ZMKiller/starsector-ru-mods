package data.weapons.scripts.beam;

import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;


public class HWIFallingStarExplosionEffect extends RiftCascadeMineExplosion {
    public static String SIZE_MULT_KEY = "core_sizeMultKey";

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        Float sizeMult = null;
        if (originalProjectile.getCustomData() != null) {
            sizeMult = (Float) originalProjectile.getCustomData().get(SIZE_MULT_KEY);
        }
        if (sizeMult == null) sizeMult = 1f;

        NegativeExplosionVisual.NEParams p = createStandardRiftParams("HWI_FallingStar_MineLayer", 25f * sizeMult);
        p.fadeOut = 1f;
        //p.hitGlowSizeMult = 0.5f;
        spawnStandardRift(explosion, p);
    }
}
