package data.campaign.HSITTMission;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kit.HSII18nUtil;

import java.awt.*;

public class HSITTMissionIntel extends HubMissionWithSearch {

    public static float MISSION_DAYS = 120f;



    public enum Stage {

        MISSION_START,
        MISSION_END_BATTLE,
        END,
        NO_ENGAGE_END

    }
    protected SectorEntityToken culann;
    protected StarSystemAPI system;



    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {//当任务被创建时自动调用
        setStartingStage(Stage.MISSION_START);

        addSuccessStages(Stage.END);

        addFailureStages(Stage.NO_ENGAGE_END);

        if (!setGlobalReference("$HSITTMission_ref")) {
            return false;
        }

        culann = Global.getSector().getEntityById("culann");

        if(culann == null) return false;

        system = culann.getStarSystem();

        makeImportant(culann, "$HSI_TTMission_Visit", Stage.MISSION_START);

        setStageOnGlobalFlag(Stage.END, "$HSI_TTMission_Finished");

        setTimeLimit(Stage.NO_ENGAGE_END, MISSION_DAYS, null);

        return true;
    }



    protected void updateInteractionDataImpl() {//当任务被手动刷新时（例如在rules里使用Call XXXXX updateData）时调用
        if (getCurrentStage() != null) {

            set("$HSITTMission_stage", ((Enum<?>)getCurrentStage()).name());

        }

        set("$HSITTMission_starName", system.getNameWithNoType());

        set("$HSITTMission_systemName", system.getNameWithLowercaseTypeShort());

        set("$HSITTMission_dist", getDistanceLY(culann));


    }



    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.MISSION_START||currentStage == Stage.MISSION_END_BATTLE) {
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_Desc0"), opad);
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_Desc1"), opad);
        } else if (currentStage == Stage.END) {
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_Desc2"), opad);
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_Desc3"), opad);
        }else if (currentStage == Stage.NO_ENGAGE_END) {
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_Desc2"), opad);
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_Desc4"), opad);
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        addDescriptionForCurrentStage(info, width, height);

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    protected void advanceImpl(float amount) {
        super.advanceImpl(amount);
    }

    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.MISSION_START) {
            if (system.isCurrentLocation()) {
                info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_NextStep"), tc, pad);
            }
            return true;
        } else if (currentStage == Stage.END) {
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_NextStep2"), tc, pad);
            return true;
        } else if (currentStage == Stage.NO_ENGAGE_END) {
            info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_NextStep2"), tc, pad);
            return true;
        }
        return false;
    }

    @Override
    public String getBaseName() {//在各处显示给玩家看的任务名称，可自定义

        return HSII18nUtil.getCampaignString("HSI_TTMission_Name");

    }
    @Override
    public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        String text = getStageDescriptionText();
        if (text != null) {
            info.addPara(text, opad);
        } else {
            String noun = getMissionTypeNoun();
            String verb = getMissionCompletionVerb();
            if (isSucceeded()) {
                info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_NextStep2"), opad);
            } else if (isFailed()) {
                info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_NextStep2"), opad);
            } else if (isAbandoned()) {
                info.addPara(HSII18nUtil.getCampaignString("HSI_TTMission_NextStep2"), opad);
            } else {
                addDescriptionForNonEndStage(info, width, height);
            }
        }
    }
    @Override
    public String getIcon() {
        return Global.getSector().getFaction("HSI").getCrest();
    }

}
