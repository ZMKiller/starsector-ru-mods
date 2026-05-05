package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomProductionPickerDelegateImpl;
import com.fs.starfarer.api.campaign.FactionProductionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.HPSID.HSIHPSIDLevelIntel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HSIContactForge extends BaseCommandPlugin{
    protected PersonAPI person;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {
        dialog.showCustomProductionPicker(new HSIContactForgeDelegate(dialog,memoryMap));
        return false;
    }

    public static class HSIContactForgeDelegate extends BaseCustomProductionPickerDelegateImpl {
        private boolean high = false;
        private int level = 0;

        private final InteractionDialogAPI  dialog;
        private final Map<String,MemoryAPI> memoryMap;
        private float leftValue = 0;
        public HSIContactForgeDelegate(InteractionDialogAPI dialog,Map<String,MemoryAPI> memoryMap){
            HSIHPSIDLevelIntel.Stage curr = HSIHPSIDLevelIntel.Stage.START;
            if(Global.getSector()!=null){
                if(Global.getSector().getMemoryWithoutUpdate().contains(HSIHPSIDLevelIntel.KEY)){
                    HSIHPSIDLevelIntel intel = HSIHPSIDLevelIntel.getInstance();
                    curr = (HSIHPSIDLevelIntel.Stage) intel.getCurrentStage().id;
                    this.leftValue = intel.getLeftValue();
                }
            }
            this.level= curr.ordinal();
            this.high = this.level>=HSIHPSIDLevelIntel.Stage.SENIOR_SHIP_PROVIDER.ordinal();
            this.dialog = dialog;
            this.memoryMap = memoryMap;
            if(Global.getSector().getPlayerFleet()!=null&&Global.getSector().getPlayerFleet().getCargo().getCredits().get()<leftValue){
                leftValue = (float)(Math.floor(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
            }
        }
        @Override
        public Set<String> getAvailableShipHulls() {
            Set<String> toR = new HashSet<>();
            for(ShipHullSpecAPI spec:Global.getSettings().getAllShipHullSpecs()){
                if(spec.getTags().contains("HSI_bp")){
                    toR.add(spec.getHullId());
                }
                if(high&&spec.getTags().contains("HSI_Elite_bp")){
                    toR.add(spec.getHullId());
                }
                if(high&&spec.getTags().contains("HSI_Ukiyo")){
                    toR.add(spec.getHullId());
                }
                if (spec.hasTag(Items.TAG_NO_DEALER)) continue;
                if (spec.hasTag(Tags.NO_SELL) && !spec.hasTag(Items.TAG_DEALER)) continue;
                if (spec.hasTag(Tags.RESTRICTED)) continue;
                if (spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.HIDE_IN_CODEX)) continue;
                if (spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE)) continue;
                if (spec.isDefaultDHull()) continue; // || spec.isDHull()) continue;
                if (spec.isDHullOldMethod()) continue;
                if ("shuttlepod".equals(spec.getHullId())) continue;


                if(Global.getSector().getPlayerFaction()!=null){
                    toR.addAll(Global.getSector().getPlayerFaction().getKnownShips());
                }
            }
            return toR;
        }

        @Override
        public Set<String> getAvailableWeapons() {
            Set<String> toR = new HashSet<>();
            for(WeaponSpecAPI spec:Global.getSettings().getAllWeaponSpecs()){
                if (spec.hasTag(Items.TAG_NO_DEALER)) continue;
                if (spec.hasTag(Tags.NO_SELL) && !spec.hasTag(Items.TAG_DEALER)) continue;
                if (spec.hasTag(Tags.RESTRICTED)) continue;
                if (spec.getAIHints().contains(WeaponAPI.AIHints.SYSTEM)) continue;

                if(spec.getTags().contains("HSI_bp")&&!spec.getTags().contains("HSI_Elite_bp")){
                    toR.add(spec.getWeaponId());
                }
                if(high&&spec.getTags().contains("HSI_Elite_bp")){
                    toR.add(spec.getWeaponId());
                }
                //if(level>2&&Global.getSector().getPlayerFaction()!=null&&)
                if(Global.getSector().getPlayerFaction()!=null){
                    toR.addAll(Global.getSector().getPlayerFaction().getKnownWeapons());
                }
            }
            return toR;
        }

        @Override
        public Set<String> getAvailableFighters() {
            Set<String> toR = new HashSet<>();
            for(FighterWingSpecAPI spec:Global.getSettings().getAllFighterWingSpecs()){
                if (spec.hasTag(Items.TAG_NO_DEALER)) continue;
                if (spec.hasTag(Tags.NO_SELL) && !spec.hasTag(Items.TAG_DEALER)) continue;
                if (spec.hasTag(Tags.RESTRICTED)) continue;

                if(spec.getTags().contains("HSI_bp")&&!spec.getTags().contains("HSI_Elite_bp")){
                    toR.add(spec.getId());
                }
                if(high&&spec.getTags().contains("HSI_Elite_bp")){
                    toR.add(spec.getId());
                }
                //if(level>2&&Global.getSector().getPlayerFaction()!=null&&)
            }
            if(Global.getSector().getPlayerFaction()!=null){
                toR.addAll(Global.getSector().getPlayerFaction().getKnownFighters());
            }
            return toR;
        }

        @Override
        public float getCostMult() {
            return 1.5f+(level-3)*(-0.1f);
        }

        @Override
        public float getMaximumValue() {
            return leftValue;
        }

        @Override
        public boolean withQuantityLimits() {
            return false;
        }

        @Override
        public void notifyProductionSelected(FactionProductionAPI factionProduction) {
            HSIHPSIDLevelIntel.getInstance().getProductionQueue().add(factionProduction.clone(),60f);
            int cost = factionProduction.getTotalCurrentCost();
            HSIHPSIDLevelIntel.getInstance().setLeftValue(HSIHPSIDLevelIntel.getInstance().getLeftValue()-cost);
            AddRemoveCommodity.addCreditsLossText(cost, dialog.getTextPanel());
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(cost);
            Misc.adjustRep("HSI",Math.min(0.1f,cost/200000f),null);
            FireBest.fire(null, dialog, memoryMap, "HSIForgePicked");
        }
    }
}
