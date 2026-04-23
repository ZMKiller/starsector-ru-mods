package org.amazigh.foundry.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Script to generate "arkTech" ships in random orbits throughout the sector
 */
public class ASF_arkTechSpawnPlugin {
    public static final Logger LOGGER = Global.getLogger(ASF_arkTechSpawnPlugin.class);
    // --- Settings --- //

    //List of ships to spawn and their count
    private static final List<Pair<String, Integer>> SHIP_SPAWNS = new ArrayList<>();
    static {
        SHIP_SPAWNS.add(new Pair<>("A_S-F_giganberg_ark", 1));
        SHIP_SPAWNS.add(new Pair<>("A_S-F_grandum_ark", 1));
        SHIP_SPAWNS.add(new Pair<>("A_S-F_gaderoga_ark", 2));
        SHIP_SPAWNS.add(new Pair<>("A_S-F_genbura_ark", 2));
        SHIP_SPAWNS.add(new Pair<>("A_S-F_gathima_ark", 3));
        SHIP_SPAWNS.add(new Pair<>("A_S-F_gatorbacker_ark", 3));
        SHIP_SPAWNS.add(new Pair<>("A_S-F_galsteel_ark", 3));
    }

    //Systems that can never get a ship spawned in them
    public static final List<String> BLACKLISTED_SYSTEMS = new ArrayList<>();
    static {
        BLACKLISTED_SYSTEMS.add("spookysecretsystem_omega");
    }

    //Systems with any of these tags can never get a ship spawned in them
    public static final List<String> BLACKLISTED_SYSTEM_TAGS = new ArrayList<>();
    static {
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_main");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_secondary");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_no_fleets");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_destroyed");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_suppressed");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_resurgent");
    }

    //Weights for the different types of locations our ships can spawn in
    private static final LinkedHashMap<LocationType, Float> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put(LocationType.GAS_GIANT_ORBIT, 3f);
        WEIGHTS.put(LocationType.IN_ASTEROID_BELT, 5f);
        WEIGHTS.put(LocationType.IN_ASTEROID_FIELD, 5f);
        WEIGHTS.put(LocationType.STAR_ORBIT, 1f);
        WEIGHTS.put(LocationType.IN_SMALL_NEBULA, 3f);
        WEIGHTS.put(LocationType.NEAR_STAR, 2f);
        WEIGHTS.put(LocationType.JUMP_ORBIT, 2f);
    }

    // Functions

    /**
     * Spawns all the ships into the sector: should be run once on sector generation
     * @param sector the sector to spawn the ships in
     */
    public static void spawnArkTech(SectorAPI sector) {
        for (Pair<String, Integer> spawnData : SHIP_SPAWNS) {
            int numberOfSpawns = 0;
            while (numberOfSpawns < spawnData.two) {
                //Continue until we've found a place to spawn
                BaseThemeGenerator.EntityLocation placeToSpawn = null;
                StarSystemAPI system = null;
                while (placeToSpawn == null) {
                    system = getRandomSystemWithBlacklist(BLACKLISTED_SYSTEMS, BLACKLISTED_SYSTEM_TAGS, sector);
                    if (system == null) {
                        //We've somehow blacklisted every system in the sector: just don't spawn anything
                        return;
                    }

                    //Gets a list of random locations in the system, and picks one
                    WeightedRandomPicker<BaseThemeGenerator.EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, 50f, WEIGHTS);
                    placeToSpawn = validPoints.pick();
                }

                //Now, simply spawn the ship in the spawn location
                //boolean recoverable = true;
                
                float recoverChance = 10f + (Global.getSettings().getMissionScore("ASF_arkDefenders") * 0.9f);
                boolean recoverable = MathUtils.getRandomNumberInRange(0f, 100f) < recoverChance;
                // Do better at the mission to make the ships more likely to be recoverable lol! (and be in better condition on average!)
                
                float condition = MathUtils.getRandomNumberInRange(0f, 75f) + (Global.getSettings().getMissionScore("ASF_arkDefenders") * 0.75f);
                ShipRecoverySpecial.ShipCondition shipCondition;
                if (condition < 30) {
                    shipCondition = ShipRecoverySpecial.ShipCondition.WRECKED;
                } else if (condition < 60) {
                    shipCondition = ShipRecoverySpecial.ShipCondition.BATTERED;
                } else if (condition < 95) {
                    shipCondition = ShipRecoverySpecial.ShipCondition.AVERAGE;
                } else if (condition < 130) {
                    shipCondition = ShipRecoverySpecial.ShipCondition.GOOD;
                } else {
                    shipCondition = ShipRecoverySpecial.ShipCondition.PRISTINE;
                }
                
                addDerelict(system, spawnData.one, placeToSpawn.orbit, shipCondition, recoverable, null);
                LOGGER.info("Spawned " + spawnData.one + " Derelict in " + system.getId());
                LOGGER.debug("Spawned " + spawnData.one + "  Derelict in " + system.getId());
                
                numberOfSpawns++;
            }
        }
    }


    /**
     *  Utility function for getting a random system, with blacklist functionality in case some systems really shouldn't
     *  be included.
     *
     *  @param blacklist A list of all the systems we are forbidden from picking
     *  @param tagBlacklist A list of all the system tags that prevent a system from being picked
     *  @param sector The SectorAPI to check for systems in
     **/
    private static StarSystemAPI getRandomSystemWithBlacklist(List<String> blacklist, List<String> tagBlacklist, SectorAPI sector) {
        //First, get all the valid systems and put them in a separate list
        List<StarSystemAPI> validSystems = new ArrayList<>();
        for (StarSystemAPI system : sector.getStarSystems()) {
            if (blacklist.contains(system.getId())) {
                continue;
            }
            boolean isValid = true;
            for (String bannedTag : tagBlacklist) {
                if (system.hasTag(bannedTag)) {
                    isValid = false;
                    break;
                }
            }

            if (system.getStar() == null || !Misc.getMarketsInLocation(system).isEmpty() || system.getConstellation() == null) {
                isValid = false;
            }

            if (isValid) {
                validSystems.add(system);
            }
        }

        //If that list is empty, return null
        if (validSystems.isEmpty()) {
            return null;
        }

        //Otherwise, get a random element in it and return that
        else {
            int rand = MathUtils.getRandomNumberInRange(0, validSystems.size()-1);
            return validSystems.get(rand);
        }
    }


    //Mini-function for generating derelicts
    private static SectorEntityToken addDerelict(StarSystemAPI system, String variantId, OrbitAPI orbit,
                                                ShipRecoverySpecial.ShipCondition condition, boolean recoverable,
                                                 @Nullable DefenderDataOverride defenders) {

        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        ship.setOrbit(orbit);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        if (defenders != null) {
            Misc.setDefenderOverride(ship, defenders);
        }
        return ship;
    }
}
