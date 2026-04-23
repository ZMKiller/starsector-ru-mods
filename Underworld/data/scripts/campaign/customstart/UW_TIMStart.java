package data.scripts.campaign.customstart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.Nex_NGCFinalize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import exerelin.utilities.NexUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UW_TIMStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(Arrays.asList(new String[]{
        "uw_infernus_starter"
    }));

    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        ExerelinSetupData.getInstance().freeStart = true;
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);

        FactionAPI faction = Global.getSector().getFaction(Factions.SCAVENGERS);
        ships.add(getShip(faction, ShipRoles.COMBAT_FREIGHTER_MEDIUM));
        ships.add(getShip(faction, ShipRoles.COMBAT_FREIGHTER_MEDIUM));
        ships.add(getShip(faction, ShipRoles.COMBAT_MEDIUM));
        ships.add(getShip(faction, ShipRoles.CARRIER_SMALL));
        ships.add(getShip(faction, ShipRoles.COMBAT_FREIGHTER_SMALL));
        ships.add(getShip(faction, ShipRoles.COMBAT_FREIGHTER_SMALL));
        ships.add(getShip(faction, ShipRoles.COMBAT_SMALL));
        ships.add(getShip(faction, ShipRoles.COMBAT_SMALL));

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);
        Nex_NGCFinalize.addStartingDModScript(memoryMap.get(MemKeys.LOCAL));

        data.addScript(() -> {
            for (FactionAPI faction1 : Global.getSector().getAllFactions()) {
                if (faction1.isNeutralFaction() || faction1.isPlayerFaction()) {
                    continue;
                }
                switch (faction1.getId()) {
                    case Factions.PIRATES ->
                        Global.getSector().getPlayerFaction().setRelationship(faction1.getId(), RepLevel.VENGEFUL);
                    case Factions.INDEPENDENT ->
                        Global.getSector().getPlayerFaction().setRelationship(faction1.getId(), 0f);
                    default -> {
                        float currLevel = Global.getSector().getPlayerFaction().getRelationship(faction1.getId());
                        if (currLevel > -0.35f) {
                            Global.getSector().getPlayerFaction().setRelationship(faction1.getId(), -0.35f);
                        }
                    }
                }
            }
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            Random random = new Random(NexUtils.getStartingSeed());
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (member.getHullId().contentEquals("uw_tim")) {
                    ShipVariantAPI v = member.getVariant().clone();
                    v.setSource(VariantSource.REFIT);
                    v.setHullVariantId(Misc.genUID());
                    member.setVariant(v, false, false);

                    member.getVariant().addPermaMod(HullMods.COMP_STRUCTURE);
                    for (int i = 0; i < 10; i++) {
                        DModManager.addDMods(member, true, 10, random);
                        if (member.getVariant().hasHullMod(HullMods.DEGRADED_DRIVE_FIELD)) {
                            member.getVariant().removePermaMod(HullMods.DEGRADED_DRIVE_FIELD);
                        }
                        if (member.getVariant().hasHullMod(HullMods.INCREASED_MAINTENANCE)) {
                            member.getVariant().removePermaMod(HullMods.INCREASED_MAINTENANCE);
                        }
                        if (member.getVariant().hasHullMod(HullMods.ERRATIC_INJECTOR)) {
                            member.getVariant().removePermaMod(HullMods.ERRATIC_INJECTOR);
                        }
                        if (member.getVariant().hasHullMod("degraded_life_support")) {
                            member.getVariant().removePermaMod("degraded_life_support");
                        }
                        if (member.getVariant().hasHullMod("faulty_auto")) {
                            member.getVariant().removePermaMod("faulty_auto");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_automation")) {
                            member.getVariant().removePermaMod("vayra_damaged_automation");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_everything")) {
                            member.getVariant().removePermaMod("vayra_damaged_everything");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_lifesupport")) {
                            member.getVariant().removePermaMod("vayra_damaged_lifesupport");
                        }
                    }

                    Global.getSector().addScript(new EveryFrameScript() {

                        private boolean done = false;

                        @Override
                        public boolean isDone() {
                            return done;
                        }

                        @Override
                        public boolean runWhilePaused() {
                            return true;
                        }

                        @Override
                        public void advance(float amount) {
                            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                                if (member.getHullId().contentEquals("uw_tim")) {
                                    if (member.getShipName().contentEquals("The Infernal Machine")) {
                                        done = true;
                                        int numDMods = 0;
                                        for (String modId : member.getVariant().getHullMods()) {
                                            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
                                            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                                                numDMods++;
                                            }
                                        }
                                        Global.getSector().getMemoryWithoutUpdate().set("$uwTIMMember", member.getId());
                                        Global.getSector().getMemoryWithoutUpdate().set("$uwRestoreSeed", Misc.genRandomSeed());
                                        Global.getSector().getMemoryWithoutUpdate().set("$uwStartingDMods", (long) numDMods);
                                        break;
                                    }

                                    member.setShipName("The Infernal Machine");
                                }
                            }
                        }
                    });
                } else {
                    ShipVariantAPI v = member.getVariant().clone();
                    v.setSource(VariantSource.REFIT);
                    v.setHullVariantId(Misc.genUID());
                    member.setVariant(v, false, false);

                    DModManager.addDMods(member, true, 3, random);
                    for (int i = 0; i < 5; i++) {
                        if (member.getVariant().hasHullMod(HullMods.DEGRADED_ENGINES)) {
                            member.getVariant().removePermaMod(HullMods.DEGRADED_ENGINES);
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod(HullMods.INCREASED_MAINTENANCE)) {
                            member.getVariant().removePermaMod(HullMods.INCREASED_MAINTENANCE);
                            DModManager.addDMods(member, true, 1, random);
                        }
                        if (member.getVariant().hasHullMod(HullMods.ERRATIC_INJECTOR)) {
                            member.getVariant().removePermaMod(HullMods.ERRATIC_INJECTOR);
                            DModManager.addDMods(member, true, 1, random);
                        }
                    }
                }
            }
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        });

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
