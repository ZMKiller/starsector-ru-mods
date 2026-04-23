package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class ASF_AmmonDeployStats extends BaseShipSystemScript {

	public String WING_NAME = "A_S-F_ammon_wing";
	
	private boolean DEPLOYED = false;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		}
        CombatEngineAPI engine = Global.getCombatEngine();
        
        if (!DEPLOYED) {
        	DEPLOYED = true;
        	
    		for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                if (weapon.isSystemSlot()) {
            		int owner = ship.getOwner();
            		
            		Vector2f posZero = weapon.computePosition(ship);
            		

        			for (int i=0; i < 4; i++) {
                		
                		CombatFleetManagerAPI FleetManager = engine.getFleetManager(owner);
                		FleetManager.setSuppressDeploymentMessages(true);
                		FleetMemberAPI platMember = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, WING_NAME);
                		platMember.getRepairTracker().setCrashMothballed(false);
                		platMember.getRepairTracker().setMothballed(false);
                		platMember.getRepairTracker().setCR(1f);
                		platMember.setOwner(owner);
                		platMember.setAlly(ship.isAlly());
                		
                		ShipAPI drone = engine.getFleetManager(owner).spawnFleetMember(platMember, posZero, weapon.computeMidArcAngle(ship), 0f);
                		drone.setCRAtDeployment(0.7f);
                		drone.setCollisionClass(CollisionClass.FIGHTER);
                		
                		float droneAngle = weapon.computeMidArcAngle(ship) - 45 + (i * 30f);                		
                		drone.setFacing(droneAngle);
                		
                		Vector2f droneVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(180f, 240f), droneAngle + MathUtils.getRandomNumberInRange(-5f, 5f));
                		drone.getVelocity().set(droneVel);
                		
                		FleetManager.setSuppressDeploymentMessages(false);

        	            for (int j=0; j < 3; j++) {
                    		engine.addSmoothParticle(posZero, MathUtils.getRandomPointInCircle(droneVel, 25f), 17f, 0.6f, 0.15f, new Color(90,150,255,255));
        	            }
                		
        			}
            		
        			Global.getSoundPlayer().playSound("system_temporalshell", 1.1f, 0.3f, posZero, ship.getVelocity());
            		
            		engine.addHitParticle(posZero, //position
            				ship.getVelocity(), //velocity
                			75f, //size
                			0.8f, //brightness
                			0.1f, //duration
                			new Color(80,175,250,255));
            		
        			for (int i=0; i < 8; i++) {
                        Vector2f smokeVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(4f, 60f));
        	            float randomSize2 = MathUtils.getRandomNumberInRange(20f, 25f);
        	            engine.addSmokeParticle(MathUtils.getRandomPointInCircle(posZero, 30f), smokeVel, randomSize2, 0.9f, MathUtils.getRandomNumberInRange(0.5f, 1.0f), new Color(100,110,110,175));
        	            
        	            for (int j=0; j < 5; j++) {
                    		Vector2f baseFxVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(0f, 45f), weapon.computeMidArcAngle(ship));
                    		
        	            	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(posZero, 15f), //position
        	            			MathUtils.getRandomPointOnCircumference(baseFxVel, MathUtils.getRandomNumberInRange(40f, 120f)), //velocity
        	            			MathUtils.getRandomNumberInRange(4f, 9f), //size
        	            			MathUtils.getRandomNumberInRange(0.5f, 0.7f), //brightness
        	            			MathUtils.getRandomNumberInRange(0.4f, 0.6f), //duration
        	            			new Color(90,150,255,255));
        	            }
        			}
                }
    		}
        }
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		DEPLOYED = false;
	}
}
