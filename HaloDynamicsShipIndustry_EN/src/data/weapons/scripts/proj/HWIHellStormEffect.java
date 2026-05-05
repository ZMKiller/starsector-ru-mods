package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.Iterator;
import java.util.List;

public class HWIHellStormEffect implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        engine.addPlugin(new HWIHellStormScript(projectile));
    }

    public static class HWIHellStormScript extends BaseEveryFrameCombatPlugin {
        private MissileAPI proj;
        private final CombatEngineAPI engine = Global.getCombatEngine();
        private float safe = 1f;
        private boolean safeOut = false;
        private IntervalUtil fuseTimer = new IntervalUtil(0.5f, 0.5f);

        private IntervalUtil checkTimer = new IntervalUtil(0.05f,0.1f);
        private boolean fuseSet = false;

        public HWIHellStormScript(DamagingProjectileAPI proj) {
            this.proj = (MissileAPI) proj;
        }

        private boolean isAlive() {
            return !proj.isExpired() && !proj.isFading() && engine.isEntityInPlay(proj);
        }

        public void advance(float amount, List<InputEventAPI> events) {
            if (!isAlive()) {
                engine.removePlugin(this);
            }
            if (engine.isPaused())
                return;
            if (!safeOut && proj.getFlightTime() > safe)
                safeOut = true;
            if (safeOut&&!fuseSet) {
                checkTimer.advance(amount);
                if(checkTimer.intervalElapsed()) {
                        Iterator<Object> o = engine.getShipGrid().getCheckIterator(proj.getLocation(), 200f, 200f);
                        boolean allFighter = true;
                        while (o.hasNext() && allFighter) {
                            Object p = o.next();
                            if (p instanceof ShipAPI) {
                                ShipAPI s = (ShipAPI) p;
                                if (!s.isFighter() && s.getOwner() != proj.getOwner())
                                    allFighter = false;
                            }
                        }
                        if (!allFighter) {
                            float fuse = (float) (0.5f + 0.5f * Math.random());
                            fuseTimer = new IntervalUtil(fuse, fuse);
                            fuseSet = true;
                        }
                }
            }
            if (fuseSet) {
                proj.setCollisionClass(CollisionClass.NONE);
                fuseTimer.advance(amount);
                if (fuseTimer.intervalElapsed()) {
                    proj.explode();
                    engine.removeEntity(proj);
                    engine.removePlugin(this);
                }
            }
        }
    }
}