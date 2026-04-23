package data.scripts.world.TTE_systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain.TileParams;

public class TTE_Generator {
   public void generate(SectorAPI sector) {	   
      StarSystemAPI system = sector.getStarSystem("Hybrasil");
      SectorEntityToken tte_stationH1 = system.addCustomEntity("tte_headquarters", "Culann Expansion HQ", "station_side07", "tritachyon");
      tte_stationH1.setCircularOrbitPointingDown(system.getEntityById("culann"), 140, 1120, 80);
      tte_stationH1.setCustomDescriptionId("tte_headquarters");
      float defaultTariff = Global.getSector().getFaction("tritachyon").getTariffFraction();
      MarketAPI market_HE1 = Global.getFactory().createMarket("tte_headquarters", "Culann Expansion HQ", 6);
      tte_stationH1.setMarket(market_HE1);
      market_HE1.setPrimaryEntity(tte_stationH1);
      market_HE1.setFactionId("tritachyon");
      market_HE1.setFreePort(false);
      market_HE1.addSubmarket("open_market");
      market_HE1.addSubmarket("generic_military");
      market_HE1.setPlanetConditionMarketOnly(false);
      market_HE1.addCondition("population_6");
      market_HE1.getTariff().modifyFlat("default_tariff", defaultTariff);	  
      market_HE1.setSurveyLevel(SurveyLevel.FULL);

      for(MarketConditionAPI cond : market_HE1.getConditions()) {
         cond.setSurveyed(true);
      }

      market_HE1.addIndustry("waystation");
      market_HE1.addIndustry("population");
      market_HE1.addIndustry("megaport");
      market_HE1.addIndustry("grounddefenses");
      market_HE1.addIndustry("orbitalworks");
      market_HE1.addIndustry("refining");
      market_HE1.addIndustry("highcommand");
      market_HE1.addIndustry("starfortress_high");
      market_HE1.addSubmarket("storage");  
      Global.getSector().getEconomy().addMarket(market_HE1, true);
	  
	  //start Valhalla
	  

      system = sector.getStarSystem("Valhalla");
      SectorEntityToken tte_stationV1 = system.addCustomEntity("tte_valhalla_station", "Skathi Trans-Hub Station", "station_side03", "tritachyon");
      tte_stationV1.setCircularOrbitPointingDown(system.getEntityById("skathi"), 265, 1100, 290);
	  tte_stationV1.setCustomDescriptionId("tte_valhalla_station");
	  
      MarketAPI market_VE1 = Global.getFactory().createMarket("tte_valhalla_station", "Skathi Trans-Hub Station", 4);
      tte_stationV1.setMarket(market_VE1);
      market_VE1.setPrimaryEntity(tte_stationV1);
      market_VE1.setFactionId("tritachyon");
	  market_VE1.setFreePort(false);
      market_VE1.addSubmarket("open_market");
      market_VE1.addSubmarket("generic_military");
      market_VE1.addSubmarket("black_market");
      market_VE1.addCondition("population_4");
      market_VE1.getTariff().modifyFlat("default_tariff", defaultTariff);		  
      market_VE1.setSurveyLevel(SurveyLevel.FULL);

      for(MarketConditionAPI cond : market_VE1.getConditions()) {
         cond.setSurveyed(true);
      }

      market_VE1.addIndustry("waystation");
      market_VE1.addIndustry("population");
      market_VE1.addIndustry("spaceport");
      market_VE1.addIndustry("patrolhq");
      market_VE1.addIndustry("lightindustry");
      market_VE1.addIndustry("starfortress_high");
      market_VE1.addSubmarket("storage");

      Global.getSector().getEconomy().addMarket(market_VE1, true);	  
  
  //Start Magec
   
      system = sector.getStarSystem("Magec");
      SectorEntityToken tte_stationM1 = system.addCustomEntity("tte_Magec_station", "Tibicena Depository", "station_side04", "tritachyon");
      tte_stationM1.setCircularOrbitPointingDown(system.getEntityById("tibicena"), 110, 1100, 275);
      tte_stationM1.setCustomDescriptionId("tte_Magec_station");	  
	  
      MarketAPI market_ME1 = Global.getFactory().createMarket("tte_Magec_station", "Tibicena Depository", 5);
      tte_stationM1.setMarket(market_ME1);
      market_ME1.setPrimaryEntity(tte_stationM1);
      market_ME1.setFactionId("tritachyon");
      market_ME1.setFreePort(false);
      market_ME1.addSubmarket("open_market");
      market_ME1.addSubmarket("generic_military");
      market_ME1.addSubmarket("black_market");
      market_ME1.addCondition("population_5");
      market_ME1.getTariff().modifyFlat("default_tariff", defaultTariff);		  
      market_ME1.setSurveyLevel(SurveyLevel.FULL);

      for(MarketConditionAPI cond : market_ME1.getConditions()) {
         cond.setSurveyed(true);
      }

      market_ME1.addIndustry("waystation");
      market_ME1.addIndustry("population");
      market_ME1.addIndustry("spaceport");
      market_ME1.addIndustry("militarybase");
      market_ME1.addIndustry("battlestation_high");
      market_ME1.addSubmarket("storage");
      Global.getSector().getEconomy().addMarket(market_ME1, true);
	  
  //Start Mayasura
   
      system = sector.getStarSystem("Mayasura");
      SectorEntityToken tte_stationMA1 = system.addCustomEntity("tte_Mayasura_station", "Port Tse Strategic Outpost", "station_side03", "tritachyon");
      tte_stationMA1.setCircularOrbitPointingDown(system.getEntityById("port_tse"), 280, 1110, 290);
      tte_stationMA1.setCustomDescriptionId("tte_Mayasura_station");	  
	  
      MarketAPI market_MAE1 = Global.getFactory().createMarket("tte_Mayasura_station", "Port Tse Strategic Outpost", 4);
      tte_stationMA1.setMarket(market_MAE1);
      market_MAE1.setPrimaryEntity(tte_stationMA1);
      market_MAE1.setFactionId("tritachyon");
      market_MAE1.setFreePort(false);
      market_MAE1.addSubmarket("open_market");
      market_MAE1.addSubmarket("generic_military");
      market_MAE1.addSubmarket("black_market");
      market_MAE1.addCondition("population_4");
      market_MAE1.getTariff().modifyFlat("default_tariff", defaultTariff);		  
      market_MAE1.setSurveyLevel(SurveyLevel.FULL);

      for(MarketConditionAPI cond : market_MAE1.getConditions()) {
         cond.setSurveyed(true);
      }

      market_MAE1.addIndustry("waystation");
      market_MAE1.addIndustry("population");
      market_MAE1.addIndustry("spaceport");
      market_MAE1.addIndustry("militarybase");
      market_MAE1.addIndustry("battlestation_high");
      market_MAE1.addSubmarket("storage");
      Global.getSector().getEconomy().addMarket(market_MAE1, true);	  

   }
}
