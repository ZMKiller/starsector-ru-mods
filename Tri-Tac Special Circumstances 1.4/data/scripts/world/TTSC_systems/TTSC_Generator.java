package data.scripts.world.TTSC_systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain.TileParams;

public class TTSC_Generator {
   public void generate(SectorAPI sector) {	   
      StarSystemAPI system = sector.getStarSystem("Hybrasil");
      SectorEntityToken ttsc_stationH1 = system.addCustomEntity("TTSC_headquarters", "SC Headquarters", "station_side04", "TTSC");
      ttsc_stationH1.setCircularOrbitPointingDown(system.getEntityById("Hybrasil"), 155.0F, 1500.0F, 600.0F);
      ttsc_stationH1.setCustomDescriptionId("TTSC_headquarters");
      float defaultTariff = Global.getSector().getFaction("TTSC").getTariffFraction();
      MarketAPI market_H1 = Global.getFactory().createMarket("TTSC_headquarters", "SC Headquarters", 6);
      ttsc_stationH1.setMarket(market_H1);
      market_H1.setPrimaryEntity(ttsc_stationH1);
      market_H1.setFactionId("TTSC");
      market_H1.setFreePort(false);
      market_H1.addSubmarket("open_market");
      market_H1.addSubmarket("generic_military");
      market_H1.setPlanetConditionMarketOnly(false);
      market_H1.addCondition("population_6");
      market_H1.getTariff().modifyFlat("default_tariff", defaultTariff);	  
      market_H1.setSurveyLevel(SurveyLevel.FULL);

      for(MarketConditionAPI cond : market_H1.getConditions()) {
         cond.setSurveyed(true);
      }

      market_H1.addIndustry("waystation");
      market_H1.addIndustry("population");
      market_H1.addIndustry("megaport");
      market_H1.addIndustry("grounddefenses");
      market_H1.addIndustry("orbitalworks");
      market_H1.addIndustry("refining");
      market_H1.addIndustry("highcommand");
      market_H1.addIndustry("starfortress_high");
      market_H1.addSubmarket("storage");  
      Global.getSector().getEconomy().addMarket(market_H1, true);
	  
	  //start station 2
	  

//      system = sector.getStarSystem("Hybrasil");
      SectorEntityToken ttsc_stationH2 = system.addCustomEntity("TTSC_hybrasil1_station", "GSV Gobuchul Station", "station_side03", "TTSC");
      ttsc_stationH2.setCircularOrbitPointingDown(system.getEntityById("Hybrasil"), 90.0F, 1200.0F, 800.0F);
	  ttsc_stationH2.setCustomDescriptionId("TTSC_hybrasil1_station");
	  
      MarketAPI market_H2 = Global.getFactory().createMarket("TTSC_hybrasil1_station", "GSV Gobuchul Station", 4);
      ttsc_stationH2.setMarket(market_H2);
      market_H2.setPrimaryEntity(ttsc_stationH2);
      market_H2.setFactionId("TTSC");
	  market_H2.setFreePort(false);
      market_H2.addSubmarket("open_market");
      market_H2.addSubmarket("generic_military");
      market_H2.addSubmarket("black_market");
      market_H2.addCondition("population_4");
      market_H2.getTariff().modifyFlat("default_tariff", defaultTariff);		  
      market_H2.setSurveyLevel(SurveyLevel.FULL);

      for(MarketConditionAPI cond : market_H2.getConditions()) {
         cond.setSurveyed(true);
      }

      market_H2.addIndustry("waystation");
      market_H2.addIndustry("population");
      market_H2.addIndustry("spaceport");
      market_H2.addIndustry("patrolhq");
      market_H2.addIndustry("lightindustry");
      market_H2.addIndustry("starfortress_high");
      market_H2.addSubmarket("storage");

      Global.getSector().getEconomy().addMarket(market_H2, true);	  
  
  //Start Magec
   
      system = sector.getStarSystem("Magec");
      SectorEntityToken ttsc_stationM1 = system.addCustomEntity("TTSC_Magec_station", "GSV Horza Outpost", "station_side04", "TTSC");
      ttsc_stationM1.setCircularOrbitPointingDown(system.getEntityById("Magec"), 170.0F, 8500.0F, 500.0F);
      ttsc_stationM1.setCustomDescriptionId("TTSC_Magec_station");	  
	  
      MarketAPI market_M1 = Global.getFactory().createMarket("TTSC_Magec_station", "GSV Horza Outpost", 4);
      ttsc_stationM1.setMarket(market_M1);
      market_M1.setPrimaryEntity(ttsc_stationM1);
      market_M1.setFactionId("TTSC");
      market_M1.setFreePort(false);
      market_M1.addSubmarket("open_market");
      market_M1.addSubmarket("generic_military");
      market_M1.addSubmarket("black_market");
      market_M1.addCondition("population_4");
      market_M1.getTariff().modifyFlat("default_tariff", defaultTariff);		  
      market_M1.setSurveyLevel(SurveyLevel.FULL);

      for(MarketConditionAPI cond : market_M1.getConditions()) {
         cond.setSurveyed(true);
      }

      market_M1.addIndustry("waystation");
      market_M1.addIndustry("population");
      market_M1.addIndustry("spaceport");
      market_M1.addIndustry("militarybase");
      market_M1.addIndustry("battlestation_high");
      market_M1.addSubmarket("storage");
      Global.getSector().getEconomy().addMarket(market_M1, true);

   }
}
