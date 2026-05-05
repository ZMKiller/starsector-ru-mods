package data.weapons.scripts.proj;

import java.util.ArrayList;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;

public class HWISmartAmmoScript extends BaseEveryFrameCombatPlugin {
    private List<HWISmartAmmo> projs = new ArrayList<HWISmartAmmo>();
    private CombatEngineAPI engine = Global.getCombatEngine();

    public class HWISmartAmmo {
        DamagingProjectileAPI proj;
        CombatEntityAPI target;
        float elapsed = 0;
        float maxTime = 0;

        public HWISmartAmmo(CombatEntityAPI target, DamagingProjectileAPI proj) {
            this.proj = proj;
            this.target = target;
            maxTime = proj.getProjectileSpec().getMaxRange()/proj.getMoveSpeed();
        }

        public void advance(float amount){
            elapsed+=amount;
        }
    }

    public HWISmartAmmoScript() {

    }

    public static HWISmartAmmoScript getInstance() {
        if (Global.getCombatEngine().getCustomData().containsKey("HWISmartAmmoScript")) {
            return (HWISmartAmmoScript) Global.getCombatEngine().getCustomData().get("HWISmartAmmoScript");
        } else {
            HWISmartAmmoScript script = new HWISmartAmmoScript();
            Global.getCombatEngine().addPlugin(script);
            Global.getCombatEngine().getCustomData().put("HWISmartAmmoScript", script);
            return script;
        }
    }

    public void addProj(CombatEntityAPI target, DamagingProjectileAPI proj) {
        projs.add(new HWISmartAmmo(target, proj));
    }

    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCombatEngine().isPaused())
            return;
        List<HWISmartAmmo> toRemove = new ArrayList<>();
        for (HWISmartAmmo ammo : projs) {
            CombatEntityAPI target = ammo.target;
            if (target == null || !engine.isEntityInPlay(target)
                    || ((target instanceof ShipAPI) && !((ShipAPI) target).isAlive())) {
                toRemove.add(ammo);
                continue;
            }
            DamagingProjectileAPI proj = ammo.proj;
            if (proj.isExpired() || proj.isFading() || !engine.isEntityInPlay(proj)) {
                toRemove.add(ammo);
                continue;
            }
            ammo.advance(amount);
            Vector2f aimingPoint = AIUtils.getBestInterceptPoint(proj.getLocation(), proj.getVelocity().length(),
            target.getLocation(), target.getVelocity());
            if (proj.getWeapon() != null /*&& proj.getWeapon().getSpec().hasTag("HSI_bypassShield")*/&&((ammo.maxTime-ammo.elapsed)*proj.getMoveSpeed()>=300f)) {
                if (target instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) target;
                    if (s.getShield() != null) {
                        if (s.getShield().isOn() && s.getShield().isWithinArc(proj.getLocation())) {
                            float rotationL = MathUtils.getShortestRotation(
                                    VectorUtils.getAngle(s.getShieldCenterEvenIfNoShield(), proj.getLocation()),
                                    s.getShield().getFacing() + s.getShield().getActiveArc() / 2f);
                            float rotationR = MathUtils.getShortestRotation(
                                    VectorUtils.getAngle(s.getShieldCenterEvenIfNoShield(), proj.getLocation()),
                                    s.getShield().getFacing() - s.getShield().getActiveArc() / 2f);
                            if (Math.abs(rotationL) > Math.abs(rotationR)) {
                                aimingPoint = MathUtils.getPoint(s.getShieldCenterEvenIfNoShield(),
                                        s.getShieldRadiusEvenIfNoShield() + 200f,
                                        VectorUtils.getAngle(s.getShieldCenterEvenIfNoShield(), proj.getLocation())
                                                + 5f);
                            }
                        }
                    }
                }
            }
            float rotation = MathUtils.getShortestRotation(VectorUtils.getFacingStrict(proj.getVelocity()),
                    VectorUtils.getAngleStrict(proj.getLocation(), aimingPoint));
            float facing = proj.getFacing();
            float rotationSpeedMax = 10;
            if (rotation > rotationSpeedMax)
                rotation = rotationSpeedMax;
            if (rotation < -rotationSpeedMax)
                rotation = -rotationSpeedMax;
            facing += rotation;
            if (Math.abs(rotation) > 1f) {
                proj.setFacing(facing);
                proj.getVelocity().set(MathUtils.getPoint(null, proj.getVelocity().length(), facing));
            }
        }
        projs.removeAll(toRemove);
    }

}