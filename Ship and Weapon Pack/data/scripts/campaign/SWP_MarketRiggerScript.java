package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.SWP_Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;

/* Based on Nicke535's work with heavy modifications */
public class SWP_MarketRiggerScript implements EveryFrameScript {

    private static final Map<String, Map<String, MarketRiggerData>> RIGGER_DATA = new HashMap<>();

    static {
        Map<String, ShipReplacerData> highRestrictionRigger = new HashMap<>();
        highRestrictionRigger.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.75f, 2, 4));
        highRestrictionRigger.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.5f, 1, 3));
        highRestrictionRigger.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.25f, 0, 0));
        highRestrictionRigger.put("ii_ebay", new ShipReplacerData(0.5f, 1, 3));

        Map<String, ShipReplacerData> showUpMoreRiggerTT1 = new HashMap<>();
        showUpMoreRiggerTT1.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.25f, 0, 0));
        showUpMoreRiggerTT1.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.25f, 0, 0));
        showUpMoreRiggerTT1.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.33f, 0, 0));

        Map<String, ShipReplacerData> showUpMoreRiggerTT2 = new HashMap<>();
        showUpMoreRiggerTT2.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.33f, 0, 0));
        showUpMoreRiggerTT2.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.33f, 0, 0));
        showUpMoreRiggerTT2.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.5f, 0, 0));

        Map<String, ShipReplacerData> showUpMoreRiggerI1 = new HashMap<>();
        showUpMoreRiggerI1.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.13f, 0, 0));
        showUpMoreRiggerI1.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.06f, 0, 0));
        showUpMoreRiggerI1.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.13f, 0, 0));

        Map<String, ShipReplacerData> showUpMoreRiggerI2 = new HashMap<>();
        showUpMoreRiggerI2.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.17f, 0, 0));
        showUpMoreRiggerI2.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.08f, 0, 0));
        showUpMoreRiggerI2.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.17f, 0, 0));

        Map<String, ShipReplacerData> showUpMoreRiggerI3 = new HashMap<>();
        showUpMoreRiggerI3.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.25f, 0, 0));
        showUpMoreRiggerI3.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.13f, 0, 0));
        showUpMoreRiggerI3.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.25f, 0, 0));

        Map<String, ShipReplacerData> showUpMoreRiggerI4 = new HashMap<>();
        showUpMoreRiggerI4.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.33f, 0, 0));
        showUpMoreRiggerI4.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.17f, 0, 0));
        showUpMoreRiggerI4.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.33f, 0, 0));

        Map<String, ShipReplacerData> showUpMoreRiggerI5 = new HashMap<>();
        showUpMoreRiggerI5.put(Submarkets.SUBMARKET_OPEN, new ShipReplacerData(0.5f, 0, 0));
        showUpMoreRiggerI5.put(Submarkets.SUBMARKET_BLACK, new ShipReplacerData(0.25f, 0, 0));
        showUpMoreRiggerI5.put(Submarkets.GENERIC_MILITARY, new ShipReplacerData(0.5f, 0, 0));

        Map<String, MarketRiggerData> lcRiggerData = new HashMap<>();
        lcRiggerData.put("swp_cathedral", new MarketRiggerData(Arrays.asList("swp_liberator_luddic_church"), highRestrictionRigger));
        RIGGER_DATA.put(Factions.LUDDIC_CHURCH, lcRiggerData);

        Map<String, MarketRiggerData> kolRiggerData = new HashMap<>();
        kolRiggerData.put("swp_cathedral", new MarketRiggerData(Arrays.asList("swp_liberator_luddic_church"), highRestrictionRigger));
        RIGGER_DATA.put(Factions.KOL, lcRiggerData);

        Map<String, MarketRiggerData> ttRiggerData = new HashMap<>();
        ttRiggerData.put("buffalo_tritachyon", new MarketRiggerData(Arrays.asList("swp_circe"), showUpMoreRiggerTT2));
        ttRiggerData.put("phantom", new MarketRiggerData(Arrays.asList("swp_circe"), showUpMoreRiggerTT1));
        ttRiggerData.put("revenant", new MarketRiggerData(Arrays.asList("swp_circe"), showUpMoreRiggerTT1));
        ttRiggerData.put("gemini", new MarketRiggerData(Arrays.asList("swp_circe"), showUpMoreRiggerTT1));
        ttRiggerData.put("atlas", new MarketRiggerData(Arrays.asList("swp_circe"), showUpMoreRiggerTT2));
        RIGGER_DATA.put(Factions.TRITACHYON, ttRiggerData);

        Map<String, MarketRiggerData> iRiggerData = new HashMap<>();
        iRiggerData.put("mule", new MarketRiggerData(Arrays.asList("swp_circe", "swp_circe", "venture"), showUpMoreRiggerI1));
        iRiggerData.put("gemini", new MarketRiggerData(Arrays.asList("swp_circe", "swp_circe", "venture"), showUpMoreRiggerI1));
        iRiggerData.put("buffalo", new MarketRiggerData(Arrays.asList("swp_circe", "swp_circe", "venture"), showUpMoreRiggerI2));
        iRiggerData.put("tarsus", new MarketRiggerData(Arrays.asList("swp_circe", "swp_circe", "venture"), showUpMoreRiggerI2));
        iRiggerData.put("colossus", new MarketRiggerData(Arrays.asList("swp_circe", "swp_circe", "venture"), showUpMoreRiggerI5));
        iRiggerData.put("atlas", new MarketRiggerData(Arrays.asList("swp_circe", "swp_circe", "venture"), showUpMoreRiggerI4));
        RIGGER_DATA.put(Factions.INDEPENDENT, iRiggerData);
    }

    /* Counts in seconds */
    private final IntervalUtil shortTracker = new IntervalUtil(1f, 1.5f);

    /* Counts in days */
    private IntervalUtil longTracker;

    /* Updates once every longTracker period */
    private final List<String> marketsToManipulate = new ArrayList<>();

    private final HashMap<String, Float> retainedMembers = new LinkedHashMap<>();

    private final Random rand = new Random();

    @Override
    public void advance(float amount) {
        SectorAPI sector = Global.getSector();
        if (sector == null) {
            return;
        }

        float longAmount = Misc.getDays(amount);
        if (sector.isPaused()) {
            longAmount = 0f;
        }

        if (longTracker == null) {
            longTracker = new IntervalUtil(1f, 1f);
        }
        longTracker.advance(longAmount);
        shortTracker.advance(amount);

        if (longTracker.intervalElapsed()) {
            marketsToManipulate.clear();
            for (MarketAPI market : sector.getEconomy().getMarketsCopy()) {
                if (!market.isHidden()) {
                    marketsToManipulate.add(market.getId());
                }
            }

            Set<Map.Entry<String, Float>> retainedSet = retainedMembers.entrySet();
            Iterator<Map.Entry<String, Float>> iter = retainedSet.iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Float> retained = iter.next();
                float curr = retained.getValue();
                curr -= 1f;
                if (curr <= 0f) {
                    iter.remove();
                } else {
                    retained.setValue(curr);
                }
            }
        }

        if (shortTracker.intervalElapsed()) {
            for (String marketID : marketsToManipulate) {
                MarketAPI market = sector.getEconomy().getMarket(marketID);
                if (market == null) {
                    continue;
                }

                if (market.getFaction() == null) {
                    continue;
                }

                Map<String, MarketRiggerData> factionRiggerData = RIGGER_DATA.get(market.getFaction().getId());
                if (factionRiggerData == null) {
                    continue;
                }

                for (SubmarketAPI submarket : market.getSubmarketsCopy()) {
                    String submarketID = submarket.getSpecId();
                    CargoAPI cargo = submarket.getCargo();
                    List<FleetMemberAPI> toDelete = new ArrayList<>();
                    for (FleetMemberAPI member : cargo.getMothballedShips().getMembersInPriorityOrder()) {
                        if (retainedMembers.containsKey(member.getId())) {
                            continue;
                        }

                        String hullID = SWP_Util.getNonDHullId(member.getHullSpec());
                        MarketRiggerData riggerData = factionRiggerData.get(hullID);
                        if (riggerData != null) {
                            ShipReplacerData replacerData = riggerData.replacementData.get(submarketID);
                            if (replacerData == null) {
                                replacerData = new ShipReplacerData(0f, 0, 0);
                            }

                            if ((float) Math.random() < replacerData.replacementChance) {
                                List<String> variantList = riggerData.replacementHulls;
                                String variantID = variantList.get(MathUtils.getRandomNumberInRange(0, variantList.size() - 1));
                                variantID += "_Hull";

                                addShip(variantID, DModManager.getNumDMods(member.getVariant()), cargo);

                                toDelete.add(member);
                            } else {
                                int DMods = MathUtils.getRandomNumberInRange(replacerData.minDMods, replacerData.maxDMods);
                                if (DMods > 0) {
                                    DModManager.setDHull(member.getVariant());
                                    DModManager.addDMods(member, true, DMods, rand);
                                }
                                float time = 30f;
                                if (submarket instanceof BaseSubmarketPlugin base) {
                                    time = base.getMinSWUpdateInterval();
                                }
                                retainedMembers.put(member.getId(), time);
                            }
                        }
                    }

                    for (FleetMemberAPI member : toDelete) {
                        cargo.getMothballedShips().removeFleetMember(member);
                    }
                }
            }
        }
    }

    private FleetMemberAPI addShip(String variantId, int dMods, CargoAPI cargo) {
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);

        if (dMods > 0) {
            DModManager.setDHull(member.getVariant());
            DModManager.addDMods(member, true, dMods, rand);
        }

        member.getRepairTracker().setMothballed(true);
        member.getRepairTracker().setCR(0.5f);
        cargo.getMothballedShips().addFleetMember(member);
        return member;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    /* Run when paused to even further limit the small grace-periods the player might find the wrong ships in */
    @Override
    public boolean runWhilePaused() {
        return true;
    }

    static class MarketRiggerData {

        final List<String> replacementHulls;
        final Map<String, ShipReplacerData> replacementData;

        MarketRiggerData(List<String> replacementHulls, Map<String, ShipReplacerData> replacementData) {
            this.replacementHulls = replacementHulls;
            this.replacementData = replacementData;
        }
    }

    static class ShipReplacerData {

        final float replacementChance;
        final int minDMods;
        final int maxDMods;

        ShipReplacerData(float replacementChance, int minDMods, int maxDMods) {
            this.replacementChance = replacementChance;
            this.minDMods = minDMods;
            this.maxDMods = maxDMods;
        }
    }
}
