package data.missions.HSI_Test;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import data.scripts.HSIGenerator.HSIDreadnought;

import java.util.List;

public class MissionDefinition implements MissionDefinitionPlugin
{

  public void defineMission(MissionDefinitionAPI api) {


    //设定我方舰队，敌方的舰船名前缀，舰队名称，战役名称
    // Set up the fleets so we can add ships and fighter wings to them.
    // In this scenario, the fleets are attacking each other, but
    // in other scenarios, a fleet may be defending or trying to escape
    api.initFleet(FleetSide.PLAYER, "HIS", FleetGoal.ATTACK, false, 5);
    api.initFleet(FleetSide.ENEMY, "UnknownShadow", FleetGoal.ATTACK, true);

    // Set a small blurb for each fleet that shows up on the mission detail and
    // mission results screens to identify each side.
    api.setFleetTagline(FleetSide.PLAYER, "HSI Test");
    api.setFleetTagline(FleetSide.ENEMY, "Test");

    // These show up as items in the bulleted list under
    // "Tactical Objectives" on the mission detail screen
    api.addBriefingItem("Test all hsi Ships");


    //在这加自己的船，用装配文件的ID，后面是船名，true和false是“是否是旗舰”的设定
    // Set up the player's fleet.  Variant names come from the
    // files in data/variants and data/variants/fighters
    //主力
    api.addToFleet(FleetSide.PLAYER, "HSI_UkiyoA_Elite", FleetMemberType.SHIP, "HPS", true);
    api.addToFleet(FleetSide.PLAYER, "HSI_UkiyoB_Elite", FleetMemberType.SHIP, "HPS", true);
    api.addToFleet(FleetSide.PLAYER, "HSI_UkiyoC_Elite", FleetMemberType.SHIP, "HPS", true);
    api.addToFleet(FleetSide.PLAYER, "HSI_Oath_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Oath_GF3_Defense", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Oath_Stalker_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Citadel_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Vision_Strike", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_FengYue_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_FengYue_GF3_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_ShanYu_Special", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_ShanYu_Stalker_Assault", FleetMemberType.SHIP, "HPS", false);
    //api.addToFleet(FleetSide.PLAYER, "HSI_ShanYu_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Conquest_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_DaoHe2_Supply", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_DaoHe_Supply", FleetMemberType.SHIP, "HPS", false);

    //巡洋
    api.addToFleet(FleetSide.PLAYER, "HSI_Spade_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Promise_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Promise_GF3_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Xianfeng_Strike", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Xianfeng_GF3_Strike", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Aurora_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_GF3_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_Stalker_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_Support", FleetMemberType.SHIP, "HPS", false);

    //驱逐
    api.addToFleet(FleetSide.PLAYER, "HSI_ZhiYuan_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Elegy_Support", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Weaver_Strike", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_DengHuo_variant", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Qiming_Strike", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Sunder_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Medusa_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Medusa_Stalker_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Shrike_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Weaver_GF3_Strike", FleetMemberType.SHIP, "HPS", false);
    //护卫
    api.addToFleet(FleetSide.PLAYER, "HSI_YunYan_Assault", FleetMemberType.SHIP, "HPS", false);
    //api.addToFleet(FleetSide.PLAYER, "HSI_Comet_Aribiter", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_YunYan_GF3_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Comet_Focus", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Wolf_Assault", FleetMemberType.SHIP, "HPS", false);

    api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Dash", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Stalker_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Scarab_Assault", FleetMemberType.SHIP, "HPS", false);
    api.addToFleet(FleetSide.PLAYER, "HSI_Scarab_Stalker_Assault", FleetMemberType.SHIP, "HPS", false);

    api.addToFleet(FleetSide.PLAYER, "HSI_Life_Elite_A", FleetMemberType.SHIP, "HPS", true);
    api.addToFleet(FleetSide.PLAYER, "HSI_T_01_68_Elite", FleetMemberType.SHIP, "HPS", true);
    // Set up the enemy fleet.

    FactionAPI hsi = Global.getSettings().createBaseFaction("HSI");


    hsi.getVariantsForRole(ShipRoles.COMBAT_CAPITAL);
    hsi.getVariantsForRole(ShipRoles.COMBAT_LARGE);
    hsi.getVariantsForRole(ShipRoles.COMBAT_MEDIUM);
    hsi.getVariantsForRole(ShipRoles.COMBAT_SMALL);

    hsi.getVariantsForRole(ShipRoles.CARRIER_LARGE);
    hsi.getVariantsForRole(ShipRoles.CARRIER_MEDIUM);
    hsi.getVariantsForRole(ShipRoles.CARRIER_SMALL);

    hsi.getVariantsForRole(ShipRoles.COMBAT_FREIGHTER_LARGE);

    FleetMemberAPI member =  api.addToFleet(FleetSide.ENEMY, "HSI_Life_Elite_B", FleetMemberType.SHIP, "HPS", true);
    member.setCaptain(HSIDreadnought.createDreadnoughtCaptain());
    member =  api.addToFleet(FleetSide.ENEMY, "HSI_T_01_68_Elite", FleetMemberType.SHIP, "HPS", true);
    member.setCaptain(HSIDreadnought.createDreadnoughtCaptain());
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




