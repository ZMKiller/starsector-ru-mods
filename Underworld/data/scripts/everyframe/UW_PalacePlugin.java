package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.UW_Util;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class UW_PalacePlugin extends BaseEveryFrameCombatPlugin {

    private CombatEngineAPI engine;
    private boolean preventedRetreat = false;
    private boolean isPalace = false;
    private final IntervalUtil interval = new IntervalUtil(0.9f, 1.1f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (!engine.isInCampaign()) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        interval.advance(amount);
        if (!interval.intervalElapsed()) {
            return;
        }

        boolean isFlagshipAlive = false;
        ShipAPI flagship = null;
        CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.ENEMY);
        for (FleetMemberAPI member : manager.getDeployedCopy()) {
            FleetDataAPI data = member.getFleetData();
            if (data == null) {
                continue;
            }

            CampaignFleetAPI fleet = data.getFleet();
            if (fleet == null) {
                continue;
            }

            if (fleet.getMemoryWithoutUpdate().contains("$uwIsPalace") && fleet.getMemoryWithoutUpdate().getBoolean("$uwIsPalace")) {
                isPalace = true;
                if (fleet.getFlagship() != null) {
                    ShipAPI fs = manager.getShipFor(fleet.getFlagship());
                    if (fs != null) {
                        isFlagshipAlive = fs.isAlive();
                        flagship = fs;
                    }
                }
                break;
            }
        }
        if (!isPalace) {
            for (FleetMemberAPI member : manager.getReservesCopy()) {
                FleetDataAPI data = member.getFleetData();
                if (data == null) {
                    continue;
                }

                CampaignFleetAPI fleet = data.getFleet();
                if (fleet == null) {
                    continue;
                }

                if (fleet.getMemoryWithoutUpdate().contains("$uwIsPalace") && fleet.getMemoryWithoutUpdate().getBoolean("$uwIsPalace")) {
                    isPalace = true;
                    break;
                }
            }
        }
        if (isPalace) {
            if (flagship == null) {
                for (FleetMemberAPI member : manager.getReservesCopy()) {
                    FleetDataAPI data = member.getFleetData();
                    if (data == null) {
                        continue;
                    }

                    CampaignFleetAPI fleet = data.getFleet();
                    if (fleet == null) {
                        continue;
                    }

                    if ((fleet.getFlagship() != null) && fleet.getFlagship().getId().contentEquals(member.getId())) {
                        isFlagshipAlive = true;
                    }
                    if (member.getHullId().contentEquals("uw_palace")) {
                        Vector2f safeSpawn = UW_Util.getSafeSpawn(UW_Util.getMemberRadiusEstimate(member),
                                FleetSide.ENEMY,
                                Global.getCombatEngine().getMapWidth(),
                                Global.getCombatEngine().getMapHeight(),
                                Global.getCombatEngine().getContext().getPlayerGoal() == FleetGoal.ESCAPE);
                        manager.spawnFleetMember(member, safeSpawn, 270f, 2f);
                        manager.removeFromReserves(member);
                    }
                }
            }

            if (isFlagshipAlive) {
                preventedRetreat = true;
                manager.getTaskManager(false).setPreventFullRetreat(true);
            } else if (preventedRetreat) {
                preventedRetreat = false;
            }
            if (isFlagshipAlive && (flagship != null)) {
                if ((manager.getTaskManager(flagship.isAlly()).getAssignmentFor(flagship) != null)
                        && (manager.getTaskManager(flagship.isAlly()).getAssignmentFor(flagship).getType() == CombatAssignmentType.RETREAT)) {
                    manager.getTaskManager(flagship.isAlly()).orderSearchAndDestroy(manager.getDeployedFleetMember(flagship), false);
                    StatMod mod = manager.getTaskManager(flagship.isAlly()).getCommandPointsStat().getFlatStatMod("uw_palace");
                    if (mod == null) {
                        manager.getTaskManager(flagship.isAlly()).getCommandPointsStat().modifyFlat("uw_palace", 1);
                    } else {
                        mod.value++;
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        interval.forceIntervalElapsed();
    }
}
