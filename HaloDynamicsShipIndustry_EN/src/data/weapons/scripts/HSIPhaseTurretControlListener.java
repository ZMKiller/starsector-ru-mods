package data.weapons.scripts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HSIPhaseTurretControlListener implements AdvanceableListener {
    private ShipAPI turret;
    private FaderUtil fader = new FaderUtil(0, 0.5f, 0.5f, false, false);
    private IntervalUtil maxFlightTime = new IntervalUtil(180f, 180f);
    private List<WeaponAPI> weapon = new ArrayList<WeaponAPI>();
    private float range = 0f;
    private int owner = 0;
    private float judgeRange = 0f;
    private boolean limAmmo = false;
    private PhaseTurretType type = PhaseTurretType.NO_PD;
    public static enum PhaseTurretType{
        PD,ONLY_PD,NO_PD,STRIKE;
    }

    public HSIPhaseTurretControlListener(ShipAPI turret, int owner,float maxTime) {
        this.turret = turret;
        weapon = turret.getAllWeapons();
        for (WeaponAPI w : weapon) {
            if (range < w.getRange()) {
                range = w.getRange();
                if(w.usesAmmo()&&w.getAmmoPerSecond()<=0){
                    limAmmo = true;
                }
                if(w.hasAIHint(AIHints.PD)||w.hasAIHint(AIHints.PD_ALSO)){
                    type = PhaseTurretType.PD;
                }
                if(w.hasAIHint(AIHints.STRIKE)){
                    type = PhaseTurretType.STRIKE;
                }
                if(w.hasAIHint(AIHints.PD_ONLY)){
                    type = PhaseTurretType.ONLY_PD;
                }
            }
        }
        judgeRange = range * 0.9f;
        this.owner = owner;
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused())
            return;
        maxFlightTime.advance(amount);
        if (maxFlightTime.intervalElapsed()) {
            turret.removeListener(this);
            Global.getCombatEngine().removeEntity(turret);
            return;
        }
        Iterator<Object> c = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(
                turret.getLocation(),
                range, range);
        boolean hasEnemyAround = false;
        while (c.hasNext()) {
            Object o = c.next();
            if ((o instanceof ShipAPI)) {
                ShipAPI s = (ShipAPI) o;
                if (s.getOwner() != owner && s.getOwner() != 100 && Misc.getDistance(s.getLocation(),
                        turret.getLocation()) < judgeRange + s.getCollisionRadius()) {
                    fader.fadeIn();
                    hasEnemyAround = true;
                    break;
                }
            }
        }
        if (!hasEnemyAround)
            fader.fadeOut();
        if (fader.getBrightness() < 0.2f) {
            turret.setPhased(true);
            turret.setOwner(100);
        } else {
            turret.setPhased(false);
            turret.setOwner(owner);
        }
        if (fader.getBrightness() < 1) {
            turret.getAllWeapons().get(0).disable();
        } else {
            turret.getAllWeapons().get(0).repair();
        }
        fader.advance(amount);
        turret.setAlphaMult(0.5f + 0.5f * fader.getBrightness());
        turret.getVelocity().set(0f, 0f);
    }
}
