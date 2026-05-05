package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HSICoordManuvers extends BaseHullMod {
    protected static final float RETURN_BUFF = 35f;
    protected static final float NORMAL_BUFF = 1.5f;
    /*
     * public class HSICatapultScript implements AdvanceableListener {
     * protected ShipAPI ship;
     * private List<ShipAPI> inHangarFighters = new ArrayList<>();
     * private List<ShipAPI> sendOutFighters = new ArrayList<>();
     * private TimeoutTracker<ShipAPI> telBackFighters = new
     * TimeoutTracker<ShipAPI>();
     * private IntervalUtil sendOutTimer = new IntervalUtil(0.2f, 0.2f);
     * // public static final String CATAPULT_KEY = "HSICatapult_KEY";
     * public final Vector2f LAUNCH_POINT = new Vector2f(0, 20);
     * public final Color JITTER_TEL = new Color(255, 15, 255, 75);
     * private TimeoutTracker<ShipAPI> catapultTracker = new
     * TimeoutTracker<ShipAPI>();
     * private WeaponAPI deco1;
     * private WeaponAPI deco2;
     * 
     * public HSICatapultScript(ShipAPI ship) {
     * this.ship = ship;
     * for (WeaponAPI weapon : ship.getAllWeapons()) {
     * if (weapon.isDecorative()) {
     * if (weapon.getSlot().getId().equals("DECO1"))
     * deco1 = weapon;
     * if (weapon.getSlot().getId().equals("DECO2"))
     * deco2 = weapon;
     * }
     * }
     * }
     * 
     * public void advance(float amount) {
     * if (ship.isPullBackFighters()) {
     * for (FighterWingAPI wing : ship.getAllWings()) {
     * for (ShipAPI fighter : wing.getWingMembers()) {
     * if (fighter.isLiftingOff() && !(wing.getRole() == WingRole.SUPPORT))
     * inHangarFighters.add(fighter);
     * 
     * if (!inHangarFighters.contains(fighter) && !telBackFighters.contains(fighter)
     * && Misc.getDistance(fighter.getLocation(),
     * ship.getLocation()) < ship.getCollisionRadius() + 400f
     * && wing.getRole() != WingRole.SUPPORT) {
     * telBackFighters.add(fighter, 0.4f);
     * }
     * }
     * }
     * } else{
     * for (FighterWingAPI wing : ship.getAllWings()) {
     * for (ShipAPI fighter : wing.getWingMembers()) {
     * if (fighter.isLiftingOff()&&!(wing.getRole() == WingRole.SUPPORT))
     * inHangarFighters.add(fighter);
     * if (wing.isReturning(fighter) && !inHangarFighters.contains(fighter)
     * && Misc.getDistance(fighter.getLocation(),
     * ship.getLocation()) < ship.getCollisionRadius() + 400f) {
     * telBackFighters.add(fighter, 0.4f);
     * }
     * }
     * }
     * }
     * 
     * relocateInHangar();
     * 
     * telBack();
     * 
     * sendOut(amount);
     * }
     * 
     * private void relocateInHangar() {
     * if (inHangarFighters.isEmpty()) {
     * return;
     * }
     * for (int i = 0; i < inHangarFighters.size(); i++) {
     * Vector2f exLoc = new Vector2f(0, 0);
     * if (i % 2 == 0) {
     * float y = -((int) (i / 2) * 32f + 25f);
     * float x = ((int) (i / 2) * 32f);
     * exLoc = new Vector2f(x, y);
     * } else {
     * float y = ((int) (i / 2) * 32f + 25f);
     * float x = ((int) (i / 2) * 32f);
     * exLoc = new Vector2f(x, y);
     * }
     * inHangarFighters.get(i).setLayer(CombatEngineLayers.BELOW_SHIPS_LAYER);
     * VectorUtils.rotateAroundPivot(exLoc, new Vector2f(0, 0), ship.getFacing(),
     * exLoc);
     * exLoc = Vector2f.add(exLoc, ship.getLocation(), exLoc);
     * inHangarFighters.get(i).getLocation().set(exLoc);
     * }
     * }
     * 
     * private void telBack() {
     * if (telBackFighters.getItems().isEmpty()) {
     * return;
     * }
     * for (ShipAPI f : telBackFighters.getItems()) {
     * float level = telBackFighters.getRemaining(f) / 0.4f;
     * f.setCircularJitter(true);
     * f.setJitterUnder(ship, JITTER_TEL, level, (int) level * 10, 5);
     * if (level >= 0.95 && !inHangarFighters.contains(f)) {
     * inHangarFighters.add(f);
     * }
     * }
     * }
     * 
     * private void sendOut(float amount) {
     * if (!catapultTracker.getItems().isEmpty()) {
     * catapultTracker.advance(amount);
     * for (ShipAPI f : catapultTracker.getItems()) {
     * if (catapultTracker.getRemaining(f) < 0.05f) {
     * f.setLayer(CombatEngineLayers.FIGHTERS_LAYER);
     * }
     * }
     * }
     * if (sendOutFighters.isEmpty())
     * return;
     * Vector2f exLoc = new Vector2f(LAUNCH_POINT);
     * sendOutTimer.advance(amount);
     * ShipAPI sendOutFighter = sendOutFighters.get(0);
     * sendOutFighter.getLocation()
     * .set(VectorUtils.rotateAroundPivot(LAUNCH_POINT, ship.getLocation(),
     * ship.getFacing(), exLoc));
     * if (sendOutTimer.intervalElapsed()) {
     * Vector2f vel = Misc.getUnitVectorAtDegreeAngle(ship.getFacing());
     * vel.scale(500f);
     * sendOutFighter.getVelocity().set(vel);
     * catapultTracker.add(sendOutFighter, 0.4f);
     * deco1.setForceFireOneFrame(true);
     * deco2.setForceFireOneFrame(true);
     * sendOutFighters.remove(sendOutFighter);
     * sendOutFighter.turnOnTravelDrive(0.7f);
     * }
     * }
     * 
     * }
     * 
     * public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
     * ship.addListener(new HSICatapultScript(ship));
     * }
     */

    /*
     * @Override
     * public String getDescriptionParam(int index, HullSize hullSize) {
     * if (index == 0)
     * return "" + 400;
     * return null;
     * }
     */

    public void advanceInCombat(ShipAPI ship, float amount) {
        for (FighterWingAPI wing : ship.getAllWings()) {
            for (ShipAPI fighter : wing.getWingMembers()) {
                if (wing.isReturning(fighter) || ship.isPullBackFighters()) {
                    fighter.getMutableStats().getMaxSpeed().modifyPercent("HSICoordManuver", RETURN_BUFF);
                } else {
                    fighter.getMutableStats().getMaxSpeed().unmodify("HSICoordManuver");
                }
                if(fighter.getLayer()==CombatEngineLayers.BELOW_SHIPS_LAYER&&!fighter.getTravelDrive().isActive()){
                    fighter.setLayer(CombatEngineLayers.FIGHTERS_LAYER);
                }
            }
        }
    }

    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        if (!ship.isPullBackFighters()) {
            if (fighter.getWing() != null && fighter.getWing().getRole() == WingRole.SUPPORT) {
                fighter.turnOnTravelDrive(0.5f);
            } else if (fighter.getWing() != null && fighter.getWing().getRole() == WingRole.BOMBER) {
                fighter.turnOnTravelDrive(1.8f);
            } else {
                fighter.turnOnTravelDrive(1f);
            }
        }
        int wingsize = ship.getAllWings().size();
        fighter.getMutableStats().getMaxSpeed().modifyPercent(id, NORMAL_BUFF * wingsize);
        fighter.getMutableStats().getAcceleration().modifyPercent(id, NORMAL_BUFF * wingsize);
        fighter.getMutableStats().getDeceleration().modifyPercent(id, NORMAL_BUFF * wingsize);
        fighter.getMutableStats().getTurnAcceleration().modifyPercent(id, NORMAL_BUFF * wingsize);
        fighter.getMutableStats().getMaxTurnRate().modifyPercent(id, NORMAL_BUFF * wingsize);
        fighter.setLayer(CombatEngineLayers.BELOW_SHIPS_LAYER);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + RETURN_BUFF + "%";
        if (index == 1)
            return "" + NORMAL_BUFF + "%";
        return null;
    }
}
