package data.scripts.weapons;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.util.Misc;

/**
 */
public class dpl_siphonEffect implements OnFireEffectPlugin {

	public static float ARC = 45f;
	public static float DECAY = 0.5f;
	public static int REAL_DAMAGE = 100;
	public static int HARD_FLUX = 100;
	private final Random rand = new Random();
	
    private static final float SPARK_DURATION = 0.5f;
    private static final float SPARK_MAX_RADIUS = 5f;
    private static final float SPARK_BRIGHTNESS = 0.9f;
    private static final Color SPARK_COLOR = new Color(125, 0, 200);
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
		CombatEntityAPI target = findTarget(projectile, weapon, engine);
		float thickness = 30f;
		float coreWidthMult = 0.67f;
		Color color = weapon.getSpec().getGlowColor();
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArc(projectile.getSource(), projectile.getLocation(), weapon.getShip(),
					   target,
					   DamageType.ENERGY, 
					   0f,
					   0f, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(125,15,200,200)
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			
			boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(arc.getTargetLocation());
			if (target instanceof ShipAPI && hitShield) {
				ShipAPI enemyShip = (ShipAPI) target;
				enemyShip.getFluxTracker().increaseFlux(dam, true);
			}
			
			if (target instanceof ShipAPI && !hitShield) {
				ShipAPI enemyShip = (ShipAPI) target;
				dealArmorDamage(projectile, enemyShip, arc.getTargetLocation(), dam*0.5f);
				enemyShip.setHitpoints(Math.max(1, enemyShip.getHitpoints() - dam));
				if (enemyShip.getHitpoints() <= 10 && !enemyShip.getVariant().hasHullMod("vastbulk")) {
					//Just kill it if its hull point is too low.
					engine.applyDamage(enemyShip, enemyShip.getLocation(), 1000000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null);
	            }
				
				ShipAPI ship = weapon.getShip();

		        ArmorGridAPI armorGrid = ship.getArmorGrid();
		        int x = rand.nextInt(armorGrid.getGrid().length);
		        int y = rand.nextInt(armorGrid.getGrid()[0].length);
		        float newArmor = armorGrid.getArmorValue(x, y);
		        float cellSize = armorGrid.getCellSize();

		        if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) < 0) {
		            newArmor += dam*0.4f;
		            armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));

		            Vector2f cellLoc = getCellLocation(ship, x, y);
		                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
		                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
		                engine.addHitParticle(cellLoc, ship.getVelocity(), SPARK_MAX_RADIUS * (float) Math.random()
		                        + SPARK_MAX_RADIUS, SPARK_BRIGHTNESS, SPARK_DURATION,
		                        SPARK_COLOR);
		            ship.setHitpoints(Math.min(ship.getHitpoints() + dam*0.8f, ship.getMaxHitpoints()));
		        }

		        ship.syncWithArmorGridState();
		        ship.syncWeaponDecalsWithArmorDamage();

		        if ((ship.getHitpoints() / ship.getMaxHitpoints()) >= 0.9f) {
		            float totalArmor = 0f;
		            float currArmor = 0f;
		            for (int x1 = armorGrid.getGrid().length - 1; x1 >= 0; x1--) {
		                for (int y1 = armorGrid.getGrid()[x1].length - 1; y1 >= 0; y1--) {
		                    totalArmor += armorGrid.getMaxArmorInCell();
		                    currArmor += armorGrid.getArmorValue(x1, y1);
		                }
		            }
		            if ((currArmor / totalArmor) >= 0.9f) {
		                ship.clearDamageDecals();
		            }
		        }
		    }
		} else {
			Vector2f from = new Vector2f(projectile.getLocation());
			Vector2f to = pickNoTargetDest(projectile, weapon, engine);
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, weapon.getShip(), to, weapon.getShip(), thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
		}
	}
	
	public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }
	
	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float spread = 15f;
		float range = weapon.getRange() - spread;
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, spread);
		return dir;
	}
	
	public CombatEntityAPI findTarget(CombatEntityAPI starting_ship, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange();
		Vector2f from = starting_ship.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		float minSizeScore = Float.MAX_VALUE;
		
		ShipAPI ship = weapon.getShip();
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof ShipAPI)) continue;
			ShipAPI other = (ShipAPI) o;
			if (other.getOwner() == owner) continue;
			if (other.isHulk()) continue;
			if (other.isPhased()) continue;
			if (other.getCollisionClass() == CollisionClass.NONE) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist > range) continue;
			
			//float angleTo = Misc.getAngleInDegrees(from, other.getLocation());
			//float score = Misc.getAngleDiff(weapon.getCurrAngle(), angleTo);
			float score = dist;
			float sizeScore = 1000f;
			if (other.isDrone()) {
				sizeScore = 100f;
			} else if (other.isFighter()) {
				sizeScore = 95f;
			} else if (other.isFrigate()) {
				sizeScore = 10f;
			} else if (other.isDestroyer()) {
				sizeScore = 5f;
			} else if (other.isCruiser()) {
				sizeScore = 2f;
			} else if (other.isCapital()) {
				sizeScore = 1f;
			}
			
			if (sizeScore < minSizeScore) {
				minSizeScore = sizeScore;
				minScore = score;
				best = other;
			} else if (sizeScore == minSizeScore) {
				if (score < minScore) {
					minSizeScore = sizeScore;
					minScore = score;
					best = other;
				}
			}
		}
		
		return best;
	}
	
	public static void dealArmorDamage(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point, float armorDamage) {
		CombatEngineAPI engine = Global.getCombatEngine();

		ArmorGridAPI grid = target.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return;
		
		int gridWidth = grid.getGrid().length;
		int gridHeight = grid.getGrid()[0].length;
		
		float damageTypeMult = DisintegratorEffect.getDamageTypeMult(projectile.getSource(), target);
		
		float damageDealt = 0f;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners
				
				int cx = cell[0] + i;
				int cy = cell[1] + j;
				
				if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
				
				float damMult = 1/30f;
				if (i == 0 && j == 0) {
					damMult = 1/15f;
				} else if (i <= 1 && i >= -1 && j <= 1 && j >= -1) { // S hits
					damMult = 1/15f;
				} else { // T hits
					damMult = 1/30f;
				}
				
				float armorInCell = grid.getArmorValue(cx, cy);
				float damage = armorDamage * damMult * damageTypeMult;
				damage = Math.min(damage, armorInCell);
				if (damage <= 0) continue;
				
				target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
				damageDealt += damage;
			}
		}
		
		if (damageDealt > 0) {
			if (Misc.shouldShowDamageFloaty(projectile.getSource(), target)) {
				engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, projectile.getSource());
			}
			target.syncWithArmorGridState();
		}
	}

}
