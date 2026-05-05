package data.campaign.customstart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import data.kit.AjimusUtils;
import data.scripts.HSIGenerator.HSIIPManager;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HSIStalkerStart extends CustomStart {
    protected List<String> ships = new ArrayList<>(Arrays.asList("HSI_Medusa_Stalker_Assault","HSI_DengHuo_variant","HSI_Scarab_Stalker_Assault"));
    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        ExerelinSetupData.getInstance().freeStart = true;
        PlayerFactionStore.setPlayerFactionIdNGC("HSI");

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");
        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog,data,(String)null,ships);

        data.addScript(new Script() {
            @Override
            public void run() {

                Global.getSector().getPlayerFaction().setRelationship("HSI", 0.8f);

            }
        });

        data.addScriptBeforeTimePass(new Script() {
            public void run() {
                //PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);
                Global.getSector().getMemoryWithoutUpdate().set("$HSISS_Start",true);
                AjimusUtils.setTraitorTrigger();
            }
        });

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
