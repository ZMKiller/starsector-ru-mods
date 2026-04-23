package org.amazigh.foundry.scripts;

import java.awt.Color;
import java.util.List;
import org.amazigh.foundry.hullmods.ASF_ArtyMount.ShipSpecificData;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.TimeoutTracker;

public class ASF_PainterBeamEffect implements BeamEffectPlugin {

	private IntervalUtil chargeInterval = new IntervalUtil(1f, 1f);
	private boolean wasZero = true;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			chargeInterval.advance(dur);
			
			if (chargeInterval.intervalElapsed()) {
				ShipAPI targetShip = (ShipAPI) target;
				
				if (!targetShip.hasListenerOfClass(ASF_artyTargetListener.class)) {
					targetShip.addListener(new ASF_artyTargetListener(targetShip));
				}
				
				List<ASF_artyTargetListener> listeners = targetShip.getListeners(ASF_artyTargetListener.class);
				if (listeners.isEmpty()) return; // ??? (idk either alex, but sanity checks are a real one)
				
				ASF_artyTargetListener listener = listeners.get(0);
				
				if (listener.recentHits.getItems().size() >= 8) {
					Vector2f point = beam.getRayEndPrevFrame();
					float dam = beam.getDamage().getDamage() * 0.4f; //20
					float emp = beam.getDamage().getDamage() * 4f; //200
						// arcs are slightly under half the power of Ion Beam arcs, at the "average" rate of ion beam arcs (but with no shield piercing and instead minor hardflux on shield!)
					engine.spawnEmpArc(
									   beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
									   DamageType.ENERGY, 
									   dam, // damage
									   emp, // emp 
									   1000f, // max range 
									   "A_S-F_quiet_emp_impact",
									   beam.getWidth() + 3f,
									   beam.getFringeColor(),
									   beam.getCoreColor()
									   );
					
				} else {
					String uniqueHash = (String.valueOf(beam.hashCode()) + String.valueOf((int) engine.getTotalElapsedTime(true)));
					
					// generating a unique identifying int for each damage instance caused by the beam
					listener.notifyHit(uniqueHash);
				}
				
			}
		}
	}
	
	
	public static class ASF_artyTargetListener implements AdvanceableListener {
		protected ShipAPI ship;
		
		public TimeoutTracker<String> recentHits = new TimeoutTracker<String>();
		
		IntervalUtil CheckTimer = new IntervalUtil(0.2f,0.2f); // only run checks every 0.2s
		
		public ASF_artyTargetListener(ShipAPI ship) {
			this.ship = ship;
		}
		
		public void notifyHit(String uniqueHash) {
			recentHits.add(uniqueHash, 10.1f, 10.1f); // just to guarantee that one beam can build up 10 charges
		}
		
		public void advance(float amount) {
			recentHits.advance(amount);

			int count = recentHits.getItems().size();
			
			if (count < 1) {
				ship.removeListener(this);
			}
			
			if (ship.isHulk() || !ship.isAlive()) {
				ship.removeListener(this);
				return;
			}

			CombatEngineAPI engine = Global.getCombatEngine();
			// rendering "pips" to mark the number of charges the target has
			float angle = engine.getTotalElapsedTime(false) + ship.getFullTimeDeployed();
    		// random (but consistent per ship) angular orientation for the "pips", we also have them *very slowly* rotate around the target
			
			float spriteScale = Math.max(0.4f, Math.min(1f, ship.getCollisionRadius() / 200f));
			// sprite size (and offset) scales with target size, going down if the target has collision radius below 200, and stopping scaling down at 80 or lower radius
			
    		double timeMult = (double) ship.getMutableStats().getTimeMult().modified; // because sprite rendering gets screwy with increases to timescale
    		int alpha = (int) Math.ceil(150 / timeMult);
    		alpha = Math.min(250, alpha);
    		
    		int red = 100 + (int) (count * 15);
    		int blue = 250 - (int) (count * 15);
    		
    		int alphaRing = (int) ((alpha + (count * 5f)) * 0.3f); 
    		alphaRing = Math.min(250, alphaRing);
    		
			Vector2f tagRingSize = new Vector2f((ship.getCollisionRadius() * 2.1f) + (15f * spriteScale), (ship.getCollisionRadius() * 2.1f) + (15f * spriteScale));
			
			SpriteAPI tagRing = Global.getSettings().getSprite("fx", "A_S-F_arty_mark_ring");
        	MagicRender.singleframe(tagRing, ship.getLocation(), tagRingSize, angle, new Color(red,100,blue,alphaRing), true);
			
			Vector2f spriteSize = new Vector2f(14f * spriteScale, 34f * spriteScale);
			
			for (int i=0; i < count; i++) {
    			
        		angle += 45; 
        		
        		Vector2f pipLoc = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + (10f * spriteScale), angle);
        		
        		SpriteAPI tagPip = Global.getSettings().getSprite("fx", "A_S-F_arty_target_pip");
        		
            	MagicRender.singleframe(tagPip, pipLoc, spriteSize, angle - 90f, new Color(red,100,blue,alpha), false);
            	
        	}
			
			CheckTimer.advance(amount);
			if (CheckTimer.intervalElapsed()) {
				if (count >= 8) {
					
					for (ShipAPI target_ship : engine.getShips()) {
						if (target_ship.isHulk() || !target_ship.isAlive()) {
							continue; // skip dead ships
						}
						
						if (target_ship.getOwner() != ship.getOwner()) {
							// check if the ship is an enemy to the targeted ship
							if (target_ship.getHullSpec().isBuiltInMod("A_S-F_ArtyMount")) {
								// check if the ship has the artillery ship hullmod
								

								if (target_ship == ship) {
									continue; // don't fire at yourself idiot.
								}
								
								if (target_ship.getFluxTracker().isOverloadedOrVenting()) {
									continue; // skip the ship if it's overloaded or venting
								}
								
								if (MathUtils.isWithinRange(ship, target_ship, 5000f)) {
									// check if the Artillery ship is within 5000 range of the target
									
									ShipSpecificData tagInfo = (ShipSpecificData) engine.getCustomData().get("ASF_ARTILLERY_DATA_KEY" + target_ship.getId());
									if (tagInfo == null) {
							            continue; // sanity check!
							        }
							        
							        if ((tagInfo.READY == true) && (tagInfo.LOCK == false)) {
							        	tagInfo.LOCK = true;
							        	tagInfo.TARGET = ship;
										engine.getCustomData().put("ASF_ARTILLERY_DATA_KEY" + target_ship.getId(), tagInfo);
										recentHits.clear();
										
										// "pulse" effect to show the charges have been cleared and an artillery weapon has been fired
										SpriteAPI markRing = Global.getSettings().getSprite("fx", "A_S-F_arty_target_ring");
										Vector2f ringSize = new Vector2f((ship.getCollisionRadius() * 2.1f) + (15f * spriteScale), (ship.getCollisionRadius() * 2.1f) + (15f * spriteScale));
										Vector2f ringGrowth = new Vector2f(ship.getCollisionRadius() * 0.69f, ship.getCollisionRadius() * 0.69f);
										
										MagicRender.battlespace(markRing, ship.getLocation(), ship.getVelocity(),
												ringSize, //size
												ringGrowth, //growth
												MathUtils.getRandomNumberInRange(0f,360f), //angle
												MathUtils.getRandomNumberInRange(-10f,10f),  //spin
												new Color(230,100,120,150),
												true,
												0.1f, //fadein
												0.1f, //full
												0.9f); //fadeout
										Global.getSoundPlayer().playSound("ui_new_radar_icon", 0.6f, 1.2f, ship.getLocation(), ship.getVelocity());
										
										return; // a sanity check or smth idk!
							        }
								}
							}
						}
					}
				}
			}
			
		}
		
		public String modifyDamageTaken(Object param,
				CombatEntityAPI target, DamageAPI damage,
				Vector2f point, boolean shieldHit) {
			return null;
		}
	}
}
