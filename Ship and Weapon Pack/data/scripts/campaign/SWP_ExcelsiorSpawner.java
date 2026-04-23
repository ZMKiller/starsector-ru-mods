package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;

/* Adapted from LOA */
public class SWP_ExcelsiorSpawner {

    private static final Logger LOG = Global.getLogger(SWP_ExcelsiorSpawner.class);

    private static final boolean DEBUG = false;

    public static final Set<String> BLACKLISTED_SYSTEMS = new HashSet<>();

    public static final Set<String> BLACKLISTED_SYSTEM_TAGS = new HashSet<>();

    static {
        BLACKLISTED_SYSTEM_TAGS.add(Tags.THEME_HIDDEN);
        BLACKLISTED_SYSTEM_TAGS.add(Tags.THEME_REMNANT_RESURGENT);
        BLACKLISTED_SYSTEM_TAGS.add(Tags.THEME_CORE);
        BLACKLISTED_SYSTEM_TAGS.add(Tags.SYSTEM_ALREADY_USED_FOR_STORY);
        BLACKLISTED_SYSTEM_TAGS.add("ix_fortified");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_main");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_secondary");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_no_fleets");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_destroyed");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_suppressed");
        BLACKLISTED_SYSTEM_TAGS.add("theme_breakers_resurgent");
    }

    private static final LinkedHashMap<LocationType, Float> WEIGHTS = new LinkedHashMap<>();

    static {
        WEIGHTS.put(LocationType.GAS_GIANT_ORBIT, 1f);
        WEIGHTS.put(LocationType.IN_ASTEROID_BELT, 1f);
        WEIGHTS.put(LocationType.IN_ASTEROID_FIELD, 3f);
        WEIGHTS.put(LocationType.IN_RING, 1f);
        WEIGHTS.put(LocationType.IN_SMALL_NEBULA, 1f);
        WEIGHTS.put(LocationType.L_POINT, 2f);
        WEIGHTS.put(LocationType.OUTER_SYSTEM, 2f);
    }

    public static void spawnExcelsior(SectorAPI sector) {
        LOG.setLevel(Level.INFO);

        EntityLocation placeToSpawn = null;
        StarSystemAPI system = null;
        while (placeToSpawn == null) {
            system = getRandomSystemWithBlacklist(BLACKLISTED_SYSTEMS, BLACKLISTED_SYSTEM_TAGS, sector);
            if (system == null) {
                return;
            }

            WeightedRandomPicker<EntityLocation> validPoints = BaseThemeGenerator.getLocations(new Random(), system, 50f, WEIGHTS);
            placeToSpawn = validPoints.pick();
        }

        PerShipData shipData = new PerShipData(Global.getSettings().getVariant("swp_excelsior_eli"), ShipCondition.WRECKED, "TTS Aperioris", Factions.TRITACHYON, 0f);
        shipData.nameAlwaysKnown = true;
        DefenderDataOverride defenders = new DefenderDataOverride(Factions.OMEGA, 1f, 40f, 40f, 2);
        defenders.minDefenderSize = 2;
        SectorEntityToken excelsior = addDerelict(system, shipData, placeToSpawn.orbit, true, defenders);
        excelsior.setDiscoverable(true);
        excelsior.removeTag(Tags.NEUTRINO_HIGH);
        excelsior.removeTag(Tags.NEUTRINO);
        excelsior.addTag(Tags.NEUTRINO_LOW);
        excelsior.setName("Drifting Ship");
        excelsior.getDetectedRangeMod().modifyMult("SWP_ExcelsiorSpawner", 0.5f);
    }

    private static StarSystemAPI getRandomSystemWithBlacklist(Set<String> blacklist, Set<String> tagBlacklist, SectorAPI sector) {
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
            if ((system.getStar() == null)
                    && (system.getType().equals(StarSystemGenerator.StarSystemType.SINGLE)
                    || system.getType().equals(StarSystemGenerator.StarSystemType.BINARY_FAR)
                    || system.getType().equals(StarSystemGenerator.StarSystemType.TRINARY_2FAR))) {
                continue;
            }

            if (isValid) {
                validSystems.add(system);
            }
        }

        if (validSystems.isEmpty()) {
            return null;
        } else {
            int rand = MathUtils.getRandomNumberInRange(0, validSystems.size() - 1);
            return validSystems.get(rand);
        }
    }

    private static SectorEntityToken addDerelict(StarSystemAPI system, PerShipData shipData, OrbitAPI orbit,
            boolean recoverable, DefenderDataOverride defenders) {
        DerelictShipData params = new DerelictShipData(shipData, false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        ship.setOrbit(orbit);

        if (recoverable) {
            ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        if (defenders != null) {
            Misc.setDefenderOverride(ship, defenders);
        }

        if (DEBUG) {
            LOG.info("Added to " + system);
        }
        return ship;
    }
}
