package data.character.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.FaderUtil;

public class HSIKnightSkillScript implements AdvanceableListener {
    private ShipAPI ship = null;
    private boolean isSP = false;
    private int mode = 0;// 0 = normal
    private FaderUtil berserkerTimer = new FaderUtil(0, 30f);
    private static final String KEY = "HSIKnightSkill";

    public HSIKnightSkillScript(ShipAPI ship, boolean isSP) {
        this.ship = ship;
        this.isSP = isSP;
    }

    @Override
    public void advance(float amount) {
        ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        if (playerShip != null) {
            if (mode == 0) {
                ship.setOwner(0);
                if (!playerShip.isAlive() && Global.getCombatEngine().getTotalElapsedTime(true) >= 2f) {
                    clearBuff(ship);
                    clearBuff(playerShip);
                    mode = 1;
                } else {
                    addBuff(ship);
                    addBuff(playerShip);
                }
            } else if (mode == 1) {
                //ship.setOwner(100);
                addBersekerBuff();
                berserkerTimer.fadeIn();
                berserkerTimer.advance(amount);
                if (berserkerTimer.isFadedIn()) {
                    mode = 2;
                }
            } else {
                ship.setOwner(0);
                //Global.getCombatEngine().getFleetManager(0).getTaskManager(false).orderRetreat(
                //        Global.getCombatEngine().getFleetManager(0).getDeployedFleetMember(ship), false, true);
                clearBuff(ship);
                clearBuff(playerShip);
            }
        }
        if (ship.getHullSpec().getTags().contains("luddic_church") || ship.getHullSpec().getTags().contains("LC_bp")) {
            ship.getMutableStats().getHullDamageTakenMult().modifyPercent(KEY + 2, 100f);
        }
    }

    public void addBuff(ShipAPI ship) {
        ship.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(KEY,
                isSP ? HSIKnightSkill.DAMAGE_SP : HSIKnightSkill.DAMAGE);
        ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(KEY,
                isSP ? HSIKnightSkill.DAMAGE_SP : HSIKnightSkill.DAMAGE);
        ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(KEY,
                isSP ? HSIKnightSkill.DAMAGE_SP : HSIKnightSkill.DAMAGE);
        ship.getMutableStats().getHullDamageTakenMult().modifyMult(KEY,
                (1f - (isSP ? HSIKnightSkill.DAMAGE_REDUCE_SP : HSIKnightSkill.DAMAGE_REDUCE) / 100f));
        ship.getMutableStats().getArmorDamageTakenMult().modifyMult(KEY,
                (1f - (isSP ? HSIKnightSkill.DAMAGE_REDUCE_SP : HSIKnightSkill.DAMAGE_REDUCE) / 100f));
        ship.getMutableStats().getShieldDamageTakenMult().modifyMult(KEY,
                (1f - (isSP ? HSIKnightSkill.DAMAGE_REDUCE_SP : HSIKnightSkill.DAMAGE_REDUCE) / 100f));
    }

    public void addBersekerBuff() {
        ship.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(KEY + 1,
                isSP ? HSIKnightSkill.AFTER_PLAYER_LOSS_SP : HSIKnightSkill.AFTER_PLAYER_LOSS);
        ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(KEY + 1,
                isSP ? HSIKnightSkill.AFTER_PLAYER_LOSS_SP : HSIKnightSkill.AFTER_PLAYER_LOSS);
        ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(KEY + 1,
                isSP ? HSIKnightSkill.AFTER_PLAYER_LOSS_SP : HSIKnightSkill.AFTER_PLAYER_LOSS);
    }

    public void clearBuff(ShipAPI ship) {
        ship.getMutableStats().getBallisticWeaponDamageMult().unmodify(KEY);
        ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(KEY);
        ship.getMutableStats().getMissileWeaponDamageMult().unmodify(KEY);
        ship.getMutableStats().getHullDamageTakenMult().unmodify(KEY);
        ship.getMutableStats().getArmorDamageTakenMult().unmodify(KEY);
        ship.getMutableStats().getShieldDamageTakenMult().unmodify(KEY);
        ship.getMutableStats().getBallisticWeaponDamageMult().unmodify(KEY + 1);
        ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(KEY + 1);
        ship.getMutableStats().getMissileWeaponDamageMult().unmodify(KEY + 1);
    }

    public void addDebuff(ShipAPI ship){
        ship.getMutableStats().getBallisticWeaponDamageMult().modifyMult(KEY + 1,
                isSP ? (1f-HSIKnightSkill.AFTER_PLAYER_LOSS_SP) : (1f-HSIKnightSkill.AFTER_PLAYER_LOSS));
        ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(KEY + 1,
                isSP ? (1f-HSIKnightSkill.AFTER_PLAYER_LOSS_SP) : (1f-HSIKnightSkill.AFTER_PLAYER_LOSS));
        ship.getMutableStats().getMissileWeaponDamageMult().modifyPercent(KEY + 1,
                isSP ? (1f-HSIKnightSkill.AFTER_PLAYER_LOSS_SP) : (1f-HSIKnightSkill.AFTER_PLAYER_LOSS));
    }

}
