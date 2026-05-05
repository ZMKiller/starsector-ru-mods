package data.weapons.scripts.beam;

import data.hullmods.WeaponMod.HSIProtocalMeltDown;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

public class HWIIonLanceEffect extends HWIFocusBeamEffect{
    private IntervalUtil fireInterval = new IntervalUtil(0.2f, 0.3f);
    private IntervalUtil arcIntervalUtil = new IntervalUtil(0.2f, 0.25f);
	private boolean wasZero = true;

	private float mult = 1f;

	private boolean once = true;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		CombatEntityAPI target = beam.getDamageTarget();
		if(beam.getSource()!=null&& HSIProtocalMeltDown.hasProtocalMeltDown(beam.getSource())&&once){
			mult = HSIProtocalMeltDown.getMult();
			once = false;
		}
		if (target instanceof ShipAPI && beam.getBrightness() >= 0.8f) {
			float dur = beam.getDamage().getDpsDuration();
			// needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
			if (!wasZero) dur = 0;
			wasZero = beam.getDamage().getDpsDuration() <= 0;
			fireInterval.advance(dur*mult);
			if (fireInterval.intervalElapsed()) {
				ShipAPI ship = (ShipAPI) target;
				boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
				float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
				
				boolean piercedShield = hitShield && (float) Math.random() < pierceChance;
				//piercedShield = true;
				
				if (!hitShield || piercedShield) {
					Vector2f point = beam.getRayEndPrevFrame();
					float emp = beam.getDamage().getFluxComponent() * 0.5f;
					float dam = beam.getDamage().getDamage() * 0.25f;
					engine.spawnEmpArc(
									   beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
									   DamageType.ENERGY, 
									   dam, // damage
									   emp, // emp 
									   100000f, // max range 
									   "tachyon_lance_emp_impact",
									   beam.getWidth() + 5f,
									   beam.getFringeColor(),
									   beam.getCoreColor()
									   );
				}
			}
		}
        if(beam.getBrightness()>0.5f){
            arcIntervalUtil.advance(amount);
            if(arcIntervalUtil.intervalElapsed())
            createVisualArc(beam,beam.getWidth());
        }
	}
}
