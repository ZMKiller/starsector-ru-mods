package org.amazigh.foundry.hullmods;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ASF_lebruckFoundry extends BaseHullMod {

	public static final float REGEN_BONUS = 80f;
	public static final float HEALTH_BONUS = 25f;
	
	public static final float REGEN_MALUS = 0.5f;
	
	public static final float MIN_DAMAGE_CLAMP_MULT = 2f; // the lowest time that is allowed for a missile to take to be forged
	public static final float FORGE_RATE_SML = 50f; // small slot forge "DPS"
	public static final float FORGE_RATE_MED = 100f; // med slot forge "DPS"
	
	private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
	
	public static final float ZWEIG_TIME = 4f;
	public static int MISSILE_CAP = 10;
	public static final float ZWEIG_RANGE = 800f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileAmmoRegenMult().modifyPercent(id, REGEN_BONUS);
		stats.getMissileHealthBonus().modifyPercent(id, HEALTH_BONUS);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("LEBRUCK_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
		MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();		
		
        float flux_scale = 0.2f + ((1f - ship.getFluxLevel()) * 0.8f);
        if (ship.getFluxTracker().isOverloadedOrVenting()) {
        	flux_scale = 0f;
        }
        stats.getMissileAmmoRegenMult().modifyMult(spec.getId() + "_MALUS", 1f - (REGEN_MALUS * (1f - ship.getFluxLevel())) ); // ALL missile regen is reduced by up to 50% as flux raises!
        
		if (ship.getVariant().getHullMods().contains("missleracks")) {
			MISSILE_CAP = 15;
		}
		
		
		boolean forging = false;
    	info.FORGE_1 = false;
    	info.FORGE_2 = false;
    	info.FORGE_3 = false;
    	info.FORGE_4 = false;
    	
    	WeaponAPI wep1 = null;
    	WeaponAPI wep2 = null;
    	WeaponAPI wep3 = null;
    	WeaponAPI wep4 = null;
		
		// check if the ship has valid weapons for forging
    	for (WeaponAPI w : ship.getAllWeapons()) {
    		
    		switch (w.getSlot().getId()) 
			{
				case "WS0006":
                if(wep1==null) {
                    wep1 = w;
                }
				break;
				case "WS0005":
	                if(wep2==null) {
	                    wep2 = w;
	                }
					break;
				case "WS0008":
	                if(wep3==null) {
	                    wep3 = w;
	                }
					break;
				case "WS0007":
	                if(wep4==null) {
	                    wep4 = w;
	                }
					break;
			}
    		
    		// abort if we're looking at a missile weapon that's not in one of the missile slots
    		if (w.getSlot().getWeaponType() != WeaponType.MISSILE && w.getType() != WeaponType.MISSILE) {
                continue;
            }
    		
            if (w == wep1) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		// checking that it's a missile weapon, so we don't give energy typed synergy weps ammo regen
            		if (w.getType() == WeaponType.MISSILE) {
            			info.M_1 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	info.FORGE_1 = true;
                        	info.DAMAGE_1 = Math.max((FORGE_RATE_MED * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
            if (w == wep2) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		// checking that it's a missile weapon, so we don't give energy typed synergy weps ammo regen
            		if (w.getType() == WeaponType.MISSILE) {
            			info.M_2 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	info.FORGE_2 = true;
                        	info.DAMAGE_2 = Math.max((FORGE_RATE_MED * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
            if (w == wep3) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		// checking that it's a missile weapon, so we don't give energy typed synergy weps ammo regen
            		if (w.getType() == WeaponType.MISSILE) {
            			info.M_3 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	info.FORGE_3 = true;
                        	info.DAMAGE_3 = Math.max((FORGE_RATE_SML * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
            if (w == wep4) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		// checking that it's a missile weapon, so we don't give energy typed synergy weps ammo regen
            		if (w.getType() == WeaponType.MISSILE) {
            			info.M_4 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	info.FORGE_4 = true;
                        	info.DAMAGE_4 = Math.max((FORGE_RATE_SML * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
		}
    	
    	if (forging) {
    		
    		if (!ship.getFluxTracker().isOverloadedOrVenting()) {
    			
    			float forgeScalarSml = amount * FORGE_RATE_SML * flux_scale;
    			float forgeScalarMed = amount * FORGE_RATE_MED * flux_scale;
    			    			
    			if (info.FORGE_1) {
    				info.TIMER_1 += forgeScalarMed;
    				
        			if (info.TIMER_1 > info.DAMAGE_1) {
        				info.TIMER_1 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        		    		if (w == wep1) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();
                        		
                        		float vol = 0.5f;
                        		if (info.DAMAGE_1 <= 200f) {
                        			vol = Math.min(2.5f, info.DAMAGE_1 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.1f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    			
    			if (info.FORGE_2) {
    				info.TIMER_2 += forgeScalarMed;
    				
        			if (info.TIMER_2 > info.DAMAGE_2) {
        				info.TIMER_2 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        		    		if (w == wep2) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();
                        		
                        		float vol = 0.5f;
                        		if (info.DAMAGE_2 <= 200f) {
                        			vol = Math.min(2.5f, info.DAMAGE_1 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.1f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    			
    			if (info.FORGE_3) {
    				info.TIMER_3 += forgeScalarSml;
    				
        			if (info.TIMER_3 > info.DAMAGE_3) {
        				info.TIMER_3 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        		    		if (w == wep3) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();
                        		
                        		float vol = 0.4f;
                        		if (info.DAMAGE_3 <= 160f) {
                        			vol = Math.min(2f, info.DAMAGE_3 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.2f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    			
    			if (info.FORGE_4) {
    				info.TIMER_4 += forgeScalarSml;
    				
        			if (info.TIMER_4 > info.DAMAGE_4) {
        				info.TIMER_4 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        		    		if (w == wep4) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();

                        		float vol = 0.4f;
                        		if (info.DAMAGE_4 <= 160f) {
                        			vol = Math.min(2f, info.DAMAGE_4 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.2f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    		}
    	}
    	
    	
		// below here is the auto-firing missile power fantasy (done via hullmod mainly to avoid autofire AI having a skill issue and turning the weapons off, but also because i thought it'd be cool)
    		// another reason tha makes it good to have this as a hullmod rather than a weapon, is the ship really runs low on weapon groups as-is!
		
		interval.advance(amount * flux_scale); // lower zweig rate when flux goes up!
        if (interval.intervalElapsed()) {
        	
        	float zweigMult = interval.getIntervalDuration();
        	if (ship.getSystem().isActive()) {
        		zweigMult *= 2.5f; // missile generation rate is boosted while system is active :)
        	}
        	info.TIMER += zweigMult;
        	
	        if (info.TIMER >= ZWEIG_TIME) {
	        	if (info.MISSILES < MISSILE_CAP) {
	            	info.MISSILES++;
	            	info.TIMER -= ZWEIG_TIME;
	        	}
	        }
	        
	        if (info.MISSILES > 0) {
	        	
	        	ShipAPI target = AIUtils.getNearestEnemy(ship);
	        	if (target != null) {
	        		if (MathUtils.getDistance(ship, AIUtils.getNearestEnemy(ship)) < ZWEIG_RANGE) {
			        	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			        		if (weapon.isSystemSlot()) {
			        			
			        			float randomArc = MathUtils.getRandomNumberInRange(-15f, 15f);
			        			
			        			Global.getCombatEngine().spawnProjectile(ship,
			        					null,
			        					"A_S-F_zweig_srm",
			        					weapon.computePosition(ship),
			        					weapon.getAngle() + ship.getFacing() + randomArc,
			        					ship.getVelocity());
			        			Global.getSoundPlayer().playSound("swarmer_fire", 1f, 0.95f, ship.getLocation(), ship.getVelocity());
			        			
			        			engine.addSwirlyNebulaParticle(weapon.computePosition(ship),
			        					ship.getVelocity(),
			        					MathUtils.getRandomNumberInRange(15f, 20f), //size
			        					2.0f, //end mult
			        					0.5f, //ramp fraction
			        					0.35f, //full bright fraction
			        					0.9f, //duration
			        					new Color(115,105,100,70),
			        					true);
			        			engine.addNebulaParticle(weapon.computePosition(ship),
			        					ship.getVelocity(),
			        					MathUtils.getRandomNumberInRange(20f, 25f), //size
			        					2.2f, //end mult
			        					0.5f, //ramp fraction
			        					0.5f, //full bright fraction
			        					1.1f, //duration
			        					new Color(115,105,100,95),
			        					true);
			        		}
			        	}
					info.MISSILES--;
		        	}
	        	}
	        }
        }
        
        if (ship == Global.getCombatEngine().getPlayerShip()) {
        	// missile reload info
        	if (info.M_1 || info.M_2 || info.M_3 || info.M_4) {
        		
    			String reloadInfo1 = "--";
    			String reloadInfo2 = "--";
    			String reloadInfo3 = "--";
    			String reloadInfo4 = "--";
    			
    			if (info.M_1) {
    				reloadInfo1 = "Loaded";
    				if (info.FORGE_1) {
        				reloadInfo1 = "Loading:" + (int) (info.TIMER_1 / info.DAMAGE_1 * 100f) +"%";
        			}
    			}
    			
    			if (info.M_2) {
    				reloadInfo2 = "Loaded";
    				if (info.FORGE_2) {
        				reloadInfo2 = "Loading:" + (int) (info.TIMER_2 / info.DAMAGE_2 * 100f) +"%";
        			}
    			}
    			
    			if (info.M_3) {
    				reloadInfo3 = "Loaded";
    				if (info.FORGE_3) {
        				reloadInfo3 = "Loading:" + (int) (info.TIMER_3 / info.DAMAGE_3 * 100f) +"%";
        			}
    			}
    			
    			if (info.M_4) {
    				reloadInfo4 = "Loaded";
    				if (info.FORGE_4) {
        				reloadInfo4 = "Loading:" + (int) (info.TIMER_4 / info.DAMAGE_4 * 100f) +"%";
        			}
    			}
    			
    			Global.getCombatEngine().maintainStatusForPlayerShip("LEBRUCK_REGEN", "graphics/icons/hullsys/missile_racks.png",  "Microforge Status:", reloadInfo1 + " / " + reloadInfo2 + " / " + reloadInfo3 + " / " + reloadInfo4, false);
        		
        	}

        	// zweig production info
        	String reloadInfo = "";
        	if (info.MISSILES == MISSILE_CAP) {
        		reloadInfo = "Fully Loaded";
        	} else {
        		float zweigPercent = 100f * (info.TIMER / ZWEIG_TIME);
        		reloadInfo = "Reload Progress: " + (int) zweigPercent + "%";
        	}
			Global.getCombatEngine().maintainStatusForPlayerShip("LEBRUCKAMMO", "graphics/icons/hullsys/missile_racks.png",  (info.MISSILES * 2) + " Zweig SRMs Stored", reloadInfo, false);
			
			
		}
        
        Global.getCombatEngine().getCustomData().put("LEBRUCK_DATA_KEY" + ship.getId(), info);
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
		float listpad = 6f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		Color miss = Misc.getMissileMountColor();
		Color grey = Misc.getGrayColor();
		Color banner = new Color(21,64,77);
		Color bad = Misc.getNegativeHighlightColor();
		
		boolean hasMissile1 = false;
		boolean missile1Valid = false;
		boolean hasMissile2 = false;
		boolean missile2Valid = false;
		boolean hasMissile3 = false;
		boolean missile3Valid = false;
		boolean hasMissile4 = false;
		boolean missile4Valid = false;
		String missile1 = "Left Medium";
		String missile1b = "Empty";
		String missile2 = "Right Medium";
		String missile2b = "Empty";
		String missile3 = "Left Small";
		String missile3b = "Empty";
		String missile4 = "Right Small";
		String missile4b = "Empty";
		
		//Unused generator capacity is re-routed to this specialised missile autoforge, offering the maximum output at 0 flux and scaling down to the minimum output at 80% or higher flux.
		// most of the below scaled with flux levels at one point, but uh, it was unecessary complication tbqh?
			// i brought it back! yah but really, it let's me BALANS hog wild regen stats!!!
		
		LabelAPI label = tooltip.addPara("A set of custom missile fabrication modules grants this vessel superior sustained missile firepower, but they suffer degraded performance when the ship takes flux pressure.", pad);
		
		label = tooltip.addPara("Missile hitpoints increased by %s.", opad, h, "" + (int)HEALTH_BONUS + "%");
		label.setHighlight("" + (int)HEALTH_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Missile Weapon ammo regeneration is increased by %s.", pad, h, "" + (int)REGEN_BONUS + "%");
		label.setHighlight("" + (int)REGEN_BONUS + "%");
		label.setHighlightColors(h);
		
		tooltip.addSectionHeading("Zweig Autofoundry ", miss, banner, Alignment.MID, opad);
		label = tooltip.addPara("An internal autofoundry that fabricates a pair of enhanced Swarmer SRMs every %s seconds. ", opad, h, "" + (int)ZWEIG_TIME + "");
		label.setHighlight("" + (int)ZWEIG_TIME + "");
		label.setHighlightColors(h);
		label = tooltip.addPara("Up to %s missiles can be stored, missiles will automatically launch if there is an enemy within %s range", pad, h, "20", "" + (int)ZWEIG_RANGE);
		label.setHighlight("20", "" + (int)ZWEIG_RANGE);
		label.setHighlightColors(h, h);
		label = tooltip.addPara("%s %s %s %s", opad, grey, "Installation of", "Expanded Missile Racks", "Increases maximium stored missile Count to", "30");
		label.setHighlight("Installation of", "Expanded Missile Racks", "Increases maximium stored missile Count to", "30");
		label.setHighlightColors(grey, h, grey, h);
		
		tooltip.addSectionHeading("Missile Microforge", miss, banner, Alignment.MID, opad);
		label = tooltip.addPara("This foundry incorporates a microforge that forges ammo for any non-reloading missile weapons installed in one of the ships %s mounts.", opad, miss, "Missile");
		label.setHighlight("Missile");
		label.setHighlightColors(miss);
//		label = tooltip.addPara("Weapons installed in the medium mounts will forge ammo at a rate that gives each launcher an equivalent of %s.", pad, h, (int)FORGE_RATE_MED + " DPS");
		label = tooltip.addPara("Weapons installed in the medium mounts will forge ammo giving each weapon the equivalent of %s.", pad, h, (int)FORGE_RATE_MED + " DPS");
		label.setHighlight((int)FORGE_RATE_MED + " DPS");
		label.setHighlightColors(h);
//		label = tooltip.addPara("Weapons installed in the small mounts will forge ammo at a rate that gives each launcher an equivalent of %s.", pad, h, (int)FORGE_RATE_SML + " DPS");
		label = tooltip.addPara("Weapons installed in the small mounts will forge ammo giving each weapon the equivalent of %s.", pad, h, (int)FORGE_RATE_SML + " DPS");
		label.setHighlight((int)FORGE_RATE_SML + " DPS");
		label.setHighlightColors(h);
		label = tooltip.addPara("%s", pad, grey, "If the installed weapon deals more EMP than damage, then the EMP value will be used to determine reload time.");
		label.setHighlight("If the installed weapon deals more EMP than damage, then the EMP value will be used to determine reload time.");
		label.setHighlightColors(grey);
		label = tooltip.addPara("%s", pad, grey, "Forge reload time cannot be lower than 2 Seconds regardless of weapon damage.");
		label.setHighlight("Forge reload time cannot be lower than 2 Seconds regardless of weapon damage.");
		label.setHighlightColors(grey);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			tooltip.addSectionHeading("Microforge Status", miss, banner, Alignment.MID, opad);
			
			if (ship.getVariant().getWeaponSpec("WS0006") != null) {
				if (ship.getVariant().getWeaponSpec("WS0006").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissile1 = true;
				}
			}
			
			if (ship.getVariant().getWeaponSpec("WS0005") != null) {
				if (ship.getVariant().getWeaponSpec("WS0005").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissile2 = true;
				}
			}
			
			if (ship.getVariant().getWeaponSpec("WS0008") != null) {
				if (ship.getVariant().getWeaponSpec("WS0008").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissile3 = true;
				}
			}
			
			if (ship.getVariant().getWeaponSpec("WS0007") != null) {
				if (ship.getVariant().getWeaponSpec("WS0007").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissile4 = true;
				}
			}
			
			for (WeaponAPI w : ship.getAllWeapons()) {
	    		if (w.getSlot().getWeaponType() != WeaponType.MISSILE) {
	                continue;
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0006")) {
	            	missile1 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getSpec().getAmmoPerSecond() <= 0) {
	            		missile1b = "" + (double) (Math.max((FORGE_RATE_MED * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE_MED;
	                	missile1Valid = true;
	            	} else {
	            		missile1b = "Not valid for Microforge.";
	            	}
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0005")) {
	            	missile2 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getAmmoPerSecond() <= 0) {
	            		missile2b = "" + (double) (Math.max((FORGE_RATE_MED * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE_MED;
	                	missile2Valid = true;
	            	} else {
	            		missile2b = "Not valid for Microforge.";
	            	}
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0008")) {
	            	missile3 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() <= 0) {
	                	missile3b = "" + (double) (Math.max((FORGE_RATE_SML * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE_SML;
	                	missile3Valid = true;
	            	} else {
	            		missile3b = "Not valid for Microforge.";
	            	}
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0007")) {
	            	missile4 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() <= 0) {
	                	missile4b = "" + (double) (Math.max((FORGE_RATE_SML * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE_SML;
	                	missile4Valid = true;
	            	} else {
	            		missile4b = "Not valid for Microforge.";
	            	}
	            }
			}
			
			if (hasMissile1) {
				if (missile1Valid) {
					label = tooltip.addPara("%s - Forge Time: %s Seconds.", opad, miss, missile1, missile1b);
					label.setHighlight(missile1, missile1b);
					label.setHighlightColors(miss, h);
				} else {
					label = tooltip.addPara("%s - %s", opad, miss, missile1, missile1b);
					label.setHighlight(missile1, missile1b);
					label.setHighlightColors(miss, bad);
				}
			} else {
				label = tooltip.addPara("%s - %s", opad, miss, missile1, missile1b);
				label.setHighlight(missile1, missile1b);
				label.setHighlightColors(grey, grey);
			}
			if (hasMissile2) {
				if (missile2Valid) {
					label = tooltip.addPara("%s - Forge Time: %s Seconds.", listpad, miss, missile2, missile2b);
					label.setHighlight(missile2, missile2b);
					label.setHighlightColors(miss, h);
				} else {
					label = tooltip.addPara("%s - %s", listpad, miss, missile2, missile2b);
					label.setHighlight(missile2, missile2b);
					label.setHighlightColors(miss, bad);
				}
			} else {
				label = tooltip.addPara("%s - %s", listpad, miss, missile2, missile2b);
				label.setHighlight(missile2, missile2b);
				label.setHighlightColors(grey, grey);
			}
			if (hasMissile3) {
				if (missile3Valid) {
					label = tooltip.addPara("%s - Forge Time: %s Seconds.", listpad, miss, missile3, missile3b);
					label.setHighlight(missile3, missile3b);
					label.setHighlightColors(miss, h);
				} else {
					label = tooltip.addPara("%s - %s", listpad, miss, missile3, missile3b);
					label.setHighlight(missile3, missile3b);
					label.setHighlightColors(miss, bad);
				}
			} else {
				label = tooltip.addPara("%s - %s", listpad, miss, missile3, missile3b);
				label.setHighlight(missile3, missile3b);
				label.setHighlightColors(grey, grey);
			}
			if (hasMissile4) {
				if (missile4Valid) {
					label = tooltip.addPara("%s - Forge Time: %s Seconds.", listpad, miss, missile4, missile4b);
					label.setHighlight(missile4, missile4b);
					label.setHighlightColors(miss, h);
				} else {
					label = tooltip.addPara("%s - %s", listpad, miss, missile4, missile4b);
					label.setHighlight(missile4, missile4b);
					label.setHighlightColors(miss, bad);
				}
			} else {
				label = tooltip.addPara("%s - %s", listpad, miss, missile4, missile4b);
				label.setHighlight(missile4, missile4b);
				label.setHighlightColors(grey, grey);
			}
			
		}
		
		label = tooltip.addPara("%s %s %s %s %s %s %s", opad, grey, "Forge rate of both the", "Zweig Autofoundry", "and", "Missile Microforge", "are reduced to a minimum of", "20%", "as flux levels raise and progress will pause if the ship is overloaded or venting.");
		label.setHighlight("Forge rate of both the", "Zweig Autofoundry", "and", "Missile Microforge", "are reduced to a minimum of", "20%", "as flux levels raise and progress will pause if the ship is overloaded or venting.");
		label.setHighlightColors(grey, h, grey, h, grey, bad, grey);
		
		int regenMalus = (int)(REGEN_MALUS * 100f);
		label = tooltip.addPara("%s %s %s", pad, grey, "Overall missile weapon ammunition regeneration rate is reduced by up to", regenMalus + "%", "as flux levels raise.");
		label.setHighlight("Overall missile weapon ammunition regeneration rate is reduced by up to", regenMalus + "%", "as flux levels raise.");
		label.setHighlightColors(grey, bad, grey);
		
	}

    private class ShipSpecificData {
        private int MISSILES = 12;
        private float TIMER = 0f;
        
		boolean FORGE_1 = false;
		boolean M_1 = false;
		float TIMER_1 = 0f;
		float DAMAGE_1 = 0f;
		
		boolean FORGE_2 = false;
		boolean M_2 = false;
		float TIMER_2 = 0f;
		float DAMAGE_2 = 0f;
		
		boolean FORGE_3 = false;
		boolean M_3 = false;
		float TIMER_3 = 0f;
		float DAMAGE_3 = 0f;
		
		boolean FORGE_4 = false;
		boolean M_4 = false;
		float TIMER_4 = 0f;
		float DAMAGE_4 = 0f;
    }

}
