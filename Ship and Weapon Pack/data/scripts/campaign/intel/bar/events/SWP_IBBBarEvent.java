package data.scripts.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseGetCommodityBarEvent;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SWPModPlugin;
import data.scripts.campaign.intel.SWP_IBBIntel;
import data.scripts.campaign.intel.SWP_IBBIntel.FamousBountyStage;
import data.scripts.campaign.intel.SWP_IBBTracker;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SWP_IBBBarEvent extends BaseGetCommodityBarEvent {

    private SWP_IBBIntel intel = null;
    private FamousBountyStage thisStage = null;
    private boolean remove = false;
    private boolean part3 = false;
    private boolean repicked = false;

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        if ((BarEventManager.getInstance() != null) && (BarEventManager.getInstance().getCreatorFor(this) != null)) {
            ((SWP_IBBBarEventCreator) BarEventManager.getInstance().getCreatorFor(this)).createInstantly = false;
        }

        if ((intel == null) && !remove) {
            intel = new SWP_IBBIntel();
            intel.init();
            thisStage = intel.getStage();
        }

        if (thisStage != null) {
            SWP_IBBTracker.getTracker().reportStagePosted(thisStage);
            super.init(dialog, memoryMap);
        } else {
            remove = true;
            intel = null;
        }
    }

    @Override
    public boolean shouldRemoveEvent() {
        return remove;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!super.shouldShowAtMarket(market)) {
            return false;
        }
        if (!SWPModPlugin.isIBBEnabled()) {
            return false;
        }

        return SWP_IBBIntel.IBB_FACTIONS.contains(market.getFactionId());
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        if ((intel == null) && !remove) {
            intel = new SWP_IBBIntel();
            intel.init();
            thisStage = intel.getStage();
        }

        if (thisStage != null) {
            super.addPromptAndOption(dialog, memoryMap);
        } else {
            remove = true;
            intel = null;
        }
    }

    @Override
    protected void doStandardConfirmActions() {
        // we want to do nothing here, real work in doConfirmActionsPreAcceptText()
    }

    @Override
    protected void doConfirmActionsPreAcceptText() {
        if (intel == null) {
            return;
        }

        intel.start();
        intel.setImportant(true);
        Global.getSector().getIntelManager().addIntel(intel, false, dialog.getTextPanel());
    }

    @Override
    protected String getPersonFaction() {
        return this.market.getFactionId();
    }

    @Override
    protected String getPersonRank() {
        return Ranks.POST_AGENT;
    }

    @Override
    protected String getPersonPost() {
        return Ranks.POST_AGENT;
    }

    @Override
    protected float getPriceMult() {
        return 0;
    }

    @Override
    protected String getPrompt() {
        if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return "Seated at the back of the bar and flanked by two cyber-augmented bodyguards is a well-dressed "
                    + getManOrWoman() + ", who makes conspicuous eye contact with you as you walk in. "
                    + Misc.ucFirst(getHeOrShe()) + " gives you a curt nod.";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return "You spot a well-dressed " + getManOrWoman() + " lounging in a dimly-lit booth alongside a pair of "
                    + "cyber-augmented bodyguards. " + Misc.ucFirst(getHeOrShe()) + " gives you a curt nod as you "
                    + "notice " + getHisOrHer() + " IBB lapel pin.";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return "Soon after entering, you see a " + getManOrWoman() + " nursing a drink while " + getHisOrHer()
                    + " bodyguards scope out the the bar dispassionately. " + Misc.ucFirst(getHeOrShe())
                    + " looks up to give you a smile as you walk in, and you quickly recognize " + getHimOrHer()
                    + " as an IBB agent.";
        } else {
            return "A " + getManOrWoman() + " is seated at the back of the bar with " + getHisOrHer()
                    + " bodyguards. The IBB agent gives you a businesslike smile as you walk in, clearly expecting your arrival.";
        }
    }

    @Override
    protected String getOptionText() {
        if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return "Approach the guarded " + getManOrWoman() + " and take a seat at " + getHisOrHer() + " table";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return "Nod to the " + getManOrWoman() + " with the IBB pin and walk to " + getHisOrHer() + " table";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return "Order a drink and talk shop with the IBB agent";
        } else {
            return "Go see what the IBB agent wants from you this time";
        }
    }

    @Override
    protected String getMainText() {
        String playerManOrWoman = "woman";
        if (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) {
            playerManOrWoman = "man";
        }
        List<String> memberFactionList = new ArrayList<>(SWP_IBBIntel.IBB_FACTIONS.size());
        for (String memberFactionId : SWP_IBBIntel.IBB_FACTIONS) {
            FactionAPI memberFaction;
            try {
                memberFaction = Global.getSector().getFaction(memberFactionId);
            } catch (Exception e) {
                continue;
            }
            if (memberFaction != null) {
                memberFactionList.add(memberFaction.getDisplayNameWithArticle());
            }
        }

        if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return Misc.ucFirst(getHeOrShe()) + " sticks " + getHisOrHer() + " hand out, which you shake in a businesslike manner. "
                    + "The " + getManOrWoman() + " introduces " + getHimOrHerself() + " as " + person.getName().getFullName()
                    + " when you exchange greetings. \"It seems my intuition was correct, " + getPlayerTitle() + " "
                    + Global.getSector().getPlayerPerson().getName().getLast() + "; you are just the " + playerManOrWoman
                    + " we needed,\" " + getHeOrShe() + " says, smiling broadly. You give " + getHimOrHer()
                    + " a look of suspicion, to which " + getHeOrShe() + " responds, \"you're not in any trouble, " + getPlayerTitle()
                    + ". I'm with the %s - 'IBB' for short - an organization comprised of offices sponsored by "
                    + Misc.getAndJoined(memberFactionList) + ". We track a number of dangerous individuals throughout the sector, "
                    + "and when they get out of hand...\" " + Misc.ucFirst(getHeOrShe()) + " tips " + getHisOrHer()
                    + " head respectfully. \"That's where you come in.\" You express incredulity to the IBB agent, who scoffs and puts "
                    + getHisOrHer() + " hands out in a placating gesture. \"You will be rewarded, of course,\" " + getHeOrShe()
                    + " reassures you before leaning in and whispering, \"and you'll have a shot at some rather %s.\"";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return "You shake the " + getManOrWoman() + "'s hand as " + getHeOrShe() + " introduces " + getHimOrHerself()
                    + " as " + person.getName().getFullName() + ". " + Misc.ucFirst(getHeOrShe()) + " smiles and says, "
                    + "\"I'm with the %s, a bounty hunting organization sponsored by " + Misc.getAndJoined(memberFactionList)
                    + ". We track a number of dangerous individuals throughout the sector. Ready for your next mission, "
                    + getPlayerTitle() + " " + Global.getSector().getPlayerPerson().getName().getLast() + "?\"";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return "After you take a sip of your drink, the IBB agent introduces " + getHimOrHerself() + " as "
                    + person.getName().getFullName() + ". " + Misc.ucFirst(getHeOrShe()) + " gives you an easy smile and confirms that "
                    + getHeOrShe() + " is with the %s, before cutting right to the chase.";
        } else {
            return "You shake the IBB agent's hand with familiar efficiency and wordlessly nod to one another. Time to get to business.";
        }
    }

    @Override
    protected String[] getMainTextTokens() {
        if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return new String[]{
                "International Bounty Board",
                "unique ships"
            };
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return new String[]{
                "International Bounty Board"
            };
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return new String[]{
                "International Bounty Board"
            };
        } else {
            return new String[]{};
        }
    }

    @Override
    protected Color[] getMainTextColors() {
        if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return new Color[]{
                Global.getSector().getFaction("famous_bounty").getColor(),
                Global.getSector().getFaction("famous_bounty").getColor()
            };
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return new Color[]{
                Global.getSector().getFaction("famous_bounty").getColor()
            };
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return new Color[]{
                Global.getSector().getFaction("famous_bounty").getColor()
            };
        } else {
            return new Color[]{};
        }
    }

    @Override
    protected String getMainText2() {
        if (intel == null) {
            return "";
        }

        int powerLevel = SWP_Util.calculatePowerLevel(Global.getSector().getPlayerFleet());
        int dangerLevel = SWP_IBBIntel.calculatePowerLevel(thisStage);
        String dangerString;
        if (repicked) {
            if (powerLevel < Math.round(dangerLevel * 0.5f)) {
                dangerString = "By your reckoning, the danger level is %s, but the IBB agent seems unconcerned; you've certainly convinced "
                        + getHimOrHer() + " that you can take on any kind of challenge, even if you can't quite convince yourself of the same.";
            } else if (powerLevel < Math.round(dangerLevel * 1f)) {
                dangerString = "By your reckoning, the danger level is %s, but the IBB agent seems unconcerned; you've certainly convinced "
                        + getHimOrHer() + " that you can take on any kind of challenge.";
            } else {
                dangerString = "By your reckoning, the danger level is %s.";
            }
        } else if (powerLevel < Math.round(dangerLevel * 0.25f)) {
            dangerString = "clearly far beyond your own capabilities. " + Misc.ucFirst(getHeOrShe())
                    + " grimaces and informs you that the danger " + intel.getPerson().getName().getFullName()
                    + " currently poses to you is %s. \"We feel you can still grow into the challenge, " + getPlayerTitle() + ".\"";
        } else if (powerLevel < Math.round(dangerLevel * 0.5f)) {
            dangerString = "depicting a grim outlook. " + Misc.ucFirst(getHeOrShe()) + " gives you a sympathetic look, saying "
                    + "\"I know this is a %s task, " + getPlayerTitle() + ", but we believe you can take "
                    + intel.getPerson().getName().getFullName() + " down if you put your mind to it.\"";
        } else if (powerLevel < Math.round(dangerLevel * 0.75f)) {
            dangerString = "which you read while " + getHeOrShe() + " gives you a reassuring smile. \"Defeating "
                    + intel.getPerson().getName().getFullName() + " will be a %s challenge for you, " + getPlayerTitle()
                    + ", but we know you are up to the task.\"";
        } else if (powerLevel < Math.round(dangerLevel * 1f)) {
            dangerString = "which you intuit to be a %s danger, give or take. \"Don't underestimate "
                    + intel.getPerson().getName().getFullName() + ", " + getPlayerTitle() + ",\" the IBB agent warns.";
        } else if (powerLevel < Math.round(dangerLevel * 1.5f)) {
            dangerString = "and " + getHeOrShe() + " gives you time to look it over. \"" + intel.getPerson().getName().getFullName()
                    + " is only a %s obstacle for someone of your caliber, " + getPlayerTitle() + ".\"";
        } else if (powerLevel < Math.round(dangerLevel * 2f)) {
            dangerString = "a %s threat to you, by all appearances. " + Misc.ucFirst(getHeOrShe()) + " gives you a wink and says \""
                    + intel.getPerson().getName().getFullName() + " is all yours, " + getPlayerTitle() + ".\"";
        } else {
            dangerString = "but " + getHeOrShe() + " waves " + getHisOrHer() + " other hand dismissively. \""
                    + intel.getPerson().getName().getFullName() + " should pose %s to you, " + getPlayerTitle() + ".\"";
        }

        if (repicked) {
            return "\"This new target is " + intel.getTargetDesc() + "\" " + Misc.ucFirst(getHeOrShe())
                    + " hands you the TriPad, showing up-to-date info on the target's %s fleet. " + dangerString;
        } else if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return Misc.ucFirst(person.getName().getFirst()) + " leans back and brings " + getHisOrHer()
                    + " hands together in a scholar's cradle. \"Now, let's get down to business. Your first target is "
                    + intel.getTargetDesc() + "\" " + Misc.ucFirst(getHeOrShe())
                    + " hands you a stock TriPad with an estimate of the target's %s fleet, " + dangerString;
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return "\"Excellent! Your next target is " + intel.getTargetDesc() + "\" " + Misc.ucFirst(getHeOrShe())
                    + " hands you a colorful TriPad with the latest estimate of the target's %s fleet, " + dangerString;
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return Misc.ucFirst(person.getName().getFirst()) + " takes a sip of " + getHisOrHer()
                    + " drink. \"The target for elimination is " + intel.getTargetDesc() + "\" " + Misc.ucFirst(getHeOrShe())
                    + " hands you a grungy TriPad with an approximation of the target's %s fleet, " + dangerString;
        } else {
            return "\"The mission is to eliminate " + intel.getTargetDesc() + "\" " + Misc.ucFirst(getHeOrShe())
                    + " hands you a flickering TriPad with the latest info on the target's %s fleet, " + dangerString;
        }
    }

    @Override
    protected String[] getMainText2Tokens() {
        if (intel == null) {
            return new String[]{};
        }

        int powerLevel = SWP_Util.calculatePowerLevel(Global.getSector().getPlayerFleet());
        int dangerLevel = SWP_IBBIntel.calculatePowerLevel(thisStage);
        String dangerString;
        if (powerLevel < Math.round(dangerLevel * 0.25f)) {
            dangerString = "extreme";
        } else if (powerLevel < Math.round(dangerLevel * 0.5f)) {
            dangerString = "deadly";
        } else if (powerLevel < Math.round(dangerLevel * 0.75f)) {
            dangerString = "tough";
        } else if (powerLevel < Math.round(dangerLevel * 1f)) {
            dangerString = "moderate";
        } else if (powerLevel < Math.round(dangerLevel * 1.5f)) {
            dangerString = "slight";
        } else if (powerLevel < Math.round(dangerLevel * 2f)) {
            dangerString = "negligible";
        } else {
            dangerString = "no threat";
        }

        return new String[]{
            intel.getPerson().getName().getFullName(),
            intel.fleetSizeString(),
            dangerString
        };
    }

    @Override
    protected Color[] getMainText2Colors() {
        int powerLevel = SWP_Util.calculatePowerLevel(Global.getSector().getPlayerFleet());
        int dangerLevel = SWP_IBBIntel.calculatePowerLevel(thisStage);
        Color dangerColor;
        if (powerLevel < Math.round(dangerLevel * 0.25f)) {
            dangerColor = new Color(255, 0, 0);
        } else if (powerLevel < Math.round(dangerLevel * 0.5f)) {
            dangerColor = new Color(255, 85, 0);
        } else if (powerLevel < Math.round(dangerLevel * 0.75f)) {
            dangerColor = new Color(255, 170, 0);
        } else if (powerLevel < Math.round(dangerLevel * 1f)) {
            dangerColor = new Color(255, 255, 0);
        } else if (powerLevel < Math.round(dangerLevel * 1.5f)) {
            dangerColor = new Color(170, 255, 0);
        } else if (powerLevel < Math.round(dangerLevel * 2f)) {
            dangerColor = new Color(85, 255, 0);
        } else {
            dangerColor = new Color(0, 255, 0);
        }

        return new Color[]{
            Global.getSector().getFaction("famous_bounty").getColor(),
            dangerColor,
            dangerColor
        };
    }

    @Override
    protected void addStoryOption() {
        if (intel == null) {
            return;
        }

        SWP_IBBTracker.getTracker().reportStageCompleted(thisStage);
        final SWP_IBBIntel newIntel = new SWP_IBBIntel();
        newIntel.init();
        SWP_IBBTracker.getTracker().reportStageExpired(thisStage);
        SWP_IBBTracker.getTracker().reportStagePosted(thisStage);
        if (newIntel.getStage() == null) {
            return;
        }

        String id = "swp_differentbounty";
        options.addOption("Convince the IBB agent to give you a different bounty", id);

        StoryOptionParams params = new StoryOptionParams(id, 1, "swp_differentbounty", Sounds.STORY_POINT_SPEND_COMBAT,
                "Convinced an IBB agent to provide an alternative bounty");

        SetStoryOption.set(dialog, params, new BaseOptionStoryPointActionDelegate(dialog, params) {

            @Override
            public void confirm() {
                super.confirm();

                SWP_IBBTracker.getTracker().reportStageRepicked(thisStage);
                intel = newIntel;
                thisStage = intel.getStage();
                SWP_IBBTracker.getTracker().reportStagePosted(thisStage);

                part3 = false;
                repicked = true;

                dialog.getTextPanel().addPara(
                        "You schmooze the IBB agent with a round of expensive drinks and a lively display of bravado. "
                        + Misc.ucFirst(getHeOrShe()) + " begins to open up about other combat opportunities, "
                        + "which you capitalize upon by suggesting that your skills might be better served facing down a different target."
                );
                dialog.getTextPanel().addPara(
                        person.getName().getFullName() + " eventually relents, clearing out " + getHisOrHer()
                        + " TriPad and tapping through a list of contacts. \"Let's see if I can't find you something better... aha! Found one!\""
                );
                OptionPanelAPI options = dialog.getOptionPanel();
                options.clearOptions();
                options.addOption("Continue", OPTION_CONTINUE);
            }

            @Override
            public void createDescription(TooltipMakerAPI info) {
                float opad = 10f;
                info.setParaInsigniaLarge();

                info.addSpacer(-opad * 1f);

                info.addPara("The current bounty target is %s.", 0f, Misc.getHighlightColor(), intel.getPerson().getName().getFullName());

                info.addPara("You're able to swap the target to %s, whose bounty is %s. The original bounty target is less likely to appear in the future.",
                        opad, Misc.getHighlightColor(), newIntel.getPerson().getName().getFullName(), Misc.getDGSCredits(newIntel.getStage().reward));

                info.addSpacer(opad * 2f);
                addActionCostSection(info);
            }
        });
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (intel == null) {
            super.optionSelected(optionText, optionData);
            return;
        }
        if (optionData == OPTION_CONTINUE && part3) {
            String mainText3;
            if (repicked) {
                mainText3 = "\"The payment for %s is %s.\" " + Misc.ucFirst(getHeOrShe())
                        + " gives you an expectant stare while you weigh your options.";
            } else if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
                mainText3 = "\"The reward for %s is %s, paid on completion. "
                        + "Should you choose to accept, you will be given ample time to deal with the target. What do you say, "
                        + getPlayerTitle() + "?\" " + Misc.ucFirst(getHeOrShe())
                        + " looks at you expectantly while you weigh your options.";
            } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
                mainText3 = "\"The payment for taking out %s is %s. How does that sound, " + getPlayerTitle() + "?\" "
                        + Misc.ucFirst(getHeOrShe()) + " looks at you expectantly while you weigh your options.";
            } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
                mainText3 = "\"The bounty on %s is %s. Do you accept, " + getPlayerTitle() + "?\" " + Misc.ucFirst(getHeOrShe())
                        + " looks at you expectantly while you weigh your options.";
            } else {
                mainText3 = "\"Eliminate %s for %s. Yes or no?\" " + Misc.ucFirst(getHeOrShe())
                        + " looks at you expectantly while you weigh your options.";
            }
            dialog.getTextPanel().addPara(mainText3, Misc.getHighlightColor(), intel.getPerson().getName().getFullName(), Misc.getDGSCredits(thisStage.reward));

            showTotalAndOptions();
        } else if (optionData == OPTION_CONTINUE) {
            if (getMainText2Tokens() != null) {
                LabelAPI main = dialog.getTextPanel().addPara(getMainText2(), Misc.getHighlightColor(), getMainText2Tokens());
                main.setHighlightColors(getMainText2Colors());
                main.setHighlight(getMainText2Tokens());
            } else {
                dialog.getTextPanel().addPara(getMainText2());
            }

            TooltipMakerAPI tooltip = dialog.getTextPanel().beginTooltip();
            String portraitSprite = intel.getPerson().getPortraitSprite();
            if (portraitSprite.contentEquals("graphics/imperium/portraits/ii_helmutreal.png")) {
                portraitSprite = "graphics/imperium/portraits/ii_helmut.png";
            }
            tooltip.addImage(portraitSprite, 3f);
            dialog.getTextPanel().addTooltip();

            dialog.getOptionPanel().clearOptions();
            dialog.getOptionPanel().addOption("Continue", OPTION_CONTINUE);
            part3 = true;
        } else {
            super.optionSelected(optionText, optionData);
        }
    }

    @Override
    protected String getConfirmText() {
        if (intel == null) {
            return "";
        }
        return "Accept the mission to eliminate " + intel.getPerson().getName().getFullName();
    }

    @Override
    protected String getCancelText() {
        if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return "Decline " + getHisOrHer() + " offer and walk away";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return "Decline the IBB mission and walk away";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return "Decline the IBB mission and offer a hasty excuse";
        } else {
            return "Decline the IBB mission and bid " + getHisOrHer() + " farewell";
        }
    }

    @Override
    protected String getAcceptText() {
        String playerAttagirl = "Attagirl";
        if (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) {
            playerAttagirl = "Good man";
        }

        if (repicked) {
            return Misc.ucFirst(person.getName().getFirst()) + " nods, expecting your answer. \"Make them bleed, " + getPlayerTitle() + ".\"";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) {
            return Misc.ucFirst(person.getName().getFirst()) + " rises from " + getHisOrHer()
                    + " seat and smiles broadly, taking your hand to give it a vigorous shake. \"You've made the right choice, "
                    + getPlayerTitle() + ". Good luck out there!\"";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return Misc.ucFirst(person.getName().getFirst()) + " grins and leans over to shake your hand. \""
                    + playerAttagirl + "! You'll go far, " + getPlayerTitle() + ".\"";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return Misc.ucFirst(person.getName().getFirst()) + " finishes " + getHisOrHer()
                    + " drink in one go and claps you on the shoulder. \"Good hunting, " + getPlayerTitle() + "!\"";
        } else {
            return Misc.ucFirst(person.getName().getFirst()) + " smiles and shakes your hand to seal the deal. \"Good luck, "
                    + getPlayerTitle() + ".\"";
        }
    }

    @Override
    protected String getDeclineText() {
        remove = true;
        intel = null;
        if (repicked) {
            return Misc.ucFirst(person.getName().getFirst()) + " seems shocked by your refusal. \"After everything you said, I could have sworn...\" "
                    + "The IBB agent shakes " + getHisOrHer() + " head, before getting up and walking away.";
        } else if ((SWP_IBBTracker.getTracker().getNumCompletedStages() == 0) && (SWP_IBBTracker.getTracker().getNumStagesBegun() == 0)) {
            return Misc.ucFirst(person.getName().getFirst()) + " frowns disapprovingly. \"I'm sorry to hear that, "
                    + getPlayerTitle() + ",\" you hear as you walk away.";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 5) {
            return Misc.ucFirst(person.getName().getFirst()) + " shakes " + getHisOrHer() + " head sullenly as you step away.";
        } else if (SWP_IBBTracker.getTracker().getNumCompletedStages() <= 10) {
            return Misc.ucFirst(person.getName().getFirst()) + " gives you a forced smile. \"Maybe next time, " + getPlayerTitle() + "?\"";
        } else {
            return Misc.ucFirst(person.getName().getFirst()) + " lets you go with a few words of pleasantries.";
        }
    }

    @Override
    protected boolean showCargoCap() {
        return false;
    }

    private String getPlayerTitle() {
        return "Captain";
    }

    @Override
    public boolean isAlwaysShow() {
        return true;
    }
}
