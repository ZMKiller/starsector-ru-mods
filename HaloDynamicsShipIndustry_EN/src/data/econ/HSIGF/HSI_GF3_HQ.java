package data.econ.HSIGF;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.*;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.kit.HSII18nUtil;

import java.util.Random;

public class HSI_GF3_HQ extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

    @Override
    public boolean isHidden() {
        return !market.getFactionId().equals("HSI");
    }

    @Override
    public boolean isFunctional() {
        return super.isFunctional()&&!market.getMemoryWithoutUpdate().contains("$HSI_GF3_Revenger_Failed");
    }

    public void apply() {
        super.apply(true);

        int size = market.getSize();

        demand(Commodities.SUPPLIES, size - 1);
        demand(Commodities.FUEL, size - 1);
        demand(Commodities.SHIPS, size - 1);

        supply(Commodities.CREW, size);

        demand(Commodities.HAND_WEAPONS, size);
        supply(Commodities.MARINES, size);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
        applyDeficitToProduction(1, deficit, Commodities.MARINES);

        modifyStabilityWithBaseMod();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);

        if (!isFunctional()) {
            supply.clear();
            unapply();
        }

    }

    @Override
    public void unapply() {
        super.unapply();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);

        unmodifyStabilityWithBaseMod();
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
            addStabilityPostDemandSection(tooltip, hasDemand, mode);
        }
    }

    @Override
    protected int getBaseStabilityMod() {
        return 5;
    }

    public String getNameForModifier() {
        if (getSpec().getName().contains("HQ")) {
            return getSpec().getName();
        }
        return Misc.ucFirst(getSpec().getName());
    }

    @Override
    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS, Commodities.HAND_WEAPONS);
    }

    @Override
    public String getCurrentImage() {
        return super.getCurrentImage();
    }


    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }

    protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.5f,
            Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.8f);

    protected IntervalUtil revenger = new IntervalUtil(4f,8f);

    protected boolean shouldRevenge = false;

    protected float revengeSize = Global.getSettings().getBattleSize()*4f;

    protected float returningPatrolValue = 0f;

    protected IntervalUtil takeBackChecker = new IntervalUtil(20f,30f);

    @Override
    protected void buildingFinished() {
        super.buildingFinished();

        tracker.forceIntervalElapsed();
    }

    @Override
    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);

        tracker.forceIntervalElapsed();
    }

    private CampaignFleetAPI stationFleet = null;


    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (Global.getSector().getEconomy().isSimMode()) return;

        if (stationFleet != null) {
            stationFleet.setAI(null);
            stationFleet.setCircularOrbit(getMarket().getPrimaryEntity(), 0, 0, 100);
        }else{
            if(!Global.getSector().getMemoryWithoutUpdate().getBoolean("$HSI_JuggernautDefeated")&&market.getFaction()!=null&&market.getFaction().getId().equals("HSI")&&market.getFaction().getMemoryWithoutUpdate().contains("$HSIFLLostToPlayer")) {
                spawnStation();
            }else{
                market.getPrimaryEntity().getMemoryWithoutUpdate().unset(MemFlags.STATION_FLEET);
                market.getPrimaryEntity().getMemoryWithoutUpdate().unset(MemFlags.STATION_BASE_FLEET);
            }
        }

        if (!isFunctional()) return;


        float days = Global.getSector().getClock().convertToDays(amount);

        float spawnRate = 1.5f;
        float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
        spawnRate *= rateMult;


        float extraTime = 0f;
        if (returningPatrolValue > 0) {
            // apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
            float interval = tracker.getIntervalDuration();
            extraTime = interval * days;
            returningPatrolValue -= days;
            if (returningPatrolValue < 0) returningPatrolValue = 0;
        }
        tracker.advance(days * spawnRate + extraTime);

        //tracker.advance(days * spawnRate * 100f);

        if (tracker.intervalElapsed()) {
            String sid = getRouteSourceId();

            int light = getCount(PatrolType.FAST);
            int medium = getCount(PatrolType.COMBAT);
            int heavy = getCount(PatrolType.HEAVY);

            int maxLight = 0;
            int maxMedium = 2;
            int maxHeavy = 4;

            /*if(getMarket()!=null&&getMarket().getStarSystem()!=null){
                StarSystemAPI system = getMarket().getStarSystem();
                for(CampaignFleetAPI fleet:system.getFleets()){
                    if(fleet.getFaction().isHostileTo("HSI")){
                        maxMedium*=2;
                        maxHeavy*=2;
                    }
                }
            }*/

            WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
            picker.add(PatrolType.HEAVY, maxHeavy - heavy);
            picker.add(PatrolType.COMBAT, maxMedium - medium);
            picker.add(PatrolType.FAST, maxLight - light);

            if (picker.isEmpty()) return;

            PatrolType type = picker.pick();
            PatrolFleetData custom = new PatrolFleetData(type);

            OptionalFleetData extra = new OptionalFleetData(market);
            extra.fleetType = type.getFleetType();

            RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
            float patrolDays = 35f + (float) Math.random() * 10f;

            route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
        }
    }

    protected void spawnStation() {
        FleetParamsV3 fParams = new FleetParamsV3(null, null,
                market.getFactionId(),
                1f,
                FleetTypes.PATROL_SMALL,
                0,
                0, 0, 0, 0, 0, 0);
        fParams.allWeapons = true;



        stationFleet = FleetFactoryV3.createFleet(fParams);
        //stationFleet.setName(getCurrentName());
        stationFleet.setNoFactionInName(true);


        stationFleet.setStationMode(true);

        //stationFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);

        // needed for AI fleets to engage it, as they engage the hidden station fleet, unlike
        // the player that interacts with the stationEntity
        stationFleet.clearAbilities();
        stationFleet.addAbility(Abilities.TRANSPONDER);
        stationFleet.getAbility(Abilities.TRANSPONDER).activate();
        stationFleet.getDetectedRangeMod().modifyFlat("gen", 10000f);

        stationFleet.setAI(null);
        stationFleet.addEventListener(this);



        stationFleet.setCircularOrbit(market.getPrimaryEntity(), 0, 0, 100);
        stationFleet.getMemoryWithoutUpdate().set(MemFlags.STATION_MARKET, market);
        stationFleet.setHidden(true);


        matchStationAndCommanderToCurrentIndustry();
    }

    protected void matchStationAndCommanderToCurrentIndustry() {
        if (stationFleet == null) return;

        stationFleet.getFleetData().clear();

        String fleetName = HSII18nUtil.getCampaignString("HSI_JuggeranutFleetName");
        String variantId = "HSI_J01_Standard";
        float radius = 60f;

        stationFleet.setName(fleetName);

//		try {
//			FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
//		} catch (Throwable t) {
//			throw new RuntimeException("Market: " + market.getId() + ", variantId: " + variantId + ", " +
//					"message: [" + t.getMessage() + "]");
//		}

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
        //String name = stationFleet.getFleetData().pickShipName(member, null);
        String name = HSII18nUtil.getCampaignString("HSI_JuggeranutName");
        member.setShipName(name);

        stationFleet.getFleetData().addFleetMember(member);
        stationFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new HSIJ01FIDConfig());

        applyCRToStation();

        market.getPrimaryEntity().getMemoryWithoutUpdate().set(MemFlags.STATION_FLEET, stationFleet);
//			stationFleet.setBattle(null);
//			stationFleet.setNoEngaging(0);
        market.getPrimaryEntity().getContainingLocation().removeEntity(stationFleet);
        stationFleet.setExpired(false);
        market.getPrimaryEntity().getContainingLocation().addEntity(stationFleet);

        if (stationFleet == null) return;

//		if (market.isPlayerOwned()) {
//			System.out.println("wefwefew");
//		}

        PersonAPI commander = null;
            //if (stationFleet.getCommander() == null || !stationFleet.getCommander().isDefault()) {
//			if (stationFleet.getFlagship() == null || stationFleet.getFlagship().getCaptain() == null ||
//					!stationFleet.getFlagship().getCaptain().isDefault()) {
//				commander = Global.getFactory().createPerson();
//			}

            if (stationFleet.getFlagship() != null) {
                int level = 8;
                PersonAPI current = stationFleet.getFlagship().getCaptain();
                if (level > 0) {
                    if (current.isAICore() || current.getStats().getLevel() != level) {
                        commander = OfficerManagerEvent.createOfficer(
                                Global.getSector().getFaction(market.getFactionId()), level, true);
                    }
                } else {
                    if (stationFleet.getFlagship() == null || stationFleet.getFlagship().getCaptain() == null ||
                            !stationFleet.getFlagship().getCaptain().isDefault()) {
                        commander = Global.getFactory().createPerson();
                    }
                }
            }


//		if (commander != null) {
//			PersonAPI current = stationFleet.getFlagship().getCaptain();
//			if (current.isAICore() == commander.isAICore() &&
//					current.isDefault() == commander.isDefault() &&
//					 current.getStats().getLevel() == commander.getStats().getLevel()) {
//				commander = null;
//			}
//		}

        if (commander != null) {
            //stationFleet.setCommander(commander); // don't want a  "this is a flagship" star showing in the fleet list
            if (stationFleet.getFlagship() != null) {
                stationFleet.getFlagship().setCaptain(commander);
                stationFleet.getFlagship().setFlagship(false);
            }
        }
    }

    protected void applyCRToStation() {
        if (stationFleet != null) {
            float cr = 1f;
            for (FleetMemberAPI member : stationFleet.getFleetData().getMembersListCopy()) {
                member.getRepairTracker().setCR(cr);
            }
        }
    }

    public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
    }


    public boolean shouldRepeat(RouteData route) {
        return false;
    }

    public int getCount(PatrolType ... types) {
        int count = 0;
        for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
            if (data.getCustom() instanceof PatrolFleetData) {
                PatrolFleetData custom = (PatrolFleetData) data.getCustom();
                for (PatrolType type : types) {
                    if (type == custom.type) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    public int getMaxPatrols(PatrolType type) {
        if (type == PatrolType.FAST) {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
        }
        if (type == PatrolType.COMBAT) {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
        }
        if (type == PatrolType.HEAVY) {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
        }
        return 0;
    }

    public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
        return false;
    }

    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if(fleet.hasTag("HSI_GF3_Revenger")&&battle.getSideFor(fleet) != battle.getSideFor(primaryWinner)){
            market.getMemoryWithoutUpdate().set("$HSI_GF3_Revenger_Failed",true,-1);
        }
    }

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (!isFunctional()) return;

        if (reason == FleetDespawnReason.REACHED_DESTINATION) {
            RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
            if (route.getCustom() instanceof PatrolFleetData) {
                PatrolFleetData custom = (PatrolFleetData) route.getCustom();
                if (custom.spawnFP > 0) {
                    float fraction  = fleet.getFleetPoints() / custom.spawnFP;
                    returningPatrolValue += fraction;
                }
            }
        }
    }

    protected void removeStationEntityAndFleetIfNeeded() {
            if (stationFleet != null) {
                stationFleet.getMemoryWithoutUpdate().unset(MemFlags.STATION_MARKET);
                stationFleet.removeEventListener(this);
            }

            stationFleet = null;
    }

    public CampaignFleetAPI spawnFleet(RouteData route) {

        PatrolFleetData custom = (PatrolFleetData) route.getCustom();
        PatrolType type = custom.type;

        Random random = route.getRandom();

        float combat = 0f;
        float tanker = 0f;
        float freighter = 0f;
        combat = Global.getSettings().getBattleSize()*(0.33f+0.07f*random.nextFloat());
        tanker = Global.getSettings().getBattleSize()*0.02f;
        freighter = Global.getSettings().getBattleSize()*0.02f;

        CampaignFleetAPI fleet = HSIGuardFleetFactory.createGuardFleet(combat,tanker,freighter);

        if (fleet.isEmpty()) return null;

        fleet.setFaction(market.getFactionId(), true);
        fleet.setNoFactionInName(true);

        fleet.addEventListener(this);

//		PatrolAssignmentAIV2 ai = new PatrolAssignmentAIV2(fleet, custom);
//		fleet.addScript(ai);

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);

        if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
        }

        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;
        switch (type) {
            case FAST:
                rankId = Ranks.SPACE_LIEUTENANT;
                break;
            case COMBAT:
                rankId = Ranks.SPACE_COMMANDER;
                break;
            case HEAVY:
                rankId = Ranks.SPACE_CAPTAIN;
                break;
        }

        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isCapital()) {
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.TAG_NO_AUTOFIT);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
            }
        }

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

        //market.getContainingLocation().addEntity(fleet);
        //fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        if (custom.spawnFP <= 0) {
            custom.spawnFP = fleet.getFleetPoints();
        }

        return fleet;
    }

    public String getRouteSourceId() {
        return getMarket().getId() + "_" + "GF3";
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    public boolean showWhenUnavailable() {
        return false;
    }

    @Override
    public boolean canImprove() {
        return false;
    }

    @Override
    public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
        return level.next();
    }

    @Override
    public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
        return level.next();
    }

    public static class HSIJ01FIDConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

            config.showTransponderStatus = false;
            config.showEngageText = false;
            config.dismissOnLeave = false;
            config.withSalvage = false;
            config.printXPToDialog = true;

            config.noSalvageLeaveOptionText = HSII18nUtil.getCampaignString("HSI_Continue");
            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                    new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.
                            postPlayerSalvageGeneration(dialog, context, salvage);
                }
                public void notifyLeave(InteractionDialogAPI dialog) {

                    SectorEntityToken other = dialog.getInteractionTarget();
                    if (!(other instanceof CampaignFleetAPI)) {
                        dialog.dismiss();
                        return;
                    }

                    MarketAPI market= Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market");

                    dialog.setInteractionTarget(market.getPrimaryEntity());
                    RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("PopulateOptions");
                    dialog.setPlugin(plugin);
                    plugin.init(dialog);
                }

                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                }
            };
            return config;
        }
    }

}
