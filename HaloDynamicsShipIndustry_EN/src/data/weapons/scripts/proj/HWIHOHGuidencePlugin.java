package data.weapons.scripts.proj;

import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.targetSeeking;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;

public class HWIHOHGuidencePlugin extends BaseEveryFrameCombatPlugin {
    protected DamagingProjectileAPI proj;
    protected CombatEntityAPI target;
    protected float elapsed = 0;
    protected float speed = 0;

    public HWIHOHGuidencePlugin(DamagingProjectileAPI proj, CombatEntityAPI target) {
        this.proj = proj;
        this.target = target;
    }

    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCombatEngine().isPaused())
            return;
        elapsed += amount;
        if (target == null) {
            target = repickTarget();
            if (target == null) {
                Global.getCombatEngine().removePlugin(this);
                return;
            }
        }
        if (target instanceof ShipAPI && !((ShipAPI) target).isAlive()
                || !Global.getCombatEngine().isEntityInPlay(target)) {
            proj.setAngularVelocity(0f);
            Global.getCombatEngine().removePlugin(this);
            return;
        }
        if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
            Global.getCombatEngine().removePlugin(this);
            return;
        }
        if (elapsed < 0.2f)
            return;
        float length = proj.getVelocity().length();
        Vector2f tloc = AIUtils.getBestInterceptPoint(proj.getLocation(), length, target.getLocation(),
                target.getVelocity());
        if (tloc == null)
            return;
        float rotation = MathUtils.getShortestRotation(VectorUtils.getFacingStrict(proj.getVelocity()),
                VectorUtils.getAngleStrict(proj.getLocation(), tloc));
        float facing = proj.getFacing();
        if (rotation > 5)
            rotation = 5;
        if (rotation < -5)
            rotation = -5;
        facing += rotation;
        if (Math.abs(rotation) > 1f) {
            proj.setFacing(facing);
            proj.getVelocity().set(MathUtils.getPoint(null, length, facing));
        }
    }

    public CombatEntityAPI repickTarget() {
        return MagicTargeting.pickShipTarget(proj.getSource(), targetSeeking.LOCAL_RANDOM,
                (int) (2000 - proj.getElapsed() * 1200), 180, 0, 1, 1, 10, 8);
    }
}
