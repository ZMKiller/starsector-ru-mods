package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.campaign.HSITTMission.HSITTMissionIntel;
import data.hullmods.HSIHaloV2;
import data.kit.HSIIds;
import data.scripts.HSIGenerator.HSIIPManager;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HSICampaignScript implements EveryFrameScript {
    public enum HSIGlobalState {
        WAITING_FOR_START, ASK_FOR_DATA, TEST_PATROL, BUILD_FACILITY, ENGAGE, WAR_STAGE_1, WAR_STAGE_2, WAR_STAGE_3,
        END_WAR, NEGOTIATE, LOST_WAR, TOTAL_WAR
    }

    protected HSIGlobalState state = HSIGlobalState.WAITING_FOR_START;
    //protected float elaspsed = 0;
    //protected float stageLength = 240f+(float)((0.7f-Math.random())*80f);
    protected SectorAPI sector;

    protected IntervalUtil StalkerTracker = new IntervalUtil(40f,60f);

    private FleetMemberAPI activeMember;

    private List<CargoAPI> tempCargos = new ArrayList<>(4);

    private boolean wasRefit = false;

    private final long TIMESTAMP_BASE;


    public HSICampaignScript(){
        if(Global.getSector().getMemoryWithoutUpdate().contains("$HSIWarStage")){
            state = (HSIGlobalState)(Global.getSector().getMemoryWithoutUpdate().get("$HSIWarStage"));
        }
        TIMESTAMP_BASE = Global.getSector().getClock().getTimestamp();
        //test only
        //StalkerTracker.setElapsed(40f);
        //Global.getSector().getCampaignUI().showInteractionDialog(new OpenCustomPanelFromDialog(null), null);
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return true;
    }

    private float TT_Mission_Timer = 0f;

    public void advance(float amount) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) {
            return;
        } else {
            sector = Global.getSector();
            
        }
        checkIPPortraits(sector);
        //sector.getMemoryWithoutUpdate().set("$HSIWarStage", state);

        boolean isRefit = sector.getCampaignUI()!=null&&sector.getCampaignUI().getCurrentCoreTab()!=null&&sector.getCampaignUI().getCurrentCoreTab().equals(CoreUITabId.REFIT);

        if(!isRefit&&wasRefit){
            if (Global.getSector() != null &&
                    Global.getSector().getPlayerFleet() != null &&
                    Global.getSector().getPlayerFleet().getCargo() != null &&
                    Global.getSector().getPlayerFleet().getCargo().getStacksCopy() != null) {
                Set<String> indKey = HSIHaloV2.BaseMap.keySet();
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
                    if (s.isWeaponStack() && (indKey.contains(s.getWeaponSpecIfWeapon().getWeaponId()))) {
                        Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                            String base = HSIHaloV2.BaseMap.get(s.getWeaponSpecIfWeapon().getWeaponId());
                            if (base != null) {
                                Global.getSector().getPlayerFleet().getCargo().addWeapons(base, Math.round(s.getSize()));
                                //ship.getVariant().removeMod("HSI_WeaponIndAdaption");
                                //ship.getVariant().setSource(VariantSource.REFIT);
                                //Global.getLogger(this.getClass()).info("added 1"+base);
                            }
                    }
                }
                for(FleetMemberAPI member:Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()){
                    if(!member.getVariant().hasHullMod("HSI_Halo")){
                        ShipVariantAPI v = member.getVariant().clone();
                        v.setSource(VariantSource.REFIT);
                        v.setHullVariantId(Misc.genUID());
                        Set<String> MapKey = HSIHaloV2.BaseMap.keySet();
                        for (String slotId : v.getNonBuiltInWeaponSlots()) {
                            String wpnId = v.getWeaponId(slotId);
                            if (wpnId != null) {
                                if (MapKey.contains(wpnId)) {
                                    v.clearSlot(slotId);
                                    v.addWeapon(slotId, HSIHaloV2.BaseMap.get(wpnId));
                                }
                            }
                        }
                        member.setVariant(v,false,false);
                    }
                }
            }

        }

        wasRefit = isRefit;

        if (sector.isPaused())
            return;
        if(Global.getSector().getFaction("HSI")==null) return;
        //Stalker
        FactionAPI HSI = Global.getSector().getFaction("HSI");
            /*if(HSI.getRelToPlayer().isHostile()){
                float mult = -HSI.getRelToPlayer().getRel();
                if(Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().contains("$HSIisTraitor")) mult = 2f;
                StalkerTracker.advance(sector.getClock().convertToDays(amount)*mult);
            }
            if(StalkerTracker.intervalElapsed()){
                createStalkerFleet();
            }*/
        if((Global.getSector().getMemoryWithoutUpdate().contains(HSIIds.CAMPAIGN_FLAG.TRAITOR_BASE)
                &&Global.getSector().getFaction("HSI").getRelToPlayer().isHostile())
        ||(Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().contains("$HSIFLLostToPlayer")&&Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().getBoolean("$HSIFLLostToPlayer"))){
                Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().set(HSIIds.CAMPAIGN_FLAG.IS_TRAITOR,true);
        }


        //Tritechyon Mission
        if(!Global.getSector().getMemoryWithoutUpdate().contains("$HSI_TTMission_Start")
                && Global.getSector().getEntityById("culann")!=null
                &&Global.getSector().getEntityById("culann").getFaction()!=null
                &&Global.getSector().getEntityById("culann").getFaction().getId().equals(Factions.TRITACHYON)&&Global.getSector().getFaction("HSI")!=null){
            if(HSI.getRelationship(Factions.TRITACHYON)>0.0f
                    && (Global.getSector().getClock().getElapsedDaysSince(TIMESTAMP_BASE)>720f||(Global.getSector().getFaction("HSI").getRelToPlayer().getRel()>0.6f&&Global.getSector().getClock().getElapsedDaysSince(TIMESTAMP_BASE)>540f)||(Global.getSector().getFaction("HSI").getRelToPlayer().getRel()>0.8f&&Global.getSector().getClock().getElapsedDaysSince(TIMESTAMP_BASE)>360f))
            ){
                Global.getSector().getMemoryWithoutUpdate().set("$HSI_TTMission_Start",true);
                HSITTMissionIntel mission = new HSITTMissionIntel();
                mission.createAndAbortIfFailed(null,false);
                mission.accept(null,null);
            }

            if(HSI.getRelationship(Factions.TRITACHYON)<=0.0f) {
                TT_Mission_Timer+=sector.getClock().convertToDays(amount);
                if(TT_Mission_Timer>=30f){
                    TT_Mission_Timer-=30f;
                    HSI.adjustRelationship(Factions.TRITACHYON,0.075f);
                }
            }
        }
    }


    private void createStalkerFleet(){
        Vector2f invStartLoc = new Vector2f();
        if(Global.getSector().getPlayerFleet()!=null){
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            if(fleet.isInHyperspace()){
                invStartLoc = Misc.getPointAtRadius(fleet.getLocationInHyperspace(),(float) (200*Math.random())+300f);
            }else{
                invStartLoc = Misc.getPointAtRadius(fleet.getLocation(),(float)(1500*Math.random()+900f) );
            }
        }else{
            return;
        }
                //Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market").getPrimaryEntity().getLocation();
        float expectPT = //200f;
                Math.max(40f,Global.getSector().getPlayerFleet().getFleetPoints()*(float)(Math.random()*0.75f+1f));
        FleetParamsV3 invFleetParam = new FleetParamsV3(Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market"),Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market").getContainingLocation().getLocation(),
                "HSI_Stalker", 1f, FleetTypes.PATROL_LARGE, expectPT, 0,
                0, 0, 0,
                0, 0);
        //invFleetParam.maxNumShips = (int) (Global.getSettings().getMaxShipsInFleet() * 1.5f);
        invFleetParam.withOfficers = true;
        invFleetParam.maxOfficersToAdd = invFleetParam.maxNumShips;
        invFleetParam.averageSMods = 2;
        invFleetParam.officerLevelBonus = Math.min(Global.getSector().getPlayerPerson().getStats().getLevel()/2,Global.getSector().getPlayerFleet().getNumCapitals()/4);

        CampaignFleetAPI invFleet = FleetFactoryV3.createFleet(invFleetParam);
        invFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
        invFleet.setLocation(invStartLoc.x, invStartLoc.y);
        invFleet.setFaction("HSI");

        Global.getSector().getPlayerFleet().getContainingLocation().addEntity(invFleet);


        invFleet.addAssignment(FleetAssignment.INTERCEPT, Global.getSector().getPlayerFleet(), 300f,
                "");
        invFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);

        //Global.getLogger(this.getClass()).info("StalkerFleet spawned.");
        //Global.getLogger(this.getClass()).info("Fleet Location:"+(invFleet.getLocation().toString())+" "+"Fleet in hyper:"+invFleet.isInHyperspace());
        //Global.getLogger(this.getClass()).info("Fleet is empty:"+(invFleet==null||invFleet.isEmpty()));
        //Global.getLogger(this.getClass()).info("Known ships:"+Global.getSector().getFaction("HSI_Stalker").getKnownShips().toString());
        //Global.getLogger(this.getClass()).info("Primary entity:"+Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market").getPrimaryEntity().getName());
    }


    protected void checkIPPortraits(SectorAPI sector) {
        for (String ip : HSIIPManager.PortraitsCheck) {
            PersonAPI person = sector.getImportantPeople().getPerson(ip);
            if (person != null) {
                switch (ip) {
                    case HSIIds.PERSON.THE_COMMISSIONER:
                        float rel = person.getRelToPlayer().getRel();
                        if (rel >= 80) {
                            person.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Cooperate.png");
                        } else if (rel >= 50) {
                            person.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Friendly.png");
                        } else if (rel >= 20) {
                            person.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Normal.png");
                        } else if (rel >= -10) {
                            person.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Inhostile.png");
                        } else {
                            person.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Hostile.png");
                        }
                        break;
                    default:
                }
            }
        }
    }

    public void setActiveMember(FleetMemberAPI member){
        if(activeMember!=member){

        }else{

        }
        activeMember = member;
    }

    private void initCargoWeaponNumControl(){

    }

}
