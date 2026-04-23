package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_unhingeChronoflowStats extends BaseShipSystemScript {
	
	public static final Color JITTER_COLOR = new Color(90,165,255,55);
	public static final Color JITTER_UNDER_COLOR = new Color(90,165,255,155);
	public static final Color SPARK_COLOR_1 = new Color(90,200,255,155);
	public static final Color SPARK_COLOR_2 = new Color(60,150,255,255);
	
	private boolean initialise = false;
	
	private float OFFSET = 0f;
	
	private float timer = 0f;
	
	private float timeMult = 0f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		// �Dark. Wet. Drink deep, and descend. The water is warm and well. It is very busy here, though you cannot see it.
		// The swimmers are curious. The flea always jumps from time to time. It will drink it all. It will drink it deep��
		
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		
		if (!initialise ) {
			initialise = true;
			
			// setting the random offset for the two "cosine wave" timescale levels
			OFFSET=(float)(Math.random()*MathUtils.FPI*2);
		}
		
		// variable to make amount randomised, so that "fast" time lasts a bit longer
		float timeScalar = 1f;
		if (timeMult > 0f) {
			timeScalar = MathUtils.getRandomNumberInRange(0.8f, 1.0f);
		}
		
		float amount = Global.getCombatEngine().getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue() * timeScalar;
		
		timer += amount;
		
		timeMult = 2f * (float) Math.cos((OFFSET + timer) * (MathUtils.FPI / 2.5f));
			//cycle goes from 2 to -2 over the course of 2.5 seconds (5 seconds for a full 2 > -2 > 2)
				// note that with the randomised amount value, time for a full cycle will be slightly higher, as this is the value for a 1.0 mult 
		
		float finalTimeMult = 4f + timeMult;
		// this gives 2-6x timeflow
		
		float jitterLevel = effectLevel;
		float jitterRangeBonus = 0;
		float maxRangeBonus = 3f + ((finalTimeMult - 1f) * 5f);
		// jitter scales up and down with effect level, just a funny little visual thing :)
		// scales from 8-28
		
		if (state == State.IN) {
			jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
			if (jitterLevel > 1) {
				jitterLevel = 1f;
			}
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		} else if (state == State.ACTIVE) {
			jitterLevel = 1f;
			jitterRangeBonus = maxRangeBonus;
		} else if (state == State.OUT) {
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		}
		jitterLevel = (float) Math.sqrt(jitterLevel);
		effectLevel *= effectLevel;
		
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
		
    	/*
		sparkInterval += (amount * effectLevel) * MathUtils.getRandomNumberInRange(1f, 1.5f);
		if (sparkInterval > 0.1f) {
        	sparkInterval -= 0.1f;
        	for (int i=0; i < 4; i++) {
        		float angle = MathUtils.getRandomNumberInRange(0f, 360f);
            	Vector2f sparkPoint = MathUtils.getPointOnCircumference(ship.getLocation(), angle, ship.getCollisionRadius() * MathUtils.getRandomNumberInRange(0.65f, 1.0f));
            	Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), angle + MathUtils.getRandomNumberInRange(-5f, 5f), 50f);
        			// this is me doing an oopsie, but it made an *interesting* effect, a sort of rippling starfield-ish thing, so left here for posterity
        		
            	Global.getCombatEngine().addSmoothParticle(sparkPoint,
						sparkVel,
						MathUtils.getRandomNumberInRange(7f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.35f, 0.5f), //duration
						SPARK_COLOR_1);
        	}
        }
    	 */
		
		float shipTimeMult = 1f + (finalTimeMult * effectLevel);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
		ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
				
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
		
		initialise = false;
		OFFSET = 0f;
		timer = 0f;
		timeMult = 0f;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("time flow altered", false);
		}
		return null;
	}
}
