package data.ai;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HSIThreatAnalysisAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;
    // private HSITurbulanceShieldListenerV2 shield = null;
    private List<WeaponAPI> threats = new ArrayList<>();
    private List<MissileAPI> missileThreats = new ArrayList<>();
    private IntervalUtil threatUpdater = new IntervalUtil(0.8f, 1f);
    private IntervalUtil missileThreatUpdater = new IntervalUtil(0.1f, 0.2f);
    private boolean hasHeavyEnemyNearby = false;
    private float useLimit = 2;
    private float maxRangeSelf = 0;
    private float minRangeSelf = 1000;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            float range = weapon.getRange();
            if (maxRangeSelf < range)
                maxRangeSelf = range;
            if (minRangeSelf > range)
                minRangeSelf = range;
        }
        if (ship.getCaptain() != null) {
            switch (ship.getCaptain().getPersonalityAPI().getId()) {
                case Personalities.RECKLESS:
                    useLimit = 0;
                    break;
                case Personalities.AGGRESSIVE:
                    useLimit = 1;
                    break;
                case Personalities.STEADY:
                    useLimit = 1;
                default:
                    break;
            }
        } else {
            useLimit = 1;
        }
    }

    // private float sinceLast = 0f;

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (system.getCooldownRemaining() > 0)
            return;
        if (system.isOutOfAmmo())
            return;
        if (system.isActive())
            return;
        if (target == null)
            return;
        if (system.getAmmo() > ((missileThreats.size() > 0) ? 1 : useLimit)) {
            ship.useSystem();
            return;
        }
        threatUpdater.advance(amount);
        if (threatUpdater.intervalElapsed()) {
            updateThreats();
        }
        boolean shouldDo = false;
        missileThreatUpdater.advance(amount);
        if (missileThreatUpdater.intervalElapsed()) {
            shouldDo = shouldDo || updateMissileThreats();
        }
        shouldDo = shouldDo || judgeThreats() || hasHeavyEnemyNearby;
        if (shouldDo) {
            ship.useSystem();
        }
    }

    protected void updateThreats() {
        threats.clear();
        hasHeavyEnemyNearby = false;
        int ordinals = 0;
        for (ShipAPI e : AIUtils.getNearbyEnemies(ship, 2500f)) {
            float dist = Misc.getDistance(e.getLocation(), ship.getLocation());
            for (WeaponAPI weapon : e.getAllWeapons()) {
                if (weapon.isDisabled() || weapon.isDecorative() || weapon.getType() == WeaponType.MISSILE)
                    continue;
                if (weapon.usesAmmo() && weapon.getAmmo() <= 0)
                    continue;
                if (weapon.getRange() <= 0.9f * dist)
                    continue;
                if (weapon.getSpec().getAIHints().contains(AIHints.DANGEROUS)
                        || weapon.getSpec().getAIHints().contains(AIHints.STRIKE))
                    threats.add(weapon);
                if (weapon.getDamage().getDamage() >= 500f)
                    threats.add(weapon);
            }
            if (ship.getCaptain() != null) {
                switch (ship.getCaptain().getPersonalityAPI().getId()) {
                    case Personalities.RECKLESS:
                        // do nothing
                        break;
                    case Personalities.AGGRESSIVE:
                        if (dist < maxRangeSelf * 0.3f)
                            ordinals += (e.getHullSize().ordinal() - 1);
                        break;
                    case Personalities.STEADY:
                        if (dist < maxRangeSelf * 0.7f)
                            ordinals += (e.getHullSize().ordinal() - 1);
                        break;
                    default:
                        if (dist < maxRangeSelf * 0.9f)
                            ordinals += (e.getHullSize().ordinal() - 1);
                        break;
                }
            }else{
                ordinals += (e.getHullSize().ordinal() - 1);
            }
        }
        hasHeavyEnemyNearby = ordinals >= ship.getHullSize().ordinal();
    }

    protected boolean updateMissileThreats() {
        boolean shouldDo = false;
        for (MissileAPI m : AIUtils.getNearbyEnemyMissiles(ship, 900f)) {
            if (m.isFading() || m.isFizzling() || m.isExpired())
                continue;
            float judgeRange = 200f;
            if (m.getDamage().getDamage() <= 500f)
                continue;
            if (m.getWeaponSpec() != null
                    && m.getWeaponSpec().getAIHints().containsAll(EnumSet.of(AIHints.DANGEROUS, AIHints.STRIKE))) {
                judgeRange = 500f;
            } else if (m.getSpec().getTypeString().equals("MIRV")) {
                try {
                    judgeRange = m.getSpec().getBehaviorJSON().getInt("splitRange") * 1.1f;
                } catch (Exception e) {
                    judgeRange = 550f;
                }
            }
            if (Misc.getDistance(m.getLocation(), ship.getLocation())-ship.getCollisionRadius() <= judgeRange) {
                shouldDo = true;
                break;
            }
        }
        return shouldDo;
    }

    protected boolean judgeThreats() {
        boolean shouldDo = false;
        for (WeaponAPI weapon : threats) {
            if (weapon.isFiring()) {
                shouldDo = true;
                break;
            }
        }
        return shouldDo;
    }
}