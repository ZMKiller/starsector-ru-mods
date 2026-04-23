package data.missions.swp_tester;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.loading.RoleEntryAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class MissionDefinition implements MissionDefinitionPlugin {

    private static final Set<String> ALWAYS_LAST = new HashSet<>(10);

    public static final Comparator<FleetMemberAPI> PRIORITY = (FleetMemberAPI member1, FleetMemberAPI member2) -> {
        if (ALWAYS_LAST.contains(member1.getHullId()) && !ALWAYS_LAST.contains(member2.getHullId())) {
            return 1;
        } else if (!ALWAYS_LAST.contains(member1.getHullId()) && ALWAYS_LAST.contains(member2.getHullId())) {
            return -1;
        }
        float wt1 = member1.getStats().getSuppliesPerMonth().getBaseValue() + member1.getMinCrew() / 100f;
        float wt2 = member2.getStats().getSuppliesPerMonth().getBaseValue() + member2.getMinCrew() / 100f;
        if (Float.compare(wt2, wt1) == 0) {
            if (member1.getHullSpec().getHullName().compareTo(member2.getHullSpec().getHullName()) != 0) {
                return member1.getHullSpec().getHullName().compareTo(member2.getHullSpec().getHullName());
            } else {
                return member1.getId().compareTo(member2.getId());
            }
        } else {
            return Float.compare(wt2, wt1);
        }
    };

    private static final List<String> FACTIONS = new ArrayList<>(28);
    private static final List<String> ROLES = new ArrayList<>(29);

    static {
        FACTIONS.add(Factions.HEGEMONY);
        FACTIONS.add(Factions.DIKTAT);
        FACTIONS.add(Factions.INDEPENDENT);
        FACTIONS.add(Factions.KOL);
        FACTIONS.add(Factions.LIONS_GUARD);
        FACTIONS.add(Factions.LUDDIC_CHURCH);
        FACTIONS.add(Factions.LUDDIC_PATH);
        FACTIONS.add(Factions.PIRATES);
        FACTIONS.add(Factions.TRITACHYON);
        FACTIONS.add(Factions.PERSEAN);
        FACTIONS.add(Factions.DERELICT);
        FACTIONS.add(Factions.REMNANTS);
        FACTIONS.add(Factions.OMEGA);
        FACTIONS.add(Factions.DWELLER);
        //FACTIONS.add(Factions.THREAT);
        FACTIONS.add("cabal");
        FACTIONS.add("interstellarimperium");
        FACTIONS.add("blackrock_driveyards");
        FACTIONS.add("exigency");
        FACTIONS.add("exipirated");
        FACTIONS.add("templars");
        FACTIONS.add("shadow_industry");
        FACTIONS.add("junk_pirates");
        FACTIONS.add("pack");
        FACTIONS.add("syndicate_asp");
        FACTIONS.add("SCY");
        FACTIONS.add("tiandong");
        FACTIONS.add("diableavionics");
        FACTIONS.add("ORA");
        FACTIONS.add("Coalition");
        FACTIONS.add("domain");
        FACTIONS.add("sector");
        FACTIONS.add("everything");

        ROLES.add(ShipRoles.COMBAT_SMALL);
        ROLES.add(ShipRoles.COMBAT_MEDIUM);
        ROLES.add(ShipRoles.COMBAT_LARGE);
        ROLES.add(ShipRoles.COMBAT_CAPITAL);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_SMALL);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_MEDIUM);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_LARGE);
        ROLES.add(ShipRoles.CIV_RANDOM);
        ROLES.add(ShipRoles.CARRIER_SMALL);
        ROLES.add(ShipRoles.CARRIER_MEDIUM);
        ROLES.add(ShipRoles.CARRIER_LARGE);
        ROLES.add(ShipRoles.PHASE_SMALL);
        ROLES.add(ShipRoles.PHASE_MEDIUM);
        ROLES.add(ShipRoles.PHASE_LARGE);
        ROLES.add(ShipRoles.PHASE_CAPITAL);
        ROLES.add(ShipRoles.FREIGHTER_SMALL);
        ROLES.add(ShipRoles.FREIGHTER_MEDIUM);
        ROLES.add(ShipRoles.FREIGHTER_LARGE);
        ROLES.add(ShipRoles.TANKER_SMALL);
        ROLES.add(ShipRoles.TANKER_MEDIUM);
        ROLES.add(ShipRoles.TANKER_LARGE);
        ROLES.add(ShipRoles.PERSONNEL_SMALL);
        ROLES.add(ShipRoles.PERSONNEL_MEDIUM);
        ROLES.add(ShipRoles.PERSONNEL_LARGE);
        ROLES.add(ShipRoles.LINER_SMALL);
        ROLES.add(ShipRoles.LINER_MEDIUM);
        ROLES.add(ShipRoles.LINER_LARGE);
        ROLES.add(ShipRoles.TUG);
        ROLES.add(ShipRoles.CRIG);
        ROLES.add(ShipRoles.UTILITY);
        ROLES.add("miningSmall");
        ROLES.add("miningMedium");
        ROLES.add("miningLarge");

        ALWAYS_LAST.add("swp_arcade_hyperzero");
        ALWAYS_LAST.add("swp_arcade_superzero");
        ALWAYS_LAST.add("swp_arcade_zero");
        ALWAYS_LAST.add("swp_arcade_cristarium");
        ALWAYS_LAST.add("swp_arcade_ezekiel");
        ALWAYS_LAST.add("swp_arcade_zeus");
        ALWAYS_LAST.add("swp_arcade_archangel");
        ALWAYS_LAST.add("swp_arcade_ultron");
        ALWAYS_LAST.add("swp_arcade_oberon");
        ALWAYS_LAST.add("swp_arcade_superhyperion");
        ALWAYS_LAST.add("ii_boss_dominus");
        ALWAYS_LAST.add("msp_boss_potniaBis");
        ALWAYS_LAST.add("ms_boss_charybdis");
        ALWAYS_LAST.add("ms_boss_mimir");
        ALWAYS_LAST.add("tem_boss_paladin");
        ALWAYS_LAST.add("tem_boss_archbishop");
        ALWAYS_LAST.add("swp_boss_phaeton");
        ALWAYS_LAST.add("swp_boss_hammerhead");
        ALWAYS_LAST.add("swp_boss_sunder");
        ALWAYS_LAST.add("swp_boss_tarsus");
        ALWAYS_LAST.add("swp_boss_medusa");
        ALWAYS_LAST.add("swp_boss_falcon");
        ALWAYS_LAST.add("swp_boss_paragon");
        ALWAYS_LAST.add("swp_boss_mule");
        ALWAYS_LAST.add("swp_boss_aurora");
        ALWAYS_LAST.add("swp_boss_odyssey");
        ALWAYS_LAST.add("swp_boss_atlas");
        ALWAYS_LAST.add("swp_boss_afflictor");
        ALWAYS_LAST.add("swp_boss_brawler");
        ALWAYS_LAST.add("swp_boss_cerberus");
        ALWAYS_LAST.add("swp_boss_dominator");
        ALWAYS_LAST.add("swp_boss_doom");
        ALWAYS_LAST.add("swp_boss_euryale");
        ALWAYS_LAST.add("swp_boss_lasher_b");
        ALWAYS_LAST.add("swp_boss_lasher_r");
        ALWAYS_LAST.add("swp_boss_onslaught");
        ALWAYS_LAST.add("swp_boss_shade");
        ALWAYS_LAST.add("swp_boss_eagle");
        ALWAYS_LAST.add("swp_boss_beholder");
        ALWAYS_LAST.add("swp_boss_dominator_luddic_path");
        ALWAYS_LAST.add("swp_boss_onslaught_luddic_path");
        ALWAYS_LAST.add("swp_boss_conquest");
        ALWAYS_LAST.add("swp_boss_frankenstein");
        ALWAYS_LAST.add("swp_boss_sporeship");
        ALWAYS_LAST.add("uw_boss_astral");
        ALWAYS_LAST.add("uw_boss_cancer");
        ALWAYS_LAST.add("uw_boss_corruption");
        ALWAYS_LAST.add("uw_boss_cyst");
        ALWAYS_LAST.add("uw_boss_disease");
        ALWAYS_LAST.add("uw_boss_malignancy");
        ALWAYS_LAST.add("uw_boss_metastasis");
        ALWAYS_LAST.add("uw_boss_pustule");
        ALWAYS_LAST.add("uw_boss_tumor");
        ALWAYS_LAST.add("uw_boss_ulcer");
        ALWAYS_LAST.add("tiandong_boss_wuzhang");
        ALWAYS_LAST.add("pack_bulldog_bullseye");
        ALWAYS_LAST.add("pack_pitbull_bullseye");
        ALWAYS_LAST.add("pack_komondor_bullseye");
        ALWAYS_LAST.add("pack_schnauzer_bullseye");
        ALWAYS_LAST.add("diableavionics_IBBgulf");
        ALWAYS_LAST.add("TSC_Onslaught");
        ALWAYS_LAST.add("sun_ice_ihs");
        ALWAYS_LAST.add("FOB_boss_rast");
        ALWAYS_LAST.add("tahlan_vestige");
        ALWAYS_LAST.add("loamtp_macnamara_boss");
        ALWAYS_LAST.add("uw_palace");
    }

    private final Set<String> allShips = new HashSet<>(300);
    private final Set<FleetMemberAPI> ships = new TreeSet<>(PRIORITY);

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        Set<String> variants = new HashSet<>(5000);
        for (String role : ROLES) {
            for (String faction : FACTIONS) {
                List<RoleEntryAPI> roleEntries;
                try {
                    roleEntries = Global.getSettings().getEntriesForRole(faction, role);
                } catch (Exception e) {
                    continue;
                }
                for (RoleEntryAPI roleEntry : roleEntries) {
                    variants.add(roleEntry.getVariantId());
                }
            }
            List<RoleEntryAPI> roleEntries = Global.getSettings().getDefaultEntriesForRole(role);
            for (RoleEntryAPI roleEntry : roleEntries) {
                variants.add(roleEntry.getVariantId());
            }
        }

        for (String variant : variants) {
            addShip(variant);
        }

        if (Global.getSettings().isDevMode()) {
            addShip("swp_arcade_hyperzero_Hull");
            addShip("swp_arcade_superzero_Hull");
            addShip("swp_arcade_zero_Hull");
            addShip("swp_arcade_cristarium_Hull");
            addShip("swp_arcade_ezekiel_Hull");
            addShip("swp_arcade_zeus_Hull");
            //addShip("swp_arcade_archangel_Hull");
            addShip("swp_arcade_ultron_Hull");
            addShip("swp_arcade_oberon_Hull");
            addShip("swp_arcade_superhyperion_Hull");
            //addShip("swp_banana_Hull");
            addShip("ii_boss_dominus_Hull");
            addShip("msp_boss_potniaBis_Hull");
            addShip("ms_boss_charybdis_Hull");
            addShip("ms_boss_mimir_Hull");
            addShip("tem_boss_paladin_Hull");
            addShip("tem_boss_archbishop_Hull");
            addShip("swp_boss_phaeton_Hull");
            addShip("swp_boss_hammerhead_Hull");
            addShip("swp_boss_sunder_Hull");
            addShip("swp_boss_tarsus_Hull");
            addShip("swp_boss_medusa_Hull");
            addShip("swp_boss_falcon_Hull");
            addShip("swp_boss_paragon_Hull");
            addShip("swp_boss_mule_Hull");
            addShip("swp_boss_aurora_Hull");
            addShip("swp_boss_odyssey_Hull");
            addShip("swp_boss_atlas_Hull");
            addShip("swp_boss_afflictor_Hull");
            addShip("swp_boss_brawler_Hull");
            addShip("swp_boss_cerberus_Hull");
            addShip("swp_boss_dominator_Hull");
            addShip("swp_boss_doom_Hull");
            addShip("swp_boss_euryale_Hull");
            addShip("swp_boss_lasher_b_Hull");
            addShip("swp_boss_lasher_r_Hull");
            addShip("swp_boss_onslaught_Hull");
            addShip("swp_boss_shade_Hull");
            addShip("swp_boss_eagle_Hull");
            addShip("swp_boss_beholder_Hull");
            addShip("swp_boss_dominator_luddic_path_Hull");
            addShip("swp_boss_onslaught_luddic_path_Hull");
            addShip("swp_boss_conquest_Hull");
            addShip("swp_boss_frankenstein_Hull");
            addShip("swp_boss_sporeship_cus");
            addShip("uw_boss_astral_Hull");
            addShip("uw_boss_cancer_Hull");
            addShip("uw_boss_corruption_Hull");
            addShip("uw_boss_cyst_Hull");
            addShip("uw_boss_disease_Hull");
            addShip("uw_boss_malignancy_Hull");
            addShip("uw_boss_metastasis_Hull");
            addShip("uw_boss_pustule_Hull");
            addShip("uw_boss_tumor_Hull");
            addShip("uw_boss_ulcer_Hull");
            addShip("tiandong_boss_wuzhang_Hull");
            addShip("pack_bulldog_bullseye_Hull");
            addShip("pack_pitbull_bullseye_Hull");
            addShip("pack_komondor_bullseye_Hull");
            addShip("pack_schnauzer_bullseye_Hull");
            addShip("diableavionics_IBBgulf_Hull");
            addShip("TSC_Onslaught_Hull");
            addShip("sun_ice_ihs_Hull");
            addShip("FOB_boss_rast_Hull");
            addShip("tahlan_vestige_Hull");
            addShip("loamtp_macnamara_boss_Hull");
            addShip("uw_palace_gra");
        }

        api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true, 5);

        api.setFleetTagline(FleetSide.PLAYER, "Your forces");
        api.setFleetTagline(FleetSide.ENEMY, "Enemy forces");

        api.addBriefingItem("Defeat all enemy forces");

        generateFleet(FleetSide.PLAYER, ships, api);
        generateFleet(FleetSide.ENEMY, ships, api);

        float width = 24000f;
        float height = 18000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        for (int i = 0; i < 50; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(minX + width * 0.25f + 2000, minY + height * 0.25f + 2000, "nav_buoy");
        api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.25f + 2000, "comm_relay");
        api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.75f - 2000, "nav_buoy");
        api.addObjective(minX + width * 0.25f + 2000, minY + height * 0.75f - 2000, "comm_relay");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");

        String[] planets = {"barren", "terran", "gas_giant", "ice_giant", "cryovolcanic", "frozen", "jungle", "desert",
            "arid"};
        String planet = planets[(int) (Math.random() * planets.length)];
        float radius = 100f + (float) Math.random() * 150f;
        api.addPlanet(0, 0, radius, planet, 200f, true);

        ships.clear();
    }

    private void addShip(String variant) {
        boolean added = allShips.add(variant);
        if (added) {
            try {
                FleetMemberAPI member;
                if (variant.endsWith("_wing")) {
                    member = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, variant);
                } else {
                    member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
                }
                if (Global.getSettings().isDevMode() || (!member.getHullSpec().getTags().contains(Tags.RESTRICTED)
                        && !member.getHullSpec().getTags().contains(Tags.MONSTER)
                        && CodexDataV2.hasUnlockedEntryForShip(CodexDataV2.getBaseHullId(member.getHullSpec())))) {
                    ships.add(member);
                }
            } catch (Exception ex) {
            }
        }
    }

    private void generateFleet(FleetSide side, Set<FleetMemberAPI> ships, MissionDefinitionAPI api) {
        Set<String> hulls = new HashSet<>(ships.size());
        Set<ModuleShip> moduleShips = new HashSet<>(ships.size() / 50);
        for (FleetMemberAPI ship : ships) {
            try {
                if (ship.isFighterWing()) {
                    if (hulls.contains(ship.getSpecId())) {
                        continue;
                    } else {
                        hulls.add(ship.getSpecId());
                    }
                    api.addToFleet(side, ship.getSpecId(), FleetMemberType.FIGHTER_WING, false);
                } else {
                    if (!ship.getVariant().getStationModules().isEmpty()) {
                        if (hulls.contains(ship.getVariant().getHullVariantId())) {
                            continue;
                        } else {
                            hulls.add(ship.getVariant().getHullVariantId());
                            ModuleShip moduleShip = new ModuleShip(ship.getVariant());
                            if (moduleShips.contains(moduleShip)) {
                                continue;
                            } else {
                                moduleShips.add(moduleShip);
                            }
                        }
                    } else {
                        if (hulls.contains(ship.getHullId())) {
                            continue;
                        } else {
                            hulls.add(ship.getHullId());
                        }
                    }
                    api.addToFleet(side, ship.getVariant().getHullVariantId(), FleetMemberType.SHIP, false);
                }
            } catch (Exception ex) {
            }
        }
    }

    private static class ModuleShip {

        final ShipVariantAPI variant;

        ModuleShip(ShipVariantAPI variant) {
            this.variant = variant;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ModuleShip other) {
                if (!variant.getHullSpec().getHullId().contentEquals(other.variant.getHullSpec().getHullId())) {
                    return false;
                }
                if (variant.getStationModules().size() != other.variant.getStationModules().size()) {
                    return false;
                }
                for (Entry<String, String> entry : variant.getStationModules().entrySet()) {
                    String key = entry.getKey();
                    String value1 = entry.getValue();
                    String value2 = other.variant.getStationModules().get(key);
                    if (value2 == null) {
                        return false;
                    }
                    if (!value1.contentEquals(value2)) {
                        return false;
                    }
                }
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5 * Objects.hashCode(this.variant.getHullSpec().getHullId());
            for (Entry<String, String> entry : variant.getStationModules().entrySet()) {
                hash += 3 * Objects.hashCode(entry.getKey());
                hash += 7 * Objects.hashCode(entry.getValue());
            }
            return hash;
        }
    }
}
