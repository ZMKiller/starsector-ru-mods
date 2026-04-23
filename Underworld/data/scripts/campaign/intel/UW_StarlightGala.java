package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseMissionIntel;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.fleets.UW_PalaceFleet;
import java.awt.Color;
import java.util.Set;
import org.apache.log4j.Logger;

public class UW_StarlightGala extends BaseMissionIntel {

    public static Logger log = Global.getLogger(UW_StarlightGala.class);

    public static float ENTRY_FEE = 1000000f;
    public static float STORY_ENTRY_FEE = 500000f;

    public CampaignFleetAPI importantFleet;

    @SuppressWarnings("LeakingThisInConstructor")
    public UW_StarlightGala(InteractionDialogAPI dialog) {
        setImportant(true);
        setMissionState(MissionState.ACCEPTED);
        Global.getSector().addScript(this);
    }

    @Override
    public void missionAccepted() {
    }

    @Override
    public void advanceMission(float amount) {
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwKilledPalace")) {
            setMissionResult(new MissionResult(0, null, null));
            setMissionState(MissionState.FAILED);
            endMission();
        }

        boolean foundPalace = false;
        if (missionState == MissionState.ACCEPTED) {
            for (EveryFrameScript script : Global.getSector().getScripts()) {
                if (script instanceof UW_PalaceFleet uW_PalaceFleet) {
                    CampaignFleetAPI fleet = uW_PalaceFleet.getFleet();
                    if ((fleet != null) && fleet.isAlive()) {
                        importantFleet = fleet;
                        foundPalace = true;
                    }
                }
            }
        }

        if (foundPalace) {
            Misc.makeImportant(importantFleet, "UW_StarlightGala");
        } else if (importantFleet != null) {
            Misc.makeUnimportant(importantFleet, "UW_StarlightGala");
            importantFleet = null;
        }
    }

    @Override
    public void endMission() {
        if (importantFleet != null) {
            Misc.makeUnimportant(importantFleet, "UW_StarlightGala");
            importantFleet = null;
        }

        endAfterDelay();
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) {
            initPad = opad;
        }

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;
        boolean knowFee = Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwKnowStarlightGalaFee");
        boolean knowNoFee = Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwKnowStarlightGalaNoFee");

        FactionAPI faction = getFactionForUIColors();
        if (isUpdate) {
            if (isFailed() || isCancelled() || isCompleted()) {
                return;
            } else if (knowFee) {
                if (knowNoFee && Global.getSector().getPlayerFaction().isAtWorst(faction, RepLevel.COOPERATIVE)) {
                    info.addPara("" + Misc.getDGSCredits(1000000f) + " entrance fee %s", initPad, g, h, "(fee waived)");
                } else {
                    info.addPara("%s entrance fee", initPad, tc, h, Misc.getDGSCredits(1000000f));
                }
            } else if (knowNoFee && Global.getSector().getPlayerFaction().isAtWorst(faction, RepLevel.COOPERATIVE)) {
                info.addPara("Fee waived", initPad, h);
            }
        } else {
            if (isFailed()) {
                if (mode == ListInfoMode.IN_DESC) {
                    info.addPara("You won't be invited back", initPad, tc);
                }
            } else if (!isCancelled() && !isCompleted()) {
                info.addPara("Located at a %s fleet", initPad, tc, faction.getBaseUIColor(), "Starlight Gala Parade");

                if (knowFee) {
                    if (knowNoFee && Global.getSector().getPlayerFaction().isAtWorst(faction, RepLevel.COOPERATIVE)) {
                        info.addPara("" + Misc.getDGSCredits(1000000f) + " entrance fee %s", 0f, g, h, "(fee waived)");
                    } else {
                        info.addPara("%s entrance fee", 0f, tc, h, Misc.getDGSCredits(1000000f));
                        info.addPara("The fleet's %s flagship is likely full of riches...", 0f, tc, faction.getBaseUIColor(), "Palace-class");
                    }
                } else if (knowNoFee && Global.getSector().getPlayerFaction().isAtWorst(faction, RepLevel.COOPERATIVE)) {
                    info.addPara("Fee waived", 0f, h);
                }
            }
        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);

        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);
    }

    @Override
    public String getName() {
        return "Starlight Gala";
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction("cabal");
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;

        FactionAPI faction = getFactionForUIColors();
        boolean knowFee = Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwKnowStarlightGalaFee");
        boolean knowNoFee = Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwKnowStarlightGalaNoFee");

        info.addImage(faction.getCrest(), width, 80, opad);

        if (isPosted() || isAccepted()) {
            String price = "supposedly steep";
            if (knowFee) {
                if (knowNoFee) {
                    price = "steep (for most)";
                } else {
                    price = "steep";
                }
            }

            LabelAPI label = info.addPara("You've been invited to attend the Starlight Gala, a seasonal event hosted by "
                    + "the Starlight Cabal. Although the price of entry is " + price + ", you've been assured that it's "
                    + "an excellent way to schmooze with some of the most powerful people in the Sector.", opad);
            label.setHighlight("Starlight Cabal");
            label.setHighlightColors(faction.getBaseUIColor());

            addBulletPoints(info, ListInfoMode.IN_DESC);
        } else {
            if (isFailed()) {
                info.addPara("Rather than attend the Starlight Gala, you decided to crash the party by blowing up the "
                        + "ship that had been hosting it, plundering its wealth in the process.", opad);

                addBulletPoints(info, ListInfoMode.IN_DESC);
            } else if (isCompleted()) {
                /* Should add more info here if you did something wacky, especially making friends with Zeb */
                info.addPara("You've attended the Starlight Gala.", opad);

                addBulletPoints(info, ListInfoMode.IN_DESC);
            }
        }

    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("uw_missions", "uw_starlight_gala");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MISSIONS);
        tags.add(getFactionForUIColors().getId());
        return tags;
    }

    @Override
    protected String getMissionTypeNoun() {
        return "invitation";
    }

    @Override
    protected MissionResult createAbandonedResult(boolean withPenalty) {
        return new MissionResult();
    }

    @Override
    public boolean canAbandonWithoutPenalty() {
        return false;
    }

    @Override
    protected MissionResult createTimeRanOutFailedResult() {
        return createAbandonedResult(true);
    }
}
