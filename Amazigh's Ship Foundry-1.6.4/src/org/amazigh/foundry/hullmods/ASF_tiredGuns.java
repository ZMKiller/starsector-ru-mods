package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_tiredGuns extends BaseHullMod {
	
	public static final float RATE_MALUS = 10f;
	public static final float FLUX_MALUS = 5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		int ballCount = 0;
		int missCount = 0;
		int otherCount = 0;
		
		if (stats.getVariant().getWeaponSpec("WS0001") != null) {
			if (stats.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.MISSILE) {
				missCount++;
			} else if (stats.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.BALLISTIC) {
				ballCount++;
			} else {
				if (stats.getVariant().getWeaponSpec("WS0001").getSize() == WeaponAPI.WeaponSize.LARGE) {
					otherCount++;
				}
			}
		}
		
		if (stats.getVariant().getWeaponSpec("WS0002") != null) {
			if (stats.getVariant().getWeaponSpec("WS0002").getType() == WeaponAPI.WeaponType.MISSILE) {
				missCount++;
			} else if (stats.getVariant().getWeaponSpec("WS0002").getType() == WeaponAPI.WeaponType.BALLISTIC) {
				ballCount++;
			} else {
				if (stats.getVariant().getWeaponSpec("WS0002").getSize() == WeaponAPI.WeaponSize.LARGE) {
					otherCount++;
				}
			}
		}
		
		if (stats.getVariant().getWeaponSpec("WS0003") != null) {
			if (stats.getVariant().getWeaponSpec("WS0003").getType() == WeaponAPI.WeaponType.MISSILE) {
				missCount++;
			} else if (stats.getVariant().getWeaponSpec("WS0003").getType() == WeaponAPI.WeaponType.BALLISTIC) {
				ballCount++;
			} else {
				if (stats.getVariant().getWeaponSpec("WS0003").getSize() == WeaponAPI.WeaponSize.LARGE) {
					otherCount++;
				}
			}
		}
		
		// based on the count of Ball/Miss weps you get -10/-20/-30 % RoF/Regen for that weapon type 
		// for having 1/2/3 you get 0.9/1.6/2.1 "effective" weapons in terms of RoF (impact is arguably less severe on missiles as it doesn't touch their ammo)
		// +0.9 / +0.7 / +0.5   value of "effective weapon gain" per weapon installed
		
		if (ballCount > 0) {
			stats.getBallisticRoFMult().modifyMult(id, 1f - ((ballCount * RATE_MALUS) * 0.01f));
			stats.getBallisticAmmoRegenMult().modifyMult(id, 1f - ((ballCount * RATE_MALUS) * 0.01f));
		}
		
		if (missCount > 0) {
			stats.getMissileRoFMult().modifyMult(id, 1f - ((missCount * RATE_MALUS) * 0.01f));
			stats.getMissileAmmoRegenMult().modifyMult(id, 1f - ((missCount * RATE_MALUS) * 0.01f));
			stats.getMissileAmmoBonus().modifyMult(id, 1f - ((missCount * RATE_MALUS) * 0.01f)); // missile weps also get -ammo at the same rate as the RoF penalty
			
		}
		
		// and a 5% flux malus if you manage to find some "other" typed weapon to slot in there! (but only if it's a large!)
		
		if (otherCount >0) {
			stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f + (otherCount * FLUX_MALUS * 0.01f));
			stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f + (otherCount * FLUX_MALUS * 0.01f));
			stats.getMissileWeaponFluxCostMod().modifyMult(id, 1f + (otherCount * FLUX_MALUS * 0.01f));
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
		Color other = Misc.MOUNT_UNIVERSAL;
		
		int ballCount = 0;
		int missCount = 0;
		int otherCount =0;
		
		LabelAPI label = tooltip.addPara("The munition loaders installed on this ship struggle to support any installed Large weaponry, and with the more of each type of weapon installed offer reduced performance.", pad);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			tooltip.addSectionHeading("Currently installed weapons", h, banner, Alignment.MID, opad);
			
			if (ship.getVariant().getWeaponSpec("WS0001") != null) {
				if (ship.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.MISSILE) {
					label = tooltip.addPara("Hardpoint: %s.", opad, miss, "Missile");
					label.setHighlight("Missile");
					missCount++;
				} else if (ship.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					label = tooltip.addPara("Hardpoint: %s.", opad, ball, "Ballistic");
					label.setHighlight("Ballistic");
					ballCount++;
				} else {
					if (ship.getVariant().getWeaponSpec("WS0001").getSize() == WeaponAPI.WeaponSize.MEDIUM) {
						label = tooltip.addPara("Hardpoint: %s.", pad, grey, "Medium Weapon");
						label.setHighlight("Medium Weapon");
					} else {
						label = tooltip.addPara("Hardpoint: %s.", opad, other, "Other");
						label.setHighlight("Other");
						otherCount++;
					}
					
				}
			} else {
				label = tooltip.addPara("Hardpoint: %s.", opad, grey, "None");
				label.setHighlight("None");
			}
			if (ship.getVariant().getWeaponSpec("WS0002") != null) {
				if (ship.getVariant().getWeaponSpec("WS0002").getType() == WeaponAPI.WeaponType.MISSILE) {
					label = tooltip.addPara("Fore Turret: %s.", pad, miss, "Missile");
					label.setHighlight("Missile");
					missCount++;
				} else if (ship.getVariant().getWeaponSpec("WS0002").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					label = tooltip.addPara("Fore Turret: %s.", pad, ball, "Ballistic");
					label.setHighlight("Ballistic");
					ballCount++;
				} else {
					if (ship.getVariant().getWeaponSpec("WS0002").getSize() == WeaponAPI.WeaponSize.MEDIUM) {
						label = tooltip.addPara("Fore Turret: %s.", pad, grey, "Medium Weapon");
						label.setHighlight("Medium");
					} else {
						label = tooltip.addPara("Fore Turret: %s.", pad, other, "Other");
						label.setHighlight("Other");
						otherCount++;
					}
				}
			} else {
				label = tooltip.addPara("Fore Turret: %s.", pad, grey, "None");
				label.setHighlight("None");
			}
			if (ship.getVariant().getWeaponSpec("WS0003") != null) {
				if (ship.getVariant().getWeaponSpec("WS0003").getType() == WeaponAPI.WeaponType.MISSILE) {
					label = tooltip.addPara("Aft Turret: %s.", pad, miss, "Missile");
					label.setHighlight("Missile");
					missCount++;
				} else if (ship.getVariant().getWeaponSpec("WS0003").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					label = tooltip.addPara("Aft Turret: %s.", pad, ball, "Ballistic");
					label.setHighlight("Ballistic");
					ballCount++;
				} else {
					if (ship.getVariant().getWeaponSpec("WS0003").getSize() == WeaponAPI.WeaponSize.MEDIUM) {
						label = tooltip.addPara("Aft Turret: %s.", pad, grey, "Medium Weapon");
						label.setHighlight("Medium");
					} else {
						label = tooltip.addPara("Fore Turret: %s.", pad, other, "Other");
						label.setHighlight("Other");
						otherCount++;
					}
				}
			} else {
				label = tooltip.addPara("Aft Turret: %s.", pad, grey, "None");
				label.setHighlight("None");
			}
			
			tooltip.addSectionHeading("Current penalties", h, banner, Alignment.MID, opad);
			
			if (ballCount > 0) {
				label = tooltip.addPara("Ballistic weapon Rate of Fire and Ammo Regeneration reduced by %s.", opad, bad, "" + (int)(ballCount * RATE_MALUS) + "%");
				label.setHighlight("" + (int)(ballCount * RATE_MALUS) + "%");
				label.setHighlightColors(bad);
			}
			
			if (missCount > 0) {
				label = tooltip.addPara("Missile weapon Rate of Fire and Ammo Regeneration reduced by %s.", opad, bad, "" + (int)(missCount * RATE_MALUS) + "%");
				label.setHighlight("" + (int)(missCount * RATE_MALUS) + "%");
				label.setHighlightColors(bad);
				label = tooltip.addPara("Missile weapon Ammo Capacity reduced by %s.", opad, bad, "" + (int)(missCount * RATE_MALUS) + "%");
				label.setHighlight("" + (int)(missCount * RATE_MALUS) + "%");
				label.setHighlightColors(bad);
			}
			
			if (otherCount >0) {
				label = tooltip.addPara("Weapon flux cost to fire increased by %s.", opad, bad, "" + (int)(otherCount * FLUX_MALUS) + "%");
				label.setHighlight("" + (int)(otherCount * FLUX_MALUS) + "%");
				label.setHighlightColors(bad);
			}
			
			if (ballCount == 0 && missCount == 0 && missCount == 0) {
				label = tooltip.addPara("No large weapons installed, no penalties.", opad);
			}
		}
		
	}

}
