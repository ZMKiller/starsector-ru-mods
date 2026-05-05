package data.weapons.scripts.beam;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import data.hullmods.WeaponMod.HSIProtocalMeltDown;
import org.lwjgl.util.vector.Vector2f;

public class HWITachyonLanceEffect implements BeamEffectPlugin{

    private IntervalUtil fireInterval = new IntervalUtil(0.2f, 0.3f);
    private boolean wasZero = true;

    //private float elapsed = 0;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        //elapsed+=amount;
        //if(beam.getSource()!=null) beam.getSource().getMutableStats().getFluxDissipation().modifyMult("TEST_ONLY",0f);
        //Global.getLogger(this.getClass()).info(","+engine.getTotalElapsedTime(false)+","+elapsed+","+beam.getBrightness()+","+beam.getWeapon().getChargeLevel()+","+beam.getDamage().getDpsDuration()+","+beam.getDamage().getDamage()+","+beam.getDamage().getFluxComponent()+","+beam.getSource().getFluxTracker().getCurrFlux());
        CombatEntityAPI target = beam.getDamageTarget();
        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            float mult = 1f;
            if(beam.getSource()!=null&& HSIProtocalMeltDown.hasProtocalMeltDown(beam.getSource())){
                mult = 2f;
            }
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
                    engine.spawnEmpArcPierceShields(
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
//			Global.getSoundPlayer().playLoop("system_emp_emitter_loop",
//											 beam.getDamageTarget(), 1.5f, beam.getBrightness() * 0.5f,
//											 beam.getTo(), new Vector2f());
    }
}
