package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin;
import com.fs.starfarer.api.campaign.ResourceCostPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;

public class UW_ExtortFleet extends BaseCommandPlugin {

    public static final Set<String> ALLOWED_FLEET_TYPES = new HashSet<>(Arrays.asList(new String[]{
        FleetTypes.TRADE, FleetTypes.TRADE_LINER, FleetTypes.TRADE_SMALL, FleetTypes.TRADE_SMUGGLER,
        FleetTypes.FOOD_RELIEF_FLEET, FleetTypes.SCAVENGER_SMALL, FleetTypes.SCAVENGER_MEDIUM,
        FleetTypes.SCAVENGER_LARGE
    }));

    public static final float CARGO_DROP_MULT = 0.5f;
    public static final float FLEET_FP_XP_MULT = 20f;
    public static final float FLEET_VALUE_EXTORT_MULT = 0.15f;
    public static final String MEMORY_FLAG_CARGO = "$uw_fleetCargo";
    public static final float PATROL_ALERT_RANGE_HYPERSPACE = 2500;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        String arg = params.get(0).getString(memoryMap);
        SectorEntityToken target = dialog.getInteractionTarget();
        MemoryAPI mem = memoryMap.get(MemKeys.ENTITY);

        if (target == null || !(target instanceof CampaignFleetAPI)) {
            return false;
        }
        CampaignFleetAPI fleet = (CampaignFleetAPI) target;

        switch (arg) {
            case "addOptions" ->
                addExtortOptions(fleet, dialog, mem);
            case "getCredits" -> {
                return getCredits(fleet, mem) > 0;
            }
            case "getCargo" -> {
                return !getCargo(fleet, mem).isEmpty();
            }
            case "printCargo" ->
                printCargo(fleet, mem, dialog.getTextPanel());
            case "modifyRep" -> {
                return modifyRep(fleet, params.get(1).getString(memoryMap), dialog);
            }
            case "addXP" ->
                addXP(fleet);
            case "dropCargo" ->
                dropCargo(fleet, mem, dialog, memoryMap);
            case "rollViolentResistance" ->
                rollViolentResistance(fleet, memoryMap.get(MemKeys.LOCAL).getString("$uw_extortMode"));
            case "alertPatrols" ->
                alertPatrols(fleet);
            case "canExtortFleet" -> {
                return canExtortFleet(fleet, mem);
            }
        }

        return true;
    }

    protected void addCargoEntry(ResourceCostPanelAPI cost, String commodityId, int available, int toDrop) {
        Color curr = Global.getSector().getPlayerFaction().getColor();
        cost.addCost(commodityId, "" + toDrop + " (" + available + ")", curr);
    }

    protected void addExtortOptions(CampaignFleetAPI fleet, InteractionDialogAPI dialog, MemoryAPI mem) {
        if (!FleetInteractionDialogPluginImpl.inConversation) {
            return;
        }
        if (mem.getBoolean("$uw_extortResistViolently")) {
            return;
        }

        if (!dialog.getOptionPanel().hasOption("uw_extortCredits")) {
            dialog.getOptionPanel().addOption("\"There is a toll for free passage here!\"", "uw_extortCredits");
        }
        if (!dialog.getOptionPanel().hasOption("uw_extortCargo")) {
            dialog.getOptionPanel().addOption("\"Jettison your cargo or face the consequences!\"", "uw_extortCargo");
        }

        if (fleet.getAI() instanceof CampaignFleetAIAPI) {
            CampaignFleetAIAPI ai = (CampaignFleetAIAPI) fleet.getAI();
            EncounterOption option = ai.pickEncounterOption(null, Global.getSector().getPlayerFleet(), true);
            if ((option != EncounterOption.DISENGAGE) && (option != EncounterOption.HOLD_VS_STRONGER)) {
                dialog.getOptionPanel().setEnabled("uw_extortCredits", false);
                dialog.getOptionPanel().setEnabled("uw_extortCargo", false);
                dialog.getOptionPanel().setTooltip("uw_extortCredits", "Only works on fleets that are intimidated.");
                dialog.getOptionPanel().setTooltip("uw_extortCargo", "Only works on fleets that are intimidated.");
            }
        }
        if (isHighRepImpact(fleet)) {
            String factionName = fleet.getFaction().getDisplayNameWithArticle();
            String isOrAre = fleet.getFaction().getDisplayNameIsOrAre();
            //String relationship = Misc.lcFirst(fleet.getFaction().getRelToPlayer().getLevel().getDisplayName());
            String extortConfirm = factionName + " " + isOrAre + " not currently hostile, and you have been positively identified. "
                    + "Are you sure you want to attempt robbing one of their fleets?";
            dialog.getOptionPanel().addOptionConfirmation("uw_extortCredits", extortConfirm, "Yes", "Never mind");
            dialog.getOptionPanel().addOptionConfirmation("uw_extortCargo", extortConfirm, "Yes", "Never mind");
        }

        // in case the player accidentally closes the comm link
        mem.unset(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS);
    }

    protected int addXP(CampaignFleetAPI target) {
        int xp = 0;
        for (FleetMemberAPI member : target.getMembersWithFightersCopy()) {
            if (member.isFighterWing()) {
                continue;
            }
            xp += member.getFleetPointCost();
        }
        xp *= FLEET_FP_XP_MULT;
        Global.getSector().getPlayerFleet().getCommander().getStats().addXP(xp);

        return xp;
    }

    protected void alertPatrols(CampaignFleetAPI victim) {
        boolean hyperspace = victim.getContainingLocation().isHyperspace();
        boolean cryForHelp = false;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (CampaignFleetAPI fleet : victim.getContainingLocation().getFleets()) {
            MemoryAPI mem = fleet.getMemoryWithoutUpdate();
            // only patrols can respond
            if (!mem.getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET)) {
                continue;
            }
            // don't do anything if we don't like the victim
            if (fleet.getFaction().isAtBest(victim.getFaction(), RepLevel.INHOSPITABLE)) {
                continue;
            }
            // if we like the player better than we like the victim, don't do anything
            RepLevel victimRep = fleet.getFaction().getRelationshipLevel(victim.getFaction());
            if (fleet.getFaction().getRelationshipLevel(Factions.PLAYER).isAtWorst(victimRep.getOneBetter())) {
                continue;
            }
            // hyperspace range limit
            if (hyperspace && MathUtils.isWithinRange(fleet, victim, PATROL_ALERT_RANGE_HYPERSPACE)) {
                continue;
            }
            // only act if we can see the player or victim
            if (!canSeeFleet(fleet, victim) && !canSeeFleet(fleet, playerFleet)) {
                continue;
            }

            cryForHelp = true;
            Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE, "uw_extort_response", true, 5);
            Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "uw_extort_response", true, 5);
            //Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF, "uw_extort_response", true, 5);
            if (!mem.getBoolean(MemFlags.FLEET_BUSY)) {
                fleet.addAssignmentAtStart(FleetAssignment.INTERCEPT, playerFleet, 5, null);
                Global.getSector().addPing(fleet, "uw_extortResponse");
                fleet.addFloatingText("Stop, criminal!", fleet.getFaction().getBaseUIColor(), 1.5f);
            }
        }
        if (cryForHelp) {
            Global.getSector().addPing(victim, "uw_extortCryForHelp");
            victim.addFloatingText("Help me!", victim.getFaction().getBaseUIColor(), 1.5f);
        }
    }

    protected boolean canExtortFleet(CampaignFleetAPI fleet, MemoryAPI mem) {
        if (fleet.getFaction().isPlayerFaction()) {
            return false;
        }

        /* Disallow extorting fleets that are already pushing to fight! */
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet != null && fleet.getAI() != null) {
            EncounterOption option = fleet.getAI().pickEncounterOption(null, playerFleet);
            if (fleet.isHostileTo(playerFleet) && (option == EncounterOption.ENGAGE || option == EncounterOption.HOLD)) {
                return false;
            }
        }

        if (mem.contains(MemFlags.MEMORY_KEY_TRADE_FLEET)) {
            return true;
        }
        if (mem.contains(MemFlags.MEMORY_KEY_SMUGGLER)) {
            return true;
        }
        if (mem.contains(MemFlags.MEMORY_KEY_SCAVENGER)) {
            return true;
        }
        return ALLOWED_FLEET_TYPES.contains(mem.getString(MemFlags.MEMORY_KEY_FLEET_TYPE));
    }

    protected boolean canSeeFleet(CampaignFleetAPI seer, CampaignFleetAPI target) {
        VisibilityLevel vis = target.getVisibilityLevelTo(seer);
        return vis == VisibilityLevel.COMPOSITION_DETAILS || vis == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS;
    }

    @SuppressWarnings("unchecked")
    protected void dropCargo(CampaignFleetAPI fleet, MemoryAPI mem,
            InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        //CustomCampaignEntityAPI pods = Misc.addCargoPods(fleet.getContainingLocation(), fleet.getLocation());
        List<CargoStackAPI> cargoStacks = mem.contains(MEMORY_FLAG_CARGO) ? (List<CargoStackAPI>) mem.get(
                MEMORY_FLAG_CARGO) : getCargo(fleet, mem);
        CargoAPI loot = Global.getFactory().createCargo(true);

        for (CargoStackAPI stack : cargoStacks) {
            int count = (int) (stack.getSize() * CARGO_DROP_MULT);
            //pods.getCargo().addCommodity(stack.getCommodityId(), count);
            loot.addCommodity(stack.getCommodityId(), count);
        }
        // remove fleet cargo separately 
        // so it doesn't empty subsequent stacks of the same commodity type while we're still iterating over them
        for (CargoStackAPI stack : cargoStacks) {
            int count = (int) (stack.getSize() * CARGO_DROP_MULT);
            fleet.getCargo().removeCommodity(stack.getCommodityId(), count);
        }

        dialog.getVisualPanel().setVisualFade(0, 0);
        dialog.hideTextPanel();
        final InteractionDialogAPI dialogF = dialog;
        final Map<String, MemoryAPI> memoryMapF = memoryMap;
        dialog.getVisualPanel().showLoot("Robbed", loot, false, true, true, () -> {
            dialogF.showTextPanel();
            new EndConversation().execute(null, dialogF, Misc.tokenize("NO_CONTINUE"), memoryMapF);
            dialogF.setPromptText("You decide to...");
        });
        dialog.getOptionPanel().clearOptions();
        dialog.setPromptText("");
    }

    protected List<CargoStackAPI> getCargo(CampaignFleetAPI fleet, MemoryAPI mem) {
        List<CargoStackAPI> cargo = new ArrayList<>(fleet.getCargo().getStacksCopy().size());
        float value = 0;

        Global.getLogger(this.getClass()).info("Checking cargo for fleet " + fleet.getName());

        for (CargoStackAPI stack : fleet.getCargo().getStacksCopy()) {
            Global.getLogger(this.getClass()).info("Fleet " + fleet.getName() + " has cargo stack " + stack.getCommodityId());
            if (stack.isNull()) {
                continue;
            }
            if (stack.isCrewStack() || stack.isMarineStack()) {
                continue;
            }
            int count = (int) (stack.getSize());
            value += stack.getBaseValuePerUnit() * count;

            cargo.add(stack);
        }
        mem.set(MEMORY_FLAG_CARGO, cargo, 0);
        mem.set("$uw_fleetCargoValue", value, 0);

        return cargo;
    }

    protected float getCredits(CampaignFleetAPI fleet, MemoryAPI mem) {
        float value = 0;
        for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
            if (member.isFighterWing()) {
                continue;
            }
            value += member.getBaseBuyValue();
        }
        value *= FLEET_VALUE_EXTORT_MULT;
        value = (float) Math.floor(value);

        mem.set("$uw_extortCredits", value, 0);
        mem.set("$uw_extortCreditsStr", Misc.getDGSCredits(value), 0);

        return value;
    }

    protected boolean isHighRepImpact(CampaignFleetAPI target) {
        if (!target.knowsWhoPlayerIs()) {
            return false;
        }
        if (target.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT)) {
            return false;
        }
        return !target.getFaction().isAtBest(Factions.PLAYER, RepLevel.INHOSPITABLE);
    }

    protected ResourceCostPanelAPI makeCostPanel(TextPanelAPI text, Color color, Color color2) {
        ResourceCostPanelAPI cost = text.addCostPanel("Cargo to drop (available)", 67, color, color2);
        cost.setNumberOnlyMode(true);
        cost.setWithBorder(false);
        cost.setAlignment(Alignment.LMID);
        return cost;
    }

    protected boolean modifyRep(SectorEntityToken entity, String type, InteractionDialogAPI dialog) {
        float delta = -0.02f;
        if (type.equals("cargo")) {
            delta = -0.03f;
        }

        if (entity.getActivePerson() != null) {
            CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
            impact.delta = delta * 2;

            CoreReputationPlugin.RepActionEnvelope envelope = new CoreReputationPlugin.RepActionEnvelope(
                    CoreReputationPlugin.RepActions.CUSTOM, impact, dialog.getTextPanel());
            Global.getSector().adjustPlayerReputation(envelope, entity.getActivePerson());
        }

        CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
        impact.delta = delta;
        impact.limit = RepLevel.HOSTILE;
        if (entity instanceof CampaignFleetAPI fleet) {
            if (isHighRepImpact(fleet)) {
                impact.ensureAtBest = RepLevel.INHOSPITABLE;
            }
        }

        CoreReputationPlugin.RepActionEnvelope envelope = new CoreReputationPlugin.RepActionEnvelope(
                CoreReputationPlugin.RepActions.CUSTOM, impact, dialog.getTextPanel());
        ReputationActionResponsePlugin.ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
                envelope, entity.getFaction().getId());
        return result.delta != 0;
    }

    @SuppressWarnings("unchecked")
    protected void printCargo(CampaignFleetAPI fleet, MemoryAPI mem, TextPanelAPI text) {
        text.setFontVictor();
        text.setFontSmallInsignia();
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        Color color = playerFaction.getColor();
        Color color2 = playerFaction.getDarkUIColor();

        List<CargoStackAPI> cargoStacks = mem.contains(MEMORY_FLAG_CARGO) ? (List<CargoStackAPI>) mem.get(
                MEMORY_FLAG_CARGO) : getCargo(fleet, mem);

        text.addParagraph("-----------------------------------------------------------------------------");
        //text.addParagraph("Cargo scanner: Cargo to drop (available)", color);

        ResourceCostPanelAPI cost = makeCostPanel(text, color, color2);
        int numEntries = 0;

        for (CargoStackAPI stack : cargoStacks) {
            if (numEntries >= 3) {
                cost = makeCostPanel(text, color, color2);
                numEntries = 0;
            }
            numEntries++;
            addCargoEntry(cost, stack.getCommodityId(), (int) stack.getSize(), (int) stack.getSize() / 2);
            cost.update();
        }
        text.addParagraph("-----------------------------------------------------------------------------");
        text.setFontInsignia();
    }

    protected boolean rollViolentResistance(CampaignFleetAPI fleet, String extortType) {
        MemoryAPI mem = fleet.getMemoryWithoutUpdate();
        // prevent multiple rolls
        if (mem.contains("$uw_extortResistViolently")) {
            return mem.getBoolean("$uw_extortResistViolently");
        }

        float chance = 0.1f;    // base chance
        //if (extortType.equals("cargo")) chance += 0.1f;    // more likely to resist if extorting cargo; can be circumvented by asking for credits first
        // hate psychopaths
        if (Global.getSector().getPlayerFleet().getMemoryWithoutUpdate().getBoolean("$uw_extortMurderous")) {
            chance += 0.2f;
        }
        // stronger fleets have a chance of raging and picking a fight with player (they won't give in regardless)
        float relStrength = mem.getFloat("$relativeStrength");
        if (relStrength >= 1) {
            chance += 0.25f;
        } else if (relStrength >= 0) {
            chance += 0.1f;
        }

        // reputation effects
        if (fleet.getFaction().isAtBest(Factions.PLAYER, RepLevel.VENGEFUL)) {
            chance += 0.2f;
        }
        PersonAPI person = fleet.getActivePerson();
        float personRel = fleet.getActivePerson().getRelToPlayer().getRel();
        if (personRel < 0) {
            chance += -personRel / 2f;
        }

        // enemy officer personality
        switch (person.getPersonalityAPI().getId()) {
            case "reckless" ->
                chance = 1f;    // guaranteed resist
            case "aggressive" ->
                chance *= 1.5f;
            case "cautious" ->
                chance *= 0.5f;
            case "timid" ->
                chance = 0f;    // guaranteed no resist
        }
        boolean resist = Math.random() <= chance;
        mem.set("$uw_extortResistViolently", resist, 7);

        return resist;
    }
}
