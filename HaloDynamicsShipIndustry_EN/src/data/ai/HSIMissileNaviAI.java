package data.ai;

import javax.print.attribute.standard.MediaSize.NA;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.magiclib.ai.MagicMissileAI;
import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.missilePriority;
import org.magiclib.util.MagicTargeting.targetSeeking;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;

import data.kit.AjimusUtils;

public class HSIMissileNaviAI implements GuidedMissileAI, MissileAIPlugin {
    private CombatEntityAPI target;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private MissileAPI self;
    private float lastQ;
    private float lastD;
    private float NAVI = 4;

    public HSIMissileNaviAI(MissileAPI missile) {
        self = missile;
        if (missile.getWeapon() != null) {
            target = AjimusUtils.PickAutoFireTarget(missile.getWeapon());
        } else {
            checkTarget();
        }
        initNavi();
    }

    public HSIMissileNaviAI(MissileAPI missile, CombatEntityAPI target) {
        this(missile);
        setTarget(target);
    }

    public void advance(float amount) {
        if (checkTarget())
            return;
        if (engine.isPaused())
            return;
        if (target == null)
            return;
        // basicAiming
        if (Math.abs(MathUtils.getShortestRotation(VectorUtils.getFacing(self.getVelocity()),
                VectorUtils.getFacing(target.getVelocity()))) >= 90f) {
            float desireD = VectorUtils.getAngle(self.getLocation(), target.getLocation());
            engine.headInDirectionWithoutTurning(self, desireD, self.getMaxSpeed() * 1.1f);
            float rotation = MathUtils.getShortestRotation(self.getFacing(), desireD);
            if (Math.abs(rotation) < 0.3f) {
                self.setAngularVelocity(self.getAngularVelocity() * 0.05f);
            } else if (Math.abs(rotation) < 1.5f) {
                self.setAngularVelocity(self.getAngularVelocity() * 0.5f);
            } else if (Math.abs(rotation) < 3f) {
                self.setAngularVelocity(self.getAngularVelocity() * 0.8f);
            } else {
                if (rotation > 0) {
                    self.giveCommand(ShipCommand.TURN_RIGHT);
                } else {
                    self.giveCommand(ShipCommand.TURN_LEFT);
                }
            }
            float currQ = getQ();
            float currD = getD();
            lastD = currD;
            lastQ = currQ;
        } else {
            float currQ = getQ();
            float currD = getD();
            float dQ = lastQ - currQ;
            float dD = NAVI * dQ;
            float desireD = lastD + dD;
            Global.getLogger(this.getClass()).info(currQ + "-" + currD + "-" + lastQ + "-" + lastD + "-" + desireD);
            engine.headInDirectionWithoutTurning(self, desireD, self.getMaxSpeed() * 1.1f);
            float rotation = MathUtils.getShortestRotation(self.getFacing(), desireD);
            if (Math.abs(rotation) < 0.3f) {
                self.setAngularVelocity(self.getAngularVelocity() * 0.05f);
            } else if (Math.abs(rotation) < 1.5f) {
                self.setAngularVelocity(self.getAngularVelocity() * 0.5f);
            } else if (Math.abs(rotation) < 3f) {
                self.setAngularVelocity(self.getAngularVelocity() * 0.8f);
            } else {
                if (rotation > 0) {
                    self.giveCommand(ShipCommand.TURN_RIGHT);
                } else {
                    self.giveCommand(ShipCommand.TURN_LEFT);
                }
            }
            lastD = currD;
            lastQ = currQ;
        }
    }

    public CombatEntityAPI getTarget() {
        return target;
    }

    public void setTarget(CombatEntityAPI target) {
        if (target instanceof MissileAPI && ((MissileAPI) target).isFlare())
            return;
        this.target = target;
    }

    private boolean checkTarget() {
        if ((target == null || !engine.isEntityInPlay(target)) && !self.getWeapon().hasAIHint(AIHints.PD_ONLY)) {
            target = MagicTargeting.pickMissileTarget(self, targetSeeking.LOCAL_RANDOM,
                    ((int) (self.getMaxRange() * (self.getFlightTime() / self.getMaxFlightTime()))), 180, 10, 8, 5, 3,
                    1);
            if (target != null) {
                initNavi();
            }
            return true;
        }
        if (target == null || !engine.isEntityInPlay(target)) {
            target = MagicTargeting.randomMissile(self, missilePriority.RANDOM, self.getLocation(),
                    (int) self.getFacing(), 180,
                    ((int) (self.getMaxRange() * (self.getFlightTime() / self.getMaxFlightTime()))));
            if (target != null) {
                initNavi();
            }
            return true;
        }
        return false;
    }

    private void initNavi() {
        if (target != null)
            lastQ = getQ();
        lastD = getD();
    }

    private float getQ() {
        float rt = MathUtils.getShortestRotation(0,
                VectorUtils.getAngleStrict(self.getLocation(), target.getLocation()));
        if (rt < 0)
            rt = 360 + rt;
        return rt;
    }

    private float getD() {
        float rt = VectorUtils.getFacing(self.getVelocity());
        if (rt > 0)
            rt = 360 - rt;
        return rt;
    }
}
