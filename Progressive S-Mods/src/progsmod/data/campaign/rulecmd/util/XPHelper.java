package progsmod.data.campaign.rulecmd.util;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import progsmod.data.campaign.rulecmd.ui.LabelWithVariables;
import progsmod.util.SModUtils;

/**
 * Helper class for the XP label, also uses reserve XP.
 */
public class XPHelper {
    public int spentReserveXP = 0;

    public enum Affordable {
        YES,
        USE_RESERVE,
        NO
    }
    private LabelWithVariables<Integer> xpLabel;

    public void clear() {
        spentReserveXP = 0;
        xpLabel.changeVar(0, 0);
        xpLabel.changeVar(1, 0);
    }

    public void init(LabelWithVariables<Integer> xpLabel) {
        this.xpLabel = xpLabel;
    }

    private void addXP(int val) {
        xpLabel.changeVar(0, xpLabel.getVar(0) + val);
    }

    /** Use this instead of calling SModUtils.useReverseXP directly. Updates the spentReserveXP tracker. */
    public void useReserveXPIfNeeded(FleetMemberAPI ship) {
        if (spentReserveXP > 0) {
            SModUtils.useReserveXP(ship.getHullSpec().getBaseHullId(), ship, spentReserveXP);
            spentReserveXP = 0;
        }
    }

    public void addReserveXP(int val) {
        xpLabel.changeVar(1, xpLabel.getVar(1) + val);
    }

    public void reduceReserveXP(int val) {
        xpLabel.changeVar(1, xpLabel.getVar(1) - val);
    }

    public int getXP() {
        return xpLabel.getVar(0);
    }

    public int getReserveXP() {
        return xpLabel.getVar(1);
    }

    public Affordable canAfford(int cost) {
        if (getXP() >= cost) {
            return Affordable.YES;
        } else if ((getXP() + getReserveXP()) >= cost) {
            return Affordable.USE_RESERVE;
        } else {
            return Affordable.NO;
        }
    }

    /** Decreases the xpLabel by cost, spending reserve XP if needed. */
    public Affordable decreaseXPLabel(int cost) {
        if (getXP() >= cost) {
            addXP(-cost);
            return Affordable.YES;
        } else if ((getXP() + getReserveXP()) >= cost) {
            int reserveCost = cost - getXP();
            // Remember how much reserve XP was spent, for two reasons
            //  1. If a hullmod selection is undone, the (label) reserve XP needs to be increased first
            //  2. If "confirm" is pressed, the (real) reserve XP needs to be added to the ship with useReserveXP()
            spentReserveXP += reserveCost;
            addXP(-getXP());
            addReserveXP(-reserveCost);
            return Affordable.USE_RESERVE;
        } else {
            return Affordable.NO;
        }
    }

    /** Increases the xpLabel by cost, restoring reserve XP first if any was spent. */
    public void increaseXPLabel(int cost) {
        int reserveXPToBeRestored = Math.min(cost, spentReserveXP);
        spentReserveXP -= reserveXPToBeRestored;
        addReserveXP(reserveXPToBeRestored);
        int xpToBeRestored = cost - reserveXPToBeRestored;
        addXP(xpToBeRestored);
    }
}
