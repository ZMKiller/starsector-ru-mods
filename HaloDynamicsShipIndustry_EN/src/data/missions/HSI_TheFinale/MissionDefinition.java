package data.missions.HSI_TheFinale;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import data.kit.HSII18nUtil;
import data.scripts.HSIGenerator.HSIDreadnought;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class MissionDefinition implements MissionDefinitionPlugin
{

  public void defineMission(MissionDefinitionAPI api) {


    //设定我方舰队，敌方的舰船名前缀，舰队名称，战役名称
    // Set up the fleets so we can add ships and fighter wings to them.
    // In this scenario, the fleets are attacking each other, but
    // in other scenarios, a fleet may be defending or trying to escape
    api.initFleet(FleetSide.PLAYER, "HPS", FleetGoal.ATTACK, false, 1);
    api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true,10);
    // Set a small blurb for each fleet that shows up on the mission detail and
    // mission results screens to identify each side.
    api.setFleetTagline(FleetSide.PLAYER, HSII18nUtil.getCampaignString("HSIMissionTheFinaleHSIName"));
    api.setFleetTagline(FleetSide.ENEMY, HSII18nUtil.getCampaignString("HSIMissionTheFinaleTTName"));

    // These show up as items in the bulleted list under
    // "Tactical Objectives" on the mission detail screen
    api.addBriefingItem(HSII18nUtil.getCampaignString("HSIMissionTheFinaleTarget0"));
    //api.addBriefingItem(HSII18nUtil.getCampaignString("HSIMissionFuseTarget1"));


    //在这加自己的船，用装配文件的ID，后面是船名，true和false是“是否是旗舰”的设定
    // Set up the player's fleet.  Variant names come from the
    // files in data/variants and data/variants/fighters
    //巡洋
    //api.addToFleet(FleetSide.PLAYER, "HSI_Promise_Support", FleetMemberType.SHIP, "HIS SeigeHammer", true);

    //驱逐
    FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "HSI_Life_Elite_A", FleetMemberType.SHIP,"HPS Floating Life" ,true);
    member.getVariant().addPermaMod("HSI_GuardFleet1");

    member = api.addToFleet(FleetSide.PLAYER, "HSI_ShanYu_Assault", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createHigherCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_ShanYu_Assault", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createHigherCaptain());

    member = api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_Support", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createHigherCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_Support", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createHigherCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_Support", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createHigherCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_Eagle_Support", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createHigherCaptain());

    member = api.addToFleet(FleetSide.PLAYER, "HSI_Elegy_Support", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createLowerCaptain());

    member = api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Dash", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Dash", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Dash", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Dash", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Dash", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.PLAYER, "HSI_TianGuang_Dash", FleetMemberType.SHIP, false);
    member.getVariant().addPermaMod("HSI_GuardFleet1");
    member.setCaptain(HSIDreadnought.createLowerCaptain());

    member = api.addToFleet(FleetSide.ENEMY,"paragon_Elite_Delta",FleetMemberType.SHIP,true);
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.ENEMY,"paragon_Elite_Delta",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.ENEMY,"paragon_Elite_Delta",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());

    member = api.addToFleet(FleetSide.ENEMY,"aurora_Elite_Delta",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.ENEMY,"aurora_Elite_Delta",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.ENEMY,"aurora_Elite_Delta",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.ENEMY,"aurora_Elite_Delta",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());

    member = api.addToFleet(FleetSide.ENEMY,"fury_HSISS_HVB2",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());
    member = api.addToFleet(FleetSide.ENEMY,"fury_HSISS_HVB2",FleetMemberType.SHIP,false);
    member.setCaptain(HSIDreadnought.createLowerCaptain());

    api.addToFleet(FleetSide.ENEMY,"afflictor_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"afflictor_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"afflictor_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);
    api.addToFleet(FleetSide.ENEMY,"brawler_tritachyon_HSISS_HVB2",FleetMemberType.SHIP,false);









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

    api.addPlanet(0f,6500f, 475f,"star_neutron", 0f, true);


    //加入特殊的每帧效果
    api.addPlugin(new HSI_TheFinaleScript());


  }

  public static class HSI_TheFinaleScript extends BaseEveryFrameCombatPlugin{
    private float elapsed = 0;

    private boolean couldCall = true;

    private boolean finishedCall = false;

    private int wave = 0;

    private int message = 0;

    private IntervalUtil callInterval = new IntervalUtil(15f,20f);
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
      if(Global.getCombatEngine().isPaused()) return;
      elapsed += amount;
      couldCall = elapsed >= 345f;
      if(couldCall&&!finishedCall){
        callInterval.advance(amount);
        if(callInterval.intervalElapsed()){
          switch (wave){
            case 0:
              Global.getCombatEngine().getCombatUI().addMessage(1, Color.CYAN, "侦测到跃迁信号");
              ShipAPI flagship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).spawnShipOrWing("HSI_Life_Elite_A",new Vector2f((float)Math.random()*2000f+13000f,12000f),80f+(float) Math.random()*20f,2f,HSIDreadnought.createHigherCaptain());
              flagship.setCaptain(HSIDreadnought.createHigherCaptain());
              flagship.setAlly(true);
              break;
            case 1:
              Global.getCombatEngine().getCombatUI().addMessage(1,Color.CYAN,"这里是第三舰队。");
              for(int i = 0;i<3;i++){
                ShipAPI ship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).spawnShipOrWing("HSI_Oath_Support",new Vector2f((float)Math.random()*2000f+14000f,3000f+(float)Math.random()*1000f+3000f*i),60f+(float) Math.random()*60f,2f,HSIDreadnought.createLowerCaptain());
                ship.setCaptain(HSIDreadnought.createLowerCaptain());
                ship.setAlly(true);
              }
              break;
            case 2:
              Global.getCombatEngine().getCombatUI().addMessage(1,Color.CYAN,"为了人之领！");
              for(int i = 0;i<2;i++){
                ShipAPI ship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).spawnShipOrWing("HSI_Aurora_Assault",new Vector2f((float)Math.random()*1000f+15000f,4000f+(float)Math.random()*2000f+4000f*i),60f+(float) Math.random()*60f,2f,HSIDreadnought.createLowerCaptain());
                ship.setCaptain(HSIDreadnought.createLowerCaptain());
                ship.setAlly(true);
              }

              for(int i = 0;i<4;i++){
                ShipAPI ship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).spawnShipOrWing("HSI_Eagle_Support",new Vector2f((float)Math.random()*1000f+15000f,2000f+(float)Math.random()*1000f+2000f*i),60f+(float) Math.random()*60f,2f);
                ship.setAlly(true);
              }

              break;
            case 3:
              for(int i = 0;i<7;i++){
                ShipAPI ship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).spawnShipOrWing("HSI_YunYan_Assault",new Vector2f(0f,2000f+(float)Math.random()*1000f+1000f*i),90f,6f);
                ship.setAlly(true);
              }
              for(int i = 0;i<7;i++){
                ShipAPI ship = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).spawnShipOrWing("HSI_Wolf_Assault",new Vector2f(0f,1200f+(float)Math.random()*1000f+1000f*i),90f,6f);
                ship.setAlly(true);
              }

              finishedCall = true;
              break;
            default:
          }
          wave++;
        }
      }
    }
  }

}




