package data.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetPersonHidden;

import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialWelcomeDialogPluginImpl;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.Faction;

import data.scripts.campaign.dpl_campaign_relations_plugin;
import data.scripts.campaign.dpl_crisis_plugin;
import data.scripts.campaign.dpl_exploratory_fleet_plugin;
import data.scripts.campaign.dpl_sat_bomb_plugin;
import data.scripts.campaign.dpl_system_defense_plugin;
import data.scripts.campaign.dpl_RuleBasedInteractionPlugin;
import data.scripts.world.systems.dpl_acheron;
import data.scripts.world.systems.dpl_capella;
import data.scripts.world.systems.dpl_helheim;
import data.scripts.world.systems.dpl_proving_ground;
import data.scripts.world.systems.dpl_voidbreak;

 
public class dpl_phase_labModPlugin extends BaseModPlugin {
	public static String ELIZA_LOVELACE = "eliza_lovelace";
	public static String ELIZA_LOVELACE_PP = "eliza_lovelace_PP";
	public static String NELSON_BONAPARTE = "nelson_bonaparte";
	public static String BANACH_SALAZAR = "banach_salazar";
	public static String VLADIMIR_VASSILIEV = "vladimir_vassiliev";
	public static String ELLY_LOVELACE = "elly_lovelace";
	public static String ROSS_HIGGS = "ross_higgs";
	public static String CAMILLA_WIENER = "camilla_wiener";
	public static String FARADAY_HOLMES = "faraday_holmes";
	public static String WEIERSTRASS_GOLDBERG = "weierstrass_goldberg";
	public static String CANTOR_ALEPH_NULL = "cantor_aleph_null";
	public static String RUSTY_HOOK = "rusty_hook";
	public static String GREATER_KAPTEYN = "kapteyn_greater";
	public static String ARES_ARCTURUS = "ares_arcturus";
	
	public static void newGenerate(SectorAPI sector) {
		ProcgenUsedNames.notifyUsed("dpl_phase_lab");
		ProcgenUsedNames.notifyUsed("dpl_persean_imperium");
	}
	
	@Override
	public void onNewGame() {SharedData.getData().getPersonBountyEventData().addParticipatingFaction("dpl_phase_lab");
		SectorAPI sector = Global.getSector();
		boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
		StarSystemAPI corvus = sector.getStarSystem("Corvus");
		Global.getSector().addScript(new dpl_exploratory_fleet_plugin());
		Global.getSector().addScript(new dpl_system_defense_plugin());
		if(!haveNexerelin || corvus != null) {
			Global.getSector().addScript(new dpl_campaign_relations_plugin());
			Global.getSector().getListenerManager().addListener(new dpl_crisis_plugin());
			Global.getSector().getListenerManager().addListener(new dpl_sat_bomb_plugin());
			new dpl_proving_ground().generate(sector);
			new dpl_voidbreak().generate(sector);
			new dpl_capella().generate(sector);
			new dpl_helheim().generate(sector);
			//new dpl_acheron().generate(sector);
		} else {
			new dpl_voidbreak().generate(sector);
		}
		
		FactionAPI player = sector.getFaction("player");
		FactionAPI hegemony = sector.getFaction("hegemony");
		FactionAPI tritachyon = sector.getFaction("tritachyon");
		FactionAPI pirates = sector.getFaction("pirates");
		FactionAPI church = sector.getFaction("luddic_church");
		FactionAPI path = sector.getFaction("luddic_path");
		FactionAPI indep = sector.getFaction("independent");
		FactionAPI diktat = sector.getFaction("sindrian_diktat");
		FactionAPI persean = sector.getFaction("persean");
		FactionAPI dpl_phase_lab = sector.getFaction("dpl_phase_lab");
		FactionAPI dpl_phase_lab_old = sector.getFaction("dpl_phase_lab_old");
		FactionAPI dpl_persean_imperium = sector.getFaction("dpl_persean_imperium");
		dpl_phase_lab.setRelationship(hegemony.getId(), 0.3f);
		dpl_phase_lab.setRelationship(persean.getId(), 0.1f);
		dpl_phase_lab.setRelationship(church.getId(), 0.0f);
		dpl_phase_lab.setRelationship(tritachyon.getId(), -0.5f);
		dpl_phase_lab.setRelationship(pirates.getId(), -0.5f);
		dpl_phase_lab.setRelationship(path.getId(), -0.5f);
		
		player.setRelationship(dpl_persean_imperium.getId(), -0.5f);
		player.setRelationship(dpl_phase_lab_old.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(hegemony.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(tritachyon.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(church.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(persean.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(diktat.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(path.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(pirates.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(indep.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(dpl_phase_lab.getId(), -0.5f);
		dpl_persean_imperium.setRelationship(player.getId(), -0.5f);
	}

    @Override
    public void onNewGameAfterEconomyLoad() {
    	
    	//Generating global tags for all special ships.
    	Global.getSettings().resetCached();
        if (Global.getSettings().getMissionScore("dpl_onemanwithcourage") > 0) {
        	Global.getSector().getMemoryWithoutUpdate().set("$dpl_test_pilot", true);
        }
        if (Global.getSettings().getMissionScore("dpl_mothtoaflame") > 0) {
            Global.getSector().getMemoryWithoutUpdate().set("$dpl_exp_drone", true);
        }
        if (Global.getSettings().getMissionScore("dpl_thinredline") > 0) {
            Global.getSector().getMemoryWithoutUpdate().set("$dpl_dirge_access", true);
        }
        if (Global.getSettings().getMissionScore("dpl_ittakesone") > 0) {
            Global.getSector().getMemoryWithoutUpdate().set("$dpl_lyre_access", true);
        }
        if (Global.getSettings().getMissionScore("dpl_forscience") > 0) {
            Global.getSector().getMemoryWithoutUpdate().set("$dpl_get_sacra_machina", true);
        }
        if (!Global.getSettings().getBoolean("dpl_Do_Not_Protect_Important_Planets")) {
        	//Generating Deciv Protections for various story critical markets.
            MarketAPI sindria = Global.getSector().getEconomy().getMarket("sindria");
            if (sindria != null) {
            	Misc.makeStoryCritical(sindria, "dpl_JustAsPlanned");
            	Industry sindrian_fuel = sindria.getIndustry("fuelprod");
            	sindrian_fuel.setImproved(true);
            }
            
            MarketAPI kantasDen = Global.getSector().getEconomy().getMarket("kantas_den");
            if (kantasDen != null) {
            	Misc.makeStoryCritical(kantasDen, "dpl_LightMeetsDark");
            }
            
            MarketAPI station_kapteyn = Global.getSector().getEconomy().getMarket("station_kapteyn");
            if (station_kapteyn != null) {
            	Misc.makeStoryCritical(station_kapteyn, "dpl_FirstStrike");
            }
            
            MarketAPI eochu_bres = Global.getSector().getEconomy().getMarket("eochu_bres");
            MarketAPI port_tse = Global.getSector().getEconomy().getMarket("port_tse");
            if (eochu_bres != null && port_tse != null) {
            	Misc.makeStoryCritical(eochu_bres, "dpl_PeaceThroughStrength");
            	Misc.makeStoryCritical(port_tse, "dpl_PeaceThroughStrength");
            }
            
            MarketAPI kazeron = Global.getSector().getEconomy().getMarket("kazeron");
            if (kazeron != null) {
            	Misc.makeStoryCritical(kazeron, "dpl_SwimmingWithSharks");
            }
            
            MarketAPI chalcedon = Global.getSector().getEconomy().getMarket("chalcedon");
            if (chalcedon != null) {
            	Misc.makeStoryCritical(chalcedon, "dpl_BeInDeepWater");
            }
            
            MarketAPI chicomoztoc = Global.getSector().getEconomy().getMarket("chicomoztoc");
            if (chicomoztoc != null) {
            	Misc.makeStoryCritical(chicomoztoc, "dpl_Justice");
            }
        }
        
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        
    	MarketAPI market1 = Global.getSector().getEconomy().getMarket("dpl_security");
    	if (market1 != null) {
    		//Protects it from deciv.
    		Misc.makeStoryCritical(market1, "camilla_wiener");
    		
    		//Generating Camilla Wiener.       	
            PersonAPI camilla_wiener = Global.getFactory().createPerson();
            camilla_wiener.setId(CAMILLA_WIENER);
            camilla_wiener.setFaction("dpl_phase_lab");
            camilla_wiener.setGender(FullName.Gender.FEMALE);
            camilla_wiener.setImportance(PersonImportance.LOW);
            camilla_wiener.setPostId("technician");
            camilla_wiener.setRankId("spaceEnsign");
            camilla_wiener.getName().setFirst("Camilla");
            camilla_wiener.getName().setLast("Wiener");
            camilla_wiener.setPortraitSprite(Global.getSettings().getSpriteName("characters", "camilla_wiener"));
            market1.getCommDirectory().addPerson(camilla_wiener,0);
            market1.addPerson(camilla_wiener);
			ip.addPerson(camilla_wiener);
			camilla_wiener.getMarket().getCommDirectory().getEntryForPerson(camilla_wiener.getId()).setHidden(true);
    		Industry highcommand1 = market1.getIndustry("highcommand");
			highcommand1.setImproved(true);
    	}

    	MarketAPI market2 = Global.getSector().getEconomy().getMarket("dpl_factory");
    	if (market2 != null) {
    		//Protects it from deciv.
    		Misc.makeStoryCritical(market2, "ross_higgs");
    		
    		//Generating Ross Higgs.       	
            PersonAPI ross_higgs = Global.getFactory().createPerson();
            ross_higgs.setId(ROSS_HIGGS);
            ross_higgs.setFaction("dpl_phase_lab");
            ross_higgs.setGender(FullName.Gender.FEMALE);
            ross_higgs.setImportance(PersonImportance.VERY_HIGH);
            ross_higgs.setPostId("engineeringChair");
            ross_higgs.setRankId("spaceMarshal");
            ross_higgs.getName().setFirst("Ross");
            ross_higgs.getName().setLast("Higgs");
            ross_higgs.setPortraitSprite(Global.getSettings().getSpriteName("characters", "ross_higgs"));
            market2.getCommDirectory().addPerson(ross_higgs,0);
            market2.addPerson(ross_higgs);
			ip.addPerson(ross_higgs);
			ross_higgs.getMarket().getCommDirectory().getEntryForPerson(ross_higgs.getId()).setHidden(true);
			Industry orbitalworks2 = market2.getIndustry("orbitalworks");
			orbitalworks2.setImproved(true);
			Industry militarybase2 = market2.getIndustry("militarybase");
			militarybase2.setImproved(true);
    	}
    	
        MarketAPI market3 = Global.getSector().getEconomy().getMarket("dpl_research_site_v");
        if (market3 != null) {
        	//Protects it from deciv.
    		Misc.makeStoryCritical(market3, "eliza_lovelace");
        	
        	//Generating Eliza Storyline boolean values.
        	market3.getMemoryWithoutUpdate().set("$dpl_metEliza", false);
        	market3.getMemoryWithoutUpdate().set("$dpl_asked_remnant", false);
        	market3.getMemoryWithoutUpdate().set("$dpl_asked_leader", false);
        	market3.getMemoryWithoutUpdate().set("$dpl_asked_age", false);
        	market3.getMemoryWithoutUpdate().set("$dpl_asked_person", false);
        	market3.getMemoryWithoutUpdate().set("$dpl_asked_well", false);
        	market3.getMemoryWithoutUpdate().set("$dpl_asked_luddic", false);
        	market3.getMemoryWithoutUpdate().set("$dpl_asked_colony", false);
        	
        	//Generating Eliza Lovelace.       	
            PersonAPI eliza_lovelace = Global.getFactory().createPerson();
            eliza_lovelace.setId(ELIZA_LOVELACE);
            eliza_lovelace.setFaction("dpl_phase_lab");
            eliza_lovelace.setGender(FullName.Gender.FEMALE);
            eliza_lovelace.setImportance(PersonImportance.VERY_HIGH);
            eliza_lovelace.setPostId(Ranks.POST_FACTION_LEADER);
            eliza_lovelace.setRankId(Ranks.FACTION_LEADER);
            eliza_lovelace.getName().setFirst("Eliza");
            eliza_lovelace.getName().setLast("Lovelace");
            eliza_lovelace.setPortraitSprite(Global.getSettings().getSpriteName("characters", "eliza_lovelace"));
            market3.getCommDirectory().addPerson(eliza_lovelace, 0);
			market3.addPerson(eliza_lovelace);
			ip.addPerson(eliza_lovelace);
			
			//Generating Banach Salazar.       	
            PersonAPI banach_salazar = Global.getFactory().createPerson();
            banach_salazar.setId(BANACH_SALAZAR);
            banach_salazar.setFaction("dpl_phase_lab");
            banach_salazar.setGender(FullName.Gender.MALE);
            banach_salazar.setPostId("seniorCommander");
            banach_salazar.setRankId("spaceMarshal");
            banach_salazar.getName().setFirst("Banach");
            banach_salazar.getName().setLast("Salazar");
            banach_salazar.setPortraitSprite(Global.getSettings().getSpriteName("characters", "banach_salazar"));
            market3.getCommDirectory().addPerson(banach_salazar);
			market3.addPerson(banach_salazar);
			ip.addPerson(banach_salazar);
			banach_salazar.getMarket().getCommDirectory().getEntryForPerson(banach_salazar.getId()).setHidden(true);
			
			banach_salazar.setPersonality(Personalities.STEADY);
			banach_salazar.getStats().setLevel(9);
			banach_salazar.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			banach_salazar.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
			banach_salazar.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
			banach_salazar.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
			banach_salazar.getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
			banach_salazar.getStats().setSkillLevel(Skills.FIGHTER_UPLINK, 1);
			banach_salazar.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
			banach_salazar.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
			banach_salazar.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
			banach_salazar.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
			banach_salazar.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			banach_salazar.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
			
			Industry militarybase3 = market3.getIndustry("militarybase");
			militarybase3.setImproved(true);
        } else {
        	MarketAPI largestMarket = null;
        	int size = 0;
        	List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
    		for (MarketAPI market : allMarkets) {
    			if (market.getFaction().equals(Global.getSector().getFaction("dpl_phase_lab"))) {
    				if (market.getSize() >= size) {
    					largestMarket = market;
    					size = market.getSize();
    				}
    			}
    		}
    		
    		//Generating Eliza Lovelace.       	
            PersonAPI eliza_lovelace = Global.getFactory().createPerson();
            eliza_lovelace.setId(ELIZA_LOVELACE);
            eliza_lovelace.setFaction("dpl_phase_lab");
            eliza_lovelace.setGender(FullName.Gender.FEMALE);
            eliza_lovelace.setImportance(PersonImportance.VERY_HIGH);
            eliza_lovelace.setPostId(Ranks.POST_FACTION_LEADER);
            eliza_lovelace.setRankId(Ranks.FACTION_LEADER);
            eliza_lovelace.getName().setFirst("Eliza");
            eliza_lovelace.getName().setLast("Lovelace");
            eliza_lovelace.setPortraitSprite(Global.getSettings().getSpriteName("characters", "eliza_lovelace"));
            largestMarket.getCommDirectory().addPerson(eliza_lovelace, 0);
            largestMarket.addPerson(eliza_lovelace);
			ip.addPerson(eliza_lovelace);
        }
        
    	MarketAPI market4 = Global.getSector().getEconomy().getMarket("dpl_research_site_v_moon");
    	if (market4 != null) {
    		Industry lightindustry4 = market4.getIndustry("lightindustry");
    		lightindustry4.setImproved(true);
    	}
    	
    	//Generating Faraday Holmes
    	PersonAPI faraday_holmes = Global.getFactory().createPerson();
	    	faraday_holmes.setId(FARADAY_HOLMES);
	    	faraday_holmes.setFaction("dpl_phase_lab");
	    	faraday_holmes.setGender(FullName.Gender.MALE);
	    	faraday_holmes.setPostId("committeeMember");
	    	faraday_holmes.setRankId("spaceMarshal");
	    	faraday_holmes.getName().setFirst("Faraday");
	    	faraday_holmes.getName().setLast("Holmes");
	    	faraday_holmes.setPortraitSprite(Global.getSettings().getSpriteName("characters", "faraday_holmes"));
			ip.addPerson(faraday_holmes);
			
			faraday_holmes.setPersonality(Personalities.AGGRESSIVE);
			faraday_holmes.getStats().setLevel(10);
			faraday_holmes.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
			faraday_holmes.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			faraday_holmes.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
			faraday_holmes.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
			faraday_holmes.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
			faraday_holmes.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
			faraday_holmes.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			faraday_holmes.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
			faraday_holmes.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
			faraday_holmes.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
		PersonAPI omega_holmes_noname = Global.getFactory().createPerson();
			omega_holmes_noname.setId("omega_holmes_noname");
			omega_holmes_noname.setFaction(Factions.OMEGA);
			omega_holmes_noname.getName().setFirst("Unknown");
			omega_holmes_noname.getName().setLast("AI");
			omega_holmes_noname.setPortraitSprite(Global.getSettings().getSpriteName("characters", "omega_holmes"));
			ip.addPerson(omega_holmes_noname);
		PersonAPI omega_holmes = Global.getFactory().createPerson();
			omega_holmes.setId("omega_holmes");
			omega_holmes.setFaction(Factions.OMEGA);
			omega_holmes.getName().setFirst("Faraday");
			omega_holmes.getName().setLast("Holmes");
			omega_holmes.setPortraitSprite(Global.getSettings().getSpriteName("characters", "omega_holmes"));
			ip.addPerson(omega_holmes);
			
		PersonAPI weierstrass_goldberg = Global.getFactory().createPerson();
			weierstrass_goldberg.setId(WEIERSTRASS_GOLDBERG);
			weierstrass_goldberg.setFaction("dpl_phase_lab");
			weierstrass_goldberg.setGender(FullName.Gender.MALE);
			weierstrass_goldberg.setPostId("committeeMember");
			weierstrass_goldberg.setRankId("spaceMarshal");
			weierstrass_goldberg.getName().setFirst("Weierstrass");
			weierstrass_goldberg.getName().setLast("Goldberg");
			weierstrass_goldberg.setPortraitSprite(Global.getSettings().getSpriteName("characters", "weierstrass_goldberg"));
			ip.addPerson(weierstrass_goldberg);
			
		PersonAPI cantor_aleph_null = Global.getFactory().createPerson();
			cantor_aleph_null.setId(CANTOR_ALEPH_NULL);
			cantor_aleph_null.setFaction("dpl_phase_lab");
			cantor_aleph_null.setGender(FullName.Gender.MALE);
			cantor_aleph_null.setPostId("fleetCommander");
			cantor_aleph_null.setRankId("spaceAdmiral");
			cantor_aleph_null.getName().setFirst("Cantor");
			cantor_aleph_null.getName().setLast("Aleph Null");
			cantor_aleph_null.setPortraitSprite(Global.getSettings().getSpriteName("characters", "cantor_aleph_null"));
			ip.addPerson(cantor_aleph_null);
		
    	//Generating Nelson Bonaparte
    	PersonAPI nelson_bonaparte = Global.getFactory().createPerson();
            nelson_bonaparte.setId(NELSON_BONAPARTE);
            nelson_bonaparte.setFaction("dpl_persean_imperium");
            nelson_bonaparte.setGender(FullName.Gender.MALE);
            nelson_bonaparte.setPostId(Ranks.POST_FACTION_LEADER);
            nelson_bonaparte.setRankId(Ranks.FACTION_LEADER);
            nelson_bonaparte.getName().setFirst("Nelson");
            nelson_bonaparte.getName().setLast("Bonaparte");
            nelson_bonaparte.setPortraitSprite(Global.getSettings().getSpriteName("characters", "nelson_bonaparte"));
			ip.addPerson(nelson_bonaparte);
			
			nelson_bonaparte.setPersonality(Personalities.RECKLESS);
			nelson_bonaparte.getStats().setLevel(10);
			nelson_bonaparte.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
			nelson_bonaparte.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			nelson_bonaparte.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
			nelson_bonaparte.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
			nelson_bonaparte.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
			nelson_bonaparte.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
			nelson_bonaparte.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
			nelson_bonaparte.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
			nelson_bonaparte.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
			nelson_bonaparte.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
			nelson_bonaparte.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
			
		//Generating the Pirate
		PersonAPI ares_arcturus = Global.getFactory().createPerson();
			ares_arcturus.setId(ARES_ARCTURUS);
			ares_arcturus.setFaction(Factions.INDEPENDENT);
			ares_arcturus.setGender(FullName.Gender.MALE);
			ares_arcturus.setPostId("pilgrim");
			ares_arcturus.setRankId("brother");
			ares_arcturus.getName().setFirst("Ares");
			ares_arcturus.getName().setLast("Arcturus");
			ares_arcturus.setPortraitSprite(Global.getSettings().getSpriteName("characters", "portrait_corporate08"));
			ip.addPerson(ares_arcturus);
					
			ares_arcturus.setPersonality(Personalities.RECKLESS);
			ares_arcturus.getStats().setLevel(7);
			ares_arcturus.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
			ares_arcturus.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			ares_arcturus.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
			ares_arcturus.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
			ares_arcturus.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
			ares_arcturus.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
			ares_arcturus.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
			ares_arcturus.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
			
		//Generating the Pirate
	    PersonAPI rusty_hook = Global.getFactory().createPerson();
	    	rusty_hook.setId(RUSTY_HOOK);
	    	rusty_hook.setFaction(Factions.PIRATES);
	    	rusty_hook.setGender(FullName.Gender.MALE);
	    	rusty_hook.setPostId("fleetCommander");
	    	rusty_hook.setRankId("spaceCommander");
	    	rusty_hook.getName().setFirst("Rusty");
	   		rusty_hook.getName().setLast("Hook");
	   		rusty_hook.setPortraitSprite(Global.getSettings().getSpriteName("characters", "portrait_pirate03"));
			ip.addPerson(rusty_hook);
				
			rusty_hook.setPersonality(Personalities.RECKLESS);
			rusty_hook.getStats().setLevel(7);
			rusty_hook.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
			rusty_hook.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			rusty_hook.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
			rusty_hook.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
			rusty_hook.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
			rusty_hook.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
			rusty_hook.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
			rusty_hook.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
			
		//Generating Greater Kapteyn
		PersonAPI kapteyn_greater = Global.getFactory().createPerson();
			kapteyn_greater.setId(GREATER_KAPTEYN);
			kapteyn_greater.setFaction(Factions.PIRATES);
			kapteyn_greater.setGender(FullName.Gender.MALE);
	        kapteyn_greater.setRankId(Ranks.SPACE_COMMANDER);
	        kapteyn_greater.setPostId(Ranks.POST_FLEET_COMMANDER);
	        kapteyn_greater.getName().setFirst("Greater");
	        kapteyn_greater.getName().setLast("Kapteyn");
	        kapteyn_greater.setPortraitSprite(Global.getSettings().getSpriteName("characters", "portrait41"));
	        ip.addPerson(kapteyn_greater);
	        
	        kapteyn_greater.setPersonality(Personalities.AGGRESSIVE);
	        kapteyn_greater.getStats().setLevel(8);
	        kapteyn_greater.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
	        kapteyn_greater.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
	        kapteyn_greater.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
	        kapteyn_greater.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
	        kapteyn_greater.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
	        kapteyn_greater.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
	        kapteyn_greater.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
	        kapteyn_greater.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
			
	    //Generating Vladimir
	    PersonAPI vladimir_vassiliev = Global.getFactory().createPerson();
	    	vladimir_vassiliev.setId(VLADIMIR_VASSILIEV);
	    	vladimir_vassiliev.setFaction("dpl_phase_lab");
	    	vladimir_vassiliev.setGender(FullName.Gender.MALE);
	    	vladimir_vassiliev.setPostId(Ranks.POST_FACTION_LEADER);
	    	vladimir_vassiliev.setRankId(Ranks.FACTION_LEADER);
	    	vladimir_vassiliev.getName().setFirst("Vladimir");
	    	vladimir_vassiliev.getName().setLast("Vassiliev");
	    	vladimir_vassiliev.setPortraitSprite(Global.getSettings().getSpriteName("characters", "vladimir_vassiliev"));
	        ip.addPerson(vladimir_vassiliev);
				
	        vladimir_vassiliev.setPersonality(Personalities.AGGRESSIVE);
	        vladimir_vassiliev.getStats().setLevel(14);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.CYBERNETIC_AUGMENTATION, 1);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
	        vladimir_vassiliev.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
	        
	    //Generating Elly Lovelace
	    PersonAPI elly_lovelace = Global.getFactory().createPerson();
	    	elly_lovelace.setId(ELLY_LOVELACE);
	    	elly_lovelace.setFaction("dpl_phase_lab");
	    	elly_lovelace.setGender(FullName.Gender.FEMALE);
	    	elly_lovelace.setPostId(Ranks.SPECIAL_AGENT);
	    	elly_lovelace.setRankId(Ranks.POST_SPECIAL_AGENT);
	    	elly_lovelace.getName().setFirst("Elly");
	    	elly_lovelace.getName().setLast("Lovelace");
	    	elly_lovelace.setPortraitSprite(Global.getSettings().getSpriteName("characters", "elly_lovelace"));
	        ip.addPerson(elly_lovelace);
	        Misc.setMercenary(elly_lovelace, true);
				
	        elly_lovelace.setPersonality(Personalities.STEADY);
	        elly_lovelace.getStats().setLevel(10);
	        elly_lovelace.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
	        elly_lovelace.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
	        
	    //Generating Eliza during Project Prometheus
	    PersonAPI eliza_lovelace_PP = Global.getFactory().createPerson();
	    	eliza_lovelace_PP.setId(ELIZA_LOVELACE_PP);
	    	eliza_lovelace_PP.setFaction("dpl_phase_lab");
	    	eliza_lovelace_PP.setGender(FullName.Gender.FEMALE);
	    	eliza_lovelace_PP.setPostId("committeeMember");
	    	eliza_lovelace_PP.setRankId(Ranks.POST_SPECIAL_AGENT);
	    	eliza_lovelace_PP.getName().setFirst("Eliza");
	    	eliza_lovelace_PP.getName().setLast("Lovelace");
	    	eliza_lovelace_PP.setPortraitSprite(Global.getSettings().getSpriteName("characters", "eliza_lovelace_lab_coat"));
	        ip.addPerson(eliza_lovelace_PP);
    }
    
    @Override
    public void onGameLoad(boolean newGame) {
    	dpl_crisis_plugin.addDPLColonyCrisis();
    	Global.getSector().registerPlugin(new dpl_RuleBasedInteractionPlugin());
    	//for (FactionAPI faction: Global.getSector().getAllFactions()) {
    	//	if(faction.knowsWeapon("dpl_nullifier")) {faction.removeKnownWeapon("dpl_nullifier");}
    	//	if(faction.knowsWeapon("dpl_nullifier_gun")) {faction.removeKnownWeapon("dpl_nullifier_gun");}
    	//	if(faction.knowsWeapon("dpl_nullifier_lg")) {faction.removeKnownWeapon("dpl_nullifier_lg");}
    	//	if(faction.knowsWeapon("dpl_null_swarm")) {faction.removeKnownWeapon("dpl_null_swarm");}
    	//}
    }

}