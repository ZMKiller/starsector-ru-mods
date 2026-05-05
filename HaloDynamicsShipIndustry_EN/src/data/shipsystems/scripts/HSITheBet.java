package data.shipsystems.scripts;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class HSITheBet extends BaseShipSystemScript {
    private ShipAPI ship;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private boolean used = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (bay.getWing() == null)
                continue;
            bay.makeCurrentIntervalFast();
            FighterWingSpecAPI spec = bay.getWing().getSpec();
            int maxTotal = spec.getNumFighters();
            int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
            if (actualAdd > 0) {
                bay.setExtraDeploymentLimit(maxTotal);
                bay.setExtraDeployments(maxTotal+actualAdd);
                bay.setFastReplacements(bay.getFastReplacements() + actualAdd);
                bay.setExtraDuration(40f);
            }
            for (FighterWingAPI fighter : ship.getAllWings()) {
                if (fighter.isDestroyed())
                    continue;
                List<ShipAPI> members = fighter.getWingMembers();
                for (ShipAPI member : members) {
                    MutableShipStatsAPI memberStats = member.getMutableStats();
                    String mid = member.getId();
                    memberStats.getMaxSpeed().modifyMult(mid, 2f);
                    memberStats.getAcceleration().modifyMult(mid, 2f);
                    memberStats.getMaxTurnRate().modifyMult(mid, 2f);
                    memberStats.getTurnAcceleration().modifyMult(mid, 2f);
                    memberStats.getTimeMult().modifyMult(mid, 1.5f);
                    memberStats.getHullDamageTakenMult().modifyMult(mid, 0.5f);
                    member.setCircularJitter(true);
                    member.setJitterUnder(ship, Color.CYAN, effectLevel, (int)effectLevel*10, 5);
                }
            }
        }
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f);
        stats.getFighterWingRange().modifyMult(id, effectLevel*4f);
        if(!used) used = true;
        if(ship.isPullBackFighters()){
            ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, ship.getLocation(), 0);
        }else{
            ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        Color expColor = new Color(15, 45, 255);
        Color expCoreColor = new Color(225, 235, 255);
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        int side = ship.getOwner();
        
        for (FighterWingAPI fighter : ship.getAllWings()) {
            if (fighter.isDestroyed())
                continue;
            List<ShipAPI> members = fighter.getWingMembers();
            for (ShipAPI member : members) {
                engine.removeObject(member);
                engine.spawnExplosion(member.getLocation(), member.getVelocity(), expColor, 10f, 2.5f);
                engine.spawnExplosion(member.getLocation(), member.getVelocity(), expCoreColor, 5f, 2.5f);
            }
        }
        stats.getZeroFluxMinimumFluxLevel().unmodify(id);
        if(used){
            stats.getFighterRefitTimeMult().modifyMult(id, 1.33f);
        }
    }
}
