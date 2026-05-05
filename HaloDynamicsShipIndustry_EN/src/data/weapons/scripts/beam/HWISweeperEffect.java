package data.weapons.scripts.beam;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class HWISweeperEffect implements BeamEffectPlugin {
    private int cost = 0;

    protected static final float DAMAGE_PER_JUDGE = 250f;
    private IntervalUtil ArcInterval = new IntervalUtil(0.25f,0.25f);
    {
        ArcInterval.setElapsed(0.25f);
    }
    float elapsed = 0f;
    float maxTime = 1.5f;
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        WeaponAPI weapon = beam.getWeapon();
        elapsed+=amount;

       // Global.getLogger(this.getClass()).info("BeamEffectWorking"+engine.getTotalElapsedTime(false));
        ((HWISweeperEveryframe)beam.getWeapon().getEffectPlugin()).updateBlock();
        if(ArcInterval.intervalElapsed()){
            ((HWISweeperEveryframe)beam.getWeapon().getEffectPlugin()).cost = ((HWISweeperEveryframe)beam.getWeapon().getEffectPlugin()).cost+1;
            cost = ((HWISweeperEveryframe)beam.getWeapon().getEffectPlugin()).cost;
            float baseAngle = beam.getWeapon().getCurrAngle();
            for(int i = 0;i<=3;i++){
                float angle = baseAngle+90f*i+(elapsed/maxTime)*90f;
                float size = cost*33f;
                Vector2f start = beam.getFrom();
                Vector2f end = new Vector2f(beam.getRayEndPrevFrame());
                Vector2f extra = (Vector2f) Misc.getUnitVectorAtDegreeAngle(angle).scale(size);
                Vector2f.add(end,extra,end);
                engine.spawnEmpArcVisual(start,beam.getSource(),end,null,8f+cost*1.5f,new Color(107, 148, 196,195),Color.white).setSingleFlickerMode();
            }
        }
        beam.getSource().setCustomData("HWISweeper_Data",new Vector2f(beam.getRayEndPrevFrame()));
        ArcInterval.advance(amount);
    }

}
