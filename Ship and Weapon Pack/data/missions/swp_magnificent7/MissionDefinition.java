package data.missions.swp_magnificent7;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static final String ENEMY_FACTION_ID = Factions.PIRATES;	// Factions.LUDDIC_PATH;
    public static final int ENEMY_FLEET_SIZE = 160;
    protected static final List<String> FACTIONS = new ArrayList<>();
    protected FactionAPI currentFaction = null;
    protected WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker();

    protected static void initFactions() {
        FACTIONS.clear();
        FACTIONS.add(Factions.HEGEMONY);
        FACTIONS.add(Factions.TRITACHYON);
        FACTIONS.add(Factions.INDEPENDENT);
        FACTIONS.add(Factions.PERSEAN);
        addFactionIfExists("interstellarimperium");
        addFactionIfExists("blackrock_driveyards");
        addFactionIfExists("exigency");
        addFactionIfExists("shadow_industry");
        addFactionIfExists("pack");
        addFactionIfExists("tiandong");
        addFactionIfExists("diableavionics");
        addFactionIfExists("SCY");
        addFactionIfExists("ORA");
        addFactionIfExists("dassault_mikoyan");
        addFactionIfExists("Coalition");	// Tyrador
        addFactionIfExists("pbc");
        addFactionIfExists("neutrinocorp");
        //addFactionIfExists("sylphon");
    }

    protected static void addFactionIfExists(String factionId) {
        if (Global.getSector().getFaction(factionId) != null) {
            FACTIONS.add(factionId);
        }
    }

    protected String getRole(String type) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        switch (type) {
            case "cruiser" -> {
                picker.add(ShipRoles.COMBAT_LARGE, 15);
                picker.add(ShipRoles.CARRIER_MEDIUM, 3);
            }
            case "destroyer" -> {
                return ShipRoles.COMBAT_MEDIUM;
            }
            case "frigate" -> {
                return ShipRoles.COMBAT_SMALL;
            }
            case "carrier" -> {
                return ShipRoles.CARRIER_SMALL;
            }
        }
        return picker.pick();
    }

    protected String getRandomFaction() {
        if (factionPicker.isEmpty()) {
            factionPicker.addAll(FACTIONS);
        }
        return factionPicker.pickAndRemove();
    }

    protected String getPlayerShip(String type) {
        String variantId = null;
        for (int tries = 0; tries < 100; tries++) {
            currentFaction = Global.getSector().getFaction(getRandomFaction());
            List<ShipRolePick> picks = currentFaction.pickShip(getRole(type), ShipPickParams.all());
            if (!picks.isEmpty()) {
                variantId = picks.get(0).variantId;
            } else {
                continue;
            }
            if (!CodexDataV2.hasUnlockedEntryForShip(CodexDataV2.getBaseHullId(Global.getSettings().getVariant(variantId).getHullSpec()))) {
                continue;
            }
            if (!type.equals("cruiser") || Global.getSettings().getVariant(variantId).getHullSize() == HullSize.CRUISER) {
                return variantId;
            }
        }
        return variantId;
    }

    protected void addPlayerShip(MissionDefinitionAPI api, String type, String name, String personality, boolean isFlagship) {
        String variantId = getPlayerShip(type);
        FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, variantId, FleetMemberType.SHIP, name, isFlagship);
        if (isFlagship) {
            api.defeatOnShipLoss(name);
        }
        // officers
        int level = 5;
        if (isFlagship) {
            level = 7;
        }
        SkillPickPreference pref = FleetFactoryV3.getSkillPrefForShip(member);
        PersonAPI officer = OfficerManagerEvent.createOfficer(currentFaction, level, pref, true, null, true, true, 1, new Random());
        if (isFlagship) {
            officer.getName().setFirst("Izanami");
            officer.getName().setLast("Mifune");
            officer.getName().setGender(FullName.Gender.FEMALE);
            officer.setPortraitSprite("graphics/portraits/portrait_league00.png");
        }
        officer.setFaction(Factions.INDEPENDENT);
        officer.setPersonality(personality);
        member.setCaptain(officer);
        float maxCR = member.getRepairTracker().getMaxCR();
        member.getRepairTracker().setCR(maxCR);
    }

    protected void addPlayerShip(MissionDefinitionAPI api, String type, String name, String personality) {
        addPlayerShip(api, type, name, personality, false);
    }

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        initFactions();

        api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Seven brave samurai");
        api.setFleetTagline(FleetSide.ENEMY, "Pirate Pillager Fleet");

        api.addBriefingItem("Defeat the attackers");
        api.addBriefingItem("The ISS Yojimbo must survive");
        //api.addBriefingItem("All friendly captains are highly skilled");
        api.addBriefingItem("Click mission to re-roll ships (more available with different mods)");

        addPlayerShip(api, "cruiser", "ISS Yojimbo", "steady", true);
        addPlayerShip(api, "cruiser", "ISS Palisade", "aggressive");
        addPlayerShip(api, "destroyer", "ISS True Love Kiss", "steady");
        addPlayerShip(api, "destroyer", "ISS Aloysha Popovich", "steady");
        addPlayerShip(api, "frigate", "ISS Sound of Silence", "aggressive");
        addPlayerShip(api, "frigate", "ISS Defiant", "steady");
        addPlayerShip(api, "carrier", "ISS Maia", "cautious");

        final FleetParamsV3 params = new FleetParamsV3(null,
                new Vector2f(0, 0),
                ENEMY_FACTION_ID,
                0.8f, // qualityOverride
                "missionFleet",
                ENEMY_FLEET_SIZE, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f); // qualityMod
        params.withOfficers = false;
        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
        p.quality = 0.8f;
        p.seed = MathUtils.getRandom().nextLong();
        p.mode = ShipPickMode.PRIORITY_THEN_ALL;
        p.allWeapons = true;
        p.factionId = ENEMY_FACTION_ID;

        DefaultFleetInflater inflater = new DefaultFleetInflater(p);
        inflater.inflate(fleet);

        for (FleetMemberAPI member : fleet.getFleetData().getMembersInPriorityOrder()) {
            api.addFleetMember(FleetSide.ENEMY, member);
        }

        float width = 16000f;
        float height = 14000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        for (int i = 0; i < 8; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(width * 0.4f, -height * 0.2f, "nav_buoy");
        api.addObjective(-width * 0.4f, height * 0.15f, "sensor_array");
        api.addObjective(0, 0, "comm_relay");

        api.addAsteroidField(0f, 0f, 45f, width, 20f, 70f, 250);

        // Nomios
        api.addPlanet(0, 0, 250f, "frozen", 350f, true);
    }
}
