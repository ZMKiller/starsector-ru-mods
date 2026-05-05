package progsmod.data.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.ValueDisplayMode;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.Misc.TokenType;

import progsmod.util.SModUtils;

/** ProgSModFilterOptionsList [$fleetMember] [$selectedVariant] [$firstTimeOpened] [option1] [option2] [option3] [option4] [option5] [option6] [option7]
 *  [option1] is the option to build in hull mods 
 *  [option2] is the option to remove them
 *  [option3] is the option to select one of the ship's modules
 *  [option4] is the option to use reserve XP.
 *  [option5] is the option to increase S-Mod limit.
 *  [option6] is the option to select a different ship. 
 *  [option7] is to go back to main menu. */
@SuppressWarnings("unused")
public class PSM_CreateOptionsList extends BaseCommandPlugin {

    private static final String BUILD_IN_TEXT = "Build up to %s hull mods into ";
    private static final String REMOVE_TEXT = "Select built-in hull mods to remove from ";
    private static final String THIS_SHIP_TEXT = "this ship";
    private static final boolean CAN_REFUND_SMODS = SModUtils.Constants.XP_REFUND_FACTOR >= 0f;

	@Override
	public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null || params.isEmpty()) {
            return false;
        }

        FleetMemberAPI fleetMember = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(params.get(0).string);
        String selectedVariantKey = params.get(1).string;
        String firstTimeOpenedKey = params.get(2).string;
        boolean firstTimeOpened = params.get(2).getBoolean(memoryMap);
        String buildInOption = params.get(3).getString(memoryMap);
        String removeOption = params.get(4).getString(memoryMap);
        String moduleOption = params.get(5).getString(memoryMap);
        String reserveXPOption = params.get(6).getString(memoryMap);
        String augmentOption = params.get(7).getString(memoryMap);
        String differentShipOption = params.get(8).getString(memoryMap);
        String goBackOption = params.get(9).getString(memoryMap);

        dialog.getOptionPanel().clearOptions();

        String shipText = THIS_SHIP_TEXT;
        ShipVariantAPI selectedVariant = (ShipVariantAPI) memoryMap.get(MemKeys.LOCAL).get(selectedVariantKey);
        int nSMods = selectedVariant.getSMods().size();
        //SModUtils.initializeSModIncreaseLimit(fleetMember, nSMods);

        int nSModsLimit = SModUtils.getMaxSMods(fleetMember);
        int nRemaining = Math.max(0, nSModsLimit - nSMods);

        if (selectedVariant != fleetMember.getVariant()) {
            shipText = "module: " + selectedVariant.getHullSpec().getHullName();
        }

        // Add build in option
        dialog.getOptionPanel().addOption(String.format(BUILD_IN_TEXT + shipText, nRemaining), buildInOption);

        // Add copy build in option


        // Add in the remove option if that was allowed
        if (CAN_REFUND_SMODS) {
            dialog.getOptionPanel().addOption(REMOVE_TEXT + shipText, removeOption);
        }

        // Add options to select ship modules
        List<ShipVariantAPI> modulesWithOP = SModUtils.getModuleVariantsWithOP(fleetMember.getVariant());
        if (!modulesWithOP.isEmpty()) {
            dialog.getOptionPanel().addOption("Manage built-in hull mods for a different module", moduleOption);
        }

        // Add option to spend reserve XP if the fleet member's hull id has reserve XP
        float reserveXP = SModUtils.getReserveXP(fleetMember.getHullSpec().getBaseHullId());
        if (reserveXP >= 1f) {
            dialog.getOptionPanel().addOption("Transfer XP to this ship from XP lost by similar ships during battle", reserveXPOption);
            dialog.getOptionPanel().addSelector("XP to transfer from reserves: ", reserveXPOption, Misc.getBasePlayerColor(), 600f, 120f, 1f, reserveXP, ValueDisplayMode.X_OVER_Y, null);
            dialog.getOptionPanel().setSelectorValue(reserveXPOption, Math.max(reserveXP / 10f, 1f));
        }

        // Add in the +extra S-Mods option if that was allowed
        if (SModUtils.Constants.ALLOW_INCREASE_SMOD_LIMIT) {
            dialog.getOptionPanel().addOption(
                    String.format("Increase this ship's built-in hull mod limit from %s to %s", nSModsLimit, nSModsLimit + 1), 
                augmentOption);
            SModUtils.AugmentSPCost augmentSPCost = SModUtils.getAugmentSPCost(fleetMember);
            int nextXPCost = SModUtils.getAugmentXPCost(fleetMember);
            List<Token> storyParams = new ArrayList<>();
            storyParams.add(params.get(0));
            storyParams.add(params.get(7));
            storyParams.add(new Token("" + augmentSPCost.spCost, TokenType.LITERAL));
            storyParams.add(new Token("" + augmentSPCost.bonusXP*100, TokenType.LITERAL));
            storyParams.add(new Token("" + nextXPCost, TokenType.LITERAL));
            new PSM_SetStoryOption().execute(ruleId, dialog, storyParams, memoryMap);
        }

        // Add in the select a different ship option
        dialog.getOptionPanel().addOption("Manage a different ship", differentShipOption);
        dialog.getOptionPanel().setShortcut(differentShipOption, Global.getSettings().getCodeFor("H"), false, false, false, true);

        // Add in the back to main menu option 
        dialog.getOptionPanel().addOption("Go back", goBackOption);
        dialog.getOptionPanel().setShortcut(goBackOption, Global.getSettings().getCodeFor("ESCAPE"), false, false, false, true);

        // Add in the text panel and change the plugin
        if (firstTimeOpened) {
            dialog.getTextPanel()
                .addPara(String.format("The %s has %s out of %s built-in hull mods.", fleetMember.getShipName(), nSMods, nSModsLimit))
                .setHighlight(fleetMember.getShipName(), "" + nSMods, "" + nSModsLimit);
            
            if (CAN_REFUND_SMODS) {
                int refundPercent = (int) (SModUtils.Constants.XP_REFUND_FACTOR * 100);
                dialog.getTextPanel()
                    .addPara(String.format("Removing an existing built-in hull mod will refund %s%% of the XP spent.",refundPercent))
                    .setHighlight("" + refundPercent);
            }
            SModUtils.displayXP(dialog, fleetMember);
            memoryMap.get(MemKeys.LOCAL).set(firstTimeOpenedKey, false, 0f);
        }

		return true;
	}
}