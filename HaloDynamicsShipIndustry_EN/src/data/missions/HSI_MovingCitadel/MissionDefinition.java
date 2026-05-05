package data.missions.HSI_MovingCitadel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

import data.kit.HSII18nUtil;

import java.util.List;

public class MissionDefinition implements MissionDefinitionPlugin
{

  public void defineMission(MissionDefinitionAPI api) {


    //设定我方舰队，敌方的舰船名前缀，舰队名称，战役名称
    // Set up the fleets so we can add ships and fighter wings to them.
    // In this scenario, the fleets are attacking each other, but
    // in other scenarios, a fleet may be defending or trying to escape
    api.initFleet(FleetSide.PLAYER, "HIS", FleetGoal.ATTACK, false, 5);
    api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

    // Set a small blurb for each fleet that shows up on the mission detail and
    // mission results screens to identify each side.
    api.setFleetTagline(FleetSide.PLAYER, HSII18nUtil.getCampaignString("HSIMissionMCName"));
    api.setFleetTagline(FleetSide.ENEMY, HSII18nUtil.getCampaignString("HSIMissionMCTTName"));

    // These show up as items in the bulleted list under
    // "Tactical Objectives" on the mission detail screen
    api.addBriefingItem(HSII18nUtil.getCampaignString("HSIMissionMCTarget0"));
    //api.addBriefingItem(HSII18nUtil.getCampaignString("HSIMissionHammerTarget1"));

    api.addToFleet(FleetSide.PLAYER, "HSI_Citadel_Elite", FleetMemberType.SHIP, "HIS Daylight", true);

    api.addToFleet(FleetSide.PLAYER, "HSI_Comet_Focus", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Comet_Focus", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Comet_Focus", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Comet_Focus", FleetMemberType.SHIP, false);

    api.defeatOnShipLoss("HIS Daylight");
    //在这加敌人的船，一样用装配文件里的ID，加了一艘统治者的Support装配
    // Set up the enemy fleet.
    api.addToFleet(FleetSide.ENEMY, "aurora_Balanced", FleetMemberType.SHIP, true);
    api.addToFleet(FleetSide.ENEMY, "aurora_Balanced", FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY, "fury_Attack", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.ENEMY, "brawler_tritachyon_Standard", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.ENEMY, "brawler_tritachyon_Standard", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.ENEMY, "brawler_tritachyon_Standard", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.ENEMY, "brawler_tritachyon_Standard", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.ENEMY, "brawler_tritachyon_Standard", FleetMemberType.SHIP, false);



    FactionAPI hsi = Global.getSettings().createBaseFaction("HSI");

    //设定地图的尺寸和贴图和里面的星云，陨石，占领点，直接粘的一个原版战役
    // Set up the map.
    float width = 24000f;
    float height = 18000f;
    api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

    float minX = -width / 2;
    float minY = -height / 2;

    api.addNebula(minX + width * 0.5f - 300, minY + height * 0.5f, 1000);
    api.addNebula(minX + width * 0.5f + 300, minY + height * 0.5f, 1000);

    for (int i = 0; i < 5; i++) {
      float x = (float) Math.random() * width - width / 2;
      float y = (float) Math.random() * height - height / 2;
      float radius = 100f + (float) Math.random() * 400f;
      api.addNebula(x, y, radius);
    }

    // Add an asteroid field
    api.addAsteroidField(minX + width / 2f, minY + height / 2f, 0, 8000f,
      20f, 70f, 100);


    //加入特殊的每帧效果
    api.addPlugin(new BaseEveryFrameCombatPlugin()
    {
      public void init(CombatEngineAPI engine) {
        engine.getContext().setStandoffRange(6000f);
      }

      public void advance(float amount, List events) {
      }
    });


  }

}




