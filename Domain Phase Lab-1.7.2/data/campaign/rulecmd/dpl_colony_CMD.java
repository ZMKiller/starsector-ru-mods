package data.campaign.rulecmd;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddShip;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import data.scripts.crisis.dpl_ProdUnionScript;

/**
 * 
 *	dpl_colony_CMD <action> <parameters>
 */
public class dpl_colony_CMD extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
				
		if ("giveSaxophone".equals(action)) {
			giveSaxophone(dialog, memoryMap);
		} else if ("givePiano".equals(action)) {
			givePiano(dialog, memoryMap);
		} else if ("giveSilence".equals(action)) {
			giveSilence(dialog, memoryMap);
		} else if ("giveDirge".equals(action)) {
			giveDirge(dialog, memoryMap);
		} else if ("giveLyre".equals(action)) {
			giveLyre(dialog, memoryMap);
		} else if ("giveHydraulophone".equals(action)) {
			giveHydraulophone(dialog, memoryMap);
		} else if ("giveSacraMachina".equals(action)) {
			giveSacraMachina(dialog, memoryMap);
		} else if ("giveHELATransport".equals(action)) {
			giveHELATransport(dialog, memoryMap);
		} else if ("hasHELATransport".equals(action)) {
			for (FleetMemberAPI member : pf.getFleetData().getMembersListCopy()) {
				if (member.getHullSpec().getHullId().equals("dpl_gate_transport") || member.getHullSpec().getHullId().equals("dpl_gate_transport_default_D")) {
					memory.set("$foundShipId", member.getId());	
					memory.set("$foundShipClass", member.getHullSpec().getNameWithDesignationWithDashClass());				
					memory.set("$foundShipName", member.getShipName());				
					return true;
				}
			}
			return false;
		} else if ("hasIndustrial".equals(action)) {
			for (MarketAPI market: Misc.getPlayerMarkets(false)) {
				for (Industry ind : market.getIndustries()) {
					if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
						return true;
					}
				}
			}
			return false;
		} else if ("hasLargeIndustrial".equals(action)) {
			for (MarketAPI market: Misc.getPlayerMarkets(false)) {
				if (market.getSize() >= 4) {
					for (Industry ind : market.getIndustries()) {
						if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
							return true;
						}
					}
				}
			}
			return false;
		} else if ("loseLargeIndustrial".equals(action)) {
			loseLargestIndustrial(dialog, memoryMap);
		} else if ("hasMilitary".equals(action)) {
			for (MarketAPI market: Misc.getPlayerMarkets(false)) {
				for (Industry ind : market.getIndustries()) {
					if (ind.getSpec().hasTag(Industries.TAG_MILITARY) || ind.getSpec().hasTag(Industries.TAG_COMMAND)) {
						return true;
					}
				}
			}
			return false;
		} else if ("labLearnOrchestra".equals(action)) {
			FactionAPI faction = Global.getSector().getFaction("dpl_phase_lab");
			String id = "dpl_orchestra";
			faction.clearShipRoleCache();
			if (!faction.knowsShip(id)) {
				faction.addKnownShip(id, true);
				faction.addUseWhenImportingShip(id);
				faction.getHullFrequency().put(id, 0.3f);
			} ;
		} else if ("giveOrchestra".equals(action)) {
			giveOrchestra(dialog, memoryMap);
		} else if ("labLearnOrchestraB".equals(action)) {
			FactionAPI faction = Global.getSector().getFaction("dpl_phase_lab");
			String id = "dpl_orchestraB";
			faction.clearShipRoleCache();
			if (!faction.knowsShip(id)) {
				faction.addKnownShip(id, true);
				faction.addUseWhenImportingShip(id);
				faction.getHullFrequency().put(id, 0.3f);
			} ;
		} else if ("giveOrchestraB".equals(action)) {
			giveOrchestraB(dialog, memoryMap);
		} else if ("labLearnOrchestraC".equals(action)) {
			FactionAPI faction = Global.getSector().getFaction("dpl_phase_lab");
			String id = "dpl_orchestraC";
			faction.clearShipRoleCache();
			if (!faction.knowsShip(id)) {
				faction.addKnownShip(id, true);
				faction.addUseWhenImportingShip(id);
				faction.getHullFrequency().put(id, 0.3f);
			} ;
		} else if ("giveOrchestraC".equals(action)) {
			giveOrchestraC(dialog, memoryMap);
		} else if ("unionStart".equals(action)) {
			if (dpl_ProdUnionScript.get() == null) new dpl_ProdUnionScript();
	        return true;
		}
		return false;
	}

	protected void giveHELATransport(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_gate_transport_Hull").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveSaxophone(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_saxophone_Hull").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void givePiano(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_piano_Hull").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveSilence(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_silence_Hull").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveDirge(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_dirge_Hull").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}

	protected void giveLyre(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
	
	ShipVariantAPI v = Global.getSettings().getVariant("dpl_lyre_Hull").clone();
	v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
	FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
	Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveOrchestra(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_orchestra_blank").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.getVariant().addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveOrchestraB(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_orchestraB_blank").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.getVariant().addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveOrchestraC(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_orchestraC_blank").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.getVariant().addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveSacraMachina(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_sacra_machina_blank").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.getVariant().addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		member.setShipName("DPLS Sacra Machina");
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
	}
	
	protected void giveHydraulophone(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		
		ShipVariantAPI v = Global.getSettings().getVariant("dpl_hydraulophone_Hull").clone();
		v.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName("DPLS Vanguard");
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
		AddShip.addShipGainText(member, dialog.getTextPanel());
		
		ShipVariantAPI v1 = Global.getSettings().getVariant("dpl_hydraulophone_Hull").clone();
		v1.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		FleetMemberAPI member1 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v1);
		member1.setShipName("DPLS Talent");
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member1);
		AddShip.addShipGainText(member1, dialog.getTextPanel());
	}
	
	protected void loseLargestIndustrial(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
		MarketAPI theMarket = null;
		int theSize = 0;
		for (MarketAPI market: Misc.getPlayerMarkets(false)) {
			if (market.getSize() >= 4) {
				for (Industry ind : market.getIndustries()) {
					if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
						if (market.getSize() >= theSize) {
							theSize = market.getSize();
							theMarket = market;
						}
					}
				}
			}
		}
		if (theMarket != null) {
			FactionAPI dpl_persean_imperium = Global.getSector().getFaction("dpl_persean_imperium");
			Set<SectorEntityToken> linkedEntities = theMarket.getConnectedEntities();
	        for (SectorEntityToken entity : linkedEntities) {
	        	entity.setFaction("dpl_persean_imperium");
	        }
	        //Change people to the new faction
	        final List<String> POSTS_TO_CHANGE_ON_CAPTURE = Arrays.asList(new String[]{
	        		Ranks.POST_BASE_COMMANDER,
	                Ranks.POST_OUTPOST_COMMANDER,
	                Ranks.POST_STATION_COMMANDER,
	                Ranks.POST_PORTMASTER,
	                Ranks.POST_SUPPLY_OFFICER,
	                Ranks.POST_ADMINISTRATOR
	        });
	        List<PersonAPI> people = theMarket.getPeopleCopy();
	        for (PersonAPI person : people) {
	        	if (POSTS_TO_CHANGE_ON_CAPTURE.contains(person.getPostId())) person.setFaction("dpl_persean_imperium");
	        }
	        
	        PersonAPI admin = theMarket.getAdmin();
	        if (admin != null && admin.equals(Global.getSector().getPlayerPerson())) {
	        	PersonAPI newAdmin = Global.getFactory().createPerson();
	        	newAdmin.setFaction("dpl_persean_imperium");
	        	theMarket.setAdmin(newAdmin);
	        }
	        
	        //set market to the new owner
	        theMarket.setFactionId("dpl_persean_imperium");
	        theMarket.setPlayerOwned(false);
	        
	        theMarket.addSubmarket(Submarkets.SUBMARKET_OPEN);
	        theMarket.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo();    // force cargo to generate if needed; fixes military submarket crash
	        
	        theMarket.addSubmarket(Submarkets.SUBMARKET_BLACK);
	        theMarket.getSubmarket(Submarkets.SUBMARKET_BLACK).getCargo();    // force cargo to generate if needed; fixes military submarket crash
	        
	        //set submarkets to the new owner
	        List<SubmarketAPI> submarkets = theMarket.getSubmarketsCopy();         
	        for (SubmarketAPI submarket : submarkets)
	        {
	        	String submarketId = submarket.getSpecId();             
	        	if (submarket.getPlugin().isFreeTransfer()) continue;
	            if (!submarket.getPlugin().isParticipatesInEconomy()) continue;
	            // reset smuggling suspicion
	            if (submarketId.equals(Submarkets.SUBMARKET_BLACK)) {  
	            	PlayerTradeDataForSubmarket tradeData = SharedData.getData().getPlayerActivityTracker().getPlayerTradeData(submarket);  
	            	tradeData.setTotalPlayerTradeValue(0);
	            	continue;
	            }  
	            submarket.setFaction(dpl_persean_imperium);
	         }
	        // transfer defense station
	        if (Misc.getStationFleet(theMarket) != null)
	        {
	            Misc.getStationFleet(theMarket).setFaction("dpl_persean_imperium", true);
	        }
	        if (Misc.getStationBaseFleet(theMarket) != null)
	        {
	            Misc.getStationBaseFleet(theMarket).setFaction("dpl_persean_imperium", true);
	        }

	        // don't lock player out of freshly captured market
	        if (!dpl_persean_imperium.isHostileTo(Factions.PLAYER)) {
	        	theMarket.getMemoryWithoutUpdate().unset("$playerHostileTimeout");
	        }
	        
	     // player: has to pay for storage unlock
	     SubmarketAPI storage = theMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE);
	     if (storage != null)
	     {
	         StoragePlugin plugin = (StoragePlugin)theMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin();
	         if (plugin != null)
	             plugin.setPlayerPaidToUnlock(false);
	         }
	     //Make sure conditions are good
	     theMarket.reapplyConditions();
		 }
	}
	
}
