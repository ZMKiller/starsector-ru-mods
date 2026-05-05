package progsmod.data.combat;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;

import progsmod.util.SModUtils;
public class ContributionTracker extends BaseEveryFrameCombatPlugin {

    HullAndArmorCombiner hullAndArmor = new HullAndArmorCombiner();
    HullArmorShieldCombiner hullArmorShield = new HullArmorShieldCombiner();

    private final Set<String> playerShips = new HashSet<>();
    private final Set<String> enemyShips = new HashSet<>();
    private static final Map<String, String> shipToFleetMemberMap = new HashMap<>();

    public static Map<String, String> getShipToFleetMemberMap() {
        return shipToFleetMemberMap;
    }

    // For each enemy ship, the amount of each type of damage
    // dealt by each player ship. Stored in the order 
    //    [hull, armor, shields]
    // Resets each combat update interval
    private final Map<String, Map<String, float[]>> damageReceivedByEnemy = new HashMap<>();
    // For each enemy ship, the amount of each type of damage
    // dealt to each player ship. Stored in the order
    //    [hull, armor, shields]
    // Resets each combat update interval
    private final Map<String, Map<String, float[]>> damageDealtByEnemy = new HashMap<>();
    
    // totalContribution[c][i][j] is the total contribution of type [c]
    // for enemy ship [i] gained by player ship [j] over all combat intervals.
    // ATTACK contribution is gained for an interval by dealing the most 
    //   combined hull and armor damage to an enemy ship;
    // DEFENSE contribution is gained for an interval by taking the most
    //   combined hull, armor, and shields damage from an enemy ship;
    // SUPPORT contribution is gained for an interval by dealing or taking
    //   any hull, armor, or shields damage to or from an enemy ship but
    //   not qualifying for ATTACK or DEFENSE contribution
    // SUPPORT contribution is only gained during intervals where the enemy ship in question
    // receives hull or armor damage.
    public enum ContributionType {ATTACK, DEFENSE, SUPPORT}
    private static final Map<ContributionType, Map<String, Map<String, Float>>> totalContribution = new EnumMap<>(ContributionType.class);

    public static Map<ContributionType, Map<String, Map<String, Float>>> getContributionTable() {
        return totalContribution;
    }

    private CombatEngineAPI engine;
    private float time = 0f;
    private float lastUpdateTime = 0f;

    /** Maps wings and modules to their parent ship or station. */
    private final Map<String, ShipAPI> baseShipTable = new HashMap<>();

    @Override
    public void init(CombatEngineAPI engine) {
        // Reset data for a new combat
        playerShips.clear();
        enemyShips.clear();
        baseShipTable.clear();
        shipToFleetMemberMap.clear();
        damageReceivedByEnemy.clear();
        damageDealtByEnemy.clear();
        totalContribution.clear();
        for (ContributionType type : ContributionType.values()) {
            totalContribution.put(type, new HashMap<String, Map<String, Float>>());
        }
        time = 0f;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        
        if (engine == null || engine.isPaused() || engine.isSimulation()) {
            return;
        }

        // If haven't even loaded the mod, return
        if (SModUtils.Constants.COMBAT_UPDATE_INTERVAL <= 0f) {
            return;
        }

        // At the end of a combat interval, for each eligible enemy ship that
        // took hull or armor damage, give contributions to eligible
        // player ships.
        if (time - lastUpdateTime >= SModUtils.Constants.COMBAT_UPDATE_INTERVAL) {
            performCombatUpdate();
            updateShipList(FleetSide.PLAYER);
            updateShipList(FleetSide.ENEMY);
            lastUpdateTime = time;
        }
        time += amount;
    }
    
    /** Looks through the deployed fleet members for [side]
     *  and adds all ships not already in [playerShips] (if side == 0) 
     *  or [enemyShips] (if side == 1) to the respective set. */
    private void updateShipList(FleetSide side) {
        Set<String> list = side == FleetSide.PLAYER ? playerShips : enemyShips;
        for (DeployedFleetMemberAPI dfm : engine.getFleetManager(side).getDeployedCopyDFM()) {
            // We don't care about damage that fighters take
            if (dfm.isFighterWing() || dfm.getShip() == null) {
                continue;
            }
            ShipAPI ship = dfm.getShip();
            String shipId = ship.getId();
            if (!list.contains(shipId)) {
                list.add(shipId);
                ShipAPI baseShip = getBaseShip(ship);
                if (baseShip != null && baseShip.getFleetMember() != null) {
                    shipToFleetMemberMap.put(shipId, baseShip.getFleetMemberId());
                }
                // Note: DamageListener listens for damage taken only
                ship.addListener(new ProgSModDamageListener(this));
            }
        }
    }

    /** If the argument is a ship, returns that ship.
     *  If the argument is a wing, returns the wing's source ship.
     *  If the argument is a module, returns the module's base ship/station. */
    private ShipAPI getBaseShip(ShipAPI shipWingOrModule) {
        return getBaseShip(shipWingOrModule, new HashSet<ShipAPI>());
    }

    private ShipAPI getBaseShip(ShipAPI shipWingOrModule, Set<ShipAPI> seen) {
        if (shipWingOrModule == null) {
            return null;
        }
        ShipAPI memo = baseShipTable.get(shipWingOrModule.getId());
        if (memo != null) {
            return memo;
        }
        if (seen.contains(shipWingOrModule)) {
            baseShipTable.put(shipWingOrModule.getId(), shipWingOrModule);
            return shipWingOrModule;
        }
        seen.add(shipWingOrModule);
        // The "ship" in question is a drone
        if (shipWingOrModule.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP)) {
            ShipAPI base = getBaseShip((ShipAPI) shipWingOrModule.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP), seen);
            baseShipTable.put(shipWingOrModule.getId(), base);
            return base;
        }
        // Possible to have wings come from a module of a station
        // and maybe even have modules of modules?
        // so function needs to be recursive
        if (shipWingOrModule.isFighter()) {
            ShipAPI base = null;
            if (shipWingOrModule.getWing() == null ||
                    shipWingOrModule.getWing().getSourceShip() == null) {
                // If the fighter has no source ship but has a fleet member,
                // just return the fighter itself
                if (shipWingOrModule.getFleetMember() != null) {
                    base = shipWingOrModule;
                }
            }
            else {
                base = getBaseShip(shipWingOrModule.getWing().getSourceShip(), seen);
            }
            baseShipTable.put(shipWingOrModule.getId(), base);
            return base;
        }
        if (shipWingOrModule.isStationModule()) {
            ShipAPI base = null;
            if (shipWingOrModule.getParentStation() == null) {
                // If the module has no parent station but has a fleet member,
                // just return the module itself
                if (shipWingOrModule.getFleetMember() != null) {
                    base = shipWingOrModule;
                }
            }
            else {
                base = getBaseShip(shipWingOrModule.getParentStation(), seen);
            }
            baseShipTable.put(shipWingOrModule.getId(), base);
            return base;
        }
        baseShipTable.put(shipWingOrModule.getId(), shipWingOrModule);
        return shipWingOrModule;
    }

    /** Assumes that [shipWingOrModuleId] already has an entry in
     *  [baseShipTable], i.e. it dealt or received damage at least once. */
    private String getBaseShipId(String shipWingOrModuleId) {
        ShipAPI base = baseShipTable.get(shipWingOrModuleId);
        return base == null ? null : base.getId();
    }

    private void registerDamage(Object source, CombatEntityAPI target, ApplyDamageResultAPI damage) {
        // Only care about ship to ship damage
        if (!(source instanceof ShipAPI) || !(target instanceof ShipAPI)) {
            return;
        }
        ShipAPI dealer = (ShipAPI) source;
        ShipAPI receiver = (ShipAPI) target;
        // For some reason it seems like the game makes a temporary shuttle ship
        // as the source if it doesn't know where the damage is coming from
        // Ignore it here too
        if (dealer.getHullSpec().getHullId().equals("shuttlepod")) {
            return;
        }
        dealer = getBaseShip(dealer);
        receiver = getBaseShip(receiver);
        // If any base ship is null, damage data can't be processed
        if (dealer == null || receiver == null) {
            return;
        }
        // Ignore friendly fire.
        // Also ignore owner = 100 sources or target. 
        // Even though the listener seems to always give ships, ship husks
        // once destroyed count as "neutral", and so contact damage
        // with a husk counts as neutral damage. We need to ignore this.
        // Thus we only care about where one ship has owner=1 and
        // the other has owner=0, which happens iff the sum of owners
        // is 1.
        if (dealer.getOwner() + receiver.getOwner() != 1) {
            return;
        }
        float[] damageList = new float[3];
        damageList[0] = damage.getDamageToHull();
        damageList[1] = damage.getTotalDamageToArmor();
        damageList[2] = damage.getDamageToShields();
        // Add the damage info to either enemyDealtBy or enemyDealtTo
        if (receiver.getOwner() == 0) {
            addToDamageTable(damageDealtByEnemy, dealer.getId(), receiver.getId(), damageList);
        }
        else {
            addToDamageTable(damageReceivedByEnemy, receiver.getId(), dealer.getId(), damageList);
        }
    }

    /** Adds [damage] to table[enemyShipId][playerShipId],
     *  placing new tables where necessary. */
    private void addToDamageTable(Map<String, Map<String, float[]>> table, String enemyShipId, String playerShipId, float[] damage) {
        Map<String, float[]> subTable = table.get(enemyShipId);
        if (subTable == null) {
            subTable = new HashMap<>();
            table.put(enemyShipId, subTable);
        }
        float[] oldDamage = subTable.get(playerShipId);
        if (oldDamage == null) {
            subTable.put(playerShipId, damage);
        }
        else {
            oldDamage[0] += damage[0];
            oldDamage[1] += damage[1];
            oldDamage[2] += damage[2];
        }
    }

    /** Takes damage data for the last combat interval and converts
     *  it into contributions for each ship in the player's fleet. */
    private void performCombatUpdate() {
        for (String enemyId : enemyShips) {
            // Ignore ships that aren't base ships (so fighters, modules, etc.)
            if (!enemyId.equals(getBaseShipId(enemyId))) {
                continue;
            } 
            // Give the player ship that dealt the most hull/armor damage the ATTACK contribution
            Map<String, float[]> damageDealtByPlayer = damageReceivedByEnemy.get(enemyId);
            String attackWinner = addContributionToMostDamage(
                enemyId, damageDealtByPlayer, hullAndArmor, ContributionType.ATTACK);
            // Give the player ship that took the most hull/armor/shield damage the DEFENSE contribution,
            // but exclude the ship that already received the ATTACK contribution.
            Map<String, float[]> damageReceivedByPlayer = damageDealtByEnemy.get(enemyId);
            String defenseWinner = addContributionToMostDamage(
                enemyId, damageReceivedByPlayer, hullArmorShield, ContributionType.DEFENSE);
            // SUPPORT contribution only counts if the ship in question took hull or armor damage
            if (attackWinner == null) {
                continue;
            }
            // For every other player ship that took or received damage from this ship,
            // add SUPPORT contribution
            for (String shipId : playerShips) {
                // Ignore the ATTACK and DEFENSE winners
                if (shipId.equals(attackWinner) || shipId.equals(defenseWinner)) {
                    continue;
                }
                float damageDealt, damageTaken = 0f;
                float[] rawDamageDealt = damageDealtByPlayer.get(shipId);
                damageDealt = rawDamageDealt == null ? 0f : hullArmorShield.combine(rawDamageDealt);
                if (damageReceivedByPlayer != null) {
                    float[] rawDamageTaken = damageReceivedByPlayer.get(shipId);
                    damageTaken = rawDamageTaken == null ? 0f : hullArmorShield.combine(rawDamageTaken);
                }
                addContribution(enemyId, shipId, damageDealt + damageTaken, ContributionType.SUPPORT);
            }
        }
        // Reset the raw damage tables for the next cycle
        damageDealtByEnemy.clear();
        damageReceivedByEnemy.clear();
    }

    /** Find the ship id with the highest damage in [damageMap]; then, use that damage
     *  to add contribution of [type] to that ship id for [enemyShip]. */
    private String addContributionToMostDamage(String enemyShip, Map<String, float[]> damageMap, DamageCombiner combiner, ContributionType type) {
        if (damageMap == null) {
            return null;
        }
        String winner = null;
        float winnerDamage = 0f;
        for (Map.Entry<String, float[]> entry : damageMap.entrySet()) {
            String shipId = entry.getKey();
            float damage = combiner.combine(entry.getValue());
            if (damage > winnerDamage) {
                winner = shipId;
                winnerDamage = damage;
            }
        }
        addContribution(enemyShip, winner, winnerDamage, type);
        return winner;
    }

    /** Adds damageToContribution([amount]) to totalContribution[type][enemyShip][playerShip],
     *  creating new tables if necessary. */
    private void addContribution(String enemyShip, String playerShip, float rawDmg, ContributionType type) {
        if (playerShip == null) {
            return;
        }
        float contribAmt = damageToContribution(rawDmg, type);
        // Don't waste space for 0 contribution entries
        if (contribAmt <= 0f) {
            return;
        }
        Map<String, Map<String, Float>> contribution = totalContribution.get(type);
        Map<String, Float> contribForEnemy = contribution.get(enemyShip);
        if (contribForEnemy == null) {
            contribForEnemy = new HashMap<>();
            contribution.put(enemyShip, contribForEnemy);
        }
        Float curAmt = contribForEnemy.get(playerShip);
        contribForEnemy.put(playerShip, curAmt == null ? contribAmt : contribAmt + curAmt);
    }

    /** Turns raw damage into contribution amount. Generally substantially
     *  less than the raw damage, in order to balance dealing a lot of damage
     *  in a short time with dealing less damage over a longer period of time. */
    private float damageToContribution(float damage, ContributionType contribType) {
        if (damage <= 0f) {
            return 0f;
        }
        switch (contribType) {
            case ATTACK: return damage;
            case DEFENSE: return (float) Math.sqrt(damage);
            // Support uses log, so it highly favors
            // doing or taking small amounts of damage over a long time
            // over doing or taking a large amount of damage over a short time
            case SUPPORT: return (float) Math.log(damage + 1f);
            default: return 0f;
        }
    }

    /** Records damage taken and passes the data back to the main
     *  combat plugin. */
    public static class ProgSModDamageListener implements DamageListener {

        private final ContributionTracker handler;
    
        public ProgSModDamageListener(ContributionTracker handler) {
            this.handler = handler;
        }
    
        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI damage) {
            handler.registerDamage(source, target, damage);
        }
    }

    /** Combines hull, armor, and shield damage into a single term. */
    private interface DamageCombiner {
        /** Assumes order is: hull, then armor, then shield damage. */
        float combine(float[] damages);
    }

    private static class HullAndArmorCombiner implements DamageCombiner {
        @Override
        public float combine(float[] damages) {
            return damages[0] + damages[1];
        }
    }

    private static class HullArmorShieldCombiner implements DamageCombiner {
        @Override
        public float combine(float[] damages) {
            return damages[0] + damages[1] + damages[2];
        }
    }

    // private void checkDamageTables() {
    //     Logger logger = Global.getLogger(getClass());
    //     for (Map.Entry<String, Map<String, float[]>> entry : damageReceivedByEnemy.entrySet()) {
    //         String enemy = entry.getKey();
    //         if (!baseShipTable.containsKey(enemy)) {
    //             logger.info("Unknown enemy (damageReceivedByEnemy)");
    //         }
    //         else if (baseShipTable.get(enemy).getOwner() == 0) {
    //             logger.info("Player where enemy should be (damageReceivedByEnemy)");
    //         }
    //         for (Map.Entry<String, float[]> entry2 : entry.getValue().entrySet()) {
    //             String player = entry2.getKey();
    //             if (!baseShipTable.containsKey(player)) {
    //                 logger.info("Unknown player (damageReceivedByEnemy)");
    //             }
    //             else if (baseShipTable.get(player).getOwner() == 1) {
    //                 logger.info("Enemy where player should be (damageReceivedByEnemy)");
    //             }
    //         }
    //     }
    //     for (Map.Entry<String, Map<String, float[]>> entry : damageDealtByEnemy.entrySet()) {
    //         String enemy = entry.getKey();
    //         if (!baseShipTable.containsKey(enemy)) {
    //             logger.info("Unknown enemy (damageDealtByEnemy)");
    //         }
    //         else if (baseShipTable.get(enemy).getOwner() == 0) {
    //             logger.info("Player where enemy should be (damageDealtByEnemy)");
    //         }
    //         for (Map.Entry<String, float[]> entry2 : entry.getValue().entrySet()) {
    //             String player = entry2.getKey();
    //             if (!baseShipTable.containsKey(player)) {
    //                 logger.info("Unknown player (damageDealtByEnemy)");
    //             }
    //             else if (baseShipTable.get(player).getOwner() == 1) {
    //                 logger.info("Enemy where player should be (damageDealtByEnemy)");
    //             }
    //         }
    //     }
    // }

    // private void checkContribTable() {
    //     Logger logger = Global.getLogger(getClass());
    //     for (Map.Entry<ContributionType, Map<String, Map<String, Float>>> entry : totalContribution.entrySet()) {
    //         for (Map.Entry<String, Map<String, Float>> entry2 : entry.getValue().entrySet()) {
    //             String enemy = entry2.getKey();
    //             if (!baseShipTable.containsKey(enemy)) {
    //                 logger.info("Unknown enemy (contrib)");
    //             }
    //             else if (baseShipTable.get(enemy).getOwner() == 0) {
    //                 logger.info("Player where enemy should be (contrib)");
    //             }
    //             for (Map.Entry<String, Float> entry3 : entry2.getValue().entrySet()) {
    //                 String player = entry3.getKey();
    //                 if (!baseShipTable.containsKey(player)) {
    //                     logger.info("Unknown player (contrib)");
    //                 }
    //                 else if (baseShipTable.get(player).getOwner() == 1) {
    //                     logger.info("Enemy where player should be (contrib)");
    //                 }
    //             }
    //         }
    //     }
    // }
}
