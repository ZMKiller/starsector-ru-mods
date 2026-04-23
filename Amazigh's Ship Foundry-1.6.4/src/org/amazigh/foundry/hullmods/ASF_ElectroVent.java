package org.amazigh.foundry.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_ElectroVent extends BaseHullMod {

	public static final float OVERLOAD_BONUS = 50f;
	
	private static Map<HullSize, Float> arcCount = new HashMap<HullSize, Float>();
	static {
		arcCount.put(HullSize.FIGHTER, 2f);
		arcCount.put(HullSize.FRIGATE, 3f);
		arcCount.put(HullSize.DESTROYER, 5f);
		arcCount.put(HullSize.CRUISER, 7f);
		arcCount.put(HullSize.CAPITAL_SHIP, 9f);
		arcCount.put(HullSize.DEFAULT, 5f);
	}
	
	public static Map<HullSize, Float> magBonus = new HashMap<HullSize, Float>();
	static {
		magBonus.put(HullSize.FRIGATE, 250f);
		magBonus.put(HullSize.DESTROYER, 500f);
		magBonus.put(HullSize.CRUISER, 750f);
		magBonus.put(HullSize.CAPITAL_SHIP, 1200f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getOverloadTimeMod().modifyMult(id, 1f - (OVERLOAD_BONUS * 0.01f));
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			float cap = (Float) magBonus.get(hullSize);
			stats.getFluxCapacity().modifyFlat(id, cap);
		}
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("ELECTRO_VENT_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        CombatEngineAPI engine = Global.getCombatEngine();
        
		if (ship.getFluxTracker().isOverloaded()) {
			
			float sizeScalar = 1f + (arcCount.get(ship.getHullSize()) / 10f);
			
			info.TIMER1 += (amount * sizeScalar);
			if (info.TIMER1 > 0.75f) {
            	info.TIMER1 -= MathUtils.getRandomNumberInRange(0.75f, 0.5f);
            	
            	for (int i=0; i < 3; i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 80f));
    				Global.getCombatEngine().addSmoothParticle(sparkPoint,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						0.6f, //brightness
    						0.7f, //duration
    						new Color(150,70,135,255));
            	}

				float distanceRandom1 = MathUtils.getRandomNumberInRange(ship.getCollisionRadius() * 0.4f, ship.getCollisionRadius() * 0.75f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = MathUtils.getRandomNumberInRange(ship.getCollisionRadius() * 0.25f, ship.getCollisionRadius() * 0.5f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(-40, 40);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(arcPoint1, distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 10f,
	        			new Color(130,70,155,50),
						new Color(255,225,255,55));
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
						Math.max(80f, ship.getCollisionRadius() / 2f),
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.4f,
						1.0f,
						new Color(140,70,130,70),
						false);
            }
            
			
			info.TIMER2 += (amount * sizeScalar);
			if (info.TIMER2 > 0.75f) {
            	info.TIMER2 -= MathUtils.getRandomNumberInRange(0.75f, 0.5f);
            	
            	for (int i=0; i < 3; i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 80f));
    				Global.getCombatEngine().addSmoothParticle(sparkPoint,
    						sparkVel,
        					MathUtils.getRandomNumberInRange(4f, 9f), //size
        					0.6f, //brightness
        					0.7f, //duration
        					new Color(150,70,135,255));
            	}

            	float distanceRandom1 = MathUtils.getRandomNumberInRange(ship.getCollisionRadius() * 0.4f, ship.getCollisionRadius() * 0.75f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = MathUtils.getRandomNumberInRange(ship.getCollisionRadius() * 0.25f, ship.getCollisionRadius() * 0.5f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(-40, 40);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(arcPoint1, distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 10f,
	        			new Color(130,70,155,50),
						new Color(255,225,255,55));
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
		        		Math.max(80f, ship.getCollisionRadius() / 2f),
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.4f,
						1.0f,
						new Color(140,70,130,70),
						false);
            }
			
			if (!info.OVERLOAD) {
				for (int i=0; i < arcCount.get(ship.getHullSize()); i++) {
					engine.spawnEmpArcPierceShields(ship,
							MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius()),
		        			ship,
		        			ship,
		        			DamageType.ENERGY,
		        			0f,
		        			200f * ship.getFluxLevel(),
		        			1000f,
		        			"tachyon_lance_emp_impact",
		        			10f,
		        			new Color(130,70,155,50),
							new Color(255,225,255,55));
			        
					float angle = MathUtils.getRandomNumberInRange(0f, 360f);
					float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
					
			        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
			        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
			        		Math.max(80f, ship.getCollisionRadius() / 2f),
							MathUtils.getRandomNumberInRange(1.6f, 2.0f),
							0.8f,
							0.4f,
							1.05f,
							new Color(140,70,130,70),
							false);
					
					for (int j=0; j < 16; j++) {
			        	
			        	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
						Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 90f));
						Global.getCombatEngine().addSmoothParticle(sparkPoint,
								sparkVel,
								MathUtils.getRandomNumberInRange(4f, 9f), //size
								0.6f, //brightness
								0.75f, //duration
								new Color(150,70,135,255));
			        }
					
				}
				
				// vent 15% of current flux!
				ship.getFluxTracker().setHardFlux(Math.max(0f, ship.getFluxTracker().getHardFlux() * 0.85f));
				ship.getFluxTracker().setCurrFlux(Math.max(0f, ship.getFluxTracker().getCurrFlux() * 0.85f));
			}
			
			info.OVERLOAD = true;
		} else {
			info.OVERLOAD = false;
		}
		
        engine.getCustomData().put("ELECTRO_VENT_DATA_KEY" + ship.getId(), info);
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
		
		LabelAPI label = tooltip.addPara("On overload this system automatically diverts a portion of the ships flux into stabilising systems, this allows for a more rapid normalisation of systems, but the resultant electomagnetic discharge that is generated can often disable weapons and engines.", pad);

		label = tooltip.addPara("Overload duration reduced by %s.", opad, h, "" + (int)OVERLOAD_BONUS + "%");
		label.setHighlight("" + (int)OVERLOAD_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Ship will instantly dissipate %s of current flux on overload.", pad, h, "15%");
		label.setHighlight("15%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("When overloaded the ship will discharge %s/%s/%s/%s EMP arcs into itself with each dealing %s EMP damage.", opad, bad, "3", "5", "7", "9", "200");
		label.setHighlight("3", "5", "7", "9", "200");
		label.setHighlightColors(bad);
		
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) magBonus.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) magBonus.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) magBonus.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) magBonus.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}
	
    private class ShipSpecificData {
        private boolean OVERLOAD = false;
        private float TIMER1 = 0f;
        private float TIMER2 = 0f;
    }

}
