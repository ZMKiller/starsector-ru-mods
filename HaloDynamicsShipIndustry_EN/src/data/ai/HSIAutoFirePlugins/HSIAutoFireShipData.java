package data.ai.HSIAutoFirePlugins;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MissileAPI;
import data.ai.HSIThreatSharedData;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;


public class HSIAutoFireShipData implements AdvanceableListener {
    private ShipAPI ship;
    private List<HSIAutoFireTargetingUnitData> ShipTargetingData = new ArrayList<>();//No using
    private List<HSIAutoFireTargetingUnitData> MissileTargetingData = new ArrayList<>();
    private List<HSIAutoFireTargetingUnitData> FighterTargetingData = new ArrayList<>();
    private IntervalUtil scanTimer = new IntervalUtil(0.08f, 0.15f);

    private float maxRange = 3000f;

    public enum TYPE {
        SHIP, MISSILE, FIGHTER
    }

    public static HSIAutoFireShipData getInstance(ShipAPI ship) {
        if (ship.hasListenerOfClass(HSIAutoFireShipData.class)) {
            return ship.getListeners(HSIAutoFireShipData.class).get(0);
        } else {
            HSIAutoFireShipData d = new HSIAutoFireShipData(ship);
            ship.addListener(d);
            return d;
        }
    }

    public HSIAutoFireShipData(ShipAPI ship){
        this.ship = ship;
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.getRange()*1.25f>maxRange){
                maxRange = weapon.getRange()*1.25f;
            }
        }
    }

    public HSIAutoFireTargetingUnitData getBestTargetingData(TYPE type,float dist) {
        switch (type) {
            case FIGHTER:
                for(HSIAutoFireTargetingUnitData d:FighterTargetingData){
                    if(d.getDist()<=dist){
                        return d;
                    }
                }
                break;
            case MISSILE:
                for(HSIAutoFireTargetingUnitData d:MissileTargetingData){
                    if(d.getDist()<=dist){
                        return d;
                    }
                }
                break;
            case SHIP:
                for(HSIAutoFireTargetingUnitData d:ShipTargetingData){
                    if(d.getDist()<=dist){
                        return d;
                    }
                }
                break;
            default:
                break;

        }
        return null;
    }

    public List<HSIAutoFireTargetingUnitData> getBestTargetingDataInRange(int num, TYPE type,float dist) {
        switch (type) {
            case FIGHTER:
                if (FighterTargetingData.size() <= num) {
                    return new ArrayList<>(FighterTargetingData);
                } else {
                    return new ArrayList<>(FighterTargetingData.subList(0, num - 1));
                }
            case MISSILE:
                if (MissileTargetingData.size() <= num) {
                    return new ArrayList<>(MissileTargetingData);
                } else {
                    return new ArrayList<>(MissileTargetingData.subList(0, num - 1));
                }
            case SHIP:
                if (ShipTargetingData.size() <= num) {
                    return new ArrayList<>(ShipTargetingData);
                } else {
                    return new ArrayList<>(ShipTargetingData.subList(0, num - 1));
                }
        }
        return new ArrayList<>();
    }

    public void advance(float amount) {
        if(!ship.isAlive()) return;
        scanTimer.advance(amount);
        if (scanTimer.intervalElapsed()) {
            FighterTargetingData.clear();
            //ShipTargetingData.clear();
            MissileTargetingData.clear();
            Iterator<Object> si = Global.getCombatEngine().getShipGrid().getCheckIterator(ship.getLocation(), maxRange, maxRange);
            while (si.hasNext()) {
                Object so = si.next();
                if (so instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) so;
                    if (s.getOwner() == ship.getOwner()) continue;
                    float distSqr = MathUtils.getDistanceSquared(s.getLocation(), ship.getLocation())+ship.getCollisionRadius();
                    if (s.isFighter()) {
                        if (s.getWing() != null) {
                            float damagePotential = 20f;
                            switch (s.getWing().getRole()) {
                                case BOMBER:
                                    for (WeaponAPI wpn : s.getUsableWeapons()) {
                                        if (wpn.getSpec().getAIHints()
                                                .containsAll(EnumSet.of(WeaponAPI.AIHints.DANGEROUS, WeaponAPI.AIHints.STRIKE)))
                                            damagePotential += wpn.getDerivedStats().getBurstDamage();
                                    }
                                    damagePotential *= 1.5f;
                                    break;
                                case SUPPORT:
                                    for (WeaponAPI wpn : s.getUsableWeapons()) {
                                        if (wpn.getRange() * wpn.getRange() < 2f * distSqr)
                                            damagePotential += wpn.getDerivedStats().getDps();
                                    }
                                    damagePotential *= 0.75f;
                                    break;
                                default:
                                    for (WeaponAPI wpn : s.getUsableWeapons()) {
                                        if (wpn.getSpec().getAIHints()
                                                .containsAll(EnumSet.of(WeaponAPI.AIHints.DANGEROUS, WeaponAPI.AIHints.STRIKE))) {
                                            damagePotential += wpn.getDerivedStats().getBurstDamage();
                                        } else {
                                            damagePotential += wpn.getDerivedStats().getDps();
                                        }
                                    }
                                    break;
                            }
                            float weight = damagePotential/2f;
                            FighterTargetingData.add(new HSIAutoFireTargetingUnitData(s,weight,MathUtils.getDistance(ship.getLocation(),s.getLocation())+ship.getCollisionRadius()));
                        }
                    }
                }
            }
            Iterator<Object> mi = Global.getCombatEngine().getMissileGrid().getCheckIterator(ship.getLocation(), maxRange, maxRange);
            while (mi.hasNext()) {
                Object mo = mi.next();
                if (mo instanceof MissileAPI) {
                    MissileAPI missile = (MissileAPI) mo;
                    if (missile.getOwner() == ship.getOwner())
                        continue;
                    if (missile.isFlare())
                        continue;
                    if (missile.isFizzling() || missile.isFading())
                        continue;
                    if (!missile.isGuided()) {
                        Vector2f closest = MathUtils.getNearestPointOnLine(ship.getLocation(), missile.getLocation(),
                                Vector2f.add(missile.getLocation(),
                                        (Vector2f) (new Vector2f(missile.getVelocity()).scale(1000f)), null));
                        if (MathUtils.getDistanceSquared(closest, ship.getLocation()) > 1.5
                                * ship.getShieldRadiusEvenIfNoShield()
                                * ship.getShieldRadiusEvenIfNoShield())
                            continue;
                    }
                    float dist = (MathUtils.getDistance(ship.getLocation(), missile.getLocation())+ship.getCollisionRadius());
                    float weight = missile.getDamageAmount()*2f
                            / dist;
                    if (missile.isMirv())
                        weight *= 1.5f;
                    if (missile.getSpec().getBehaviorJSON() != null
                            && missile.getSpec().getBehaviorJSON().has("triggerDistance"))
                        weight *= 2f;
                    MissileTargetingData.add(new HSIAutoFireTargetingUnitData(missile,weight,dist));
                }
            }
        }
    }

    private void sortData(TYPE type) {
        switch (type) {
            case FIGHTER:
                Collections.sort(FighterTargetingData, new HSIAutoFireTargetingUnitData.HSIAutoFireTargetingUnitDataScoreComparator());
                break;
            case MISSILE:
                Collections.sort(MissileTargetingData, new HSIAutoFireTargetingUnitData.HSIAutoFireTargetingUnitDataScoreComparator());
                break;
            case SHIP:
                Collections.sort(ShipTargetingData, new HSIAutoFireTargetingUnitData.HSIAutoFireTargetingUnitDataScoreComparator());
                break;
            default:
                break;
        }
    }

    private void sortAll(){
        sortData(TYPE.FIGHTER);
        sortData(TYPE.SHIP);
        sortData(TYPE.MISSILE);
    }
}
