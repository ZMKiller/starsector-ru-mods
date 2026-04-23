package org.amazigh.foundry.hullmods;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ASF_transparenceHullmod extends BaseHullMod {

	public static final float VELOCITY_BONUS = 25f;
	public static final float B_FLUX_BONUS = 15f;
	public static final float VENT_BONUS = 20f;
	public static final float SWELL_BONUS = 50f;
	public static final float SHIELD_MALUS = 120f;

	public static final float SYSTEM_DAMAGE_BONUS = 50f;
	
	private IntervalUtil interval1 = new IntervalUtil(0.05f,0.1f);
	private IntervalUtil interval2 = new IntervalUtil(0.05f,0.05f);
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyProjectileSpeedMult().modifyPercent(id, VELOCITY_BONUS);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -B_FLUX_BONUS);
		// Ballistic bonus as a "trap" to make you think they are a good idea when they get no bonuses from the system / swell
		stats.getVentRateMult().modifyPercent(id, VENT_BONUS);
		// uhh, venting is cool!
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("TRANSPARENCE_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
		if (engine.isPaused()) {
			return;
		}
		if (!ship.isAlive() || ship.isPiece()) {
     		info.CHARGE = 0f;
     		engine.getCustomData().put("TRANSPARENCE_DATA_KEY" + ship.getId(), info);
			return;
     	}
        MutableShipStatsAPI stats = ship.getMutableStats();
        
        if (info.CHARGE > 0f) {
        	if (ship.getFluxTracker().isOverloaded()) {
         		info.CHARGE -= (info.CHARGE * 0.4 * amount);
             	// doubled charge decay if you are overloaded!
         	} else if (info.TRIGGER) {
         		info.CHARGE -= (info.CHARGE * 0.12 * amount);
         		// notably less charge decay when system is active!
         	} else {
         		info.CHARGE -= (info.CHARGE * 0.2 * amount);
         	}
            // decay charge passively at a rate of 20% of current charge per second
            // we decay before charge gain, to *slightly* bias towards having higher charge levels
        }
        if (ship.getVelocity().length() > 0f && !ship.getFluxTracker().isOverloadedOrVenting()) {
        	
        	float degradeMult = 1f;
        	if (info.DEGRADE_TIMER > 0f ) {
        		info.DEGRADE_TIMER -= amount;
        		degradeMult = 0.5f;
        	}
        	/// halved reduced swell gain after system usage, to make it more *tactical* and thoughtful on when to use it.
        	
        	info.CHARGE += degradeMult * ((amount * 1.1f * Math.sqrt(ship.getVelocity().length())) + (amount * 0.05f * ship.getVelocity().length()));
        	
        	// gain charge when not: overloaded or venting
        	// amount of charge gained per second being: the square root of current speed + 1/10th of current speed (this is so +speed = MORE CHARGE)
        	
        	// (you hit ~148 with elite helmsmanship, and that's pretty much an "essential" skill for this ship, and as it's not locked behind multiple levels like SysEx is it can be all but guaranteed that you will have it if you are using this ship)
        	// so lets consider "charge thresholds" (the theoretical max you "should" be able to reach/sustain at various speeds)
        		// these are approximate values, and for this example, let's approx elite helms to a flat 20, which is very wrong, but "close enough"
        	// 130 speed - 95 charge	(base)
        	// 150 speed - 104 charge	(elite helms)
        	// 170 speed - 114 charge	(elite helms + UI)
        	// 200 speed - 127 charge	(elite helms + zero flux)
        	// 220 speed - 136 charge	(elite helms + UI + zero flux)
        	// 250 speed - 149 charge	(elite helms + UI + SO)
        		// with system usage decay is lower as well as increased speed, so you get even MORE charge
        	// 180 speed - 197 charge	(base)
        	// 210 speed - 220 charge	(elite helms)
        	// 230 speed - 234 charge	(elite helms + UI)
        	// 260 speed - 256 charge	(elite helms + zero flux)
        	// 280 speed - 270 charge	(elite helms + UI + zero flux)
        	// 310 speed - 290 charge	(elite helms + UI + SO)
        		// so you (more or less) get ~doubled maximum potential charge when system is active!
        		// used https://www.desmos.com/calculator for graph calcing to get these values
        }
        
        float swellMod = 0f;
        float shieldMod = 0f;
        float jitterRed1 = 80f/255f;
 		float jitterGreen1 = 230f/255f;
 		float jitterBlue1 = 215f/255f;
 		float jitterAlpha1 = 25f/255f;

        float jitterRed2 = 80f/255f;
 		float jitterGreen2 = 250f/255f;
 		float jitterBlue2 = 195f/255f;
 		
 		if (info.CHARGE > 100f) {
        	// swellMod = SWELL_BONUS + (float) (0.5f * (((info.CHARGE - 100f) * 0.01f) * SWELL_BONUS));
        	// if "charge" is over 100, then you only gain "50% strength" for the charge over 100 in terms of the flux cost reduction
 				// old stinky swellMod
 			swellMod = 100f - ((100f - SWELL_BONUS) * (1f - (((SWELL_BONUS * 0.01f) / 100f ) * (info.CHARGE -100f))));
 			// if "charge" is over 100, then any additional "strength" is *multiplicative* on that gained from the first 100
 				// new and uncringe swellMod
 			
 			shieldMod = ((info.CHARGE - 100f) * 0.01f) * SHIELD_MALUS;
        	
        	jitterRed1 = Math.min(255f, 80f+((info.CHARGE-100f)*1.6f))/255f;
        	jitterGreen1 = Math.max(0f, 230f-(info.CHARGE-100f))/255f;
     		jitterBlue1 = Math.max(0f, 215f-((info.CHARGE-100f)*1.6f))/255f;
     		jitterAlpha1 = Math.min(255f, 25f+((info.CHARGE-100)*0.2f))/255f;
     		
     		jitterRed2 = Math.min(255f, 80f+((info.CHARGE-100f)*1.6f))/255f;
        	jitterGreen2 = Math.max(0f, 250f-(info.CHARGE-100f))/255f;
     		jitterBlue2 = Math.max(0f, 195f-((info.CHARGE-100f)*1.6f))/255f;
        	// setting jitter colors here to save a second if statement lol
     		// this makes the "upper" jitter color gain intensity *and* both fade to red/orange when "charge" is over 100
        	
        	float effectAlpha = Math.min(1f, (((info.CHARGE - 100f) * 0.01f) + 0.05f));
        	interval1.advance(amount);
            if (interval1.intervalElapsed()) {
            	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            		if (weapon.isSystemSlot()) {
            			Vector2f posZero = weapon.computePosition(ship);
                        Vector2f smokeVel = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(0.8f, 16f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-15f, 15f));
        	            float randomSize2 = MathUtils.getRandomNumberInRange(15f, 20f);
        	            engine.addSmokeParticle(posZero, smokeVel, randomSize2, 0.9f, MathUtils.getRandomNumberInRange(0.5f, 1.0f), new Color(100f/255f,115f/255f,105f/255f,effectAlpha));
            			for (int i=0; i < 2; i++) {
            				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(7f, 28f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-18f, 18f));
            	            float randomSize1 = MathUtils.getRandomNumberInRange(5f, 9f);
            	            engine.addSmoothParticle(posZero, particleVel, randomSize1, 0.8f, MathUtils.getRandomNumberInRange(0.3f, 0.9f), new Color(1f,120f/255f,80f/255f,effectAlpha));
            	            // spawns smoke and "spark" particles if charge is over 100, they ramp up in visual potency as charge increases, reaching max potency at ~190 charge.
            			}
            		}
            	}
            }
        } else {
        	swellMod = (info.CHARGE * 0.01f) * SWELL_BONUS;
        	shieldMod = 0f;
        }
        stats.getEnergyWeaponFluxCostMod().modifyPercent(spec.getId(), -swellMod);
        stats.getShieldDamageTakenMult().modifyPercent(spec.getId(), shieldMod);
        // apply stat modifiers based on the funny charge math calculation
        
     	float jitterLevel = Math.min(1f, (info.CHARGE * 0.01f));
     	if (jitterLevel > 0f) {
     		float jitterRangeBonus = jitterLevel * 10f;
     		jitterLevel = (float) Math.sqrt(jitterLevel);
     		Color jitterColor1 = new Color(jitterRed1,jitterGreen1,jitterBlue1,jitterAlpha1);
     		Color jitterColor2 = new Color(jitterRed2,jitterGreen2,jitterBlue2,75f/255f);
     		ship.setJitter(this, jitterColor1, jitterLevel, 3, 0, 0 + jitterRangeBonus);
     		ship.setJitterUnder(this, jitterColor2, jitterLevel, 10, 0f, 9f + jitterRangeBonus);
     		// jitter that scales with charge level, because... why not?
     	}
     	
     	// wtf the shipSystem is (partially) handled by a hullmod? is this cringe? or is it based?
     	if(ship.getSystem().isActive()) {
         	info.TRIGGER = true;
     		float systemMult = 0f;
            if (info.CHARGE > 100f) {
            	systemMult = SYSTEM_DAMAGE_BONUS + (float) (0.5f * (((info.CHARGE - 100f) * 0.01f) * SYSTEM_DAMAGE_BONUS));	
            } else {
            	systemMult = (info.CHARGE * 0.01f) * SYSTEM_DAMAGE_BONUS;
            }
            
            if (ship.getSystem().isChargedown()) {
            	if (info.CHARGE > 100f) {
            		info.STORED = 100f;
            	} else {
                	info.STORED = info.CHARGE;
            	}
            	
            	if (info.CHARGE  > 0f) {
            		info.CHARGE -= (info.STORED * amount * ship.getSystem().getEffectLevel());
            		// this gives a decent chunk of decay when system is in the OUT state
            	} else {
            		info.CHARGE = 0f;
            	}
            }
     		systemMult *= ship.getSystem().getEffectLevel();
     		stats.getEnergyWeaponDamageMult().modifyPercent(spec.getId(), systemMult);
     		if (ship == engine.getPlayerShip()) {
     			engine.maintainStatusForPlayerShip("TRANSPARENCESYS", "graphics/icons/hullsys/high_energy_focus.png", "Amita Drive Active", "Energy Weapon Damage Increased by: " + (int)systemMult + "%", false);
     		}
     		
     		interval2.advance(amount);
            if (interval2.intervalElapsed()) {
            	//engine.addSmoothParticle(ship.getLocation(), ship.getVelocity(), 180f, 0.3f, 0.1f, new Color(75f/255f,250f/255f,190f/255f,ship.getSystem().getEffectLevel()*0.8f));
            	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            		if (weapon.isSystemSlot()) {
            			Vector2f posZero = weapon.computePosition(ship);
            			engine.addSmoothParticle(posZero, ship.getVelocity(), 48f, 0.3f, 0.1f, new Color(75f/255f,250f/255f,190f/255f,ship.getSystem().getEffectLevel()*0.8f));
                        for (int i=0; i < 2; i++) {
                        	Vector2f randomPos = MathUtils.getRandomPointInCircle(posZero, 12f);
            				float distanceRandom1 = MathUtils.getRandomNumberInRange(0.6f, 1.15f);
            	            Vector2f Dir1 = Misc.getUnitVectorAtDegreeAngle(weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-22f, 22f));
            	            Vector2f particleVel = new Vector2f(Dir1.x * 128 * distanceRandom1, Dir1.y * 128 * distanceRandom1);
            	            particleVel.x += ship.getVelocity().x;
            	            particleVel.y += ship.getVelocity().y;
            	            float randomSize1 = MathUtils.getRandomNumberInRange(3f, 7f);
            	            engine.addSmoothParticle(randomPos, particleVel, randomSize1, 0.8f, MathUtils.getRandomNumberInRange(0.9f, 1.5f), new Color(75f/255f,250f/255f,MathUtils.getRandomNumberInRange(0.75f, 0.8f),ship.getSystem().getEffectLevel()));
            	            Vector2f randomPos3 = MathUtils.getRandomPointInCircle(ship.getLocation(), 40f);
            	            float distanceRandom3 = MathUtils.getRandomNumberInRange(0.65f, 1.1f);
            	            Vector2f Dir3 = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(-180f, 180f));
            	            Vector2f particleVel3 = new Vector2f(Dir3.x * 150 * distanceRandom3, Dir3.y * 150 * distanceRandom3);
            	            particleVel3.x += ship.getVelocity().x;
            	            particleVel3.y += ship.getVelocity().y;
            	            float randomSize3 = MathUtils.getRandomNumberInRange(4f, 8f);
            	            engine.addSmoothParticle(randomPos3, particleVel3, randomSize3, 0.75f, MathUtils.getRandomNumberInRange(0.8f, 1.75f), new Color(75f/255f,250f/255f,MathUtils.getRandomNumberInRange(0.75f, 0.8f),ship.getSystem().getEffectLevel()));
            	            for (int j=0; j < 3; j++) {
            	            	Vector2f randomPos2 = MathUtils.getRandomPointInCircle(ship.getLocation(), 56f);
                	            float distanceRandom2 = MathUtils.getRandomNumberInRange(0.75f, 1.1f);
                	            Vector2f Dir2 = Misc.getUnitVectorAtDegreeAngle(MathUtils.getRandomNumberInRange(-180f, 180f));
                	            Vector2f particleVel2 = new Vector2f(Dir2.x * 180 * distanceRandom2, Dir2.y * 180 * distanceRandom2);
                	            particleVel2.x += ship.getVelocity().x;
                	            particleVel2.y += ship.getVelocity().y;
                	            float randomSize2 = MathUtils.getRandomNumberInRange(2f, 5f);
                	            engine.addSmoothParticle(randomPos2, particleVel2, randomSize2, 0.7f, MathUtils.getRandomNumberInRange(0.7f, 1.25f), new Color(75f/255f,250f/255f,MathUtils.getRandomNumberInRange(0.75f, 0.8f),ship.getSystem().getEffectLevel()));
            	            }
            	            // spawns a (sort of mostly) rear facing particle "spray" while system is active
            			}
            		}
            	}
            }
            
     	} else if (info.TRIGGER){
     		info.TRIGGER = false;
     		info.DEGRADE_TIMER = 3f;
     		stats.getEnergyWeaponDamageMult().unmodify(spec.getId());
     	}
     	// yeah so i handle (most of) this ships shipSystem in the hullmod, what you gonna do huh!
     	
        if (ship == engine.getPlayerShip()) {
        	engine.maintainStatusForPlayerShip("TRANSPARENCECHARGE", "graphics/icons/hullsys/temporal_shell.png",  "Photon Swell Level: " + (int)info.CHARGE + "", "Energy Weapon Flux cost Reduced by: " + (int)swellMod + "%", false);
			if ((int)shieldMod > 0) {
				engine.maintainStatusForPlayerShip("TRANSPARENCESHIELD", "graphics/icons/hullsys/entropy_amplifier.png",  "Photon Swell Exceeding Safe Levels!" ,"Damage taken by Shields Increased by: " + (int)shieldMod + "%", true);
			}
			// display ui info on current charge, and the current bonus / malus values
		}
        engine.getCustomData().put("TRANSPARENCE_DATA_KEY" + ship.getId(), info);
	}
	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		LabelAPI label = tooltip.addPara("This integral accelerator core broadly increases damage potential at high velocities.", pad);
		label = tooltip.addPara("Active flux vent rate increased by %s.", pad, h, "" + (int)VENT_BONUS + "%");
		label.setHighlight("" + (int)VENT_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Energy weapon projectile velocity is increased by %s.", pad, h, "" + (int)VELOCITY_BONUS + "%");
		label.setHighlight("" + (int)VELOCITY_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Ballistic weapon flux cost is reduced by %s.", pad, h, "" + (int)B_FLUX_BONUS + "%");
		label.setHighlight("" + (int)B_FLUX_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("%s is generated based on ship velocity.", opad, h, "Photon Swell");
		label.setHighlight("Photon Swell");
		label.setHighlightColors(h);
		label = tooltip.addPara("%s reduces the flux cost of energy weapons, reaching %s reduction at %s.", pad, h, "Photon Swell", "" + (int)SWELL_BONUS + "%", "100 Photon Swell");
		label.setHighlight("Photon Swell", "" + (int)SWELL_BONUS + "%", "100 Photon Swell");
		label.setHighlightColors(h, h, h);
		label = tooltip.addPara("Going over %s overcharges the core granting greater flux cost reduction at the cost of reduced shield efficiency.", pad, h, "100 Photon Swell");
		label.setHighlight("100 Photon Swell");
		label.setHighlightColors(h);
		
		// Thanks to Wisp and SafariJohn for help with re-wording this to be nicer to read
	}

    private class ShipSpecificData {
    	private float CHARGE = 0f;
    	private boolean TRIGGER = false; //this variable exists to give something to know when the script should reset stored system based variables
    	private float STORED = 0f; // a variable for how much to decay charge by when system is "spooling down"
    	private float DEGRADE_TIMER = 0f; // a timer for reduced swell gain after system use 
    }
	
}
