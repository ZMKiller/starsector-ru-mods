package org.amazigh.foundry.hullmods;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class ASF_initoneBank extends BaseHullMod {
	
	public static final float BANK_CAPACITY = 4000f;
	
	private IntervalUtil arcInterval = new IntervalUtil(0.8f,1.0f);
	private IntervalUtil sparkleInterval1 = new IntervalUtil(0.1f,0.15f);
	private IntervalUtil sparkleInterval2 = new IntervalUtil(0.1f,0.15f);
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (!ship.isAlive() || ship.isPiece()) {
			return;
		}
		// Global.getCombatEngine().isPaused() ||
		
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("INITONE_BANK_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        CombatEngineAPI engine = Global.getCombatEngine();
        
        info.bankDiss = 100f;
        
        if (ship.getFluxTracker().isOverloaded()) {
        	info.bankDiss = 0f;
        }
		
        if (ship.getFluxTracker().isVenting() || ship.getFluxTracker().isEngineBoostActive()) {
        	info.bankDiss *= 2f;
        }
        
        if (ship.getSystem().isActive()) {
        	info.bankDiss *= 3f;
        }
        
        
        if (info.storedFlux > 0) {
            // render a glow over the bank, with slight jitter
        	SpriteAPI GlowA = Global.getSettings().getSprite("fx", "A_S-F_initone_glow_a");
        	SpriteAPI GlowB = Global.getSettings().getSprite("fx", "A_S-F_initone_glow_b");
        	Vector2f glowSizeA = new Vector2f(20f, 44f);
        	Vector2f glowSizeB = new Vector2f(26f, 58f);
        	Vector2f glowLocInitA = MathUtils.getPointOnCircumference(ship.getLocation(), 44f, ship.getFacing());
        	Vector2f glowLocInitB = MathUtils.getPointOnCircumference(ship.getLocation(), 48f, ship.getFacing());
        	
        	int alpha = 190;
        	float alphaMult = alpha * (info.storedFlux / BANK_CAPACITY);
        	double alphaTemp = alphaMult;
    		double timeMult = (double) ship.getMutableStats().getTimeMult().modified;
    		alpha = (int) Math.ceil(alphaTemp / timeMult);
        	alpha = Math.min(alpha, 255);
    		
        	MagicRender.singleframe(GlowB, glowLocInitB, glowSizeB, ship.getFacing() - 90f, new Color(130,70,155,alpha), true);
        	MagicRender.singleframe(GlowB, MathUtils.getRandomPointInCircle(glowLocInitB, 1f), glowSizeB, ship.getFacing() - 90f, new Color(130,70,155,alpha), true);
        	MagicRender.singleframe(GlowA, MathUtils.getRandomPointInCircle(glowLocInitA, 2f), glowSizeA, ship.getFacing() - 90f, new Color(130,70,155,alpha), true);
        	
        	// spawn some sparklies in the bank, and (sometimes) spawn some visual arcs.
            sparkleInterval1.advance(amount);
            sparkleInterval2.advance(amount);
        	arcInterval.advance(amount);
            if (sparkleInterval1.intervalElapsed()) {
            	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            		if (weapon.isSystemSlot()) {
            			for (int i=0; i < 3; i++) {
                			engine.addSmoothParticle(MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 3f), // loc
                					MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(75f, 130f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-2f, 2f)), // vel
            	            		MathUtils.getRandomNumberInRange(3f, 5f), // size
            	            		0.8f, // intensity
            	            		MathUtils.getRandomNumberInRange(0.45f, 0.6f), // lifetime
            	            		new Color(205,130,190,alpha));
            				
            			}
            		}
            	}
            }
            if (sparkleInterval2.intervalElapsed()) {
            	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            		if (weapon.isSystemSlot()) {
            			for (int i=0; i < 3; i++) {
                			engine.addSmoothParticle(MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 3f), // loc
                					MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(75f, 130f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-2f, 2f)), // vel
            	            		MathUtils.getRandomNumberInRange(3f, 5f), // size
            	            		1.0f, // intensity
            	            		MathUtils.getRandomNumberInRange(0.45f, 0.6f), // lifetime
            	            		new Color(180,80,165,alpha));
            				
            			}
            		}
            	}
            }
            if (arcInterval.intervalElapsed()) {
            	float fullness = Math.max(0.15f, info.storedFlux / BANK_CAPACITY);
            	// 15% base, and scales higher as stored flux levels increase
            	if (fullness > Math.random()) {
            		// if the RnG check has passed, spawn an emp arc across the "flux bank"
                	Vector2f arcPoint1 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), 1f);
                	Vector2f arcPoint2 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), 3f);
                	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                		if (weapon.isSystemSlot()) {
                			if (weapon.getSlotSize() == WeaponSize.SMALL) {
                				arcPoint1 = MathUtils.getRandomPointOnCircumference(weapon.computePosition(ship), 2f);
                			}
                			if (weapon.getSlotSize() == WeaponSize.MEDIUM) {
                				arcPoint2 = MathUtils.getRandomPointOnCircumference(weapon.computePosition(ship), 3f);
                			}
                		}
                	}
    				int alpha2 = (int) (fullness * 50);
    				engine.spawnEmpArcVisual(arcPoint1, ship,
    						arcPoint2, ship, 10f,
    	        			new Color(130,70,155,alpha2+10),
    						new Color(255,225,255,alpha2+20));
            	}
            }
        }
    	
        // lower the stored flux
        info.storedFlux = Math.max(0f, info.storedFlux - (info.bankDiss * amount));
        
        
        
    	float fluxDiff = amount * Math.min(800f, (ship.getFluxTracker().getCurrFlux() - ship.getFluxTracker().getHardFlux()));
    	
    	if (info.storedFlux < 4000f) {
        	if (fluxDiff > 0f) {
        		
        		// check to see if there is enough capacity to bank all of the fluxDiff, if not then don't.
        		if ((info.storedFlux + fluxDiff) < BANK_CAPACITY) {
            		
            		ship.getFluxTracker().setCurrFlux(Math.max(0f, ship.getFluxTracker().getCurrFlux() - fluxDiff));
            		info.storedFlux += fluxDiff;
        			
        		}
        	}
        	
    	}
        
        if (ship == Global.getCombatEngine().getPlayerShip()) {
            MagicUI.drawInterfaceStatusBar(
           		 ship,
           		 info.storedFlux / BANK_CAPACITY,
           		 null,
           		 null,
           		 0,
           		 "Bank",
           		 (int) info.storedFlux);
        }
        
        engine.getCustomData().put("INITONE_BANK_DATA_KEY" + ship.getId(), info);
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
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("This ship features a secondary flux bank which diverts and dissipates a portion of the ships generated soft flux.", pad);
		
		label = tooltip.addPara("The ship transfers up to %s soft flux a second into the secondary flux bank.", opad, h, "800");
		label.setHighlight("800");
		label.setHighlightColors(h);
		label = tooltip.addPara("The secondary flux bank dissipates %s flux per second and can store up to %s flux.", pad, h, "100", "" + (int) BANK_CAPACITY);
		label.setHighlight("100", "" + (int) BANK_CAPACITY);
		label.setHighlightColors(h, h);
		label = tooltip.addPara("The secondary flux bank dissipates flux at %s the normal rate if the ship has the %s, or is %s.", pad, h, "Double", "Zero-Flux Speed boost", "Actively Venting");
		label.setHighlight("Double", "Zero-Flux Speed boost", "Actively Venting");
		label.setHighlightColors(h, h, h);
		
		label = tooltip.addPara("The secondary flux bank %s dissipating if the ship is %s.", opad, bad, "Stops", "Overloaded");
		label.setHighlight("Stops", "Overloaded");
		label.setHighlightColors(bad, bad);
		
	}

    private class ShipSpecificData {
        private float storedFlux = 0f;
        private float bankDiss = 100f;
    }

}
