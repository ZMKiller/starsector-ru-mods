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
import org.amazigh.foundry.scripts.ASF_diadolProjScript;
import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class ASF_ArtyWepHighTech extends BaseHullMod {

	private float rechargeTime = 3f;
	private float spoolTime = 1.5f; // portion of recharge time that the vfx is not spooling up
	private float damage = 1500f;
	
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

        	Vector2f portLoc = new Vector2f();
			for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
				if (port.isDecorative()) {
					portLoc = port.computePosition(ship);
				}
			}
			
			float spoolVal = info.TIMER - spoolTime;
    		SpriteAPI chargeGlow = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
    		
    		double timeMult = (double) ship.getMutableStats().getTimeMult().modified; // this timeMult stuff is a "well fuck sprite rendering gets screwy with increases to timescale, let's fix it!"
    		int alpha = (int) Math.ceil((5 + (24 * spoolVal)) / timeMult);
    		alpha = Math.min(250, alpha);
    		
    		for (int i=0; i < 2; i++) {
    			float sizeRandom1 = MathUtils.getRandomNumberInRange(28f, 38f);
    			Vector2f spriteSize1 = new Vector2f(sizeRandom1, sizeRandom1);
        		Vector2f spritePos1 = MathUtils.getRandomPointOnCircumference(portLoc, 2f);
            	MagicRender.singleframe(chargeGlow, spritePos1, spriteSize1, ship.getFacing() - 90f, new Color(141,90,225,alpha), true);
            	
    			float sizeRandom2 = MathUtils.getRandomNumberInRange(58f, 69f);
    			Vector2f spriteSize2 = new Vector2f(sizeRandom2, sizeRandom2);
            	Vector2f spritePos2 = MathUtils.getRandomPointOnCircumference(portLoc, i);
        		MagicRender.singleframe(chargeGlow, spritePos2, spriteSize2, ship.getFacing() - 90f, new Color(69,10,255,alpha), true);
    		}
    		
    		
        	info.fxInterval1.advance(amount);
        	if (info.fxInterval1.intervalElapsed()) {

        		int particleAlpha = (int) Math.ceil((22 + (120 * spoolVal)) / timeMult);
        		particleAlpha = Math.min(250, particleAlpha);
        		
        		for (int i=0; i < 3; i++) {
            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 9f));
            		
                	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(portLoc, 7.6f),
                    		sparkVel,
            				MathUtils.getRandomNumberInRange(2.5f, 4.6f), //size
            				1f, //brightness
            				MathUtils.getRandomNumberInRange(0.1f, 0.2f), //duration
            				new Color(255,103,169,particleAlpha));
            	}
        		
            	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(portLoc, 1f),
            			ship.getVelocity(),
        				MathUtils.getRandomNumberInRange(13f, 15f), //size
        				1f, //brightness
        				MathUtils.getRandomNumberInRange(0.1f, 0.2f), //duration
        				new Color(255,113,146,particleAlpha));
        	}
        }
        
        
        if (info.READY) {
        	
    		if (info.LOCK) {
    			
    			Vector2f portLoc = new Vector2f();
    			for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
    				if (port.isDecorative()) {
    					portLoc = port.computePosition(ship);
    				}
    			}
    			
    			float shotAngle = VectorUtils.getAngle(ship.getLocation(), info.TARGET.getLocation());
    			Vector2f baseVel = ship.getVelocity();
    			
    			Global.getSoundPlayer().playSound("A_S-F_diadol_fire", 1f, 1f, portLoc, baseVel);
    			
    			Global.getSoundPlayer().playSound("hit_heavy_energy", 0.6f, 0.8f, portLoc, baseVel); // "punch" addition to the firing sound
    			
    			CombatEntityAPI projC = engine.spawnProjectile(ship, null, "A_S-F_diadol",
    					portLoc, // pos
    					shotAngle, //angle
    					baseVel);
    			
    			engine.addPlugin(new ASF_diadolProjScript((DamagingProjectileAPI) projC, info.TARGET));
    			info.TARGET = null;
    			
    			// muzzle effect
    			engine.addHitParticle(portLoc, baseVel, 75f, 1f, 0.1f, new Color(255,103,169,222).darker());
    			engine.spawnExplosion(portLoc, baseVel, new Color(174,25,253,247), 80f, 1.2f);
    			
    			// radial particles
    			ASF_RadialEmitter emitterRadial = new ASF_RadialEmitter((CombatEntityAPI) ship);
    			emitterRadial.location(portLoc);
    			emitterRadial.life(0.37f, 0.53f);
    			emitterRadial.size(15f, 30f);
    			emitterRadial.velocity(8f, 6f);
    			emitterRadial.color(255,97,218,212); //255,103,169
    			emitterRadial.coreDispersion(10f);
    			emitterRadial.burst(36);
    			
    			// frontal particles
    			ASF_RadialEmitter emitterFrontal = new ASF_RadialEmitter((CombatEntityAPI) ship);
    			emitterFrontal.location(portLoc);
    			emitterFrontal.angle(shotAngle, 0f);
    			emitterFrontal.life(0.25f, 0.35f);
    			emitterFrontal.size(16f, 15f);
    			emitterFrontal.velocity(3f, 43f);
    			emitterFrontal.distance(0f, 28.8f);
    			emitterFrontal.color(255,103,201,222); //255,103,169
    			emitterFrontal.emissionOffset(-15f, 30f);
    			emitterFrontal.coreDispersion(8f);
    			emitterFrontal.burst(40);
    			
                // sparkle particles
    			ASF_RadialEmitter SparkleEmitter1 = new ASF_RadialEmitter((CombatEntityAPI) ship);
    			SparkleEmitter1.location(portLoc);
    			SparkleEmitter1.angle(shotAngle -12f, 24f);
    			SparkleEmitter1.life(1.49f, 2.9f);
    	        SparkleEmitter1.size(2.3f, 4.3f);
    	        SparkleEmitter1.velocity(2f, 13f);
    	        SparkleEmitter1.distance(14f, 78f);
    	        SparkleEmitter1.color(182,26,255,251);
    	        SparkleEmitter1.velDistLinkage(false);
    	        SparkleEmitter1.lifeLinkage(true);
    	        SparkleEmitter1.emissionOffset(-57f, 114f);
    	        SparkleEmitter1.coreDispersion(5f);
    	        SparkleEmitter1.burst(46);
    	        ASF_RadialEmitter SparkleEmitter2 = new ASF_RadialEmitter((CombatEntityAPI) ship);
    	        SparkleEmitter2.location(portLoc);
    	        SparkleEmitter2.angle(shotAngle -11f, 22f);
    	        SparkleEmitter2.life(1.33f, 2.53f);
    	        SparkleEmitter2.size(2.3f, 4.1f);
    	        SparkleEmitter2.velocity(3f, 13f);
    	        SparkleEmitter2.distance(10f, 34.5f);
    	        SparkleEmitter2.color(233,87,255,253);
    	        SparkleEmitter2.velDistLinkage(false);
    	        SparkleEmitter2.lifeLinkage(true);
    	        SparkleEmitter2.emissionOffset(-54f, 108f);
    	        SparkleEmitter2.burst(23);
    	        
//                for (int i=0; i < 23; i++) {
//
//                    for (int j=0; j < 2; j++) {
//                    	float arcPoint = shotAngle + MathUtils.getRandomNumberInRange(-12f, 12f);
//                    	
//                    	Vector2f velocity = MathUtils.getPointOnCircumference(baseVel, MathUtils.getRandomNumberInRange(2f, 13f), shotAngle + MathUtils.getRandomNumberInRange(-69f, 69f));
//                    	
//                    	float sparkRange = 10 + (i*4);
//                    	Vector2f spawnLocation = MathUtils.getPointOnCircumference(portLoc, sparkRange, arcPoint);
//                    	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 5f));
//                    	
//                    	engine.addSmoothParticle(spawnLocation,
//                    			velocity,
//                    			MathUtils.getRandomNumberInRange(2.3f, 4.3f),
//                    			1f,
//                    			(i * 0.05f) + MathUtils.getRandomNumberInRange(1.44f, 1.75f),
//                    			new Color(105,15,255,234));
//                    }
//                    
//                    float arcPoint2 = shotAngle + MathUtils.getRandomNumberInRange(-11f, 11f);
//                	
//                	Vector2f velocity2 = MathUtils.getPointOnCircumference(baseVel, MathUtils.getRandomNumberInRange(3f, 13f), shotAngle + MathUtils.getRandomNumberInRange(-65f, 65f));
//                	
//                	float sparkRange2 = 10 + (i*4);
//                	Vector2f spawnLocation2 = MathUtils.getPointOnCircumference(portLoc, sparkRange2, arcPoint2);
//                	
//                	engine.addSmoothParticle(spawnLocation2,
//                			velocity2,
//                			MathUtils.getRandomNumberInRange(2.3f, 4.1f),
//                			1f,
//                			(i * 0.04f) + MathUtils.getRandomNumberInRange(1.29f, 1.61f),
//                			new Color(181,69,250,234));
//                }
                
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
		
		label = tooltip.addPara("Fires an energy bolt with limited tracking that deals %s damage.", opad, h, (int)damage + " Energy");
		label.setHighlight((int)damage + " Energy");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("This weapon takes %s seconds to recharge after firing.", opad, h, "" + (int)rechargeTime);
		label.setHighlight("" + (int)rechargeTime);
		label.setHighlightColors(h);
		
	}
	
	@Override
    public int getDisplaySortOrder() {
        return 22213;
    }
	
}
