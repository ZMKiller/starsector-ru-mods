package org.amazigh.foundry.hullmods;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicRender;

public class ASF_PhantasmagoriaRegulator_on extends BaseHullMod {
	
	public static final float MAINT_MALUS = 75f;
	public static final float FLUX_BONUS = 0.1f;
	public static final float REGEN_BONUS = 10f;
	public static final float EMP_BONUS = 0.2f;
	public static final float PPT_BONUS = 60f;
	public static final float CR_BONUS = 15f;
	
	private static final float HULL_REPAIR_MULTIPLIER = 12.0f;
    private static final float SPARK_MAX_RADIUS = 5f;
    private static final float SPARK_BRIGHTNESS = 0.8f;
    private static final float SPARK_DURATION = 0.4f;
    private static final Color SPARK_COLOR = new Color(50, 240, 100);
    private final IntervalUtil repairSparkInterval = new IntervalUtil(0.033f, 0.033f);
    
	@Override
    public int getDisplaySortOrder() {
        return 2221;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 3;
    }
    
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, (1f - FLUX_BONUS));
		stats.getMissileAmmoRegenMult().modifyMult(id, 1f + (REGEN_BONUS * 0.01f));
		
		stats.getEmpDamageTakenMult().modifyMult(id, (1f - EMP_BONUS));
		
		stats.getPeakCRDuration().modifyFlat(id, PPT_BONUS);
		stats.getCRPerDeploymentPercent().modifyFlat(id, - CR_BONUS);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		
		if (!ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        CombatEngineAPI engine = Global.getCombatEngine();
        
        boolean repair = false;
        
        // start setting up the glow stuff
    	int red = 85;
        int green = 185;
        int blue = 255;
        // base		- 85,185,255
    	// repair	- 60,255,120
        
        int alpha = 225; //155
        double timeMult = (double) ship.getMutableStats().getTimeMult().modified;
        alpha = (int) Math.ceil(225 / timeMult);
        alpha = Math.min(alpha, 255);
        
        
        if (ship.getPhaseCloak().isActive()) {
        	
        	
            float regenMult = (1f + ship.getPhaseCloak().getEffectLevel()) * amount * (1.5f - (ship.getFluxLevel() * 0.5f));
            // regeneration is up to 50% faster at lower flux levels
            // and goes up to double the rate at full effectLevel
            
        	if (DefenseUtils.hasArmorDamage(ship)) {
        		repair = true;
        		
	        	ArmorGridAPI armorGrid = ship.getArmorGrid();
		        final float[][] grid = armorGrid.getGrid();
		        final float max = armorGrid.getMaxArmorInCell();
		        
		        float baseCell = armorGrid.getMaxArmorInCell() * Math.min(ship.getHullSpec().getArmorRating(), 250f) / armorGrid.getArmorRating(); // clamping regen to max out 250 armour, so going sicko with stacking HA/etc doesn't give massive regen as well
		        float repairAmount = baseCell * 0.02f * regenMult;
		        // 2-3% armor repair per second, with the flux modifier
		        // make this 4-6% at max effectLevel
		        
				for (int x = 0; x < grid.length; x++) {
		            for (int y = 0; y < grid[0].length; y++) {
		                if (grid[x][y] < max) {
		                    float regen = grid[x][y] + repairAmount;
		                    armorGrid.setArmorValue(x, y, regen);
		                }
		            }
		        }
				
		        ship.syncWithArmorGridState();
        	}
        	
        	if (ship.getHitpoints() < ship.getMaxHitpoints()) {
        		repair = true;
        		ship.setHitpoints(Math.min(ship.getHitpoints() + (HULL_REPAIR_MULTIPLIER * regenMult), ship.getMaxHitpoints()));
        		// 24-36 hull repaired a second, with the flux modifier, and when at max effectLevel
        	}
        	
        	red = Math.max(0, 85 - (int) (ship.getPhaseCloak().getEffectLevel() * 25f));
            green = Math.min(255, 185 + (int) (ship.getPhaseCloak().getEffectLevel() * 70f));
            blue = Math.min(255, 255 - (int) (ship.getPhaseCloak().getEffectLevel() * 135f));
            // base		- 85,185,255
        	// repair	- 60,255,120
        	
        }
        
        // rendering the core glow
        SpriteAPI Glow = Global.getSettings().getSprite("fx", "A_S-F_phantasmagoria_glow");
        Vector2f glowSize = new Vector2f(60f, 34f);
        Vector2f glowLocInit = MathUtils.getPointOnCircumference(ship.getLocation(), -20f, ship.getFacing());
        
        Color glowColor = new Color(red,green,blue,alpha);
        
        MagicRender.singleframe(Glow, glowLocInit, glowSize, ship.getFacing() - 90f, glowColor, true);
        
        // if we're repairing, spawn some particles
        if (repair) {
    		repairSparkInterval.advance(amount);
	        if (repairSparkInterval.intervalElapsed()) {
	        	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f);
	        	engine.addHitParticle(sparkLoc, ship.getVelocity(), SPARK_MAX_RADIUS * (float) Math.random()
	        			+ SPARK_MAX_RADIUS, SPARK_BRIGHTNESS, SPARK_DURATION,
	        			SPARK_COLOR);
	        }
    	}
        
        //engine.getCustomData().put("PHANTASMAGORIA_REGULATOR_ON_DATA_KEY" + ship.getId(), info);
        
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
		
		float fluxDisplay = FLUX_BONUS * 100f;
		float empDisplay = EMP_BONUS * 100f;
		
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color grey = Misc.getGrayColor();
		final Color flavor = new Color(165,90,255,180);
		Color banner = new Color(64,21,77);
		
		LabelAPI label = tooltip.addPara("The Temporal Core currently has its regulator engaged, this results in reduced combat performance, in exchange for a more stable operation of systems.", opad);
		
		label = tooltip.addPara("The monthly maintenance supply cost is increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Energy weapon flux cost to fire reduced by %s.", pad, h, "" + Math.round(fluxDisplay) + "%");
		label.setHighlight("" + Math.round(fluxDisplay)+ "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Missile weapon ammo regeneration rate is increased by %s.", pad, h, "" + (int) REGEN_BONUS + "%");
		label.setHighlight("" + (int) REGEN_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("EMP damage taken is decreased by %s.", pad, h, "" + Math.round(empDisplay) + "%");
		label.setHighlight("" + Math.round(empDisplay)+ "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Peak operating time increased by %s.", pad, h, "" + (int) PPT_BONUS + "s");
		label.setHighlight("" + (int) PPT_BONUS + "s");
		label.setHighlightColors(h);
		label = tooltip.addPara("Combat readiness cost per deployment reduced by %s.", pad, h, "" + (int) CR_BONUS);
		label.setHighlight("" + (int) CR_BONUS);
		label.setHighlightColors(h);
		
		tooltip.addSectionHeading("Dimensional Reconstructor", h, banner, Alignment.MID, opad);
		label = tooltip.addPara("This ship features an automated hull and armour repair system that distributes repair nanites.", opad);
		label = tooltip.addPara("When %s the reconstructor will activate.", pad, h, "Cycling the Temporal Core");
		label.setHighlight("Cycling the Temporal Core");
		label.setHighlightColors(h);
		label = tooltip.addPara("The reconstructor repairs up to %s faster at lower flux levels.", pad, h, "50%");
		label.setHighlight("50%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("%s", opad, grey, "Remove hullmod to disable core regulator.");
		label.setHighlight("Remove hullmod to disable core regulator.");
		label.setHighlightColors(grey);
        tooltip.addPara("%s", 6f, flavor, new String[] { "\"Come to terms with death, thereafter anything is possible.\"" });
	}
	
}
