package data.ai;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.shipsystems.scripts.HSITacTeleport;

public class HSITacTeleportAI implements ShipSystemAIScript{
	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private boolean systemuse = true;
	private ShipwideAIFlags flags;
	private IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);
	private float range = 2000f;
    private float shortest = 1000f;

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.engine = engine;
		this.flags = flags;
		this.system = system;
		range = HSITacTeleport.getRange(ship);
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (target == null)
			return;
		if (system.getCooldownRemaining() > 0)
			return;
		if (system.isOutOfAmmo())
			return;
		if (system.isActive())
			return;
		tracker.advance(amount);
		systemuse = false;
		if (tracker.intervalElapsed()) {
            float weightLim = 1;
                switch (ship.getCaptain().getPersonalityAPI().getId()) {
                    case Personalities.AGGRESSIVE:
                        weightLim = 0.6f;
                        break;
                    case Personalities.RECKLESS:
                        weightLim = 0.8f;
                        break;
                    case Personalities.STEADY:
                        weightLim = 1f;
                        break;
                    default:
                        weightLim = 1.4f;
                        break;
                }
            if(ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)){
                HSITurbulanceShieldListenerV2 shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
                if(shield.getShield().getCurrent()/shield.getShield().getShieldCap()<0.1f&&ship.getFluxLevel()>target.getFluxLevel()/weightLim){
                    systemuse = true;
                    Vector2f add = VectorUtils.getDirectionalVector(AIUtils.getNearestEnemy(ship).getLocation(), ship.getLocation());
                    add.scale(Math.max(shortest,range-150f));
                    Vector2f.add((Vector2f)Misc.getUnitVectorAtDegreeAngle(360f*(float)Math.random()).scale(100f), add, add);
                    flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 5f, add);
                }
            }
            if(ship.getFluxLevel()>0.9f&&ship.areSignificantEnemiesInRange()&&weightLim>=0.8f){
                systemuse = true;
                Vector2f add = VectorUtils.getDirectionalVector(AIUtils.getNearestEnemy(ship).getLocation(), ship.getLocation());
                add.scale(Math.max(shortest,range-150f));
                Vector2f.add((Vector2f)Misc.getUnitVectorAtDegreeAngle(360f*(float)Math.random()).scale(100f), add, add);
                flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 5f, add);
            }
			if(!systemuse&&target!=null&&MathUtils.getDistance(target.getLocation(), ship.getLocation())<range&&MathUtils.getDistance(target.getLocation(), ship.getLocation())>shortest){               
                float fpt = ship.getFleetMember().getDeploymentPointsCost();
                for(ShipAPI f:AIUtils.getNearbyEnemies(target, 3000f)){
                    if(f.getFleetMember()!=null)
                    fpt+=f.getFleetMember().getDeploymentPointsCost();
                }
                float ept = target.getFleetMember().getDeploymentPointsCost();
                for(ShipAPI e:AIUtils.getNearbyAllies(target, 3000f)){
                    if(e.getFleetMember()!=null)
                    ept+=e.getFleetMember().getDeploymentPointsCost();
                }
                float weight = fpt/ept;
                if(weight>weightLim){
                    systemuse = true;
                }
                float to = (float)(120f*(Math.random()-0.5f)+target.getFacing());
                Vector2f add = Misc.getUnitVectorAtDegreeAngle(to);
                add.scale(target.getCollisionRadius()+ship.getCollisionRadius());
                Vector2f.add(add, target.getLocation(), add);
                if(MathUtils.getDistance(ship.getLocation(), add)>range){
                    Vector2f re = VectorUtils.getDirectionalVector(ship.getLocation(), add);
                    re.scale(range*0.95f);
                    Vector2f.add(ship.getLocation(), re, add);
                }
                flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 5f, add);
            }
		}
        if(systemuse){
            ship.useSystem();
        }
	}

}

