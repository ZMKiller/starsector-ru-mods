package data.kit;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import data.ai.HSIThreatSharedData;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class HSIAIUtils {
    public static final String DATA_KEY = "HSIThreatSharedData";
    public static boolean isAcceptableAimLocation(Vector2f location, WeaponAPI weapon) {
        if (Math.abs(weapon.distanceFromArc(location)) > 0) {
            return false;
        }
        float computeRange = weapon.getRange();
        return !(MathUtils.getDistanceSquared(location, weapon.getLocation()) > computeRange * computeRange);
    }

    public static boolean isOverKilled(CombatEntityAPI target){
        if(target.getCustomData().containsKey(DATA_KEY)){
            HSIThreatSharedData data = (HSIThreatSharedData) target.getCustomData().get(DATA_KEY);
            if(target instanceof ShipAPI){
                return data.getDMGToThis()>((ShipAPI) target).getArmorGrid().getArmorRating()*2f+target.getHitpoints();
            }else{
                return data.getDMGToThis()>target.getHitpoints()*1.1f;
            }
        }
        return false;
    }


    public static Vector2f getAimPointSimple(WeaponAPI weapon,CombatEntityAPI target){
        Vector2f l1 = new Vector2f(target.getLocation());
        Vector2f v1 = new Vector2f(target.getVelocity());
        float dist = Misc.getDistance(weapon.getLocation(),target.getLocation());
        float t1 = dist/weapon.getProjectileSpeed();
        return Vector2f.add(l1,(Vector2f) v1.scale(t1),null);
    }

    public static float evaluateShipScore(ShipAPI target,ShipAPI self){
        return 0;
    }

    public static Vector2f getBestFireTargetLocation(CombatEntityAPI target,WeaponAPI source){
        return  new Vector2f();
    }

    public static void setShipFacingSmooth(float expectedFacing,ShipAPI ship){
        float diff = MathUtils.getShortestRotation(ship.getFacing(),expectedFacing);
        float toTurnDec = ship.getAngularVelocity()*ship.getAngularVelocity()/(0.5f*ship.getTurnDeceleration())+1f;
        if(Math.abs(diff)>toTurnDec){
            if(diff>0) ship.giveCommand(ShipCommand.TURN_LEFT,null,0);
            else ship.giveCommand(ShipCommand.TURN_RIGHT,null,0);
        }else if(Math.abs(diff)>1){
            if(diff>0) ship.giveCommand(ShipCommand.TURN_RIGHT,null,0);
            else ship.giveCommand(ShipCommand.TURN_LEFT,null,0);
        }else{
            ship.setAngularVelocity(ship.getAngularVelocity()*0.01f);
        }
    }

    public static void moveShipSmooth(Vector2f target,float expectedSpeed,ShipAPI ship){
        Vector2f expectVel = Vector2f.sub(target,ship.getLocation(),null);
        expectVel = (Vector2f) expectVel.scale(Math.min(ship.getMaxSpeed(),expectedSpeed)/expectVel.length());
        Vector2f currVel = new Vector2f(ship.getVelocity());


        float eTheta = Math.abs(MathUtils.getShortestRotation(VectorUtils.getFacing(expectVel),ship.getFacing()));
        float cTheta = Math.abs(MathUtils.getShortestRotation(VectorUtils.getFacing(currVel),ship.getFacing()));

        float ex = (float)Math.cos(Math.toRadians(eTheta))*expectVel.length();
        float cx = (float)Math.cos(Math.toRadians(cTheta))*currVel.length();
        float deltax = Math.abs(cx-ex);

        float ey = (float)Math.sin(Math.toRadians(eTheta))*expectVel.length();
        float cy = (float)Math.sin(Math.toRadians(cTheta))*currVel.length();
        float deltay = Math.abs(cy-ey);

        float toStopRange = Math.max(cx*cx/(2*ship.getDeceleration()),cy*cy/(2*ship.getAcceleration()))+10;
        if(MathUtils.getDistance(ship.getLocation(),target)<toStopRange){
            if(deltax>=deltay){
                if(cx-ex>0.01){
                    ship.giveCommand(ShipCommand.ACCELERATE,null,0);
                }else if(cx-ex<-0.01){
                    ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS,null,0);
                }
            }else{
                if(cy-ey>0.01){
                    ship.giveCommand(ShipCommand.STRAFE_LEFT,null,0);
                }else if(cy-ey<-0.01){
                    ship.giveCommand(ShipCommand.STRAFE_RIGHT,null,0);
                }
            }
        }else{
            if(deltax>=deltay){
                if(cx-ex>0.01){
                    ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS,null,0);
                }else if(cx-ex<-0.01){
                    ship.giveCommand(ShipCommand.ACCELERATE,null,0);
                }
            }else{
                if(cy-ey>0.01){
                    ship.giveCommand(ShipCommand.STRAFE_RIGHT,null,0);
                }else if(cy-ey<-0.01){
                    ship.giveCommand(ShipCommand.STRAFE_LEFT,null,0);
                }
            }
        }
    }



    public static void moveShiptoLocationWithFacing(float expectedFacing,Vector2f target,float expectedSpeed,ShipAPI ship){
        //转向
        setShipFacingSmooth(expectedFacing,ship);
        //移动
        moveShipSmooth(target,expectedSpeed,ship);
    }
}
