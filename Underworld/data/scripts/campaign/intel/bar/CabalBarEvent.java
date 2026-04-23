package data.scripts.campaign.intel.bar;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.UnderworldModPlugin;
import data.scripts.campaign.intel.UW_StarlightGala;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CabalBarEvent extends BaseBarEventWithPerson {

    public static enum OptionId {
        INIT,
        DICE_PROMPT,
        DICE_ACCEPT,
        DRUGS,
        BLACKJACK_PROMPT,
        BLACKJACK_ACCEPT,
        GALA,
        LEAVE
    }

    public static final Map<PersonImportance, Integer> DICE_WAGER = new HashMap<>();
    public static final Map<PersonImportance, Float> DICE_REP = new HashMap<>();
    public static final Map<PersonImportance, Float> DICE_REP_WIN = new HashMap<>();
    public static final float DRUGS_REP = 0.02f;
    public static final String HR = "-----------------------------------------------------------------------------";

    static {
        DICE_WAGER.put(PersonImportance.LOW, 500);
        DICE_WAGER.put(PersonImportance.MEDIUM, 10000);
        DICE_WAGER.put(PersonImportance.HIGH, 200000);

        DICE_REP.put(PersonImportance.LOW, 0.02f);
        DICE_REP.put(PersonImportance.MEDIUM, 0.04f);
        DICE_REP.put(PersonImportance.HIGH, 0.06f);

        DICE_REP_WIN.put(PersonImportance.LOW, 0.03f);
        DICE_REP_WIN.put(PersonImportance.MEDIUM, 0.06f);
        DICE_REP_WIN.put(PersonImportance.HIGH, 0.1f);
    }

    protected String diceGameId;
    protected Boolean shouldShow = null;

    public CabalBarEvent() {
    }

    @Override
    protected PersonAPI createPerson() {
        PersonAPI newPerson = super.createPerson();
        PersonImportance importance = pickImportance();
        newPerson.setImportanceAndVoice(importance, random);
        String tag = pickTag(importance);
        newPerson.addTag(tag);
        if (importance == PersonImportance.HIGH) {
            if ((tag.contentEquals(Tags.CONTACT_UNDERWORLD)) && (Math.random() > 0.5)) {
                newPerson.addTag(Tags.CONTACT_MILITARY);
            } else if ((tag.contentEquals(Tags.CONTACT_MILITARY)) && (Math.random() > 0.5)) {
                newPerson.addTag(Tags.CONTACT_UNDERWORLD);
            }
        }
        return newPerson;
    }

    public PersonImportance pickImportance() {
        WeightedRandomPicker<PersonImportance> picker = new WeightedRandomPicker<>(random);
        picker.add(PersonImportance.LOW, 4f);
        if (market != null) {
            if (market.getFactionId().contentEquals(Factions.TRITACHYON) || market.getFactionId().contentEquals("cabal")) {
                picker.add(PersonImportance.MEDIUM, 2f);
                if (market.hasCondition("cabal_influence")) {
                    picker.add(PersonImportance.HIGH, 1f);
                }
            }
        }
        return picker.pick();
    }

    public String pickTag(PersonImportance importance) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(random);
        picker.add(Tags.CONTACT_UNDERWORLD, 4f);
        if (importance == PersonImportance.MEDIUM) {
            picker.add(Tags.CONTACT_MILITARY, 1f);
        } else if (importance == PersonImportance.HIGH) {
            picker.add(Tags.CONTACT_MILITARY, 2f);
        }
        return picker.pick();
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!super.shouldShowAtMarket(market)) {
            return false;
        }
        if (!UnderworldModPlugin.isStarlightCabalEnabled()) {
            return false;
        }
        regen(market);

        if (shouldShow == null) {
            float chance = 0.3f;
            if (market.hasCondition("cabal_influence")) {
                chance += 0.2f;
            }
            if (market.getFactionId().equals(Factions.TRITACHYON)) {
                chance += 0.1f;
            } else if (!market.getFaction().isHostileTo("cabal")) {
                chance += 0.05f;
            }
            if (market.isFreePort()) {
                chance += 0.1f;
            }

            Global.getLogger(this.getClass()).info("Rolling Cabal bar event at " + market.getName() + ", chance: " + chance);
            shouldShow = Math.random() < chance;
        }
        return shouldShow;
    }

    @Override
    protected void regen(MarketAPI market) {
        if (this.market == market) {
            return;
        }
        super.regen(market);
        diceGameId = null;
        shouldShow = null;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        regen(dialog.getInteractionTarget().getMarket());

        dialog.getTextPanel().addPara("A gaudily-dressed " + getManOrWoman() + " is dancing on a table. Judging from " + getHisOrHer()
                + " flamboyant coloration and excessive use of jewellery, this must be a member of the notorious Starlight Cabal.");

        dialog.getOptionPanel().addOption("Approach the Cabalero", this, null);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

        done = false;
        dialog.getVisualPanel().showPersonInfo(person, true);

        optionSelected(null, OptionId.INIT);
    }

    protected void modifyRep(float amount) {
        CustomRepImpact impact = new CustomRepImpact();
        impact.delta = amount * 2f;
        RepActionEnvelope envelope = new RepActionEnvelope(
                CoreReputationPlugin.RepActions.CUSTOM, impact, dialog.getTextPanel());
        Global.getSector().adjustPlayerReputation(envelope, person);

        impact = new CustomRepImpact();
        impact.delta = amount;
        envelope = new RepActionEnvelope(
                CoreReputationPlugin.RepActions.CUSTOM, impact, dialog.getTextPanel());
        Global.getSector().adjustPlayerReputation(envelope, "cabal");
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (!(optionData instanceof OptionId)) {
            return;
        }
        OptionId option = (OptionId) optionData;

        options.clearOptions();

        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        int credits = (int) cargo.getCredits().get();

        Color h = Misc.getHighlightColor();
        Color n = Misc.getNegativeHighlightColor();

        String mOrW = getManOrWoman();
        String heOrShe = getHeOrShe();
        String hisOrHer = getHisOrHer();

        boolean galaCheck = false;
        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwCompletedStarlightGala")
                && !Global.getSector().getMemoryWithoutUpdate().getBoolean("$uwKilledPalace") && (credits >= 100000f)) {
            if (Global.getSector().getIntelManager().getFirstIntel(UW_StarlightGala.class) == null) {
                galaCheck = true;
            }
        }
        if (person.getImportance() == PersonImportance.LOW) {
            galaCheck = false;
        }
        galaCheck = false;

        CabalDiceGame game;
        int payout;
        String payoutStr;

        switch (option) {
            case INIT -> {
                text.addPara("\"Hey there, spacer,\" the " + mOrW + " greets you as you approach. "
                        + "You look like the kind of person who appreciates the fast life.\"");
                text.addPara(Misc.ucFirst(heOrShe) + " holds up a hand, with three dice between " + hisOrHer + " fingers. "
                        + "\"Care to wager on some dice? Or if you have any mind-altering substances, "
                        + "perhaps you'd like to share them with the class?\"");

                boolean haveDrugs = cargo.getCommodityQuantity(Commodities.DRUGS) >= 1;
                boolean canDice = credits >= DICE_WAGER.get(person.getImportance());

                LabelAPI label = text.addPara("The dice bet will cost %s. You have %s available.",
                        h,
                        Misc.getDGSCredits(DICE_WAGER.get(person.getImportance())),
                        Misc.getDGSCredits(credits));
                label.setHighlightColors(canDice ? h : n, h);
                label.setHighlight(Misc.getDGSCredits(DICE_WAGER.get(person.getImportance())), Misc.getDGSCredits(credits));

                options.addOption("Bet on dice", OptionId.DICE_PROMPT);
                if (!canDice) {
                    options.setEnabled(OptionId.DICE_PROMPT, false);
                    options.setTooltip(OptionId.DICE_PROMPT, "Not enough credits.");
                }
                options.addOption("Distribute drugs to the bar patrons", OptionId.DRUGS);
                if (!haveDrugs) {
                    options.setEnabled(OptionId.DRUGS, false);
                    options.setTooltip(OptionId.DRUGS, "You have no drugs in cargo.");
                }
                options.addOption("Leave", OptionId.LEAVE);
            }

            case DRUGS -> {
                text.addPara("You contact your fleet's watch officer and have them send down a unit of drugs. "
                        + "After handing them out to your appreciative fellow bargoers, "
                        + "you sit down with the Cabalero and let the psychotropics go to work.");
                text.addPara("Half an hour later, you know enough about Cabal internal politics for at least two seasons of a TriVid soap opera, "
                        + "as your new, intoxicated friend goes on and on about all the hidden drama of its great families. "
                        + "A shame you're too high yourself to be able to commit more than a few scraps to memory.");

                cargo.removeCommodity(Commodities.DRUGS, 1);
                AddRemoveCommodity.addCommodityLossText(Commodities.DRUGS, 1, text);
                modifyRep(DRUGS_REP);
                person.setImportance(PersonImportance.LOW);
                ContactIntel.addPotentialContact(ContactIntel.DEFAULT_POTENTIAL_CONTACT_PROB * 0.5f, person, market, dialog.getTextPanel());

                BarEventManager.getInstance().notifyWasInteractedWith(this);
                options.addOption("Continue", OptionId.LEAVE);
            }

            case DICE_PROMPT -> {
                if (diceGameId == null) {
                    diceGameId = CabalDiceGame.getRandomGameId();
                }
                game = CabalDiceGame.getGame(diceGameId);
                String rule = game.getRuleString();
                payout = game.getWinningsMult() * DICE_WAGER.get(person.getImportance());
                payoutStr = Misc.getDGSCredits(payout);

                text.addPara("\"Excellent. Now, I have a little gamble for ya...\"");
                text.setFontSmallInsignia();
                text.addPara(HR);
                text.addPara("Rules: " + rule, h, rule);
                text.addPara("Payout: " + payoutStr, h, payoutStr);

                text.addPara(HR);
                text.setFontInsignia();
                options.addOption("Bet " + Misc.getDGSCredits(DICE_WAGER.get(person.getImportance())), OptionId.DICE_ACCEPT);
                options.addOption("Decline and walk away", OptionId.LEAVE);
            }

            case DICE_ACCEPT -> {
                cargo.getCredits().subtract(DICE_WAGER.get(person.getImportance()));
                AddRemoveCommodity.addCreditsLossText(DICE_WAGER.get(person.getImportance()), text);

                text.addPara("The Cabalero flicks the dice onto the table.");

                game = CabalDiceGame.getGame(diceGameId);
                int[] dice = CabalDiceGame.roll();
                int sum = dice[0] + dice[1] + dice[2];
                int product = dice[0] * dice[1] * dice[2];
                String d1 = dice[0] + "";
                String d2 = dice[1] + "";
                String d3 = dice[2] + "";
                String sumStr = sum + "";
                String productStr = product + "";
                boolean success = game.isWinner(dice);

                text.setFontSmallInsignia();
                text.addPara(HR);

                if (game.printSum()) {
                    text.addPara("Roll: " + Arrays.toString(dice) + " (sum " + sumStr + ")",
                            h, d1, d2, d3, sumStr);
                } else if (game.printProduct()) {
                    text.addPara("Roll: " + Arrays.toString(dice) + " (product " + productStr + ")",
                            h, d1, d2, d3, productStr);
                } else {
                    text.addPara("Roll: " + Arrays.toString(dice), h, d1, d2, d3);
                }

                if (success) {
                    text.addPara("You win!", Misc.getPositiveHighlightColor());
                    payout = DICE_WAGER.get(person.getImportance()) * game.getWinningsMult();
                    cargo.getCredits().add(payout);
                    AddRemoveCommodity.addCreditsGainText(payout, text);
                } else {
                    text.addPara("You lost...", Misc.getNegativeHighlightColor());
                }

                text.setFontSmallInsignia();
                text.addPara(HR);
                text.setFontInsignia();

                if (success) {
                    text.addPara("The " + mOrW + " nods with a thin smile. \"Guess I lost. Well played, friend. Take your winnings, you've earned it.\"");
                    modifyRep(DICE_REP_WIN.get(person.getImportance()));
                    ContactIntel.addPotentialContact(person, market, dialog.getTextPanel());
                } else {
                    text.addPara("The " + mOrW + " grins. \"A shame, there. Better luck next time, eh?\"");
                    modifyRep(DICE_REP.get(person.getImportance()));
                    galaCheck = false;
                }
                diceGameId = null;

                if (galaCheck) {
                    options.addOption("Continue", OptionId.GALA);
                } else {
                    BarEventManager.getInstance().notifyWasInteractedWith(this);
                    options.addOption("Leave", OptionId.LEAVE);
                }
            }

            case GALA -> {
                text.addPara("\"Say, you look like you're moving up in the world.\" The Cabalero reaches into " + hisOrHer
                        + " coat and pulls out a small chit that looks to be made of solid platinum. \"A couple times per "
                        + "cycle, an... event takes place. The Starlight Gala.\"");
                text.addPara("Placing it in your hand, the " + mOrW + " says, \"The entry fee is steep. Very steep. "
                        + "Unless you walk the shadowsphere like me, of course,\" the Cabalero chuckles. \"It's an "
                        + "experience not soon forgotten. And you'll get to rub shoulders with some of the most powerful "
                        + "individuals in the Sector. Think about it.\"");

                Global.getSector().getIntelManager().addIntel(new UW_StarlightGala(dialog), false, dialog == null ? null : dialog.getTextPanel());
                BarEventManager.getInstance().notifyWasInteractedWith(this);
                options.addOption("Leave", OptionId.LEAVE);
            }

            case LEAVE -> {
                noContinue = true;
                done = true;
            }
        }
    }

    @Override
    protected String getPersonFaction() {
        return "cabal";
    }

    @Override
    protected String getPersonRank() {
        return Ranks.CITIZEN;
    }

    @Override
    protected String getPersonPost() {
        return Ranks.CITIZEN;
    }

    @Override
    protected String getPersonPortrait() {
        return null;
    }

    @Override
    protected Gender getPersonGender() {
        return Gender.ANY;
    }
}
