package data.weapons.scripts.proj;

import java.util.List;

import org.lazywizard.lazylib.combat.AIUtils;
import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.weapons.scripts.beam.HWIFMBeamEffect;

public class HSIThorWeaponProjEffect implements OnFireEffectPlugin{
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (projectile instanceof MissileAPI)
            engine.addPlugin(new HSIThorWeaponProjPlugin((MissileAPI) projectile));
    }

    public class HSIThorWeaponProjPlugin extends BaseEveryFrameCombatPlugin {
        private MissileAPI missile;
        private IntervalUtil FlickerTimer = new IntervalUtil(0.22f, 0.38f);
        private CombatEngineAPI engine = Global.getCombatEngine();

        public HSIThorWeaponProjPlugin(MissileAPI m) {
            this.missile = m;
        }

        public void advance(float amount, List<InputEventAPI> events) {
            if (engine.isPaused())
                return;
            if(missile.getHitpoints()<0||!Global.getCombatEngine().isEntityInPlay(missile)||missile.isExpired()){
                engine.removePlugin(this);
                return;
            }
            FlickerTimer.advance(amount);
            if (!missile.isExpired()) {
                if (FlickerTimer.intervalElapsed()) {
                    EmpArcEntityAPI a = engine.spawnEmpArcVisual(missile.getLocation(), missile,
                            Misc.getPointAtRadius(missile.getLocation(), 8 + (float) (3 * Math.random())),
                            missile,
                            6 + (float) Math.random() * 3f, HWIFMBeamEffect.STANDARD_RIFT_COLOR, Color.WHITE);
                    a.setSingleFlickerMode();
                    for (MissileAPI m : AIUtils.getNearbyEnemyMissiles(missile, 500f)) {
                        if (Math.random() > 0.92f) {
                            engine.spawnEmpArc(missile.getSource(), missile.getLocation(), missile, m,
                                    DamageType.ENERGY,
                                    100f, 0f,
                                    600f, null, 8,
                                    new Color(210, 219, 255, 155),
                                    new Color(255, 255, 255, 255));
                        }
                    }
                } else if (Math.random() > 0.01f) {
                    EmpArcEntityAPI a = engine.spawnEmpArcVisual(missile.getLocation(), missile,
                            Misc.getPointAtRadius(missile.getLocation(), 8 + (float) (3 * Math.random())),
                            missile,
                            6 + (float) Math.random() * 3f, HWIFMBeamEffect.STANDARD_RIFT_COLOR, Color.WHITE);
                    a.setSingleFlickerMode();
                    for (MissileAPI m : AIUtils.getNearbyEnemyMissiles(missile, 500f)) {
                        if (Math.random() > 0.95f) {
                            engine.spawnEmpArc(missile.getSource(), missile.getLocation(), missile, m,
                                    DamageType.ENERGY,
                                    100f, 0f,
                                    600f, null, 8,
                                    new Color(210, 219, 255, 155),
                                    new Color(255, 255, 255, 255));
                        }
                    }
                }
            }
        }
    }
}
