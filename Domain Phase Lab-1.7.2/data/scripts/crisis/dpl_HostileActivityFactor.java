package data.scripts.crisis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.*;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction.FGRaidType;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel.FGIEventListener;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI.GenericRaidParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission.FleetStyle;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.ComplicationRepImpact;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class dpl_HostileActivityFactor extends BaseHostileActivityFactor implements FGIEventListener {
    public static String DEFEATED_DPL_ATTACK = "$dpl_defeatedDPLAttack";

    public static void setPlayerDefeatedDPL() {
		Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_DPL_ATTACK, true);
	}
    
    public dpl_HostileActivityFactor(HostileActivityEventIntel intel) {
        super(intel);
        Global.getSector().getListenerManager().addListener(this);
    }

    @Override
    public String getProgressStr(BaseEventIntel intel) {
        return "";
    }

    @Override
    public int getProgress(BaseEventIntel intel) {
    	boolean canHaveProgress = checkFactionExists("dpl_phase_lab", true);
    	boolean hasSecurityDepartment = (Global.getSector().getEconomy().getMarket("dpl_security") != null);
    	if (canHaveProgress && hasSecurityDepartment) {
    		return super.getProgress(intel);
    	}
        return 0;
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Phase Lab";
    }

    @Override
    public String getNameForThreatList(boolean first) {
        return "Phase Lab";
    }

    @Override
    public Color getDescColor(BaseEventIntel intel) {
        return getProgress(intel) <= 0 ? Misc.getGrayColor() : Global.getSector().getFaction("dpl_phase_lab").getBaseUIColor();
    }

    @Override
    public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				float opad = 10f;
				
				tooltip.addPara("The Phase Lab is interested in the industrial potential of your colonies "
						+ "and some individuals are taking extremal actions due to such interests.", 0f);
				tooltip.addPara("Those mysterious individuals hired some independent mercenaries "
						+ "to force you giving up your colonies by disrupting your production.", opad);
			}
		};
	}

    @Override
    public boolean shouldShow(BaseEventIntel intel) {
        return getProgress(intel) > 0;
    }

    @Override
    public int getMaxNumFleets(StarSystemAPI system) {
        return Global.getSettings().getInt("dpl_MaxFleets");
    }

    @Override
    public float getSpawnInHyperProbability(StarSystemAPI system) {
        return 0f;
    }

    @Override
    public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
    	float f = intel.getMarketPresenceFactor(system);
		
		// even if magnitude is not factored in, if it's 0 fleets won't spawn
		// and its value affects the likelihood of merc fleets spawning
		//getEffectMagnitude(system);
		
		int difficulty = 8;
		
		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		Vector2f loc = system.getLocation();
		String factionId = "dpl_phase_lab";
		if (random.nextFloat() < 0.5f) {
			factionId = Factions.MERCENARY;
		}
		
		m.createQualityFleet(difficulty, factionId, loc);
		
		m.triggerSetFleetQuality(FleetQuality.SMOD_1);
		
		m.triggerSetFleetFaction(Factions.INDEPENDENT);
		m.triggerSetFleetType(FleetTypes.RAIDER);
		m.triggerSetFleetDoctrineOther(3, 0);
		
		m.triggerSetPirateFleet();
		//m.triggerMakeHostile();
		m.triggerMakeHostileToAllTradeFleets();
		m.triggerMakeEveryoneJoinBattleAgainst();
		
		m.triggerMakeNonHostileToFaction(Factions.PIRATES);
		m.triggerMakeNoRepImpact();
		m.triggerFleetAllowLongPursuit();
		
		m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
		m.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
 		m.triggerFleetAddCommanderSkill(Skills.FLUX_REGULATION, 1);
		m.triggerFleetAddCommanderSkill(Skills.PHASE_CORPS, 1);
//		m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
		
		m.triggerSetFleetFlag("$dpl_crisis_merc");
		m.triggerFleetSetName("Mysterious Raiders");
		
		int tugs = 0;
		if (Factions.MERCENARY.equals(factionId)) {
			tugs = random.nextInt(3);
		}
		
		m.triggerFleetMakeFaster(true, tugs, true);
		
		CampaignFleetAPI fleet = m.createFleet();

		return fleet;
    }

    @Override
    public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
			 ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
    	Color c = Global.getSector().getFaction("dpl_phase_lab").getBaseUIColor();
    	info.addPara("Impending Phase Lab mercenary attack", initPad, tc, c, "Phase Lab");
    }

    @Override
    public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info,
    		ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
    	info.addPara("Phase Lab mercenary attack averted", tc, initPad);
    }

    @Override
    public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage, TooltipMakerAPI info) {
		float small = 0f;
		float opad = 10f;
		
		small = 8f;

		Color c = Global.getSector().getFaction("dpl_phase_lab").getBaseUIColor();
		
		Color h = Misc.getHighlightColor();
		info.addPara("You've received intel that some Phase Lab individuals are allocating funds to hire and "
				+ "equip a mercenary company to raid and disrupt your industrial base.",
				small, Misc.getNegativeHighlightColor(), "raid and disrupt your industrial base");

		info.addPara("These mercenaries just keep coming, and they would even attack the Phase Lab's own ships. "
				+ "So it's more important to figure out who is behind all these and collect evidence.", 
				//opad, tri, "Tri-Tachyon Corporation");
				opad, h, "who is behind all these");
		
		info.addPara("Finally you need to make a complaint at Research Site V planet, NOT station, and "
				+ "report the evidence you collected to stop the attacks.",
				//opad, tri, "Tri-Tachyon Corporation");
				opad, h, "report the evidence");
		
		info.addPara("Or if you know someone with high authority in the Phase Lab, you can visit that person,  "
				+ "and report the evidence you collected to stop the attacks.",
				//opad, tri, "Tri-Tachyon Corporation");
				opad, h, "someone with high authority");
		
		stage.beginResetReqList(info, true, "crisis", opad);
		info.addPara("You are invited to go to Lab Security Department planet, NOT station, for a negotiation.", 0f, c, "Lab Security Department");
		stage.endResetReqList(info, false, "crisis", -1, -1);
		
		addBorder(info, Global.getSector().getFaction("dpl_phase_lab").getBaseUIColor());
	}

    @Override
    public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
        return Global.getSector().getFaction("dpl_phase_lab").getCrest();
    }

    @Override
    public TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final EventStageData stage) {
		if (stage.id == Stage.HA_EVENT) {
			return getDefaultEventTooltip("Phase Lab mercenary attack", intel, stage);
		}
		return null;
	}

    @Override
    public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
    	if (dpl_DealtWithBadGuy()) return 0;
    	
    	if (dpl_DefeatedDPLAttack()) return 0;
    	
    	MarketAPI dpl_security = Global.getSector().getEconomy().getMarket("dpl_security");
    	if (dpl_security == null) {
    		return 0;
		}
    	
		if (stage.id == Stage.HA_EVENT) {
			
			StarSystemAPI target = findExpeditionTarget(intel, stage);
			if (target != null) {
				return 10f;
			}
		}
		return 0;
	}

    @Override
    public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
		HAERandomEventData data = new HAERandomEventData(this, stage);
		stage.rollData = data;
		intel.sendUpdateIfPlayerHasIntel(data, false);
	}
	
    @Override
	public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
		StarSystemAPI target = findExpeditionTarget(intel, stage);
		MarketAPI source = findExpeditionSource(intel, stage, target);
		
		if (source == null || target == null) {
			return false;
		}
	
		stage.rollData = null;
		return startMercenaryAttack(source, target, stage, intel, getRandomizedStageRandom(3));
	}
	
	
	public static StarSystemAPI findExpeditionTarget(HostileActivityEventIntel intel, EventStageData stage) {
		CountingMap<MarketAPI> scores = new CountingMap<MarketAPI>();
		for (MarketAPI market : Misc.getPlayerMarkets(false)) {
			int size = market.getSize();
			int weight = size;
			if (!Misc.isMilitary(market)) weight += size * 10;
			
			for (Industry ind : market.getIndustries()) {
        		if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY) && (ind.getSpecialItem() != null)) {
        			weight += size * 10;
        		} else if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
        			weight += size;
        		}
        	}
			
			scores.add(market, weight);
		}
		
		return scores.getLargest().getStarSystem();
	}
	
	public static MarketAPI findExpeditionSource(HostileActivityEventIntel intel, EventStageData stage, StarSystemAPI target) {
		CountingMap<MarketAPI> scores = new CountingMap<MarketAPI>();
		for (MarketAPI market : Misc.getFactionMarkets("dpl_phase_lab")) {
			int size = market.getSize();
			int weight = size;
			if (!Misc.isMilitary(market)) weight += size * 10;
			
			for (Industry ind : market.getIndustries()) {
        		if (ind.getSpec().hasTag(Industries.TAG_HEAVYINDUSTRY)) {
        			weight += size;
        		}
        	}
			
			scores.add(market, weight);
		}
		
		return scores.getLargest();
	}

    @Override
    public void reportFGIAborted(FleetGroupIntel intel) {
    	setPlayerDefeatedDPL();
    	Global.getSector().addScript(new DelayedActionScript(0.1f) { 
			@Override
			public void doAction() {
				MessageIntel msg = new MessageIntel();
				msg.addLine("Phase Lab Raid defeat!", Misc.getBasePlayerColor());
				msg.addLine(BaseIntelPlugin.BULLET + 
						"This may allow you to make better deals if you go to Lab Security Department for negotiation.");
				msg.setIcon(Global.getSector().getFaction("dpl_phase_lab").getCrest());
				msg.setSound(Sounds.REP_GAIN);
				Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.NOTHING);
			}
		});
    }

    @Override
    public void notifyFactorRemoved() {
        Global.getSector().getListenerManager().removeListener(this);
    }

    @Override
    public void notifyEventEnding() {
        notifyFactorRemoved();
    }

    @Override
    public void advance(float amount) {
		super.advance(amount);
		
		EventStageData stage = intel.getDataFor(Stage.HA_EVENT);
		if (stage != null && stage.rollData instanceof HAERandomEventData && 
				((HAERandomEventData)stage.rollData).factor == this) {
			if (dpl_DealtWithBadGuy() || dpl_DefeatedDPLAttack()) {
				intel.resetHA_EVENT();
			}		
		}
		if ((dpl_DealtWithBadGuy() || dpl_DefeatedDPLAttack()) && dpl_MercenaryAttack.get() != null) {
			dpl_MercenaryAttack.get().finish(false);
		}
		MarketAPI dpl_security = Global.getSector().getEconomy().getMarket("dpl_security");
        if (dpl_security == null) {
        	intel.resetHA_EVENT();
        	if ((dpl_security == null) && dpl_MercenaryAttack.get() != null) {
    			dpl_MercenaryAttack.get().finish(false);
    		}
        }
	}

    public static boolean dpl_DealtWithBadGuy() {
    	boolean DealtWithBadGuy = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_DealtWithBadGuy");
		return DealtWithBadGuy;
	}
    
    public boolean dpl_DefeatedDPLAttack() {
    	boolean DefeatedDPLAttack = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_defeatedDPLAttack");
		return DefeatedDPLAttack;
	}

    public boolean startMercenaryAttack(MarketAPI source, StarSystemAPI target, 
			EventStageData stage, HostileActivityEventIntel intel, Random random) {
    	if (dpl_DealtWithBadGuy() || dpl_DefeatedDPLAttack()) return false;
    	if (source == null || target == null) return false;
    	
    	MarketAPI dpl_security = Global.getSector().getEconomy().getMarket("dpl_security");
    	if (dpl_security == null) {
    		return false;
		}

    	GenericRaidParams params = new GenericRaidParams(new Random(random.nextLong()), true);
    	params.makeFleetsHostile = false; // will be made hostile when they arrive, not before
    	params.source = source;

    	params.prepDays = 21f + random.nextFloat() * 7f;
    	params.payloadDays = 27f + 7f * random.nextFloat();

    	params.raidParams.where = target;
    	params.raidParams.type = FGRaidType.SEQUENTIAL;

    	Set<String> disrupt = new LinkedHashSet<String>();
    	for (MarketAPI market : Misc.getMarketsInLocation(target, Factions.PLAYER)) {
    		params.raidParams.allowedTargets.add(market);
    		params.raidParams.allowNonHostileTargets = true;
    		for (Industry ind : market.getIndustries()) {
    			if (ind.getSpec().hasTag(Industries.TAG_UNRAIDABLE)) continue;
    			disrupt.add(ind.getId());
	
    		}
    	}

    	params.raidParams.disrupt.addAll(disrupt);
    	params.raidParams.raidsPerColony = Math.min(disrupt.size(), 4);
    	if (disrupt.isEmpty()) {
    		params.raidParams.raidsPerColony = 2;
    	}

    	if (params.raidParams.allowedTargets.isEmpty()) {
    		return false;
    	}

    	params.factionId = "dpl_phase_lab";
    	params.style = FleetStyle.QUALITY;
    	params.repImpact = ComplicationRepImpact.NONE;


    	float fleetSizeMult = 1f;

    	float f = intel.getMarketPresenceFactor(target);

    	float totalDifficulty = fleetSizeMult * 50f * (1f + 0.4f);

    	totalDifficulty -= 12;
    	params.fleetSizes.add(12); // first size 12 pick becomes the Operational Command

    	while (totalDifficulty > 0) {
    		int min = 8;
    		int max = 10;

    		//int diff = Math.round(StarSystemGenerator.getNormalRandom(random, min, max));
    		int diff = min + random.nextInt(max - min + 1);

    		params.fleetSizes.add(diff);
    		totalDifficulty -= diff;
    	}

    	dpl_MercenaryAttack attack = new dpl_MercenaryAttack(params);
    	attack.setListener(this);
    	//attack.setPreFleetDeploymentDelay(30f + random.nextFloat() * 60f);
    	//attack.setPreFleetDeploymentDelay(1f);
    	Global.getSector().getIntelManager().addIntel(attack, false);		

    	return true;
    }
}