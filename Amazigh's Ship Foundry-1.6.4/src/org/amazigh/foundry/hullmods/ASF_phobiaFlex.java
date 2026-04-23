package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_phobiaFlex extends BaseHullMod {

	public static final float E_FLUX_BONUS = 4f;
	
	public static final float MIN_DAMAGE_CLAMP_MULT = 2f; // the lowest time that is allowed for a missile to take to be forged
	public static final float FORGE_RATE = 40f; // forge "DPS"
	
	public static String PHOBIA_DATA_KEY = "core_phobia_data_key";
	
	public static class PhobiaData {
		boolean FORGE_1 = false;
		boolean M_1 = false;
		float TIMER_1 = 0f;
		float DAMAGE_1 = 0f;
		//String SLOT_1 = "WS0003";
		
		boolean FORGE_2 = false;
		boolean M_2 = false;
		float TIMER_2 = 0f;
		float DAMAGE_2 = 0f;
		//String SLOT_2 = "WS0003";
		
		boolean FORGE_3 = false;
		boolean M_3 = false;
		float TIMER_3 = 0f;
		float DAMAGE_3 = 0f;
		//String SLOT_3 = "WS0003";
		
		boolean FORGE_4 = false;
		boolean M_4 = false;
		float TIMER_4 = 0f;
		float DAMAGE_4 = 0f;
		//String SLOT_4 = "WS0003";
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		int energyCount = 0;
		
		for (String slot : stats.getVariant().getNonBuiltInWeaponSlots() ) {
			if (stats.getVariant().getSlot(slot).getWeaponType() == WeaponType.UNIVERSAL && stats.getVariant().getWeaponSpec(slot).getType() == WeaponType.ENERGY) {
				energyCount++;
			}
		}
        
		if (energyCount > 0) {
			stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (energyCount * 0.01f * E_FLUX_BONUS));
		}
		
	}
	
    public void advanceInCombat(ShipAPI ship, float amount) {
    	
    	if (!ship.isAlive() || ship.getCurrentCR() == 0f) return;
		CombatEngineAPI engine = Global.getCombatEngine();
        
		// data thing here!!!
		
		String key = PHOBIA_DATA_KEY + "_" + ship.getId();
		PhobiaData data = (PhobiaData) engine.getCustomData().get(key);
		if (data == null) {
			data = new PhobiaData();
			engine.getCustomData().put(key, data);
		}
		
    	boolean forging = false;
    	data.FORGE_1 = false;
    	data.FORGE_2 = false;
    	data.FORGE_3 = false;
    	data.FORGE_4 = false;
    	
    	WeaponAPI wep1 = null;
    	WeaponAPI wep2 = null;
    	WeaponAPI wep3 = null;
    	WeaponAPI wep4 = null;
    	
    	
    	// check if the ship has valid weapons for forging
    	for (WeaponAPI w : ship.getAllWeapons()) {
    		
    		switch (w.getSlot().getId()) 
			{
				case "WS0003":
                if(wep1==null) {
                    wep1 = w;
                }
				break;
				case "WS0004":
	                if(wep2==null) {
	                    wep2 = w;
	                }
					break;
				case "WS0005":
	                if(wep3==null) {
	                    wep3 = w;
	                }
					break;
				case "WS0006":
	                if(wep4==null) {
	                    wep4 = w;
	                }
					break;
			}
    		
    		// abort if we find a missile weapon that's not in one of the universal slots
    		if (w.getSlot().getWeaponType() != WeaponType.UNIVERSAL && w.getType() != WeaponType.MISSILE) {
                continue;
            }
    		
    		// w.getSpec() == ship.getVariant().getWeaponSpec("WS0003")
    		// w.getSlot().getId() == data.SLOT_1
            if (w == wep1) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		
            		// checking that it's a missile weapon, so we don't give AMBs/etc ammo regen
            		if (w.getType() == WeaponType.MISSILE) {
            			
                		data.M_1 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	data.FORGE_1 = true;
                        	data.DAMAGE_1 = Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
            if (w == wep2) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		
            		// checking that it's a missile weapon, so we don't give AMBs/etc ammo regen
            		if (w.getType() == WeaponType.MISSILE) {
            			
                		data.M_2 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	data.FORGE_2 = true;
                        	data.DAMAGE_2 = Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
            if (w == wep3) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		
            		// checking that it's a missile weapon, so we don't give AMBs/etc ammo regen
            		if (w.getType() == WeaponType.MISSILE) {

                		data.M_3 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	data.FORGE_3 = true;
                        	data.DAMAGE_3 = Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
            if (w == wep4) {
            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() == 0) {
            		
            		// checking that it's a missile weapon, so we don't give AMBs/etc ammo regen
            		if (w.getType() == WeaponType.MISSILE) {
            			
                		data.M_4 = true;
                    	if (w.getAmmo() < w.getMaxAmmo()) {
                        	forging = true;
                        	data.FORGE_4 = true;
                        	data.DAMAGE_4 = Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()));
                    	}
            		}
                	continue;
            	}
            }
		}
    	
    	if (forging) {
    		
    		if (!ship.getFluxTracker().isOverloadedOrVenting()) {
    			
    			// interval advance variable (50% speed if phased)
    			float phaseScalar = 40f;
    			if (ship.isPhased()) {
    				phaseScalar = 20f;
	    		}
    			
    			if (data.FORGE_1) {
        			data.TIMER_1 += (amount * phaseScalar);
    				
        			if (data.TIMER_1 > data.DAMAGE_1) {
        				data.TIMER_1 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        					// w.getSpec() == ship.getVariant().getWeaponSpec("WS0006")
        		    		if (w == wep1) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();
                        		
                        		float vol = 0.4f;
                        		if (data.DAMAGE_1 <= 160f) {
                        			vol = Math.min(2f, data.DAMAGE_1 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.2f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    			
    			if (data.FORGE_2) {
        			data.TIMER_2 += (amount * phaseScalar);
    				
        			if (data.TIMER_2 > data.DAMAGE_2) {
        				data.TIMER_2 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        		    		if (w == wep2) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();
                        		
                        		float vol = 0.4f;
                        		if (data.DAMAGE_2 <= 160f) {
                        			vol = Math.min(2f, data.DAMAGE_2 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.2f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    			
    			if (data.FORGE_3) {
        			data.TIMER_3 += (amount * phaseScalar);
    				
        			if (data.TIMER_3 > data.DAMAGE_3) {
        				data.TIMER_3 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        		    		if (w == wep3) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();
                        		
                        		float vol = 0.4f;
                        		if (data.DAMAGE_3 <= 160f) {
                        			vol = Math.min(2f, data.DAMAGE_3 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.2f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    			
    			if (data.FORGE_4) {
        			data.TIMER_4 += (amount * phaseScalar);
    				
        			if (data.TIMER_4 > data.DAMAGE_4) {
        				data.TIMER_4 = 0f;
        				
        				for (WeaponAPI w : ship.getAllWeapons()) {
        		    		if (w == wep4) {
                        		w.setAmmo(w.getAmmo() + 1);
                        		w.beginSelectionFlash();

                        		float vol = 0.4f;
                        		if (data.DAMAGE_4 <= 160f) {
                        			vol = Math.min(2f, data.DAMAGE_4 / 400f);
                        		}
                            	Global.getSoundPlayer().playSound("system_forgevats", 1.2f, vol, w.getLocation(), ship.getVelocity());
        		            }
        				}
                	}
    			}
    		}
    	}
    	

    	
    	if (ship == Global.getCombatEngine().getPlayerShip()) {
    		
        	if (data.M_1 || data.M_2 || data.M_3 || data.M_4) {
        		
    			String reloadInfo1 = "--";
    			String reloadInfo2 = "--";
    			String reloadInfo3 = "--";
    			String reloadInfo4 = "--";
    			
    			if (data.M_1) {
    				reloadInfo1 = "Loaded";
    				if (data.FORGE_1) {
        				reloadInfo1 = "Loading:" + (int) (data.TIMER_1 / data.DAMAGE_1 * 100f) +"%";
        			}
    			}
    			
    			if (data.M_2) {
    				reloadInfo2 = "Loaded";
    				if (data.FORGE_2) {
        				reloadInfo2 = "Loading:" + (int) (data.TIMER_2 / data.DAMAGE_2 * 100f) +"%";
        			}
    			}
    			
    			if (data.M_3) {
    				reloadInfo3 = "Loaded";
    				if (data.FORGE_3) {
        				reloadInfo3 = "Loading:" + (int) (data.TIMER_3 / data.DAMAGE_3 * 100f) +"%";
        			}
    			}
    			
    			if (data.M_4) {
    				reloadInfo4 = "Loaded";
    				if (data.FORGE_4) {
        				reloadInfo4 = "Loading:" + (int) (data.TIMER_4 / data.DAMAGE_4 * 100f) +"%";
        			}
    			}
    			
    			Global.getCombatEngine().maintainStatusForPlayerShip("PHOBIA_REGEN", "graphics/icons/hullsys/missile_racks.png",  "Microforge Status:", reloadInfo1 + " / " + reloadInfo2 + " / " + reloadInfo3 + " / " + reloadInfo4, false);
        		
        	}
    		
		}
    	
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
		Color grey = Misc.getGrayColor();
		
		Color banner = new Color(21,64,77);
		
		Color ball = Misc.getBallisticMountColor();
		Color miss = Misc.getMissileMountColor();
		Color energy = Misc.getEnergyMountColor();
		Color uni = Misc.MOUNT_UNIVERSAL;
		
		Color kin = new Color(199,182,158);
		
		boolean empty = true;
		int energyCount = 0;
		int ballisticCount = 0;
		boolean hasMissiles = false;
		
		boolean hasMissile1 = false;
		boolean missile1Valid = false;
		boolean hasMissile2 = false;
		boolean missile2Valid = false;
		boolean hasMissile3 = false;
		boolean missile3Valid = false;
		boolean hasMissile4 = false;
		boolean missile4Valid = false;
		String missile1 = "";
		String missile1b = "";
		String missile2 = "";
		String missile2b = "";
		String missile3 = "";
		String missile3b = "";
		String missile4 = "";
		String missile4b = "";
		
		LabelAPI label = tooltip.addPara("This vessel features a set of adaptable mounts that feature a selection of support hardware that is variably activated depending on what weaponry is installed.", pad);
		
		//"Adaptive Variable Mounts"
		// This vessel features a set of adaptable mounts that feature a selection of support hardware that is variably activated depending on what weaponry is installed.
		
		tooltip.addSectionHeading("Ballistic Weapons", ball, banner, Alignment.MID, opad);
		label = tooltip.addPara("Each ballistic weapon installed in one of the %s mounts increases system sub-arc count by %s, and gives the sub-arcs a chance to pierce shields that scales with the targets hardflux level.", opad, uni, "Universal", "One");
		label.setHighlight("Universal", "One");
		label.setHighlightColors(uni, h);
		label = tooltip.addPara("%s %s %s %s %s %s %s %s %s", pad, grey, "System at base fires a main ", "400 Kinetic", " / ", "1600 EMP", " arc, and four ", "60 Energy", " / ", "500 EMP", " sub-arcs that chain from the primary impact.");
		label.setHighlight("System at base fires a main ", "400 Kinetic", " / ", "1600 EMP", " arc, and four ", "60 Energy", " / ", "500 EMP", " sub-arcs that chain from the primary impact.");
		label.setHighlightColors(grey, kin, grey, energy, grey, energy, grey, energy, grey);
		
		tooltip.addSectionHeading("Energy Weapons", energy, banner, Alignment.MID, opad);
		label = tooltip.addPara("Energy weapon flux cost reduced by %s for each energy weapon installed in one of the %s mounts.", opad, h, (int)E_FLUX_BONUS + "%", "Universal");
		label.setHighlight((int)E_FLUX_BONUS + "%", "Universal");
		label.setHighlightColors(h, uni);
		
		tooltip.addSectionHeading("Missile Weapons", miss, banner, Alignment.MID, opad);
		label = tooltip.addPara("Features a microforge that forges ammo for any non-reloading missile weapons installed in one of the %s mounts at a rate that gives each launcher an equivalent of %s. Reload rate is halved when phased and progress will pause if the ship is overloaded or venting.", opad, uni, "Universal", (int)FORGE_RATE + " DPS");
		label.setHighlight("Universal", (int)FORGE_RATE + " DPS");
		label.setHighlightColors(uni, h);
		label = tooltip.addPara("%s", pad, grey, "If the installed weapon deals more EMP than damage, then the EMP value will be used to determine reload time.");
		label.setHighlight("If the installed weapon deals more EMP than damage, then the EMP value will be used to determine reload time.");
		label.setHighlightColors(grey);
		label = tooltip.addPara("%s", pad, grey, "Forge reload time cannot be lower than " + (int)MIN_DAMAGE_CLAMP_MULT + " Seconds regardless of weapon damage.");
		label.setHighlight("Forge reload time cannot be lower than " + (int)MIN_DAMAGE_CLAMP_MULT + " Seconds regardless of weapon damage.");
		label.setHighlightColors(grey);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			tooltip.addSectionHeading("Current Bonuses", uni, banner, Alignment.MID, opad);
			
			if (ship.getVariant().getWeaponSpec("WS0003") != null) {
				if (ship.getVariant().getWeaponSpec("WS0003").getType() == WeaponAPI.WeaponType.ENERGY) {
					energyCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0003").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					ballisticCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0003").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissiles = true;
					hasMissile1 = true;
					empty = false;
				}
			}
			
			if (ship.getVariant().getWeaponSpec("WS0004") != null) {
				if (ship.getVariant().getWeaponSpec("WS0004").getType() == WeaponAPI.WeaponType.ENERGY) {
					energyCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0004").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					ballisticCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0004").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissiles = true;
					hasMissile2 = true;
					empty = false;
				}
			}
			
			if (ship.getVariant().getWeaponSpec("WS0005") != null) {
				if (ship.getVariant().getWeaponSpec("WS0005").getType() == WeaponAPI.WeaponType.ENERGY) {
					energyCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0005").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					ballisticCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0005").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissiles = true;
					hasMissile3 = true;
					empty = false;
				}
			}
			
			if (ship.getVariant().getWeaponSpec("WS0006") != null) {
				if (ship.getVariant().getWeaponSpec("WS0006").getType() == WeaponAPI.WeaponType.ENERGY) {
					energyCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0006").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					ballisticCount ++;
					empty = false;
				} else if (ship.getVariant().getWeaponSpec("WS0006").getType() == WeaponAPI.WeaponType.MISSILE) {
					hasMissiles = true;
					hasMissile4 = true;
					empty = false;
				}
			}
			
			for (WeaponAPI w : ship.getAllWeapons()) {
	    		if (w.getSlot().getWeaponType() != WeaponType.UNIVERSAL) {
	                continue;
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0003")) {
	            	missile1 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getSpec().getAmmoPerSecond() <= 0) {
	            		missile1b = "" + (double) (Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE;
	                	missile1Valid = true;
	            	} else {
	            		missile1b = "Not valid for Microforge.";
	            	}
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0004")) {
	            	missile2 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getAmmoPerSecond() <= 0) {
	            		missile2b = "" + (double) (Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE;
	                	missile2Valid = true;
	            	} else {
	            		missile2b = "Not valid for Microforge.";
	            	}
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0005")) {
	            	missile3 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() <= 0) {
	                	missile3b = "" + (double) (Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE;
	                	missile3Valid = true;
	            	} else {
	            		missile3b = "Not valid for Microforge.";
	            	}
	            }
	            if (w.getSpec() == ship.getVariant().getWeaponSpec("WS0006")) {
	            	missile4 = "" + w.getDisplayName();
	            	if (w.usesAmmo() && w.getAmmoTracker().getAmmoPerSecond() <= 0) {
	                	missile4b = "" + (double) (Math.max((FORGE_RATE * MIN_DAMAGE_CLAMP_MULT), Math.max(w.getDerivedStats().getDamagePerShot(), w.getDerivedStats().getEmpPerShot()))) / FORGE_RATE;
	                	missile4Valid = true;
	            	} else {
	            		missile4b = "Not valid for Microforge.";
	            	}
	            }
			}
			
			if (!empty) {
				
				if (ballisticCount > 0) {
					label = tooltip.addPara("System sub-arc count increased by %s, and sub-arcs have up to a %s chance to pierce shields scaling with the targets hardflux level.", opad, h, ballisticCount + "", (ballisticCount * 20) + "%");
					label.setHighlight(ballisticCount + "", (ballisticCount * 20) + "%");
					label.setHighlightColors(h, h);
				}
				
				if (energyCount > 0){
					label = tooltip.addPara("Energy weapon flux cost reduced by %s.", opad, h, (int)(E_FLUX_BONUS * energyCount) + "%");
					label.setHighlight((int)(E_FLUX_BONUS * energyCount) + "%");
					label.setHighlightColors(h);
				}
				
				if (hasMissiles) {
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
					}
					if (hasMissile2) {
						if (missile2Valid) {
						label = tooltip.addPara("%s - Forge Time: %s Seconds.", opad, miss, missile2, missile2b);
						label.setHighlight(missile2, missile2b);
						label.setHighlightColors(miss, h);
					} else {
						label = tooltip.addPara("%s - %s", opad, miss, missile2, missile2b);
						label.setHighlight(missile2, missile2b);
						label.setHighlightColors(miss, bad);
					}
					}
					if (hasMissile3) {
						if (missile3Valid) {
						label = tooltip.addPara("%s - Forge Time: %s Seconds.", opad, miss, missile3, missile3b);
						label.setHighlight(missile3, missile3b);
						label.setHighlightColors(miss, h);
					} else {
						label = tooltip.addPara("%s - %s", opad, miss, missile3, missile3b);
						label.setHighlight(missile3, missile3b);
						label.setHighlightColors(miss, bad);
					}
					}
					if (hasMissile4) {
						if (missile4Valid) {
						label = tooltip.addPara("%s - Forge Time: %s Seconds.", opad, miss, missile4, missile4b);
						label.setHighlight(missile4, missile4b);
						label.setHighlightColors(miss, h);
					} else {
						label = tooltip.addPara("%s - %s", opad, miss, missile4, missile4b);
						label.setHighlight(missile4, missile4b);
						label.setHighlightColors(miss, bad);
					}
					}
				}
				
			} else {
				label = tooltip.addPara("No weapons installed in the %s mounts.", opad, uni, "Universal");
				label.setHighlight("Universal");
				label.setHighlightColors(uni);
			}
			
			
		}
		
		/*
		Ballistic : Each ballistic weapon installed in one of the universal mounts increases system sub-arc count by one, and gives the sub-arcs a chance to pierce shields that scales with the targets hardflux level.
		Energy : Energy weapon flux cost reduced by 4% for each energy weapon installed in one of the universal mounts.
		Missile : Features a microforge that forges ammo for any non-reloading missile weapons installed in one of the universal mounts at a rate that gives each launcher an equivalent of 40 DPS. Reload rate is halved when phased and progress will pause if the ship is overloaded or venting.
		*/
		
	}

}
