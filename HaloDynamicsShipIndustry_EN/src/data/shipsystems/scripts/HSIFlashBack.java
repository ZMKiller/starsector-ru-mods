package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSIFlashBack extends BaseShipSystemScript {
    private ShipAPI ship;
    // private boolean once = true;
    private CombatEngineAPI engine = Global.getCombatEngine();
    protected List<HSIFlashBackSnapShot> snapshots = new ArrayList<>();
    protected float elaspsed = 0f;
    protected HSIFlashBackSnapShot bestShot = null;
    public static final Color JITTER_COLOR = new Color(255, 165, 255, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(255, 165, 255, 155);

    public class HSIFlashBackSnapShot {
        protected float softFlux;
        protected float hardFlux;
        protected float hitpoints;
        protected float[][] aromr;
        protected float shield;
        protected Map<WeaponAPI, Integer> ammos = new HashMap<>();
        protected float armorAvg;
        protected float amount;
        protected float facing;
        protected Vector2f loc;
        protected Vector2f vel;

        public HSIFlashBackSnapShot(ShipAPI ship, float amount) {
            this.amount = amount;
            hardFlux = ship.getFluxTracker().getHardFlux();
            softFlux = ship.getFluxTracker().getCurrFlux() - hardFlux;
            hitpoints = ship.getHitpoints();
            facing = ship.getFacing();
            loc = new Vector2f(ship.getLocation());
            vel = new Vector2f(ship.getVelocity());
            if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
                shield = (ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0).getShield().getCurrent());
            }
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.usesAmmo())
                    ammos.put(weapon, weapon.getAmmo());
            }
            aromr = ship.getArmorGrid().getGrid();
            armorAvg = ship.getAverageArmorInSlice(0, 360);
        }
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        float jitterLevel = (float) (Math.pow(effectLevel, 2));
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 2, 0, 0 + jitterLevel * 3f);
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 12, 0f, 7f + jitterLevel * 3f);
        if (engine.isPaused())
            return;
        if (effectLevel <= 0) {
            elaspsed += engine.getElapsedInLastFrame();
            if (elaspsed > 0.1f) {
                snapshots.add(new HSIFlashBackSnapShot(ship, elaspsed));
                elaspsed = 0f;
                float total = 0;
                do {
                    if (total != 0) {
                        snapshots.remove(0);
                        total = 0;
                    }
                    for (int i = 0; i < snapshots.size(); i++) {
                        if (i == 0)
                            continue;
                        total += snapshots.get(i).amount;
                    }
                } while (total > 4f);
            }
        } else {
            if (effectLevel < 1) {
                float slice = 1f / snapshots.size();
                int index = snapshots.size() - (int) (effectLevel / slice);
                if (index < 0)
                    index = 0;
                if (index > snapshots.size() - 1)
                    index = snapshots.size() - 1;
                HSIFlashBackSnapShot now = snapshots.get(index);
                flashback(ship, now);
            } else {
                HSIFlashBackSnapShot now = snapshots.get(0);
                flashback(ship, now);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (bestShot == null) {
            bestShot = new HSIFlashBackSnapShot(ship, elaspsed);
            elaspsed = 0;
        } else {
            HSIFlashBackSnapShot now = new HSIFlashBackSnapShot(ship, elaspsed);
            if ((now.armorAvg >= bestShot.armorAvg && now.hitpoints >= bestShot.hitpoints)
                    && (now.hardFlux <= bestShot.hardFlux || now.softFlux <= bestShot.softFlux)) {
                bestShot = now;
                elaspsed = 0;
            }
        }
    }

    public void flashback(ShipAPI ship, HSIFlashBackSnapShot snapShot) {
        ship.getVelocity().set(snapShot.vel);
        ship.getLocation().set(snapShot.loc);
        ship.setFacing(snapShot.facing);
        ship.setHitpoints(snapShot.hitpoints);
        ship.getFluxTracker().setHardFlux(snapShot.hardFlux);
        ship.getFluxTracker().setCurrFlux(snapShot.softFlux);
        ArmorGridAPI aromr = ship.getArmorGrid();
        int y = 0;
        for (float[] row : aromr.getGrid()) {
            int x = 0;
            for (float col : row) {
                aromr.setArmorValue(x, y, snapShot.aromr[x][y]);
                x++;
            }
            y++;
        }
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(snapShot.ammos.containsKey(weapon)){
                weapon.setAmmo(snapShot.ammos.get(weapon));
            }
        }
    }
}
