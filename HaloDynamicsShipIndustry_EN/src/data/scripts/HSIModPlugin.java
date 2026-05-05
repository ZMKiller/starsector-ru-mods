package data.scripts;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAAdvLoader;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;
import data.ai.HSIWeaponAI.HSIApocalypseLanceAI;
import data.ai.HSIWeaponAI.HSILiebeWeaponAI;
import data.ai.HSIWeaponAI.HSIMissilePDAI;
import data.ai.HSIWeaponAI.HSIScatterBeamAI;
import data.campaign.HPSID.HSIHPSIDTaleLoader;
import data.campaign.HSIStellaArena.HSISAAdvBCP;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import data.ai.HSICrossWindAI;
import data.ai.HSILAAMAI;
import data.ai.HSIPOSAI;
import data.ai.HWIAdvancedPDAI;
import data.ai.HSIMissileAI.HSIBaseMissileAI;
import data.campaign.HPSID.HSIHPSIDLevelIntel;
import data.scripts.HSIGenerator.HSIGen;
import data.scripts.HSIGenerator.HSIIPManager;
import data.scripts.HSIGenerator.HSIMonthListener;

public class HSIModPlugin extends BaseModPlugin {
  public static final String Wingman_ID = "HWI_Wingman";

  // shipAI plugin pick
  /*
   * @Override
   * public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI
   * ship) {
   * return null;
   * }
   */

  @Override
  public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
    switch (missile.getProjectileSpecId()) {
      case "HSI_ButterFly":
        return new PluginPick<MissileAIPlugin>(new HSILAAMAI(launchingShip, missile),
            CampaignPlugin.PickPriority.MOD_SPECIFIC);
      /*
       * case "HWI_FirstMantitude":
       * return new PluginPick<MissileAIPlugin>(new HSILAAMAI(launchingShip, missile),
       * CampaignPlugin.PickPriority.MOD_SPECIFIC);
       */
      case "HWI_POS_Shot2":
        return new PluginPick<MissileAIPlugin>(new HSIPOSAI(missile, launchingShip),
            CampaignPlugin.PickPriority.MOD_SPECIFIC);

      case "HWI_MGSystem_M":
        CombatEntityAPI t3= null;

      if(launchingShip!=null
              &&missile.getWeapon()!=null
              && launchingShip.getWeaponGroupFor(missile.getWeapon())!=null
              &&launchingShip.getWeaponGroupFor(missile.getWeapon()).getAutofirePlugin(missile.getWeapon())!=null){
        t3 = (launchingShip.getWeaponGroupFor(missile.getWeapon())
              .getAutofirePlugin(missile.getWeapon()).getTargetMissile());
      }
        return new PluginPick<MissileAIPlugin>(
            new HSIBaseMissileAI(t3, missile),
            CampaignPlugin.PickPriority.MOD_SPECIFIC);

      //case "HWI_POS_Shot2":
        //return new PluginPick<MissileAIPlugin>(
            //new HSIBaseMissileAI(launchingShip.getShipTarget(), missile),
            //CampaignPlugin.PickPriority.MOD_SPECIFIC);
    }
    return null;
  }

  // weaponAI plugin pick
  @Override
  public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
    /*
     * if (weapon.getId().equals(Wingman_ID)) {
     * return new PluginPick<AutofireAIPlugin>(new HWIAntiProjectileAI(weapon),
     * CampaignPlugin.PickPriority.MOD_SPECIFIC);
     * }
     */
    switch (weapon.getId()) {
      //case "HWI_Duality":
        //return new PluginPick<AutofireAIPlugin>(new HSIThreatAnalysisPDAI(weapon),
           // CampaignPlugin.PickPriority.MOD_SPECIFIC);
      case "HSI_Apocalypse_RA":
        return new PluginPick<AutofireAIPlugin>(new HSIApocalypseLanceAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
      case "HWI_Wingman":
        return new PluginPick<AutofireAIPlugin>(new HWIAdvancedPDAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
      case "HWI_Crosswind":
        return new PluginPick<AutofireAIPlugin>(new HSICrossWindAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
      case "HWI_Standard":
      case "HWI_HuoShu":
        return new PluginPick<AutofireAIPlugin>(new HSIMissilePDAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
      case "HWI_Liebe":
        return new PluginPick<AutofireAIPlugin>(new HSILiebeWeaponAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
      case "HWI_ScatterCascade":
        return new PluginPick<AutofireAIPlugin>(new HSIScatterBeamAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
      default:
        break;
    }
    return null;
  }

  // create a sector
  @Override
  public void onNewGame() {
    new HSIGen().generate(Global.getSector());
    SharedData.getData().getPersonBountyEventData().addParticipatingFaction("HSI");
  }

  @Override
  public void onNewGameAfterEconomyLoad() {
    HSIIPManager.createCommisioner(Global.getSector());
    HSIIPManager.createKnight(Global.getSector());
    HSIIPManager.createStalkerContact(Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market"));
    HSIIPManager.createHSIContact(Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market"));
    if (!Global.getSector().hasScript(HSICampaignScript.class)) {
      Global.getSector().addScript(new HSICampaignScript());
    }
    if (Global.getSector().getEconomy().getMarket("HSI_Wanderer_Station_Market") != null) {
      HSIIPManager.createHPSIDOperator(Global.getSector().getEconomy().getMarket("HSI_Wanderer_Station_Market"));
    }
  }

  public void onGameLoad(boolean newGame) {
    addBarEvents();
    Global.getSector().registerPlugin(new HSICampaignPlugin());
    if (!Global.getSector().hasScript(HSICampaignScript.class)) {
      Global.getSector().addScript(new HSICampaignScript());
    }
    if (!Global.getSector().getListenerManager().hasListenerOfClass(HSIMonthListener.class)) {
      Global.getSector().getListenerManager().addListener(new HSIMonthListener());
    }
    if (Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market") == null) {
      onNewGame();
      onNewGameAfterEconomyLoad();
    }
    HSIHPSIDLevelIntel.getInstance();
    HSISAAdvLoader.load();
    HSIHPSIDTaleLoader.load();
  }

  protected void addBarEvents() {
    // BarEventManager bar = BarEventManager.getInstance();
    /*
     * if (!bar.hasEventCreator(HSICommissionerBarEvent0Creator.class)) {
     * bar.addEventCreator(new HSICommissionerBarEvent0Creator());
     * }
     */
    /*
     * if (!bar.hasEventCreator(HSICommissionerBarEvent1Creator.class)) {
     * bar.addEventCreator(new HSICommissionerBarEvent1Creator());
     * }
     */
  }

  public void onApplicationLoad() throws Exception {
    boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
    if (hasGraphicsLib) {
      ShaderLib.init();
      LightData.readLightDataCSV("data/config/HSI_light_data.csv");
      TextureData.readTextureDataCSV("data/config/HSI_texture_data.csv");
    }
  }

  @Override
  public void onNewGameAfterTimePass() {
    if(Global.getSector().getEconomy().getMarket("chicomoztoc")!=null){
      Global.getSector().getEconomy().getMarket("chicomoztoc").addIndustry("HSI_Support");
    }
  }
}
