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
import org.amazigh.foundry.scripts.ASF_colapProjScript;
import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class ASF_ArtyWepRem extends BaseHullMod {

	private float rechargeTime = 4f;
	private float spoolTime = 2.5f; // portion of recharge time that the vfx is not spooling up
	private float damage = 150f;
	private float emp = 600f;
	
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
    		
    		for (int i=0; i < 2; i++) {
    			float sizeRandom1 = MathUtils.getRandomNumberInRange(28f, 38f);
    			Vector2f spriteSize1 = new Vector2f(sizeRandom1, sizeRandom1);
        		Vector2f spritePos1 = MathUtils.getRandomPointOnCircumference(portLoc, 2f);
            	MagicRender.singleframe(chargeGlow, spritePos1, spriteSize1, ship.getFacing() - 90f, new Color(100,172,255,alpha), true);
            	
    			float sizeRandom2 = MathUtils.getRandomNumberInRange(58f, 69f);
    			Vector2f spriteSize2 = new Vector2f(sizeRandom2, sizeRandom2);
            	Vector2f spritePos2 = MathUtils.getRandomPointOnCircumference(portLoc, i);
        		MagicRender.singleframe(chargeGlow, spritePos2, spriteSize2, ship.getFacing() - 90f, new Color(28,169,255,alpha), true);
    		}
    		
    		info.fxInterval1.advance(amount);
        	if (info.fxInterval1.intervalElapsed()) {
        		
        		int particleAlpha = (int) Math.ceil((20 + (120 * spoolVal)) / timeMult);
        		particleAlpha = Math.min(250, particleAlpha);
        		
            	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(portLoc, 1f),
            			ship.getVelocity(),
        				MathUtils.getRandomNumberInRange(12f, 15f), //size
        				1f, //brightness
        				MathUtils.getRandomNumberInRange(0.1f, 0.2f), //duration
        				new Color(89,178,255,particleAlpha));
        	}
        	
        	info.fxInterval2.advance(amount);
        	if (info.fxInterval2.intervalElapsed()) {
        		
        		int arcAlpha = Math.min(255, (int) Math.ceil((80 * spoolVal) / timeMult));
        		Vector2f arcPoint = MathUtils.getPointOnCircumference(portLoc, MathUtils.getRandomNumberInRange(12f, 17f), MathUtils.getRandomNumberInRange(0, 360));
        		
        		engine.spawnEmpArcVisual(portLoc, ship, arcPoint, ship, 7f,
        				new Color(25,130,157,10 + arcAlpha),
        				new Color(220,240,252,22 + arcAlpha));
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
    			
    			Global.getSoundPlayer().playSound("A_S-F_colap_fire", 1f, 1.1f, portLoc, baseVel);
    			
    			Global.getSoundPlayer().playSound("A_S-F_ionDriver_fire", 0.65f, 0.6f, portLoc, baseVel); // "punch" addition to the firing sound
    			
        		for (int i=0; i < 6; i++) {
        			
        			float realAngle = shotAngle + MathUtils.getRandomNumberInRange(-33f, 33f); // 20, 17
        			
        			CombatEntityAPI projC = engine.spawnProjectile(ship, null, "A_S-F_colap",
        					portLoc, // pos
        					realAngle, //angle
        					baseVel);
        			
        			engine.addPlugin(new ASF_colapProjScript((DamagingProjectileAPI) projC, info.TARGET));
        			
        		}
    			info.TARGET = null;
    			
    			
    			// muzzle effect
    			engine.addHitParticle(portLoc, baseVel, 69f, 1f, 0.09f, new Color(90,205,220,230).darker());
    			engine.spawnExplosion(portLoc, baseVel, new Color(85,195,210,200), 57f, 1.1f);
    			
    			// radial particles
    			ASF_RadialEmitter emitterRadial = new ASF_RadialEmitter((CombatEntityAPI) ship);
    			emitterRadial.location(portLoc);
    			emitterRadial.life(0.37f, 0.53f);
    			emitterRadial.size(15f, 30f);
    			emitterRadial.velocity(8f, 6f);
    			emitterRadial.color(100,195,255,202);
    			emitterRadial.coreDispersion(10f);
    			emitterRadial.burst(36);
    			
    			// frontal particles
    			ASF_RadialEmitter emitterFrontal = new ASF_RadialEmitter((CombatEntityAPI) ship);
    			emitterFrontal.location(portLoc);
    			emitterFrontal.angle(shotAngle, 0f);
    			emitterFrontal.life(0.25f, 0.35f);
    			emitterFrontal.size(15f, 13f);
    			emitterFrontal.velocity(3f, 43f);
    			emitterFrontal.distance(0f, 28.8f);
    			emitterFrontal.color(100,195,250,220);
    			emitterFrontal.emissionOffset(-13f, 26f);
    			emitterFrontal.coreDispersion(8f);
    			emitterFrontal.burst(40);
    			
    			// frontal particles
//                for (int i=0; i < 40; i++) {
//                	Vector2f velocity = MathUtils.getPointOnCircumference(baseVel, i + MathUtils.getRandomNumberInRange(0f, 3f), shotAngle + MathUtils.getRandomNumberInRange(-22f, 22f));
//                	Vector2f spawnLocation = MathUtils.getPointOnCircumference(portLoc, i * 0.72f, shotAngle);
//                	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, 8f);
//                	
//                	engine.addSmoothParticle(spawnLocation,
//                			velocity,
//                			MathUtils.getRandomNumberInRange(17f, 33f),
//                			0.9f,
//                			MathUtils.getRandomNumberInRange(0.25f, 0.35f),
//                			new Color(100,195,250,220));
//                }
    			
                // sparkle particles
                
                ASF_RadialEmitter SparkleEmitter1 = new ASF_RadialEmitter((CombatEntityAPI) ship);
    			SparkleEmitter1.location(portLoc);
    			SparkleEmitter1.angle(shotAngle -12f, 24f);
    			SparkleEmitter1.life(1.49f, 2.9f);
    	        SparkleEmitter1.size(2.3f, 4.3f);
    	        SparkleEmitter1.velocity(2f, 13f);
    	        SparkleEmitter1.distance(14f, 78f);
    	        SparkleEmitter1.color(29,170,255,211);
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
    	        SparkleEmitter2.color(25,110,170,216);
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
//                    			new Color(29,170,255,234));
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
//                			new Color(25,110,170,240));
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
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		// this tactical artillery system is designed as extendable by other mods!
		// they just need have a ship with the base hullmod, and an individual "weapon" hullmod that follows the same mechanical setup as this one.
		
		LabelAPI label = tooltip.addPara("An integrated Tactical Artillery weapon.", opad);
		
		label = tooltip.addPara("Fires %s guided ion bolts, each of which deals %s and %s damage.", opad, h, "Six", (int)damage + " Energy", (int)emp + " EMP");
		label.setHighlight("Six", (int)damage + " Energy", (int)emp + " EMP");
		label.setHighlightColors(h, h, h);
		
		label = tooltip.addPara("Hits on hull or armor will arc to weapons and engines. Hits on shields have a chance to generate a shield-penetrating arc based on the target's hard flux level.", pad);
		label = tooltip.addPara("Each arc deals %s and %s damage.", pad, h, (int)(damage * 0.4) + " Energy", (int)(emp * 0.75) + " EMP");
		label.setHighlight((int)(damage * 0.4) + " Energy", (int)(emp * 0.75) + " EMP");
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
