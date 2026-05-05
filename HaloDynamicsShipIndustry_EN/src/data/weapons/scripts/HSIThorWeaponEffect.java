package data.weapons.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.kit.AjimusUtils;
import data.weapons.scripts.beam.HWIFMBeamEffect;

public class HSIThorWeaponEffect implements EveryFrameWeaponEffectPlugin {
    private IntervalUtil FlickerTimer = new IntervalUtil(0.7f, 0.8f);
    private IntervalUtil LinkFlicker = new IntervalUtil(0.18f, 0.25f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused())
            return;
        if(!weapon.getShip().isAlive()) return;
        FlickerTimer.advance(amount);
        LinkFlicker.advance(amount);
        if (weapon.getAmmo() > 0 && weapon.getCooldownRemaining() <= 0) {
            if (LinkFlicker.intervalElapsed()) {
                EmpArcEntityAPI a = engine.spawnEmpArcVisual(
                        AjimusUtils.getEngineCoordFromRelativeCoord(weapon.getShip().getLocation(),
                                new Vector2f(-10f, 18f), weapon.getShip().getFacing()),
                        weapon.getShip(),
                        weapon.getFirePoint(0),
                        weapon.getShip(),
                        8 + (float) Math.random() * 4f, HWIFMBeamEffect.STANDARD_RIFT_COLOR, Color.WHITE);
                a.setSingleFlickerMode();
                a = engine.spawnEmpArcVisual(
                        AjimusUtils.getEngineCoordFromRelativeCoord(weapon.getShip().getLocation(),
                                new Vector2f(-10f, -18f), weapon.getShip().getFacing()),
                        weapon.getShip(),
                        weapon.getFirePoint(0),
                        weapon.getShip(),
                        8 + (float) Math.random() * 4f, HWIFMBeamEffect.STANDARD_RIFT_COLOR, Color.WHITE);
                a.setSingleFlickerMode();
            }
            if (FlickerTimer.intervalElapsed()) {
                /*EmpArcEntityAPI a = engine.spawnEmpArcVisual(weapon.getFirePoint(0), weapon.getShip(),
                        Misc.getPointAtRadius(weapon.getFirePoint(0), 1 + (float) (1* Math.random())),
                        weapon.getShip(),
                        0.5f + (float) Math.random() * 0.5f, HWIFMBeamEffect.STANDARD_RIFT_COLOR, Color.WHITE);
                a.setSingleFlickerMode();*/
                for (MissileAPI m : AIUtils.getNearbyEnemyMissiles(weapon.getShip(), 500f)) {
                    if (Math.random() > 0.95f) {
                        engine.spawnEmpArc(weapon.getShip(), weapon.getFirePoint(0), weapon.getShip(), m,
                                DamageType.ENERGY,
                                100f, 0f,
                                600f, null, 8,
                                new Color(210, 219, 255, 155),
                                new Color(255, 255, 255, 255));
                    }
                }
            } else if (Math.random() > 0.01f) {
                /*EmpArcEntityAPI a = engine.spawnEmpArcVisual(weapon.getFirePoint(0), weapon.getShip(),
                        Misc.getPointAtRadius(weapon.getFirePoint(0), 1 + (float) (1 * Math.random())),
                        weapon.getShip(),
                        0.5f + (float) Math.random() * 0.5f, HWIFMBeamEffect.STANDARD_RIFT_COLOR, Color.WHITE);
                a.setSingleFlickerMode();*/
                for (MissileAPI m : AIUtils.getNearbyEnemyMissiles(weapon.getShip(), 500f)) {
                    if (Math.random() > 0.92f) {
                        engine.spawnEmpArc(weapon.getShip(), weapon.getFirePoint(0), weapon.getShip(), m,
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
