package data.kit;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class AjimusUtils {
    public static final Vector2f ZERO_VECTOR = new Vector2f(0, 0);

    public static float EnsurePositive(float base){
        if(base<0) return 0;
        return base;
    }

    public static CombatEntityAPI PickAutoFireTarget(WeaponAPI weapon) {
        ShipAPI source = weapon.getShip();
        if (source != null) {
            WeaponGroupAPI group = source.getWeaponGroupFor(weapon);
            if (group != null) {
                if (group.isAutofiring()) {
                    AutofireAIPlugin ai = group.getAutofirePlugin(weapon);
                    ShipAPI targetShip = ai.getTargetShip();
                    MissileAPI targetMissile = ai.getTargetMissile();
                    if (weapon.getSpec().getAIHints().contains(AIHints.PD_ALSO) && targetShip != null) {
                        return targetShip;
                    } else if (weapon.getSpec().getAIHints().contains(AIHints.PD_ONLY)) {
                        return targetMissile;
                    } else {
                        if (targetShip != null) {
                            return targetShip;
                        } else {
                            return targetMissile;
                        }
                    }
                } else {
                    Vector2f interestPoint = source.getMouseTarget();
                    ShipAPI targetShip = Misc.findClosestShipEnemyOf(source, interestPoint, HullSize.FIGHTER, 600f,
                            true);
                    return targetShip;
                }
            } else {
                Vector2f interestPoint = source.getMouseTarget();
                ShipAPI targetShip = Misc.findClosestShipEnemyOf(source, interestPoint, HullSize.FIGHTER, 600f, true);
                return targetShip;
            }
        } else {
            return null;
        }
    }

    public static Vector2f getEngineCoordFromRelativeCoord(Vector2f pivot, Vector2f relative, float angle) {
        Vector2f currLoc = new Vector2f(0, 0);
        VectorUtils.rotateAroundPivot(relative, currLoc, angle, currLoc);
        return Vector2f.add(pivot, currLoc, null);
    }

    public static Vector2f getRelativeCoordFromEngineCoord(Vector2f pivot, Vector2f point, float angle) {
        Vector2f relative = Vector2f.sub(point, pivot, null);
        VectorUtils.rotateAroundPivot(relative, ZERO_VECTOR, -angle);
        return new Vector2f(relative);
    }

    public static void ExecuteShipManeuver(ShipAPI ship, float ExpectAngle, float ExpectSpeed) {
        float aimAngle = MathUtils.getShortestRotation(ship.getFacing(), ExpectAngle);
        if (aimAngle < 0) {
            ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
        } else {
            ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
        }
        Global.getCombatEngine().headInDirectionWithoutTurning(ship, ExpectAngle, ExpectSpeed);
    }

    public static void ExecuteMissileManeuver(MissileAPI missile, float ExpectAngle, float ExpectSpeed) {
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), ExpectAngle);
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }
        float spd = missile.getMoveSpeed();
        if (ExpectSpeed > spd * 1.05f) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        } else if (ExpectSpeed < spd * 0.95f) {
            missile.giveCommand(ShipCommand.DECELERATE);
        }
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * 0.05f) {
            missile.setAngularVelocity(aimAngle / 0.05f);
        }
    }

    public static boolean isTargetLegal(CombatEntityAPI target) {
        if (target instanceof ShipAPI) {
            return ((ShipAPI) target).isAlive();
        } else if (target instanceof MissileAPI) {
            return !((MissileAPI) target).isExpired() && (!((MissileAPI) target).isFading())
                    && (!((MissileAPI) target).isFizzling());
        }
        return target != null && !target.isExpired() && Global.getCombatEngine().isEntityInPlay(target);
    }

    public static void repairHP(ShipAPI ship, float repairHP) {
        ship.setHitpoints(
                Math.min(ship.getMaxHitpoints(), ship.getHitpoints() + repairHP));
    }

    public static void repairArmorGenerally(ShipAPI ship, float repairArmor) {
        ArmorGridAPI aromr = ship.getArmorGrid();
        int y = 0;
        for (float[] row : aromr.getGrid()) {
            int x = 0;
            for (float col : row) {
                if (col < aromr.getMaxArmorInCell()) {
                    aromr.setArmorValue(x, y, Math.max(aromr.getArmorRating() / 15f,
                            aromr.getArmorValue(x, y) + repairArmor));
                }
                x++;
            }
            y++;
        }
    }

    public static void repairArmorTotal(ShipAPI ship, float repairArmor) {
        ArmorGridAPI aromr = ship.getArmorGrid();
        int y = 0;
        float r = repairArmor;
        for (float[] row : aromr.getGrid()) {
            int x = 0;
            for (float col : row) {
                if (r>0&&col < aromr.getMaxArmorInCell()) {
                    if (r > aromr.getMaxArmorInCell() - col) {
                        aromr.setArmorValue(x, y, aromr.getMaxArmorInCell());
                        r-=(aromr.getMaxArmorInCell() - col);
                    }else{
                        aromr.setArmorValue(x, y, col+r);
                        r = 0;
                    }
                }
                x++;
            }
            y++;
        }
    }
    //from alex
    public static Vector2f findClearLocation(Vector2f dest,float checkDist) {
		if (AjimusUtils.isLocationClear(dest,checkDist)) return dest;
		
		float incr = 50f;

		WeightedRandomPicker<Vector2f> tested = new WeightedRandomPicker<Vector2f>();
		for (float distIndex = 1; distIndex <= 32f; distIndex *= 2f) {
			float start = (float) Math.random() * 360f;
			for (float angle = start; angle < start + 360; angle += (30f+(float)Math.random()*60f)) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(incr * distIndex);
				Vector2f.add(dest, loc, loc);
				tested.add(loc);
				if (AjimusUtils.isLocationClear(loc,checkDist)) {
					return loc;
				}
			}
		}
		
		if (tested.isEmpty()) return dest; // shouldn't happen
		
		return tested.pick();
	}
	//from alex
	private static boolean isLocationClear(Vector2f loc,float checkDist) {
		for (ShipAPI other : Global.getCombatEngine().getShips()) {
			if (other.isShuttlePod()) continue;
			if (other.isFighter()) continue;
			
			Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
			float otherR = other.getShieldRadiusEvenIfNoShield();
			if (other.isPiece()) {
				otherLoc = other.getLocation();
				otherR = other.getCollisionRadius();
			}
			
			
			float dist = Misc.getDistance(loc, otherLoc);
			float r = otherR;
			//r = Math.min(r, Misc.getTargetingRadius(loc, other, false) + r * 0.25f);
			if (dist < r + checkDist) {
				return false;
			}
		}
		for (CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
			float dist = Misc.getDistance(loc, other.getLocation());
			if (dist < other.getCollisionRadius() + checkDist) {
				return false;
			}
		}
		
		return true;
	}

    //MissileTypes:"MISSILE","ROCKET","MISSILE_TWO_STAGE_SECOND_UNGUIDED","BOMB","BOMB_WITH_SLOW","CIRCLE_TARGET","HEATSEEKER","PHASE_MINE","PHASE_CHARGE","NO_AI","MIRV"
    public static boolean isGuidedMissile(String typeString){
        return (typeString.equals("MISSILE")||typeString.equals("MISSILE_TWO_STAGE_SECOND_UNGUIDED")||typeString.equals("CIRCLE_TARGET")||typeString.equals("HEATSEEKER")||(typeString.equals("MIRV")));
    }

    public static DamagingExplosionSpec createExplosionSpec(float damage,DamageType type,Color particleC,Color explosionC,float coreR,float R,float dur) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                dur, // duration
                R, // radius
                coreR, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                150, // particleCount
                particleC, // particleColor
                explosionC // explosionColor
        );

        spec.setDamageType(type);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_guardian");
        return spec;
    }

    public static boolean hasIncompatible(List<String> IncompatibleMods, HullModSpecAPI spec, ShipAPI ship){
        boolean hasIc = false;
        for(String ic:IncompatibleMods){
            if(!spec.getId().equals(ic)) hasIc = hasIc||ship.getVariant().hasHullMod(ic);
        }
        return hasIc;
    }

    public static String FormatMapOutPut(Map<HullSize,Float> m){
        String o = "";
        if(m.containsKey(HullSize.FIGHTER)){
            o+=(int)((float)m.get(HullSize.FIGHTER));
            o+="/";
        }
        if(m.containsKey(HullSize.FRIGATE)){
            o+=(int)((float)m.get(HullSize.FRIGATE));
            o+="/";
        }
        if(m.containsKey(HullSize.DESTROYER)){
            o+=(int)((float)m.get(HullSize.DESTROYER));
            o+="/";
        }
        if(m.containsKey(HullSize.CRUISER)){
            o+=(int)((float)m.get(HullSize.CRUISER));
            o+="/";
        }
        if(m.containsKey(HullSize.CAPITAL_SHIP)){
            o+=(int)((float)m.get(HullSize.CAPITAL_SHIP));

        }
        return o;
    }

    public static String FormatMapOutPutWithPercentage(Map<HullSize,Float> m){
        String o = "";
        if(m.containsKey(HullSize.FIGHTER)){
            o+=(int)((float)m.get(HullSize.FIGHTER));
            o+="%";
            o+="/";
        }
        if(m.containsKey(HullSize.FRIGATE)){
            o+=(int)((float)m.get(HullSize.FRIGATE));
            o+="%";
            o+="/";
        }
        if(m.containsKey(HullSize.DESTROYER)){
            o+=(int)((float)m.get(HullSize.DESTROYER));
            o+="%";
            o+="/";
        }
        if(m.containsKey(HullSize.CRUISER)){
            o+=(int)((float)m.get(HullSize.CRUISER));
            o+="%";
            o+="/";
        }
        if(m.containsKey(HullSize.CAPITAL_SHIP)){
            o+=(int)((float)m.get(HullSize.CAPITAL_SHIP));
            o+="%";
        }
        return o;
    }


    public static void setTraitorTrigger(){
        if(!Global.getSector().getMemoryWithoutUpdate().contains(HSIIds.CAMPAIGN_FLAG.TRAITOR_BASE)){
            Global.getSector().getMemoryWithoutUpdate().set(HSIIds.CAMPAIGN_FLAG.TRAITOR_BASE,true);
        }
    }

    //from AnyIDElse
    public static boolean isInRefit(ShipAPI ship) {
        if (ship.getOriginalOwner() != -1) return false; // however, when outside refit screen, ship's orig owner may be -1
        if (Global.getCurrentState() == GameState.COMBAT) return false; // not in combat
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return false; // why
        if (engine.isSimulation()) return false; // not in sim
        if (engine.getCombatUI() != null) return false; // not ui
        return true;
    }

}
