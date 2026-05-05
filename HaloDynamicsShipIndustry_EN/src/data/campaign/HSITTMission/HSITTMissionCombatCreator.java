package data.campaign.HSITTMission;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.HSIStellaArena.HSISABuffManager;

import java.util.List;
import java.util.Random;

public class HSITTMissionCombatCreator extends BattleCreationPluginImpl {
    @Override
    public void initBattle(BattleCreationContext context, MissionDefinitionAPI loader) {

        this.context = context;
        this.loader = loader;
        CampaignFleetAPI playerFleet = context.getPlayerFleet();
        CampaignFleetAPI otherFleet = context.getOtherFleet();
        FleetGoal playerGoal = context.getPlayerGoal();
        FleetGoal enemyGoal = context.getOtherGoal();

        Random random = Misc.getRandom(Misc.getSalvageSeed(otherFleet) *
                (long)otherFleet.getFleetData().getNumMembers(), 23);

        int baseCommandPoints = (int) Global.getSettings().getFloat("startingCommandPoints");

        //
        loader.initFleet(FleetSide.PLAYER, "ISS", playerGoal, false,
                context.getPlayerCommandPoints() - baseCommandPoints,
                (int) playerFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);
        loader.initFleet(FleetSide.ENEMY, "", enemyGoal, true,
                (int) otherFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);

        List<FleetMemberAPI> playerShips = playerFleet.getFleetData().getCombatReadyMembersListCopy();
        if (playerGoal == FleetGoal.ESCAPE) {
            playerShips = playerFleet.getFleetData().getMembersListCopy();
        }
        for (FleetMemberAPI member : playerShips) {
            loader.addFleetMember(FleetSide.PLAYER, member);
        }


        List<FleetMemberAPI> enemyShips = otherFleet.getFleetData().getCombatReadyMembersListCopy();
        if (enemyGoal == FleetGoal.ESCAPE) {
            enemyShips = otherFleet.getFleetData().getMembersListCopy();
        }
        for (FleetMemberAPI member : enemyShips) {
            loader.addFleetMember(FleetSide.ENEMY, member);
        }

        width = 18000f;
        height = 18000f;

        loader.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        //createMap(random);

        //loader.setBackgroundSpriteName();

        context.setInitialDeploymentBurnDuration(1.5f);
        context.setNormalDeploymentBurnDuration(6f);
        context.setEscapeDeploymentBurnDuration(1.5f);

        xPad = 2000f;
        yPad = 3000f;

        context.setStandoffRange(6000f);
        context.setFlankDeploymentDistance(height/2f); // matters for Force Concentration
        loader.setHyperspaceMode(false);
    }

    @Override
    public void afterDefinitionLoad(CombatEngineAPI engine) {
        super.afterDefinitionLoad(engine);
        engine.addPlugin(new HSITTMissionCombatPlugin());
        engine.setRenderStarfield(false);
    }
}
