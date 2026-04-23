package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

import org.amazigh.foundry.hullmods.ASF_ArtyMount.ShipSpecificData;
import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.amazigh.foundry.scripts.ASF_celpekProjScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class ASF_ArtyWepMid extends BaseHullMod {

	private float rechargeTime = 3f;
	private float spoolTime = 1.5f; // portion of recharge time that the vfx is not spooling up
	private float damage = 180f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("ASF_ARTILLERY_DATA_KEY" + ship.getId());
        if (info == null) {
            return; // sanity check, just do nothing if the "main" hullmod hasn't set things up yet
        }
        
        if (info.TIMER > spoolTime) {
			
			float spoolVal = info.TIMER - spoolTime;
    		SpriteAPI chargeGlow = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
    		
    		double timeMult = (double) ship.getMutableStats().getTimeMult().modified; // this timeMult stuff is a "well fuck sprite rendering gets screwy with increases to timescale, let's fix it!"
    		int alpha = (int) Math.ceil((5 + (21 * spoolVal)) / timeMult);

    		info.fxInterval1.advance(amount);

        	Vector2f portLoc = new Vector2f();
			for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
				if (port.isDecorative()) {
					portLoc = port.computePosition(ship);
					
		    		for (int i=0; i < 2; i++) {
		    			float sizeRandom1 = MathUtils.getRandomNumberInRange(21f, 29f);
		    			Vector2f spriteSize1 = new Vector2f(sizeRandom1, sizeRandom1);
		        		Vector2f spritePos1 = MathUtils.getRandomPointOnCircumference(portLoc, 2f);
		            	MagicRender.singleframe(chargeGlow, spritePos1, spriteSize1, ship.getFacing() - 90f, new Color(140,140,250,alpha), true);
		            	
		    			float sizeRandom2 = MathUtils.getRandomNumberInRange(43f, 51f);
		    			Vector2f spriteSize2 = new Vector2f(sizeRandom2, sizeRandom2);
		            	Vector2f spritePos2 = MathUtils.getRandomPointOnCircumference(portLoc, i);
		        		MagicRender.singleframe(chargeGlow, spritePos2, spriteSize2, ship.getFacing() - 90f, new Color(32,32,200,alpha), true);
		    		}
		    		
		    		if (info.fxInterval1.intervalElapsed()) {
		        		
		        		int particleAlpha = (int) Math.ceil((20 + (120 * spoolVal)) / timeMult);
		        		particleAlpha = Math.min(250, particleAlpha);
		        		
		        		for (int i=0; i < 3; i++) {
		            		Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(2.5f, 10.3f), port.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-37f, 37f));
		            		
		                	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(portLoc, 8.1f),
		                    		sparkVel,
		            				MathUtils.getRandomNumberInRange(2.2f, 3.9f), //size
		            				1f, //brightness
		            				MathUtils.getRandomNumberInRange(0.1f, 0.23f), //duration
		            				new Color(45,45,193,particleAlpha));
		            	}
		        		
		            	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(portLoc, 1f),
		            			ship.getVelocity(),
		        				MathUtils.getRandomNumberInRange(9f, 11f), //size
		        				1f, //brightness
		        				MathUtils.getRandomNumberInRange(0.1f, 0.2f), //duration
		        				new Color(91,91,250,particleAlpha));
		        	}
		        	
				}
			}
    		
        }
        
        
        if (info.READY) {
        	
    		if (info.LOCK) {
    			
    			Vector2f portLoc = new Vector2f();
    			for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {

	    			Vector2f baseVel = ship.getVelocity();
	    			
    				if (port.isDecorative()) {
    					portLoc = port.computePosition(ship);
    					float shotAngle = port.computeMidArcAngle(ship);
    	    			
    	    			// Global.getSoundPlayer().playSound("autopulse_laser_fire", 0.95f, 1.1f, portLoc, baseVel);
    	    			Global.getSoundPlayer().playSound("A_S-F_x-pulse_fire", 0.9f, 1.1f, portLoc, baseVel); // "punch" addition to the firing sound
    	    			
    	    			Global.getSoundPlayer().playSound("hit_heavy_energy", 0.6f, 0.65f, portLoc, baseVel); // "punch" addition to the firing sound
    	    			
    	        		for (int i=0; i < 7; i++) { // 5
    	        			
    	        			float realAngle = (shotAngle - 24f) + (i * 8f); // 22,11
    	        			
    	        			CombatEntityAPI projC = engine.spawnProjectile(ship, null, "A_S-F_celpek",
    	        					portLoc, // pos
    	        					realAngle, //angle
    	        					baseVel);
    	        			
    	        			engine.addPlugin(new ASF_celpekProjScript((DamagingProjectileAPI) projC, info.TARGET));
    	        			
    	        		}
    					
    					// muzzle effect
    	    			engine.addHitParticle(portLoc, baseVel, 47f, 1f, 0.07f, new Color(110,110,230,230).darker());
    	    			engine.spawnExplosion(portLoc, baseVel, new Color(90,90,212,160), 41f, 1.0f);
    	    			
    	    			// radial particles
    	    			ASF_RadialEmitter emitterRadial = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	    			emitterRadial.location(portLoc);
    	    			emitterRadial.life(0.37f, 0.53f);
    	    			emitterRadial.size(14f, 18f);
    	    			emitterRadial.velocity(8f, 6f);
    	    			emitterRadial.color(100,100,255,180);
    	    			emitterRadial.coreDispersion(10f);
    	    			emitterRadial.burst(23);
    	    			
    	    			// frontal particles
    	    			ASF_RadialEmitter emitterFrontal = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	    			emitterFrontal.location(portLoc);
    	    			emitterFrontal.angle(shotAngle, 0f);
    	    			emitterFrontal.life(0.28f, 0.39f);
    	    			emitterFrontal.size(15f, 13f);
    	    			emitterFrontal.velocity(3f, 43f);
    	    			emitterFrontal.distance(0f, 27.1f);
    	    			emitterFrontal.color(100,100,250,208);
    	    			emitterFrontal.emissionOffset(-25f, 50f);
    	    			emitterFrontal.coreDispersion(8f);
    	    			emitterFrontal.burst(34);
    	    			
    	                // sparkle particles
    	                ASF_RadialEmitter SparkleEmitter1 = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	    			SparkleEmitter1.location(portLoc);
    	    			SparkleEmitter1.angle(shotAngle -19f, 38f);
    	    			SparkleEmitter1.life(1.49f, 2.9f);
    	    	        SparkleEmitter1.size(2.3f, 4.3f);
    	    	        SparkleEmitter1.velocity(2f, 13f);
    	    	        SparkleEmitter1.distance(14f, 46f);
    	    	        SparkleEmitter1.color(39,39,240,234);
    	    	        SparkleEmitter1.velDistLinkage(false);
    	    	        SparkleEmitter1.lifeLinkage(true);
    	    	        SparkleEmitter1.emissionOffset(-65f, 130f);
    	    	        SparkleEmitter1.coreDispersion(5f);
    	    	        SparkleEmitter1.burst(42);
    	    	        ASF_RadialEmitter SparkleEmitter2 = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	    	        SparkleEmitter2.location(portLoc);
    	    	        SparkleEmitter2.angle(shotAngle -16f, 32f);
    	    	        SparkleEmitter2.life(1.33f, 2.53f);
    	    	        SparkleEmitter2.size(2.3f, 4.1f);
    	    	        SparkleEmitter2.velocity(3f, 13f);
    	    	        SparkleEmitter2.distance(14f, 37.3f);
    	    	        SparkleEmitter2.color(169,169,255,240);
    	    	        SparkleEmitter2.velDistLinkage(false);
    	    	        SparkleEmitter2.lifeLinkage(true);
    	    	        SparkleEmitter2.emissionOffset(-61f, 122f);
    	    	        SparkleEmitter2.burst(21);
    				}
    			}
    			
    			info.TARGET = null;
    			
        		info.READY = false;
        		info.LOCK = false;
        		info.TIMER = 0f;
        	}
        	
        	
        } else {
        	info.TIMER += amount;
        	if (info.TIMER >= rechargeTime) {
        		info.READY = true;
        	}
        }
        
		engine.getCustomData().put("ASF_ARTILLERY_DATA_KEY" + ship.getId(), info);
		
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
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		// this tactical artillery system is designed as extendable by other mods!
		// they just need have a ship with the base hullmod, and an individual "weapon" hullmod that follows the same mechanical setup as this one.
		
		LabelAPI label = tooltip.addPara("An integrated Tactical Artillery weapon.", opad);
		
		label = tooltip.addPara("Fires %s homing energy bolts, each of which deals %s damage.", opad, h, "Fourteen", (int)damage + " Energy");
		label.setHighlight("Fourteen", (int)damage + " Energy");
		label.setHighlightColors(h, h);
		
		label = tooltip.addPara("This weapon takes %s seconds to recharge after firing.", opad, h, "" + (int)rechargeTime);
		label.setHighlight("" + (int)rechargeTime);
		label.setHighlightColors(h);
		
	}
	
	@Override
    public int getDisplaySortOrder() {
        return 22213;
    }
	
}
