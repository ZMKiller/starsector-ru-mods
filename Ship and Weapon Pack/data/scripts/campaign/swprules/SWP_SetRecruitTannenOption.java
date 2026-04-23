package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.campaign.OptionPanelAPI.OptionTooltipCreator;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI.PersonDataAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * SetStoryColor <option id> <story points> <bonus XP fraction key> <personId> <optional: story point spent sound id>
 * <optional: playthrough log text>
 */
public class SWP_SetRecruitTannenOption extends BaseCommandPlugin {

    public static class TannenStoryOptionParams extends StoryOptionParams {

        public String personId;

        public TannenStoryOptionParams(Object optionId, int numPoints, String bonusXPID, String personId, String soundId, String logText) {
            super(optionId, numPoints, bonusXPID, soundId, logText);
            this.personId = personId;
        }
    }

    public static class TannenOptionStoryPointActionDelegate extends BaseOptionStoryPointActionDelegate {

        public String personId;

        public TannenOptionStoryPointActionDelegate(InteractionDialogAPI dialog, TannenStoryOptionParams params) {
            super(dialog, params);
            this.personId = params.personId;
        }

        @Override
        public void createDescription(TooltipMakerAPI info) {
            TooltipMakerAPI text = info;
            float opad = 10f;

            PersonDataAPI data = Global.getSector().getImportantPeople().getData(personId);
            if (data == null) {
                text.addPara("ERROR", opad);
                return;
            }
            PersonAPI person = data.getPerson();
            if (person == null) {
                text.addPara("ERROR", opad);
                return;
            }
            ShipVariantAPI starRunner = Global.getSettings().getVariant("swp_afflictor_tannen");
            if (starRunner == null) {
                text.addPara("ERROR", opad);
                return;
            }

            info.addSpacer(-opad);

            MutableCharacterStatsAPI stats = person.getStats();

            Color hl = Misc.getHighlightColor();

            text.addPara("You consider convincing " + person.getNameString() + " to join your fleet.", opad);

            text.addPara("Level: %s", opad, hl, "" + (int) stats.getLevel());

            for (String skillId : Global.getSettings().getSortedSkillIds()) {
                int level = (int) stats.getSkillLevel(skillId);
                if (level > 0) {
                    SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
                    String skillName = spec.getName();
                    if (spec.isAptitudeEffect()) {
                        skillName += " Aptitude";
                    }

                    if (level <= 1) {
                        text.addPara(skillName, opad);
                    } else {
                        text.addPara(skillName + " (Elite)", opad);
                    }
                }
            }

            String personality = Misc.lcFirst(person.getPersonalityAPI().getDisplayName());
            text.addPara("Personality: %s", opad, Misc.getHighlightColor(), personality);

            text.addPara(person.getNameString() + " is similar to a mercenary officer; he effectively doesn't count against the maximum "
                    + "number of officers in your fleet. More specifically, recruiting him will increase your officer maximum "
                    + "to %s.", opad, Misc.getHighlightColor(), "" + (Misc.getMaxOfficers(Global.getSector().getPlayerFleet()) + 1));
            text.addPara(person.getNameString() + " is the captain of the %s, an %s. For the time being, he will refuse to command anything else.",
                    opad, Misc.getHighlightColor(), "Star Runner", starRunner.getHullSpec().getNameWithDesignationWithDashClass());

            info.addSpacer(opad * 2f);

            addActionCostSection(info);
        }
    }

    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        String optionId;
        String bonusXPID;
        String soundId;
        String logText = null;
        int numPoints;
        if (params.size() == 4) {
            optionId = params.get(0).string;
            numPoints = 1;
            bonusXPID = optionId;
            soundId = params.get(1).string;
            logText = params.get(2).getStringWithTokenReplacement(params.get(2).getString(memoryMap), dialog, memoryMap);
        } else {
            optionId = params.get(0).string;
            numPoints = (int) params.get(1).getFloat(memoryMap);
            bonusXPID = params.get(2).getString(memoryMap);
            soundId = params.size() >= 5 ? params.get(4).string : null;

            if (params.size() >= 6) {
                logText = params.get(5).getStringWithTokenReplacement(params.get(5).getString(memoryMap), dialog, memoryMap);
            }
        }
        String personId = params.get(3).getString(memoryMap);

        TannenStoryOptionParams storyParams = new TannenStoryOptionParams(optionId, numPoints, bonusXPID, personId, soundId, logText);
        return set(dialog, storyParams, new TannenOptionStoryPointActionDelegate(dialog, storyParams));
    }

    public static boolean set(final InteractionDialogAPI dialog, final TannenStoryOptionParams params, StoryPointActionDelegate delegate) {
        final float bonusXPFraction = Global.getSettings().getBonusXP(params.bonusXPID);
        dialog.makeStoryOption(params.optionId, params.numPoints, bonusXPFraction, params.soundId);

        if (params.numPoints > Global.getSector().getPlayerStats().getStoryPoints()) {
            dialog.getOptionPanel().setEnabled(params.optionId, false);
        }

        dialog.getOptionPanel().addOptionTooltipAppender(params.optionId, new OptionTooltipCreator() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
                float opad = 10f;
                float initPad = 0f;
                if (hadOtherText) {
                    initPad = opad;
                }
                tooltip.addStoryPointUseInfo(initPad, params.numPoints, bonusXPFraction, true);
                int sp = Global.getSector().getPlayerStats().getStoryPoints();
                String points = "points";
                if (sp == 1) {
                    points = "point";
                }
                tooltip.addPara("You have %s " + Misc.STORY + " " + points + ".", opad,
                        Misc.getStoryOptionColor(), "" + sp);
            }
        });

        dialog.getOptionPanel().addOptionConfirmation(params.optionId, delegate);

        return true;
    }
}
